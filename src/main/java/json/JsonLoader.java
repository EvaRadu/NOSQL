package json;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonLoader {
    public static void main(String[] args) throws FileNotFoundException {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        // Replace the arguments with your own database name and user/password
        ODatabaseSession db = orientDB.open("testdb", "root", "2610");

        if (db.getClass("Product") == null) {
            OClass product = db.createVertexClass("Product");
            product.createProperty("asin", OType.STRING);
            product.createProperty("title", OType.STRING);
            product.createProperty("price", OType.FLOAT);
            product.createProperty("imgUrl", OType.STRING);
            product.createIndex("product_asin_index", OClass.INDEX_TYPE.UNIQUE, "asin");
        }

        // Loading the csv product into a list of list of String
        List<List<String>> records = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/Product/Product.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Line 0 only contains the columns names, so we start at line 1
        for(int p=1; p<records.size(); p++){
            // We check if the product already exists before adding it
            String query = "SELECT * from Product where asin = ?";
            OResultSet rs = db.query(query, records.get(p).get(0));
            if(rs.elementStream().count()==0) {
                createProduct(db, records.get(p).get(0), records.get(p).get(1), Float.parseFloat(records.get(p).get(2)), records.get(p).get(0));
            }
        }

        db.close();
        orientDB.close();
    }

    private static OVertex createProduct(ODatabaseSession db, String asin, String title, float price, String imgUrl) {
        OVertex result = db.newVertex("Product");
        result.setProperty("asin", asin);
        result.setProperty("title", title);
        result.setProperty("price", price);
        result.setProperty("imgUrl", imgUrl);
        result.save();
        return result;
    }


}
