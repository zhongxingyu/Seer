 package database;
 
 import java.util.List;
 import java.io.Serializable;
 import relationenalgebra.*;
 
 public class Table implements Serializable {
 
 	static final long serialVersionUID = 1234;
 	protected static String databaseDirectory;
 	protected String name;
 	protected String alias;
 	protected boolean drop;
 	protected List<String> columnNames;
 	protected List<List<String>> rows;
 
 	/**
 	 * Loads a Table from dir by its name.
 	 */
 	public static Table loadTable(String name) {
 		// TODO implement this
 		return null;
 	}
 
 	/**
 	 *  Writes the actual instance to the filesystem.
 	 */
 	public void write() {
 		// TODO implement this
 	}
 
 	/**
 	 *  Returns one row from privat row repository.
 	 */
 	public List<String> getRow(int number) {
 		// TODO implement this
 		return null;
 	}
 
 	public void addRow(List<String> names) {
 		// TODO implement this
 	}
 
 	public void deleteRow(int number) {
 		// TODO implement this
 	}
 
 	public Table projectTo(List<String> param) {
 		// TODO implement this
 		return null;
 	}
 
 	public Table select(AndExpression exp) {
 		// TODO implement this
 		return null;
 	}
 
 	public Table join(Table table, AndExpression exp) {
 		// TODO implement this
 		return null;
 	}
 
 	public Table cross(Table table) {
 		// TODO implement this
 		return null;
 	}
 
 	public String toString() {
 		// TODO implement this
		String s = "table '" + this.name + "' with columns '";
		for (String c : this.columnNames) {
			s += c + ", ";
		}
		s += "'";
		return s;
 	}
 
 }
