 package domain;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 
 public class Library extends Observable{
 
 	private List<Copy> copies;
 	private List<Customer> customers;
 	private List<Loan> loans;
 	private List<Book> books;
 
 	public Library() {
 		copies = new ArrayList<Copy>();
 		customers = new ArrayList<Customer>();
 		loans = new ArrayList<Loan>();
 		books = new ArrayList<Book>();
 	}
 
 	public Loan createAndAddLoan(Customer customer, Copy copy) {
 		this.setChanged();
 		
 		if (!isCopyLent(copy)) {
 			Loan l = new Loan(customer, copy);
 			loans.add(l);
 			this.notifyObservers(this);
 			return l;
 		} else {
 			this.notifyObservers(this);
 			return null;
 		}
 	}
 
 	public Customer createAndAddCustomer(String name, String surname) {
 		this.setChanged();
 		
 		Customer c = new Customer(name, surname);
 		customers.add(c);
 		this.notifyObservers(this);
 
 		return c;
 	}
 
 	public Book createAndAddBook(String name) {
 		this.setChanged();
 		
 		Book b = new Book(name);
 		books.add(b);
 		this.notifyObservers(this);
 
 		return b;
 	}
 
 	public Copy createAndAddCopy(Book title) {
 		this.setChanged();
 
 		Copy c = new Copy(title);
 		copies.add(c);
 		this.notifyObservers(this);
 
 		return c;
 	}
 
 	public Book findByBookTitle(String title) {
 		for (Book b : books) {
 			if (b.getName().equals(title)) {
 				return b;
 			}
 		}
 		return null;
 	}
 
 	public boolean isCopyLent(Copy copy) {
 		for (Loan l : loans) {
 			if (l.getCopy().equals(copy) && l.isLent()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public List<Copy> getCopiesOfBook(Book book) {
 		List<Copy> res = new ArrayList<Copy>();
 		for (Copy c : copies) {
 			if (c.getTitle().equals(book)) {
 				res.add(c);
 			}
 		}
 
 		return res;
 	}
 
 	public List<Loan> getLentCopiesOfBook(Book book) {
 		List<Loan> lentCopies = new ArrayList<Loan>();
 		for (Loan l : loans) {
 			if (l.getCopy().getTitle().equals(book) && l.isLent()) {
 				lentCopies.add(l);
 			}
 		}
 		return lentCopies;
 	}
 	
 	public int getNoOfAvailableCopiesOfBook(Book book){
 	
 		return getCopiesOfBook(book).size() - getLentCopiesOfBook(book).size();
 		
 		
 	}
 
 	public List<Loan> getCustomerLoans(Customer customer) {
 		List<Loan> lentCopies = new ArrayList<Loan>();
 		for (Loan l : loans) {
 			if (l.getCustomer().equals(customer)) {
 				lentCopies.add(l);
 			}
 		}
 		return lentCopies;
 	}
 	
 	public List<Loan> getOverdueLoans() {
 		List<Loan> overdueLoans = new ArrayList<Loan>();
 		for ( Loan l : getLoans() ) {
 			if (l.isOverdue())
 				overdueLoans.add(l);
 		}
 		return overdueLoans;
 	}
 	
 	public String getCustomerStatus(Customer cust) {
 		for(Loan loan : this.getLoansOfCustomer(cust)) {
 			if(loan.isOverdue()) {
				return "überfällig";
 			}
 		} 
		return "OK";
 		
 	}
 	
 	public List<Loan> getLoansOfCustomer(Customer cust) {
 		List<Loan> customerLoans = new ArrayList<>();
 		
 		for(Loan loan : this.getLoans()) {
 			if(loan.getCustomer().equals(cust)){
 				customerLoans.add(loan);
 			}
 		}
 		return customerLoans;
 		
 		
 	}
 	
 	public List<Copy> getAvailableCopies(){
 		return getCopies(false);
 	}
 	
 	public List<Copy> getLentOutBooks(){
 		return getCopies(true);
 	}
 
 	private List<Copy> getCopies(boolean isLent) {
 		List<Copy> retCopies = new ArrayList<Copy>();
 		for (Copy c : copies) {
 			if (isLent == isCopyLent(c)) {
 				retCopies.add(c);
 			}
 		}
 		return retCopies;
 	}
 	
 
 
 	public List<Copy> getCopies() {
 		return copies;
 	}
 
 	public List<Loan> getLoans() {
 		return loans;
 	}
 
 	public List<Book> getBooks() {
 		return books;
 	}
 
 	public List<Customer> getCustomers() {
 		return customers;
 	}
 
 }
