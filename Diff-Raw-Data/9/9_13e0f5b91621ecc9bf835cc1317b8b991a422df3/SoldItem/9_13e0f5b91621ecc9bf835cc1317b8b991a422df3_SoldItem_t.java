 package ee.ut.math.tvt.salessystem.domain.data;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "SOLDITEM")
 public class SoldItem implements Cloneable, DisplayableItem {
 
 	@Id
 	@Column(name = "id")
 	private Long id;
 
 	@OneToOne
 	@JoinColumn(name = "stockitem_id", nullable = true)
 	private StockItem stockItem;
 
 	@Column(name = "name")
 	private String name;
 
 	@Column(name = "quantity")
 	private Integer quantity;
 
	@Column(name = "itemprice")
 	private double price;
 
 	@ManyToOne
 	@JoinColumn(name = "acceptedorder_id", nullable = true)
 	private AcceptedOrder acceptedOrder;
 
 	public SoldItem(StockItem stockItem, int quantity) {
 		this.stockItem = stockItem;
 		this.id = stockItem.getId();
 		this.name = stockItem.getName();
 		this.price = stockItem.getPrice();
 		this.quantity = quantity;
 	}
 
 	@Override
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public double getPrice() {
 		return price;
 	}
 
 	public void setPrice(double price) {
 		this.price = price;
 	}
 
 	public Integer getQuantity() {
 		return quantity;
 	}
 
 	public void setQuantity(Integer quantity) {
 		this.quantity = quantity;
 	}
 
 	public double getSum() {
 		return price * ((double) quantity);
 	}
 
 	public StockItem getStockItem() {
 		return stockItem;
 	}
 
 	public void setStockItem(StockItem stockItem) {
 		this.stockItem = stockItem;
 	}
 
 	public AcceptedOrder getAcceptedOrder() {
 		return acceptedOrder;
 	}
 
 	public void setAcceptedOrder(AcceptedOrder acceptedOrder) {
 		this.acceptedOrder = acceptedOrder;
 	}
 }
