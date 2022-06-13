package feedback;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
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
            OClass feedback = db.createEdgeClass("Feedback");
            feedback.createProperty("productAsin", OType.STRING);
            feedback.createProperty("personID", OType.STRING);
            feedback.createProperty("comment", OType.STRING);
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

            String query = "SELECT * from Product where asin = ?";
            OResultSet productAsinres = db.query(query, records.get(p).get(0));
            Optional<OElement> productOptional = productAsinres.elementStream().findFirst();
            OElement product = productOptional.get();

            String query2 = "SELECT * from Customer where id = ?";
            OResultSet customerIDres = db.query(query2, records.get(p).get(1));
            Optional<OVertex> customerOptional = customerIDres.vertexStream().findFirst();
            OVertex customer = customerOptional.get();

            String feedbackRecord = records.get(p).get(2);

            creerFeedback(db, (OVertex) product, customer, feedbackRecord);
        }
    }

    private static void creerFeedback(ODatabaseSession db, OVertex product,OVertex person, String feedback) {
        OEdge feedbackClass = db.newEdge(person, product, "Feedback");
        feedbackClass.setProperty("feedback", feedback);
        feedbackClass.save();
    }

    public static void supprimerFeedbackParCustomer(ODatabaseDocument db, String firstName){
        String supprimer = "DELETE EDGE Feedback WHERE out.firstName = ?";
        OResultSet supprimerRes = db.query(supprimer,firstName);
    }
}