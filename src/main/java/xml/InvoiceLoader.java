package xml;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.*;
import java.io.*;
public class InvoiceLoader {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        // Replace the arguments with your own database name and user/password
        ODatabaseSession db = orientDB.open("testdb", "root", "2610");


        if (db.getClass("Invoice") == null) {
            OClass invoice = db.createVertexClass("Invoice");
            invoice.createProperty("orderId", OType.STRING);
            invoice.createProperty("personId", OType.INTEGER);
            invoice.createProperty("orderDate", OType.DATE);
            invoice.createProperty("price", OType.FLOAT);
            //product.createProperty("imgUrl", OType.STRING);
            //invoice.createIndex("invoice_asin_index", OClass.INDEX_TYPE.UNIQUE, "asin");
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
        System.out.println(root.getNodeName());

        //Get all invoices
        NodeList nList = document.getElementsByTagName("Invoice.xml");
        System.out.println("============================");

        for (int temp = 0; temp < nList.getLength(); temp++)
        {
            Node node = nList.item(temp);
            System.out.println("");    //Just a separator
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                //Print each invoice's detail
                Element eElement = (Element) node;
                System.out.println(temp);
                System.out.println("Order id : "  + eElement.getElementsByTagName("OrderId").item(0).getTextContent());
                System.out.println("Person id : "  + eElement.getElementsByTagName("PersonId").item(0).getTextContent());
                System.out.println("Order date : "  + eElement.getElementsByTagName("OrderDate").item(0).getTextContent());
                System.out.println("Total price : "  + eElement.getElementsByTagName("TotalPrice").item(0).getTextContent());

                NodeList orderLineList = eElement.getElementsByTagName("Orderline");//.getElementByTagName("").item(0).getTextContent();
                    Node nodeOL = orderLineList.item(0);
                    if (node.getNodeType() == Node.ELEMENT_NODE)
                    {
                        Element eElementOL = (Element) node;
                        System.out.println("Order line - product id: "  + eElementOL.getElementsByTagName("productId").item(0).getTextContent());
                        System.out.println("Order line - asin: "  + eElementOL.getElementsByTagName("asin").item(0).getTextContent());
                        System.out.println("Order line - title: "  + eElementOL.getElementsByTagName("title").item(0).getTextContent());
                        System.out.println("Order line - price: "  + eElementOL.getElementsByTagName("price").item(0).getTextContent());
                        System.out.println("Order line - brand: "  + eElementOL.getElementsByTagName("brand").item(0).getTextContent());
                }
            }
        }

        db.close();
        orientDB.close();
    }

    private static OVertex createInvoice(ODatabaseSession db, String vendor, String country, String industry) {
        OVertex result = db.newVertex("InvoiceVertex");
        result.setProperty("vendor", vendor);
        result.setProperty("country", country);
        result.setProperty("industry", industry);
        result.save();
        return result;
    }
}
