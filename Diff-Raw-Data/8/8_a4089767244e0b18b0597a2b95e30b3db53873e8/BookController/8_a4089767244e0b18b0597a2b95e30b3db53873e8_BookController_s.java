 package com.thoughtworks.controllers;
 
 import com.thoughtworks.models.Book;
 import com.thoughtworks.models.Books;
 import com.thoughtworks.models.IssueBook;
 import com.thoughtworks.services.BookService;
 import lombok.NoArgsConstructor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.validation.Valid;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 
 @NoArgsConstructor
 @Controller
 public class BookController {
   @Autowired
 
   BookService bookService;
 
 
   public BookController(BookService bookService) {
     this.bookService = bookService;
   }
 
   @RequestMapping(value = "/", method = RequestMethod.GET)
   public String index(Map<String, Object> model) {
 
     Books books = new Books();
     books.getBookList().addAll(bookService.getAll());
     model.put("books", books);
     return "book/index";
   }
 
   @RequestMapping(value = "/new", method = RequestMethod.GET)
   public String initCreationForm(Map<String, Object> model) {
     Book book = new Book();
     model.put("book", book);
     return "book/add";
   }
 
   @RequestMapping(value = "/new", method = RequestMethod.POST)
   public String processCreationForm(@Valid Book book, BindingResult result, SessionStatus status) {
     if (result.hasErrors()) {
       return "book/add";
     } else {
       this.bookService.save(book);
       status.setComplete();
       return "redirect:/";
     }
   }
   @RequestMapping(value = "/edit", method = RequestMethod.GET, headers = "Accept=application/json")
   public void getBook()
       {
         int searchId=88;
         Book book=  bookService.get(searchId);
         System.out.println("BookName= "+book.getName());
       }
 
     @RequestMapping(value = "/deleteBook", method = RequestMethod.GET, headers = "Accept=application/json")
     public void deleteBook()
     {
         int id = 105;
         Book book = bookService.deleteBook(id);
         System.out.println(book.getName());
     }
 
 
 
 
   @RequestMapping(value = "/issue_book", method = RequestMethod.GET)
     public String issue_request(Map<String, Object>model) {
       IssueBook issueBook = new IssueBook();
       model.put("book",issueBook);
       return "book/issue";
     }
 
   @RequestMapping(value = "/issue_book", method = RequestMethod.POST, headers = "Accept=application/json")
     public String issue(@RequestParam("bookId") int bookId) {
     DateFormat dateFormat = new SimpleDateFormat("mm/dd/yyyy");
     Date date = new Date();
     int employeeId = 0;
     Date returnedDate = null;
     bookService.issue(bookId, date, returnedDate, employeeId);
       return "redirect:/";
   }
 }
