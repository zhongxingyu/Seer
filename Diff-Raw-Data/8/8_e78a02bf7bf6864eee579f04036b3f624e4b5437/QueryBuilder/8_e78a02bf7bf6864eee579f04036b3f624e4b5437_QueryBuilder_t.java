 package net.catharos.lib.mysql;
 
 import net.catharos.lib.util.ArrayUtil;
 import net.catharos.lib.util.StringUtil;
 import org.apache.commons.lang.Validate;
 
 
 public class QueryBuilder {
 	/** The table prefix used in queries */
 	private String prefix;
 	
 	
 	public QueryBuilder() {
 		this("");
 	}
 	
 	public QueryBuilder( String prefix ) {
 		setPrefix(prefix);
 	}
 	
 	public String selectAll(String table) {
 		StringBuilder str = new StringBuilder("SELECT ");
 		str.append(" * FROM ").append(getTableName(table));
 		
 		return str.toString();
 	}
 	
 	public String selectAllWhere(String table, String where) {
 		return this.selectWhere(table, "*", where);
 	}
 	
 	public String selectWhere(String table, String what, String where) {		
 		StringBuilder str = new StringBuilder("SELECT ");
 		str.append(what).append(" FROM ").append(getTableName(table));
 		str.append(" WHERE ").append(where);
 		
 		return str.toString();
 	}
 	
 	/** Generates a SQL update string */
	public String update(String table, String[] keys, Object[] values) {
 		Validate.notEmpty(keys);
 		Validate.notEmpty(values);
		Validate.isTrue(keys.length >= values.length);
 		
 		StringBuilder str = new StringBuilder("UPDATE ");
 		str.append(getTableName(table)).append(" SET ");
 		
 		if(values.length == 1) {
 			str.append(StringUtil.getMySQLString(values[0]));
 		} else {
 			for(int i = 0; i < keys.length; i++) {
				String key = keys[i];
 				Object val = values.length < i ? values[i] : null;
 				
 				str.append(key).append("=").append(StringUtil.getMySQLString(val));
 			}
 		}
 		
 		return str.toString();
 	}
 	
 	/** Generates a SQL insert string */
 	public String insert(String table, Object... values) {
 		Validate.notEmpty(values);
 		
 		StringBuilder str = new StringBuilder("INSERT INTO ");
 		str.append(getTableName(table)).append(" VALUES(");
 		
 		String[] val = new String[values.length];
 		for(int i = 0; i < val.length; i++) {
 			val[i] = StringUtil.getMySQLString(values[i]);
 		}
 		
 		return str.append(ArrayUtil.implode(val, ", ")).append(")").toString();
 	}
 	
 	public String createIfNotExists(String table, String[] lines, String suffix) {
 		return createIfNotExists(table, lines, suffix, true);
 	}
 	
 	public String createIfNotExists(String table, String[] lines, String suffix, boolean primary) {
 		StringBuilder str = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
 		str.append(getTableName(table)).append(" (");
 		
 		for(int s = 0; s < lines.length; s++) {
 			String[] split = lines[s].split(" ", 2);
 			
 			split[0] = replaceMagic(StringUtil.getMySQLString(split[0]));
 			str.append(ArrayUtil.implode(split));
 			
 			if(s == 0 && primary) str.append(" NOT NULL PRIMARY KEY");
 			
 			str.append(", ");
 		}
 		
 		return str.append(replaceMagic(suffix)).append(")").toString();
 	}
 	
 	/** Returns the table name including the set prefix */
 	public final String getTableName( String table ) {
 		return prefix + table;
 	}
 	
 	/** Sets the table prefix used in queries */
 	protected final void setPrefix( String prefix ) {
 		this.prefix = prefix;
 	}
 	
 	protected final String replaceMagic( String msg ) {
 		return msg.replace("'", "`");
 	}
 }
