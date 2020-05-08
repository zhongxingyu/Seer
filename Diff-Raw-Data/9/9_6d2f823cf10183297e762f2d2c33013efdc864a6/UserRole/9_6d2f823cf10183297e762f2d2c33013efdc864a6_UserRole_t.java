 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import play.db.jpa.Model;
 
 /**
  *
  * @author wojciech.luczkow
  */
 @Entity
 @Table( name = "user_role" )
 public class UserRole extends Model {
 
    @JoinColumn(referencedColumnName = "username", name="username")
     @ManyToOne( optional = false )
     public NdgUser ndgUser;
     
     @JoinColumn(referencedColumnName = "roleName")
     @ManyToOne( optional = false )
     public NdgRole ndgRole;
 
     @Override
     public String toString() {
         return "br.org.indt.ndg.server.persistence.structure.UserRole[ id=" + id + " ]";
     }
     
 }
