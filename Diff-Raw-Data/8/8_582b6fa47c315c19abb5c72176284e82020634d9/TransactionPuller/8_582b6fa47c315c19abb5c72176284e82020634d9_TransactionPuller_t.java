 package emcshop.scraper;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.logging.Logger;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.util.EntityUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 /**
  * Downloads and parses transactions from EMC.
  * @author Michael Angstadt
  */
 public class TransactionPuller {
 	private static final Logger logger = Logger.getLogger(TransactionPuller.class.getName());
 	private static final List<RupeeTransaction> noMoreElements = Arrays.asList();
 
 	private final EmcSession session;
 	private final BlockingQueue<List<RupeeTransaction>> queue = new LinkedBlockingQueue<List<RupeeTransaction>>();
 	private final Config config;
 
 	private final Date oldestPaymentTransactionDate;
 	private final Date latestTransactionDate;
 	private final Integer rupeeBalance;
 	private final AtomicInteger pageCounter;
 
 	private int pageCount = 0, transactionCount = 0;
 	private DownloadException thrown = null;
 
 	private int deadThreads = 0;
 	private boolean cancel = false;
 
 	/**
 	 * Constructs a new transaction puller.
 	 * @param session the EMC session
 	 * @throws BadSessionException if the EMC session was invalid
 	 * @throws IOException if there was a network problem contacting EMC
 	 */
 	public TransactionPuller(EmcSession session) throws BadSessionException, IOException {
 		this(session, new Config.Builder().build());
 	}
 
 	/**
 	 * Constructs a new transaction puller.
 	 * @param session the EMC session
 	 * @param config configuration parameters for this transaction puller
 	 * @throws BadSessionException if the EMC session was invalid
 	 * @throws IOException if there was a network problem contacting EMC
 	 */
 	public TransactionPuller(EmcSession session, Config config) throws BadSessionException, IOException {
 		this.session = session;
 		this.config = config;
 
 		TransactionPage firstPage = getPage(1, session.createHttpClient());
 
 		//is the user logged in?
 		if (!firstPage.isLoggedIn()) {
 			throw new BadSessionException();
 		}
 
 		//calculate how old a payment transaction can be before it is ignored
 		if (config.getMaxPaymentTransactionAge() != null) {
 			Calendar c = Calendar.getInstance();
 			c.add(Calendar.DATE, -config.getMaxPaymentTransactionAge());
 			oldestPaymentTransactionDate = c.getTime();
 		} else {
 			oldestPaymentTransactionDate = null;
 		}
 
 		//get the rupee balance
 		rupeeBalance = firstPage.getRupeeBalance();
 
 		//get the date of the latest transaction so we know when we've reached the last transaction page
 		latestTransactionDate = firstPage.getFirstTransactionDate();
 
 		//start threads
 		pageCounter = new AtomicInteger(config.getStartAtPage());
 		for (int i = 0; i < config.getThreadCount(); i++) {
 			ScrapeThread thread = new ScrapeThread();
 			thread.setDaemon(true);
 			thread.setName(getClass().getSimpleName() + "-" + i);
 			thread.start();
 		}
 	}
 
 	/**
 	 * Gets the next page of transactions. Note that the pages are not
 	 * guaranteed to be returned in any particular order.
 	 * @return the next page or null if there are no more pages
 	 * @throws DownloadException if an error occurred while downloading or
 	 * parsing one of the pages
 	 */
 	public List<RupeeTransaction> nextPage() throws DownloadException {
 		synchronized (this) {
 			if (queue.isEmpty()) {
 				if (thrown != null) {
 					throw thrown;
 				}
 				if (deadThreads == config.getThreadCount()) {
 					return null;
 				}
 			}
 		}
 
 		try {
 			List<RupeeTransaction> transactions = queue.take();
 			if (transactions == noMoreElements) {
 				synchronized (this) {
 					if (thrown != null) {
 						throw thrown;
 					}
 				}
 				return null;
 			}
 			return transactions;
 		} catch (InterruptedException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Stops the background threads that are downloading the transactions.
 	 */
 	public synchronized void cancel() {
 		cancel = true;
 		queue.add(noMoreElements); //null values cannot be added to the queue
 	}
 
 	/**
 	 * Determines if the background threads that are downloading the
 	 * transactions have been canceled.
 	 * @return true if they were canceled, false if not
 	 */
 	public boolean isCanceled() {
 		return cancel;
 	}
 
 	/**
 	 * Gets the rupee balance that was parsed from the first page.
 	 * @return the rupee balance or null if it could not be found
 	 */
 	public Integer getRupeeBalance() {
 		return rupeeBalance;
 	}
 
 	/**
 	 * Gets the number of pages that have been downloaded and parsed.
 	 * @return the page count
 	 */
 	public int getPageCount() {
 		return pageCount;
 	}
 
 	/**
 	 * Gets the number of transactions that have been downloaded and parsed.
 	 * @return the transaction count
 	 */
 	public int getTransactionCount() {
 		return transactionCount;
 	}
 
 	/**
 	 * Gets the configuration object for this transaction puller.
 	 * @return the configuration object
 	 */
 	public Config getConfig() {
 		return config;
 	}
 
 	/**
 	 * Downloads and parses a rupee transaction page.
 	 * @param page the page number
 	 * @param client the HTTP client
 	 * @return the page
 	 * @throws IOException if there's a problem downloading the page
 	 */
 	private TransactionPage getPage(int page, HttpClient client) throws IOException {
 		/*
 		 * Note: The HttpClient library is used here because using
 		 * "Jsoup.connect()" doesn't always work when the application is run as
 		 * a Web Start app.
 		 * 
 		 * The login dialog was repeatedly appearing because, even though the
 		 * login was successful (a valid session cookie was generated), the
 		 * TransactionPuller would fail when it tried to get the first
 		 * transaction from the first page (i.e. when calling "isLoggedIn()").
 		 * It was failing because it was getting back the unauthenticated
 		 * version of the rupee page. It was as if jsoup wasn't sending the
 		 * session cookie with the request.
 		 * 
 		 * The issue appeared to only occur when running under Web Start. It
 		 * could not be reproduced when running via Eclipse.
 		 */
 
 		String base = "http://empireminecraft.com/rupees/transactions/";
 		String url = base + "?page=" + page;
 
 		HttpGet request = new HttpGet(url);
 		HttpResponse response = client.execute(request);
 		HttpEntity entity = response.getEntity();
 		Document document = Jsoup.parse(entity.getContent(), "UTF-8", base);
 		EntityUtils.consume(entity);
 
 		return new TransactionPage(document);
 	}
 
 	/**
 	 * Filters out the instances of a specific sub-class of
 	 * {@link RupeeTransaction} from a list of transactions.
 	 * @param <T> the class to filter out
 	 * @param transactions the list of transactions
 	 * @param filterBy the class to filter out
 	 * @return the filtered instances
 	 */
 	public static <T extends RupeeTransaction> List<T> filter(List<RupeeTransaction> transactions, Class<T> filterBy) {
 		List<T> filtered = new ArrayList<T>();
 		for (RupeeTransaction transaction : transactions) {
 			if (transaction.getClass() == filterBy) {
 				T t = filterBy.cast(transaction);
 				filtered.add(t);
 			}
 		}
 		return filtered;
 	}
 
 	private class ScrapeThread extends Thread {
 		private HttpClient client;
 
 		public ScrapeThread() {
 			this.client = session.createHttpClient();
 		}
 
 		@Override
 		public void run() {
 			int page = 0;
 			try {
 				while (true) {
 					page = pageCounter.getAndIncrement();
 
 					if (config.getStopAtPage() != null && page > config.getStopAtPage()) {
 						break;
 					}
 
 					TransactionPage transactionPage = null;
 					try {
 						transactionPage = getPage(page, client);
 					} catch (ConnectException e) {
 						//one user reported getting connection errors at various points while trying to download 12k pages: http://empireminecraft.com/threads/shop-statistics.22507/page-14#post-684085
 						//if there's a connection problem, try re-creating the connection
 						client = session.createHttpClient();
 						transactionPage = getPage(page, client);
 					}
 
 					//the session shouldn't expire while a download is in progress, but run a check just in case something wonky happens
 					if (!transactionPage.isLoggedIn()) {
 						throw new BadSessionException();
 					}
 
 					//EMC will load the first page if an invalid page number is given (i.e. if we've reached the last page)
 					boolean lastPageReached = page > 1 && transactionPage.getFirstTransactionDate().getTime() >= latestTransactionDate.getTime();
 					if (lastPageReached) {
 						logger.info("Page " + page + " doesn't exist (page " + (page - 1) + " is the last page).");
 						break;
 					}
 
 					List<RupeeTransaction> transactions = transactionPage.getTransactions();
 
 					//remove old payment transactions
 					if (oldestPaymentTransactionDate != null) {
 						for (int i = 0; i < transactions.size(); i++) {
 							RupeeTransaction transaction = transactions.get(i);
 							if (!(transaction instanceof PaymentTransaction)) {
 								continue;
 							}
 
 							long ts = transaction.getTs().getTime();
 							if (ts < oldestPaymentTransactionDate.getTime()) {
 								transactions.remove(i);
 								i--;
 							}
 						}
 					}
 
 					//remove any transactions that are past the "stop-at" date
 					boolean done = false;
 					if (config.getStopAtDate() != null) {
 						int end = -1;
 						for (int i = 0; i < transactions.size(); i++) {
 							RupeeTransaction transaction = transactions.get(i);
 							long ts = transaction.getTs().getTime();
 
 							if (ts <= config.getStopAtDate().getTime()) {
 								end = i;
 								break;
 							}
 						}
 						if (end >= 0) {
 							transactions.subList(end, transactions.size()).clear();
 							done = true;
 						}
 					}
 
 					synchronized (TransactionPuller.this) {
 						if (cancel) {
 							return;
 						}
 						pageCount++;
 						transactionCount += transactions.size();
 
 						queue.add(transactions);
 					}
 
 					if (done) {
 						break;
 					}
 				}
 			} catch (Throwable t) {
 				synchronized (TransactionPuller.this) {
 					if (cancel) {
 						return;
 					}
 					thrown = new DownloadException(page, t);
 					cancel();
 				}
 			} finally {
 				synchronized (TransactionPuller.this) {
 					deadThreads++;
 					if (!cancel && deadThreads == config.getThreadCount()) {
 						//this is the last thread to terminate
 						queue.add(noMoreElements);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Contains initialization settings for the {@link TransactionPuller} class.
 	 * Use the {@link Config.Builder} class to construct new instances.
 	 */
 	public static class Config {
 		private final Date stopAtDate;
 		private final Integer stopAtPage;
 		private final int startAtPage;
 		private final Integer maxPaymentTransactionAge;
 		private final int threadCount;
 
 		private Config(Builder builder) {
 			stopAtDate = builder.stopAtDate;
 			stopAtPage = builder.stopAtPage;
 			startAtPage = builder.startAtPage;
 			maxPaymentTransactionAge = builder.maxPaymentTransactionAge;
 			threadCount = builder.threadCount;
 		}
 
 		/**
 		 * Gets the date at which the puller should stop parsing transactions.
 		 * @return the stop date or null for no end date
 		 */
 		public Date getStopAtDate() {
 			return stopAtDate;
 		}
 
 		/**
 		 * Gets the page at which the puller should stop parsing transactions.
 		 * @return the stop page (inclusive) or null to parse all pages
 		 */
 		public Integer getStopAtPage() {
 			return stopAtPage;
 		}
 
 		/**
 		 * Gets the page at which the puller should start parsing transactions.
 		 * @return the start page
 		 */
 		public int getStartAtPage() {
 			return startAtPage;
 		}
 
 		/**
 		 * Gets the number of number of days old a payment transaction can be
 		 * before it is ignored.
 		 * @return the number of days or null if disabled
 		 */
 		public Integer getMaxPaymentTransactionAge() {
 			return maxPaymentTransactionAge;
 		}
 
 		/**
 		 * Gets the number of background threads to spawn for downloading and
 		 * parsing the transactions.
 		 * @return the thread count
 		 */
 		public int getThreadCount() {
 			return threadCount;
 		}
 
 		/**
 		 * Creates new instances of the {@link Config} class.
 		 */
 		public static class Builder {
 			private Date stopAtDate;
 			private Integer stopAtPage;
 			private int startAtPage = 1;
 			private Integer maxPaymentTransactionAge;
 			private int threadCount = 4;
 
 			/**
 			 * Sets the date at which the puller should stop parsing
 			 * transactions (defaults to no end date).
 			 * @param stopAtDate the stop date or null for no end date
 			 * @return this
 			 */
 			public Builder stopAtDate(Date stopAtDate) {
				this.stopAtDate = (stopAtDate == null) ? null : new Date(stopAtDate.getTime());
 				return this;
 			}
 
 			/**
 			 * Sets the page at which the puller should stop parsing
 			 * transactions (defaults to all pages).
 			 * @param stopAtPage the stop page (inclusive) or null to parse all
 			 * pages
 			 * @return this
 			 */
 			public Builder stopAtPage(Integer stopAtPage) {
				if (stopAtPage != null && stopAtPage <= 0) {
 					throw new IllegalArgumentException("Stop page must be greater than zero.");
 				}
 				this.stopAtPage = stopAtPage;
 				return this;
 			}
 
 			/**
 			 * Sets the page at which the puller should start parsing
 			 * transactions (defaults to 1).
 			 * @param startAtPage the start page
 			 * @return this
 			 */
 			public Builder startAtPage(int startAtPage) {
 				if (startAtPage <= 0) {
 					throw new IllegalArgumentException("Start page must be greater than zero.");
 				}
 				this.startAtPage = startAtPage;
 				return this;
 			}
 
 			/**
 			 * Sets the number of number of days old a payment transaction can
 			 * be before it is ignored (disabled by default).
 			 * @param maxPaymentTransactionAge the number of days or null to
 			 * disable
 			 * @return this
 			 */
 			public Builder maxPaymentTransactionAge(Integer maxPaymentTransactionAge) {
				if (maxPaymentTransactionAge != null && maxPaymentTransactionAge <= 0) {
 					throw new IllegalArgumentException("Max payment transaction age must be greater than zero.");
 				}
 				this.maxPaymentTransactionAge = maxPaymentTransactionAge;
 				return this;
 			}
 
 			/**
 			 * Sets the number of background threads to spawn for downloading
 			 * and parsing the transactions (defaults to 4).
 			 * @param threadCount the thread count
 			 * @return this
 			 */
 			public Builder threadCount(int threadCount) {
 				if (threadCount <= 0) {
 					throw new IllegalArgumentException("Thread count must be greater than zero.");
 				}
 				this.threadCount = threadCount;
 				return this;
 			}
 
 			/**
 			 * Builds the final {@link Config} object.
 			 * @return the config object
 			 */
 			public Config build() {
 				if (stopAtPage != null && startAtPage > stopAtPage) {
 					throw new IllegalArgumentException("Start page must come before stop page.");
 				}
 				return new Config(this);
 			}
 		}
 	}
 }
