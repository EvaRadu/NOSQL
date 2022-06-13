package json;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class JsonLoader {
    ODatabaseSession db;

    public JsonLoader(ODatabaseSession db){
        this.db = db;
    }

    public void load(){
        if (this.db.getClass("Product") == null) {
            OClass product = db.createVertexClass("Product");
            product.createProperty("asin", OType.STRING);
            product.createProperty("title", OType.STRING);
            product.createProperty("price", OType.FLOAT);
            product.createProperty("imgUrl", OType.STRING);
            product.createIndex("product_asin_index", OClass.INDEX_TYPE.UNIQUE, "asin");
        }
        if (this.db.getClass("Brand") == null) {
            OClass Brand = db.createVertexClass("Brand");
            Brand.createProperty("bname", OType.STRING);
            Brand.createIndex("brand_name_index", OClass.INDEX_TYPE.UNIQUE, "bname");
        }
        if (this.db.getClass("IsFromBrand")== null) {
            OClass IsFromBrand = db.createEdgeClass("IsFromBrand");
        }


        // Loading the csv product into a list of list of String
        List<List<String>> productRecords = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/Product/Product.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                productRecords.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Line 0 only contains the columns names, so we start at line 1
        for(int p=1; p<productRecords.size(); p++){
            // We check if the product already exists before adding it
            String query = "SELECT * from Product where asin = ?";
            OResultSet rs = this.db.query(query, productRecords.get(p).get(0));
            if(rs.elementStream().count()==0) {
                createProduct(this.db, productRecords.get(p).get(0), productRecords.get(p).get(1), Float.parseFloat(productRecords.get(p).get(2)), productRecords.get(p).get(0));
            }
            rs.close();
        }

        // Loading the csv BrandByProduct into a list of list of String
        List<List<String>> brandRecords = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/Product/BrandByProduct.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                brandRecords.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HashSet<String> uniqueBrands = new HashSet<>();
        for(int b=0; b<brandRecords.size(); b++){
            uniqueBrands.add(brandRecords.get(b).get(0));
        }
        for(String brand : uniqueBrands){
            String query = "SELECT * from Brand where bname = ?";
            OResultSet rs = this.db.query(query, brand);
            if(rs.elementStream().count()==0) {
                createBrand(this.db, brand);
            }
        }

        // Creating edges from products to brands
        for(int p=0; p<brandRecords.size(); p++){
            linkProductToBrand(this.db, brandRecords.get(p).get(0), brandRecords.get(p).get(1));
        }

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

    private static OVertex createBrand(ODatabaseSession db, String bname) {
        OVertex result = db.newVertex("Brand");
        result.setProperty("bname", bname);
        result.save();
        return result;
    }

    private static OEdge linkProductToBrand(ODatabaseSession db, String bname, String asin){
        String query = "SELECT * from Brand where bname = ?";
        OResultSet rsb = db.query(query, bname);
        OEdge result = null;

        if(rsb.hasNext()){
            //System.out.println("hasnext");
            Optional<OVertex> optional = rsb.next().getVertex();
            rsb.close();
            if(optional.isPresent()){
                //System.out.println("ispresent");
                OVertex Brand = optional.get();

                query = "SELECT * from Product where asin = ?";
                OResultSet rsp = db.query(query, asin);
                if(rsp.hasNext()){
                    //System.out.println("hasnext2");
                    optional = rsp.next().getVertex();
                    rsp.close();
                    if(optional.isPresent()){
                        //System.out.println("ispresent2");
                        OVertex Product = optional.get();
                        result = db.newEdge(Product, Brand, db.getClass("IsFromBrand"));
                        result.save();
                    }
                }
            }
        }
        return result;
    }


}
