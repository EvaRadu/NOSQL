import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import graph.GraphLoader;
import json.JsonsLoader;
import relational.CustomerLoader;
import relational.VendorLoader;
import xml.InvoiceLoader;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {
    //REQUEST 1
        /*
        For a given customer, find his/her all related data including profile, orders, invoices,
        feedback, comments, and posts in the last month, return the category in which he/she has
        bought the largest number of products, and return the tag which he/she has engaged the
        greatest times in the posts.
        */
    public void request1(ODatabaseSession db, String id, Date date) {

        String queryCust = "SELECT * from Customer where id = ?";
        OResultSet rsCust = db.query(queryCust, id);
        Optional custRes = rsCust.elementStream().findFirst();
        if (custRes.isPresent()) {
            OVertex customerVertex = (OVertex) custRes.get();

            customerVertex.getProperty("id");
            customerVertex.getProperty("firstName");
            customerVertex.getProperty("lastName");
            customerVertex.getProperty("gender");
            customerVertex.getProperty("birthday");
            customerVertex.getProperty("creationDate");
            customerVertex.getProperty("locationIP");
            customerVertex.getProperty("browserUsed");
            customerVertex.getProperty("place");

            for (OEdge e : customerVertex.getEdges(ODirection.OUT, "hasCreated")) {
                if (e.getProperty("idPerson").equals(id)) {
                    OVertex post = e.getVertex(ODirection.OUT);

                    post.getProperty("idPost");
                    post.getProperty("imageFile");
                    post.getProperty("creationDate");
                    post.getProperty("locationIP");
                    post.getProperty("browserUsed");
                    post.getProperty("language");
                    post.getProperty("content");
                    post.getProperty("length");

                }
            }

        }

        String queryOrder = "SELECT * from Order where PersonId = ?";
        OResultSet rsOrder = db.query(queryOrder, id);

        while (rsOrder.hasNext()) {
            Optional<OVertex> optional = rsOrder.next().getVertex();
            if (optional.isPresent()) {
                OVertex order = optional.get();
                order.getProperty("OrderId");
                order.getProperty("PersonId");
                order.getProperty("OrderDate");
                order.getProperty("TotalPrice");
            }
        }
        rsOrder.close();

        String queryInvoice = "SELECT * from Invoice where personId = ?";
        OResultSet rsInvoice = db.query(queryInvoice, id);
        while (rsInvoice.hasNext()) {
            Optional<OVertex> optional = rsInvoice.next().getVertex();
            if (optional.isPresent()) {
                OVertex invoice = optional.get();
                invoice.getProperty("OrderId");
                invoice.getProperty("PersonId");
                invoice.getProperty("OrderDate");
                invoice.getProperty("TotalPrice");
            }
        }
        rsInvoice.close();

        String queryFeedback = "SELECT * from FeedBack where personID = ?";
        OResultSet rsFeedback = db.query(queryFeedback, id);
        while (rsFeedback.hasNext()) {
            Optional<OVertex> optional = rsFeedback.next().getVertex();
            if (optional.isPresent()) {
                OVertex invoice = optional.get();
                invoice.getProperty("comment");
            }
        }
        rsFeedback.close();

    }

    /* QUERY 2 :
       For a given product during a given period, find the people who commented or
       posted on it, and had bought it */
    public void query2(String idProduct, Date beginningDate, Date endDate) {

    }

    public static void main(String[] args) throws ParseException, IOException, org.json.simple.parser.ParseException {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        // Replace the arguments with your own database name and user/password
        // A remplacer avec le nom de la base de donnée et les identifiants
        ODatabaseSession db = orientDB.open("testdb", "root", "2610");

        // LOADING THE PRODUCT DATA
        //VendorLoader vendorLoader = new VendorLoader(db);
        //vendorLoader.load();
        //JsonsLoader jsonLoader = new JsonsLoader(db);
        //jsonLoader.load();
        //jsonLoader.createOutEdges();
        /*
        InvoiceLoader invoiceLoader = new InvoiceLoader(db);
        invoiceLoader.load();
        */


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
*/
        // LOADING THE CUSTOMER DATA
        CustomerLoader customerLoader = new CustomerLoader(db);
        //customerLoader.load();
        //customerLoader.loadEdges();
        // LOADING THE VENDOR DATA
        VendorLoader vendorLoader = new VendorLoader(db);
        //vendorLoader.load();
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
        docCustomer1.field("id", "123");
        docCustomer1.field("firstName", "Eva");
        docCustomer1.field("lastName", "Radu");
        docCustomer1.field("gender", "female");
        docCustomer1.field("birthday", "2001-02-26");
        docCustomer1.field("creationDate", "2022-06-13T02:10:23.099+0000");
        docCustomer1.field("locationIP", "27.98.237.197");
        docCustomer1.field("browserUsed", "Opera");
        docCustomer1.field("place", "2037");

        ODocument docCustomer2 = new ODocument("Customer");
        docCustomer2.field("id", "123");
        docCustomer2.field("firstName", "Eva");
        docCustomer2.field("lastName", "Radu");
        docCustomer2.field("gender", "female");
        docCustomer2.field("birthday", "2001-02-26");
        docCustomer2.field("creationDate", "2022-06-13T02:10:23.099+0000");
        docCustomer2.field("locationIP", "27.98.237.197");
        docCustomer2.field("browserUsed", "Chrome");
        docCustomer2.field("place", "2037");

        ODocument docCustomer3 = new ODocument("Customer");
        docCustomer3.field("id", "1234");
        docCustomer3.field("firstName", "Mia");
        docCustomer3.field("lastName", "Swery");
        docCustomer3.field("gender", "female");
        docCustomer3.field("birthday", "2000-04-16");
        docCustomer3.field("creationDate", "2022-06-13T02:10:23.099+0000");
        docCustomer3.field("locationIP", "20.10.458.130");
        docCustomer3.field("browserUsed", "Firefox");
        docCustomer3.field("place", "2160");

        List<ODocument> docsCustomer = new ArrayList<ODocument>();
        docsCustomer.add(docCustomer1);
        docsCustomer.add(docCustomer3);


        customerLoader.insertOneCustomer(db, docCustomer1);
        customerLoader.updateOneCustomer(db, docCustomer2);
        customerLoader.deleteOneCustomer(db, docCustomer2);

        customerLoader.insertManyCustomers(db, docsCustomer);
        customerLoader.updateManyCustomers(db, docsCustomer);
        customerLoader.deleteManyCustomers(db, docsCustomer);




        /* ------------------------ */
        /* --- TESTS 4.4 VENDORS -- */
        /* ------------------------ */

        ODocument docVendor1 = new ODocument("VendorVertex");
        docVendor1.field("Vendor", "EvaShop");
        docVendor1.field("Country", "Romania");
        docVendor1.field("Industry", "Clothes");

        ODocument docVendor2 = new ODocument("VendorVertex");
        docVendor2.field("Vendor", "EvaShop");
        docVendor2.field("Country", "Romania");
        docVendor2.field("Industry", "Sports");

        ODocument docVendor3 = new ODocument("VendorVertex");
        docVendor3.field("Vendor", "MiaShop");
        docVendor3.field("Country", "France");
        docVendor3.field("Industry", "Sports");

        List<ODocument> docsVendor = new ArrayList<ODocument>();
        docsVendor.add(docVendor2);
        docsVendor.add(docVendor3);


        vendorLoader.insertOneVendor(db, docVendor1);
        vendorLoader.updateOneVendor(db, docVendor2);
        vendorLoader.deleteOneVendor(db, docVendor3);

        vendorLoader.insertManyVendors(db, docsVendor);
        vendorLoader.updateManyVendors(db, docsVendor);
        vendorLoader.deleteManyVendors(db, docsVendor);




        /*** 4.4 Graph ****/
        GraphLoader graphLoader = new GraphLoader(db);

        /* Créer un post */
        graphLoader.createPost("1399511627255", "image.png",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-11-18 07:13:13.099+0000"),
                "43.290.55.178","Chrome", "fr", "A new post", "350");

        /* Mise à jour post */
        ODocument post = new ODocument("Post");
        post.field("idPost","1399511627255");
        post.field("imageFile","anotherImage.png");
        post.field("creationDate",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-23 09:13:13.099+0000"));
        post.field("locationIP","43.290.55.178");
        post.field("browerUsed","Opera");
        post.field("language","SP");
        post.field("content","A new post 2");
        post.field("length","890");

        graphLoader.updatePost(post);
    }


    /** Query 4 **/

}
