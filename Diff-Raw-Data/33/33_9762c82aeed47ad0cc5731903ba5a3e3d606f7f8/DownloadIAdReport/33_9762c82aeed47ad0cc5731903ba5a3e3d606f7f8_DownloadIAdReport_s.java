 package com.apple.iad.api;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import com.apple.iad.beans.ReportRequestBean;
 import com.apple.iad.domain.IAdReportByNameStory;
 import com.apple.iad.ingestion.IAdReportInfoDirector;
 import com.apple.iad.ingestion.builder.IAdReportByNameBuilder;
 import com.apple.iad.parser.ParserByName;
 import com.apple.iad.parser.ParserByNameMapper;
 
 /**
  * 
  * @author Georgi Lambov
  * 
  */
 public class DownloadIAdReport {
 
 	@Getter
 	@Setter
 	private ReportRequestBean reportRequest;
 
 	@Getter
 	@Setter
 	private IAdReportInfoDirector director = new IAdReportInfoDirector();
 
 	/**
 	 * Retrieves daily iAd reports from certain days back.
 	 * 
 	 * @return
 	 */
 	public List<IAdReportByNameStory> fetchDailyReportsByName() {
 		if (reportRequest.getDaysGoBack() <= 0) {
 			throw new IllegalArgumentException("daysGoback can't be <= 0");
 		}
 
 		if (reportRequest.getFromDate() == null) {
 			throw new IllegalArgumentException("fromDate is requred");
 		}
 
 		director.setReportBuilder(new IAdReportByNameBuilder());
 
 		List<IAdReportByNameStory> stories = new ArrayList<IAdReportByNameStory>();
 
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(reportRequest.getFromDate());
 		calendar.add(Calendar.DATE, 1); // current date and -n day back
 
 		for (int i = 0; i < reportRequest.getDaysGoBack(); i++) {
 			calendar.add(Calendar.DATE, -1);
 			reportRequest.setFromDate(calendar.getTime());
 
 			director.constructReport(reportRequest);
			ParserByNameMapper parserMapper = new ParserByNameMapper(new ParserByName(director));
 
 			IAdReportByNameStory story = new IAdReportByNameStory();
			story.setContent(director.getReportContent());
 			story.setFromDate(calendar.getTime());
 			story.setToDate(calendar.getTime());
 			story.setReports(parserMapper.getReportContent());
 			stories.add(story);
 		}
 
 		return stories;
 	}
 
 	public List<IAdReportByNameStory> fetchDailyReportsByNameWithRange() {
 		// TODO implementation
 		return null;
 	}
 
 }
