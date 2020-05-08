 package com.iappsam.forms;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import com.iappsam.Employee;
 import com.iappsam.Item;
 import com.iappsam.Validatable;
 
 @Entity
 @Table(name = "IE_Line")
 public class IELine implements Serializable, Validatable {
 
 	private static final long serialVersionUID = 3724182303894246727L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "id")
 	private int id;
 
 	@ManyToOne
 	@JoinColumn(name = "Item_ID")
 	private Item item;
 
 	@ManyToOne
 	@JoinColumn(name = "IE_ID")
 	private IE ie;
 
 	@Column(name = "Quantity")
 	private int quantity;
 
 	@ManyToOne
 	@JoinColumn(name = "Employee_ID")
 	private Employee employee;
 
 	@ManyToOne
 	@JoinColumn(name = "How_Acquired")
 	private ModeOfProcurement howAcquired;
 
 	@Column(name = "Remarks")
 	private String remarks;
 
 	public IELine() {
 		super();
 	}
 
 	public IELine(IE ie, Item item, String quantity, Employee employee, ModeOfProcurement howAcquired, String remarks) {
 		super();
 		this.item = item;
 		this.ie = ie;
 		setQuantity(quantity);
 		this.employee = employee;
 		this.howAcquired = howAcquired;
 		this.remarks = remarks;
 	}
 
 	private void setQuantity(String quantity2) {
 		setQuantity(checkNum(quantity2));
 	}
 
 	private int checkNum(String value) {
 		int num;
 		try {
 			num = Integer.parseInt(value);
 			if (num < 0)
 				return 0;
 		} catch (Exception e) {
 			return 0;
 		}
 		return num;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public Item getItem() {
 		return item;
 	}
 
 	public void setItem(Item item) {
 		this.item = item;
 	}
 
 	public IE getIe() {
 		return ie;
 	}
 
 	public void setIe(IE ie) {
 		this.ie = ie;
 	}
 
 	public int getQuantity() {
 		return quantity;
 	}
 
 	public void setQuantity(int quantity) {
 		this.quantity = quantity;
 	}
 
 	public Employee getEmployee() {
 		return employee;
 	}
 
 	public void setEmployee(Employee employee) {
 		this.employee = employee;
 	}
 
 	public ModeOfProcurement getHowAcquired() {
 		return howAcquired;
 	}
 
 	public void setHowAcquired(ModeOfProcurement howAcquired) {
 		this.howAcquired = howAcquired;
 	}
 
 	public String getRemarks() {
 		return remarks;
 	}
 
 	public void setRemarks(String remarks) {
 		this.remarks = remarks;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((employee == null) ? 0 : employee.hashCode());
 		result = prime * result + ((howAcquired == null) ? 0 : howAcquired.hashCode());
 		result = prime * result + ((item == null) ? 0 : item.hashCode());
 		result = prime * result + quantity;
 		result = prime * result + ((remarks == null) ? 0 : remarks.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		IELine other = (IELine) obj;
 		if (employee == null) {
 			if (other.employee != null)
 				return false;
 		} else if (!employee.equals(other.employee))
 			return false;
 		if (howAcquired == null) {
 			if (other.howAcquired != null)
 				return false;
 		} else if (!howAcquired.equals(other.howAcquired))
 			return false;
 		if (item == null) {
 			if (other.item != null)
 				return false;
 		} else if (!item.equals(other.item))
 			return false;
 		if (quantity != other.quantity)
 			return false;
 		if (remarks == null) {
 			if (other.remarks != null)
 				return false;
 		} else if (!remarks.equals(other.remarks))
 			return false;
 		return true;
 	}
 	
 
 	@Override
 	public boolean validate() {
 		boolean validItem = item != null && item.validate();
 		if (quantity < 0)
 			quantity = 0;
 		boolean validQuantity = quantity > 0;
 		boolean validEmployee = employee != null && employee.validate();
 		boolean validHowAcquired = howAcquired != null && howAcquired.validate();
 		return validItem && validQuantity && validEmployee && validHowAcquired;
 	}
 
 	public Object[] toArrayObject() {
 		Object[] objs = new Object[11];
 
 		objs[0] = item.getPropertyNumber();
 		objs[1] = item.getDescription();
 		objs[2] = "";
 		objs[3] = quantity + "";
 		objs[4] = item.getPrice();
 		objs[5] = (item.getPrice() * quantity) + "";
 		objs[6] = item.getDateAcquired() == null ? "" : item.getDateAcquired().getYear();
 		objs[7] = howAcquired + "";
 		objs[8] = item.getCondition() + "";
 		objs[9] = employee.getPerson().getName();
 		objs[10] = remarks == null ? "" : remarks;
 
 		return objs;
 	}
 
	public double getTotalAmount() {
		return item.getPrice() * quantity;
 	}
 }
