 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package qck.quickjs.controllers;
 
 import com.amazonaws.util.json.JSONArray;
 import com.amazonaws.util.json.JSONException;
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.UUID;
 import java.util.zip.GZIPInputStream;
 import javax.annotation.PostConstruct;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import qck.quickjs.domain.DataTables;
 import qck.quickjs.domain.JsFile;
 import qck.quickjs.services.DataStorageService;
 
 /**
  *
  * @author Anis
  */
 @Controller("jsController")
 public class JsController {
 
     @Autowired
     private DataStorageService dss;
     @Value("${deployment.location}")
     private String location;
     private ObjectMapper mapper = new ObjectMapper();
 
     @PostConstruct
     public void init() {
         if (location.equals("DEVELOPMENT")) {
             JsFile jf = new JsFile();
             jf.setDescription("haha");
             jf.setFilename("test");
             jf.setMimetype("mimetype");
             jf.setSssurl("test");
             jf.setName("momma");
             dss.create(jf);
            JsFile jf1 = new JsFile();
            jf1.setDescription("haha1");
            jf1.setFilename("test1");
            jf1.setMimetype("mimetype1");
            jf1.setSssurl("test1");
            jf1.setName("momma1");
            dss.create(jf1);
         }
     }
 
     //After S3 upload come here.
     @RequestMapping(value = "api/storeFile", method = RequestMethod.POST, produces = "application/json")
     @ResponseBody
     public JsFile store(@RequestParam("name") String name, @RequestParam("sssurl") String sssurl,
             @RequestParam("mimetype") String mimetype, @RequestParam("realName") String realName,
             @RequestParam("description") String description) throws IOException {
         JsFile f = dss.findByName(name);
         if (f == null) {
             f = new JsFile();
         }
         f.setName(name);
         f.setDescription(description);
         f.setFilename(realName);
         f.setSssurl(sssurl);
         f.setMimetype(mimetype);
         dss.update(f);
         return f;
     }
 
     @RequestMapping(value = "api/files/{id}", method = RequestMethod.GET, produces = "application/json")
     @ResponseBody
     public JsFile getFile(@PathVariable Long id) {
         return dss.read(id);
     }
 
     @RequestMapping(value = "api/bundle/{files}/", method = RequestMethod.GET, produces = "application/json")
     @ResponseBody
     public ResponseEntity<byte[]> bundle(@PathVariable(value="files") String files) throws IOException, JSONException {
         JSONArray arr = new JSONArray(files);
         if (arr.length() <= 0) {
             return null;
         }
         URL get;
         ByteArrayOutputStream out = new ByteArrayOutputStream();;
         byte[] content;
         for (int i = 0; i < arr.length(); i++) {
             if (extension(arr.getString(i)).equals("js")) {
                 get = new URL("http://dzupduoksghyh.cloudfront.net/ajax/lib/" + arr.getString(i));
                 GZIPInputStream gzis = new GZIPInputStream(get.openStream());
                 byte[] buffer = new byte[1024];
                 int len;
                 while ((len = gzis.read(buffer)) > 0) {
                     out.write(buffer, 0, len);
                 }
             }
         }
         content = out.toByteArray();
         out.close();
         
         String fileName = UUID.randomUUID().toString().split("-")[0];
         
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
         headers.setContentLength(content.length);
         headers.set("Content-Disposition", "attachment; filename=\""+fileName+".js\"");
 
         return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
     }
 
     @RequestMapping(value = "api/files", method = RequestMethod.GET, produces = "application/json")
     @ResponseBody
     public DataTables getFiles() {
         DataTables dt = new DataTables();
         dt.setAaData(dss.list());
         return dt;
     }
 
     private String extension(String filename) {
         String ext = "";
         int i = filename.lastIndexOf(".");
         ext = filename.substring(i + 1);
         return ext;
     }
 }
