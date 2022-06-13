package xml;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class InvoiceLoader {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, ParseException {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        // Replace the arguments with your own database name and user/password
        ODatabaseSession db = orientDB.open("testdb", "root", "2610");


        if (db.getClass("Invoice") == null) {
            OClass invoice = db.createVertexClass("Invoice");
            invoice.createProperty("orderId", OType.STRING);
            invoice.createProperty("personId", OType.STRING);
            invoice.createProperty("orderDate", OType.DATE);
            invoice.createProperty("price", OType.FLOAT);
            //product.createProperty("imgUrl", OType.STRING);
            invoice.createIndex("invoice_orderId_index", OClass.INDEX_TYPE.UNIQUE, "orderId");

        }

        //Get Document Builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        //Build Document
        Document document = builder.parse(new File("DATA/Invoice/Invoice.xml"));

        //Normalize the XML Structure; It's just too important !!
        document.getDocumentElement().normalize();

        //Here comes the root node
        Element root = document.getDocumentElement();
        //System.out.println(root.getNodeName());

        //Get all invoices
        NodeList nList = document.getElementsByTagName("Invoice.xml");
        //System.out.println("============================");


        List<List<String>> records = new ArrayList<List<String>>();

        for (int temp = 0; temp < nList.getLength(); temp++)
        {
            Node node = nList.item(temp);
            //System.out.println("");    //Just a separator
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                //Print each invoice's detail
                Element eElement = (Element) node;
                //System.out.println(temp);



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
                        String productid = eElementOL.getElementsByTagName("productId").item(0).getTextContent();
                        //System.out.println("Order line - product id: " + productid);

                        String asin = eElementOL.getElementsByTagName("asin").item(0).getTextContent();
                        //System.out.println("Order line - asin: " + asin);

                        String title = eElementOL.getElementsByTagName("title").item(0).getTextContent();
                        //System.out.println("Order line - title: " + title);

                        String price = eElementOL.getElementsByTagName("price").item(0).getTextContent();
                        //System.out.println("Order line - price: " + price);

                        String brand = eElementOL.getElementsByTagName("brand").item(0).getTextContent();
                        //System.out.println("Order line - brand: " + brand);

                        //System.out.println(j);
                        String[] values = {orderid, personid, orderdate, totalprice, productid, asin, title, price, brand};
                        records.add(Arrays.asList(values));
                    }
                }

                //String[] values = {orderid, personid, orderdate, totalprice};//, productid, asin, title, price, brand};
                //records.add(Arrays.asList(values));

            }
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        for(int p=0; p<records.size(); p++){
            // We check if the product already exists before adding it
            String query = "SELECT * from Invoice where orderId = ?";
            OResultSet rs = db.query(query, records.get(p).get(0));
            if(rs.elementStream().count()==0) {
                System.out.println(records.get(p).get(1));
                OVertex obj = createInvoice(db,
                        records.get(p).get(0),
                        records.get(p).get(1),
                        format.parse(records.get(p).get(2)),
                        Float.parseFloat(records.get(p).get(3))
                        );
            }
        }

        db.close();
        orientDB.close();
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
}
