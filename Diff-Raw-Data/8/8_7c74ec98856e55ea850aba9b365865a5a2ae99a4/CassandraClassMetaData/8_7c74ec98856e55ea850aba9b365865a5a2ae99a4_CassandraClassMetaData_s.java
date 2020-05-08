 /**
  * 
  */
 package com.datastax.hectorjpa.store;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.openjpa.meta.ClassMetaData;
 import org.apache.openjpa.meta.MetaDataRepository;
 import org.apache.openjpa.meta.ValueMetaData;
 
 import com.datastax.hectorjpa.index.IndexDefinitions;
 
 /**
  * Meta data for classes specific to Cassandra
  * 
  * @author Todd Nine
  * 
  */
 public class CassandraClassMetaData extends ClassMetaData {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7584475014214868856L;
 
 	private String discriminatorColumn;
 
 	private String columnFamily;
 
 	private IndexDefinitions indexDefinitions;
 	
 	private IndexDefinitions allDefinitions;
 
 	private String[] superClassDiscriminators;
 	
 	private boolean mappedSuperClass = false;
 
 	protected CassandraClassMetaData(Class<?> type, MetaDataRepository repos) {
 		super(type, repos);
 	}
 
 	public CassandraClassMetaData(ValueMetaData owner) {
 		super(owner);
 	}
 
 	/**
 	 * Get the discriminator value. Null if one hasn't been set
 	 * 
 	 * @return
 	 */
 	public String getDiscriminatorColumn() {
 		return discriminatorColumn;
 	}
 
 	/**
 	 * Set the discriminator column
 	 * 
 	 * @param discriminatorColumn
 	 */
 	public void setDiscriminatorColumn(String discriminatorColumn) {
 		this.discriminatorColumn = discriminatorColumn;
 	}
 
 	public String getColumnFamily() {
 		return columnFamily;
 	}
 
 	public void setColumnFamily(String columnFamily) {
 		this.columnFamily = columnFamily;
 	}
 
 	/**
 	 * The index definitions defined on this class only
 	 * @return
 	 */
 	public IndexDefinitions getIndexDefinitions() {
 		return indexDefinitions;
 	}
 
 	/**
 	 * @param indexDefinitions
 	 */
 	public void setIndexDefinitions(IndexDefinitions indexDefinitions) {
 		this.indexDefinitions = indexDefinitions;
 	}
 
 	/**
    * @return the mappedSuperClass
    */
   public boolean isMappedSuperClass() {
     return mappedSuperClass;
   }
 
   /**
    * @param mappedSuperClass the mappedSuperClass to set
    */
   public void setMappedSuperClass(boolean mappedSuperClass) {
     this.mappedSuperClass = mappedSuperClass;
   }
 
   /**
 	 * Get a string array of all discriminators starting at the current class.  This is a lazy init and should not be invoked until all 
 	 * meta data has been parsed
 	 * 
 	 * @return
 	 */
 	public String[] getSuperClassDiscriminators() {
 
 		if (this.superClassDiscriminators != null) {
 			return this.superClassDiscriminators;
 		}
 
 		List<String> subclasses = new ArrayList<String>();
 
 		CassandraClassMetaData current = this;
 
 		do {
 			// TODO TN, should probably throw a metadata exception here
 			if (current.getDiscriminatorColumn() != null) {
 				subclasses.add(current.getDiscriminatorColumn());
 			}
 
 			current = (CassandraClassMetaData) current
 					.getPCSuperclassMetaData();
 		} while (current != null);
 
 		String[] subArray = new String[subclasses.size()];
 
 		subclasses.toArray(subArray);
 
 		this.superClassDiscriminators = subArray;
 		
 		return this.superClassDiscriminators;
 
 	}
 
 	/**
 	 * Return the current index definition combined with all parent index definitions.  Lazy operation.  Should not be invoked until all meta data is loaded
 	 * @return
 	 */
 	public IndexDefinitions getAllDefinitions(){
 		if(allDefinitions != null){
 			return allDefinitions;
 		}
 		
 		synchronized (this) {
 		  if(allDefinitions == null){
		    allDefinitions = new IndexDefinitions();
     		
     		CassandraClassMetaData current = this;
     
     		do {
     			// TODO TN, should probably throw a metadata exception here
     			if (current.getIndexDefinitions() != null) {
    			  allDefinitions.getDefinitions().addAll(current.getIndexDefinitions().getDefinitions());
     			}
     
     			
     			current = (CassandraClassMetaData) current
     					.getPCSuperclassMetaData();
     		} while (current != null);
     	}
 		  return allDefinitions;
 		}
 	}
 	
 	
 }
