package relational;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

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

    /* ------------------------------ */
    /* -- METHODES DE MISES A JOUR -- */
    /* ------------------------------ */

    public static void newCustomer(ODatabaseSession db, String id, String firstName, String lastName,
                                   String gender, String birthday, String creationDate, String locationIP,
                                   String browserUsed, String place) {
        String query = "SELECT * from Customer where id = ?";
        OResultSet rs = db.query(query, id);
        if(!rs.elementStream().findFirst().isPresent()) {
            createCustomer(db, id, firstName, lastName, gender, birthday, creationDate, locationIP, browserUsed, place);
            System.out.println("The customer has been inserted");
        }
        else{
            System.out.println("The id is already present among the customer vertices");
        }
    }

    public static void deleteCustomer(ODatabaseSession db, String id) {
        String query = "SELECT * from Customer where id = ?";
        OResultSet rs = db.query(query, id);
        Optional customer = rs.elementStream().findFirst();
        if(customer.isPresent()) {
            db.delete((OVertex)customer.get());
            System.out.println("The customer has been deleted");
        }
        else{
            System.out.println("The customer is already not present.");
        }
    }

    public static void updateCustomer(ODatabaseSession db, String id, String firstName, String lastName,
                                      String gender, String birthday, String creationDate, String locationIP,
                                      String browserUsed, String place){
        String query = "SELECT * from Customer where id = ?";
        OResultSet rs = db.query(query, id);
        Optional customer = rs.elementStream().findFirst();
        if(customer.isPresent()) {
            OVertex customerVertex =  (OVertex)customer.get();
            db.delete(customerVertex);
            createCustomer(db,id,firstName,lastName, gender, birthday, creationDate, locationIP, browserUsed, place);

            System.out.println("The customer has been updated");
        }
        else{
            System.out.println("The customer is not present.");
        }
    }


}
