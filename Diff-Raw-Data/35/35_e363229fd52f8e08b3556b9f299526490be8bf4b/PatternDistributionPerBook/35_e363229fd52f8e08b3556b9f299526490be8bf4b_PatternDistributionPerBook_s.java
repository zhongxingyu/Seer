 package com.hidden.data.web.service.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.hidden.data.nosql.model.discovery.BookDiscovery;
 
 class PatternDistributionPerBook {
 
 	private static final int THRESHOLD_NUMBER = 10;
 	private List<Double> thresholdList = new ArrayList<Double>();
 	private List<Integer> patternsPerThresholdList = new ArrayList<Integer>();
 	private int bookTotalLines;
 	private List<BookDiscovery> sortedPatternsDistribution;
 
 	PatternDistributionPerBook(int bookTotalLines) {
 		this.bookTotalLines = bookTotalLines;
 	}
 
 	public List<Integer> getPatternsPerPercentageThreshold() {
 		return patternsPerThresholdList;
 	}
 
 	public void setPatternThreshold(List<BookDiscovery> patternsPerBook) {
 		initiateThresholdList();
 		setSortedPatternsDistribution(patternsPerBook);
 		setPatternDistributionIntoThresholdList();
 	}
 
 	private void initiateThresholdList() {
 		double bookLinesThreshold = bookTotalLines / THRESHOLD_NUMBER;
 		for (double i = bookLinesThreshold; i <= bookTotalLines; i += bookLinesThreshold) {
 			thresholdList.add(i);
 		}
 	}
 
	private void setSortedPatternsDistribution(List<BookDiscovery> patternsPerBook) {
		sortedPatternsDistribution = new ArrayList<BookDiscovery>(patternsPerBook);
 		Collections.sort(sortedPatternsDistribution,
 				new PatternDistributionComparator());
 	}
 
 	private void setPatternDistributionIntoThresholdList() {
 		int currentThresholdIndex = 0;
 		int currentThresholdCount = 0;
 		for (BookDiscovery bookDiscovery : sortedPatternsDistribution) {
 			while (bookDiscovery.getFirstPatternLineNumber() > thresholdList
 					.get(currentThresholdIndex)) {
				patternsPerThresholdList.add(currentThresholdCount);
 				currentThresholdIndex++;
 				currentThresholdCount = 0;
 			}
 			currentThresholdCount++;
 		}
		if (patternsPerThresholdList.size() < currentThresholdIndex + 1) {
			patternsPerThresholdList.add(currentThresholdCount);
 		}
 	}
 }
