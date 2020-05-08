 /*
  */
 package org.fit.pis.library.back;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import org.fit.pis.library.data.*;
 import org.richfaces.component.UIDataTable;
 
 /**
  * 
  * @author Vojtěch Sysel <xsysel03@setud.fit.vutbr.cz>
  */
 @ManagedBean
 @SessionScoped
 public class SearchBooksBean {
 
 	@EJB
 	private BookManager bookMgr;
 	@EJB
 	private GenreManager genreMgr;
 	@EJB
 	private BookingManager bookingMgr;
 	@EJB
 	private UserManager userMgr;
 	@EJB
 	private ExemplarManager exemplarMgr;
 	@EJB
 	private PublisherManager publisherMgr;
 	@EJB
 	private BorrowManager borrowMgr;
 	@EJB
 	private AuthorManager authorMgr;
 	private Book book;
 	private Exemplar exemplar;
 	private UIDataTable listTable;
 	private UIDataTable exemplarListTable;
 	private UIDataTable bookingListTable;
 	@ManagedProperty(value = "#{authenticationBean}")
 	private AuthenticationBean authBean;
 	
 	// variables used for filtering table
 	private int a;
 	private String filter_name;
 	private String filter_author;
 	private Calendar filter_dateFrom;
 	private Calendar filter_dateTo;
 	private String filter_genre;
 	private String filter_isbn_issn;
 
 	SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
 	private int minBookYear;
 	private int maxBookYear;
 
 	/** Creates a new instance of ManageUsersBean */
 	public SearchBooksBean() {
 		clearFilter();
 	}
 
 	/**
 	 * Clear filter variables
 	 */
 	private void clearFilter() {
 		book = new Book();
 
 		// set empty filtering
 		filter_name = "";
 		filter_author = "";
 		filter_genre = "";
 		filter_isbn_issn = "";
 		filter_dateFrom = Calendar.getInstance();
 		filter_dateFrom.set(Book.MIN_BOOK_YEAR, Calendar.JANUARY, 1);
 		filter_dateTo = Calendar.getInstance();
 		filter_dateTo.set(Book.MAX_BOOK_YEAR, Calendar.DECEMBER, 31);
 
 		minBookYear = Book.MIN_BOOK_YEAR;
 		maxBookYear = Book.MAX_BOOK_YEAR;
 	}
 
 	/**
 	 * Get user
 	 * @return 
 	 */
 	public Book getBook() {
 		return book;
 	}
 
 	/**
 	 * Set user
 	 * @param user 
 	 */
 	public void setBook(Book book) {
 		this.book = book;
 	}
 
 	public Exemplar getExemplar() {
 		return exemplar;
 	}
 
 	/**
 	 * Set user
 	 * @param user 
 	 */
 	public void Exemplar(Exemplar exemplar) {
 		this.exemplar = exemplar;
 	}
 
 	public List<Author> getAuthors() {
 		return authorMgr.findAll();
 	}
 	
 	/**
 	 * Authentication bean setter
 	 * @param authBean 
 	 */
 	public void setAuthBean(AuthenticationBean authBean) {
 		this.authBean = authBean;
 	}
 
 	// <editor-fold defaultstate="collapsed" desc="Filter getters and setters">
 	public String getFilter_name() {
 		return filter_name;
 	}
 
 	public void setFilter_name(String filter_name) {
 		this.filter_name = filter_name;
 	}
 
 	// <editor-fold defaultstate="collapsed" desc="Filter getters and setters">
 	public String getFilter_author() {
 		return filter_author;
 	}
 
 	public void setFilter_author(String filter_author) {
 		this.filter_author = filter_author;
 	}
 
 	public String getFilter_isbn_issn() {
 		return filter_isbn_issn;
 	}
 
 	public void setFilter_isbn_issn(String filter_isbn_issn) {
 		this.filter_isbn_issn = filter_isbn_issn;
 	}
 
 	public String getFilter_genre() {
 		return filter_genre;
 	}
 
 	public void setFilter_genre(String genre) {
 		filter_genre = genre;
 	}
 
 	public Date getFilter_dateFrom() {
 		return filter_dateFrom.getTime();
 	}
 
 	public void setFilter_dateFrom(Date dateFrom) {
 		this.filter_dateFrom.setTime(dateFrom);
 	}
 
 	public String getFilter_yearFrom() {
 		if (filter_dateFrom == null) {
 			return "";
 		}
 
 		// format date
 		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy");
 		return dateformat.format(filter_dateFrom.getTime());
 	}
 
 	public int getA() {
 		return this.a;
 
 	}
 
 	public int getMinBookYear() {
 		return minBookYear;
 	}
 
 	public void setMinBookYear(int year) {
 		minBookYear = year;
 	}
 
 	public int getMaxBookYear() {
 		return maxBookYear;
 	}
 
 	public void setMaxBookYear(int year) {
 		maxBookYear = year;
 	}
 
 	public void setFilter_yearFrom(String yearFrom) {
 		int year = Integer.parseInt(yearFrom);
 		filter_dateFrom.set(year, Calendar.JANUARY, 1);
 	}
 
 	public String getFilter_yearTo() {
 		if (filter_dateTo == null) {
 			return "";
 		}
 
 		// format date
 		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy");
 		return dateformat.format(filter_dateTo.getTime());
 	}
 
 	public void setFilter_yearTo(String yearFrom) {
 		int year = Integer.parseInt(yearFrom);
 		filter_dateTo.set(year, Calendar.DECEMBER, 31);
 	}
 	// </editor-fold>
 
 	/**
 	 * Get list of users
 	 * @return 
 	 */
 	public List<Book> getBooks() {
 		// switch dates
 		if (filter_dateFrom.compareTo(filter_dateTo) > 0) {
 			Calendar tmp = filter_dateFrom;
 			filter_dateFrom = filter_dateTo;
 			filter_dateTo = tmp;
 		}
 
 		Date yearFrom = filter_dateFrom.getTime();
 		Date yearTo = filter_dateTo.getTime();
 
 		// genre
 		Genre genre = null;
 		if (!filter_genre.isEmpty() && !filter_genre.equalsIgnoreCase("all")) {
 			genre = genreMgr.findByName(filter_genre);
 			if (genre == null) {
 				System.err.println("Badly working database encoding!");
 			}
 		}
 		// author
 		Author author = null;
 		if (!filter_author.isEmpty() && !filter_author.equalsIgnoreCase("all")) {
 			author = authorMgr.findByName(filter_author);
 			if (author == null) {
 				System.err.println("Badly working database encoding!");
 			}
 		}
 
 		return bookMgr.find(filter_name, author, yearFrom, yearTo, genre, filter_isbn_issn);
 	}
 
 	/**
 	 * Get records table
 	 * @return 
 	 */
 	public UIDataTable getListTable() {
 		return null;	// force rebuild of table
 	}
 
 	/**
 	 * Set records table
 	 * @param table 
 	 */
 	public void setListTable(UIDataTable table) {
 		this.listTable = table;
 	}
 
 	/**
 	 * Get records table
 	 * @return 
 	 */
 	public UIDataTable getExemplarListTable() {
 		return null;	// force rebuild of table
 	}
 
 	/**
 	 * Set records table
 	 * @param table 
 	 */
 	public void setExemplarListTable(UIDataTable table) {
 		this.exemplarListTable = table;
 	}
 
 	/**
 	 * Get booking table
 	 * @return 
 	 */
 	public UIDataTable getBookingListTable() {
 		return null;	// force rebuild of table
 	}
 
 	/**
 	 * Set booking list table
 	 * @return 
 	 */
 	public void setBookingListTable(UIDataTable table) {
 		this.bookingListTable = table;
 	}
 
 	/**
 	 * Count of curently free exemplars
 	 * @return 
 	 */
 	public int getCountExemplarsFree() {
 		if (book == null) {
 			return 0;
 		}
 
 		int freeCount = 0;
 		List<Exemplar> exemplars = exemplarMgr.findByBook(book);
 		// list exemplars
 		for (Exemplar e : exemplars) {
 			if (!e.getIsBorrowed()) {
 				freeCount++;
 			}
 		}
 
 		return freeCount;
 	}
 
 	/**
 	 * Count of currently borrowed exemplars
 	 * @return 
 	 */
 	public int getCountExemplarsBorrowed() {
 		if (book == null) {
 			return 0;
 		}
 
 		int borrowedCount = 0;
 		List<Exemplar> exemplars = exemplarMgr.findByBook(book);
 		// list exemplars
 		for (Exemplar e : exemplars) {
 			if (e.getIsBorrowed()) {
 				borrowedCount++;
 			}
 		}
 
 		return borrowedCount;
 	}
 
 	/**
 	 * REturn true if user can borrow book
 	 * @param user
 	 * @return 
 	 */
 	public boolean canBorrowBook(User user) {
 		if (book == null) {
 			return false;
 		}
 
 		// Zkontroluje, jestli náhodou nemám půjčený exemplář
 		List<Exemplar> exemplars = exemplarMgr.findByBook(book);
 		if (!exemplars.isEmpty()) {
 			for (Exemplar e : exemplars) {
 				System.out.println(e);
 				// book is already borrowed or borrowed by user
 				if (e.getIsBorrowed() || e.isBorrowedByUser(user)) {
 					return false;
 				}
 			}
 		}
 
 		List<Booking> booking = bookingMgr.find(book);
 		// no booking record
 		if (booking.isEmpty()) {
 			return true;
 		}
 
 		// can borrow only if is first in list
 		if (booking.get(0).getUser().getIduser() == user.getIduser()) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Return exemplar collection
 	 * @param book
 	 * @return 
 	 */
 	public List<Exemplar> getExemplarCollection(Book book) {
 		return exemplarMgr.findByBook(book);
 	}
 
 	/**
 	 * Return booking collection
 	 * @param book
 	 * @return 
 	 */
 	public List<Booking> getBookingCollection(Book book) {
 		return bookingMgr.find(book);
 	}
 
 	public boolean isBorrowLinkDisplayed(Exemplar exemplar, User user) {
 		return !exemplar.getIsBorrowed() && canBorrowBook(user);
 	}
 
 	/**
 	 * Search books
 	 * @return 
 	 */
 	public String actionSearchBooks() {
 		return "searchBooks";
 	}
 
 	/**
 	 * Just view catalog
 	 * @return 
 	 */
 	public String actionViewBooks() {
 		clearFilter();
 
 		return "viewBooks";
 	}
 
 	/**
 	 * Book detail
 	 * @return 
 	 */
 	public String actionDetail() {
 
 		setBook((Book) listTable.getRowData());
 		this.a = book.getIdbook();
 
 		return "detail";
 	}
 
 	/**
 	 * Book cancel detail
 	 * @return 
 	 */
 	public String actionCancelDetail() {
 		book = null;
 
 		return "cancelDetail";
 	}
 
 	/**
 	 * Book booking ;-)
 	 * @return 
 	 */
 	public String actionBookBooking() {
 		// user
 		User user = userMgr.find(authBean.getIduser());
 
 		return bookBooking(user);
 	}
 
 	/**
 	 * Book booking ;-)
 	 * @return 
 	 */
 	public String actionBookBooking(User user) {
 		return bookBooking(user);
 	}
 
 	/**
 	 * Book booking function
 	 * @param user
 	 * @return 
 	 */
 	private String bookBooking(User user) {
 		String action = "detailBookBooking";
 
 		// check if user has already booked this title
 		Collection<Booking> colection = bookingMgr.find(user);
 		if (colection != null) {
 			for (Booking b : colection) {
 				// book is already booked
 				if (b.getBook().getIdbook() == book.getIdbook()) {
 					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Book is already booked!"));
 					return action;
 				}
 			}
 		}
 
 		// check borrowed
 		List<Exemplar> exemplars = exemplarMgr.findByBook(book);
 		if (!exemplars.isEmpty()) {
 			for (Exemplar e : exemplars) {
 				// book is already borrowed
 				if (e.getIsBorrowed()) {
 					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Can't book borrowed book!"));
 					return action;
 				}
 			}
		// no exemplar - can't book
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Can't book book with no exemplars!"));
			return action;
 		}
 
 
 		// create new booking
 		Booking booking = new Booking();
 		booking.setBook(book);
 		booking.setState(0);
 		booking.setDate(new Date(System.currentTimeMillis()));
 		booking.setUser(user);
 
 		try {
 			// save booking
 			bookingMgr.Save(booking);
 		} catch (javax.ejb.EJBException e) {
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Changes couldn't be saved. Please try again later."));
 			return action;
 		}
 
 		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Book was successfully booked."));
 
 		// reaload book
 		bookReload();
 
 		return action;
 	}
 
 	/**
 	 * Reload book info
 	 */
 	private void bookReload() {
 		book = bookMgr.findByIdbook(book.getIdbook());
 	}
 
 	/**
 	 * Remove book booking
 	 * @return 
 	 */
 	public String actionBookingRemove() {
 		Booking selected = (Booking) bookingListTable.getRowData();
 
 		try {
 			bookingMgr.Remove(selected);
 		} catch (javax.ejb.EJBException e) {
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Changes couldn't be saved. Please try again later."));
 			return "viewMyBooking";
 		}
 
 		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Booking successfully removed."));
 
 		// clear list table
 		bookingListTable = null;
 
 		// reload book
 		bookReload();
 
 		return "";
 	}
 
 	/**
 	 * Borrow book
 	 * @return 
 	 */
 	public String actionBorrow(User user) {
 		// check 
 		if (!canBorrowBook(user)) {
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Can't borrow book!"));
 			return "";
 		}
 
 		// check borrowed
 		List<Exemplar> exemplars = exemplarMgr.findByBook(book);
 		if (!exemplars.isEmpty()) {
 			for (Exemplar e : exemplars) {
 				// book is already borrowed
 				if (e.getIsBorrowed()) {
 					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Can't borrow borrowed book!"));
 					return "newBorrow";
 				}
 			}
 		}
 
 		// check booking
 		List<Booking> bookings = bookingMgr.find(book);
 
 		// has booking record
 		if (!bookings.isEmpty()) {
 			// get first booking
 			Booking booking = bookings.get(0);
 			// remove booking
 			bookingMgr.Remove(booking);
 		}
 
 		// borrow
 		Borrow borrow = new Borrow();
 		borrow.setExemplar((Exemplar) exemplarListTable.getRowData());
 		borrow.setUser(user);
 		borrow.setBorrowed(new Date(System.currentTimeMillis()));
 
 		try {
 			borrowMgr.Save(borrow);
 		} catch (javax.ejb.EJBException e) {
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Changes couldn't be saved. Please try again later."));
 			return "newBorrow";
 		}
 
 		// add borrow
 		return "newBorrow";
 	}
 
 	/**
 	 * Manage bookings action
 	 * @return 
 	 */
 	public String actionManageBookings() {
 		return "manageBookings";
 	}
 
 	/**
 	 * Manage borrows action
 	 * @return 
 	 */
 	public String actionManageBorrows() {
 		return "manageBorrows";
 	}
 
 	/**
 	 * Just empty action
 	 * @return 
 	 */
 	public String actionEmptyAction() {
 		return "";
 	}
 }
