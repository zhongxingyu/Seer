 package org.shept.apps.demo.order.orm;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.hibernate.annotations.ForeignKey;
 import org.shept.persistence.ModelCreation;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
 
 @Entity
public class Order implements Serializable, ModelCreation, Cloneable {
 	/**
 	 * 
 	 */
 
 	private static final long serialVersionUID = 1L;
 
 	private Integer id;
 
 	private Integer version;
 
 	private OrderNumber number;
 
 	private Calendar date;
 
 	private List<OrderItem> orderItems = new ArrayList<OrderItem>();
 
 	private Customer customer;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	@Version
 	public Integer getVersion() {
 		return version;
 	}
 
 	public void setVersion(Integer version) {
 		this.version = version;
 	}
 
 	/**
 	 * @return the ordernumber
 	 */
 	@OneToOne
 	@Cascade({ CascadeType.SAVE_UPDATE })
 	@ForeignKey(name = "fk_order_number")
 	public OrderNumber getNumber() {
 		return number;
 	}
 
 	/**
 	 * @param ordernumber
 	 *            the number to set
 	 */
 
 	public void setNumber(OrderNumber orderNumber) {
 		this.number = orderNumber;
 	}
 
 	/**
 	 * @return the date
 	 */
 	public Calendar getDate() {
 		return date;
 	}
 
 	/**
 	 * @param date
 	 *            the date to set
 	 */
 	public void setDate(Calendar date) {
 		this.date = date;
 	}
 
 	@ManyToOne()
 	@ForeignKey(name = "fk_order_customer")
 	public Customer getCustomer() {
 		return customer;
 	}
 
 	public void setCustomer(Customer customer) {
 		this.customer = customer;
 	}
 
 	@OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
 	public List<OrderItem> getOrderItems() {
 		return orderItems;
 	}
 
 	/**
  * 
  */
 	public void setOrderItems(List<OrderItem> orderItems) {
 		this.orderItems = orderItems;
 	}
 
 	public boolean isCreationAllowed(Object arg0) {
 		// Order order = (Order) arg0;
 		// if ( ! StringUtils.hasText(order.getName())) return false;
 		// if (StringUtils.hasText(name) &&
 		// name.equalsIgnoreCase(order.getName())) {
 		// return false;
 		// }
 		return true;
 	}
 
 	@Transient
 	public boolean isTransient() {
 		return getId() == null;
 	}
 
 	@Transient
 	public String getOrdernumberString() {
		if (number == null) {
			return "";
		}
 		Integer numId = this.getNumber().getId();
		if (numId == null) {
 			return "";
		}
		return numId.toString();
	}

	/**
	 * return a copy of this object without identifying infos that can be saved as a copy
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		Order copy = BeanUtils.instantiate(this.getClass());
		BeanUtils.copyProperties(this, copy, new String[]{"id"});
		copy.setNumber(new OrderNumber());
		copy.setDate(Calendar.getInstance());
		return copy;
 	}
 
 	/**
 	 * let's do some chaining here
 	 * 
 	 * @param person
 	 */
 	public void initialize(Customer customer) {
 		this.customer = customer;
 	}
 
 }
