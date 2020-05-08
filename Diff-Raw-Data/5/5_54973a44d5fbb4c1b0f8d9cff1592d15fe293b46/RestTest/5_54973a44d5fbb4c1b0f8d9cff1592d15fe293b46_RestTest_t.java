 package rest;
 
 import com.sun.jersey.multipart.BodyPartEntity;
 import com.sun.jersey.multipart.FormDataMultiPart;
 import com.sun.jersey.spi.resource.Singleton;
 import fr.thumbnailsdb.*;
 import fr.thumbnailsdb.diskmonitor.DiskListener;
 import fr.thumbnailsdb.diskmonitor.DiskWatcher;
 import fr.thumbnailsdb.duplicate.DuplicateFolderGroup;
 import fr.thumbnailsdb.duplicate.DuplicateFolderList;
 import fr.thumbnailsdb.utils.Logger;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.FileUtils;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import javax.imageio.ImageIO;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.net.URLDecoder;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 @Path("/hello")
 @Singleton
 public class RestTest {
 
     private final String dbFileName = "db.txt";
 
     String bdName;
     protected ThumbStore tb;
     protected SimilarImageFinder si;
     protected DuplicateMediaFinder df;
     protected DiskWatcher dw;
 
     public RestTest() {
         // System.out.println("RestTest.RestTest()");
 
         File f = new File(dbFileName);
         if (f.exists()) {
             try {
                 //TODO : Loop over lines
                 BufferedReader fr = new BufferedReader(new FileReader(f));
                 while ((bdName = fr.readLine()) != null) {
                     if (tb == null) {
                         tb = new ThumbStore(bdName);
                     } else {
                         tb.addDB(bdName);
                     }
                 }
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         } else {
             tb = new ThumbStore();
         }
 
         si = new SimilarImageFinder(tb);
         df = new DuplicateMediaFinder(tb);
         try {
             dw = new DiskWatcher(tb.getIndexedPaths().toArray(new String[]{}));
             dw.addListener(new DBDiskUpdater());
             dw.processEvents();
         } catch (IOException e) {
             Logger.getLogger().err("Could not register path");
         }
 
 
     }
 
 
     @GET
     @Path("/db/{param}")
     public Response getDBInfo(@PathParam("param") String info) {
    //     System.out.println("RestTest.getDBInfo() " + info);
         if ("size".equals(info)) {
     //       System.out.println("RestTest.getDBInfo() " + tb.size());
             return Response.status(200).entity(tb.size() + "").build();
         }
         if ("path".equals(info)) {
             return Response.status(200).entity(tb.getPath() + "").build();
         }
         if ("status".equals(info)) {
             return Response.status(200).entity("idle").build();
         }
         return Response.status(404).build();
     }
 
 
     @GET
     @Path("/monitor")
     @Produces({MediaType.APPLICATION_JSON})
     public Response monitor() {
         long usedMemory = (Runtime.getRuntime().totalMemory() -Runtime.getRuntime().freeMemory());
         //System.out.println("RestTest.monitor() ");
         JSONObject result = new JSONObject();
         try {
 //            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
 //            String date = DATE_FORMAT.format(new Date());
 //            result.put("time", date);
             result.put("time", System.currentTimeMillis());
             result.put("usedMemory",usedMemory);
             result.put("totalMemory",Runtime.getRuntime().totalMemory());
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 //        if ("size".equals(info)) {
 //            System.out.println("RestTest.getDBInfo() " + tb.size());
 //            return Response.status(200).entity(tb.size() + "").build();
 //        }
 //        if ("path".equals(info)) {
 //            return Response.status(200).entity(tb.getPath() + "").build();
 //        }
 //        if ("status".equals(info)) {
 //            return Response.status(200).entity("idle").build();
 //        }
 
         return Response.status(200).entity(result).build();
     }
 
 
     @GET
     @Path("/status")
     @Produces({MediaType.APPLICATION_JSON})
     public Response getStatus() {
 
         return Response.status(200).entity(Status.getStatus()).build();
 
     }
 
     @GET
     @Path("/paths")
     @Produces({MediaType.APPLICATION_JSON})
     public Response getPaths() {
         return Response.status(200).entity(tb.getIndexedPaths()).build();
     }
 
 
     protected String[] parseFolders(String json) {
         String[] folders = null;
         try {
 //            System.out.println("RestTest.getDuplicate  folder string " + json);
             JSONObject paramString = new JSONObject(json);
             System.out.println("RestTest.getDuplicate " + paramString);
             JSONArray jArray = paramString.getJSONArray("folders");
             folders = new String[jArray.length()];
             for (int i = 0; i < jArray.length(); i++) {
                 folders[i] = jArray.getString(i);
             }
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return folders;
     }
 
 
     @GET
     @Path("/identical")
     @Produces({MediaType.APPLICATION_JSON})
     public Response getDuplicate(@QueryParam("max") String max, @QueryParam("folder") final String obj) {
         String[] folders = this.parseFolders(obj);
 
 
         System.out.println("RestTest.getDuplicate " + obj);
         Status.getStatus().setStringStatus("Requesting duplicate media");
         Collection dc = (Collection) df.computeDuplicateSets(df.findDuplicateMedia()).toCollection(Integer.parseInt(max), folders);
         Status.getStatus().setStringStatus(Status.IDLE);
         return Response.status(200).entity(dc).build();
     }
 
 
     @GET
     @Path("/duplicateFolder")
     @Produces({MediaType.APPLICATION_JSON})
     public Response getDuplicateFolder(@QueryParam("folder") final String obj) {
         String[] folders = this.parseFolders(obj);
         Logger.getLogger().log("RestTest.getDuplicateFolder " + obj);
         Status.getStatus().setStringStatus("Requesting duplicate folder list");
 
         Collection<DuplicateFolderGroup> col = getDuplicateFolderGroup().asSortedCollection(folders, 300);
         Logger.getLogger().log("RestTest.getDuplicateFolder sending results of size " + col.size());
         Status.getStatus().setStringStatus(Status.IDLE);
 
         return Response.status(200).entity(col).build();
     }
 
     private synchronized DuplicateFolderList getDuplicateFolderGroup() {
         PreloadedDescriptors mfdList = df.findDuplicateMedia();
         Status.getStatus().setStringStatus("Computing duplicate folders on preloaded list of size " + mfdList.size());
         DuplicateFolderList dc = df.computeDuplicateFolderSets(mfdList);
         Status.getStatus().setStringStatus(Status.IDLE);
         return dc;
     }
 
     @GET
     @Path("/duplicateFolderDetails")
     @Produces({MediaType.APPLICATION_JSON})
     public Response getDuplicateFolderDetails(@QueryParam("folder1") String f1, @QueryParam("folder2") String f2) {
         DuplicateFolderGroup group = getDuplicateFolderGroup().getDetails(f1, f2);
         JSONObject json = new JSONObject();
 
         try {
             json.put("file1", group.getFile1());
             json.put("file2", group.getFile2());
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return Response.status(200).entity(json).build();
     }
 
     @GET
     @Path("getThumbnail/")
     @Produces("image/jpg")
     public Response getThumbnail(@QueryParam("path") String imageId) {
 //        System.out.println("Thubnail : imageID " + imageId);
         BufferedInputStream source = null;
         try {
 
             BufferedImage bf = ImageIO.read(new FileInputStream(new File(imageId)));
             // scale it to the new size on-the-fly
             BufferedImage thumbImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
             Graphics2D graphics2D = thumbImage.createGraphics();
             graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
             graphics2D.drawImage(bf, 0, 0, 100, 100, null);
             // save thumbnail image to outFilename
             ByteArrayOutputStream bout = new ByteArrayOutputStream();
             BufferedOutputStream out = new BufferedOutputStream(bout);
             ImageIO.write(thumbImage, "jpg", out);
             final byte[] imgData = bout.toByteArray();
             final InputStream bigInputStream =
                     new ByteArrayInputStream(imgData);
             return Response.status(200).entity(bigInputStream).build();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return Response.status(404).build();
     }
 
     @GET
     @Path("getSignature/")
     @Produces("image/jpg")
     public Response getSignature(@QueryParam("path") String imageId) {
         System.out.println("Signature : imageID " + imageId);
         BufferedInputStream source = null;
 
         MediaFileDescriptor mdf = tb.getMediaFileDescriptor(imageId);
 
         final InputStream bigInputStream =
                 new ByteArrayInputStream(mdf.getSignatureAsByte());
         return Response.status(200).entity(bigInputStream).build();
 
         //return Response.status(404).build();
     }
 
 
     @GET
     @Path("open/")
     public Response openPath(@QueryParam("path") final String obj) {
         String[] folders = parseFolders(obj);
 
 
         for (String path : folders) {
             System.out.println("RestTest.openPath2 " + path);
             File file = new File(path);
             Desktop desktop = Desktop.getDesktop();
             try {
                 desktop.open(file);
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
         return Response.status(200).build();
     }
 
 
     protected String getImageAsHTMLImg(String imageId) {
         String img = "";
         try {
             File f = new File(imageId);
             img = "{\"data\" : \"" + Base64.encodeBase64String(FileUtils.readFileToByteArray(f)) + "\", \"title\" : \"" + f.getParent() + "\" }";
 
         } catch (IOException e) {
             e.printStackTrace();
         }
         return img;
     }
 
 
     @GET
     @Path("shrink/")
     public Response shrink(@QueryParam("folder") final java.util.List<String> obj) {
         tb.flushPreloadedDescriptors();
         tb.shrink(obj);
         return Response.status(200).entity("Shrink done").build();
     }
 
 
     @GET
     @Path("update/")
     public Response update(@QueryParam("folder") String obj) {
         String[] folders = this.parseFolders(obj);
         tb.flushPreloadedDescriptors();
         new MediaIndexer(tb).updateDB(folders);
         return Response.status(200).entity("Update done").build();
     }
 
     @GET
     @Path("shrinkUpdate/")
     public Response shrinkUpdate(@QueryParam("folder") final String obj) {
 
         String[] folders = this.parseFolders(obj);
 
         tb.flushPreloadedDescriptors();
        tb.shrink();
         MediaIndexer mdi = new MediaIndexer(tb);
         mdi.updateDB(folders);
         return Response.status(200).entity("Update done").build();
     }
 
     @GET
     @Path("folder/")
     public Response getFolder(@QueryParam("path") String path) {
         System.out.println("RestTest.getFolder() input_path " + path);
         return Response.status(200).entity(path).type("application/folder").build();
     }
 
     @GET
     @Path("index/")
     public Response index(@QueryParam("path") String path) {
         System.out.println("RestTest.index() input_path " + path);
         return Response.status(200).entity("Indexing in progress").build();
     }
 
 
     @GET
     @Path("trash/")
     public Response moveToTrash(@QueryParam("path") String path) {
         System.out.println("RestTest.moveToTrash() input_path " + path);
         java.nio.file.Path currentRelativePath = Paths.get("");
         String s = currentRelativePath.toAbsolutePath().toString();
         System.out.println("Current relative path is: " + s);
         try {
             this.moveToTrash(path, s);
         } catch (IOException e) {
             e.printStackTrace();
             return Response.status(500).build();
         }
         return Response.status(200).build(); //.entity(path).type("application/folder").build();
     }
 
     protected void moveToTrash(String path, String trashRoot) throws IOException {
         //create a Trash folder if it does not exist
 
         java.nio.file.Path trash = FileSystems.getDefault().getPath(trashRoot + "/trash");
 
         if (!Files.exists(trash)) {
             trash = Files.createDirectory(trash);
         }
         if (trash == null) {
             Logger.getLogger().err("Error creating trash directory at " + trashRoot);
             return;
         }
 //        File f = new File(trashRoot+"/trash");
 //        if (!f.exists()) {
 //            if (!f.mkdir()) {
 //                Logger.getLogger().err("Error creating trash directory at " + trashRoot);
 //                return;
 //            }
 //        }
 
         java.nio.file.Path source = FileSystems.getDefault().getPath(path);
         String sourceFilePath = source.getFileName().toString();
         java.nio.file.Path trashedFileDest = FileSystems.getDefault().getPath(trashRoot + "/trash/" + sourceFilePath);
         System.out.println("RestTest.moveToTrash " + trashedFileDest);
         java.nio.file.Files.move(source, trashedFileDest);
 
 
     }
 
 
     @POST
     @Path("findSimilar/")
     @Consumes(MediaType.MULTIPART_FORM_DATA)
     @Produces({MediaType.APPLICATION_JSON})
     public Response findSimilar(FormDataMultiPart multipart) {
         ThumbnailGenerator tg = new ThumbnailGenerator(null);
         BodyPartEntity bpe = (BodyPartEntity) multipart.getBodyParts().get(0).getEntity();
         Collection<MediaFileDescriptor> c = null;
         ArrayList<SimilarImage> al = null;
         File temp = null;
         MediaFileDescriptor initialImage = null;
 
         try {
             InputStream source = bpe.getInputStream();
             Logger.getLogger().log("RestTest.findSimilar() received " + source);
             temp = File.createTempFile("tempImage", ".jpg");
             FileOutputStream fo = new FileOutputStream(temp);
 
             byte[] buffer = new byte[8 * 1024];
 
             int total = 0;
             try {
                 int bytesRead;
                 while ((bytesRead = source.read(buffer)) != -1) {
                     fo.write(buffer, 0, bytesRead);
                     total += bytesRead;
                 }
             } finally {
                 fo.close();
             }
 
             initialImage = tg.buildMediaDescriptor(temp);
             Logger.getLogger().log("RestTest.findSimilar()  written to " + temp + " with size " + total);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         long t1 = System.currentTimeMillis();
         c = si.findSimilarMedia(temp.getAbsolutePath(), 20);
         long t2 = System.currentTimeMillis();
         System.out.println("Found similar files " + c.size() + " took " + (t2 - t1) + "ms");
 
         al = new ArrayList<SimilarImage>(c.size());
         for (MediaFileDescriptor mdf : c) {
 
             String path = mdf.getPath();
 
             String imgData = null;
             String sigData = null;
             try {
                 FileInputStream f = new FileInputStream(new File(path));
                 try {
                     //encode thumbnail
                     BufferedImage bf = tg.downScaleImage(ImageIO.read(f), 200, 200);
                     ByteArrayOutputStream out = new ByteArrayOutputStream();
                     ImageIO.write(bf, "JPEG", out);
                     imgData = Base64.encodeBase64String(out.toByteArray());
                     //encode image signature
                     out = new ByteArrayOutputStream();
                     ImageIO.write(mdf.getSignatureAsImage(), "JPEG", out);
 
                     sigData = Base64.encodeBase64String(out.toByteArray());
                 } finally {
                     f.close();
                 }
             } catch (IOException e) {
                 System.err.println("Err: File " + path + " not found");
             }
 
             String folder = Utils.fileToDirectory(path);
             SimilarImage si = new SimilarImage(path, Utils.folderSize(folder), imgData, mdf.getDistance(), sigData);
             al.add(si);
           //  System.out.println(si);
 
         }
         System.out.println("RestTest.findSimilar sending " + al.size() + " elements");
 
         JSONArray mJSONArray = new JSONArray();
         for (int i = 0; i < al.size(); i++) {
             JSONObject json = new JSONObject();
             try {
                 json.put("foldersize", al.get(i).folderSize);
                 json.put("path", al.get(i).path);
                 json.put("base64Data", al.get(i).base64Data);
                 json.put("base64Sig", al.get(i).base64Sig);
                 json.put("distance", al.get(i).rmse);
                 mJSONArray.put(json);
             } catch (JSONException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
 
         //prepare signature of original image
         //TODO : create utility function
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         try {
 
             ImageIO.write(initialImage.getSignatureAsImage(), "JPEG", out);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
 
         JSONObject responseDetailsJson = new JSONObject();
         try {
             responseDetailsJson.put("success", true);
             responseDetailsJson.put("sourceSig", Base64.encodeBase64String(out.toByteArray()));
             responseDetailsJson.put("images", mJSONArray);
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return Response.status(200).entity(responseDetailsJson).type(MediaType.APPLICATION_JSON).build();
     }
 
 
     @POST
     @Path("findGPS/")
     @Consumes(MediaType.MULTIPART_FORM_DATA)
     @Produces({MediaType.APPLICATION_JSON})
     public Response findGPS(FormDataMultiPart multipart) {
         BodyPartEntity bpe = (BodyPartEntity) multipart.getBodyParts().get(0).getEntity();
         Collection<MediaFileDescriptor> c = null;
         ArrayList<SimilarImage> al = null;
         File temp = null;
         try {
             InputStream source = bpe.getInputStream();
             System.out.println("RestTest.findGPS() received " + source);
 
             temp = File.createTempFile("tempImage", ".jpg");
             FileOutputStream fo = new FileOutputStream(temp);
 
             byte[] buffer = new byte[8 * 1024];
 
             int total = 0;
             try {
                 int bytesRead;
                 while ((bytesRead = source.read(buffer)) != -1) {
                     fo.write(buffer, 0, bytesRead);
                     total += bytesRead;
                 }
             } finally {
                 fo.close();
             }
             System.out.println("RestTest.findGPS()  written to " + temp + " with size " + total);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         MetaDataFinder mdf = new MetaDataFinder(temp);
         double[] coo = mdf.getLatLong();
 
         JSONObject responseDetailsJson = new JSONObject();
         try {
             responseDetailsJson.put("lat", coo == null ? 0 : coo[0]);
 
             responseDetailsJson.put("lon", coo == null ? 0 : coo[1]);
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
 
         System.out.println("RestTest.findGPS sending json " + responseDetailsJson);
         return Response.status(200).entity(responseDetailsJson).type(MediaType.APPLICATION_JSON).build();
     }
 
 
     @GET
     @Path("findGPSFromPath/")
     @Produces({MediaType.APPLICATION_JSON})
     public Response findGPSFromPath(@QueryParam("path") String path) {
         System.out.println("RestTest.findGPSFromPath " + path);
         String rPath = null;
         try {
             rPath = URLDecoder.decode(path, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         System.out.println("RestTest.findGPSFromPath real path " + rPath);
         File temp = new File(rPath);
         MetaDataFinder mdf = new MetaDataFinder(temp);
         double[] coo = mdf.getLatLong();
         JSONObject responseDetailsJson = new JSONObject();
         try {
             responseDetailsJson.put("lat", coo[0]);
 
             responseDetailsJson.put("lon", coo[1]);
             responseDetailsJson.put("date", mdf.getDate());
             responseDetailsJson.put("gps", mdf.getGPS());
         } catch (JSONException e) {
             e.printStackTrace();
         }
         // System.out.println("RestTest.findGPSFromPath sending json " + responseDetailsJson);
         return Response.status(200).entity(responseDetailsJson).type(MediaType.APPLICATION_JSON).build();
     }
 
 
     @GET
     @Path("getAllGPS/")
     @Produces({MediaType.APPLICATION_JSON})
     public Response getAllGPS() {
         ArrayList<String> al = tb.getAllWithGPS();
         return Response.status(200).entity(al).type(MediaType.APPLICATION_JSON).build();
     }
 
 
     @GET
     @Path("getAll/")
     @Produces({MediaType.APPLICATION_JSON})
     public Response getAll(@QueryParam("filter") String filter) {
         Status.getStatus().setStringStatus("Requesting all media with filter : " + filter);
         long t0 = System.currentTimeMillis();
         ArrayList<MediaFileDescriptor> pd = tb.getFromDB(filter);
         long t1 = System.currentTimeMillis();
         System.out.println("RestTest.getAll with filter " + filter + "  took " + (t1-t0) + " ms" );
         Iterator<MediaFileDescriptor> it = pd.iterator();
         JSONArray mJSONArray = new JSONArray(pd.size());
         int i = 0;
         while (it.hasNext() && i < 10000) {
             i++;
             JSONObject json = new JSONObject();
             MediaFileDescriptor mfd = it.next();
          //   if (mfd.getPath().contains(filter)) {
                 try {
                     json.put("path", mfd.getPath());
                     json.put("size", mfd.getSize());
                     mJSONArray.put(json);
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
        // }
         Status.getStatus().setStringStatus(Status.IDLE);
         return Response.status(200).entity(mJSONArray).type(MediaType.APPLICATION_JSON).build();
     }
 //    public Response getAll(@QueryParam("filter") String filter) {
 //        PreloadedDescriptors pd = tb.getPreloadedDescriptors();
 //        Iterator<MediaFileDescriptor> it = pd.iterator();
 //        JSONArray mJSONArray = new JSONArray(pd.size());
 //        int i = 0;
 //        while (it.hasNext() && i < 10000) {
 //            i++;
 //            JSONObject json = new JSONObject();
 //            MediaFileDescriptor mfd = it.next();
 //            if (mfd.getPath().contains(filter)) {
 //                try {
 //                    json.put("path", mfd.getPath());
 //                    json.put("size", mfd.getSize());
 //                    mJSONArray.put(json);
 //                } catch (JSONException e) {
 //                    e.printStackTrace();
 //                }
 //            }
 //        }
 //        return Response.status(200).entity(mJSONArray).type(MediaType.APPLICATION_JSON).build();
 //    }
 
 
     @XmlRootElement
     public class SimilarImage {
         @XmlElement
         public String path;
         @XmlElement
         public String base64Data;
         @XmlElement
         public double rmse;
         @XmlElement
         public String base64Sig;
         @XmlElement
         public int folderSize;
 
         public SimilarImage(String path, int folderSize, String base64Data, double rmse, String base64Sig) {
             this.rmse = rmse;
             this.path = path;
             this.folderSize = folderSize;
             this.base64Data = base64Data;
             this.base64Sig = base64Sig;
         }
     }
 
     public class DBDiskUpdater implements DiskListener {
 
         public void fileCreated(java.nio.file.Path p) {
             Logger.getLogger().log("RestTest$DBDiskUpdater.fileCreated " + p);
             try {
                 new MediaIndexer(tb).processMT(new File(p.toString()));
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         public void fileModified(java.nio.file.Path p) {
             Logger.getLogger().log("RestTest$DBDiskUpdater.fileModified " + p);
             try {
                 new MediaIndexer(tb).processMT(new File(p.toString()));
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         public void fileDeleted(java.nio.file.Path p) {
             Logger.getLogger().log("RestTest$DBDiskUpdater.fileDeleted " + p);
 
             // MediaFileDescriptor mf = new MediaIndexer(tb).buildMediaDescriptor(new File(p.toString()));
             tb.deleteFromDatabase(p.toString());
         }
 
         public void folderCreated(java.nio.file.Path p) {
             Logger.getLogger().log("RestTest$DBDiskUpdater.folderCreated " + p);
             try {
                 new MediaIndexer(tb).processMT(new File(p.toString()));
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         public void folderModified(java.nio.file.Path p) {
             Logger.getLogger().log("RestTest$DBDiskUpdater.fileModified " + p);
             try {
                 new MediaIndexer(tb).processMT(new File(p.toString()));
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         public void folderDeleted(java.nio.file.Path p) {
 
 
         }
     }
 
 
 }
