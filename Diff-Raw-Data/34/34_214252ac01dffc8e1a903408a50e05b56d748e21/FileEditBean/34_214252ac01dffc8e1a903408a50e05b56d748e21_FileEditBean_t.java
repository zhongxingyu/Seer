 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ru.alexletov.fs;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import ru.alexletov.fs.dto.FileDTO;
 
 /**
  *
  * @author Alex
  */
 @ManagedBean
 @SessionScoped
 public class FileEditBean {
     @EJB
     private FileBean fb;
     
     private FileDTO fileToEdit;
    private String name;
    private String ext;
     private String description;
     private boolean shared;
     /**
      * Creates a new instance of FileEditBean
      */
     public FileEditBean() {
     }
 
     public String edit() {
        fileToEdit.setName(name + "." + ext);
         fileToEdit.setDescription(description);
         fileToEdit.setShared(shared ? 1 : 0);
         fb.editFile(fileToEdit);
         return "../index.xhtml";
     }
     
     public FileDTO getFileToEdit() {
         return fileToEdit;
     }
 
     public void setFileToEdit(FileDTO file) {
         this.fileToEdit = file;
         
         //Get file info
         description = fileToEdit.getDescription();
         shared = (fileToEdit.getShared() != 0);
        String fname = fileToEdit.getName();
        Integer extp = fname.lastIndexOf('.');
        if (extp > -1) {
            name = fname.substring(0, extp);
            ext = fname.substring(extp + 1);    
        }
        else {
            name = fname;
            ext = "";
        }            
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public boolean isShared() {
         return shared;
     }
 
     public void setShared(boolean shared) {
         this.shared = shared;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
 }
