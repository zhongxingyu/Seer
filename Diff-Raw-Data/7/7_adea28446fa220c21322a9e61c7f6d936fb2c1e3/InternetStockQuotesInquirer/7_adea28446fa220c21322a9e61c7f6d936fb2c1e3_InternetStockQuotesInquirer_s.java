 package com.ghlh.stockquotes;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.log4j.Logger;
 
 import com.ghlh.autotrade.AutoDataCollectingJob;
 import com.ghlh.data.db.GhlhDAO;
 import com.ghlh.data.db.StockdailyinfoVO;
 
 public abstract class InternetStockQuotesInquirer implements
 		StockQuotesInquirer {
 	private static Logger logger = Logger
 			.getLogger(InternetStockQuotesInquirer.class);
 
 	private static HttpClient client = new HttpClient();
 	private static HttpMethod method = null;
 
 	private static StockQuotesInquirer instance = new SinaStockQuotesInquirer();
 
 	public static StockQuotesInquirer getInstance() {
 		return instance;
 	}
 
 	private static StockQuotesInquirer eastMoneyInstance = new EastMoneyStockQuotesInquirer();
 
 	public static StockQuotesInquirer getEastMoneyInstance() {
 		return eastMoneyInstance;
 	}
 
 	public StockQuotesBean getStockQuotesBean(String stockId) {
 		if (testingInjectStockQuotesBean != null) {
 			return testingInjectStockQuotesBean;
 		}
 		StockQuotesBean result = null;
 		try {
 			boolean isSZ = isFromShenzhenMarket(stockId);
 			String url = getStockQuotesPageURL(stockId, isSZ);
 			String data = getStockQuotesInfoFromInternet(url);
 			result = parseStockQuotes(data);
 		} catch (StockQuotesException ex) {
 			logger.error("getStockQuotesBean throw : ", ex);
 		}
 		return result;
 	}
 
 	private StockQuotesBean testingInjectStockQuotesBean;
 
 	public void setTestingInjectStockQuotesBean(StockQuotesBean stockQuotesBean) {
 		this.testingInjectStockQuotesBean = stockQuotesBean;
 	}
 
 	private boolean isFromShenzhenMarket(String stockId) {
 		boolean result = false;
 		if (stockId.indexOf("0") == 0 || stockId.indexOf("3") == 0) {
 			result = true;
 		}
 		return result;
 	}
 
 	protected String getCharset() {
 		return "gb2312";
 	}
 
 	private String getStockQuotesInfoFromInternet(String url)
 			throws StockQuotesException {
 		try {
 			method = new GetMethod(url);
 			client.executeMethod(method);
 			InputStream in = method.getResponseBodyAsStream();
 			BufferedReader br = new BufferedReader(new InputStreamReader(in,
 					getCharset()));
 			String result = br.readLine();
 			return result;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			throw new StockQuotesException(
 					"There is an exception while reading quotes from sohu ", ex);
 
 		}
 	}
 
 	protected abstract String getStockQuotesPageURL(String stockID, boolean isSZ);
 
 	protected abstract StockQuotesBean parseStockQuotes(String stockInfo)
 			throws StockQuotesException;
 
 	public double getLatestPrice(String stockId) {
 		String sql = "SELECT * FROM stockdailyinfo WHERE stockid = '" + stockId
 				+ "' ORDER BY DATE DESC";
 		double result = 0;
 		List list = GhlhDAO
 				.list(sql, "com.ghlh.data.db.StockdailyinfoVO", 0, 1);
 		if (list.size() > 0) {
 			StockdailyinfoVO stockdailyinfoVO = (StockdailyinfoVO) list.get(0);
 			result = stockdailyinfoVO.getCurrentprice();
 		}
 		return result;
 	}
 
 }
