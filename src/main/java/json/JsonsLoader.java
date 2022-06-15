package json;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

public class JsonsLoader {
    ODatabaseSession db;

    public JsonsLoader(ODatabaseSession db){
        this.db = db;
    }

    public void load() throws IOException, ParseException {
        if (this.db.getClass("Product") == null) {
            OClass product = db.createVertexClass("Product");
            product.createProperty("asin", OType.STRING);
            product.createProperty("title", OType.STRING);
            product.createProperty("price", OType.FLOAT);
            product.createProperty("imgUrl", OType.STRING);
            product.createIndex("product_asin_index", OClass.INDEX_TYPE.UNIQUE, "asin");
        }
        if (this.db.getClass("Order") == null) {
            OClass product = db.createVertexClass("Order");
            product.createProperty("OrderId", OType.STRING);
            product.createProperty("PersonId", OType.STRING);
            product.createProperty("OrderDate", OType.STRING);
            product.createProperty("TotalPrice", OType.FLOAT);
            product.createIndex("order_id_index", OClass.INDEX_TYPE.UNIQUE, "OrderId");
        }
        if (this.db.getClass("IsFromBrand")== null) {
            OClass IsFromBrand = db.createEdgeClass("IsFromBrand");
            IsFromBrand.createProperty("asin", OType.STRING);
            IsFromBrand.createIndex("IsFromBrand_asin_index", OClass.INDEX_TYPE.UNIQUE, "asin");
        }
        if (this.db.getClass("Orderline")== null) {
            OClass Orderline = db.createEdgeClass("Orderline");
            Orderline.createProperty("productId", OType.STRING);
            Orderline.createIndex("orderline_productId_index", OClass.INDEX_TYPE.NOTUNIQUE, "productId");
        }
        if (this.db.getClass("HasInvoice")== null) {
            OClass HasInvoice = db.createEdgeClass("HasInvoice");
            HasInvoice.createProperty("OrderId", OType.STRING);
            HasInvoice.createIndex("HasInvoice_OrderId_index", OClass.INDEX_TYPE.UNIQUE, "OrderId");
        }

        // Loading the csv product into a list of list of String
        List<List<String>> productRecords = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/Product/Product.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                productRecords.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Line 0 only contains the columns names, so we start at line 1
        for(int p=1; p<productRecords.size(); p++){
            // We check if the product already exists before adding it
            String query = "SELECT * from Product where asin = ?";
            OResultSet rs = this.db.query(query, productRecords.get(p).get(0));
            if(rs.elementStream().count()==0) {
                createProduct(this.db, productRecords.get(p).get(0), productRecords.get(p).get(1), Float.parseFloat(productRecords.get(p).get(2)), productRecords.get(p).get(0));
            }
            rs.close();
        }

        // The data in Order.json is malformed, so we need to modify it.
        // We'll add a '[' at the very beginning, a ',' at the end of each line, and a ']' at the very end.
        BufferedReader inputStream = null;
        PrintWriter outputStream = null;
        try {
            inputStream = new BufferedReader(new FileReader("DATA/Order/Order.json"));
            outputStream = new PrintWriter(new FileWriter("DATA/Order/OrderParsed.json"));
            String l;
            int cpt = 0;
            while ((l = inputStream.readLine()) != null) {
                if(cpt==0){outputStream.println("["+l+",");}
                else if(cpt==142256){
                    outputStream.println(l + "]");
                } else {
                    outputStream.println(l + ",");
                }
                cpt++;
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }


        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader("DATA/Order/OrderParsed.json");
        Object obj = jsonParser.parse(reader);
        JSONArray orderlist = (JSONArray) obj;
        for(int o=0;o<orderlist.size(); o++){
            // We check if the order already exists before adding it
            String query = "SELECT * from Order where OrderId = ?";
            OResultSet rs = this.db.query(query, (String)((JSONObject)orderlist.get(o)).get("OrderId"));
            if(rs.elementStream().count()==0) {
                OVertex order = createOrder(this.db, (String)((JSONObject)orderlist.get(o)).get("OrderId"), (String)((JSONObject)orderlist.get(o)).get("PersonId"), (String)((JSONObject)orderlist.get(o)).get("OrderDate"), (Double)((JSONObject)orderlist.get(o)).get("TotalPrice"));
                // We add the orderlines for this order
                JSONArray orderlines = (JSONArray)((JSONObject)orderlist.get(o)).get("Orderline");
                for(int l=0;l<orderlines.size();l++){
                    linkOrderToProduct(this.db, order, (String)((JSONObject)orderlines.get(l)).get("asin"), (String)((JSONObject)orderlines.get(l)).get("productId"));
                }
            }
            rs.close();
        }
        System.out.println("The Jsons have been loaded");

    }

    public void createOutEdges(){
        System.out.println("DANS EDGES");
        // Loading the csv BrandByProduct into a list of list of String
        List<List<String>> brandRecords = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/Product/BrandByProduct.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                brandRecords.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for(int p=0; p<brandRecords.size(); p++){
            linkProductToBrand(this.db, brandRecords.get(p).get(0), brandRecords.get(p).get(1));
        }

        // Linking Orders to Invoices
        /*
        String query = "SELECT * from Order order by OrderId";
        OResultSet rso = this.db.query(query);
        String queryi = "SELECT * from Invoice order by orderId";
        OResultSet rsi = this.db.query(queryi);




        while(rso.hasNext() && rsi.hasNext()){
            Optional<OVertex> optional_order = rso.next().getVertex();
            Optional<OVertex> optional_invoice = rsi.next().getVertex();
            if(optional_order.isPresent() && optional_invoice.isPresent()) {
                OVertex order = optional_order.get();
                OVertex invoice = optional_invoice.get();
                OEdge result = db.newEdge(order, invoice, db.getClass("HasInvoice"));
                result.save();
            }
        }
        rso.close();
        rsi.close();

         */

        /*
        String query = "SELECT * from Order";
        OResultSet rs = this.db.query(query);
        while(rs.hasNext()){
            Optional<OVertex> optional = rs.next().getVertex();
            if(optional.isPresent()) {
                OVertex order = optional.get();
                String queryedge = "SELECT * from HasInvoice where OrderId = ?";
                OResultSet rsedge = this.db.query(queryedge, (String)order.getProperty("OrderId"));

                if(!rsedge.hasNext()) {
                    rsedge.close();
                    String queryi = "SELECT * from Invoice where OrderId = ?";
                    OResultSet rsi = this.db.query(queryi, (String) order.getProperty("OrderId"));
                    if (rsi.elementStream().count() > 1) {
                        System.out.println("UN ORDER PEUT AVOIR PLUSIEURS INVOICES");
                    }
                    if (rsi.hasNext()) {
                        System.out.println("LALALA");
                        Optional<OVertex> optionali = rsi.next().getVertex();
                        rsi.close();
                        if (optionali.isPresent()) {
                            OVertex invoice = optional.get();
                            OEdge result = db.newEdge(order, invoice, db.getClass("HasInvoice"));
                            result.setProperty("OrderId", (String) order.getProperty("OrderId"));
                            result.save();
                        }
                    }
                }

            }

        }
        rs.close();

         */


    }

    private static OVertex createProduct(ODatabaseSession db, String asin, String title, float price, String imgUrl) {
        OVertex result = db.newVertex("Product");
        result.setProperty("asin", asin);
        result.setProperty("title", title);
        result.setProperty("price", price);
        result.setProperty("imgUrl", imgUrl);
        result.save();
        return result;
    }

    private static OVertex createOrder(ODatabaseSession db, String OrderId, String PersonId, String OrderDate, Double TotalPrice) {
        OVertex result = db.newVertex("Order");
        result.setProperty("OrderId", OrderId);
        result.setProperty("PersonId", PersonId);
        result.setProperty("OrderDate", OrderDate);
        result.setProperty("TotalPrice", TotalPrice.floatValue());
        result.save();
        return result;
    }

    private static OEdge linkProductToBrand(ODatabaseSession db, String bname, String asin){
        OEdge result = null;
        String queryedge = "SELECT * from IsFromBrand where asin = ?";
        OResultSet rse = db.query(queryedge, asin);
        if(!rse.hasNext()) {
            String query = "SELECT * from VendorVertex where Vendor = ?";
            OResultSet rsb = db.query(query, bname);

            if (rsb.hasNext()) {
                Optional<OVertex> optional = rsb.next().getVertex();
                rsb.close();
                if (optional.isPresent()) {
                    OVertex Brand = optional.get();

                    query = "SELECT * from Product where asin = ?";
                    OResultSet rsp = db.query(query, asin);
                    if (rsp.hasNext()) {
                        optional = rsp.next().getVertex();
                        rsp.close();
                        if (optional.isPresent()) {
                            OVertex Product = optional.get();
                            result = db.newEdge(Product, Brand, db.getClass("IsFromBrand"));
                            result.setProperty("asin", asin);
                            result.save();
                        }
                    }
                }
            }
        }
        return result;
    }

    private static OEdge linkOrderToProduct(ODatabaseSession db, OVertex order, String asin, String productId){
        String query = "SELECT * from Product where asin = ?";
        OResultSet rsp = db.query(query, asin);
        OEdge result = null;
        if(rsp.hasNext()){
            Optional<OVertex> optional = rsp.next().getVertex();
            rsp.close();
            if(optional.isPresent()){
                OVertex product = optional.get();
                result = db.newEdge(order, product, db.getClass("Orderline"));
                result.save();
            }
        }
        return result;
    }



    public static void insertOneOrder(ODatabaseSession db, ODocument doc) {

        ORecord r = doc.getRecord();
        String s = r.toJSON();
        JSONObject json = (JSONObject) JSONValue.parse(s);

        String orderId = (String) json.get("OrderId");
        String personId = (String) json.get("PersonId");
        String orderDate = (String) json.get("OrderDate");
        Double price = (Double) json.get("Price");
        String asin = (String) json.get("asin");
        String productId = (String) json.get("ProductId");

        String query = "SELECT * from Order where OrderId = ?";
        OResultSet rs = db.query(query, orderId);
        if (!rs.elementStream().findFirst().isPresent()) {
            rs.close();
            OVertex order = createOrder(db, orderId, personId, orderDate, price);
            query = "SELECT * from Product where asin = ?";
            OResultSet rsp = db.query(query, asin);
            if (rsp.elementStream().findFirst().isPresent()) {
                Optional<OVertex> optional = rsp.next().getVertex();
                if(optional.isPresent()) {
                    OVertex product = optional.get();
                    rsp.close();
                    OEdge edge = db.newEdge(order, product, db.getClass("Orderline"));
                    edge.setProperty("ProductId", productId);
                    edge.save();
                }
            }
            order.save();
            System.out.println("The Order " + orderId + " has been inserted");
        } else {
            System.out.println("The Order " + orderId + " is already present among the Order vertices");
        }
    }


    public static void updateOrder(ODatabaseSession db, ODocument doc) {
        ORecord r = doc.getRecord();
        String s = r.toJSON();
        JSONObject json = (JSONObject) JSONValue.parse(s);

        String orderId = (String) json.get("OrderId");
        String personId = (String) json.get("PersonId");
        String orderDate = (String) json.get("OrderDate");
        Double price = (Double) json.get("Price");

        String query = "SELECT * from Order where OrderId = ?";
        OResultSet rs = db.query(query, orderId);

        if (rs.elementStream().findFirst().isPresent()) {
            Optional<OVertex> optional = rs.next().getVertex();
            if (optional.isPresent()) {
                OVertex order = optional.get();
                order.setProperty("PersonId", personId);
                order.setProperty("OrderDate", orderDate);
                order.setProperty("Price", price.floatValue());
                System.out.println("The Order " + orderId + " has been updated");
            }
        } else {
            System.out.println("The Order " + orderId + " is not present");
        }
        rs.close();
    }

    public static void deleteOneOrder(ODatabaseSession db, ODocument doc) {

        ORecord r = doc.getRecord();
        String s = r.toJSON();
        JSONObject json = (JSONObject) JSONValue.parse(s);

        String orderId = (String) json.get("OrderId");

        String query = "SELECT * from Order where OrderId = ?";
        OResultSet rs = db.query(query, orderId);

        if (rs.elementStream().findFirst().isPresent()) {
            Optional<OVertex> optional = rs.next().getVertex();
            if (optional.isPresent()) {
                OVertex order = optional.get();
                db.delete(order);
                System.out.println("The Order " + orderId + " has been deleted");
            } else {
                System.out.println("The Order " + orderId + " is not present.");
            }
        }
    }





}
