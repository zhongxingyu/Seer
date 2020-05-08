 import java.io.File;
 import java.net.MalformedURLException;
 
 import com.yourmediashelf.fedora.client.FedoraClient;
 import com.yourmediashelf.fedora.client.FedoraClientException;
 import com.yourmediashelf.fedora.client.FedoraCredentials;
 import com.yourmediashelf.fedora.client.request.AddDatastream;
 import com.yourmediashelf.fedora.client.request.FedoraRequest;
 import com.yourmediashelf.fedora.client.request.GetObjectXML;
 import com.yourmediashelf.fedora.client.request.Ingest;
 import com.yourmediashelf.fedora.client.request.PurgeObject;
 import com.yourmediashelf.fedora.client.response.AddDatastreamResponse;
 import com.yourmediashelf.fedora.client.response.FedoraResponse;
 import com.yourmediashelf.fedora.client.response.GetObjectProfileResponse;
 import com.yourmediashelf.fedora.client.response.IngestResponse;
 
 public class Fedora7TestRunner {
 
     private static String FCREPO_URL = "http://localhost:8080/fedora/";
 
     private static String FCREPO_USER = "fedoraAdmin";
 
     private static String FCREPO_PWD = "fed";
     
     public static void main(String[] args) {
         Fedora7TestRunner runner = new Fedora7TestRunner();
 
         FedoraClient cl = null;
         try {
             FedoraCredentials creds= new FedoraCredentials(FCREPO_URL, FCREPO_USER, FCREPO_PWD);
             cl = new FedoraClient(creds);
         } catch (MalformedURLException e1) {
             e1.printStackTrace();
             return;
         }
         
         FedoraRequest.setDefaultClient(cl);
 
         try {
             runner.purgeTest();
             runner.ingestTest();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void purgeTest() throws Exception {
         FedoraResponse getResp = new GetObjectXML("test:fedora-7")
             .execute();
         if (getResp.getStatus() == 200){
             System.out.println("purging old fedora test object \"test:fedora-7\"");
             FedoraResponse respPurge = new PurgeObject("test:fedora-7")
             .execute();
         }
     }
 
     private void ingestTest() throws Exception {
         File dir = new File(this.getClass().getClassLoader().getResource("ingest_package").toURI());
         IngestResponse respIngest =
                 new Ingest("test:fedora-7").label("Test for FEDORA-7")
                 .execute();
         String[] names = dir.list();
         for(int i =0;i<names.length;i++){
             String name = names[i];
             if (name.endsWith("doc.xml")){
                 continue;
             }
             AddDatastreamResponse respAdd =
                     new AddDatastream("test:fedora-7", "test_ds_" + name.substring(0,name.lastIndexOf('.')))
                         .controlGroup("M")
                         .content(new File(dir,name))
                         .execute();
             if (i % 50 == 1){
                 System.out.println(i + "/" + names.length);
             }
         }
         
     }
 }
