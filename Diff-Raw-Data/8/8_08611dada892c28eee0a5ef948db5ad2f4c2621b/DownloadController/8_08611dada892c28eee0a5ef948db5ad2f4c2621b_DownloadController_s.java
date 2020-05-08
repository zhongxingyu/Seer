 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fr.gphy.piotrgui.j2eged.controllers;
 
 import fr.gphy.piotrgui.j2eged.helpers.DownloadHelper;
 import fr.gphy.piotrgui.j2eged.model.Metadata;
 import fr.gphy.piotrgui.j2eged.model.PhysicalDocument;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpServletRequest;
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.StreamedContent;
 
 /**
  *
  * @author Piotr
  */
 @ManagedBean(name = "DownloadController")
 @SessionScoped
 public class DownloadController implements Serializable {
 
     private BrowserController.DisplayDoc selectedDoc;
     private StreamedContent file;
     private DownloadHelper helper;
 
     public DownloadController() {
         this.helper = new DownloadHelper();
     }
 
     public void save(ActionEvent event) throws IOException {
         String paramId = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter("DocID");
         
         if (paramId.equals("null")) {
             System.err.println("Save : idNUll");
             return;
         }
         Metadata meta = this.helper.getMetaFromID(paramId);
 
         PhysicalDocument phyDoc = this.helper.getByteFromID(paramId);
       
         ByteArrayInputStream stream = new ByteArrayInputStream(phyDoc.getBinaryBlob());
 
         file = new DefaultStreamedContent(stream, meta.getType().getMimeType(), meta.getName());


     }
 
     public BrowserController.DisplayDoc getSelectedDoc() {
         return selectedDoc;
     }
 
     public void setSelectedDoc(BrowserController.DisplayDoc selectedDoc) {
         System.err.println("plop");
         this.selectedDoc = selectedDoc;
     }
 
     public StreamedContent getFile() {
         //System.err.println(file.getName());
         if (file == null) {
             System.err.println("error getFile");
         }
         
         //System.err.println("Name of file : " + file.getName());
         return file;
     }
 
     public void setFile(StreamedContent file) {
         this.file = file;
     }
 }
