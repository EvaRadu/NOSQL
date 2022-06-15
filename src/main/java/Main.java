import com.orientechnologies.common.collection.OMultiCollectionIterator;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.xml.sax.SAXException;
import relational.CustomerLoader;
import relational.VendorLoader;
import xml.InvoiceLoader;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {
    //REQUEST 1
        /*
        For a given customer, find his/her all related data including profile, orders, invoices,
        feedback, comments, and posts in the last month, return the category in which he/she has
        bought the largest number of products, and return the tag which he/she has engaged the
        greatest times in the posts.
        */
    public static void query1(ODatabaseSession db, String id, Date date) throws ParseException {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        Date lastMonth = cal.getTime();

        String queryCust = "SELECT * from Customer where id = ?";
        OResultSet rsCust = db.query(queryCust, id);
        Optional custRes = rsCust.elementStream().findFirst();
        if (custRes.isPresent()) {
            OVertex customerVertex = (OVertex) custRes.get();
            System.out.println("========= PROFILE =========");
            System.out.println((String) customerVertex.getProperty("id"));
            System.out.println((String) customerVertex.getProperty("firstName"));
            System.out.println((String) customerVertex.getProperty("lastName"));
            System.out.println((String) customerVertex.getProperty("gender"));
            System.out.println((String) customerVertex.getProperty("birthday"));
            System.out.println((String) customerVertex.getProperty("creationDate"));
            System.out.println((String) customerVertex.getProperty("locationIP"));
            System.out.println((String) customerVertex.getProperty("browserUsed"));
            System.out.println((String) customerVertex.getProperty("place"));
            System.out.println("\n========= HasCreatedPosts =========");

            for (OEdge e : customerVertex.getEdges(ODirection.OUT, "hasCreated")) {
                if (e.getProperty("idPerson").equals(id)) {
                    OVertex post = e.getVertex(ODirection.OUT);
                    System.out.println((String) post.getProperty("idPost"));
                    System.out.println((String) post.getProperty("imageFile"));
                    System.out.println((String) post.getProperty("creationDate"));
                    System.out.println((String) post.getProperty("locationIP"));
                    System.out.println((String) post.getProperty("browserUsed"));
                    System.out.println((String) post.getProperty("language"));
                    System.out.println((String) post.getProperty("content"));
                    System.out.println((String) post.getProperty("length"));

                }
            }

        }

        String queryOrder = "SELECT * from Order where PersonId = ?";
        OResultSet rsOrder = db.query(queryOrder, id);
        System.out.println("\n========= ORDERS =========");
        while (rsOrder.hasNext()) {
            Optional<OVertex> optional = rsOrder.next().getVertex();
            if (optional.isPresent()) {
                OVertex order = optional.get();
                System.out.println((String) order.getProperty("OrderId"));
                System.out.println((String) order.getProperty("PersonId"));
                System.out.println((String) order.getProperty("OrderDate"));
                System.out.println((Float) order.getProperty("TotalPrice"));
            }
        }
        rsOrder.close();

        String queryInvoice = "SELECT * from Invoice where personId = ?";
        OResultSet rsInvoice = db.query(queryInvoice, id);
        System.out.println("\n========= INVOICES =========");
        while (rsInvoice.hasNext()) {
            Optional<OVertex> optional = rsInvoice.next().getVertex();
            if (optional.isPresent()) {
                OVertex invoice = optional.get();
                System.out.println((String) invoice.getProperty("orderId"));
                System.out.println((String) invoice.getProperty("personId"));
                System.out.println((Date) invoice.getProperty("orderDate"));
                System.out.println((Float) invoice.getProperty("price"));
            }
        }
        rsInvoice.close();

        String queryFeedback = "SELECT * from Feedback where personID = ?";
        OResultSet rsFeedback = db.query(queryFeedback, id);
        System.out.println("\n========= FEEDBACKS =========");
        while (rsFeedback.hasNext()) {
            Optional<OEdge> optional = rsFeedback.next().getEdge();
            if (optional.isPresent()) {
                OEdge feedback = optional.get();
                System.out.println((String) feedback.getProperty("comment"));
            }
        }
        rsFeedback.close();

        /*
        //Select * from Post where idPost in (Select idPost from HasCreated where idPerson=?)
        String queryHasCreated = "Select * from HasCreated where idPerson = ?";
        OResultSet rsHasCreated = db.query(queryHasCreated, id);
        while (rsHasCreated.hasNext()) {
            Optional<OEdge> optional = rsHasCreated.next().getEdge();
            if (optional.isPresent()) {
                OVertex post = optional.get().getVertex(ODirection.OUT);
                Date datePost = (Date) post.getProperty("creationDate");
                if (datePost.after(lastMonth) && datePost.before(date)) {

                    System.out.println((String) post.getProperty("imageFile"));
                    System.out.println((Date)   post.getProperty("creationDate"));
                    System.out.println((String) post.getProperty("locationIP"));
                    System.out.println((String) post.getProperty("browserUsed"));
                    System.out.println((String) post.getProperty("language"));
                    System.out.println((String) post.getProperty("content"));
                    System.out.println((Integer) post.getProperty("length"));
                    System.out.println(datePost);
                }
            }
            rsHasCreated.close();
        */

        //RECUPERER LES PRODUITS ACHETES PAR UN UTILISATEUR
        String queryMostProdTag = "SELECT asin from Product WHERE in(\"Orderline\").PersonId= ?";
        OResultSet rsMPT = db.query(queryMostProdTag,id);
        ArrayList<String> asins = new ArrayList<>();
        while(rsMPT.hasNext()) {
            OResult optional = rsMPT.next();
            asins.addAll(optional.getProperty("asin"));
        }
        rsMPT.close();

        String queryMostProdTag2 = "SELECT in.idTag as idTag, COUNT(in.idTag) as cptT from ProductTag where out.asin in ? group by in.idTag order by cptT DESC";
        OResultSet rsMPT2 = db.query(queryMostProdTag,asins);
        OResult optional = rsMPT2.next();
        String idTag = optional.getProperty("idTag");
        rsMPT2.close();
        String subQuery = "SELECT name from TAG where idTag = ?";
        OResult optional2 = rsMPT2.next();
        String name = optional2.getProperty("name");
        System.out.println("The tag of the most sold product is " + name);


        String queryTag = "Select in.name, Count(idTag) as nbTags from HasTag where idPost in (Select idPost from HasCreated where idPerson = 4145) GROUP BY idTag ORDER BY nbTags DESC";
        OResultSet rsTag = db.query(queryTag, id);
        System.out.println("\n========= The tag which the person has engaged the greatest times in the posts =========");
        System.out.println(rsTag.stream().findFirst().get());
    }



    /*
     QUERY 2 :
       For a given product during a given period, find the people who commented or
       posted on it, and had bought it
     */
    public static void query2(ODatabaseSession db, String idProduct, String startDate, String endDate) throws ParseException {

        // GETTING THE ORDER DATES OF THE PRODUCT
        String queryDate = "SELECT IN(\"Orderline\").OrderDate from Product where asin = ?";
        OResultSet rsDate = db.query(queryDate, idProduct);
        ArrayList<String> dateRes = new ArrayList<>();
        while(rsDate.hasNext()) {
            OResult optional = rsDate.next();
            dateRes.addAll(optional.getProperty("IN(\"Orderline\").OrderDate"));
        }


        // GETTING THE CUSTOMERS WHO ORDERED THE PRODUCT
        String queryOrder = "SELECT IN(\"Orderline\").PersonId from Product where asin = ?";
        OResultSet rsOrder = db.query(queryOrder, idProduct);
        ArrayList<String> orderRes = new ArrayList<>();
        while(rsOrder.hasNext()) {
            OResult optional = rsOrder.next();
            orderRes.addAll(optional.getProperty("IN(\"Orderline\").PersonId"));
        }

        // GETTING THE CUSTOMERS WHO COMMENTED THE PRODUCT
        String queryFeedback = "select personID from Feedback where productAsin= ?";
        OResultSet rsFeedback = db.query(queryFeedback, idProduct);
        ArrayList<String> feedbackRes = new ArrayList<>();
        while(rsFeedback.hasNext()) {
            OResult optional2 = rsFeedback.next();
            feedbackRes.add(optional2.getProperty("personID").toString());
        }


        // GETTING THE TAGS OF THE PRODUCT
        String queryTag = "SELECT OUT(\"ProductTag\").idTag FROM Product where asin = ?";
        OResultSet rsTag = db.query(queryTag, idProduct);
        ArrayList<String> tagRes = new ArrayList<>();
        while(rsTag.hasNext()) {
            OResult optional3 = rsTag.next();
            tagRes.addAll(optional3.getProperty("OUT(\"ProductTag\").idTag"));
        }

        // GETTING THE POSTS RELATED OF THE TAGS
        String queryPost = "SELECT idPost FROM `HasTag` WHERE idTag=?";
        ArrayList<String> postRes = new ArrayList<>();
        for(String tag : tagRes){
            OResultSet rsPost = db.query(queryPost, tag);
            postRes.add(rsPost.next().getProperty("idPost").toString());
        }

        // GETTING THE CUSTOMERS WHO CREATED THESE POSTS AND CHECKING IF IT'S IN THE CORRECT PERIOD
        String queryCustomerPost = "select creationDate, OUT(\"HasCreated\").id from `Post` where idPost=? and creationDate between ? and ?";
        ArrayList<String> customerPostRes = new ArrayList<>();
        for(String post : postRes){
            OResultSet rsCustomerPost = db.query(queryCustomerPost, post, startDate, endDate);
            while(rsCustomerPost.hasNext()) {
                customerPostRes.addAll(rsCustomerPost.next().getProperty("OUT(\"HasCreated\").id"));
            }
        }




        // GETTING THE CUSTOMERS WHO ORDERED THE PRODUCT IN THE PERIOD INTERVAL
        ArrayList<String> finalOrderRes = new ArrayList<>();

        for(int i = 0; i<=dateRes.size()-1;i++){
            Boolean bool = (new SimpleDateFormat("yyyy-MM-dd").parse(dateRes.get(i))).before( new SimpleDateFormat("yyyy-MM-dd").parse(endDate));
            Boolean bool2 = (new SimpleDateFormat("yyyy-MM-dd").parse(startDate).before( new SimpleDateFormat("yyyy-MM-dd").parse(dateRes.get(i))));
            if(bool&&bool2){
                finalOrderRes.add(orderRes.get(i));
            }

        }

        // GETTING THE CUSTOMERS WHO ORDERED THE PRODUCT IN THE PERIOD INTERVAL
        // AND HAD COMMENTED IT OR POSTED ON IT

        ArrayList<String> finalRes = new ArrayList<>();
        for(String s : feedbackRes){
            if(finalOrderRes.contains(s)){
                finalRes.add(s);
            }
        }
        for(String s : customerPostRes){
            if(finalOrderRes.contains(s)){
                finalRes.add(s);
            }
        }


        // FINAL RESULTS :
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("                 CUSTOMERS WHO GAVE A FEEDBACK ON THE PRODUCT :                ");
        System.out.println("-------------------------------------------------------------------------------");

        for (String s : feedbackRes) {
            String q1 = "SELECT * from Customer where id = ?";
            OResultSet r1 = db.query(q1, s);
            System.out.println(r1.elementStream().findFirst().get());
        }

        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("          CUSTOMERS WHO POSTED ON THE PRODUCT IN THE SPECIFIED PERIOD :        ");
        System.out.println("-------------------------------------------------------------------------------");

        for (String s : customerPostRes) {
            String q1 = "SELECT * from Customer where id = ?";
            OResultSet r1 = db.query(q1, s);
            System.out.println(r1.elementStream().findFirst().get());
        }

        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("           CUSTOMERS WHO ORDERED THE PRODUCT IN THE SPECIFIED PERIOD :         ");
        System.out.println("-------------------------------------------------------------------------------");

        for (String s : finalOrderRes) {
            String q1 = "SELECT * from Customer where id = ?";
            OResultSet r1 = db.query(q1, s);
            System.out.println(r1.elementStream().findFirst().get());
        }

        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.println("   CUSTOMERS WHO ORDERED THE PRODUCT IN THE SPECIFIED PERIOD AND COMMENTED OR POSTED ON IT  ");
        System.out.println("--------------------------------------------------------------------------------------------");

        for (String s : finalRes) {
            String q1 = "SELECT * from Customer where id = ?";
            OResultSet r1 = db.query(q1, s);
            System.out.println(r1.elementStream().findFirst().get());
        }
        System.out.println("--------------------------------------------------------------------------------------------");

    }

    // Getting comments of the Feedbacks for a product with a low grade,
    // and the content of Posts posted in the specified interval
    public static ArrayList<String> query3(ODatabaseSession db, String from, String to, String product_asin){
        ArrayList<String> res = new ArrayList<>();

        // We select the feedbacks related to our product
        String query = "SELECT * from Feedback where productAsin = ?";
        OResultSet rs = db.query(query, product_asin);

        while(rs.hasNext()){
            Optional<OEdge> optional = rs.next().getEdge();
            if (optional.isPresent()) {
                OEdge feedback = optional.get();
                String text = (String)feedback.getProperty("comment");
                // We need to parse the grading in the comment ex: "4.5,blablabla"
                String grade = "";
                int cpt = 1;
                while(text.charAt(cpt) !=",".charAt(0)){
                    grade = grade + text.charAt(cpt);
                    cpt ++;
                }
                float sentiment = Float.parseFloat(grade);
                // We keep the comment if the grading is bad
                if(sentiment<2.5){
                    res.add(text);
                }
            }
        }
        rs.close();

        // We now select Post created in our interval
        query = "SELECT * from Post where creationDate between ? and ?";
        OResultSet rs2 = db.query(query, from, to);

        while(rs2.hasNext()){
            Optional<OVertex> optional = rs2.next().getVertex();
            if (optional.isPresent()) {
                OVertex post = optional.get();
                String text = (String)post.getProperty("content");
                res.add(text);
            }
        }
        return res;
    }

    /*
    Query 9. Find top-3 companies who have the largest amount of sales at one country, for each
    company, compare the number of the male and female customers, and return the most recent
    posts of them.
    */
    public static void query9(ODatabaseSession db, String Country) throws ParseException {


        //RECUPERER TOP3 MARQUES
        String queryC = "Select out(\"IsFromBrand\").Vendor as brands, COUNT(*) as nbSells from Product where out(\"IsFromBrand\").Country = ? GROUP BY brands ORDER BY nbSells DESC";
        OResultSet rsC = db.query(queryC, Country);
        System.out.println("\n========= BRANDS =========");
        int j= 0;

        ArrayList<String> brands = new ArrayList<>();
        while(rsC.hasNext() && j < 3) {
            OResult optional = rsC.next();
            brands.addAll(optional.getProperty("brands"));
            j++;
        }
        rsC.close();
        System.out.println(brands);

        //RECUPERER LES ID DES PERSONNES PAR MARQUE
        String queryB1 = "select out.PersonId as PersonId from Orderline where in.asin in (select asin from Product WHERE OUT(\"IsFromBrand\").Vendor = ?)";
        OResultSet rsB1 = db.query(queryB1, brands.get(0));
        System.out.println("\n========= PERSONS =========");
        ArrayList<String> customersB1 = new ArrayList<>();

        while(rsB1.hasNext()) {
            OResult optional = rsB1.next();
            String personId = optional.getProperty("PersonId");
            customersB1.add(personId);

            String subQuery = "SELECT out.idPost, out.content, out.creationDate from HASCREATED where idPerson = ? order by out.creationDate DESC";
            System.out.println("\n========= LAST POST =========");
            OResultSet res = db.query(subQuery,personId);
            if (res.hasNext()) {
                System.out.println("Last post of customer " + personId + ":");
                System.out.println(res.stream().findFirst().get());
            }
        }
        rsB1.close();
        System.out.println("done : \n" + customersB1);


        System.out.println("\n========= PERSONS =========");
        Map dictB1 = new HashMap<String,Long>();
        System.out.println(customersB1);
        String q1 = "SELECT COUNT(gender) as cptG, gender from Customer where id in ? group by gender order by cptG DESC";
        OResultSet r1 = db.query(q1, customersB1);
        while (r1.hasNext()) {
            OResult optional = r1.next();
            dictB1.put((String) optional.getProperty("gender"),(Long) optional.getProperty("cptG") );
        }
        if ((long) dictB1.get("male") > (long) dictB1.get("female")){
            System.out.println("There are more male customers (" + dictB1.get("male") + ") than female ones (" + dictB1.get("female") + ")");
        }
        else if ((long) dictB1.get("male") < (long) dictB1.get("female")){
            System.out.println("There are more female customers (" + dictB1.get("female") + ") than male ones (" + dictB1.get("male") + ")");
        } else {
            System.out.println("There is as much male customers as female ones (" + dictB1.get("male") + ")");
        }


        if (j>=2) {
            String queryB2 = "select out.PersonId as PersonId from Orderline where in.asin in (select asin from Product WHERE OUT(\"IsFromBrand\").Vendor = ?)";
            OResultSet rsB2 = db.query(queryB1, brands.get(1));
            System.out.println("\n========= CUSTOMERS BY BRAND =========");
            ArrayList<String> personsB2 = new ArrayList<>();
            while (rsB2.hasNext()) {
                OResult optional = rsB2.next();
                String personId = (String) optional.getProperty("PersonId");
                personsB2.add(personId);

                String subQuery = "SELECT out.idPost, out.content, out.creationDate from HASCREATED where idPerson = ? order by out.creationDate DESC";
                System.out.println("\n========= LAST POST =========");
                OResultSet res = db.query(subQuery,personId);
                if (res.hasNext()) {
                    System.out.println("Last post of customer " + personId + ":");
                    System.out.println(res.stream().findFirst().get());
                }
            }
            rsB2.close();

            System.out.println("\n========= PERSONS =========");
            Map dictB2 = new HashMap<String,Long>();
            System.out.println(customersB1);
            String q2 = "SELECT COUNT(gender) as cptG, gender from Customer where id in ? group by gender order by cptG DESC";
            OResultSet r2 = db.query(q2, customersB1);
            while (r2.hasNext()) {
                OResult optional = r2.next();
                dictB2.put((String) optional.getProperty("gender"),(Long) optional.getProperty("cptG") );
            }
            if ((long) dictB2.get("male") > (long) dictB2.get("female")){
                System.out.println("There are more male customers (" + dictB2.get("male") + ") than female ones (" + dictB2.get("female") + ")");
            }
            else if ((long) dictB1.get("male") < (long) dictB2.get("female")){
                System.out.println("There are more female customers (" + dictB2.get("female") + ") than male ones (" + dictB2.get("male") + ")");
            } else {
                System.out.println("There is as much male customers as female ones (" + dictB2.get("male") + ")");
            }
        }

        if (j==3) {
            String queryB3 = "select out.PersonId as PersonId from Orderline where in.asin in (select asin from Product WHERE OUT(\"IsFromBrand\").Vendor = ?";
            OResultSet rsB3 = db.query(queryB1, brands.get(2));
            System.out.println("\n========= CUSTOMERS BY BRAND =========");
            ArrayList<String> personsB3 = new ArrayList<>();
            while (rsB3.hasNext()) {
                OResult optional = rsB3.next();
                String personId = optional.getProperty("PersonId");
                personsB3.add(optional.getProperty("PersonId"));

                String subQuery = "SELECT out.idPost, out.content, out.creationDate from HASCREATED where idPerson = ? order by out.creationDate DESC";
                System.out.println("\n========= LAST POST =========");
                OResultSet res = db.query(subQuery,personId);
                if (res.hasNext()) {
                    System.out.println("Last post of customer " + personId + ":");
                    System.out.println(res.stream().findFirst().get());
                }
            }
            rsB3.close();

            System.out.println("\n========= PERSONS =========");
            Map dictB3 = new HashMap<String,Long>();
            System.out.println(customersB1);
            String q3 = "SELECT COUNT(gender) as cptG, gender from Customer where id in ? group by gender order by cptG DESC";
            OResultSet r3 = db.query(q3, customersB1);
            while (r3.hasNext()) {
                OResult optional = r3.next();
                dictB3.put((String) optional.getProperty("gender"),(Long) optional.getProperty("cptG") );
            }
            if ((long) dictB3.get("male") > (long) dictB3.get("female")){
                System.out.println("There are more male customers (" + dictB3.get("male") + ") than female ones (" + dictB3.get("female") + ")");
            }
            else if ((long) dictB1.get("male") < (long) dictB3.get("female")){
                System.out.println("There are more female customers (" + dictB3.get("female") + ") than male ones (" + dictB3.get("male") + ")");
            } else {
                System.out.println("There is as much male customers as female ones (" + dictB3.get("male") + ")");
            }
        }
    }

    public static void query4(ODatabaseSession db) {
        /**
         * Query 4. Find the top-2 persons who spend the highest amount of money in orders. Then for
         each person, traverse her knows-graph with 3-hop to find the friends, and finally return the
         common friends of these two persons.
         * */
        String query = "SELECT PersonId FROM (SELECT PersonId, SUM(TotalPrice) as amountSpent FROM Order GROUP BY PersonId ORDER BY amountSpent DESC LIMIT 2)";
        OResultSet rs = db.query(query);
        ArrayList<String> listCustomers = new ArrayList<>();
        while (rs.hasNext()) {
            listCustomers.add(rs.next().getProperty("PersonId").toString());
        }
        String sql = "SELECT intersect((TRAVERSE OUT(\"Knows\") FROM ( SELECT FROM Customer WHERE id = ? ) MAXDEPTH 3)," +
                "(TRAVERSE OUT(\"Knows\") FROM ( SELECT FROM Customer WHERE id = ?  ) MAXDEPTH 3))";

        OResultSet result = db.query(sql, listCustomers.get(0), listCustomers.get(1));

        ArrayList<String> customers = new ArrayList<>();
        while(result.hasNext()) {
            String customersOridsString = result.next().getProperty("intersect(($$$SUBQUERY$$_0), ($$$SUBQUERY$$_1))").toString();
            customersOridsString = customersOridsString.replace("[", "");
            customersOridsString = customersOridsString.replace("]", "");
            String[] customersOrids  = customersOridsString.split(",");

            System.out.println("People found in the interesction of the friends graph of 2 customers");
            for(String orid : customersOrids)
            {
                String query5 = "SELECT firstName, lastName FROM Customer WHERE @rid = ?";
                OResultSet firstLastName = db.query(query5, orid);
                System.out.println(firstLastName.stream().findFirst().get());
            }
        }
    }


    /* Query 5 :
Given a start customer and a product category, find persons who are this customer's
friends within 3-hop friendships in Knows graph, besides, they have bought products in the
given category. Finally, return feedback with the 5-rating review of those bought products.
        */
    public static void query5(ODatabaseSession db, String idTag, String idCustomer) {
        String query1 = "TRAVERSE OUT(\"Knows\") FROM ( SELECT FROM Customer WHERE id = ?  ) MAXDEPTH 3";
        OResultSet rs = db.query(query1, idCustomer);

        ArrayList<List> listProducts = new ArrayList<>();
        ArrayList<String> listIdTags = new ArrayList<>();
        ArrayList<OVertex> listCustomersBought = new ArrayList<>();
        ArrayList<String> listFeedback = new ArrayList<>();

        int i = 0;
        while (rs.hasNext()) {
            OVertex prochainCustomer = rs.next().getVertex().get();
            String query2 = "SELECT tags.idTag FROM (SELECT OUT(\"Orderline\").OUT(\"ProductTag\") as tags FROM Order WHERE PersonId = ? )";
            OResultSet rs2 = db.query(query2, prochainCustomer.getProperty("id").toString());

            while(rs2.hasNext()){
                listIdTags.addAll(rs2.next().getProperty("tags.idTag"));
                if(listIdTags.contains(idTag)){
                    String query3 = "SELECT OUT(\"Orderline\").asin as produits FROM Order WHERE PersonId = ?";
                    OResultSet rs3 = db.query(query3, prochainCustomer.getProperty("id").toString());
                    while(rs3.hasNext()){
                        listProducts.add(rs3.next().getProperty("produits"));
                    }
                    listCustomersBought.add(prochainCustomer);
                }
            }
            if (i == 5){
                break;
            }
            i = i+1;
        }

        for (List produitAsin: listProducts) {

            String query4 = "SELECT comment FROM Feedback WHERE productAsin = ?";
            OResultSet rs4 = db.query(query4, produitAsin.get(0));

            while (rs4.hasNext()){
                listFeedback.add(rs4.next().toString());
            }
        }

        List<OVertex> resultListCustomers = listCustomersBought.stream().distinct().toList();
        for (OVertex customer: resultListCustomers) {
            System.out.println(customer.getProperty("firstName").toString() +" "+ customer.getProperty("lastName").toString());
        }

        List<String> listFeedbackResult = listFeedback.subList(0,10);
        for (String comment: listFeedbackResult) {
            System.out.println(comment);
        }
    }

    public static void query6(ODatabaseSession db, OVertex customer1, OVertex customer2) {
        /**
         Query 6. Given customer 1 and customer 2, find persons in the shortest path between them
         in the subgraph, and return the TOP 3 best sellers from all these persons' purchases.
         **/
        String query = "SELECT shortestPath( ? , ? , \"OUT\", \"knows\") as sp";
        OResultSet rs = db.query(query, customer1.getIdentity(), customer2.getIdentity());

        String customersOridsString = rs.stream().findFirst().get().getProperty("sp").toString();

        customersOridsString = customersOridsString.replace("[", "");
        customersOridsString = customersOridsString.replace("]", "");
        String[] customersOrids = customersOridsString.split(",");

        List<String> listIDs = new ArrayList<>();
        for (String orid : customersOrids) {
            String query5 = "SELECT id FROM Customer WHERE @rid = ?";
            OResultSet ids = db.query(query5, orid);
            listIDs.add(ids.stream().findFirst().get().getProperty("id").toString());
        }

        String query2 = " SELECT OUT(\"Orderline\").asin as products FROM Order " +
                "WHERE PersonId = ? OR PersonId = ? OR PersonId = ? OR PersonId = ? GROUP BY products";

        OResultSet rs2 = db.query(query2, listIDs.get(0), listIDs.get(1), listIDs.get(2), listIDs.get(3));
        List<String> listProducts = new ArrayList<>();

        while (rs2.hasNext()){

            String productsString = rs2.next().getProperty("products").toString();
            productsString = productsString.replace("[", "");
            productsString = productsString.replace("]", "");
            String[] productsIds = productsString.split(",");
            listProducts.add(productsIds[0]);
        }

        Map<String, Long> counts =
                listProducts.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));


        List ordered = counts.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue()).toList();

        System.out.println("Customers found in shortestPath");
        for(String orid : customersOrids)
        {
            String query5 = "SELECT firstName, lastName FROM Customer WHERE @rid = ?";
            OResultSet firstLastName = db.query(query5, orid);
            System.out.println(firstLastName.stream().findFirst().get());
        }

        System.out.println("TOP 3 sales of the products bought by the above customers");
        System.out.println(ordered.get(ordered.size()-1) + " / " +
                ordered.get(ordered.size()-2) + " / " + ordered.get(ordered.size()-3));

    }

    // We get the ratio of negative reviews for the Products of the given Vendor where the sales
    // of the Product have been declining for the last quarter
    public static HashMap<String, Float> query7(ODatabaseSession db, String vendor){
        HashMap<String, Float> res = new HashMap<>();
        ArrayList<OVertex> declining_products = new ArrayList<>();

        // We get the products of the vendor
        String query = "SELECT * from Product where out(\"IsFromBrand\").Vendor = ?";
        OResultSet rs = db.query(query, vendor);

        while(rs.hasNext()){

            Optional<OVertex> product = rs.next().getVertex();
            if(product.isPresent()){

                // We check if that product has declining sales
                query = "SELECT * from Orderline where in.asin = ? and out.OrderDate between ? and ?";
                OResultSet rs2 = db.query(query, (String)product.get().getProperty("asin"), "2021-06-15", "2022-06-15");
                long current_sales = rs2.stream().count();

                query = "SELECT * from Orderline where in.asin = ? and out.OrderDate between ? and ?";
                rs2 = db.query(query, (String)product.get().getProperty("asin"), "2020-06-15", "2021-06-15");
                long last_sales = rs2.stream().count();

                if(current_sales < last_sales){
                    declining_products.add(product.get());
                }
                rs2.close();
            }
        }
        rs.close();

        // We now get the Feedbacks  of these Products
        ArrayList<OVertex> feedbacks = new ArrayList<>();
        for(OVertex p :declining_products){
            float neg = 0;
            float pos = 0;
            query = "select * from Feedback where productAsin = ?";
            OResultSet rs3 = db.query(query, (String)p.getProperty("asin"));
            while(rs3.hasNext()) {
                Optional<OVertex> fo = rs3.next().getVertex();
                if (fo.isPresent()) {
                    OVertex f = fo.get();
                    String text = (String) f.getProperty("comment");
                    // We need to parse the grading in the comment ex: "4.5,blablabla"
                    String grade = "";
                    int cpt = 1;
                    while (text.charAt(cpt) != ",".charAt(0)) {
                        grade = grade + text.charAt(cpt);
                        cpt++;
                    }
                    float sentiment = Float.parseFloat(grade);
                    // We keep the comment if the grading is bad
                    if (sentiment < 2.5) {
                        neg++;
                    } else {
                        pos++;
                    }
                }
                rs3.close();
            }
            if(pos+neg == 0){
                res.put(p.getProperty("asin"), neg);
            } else {
                res.put(p.getProperty("asin"), (pos + neg) / neg);
            }
        }

        return res;
    }

    /*
        Query 8 :
         For all the products of a given category during a given year, compute its total sales
         amount, and measure its popularity in the social media.
    */
    public static void query8(ODatabaseSession db, String year, String category) throws ParseException {
        String query1 = "SELECT asin, IN(\"Orderline\").OrderDate, IN(\"Orderline\").TotalPrice from Product";
        OResultSet rs = db.query(query1);


        while(rs.hasNext()) {
            OResult optional = rs.next();
            ArrayList<String> currentDates = optional.getProperty("IN(\"Orderline\").OrderDate");
            ArrayList<Float> currentAmounts = optional.getProperty("IN(\"Orderline\").TotalPrice");
            String currentAsin = optional.getProperty("asin");
            ArrayList<String> resDates = new ArrayList<>();
            ArrayList<Float> resAmounts = new ArrayList<>();
            float totalSales = 0;

            if(!currentDates.isEmpty()){

                // STEP 1 : FILTERING THE DATES
                for(int i = 0; i<currentDates.size(); i++){
                    Boolean bool = (new SimpleDateFormat("yyyy-MM-dd").parse(currentDates.get(i))).before( new SimpleDateFormat("yyyy-MM-dd").parse(year+"-12-31"));
                    Boolean bool2 = (new SimpleDateFormat("yyyy-MM-dd").parse(year+"-01-01").before( new SimpleDateFormat("yyyy-MM-dd").parse(currentDates.get(i))));
                    if(bool&&bool2) {
                        resDates.add(currentDates.get(i));
                        resAmounts.add(currentAmounts.get(i));
                    }
                }

                // STEP 2 : FILTERING THE CATEGORIES
                String query2 = "SELECT asin, OUT(\"ProductTag\").name, OUT(\"ProductTag\").idTag FROM Product where asin = ?";
                OResultSet rs2 = db.query(query2,currentAsin);
                OResult optional2 = rs2.next();
                ArrayList<String> currentCategories = optional2.getProperty("OUT(\"ProductTag\").name");
                ArrayList<String> currentPost = optional2.getProperty("OUT(\"ProductTag\").idTag");

                if(currentCategories.contains(category)){
                    for(Float money : resAmounts){
                        totalSales = totalSales + money;
                    }

                    System.out.println("FOR THE PRODUCT n° " + currentAsin);
                    System.out.println("------ DURING THE YEAR                :  " + year);
                    System.out.println("------ BEING IN THE CATEGORY          :  " + category);
                    System.out.println("------ TOTAL SALES AMOUNT             :  " + totalSales);
                    System.out.println("------ POPULARITY IN THE SOCIAL MEDIA :  " + currentPost.size() + " posts");

                }



            }
        }
    }

    public static void query10(ODatabaseSession db) {
        String query10="SELECT id, max(OUT(\"EdgeCustomerOrder\").OrderDate) as Recency, " +
                "COUNT(OUT(\"EdgeCustomerOrder\").OrderId) as Frequency, SUM(OUT(\"EdgeCustomerOrder\").TotalPrice) as " +
                "Monetary FROM Customer Where id IN (Select id, count(id) as counts from (Select OUT('HasCreated').id " +
                "as id From Post Where creationDate >= date('05-01-2010', 'dd-MM-yyyy') Group by id) " +
                "Order by counts DESC limit 10) GROUP BY id";

        OResultSet result = db.query(query10);
        while (result.hasNext()){
            System.out.println(result.next());
        }
        result.close();
    }

        public static void main(String[] args) throws ParseException, IOException, org.json.simple.parser.ParseException, ParserConfigurationException, SAXException {
        OrientDB orientDB = new OrientDB("remote:localhost/", OrientDBConfig.defaultConfig());

        ODatabaseSession db = orientDB.open("testdb", "root", "2610");

        /* ------------------------ */
        /* -- PARTIE 5 : QUERIES -- */
        /* ------------------------ */

        // QUERY 1

        /*query1(db, "4145", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2011-09-15 00:00:00") );
        System.out.println("done!");
        db.close();
        */



        // QUERY 2
        //query2(db,"B005FUKW6M","2001-12-18", "2021-01-18");



          //QUERY 8
          //  query8(db,"2018");

            //QUERY 9
            query9(db, "Australia");


        // graphLoader.query4();
        // graphLoader.query4();


        //QUERY 8
        //query8(db,"2018", " Levis");

        /* ---------------------- */
        // -- LOADING THE DATA -- */
        /* ---------------------- */
        /*
        // LOADING THE PRODUCT DATA
        JsonsLoader jsonLoader = new JsonsLoader(db);
        jsonLoader.load();
        jsonLoader.createOutEdges();

        InvoiceLoader invoiceLoader = new InvoiceLoader(db);
        invoiceLoader.load();

        //FeedbackLoader.chargementFeedback(db);

        // LOADING THE CUSTOMER DATA
        CustomerLoader customerLoader = new CustomerLoader(db);
        customerLoader.load();
        customerLoader.loadEdges();
        // LOADING THE VENDOR DATA
        VendorLoader vendorLoader = new VendorLoader(db);
        vendorLoader.load();
     */
        GraphLoader graphLoader = new GraphLoader(db);
       // graphLoader.createEdgeProductTag();



        // Créer un post
            /*
        graphLoader.createPost("1399511627255", "image.png",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-11-18 07:13:13.099+0000"),
                "43.290.55.178", "Chrome", "fr", "A new post", "350");

        // Mise à jour post
        ODocument post = new ODocument("Post");
        post.field("idPost", "1399511627255");
        post.field("imageFile", "anotherImage.png");
        post.field("creationDate",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-23 09:13:13.099+0000"));
        post.field("locationIP", "43.290.55.178");
        post.field("browerUsed", "Opera");
        post.field("language", "SP");
        post.field("content", "A new post 2");
        post.field("length", "890");

        graphLoader.updatePost(post);

        post.delete().save();
        */

        /** Query 4 **/

        //Main.query4(db);
            /** Query 6 **/

            /*
            OVertex customer1 = null;
            OVertex customer2 = null;
                String query = "SELECT * FROM Customer LIMIT 20";
                OResultSet rs = db.query(query);
                List<OVertex> customersList = rs.vertexStream().toList();
                customer1 = customersList.get(0);
                customer2 = customersList.get(7);
                rs.close();

            Main.query6(db, customer1, customer2);
        */
           // Main.query10(db);


            /* TEST update edge HasTag
            OVertex post = null;
            OVertex tag = null;
            OEdge hasTagEdge = null;


            ODocument newpost = new ODocument("Post");
            newpost.field("idPost", "1339511621255");
            newpost.field("imageFile", "anotherImage.png");
            newpost.field("creationDate",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-23 09:13:13.099+0000"));
            newpost.field("locationIP", "43.290.55.178");
            newpost.field("browerUsed", "Opera");
            newpost.field("language", "SP");
            newpost.field("content", "A new post 2");
            newpost.field("length", "890");

            graphLoader.createPost(newpost.getProperty("idPost").toString(), newpost.getProperty("imageFile").toString(),
                   newpost.getProperty("creationDate"), newpost.getProperty("locationIP").toString(),
                    newpost.getProperty("browerUsed").toString(), newpost.getProperty("language").toString(),
                    newpost.getProperty("content").toString(), newpost.getProperty("length").toString());

            ODocument newTag= new ODocument("Tag");
            newTag.field("idTag", "22100");
            newTag.field("name", "NewTag");

            graphLoader.createTag(newTag.getProperty("idTag").toString(), newTag.getProperty("name").toString());


            String queryPostNew = "SELECT * FROM Post WHERE idPost = ? LIMIT 20";
            OResultSet rsPostnew = db.query(queryPostNew, newpost.getProperty("idPost").toString());
            OVertex newpostVertex = rsPostnew.vertexStream().findFirst().get();

            String queryTagNew = "SELECT * FROM Tag WHERE idTag = ? LIMIT 20";
            OResultSet rsTagNew = db.query(queryTagNew, newTag.getProperty("idTag").toString());
            OVertex newtagVertex = rsTagNew.vertexStream().findFirst().get();


            String queryPost = "SELECT * FROM Post WHERE idPost = ? LIMIT 20";
            OResultSet rsPost = db.query(queryPost, "687194767630");
            post = rsPost.vertexStream().findFirst().get();

            String queryTag = "SELECT * FROM Tag WHERE idTag = ? LIMIT 20";
            OResultSet rsTag = db.query(queryTag, "3198");
            tag = rsTag.vertexStream().findFirst().get();

            graphLoader.updateHasTag(post, tag, newpostVertex, newtagVertex);

            newtagVertex.delete().save();
            newpostVertex.delete().save();


             */
    }


}
