 package org.virtualrepository.csv;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import javax.xml.namespace.QName;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.virtualrepository.impl.PropertyHolder;
 import org.virtualrepository.tabular.Column;
 import org.virtualrepository.tabular.DefaultTable;
 import org.virtualrepository.tabular.Row;
 import org.virtualrepository.tabular.Table;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * A {@link Table} backed up by an {@link InputStream} of CSV data.
  * 
  * @author Fabio Simeoni
  * 
  */
 public class CsvTable extends PropertyHolder implements Table {
 
 	private static final Logger log = LoggerFactory.getLogger(CsvTable.class);
 
 	
 	private final Table inner;
 	private final CsvAsset asset;
 	private final CSVReader reader;
 
 	List<Column> columns =new ArrayList<Column>();
 
 	/**
 	 * Creates an instance for a given {@link CsvAsset} asset and {@link InputStream}.
 	 * 
 	 * @param asset the asset
 	 * @param stream the stream
 	 * 
 	 * @throws IllegalArgumentException if the asset is inconsistently described
 	 */
 	public CsvTable(CsvAsset asset, InputStream stream) {
 		
 		this.asset=asset;
 		
 		this.reader = validateAssetAndBuildReader(asset, stream);
 		
 		RowIterator iterator = new RowIterator();
		
		this.columns = asset.columns();
 
 		inner = new DefaultTable(asset.columns(), iterator);
 	}
 	
 	
 	// helper
 	private CSVReader validateAssetAndBuildReader(CsvAsset asset,InputStream stream) {
 		
 		CSVReader reader = new CSVReader(new InputStreamReader(stream, asset.encoding()),asset.delimiter(),asset.quote());
 
 		List<Column> columns =new ArrayList<Column>();
 		
 		if (asset.hasHeader())
 			try {
 				for (String name : reader.readNext())
 					columns.add(new Column(name));
 			}
 			catch (Exception e) {
 				throw new IllegalArgumentException("invalid CSV asset " + asset.id() + ": cannot read stream",e);
 			}
 		
 		if (!columns.isEmpty())
 			updateColumns(columns);
 		
 		
 		return reader;
 	}
 
 	@Override
 	public Iterator<Row> iterator() {
 		return inner.iterator();
 	}
 	
 	private void updateColumns(List<Column> newColumns) {
 		
 		columns = newColumns;
 		
 		//update asset
 		asset.setColumns(newColumns.toArray(new Column[0]));
 		
 	}
 
 	@Override
 	public List<Column> columns() {
 		return columns;
 	}
 
 	// iterates over rows pulling them from the reader
 	class RowIterator implements Iterator<Row> {
 
 		private final Map<QName, String> data = new HashMap<QName, String>();
 
 		private String[] row;
 		private Throwable error;
 		private int count;
 
 		public boolean hasNext() {
 
 			if (row!=null)
 				return true;
 			
 			if (asset.rows() <= count) {
 				close();
 				return false;
 			}
 
 			try {
 				row = reader.readNext();
 				
 				count++;
 				
 			} catch (IOException e) {
 				error = e;
 			}
 
 			return row != null;
 		}
 
 		public Row next() {
 			
 			try {
 				checkRow();
 			}
 			catch(RuntimeException e) {
 				close();
 			}
 			
 			Row result = buildRow();
 			
 			row=null;
 			
 			return result;
 		}
 
 		// helper
 		private void synthesiseColumns(String[] row) {
 			
 			if (row.length>columns.size()) {
 				List<Column> newcolumns = new ArrayList<Column>();
 				for (int i=0;i<row.length;i++)
 					if (i+1<=columns.size()) 
 						newcolumns.add(columns.get(i));
 					else
 						newcolumns.add(new Column("column-"+(i+1)));
 			
 				updateColumns(newcolumns);
 				
 			}
 		}
 		
 		private void checkRow() {
 
 			if (error != null)
 				throw new RuntimeException(error);
 
 			if (row == null && !this.hasNext()) // reads ahead
 				throw new NoSuchElementException();
 
 		}
 
 		// helper
 		private Row buildRow() {
 
 			data.clear();
 			
 			//invent missing columns based on data evidence
 			synthesiseColumns(row);
 
 			for (int i = 0; i < row.length; i++)
 				data.put(columns.get(i).name(), row[i]);
 
 			return new Row(data);
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 		private void close() {
 			try {
 				reader.close();
 			} catch (Exception e) {
 				log.warn("could not close CSV stream", e);
 			}
 		}
 	}
 	
 	@Override
 	public String toString() {
 		final int maxLen = 100;
 		return "Table [columns="
 				+ (columns != null ? columns.subList(0, Math.min(columns.size(), maxLen)) : null) + ", properties="
 				+ inner.properties() + "]";
 	}
 
 }
