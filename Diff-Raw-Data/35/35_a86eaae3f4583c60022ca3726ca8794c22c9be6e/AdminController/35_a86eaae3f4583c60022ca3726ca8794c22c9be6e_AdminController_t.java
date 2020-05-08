 package fi.essentia.somacms.controllers;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import fi.essentia.somacms.dao.DataDao;
import fi.essentia.somacms.dao.ReadOnlyDataDao;
 import fi.essentia.somacms.json.*;
 import fi.essentia.somacms.json.Error;
 import fi.essentia.somacms.tree.DocumentManager;
 import fi.essentia.somacms.tree.TreeDocument;
 import fi.essentia.somacms.tree.UnsupportedMimeTypeException;
 import fi.essentia.somacms.util.ArchiveHelper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.dao.DuplicateKeyException;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.context.request.RequestAttributes;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.multipart.MultipartFile;
 
 import java.io.IOException;
 import java.util.Collection;
 
 /**
  * Takes care of all the administration tasks
  */
 @Controller
 @RequestMapping(value="/admin/")
 @Secured(value = "ROLE_ADMIN")
 public class AdminController {
     private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
     public static final String KEY_NEXT_MESSAGE = "nextMessage";
 
     @Autowired private DocumentManager documentManager;
    @Autowired private ReadOnlyDataDao dataDao;
     @Autowired private ArchiveHelper archiveHelper;
     @Value("${somacms.version}") String version;
 
     @RequestMapping(method=RequestMethod.GET)
     public String admin() {
         return "redirect:document/0";
     }
 
     @RequestMapping(value="/document/{id}", method=RequestMethod.GET)
     public String showDocument(@PathVariable Long id, Model model, WebRequest webRequest) {
         TreeDocument document = documentManager.documentById(id);
         if (document == null) {
             throw new ResourceNotFoundException();
         }
         model.addAttribute("contextPath", webRequest.getContextPath());
         model.addAttribute("document", document);
         model.addAttribute("version", version);
 
         String nextMessage = (String)webRequest.getAttribute(KEY_NEXT_MESSAGE, RequestAttributes.SCOPE_SESSION);
         if (nextMessage != null) {
             model.addAttribute("message", nextMessage);
             webRequest.removeAttribute(KEY_NEXT_MESSAGE, RequestAttributes.SCOPE_SESSION);
         }
 
         if (document.isFolder()) {
             return "admin/folder";
         } else if (document.isImage()) {
             return "admin/image";
         } else if (document.isText()) {
             byte[] bytes = dataDao.loadData(document.getId());
             model.addAttribute("documentText", new String(bytes));
             return "admin/text";
         } else {
             throw new ResourceNotFoundException();
         }
     }
 
     @RequestMapping(value="/api/document/{parentId}/folders", method=RequestMethod.POST)
     public @ResponseBody Result createFolder(@PathVariable Long parentId, @RequestParam("name") String name, WebRequest request) {
         TreeDocument folder;
         try {
             folder = documentManager.createFolder(parentId, name);
         } catch (DuplicateKeyException e) {
             return new Error("There is already a document with the same name.");
         }
         storeNextMessage(request, "Folder <b>" + name + "</b> created");
         return new Created(folder.getId());
     }
 
     private void storeNextMessage(WebRequest request, String message) {
         request.setAttribute(KEY_NEXT_MESSAGE, message, RequestAttributes.SCOPE_SESSION);
     }
 
     @RequestMapping(value="/api/document/{parentId}/documents", method=RequestMethod.POST)
     public @ResponseBody Result createTextFile(@PathVariable Long parentId, @RequestParam("name") String name) {
         try {
             TreeDocument testFile = documentManager.createTextFile(parentId, name);
             return new Created(testFile.getId());
         } catch (UnsupportedMimeTypeException e) {
             return new Error("The file doesn't seem to be a text document.");
         } catch (DuplicateKeyException e) {
             return new Error("There is already a document with the same name.");
         }
     }
 
     @RequestMapping(value="/api/document/{parentId}/files", method=RequestMethod.POST)
     public @ResponseBody Result uploadFile(@PathVariable Long parentId, @RequestParam(value="qqfile", required=true) MultipartFile file, WebRequest request) throws IOException {
         try {
             String contentType = file.getContentType();
             if (contentType.equals("application/zip")) {
                 TreeDocument parent = documentManager.documentById(parentId);
                 byte[] bytes = file.getBytes();
                 archiveHelper.storeDocuments(parent, bytes);
                 storeNextMessage(request, "Archive " + file.getOriginalFilename() + " extracted");
                 return Result.success();
             } else {
                 TreeDocument treeDocument = documentManager.storeDocument(parentId, file.getOriginalFilename(), file.getBytes());
                 storeNextMessage(request, "File " + file.getOriginalFilename() + " uploaded");
                 return new Created(treeDocument.getId());
             }
         } catch (RuntimeException e) {
             logger.error("Upload of " + file.getOriginalFilename() + " failed", e);
             return new Error(e.getMessage());
         }
     }
 
     @RequestMapping(value="/api/document/{documentId}/replace", method=RequestMethod.POST)
     public @ResponseBody Result replace(@PathVariable Long documentId, @RequestParam(value="qqfile", required=true) MultipartFile file, WebRequest request) throws IOException {
         try {
             String contentType = file.getContentType();
             if (contentType.equals("application/zip")) {
                 return new Error("Updating archives is not supported");
             } else {
                 TreeDocument document = documentManager.documentById(documentId);
                 String fileName = file.getOriginalFilename();
                 if (!document.getName().equals(fileName)) {
                     throw new RuntimeException("Received upload of " + fileName + " that was trying to replace " + document.getName());
                 }
 
                 documentManager.storeDocument(document.getParentId(), fileName, file.getBytes());
                 storeNextMessage(request,"File " + fileName + " updated");
                 return Result.success();
             }
         } catch (RuntimeException e) {
             logger.error("Update of " + file.getOriginalFilename() + " failed", e);
             return new Error(e.getMessage());
         }
     }
 
     @RequestMapping(value="/api/document/{documentId}/save", method=RequestMethod.PUT)
     public @ResponseBody Result saveTextDocument(@PathVariable Long documentId, @RequestBody String contents) {
         TreeDocument document = documentManager.documentById(documentId);
         documentManager.storeDocument(document.getParentId(), document.getName(), contents.getBytes());
         return Result.success();
     }
 
     @RequestMapping(value="/api/document/{documentId}", method=RequestMethod.DELETE)
     public @ResponseBody Result delete(@PathVariable Long documentId, WebRequest request) {
         TreeDocument treeDocument = documentManager.deleteDocument(documentId);
         String message = (treeDocument.isFolder() ? "Folder " : "Document") + " <b>" + treeDocument.getName() + "</b> deleted.";
         storeNextMessage(request, message);
         return Result.success();
     }
 
     @RequestMapping(value= "/api/search/", method = RequestMethod.GET)
     public @ResponseBody Collection<SearchResult> listDocuments(@RequestParam(value = "query") String query) {
         Collection<TreeDocument> treeDocuments = documentManager.documentsByPath(query);
         return Collections2.transform(treeDocuments, new Function<TreeDocument, SearchResult>() {
             @Override
             public SearchResult apply(TreeDocument treeDocument) {
                 return new SearchResult(treeDocument.getId(), treeDocument.getPath());
             }
         });
     }
 }
