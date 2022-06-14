import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.record.impl.ODocument;
import json.JsonsLoader;
import relational.CustomerLoader;
import relational.VendorLoader;
import xml.InvoiceLoader;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws ParseException {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        // Replace the arguments with your own database name and user/password
        // A remplacer avec le nom de la base de donnée et les identifiants
        ODatabaseSession db = orientDB.open("testdb", "root", "2610");

        // LOADING THE PRODUCT DATA
        /*VendorLoader vendorLoader = new VendorLoader(db);
        vendorLoader.load();
        JsonsLoader jsonLoader = new JsonsLoader(db);
        jsonLoader.load();
        jsonLoader.createOutEdges();

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

        CustomerLoader customerLoader = new CustomerLoader(db);
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

        VendorLoader vendorLoader = new VendorLoader(db);

        vendorLoader.insertOneVendor(db,docVendor1);
        vendorLoader.updateOneVendor(db,docVendor2);
        vendorLoader.deleteOneVendor(db,docVendor3);

        vendorLoader.insertManyVendors(db,docsVendor);
        vendorLoader.updateManyVendors(db,docsVendor);
        vendorLoader.deleteManyVendors(db,docsVendor);


    }
}
