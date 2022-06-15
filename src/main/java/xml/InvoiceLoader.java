package xml;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InvoiceLoader {
    ODatabaseSession db;

    public InvoiceLoader(ODatabaseSession db){
        this.db = db;
    }

    public void load() throws ParserConfigurationException, IOException, SAXException, ParseException {

        if (db.getClass("Invoice") == null) {
            OClass invoice = db.createVertexClass("Invoice");
            invoice.createProperty("orderId", OType.STRING);
            invoice.createProperty("personId", OType.STRING);
            invoice.createProperty("orderDate", OType.DATE);
            invoice.createProperty("price", OType.FLOAT);
            //product.createProperty("imgUrl", OType.STRING);
            invoice.createIndex("invoice_orderId_index", OClass.INDEX_TYPE.UNIQUE, "orderId");
        }
        if (db.getClass("OrderlineInvoice")== null) {
            OClass OrderlineInvoice = db.createEdgeClass("OrderlineInvoice");
            OrderlineInvoice.createProperty("productId", OType.STRING);
            OrderlineInvoice.createIndex("orderlineInvoice_productId_index", OClass.INDEX_TYPE.NOTUNIQUE, "productId");
        }

        //Get Document Builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        //Build Document
        Document document = builder.parse(new File("DATA/Invoice/Invoice.xml"));

        //Normalize the XML Structure; It's just too important !!
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();

        //Get all invoices
        NodeList nList = document.getElementsByTagName("Invoice.xml");

        List<List<String>> records = new ArrayList<List<String>>();

        for (int temp = 0; temp < nList.getLength(); temp++)
        {
            Node node = nList.item(temp);
            //System.out.println("");    //Just a separator
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element eElement = (Element) node;


                String orderid = eElement.getElementsByTagName("OrderId").item(0).getTextContent();
                //System.out.println("Order id : "  + orderid);

                String personid = eElement.getElementsByTagName("PersonId").item(0).getTextContent();
                //System.out.println("Person id : "  + personid );

                String orderdate = eElement.getElementsByTagName("OrderDate").item(0).getTextContent();
                //System.out.println("Order date : "  + orderdate);

                String totalprice = eElement.getElementsByTagName("TotalPrice").item(0).getTextContent();
                //System.out.println("Total price : "  + totalprice);


                NodeList orderLineList = eElement.getElementsByTagName("Orderline");//.getElementByTagName("").item(0).getTextContent();

                for (int j = 0; j < orderLineList.getLength(); j++) {

                    Node nodeOL = orderLineList.item(j);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElementOL = (Element) node;
                        /*String productid = eElementOL.getElementsByTagName("productId").item(0).getTextContent();
                        System.out.println("Order line - product id: " + productid);

                        String asin = eElementOL.getElementsByTagName("asin").item(0).getTextContent();
                        //System.out.println("Order line - asin: " + asin);

                        String title = eElementOL.getElementsByTagName("title").item(0).getTextContent();
                        //System.out.println("Order line - title: " + title);

                        String price = eElementOL.getElementsByTagName("price").item(0).getTextContent();
                        //System.out.println("Order line - price: " + price);

                        String brand = eElementOL.getElementsByTagName("brand").item(0).getTextContent();
                        //System.out.println("Order line - brand: " + brand);
                        */
                        String productid = eElementOL.getElementsByTagName("productId").item(0).getTextContent();
                        String asin = eElementOL.getElementsByTagName("asin").item(0).getTextContent();

                        String[] values = {orderid, personid, orderdate, totalprice,asin, productid}; // productid, asin, title, price, brand};
                        records.add(Arrays.asList(values));
                    }
                }
            }
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        for(int p=0; p<records.size(); p++){
            // We check if the product already exists before adding it
            String query = "SELECT * from Invoice where orderId = ?";
            OResultSet rs = db.query(query, records.get(p).get(0));
            if(rs.elementStream().count()==0) {
                //System.out.println(records.get(p).get(1));
                OVertex invoice = createInvoice(db,
                        records.get(p).get(0),
                        records.get(p).get(1),
                        format.parse(records.get(p).get(2)),
                        Float.parseFloat(records.get(p).get(3))
                        );
                linkInvoiceToProduct(this.db, invoice,records.get(p).get(4),records.get(p).get(5));
            }
            rs.close();
        }

        System.out.println("The invoices have been loaded");

        //db.close();
    }

    private static OVertex createInvoice(ODatabaseSession db,
                                         String orderId,
                                         String personId,
                                         Date orderDate,
                                         float price) {
        OVertex result = db.newVertex("Invoice");
        result.setProperty("orderId", orderId);
        result.setProperty("personId", personId);
        result.setProperty("orderDate", orderDate);
        result.setProperty("price", price);
        result.save();
        return result;
    }

    private static OEdge linkInvoiceToProduct(ODatabaseSession db, OVertex invoice, String asin, String productId){
        String query = "SELECT * from Product where asin = ?";
        OResultSet rsp = db.query(query, asin);
        OEdge result = null;
        if(rsp.hasNext()){
            Optional<OVertex> optional = rsp.next().getVertex();
            rsp.close();
            if(optional.isPresent()){
                OVertex product = optional.get();
                result = db.newEdge(invoice, product, db.getClass("OrderlineInvoice"));
                result.save();
            }
        }
        return result;
    }

    /* --------------------------------------- */
    /* -- METHODS INSERT, UPDATE AND DELETE -- */
    /* --------------------------------------- */

    public static void insertOneInvoice(ODatabaseSession db, ODocument doc) {

        String orderId = doc.getProperty("orderId");
        String personId = doc.getProperty("personId");
        Date orderDate =  doc.getProperty("orderDate");
        float price =  doc.getProperty("price");
        String asin =   doc.getProperty("asin");
        String productId =  doc.getProperty("productId");

        String query = "SELECT * from Invoice where Invoice = ?";
        OResultSet rs = db.query(query, orderId);
        if (!rs.elementStream().findFirst().isPresent()) {

            OVertex invoice = createInvoice(db, orderId, personId, orderDate, price);
            invoice.getEdges(ODirection.OUT);
            linkInvoiceToProduct(db,invoice,asin,productId);
            invoice.save();
            System.out.println("The invoice n°" + orderId + " has been inserted");
        } else {
            System.out.println("The invoice n°" + orderId + " is already present among the vendor vertices");
        }
    }


    public static void updateOneInvoice(ODatabaseSession db, ODocument doc) {


        String orderId = doc.getProperty("orderId");
        String personId = doc.getProperty("personId");
        Date orderDate =  doc.getProperty("orderDate");
        float price =  doc.getProperty("price");

        String query = "SELECT * from Invoice where Invoice = ?";
        OResultSet rs = db.query(query, orderId);
        Optional invoiceRes = rs.elementStream().findFirst();
        if (invoiceRes.isPresent()) {
            OVertex invoiceVertex = (OVertex) invoiceRes.get();
            if (invoiceVertex.getProperty("orderId") != orderId) {
                invoiceVertex.setProperty("orderId", orderId);
            }
            if (invoiceVertex.getProperty("personId") != personId) {
                invoiceVertex.setProperty("personId", personId);
            }
            if (invoiceVertex.getProperty("orderDate") != orderDate) {
                invoiceVertex.setProperty("orderDate", orderDate);
            }
            if ((float) invoiceVertex.getProperty("price") != price) {
                invoiceVertex.setProperty("price", price);
            }
            /*OVertex invoice = createInvoice(db, orderId, personId, orderDate, price);
            for (OEdge e : invoiceVertex.getEdges(ODirection.OUT)) {
                String asin = e.getVertex(ODirection.OUT).getProperty("asin");
                linkInvoiceToProduct(db,invoice,asin,productId);
                db.delete(e);
            };
            db.delete(invoiceVertex);
            */
            invoiceVertex.save();
            System.out.println("The invoice n°" + orderId + " has been updated");
        } else {
            System.out.println("The invoice n°" + orderId + " is not present");
        }
    }


    public static void deleteOneInvoice(ODatabaseSession db, ODocument doc) {

        String orderId = doc.getProperty("orderId");
        String personId = doc.getProperty("personId");
        Date orderDate =  doc.getProperty("orderDate");
        float price =  doc.getProperty("price");
        String asin =   doc.getProperty("asin");
        String productId =  doc.getProperty("productId");


        String query = "SELECT * from Invoice where orderId = ?";
        OResultSet rs = db.query(query, orderId);
        Optional invoiceRes = rs.elementStream().findFirst();
        if (invoiceRes.isPresent()) {
            db.delete((OVertex) invoiceRes.get());
            System.out.println("The invoice n°" + orderId + " has been deleted");
        } else {
            System.out.println("The invoice n°" + orderId + " is already not present.");
        }
    }



    /* ------------------------------------------------------- */
    /* -- METHODS INSERT, UPDATE AND DELETE FOR MANY VALUES -- */
    /* ------------------------------------------------------- */
    public static void insertManyInvoice(ODatabaseSession db, List<ODocument> docs){
        for(ODocument document : docs){
            insertOneInvoice(db, document);
        }
    }

    public static void updateManyInvoice(ODatabaseSession db, List<ODocument> docs){
        for(ODocument document : docs){
            updateOneInvoice(db, document);
        }
    }

    public static void deleteManyInvoice(ODatabaseSession db, List<ODocument> docs){
        for(ODocument document : docs){
            deleteOneInvoice(db, document);
        }
    }

    public void tests(ODatabaseSession db){
        InvoiceLoader invoiceLoader = new InvoiceLoader(db);

        ODocument doc = new ODocument("Invoice");
        doc.setProperty("orderId","123");
        doc.setProperty("personId","007");
        doc.setProperty("orderDate","2019-03-07");
        doc.setProperty("price","400");

        ODocument doc2 = new ODocument("Invoice");
        doc2.setProperty("orderId","000");
        doc2.setProperty("personId","734");
        doc2.setProperty("orderDate","2009-01-07");
        doc2.setProperty("price","700");

        List<ODocument> docs = new ArrayList<ODocument>();
        docs.add(doc);
        docs.add(doc2);


        invoiceLoader.insertOneInvoice(db,doc);
        invoiceLoader.deleteOneInvoice(db,doc);

        invoiceLoader.insertManyInvoice(db,docs);
        invoiceLoader.deleteManyInvoice(db,docs);
    }
}
