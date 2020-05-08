 package com.thebuzzmedia.simple.generator;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
import com.thebuzzmedia.simple.generator.annotation.Encode;
import com.thebuzzmedia.simple.generator.annotation.Encode.Type;
 import com.thebuzzmedia.simple.generator.annotation.Recursable;
 
 @Recursable
 @XmlRootElement
 public class Library {
 	public String name;
 	public String address;
 	public List<Book> books;
 	public int[] ids = new int[] {1, 5, 7, 9, 13};
 
 	public Library() {
 		// no-arg default constructor.
 	}
 
 	public Library(String name, String address, Book... books) {
 		this.name = name;
 		this.address = address;
 		this.books = Arrays.asList(books);
 	}
 
 	@Recursable
 	public static class Book {
 		public Boolean checkedOut;
 		public String title;
 		public String isbn;
 		public Integer pageCount;
 		public Long printDate;
 		public List<Author> authors;
 		public Double replacementCost;
 
 		public Book() {
 			// no-arg default constructor.
 		}
 
 		public Book(Boolean checkedOut, String title, String isbn,
 				Integer pageCount, long printDateMillis,
 				Double replacementCost, Author... authors) {
 			this.checkedOut = checkedOut;
 			this.title = title;
 			this.isbn = isbn;
 			this.pageCount = pageCount;
 			this.printDate = printDateMillis;
 			this.replacementCost = replacementCost;
 			this.authors = Arrays.asList(authors);
 		}
 
 		@Recursable
 		public static class Author {
 			public String firstName;
 			public String lastName;
 			public Long dob;
 
			@Encode(Type.URL)
 			public String amazonURL;
 
 			public Author() {
 				// no-arg default constructor.
 			}
 
 			public Author(String fName, String lName, long dobMillis,
 					String amazonURL) {
 				this.firstName = fName;
 				this.lastName = lName;
 				this.dob = dobMillis;
 				this.amazonURL = amazonURL;
 			}
 		}
 	}
 }
