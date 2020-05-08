 package edu.illinois.medusa;
 
 import java.net.URI;
 import java.util.Iterator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hading
  * Date: 4/5/12
  * Time: 11:39 AM
  * To change this template use File | Settings | File Templates.
  */
 public class EnumTester2 {
 
     public static void main(String[] argv) throws Exception {
 
         //Get blobstores
        FedoraObjectBlobStore objects = new FedoraObjectBlobStore(URI.create("objects"), "tmp/enum-tester-2-config.properties");
        FedoraDatastreamBlobStore datastreams = new FedoraDatastreamBlobStore(URI.create("datastreams"), "tmp/enum-tester-2-config.properties");
 
         //get enumerators
         Iterator<URI> objectIterator = objects.openConnection().listBlobIds(null);
         Iterator<URI> datastreamIterator = datastreams.openConnection().listBlobIds(null);
 
         //enumerate and print
         System.out.println("Object PIDS:");
         while (objectIterator.hasNext()) {
             URI uri = objectIterator.next();
             System.out.println(uri.toString());
         }
         System.out.println("\n");
 
         System.out.println("Datastream PIDS:");
         while (datastreamIterator.hasNext()) {
             URI uri = datastreamIterator.next();
             System.out.println(uri.toString());
         }
         System.out.println("\n");
     }
 }
