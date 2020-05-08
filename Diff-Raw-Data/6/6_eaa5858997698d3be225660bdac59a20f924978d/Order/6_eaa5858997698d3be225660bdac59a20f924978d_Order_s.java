 import java.util.ArrayList;
 
 public class Order {
 	private int id;
 	private String customerName;
 	private String customerAddress;
 	private ArrayList<Book> books;
 
 	private static int idCounter = 1;
 	private static final int MAX_BOOK_COUNT = 5;
 
 	public Order() {
 		this.id = idCounter++;
 		this.customerName = "";
 		this.customerAddress = "";
 		this.books = new ArrayList<Book>(MAX_BOOK_COUNT);
 	}
 
 	public Order(String customerName, String customerAddress) {
 		this.id = idCounter++;
 		this.customerName = customerName;
 		this.customerAddress = customerAddress;
 		this.books = new ArrayList<Book>(MAX_BOOK_COUNT);
 	}
 
 	public void addBook(Book book) {
		this.books.add(book);
 	}
 
 	public int getTotalPrice() {
 		int totalPrice = 0;
 		for (Book book : this.books) {
 			totalPrice += book.getPrice();
 		}
 		return totalPrice;
 	}
 	
 	public void setCustomerName(String customerName)
 	{
 		this.customerName = customerName;
 	}
 	
 	public void setCustomerAddress(String customerAddress)
 	{
 		this.customerAddress = customerAddress;
 	}
 
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 
 		sb.append(String.format("Order id: %d, Customer: %s, %s\n", this.id,
 				this.customerName, this.customerAddress));
 		for (Book book : this.books) {
 			sb.append(book.toString() + "\n" );
 		}
		sb.append(String.format("Total price: %d CHF\n", this.getTotalPrice()));
 
 		return sb.toString();
 	}
 }
