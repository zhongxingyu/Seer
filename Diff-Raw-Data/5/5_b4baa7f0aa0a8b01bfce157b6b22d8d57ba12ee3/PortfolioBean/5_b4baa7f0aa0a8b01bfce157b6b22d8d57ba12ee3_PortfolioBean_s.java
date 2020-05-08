 /*
  * Encapsulates all the methods for reading in lists of ShareTransactions, as
  * well as adding and deleting transactions.
  * 
  * Originally I had these as two separate classes; I brought them together when
  * I realised that I wanted a delete() or save() operation to invalidate the
  * cache I was maintaining for listing operations.
  */
 package org.tomhume.webapps;
 
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Named;
 import uk.ac.susx.inf.ianw.shareManagement.PurchaseResult;
 
 /**
  *
  * @author twhume
  */
 @Named(value = "portfolio")
 @RequestScoped
 public class PortfolioBean {
     @EJB PortfolioControllerLocal portfolio; 
     @EJB ShareBrokerControllerLocal broker;
     
     /* Fields used for adding a new ShareTransaction */
     
     private String company;
     private String transactionType;
     private String purchasePrice;
     private String submit;
     private String amount;
     private String id;
     
     /* List where we store the current portfolio values, so that a JSF page
      * can refer to them many times in the course of a single request without
      * their being re-fetched from the database.
      */
     private List<ShareValue> svList = null;
     
     /* Similarly, here's a place where we can cache lists of transactions.
      * This means that in future we could use this bean in pages where we 
      * had, say, separate Datatables for each company.
      */
     private Map<String, List<ShareTransaction>> txCache = new TreeMap<String, List<ShareTransaction>>();
     
     /* Magic key used to store the transactions for "all companies" in txCache */
     private static final String ALL_COMPANIES_KEY = "___ALL___";
     
     public PortfolioBean() {
     }
     
     /**
      * Fetch a complete log of all ShareTransactions to date, pulling it from the cache if possible.
      * 
      * @return 
      */
     
     public List<ShareTransaction> getTransactions() {
         if (txCache.get(ALL_COMPANIES_KEY)==null) txCache.put(ALL_COMPANIES_KEY, portfolio.list());
         return txCache.get(ALL_COMPANIES_KEY);
     }
     
     /**
      * Fetch a complete log of all ShareTransactions to date for a given company,
      * pulling it from the cache if necessary.
      * 
      * Hmph, can't remember why I wrote this. But it's the kind of thing which might
      * come in handy one day, so I'll leave it here.
      * 
      * @return 
      */
 
     public List<ShareTransaction> getTransactionsForCompany(String c) {
         if (txCache.get(c)==null) txCache.put(c, portfolio.listForCompany(c));
         return txCache.get(c);
     }
 
     /**
      * Fetch a summary report of the current portfolio, including values at the
      * valuation provided by the ShareBroker service.
      * 
      * @return 
      */
 
     public List<ShareValue> getValues() {
         if (svList==null) svList = broker.getPortfolioValues();
         return svList;
     }
     
     /**
      * Empty all the caches.
      * 
      * @return 
      */
 
     
     private void invalidateCache() {
         txCache = new TreeMap<String, List<ShareTransaction>>();
         svList = null;
     }
     
     public String getId() {
         return id;
     }
     
     public void setId(String id) {
         this.id = id;
     }
 
     public String getSubmit() {
         return submit;
     }
 
     public void setSubmit(String submit) {
         this.submit = submit;
     }
     
     public String getCompany() {
         return company;
     }
 
     public void setCompany(String company) {
         this.company = company;
     }
 
     public String getTransactionType() {
         return transactionType;
     }
 
     public void setTransactionType(String transactionType) {
         this.transactionType = transactionType;
     }
 
     public String getAmount() {
         return amount;
     }
 
     public void setAmount(String amount) {
         this.amount = amount;
     }
     
     public String getPurchasePrice() {
         return purchasePrice;
     }
 
     public void setPurchasePrice(String purchasePrice) {
         this.purchasePrice = purchasePrice;
     }
     
     public String[] getCompanies() {
         return ShareTransaction.COMPANIES;
     }
     
     /**
      * Delete a single ShareTransaction from our records, referenced by its ID
      * 
      * @return 
      */
 
     
     public String delete() {
         portfolio.delete(new ShareTransaction(Long.parseLong(this.id)));
         invalidateCache();
         return "index";
     }
     
     /**
      * Save a single ShareTransaction into the portfolio, the invalidate the cache
      * so we see it in the lists we get in the getTransactions... and getValues()
      * methods above.
      * 
      * @return 
      */
 
     public String saveTransaction() {
         double amountD = Double.parseDouble(this.amount);
         if (transactionType.equalsIgnoreCase("Sell")) amountD = amountD * -1;
 
         /* Trying to sell more than we own? Drop the transaction */
         
         if (!isValidTransaction(company, amountD)) {
             cleanParameters();
             return "index";
         }
 
         ShareTransaction st = new ShareTransaction();
         st.setAmount(amountD);
         
         int pricePaid = centsFromDollars(this.purchasePrice);
         
        st.setPricePaid((int) Math.abs(amountD * pricePaid));
         st.setCompany(this.company);
 
         portfolio.add(st);
         invalidateCache();
         cleanParameters();
         return "index";
     }
     
     /**
      * The other sort of action we can trigger is a full PurchaseRequest: where
      * we go to the ShareBroker, request a sale, get back details of the price
      * paid, and save these details as a new ShareTransaction.
      * 
      * @return 
      */
     
     public String makePurchaseRequest() {
         double amountD = Double.parseDouble(this.amount);
         if (transactionType.equalsIgnoreCase("Sell")) amountD = amountD * -1;
 
         /* Trying to sell more than we own? Drop the transaction */
 
         if (!isValidTransaction(company, amountD)) {
             cleanParameters();
             return "index";
         }
 
         try {
             PurchaseResult res = broker.makePurchase(company, amountD);
             if (res!=null) {
                 ShareTransaction st = new ShareTransaction();
                 st.setAmount(amountD);
                st.setPricePaid(Math.abs((int)(amountD * res.getPrice()*100)));
                 st.setCompany(company);
                 portfolio.add(st);
             }
         } catch (Exception e) {
             /* Generally it's bad practice to catch Exception, but there's so many it 
              * could be and I don't think we should be floating messages about
              * KeyStores or RemoteExceptions to the UI
              */
             e.printStackTrace();
         }
         invalidateCache();
         cleanParameters();
         return "index";
     }
     
     /**
      * When we've saved a new transaction, we want to clean out all the parameters
      * so that the addition form isn't pre-filled with the details of the transaction
      * we just created...
      */
     
     private void cleanParameters() {
         this.amount = "";
         this.company = "";
         this.purchasePrice = "";
         this.id = "";
         this.transactionType = "";
     }
     
     /**
      * Takes a string of the form "100" or "100.01" and returns the number of
      * cents as an integer. So "100" would become "10000" and "100.01" "10001".
      * @param p
      * @return 
      */
 
     private int centsFromDollars(String p) {
         if (!p.contains(".")) {
             return (Integer.parseInt(p)*100);
         }
         String[] fields = p.split("\\.");
         System.err.println("Have " + fields.length + " from " +p);
         //TODO add proper error handling here
         return (Integer.parseInt(fields[0])*100) + Integer.parseInt(fields[1]);
     }
     
     /**
      * Checks whether the proposed share transaction is valid; in particular,
      * looks to see that we are not trying to sell more shares than we currently
      * own.
      * 
      * Out of respect for the beauty of free market economics, the author has
      * placed no limit on how many shares can be purchased.
      * 
      * @param company   Company in which we are buying/selling shares
      * @param amount    Quantity of shares we are trying to buy/sell
      * @return 
      */
     
     private boolean isValidTransaction(String company, double amount) {
         
         /* We can purchase any amount */
         if (amount>=0) return true;
         
         /* Tot up how many shares we own in this company, by adding all sales
          * and purchases. We could go back to the database for this, but we
          * will likely have the data cached locally; so let's avoid that now.
          */
         List<ShareTransaction> shares = getTransactionsForCompany(company);
         double sharesHeld = 0;
         for (ShareTransaction t: shares) {
             sharesHeld += t.getAmount();
         }
 
         if (sharesHeld>= (amount * -1)) return true;
         return false;
     }
 }
