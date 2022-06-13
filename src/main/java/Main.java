import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import json.JsonsLoader;
import relational.CustomerLoader;
import relational.VendorLoader;

import java.text.ParseException;

public class Main {

    public static void main(String[] args) throws ParseException {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        // Replace the arguments with your own database name and user/password
        // A remplacer avec le nom de la base de donnée et les identifiants
        ODatabaseSession db = orientDB.open("testdb", "root", "2610");

        // LOADING THE PRODUCT DATA
        /*
        VendorLoader vendorLoader = new VendorLoader(db);
        vendorLoader.load();
        JsonsLoader jsonLoader = new JsonsLoader(db);
        jsonLoader.load();
        jsonLoader.createOutEdges();

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



        // TESTS DE MISES A JOUR POUR LES CUSTOMERS (4.4)
        CustomerLoader customerLoader = new CustomerLoader(db);
        customerLoader.newCustomer(db,"123","Eva","Radu","female","2001-02-26","2022-06-13T02:10:23.099+0000","27.98.237.197","Opera","2037");
        customerLoader.updateCustomer(db,"123","Evaaa","Radu","female","2001-02-26","2022-06-13T02:10:23.099+0000","27.98.237.197","Opera","2037");
        customerLoader.deleteCustomer(db,"123");


        // TESTS DE MISES A JOUR POUR LES VENDEURS (4.4)
        VendorLoader vendorLoader = new VendorLoader(db);
        vendorLoader.newVendor(db,"EvaShop","Romania","Clothes");
        vendorLoader.updateVendor(db,"EvaShop","Romania","Sports");
        vendorLoader.deleteVendor(db,"EvaShop");


    }
}
