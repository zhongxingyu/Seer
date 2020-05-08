 package models;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import play.Logger;
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Page;
 
 /**
  * Payment entity managed by Ebean
  */
 @Entity
 @Table(name = "payments")
 public class Payment extends Model {
 
 	@Id
 	public Long id;
 
 	public String name;
 
 	@Constraints.Required
 	public BigDecimal amount;
 
 	@Constraints.MaxLength(value = 500)
 	public String remarks;
 
 	public String reference;
 
 	@Column(name = "paid_date")
 	@Formats.DateTime(pattern = "dd/MM/yyyy")
 	public Date paidDate = new Date();
 
 	@Constraints.Required
 	@ManyToOne
 	public Payee payee;
 
 	@Constraints.Required
 	@ManyToOne
 	public PaymentType paymentType;
 
 	@Transient
 	private String monthName;
 
 	@Transient
 	public static Map<String, String> months;
 
 	@Column(name = "start_period")
 	@Formats.DateTime(pattern = "dd/MM/yyyy")
 	public Date startPeriod; 
 
 	@Column(name = "end_period")
 	@Formats.DateTime(pattern = "dd/MM/yyyy")
 	public Date endPeriod; 
 
 	/**
 	 * Generic query helper for entity Payment with id Long
 	 */
 	public static Finder<Long, Payment> find = new Finder<Long, Payment>(
 			Long.class, Payment.class);
 
 	/**
 	 * Return a page of payment
 	 * 
 	 * @param page
 	 *            Page to display
 	 * @param pageSize
 	 *            Number of payments per page
 	 * @param sortBy
 	 *            Payment property used for sorting
 	 * @param order
 	 *            Sort order (either or asc or desc)
 	 * @param filter
 	 *            Filter applied on the name column
 	 */
 	public static Page<Payment> page(int page, int pageSize, String sortBy,
 			String order, String filter) {
 		return find.where().ilike("payee.name", "%" + filter + "%")
 				.orderBy(sortBy + " " + order).fetch("payee")
 				.findPagingList(pageSize).getPage(page);
 	}
 
 	public static Map<String, String> yearOptions() {
 		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
 
 		options.put("2013", "2013");
 		options.put("2012", "2012");
 
 		return options;
 	}
 
 	public Date getStartPeriod(){
 		if(this.id == null && this.startPeriod == null){
 			Calendar tempDate = Calendar.getInstance();
 			tempDate.set(Calendar.DATE, 1);
 			this.startPeriod = tempDate.getTime();
 		}
 		return this.startPeriod;
 	}
 	
 	public Date getEndPeriod(){
 		if(this.id == null && this.endPeriod == null){
 			Calendar tempDate = Calendar.getInstance();
 			tempDate.set(Calendar.DATE, tempDate.getActualMaximum(Calendar.DATE));
 			this.endPeriod = tempDate.getTime();
 		}
 		return this.endPeriod;
 	}
 
 }
