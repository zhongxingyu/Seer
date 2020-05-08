 package models;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.data.validation.Constraints.MaxLength;
 import play.data.validation.Constraints.Min;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Page;
 
 /**
  * Computer entity managed by Ebean
  */
 @Entity 
 public class Transaction extends Model {
 
 	@Id
 	public Long id;
 	
 	@ManyToOne
 	@Required
 	@Formats.NonEmpty
 	public Account accountFrom;
 	
 	@ManyToOne
 	@Required
 	@Formats.NonEmpty
 	public Account accountTo;
 	
 	@MaxLength(80)
 	public String reference;
 
 	@Formats.DateTime(pattern="yyyy-MM-dd")
 	@Constraints.Required
 	public Date date;
 	
 	@Constraints.Required
 	@Column(precision = 15, scale = 2) 
 	@Min(0)
 	public BigDecimal value;	//TODO:Format to 2 Decimals
 
 //	@Constraints.Required
 //	public String currency;
 
 	@Constraints.Required
 	@Enumerated(EnumType.STRING)
 	public Currency currency;
 	
 	
 	public enum Currency {  
         USD, EUR, GBP  
     }  
 
 	/**
 	 * Generic query helper for entity Transaction with id Long
 	 */
 	public static Finder<Long,Transaction> find = new Finder<Long,Transaction>(Long.class, Transaction.class); 
 
 	/**
 	 * Return a page of transactions
 	 *
 	 * @param page Page to display
 	 * @param pageSize Number of transactions per page
 	 * @param sortBy Transaction property used for sorting
 	 * @param order Sort order (either or asc or desc)
 	 * @param filter Filter applied on the name column
 	 */
 	public static Page<Transaction> page(int page, int pageSize, String sortBy, String order, String filter) {
 		return 
 				find.where()
 				.ilike("reference", "%" + filter + "%")
 				.orderBy(sortBy + " " + order)
 				.fetch("accountTo")	//TODO: is this gonna work? -> just copied..
 				.findPagingList(pageSize)
 				.getPage(page);
 	}
 	
 	/**
 	 * Return a page of transactions
 	 *
 	 * @param page Page to display
 	 * @param pageSize Number of transactions per page
 	 * @param sortBy Transaction property used for sorting
 	 * @param order Sort order (either or asc or desc)
 	 * @param filter Filter applied on the name column
 	 */
 	public static Page<Transaction> pageByCustomColumn(int page, int pageSize, String sortBy, String order, String filterBy, String filter) {
 		return 
 				find.where()
 				.ilike(filterBy, "%" + filter + "%")
 				.orderBy(sortBy + " " + order)
 				.fetch("accountTo")	//TODO: is this gonna work? -> just copied..
 				.findPagingList(pageSize)
 				.getPage(page);
 	}
 
 }
