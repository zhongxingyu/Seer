 package org.ocha.hdx.exporter.helper;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public abstract class MultiIndicatorReportRow {
 
 	private final Map<Integer, Map<String, Double>> values = new HashMap<Integer, Map<String, Double>>();
 
 	public void addNewValue(final Integer year, final String indicatorCode, final Double value) {
 		Map<String, Double> yearResults = values.get(year);
 		if (yearResults == null) {
 			yearResults = new HashMap<>();
 			values.put(year, yearResults);
 		}
 		yearResults.put(indicatorCode, value);
 	}
 
 	public Double getValue(final Integer year, final String indicatorCode) {
 		final Map<String, Double> yearResults = values.get(year);
 
 		if (yearResults == null)
 			return null;
 
 		return yearResults.get(indicatorCode);
 	}
 
 }
