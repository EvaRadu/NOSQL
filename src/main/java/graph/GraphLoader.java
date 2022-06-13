package graph;

import com.opencsv.CSVReader;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GraphLoader {

    public static void createSocialNetworkGraph(ODatabaseSession db) throws ParseException {

        /*
        if (db.getClass("Person") == null) {
            OClass person = db.createVertexClass("Person");
            person.createProperty("id", OType.STRING);
            person.createProperty("firstName", OType.STRING);
            person.createProperty("lastName", OType.STRING);
            person.createProperty("gender", OType.STRING);
            person.createProperty("birthday", OType.DATE);
            person.createProperty("creationDate", OType.DATE);
            person.createProperty("locationIP", OType.STRING);
            person.createProperty("browserUsed", OType.STRING);
            person.createProperty("place", OType.INTEGER);
            person.createIndex("Person_id_index", OClass.INDEX_TYPE.UNIQUE, "id");
        }
        */
        if (db.getClass("Post") == null) {
            OClass post = db.createVertexClass("Post");
            post.createProperty("idPost", OType.STRING);
            post.createProperty("imageFile", OType.STRING);
            post.createProperty("creationDate", OType.DATE);
            post.createProperty("locationIP", OType.STRING);
            post.createProperty("browserUsed", OType.STRING);
            post.createProperty("language", OType.STRING);
            post.createProperty("content", OType.STRING);
            post.createProperty("length", OType.INTEGER);
            post.createIndex("Post_id_index", OClass.INDEX_TYPE.UNIQUE, "idPost");
        }

        if (db.getClass("Tag") == null) {
            OClass tag = db.createVertexClass("Tag");
            tag.createProperty("idTag", OType.STRING);
            tag.createProperty("name", OType.STRING);
            tag.createIndex("Tag_id_index", OClass.INDEX_TYPE.UNIQUE, "idTag");
        }

        if (db.getClass("Knows") == null) {
            OClass knows = db.createEdgeClass("Knows");
            knows.createProperty("idPerson", OType.STRING);
            knows.createProperty("idPerson2", OType.STRING);
            knows.createProperty("creationDate", OType.DATE);
            knows.createIndex("knows_index", OClass.INDEX_TYPE.NOTUNIQUE,"idPerson","idPerson2");
        }

        if (db.getClass("HasTag") == null) {
            OClass hasTag = db.createEdgeClass("HasTag");
            hasTag.createProperty("idPost", OType.STRING);
            hasTag.createProperty("idTag", OType.STRING);
            hasTag.createIndex("hastag_index", OClass.INDEX_TYPE.NOTUNIQUE_HASH_INDEX, "idPost","idTag");
        }

        if (db.getClass("HasInterest") == null) {
            OClass hasInterest = db.createEdgeClass("HasInterest");
            hasInterest.createProperty("idPerson", OType.STRING);
            hasInterest.createProperty("idTag", OType.STRING);
            hasInterest.createIndex("hasinterest_index", OClass.INDEX_TYPE.NOTUNIQUE_HASH_INDEX, "idPerson","idTag");
        }

        if (db.getClass("HasCreated") == null) {
            OClass hasCreated = db.createEdgeClass("HasCreated");
            hasCreated.createProperty("idPost", OType.STRING);
            hasCreated.createProperty("idPerson", OType.STRING);
            hasCreated.createIndex("hasicreated_index", OClass.INDEX_TYPE.NOTUNIQUE_HASH_INDEX, "idPost","idPerson");
        }
        loadSocialNetworkData(db);
    }

    public static void loadSocialNetworkData(ODatabaseSession db) throws ParseException {
        List<List<String>> records = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/Customer/person_0_0.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<List<String>> records2 = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/SocialNetwork/post_0_0.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records2.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<List<String>> records3 = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/SocialNetwork/tag.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records3.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<List<String>> records4 = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/SocialNetwork/person_knows_person_0_0.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records4.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<List<String>> records5 = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/SocialNetwork/post_hasTag_tag_0_0.csv"));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records5.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<List<String>> records6 = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/SocialNetwork/person_hasInterest_tag_0_0.csv"))) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records6.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<List<String>> records7 = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader("DATA/SocialNetwork/post_hasCreator_person_0_0.csv"))) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records7.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //loadPost(records2, db);
        //loadTag(records3, db);
        //loadKnows(records4, db);
        //loadHasTag(records5, db);
        //loadHasInterest(records6, db);
        //loadHasCreated(records7, db);
    }

    private static void loadPerson(List<List<String>> records, ODatabaseSession db) throws ParseException {
        for(int p=1; p<records.size(); p++){
            // split string by no space
            String[] line = records.get(p).toString().split("\\|");
            // Now convert string into ArrayList
            line[0] =  line[0].replace("[", "");
            line[line.length -1] =  line[line.length -1].replace("]", "");

            ArrayList<String> lineArray = new ArrayList<>(Arrays.asList(line));

            String id = lineArray.get(0);
            String firstName = lineArray.get(1);
            String lastName = lineArray.get(2);
            String gender = lineArray.get(3);
            String birthdayString = lineArray.get(4);
            Date birthday =new SimpleDateFormat("yyyy-MM-dd").parse(birthdayString);
            String creationDateString = lineArray.get(5);
            creationDateString = creationDateString.replace("T", " ");
            Date creationDate =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(creationDateString);
            String locationIP = lineArray.get(6);
            String browserUsed = lineArray.get(7);
            String place = lineArray.get(8);

            String query = "SELECT * from Person where id = ?";
            OResultSet rs = db.query(query, id);

            // Quand on appel rs.elementStream().count()rs.elementStream().count()
            // Il donne la première fois seulement comme FALSE et après TRUE sur la même record !!!!!!
            boolean createOrnot = rs.elementStream().count() == 0;
            if(createOrnot) {
                createPerson(db, id, firstName, lastName, gender, birthday,creationDate, locationIP, browserUsed, place );
            }
            }
        }

    private static void createPerson(ODatabaseSession db, String id, String firstname, String lastname,
                                         String gender, Date birthday, Date creationDate,
                                         String locationIP, String browserUsed, String place) {
        OVertex person = db.newVertex("Person");
        person.setProperty("id", id);
        person.setProperty("firstName",firstname);
        person.setProperty("lastName", lastname);
        person.setProperty("gender", gender);
        person.setProperty("birthday", birthday);
        person.setProperty("creationDate", creationDate);
        person.setProperty("locationIP", locationIP);
        person.setProperty("browserUsed", browserUsed);
        person.setProperty("place", Integer.parseInt(place));
        person.save();
    }

    private static void loadPost(List<List<String>> records, ODatabaseSession db) throws ParseException {
        for(int p=1; p<records.size(); p++){

            String[] line = records.get(p).toString().split("\\|");

            line[0] =  line[0].replace("[", "");
            line[line.length -1] =  line[line.length -1].replace("]", "");

            ArrayList<String> lineArray = new ArrayList<>(Arrays.asList(line));

            String id = lineArray.get(0);
            String imageFile = lineArray.get(1);
            String creationDateString = lineArray.get(2);
            creationDateString = creationDateString.replace("T", " ");
            Date creationDate =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(creationDateString);
            String locationIP = lineArray.get(3);
            String browserUsed = lineArray.get(4);
            String language = lineArray.get(5);
            String content = lineArray.get(6);
            String length = lineArray.get(7);

            String query = "SELECT * from Post where idPost = ?";
            OResultSet rs = db.query(query, id);

            boolean createOrnot = rs.elementStream().count() == 0;
            if(createOrnot) {
                createPost(db, id, imageFile, creationDate, locationIP, browserUsed,language, content, length );
            }
        }
    }

    private static void createPost(ODatabaseSession db, String id, String imageFile, Date creationDate,
                                   String locationIP, String browserUsed,
                                   String language, String content, String length) {
        OVertex post = db.newVertex("Post");
        post.setProperty("idPost", id);
        post.setProperty("imageFile",imageFile);
        post.setProperty("creationDate", creationDate);
        post.setProperty("locationIP", locationIP);
        post.setProperty("browserUsed", browserUsed);
        post.setProperty("language", language);
        post.setProperty("content", content);
        post.setProperty("length", Integer.parseInt(length));
        post.save();
    }


    private static void loadTag(List<List<String>> records, ODatabaseSession db) throws ParseException {
        for(int p=1; p<records.size(); p++){
            String[] line = records.get(p).toString().split(",");

            line[0] =  line[0].replace("[", "");
            line[line.length -1] =  line[line.length -1].replace("]", "");

            ArrayList<String> lineArray = new ArrayList<>(Arrays.asList(line));
            String id = lineArray.get(0);
            String name = lineArray.get(1);

            String query = "SELECT * from Tag where idTag = ?";
            OResultSet rs = db.query(query, id);

            // Quand on appel rs.elementStream().count()rs.elementStream().count()
            // Il donne la première fois seulement comme FALSE et après TRUE sur la même record !!!!!!
            boolean createOrnot = rs.elementStream().count() == 0;
            if(createOrnot) {
                createTag(db, id, name);
            }
        }
    }

    private static void createTag(ODatabaseSession db, String id, String name) {
        OVertex tag = db.newVertex("Tag");
        tag.setProperty("idTag", id);
        tag.setProperty("name",name);
        tag.save();
    }

    private static void loadKnows(List<List<String>> records, ODatabaseSession db) throws ParseException {
        for(int p=1; p<records.size() ; p++){
            String[] line = records.get(p).toString().split("\\|");

            line[0] =  line[0].replace("[", "");
            line[line.length -1] =  line[line.length -1].replace("]", "");

            ArrayList<String> lineArray = new ArrayList<>(Arrays.asList(line));
            String id = lineArray.get(0);
            String id2 = lineArray.get(1);
            String creationDateString = lineArray.get(2);
            creationDateString = creationDateString.replace("T", " ");
            Date creationDate =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(creationDateString);

            String query1 = "SELECT * from Customer where id = ?";
            OResultSet rs1 = db.query(query1, id);
            Optional<OVertex> optionalID = rs1.vertexStream().findFirst();
            OVertex refPerson1 = optionalID.get();

            String query2 = "SELECT * from Customer where id = ?";
            OResultSet rs2 = db.query(query2, id2);
            Optional<OVertex> optionalID2 = rs2.vertexStream().findFirst();
            OVertex refPerson2 = optionalID2.get();

            String query = "SELECT * from Knows where idPerson = ? and idPerson2 = ? ";
            OResultSet rs = db.query(query, id, id2);

            boolean createOrnot = rs.elementStream().count() == 0;
            if(createOrnot) {
                createKnows(db, refPerson1, refPerson2, creationDate);
            }
        }
    }

    private static void createKnows(ODatabaseSession db, OVertex id, OVertex id2, Date creationDate) {
        OElement knows = db.newEdge(id,id2,"Knows");
        knows.setProperty("idPerson", id.getProperty("id"));
        knows.setProperty("idPerson2", id2.getProperty("id"));
        knows.setProperty("creationDate", creationDate);
        knows.save();
    }


    private static void loadHasTag(List<List<String>> records, ODatabaseSession db) throws ParseException {
        for(int p=1; p<records.size() ; p++){
            String[] line = records.get(p).toString().split("\\|");

            line[0] =  line[0].replace("[", "");
            line[line.length -1] =  line[line.length -1].replace("]", "");

            ArrayList<String> lineArray = new ArrayList<>(Arrays.asList(line));
            String idPost = lineArray.get(0);
            String idTag = lineArray.get(1);

            String query1 = "SELECT * from Post where idPost = ?";
            OResultSet rs1 = db.query(query1, idPost);
            Optional<OVertex> optionalID = rs1.vertexStream().findFirst();
            OVertex refPost = optionalID.get();

            String query2 = "SELECT * from Tag where idTag = ?";
            OResultSet rs2 = db.query(query2, idTag);
            Optional<OVertex> optionalID2 = rs2.vertexStream().findFirst();
            OVertex refTag = optionalID2.get();

            String query = "SELECT * from HasTag where idPost = ? and idTag = ? ";
            OResultSet rs = db.query(query, idPost, idTag);

            boolean createOrnot = rs.elementStream().count() == 0;
            if(createOrnot) {
                createHasTag(db, refPost, refTag);
            }
        }
    }

    private static void createHasTag(ODatabaseSession db, OVertex idPost, OVertex idTag) {
        OElement hasTag = db.newEdge(idPost, idTag, "HasTag");
        hasTag.setProperty("idPost", idPost.getProperty("idPost"));
        hasTag.setProperty("idTag", idTag.getProperty("idTag"));
        hasTag.save();
    }

    private static void loadHasInterest(List<List<String>> records, ODatabaseSession db) throws ParseException {
        for(int p=1; p<records.size() ; p++){
            String[] line = records.get(p).toString().split("\\|");

            line[0] =  line[0].replace("[", "");
            line[line.length -1] =  line[line.length -1].replace("]", "");

            ArrayList<String> lineArray = new ArrayList<>(Arrays.asList(line));
            String idPerson = lineArray.get(0);
            String idTag = lineArray.get(1);

            String query1 = "SELECT * from Customer where id = ?";
            OResultSet rs1 = db.query(query1, idPerson);
            Optional<OVertex> optionalID = rs1.vertexStream().findFirst();
            OVertex refPerson = optionalID.get();

            String query2 = "SELECT * from Tag where idTag = ?";
            OResultSet rs2 = db.query(query2, idTag);
            Optional<OVertex> optionalID2 = rs2.vertexStream().findFirst();
            OVertex refTag = optionalID2.get();


            String query = "SELECT * from HasInterest where idPerson = ? and idTag = ? ";
            OResultSet rs = db.query(query, idPerson, idTag);

            boolean createOrnot = rs.elementStream().count() == 0;
            if(createOrnot) {
                createHasInterest(db, refPerson, refTag);
            }
        }
    }

    private static void createHasInterest(ODatabaseSession db, OVertex idPerson, OVertex idTag) {
        OElement hasInterest = db.newEdge(idPerson, idTag, "HasInterest");
        hasInterest.setProperty("idPerson", idPerson.getProperty("id"));
        hasInterest.setProperty("idTag", idTag.getProperty("idTag"));
        hasInterest.save();
    }

    private static void loadHasCreated(List<List<String>> records, ODatabaseSession db) throws ParseException {
        for(int p=1; p<records.size() ; p++){
            String[] line = records.get(p).toString().split("\\|");

            line[0] =  line[0].replace("[", "");
            line[line.length -1] =  line[line.length -1].replace("]", "");

            ArrayList<String> lineArray = new ArrayList<>(Arrays.asList(line));
            String idPost = lineArray.get(0);
            String idPerson = lineArray.get(1);

            String query1 = "SELECT * from Customer where id = ?";
            OResultSet rs1 = db.query(query1, idPerson);
            Optional<OVertex> optionalID = rs1.vertexStream().findFirst();
            OVertex refPerson = optionalID.get();

            String query2 = "SELECT * from Post where idPost = ?";
            OResultSet rs2 = db.query(query2, idPost);
            Optional<OVertex> optionalID2 = rs2.vertexStream().findFirst();
            OVertex refPost = optionalID2.get();

            String query = "SELECT * from HasCreated where idPost = ? and idPerson = ? ";
            OResultSet rs = db.query(query, idPost, idPerson);

            boolean createOrnot = rs.elementStream().count() == 0;
            if(createOrnot) {
                createHasCreated(db, refPost, refPerson);
            }
        }
    }

    private static void createHasCreated(ODatabaseSession db, OVertex idPost, OVertex idPerson) {
        OElement hasCreated = db.newEdge(idPost.getRecord(), idPerson.getRecord(), "HasCreated");
        hasCreated.setProperty("idPost", idPost.getProperty("idPost"));
        hasCreated.setProperty("idPerson", idPerson.getProperty("id"));
        hasCreated.save();
    }
}
