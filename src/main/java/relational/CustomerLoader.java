package relational;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// OK.
public class CustomerLoader {

    ODatabaseSession db;

    public CustomerLoader(ODatabaseSession db) {
        this.db = db;
    }


    public void load(){
        if (this.db.getClass("Customer") == null) {
            OClass customer = this.db.createVertexClass("Customer");
            customer.createProperty("id", OType.STRING);
            customer.createProperty("firstName", OType.STRING);
            customer.createProperty("lastName", OType.STRING);
            customer.createProperty("gender", OType.STRING);
            customer.createProperty("birthday", OType.STRING);
            customer.createProperty("creationDate", OType.STRING);
            customer.createProperty("locationIP", OType.STRING);
            customer.createProperty("browserUsed", OType.STRING);
            customer.createProperty("place", OType.STRING);
            customer.createIndex("customer_index", OClass.INDEX_TYPE.UNIQUE, "id");
        }


        // Loading the csv product into a list of list of String
        List<List<String>> records = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/Customer/person_0_0.csv"),'|');) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Line 0 only contains the columns names, so we start at line 1
        for(int p=1; p<records.size(); p++){
            // We check if the customer already exists before adding it
            String query = "SELECT * from Customer where id = ?";

            OResultSet rs = this.db.query(query, records.get(p).get(0));
            if(rs.elementStream().count()==0) {
                createCustomer(this.db, records.get(p).get(0), records.get(p).get(1), records.get(p).get(2),
                        records.get(p).get(3),records.get(p).get(4),records.get(p).get(5),
                        records.get(p).get(6),records.get(p).get(7),records.get(p).get(8));
            }
        }
    }

    private static OVertex createCustomer(ODatabaseSession db, String id, String firstName, String lastName,
                                          String gender, String birthday, String creationDate, String locationIP,
                                          String browserUsed, String place) {
        OVertex result = db.newVertex("Customer");
        result.setProperty("id", id);
        result.setProperty("firstName", firstName);
        result.setProperty("lastName", lastName);
        result.setProperty("gender", gender);
        result.setProperty("birthday", birthday);
        result.setProperty("creationDate", creationDate);
        result.setProperty("locationIP", locationIP);
        result.setProperty("browserUsed", browserUsed);
        result.setProperty("place", place);

        result.save();
        return result;
    }

    /* --------------------------------------- */
    /* -- METHODS INSERT, UPDATE AND DELETE -- */
    /* --------------------------------------- */

    public static void insertOneCustomer(ODatabaseSession db, ODocument doc) {

        ORecord r = doc.getRecord();
        String s = r.toJSON();
        JSONObject json = (JSONObject) JSONValue.parse(s);

        String id = (String) json.get("id");
        String firstName = (String) json.get("firstName");
        String lastName = (String) json.get("lastName");
        String gender = (String) json.get("gender");
        String birthday = (String) json.get("birthday");
        String creationDate = (String) json.get("creationDate");
        String locationIP = (String) json.get("locationIP");
        String browserUsed = (String) json.get("browserUsed");
        String place = (String) json.get("place");

        String query = "SELECT * from Customer where id = ?";
        OResultSet rs = db.query(query, id);
        if(!rs.elementStream().findFirst().isPresent()) {
            createCustomer(db, id, firstName, lastName, gender, birthday, creationDate, locationIP, browserUsed, place);
            System.out.println("The customer " + firstName + " has been inserted");
        }
        else{
            System.out.println("The id " + id + " is already present among the customer vertices");
        }
    }

    public static void deleteOneCustomer(ODatabaseSession db, ODocument doc) {

        ORecord r = doc.getRecord();
        String s = r.toJSON();
        JSONObject json = (JSONObject) JSONValue.parse(s);

        String id = (String) json.get("id");
        String firstName = (String) json.get("firstName");

        String query = "SELECT * from Customer where id = ?";
        OResultSet rs = db.query(query, id);
        Optional customer = rs.elementStream().findFirst();
        if(customer.isPresent()) {
            db.delete((OVertex)customer.get());
            System.out.println("The customer " + firstName + " has been deleted");
        }
        else{
            System.out.println("The customer " + firstName + " is already not present.");
        }
    }

    public static void updateOneCustomer(ODatabaseSession db,ODocument doc) {

        ORecord r = doc.getRecord();
        String s = r.toJSON();
        JSONObject json = (JSONObject) JSONValue.parse(s);

        String id = (String) json.get("id");
        String firstName = (String) json.get("firstName");
        String lastName = (String) json.get("lastName");
        String gender = (String) json.get("gender");
        String birthday = (String) json.get("birthday");
        String creationDate = (String) json.get("creationDate");
        String locationIP = (String) json.get("locationIP");
        String browserUsed = (String) json.get("browserUsed");
        String place = (String) json.get("place");

        String query = "SELECT * from Customer where id = ?";
        OResultSet rs = db.query(query, id);
        Optional customer = rs.elementStream().findFirst();
        if(customer.isPresent()) {
            OVertex customerVertex =  (OVertex)customer.get();
            db.delete(customerVertex);
            createCustomer(db,id,firstName,lastName, gender, birthday, creationDate, locationIP, browserUsed, place);

            System.out.println("The customer " + firstName + " has been updated");
        }
        else{
            System.out.println("The customer " + firstName + " is not present.");
        }
    }

    /* ------------------------------------------------------- */
    /* -- METHODS INSERT, UPDATE AND DELETE FOR MANY VALUES -- */
    /* ------------------------------------------------------- */
    public static void insertManyCustomers(ODatabaseSession db, List<ODocument> docs){
        for(ODocument document : docs){
            insertOneCustomer(db, document);
        }
    }

    public static void updateManyCustomers(ODatabaseSession db, List<ODocument> docs){
        for(ODocument document : docs){
            updateOneCustomer(db, document);
        }
    }

    public static void deleteManyCustomers(ODatabaseSession db, List<ODocument> docs){
        for(ODocument document : docs){
            deleteOneCustomer(db, document);
        }
    }


}
