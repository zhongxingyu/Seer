 package com.apple.iad.parser;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
import lombok.Getter;
import lombok.Setter;

 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.bean.CsvToBean;
 import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
 
 import com.apple.iad.beans.IAdByNameBean;
 import com.apple.iad.ingestion.IAdReportInfoDirector;
 import com.apple.iad.ingestion.IAdReprotInfo;
 
 /**
  * 
  * @author Georgi Lambov
  * 
  */
 public class ParserByName extends IAdReportParser implements IAdReprotInfo<List<IAdByNameBean>> {
 
 	// mapped fields
 	public static final String NAME = "name";
 	public static final String REVENUE = "revenue";
 	public static final String ECPM = "ecpm";
 	public static final String TTR = "ttr";
 	public static final String REQUESTS = "requests";
 	public static final String IMPRESSIONS = "impressions";
 	public static final String FILL_RATE = "fillRate";
 	public static final String IAD_FILL_RATE = "iAdFillRate";
 
 	// CSV fields
 	public static final String CSV_NAME = "Name";
 	public static final String CSV_REVENUE = "Revenue";
 	public static final String CSV_ECPM = "eCPM";
 	public static final String CSV_REQUESTS = "Requests";
 	public static final String CSV_IMPRESSIONS = "Impressions";
 	public static final String CSV_FILL_RATE = "Fill Rate";
 	public static final String CSV_IAD_FILL_RATE = "iAd Fill Rate";
 	public static final String CSV_TTR = "TTR";
 
 	// options
 	public static final String REPORT_ENCODING = "UTF-16";
 
 	public ParserByName(IAdReportInfoDirector director) {
 		super(director);
 	}
 
	@Getter
	@Setter
	private String content;

 	@Override
 	public List<IAdByNameBean> getReportContent() {
 		Map<String, String> columns = new HashMap<String, String>();
 		columns.put(ParserByName.CSV_NAME, ParserByName.NAME);
 		columns.put(ParserByName.CSV_ECPM, ParserByName.ECPM);
 		columns.put(ParserByName.CSV_FILL_RATE, ParserByName.FILL_RATE);
 		columns.put(ParserByName.CSV_IAD_FILL_RATE, ParserByName.IAD_FILL_RATE);
 		columns.put(ParserByName.CSV_IMPRESSIONS, ParserByName.IMPRESSIONS);
 		columns.put(ParserByName.CSV_REQUESTS, ParserByName.REQUESTS);
 		columns.put(ParserByName.CSV_TTR, ParserByName.TTR);
 		columns.put(ParserByName.CSV_REVENUE, ParserByName.REVENUE);
 
 		HeaderColumnNameTranslateMappingStrategy<IAdByNameBean> strat = new HeaderColumnNameTranslateMappingStrategy<IAdByNameBean>();
 		strat.setType(IAdByNameBean.class);
 		strat.setColumnMapping(columns);
 		CsvToBean<IAdByNameBean> csv = new CsvToBean<IAdByNameBean>();
 
 		CSVReader reader = null;
		reader = new CSVReader(new StringReader(content == null ? director.getReportContent() : content));
 
 		List<IAdByNameBean> mappedData;
 		mappedData = csv.parse(strat, reader);
 
 		try {
 			reader.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return mappedData;
 	}
 }
