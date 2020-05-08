 package org.ocha.hdx.importer.report;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ocha.hdx.persistence.entity.curateddata.Indicator;
 
 public class ImportReport implements Serializable {
 
 	private static final long serialVersionUID = -7350328255412730901L;
 
 	public ImportReport() {
 		super();
 		this.overallStatus = ImportStatus.SUCCESS;
 		entries = new ArrayList<>();
 	}
 
 	private final ImportStatus overallStatus;
 	private final List<ImportReportEntry> entries;
 
 	public ImportStatus getOverallStatus() {
 		return overallStatus;
 	}
 
 	public boolean isSuccess() {
 		return ImportStatus.SUCCESS.equals(overallStatus);
 	}
 
 	public void addEntry(final ImportStatus status, final String message) {
 		entries.add(new ImportReportEntry(status, message));
 	}
 
 	public void addEntry(final ImportStatus status, final String message, final Indicator indicator) {
		entries.add(new ImportReportEntry(status, message, indicator));
 	}
 
 	public List<ImportReportEntry> getEntries() {
 		return entries;
 	}
 }
