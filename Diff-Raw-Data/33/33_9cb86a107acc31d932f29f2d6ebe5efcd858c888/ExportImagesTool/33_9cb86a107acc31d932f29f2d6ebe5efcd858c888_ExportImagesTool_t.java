 package com.polopoly.ps.pcmd.tools;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.polopoly.cm.ContentHistory;
 import com.polopoly.cm.ContentId;
 import com.polopoly.cm.VersionInfo;
 import com.polopoly.cm.VersionedContentId;
 import com.polopoly.cm.app.imagemanager.ImageFormatException;
 import com.polopoly.cm.app.imagemanager.impl.HttpImageManagerPolicy;
 import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
 import com.polopoly.cm.client.CMException;
 import com.polopoly.cm.client.CMServer;
 import com.polopoly.cm.client.Content;
 import com.polopoly.cm.policy.Policy;
 import com.polopoly.pcmd.tool.Tool;
 import com.polopoly.ps.pcmd.FatalToolException;
 import com.polopoly.util.client.PolopolyContext;
 import com.polopoly.util.exception.PolicyGetException;
 import com.polopoly.util.policy.PolicyModification;
 
 public class ExportImagesTool implements Tool<ExportImagesParameters> {
 
     @Override
     public ExportImagesParameters createParameters()
     {
         return new ExportImagesParameters();
     }
 
     @Override
     public void execute(PolopolyContext context, ExportImagesParameters params) throws FatalToolException
     {
        URI uri = params.getFileServerUrl();
         for (ContentId id : params) {
             try {
                 handle(context, uri, id);
             } catch (PolicyGetException e) {
                 e.printStackTrace(System.err);
                 System.err.println("Failed to get " + id.getContentIdString());
             } catch (CMException e) {
                 e.printStackTrace(System.err);
                 System.err.println("CM Operaiton failed");
                 return;
             }
         }
     }
 
     /**
      * Export the image file from the image, upload it to a file server then change the image to be
      * a HTTP image pointing towards the exported image.
      */
     private void handle(PolopolyContext context, URI uri, ContentId id) throws PolicyGetException, CMException
     {
         Policy policy = context.getPolicy(id);
         Content content = policy.getContent();
         String image = getImageFile(content);
         if (image == null) {
             return;
         }
         URL location = uploadImageFile(uri, content, image);
         if (location == null) {
             return;
         }
         Policy latest = context.getPolicyUtil(content.getContentId()).modify(updateImage(image, location), Policy.class);
         removeOldVersions(context, latest.getContentId());
     }
 
     /**
      * Drop all versions except the latest one to allow the file content to be garbage collected.
      */
     private void removeOldVersions(PolopolyContext context, VersionedContentId contentId) throws CMException
     {
         System.out.println("Removing old versions of " + contentId.getContentIdString());
         CMServer server = context.getCMServer();
         ContentHistory history = server.getContentHistory(contentId);
         List<VersionInfo> infos = new ArrayList<VersionInfo>(Arrays.asList(history.getVersionInfos()));
         VersionInfo latest = infos.remove(infos.size() - 1);
         if (latest.getVersion() != contentId.getVersion()) {
             System.err.println("Sanity check before removal failed, wrong latest version expected " +
                     contentId.getVersion() + " but got " + latest.getVersion());
             return;
         }
         for (VersionInfo vi : infos) {
             VersionedContentId vid = new VersionedContentId(contentId, vi.getVersion());
             server.removeContentVersion(vid);
         }
     }
 
     /**
      * Change the image to be an http image pointing towards location, also removes the file from the content.
      */
     private PolicyModification<Policy> updateImage(final String file, final URL location) {
         return new PolicyModification<Policy>() {
             public void modify(Policy newVersion) throws CMException {
                 System.out.println("Modifying " + newVersion.getContentId().getContentIdString() + " to point towards " + location);
                 SelectableSubFieldPolicy sub = (SelectableSubFieldPolicy) newVersion.getChildPolicy("imageType");
                 sub.setSelectedSubFieldName("httpImage");
                 HttpImageManagerPolicy http = (HttpImageManagerPolicy) sub.getChildPolicy("httpImage");
                 try {
                     http.updateImage(location);
                 } catch (IOException e) {
                     throw new CMException("Could not fetch image", e);
                 } catch (ImageFormatException e) {
                     throw new CMException("Invalid image", e);
                 }
                 try {
                     newVersion.getContent().deleteFile(file);
                 } catch (IOException e) {
                     throw new CMException("Could not remove old image", e);
                 }
             }
         };
     }
 
     /**
      * Find the file name of the original file of an image, assumes the Greenfield Times
      * example.Image with a subfield selector pointing towards and image field.
      */
     private String getImageFile(Content content) throws CMException
     {
         String type = content.getComponent("imageType", "subField");
         if (type == null) {
             type = "image";
         }
         if (!"image".equals(type)) {
             System.err.println("Type of " + content.getContentId().getContentIdString() + " is " + type + " skipping");
             return null;
         }
         String image = content.getComponent("image", "selected");
         if (image == null) {
             System.err.println("No selected image of " + content.getContentId().getContentIdString());
             return null;
         }
         return image;
     }
 
     /**
      * Save the original image outside of the database and provide an URL that can be used
      * to access it.
      */
     private URL uploadImageFile(URI uri, Content content, String image) throws CMException
     {
         URI imageUrl = uri.resolve(image.substring(image.lastIndexOf('/')+1));
         System.out.println("Uploading " + content.getContentId().getContentIdString() + "!" + image + " to " + imageUrl);
         try {
             HttpURLConnection c = (HttpURLConnection) imageUrl.toURL().openConnection();
             c.setRequestMethod("POST");
             c.setDoOutput(true);
             c.connect();
             OutputStream out = c.getOutputStream();
             content.exportFile(image, out);
             out.close();
             c.disconnect();
             if (c.getResponseCode() != 201) {
                 System.err.println("File server responded with " + c.getResponseCode() + " for url " + imageUrl);
                 return null;
             }
             String location = c.getHeaderField("Location");
             if (location == null) {
                 System.err.println("File server responded without a Location header");
                 return null;
             }
             return new URL(location);
         } catch (MalformedURLException e) {
             throw new FatalToolException("Invalid file server url", e);
         } catch (IOException e) {
             throw new FatalToolException("Could not upload file", e);
         }
     }
 
     @Override
     public String getHelp() {
         return "Exports the image file from images and uploads it to a file server, converts the image to an http image pointing to the exported image";
     }
 }
