 package org.javaswift.filecli;
 
 import com.beust.jcommander.JCommander;
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import org.javaswift.joss.client.factory.AccountFactory;
 import org.javaswift.joss.client.factory.TempUrlHashPrefixSource;
 import org.javaswift.joss.model.Account;
 import org.javaswift.joss.model.Container;
 import org.javaswift.joss.model.FormPost;
 import org.javaswift.joss.model.StoredObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import spark.Request;
 import spark.Response;
 import spark.Route;
 import spark.Spark;
 
 import java.io.*;
 import java.net.URLDecoder;
import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class Main {
 
     public static final Logger LOG = LoggerFactory.getLogger(Main.class);
     public static final String LISTING_PLAIN_FTL = "listing_plain.ftl";
     public static final String LISTING_HTML_FTL = "listing_html.ftl";
 
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

     public static void main(String[] args) {
 
         Main main = new Main();
         Arguments arguments = main.determineArguments(args);
         if (arguments == null) {
             return;
         }
         Account account = main.createAccount(arguments);
 
         LOG.info("Original host: "+account.getOriginalHost());
 
         Container container = account.getContainer(arguments.getContainer());
         if (!container.exists()) {
             container.create();
         }
 
         if (arguments.isServer()) {
             main.startServer(arguments, account);
         } else if (arguments.getFile() != null) { // Upload file
             main.uploadFile(arguments, container);
         } else if (arguments.getDeleteFile() != null) { // Delete file
             main.deleteFile(arguments, container);
         } else { // List files
             main.listFiles(arguments, container);
         }
 
     }
 
     private Arguments determineArguments(String[] args) {
         Arguments arguments = new Arguments();
         final JCommander commander;
         try {
             commander = new JCommander(arguments, args);
         } catch (Exception err) {
             LOG.error(err.getMessage());
             return null;
         }
 
         if (arguments.isHelp()) {
             commander.usage();
             return null;
         }
         return arguments;
     }
 
     private Account createAccount(Arguments arguments) {
 
         LOG.info("Executing with "+
                 "tenant name "+arguments.getTenantName()+
                 ", tenant ID "+arguments.getTenantId()+
                 " and usr/pwd "+arguments.getUsername()+"/"+arguments.getPassword()+"@"+arguments.getUrl());
 
         return new AccountFactory()
                 .setUsername(arguments.getUsername())
                 .setPassword(arguments.getPassword())
                 .setAuthUrl(arguments.getUrl())
                 .setPublicHost(arguments.getHost())
                 .setTenantId(arguments.getTenantId())
                 .setTenantName(arguments.getTenantName())
                 .setHashPassword(arguments.getHashPassword())
                 .setTempUrlHashPrefixSource(TempUrlHashPrefixSource.INTERNAL_URL_PATH)
                 .createAccount();
     }
 
     private String decode(String encoded) {
         try {
             return URLDecoder.decode(encoded, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void startServer(final Arguments arguments, final Account account) {
         Spark.setPort(arguments.getPort());
 
         // Get a listing for all Containers
         Spark.get(new Route("/") {
             @Override
             public Object handle(Request request, Response response) {
                 response.type("text/html"); // @TODO - must be dependent on Accept header
                 Map<String, Object> values = new TreeMap<>();
                 values.put("containers", convertAccountToList(account, arguments));
                 return callTemplate(determineTemplate(request.raw().getHeader("Accept")), values);
             }
         });
 
         // Get a listing for a single Container
         Spark.get(new Route("/container/:container") {
             @Override
             public Object handle(Request request, Response response) {
                 response.type("text/html"); // @TODO - must be dependent on Accept header
                 Container container = getContainer(request, account);
                 if (!container.exists()) {
                     return notFound(response, "Container", container.getName());
                 }
 
                 String redirectUrl = arguments.getRedirectUrl() + "/container/" + container.getName();
                 long maxFileSize = 500000000;
                 long maxFileCount = 30;
                 FormPost formPost = container.getFormPost(redirectUrl, maxFileSize, maxFileCount, 86400);
                 Map<String, Object> values = new TreeMap<>();
                 values.put("upload_host", account.getOriginalHost());
                 values.put("containerName", container.getName());
                 values.put("objects", convertContainerToList(container, arguments));
                 values.put("redirect", redirectUrl);
                 values.put("max_file_size", String.valueOf(maxFileSize));
                 values.put("max_file_count", String.valueOf(maxFileCount));
                 values.put("expires", String.valueOf(formPost.expires));
                 values.put("signature", formPost.signature);
                 return callTemplate(determineTemplate(request.raw().getHeader("Accept")), values);
             }
         });
 
         // Delete a single Object
         Spark.delete(new Route("/object/:container/:object") {
             @Override
             public Object handle(Request request, Response response) {
                 StoredObject object = getStoredObject(request, account);
                 object.delete();
                 LOG.info(object.getName() + " deleted from Swift");
                 return "";
             }
         });
 
         // Fetch the temporary GET URL for an Object
         Spark.get(new Route("/object/:container/:object") {
             @Override
             public Object handle(Request request, Response response) {
                 StoredObject object = getStoredObject(request, account);
                 if (!object.exists()) {
                     return notFound(response, "Object", object.getName());
                 }
                 LOG.info("Drafting temp GET URL for " + object.getPath());
                 return object.getTempGetUrl(arguments.getSeconds());
             }
         });
 
         // Fetch the temporary PUT URL for an Object
         Spark.get(new Route("/upload/:container/:object") {
             @Override
             public Object handle(Request request, Response response) {
                 StoredObject object = getStoredObject(request, account);
                 LOG.info("Drafting temp PUT URL for " + object.getPath());
                 return object.getTempPutUrl(arguments.getSeconds());
             }
         });
 
     }
 
     private Container getContainer(Request request, Account account) {
         String containerName = decode(request.params(":container"));
         return getContainer(account, containerName);
     }
 
     private StoredObject getStoredObject(Request request, Account account) {
         String containerName = decode(request.params(":container"));
         String objectName = decode(request.params(":object"));
         return getObject(account, containerName, objectName);
     }
 
     private String determineTemplate(String accept) {
         return
                 accept != null && accept.equals("text/plain") ?
                         LISTING_PLAIN_FTL :
                         LISTING_HTML_FTL;
     }
 
     private StoredObject getObject(Account account, String containerName, String objectName) {
         Container container = getContainer(account, containerName);
         return container.getObject(objectName);
     }
 
     private Container getContainer(Account account, String containerName) {
         return account.getContainer(containerName);
     }
 
     private String notFound(Response response, String entityType, String entityName) {
         response.status(404);
         Map<String, Object> values = new TreeMap<>();
         values.put("entityType", entityType);
         values.put("entityName", entityName);
         return callTemplate("notfound.ftl", values);
     }
 
     private List<Map<String,Object>> convertAccountToList(Account account, Arguments arguments) {
         List<Map<String,Object>> containers = new ArrayList<>();
         for (Container container : account.list()) {
             Map<String,Object> containerMap = new TreeMap<>();
             containerMap.put("name", container.getName());
             containerMap.put("objects", convertContainerToList(container, arguments));
             containers.add(containerMap);
         }
         return containers;
     }
 
     private List<Map<String, Object>> convertContainerToList(Container container, Arguments arguments) {
         List<Map<String,Object>> objects = new ArrayList<>();
         for (StoredObject object : container.list()) {
             Map<String,Object> objectMap = new TreeMap<>();
             objectMap.put("name", object.getName());
             objectMap.put("size", longToBytes(object.getContentLength()));
            objectMap.put("lastModified", formatter.format(object.getLastModifiedAsDate()));
             objectMap.put("tempUrl", encodeUrl(object.getTempGetUrl(arguments.getSeconds())));
             objects.add(objectMap);
         }
         return objects;
     }
 
     private String encodeUrl(String url) {
         return url.replace("&", "&amp;");
     }
 
     private String callTemplate(String templateName, Map<String, Object> values) {
         Configuration configuration = new Configuration();
         configuration.setClassForTemplateLoading(Main.class, "/");
         StringWriter writer = new StringWriter();
         try {
             Template template = configuration.getTemplate(templateName);
             template.process(values, writer);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return writer.toString();
     }
 
     private void listFiles(Arguments arguments, Container container) {
         for (StoredObject object : container.list(arguments.getPrefix(), null, -1)) {
             System.out.println("* "+object.getName() + " ("+ longToBytes(object.getContentLength())+") -> "+
                     (arguments.isShowTempUrl() ? object.getTempGetUrl(arguments.getSeconds()) : object.getPublicURL()));
         }
     }
 
     private void deleteFile(Arguments arguments, Container container) {
         StoredObject object = container.getObject(arguments.getDeleteFile());
         object.delete();
         LOG.info(object.getName() +" deleted from Swift");
     }
 
     private void uploadFile(Arguments arguments, Container container) {
         File uploadFile = new File(arguments.getFile());
         StoredObject object = container.getObject(uploadFile.getName());
         if (object.exists() && !arguments.isAllowOverride()) {
             LOG.error("File already exists. Upload cancelled");
             return;
         }
         object.uploadObject(uploadFile);
         System.out.println(object.getPublicURL());
     }
 
     public static String longToBytes(long bytesUsed) {
         String suffix = "B";
         if (bytesUsed / 1024 > 0) {
             bytesUsed /= 1024;
             suffix = "KB";
         }
         if (bytesUsed / 1024 > 0) {
             bytesUsed /= 1024;
             suffix = "MB";
         }
         if (bytesUsed / 1024 > 0) {
             bytesUsed /= 1024;
             suffix = "GB";
         }
         if (bytesUsed / 1024 > 0) {
             bytesUsed /= 1024;
             suffix = "TB";
         }
         return bytesUsed + " " + suffix;
     }
 
 }
