package keyValue;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeedbackLoader {

    ODatabaseSession db;

    public FeedbackLoader(ODatabaseSession db) {
        this.db = db;
    }


    public void load(){
        /* FEEDBACK  =  EDGE CUSTOMER / PRODUCT  */

        if (this.db.getClass("edgeProductCustomer") == null) {
            OClass edgeProductCustomer = this.db.createEdgeClass("edgeProductCustomer");
            edgeProductCustomer.createProperty("idProduct", OType.STRING);
            edgeProductCustomer.createProperty("idCustomer", OType.STRING);
            edgeProductCustomer.createProperty("opinion", OType.STRING);
            edgeProductCustomer.createIndex("edgeProductCustomer_index", OClass.INDEX_TYPE.NOTUNIQUE, "idProduct","idCustomer");
        }


        // Loading the csv product into a list of list of String
        List<List<String>> records = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/Feedback/Feedback.csv"),'|');) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Line 0 only contains the columns names, so we start at line 1
        for(int p=1; p<records.size(); p++){

            String query = "SELECT * from Product where asin = ?";
           // System.out.println(records.get(p).get(0));
            OResultSet rs = this.db.query(query, records.get(p).get(0));
            OVertex product = (OVertex) rs.elementStream().findFirst().get();

            String query2 = "SELECT * from Customer where id = ?";
          //  System.out.println(records.get(p).get(1));
            OResultSet rs2 = this.db.query(query2, records.get(p).get(1));
            OVertex customer = (OVertex) rs2.elementStream().findFirst().get();



            if ((rs2.elementStream().count() == 0)&&(rs.elementStream().count() == 0) ){
                createEdgeProductCustomer(this.db, customer,  product,records.get(p).get(2));
            }


        }
    }

    private static void createEdgeProductCustomer(ODatabaseSession db, OVertex customer,OVertex product, String opinion) {
        customer.addEdge(product,"edgeProductCustomer").setProperty("opinion",opinion);
        customer.save();
        db.commit();
    }
}
