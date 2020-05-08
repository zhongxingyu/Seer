 package org.springframework.data.demo.repository;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.demo.domain.Author;
 import org.springframework.data.demo.domain.Book;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class MongoBookShelf implements BookShelf {
 	
 	@Autowired
 	MongoTemplate mongoTemplate;
 
 	@Override
 	public void add(Book book) {
 		lookUpAuthor(book);
 		mongoTemplate.insert(book);
 	}
 
 	@Override
 	public void save(Book book) {
 		lookUpAuthor(book);
 		mongoTemplate.save(book);
 	}
 
 	@Override
 	public Book find(String isbn) {
 		Query query = new Query(Criteria.where("isbn").is(isbn));
 		return mongoTemplate.findOne(query, Book.class);
 	}
 
 	@Override
 	public void remove(String isbn) {
 		Query query = new Query(Criteria.where("isbn").is(isbn));
 		mongoTemplate.remove(query, Book.class);
 	}
 
 	@Override
 	public List<Book> findAll() {
 		List<Book> books = mongoTemplate.findAll(Book.class); 
 		return Collections.unmodifiableList(books);
 	}
 
 	private void lookUpAuthor(Book book) {
 		if (book.getAuthor() != null) {
 			Query query = new Query(Criteria.where("name").is(book.getAuthor().getName()));
 			Author existing = mongoTemplate.findOne(query ,Author.class);
 			if (existing != null) {
 				book.setAuthor(existing);
 			}
			else {
				mongoTemplate.insert(book.getAuthor());
			}
 		}
 	}
 }
