 package edu.sjsu.cmpe.library.domain;
 import java.util.*;
 
 public class Library 
 {
 	private Book books[];
 	private int book_count;
 	private Map<Integer, List<Author>> authors_map;  
 	private Map<Integer, List<Review>> reviews_map;
 	private int size;
 	public Library(int size)
 	{
 		this.size = size;
 		books = new Book[this.size];
 		book_count = 0;
 		authors_map = new HashMap<Integer, List<Author>>();
 		reviews_map = new HashMap<Integer, List<Review>>();
 	}
 	public int getBookCount()
 	{
 		return this.book_count;
 	}
 	public Boolean addBook(Book book)
 	{	
 		if(book_count < size)
 		{
 			book.setIsbn(book_count+1);
 			books[book_count] = book;
 			book_count++;
 			return true;
 		}
 		else
 			return false;
 	}
 	public Boolean deleteBook(int isbn)
 	{
 		if(books[isbn-1] == null)
 			return false;
 		books[isbn-1] = null;
		authors_map.remove(isbn);
		reviews_map.remove(isbn);
 		return true;
 	}
 	public Boolean updateBook(int isbn, Book book)
 	{
 		if(books[isbn-1] == null)
 			return false;
 		books[isbn-1] = book;
 		return true;
 	}
 	public Book getBook(int isbn)
 	{
 		return books[isbn-1];
 	}
 	public Boolean addReview(int isbn, Review review)
 	{
 		if(books[isbn-1] == null)
 			return false;
 		List<Review> review_list = reviews_map.get(isbn);
 		if(review_list == null)
 		{
 			review_list = new ArrayList<Review>();
 		}
 		int id = review_list.size();
 		review.setId(id+1);
 		review_list.add(review);
 		reviews_map.put(isbn, review_list);
 		return true;
 	}
 	public int getNumReviews(int isbn)
 	{
 		if(reviews_map.get(isbn) != null)
 			return reviews_map.get(isbn).size();
 		else
 			return 0;
 	}
 	
 	public Boolean addAuthors(int isbn, List<Author> authors)
 	{
 		for(int i = 0; i < authors.size(); i++)
 		{
 			Author author = authors.get(i);
 			author.setID(i+1);
 			authors.set(i, author);
 		}
 		if(authors_map.put(isbn, authors) != null)
 			return false;
 		return true;
 	}
 	public int getNumAuthors(int isbn)
 	{
 		if(authors_map.get(isbn) != null)
 			return authors_map.get(isbn).size();
 		else
 			return 0;
 	}
 	public List<Review> getAllReviews(int isbn)
 	{
 		List<Review> review_list = reviews_map.get(isbn);
 		return review_list;
 	}
 	public List<Author> getAllAuthors(int isbn)
 	{
 		List<Author> author_list = authors_map.get(isbn);
 		return author_list;
 	}
 	public Review getReviewByID(int isbn, int id)
 	{
 		List<Review> review_list = reviews_map.get(isbn);
 		if(review_list == null)
 			return null;
 		Review review = review_list.get(id-1);
 		return review;
 	}
 	public Author getAuthorByID(int isbn, int id)
 	{
 		List<Author> author_list = authors_map.get(isbn);
 		if(author_list == null)
 			return null;
 		Author author = author_list.get(id-1);
 		return author;
 	}
 }
