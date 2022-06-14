package feedback;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.OEdgeDocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FeedbackLoader {

    ODatabaseSession db;

    public FeedbackLoader(ODatabaseSession db){
        this.db = db;
    }
    public void chargementFeedback(){

        if (this.db.getClass("Feedback") == null) {
            OClass feedback = this.db.createEdgeClass("Feedback");
            feedback.createProperty("productAsin", OType.STRING);
            feedback.createProperty("personID", OType.STRING);
            feedback.createProperty("comment", OType.STRING);
            feedback.createIndex("feedback_index", OClass.INDEX_TYPE.UNIQUE, "productAsin", "personID");
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
            OResultSet productAsinres = this.db.query(query, records.get(p).get(0));
            Optional<OElement> productOptional = productAsinres.elementStream().findFirst();
            OElement product = productOptional.get();

            String query2 = "SELECT * from Customer where id = ?";
            OResultSet customerIDres = this.db.query(query2, records.get(p).get(1));
            Optional<OElement> customerOptional = customerIDres.elementStream().findFirst();
            OElement customer = customerOptional.get();

            String feedbackRecord = records.get(p).get(2);

            creerFeedback((OVertex) product, records.get(p).get(0), (OVertex) customer, records.get(p).get(1), feedbackRecord);
        }
    }

    private void creerFeedback(OVertex product, String productAsin,OVertex person, String customerId, String feedback) {
        OEdge feedbackClass = this.db.newEdge(person, product, "Feedback");
        feedbackClass.setProperty("productAsin", productAsin);
        feedbackClass.setProperty("personID", customerId);
        feedbackClass.setProperty("comment", feedback);
        feedbackClass.save();
    }

    public void supprimerFeedbackParCustomer(String firstName){
        String supprimer = "DELETE EDGE Feedback WHERE out.firstName = ?";
        OResultSet supprimerRes = this.db.query(supprimer,firstName);
    }

    public void supprimerTousFeedbackParCustomerVertex(OVertex customer){
        customer.getEdges(ODirection.OUT, "FeedBack").forEach(oEdge -> oEdge.delete().save());
        this.db.commit();
    }

    public void majFeedbackChangeCustomer(ODatabaseDocument db, String firstName, String productAsin, String nouveauComment){
        String supprimer = "UPDATE EDGE Feedback SET out = (SELECT FROM Person WHERE firstName = ?) WHERE in = (SELECT FROM Product WHERE asin = ?)";
        OResultSet changeCustomer = db.query(supprimer,firstName, productAsin);
        changeCustomer.close();
        db.commit();
    }

    public void majFeedbackParCustomerETProductVertex(OVertex customer, OVertex product, String nouveauComment){
        Iterable<OEdge> edges = customer.getEdges(ODirection.OUT, "Feedback");
        for(OEdge feedback: edges){
            if(feedback.getProperty("productAsin").equals(product.getProperty("productAsin"))){
                feedback.setProperty("comment", nouveauComment);
                feedback.save();
            }
        }
        this.db.commit();
    }

    public void insererFeedback(OVertex customer, OVertex product, String nouveauComment){
        OEdge nouvFeedback =  db.newEdge(customer, product, "Feedback");
        nouvFeedback.setProperty("comment", nouveauComment);
        nouvFeedback.save();
        db.commit();
    }

    public void insererFeedbackSQL(OVertex customer, OVertex product, String nouveauComment){
        String creer = "CREATE EDGE Feedback FROM ? TO ? SET comment = ? ";
        OResultSet creerRes = this.db.query(creer, customer.getIdentity(), product.getIdentity(), nouveauComment);
        creerRes.close();
        db.commit();
    }

}