 package org.commacq;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.springframework.jmx.export.annotation.ManagedAttribute;
 import org.springframework.jmx.export.annotation.ManagedOperation;
 import org.springframework.jmx.export.annotation.ManagedResource;
 
 /**
  * Defines a set of CsvDataSources. Sources within the same layer
  * can be updated together (transactionally). An example of the use
  * of layers is creating a cache CsvDataSource for each "real"
  * data source in a layer.
  */
 @ManagedResource
 public class CsvDataSourceLayerCollection implements CsvDataSourceLayer {
 	
 	private ThreadLocal<CsvLineCallbackSingleImpl> csvLineCallbackSingleImplLocal = new ThreadLocal<CsvLineCallbackSingleImpl>() {
 		protected CsvLineCallbackSingleImpl initialValue() {
 			return new CsvLineCallbackSingleImpl();
 		};
 	};
 
 	private final SortedSet<String> entityIds = new TreeSet<String>();
 	//Unmodifiable wrapper to give to clients
 	private final SortedSet<String> entityIdsUnmodifiable = Collections.unmodifiableSortedSet(entityIds);
 	
 	private final Map<String, CsvDataSource> csvDataSourceMap = new HashMap<String, CsvDataSource>();
 	//Unmodifiable wrapper to give to clients
 	private final Map<String, CsvDataSource> csvDataSourceMapUnmodifiable = Collections.unmodifiableMap(csvDataSourceMap);	
 	
 	public CsvDataSourceLayerCollection(Collection<? extends CsvDataSource> csvDataSources) {
 		for(CsvDataSource source : csvDataSources) {
 			boolean newEntry = entityIds.add(source.getEntityId());
 			if(!newEntry) {
 				throw new RuntimeException("Duplicate entityId in layer: " + source.getEntityId());
 			}
 			csvDataSourceMap.put(source.getEntityId(), source);
 		}
 	}
 	
 	@Override
 	@ManagedAttribute
 	public SortedSet<String> getEntityIds() {
 		return entityIdsUnmodifiable;
 	}
 	
 	@Override
 	@ManagedOperation
 	public String getCsvEntry(String entityId, String id) {
 		CsvLineCallbackSingleImpl callback = csvLineCallbackSingleImplLocal.get();  
 	    getCsvDataSource(entityId).getCsvLine(id, callback);
 	    return callback.getCsvLineAndClear().getCsvLine();
 	}
 	
 	@Override
 	@ManagedOperation
 	public String pokeCsvEntry(String entityId, String id) throws CsvUpdateBlockException {
 		CsvDataSource source = getCsvDataSource(entityId);
 		if(!(source instanceof CsvUpdatableDataSource)) {
 			throw new RuntimeException("Not an updatable data source: " + entityId);
 		}
 		
 		CsvUpdatableDataSource updatableDataSource = (CsvUpdatableDataSource)source;
 		updatableDataSource.startUpdateBlock(source.getColumnNamesCsv());
 		updatableDataSource.updateUntrusted(id);
 		updatableDataSource.finishUpdateBlock();
 		
 		return getCsvEntry(entityId, id);
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.commacq.CsvDataSourceLayerI#getCsvDataSource(java.lang.String)
 	 */
 	@Override
 	public CsvDataSource getCsvDataSource(String entityId) {
 		return csvDataSourceMap.get(entityId);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.commacq.CsvDataSourceLayerI#getMap()
 	 */
 	@Override
 	public Map<String, CsvDataSource> getMap() {
 		return csvDataSourceMapUnmodifiable;
 	}
 	
 	@Override
 	@ManagedOperation
 	public void reload(String entityId) throws CsvUpdateBlockException {
 		CsvDataSource csvDataSource = getMap().get(entityId);
		if(csvDataSource == null) {
			throw new IllegalArgumentException("entityId " + entityId + " not recognised");
		}
 		if(csvDataSource instanceof CsvUpdatableDataSource) {
 			((CsvUpdatableDataSource)csvDataSource).reload();
		} else {
			throw new IllegalArgumentException("entityId " + entityId + " not updatable");
 		}
 	}
 	
 	@Override
 	@ManagedOperation
 	public void reloadAll() throws CsvUpdateBlockException {
 		for(String entityId : entityIds) {
 			reload(entityId);
 		}
 	}
 	
 }
