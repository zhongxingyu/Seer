 /*
  * Created on 6 sept. 2004
  */
 package net.sf.jmoney.charts;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.security.InvalidParameterException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.TreeSet;
 
import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.CurrencyAccount;
 import net.sf.jmoney.model2.Session;
 
 import org.jfree.chart.ChartMouseEvent;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.entity.PieSectionEntity;
 import org.jfree.chart.plot.PiePlot;
 import org.jfree.chart.title.TextTitle;
 import org.jfree.data.DefaultPieDataset;
 
 
 public class ExpensePieChart extends PieChart {
  
     protected Date fromDate; 
 	protected Date toDate;
 	protected int  maxLevel;
 	String[] accounts;
 	Container containerForSeveralGraphics;
 	
 	public ExpensePieChart(String title, Session session, int maxLevel, String[] accounts) {
         super(title, session);
 	    this.accounts = accounts;
 	    this.maxLevel = maxLevel;
 	}
 
 	public ExpensePieChart(String title, Session session, int maxLevel, String account) {
 		this(title, session, maxLevel, new String[1]);
 		accounts[0] = account;
 	}
 
 	public ExpensePieChart(String title, Session session, int maxLevel) {
 		this(title, session, maxLevel, "Dpenses");
 	}
 	
 	public void setAccount (String account) {
 	    accounts[0] = account;
 	}
 	
 	public void setDates (Date from, Date to) {
 	  fromDate = from;
 	  toDate = to;
 	}
 
 	public void setMaxLevel(String newMaxLevel) {
 	    try {
 	        maxLevel = Integer.parseInt(newMaxLevel);
 	    } catch (NumberFormatException e ) {}
 	}
 	
 	protected void createValues() {
 
 		// TODO: Faucheux - der gewhlte Level sollte  parametrisierbar sein.
 		long totalBalance = 0;
 
 		// collect the values
 		TreeSet values = new TreeSet ();
 		
 		// TODO: Faucheux - The list of accounts should be parametrisabled.
         List listAccounts = new LinkedList();
         for (int i=0; i<accounts.length; i++) {
             CapitalAccount theAccount = (CapitalAccount) Util.getAccountByFullName(session, accounts[i]);
             listAccounts.add(theAccount);
             if (maxLevel > theAccount.getLevel())
                 listAccounts.addAll(Util.getSubAccountsUntilLevel(theAccount, maxLevel));
             else
                 listAccounts.addAll(Util.getSubAccounts(theAccount));
         }
         Iterator aIt = listAccounts.iterator(); 
 	    // Iterator aIt = util.getAccountsUntilLevel(session,maxLevel).iterator();
 	    while (aIt.hasNext()) {
 	        Account currentAccount = (Account) aIt.next();
 	        // TODO: Konten, die mehrere Whrung enthalten,
 	        // mussen auch gezeigt sind.
 	        if (currentAccount instanceof CurrencyAccount) {
 	        	CurrencyAccount currentCapitalAccount = (CurrencyAccount) currentAccount;
 	        	long balance;
 	        	
 	        	if (currentAccount.getLevel() < maxLevel) {
 	        		// If the account has sub accounts, they will have their own entry -> we don't have to include them here
 	        		balance = currentCapitalAccount.getBalance(session, fromDate, toDate);
 	        	} else {
 	        		// If the account has sub accounts, they won't have their own entry -> we have to include them here
 	        		balance = currentCapitalAccount.getBalanceWithSubAccounts(session, fromDate, toDate);
 	        	}
 	        	
 			    if (balance >= 0) {
 			    	if (ChartsPlugin.DEBUG) System.out.println(currentAccount.getName() + " : " + balance/100 +".");
 			        values.add(new CoupleStringNumber(currentAccount.getFullAccountName(), currentAccount.getName(), balance/100));
 			    	totalBalance += balance /100 ;
 			    }
 	        }
 	    }
 	    
 	    // Calculate the 5 % of the graph and group the values which generate it.
 	    long smallBalance = 0;
 	    boolean finished = false;
 	    String nameRest = new String();
 	    while (! finished) {
 	        CoupleStringNumber csn=null ;
 	        try { csn = (CoupleStringNumber) values.last(); } catch (NoSuchElementException e) {}; 
 	        if (csn!=null && smallBalance + csn.n < (totalBalance * 0.05)) {
 	            nameRest = nameRest + " + " + csn.s;
 	            smallBalance += csn.n;
 	            values.remove(csn);
 	        } else {
 	            finished = true;
 	        }
 	    }
 	    if (nameRest.length() > 3) nameRest = nameRest.substring(3);    // to avoid to begin with a +
 	    if (nameRest.length() > 60) nameRest = nameRest.substring(0, 60) + "...";
 	    nameRest = "Reste";
 
 	    // create a new Dataset
 	    data = new DefaultPieDataset();
 	    if (chart!=null) ((PiePlot) chart.getPlot()).setDataset(data);
 	    
 	    // set the (sorted) values in the graph
 	    Iterator it = values.iterator();
 	    // TODO: Variant -> Do not display the value for the all time, but per day
 	    long intervallInDays = (toDate.getTime() - fromDate.getTime()) / 1000 / 60 / 60 / 24;
 	    intervallInDays = intervallInDays / 100; // To have Cent and not Euro
 	    while (it.hasNext()) {
 	    	CoupleStringNumber csn = (CoupleStringNumber) it.next();
 		    // data.setValue(csn, new Long(csn.n));
 	    	data.setValue(csn, new Long(csn.n / intervallInDays));
 	    }
 	    if (smallBalance > 0)
 	        // data.setValue(nameRest, smallBalance); 
 		    data.setValue(nameRest, smallBalance / intervallInDays);
 
 	    // set the title
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
     	title = accounts[0];
     	subTitle = new String (" from " + df.format(fromDate) + " to " + df.format(toDate));
     	if (chart != null) {
     	    chart.setTitle(title);
     	    chart.addSubtitle(new TextTitle(subTitle));
     	}
     	
 
     }
 
 	protected void initParameters() {
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         try {
         	fromDate = df.parse("2004-01-01");
         	title = title + "\n" + fromDate + " - " + toDate;
         } catch (ParseException e) {
        	JMoneyPlugin.log(e);
         }
     	toDate = new Date();
 	}
 
 	/**
 	 * Object to stock the String and Number the time to sort them in a TreeSet
 	 * @author Faucheux
 	 */
 	private class CoupleStringNumber implements Comparable {
 		public String s;
 		public String longName;
 		public long n;
 		
 		
 		public CoupleStringNumber(String s, long n) {
 			this.s = s;
 			this.n = n;
 			this.longName = s;
 		}
 		
 		public CoupleStringNumber(String longName, String s, long n) {
 			this(s,n);
 			this.longName = longName;
 		}
 
 		
 		public String toString () {
 		    return s;
 		}
 		
 		public int compareTo (Object csn2) {
 		    // Compare first the values, than the names.
 		    // If the names are the same too, then compare the hash codes: two objects 
 		    // should be considered as equal iff they really are the same.
 		    // Caution: to have the display well-displayed, we have to say "bigger first"
 		    // and therefore inverse the results;
 			if (csn2 instanceof CoupleStringNumber) {
 			    String s2 = ((CoupleStringNumber)csn2).s;
 			    long n2 = ((CoupleStringNumber)csn2).n;
 			    int difference = (new Long(n)).compareTo(new Long(n2));
 			    if (difference == 0) difference = s.compareTo(s2);
 			    if (difference == 0) difference = s.hashCode() - s2.hashCode();
 			    return - difference;
 			} else {
 				throw new InvalidParameterException("A CoupleStringNumber object can't be compared to an other typ of object.");
 			}
 			
 		}
 
 }
 	
 	public void chartMouseClicked (ChartMouseEvent e) {
 		if (ChartsPlugin.DEBUG) System.out.println("in chartMouseClicked");
 	    if (e.getEntity() != null) {
 	        PieSectionEntity pieSection = (PieSectionEntity) e.getEntity();
 	        String accountClicked = ((CoupleStringNumber) pieSection.getSectionKey()).longName;
 	        if (ChartsPlugin.DEBUG) System.out.println("Im Entity : " + accountClicked);
 	        PieChart newChart = new ExpensePieChart(accountClicked, this.session, this.maxLevel + 1, accountClicked);
 	        newChart.run();
 	        ChartPanel chartPanel = newChart.getChartPanel();
 	        if (containerForSeveralGraphics != null) {
 	            GridBagConstraints gbConstraints = new GridBagConstraints();
 				gbConstraints.fill = GridBagConstraints.BOTH;
 				gbConstraints.weightx = 1; gbConstraints.weighty = 1;
 				gbConstraints.gridy = 2;
 				((GridBagLayout) containerForSeveralGraphics.getLayout()).setConstraints(chartPanel, gbConstraints);
 	            containerForSeveralGraphics.add(chartPanel);
 	            containerForSeveralGraphics.setBackground(Color.MAGENTA);
 	            containerForSeveralGraphics.validate();
 	        }
 	    }
 	}
 
 
     /**
      * @param containerForSeveralGraphics The containerForSeveralGraphics to set.
      */
     public void setContainerForSeveralGraphics(
             Container containerForSeveralGraphics) {
         this.containerForSeveralGraphics = containerForSeveralGraphics;
     }
 }
