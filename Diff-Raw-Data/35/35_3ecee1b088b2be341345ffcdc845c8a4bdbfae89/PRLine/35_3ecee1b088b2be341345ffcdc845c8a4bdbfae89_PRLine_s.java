 package com.iappsam.forms;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import com.iappsam.Item;
 import com.iappsam.Validatable;
 
 @Entity
 @Table(name = "PR_Line")
 public class PRLine implements Validatable {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "PR_Line_ID")
 	private int id;
 
 	@Column(name = "Quantity")
 	private int quantity;
 
 	@ManyToOne
 	@JoinColumn(name = "Item_ID")
 	private Item item;
 
 	@Column(name = "Estimated_Unit_Cost")
	private long estimatedUnitCost;
 
 	@ManyToOne
 	@JoinColumn(name = "Purchase_Request_ID")
 	private PR purchaseRequest;
 
 	public PRLine() {
 		super();
 	}
 
 	public PRLine(PR pr, int quantity, Item item) {
 		super();
 		this.purchaseRequest = pr;
 		this.quantity = quantity;
 		this.item = item;
 	}
 
 	public int getQuantity() {
 		return quantity;
 	}
 
 	public String getItemDescription() {
 		return item.getDescription();
 	}
 
	public long getEstimatedUnitCost() {
		return estimatedUnitCost;
 	}
 
	public long getEstimatedCost() {
		return estimatedUnitCost * quantity;
 	}
 
 	public void setQuantity(int quantity) {
 		this.quantity = quantity;
 	}
 
 	public PR getPurchaseRequest() {
 		return purchaseRequest;
 	}
 
 	public void setPurchaseRequest(PR purchaseRequest) {
 		this.purchaseRequest = purchaseRequest;
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
 
	public void setEstimatedUnitCost(long estimatedUnitCost) {
 		this.estimatedUnitCost = estimatedUnitCost;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
		result = prime * result + (int) (estimatedUnitCost ^ (estimatedUnitCost >>> 32));
 		result = prime * result + ((item == null) ? 0 : item.hashCode());
 		result = prime * result + quantity;
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
 		PRLine other = (PRLine) obj;
		if (estimatedUnitCost != other.estimatedUnitCost)
 			return false;
 		if (item == null) {
 			if (other.item != null)
 				return false;
 		} else if (!item.equals(other.item))
 			return false;
 		if (quantity != other.quantity)
 			return false;
 		return true;
 	}
 
 	@Override
 	public boolean validate() {
 		boolean validItem = item != null && item.validate();
 		boolean validPr = purchaseRequest != null;
 		boolean validQuantity = quantity > 0;
 		boolean validEstUnitCost = estimatedUnitCost >= 0;
 		return validItem && validPr && validQuantity && validEstUnitCost;
 	}
 
 	public Object[] toArrayObjects() {
 		Object[] objs = new Object[6];
 		objs[0] = quantity + "";
 		objs[1] = item.getUnit().getName();
 		objs[2] = item.getDescription();
 		objs[3] = item.getStockNumber();
 		objs[4] = getEstimatedUnitCost() + "";
 		objs[5] = getEstimatedCost() + "";
 		return objs;
 	}
 }
