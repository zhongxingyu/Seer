 package base;
 
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 
 /**
  * 
  * @author darius
  * 
  */
 @XmlRootElement
 public class Order implements Comparable<Order>, Cloneable {
 
 	private int num;
 	private String name;
 	private String surname;
 	private String country;
 	private String city;
 	private String address;
 	private String postCode;
 	private Date createdAt;
 	private Vector<OrderProduct> products;
 
 	public Order() {
 
 	}
 
 	public Order(int num, String name, String surname, String country,
 			String city, String address, String postCode, Cart cart)
 			throws EmptyValueException {
 		this.setNum(num);
 		this.setName(name);
 		this.setSurname(surname);
 		this.setCountry(country);
 		this.setCity(city);
 		this.setAddress(address);
 		this.setPostCode(postCode);
 		this.setProducts(new Vector<OrderProduct>());
 		this.setProductsFromCart(cart);
 		this.setCreatedAt(new Date());
 	}
 
 	public int getNum() {
 		return num;
 	}
 
 	@XmlAttribute
 	public void setNum(int num) {
 		this.num = num;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	@XmlElement
 	public void setName(String name) throws EmptyValueException {
 		if (name.isEmpty()) {
 			throw new EmptyValueException("Name");
 		}
 		this.name = name;
 	}
 
 	public String getSurname() {
 		return surname;
 	}
 
 	@XmlElement
 	public void setSurname(String surname) throws EmptyValueException {
 		if (surname.isEmpty()) {
 			throw new EmptyValueException("Surname");
 		}
 		this.surname = surname;
 	}
 
 	public String getCountry() {
 		return country;
 	}
 
 	@XmlElement
 	public void setCountry(String country) throws EmptyValueException {
 		if (country.isEmpty()) {
 			throw new EmptyValueException("Country");
 		}
 		this.country = country;
 	}
 
 	public String getCity() {
 		return city;
 	}
 
 	@XmlElement
 	public void setCity(String city) throws EmptyValueException {
 		if (city.isEmpty()) {
 			throw new EmptyValueException("City");
 		}
 		this.city = city;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	@XmlElement
 	public void setAddress(String address) throws EmptyValueException {
 		if (address.isEmpty()) {
 			throw new EmptyValueException("Address");
 		}
 		this.address = address;
 	}
 
 	public String getPostCode() {
 		return postCode;
 	}
 
 	@XmlElement
 	public void setPostCode(String postCode) throws EmptyValueException {
 		if (postCode.isEmpty()) {
 			throw new EmptyValueException("PostCode");
 		}
 		this.postCode = postCode;
 	}
 
 	public Date getCreatedAt() {
 		return createdAt;
 	}
 
 	@XmlElement
 	public void setCreatedAt(Date createAt) {
 		this.createdAt = createAt;
 	}
 
 	public Vector<OrderProduct> getProducts() {
 		return products;
 	}
 
 	@XmlElement
 	public void setProducts(Vector<OrderProduct> products) {
 		this.products = products;
 	}
 
 	public void setProductsFromCart(Cart cart) {
 		if (cart != null) {
 			Iterator<CartProduct> productsIterator = cart.getProducts()
 					.iterator();
 			while (productsIterator.hasNext()) {
 				CartProduct cartProduct;
 				cartProduct = productsIterator.next();
 
 				OrderProduct orderProduct;
 				orderProduct = new OrderProduct(cartProduct.getIsbn(),
 						cartProduct.getName(), cartProduct.getPrice(),
 						cartProduct.getQuantity());
 
 				products.add(orderProduct);
 			}
 		}
 	}
 
 	public double getTotalPrice() {
 		double amount = 0;
 
 		Iterator<OrderProduct> productsIterator = this.getProducts().iterator();
 		while (productsIterator.hasNext()) {
 			amount += productsIterator.next().getTotalPrice();
 		}
 
 		return amount;
 	}
 
 	public int getTotalProductsQuantity() {
 		int amount = 0;
 
 		Iterator<OrderProduct> productsIterator = this.getProducts().iterator();
 		while (productsIterator.hasNext()) {
 			amount += productsIterator.next().getQuantity();
 		}
 
 		return amount;
 	}
 
 	@Override
 	public String toString() {
 		Format dateFormatter;
 		dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		StringBuilder s = new StringBuilder();
 
 		s.append("-=| Order data output |=-");
 		s.append("\n" + "Num: " + this.getNum());
 		s.append("\n" + "Name: " + this.getName());
 		s.append("\n" + "Surname: " + this.getSurname());
 		s.append("\n" + "Country: " + this.getCountry());
 		s.append("\n" + "City: " + this.getCity());
 		s.append("\n" + "Address: " + this.getAddress());
 		s.append("\n" + "Post code: " + this.getPostCode());
 		s.append("\n" + "Created at: "
 				+ dateFormatter.format(this.getCreatedAt()));
 		s.append("\n" + "Order products:");
 
 		Iterator<OrderProduct> productsIterator = this.getProducts().iterator();
 
 		while (productsIterator.hasNext()) {
 			s.append("\n" + productsIterator.next().toString());
 		}
 
 		return s.toString();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Order) {
 			if (this.getNum() == ((Order) obj).getNum()) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return super.equals(obj);
 		}
 	}
 
 	@Override
 	public int compareTo(Order o) {
 		return this.getNum() - o.getNum();
 	}
 	
 	public static Comparator<Order> OrderPriceComparator = new Comparator<Order>() {
 		@Override
 		public int compare(Order o1, Order o2) {
 			if(o2.getTotalPrice() > o1.getTotalPrice()) {
 				return 1;
 			}else if(o2.getTotalPrice() < o1.getTotalPrice()) {
 				return -1;
 			}else{
 				return 0;
 			}
 		}
 	
 	};
 	
	@SuppressWarnings("unchecked")
 	public Object clone() {
 		try {
 			Order order = (Order) super.clone();
 			
 			// new vector
			order.setProducts((Vector<OrderProduct>) this.getProducts().clone());
			order.getProducts().removeAllElements();
 			
 			// clone products
 			Iterator<OrderProduct> productsIterator = this.getProducts().iterator();
 
 			while (productsIterator.hasNext()) {
 				order.getProducts().add((OrderProduct) productsIterator.next().clone());
 			}
 			
 			return order;
 		} catch (CloneNotSupportedException e) {
 			// This should never happen
 			throw new InternalError(e.toString());
 		}
 	}
 	
 
 }
