 package emcshop.gui;
 
 import static emcshop.util.GuiUtils.toolTipText;
 
 import java.awt.Font;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JRootPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.apache.commons.lang3.time.DurationFormatUtils;
 
 import emcshop.ReportSender;
 import emcshop.db.DbDao;
 import emcshop.gui.images.ImageManager;
 import emcshop.scraper.BadSessionException;
 import emcshop.scraper.BonusFeeTransaction;
 import emcshop.scraper.EmcSession;
 import emcshop.scraper.PaymentTransaction;
 import emcshop.scraper.RupeeTransaction;
 import emcshop.scraper.ShopTransaction;
 import emcshop.scraper.TransactionPuller;
 import emcshop.util.GuiUtils;
 import emcshop.util.Settings;
 import emcshop.util.TimeUtils;
 
 @SuppressWarnings("serial")
 public class UpdateDialog extends JDialog {
 	private static final Logger logger = Logger.getLogger(UpdateDialog.class.getName());
 
 	private final MainFrame owner;
 	private final DbDao dao;
 
 	private JButton cancel, stop;
 	private volatile boolean cancelClicked = false;
 	private JLabel shopTransactionsLabel, paymentTransactionsLabel, bonusFeeTransactionsLabel;
 	private JLabel pages;
 	private JLabel timerLabel;
 	private JCheckBox display;
 
 	private TransactionPuller puller;
 	private Thread pullerThread;
 
 	private long started;
 	private TransactionPullerListener listener;
 
 	private UpdateDialog(final MainFrame owner, final DbDao dao, final Settings settings, final TransactionPuller puller, final Long estimatedTime) {
 		super(owner, "Updating Transactions", true);
 		this.owner = owner;
 		this.dao = dao;
 		this.puller = puller;
 		listener = new TransactionPullerListener(puller.getStopAtPage());
 
 		setUndecorated(true);
 		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
 		setResizable(false);
 
		boolean firstUpdate = (puller.getStopAtDate() == null);
 		createWidgets(firstUpdate);
 		layoutWidgets(firstUpdate);
 
 		pack();
 		setSize(getWidth() + 20, getHeight());
 		setLocationRelativeTo(owner);
 
 		pullerThread = new Thread() {
 			@Override
 			public void run() {
 				TransactionPuller.Result result = null;
 				boolean repeat;
 				do {
 					repeat = false;
 
 					Thread timerThread = new TimerThread(estimatedTime);
 					timerThread.setDaemon(true);
 					timerThread.start();
 
 					started = System.currentTimeMillis();
 
 					try {
 						result = puller.start(settings.getSession(), listener);
 					} catch (BadSessionException e) {
 						String username = null;
 						EmcSession oldSession = settings.getSession();
 						if (oldSession != null) {
 							username = oldSession.getUsername();
 						}
 						LoginDialog.Result loginResult = LoginDialog.show(UpdateDialog.this, settings.isPersistSession(), username);
 						EmcSession session = loginResult.getSession();
 						if (session != null) {
 							settings.setSession(session);
 							settings.setPersistSession(loginResult.isRememberMe());
 							settings.save();
 
 							if (settings.isPersistSession()) {
 								owner.setClearSessionMenuItemEnabled(true);
 							}
 
 							//restart the puller with the new session token
 							repeat = true;
 						}
 					} catch (Throwable t) {
 						//an error occurred
 						dispose();
 
						if (listener.getParsedTransactionsCount() == 0) {
 							dao.rollback();
 							throw new RuntimeException(t);
 						}
 
 						long timeTaken = System.currentTimeMillis() - started;
 						boolean saveTransactions = UpdateErrorDialog.show(owner, listener.pageCount, listener.getParsedTransactionsCount(), listener.earliestParsedTransactionDate, t);
 						if (saveTransactions) {
 							try {
 								if (listener.earliestParsedTransactionDate != null) {
 									dao.updateBonusesFeesSince(listener.earliestParsedTransactionDate);
 								}
 								dao.commit();
 
 								owner.updateSuccessful(new Date(started), puller.getRupeeBalance(), timeTaken, listener.shopTransactionCount, listener.paymentTransactionCount, listener.bonusFeeTransactionCount, listener.pageCount, display.isSelected());
 							} catch (SQLException e) {
 								dao.rollback();
 								throw new RuntimeException(e);
 							}
 						} else {
 							dao.rollback();
 						}
 						return;
 					} finally {
 						timerThread.interrupt();
 					}
 				} while (repeat);
 
 				//puller was canceled
 				if (result == null) {
 					if (!cancelClicked) {
 						dao.rollback();
 					}
 
 					dispose();
 					return;
 				}
 
 				try {
 					if (listener.earliestParsedTransactionDate != null) {
 						dao.updateBonusesFeesSince(listener.earliestParsedTransactionDate);
 					}
 					dao.commit();
 					dispose();
 
 					owner.updateSuccessful(new Date(started), result.getRupeeBalance(), result.getTimeTaken(), listener.shopTransactionCount, listener.paymentTransactionCount, listener.bonusFeeTransactionCount, result.getPageCount(), display.isSelected());
 				} catch (SQLException e) {
 					dao.rollback();
 					throw new RuntimeException(e);
 				}
 			}
 		};
 		pullerThread.setDaemon(true);
 		pullerThread.start();
 	}
 
 	private void createWidgets(boolean firstUpdate) {
 		cancel = new JButton("Cancel");
 		if (firstUpdate) {
 			cancel.setToolTipText(toolTipText("Stops the update process and <b>discards</b> all transactions that were parsed."));
 		}
 		cancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				synchronized (puller) {
 					//synchronized so the code in the listener is not executed at the same time as this code
 					cancelClicked = true;
 					puller.cancel();
 					dao.rollback();
 				}
 				dispose();
 			}
 		});
 
 		if (firstUpdate) {
 			stop = new JButton("Stop");
 			stop.setToolTipText(toolTipText("Stops the update process and <b>saves</b> all transactions that were parsed."));
 			stop.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					synchronized (puller) {
 						//synchronized so the code in the listener is not executed at the same time as this code
 						cancelClicked = true;
 						puller.cancel();
 
 						if (listener.getParsedTransactionsCount() == 0) {
 							dispose();
 							return;
 						}
 
 						try {
 							if (listener.earliestParsedTransactionDate != null) {
 								dao.updateBonusesFeesSince(listener.earliestParsedTransactionDate);
 							}
 							dao.commit();
 						} catch (SQLException e) {
 							dao.rollback();
 							dispose();
 							throw new RuntimeException(e);
 						}
 					}
 
 					dispose();
 					long timeTaken = System.currentTimeMillis() - started;
 					owner.updateSuccessful(new Date(started), puller.getRupeeBalance(), timeTaken, listener.shopTransactionCount, listener.paymentTransactionCount, listener.bonusFeeTransactionCount, listener.pageCount, display.isSelected());
 				}
 			});
 		}
 
 		pages = new JLabel("0");
 		shopTransactionsLabel = new JLabel("0");
 		paymentTransactionsLabel = new JLabel("0");
 		bonusFeeTransactionsLabel = new JLabel("0");
 		timerLabel = new JLabel("...");
 		display = new JCheckBox("Display transactions when finished");
 		display.setSelected(true);
 	}
 
 	private void layoutWidgets(boolean firstUpdate) {
 		setLayout(new MigLayout());
 
 		add(new JLabel(ImageManager.getLoading()), "span 2, split 2, align center");
 		add(new JLabel("<html><b>Updating...</b></html>"), "wrap");
 
 		add(new JLabel("Pages:"));
 		add(pages, "wrap");
 
 		add(new JLabel("Transactions:"), "span 2, wrap");
 		add(new JLabel("Shop:"), "gapleft 30");
 		add(shopTransactionsLabel, "wrap");
 		add(new JLabel("Payment:"), "gapleft 30");
 		add(paymentTransactionsLabel, "wrap");
 		add(new JLabel("Bonus/Fee:"), "gapleft 30");
 		add(bonusFeeTransactionsLabel, "wrap");
 
 		add(new JLabel("Time:"));
 		add(timerLabel, "wrap");
 
 		add(display, "span 2, align center, wrap");
 
 		if (firstUpdate) {
 			add(cancel, "span 2, split 2, align center");
 			add(stop);
 		} else {
 			add(cancel, "span 2, align center");
 		}
 	}
 
 	private class TransactionPullerListener extends TransactionPuller.Listener {
 		private final NumberFormat nf = NumberFormat.getInstance();
 		private final NumberFormat timeNf = new DecimalFormat("00");
 		private final Integer stopAtPage;
 		private int shopTransactionCount = 0, paymentTransactionCount = 0, bonusFeeTransactionCount = 0;
 		private int pageCount = 0;
 		private long previousTime = 0;
 		private Date earliestParsedTransactionDate;
 
 		public TransactionPullerListener(Integer stopAtPage) {
 			this.stopAtPage = stopAtPage;
 		}
 
 		@Override
 		public void onPageScraped(int page, List<RupeeTransaction> transactions) throws Throwable {
 			synchronized (puller) { //the method itself cannot have a "synchronized" flag because there is a synchronized block in the cancel button's click handler
 				if (puller.isCanceled()) {
 					return;
 				}
 
 				//keep track of the oldest transaction date
 				if (!transactions.isEmpty()) {
 					RupeeTransaction last = transactions.get(transactions.size() - 1); //transactions are ordered date descending
 					Date lastTs = last.getTs();
 					if (earliestParsedTransactionDate == null || lastTs.before(earliestParsedTransactionDate)) {
 						earliestParsedTransactionDate = lastTs;
 					}
 				}
 
 				List<ShopTransaction> shopTransactions = filter(transactions, ShopTransaction.class);
 				dao.insertTransactions(shopTransactions, true);
 				shopTransactionCount += shopTransactions.size();
 
 				List<PaymentTransaction> paymentTransactions = filter(transactions, PaymentTransaction.class);
 				dao.insertPaymentTransactions(paymentTransactions);
 				paymentTransactionCount += paymentTransactions.size();
 
 				List<BonusFeeTransaction> bonusFeeTransactions = filter(transactions, BonusFeeTransaction.class);
 				dao.updateBonusesFees(bonusFeeTransactions);
 				bonusFeeTransactionCount += bonusFeeTransactions.size();
 
 				pageCount++;
 
 				String pagesText = nf.format(pageCount);
 				if (stopAtPage != null) {
 					pagesText += " / " + stopAtPage;
 				}
 				UpdateDialog.this.pages.setText(pagesText);
 
 				shopTransactionsLabel.setText(nf.format(shopTransactionCount));
 				paymentTransactionsLabel.setText(nf.format(paymentTransactionCount));
 				bonusFeeTransactionsLabel.setText(nf.format(bonusFeeTransactionCount));
 
 				if (pageCount % 100 == 0 && logger.isLoggable(Level.FINEST)) {
 					long fromStart = System.currentTimeMillis() - started;
 					long fromPrevious = System.currentTimeMillis() - previousTime;
 					long fromStartComponents[] = TimeUtils.parseTimeComponents(fromStart);
 					long fromPreviousComponents[] = TimeUtils.parseTimeComponents(fromPrevious);
 					//@formatter:off
 						logger.finest(
 							"DOWNLOAD STATS | " + 
 							"Pages: " + pageCount + " | " +
 							"From start: " + timeNf.format(fromStartComponents[3]) + ":" + timeNf.format(fromStartComponents[2]) + ":" + timeNf.format(fromStartComponents[1]) + " | " +
 							"From previous: " + timeNf.format(fromPreviousComponents[3]) + ":" + timeNf.format(fromPreviousComponents[2]) + ":" + timeNf.format(fromPreviousComponents[1])
 						);
 						//@formatter:on
 					previousTime = System.currentTimeMillis();
 				}
 			}
 		}
 
 		public int getParsedTransactionsCount() {
 			return shopTransactionCount + paymentTransactionCount + bonusFeeTransactionCount;
 		}
 	}
 
 	private class TimerThread extends Thread {
 		private final String estimatedTimeDisplay;
 
 		public TimerThread(Long estimatedTime) {
 			estimatedTimeDisplay = (estimatedTime == null) ? null : DurationFormatUtils.formatDuration(estimatedTime, "HH:mm:ss", true);
 		}
 
 		@Override
 		public void run() {
 			long start = System.currentTimeMillis();
 			NumberFormat nf = new DecimalFormat("00");
 			while (isVisible()) {
 				long elapsed = System.currentTimeMillis() - start;
 				long components[] = TimeUtils.parseTimeComponents(elapsed);
 				String timerText = nf.format(components[3]) + ":" + nf.format(components[2]) + ":" + nf.format(components[1]);
 				if (estimatedTimeDisplay != null) {
 					timerText += " / " + estimatedTimeDisplay;
 				}
 				timerLabel.setText(timerText);
 
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					break;
 				}
 			}
 		}
 	}
 
 	public static void show(MainFrame owner, DbDao dao, Settings settings, TransactionPuller puller, Long estimatedTime) {
 		UpdateDialog dialog = new UpdateDialog(owner, dao, settings, puller, estimatedTime);
 		dialog.setVisible(true);
 	}
 
 	private static class UpdateErrorDialog extends JDialog {
 		private boolean saveTransactions = false;
 
 		private UpdateErrorDialog(Window owner, int pages, int transactions, Date oldestTransaction, final Throwable thrown) {
 			super(owner, "Error", ModalityType.DOCUMENT_MODAL);
 			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 			GuiUtils.closeOnEscapeKeyPress(this);
 
 			JTextArea displayText = new JTextArea("An unexpected error occurred while downloading your transactions.");
 			displayText.setEditable(false);
 			displayText.setBackground(getBackground());
 			displayText.setLineWrap(true);
 			displayText.setWrapStyleWord(true);
 
 			JLabel errorIcon = new JLabel(ImageManager.getErrorIcon());
 
 			JTextArea stackTrace = new JTextArea(ExceptionUtils.getStackTrace(thrown));
 			stackTrace.setEditable(false);
 			stackTrace.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
 
 			final JButton report = new JButton("Report Error");
 			report.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					ReportSender.instance().report(thrown);
 					report.setEnabled(false);
 					report.setText("Reported");
 					JOptionPane.showMessageDialog(UpdateErrorDialog.this, "Error report sent.  Thanks!");
 				}
 			});
 
 			JButton save = new JButton("Save Transactions");
 			save.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					saveTransactions = true;
 					dispose();
 				}
 			});
 
 			JButton discard = new JButton("Discard transactions");
 			discard.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					dispose();
 				}
 			});
 
 			setLayout(new MigLayout());
 			add(errorIcon, "split 2");
 			add(displayText, "w 100:100%:100%, gapleft 10, wrap");
 			JScrollPane scroll = new JScrollPane(stackTrace);
 			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 			add(scroll, "grow, w 100%, h 100%, align center, wrap");
 			add(report, "align right, wrap");
 
 			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
 			//@formatter:off
 			add(new JLabel(
 			"<html>" +
 				"<b>Do you want to save the transactions that have been parsed or try starting over from scratch?</b><br><br>" +
 				"<table border=0>" +
 					"<tr><td>Date of oldest transaction:</td><td>" + df.format(oldestTransaction) + "</td></tr>" +
 					"<tr><td>Total transactions parsed:</td><td>" + transactions + "</td></tr>" +
 					"<tr><td>Total pages parsed:</td><td>" + pages + "</td></tr>" +
 				"</table>" +
 			"</html>"), "align center, wrap");
 			//@formatter:on
 
 			add(save, "align center, split 2");
 			add(discard);
 
 			setSize(500, 500);
 			setLocationRelativeTo(owner);
 		}
 
 		public static boolean show(Window owner, int pages, int transactions, Date oldestTransaction, Throwable thrown) {
 			logger.log(Level.SEVERE, "Error downloading transactions.", thrown);
 
 			UpdateErrorDialog dialog = new UpdateErrorDialog(owner, pages, transactions, oldestTransaction, thrown);
 			dialog.setVisible(true);
 			return dialog.saveTransactions;
 		}
 	}
 }
