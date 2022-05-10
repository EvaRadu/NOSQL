import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.OVertex;
import graph.Person;

public class Main {

    private ODatabaseDocument db;
    public void newGraph(){
        OVertex person = db.newInstance(Person.class.getName());
        person.save();
    }
    public static void main(String[] args) {

    }
}
