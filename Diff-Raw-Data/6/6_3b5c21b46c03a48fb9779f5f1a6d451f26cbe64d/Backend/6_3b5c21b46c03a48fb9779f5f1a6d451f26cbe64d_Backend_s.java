 /**
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
 
 import java.util.List;
 
 import dat255.grupp06.bibbla.backend.login.Session;
 import dat255.grupp06.bibbla.backend.tasks.DetailedViewJob;
 import dat255.grupp06.bibbla.backend.tasks.MyBooksJob;
 import dat255.grupp06.bibbla.backend.tasks.MyDebtJob;
 import dat255.grupp06.bibbla.backend.tasks.MyReservationsJob;
 import dat255.grupp06.bibbla.backend.tasks.RenewJob;
 import dat255.grupp06.bibbla.backend.tasks.ReserveJob;
 import dat255.grupp06.bibbla.backend.tasks.SearchJob;
 import dat255.grupp06.bibbla.backend.tasks.Task;
 import dat255.grupp06.bibbla.backend.tasks.UnreserveJob;
 import dat255.grupp06.bibbla.model.Book;
 import dat255.grupp06.bibbla.model.Credentials;
 import dat255.grupp06.bibbla.model.CredentialsMissingException;
 import dat255.grupp06.bibbla.utils.Callback;
 
 /**
  * Performs tasks like searching, reserving and logging in.
  * Does all the heavy lifting.
  * 
  * @author Niklas Logren
  */
 public final class Backend {
 	
 	public final static int MAX_CONNECTION_ATTEMPTS = 5;
 	public final static int CONNECTION_TIMEOUT = 2000;
 	private Settings settings;
 	private Session session;
 	
 	/**
 	 * Creates a new instance of our Backend.
 	 * Initialises a new session and fetches settings.
 	 */
 	public Backend() {
 		settings = new Settings();
		session = new Session(settings.getName(), settings.getCode(), settings.getPin());
 	}
 	
 	/**
 	 *  @returns the user's username.
 	 *  If no name is saved, returns empty string.
 	 */
 	public String getUserName() {
 		return session.getName();
 	}
 	
 	/**
 	 *  Starts fetching the user's current debt. Reports results using callback.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called when logging in is done. 
 	 */
 	public void fetchUserDebt(Callback frontendCallback)
 	throws CredentialsMissingException {
 		final MyDebtJob job = new MyDebtJob(settings.getCredentials(),
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
 	 *  Searches backend for the supplied string, and reports results using callback.
 	 *  
 	 *  @param s - The string to search for.
 	 *  @param page - which page of search results to fetch. If too high, returns empty list.
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
 	 */
 	public void search(final String s, final int page, final Callback frontendCallback) {
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
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
 	
 	/**
 	 *  Fetches the DetailedView of the supplied book.
 	 *  Sends a new book with all the additional information to the callback.
 	 */
 	public void fetchDetailedView(final Book book, final Callback frontendCallback) {
 		// Create a new Task and define its body.
 		Task task = new Task(frontendCallback) {
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
 	
 	/**
 	 *  Fetches a list of the user's current reservations.
 	 *  Returns it using callback.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
 	 */
 	public void fetchReservations(final Callback frontendCallback)
 	throws CredentialsMissingException {
 		final MyReservationsJob job = new MyReservationsJob(
 				settings.getCredentials(), session);
 		Task task = new Task(frontendCallback) {
 			@Override
 			protected Void doInBackground(String... arg0) {
 				message = job.run();
 				return null;
 			}
 		};
 		task.execute();
 	}
 
 	/**
 	 *  Fetches a list of the user's currently loaned books. Returns it using callback.
 	 *  
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
 	 */
 	public void fetchLoans(final Callback frontendCallback)
 	throws CredentialsMissingException {
 		final MyBooksJob job = new MyBooksJob(settings.getCredentials(), session);
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
 	 *  Reserves the supplied book. Result is reported via callback.
 	 *  
 	 *  @param book - the book to reserve. Needs to have its reserveUrl property set.
 	 *  @param libraryCode - the code of the library the book should be sent to.
 	 *   TODO: change to Library!
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
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
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
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
 	 *  @param books - A list of books to unreserve. Needs to have their unreserveIds property set.
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
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
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
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
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
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
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
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
 	 *  @param frontendCallback - the callback object which will be called when searching is done.
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
 	}
 	
 	public void saveCredentials(Credentials cred) {
 		if (cred != null) {
 			settings.setName(cred.name);
 			settings.setCode(cred.card);
 			settings.setPin(cred.pin);
 		}
 	}
 }
