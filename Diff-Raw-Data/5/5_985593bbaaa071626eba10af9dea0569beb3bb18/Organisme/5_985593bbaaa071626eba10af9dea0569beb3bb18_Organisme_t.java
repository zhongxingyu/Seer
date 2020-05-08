 package models;
 
 import play.data.validation.Email;
import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.data.validation.URL;
 import play.db.jpa.Blob;
 import play.db.jpa.Model;
 import play.modules.search.Field;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import java.util.Date;
 
 @Entity
 @Table(name = "organisme")
 public class Organisme extends Model {
 
     @ManyToOne
     public OrganismeMaster master;
 
     @Field(sortable=true)
     @Required
     public String nom;
 
     public Blob logo;
 
     public String siret;
 
     @Field
     public String telephone;
 
     @Field
     @Email
     public String email;
 
     @Field
     @Required
     @URL
     public String siteweb;
 
     @Field
     public String adresse;
 
     @Field
     @Required
     public String ville;
 
     @Field
     public String codePostal;
 
     @Field
     @Required
    @MaxSize(255)
     public String produit;
 
     @Field
    @MaxSize(255)
     public String description;
 
     @Field
     @Required
     public String tags;
 
     public Date creation;
 
     public String interlocuteur;
 
     @URL
     public String twitter;
 
     @URL
     public String facebook;
 
     @Field(sortable=true)
     public Date created = new Date();
 
     public Double wsg_x;
 
     public Double wsg_y;
 
     @ManyToOne
     public User user;
 
     @Field(joinField="libelle")
     @Required
     @ManyToOne
     public OrganismeType type;
 
     @Field(joinField="libelle")
     @Required
     @ManyToOne
     public OrganismeNbSalarie nbSalarie;
 
     @Field(joinField="libelle")
     @Required
     @ManyToOne
     public OrganismeActivite activite;
 
 }
