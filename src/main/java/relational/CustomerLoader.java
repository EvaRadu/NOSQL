package relational;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
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

// OK.
public class CustomerLoader {
    public static void main(String[] args) throws FileNotFoundException {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        // Replace the arguments with your own database name and user/password
        ODatabaseSession db = orientDB.open("testdb", "root", "1604");

        if (db.getClass("Customer") == null) {
            OClass customer = db.createVertexClass("Customer");
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
            // il n'y a pas d'id ???????
            // We check if the vendor already exists before adding it
            String query = "SELECT * from Customer where id = ?";

            OResultSet rs = db.query(query, records.get(p).get(0));
            if(rs.elementStream().count()==0) {
                createCustomer(db, records.get(p).get(0), records.get(p).get(1), records.get(p).get(2),
                        records.get(p).get(3),records.get(p).get(4),records.get(p).get(5),
                                records.get(p).get(6),records.get(p).get(7),records.get(p).get(8));
            }
        }

        db.close();
        orientDB.close();
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
}
