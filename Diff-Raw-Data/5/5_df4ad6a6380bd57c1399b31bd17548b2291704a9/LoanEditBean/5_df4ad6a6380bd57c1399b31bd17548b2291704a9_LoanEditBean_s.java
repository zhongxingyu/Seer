 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.web;
 
 import cz.muni.fi.pa165.library.services.LoanService;
 import cz.muni.fi.pa165.library.dtos.BookTO;
 import cz.muni.fi.pa165.library.dtos.CustomerTO;
 import cz.muni.fi.pa165.library.dtos.LoanTO;
 import cz.muni.fi.pa165.library.services.BookService;
 import cz.muni.fi.pa165.library.services.CustomerService;
 import java.util.List;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import net.sourceforge.stripes.validation.ValidationErrorHandler;
 import net.sourceforge.stripes.validation.ValidationErrors;
 import org.joda.time.LocalDate;
 
 /**
  *
  * @author MiškoHu
  */
 public class LoanEditBean extends BaseBean implements ValidationErrorHandler {
 
     @SpringBean
     private LoanService loanService;
     @SpringBean
     private CustomerService custService;
     @SpringBean
     private BookService bookService;
     @ValidateNestedProperties(value = {
         @Validate(on = {"returnBook"}, field = "conditionReturned", required = true)
     })
     private LoanTO loan;
     private CustomerTO customer;
     private List<BookTO> books;
     private List<LoanTO> loans;
     private BookTO book;
 
     @DefaultHandler
     public Resolution displayAvailable() {
         //TODO nepozicane knihy
         customer = custService.findCustomerById(Long.parseLong(getContext().getRequest().getParameter("customer.id")));
         books = bookService.findAllBooks();
         return new ForwardResolution("/loan/available.jsp");
     }
 
     public Resolution create() {
         BookTO book = bookService.findBookById(Long.parseLong(getContext().getRequest().getParameter("book.id")));
         customer = custService.findCustomerById(Long.parseLong(getContext().getRequest().getParameter("customer.id")));
         LoanTO loan = new LoanTO();
         loan.setBook(book);
         loan.setCustomer(customer);
         loan.setFromDate(new LocalDate());
         loanService.createLoan(loan);
         return new RedirectResolution("/index.jsp");
     }
 
     public Resolution findByBook() {
         book = bookService.findBookById(Long.parseLong(getContext().getRequest().getParameter("book.id")));
         loans = loanService.findLoansForBook(book);
         return new ForwardResolution("/loan/loans_for_book.jsp");
     }
 
     public Resolution returnBook() {
        loan = loanService.findLoanById(Long.parseLong(getContext().getRequest().getParameter("loan.id")));
        loanService.returnBook(loan);
         return new ForwardResolution("/index.jsp");
     }
 
     public Resolution findByCustomer() {
         customer = custService.findCustomerById(Long.parseLong(getContext().getRequest().getParameter("customer.id")));
         loans = loanService.findLoansForCustomer(customer);
         return new ForwardResolution("/loan/loans_for_customer.jsp");
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) {
         return new RedirectResolution("/index.jsp");
     }
 
     public CustomerTO getCustomer() {
         return customer;
     }
 
     public void setCustomer(CustomerTO customer) {
         this.customer = customer;
     }
 
     public List<BookTO> getBooks() {
         return books;
     }
 
     public void setBooks(List<BookTO> books) {
         this.books = books;
     }
 
     public List<LoanTO> getLoans() {
         return loans;
     }
 
     public void setLoans(List<LoanTO> loans) {
         this.loans = loans;
     }
 
     public BookTO getBook() {
         return book;
     }
 
     public void setBook(BookTO book) {
         this.book = book;
     }
 
     public LoanTO getLoan() {
         return loan;
     }
 
     public void setLoan(LoanTO loan) {
         this.loan = loan;
     }
 }
