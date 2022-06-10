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
        if (this.db.getClass("Feedback") == null) {
            OClass feedback = this.db.createVertexClass("Feedback");
            feedback.createProperty("productId", OType.STRING);
            feedback.createProperty("customerId", OType.STRING);
            feedback.createProperty("opinion", OType.STRING);
            feedback.createIndex("feedback_index", OClass.INDEX_TYPE.UNIQUE, "productId","customerId");
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
            // We check if the feedback already exists before adding it
            String query = "SELECT * from Feedback where productId = ? and customerId = ?";
            OResultSet rs = this.db.query(query, records.get(p).get(0));
            if(rs.elementStream().count()==0) {
                createFeeback(this.db, records.get(p).get(0), records.get(p).get(1), records.get(p).get(2));
            }
        }
    }

    private static OVertex createFeeback(ODatabaseSession db, String productId, String customerId, String opinion) {
        OVertex result = db.newVertex("Feedback");
        result.setProperty("productId", productId);
        result.setProperty("customerId", customerId);
        result.setProperty("opinion", opinion);
        result.save();
        return result;
    }
}
