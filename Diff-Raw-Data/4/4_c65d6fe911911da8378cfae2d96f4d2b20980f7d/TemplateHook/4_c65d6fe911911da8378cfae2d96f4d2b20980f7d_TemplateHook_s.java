 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.web;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import net.wgr.server.web.handling.WebHook;
 import net.wgr.settings.Settings;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Oct 5, 2011
  * @author double-u
  */
 public class TemplateHook extends WebHook {
     
     public TemplateHook() {
         super("view");
     }
     
     @Override
     public void handle(RequestBundle rb) throws IOException {
         if (rb.getPathParts().length < 1) {
             return;
         }
         String path = "";
         try {
             URI uri = new URI(StringUtils.join(rb.getPathParts()));
             uri = uri.normalize();
             path = uri.getPath();
         } catch (URISyntaxException ex) {
             Logger.getLogger(getClass()).error(ex);
         }
         if (path.isEmpty()) {
             return;
         }
         
         path = Settings.getInstance().getString("WebContentPath") + "/" + this.getSelector() + "/" + path;
         File f = new File(path);
         if (f.exists() && f.isDirectory()) {
             Path p = f.toPath();
             JsonObject contentTree = new JsonObject();
             try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
                 for (Path file : stream) {
                     if (file.toFile().isFile() && !file.startsWith(".")) {
                        contentTree.addProperty(file.toFile().getName(), IOUtils.toString(new FileInputStream(file.toFile())));
                     }
                 }
             }
             Gson gson = new Gson();
             rb.replyWithString(gson.toJson(contentTree));
         }
     }
 }
