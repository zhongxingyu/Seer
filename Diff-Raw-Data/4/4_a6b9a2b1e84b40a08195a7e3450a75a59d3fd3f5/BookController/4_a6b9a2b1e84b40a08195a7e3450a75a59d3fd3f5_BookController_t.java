 package test.controller;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.enterprise.context.SessionScoped;
 import javax.faces.model.ListDataModel;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import test.entity.Book;
 import test.service.BookService;
 
 // Sessionスコープにしないと、dataTableの中のcommandLinkが機能しない
 @SessionScoped
 @Named
 public class BookController implements Serializable{
 
 	private static final long serialVersionUID = 1L;
 
 	private static final String BOOKLIST_PAGE = "bookList";
 	private static final String ENTRY_PAGE ="entry";
 	
 	private String currentPage = BOOKLIST_PAGE;
 
 	@Inject
 	private BookService service;
 	
 	private List<Book> bookList = new ArrayList<>();
 
 	private ListDataModel<Book> bookListModel;
 	
 	private Book searchCriteria = new Book();
 	
 	private Book entryBook = new Book();
 	
 	public BookController() {
 	}
 	
 	private void updateAllBookList() {
 		setBookList(service.listAllBook());
 	}
 	
 	public String showBookList() {
 		updateAllBookList();
 		currentPage = BOOKLIST_PAGE;
 		return "/view/showBookList";
 	}
 	
 	public String showEntryPage() {
 		currentPage = ENTRY_PAGE;
 		return "/view/entryBook";
 	}
 	
 	public String entry() {
 		System.out.println("entry");
 		service.entry(entryBook);
		// 登録したBookはDETACHED状態のエンティティとなる
		// そのため、ここで初期化しておかないと続けて登録しようとしたときに
		// DETACHEDエンティティをpersistしようとしてエラーとなる
		entryBook = new Book();
 		return null;
 	}
 	
 	public void search() {
 		System.out.println("search");
 		setBookList(service.search(searchCriteria));
 	}
 	
 	public String edit() {
 		System.out.println("edit");
 		updateAllBookList();
 		return null;
 	}
 	
 	public String delete() {
 		// ListDataModelから選択されたBookを取得して削除
 		service.delete(bookListModel.getRowData());
 		updateAllBookList();
 		return null;
 	}
 	
 	public List<Book> getBookList() {
 		return bookList;
 	}
 
 	public void setBookList(List<Book> bookList) {
 		this.bookList = bookList;
 		if (bookListModel == null) {
 			bookListModel = new ListDataModel<>();
 		}
 		bookListModel.setWrappedData(bookList);
 	}
 
 	public Book getEntryBook() {
 		return entryBook;
 	}
 
 	public void setEntryBook(Book entryBook) {
 		this.entryBook = entryBook;
 	}
 
 	public ListDataModel<Book> getBookListModel() {
 		if (bookListModel == null) {
 			bookListModel = new ListDataModel<>(bookList);
 		}
 		return bookListModel;
 	}
 
 	public void setBookListModel(ListDataModel<Book> bookListModel) {
 		this.bookListModel = bookListModel;
 	}
 
 	public String getCurrentPage() {
 		return currentPage;
 	}
 
 	public void setCurrentPage(String currentPage) {
 		this.currentPage = currentPage;
 	}
 
 	public Book getSearchCriteria() {
 		return searchCriteria;
 	}
 
 	public void setSearchCriteria(Book searchCriteria) {
 		this.searchCriteria = searchCriteria;
 	}
 	
 	
 	
 }
