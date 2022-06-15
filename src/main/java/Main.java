import com.orientechnologies.common.collection.OMultiCollectionIterator;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import graph.GraphLoader;
import json.JsonsLoader;
import relational.CustomerLoader;
import relational.VendorLoader;
import xml.InvoiceLoader;

import java.io.IOException;
//import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    public static void request1(ODatabaseSession db, String id, Date date) throws ParseException {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        Date lastMonth = cal.getTime();

        String queryCust = "SELECT * from Customer where id = ?";
        OResultSet rsCust = db.query(queryCust, id);
        Optional custRes = rsCust.elementStream().findFirst();
        if (custRes.isPresent()) {
            OVertex customerVertex = (OVertex) custRes.get();
            System.out.println("========= PROFILE =========");
            System.out.println((String) customerVertex.getProperty("id"));
            System.out.println((String) customerVertex.getProperty("firstName"));
            System.out.println((String) customerVertex.getProperty("lastName"));
            System.out.println((String) customerVertex.getProperty("gender"));
            System.out.println((String) customerVertex.getProperty("birthday"));
            System.out.println((String) customerVertex.getProperty("creationDate"));
            System.out.println((String) customerVertex.getProperty("locationIP"));
            System.out.println((String) customerVertex.getProperty("browserUsed"));
            System.out.println((String) customerVertex.getProperty("place"));
            System.out.println("\n========= HasCreatedPosts =========");

            for (OEdge e : customerVertex.getEdges(ODirection.OUT, "hasCreated")) {
                if (e.getProperty("idPerson").equals(id)) {
                    OVertex post = e.getVertex(ODirection.OUT);
                    System.out.println((String) post.getProperty("idPost"));
                    System.out.println((String) post.getProperty("imageFile"));
                    System.out.println((String) post.getProperty("creationDate"));
                    System.out.println((String) post.getProperty("locationIP"));
                    System.out.println((String) post.getProperty("browserUsed"));
                    System.out.println((String) post.getProperty("language"));
                    System.out.println((String) post.getProperty("content"));
                    System.out.println((String) post.getProperty("length"));

                }
            }

        }

        String queryOrder = "SELECT * from Order where PersonId = ?";
        OResultSet rsOrder = db.query(queryOrder, id);
        System.out.println("\n========= ORDERS =========");
        while (rsOrder.hasNext()) {
            Optional<OVertex> optional = rsOrder.next().getVertex();
            if (optional.isPresent()) {
                OVertex order = optional.get();
                System.out.println((String) order.getProperty("OrderId"));
                System.out.println((String) order.getProperty("PersonId"));
                System.out.println((String) order.getProperty("OrderDate"));
                System.out.println((Float) order.getProperty("TotalPrice"));
            }
        }
        rsOrder.close();

        String queryInvoice = "SELECT * from Invoice where personId = ?";
        OResultSet rsInvoice = db.query(queryInvoice, id);
        System.out.println("\n========= INVOICES =========");
        while (rsInvoice.hasNext()) {
            Optional<OVertex> optional = rsInvoice.next().getVertex();
            if (optional.isPresent()) {
                OVertex invoice = optional.get();
                System.out.println((String) invoice.getProperty("orderId"));
                System.out.println((String) invoice.getProperty("personId"));
                System.out.println((Date) invoice.getProperty("orderDate"));
                System.out.println((Float) invoice.getProperty("price"));
            }
        }
        rsInvoice.close();

        String queryFeedback = "SELECT * from Feedback where personID = ?";
        OResultSet rsFeedback = db.query(queryFeedback, id);
        System.out.println("\n========= FEEDBACKS =========");
        while (rsFeedback.hasNext()) {
            Optional<OEdge> optional = rsFeedback.next().getEdge();
            if (optional.isPresent()) {
                OEdge feedback = optional.get();
                System.out.println((String) feedback.getProperty("comment"));
            }
        }
        rsFeedback.close();

        /*
        //Select * from Post where idPost in (Select idPost from HasCreated where idPerson=?)
        String queryHasCreated = "Select * from HasCreated where idPerson = ?";
        OResultSet rsHasCreated = db.query(queryHasCreated, id);
        while (rsHasCreated.hasNext()) {
            Optional<OEdge> optional = rsHasCreated.next().getEdge();
            if (optional.isPresent()) {
                OVertex post = optional.get().getVertex(ODirection.OUT);
                Date datePost = (Date) post.getProperty("creationDate");
                if (datePost.after(lastMonth) && datePost.before(date)) {

                    System.out.println((String) post.getProperty("imageFile"));
                    System.out.println((Date)   post.getProperty("creationDate"));
                    System.out.println((String) post.getProperty("locationIP"));
                    System.out.println((String) post.getProperty("browserUsed"));
                    System.out.println((String) post.getProperty("language"));
                    System.out.println((String) post.getProperty("content"));
                    System.out.println((Integer) post.getProperty("length"));
                    System.out.println(datePost);
                }
            }
            rsHasCreated.close();
        */

        String queryTag = "Select in.name, Count(idTag) as nbTags from HasTag where idPost in (Select idPost from HasCreated where idPerson = 4145) GROUP BY idTag ORDER BY nbTags DESC";
        OResultSet rsTag = db.query(queryTag, id);
        System.out.println("\n========= The tag which the person has engaged the greatest times in the posts =========");
        System.out.println(rsTag.stream().findFirst().get());
    }


    
    /*
     QUERY 2 :
       For a given product during a given period, find the people who commented or
       posted on it, and had bought it
     */
    public static void query2(ODatabaseSession db, String idProduct, String startDate, String endDate) throws ParseException {

        // GETTING THE ORDER DATES OF THE PRODUCT
        String queryDate = "SELECT IN(\"Orderline\").OrderDate from Product where asin = ?";
        OResultSet rsDate = db.query(queryDate, idProduct);
        ArrayList<String> dateRes = new ArrayList<>();
        while(rsDate.hasNext()) {
            OResult optional = rsDate.next();
            dateRes = optional.getProperty("IN(\"Orderline\").OrderDate");
        }


        // GETTING THE CUSTOMERS WHO ORDERED THE PRODUCT
        String queryOrder = "SELECT IN(\"Orderline\").PersonId from Product where asin = ?";
        OResultSet rsOrder = db.query(queryOrder, idProduct);
        ArrayList<String> orderRes = new ArrayList<>();
        while(rsOrder.hasNext()) {
            OResult optional = rsOrder.next();
            orderRes = optional.getProperty("IN(\"Orderline\").PersonId");
        }

        // GETTING THE CUSTOMERS WHO COMMENTED THE PRODUCT
        String queryFeedback = "select personID from Feedback where productAsin= ?";
        OResultSet rsFeedback = db.query(queryFeedback, idProduct);
        ArrayList<String> feedbackRes = new ArrayList<>();
        while(rsFeedback.hasNext()) {
            OResult optional2 = rsFeedback.next();
            feedbackRes.add(optional2.getProperty("personID").toString());
        }

        // GETTING THE CUSTOMERS WHO ORDERED THE PRODUCT IN THE PERIOD INTERVAL
        ArrayList<String> finalOrderRes = new ArrayList<>();

        for(int i = 0; i<=dateRes.size()-1;i++){
            Boolean bool = (new SimpleDateFormat("yyyy-MM-dd").parse(dateRes.get(i))).before( new SimpleDateFormat("yyyy-MM-dd").parse(endDate));
            Boolean bool2 = (new SimpleDateFormat("yyyy-MM-dd").parse(startDate).before( new SimpleDateFormat("yyyy-MM-dd").parse(dateRes.get(i))));
            if(bool&&bool2){
                finalOrderRes.add(orderRes.get(i));
            }

        }

        // GETTING THE CUSTOMERS WHO ORDERED THE PRODUCT IN THE PERIOD INTERVAL
        // AND HAD COMMENTED IT

        ArrayList<String> finalRes = new ArrayList<>();
        for(String s : feedbackRes){
            if(finalOrderRes.contains(s)){
                finalRes.add(s);
            }
        }


        // FINAL RESULTS :
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("                 CUSTOMERS WHO GAVE A FEEDBACK ON THE PRODUCT :                ");
        System.out.println("-------------------------------------------------------------------------------");

        for (String s : feedbackRes) {
            String q1 = "SELECT * from Customer where id = ?";
            OResultSet r1 = db.query(q1, s);
            System.out.println(r1.elementStream().findFirst().get());
        }

        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("           CUSTOMERS WHO ORDERED THE PRODUCT IN THE SPECIFIED PERIOD :         ");
        System.out.println("-------------------------------------------------------------------------------");

        for (String s : finalOrderRes) {
            String q1 = "SELECT * from Customer where id = ?";
            OResultSet r1 = db.query(q1, s);
            System.out.println(r1.elementStream().findFirst().get());
        }

        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("   CUSTOMERS WHO ORDERED THE PRODUCT IN THE SPECIFIED PERIOD AND COMMENTED IT  ");
        System.out.println("-------------------------------------------------------------------------------");

        for (String s : finalRes) {
            String q1 = "SELECT * from Customer where id = ?";
            OResultSet r1 = db.query(q1, s);
            System.out.println(r1.elementStream().findFirst().get());
        }
        System.out.println("-------------------------------------------------------------------------------");

    }

    /*
        Query 8 :
         For all the products of a given category during a given year, compute its total sales
         amount, and measure its popularity in the social media.
    */
    public static void query8(ODatabaseSession db) throws ParseException {
        // GETTING THE ORDER DATES OF THE PRODUCT
        String queryDate = "SELECT IN(\"Orderline\").OrderDate from Product";
        OResultSet rsDate = db.query(queryDate);
        ArrayList<String> dateRes = new ArrayList<>();
        while(rsDate.hasNext()) {
            OResult optional = rsDate.next();
            dateRes = optional.getProperty("IN(\"Orderline\").OrderDate");
        }
        System.out.println(dateRes);
    }






        public static void main(String[] args) throws ParseException, IOException, org.json.simple.parser.ParseException {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        ODatabaseSession db = orientDB.open("testdb", "root", "2610");

        /* -------------- */
        /* -- PARTIE 5 -- */
        /* -------------- */

        // QUERY 1


        request1(db, "4145", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2011-09-15 00:00:00") );
        System.out.println("done!");
        db.close();



        // QUERY 2
        //query2(db,"B005FUKW6M","2018-12-18", "2021-01-18");


            //QUERY 8
            //query8(db);



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
        /*
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

       /* ODocument docCustomer1 = new ODocument("Customer");
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

        /*
        customerLoader.insertOneCustomer(db,docCustomer1);
        customerLoader.updateOneCustomer(db,docCustomer2);
        customerLoader.deleteOneCustomer(db,docCustomer2);

        customerLoader.insertManyCustomers(db,docsCustomer);
        customerLoader.updateManyCustomers(db,docsCustomer);
        customerLoader.deleteManyCustomers(db,docsCustomer);
        */






        /* ------------------------ */
        /* --- TESTS 4.4 VENDORS -- */
        /* ------------------------ */

       /* ODocument docVendor1 = new ODocument("VendorVertex");
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

        /*
        vendorLoader.insertOneVendor(db,docVendor1);
        vendorLoader.updateOneVendor(db,docVendor2);
        vendorLoader.deleteOneVendor(db,docVendor3);

        vendorLoader.insertManyVendors(db,docsVendor);
        vendorLoader.updateManyVendors(db,docsVendor);
        vendorLoader.deleteManyVendors(db,docsVendor);
         */



        /*** 4.4 Graph ****/
        /*GraphLoader graphLoader = new GraphLoader(db);

        /* Créer un post
        graphLoader.createPost("1399511627255", "image.png",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-11-18 07:13:13.099+0000"),
                "43.290.55.178","Chrome", "fr", "A new post", "350");

         Mise à jour post
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

        graphLoader.updatePost(post);*/
    }



}
