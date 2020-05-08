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
 
 import com.iappsam.Item;
 import com.iappsam.Validatable;
 
 @Entity
 @Table(name = "PO_Line")
 public class POLine implements Serializable, Validatable {
 	private static final long serialVersionUID = 7312439919136440157L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "Id")
 	private int id;
 
 	@ManyToOne
 	@JoinColumn(name = "Item_ID")
 	private Item item;
 
 	@ManyToOne
 	@JoinColumn(name = "PO_ID")
 	private PO po;
 
 	@Column(name = "quantity")
 	private int quantity;
 
 	public POLine() {
 		super();
 	}
 
 	public POLine(PO po, Item item, String quantity) {
 		super();
 		this.po = po;
 		this.item = item;
 		setQuantity(quantity);
 	}
 
 	private void setQuantity(String quantity) {
 		setQuantity(checkNum(quantity));
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
 
 	public int getQuantity() {
 		return quantity;
 	}
 
 	public void setQuantity(int quantity) {
 		this.quantity = quantity;
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
 
 	public PO getPo() {
 		return po;
 	}
 
 	public void setPo(PO po) {
 		this.po = po;
 	}
 
 	public float getAmount() {
 		double amount = item.getPrice() * quantity;
		return Float.parseFloat(Double.toString(amount));
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((item == null) ? 0 : item.hashCode());
 		result = prime * result + ((po == null) ? 0 : po.hashCode());
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
 		POLine other = (POLine) obj;
 		if (item == null) {
 			if (other.item != null)
 				return false;
 		} else if (!item.equals(other.item))
 			return false;
 		if (po == null) {
 			if (other.po != null)
 				return false;
 		} else if (!po.equals(other.po))
 			return false;
 		return true;
 	}
 
 	@Override
 	public boolean validate() {
 		if (quantity < 0)
 			quantity = 0;
 		return item != null && item.validate() && po != null && quantity > 0;
 	}
 
 	public Object[] toArrayObjects() {
 		Object[] objs = new Object[6];
 		objs[0] = item.getStockNumber();
 		objs[1] = item.getUnit().toString();
 		objs[2] = item.getDescription();
 		objs[3] = quantity + "";
 		objs[4] = item.getPrice() + "";
 		objs[5] = getAmount();
 
 		return objs;
 	}
 }
