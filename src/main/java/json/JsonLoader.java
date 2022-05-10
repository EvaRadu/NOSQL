package json;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;

public class JsonLoader {
    public static void main(String[] args){
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

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

        db.close();
        orientDB.close();
    }

}
