import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import graph.Person;
import json.JsonLoader;
import keyValue.FeedbackLoader;
import relational.CustomerLoader;
import relational.VendorLoader;

public class Main {

    public static void main(String[] args) {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        // Replace the arguments with your own database name and user/password
        // A remplacer avec le nom de la base de donnée et les identifiants
        ODatabaseSession db = orientDB.open("testdb", "root", "2610");

        if (db.getClass("Product") == null) {
            OClass product = db.createVertexClass("Product");
            product.createProperty("asin", OType.STRING);
            product.createProperty("title", OType.STRING);
            product.createProperty("price", OType.FLOAT);
            product.createProperty("imgUrl", OType.STRING);
            product.createIndex("product_asin_index", OClass.INDEX_TYPE.UNIQUE, "asin");
        }

        //Partie modèle Graph: pour mettre tous dans un seul main
        if (db.getClass("Person") == null) {
            OClass person = db.createVertexClass("Person");
            person.createProperty("id", OType.INTEGER);
            person.createProperty("firstName", OType.STRING);
            person.createProperty("lastName", OType.FLOAT);
            person.createProperty("gender", OType.STRING);
            person.createProperty("birthday", OType.DATE);
            person.createProperty("creationDate", OType.DATE);
            person.createProperty("locationIP", OType.STRING);
            person.createProperty("browserUsed", OType.STRING);
            person.createProperty("place", OType.INTEGER);
            person.createIndex("Person_id_index", OClass.INDEX_TYPE.UNIQUE, "id");
        }

        if (db.getClass("Post") == null) {
            OClass post = db.createVertexClass("Post");
            post.createProperty("id", OType.INTEGER);
            post.createProperty("imageFile", OType.BINARY);
            post.createProperty("creationDate", OType.DATE);
            post.createProperty("locationIP", OType.STRING);
            post.createProperty("browserUsed", OType.STRING);
            post.createProperty("language", OType.STRING);
            post.createProperty("content", OType.STRING);
            post.createProperty("length", OType.INTEGER);
            post.createIndex("Post_id_index", OClass.INDEX_TYPE.UNIQUE, "id");
        }

        if (db.getClass("Tag") == null) {
            OClass tag = db.createVertexClass("Tag");
            tag.createProperty("id", OType.INTEGER);
            tag.createProperty("name", OType.STRING);
            tag.createIndex("Tag_id_index", OClass.INDEX_TYPE.UNIQUE, "id");
        }

        if (db.getClass("Knows") == null) {
            db.createEdgeClass("Knows");
        }
        if (db.getClass("HasTag") == null) {
            db.createEdgeClass("HasTag");
        }
        if (db.getClass("HasInterest") == null) {
            db.createEdgeClass("HasInterest");
        }
        if (db.getClass("HasCreated") == null) {
            db.createEdgeClass("HasCreated");
        }

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



        /* LOADING THE PRODUCT DATA */
        System.out.println("Loading the product data");
        JsonLoader jsonLoader = new JsonLoader(db);
        jsonLoader.load();

        /* LOADING THE CUSTOMER DATA */
        System.out.println("Loading the customer data");
        CustomerLoader customerLoader = new CustomerLoader(db);
        customerLoader.load();

        /* LOADING THE FEEDBACK DATA */
        System.out.println("Loading the feedback data");
        // FeedbackLoader feedbackLoader = new FeedbackLoader(db);
        // feedbackLoader.load();


        /* LOADING THE VENDOR DATA */
        System.out.println("Loading the vendor data");
        VendorLoader vendorLoader = new VendorLoader(db);
        vendorLoader.load();




        /*
        ODocument doc = new ODocument("Person2");
        doc.field( "name", "Luke" );
        doc.field( "surname", "Skywalker" );
        doc.field( "city", "lalala");

        // SAVE THE DOCUMENT
        db.save(doc);
        */




        db.close();
        orientDB.close();

    }
}
