 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.training.dao;
 
 import com.artivisi.training.domain.User;
 import java.util.List;
 import java.util.Random;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author endy
  */
 @Repository @Transactional
 public class UserDao {
     
     @PersistenceContext
     private EntityManager entityManager;
         
     public void simpan(User u) throws Exception {
         if(u.getId() == null){
             entityManager.persist(u);
         } else {
             entityManager.merge(u);
         }
         
         // error secara random, untuk mengetes rollback @Transactional
         int rand = new Random().nextInt(10);
         if(rand % 2 == 0){
             throw new IllegalStateException("Pura-puranya terjadi error");
         }
     }
     
     public void hapus(User u){
         User ux = entityManager.find(User.class, u.getId());
         if(ux != null){
             entityManager.remove(ux);
         }
     }
     
     public User cariByUsername(String username){
         try {
             User u = (User) entityManager
                     .createQuery("select u from User u where u.username = :x")
                     .setParameter("x", username)
                     .getSingleResult();
            
            // supaya tidak kena LIE, load dulu data permission disini
            u.getRole().getDaftarPermission().size();
            
             return u;
         } catch (NoResultException err){
             return null;
         }
     }
     
     public List<User> semuaUser(Integer start, Integer rows){
         List<User> hasil = entityManager
                 .createQuery("select u from User u order by u.username")
                 .setFirstResult(start)
                 .setMaxResults(rows)
                 .getResultList();
         return hasil;
     }
     
     public Long hitungSemuaUser(){
         Long hasil = (Long) entityManager
                 .createQuery("select count(u) from User u")
                 .getSingleResult();
         return hasil;
     }
     
     public List<User> cariUserDenganNamaRole(String r){
         List<User> hasil = entityManager
                 .createQuery("select u from User u "
                 + "where u.role.kode like :kode or u.role.nama like :nama "
                 + "order by u.username")
                 .setParameter("kode", "%"+r+"%")
                 .setParameter("nama", "%"+r+"%")
                 .getResultList();
         return hasil;
     }
 }
