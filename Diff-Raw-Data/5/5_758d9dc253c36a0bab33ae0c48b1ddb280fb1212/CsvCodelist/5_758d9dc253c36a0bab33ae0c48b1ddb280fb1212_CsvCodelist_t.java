 package org.virtualrepository.csv;
 
 import static org.virtualrepository.Utils.*;
 
 import org.virtualrepository.impl.AbstractType;
 import org.virtualrepository.impl.Type;
 
 /**
  * A {@link CsvAsset} that represents a codelist.
  * 
  * @author Fabio Simeoni
  *
  */
 public class CsvCodelist extends CsvAsset {
 	
 	private static final String name = "csv/codelist";
 	
	private int codeColumn;
 	
 	/**
 	 * The type of {@link CsvCodelist}s.
 	 */
 	public static final Type<CsvCodelist> type = new AbstractType<CsvCodelist>(name) {};
 
 	/**
 	 * Creates an instance with a given identifier, name, and code column.
 	 * 
 	 * @param name the identifier
 	 * @param name the name
 	 * @param column the position of the column that contains the codes of this list.
 	 * @param properties the properties
 	 */
 	public CsvCodelist(String id, String name, int column) {
 		
 		super(type,id, name);
 		
 		notNull("code column position",column);
 		this.codeColumn=column;
 	}
 	
 	/**
 	 * Returns the position of the column that contains the codes of this list.
 	 * 
 	 * @return the code column position
 	 */
	public int codeColumn() {
 
 		return codeColumn;
 
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + codeColumn;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		CsvCodelist other = (CsvCodelist) obj;
 		if (codeColumn != other.codeColumn)
 			return false;
 		return true;
 	}
 	
 	
 }
