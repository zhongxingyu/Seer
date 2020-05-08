 package org.jyald.loggingmodel;
 
import org.jyald.debuglog.Log;
 import org.jyald.uicomponents.TabContent;
 
 public class FilteredLogSlot {
 	private String slotName;
 	private EntryList entries;
 	private FilterList filters;
 	private TabContent loggerUi;
 	private boolean disposed;
 	
 	public FilteredLogSlot(String name, FilterList filterList) {
 		entries = new EntryList();
 		slotName = name;
 		filters = filterList;
 		disposed = false;
 	}
 	
 	private void checkDisposed() throws Exception {
 		if (disposed) 
 			throw new Exception("Object was disposed! Please create a new instance");
 		
 	}
 	
 	public final String getSlotName() {
 		return slotName;
 	}
 	
 	public final int getFilterCount() throws Exception {
 		checkDisposed();
 		
 		if (filters != null)
 			return filters.getCount();
 		
 		return -1;
 	}
 	
 	public void linkUi(TabContent ui) throws Exception {
 		checkDisposed();
 		
 		loggerUi = ui;
 	}
 	
 	public TabContent getLoggerUi() {
 		return loggerUi;
 	}
 	
 	
 	public boolean tryAdd(LogEntry entry) throws Exception {
 		checkDisposed();
 		
 		if (filters == null) 
 			return false;
 		
 		if (filters.match(entry)) {
 			loggerUi.writeLog(entry);
 			entries.addEntry(entry);
			return true;
 		}
 		
		return false;
 	}
 	
 	public void dispose() throws Exception {
 		checkDisposed();
 		
 		filters.clear();
 		filters = null;
 		entries.clear();
 		entries = null;
 		
 		loggerUi.dispose();
 		loggerUi = null;
 		
 		disposed = true;
 	}
 	
 }
