 package vsp;
 
 import java.io.*;
 import java.net.*;
 import java.text.*;
 import java.util.*;
 
 import vsp.dataObject.HistoricalStockInfo;
 import vsp.dataObject.Stock;
 import vsp.dataObject.StockInfo;
 
 public final class StockInfoServiceProvider
 {
 	public StockInfoServiceProvider()
 	{
 		// no implementation required
 	}
 	
 	public boolean isWithinTradingHours()
 	{
 		boolean withinTradingHours = false;
 		String url = "http://finance.yahoo.com/d/quotes.csv?s=CIF&f=a";
 		// a, Ask
 		
 		List<String> responseLines = null;
 		try
 		{
 			responseLines = getDataFromUrl(url);
 			if (responseLines.size() == 1)
 			{
 				if (!responseLines.get(0).contains("N/A"))
 				{
 					withinTradingHours = true;
 				}
 			}
 		}
 		catch (Exception ex)
 		{
 			// ignore
 		}
 		
 		return withinTradingHours;
 	}
 	
 	public List<HistoricalStockInfo> requestDailyHistoricalStockData(String symbol, Date since)
 	{
 		Date today = new Date();
 		String historyUrl = "http://ichart.yahoo.com/table.csv?s=" + symbol;
 		historyUrl += "&a=" + Integer.toString(since.getMonth() - 1); // month - 1
 		historyUrl += "&b=" + Integer.toString(since.getDay()); // day
 		historyUrl += "&c=" + Integer.toString(since.getYear()); // year
 		historyUrl += "&d=" + Integer.toString(today.getMonth() - 1); // month - 1
 		historyUrl += "&e=" + Integer.toString(today.getDay()); // day
 		historyUrl += "&f=" + Integer.toString(today.getYear()); // year
 		historyUrl += "g=d"; // daily history 
 		
 		List<HistoricalStockInfo> results = new ArrayList<HistoricalStockInfo>();
 		List<String> data = null;
 		try
 		{
 			data = getDataFromUrl(historyUrl);
 		}
 		catch (Exception ex)
 		{
 			// ignore
 		}
 		
 		if (data != null && data.size() > 1)
 		{
 			HistoricalStockInfo stockInfo = null;
 			// ignore the first row as it's just the column headers
 			for (int i = 1; i < data.size(); ++i)
 			{
 				stockInfo = parseHistoricalStockInfo(data.get(i));
 				if (stockInfo != null)
 				{
 					results.add(stockInfo);
 				}
 			}
 		}
 		
 		return results;
 	}
 	
 	public List<Stock> searchForStocks(String search)
 	{
 		String searchUrl = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=" + search + "&callback=YAHOO.Finance.SymbolSuggest.ssCallback";		
 		
 		List<Stock> results = new ArrayList<Stock>();
 		List<String> data = null;
 		try
 		{
 			data = getDataFromUrl(searchUrl);
 		}
 		catch (Exception ex)
 		{
 			// ignore
 		}
 		
 		if (data != null && data.size() == 1)
 		{
 			results = parseStockSymbolsAndNames(data.get(0));
 		}
 		
 		return results;
 	}
 	
 	public StockInfo requestCurrentStockData(String symbol)
 	{
 		StockInfo stockInfo = null;
 		String url = "http://finance.yahoo.com/d/quotes.csv?s=" + symbol + "&f=nb3b2ghvoc6p2qd";
 		// n, Name
 		// b3, Bid
 		// b2, Ask
 		// g, Day Low
 		// h, Day High
 		// v, Volume
 		// o, Open
 		// c6, Change real-time
 		// p2, Percent Change real-time
 		// q, Ex-dividend date
 		// d, Dividend/Share
 		
 		List<String> responseLines = null;
 		try
 		{
 			responseLines = getDataFromUrl(url);
 		}
 		catch (Exception ex)
 		{
 			// ignore
 		}
 		
 		if (responseLines != null && responseLines.size() == 1)
 		{
 			stockInfo = parseStockInfo(symbol, responseLines.get(0));
 		}
 		
 		return stockInfo;
 	}
 	
 	public List<StockInfo> requestCurrentStockData(List<String> symbols)
 	{
 		List<StockInfo> results = new ArrayList<StockInfo>();
 		if (!symbols.isEmpty())
 		{
 			String url = "http://finance.yahoo.com/d/quotes.csv?s=" + symbols.get(0);
 			for (int i = 1; i < symbols.size(); ++i)
 			{
 				url += "+" + symbols.get(i).trim();
 			}
 			
 			url += "&f=nb3b2ghvoc6p2qd";
 			// n, Name
 			// b3, Bid real-time
 			// b2, Ask real-time
 			// g, Day Low
 			// h, Day High
 			// v, Volume
 			// o, Open
 			// c6, Change real-time
 			// p2, Percent Change real-time
 			// q, Ex-dividend date
 			// d, Dividend/Share
 			
 			List<String> responseLines = null;
 			try
 			{
 				responseLines = getDataFromUrl(url);
 			}
 			catch (Exception ex)
 			{
 				// ignore
 			}
 			
 			if (responseLines != null)
 			{
 				StockInfo stockInfo = null;
 				for (int i = 0; i < responseLines.size(); ++i)
 				{
 					stockInfo = parseStockInfo(symbols.get(i), responseLines.get(i));
 					if (stockInfo != null)
 					{
 						results.add(stockInfo);
 					}
 				}
 			}
 		}
 		
 		return results;
 	}
 	
 	public HistoricalStockInfo requestHistoricalStockData(String symbol, int months)
 	{
 		// TODO: implement
 		return null;
 	}
 	
 	private static List<Stock> parseStockSymbolsAndNames(String json)
 	{
 		/*YAHOO.Finance.SymbolSuggest.ssCallback(
 		 * {
 		 * 	"ResultSet":
 		 * 	{
 		 * 		"Query":"mfs",
 		 * 		"Result":[
 		 * 			{"symbol":"MCR","name": "MFS Charter Income Trust Common","exch": "NYQ","type": "S","exchDisp":"NYSE","typeDisp":"Equity"},
 		 * 			{"symbol":"CMK","name": "MFS InterMarket Income Trust I","exch": "NYQ","type": "S","exchDisp":"NYSE","typeDisp":"Equity"},
 		 * 			{"symbol":"CMU","name": "MFS High Yield Municipal Trust","exch": "NYQ","type": "S","exchDisp":"NYSE","typeDisp":"Equity"},
 		 * 			{"symbol":"MIN","name": "MFS Intermediate Income Trust","exch": "NYQ","type": "S","exchDisp":"NYSE","typeDisp":"Equity"},
 		 * 			{"symbol":"MHOCX","name": "MFS High Yield Opportunities C","exch": "NAS","type": "M","exchDisp":"NASDAQ","typeDisp":"Fund"},
 		 * 			{"symbol":"CIF","name": "MFS Intermediate High Income Fu","exch": "NYQ","type": "S","exchDisp":"NYSE","typeDisp":"Equity"},
 		 * 			{"symbol":"MMUFX","name": "MFS Utilities A","exch": "NAS","type": "M","exchDisp":"NASDAQ","typeDisp":"Fund"},
 		 * 			{"symbol":"CCA","name": "MFS California Municipal Fund C","exch": "ASE","type": "S","exchDisp":"AMEX","typeDisp":"Equity"},
 		 * 			{"symbol":"MMT","name": "MFS Multimarket Income Trust","exch": "NYQ","type": "S","exchDisp":"NYSE","typeDisp":"Equity"},
 		 * 			{"symbol":"MIGFX","name": "MFS Massachusetts Investors Gr Stk A","exch": "NAS","type": "M","exchDisp":"NASDAQ","typeDisp":"Fund"}
 		 * 		]
 		 * 	}
 		 * })
 		 * */
 		
 		List<Stock> results = new ArrayList<Stock>();
 		if (json.startsWith("YAHOO.Finance.SymbolSuggest.ssCallback({\"ResultSet\":{\"Query\":\""))
 		{
 			// reduce string to result entries only
 			String beginString = "\"Result\":[";
 			int begin = json.indexOf(beginString);
 			int end = json.indexOf("]}})");
 			json = json.substring(begin + beginString.length(), end);
 			
 			// process result entries
 			String[] resultItems = json.split("}");
 			if (resultItems.length > 0)
 			{
 				// cleanup result entries
 				for (int i = 0; i < resultItems.length; ++i)
 				{
 					resultItems[i] = resultItems[i].replace(",{",  "");
 					resultItems[i] = resultItems[i].replace("{",  "");
 				}
 				
 				// extract pairs per entry
 				boolean stock;
 				String name, symbol;
 				for (int i = 0; i < resultItems.length; ++i)
 				{
 					stock = false;
 					name = null;
 					symbol = null;
 					
 					String[] pairs = resultItems[i].split("\",\"");
 					if (pairs.length > 0)
 					{
 						// cleanup pairs & extract name/symbol data
 						for (int j = 0; j < pairs.length; ++j)
 						{
 							pairs[j] = pairs[j].replace("\"", "");
 							if (pairs[j].startsWith("symbol:"))
 							{
 								symbol = pairs[j];
 							}
 							else if (pairs[j].startsWith("name:"))
 							{
 								name = pairs[j];
 							}
 							else if (pairs[j].startsWith("type:") && pairs[j].endsWith("S"))
 							{
 								stock = true;
 							}
 						}
 						
 						// extract symbol & name into a Stock instance
 						if (stock && symbol != null && name != null)
 						{
 							symbol = symbol.replace("symbol:", "").trim();
 							name = name.replace("name:", "").trim();
 							results.add(new Stock(symbol, name));
 						}
 					}
 				}
 			}
 		}
 		
 		return results;
 	}
 	
 	private static HistoricalStockInfo parseHistoricalStockInfo(String line)
 	{
 		HistoricalStockInfo stockInfo = null;
 		
 		// parse into columns
 		String[] columns = line.split(",");
 		for (int i = 0; i < columns.length; ++i)
 		{
 			columns[i] = columns[i].replaceAll("\"", "").trim();
 		}
 			
 		if (columns.length == 7)
 		{		
 			// parse date
 			Date date = null;
 			try
 			{
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);	
 				date = sdf.parse(columns[0]);
 				
 				// create StockInfo instance
 				stockInfo = new HistoricalStockInfo(date, 	// date
 						Double.parseDouble(columns[1]),		// open
 						Double.parseDouble(columns[2]),		// day high
 						Double.parseDouble(columns[3]),		// day low
 						Double.parseDouble(columns[4]),		// close
 						Integer.parseInt(columns[5]),		// volume
 						Double.parseDouble(columns[6])	 	// adjusted close
 						);
 			}
 			catch (ParseException pe)
 			{
				// ignore
 			}
 			catch (NumberFormatException nfe)
 			{
 				// ignore
 			}
 		}
 		
 		return stockInfo;
 	}
 	
 	private static StockInfo parseStockInfo(String symbol, String line)
 	{
 		StockInfo stockInfo = null;
 		
 		// parse into columns
 		String[] columns = line.split(",");
 		for (int i = 0; i < columns.length; ++i)
 		{
 			columns[i] = columns[i].replaceAll("\"", "").trim();
 		}
 			
 		if (columns.length == 11)
 		{		
 			// parse date
 			Date date = null;
 			try
 			{
 				SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.ENGLISH);	
 				date = sdf.parse(columns[9]);
 				
 				int year = Calendar.getInstance().get(Calendar.YEAR);
 				date.setYear(year);
 				
 				// create StockInfo instance
 				stockInfo = new StockInfo(symbol, 
 						columns[0], 										// description
 						Double.parseDouble(columns[4]),						// dayHigh
 						Double.parseDouble(columns[3]),						// dayLow
 						date,				 								// ex-dividend date, Date
 						Double.parseDouble(columns[10]),					// ex-dividend
 						Double.parseDouble(columns[7].replace("+", "")), 	// price change since open
 						Double.parseDouble(columns[8].replace("%", "")), 	// percent change since open
 						Integer.parseInt(columns[5]), 						// volume
 						Double.parseDouble(columns[1]), 					// bid
 						Double.parseDouble(columns[2]),						// ask
 						Double.parseDouble(columns[4]) 						// open
 						);
 			}
 			catch (ParseException pe)
 			{
 				// ignore
 			}
 			catch (NumberFormatException nfe)
 			{
 				// ignore
 			}
 		}
 		
 		return stockInfo;
 	}
 	
 	private static List<String> getDataFromUrl(String url) throws MalformedURLException, IOException
 	{
 		List<String> results = new ArrayList<String>();
 
 		URL requestUrl = new URL(url);
         URLConnection connection = requestUrl.openConnection();
         BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         String inputLine;
 
         while ((inputLine = in.readLine()) != null) 
         {
         	results.add(inputLine);
         }
         
         in.close();
 		
 		return results;
 	}
 }
