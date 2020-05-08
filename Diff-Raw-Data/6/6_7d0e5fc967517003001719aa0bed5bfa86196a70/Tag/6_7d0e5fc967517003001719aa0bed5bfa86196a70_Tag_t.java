 package models;
 
 import javax.persistence.ColumnResult;
 import javax.persistence.Entity;
 import javax.persistence.EntityResult;
 import javax.persistence.FieldResult;
import javax.persistence.ManyToOne;
 import javax.persistence.SqlResultSetMapping;
 import javax.persistence.Table;
 
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 @Entity
 @Table(name = "TAG")
 @SqlResultSetMapping(name = "TagWithCount", entities = { @EntityResult(entityClass = Tag.class, fields = { @FieldResult(name = "id", column = "id"), @FieldResult(name = "nom", column = "nom"),
 		@FieldResult(name = "showOnGraph", column = "showOnGraph") }) }, columns = { @ColumnResult(name = "count") })
 public class Tag extends Model {
 
 	@Required
 	public String nom;
 
 	@Required
 	public boolean showOnGraph;

	@Required
	@ManyToOne
	public User user;
 }
