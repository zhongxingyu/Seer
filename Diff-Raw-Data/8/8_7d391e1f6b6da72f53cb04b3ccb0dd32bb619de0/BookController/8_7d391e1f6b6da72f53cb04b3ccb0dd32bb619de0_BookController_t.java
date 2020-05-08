 package masterclass.spring.controllers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import masterclass.spring.domain.Book;
 import masterclass.spring.service.BookService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.validation.BindException;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 @Controller
 @Transactional
 @RequestMapping("/books")
 public class BookController {
 
 	@Autowired
 	private BookService bookService;
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = { "Accept=application/json" })
 	public @ResponseBody
 	Book getBook(@PathVariable("id") long id) {
 		System.out.println("GET a book");
 		Book book = new Book();
 		book.setTitle("Design Patterns");
 		return book;
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, headers = { "Content-Type=application/json" })
 	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void putBook(@PathVariable("id") long id, @Valid @RequestBody Book book, BindingResult result) throws BindException {
 		System.out.println("PUT a book " + book);
     if (result.hasErrors()) {
       throw new BindException(result);
     }
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
 	@ResponseStatus(HttpStatus.NO_CONTENT)
 	public void deleteBook(@PathVariable("id") long id) {
 		System.out.println("DELETE a book");
 	}
 
 	@RequestMapping(method = RequestMethod.POST, headers = { "Accept=application/json", "Content-Type=application/json" })
 	@ResponseStatus(HttpStatus.CREATED)
 	public @ResponseBody
	Book createBook(@Valid @RequestBody Book book, BindingResult result,
 			HttpServletResponse response) throws BindException {
 		System.out.println("POST a book" + book);
 		if (result.hasErrors()) {
 			throw new BindException(result);
 		}
 
 		Book createdBook = bookService.createBook(book);
 		
 		response.setHeader("Location", "/books/" + createdBook.getId());
 		return book;
 
 	}
 
 }
