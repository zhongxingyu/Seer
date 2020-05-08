 package org.djv.stockresearcher.broker;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.djv.stockresearcher.db.YahooFinanceUtil;
 import org.djv.stockresearcher.model.SectorIndustry;
 import org.djv.stockresearcher.model.StockIndustry;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 public class YahooHTMLSectorIndustryDataBroker implements ISectorIndustryDataBroker{
 	
 	public List<SectorIndustry> getSectors() throws Exception {
 		
 		List<SectorIndustry> list = new ArrayList<SectorIndustry>();
 		String YQLquery = "select * from yahoo.finance.sectors";
 		BufferedReader br = YahooFinanceUtil.getYQLJson(YQLquery);
 		 JsonParser parser = new JsonParser();
 		 JsonObject json = parser.parse(br).getAsJsonObject();
 		 JsonObject query = json.get("query").getAsJsonObject();
 		 JsonObject results = query.get("results").getAsJsonObject();
 		 JsonArray sectors = results.get("sector").getAsJsonArray();
 		 for (JsonElement sectorE: sectors){
 			 JsonObject sector = sectorE.getAsJsonObject();
 			 String sectorname = sector.get("name").getAsString();
 			 
 			 boolean first = true;
 			 JsonElement industryE = sector.get("industry");
 			 if (industryE.isJsonObject()){
 				handleIndustry(list, sectorname, industryE, first);
 				if (first){
 					first = false;
 				}
 			 } else {
 				 JsonArray indArr = industryE.getAsJsonArray();
 				 for (JsonElement industryEe: indArr){
 					 handleIndustry(list, sectorname, industryEe, first);
 						if (first){
 							first = false;
 						}
 				 }
 			 }
 		 }
 
 		 br.close();
 		 
 		 return list;
 	}
 	
 	private void handleIndustry(List<SectorIndustry> list, String sectorname, JsonElement industryEe, boolean first) throws Exception {
 		JsonObject industryO = industryEe.getAsJsonObject();
 		 Integer id = industryO.get("id").getAsInt();
 		 String iname = industryO.get("name").getAsString();
 		 
 		 if (first){
 			 Integer sectid = id / 100 * 100;
 			 String sectname = "ALL";
 			 SectorIndustry si = new SectorIndustry();
 			 si.setIndustryId(sectid);
 			 si.setIndustryName(sectname);
 			 si.setSectorName(sectorname);
 			 list.add(si);
 		 }
 		 SectorIndustry si = new SectorIndustry();
 		 si.setIndustryId(id);
 		 si.setIndustryName(iname);
 		 si.setSectorName(sectorname);
 		 list.add(si);
 	}
 
 	@Override
 	public List<StockIndustry> getStocksForIndustry(Integer ind) {
 		List<StockIndustry> list = getStockIndustryYQL(ind);
 		if (list == null){
 			list = getStockIndustryHTML(ind);
 		}
 		return list;
 	}
 	
 
 	private List<StockIndustry> getStockIndustryYQL(Integer ind) {
 		List<StockIndustry> list = new ArrayList<StockIndustry>();
 		String YQLquery = "select * from yahoo.finance.industry where id=\""
 				+ ind + "\"";
 		BufferedReader br = YahooFinanceUtil.getYQLJson(YQLquery);
 		try {
 			if (br != null) {
 				JsonParser parser = new JsonParser();
 				JsonObject json = parser.parse(br).getAsJsonObject();
 				JsonObject query = json.get("query").getAsJsonObject();
 				JsonObject results = query.get("results").getAsJsonObject();
 				JsonObject industry = results.get("industry").getAsJsonObject();
 				JsonElement iEle = industry.get("company");
 				if (iEle == null) {
 					return null;
 				}
 				if (iEle.isJsonArray()) {
 					JsonArray companies = iEle.getAsJsonArray();
 					for (JsonElement ce : companies) {
 						handleCompany(list, ind, ce);
 					}
 				} else {
 					handleCompany(list, ind, iEle);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				br.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return list;
 	}
 
 	public void handleCompany(List<StockIndustry> list, Integer ind, JsonElement ce) throws Exception {
 		JsonObject c = ce.getAsJsonObject();
 		String name = c.get("name").getAsString();
 		String symbol = c.get("symbol").getAsString();
 		 StockIndustry si = new StockIndustry();
 		 si.setIndId(ind);
 		 si.setName(name);
 		 si.setSymbol(symbol);
 		 list.add(si);
 	}
 	
 	private List<StockIndustry> getStockIndustryHTML(int industryId) {
 		List<StockIndustry> list = new ArrayList<StockIndustry>();
 		BufferedReader br = YahooFinanceUtil.getYahooCSVNice("http://biz.yahoo.com/p/" + industryId + "conameu.html");
 		try {
 			if (br == null){
 				return null;
 			}
 			StringBuffer sb = new StringBuffer();
 			String s = br.readLine();
 			while (s != null){
 				sb.append(s);
 				sb.append(" ");
 				s = br.readLine();
 			}
 			int startIx = sb.indexOf("<b>Companies</b>");
 			String lookFor = "<a href=\"http://us.rd.yahoo.com/finance/industry/quote/colist/*http://biz.yahoo.com/p/";
 			do {
 				int linkIx = sb.indexOf(lookFor, startIx);
 				if (linkIx > -1){
 					int linkEnd = sb.indexOf("</a>", linkIx + lookFor.length());
 					String chopped = sb.substring(linkIx + lookFor.length() + 2, linkEnd);
 					String lookFor2 = ".html\">";
 					int hIx = chopped.indexOf(lookFor2);
 					String symbol = chopped.substring(0, hIx).toUpperCase();
 					String name = chopped.substring(hIx + lookFor2.length());
 	
 					 StockIndustry si = new StockIndustry();
 					 si.setIndId(industryId);
 					 si.setName(name);
 					 si.setSymbol(symbol);
 					 list.add(si);
 					startIx = linkEnd + 4;
 				} else {
 					break;
 				}
 			} while (startIx > -1);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
				if (br != null){
					br.close();
				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return list;
 	}
 
 }
