 package nl.astraeus.persistence;
 
 import nl.astraeus.persistence.model.Company;
 import nl.astraeus.persistence.model.CompanyDao;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Collection;
 
 /**
  * User: rnentjes
  * Date: 4/11/12
  * Time: 10:19 PM
  */
 public class TestSerializer {
 
     public static void main(String [] args) throws IOException {
        new TestSerializer();
     }
 
    public TestSerializer() throws IOException {
         CompanyDao dao = new CompanyDao();
 
         Collection<Company> comps = dao.find(0, 1);
 
         if (!comps.isEmpty()) {
             SimpleSerializer simpleSer = new SimpleSerializer();
 
             FileOutputStream fos = new FileOutputStream("test_serializer.txt");
 
             simpleSer.writeObject(fos, comps.iterator().next());
 
             fos.close();
         }
     }
 }
