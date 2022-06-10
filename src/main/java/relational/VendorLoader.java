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
public class VendorLoader {
    public static void main(String[] args) throws FileNotFoundException {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        // Replace the arguments with your own database name and user/password
        ODatabaseSession db = orientDB.open("testdb", "root", "2610");

        if (db.getClass("VendorVertex") == null) {
            OClass vendor = db.createVertexClass("VendorVertex");
            vendor.createProperty("Vendor", OType.STRING);
            vendor.createProperty("Country", OType.STRING);
            vendor.createProperty("Industry", OType.STRING);
            vendor.createIndex("vendor_index", OClass.INDEX_TYPE.UNIQUE, "Vendor");
        }


        // Loading the csv product into a list of list of String
        List<List<String>> records = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/Vendor/Vendor.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Line 0 only contains the columns names, so we start at line 1
        for(int p=1; p<records.size(); p++){
            // We check if the vendor already exists before adding it
            String query = "SELECT * from VendorVertex where Vendor = ?";
            OResultSet rs = db.query(query, records.get(p).get(0));
            if(rs.elementStream().count()==0) {
                createVendor(db, records.get(p).get(0), records.get(p).get(1), records.get(p).get(2));
            }
        }

        db.close();
        orientDB.close();
    }

    private static OVertex createVendor(ODatabaseSession db, String vendor, String country, String industry) {
        OVertex result = db.newVertex("VendorVertex");
        result.setProperty("vendor", vendor);
        result.setProperty("country", country);
        result.setProperty("industry", industry);
        result.save();
        return result;
    }
}
