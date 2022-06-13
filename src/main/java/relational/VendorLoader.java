package relational;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.client.remote.OStorageRemotePushThread;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
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
public class VendorLoader {

    ODatabaseSession db;
    public VendorLoader(ODatabaseSession db) {
        this.db = db;
    }

    public void load(){
        if (this.db.getClass("VendorVertex") == null) {
            OClass vendor = this.db.createVertexClass("VendorVertex");
            vendor.createProperty("Vendor", OType.STRING);
            vendor.createProperty("Country", OType.STRING);
            vendor.createProperty("Industry", OType.STRING);
            vendor.createIndex("vendor_index", OClass.INDEX_TYPE.UNIQUE, "Vendor");
        }


        // Loading the csv vendor into a list of list of String
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
            OResultSet rs = this.db.query(query, records.get(p).get(0));
            if(rs.elementStream().count()==0) {
                createVendor(this.db, records.get(p).get(0), records.get(p).get(1), records.get(p).get(2));
            }
        }

        /*
        // EDGE VENDOR / PRODUCT

        if (this.db.getClass("edgeVendorProduct") == null) {
            OClass edgeVendorProduct = this.db.createEdgeClass("edgeVendorProduct");
            edgeVendorProduct.createProperty("idVendor", OType.STRING);
            edgeVendorProduct.createProperty("idProduct", OType.STRING);
            edgeVendorProduct.createIndex("edgeVendorProduct_index", OClass.INDEX_TYPE.UNIQUE, "idVendor");
        }



        // Loading the csv product into a list of list of String
        List<List<String>> records2 = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/Product/BrandByProduct.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records2.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Line 0 only contains the columns names, so we start at line 1
        for(int p=1; p<records2.size(); p++){
            // We check if the vendor already exists before adding it
            String query = "SELECT * from VendorVertex where vendor = ?";
            OResultSet rs = this.db.query(query, records2.get(p).get(0));
            if(rs.elementStream().count()==0) {

            }
            OVertex vendor = (OVertex) rs.elementStream().findFirst().get();


            String query2 = "SELECT * from Product where asin = ?";
            OResultSet rs2 = this.db.query(query2, records2.get(p).get(1));
            OVertex product = (OVertex) rs2.elementStream().findFirst().get();
            //System.out.println(rs2.elementStream().count());
            if ((rs2.elementStream().count() == 0)&&(rs.elementStream().count() == 0) ){
                createEdgeVendorProduct(this.db, vendor,  product);
            }


            */
    }

    private static OElement createVendor(ODatabaseSession db, String vendor, String country, String industry) {
        OElement result = db.newVertex("VendorVertex");
        result.setProperty("Vendor", vendor);
        result.setProperty("Country", country);
        result.setProperty("Industry", industry);
        result.save();
        return result;
    }

    private static void createEdgeVendorProduct(ODatabaseSession db, OVertex vendor,OVertex product) {
        vendor.addEdge(product,"edgeVendorProduct").save();
        db.commit();
    }

}
