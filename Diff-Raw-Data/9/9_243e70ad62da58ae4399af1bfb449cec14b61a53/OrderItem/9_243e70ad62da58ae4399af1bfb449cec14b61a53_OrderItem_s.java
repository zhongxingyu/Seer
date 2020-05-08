 package de.switajski.priebes.flexibleorders.domain;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.validation.constraints.NotNull;
 
 import org.codehaus.jackson.annotate.JsonAutoDetect;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 import de.switajski.priebes.flexibleorders.domain.specification.CompletedSpecification;
 import de.switajski.priebes.flexibleorders.domain.specification.ConfirmedSpecification;
 import de.switajski.priebes.flexibleorders.domain.specification.OrderedSpecification;
 import de.switajski.priebes.flexibleorders.domain.specification.ShippedSpecification;
 import de.switajski.priebes.flexibleorders.reference.ProductType;
 import de.switajski.priebes.flexibleorders.web.entities.ReportItem;
 
 @Entity
 @JsonAutoDetect
 public class OrderItem extends GenericEntity implements Comparable<OrderItem> {
 
 	@JsonIgnore
 	@NotNull
 	@OneToMany(fetch = FetchType.LAZY, mappedBy = "orderItem")
 	private Set<HandlingEvent> deliveryHistory = new HashSet<HandlingEvent>();
 	
 	@NotNull
 	private Integer orderedQuantity;
 	
 	private Amount negotiatedPriceNet;
 	
 	@NotNull
 	@Embedded
 	private Product product;
 	
 	private String packageNumber;
 	
 	private String trackingNumber;
 	
 	@JsonIgnore
 	@NotNull
 	@ManyToOne
 	private FlexibleOrder flexibleOrder;
 
 	protected OrderItem() {}
 	
 	/**
 	 * Constructor with all attributes needed to create a valid item
 	 * @param order
 	 * @param product
 	 * @param orderedQuantity
 	 */
 	public OrderItem(FlexibleOrder order, Product product, int orderedQuantity){
 		this.deliveryHistory = new HashSet<HandlingEvent>();
 		this.flexibleOrder = order;
 		this.orderedQuantity = orderedQuantity;
 		setProduct(product);
 		
 		// handle birectional relationship
 		if (!order.getItems().contains(this))
 			order.getItems().add(this);
 	}
 	
 	public String toString(){
 		String s = "#"+getId().toString() + ": " + getOrderedQuantity() + 
 				" x " + getProduct().getName() + " " + provideStatus();
 		return s;
 	}
 	
 	@Override
 	public int compareTo(OrderItem o) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	public Set<HandlingEvent> getDeliveryHistory() {
 		return deliveryHistory;
 	}
 
 	public void setDeliveryHistory(Set<HandlingEvent> deliveryHistory) {
 		this.deliveryHistory = deliveryHistory;
 	}
 
 	public Amount getNegotiatedPriceNet() {
 		return negotiatedPriceNet;
 	}
 
 	public void setNegotiatedPriceNet(Amount negotiatedPriceNet) {
 		this.negotiatedPriceNet = negotiatedPriceNet;
 	}
 
 	public Integer getOrderedQuantity() {
 		return orderedQuantity;
 	}
 
 	public void setOrderedQuantity(Integer orderedQuantity) {
 		this.orderedQuantity = orderedQuantity;
 	}
 
 	public FlexibleOrder getOrder() {
 		return flexibleOrder;
 	}
 
 	public void setOrder(FlexibleOrder order) {
 		if (!order.getItems().contains(this))
 			order.getItems().add(this);
 		this.flexibleOrder = order;
 	}
 
 	public Product getProduct() {
 		return product;
 	}
 
 	public void setProduct(Product product) {
 		this.product = product;
 	}
 	
 	public String getPackageNumber() {
 		return packageNumber;
 	}
 
 	public void setPackageNumber(String packageNumber) {
 		this.packageNumber = packageNumber;
 	}
 
 	public String getTrackingNumber() {
 		return trackingNumber;
 	}
 
 	public void setTrackingNumber(String trackingNumber) {
 		this.trackingNumber = trackingNumber;
 	}
 
 	public Set<HandlingEvent> getAllHesOfType(HandlingEventType type) {
 		Set<HandlingEvent> hesOfType = new HashSet<HandlingEvent>();
 		for (HandlingEvent he: deliveryHistory){
 			if (he.getType() == type)
 				hesOfType.add(he);
 		}
 		return hesOfType;
 	}
 
 	/**
 	 * handles bidirectional relationship
 	 * @param handlingEvent has no other Report than this. If null, this orderItem will be set.
 	 */
 	public void addHandlingEvent(HandlingEvent handlingEvent) {
 		//prevent endless loop
 		if (deliveryHistory.contains(handlingEvent)) return;
 		deliveryHistory.add(handlingEvent);
 		handlingEvent.setOrderItem(this);
 	}
 	
 	public void removeHandlingEvent(HandlingEvent handlingEvent){
 		if (!deliveryHistory.contains(handlingEvent)) return;
 		deliveryHistory.remove(handlingEvent);
 		handlingEvent.setOrderItem(null);
 	}
 
 	public Report getReport(String invoiceNo) {
 		for (HandlingEvent he: getDeliveryHistory())
 			if (he.getReport().getDocumentNumber().equals(invoiceNo))
 				return he.getReport();
 		return null;
 	}
 	
 	/**
 	 * returns the summed quantity of all HandlingEvents of given HandlingEventType
 	 * 
 	 * @return null if no HandlingEvents of given type found
 	 */
 	public Integer getHandledQuantity(HandlingEventType type) {
 		Set<HandlingEvent> hes = getAllHesOfType(type);
 		if (hes.isEmpty()) return 0;
 		int summed = 0;
 		for (HandlingEvent he: hes){
 			summed = summed + he.getQuantity();
 		}
 		return summed;
 	}
 
 	public String provideStatus() {
 		String s = "";
 		if (new CompletedSpecification().isSatisfiedBy(this)) s+= "fertig";
 		else if (new ShippedSpecification(false, false).isSatisfiedBy(this)) s += "versendet";
 		else if (new ConfirmedSpecification(false, false).isSatisfiedBy(this)) s+= "best&auml;tigt";
 		else if (new OrderedSpecification().isSatisfiedBy(this)) s+= "bestellt";
 		else s+= "abgebrochen";
 		return s;
 	}
 	
 	/**
 	 * Creates a report item out of this order item.</br>
 	 * </br>
 	 * Report items in certain HandlingEvents are provided by 
 	 * {@link OrderItem#toReportItems}
 	 * 
 	 * @return
 	 */
 	public ReportItem toReportItem(){
 		ReportItem item = new ReportItem();
 		item.setCreated(getCreated());
 		item.setCustomer(getOrder().getCustomer().getId());
 		item.setCustomerNumber(getOrder().getCustomer().getCustomerNumber());
 		item.setCustomerName(getOrder().getCustomer().getLastName());
 		item.setId(getId());
 		if (getNegotiatedPriceNet() != null)
 			item.setPriceNet(getNegotiatedPriceNet().getValue());
 		item.setOrderNumber(getOrder().getOrderNumber());
 		item.setProduct(getProduct().getProductNumber());
 		item.setProductName(getProduct().getName());
 		item.setStatus(provideStatus());
 		item.setQuantity(getOrderedQuantity());
 		item.setQuantityLeft(getOrderedQuantity() - getHandledQuantity(HandlingEventType.CONFIRM));
 		return item;
 	}
 	
 	/**
 	 * Creates report items of this order item. </br>
 	 * 
 	 * @param type
 	 * @return
 	 */
 	public Set<ReportItem> toReportItems(HandlingEventType type){
 		Set<ReportItem> ris = new HashSet<ReportItem>();
 		if (type != null){
 			for (HandlingEvent he: this.getAllHesOfType(type)){
 				ReportItem item = he.toReportItem();
 				item.setQuantityLeft(calculateQuantityLeft(type));
 				ris.add(item);
 			}
 		} else 
 			ris.add(this.toReportItem());
 		return ris;
 	}
 
 	/**
 	 * Provides the quantity stuck in given {@link HandlingEventType}.
 	 * @param type HandlingEventType representing a state
 	 * @return quantity left in given {@link HandlingEventType}
 	 * @see ReportItem#getQuantityLeft
 	 */
 	public int calculateQuantityLeft(HandlingEventType type) {
 		int quantityLeft = 0;
 		switch (type) {
 		case CONFIRM:
 			quantityLeft = getOrderedQuantity() - getHandledQuantity(HandlingEventType.SHIP);
 			break;
 		case SHIP:
 			quantityLeft = getHandledQuantity(HandlingEventType.CONFIRM) 
 					- getHandledQuantity(HandlingEventType.SHIP);
 			break;
 		case INVOICE:
			quantityLeft = getHandledQuantity(HandlingEventType.INVOICE) 
					- getHandledQuantity(HandlingEventType.SHIP);
 			break;
 		case PAID:
			quantityLeft = getHandledQuantity(HandlingEventType.PAID)
					- getHandledQuantity(HandlingEventType.INVOICE);
 			break;
 		}
 		return quantityLeft;
 	}
 
 	public boolean isShippingCosts() {
 		return this.getProduct().getProductType().equals(ProductType.SHIPPING);
 	}
 	
 }
