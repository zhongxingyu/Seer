 package org.makumba.parade.view.managers;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.makumba.parade.init.ParadeProperties;
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.FileCVS;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.RowCVS;
 import org.makumba.parade.view.interfaces.ParadeView;
 
 import freemarker.template.SimpleHash;
 
 public class CVSViewManager implements ParadeView {
     
     static Logger logger = Logger.getLogger(CVSViewManager.class.getName());
 
     public void setParadeViewHeader(List headers) {
         headers.add("CVS user");
         headers.add("module");
         headers.add("branch");
     }
 
     public void setParadeView(SimpleHash rowInformation, Row r) {
         SimpleHash cvsModel = new SimpleHash();
         RowCVS cvsdata = (RowCVS) r.getRowdata().get("cvs");
         
        cvsModel.put("user", cvsdata.getUser());
        cvsModel.put("module", cvsdata.getModule());
        cvsModel.put("branch", cvsdata.getBranch());
         
         rowInformation.put("cvs", cvsModel);
 
     }
 
     public void setFileView(SimpleHash fileView, Row r, String path, File f) {
         
         FileCVS cvsdata = (FileCVS) f.getFiledata().get("cvs");
         RowCVS rowcvsdata = (RowCVS) r.getRowdata().get("cvs");        
 
         String cvsweb = ParadeProperties.getProperty("cvs.site");
         String webPath = f.getPath().substring(r.getRowpath().length() + 1).replace(java.io.File.separatorChar,'/');
         String cvswebLink = cvsweb + rowcvsdata.getModule() + "/" + webPath;
         
         // populating model
         fileView.put("cvsWebLink", cvswebLink);
         fileView.put("cvsIsNull", cvsdata == null);
         fileView.put("isConflictBackup", f.getName().startsWith(".#"));
         if(cvsdata != null) {
             fileView.put("cvsRevision", cvsdata.getRevision());
             fileView.put("cvsStatus", cvsdata.getStatus());
         } else {
             fileView.put("cvsRevision", "");
             fileView.put("cvsStatus", "");
         }
         
     }
         
 
 }
