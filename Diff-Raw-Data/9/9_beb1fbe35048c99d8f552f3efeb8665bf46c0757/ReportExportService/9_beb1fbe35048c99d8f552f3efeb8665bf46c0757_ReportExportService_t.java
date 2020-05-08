 package org.chai.kevin.reports;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.LanguageService;
 import org.chai.kevin.Period;
 import org.chai.kevin.data.Calculation;
 import org.chai.kevin.util.DataUtils;
 import org.chai.kevin.util.Utils;
 import org.chai.kevin.value.DataValue;
 import org.chai.kevin.value.Value;
 import org.chai.location.CalculationLocation;
 import org.chai.location.Location;
 import org.chai.location.LocationLevel;
 import org.chai.location.LocationService;
 import org.springframework.transaction.annotation.Transactional;
 import org.supercsv.io.CsvListWriter;
 import org.supercsv.io.ICsvListWriter;
 import org.supercsv.prefs.CsvPreference;
 
 public class ReportExportService {
 	
 	private static final Log log = LogFactory.getLog(ReportExportService.class);
 	
 	private LanguageService languageService;
 	private LocationService locationService;
 	private Set<String> skipLevels;
 	
 	public void setLanguageService(LanguageService languageService) {
 		this.languageService = languageService;
 	}
 	
 	public void setLocationService(LocationService locationService) {
 		this.locationService = locationService;
 	}
 	
 	public void setSkipLevels(Set<String> skipLevels) {
 		this.skipLevels = skipLevels;
 	}
 	
 	private List<LocationLevel> getSkipLevelList() {
 		List<LocationLevel> result = new ArrayList<LocationLevel>();
 		for (String level : skipLevels) {
 			result.add(locationService.findLocationLevelByCode(level));
 		}
 		return result;
 	}
 	
 	public List<LocationLevel> getLevels() {
 		List<LocationLevel> result = locationService.listLevels();
 		for (String level : skipLevels) {
 			result.remove(locationService.findLocationLevelByCode(level));
 		}
 		return result;
 	}
 	
 	private final static String CSV_FILE_EXTENSION = ".csv";
 
 	private final static String LOCATION_HEADER = "Location";
	private final static String LOCATIONS_HEADER = "Locations";
 	
 	private String[] getExportDataHeaders(List<AbstractReportTarget> indicators) {
 		List<String> headers = new ArrayList<String>();		
 		headers.add(LOCATION_HEADER);
		headers.add(LOCATIONS_HEADER);
 		for (AbstractReportTarget indicator : indicators) {
 			headers.add(DataUtils.noNull(indicator.getNames()));
 		}
 		return headers.toArray(new String[0]);
 	}	
 	
 	public String getReportExportFilename(String report, CalculationLocation location, ReportProgram program, Period period){
 		String exportFilename = report + "_" + period.getCode() + "_" + program.getCode() + "_" + location.getCode() + "_";
 		return exportFilename;
 	}
 	
 	@Transactional(readOnly=true)
 	public File getReportExportFile(String filename, ReportTable reportTable, CalculationLocation currentLocation) throws IOException { 
 		
 		File csvFile = File.createTempFile(filename, CSV_FILE_EXTENSION);
 		
 		FileWriter csvFileWriter = new FileWriter(csvFile);
 		ICsvListWriter writer = new CsvListWriter(csvFileWriter, CsvPreference.EXCEL_PREFERENCE);
 		try {
 			String[] csvHeaders = null;
 			List<AbstractReportTarget> indicators = reportTable.getIndicators();
 			
 			// headers
 			if(csvHeaders == null){
 				csvHeaders = getExportDataHeaders(indicators);
 				writer.writeHeader(csvHeaders);
 			}
 			
 			exportLocation(reportTable, writer, indicators, currentLocation);
 													
 		} catch (IOException ioe){
 			// TODO is this good ?
 			throw ioe;
 		} finally {
 			writer.close();
 		}
 		
 		return csvFile;
 	}
 
 	private void exportLocation(ReportTable reportTable, ICsvListWriter writer,
 			List<AbstractReportTarget> indicators, CalculationLocation location)
 			throws IOException {
 		if (log.isDebugEnabled()) log.debug("getReportExportFile(location="+location+")");
 		
 		ArrayList<String> reportExportRow = new ArrayList<String>();
 		
		//Location
		reportExportRow.add(DataUtils.noNull(location.getNames()));

 		//Locations
 		List<String> rowLocations = new ArrayList<String>();
 		for (LocationLevel level : getLevels()){			
 			Location parent = location.getParentOfLevel(level);
 			if (parent != null && !parent.equals(location)){
 				rowLocations.add(DataUtils.noNull(parent.getNames()));
 			}
 		}
 		String locationNames = StringUtils.join(rowLocations, "-");
 		reportExportRow.add(locationNames);
 		
 		//Indicators
 		for (AbstractReportTarget indicator: indicators){
 			DataValue value = reportTable.getTableReportValue(location, indicator);
 			if(value != null && !value.getValue().isNull())
 				reportExportRow.add(Utils.getStringValue(value.getValue(), indicator.getType()));
 			else{
 				reportExportRow.add("N/A");
 			}
 		}
 		
 		writer.write(reportExportRow);
 		
 		if (location instanceof Location) {
 			List<CalculationLocation> locations = reportTable.getLocations((Location)location, new HashSet(getSkipLevelList()), new HashSet(locationService.listTypes()));
 			for (CalculationLocation calculationLocation : locations) {
 				exportLocation(reportTable, writer, indicators, calculationLocation);
 			}
 		}
 	}
 }
