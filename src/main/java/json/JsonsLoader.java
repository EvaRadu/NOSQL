package json;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
            //createOrder(this.db, (String)((JSONObject)orderlist.get(o)).get("OrderId"), (String)((JSONObject)orderlist.get(o)).get("PersonId"), (String)((JSONObject)orderlist.get(o)).get("OderDate"), 55);
            if(rs.elementStream().count()==0) {
                createOrder(this.db, (String)((JSONObject)orderlist.get(o)).get("OrderId"), (String)((JSONObject)orderlist.get(o)).get("PersonId"), (String)((JSONObject)orderlist.get(o)).get("OderDate"), (Double)((JSONObject)orderlist.get(o)).get("TotalPrice"));
            }
            rs.close();
        }

    }

    public void createOutEdges(){
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
        String query = "SELECT * from VendorVertex where Vendor = ?";
        OResultSet rsb = db.query(query, bname);
        OEdge result = null;

        if(rsb.hasNext()){
            //System.out.println("hasnext");
            Optional<OVertex> optional = rsb.next().getVertex();
            rsb.close();
            if(optional.isPresent()){
                //System.out.println("ispresent");
                OVertex Brand = optional.get();

                query = "SELECT * from Product where asin = ?";
                OResultSet rsp = db.query(query, asin);
                if(rsp.hasNext()){
                    //System.out.println("hasnext2");
                    optional = rsp.next().getVertex();
                    rsp.close();
                    if(optional.isPresent()){
                        //System.out.println("ispresent2");
                        OVertex Product = optional.get();
                        result = db.newEdge(Product, Brand, db.getClass("IsFromBrand"));
                        result.save();
                    }
                }
            }
        }
        return result;
    }


}
