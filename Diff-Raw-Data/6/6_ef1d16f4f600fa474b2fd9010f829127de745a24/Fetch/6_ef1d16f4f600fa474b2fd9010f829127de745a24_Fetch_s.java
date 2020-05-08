 package fr.univnantes.atal.web.piubella.servlets;
 
 import au.com.bytecode.opencsv.CSVReader;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.files.AppEngineFile;
 import com.google.appengine.api.files.FileService;
 import com.google.appengine.api.files.FileServiceFactory;
 import com.google.appengine.api.files.FileWriteChannel;
 import fr.univnantes.atal.web.piubella.app.Constants;
 import fr.univnantes.atal.web.piubella.model.Address;
 import fr.univnantes.atal.web.piubella.model.CollectDay;
 import fr.univnantes.atal.web.piubella.model.JSONInfo;
 import fr.univnantes.atal.web.piubella.persistence.PMF;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerator;
 
 /**
  * Servlet fetching data on NOD.
  * 
  * This servlet has two roles. The first one is to store the current NOD dataset
  * in the datastore. The second one is to create a blob in the blobstore
  * containing a filtered dataset for use by the client in a typeahead for
  * example.
  */
 public class Fetch extends HttpServlet {
 
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         PersistenceManager pm = PMF.get().getPersistenceManager();
         Query q = pm.newQuery(JSONInfo.class);
         try {
             FileService fileService = FileServiceFactory.getFileService();
 
             JSONInfo jsonInfo;
             List<JSONInfo> results =
                     (List<JSONInfo>) q.execute();
             AppEngineFile file;
             if (results.isEmpty()) {
                 jsonInfo = new JSONInfo();
                 pm.makePersistent(jsonInfo);
             } else {
                 jsonInfo = results.get(0);
             }
             file = fileService.createNewBlobFile("text/plain");
 
             boolean lock = true;
             FileWriteChannel writeChannel = fileService.openWriteChannel(file, lock);
 
             JsonFactory f = new JsonFactory();
             try (OutputStream os = Channels.newOutputStream(writeChannel);
                     JsonGenerator g = f.createJsonGenerator(os)) {
                 g.writeStartObject();
                 g.writeArrayFieldStart("data");
 
                 try (PrintWriter out = response.getWriter()) {
                     out.println(file.getFullPath());
                 }
                 q = pm.newQuery(Address.class);
                 q.deletePersistentAll();
                 URL url = new URL(Constants.NOD_DATASET_CSV_URL);
                 CSVReader reader = new CSVReader(new InputStreamReader(url.openStream(), "UTF-8"));
                 String[] nextLine;
                 Collection<Address> addresses = new ArrayList<>();
                 while ((nextLine = reader.readNext()) != null) {
                     if (nextLine.length < 13) {
                         System.err.println("Data import failed.");
                         System.err.println("Couldn't find 13 fields in the dataset "
                                 + "which means I don't know what to do.");
                         return;
                     } else {
                         String blue = nextLine[10],
                                 obsCollect = nextLine[12];
                         if (!blue.isEmpty() && obsCollect.isEmpty()) {
                             String street = nextLine[1],
                                     obsStreet = nextLine[9],
                                     yellow = nextLine[11];
                             Set<CollectDay> blueDays = new HashSet<>(),
                                     yellowDays = new HashSet<>();
                             Boolean singleCollect = false;
                             if (yellow.isEmpty()) {
                                 singleCollect = true;
                             }
                             if (blue.contains("lundi")) {
                                 blueDays.add(CollectDay.MONDAY);
                             }
                             if (blue.contains("mardi")) {
                                 blueDays.add(CollectDay.TUESDAY);
                             }
                             if (blue.contains("mercredi")) {
                                 blueDays.add(CollectDay.WEDNESDAY);
                             } else if (blue.contains("merc_sem_impaires")) {
                                 blueDays.add(CollectDay.WEDNESDAY_ODD);
                             } else if (blue.contains("merc_sem_paires")) {
                                 blueDays.add(CollectDay.WEDNESDAY_EVEN);
                             }
                             if (blue.contains("jeudi")) {
                                 blueDays.add(CollectDay.THURSDAY);
                             }
                             if (blue.contains("vendredi")) {
                                 blueDays.add(CollectDay.FRIDAY);
                             }
                             if (blue.contains("samedi")) {
                                 blueDays.add(CollectDay.SATURDAY);
                             }
                             if (blue.contains("dimanche")) {
                                 blueDays.add(CollectDay.SUNDAY);
                             }
                             if (yellow.contains("lundi")) {
                                 yellowDays.add(CollectDay.MONDAY);
                             }
                             if (yellow.contains("mardi")) {
                                 yellowDays.add(CollectDay.TUESDAY);
                             }
                             if (yellow.contains("mercredi")) {
                                 yellowDays.add(CollectDay.WEDNESDAY);
                             } else if (yellow.contains("merc_sem_impaires")) {
                                 yellowDays.add(CollectDay.WEDNESDAY_ODD);
                             } else if (yellow.contains("merc_sem_paires")) {
                                 yellowDays.add(CollectDay.WEDNESDAY_EVEN);
                             }
                             if (yellow.contains("jeudi")) {
                                 yellowDays.add(CollectDay.THURSDAY);
                             }
                             if (yellow.contains("vendredi")) {
                                 yellowDays.add(CollectDay.FRIDAY);
                             }
                             if (yellow.contains("samedi")) {
                                 yellowDays.add(CollectDay.SATURDAY);
                             }
                             if (yellow.contains("dimanche")) {
                                 yellowDays.add(CollectDay.SUNDAY);
                             }
                             Address address = new Address();
                            address.setStreet(street + (obsStreet.isEmpty()
                                     ? ""
                                    : " " + obsStreet));
                             address.setBlueDays(blueDays);
                             address.setYellowDays(yellowDays);
                             addresses.add(address);
                             g.writeStartArray();
                             g.writeString(street);
                             g.writeStartArray();
                             for (CollectDay cd : blueDays) {
                                 g.writeNumber(cd.ordinal());
                             }
                             g.writeEndArray();
                             if (!yellowDays.isEmpty()) {
                                 g.writeStartArray();
                                 for (CollectDay cd : yellowDays) {
                                     g.writeNumber(cd.ordinal());
                                 }
                                 g.writeEndArray();
                             }
                             g.writeEndArray();
                         }
                     }
                     if (addresses.size() > 100) {
                         pm.makePersistentAll(addresses);
                         addresses.clear();
                     }
                 }
                 g.writeEndArray();
                 g.writeEndObject();
             }
 
             writeChannel.closeFinally();
             if (jsonInfo.getBlobKey() != null) {
                 BlobstoreService blobstoreService =
                         BlobstoreServiceFactory.getBlobstoreService();
                 blobstoreService.delete(jsonInfo.getBlobKey());
             }
             jsonInfo.setBlobKey(fileService.getBlobKey(file));
         } finally {
             q.closeAll();
         }
     }
 }
