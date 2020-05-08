 package com.crispy;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Row implements IJSONConvertible {
 	private HashMap<String, Object> columns;
 	private String table;
 
 	protected Row(ResultSet results) throws SQLException {
 		columns = new HashMap<String, Object>();
 		ResultSetMetaData meta = results.getMetaData();
 		for (int c = 0; c < meta.getColumnCount(); c++) {
 			table = meta.getTableName(c + 1);
 			columns.put(
 					meta.getTableName(c + 1) + "." + meta.getColumnName(c + 1),
 					results.getObject(c + 1));
 		}
 	}
 
 	public String display() {
 		Metadata m = DB.getMetadata(table);
 		if (m.getDisplay() == null)
 			return columnAsString(m.columns.get(0).getName());
 		return columnAsString(m.getDisplay());
 	}
 
 	public Object column(String name) {
 		return columns.get(table + "." + name);
 	}
 
 	public Row table(String table) {
 		this.table = table;
 		return this;
 	}
 
 	public String columnAsString(String name) {
 		Column c = DB.getMetadata(table).getColumn(name);
 		return columnAsString(name, c.def);
 	}
 
 	public String columnAsString(String name, String def) {
 		Object o = column(name);
 		if (o instanceof String)
 			return (String) o;
 		return (o != null) ? o.toString() : def;
 	}
 
 	public String columnAsString(String name, String def, int limit) {
 		String s = columnAsString(name, def);
 		if (s.length() > limit)
 			return s.substring(0, limit) + "...";
 		return s;
 	}
 	
 	public JSONObject columnAsJSONObject(String name) throws JSONException {
 		return new JSONObject(columnAsString(name));
 	}
 
 	public String columnAsString(String name, int limit) {
 		return columnAsString(name, "", limit);
 	}
 
 	public String moneyAsString(String name, String currency) {
 		long money = columnAsLong(name);
 		if (currency.equals("USD")) {
 			if (money == 0)
 				return "";
 			if (money < 1000)
 				return String.format("$%d", money);
 			if (money < 1000000)
 				return String.format("$%.1fK", (money * 1.0f) / 1000);
 			return String.format("$%.1fM", (money * 1.0f) / 1000000);
 		} else if (currency.equals("INR")) {
 			if (money == 0)
 				return "";
 			if (money < 1000)
 				return String.format("%d", money);
 			if (money < 100000)
 				return String.format("%dK", (money) / 1000);
 			if (money < 10000000)
 				return String.format("%dL", (money) / 100000);
 			return String.format("%dCr", (money) / 10000000);
 		}
 		throw new IllegalStateException("Currency not supported");
 	}
 
 	public String dateAsString(String name, String format) {
 		Object o = column(name);
 		if (o instanceof java.sql.Date) {
 			java.sql.Date d = (java.sql.Date) o;
 			SimpleDateFormat sdf = new SimpleDateFormat(format);
 			return sdf.format(new Date(d.getTime()));
 		}
 		if (o == null)
 			return "";
 		return o.toString();
 	}
 
 	public long columnAsLong(String name) {
 		Object o = column(name);
 		if (o == null)
 			return 0;
 		if (o instanceof Number)
 			return (Long) o;
 		return Long.parseLong(o.toString());
 	}
 
 	public int columnAsInt(String name) {
 		Object o = column(name);
 		if (o instanceof Number)
			return (Integer) o;
 		return Integer.parseInt(o.toString());
 	}
 
 	public boolean columnAsBool(String name) {
 		Object o = column(name);
 		if (o instanceof Boolean)
 			return (Boolean) o;
 		if (o instanceof Number) {
 			return ((Integer) o) > 0;
 		}
 		return o != null;
 	}
 
 	public int columnAsInt(String name, int def) {
 		Object o = column(name);
 		if (o == null)
 			return def;
 		if (o instanceof Number)
			return (Integer) o;
 		return Integer.parseInt(o.toString());
 	}
 
 	public Date columnAsDate(String name) {
 		java.sql.Date sqlDate = (java.sql.Date) column(name);
 		return new Date(sqlDate.getTime());
 	}
 
 	@Override
 	public JSONObject toJSONObject() {
 		return new JSONObject(columns);
 	}
 }
