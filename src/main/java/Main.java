import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import feedback.FeedbackLoader;
import json.JsonsLoader;
import org.xml.sax.SAXException;
import relational.CustomerLoader;
import relational.VendorLoader;
import xml.InvoiceLoader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) throws ParseException, ParserConfigurationException, IOException, SAXException, org.json.simple.parser.ParseException {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        // Replace the arguments with your own database name and user/password
        // A remplacer avec le nom de la base de donnée et les identifiants
        ODatabaseSession db = orientDB.open("testdb", "root", "2610");

        // LOADING THE PRODUCT DATA
        VendorLoader vendorLoader = new VendorLoader(db);
        /*
        VendorLoader vendorLoader = new VendorLoader(db);
        vendorLoader.load();
        JsonsLoader jsonLoader = new JsonsLoader(db);
        jsonLoader.load();

        InvoiceLoader invoiceLoader = new InvoiceLoader(db);
        invoiceLoader.load();
        jsonLoader.createOutEdges();
        System.out.println("EDGE OK");
        FeedbackLoader feedbackLoader = new FeedbackLoader(db);
        feedbackLoader.chargementFeedback(); */
        System.out.println("OK");
        ArrayList<String> test = query3(db, "2020-03-10", "2020-07-10", "B003D9RBMU");
        for(String t:test){
            System.out.println(t);
        }
        CustomerLoader customerLoader = new CustomerLoader(db);
        customerLoader.load();







        //FeedbackLoader.chargementFeedback(db);

        //GraphLoader.createSocialNetworkGraph(db);
        /* Exemple pour ajouter des records
        OVertex v1 = db.newVertex("Tag");
        v1.setProperty("name", "OneRF");
        v1.save();
        OVertex v2 = db.newVertex("Post");
        v2.setProperty("content", "TwoTwosSS");
        v2.save();
        v1.addEdge(v2, "HasTag").save();
        db.commit();
        */


/*
        // LOADING THE PRODUCT DATA
        JsonLoader jsonLoader = new JsonLoader(db);
        jsonLoader.load();
        // LOADING THE CUSTOMER DATA
        CustomerLoader customerLoader = new CustomerLoader(db);
        customerLoader.load();
        // LOADING THE VENDOR DATA
        VendorLoader vendorLoader = new VendorLoader(db);
        vendorLoader.load();
*/
        // exemple de création d'un document, qui sera dans GENERIC CLASS dans la BD
        // pour voir les données dans une classe, choisi la classe et après fait QUERY ALL

        /*
        ODocument doc = new ODocument("Persons");
        doc.field( "name", "Luke" );
        doc.field( "surname", "Skywalker" );
        doc.field( "city", "lalala");
        // SAVE THE DOCUMENT
        db.save(doc);
        db.commit();
        db.close();
    */

        /* ------------------------------------------------------------------------------- */
        // Tests Eva

        /* -------------------------- */
        /* --- TESTS 4.4 CUSTOMERS -- */
        /* -------------------------- */


        ODocument docCustomer1 = new ODocument("Customer");
        docCustomer1.field( "id", "123" );
        docCustomer1.field( "firstName", "Eva" );
        docCustomer1.field( "lastName", "Radu");
        docCustomer1.field( "gender", "female");
        docCustomer1.field( "birthday", "2001-02-26");
        docCustomer1.field( "creationDate", "2022-06-13T02:10:23.099+0000");
        docCustomer1.field( "locationIP", "27.98.237.197");
        docCustomer1.field( "browserUsed", "Opera");
        docCustomer1.field( "place", "2037");

        ODocument docCustomer2 = new ODocument("Customer");
        docCustomer2.field( "id", "123" );
        docCustomer2.field( "firstName", "Eva" );
        docCustomer2.field( "lastName", "Radu");
        docCustomer2.field( "gender", "female");
        docCustomer2.field( "birthday", "2001-02-26");
        docCustomer2.field( "creationDate", "2022-06-13T02:10:23.099+0000");
        docCustomer2.field( "locationIP", "27.98.237.197");
        docCustomer2.field( "browserUsed", "Chrome");
        docCustomer2.field( "place", "2037");

        ODocument docCustomer3 = new ODocument("Customer");
        docCustomer3.field( "id", "1234" );
        docCustomer3.field( "firstName", "Mia" );
        docCustomer3.field( "lastName", "Swery");
        docCustomer3.field( "gender", "female");
        docCustomer3.field( "birthday", "2000-04-16");
        docCustomer3.field( "creationDate", "2022-06-13T02:10:23.099+0000");
        docCustomer3.field( "locationIP", "20.10.458.130");
        docCustomer3.field( "browserUsed", "Firefox");
        docCustomer3.field( "place", "2160");

        List<ODocument> docsCustomer =  new ArrayList<ODocument>();
        docsCustomer.add(docCustomer1);
        docsCustomer.add(docCustomer3);

        customerLoader.insertOneCustomer(db,docCustomer1);
        customerLoader.updateOneCustomer(db,docCustomer2);
        customerLoader.deleteOneCustomer(db,docCustomer2);

        customerLoader.insertManyCustomers(db,docsCustomer);
        customerLoader.updateManyCustomers(db,docsCustomer);
        customerLoader.deleteManyCustomers(db,docsCustomer);




        /* ------------------------ */
        /* --- TESTS 4.4 VENDORS -- */
        /* ------------------------ */

        ODocument docVendor1 = new ODocument("VendorVertex");
        docVendor1.field( "Vendor", "EvaShop" );
        docVendor1.field( "Country", "Romania" );
        docVendor1.field( "Industry", "Clothes");

        ODocument docVendor2 = new ODocument("VendorVertex");
        docVendor2.field( "Vendor", "EvaShop" );
        docVendor2.field( "Country", "Romania" );
        docVendor2.field( "Industry", "Sports");

        ODocument docVendor3 = new ODocument("VendorVertex");
        docVendor3.field( "Vendor", "MiaShop" );
        docVendor3.field( "Country", "France" );
        docVendor3.field( "Industry", "Sports");

        List<ODocument> docsVendor =  new ArrayList<ODocument>();
        docsVendor.add(docVendor2);
        docsVendor.add(docVendor3);

        vendorLoader = new VendorLoader(db);

        vendorLoader.insertOneVendor(db,docVendor1);
        vendorLoader.updateOneVendor(db,docVendor2);
        vendorLoader.deleteOneVendor(db,docVendor3);

        vendorLoader.insertManyVendors(db,docsVendor);
        vendorLoader.updateManyVendors(db,docsVendor);
        vendorLoader.deleteManyVendors(db,docsVendor);


    }

    public static ArrayList<String> query3(ODatabaseSession db, String from, String to, String product_asin){
        // On retourne tous les commentaires des Feedbacks sur le produit asin avec une note en dessous de la moyenne,
        // et le contenu des Posts posté entre les dates from et to.
        ArrayList<String> res = new ArrayList<>();
        String query = "SELECT * from Feedback where productAsin = ?";
        OResultSet rs = db.query(query, product_asin);

        while(rs.hasNext()){
            Optional<OEdge> optional = rs.next().getEdge();
            if (optional.isPresent()) {
                OEdge feedback = optional.get();
                String text = (String)feedback.getProperty("comment");
                String grade = "";
                int cpt = 1;
                while(text.charAt(cpt) !=",".charAt(0)){
                    grade = grade + text.charAt(cpt);
                    cpt ++;
                }
                float sentiment = Float.parseFloat(grade);

                if(sentiment<2.5){
                    res.add(text);
                }
            }
        }
        rs.close();


        query = "SELECT * from Post where creationDate between ? and ?";
        OResultSet rs2 = db.query(query, from, to);

        while(rs2.hasNext()){
            Optional<OVertex> optional = rs2.next().getVertex();
            if (optional.isPresent()) {
                OVertex post = optional.get();
                String text = (String)post.getProperty("content");
                res.add(text);
            }
        }


        return res;
    }
}
