package feedback;


import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FeedbackLoader {

    public static void chargementFeedback(ODatabaseSession db){

        if (db.getClass("Feedback") == null) {
            OClass feedback = db.createClass("Feedback");
            feedback.createProperty("productAsin", OType.STRING);
            feedback.createProperty("personID", OType.STRING);
            feedback.createProperty("feedback", OType.STRING);
            feedback.createIndex("feedback_index", OClass.INDEX_TYPE.NOTUNIQUE, "productAsin", "personID");
        }

        List<List<String>> records = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/Feedback/Feedback.csv"),'|');) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(int p=1; p<records.size(); p++){

            String query = "SELECT rid from INDEX:product_asin_index where key = ?";
            OResultSet productAsinres = db.query(query, records.get(p).get(0));
            ORID productAsin = productAsinres.stream().findFirst().get().getProperty("rid");

            String query2 = "SELECT rid from INDEX:customer_index where key = ?";
            OResultSet customerIDres = db.query(query2, records.get(p).get(1));
            ORID customerID = customerIDres.stream().findFirst().get().getProperty("rid");

            String feedbackRecord = records.get(p).get(2);

            String query3 = "SELECT * from Feedback where productAsin = ? and personID = ? ";
            OResultSet count = db.query(query3, productAsin, customerID);

            if (count.stream().count() == 0){
                creerFeedback(db, productAsin, customerID, feedbackRecord);
            }
        }
    }

    private static void creerFeedback(ODatabaseSession db, ORID productAsin,ORID personID, String feedback) {
        OElement feedbackClass = db.newElement("Feedback");
        feedbackClass.setProperty("productAsin", productAsin);
        feedbackClass.setProperty("personID", personID);
        feedbackClass.setProperty("feedback", feedback);
        feedbackClass.save();
    }
}
