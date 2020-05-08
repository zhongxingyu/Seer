 package nl.tudelft.stocktrader;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.xml.datatype.DatatypeFactory;
 
 import nl.tudelft.ewi.st.atlantis.tudelft.v1.types.AccountData;
 import nl.tudelft.ewi.st.atlantis.tudelft.v1.types.AccountProfileData;
 import nl.tudelft.ewi.st.atlantis.tudelft.v1.types.HoldingData;
 import nl.tudelft.ewi.st.atlantis.tudelft.v1.types.MarketSummaryData;
 import nl.tudelft.ewi.st.atlantis.tudelft.v1.types.OrderData;
 import nl.tudelft.ewi.st.atlantis.tudelft.v1.types.QuoteData;
 import nl.tudelft.ewi.st.atlantis.tudelft.v1.types.WalletData;
 
 import nl.tudelft.stocktrader.Order;
 
 public class TypeFactory {
	public static Order toOrder(OrderData data) {
		return new Order(data.getOrderID(),data.getOrderType(),
						data.getOrderStatus(), (Calendar)data.getOpenDate().toGregorianCalendar(),
						(Calendar)data.getCompletionDate().toGregorianCalendar(), data.getQuantity(),
						new BigDecimal(data.getPrice()), new BigDecimal(data.getOrderFee()),
						data.getSymbol());
	}
	
 	public static OrderData toOrderData(Order order) {
 		DatatypeFactory dtf = null;
 		
 		try {
 			dtf = DatatypeFactory.newInstance();
 		} catch (Exception e) {
 			System.err.println("COULD NOT GET A DATATYPEFACTORY!");
 		}
 		
 		return toOrderData(order, dtf);
 	}
 	
 	public static OrderData toOrderData(Order order, DatatypeFactory dtf) {
 		OrderData ret = new OrderData();
 		
 		ret.setAccountID(order.getAccountId());
 		ret.setCompletionDate(dtf.newXMLGregorianCalendar((GregorianCalendar)order.getCompletionDate()));
 		ret.setHoldingID(order.getHoldingId());
 		GregorianCalendar openDate = order.getOpenDate() != null ?
 										(GregorianCalendar)order.getOpenDate() :
 											null;
 		if (openDate == null) {
 			openDate = (GregorianCalendar)Calendar.getInstance();
 			openDate.setTimeInMillis(0);
 		}
 		ret.setOpenDate(dtf.newXMLGregorianCalendar(openDate));
 		ret.setOrderFee(order.getOrderFee().doubleValue());
 		ret.setOrderID(order.getOrderID());
 		ret.setOrderStatus(order.getOrderStatus());
 		ret.setOrderType(order.getOrderType());
 		ret.setPrice(order.getPrice().doubleValue());
 		ret.setQuantity(order.getQuantity());
 		ret.setSymbol(order.getSymbol());
 		
 		return ret;
 	}
 	
 	public static List<OrderData> toListOrderData(List<Order> orders) {
 		DatatypeFactory dtf = null;
 		
 		try {
 			dtf = DatatypeFactory.newInstance();
 		} catch (Exception e) {
 			System.err.println("COULD NOT GET A DATATYPEFACTORY!");
 		}
 		
 		List<OrderData> orderData = new ArrayList<OrderData>(orders.size());
 		
 		for(Order o : orders) {
 			orderData.add(toOrderData(o, dtf));
 		}
 		
 		return orderData;
 	}
 	
 	public static AccountData toAccountData(Account account) {
 		DatatypeFactory dtf = null;
 		
 		try {
 			dtf = DatatypeFactory.newInstance();
 		} catch (Exception e) {
 			System.err.println("COULD NOT GET A DATATYPEFACTORY!");
 		}
 		
 		try {
 			AccountData ret = new AccountData();
 			
 			ret.setAccountID(account.getAccountID());
 			ret.setBalance(account.getBalance().doubleValue());
 			ret.setCreationDate(dtf.newXMLGregorianCalendar((GregorianCalendar)account.getCreationDate()));
 			ret.setLastLogin(dtf.newXMLGregorianCalendar((GregorianCalendar)account.getLastLogin()));
 			ret.setLoginCount(account.getLoginCount());
 			ret.setLogoutCount(account.getLogoutCount());
 			ret.setOpenBalance(account.getOpenBalance().doubleValue());
 			ret.setProfileID(account.getProfileID());
 			ret.setCurrencyType(account.getCurrencyType());
 			
 			return ret;
 		} catch (NullPointerException e) {
 			return null;
 		}
 	}
 	
 	public static HoldingData toHoldingData(Holding holding, DatatypeFactory dtf) {
 		try {
 			dtf = DatatypeFactory.newInstance();
 		} catch (Exception e) {
 			System.err.println("COULD NOT GET A DATATYPEFACTORY!");
 		}
 		
 		HoldingData ret = new HoldingData();
 		
 		ret.setHoldingID(holding.getHoldingID());
 		ret.setPurchaseDate(dtf.newXMLGregorianCalendar((GregorianCalendar)holding.getPurchaseDate()));
 		ret.setPurchasePrice(holding.getPurchasePrice().doubleValue());
 		ret.setQuantity(holding.getQuantity());
 		ret.setQuoteID(holding.getQuoteId());
 		
 		return ret;
 	}
 	
 	public static HoldingData toHoldingData(Holding holding) {
 		DatatypeFactory dtf = null;
 		
 		try {
 			dtf = DatatypeFactory.newInstance();
 		} catch (Exception e) {
 			System.err.println("COULD NOT GET A DATATYPEFACTORY!");
 		}
 		
 		return toHoldingData(holding, dtf);
 	}
 	
 	public static List<HoldingData> toListHoldingData(List<Holding> holdings) {
 		DatatypeFactory dtf = null;
 		
 		try {
 			dtf = DatatypeFactory.newInstance();
 		} catch (Exception e) {
 			System.err.println("COULD NOT GET A DATATYPEFACTORY!");
 		}
 		
 		List<HoldingData> holdingDatum = new ArrayList<HoldingData>(holdings.size());
 		
 		for(Holding h : holdings) {
 			holdingDatum.add(toHoldingData(h, dtf));
 		}
 		
 		return holdingDatum;
 	}
 	
 	public static AccountProfile toAccountProfile(AccountProfileData ap) {
 		AccountProfile ret = new AccountProfile();
 		
 		ret.setAddress(ap.getAddress());
 		ret.setCreditCard(ap.getCreditCard());
 		ret.setEmail(ap.getEmail());
 		ret.setFullName(ap.getFullName());
 		ret.setPassword(ap.getPassword());
 		ret.setUserID(ap.getUserID());
 
 		return ret;
 	}
 	
 	public static AccountProfileData toAccountProfileData(AccountProfile ap) {
 		AccountProfileData ret = new AccountProfileData();
 		
 		ret.setAddress(ap.getAddress());
 		ret.setCreditCard(ap.getCreditCard());
 		ret.setEmail(ap.getEmail());
 		ret.setFullName(ap.getFullName());
 		ret.setPassword(ap.getPassword());
 		ret.setUserID(ap.getUserID());
 		
 		return ret;
 	}
 	
 	public static List<QuoteData> toListQuoteData(List<Quote> quotes) {
 		List<QuoteData> quoteData = new ArrayList<QuoteData>(quotes.size());
 		
 		for(Quote q : quotes) {
 			quoteData.add(toQuoteData(q));
 		}
 		
 		return quoteData;
 	}
 	
 	public static QuoteData toQuoteData(Quote q) {
 		QuoteData ret = new QuoteData();
 		
 		ret.setChange(q.getChange());
 		ret.setCompanyName(q.getCompanyName());
 		ret.setHigh(q.getHigh().doubleValue());
 		ret.setLow(q.getLow().doubleValue());
 		ret.setOpen(q.getOpen().doubleValue());
 		ret.setPrice(q.getPrice().doubleValue());
 		ret.setSymbol(q.getSymbol());
 		ret.setVolume(q.getVolume());
 		
 		return ret;
 	}
 	
 	public static MarketSummaryData toMarketSummaryData(MarketSummary ms) {
 		DatatypeFactory dtf = null;
 		
 		try {
 			dtf = DatatypeFactory.newInstance();
 		} catch (Exception e) {
 			System.err.println("COULD NOT GET A DATATYPEFACTORY!");
 		}
 		
 		MarketSummaryData ret = new MarketSummaryData();
 		
 		ret.setOpenTSIA(ms.getOpenTSIA().doubleValue());
 		ret.setSummaryDate(dtf.newXMLGregorianCalendar((GregorianCalendar)ms.getSummaryDate()));
 		ret.setTSIA(ms.getTSIA().doubleValue());
 		ret.setVolume(ms.getVolume());
 		
 		return ret;
 	}
 	
 	public static WalletData toWalletData(Wallet w){
 		WalletData wd = new WalletData();
 		
 		wd.setUserID(w.getUserID());
 		wd.setCny(w.getCny().doubleValue());
 		wd.setEur(w.getEur().doubleValue());
 		wd.setGbp(w.getGbp().doubleValue());
 		wd.setInr(w.getInr().doubleValue());
 		wd.setUsd(w.getUsd().doubleValue());
 		
 		return wd;
 	}
 	
 	public static Wallet toWallet(WalletData wd){
 		Wallet w = new Wallet(wd.getUserID());
 		w.setCny(BigDecimal.valueOf(wd.getCny()));
 		w.setEur(BigDecimal.valueOf(wd.getEur()));
 		w.setGbp(BigDecimal.valueOf(wd.getGbp()));
 		w.setInr(BigDecimal.valueOf(wd.getInr()));
 		w.setUsd(BigDecimal.valueOf(wd.getUsd()));
 		
 		return w;
 	}
 	
 }
