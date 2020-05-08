 package swp_compiler_ss13.common.types;
 
 /**
  * Base Type for type definitions.
  * 
  * @author "Frank Zechert", "Danny Maasch"
  * @version 1
  * @see <a target="_top" href="https://github.com/swp-uebersetzerbau-ss13/common/wiki/Types">Types Wiki</a>
  * @see <a target="_top" href="https://github.com/swp-uebersetzerbau-ss13/common/issues/8">Types Issue Tracker</a>
  */
 public abstract class Type {
 	
 	/**
 	 * Width of this type in bytes.
 	 */
 	protected Long width;
 
 	/**
 	 * Returns the width of this type in bytes.
 	 * @return Number of bytes for this type.
 	 */
	public long getWidth() {
 		return this.width;
 	}
 	
 	/**
 	 * Get the name of the type.
 	 * @return The name of the type.
 	 */
 	public abstract String getTypeName();
 	
 	/**
 	 * Returns a nice string representation of this type
 	 * @return String representation of this type
 	 */
 	public abstract String toString();
 }
