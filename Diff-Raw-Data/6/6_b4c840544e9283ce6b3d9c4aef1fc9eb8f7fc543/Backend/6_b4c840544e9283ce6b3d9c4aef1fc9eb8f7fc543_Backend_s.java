 /**
 	Copyright 2012 Fahad Al-Khameesi, Madeleine Appert, Niklas Logren, Arild Matsson and Jonathan Orr.
 
     This file is part of Bibbla.
 
     Bibbla is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Bibbla is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Bibbla.  If not, see <http://www.gnu.org/licenses/>.    
  **/
 
 package dat255.grupp06.bibbla.backend;
 
 
 import java.util.AbstractMap;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import android.annotation.SuppressLint;
 import dat255.grupp06.bibbla.backend.tasks.*;
 import dat255.grupp06.bibbla.model.*;
 import dat255.grupp06.bibbla.utils.*;
 import dat255.grupp06.bibbla.model.Library;
 import dat255.grupp06.bibbla.utils.Callback;
 import dat255.grupp06.bibbla.utils.Message;
 import dat255.grupp06.bibbla.utils.Session;
 
 /**
  * Performs tasks like searching, reserving and logging in.
  * Does all the heavy lifting.
  * 
  * @author Niklas Logren
  */
 @SuppressLint({ "NewApi", "NewApi" })
 public final class Backend {
 	
 	public final static int MAX_CONNECTION_ATTEMPTS = 5;
 	public final static int CONNECTION_TIMEOUT = 2000;
 	private Settings settings;
 	private Session session;
 	private static Backend backendObject;
 	
 	// Cached results.
 	private Map<String,Book> detailedViews = new HashMap<String, Book>();
 	private Map<Entry<String, Integer>,List<Book>> searchResults
 	= new HashMap<Entry<String, Integer>,List<Book>>();
 	private List<Book> reservations;
 	private List<Book> loanedBooks;
 	private List<Library> libraries;
 	private Integer debt;
 	
 	/**
 	 * Creates a new instance of our Backend.
 	 * Initialises a new session and fetches settings.
 	 */
 	private Backend() {
 		settings = new Settings();
 		session = new Session();
 	}
 	
 	public static Backend getBackend() {
 		if(backendObject == null) {
 			backendObject = new Backend();
 		}
 		return backendObject;
 	}
 	
 	/**
 	 *  @returns the user's username.
 	 *  If no name is saved, returns empty string.
 	 */
 	public void getUserName(Callback frontendCallback)
 	throws CredentialsMissingException {
 		final UserNameJob job = new UserNameJob(settings.getCredentials(),
 				session);
 		Task task = new Task(frontendCallback) {
 			@Override
 			protected Void doInBackground(String... params) {
 				message = job.run();
 				return null;
 			}
 		};
 		task.execute();
 	}
 	
 	/**
 	 *  Starts fetching the user's current debt. Reports results using callback.
 	 *  Uses cached values if available; otherwise, simply runs the job.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when logging in is done.
 	 */
 	public void fetchUserDebt(final Callback frontendCallback)
 	throws CredentialsMissingException {
 		fetchUserDebt(frontendCallback, false);
 	}
 	
 	/**
 	 * Returns the logged in state of the backend.
 	 */
 	public boolean isLoggedIn() {
 		return session.isActive();
 	}
 
 	/**
 	 *  Starts fetching the user's current debt. Reports results using callback.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when logging in is done.
 	 *  @param refresh - should we run the job directly, and bypass caching? 
 	 */
 	public void fetchUserDebt(final Callback frontendCallback, boolean refresh)
 	throws CredentialsMissingException {
 		// Return cached result if it's available.
 		if (!refresh && (debt!=null)) {
 			Message message = new Message();
 			message.obj = debt;
 			frontendCallback.handleMessage(message);
 		}
 		// Run the job.
 		else {
 			Callback backendCallback = new Callback() {
 				@Override
 				public void handleMessage(Message message) {
 					Backend.this.fetchDebtDone(message, frontendCallback);
 				}
 			};
 			
 			final MyDebtJob job = new MyDebtJob(settings.getCredentials(),
 					session);
 			// Create a new Task and define its body.
 			Task task = new Task(backendCallback) {
 				@Override
 				// The code that's run in the Task (on new thread).
 				protected Void doInBackground(String... params) {
 					message = job.run();
 					return null;
 				}
 			};
 			// Start the task.
 			task.execute();
 		}
 	}
 		
 	private void fetchDebtDone(Message message, Callback callback) {
 		debt = (Integer) message.obj;
 		callback.handleMessage(message);
 	}
 
 	/**
 	 *  Searches backend for the supplied string, and reports results using callback.
 	 *  
 	 *  @param s - The string to search for.
 	 *  @param page - which page of search results to fetch.
 	 *   If too high, returns empty list.
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when searching is done.
 	 */
 	public void search(final String s, final int page, final Callback frontendCallback) {
 		// Create the key identifying this list of search results.
 		// Consists of the search string and page number.
 		final Map.Entry<String,Integer> key =
 				new AbstractMap.SimpleEntry<String, Integer>(s, page);
 		// Return cached result if it's available.
 		if(searchResults.containsKey(key)) {
 			Message message = new Message();
 			message.obj = searchResults.get(key);
 			frontendCallback.handleMessage(message);
 		}
 		// Run the job.
 		else{
 			Callback backendCallback = new Callback() {
 				@Override
 				public void handleMessage(Message message) {
 					Backend.this.fetchSearchResultsDone(message, frontendCallback, key);
 				}
 				
 			};
 			
 		// Create a new Task and define its body.
 		Task task = new Task(backendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				SearchJob job = new SearchJob(s,page);
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 		}
 	}
 	
 	private void fetchSearchResultsDone(Message message,
 			Callback callback, Entry<String, Integer> entry) {
 		searchResults.put(entry, (List<Book>) message.obj);
 		callback.handleMessage(message);
 		
 	}
 
 	/**
 	 *  Fetches the DetailedView of the supplied book.
 	 *  Sends a new book with all the additional information to the callback.
 	 *  Uses cached values if available; otherwise, simply runs the job.
 	 *  
 	 *  @param book - the book whose detailed view we're going to fetch.
 	 *   Needs to have its url set.
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when fetching detailed view is done.
 	 */
 	public void fetchDetailedView(final Book book, final Callback frontendCallback) {
 		fetchDetailedView(book, frontendCallback, false);
 	}
 	
 	/**
 	 *  Fetches the DetailedView of the supplied book.
 	 *  Sends a new book with all the additional information to the callback.
 	 *  
 	 *  @param book - the book whose detailed view we're going to fetch.
 	 *   Needs to have its url set.
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when fetching detailed view is done.
 	 *  @param refresh - should we run the job directly, and bypass caching?
 	 */
 	public void fetchDetailedView(final Book book, final Callback frontendCallback, boolean refresh) {
 		// Return cached result if it's available.
 		if (detailedViews.containsKey(book.getUrl()) && !refresh) {
 			Message message = new Message();
 			message.obj = detailedViews.get(book.getUrl());
 			frontendCallback.handleMessage(message);
 		}
 		// Run the job.
 		else {
 			detailedViews.clear();
 			// Create a new callback, which saves the result before passing it on to frontend.
 			Callback backendCallback = new Callback() {
 				@Override
 				public void handleMessage(Message message) {
 					Backend.this.fetchDetailedViewDone(message, frontendCallback);
 				}	
 			};
 			
 			// Create a new Task and define its body.
 			Task task = new Task(backendCallback) {
 				@Override
 				// The code that's run in the Task (on new thread).
 				protected Void doInBackground(String... params) {
 					DetailedViewJob job = new DetailedViewJob(book);
 					message = job.run();
 					return null;
 				}
 			};
 			// Start the task.
 			task.execute();
 		}
 	}
 	
 	/**
 	 * Is called when fetchDetailedView is done.
 	 * Saves the result in a member variable, and then passes it on to frontend.
 	 * 
 	 * @param message - the message returned from fetchDetailedView.
 	 * @param frontendCallback - the callback object which will be called
 	 *  when fetching detailed view is done.
 	 */
 	private void fetchDetailedViewDone(Message message, Callback frontendCallback) {
 		// Save results for caching.
 		detailedViews.put(((Book) message.obj).getUrl() ,(Book) message.obj); 
 		frontendCallback.handleMessage(message);
 	}
 
 	
 	/**
 	 *  Fetches a list of the user's current reservations.
 	 *  Returns it using callback.
 	 *  Uses cached values if available; otherwise, simply runs the job.
 	 *  
 	 *  @param frontendCallback - the callback object which will be
 	 *   called when fetching reservations is done.
 	 */
 	public void fetchReservations(final Callback frontendCallback)
 	throws CredentialsMissingException {
 		fetchReservations(frontendCallback, false);
 	}
 	
 	/**
 	 *  Fetches a list of the user's current reservations.
 	 *  Returns it using callback.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called
 	 *  when fetching reservations is done.
 	 *  @param refresh - should we run the job directly, and bypass caching?
 	 */
 	public void fetchReservations(final Callback frontendCallback, boolean refresh)
 	throws CredentialsMissingException {
 		// Return cached result if it's available.
 		if (!refresh && (reservations!=null)) {
 			Message message = new Message();
 			message.obj = reservations;
 			frontendCallback.handleMessage(message);
 		}
 		// Run the job.
 		else {
 			reservations = null;
 			// Create a new callback, which saves the result before passing it on to frontend.
 			Callback backendCallback = new Callback(){
 				@Override
 				public void handleMessage(Message message) {
 					Backend.this.fetchReservationsDone(message, frontendCallback);
 				}
 				
 			};
 		
 		final MyReservationsJob job = new MyReservationsJob(
 				settings.getCredentials(), session);
 		Task task = new Task(backendCallback) {
 			@Override
 			protected Void doInBackground(String... arg0) {
 				message = job.run();
 				return null;
 			}
 		};
 		task.execute();
 		}
 	}
 	
 	/**
 	 * Is called when fetchReservations is done.
 	 * Saves the result in a member variable, and then passes it on to frontend.
 	 * 
 	 * @param message - the message returned from fetchReservations.
 	 * @param frontendCallback - the callback object which will be called
 	 *  when fetching reservations is done.
 	 */	
 	private void fetchReservationsDone(Message message, Callback callback){
 		reservations = (List<Book>) message.obj;
 		callback.handleMessage(message);
 		
 	}
 
 	/**
 	 *  Fetches a list of the user's currently loaned books. Returns it using callback.
 	 *  Uses cached values if available; otherwise, simply runs the job.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when fetching loans is done.
 	 */
 	public void fetchLoans(final Callback frontendCallback)
 	throws CredentialsMissingException {
 		fetchLoans(frontendCallback, false);
 	}
 	/**
 	 *  Fetches a list of the user's currently loaned books. Returns it using callback.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when fetching loans is done.
 	 *  @param refresh - should we run the job directly, and bypass caching?
 	 */
 	public void fetchLoans(final Callback frontendCallback, boolean refresh)
 	throws CredentialsMissingException {
 		// Return cached result if it's available.
 		if (!refresh && (loanedBooks!=null)) {
 			Message message = new Message();
 			message.obj = loanedBooks;
 			frontendCallback.handleMessage(message);
 		}
 		// Run the job.
 		else {
 			loanedBooks = null;
 			// Create a new callback, which saves the result before passing it on to frontend. 
 			Callback backendCallback = new Callback() {
 				@Override
 				public void handleMessage(Message message) {
 					Backend.this.fetchLoanedBooksDone(message, frontendCallback);
 				}
 			};
 			
 			final MyBooksJob job = new MyBooksJob(settings.getCredentials(), session);
 			// Create a new Task and define its body.
 			Task task = new Task(backendCallback) {
 				@Override
 				// The code that's run in the Task (on new thread).
 				protected Void doInBackground(String... params) {
 					message = job.run();
 					return null;
 				}
 			};
 			// Start the task.
 			task.execute();
 		}
 	}
 	
 	/**
 	 * Is called when fetchDetailedView is done.
 	 * Saves the result in a member variable, and then passes it on to frontend.
 	 * 
 	 * @param message - the message returned from fetchDetailedView.
 	 * @param frontendCallback - the callback object which will be called
 	 *  when fetching loaned books is done.
 	 */
 	public void fetchLoanedBooksDone(Message message, Callback callback){
 		loanedBooks = (List<Book>) message.obj;
 		callback.handleMessage(message);
 	}
 
 	/**
 	 *  Reserves the supplied book. Result is reported via callback.
 	 *  
 	 *  @param book - the book to reserve. Needs to have its reserveUrl property set.
 	 *  @param libraryCode - the code of the library the book should be sent to.
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when reserving is done.
 	 */
 	public void reserve(final Book book, final String libraryCode,
 			final Callback frontendCallback)
 	throws CredentialsMissingException {
 		final ReserveJob job = new ReserveJob(book, libraryCode,
 				settings.getCredentials(), session);
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 
 	/**
 	 *  Unreserves the supplied book. Result is reported via callback.
 	 *  
 	 *  @param book - The book to unreserve. Needs to have its unreserveId property set.
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when unreserving is done.
 	 */
 	public void unreserve(final Book book, final Callback frontendCallback)
 	throws CredentialsMissingException {
 		final UnreserveJob job = new UnreserveJob(book,
 				settings.getCredentials(), session);
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 	
 	/**
 	 *  Unreserves all supplied books. Result is reported via callback.
 	 *  
 	 *  @param books - A list of books to unreserve.
 	 *   Needs to have their unreserveIds property set.
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when unreserving is done.
 	 */
 	public void unreserve(final List<Book> books, final Callback frontendCallback)
 	throws CredentialsMissingException {
 		final UnreserveJob job = new UnreserveJob(books,
 				settings.getCredentials(), session);
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 	
 	/**
 	 *  Unreserves all reserved books. Result is reported via callback.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when unreserving is done.
 	 */
 	public void unreserve(final Callback frontendCallback)
 	throws CredentialsMissingException {
 		final UnreserveJob job = new UnreserveJob(settings.getCredentials(),
 				session);
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 	
 	/**
 	 *  Renews the supplied book. Result is reported via callback.
 	 *  
 	 *  @param book - The book to renew. Needs to have its renewId property set.
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when renewal is done.
 	 */
 	public void renew(final Book book, final Callback frontendCallback)
 	throws CredentialsMissingException {
 		final RenewJob job = new RenewJob(book, settings.getCredentials(),
 				session);
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 	
 	/**
 	 *  Renews all supplied books. Result is reported via callback.
 	 *  
 	 *  @param books - A list of books to renew. Needs to have their renewId properties set.
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when renewal is done.
 	 */
 	public void renew(final List<Book> books, final Callback frontendCallback)
 	throws CredentialsMissingException {
 		final RenewJob job = new RenewJob(books, settings.getCredentials(),
 				session);
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 	
 	/**
 	 *  Renews all loaned books. Result is reported via callback.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when renewal is done.
 	 */
 	public void renew(final Callback frontendCallback)
 	throws CredentialsMissingException {
 		final RenewJob job = new RenewJob(settings.getCredentials(), session);
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 	}
 	
 	/**
 	 * Logs the user out.
 	 */
 	public void logOut(){
 		session = new Session();
 		settings = new Settings();
 	}
 	
 	public void saveCredentials(Credentials cred) {
 		if (cred != null) {
 			settings.setName(cred.name);
 			settings.setCode(cred.card);
 			settings.setPin(cred.pin);
 		}
 	}
 
 	/**
 	 *  Searches backend for the library information.
 	 *  Uses cached values if available; otherwise, simply runs the job.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when fetching library info is done.
 	 */
 	public void libInfo(final Callback frontendCallback) {
 		libInfo(frontendCallback, false);
 	}
 	
 	/**
 	 *  Searches backend for the library information.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called
 	 *   when fetching library information is done.
 	 *   @param refresh - should we run the job directly, and bypass caching?
 	 */
 	public void libInfo(final Callback frontendCallback, boolean refresh) {
 		if(libraries != null && !refresh){
 			Message message = new Message();
 			message.obj = libraries;
 			frontendCallback.handleMessage(message);
 		}
 		else{
 			Callback backendCallback = new Callback(){
 
 				@Override
 				public void handleMessage(Message message) {
 					Backend.this.fetchLibInfoDone(message ,frontendCallback);	
 				}
 			};
 		// Create a new Task and define its body.
 		Task task = new Task(backendCallback) {
 			@Override
 			// The code that's run in the Task (on new thread).
 			protected Void doInBackground(String... params) {
 				LibInfoJob job = new LibInfoJob();
 				message = job.run();
 				return null;
 			}
 		};
 		// Start the task.
 		task.execute();
 		}
 	}
 
 	/**
 	 * Is called when libInfo is done.
 	 * Saves the result in a member variable, and then passes it on to frontend.
 	 * 
 	 * @param message - the message returned from libInfo.
 	 * @param frontendCallback - the callback object which will be called
 	 *  when fetching library information is done.
 	 */
 	private void fetchLibInfoDone(Message message, Callback callback) {
 		libraries = (List<Library>) message.obj;
 		callback.handleMessage(message);
 	}
 }
 
