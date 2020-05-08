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
        //create connection, router configs
        CaringoConfigConnection connectionConfig = new CaringoConfigConnection("libstor.grainger.illinois.edu", "medusa.grainger.illinois.edu", "open");
        FedoraContentRouterConfig contentRouterConfig = new FedoraContentRouterConfig("172.22.70.6", 8080, "development-test-repo");
 
         //Get blobstores
        FedoraObjectBlobStore objects = new FedoraObjectBlobStore(URI.create("objects"), "development-test-repo", connectionConfig, contentRouterConfig);
        FedoraDatastreamBlobStore datastreams = new FedoraDatastreamBlobStore(URI.create("datastreams"), "development-test-repo", connectionConfig, contentRouterConfig);
 
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
