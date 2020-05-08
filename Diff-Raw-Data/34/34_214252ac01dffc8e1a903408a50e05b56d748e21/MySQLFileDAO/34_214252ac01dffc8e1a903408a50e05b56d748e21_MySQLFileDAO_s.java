 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.alexletov.fs.entities.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import ru.alexletov.fs.dto.FileDTO;
 import ru.alexletov.fs.dto.FileDTO;
 import ru.alexletov.fs.entities.File;
 import ru.alexletov.fs.entities.User;
 
 /**
  *
  * @author Alex
  */
 public class MySQLFileDAO implements FileDAO {
     protected EntityManager entityManager; 
 
     public MySQLFileDAO(EntityManager entityManager) {
         this.entityManager = entityManager;
     }
 
     @Override
     public ArrayList<FileDTO> getFilesByUserFolder(Integer id) {
         CriteriaBuilder builder = entityManager.getCriteriaBuilder();
         CriteriaQuery<File> criteria = builder.createQuery(File.class);
 
         Root<File> userRoot = criteria.from(File.class);
         criteria.select(userRoot);
         if(id != 0) {
             criteria.where(builder.equal(userRoot.get("userid"), id));
         }
         else {
             criteria.where(builder.equal(userRoot.get("shared"), 1));
         }
         List<File> files = null;
         try {
             files = entityManager.createQuery(criteria).getResultList();
         } catch (NoResultException ex) {
             Logger.getLogger(MySQLUserDAO.class.getName()).log(Level.INFO, null, ex);
             return new ArrayList<FileDTO>();
         }
         
         ArrayList<FileDTO> fid = new ArrayList<FileDTO>();
         for(File f : files) {
             FileDTO dto = new FileDTO(f);
             fid.add(dto);
         }
         return fid;
     }
 
     @Override
     public void addFile(FileDTO file) {
         File f = new File();
         f.setCreateDate(new Date());
         f.setDescription(file.getDescription());
         f.setPath(file.getPath());
         f.setName(file.getName());
         f.setShared(file.getShared()); 
         f.setContentType(file.getContentType());
         //FIXME: !!!
         User user = entityManager.find(User.class, file.getOwner().getId());
         
         f.setUserid(user);
         entityManager.persist(f);
     }
 
     @Override
     public void editFile(FileDTO file) {
         File f = entityManager.find(File.class, file.getId());
         f.setShared(file.getShared());
         f.setDescription(file.getDescription());
         entityManager.persist(f);
     }
     
     @Override
     public void deleteFile(FileDTO file) {
         File f = entityManager.find(File.class, file.getId());
         entityManager.remove(f);
     }
 }
