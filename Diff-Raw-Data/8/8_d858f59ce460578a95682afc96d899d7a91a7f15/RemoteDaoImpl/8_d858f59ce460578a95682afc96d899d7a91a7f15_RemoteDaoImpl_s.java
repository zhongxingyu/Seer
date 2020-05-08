 package org.db4a.dao.impl;
 
 import java.lang.reflect.Field;
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.db4a.annotation.Column;
 import org.db4a.annotation.Id;
 import org.db4a.annotation.Table;
 import org.db4a.dao.BaseDao;
 import org.db4a.util.RemoteDBHelper;
 import org.db4a.util.NativeTableHelper;
 
 import android.util.Log;
 
 public class RemoteDaoImpl<T> implements BaseDao<T> {
 	private static final String TAG = "db4a";
 	
 	private String tableName;
 	private String idColumn;
 	private boolean isAutoId = false;
 	private Class<T> clazz;
 	private List<Field> allFields;
 	RemoteDBHelper rdb;
 
 	@SuppressWarnings("unchecked")
 	public RemoteDaoImpl(RemoteDBHelper rdb) {
 		this.rdb = rdb;
 		this.clazz = ((Class<T>) ((java.lang.reflect.ParameterizedType) super
 				.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
 
 		if (this.clazz.isAnnotationPresent(Table.class)) {
 			Table table = (Table) this.clazz.getAnnotation(Table.class);
 			this.tableName = table.name();
 		}
 		this.allFields = NativeTableHelper.joinFields(this.clazz.getDeclaredFields(),
 				this.clazz.getSuperclass().getDeclaredFields());
 		for (Field field : this.allFields) {
 			if (field.isAnnotationPresent(Id.class)) {
 				Column column = (Column) field.getAnnotation(Column.class);
 				this.idColumn = column.name();
 				Id id = (Id) field.getAnnotation(Id.class);
 				isAutoId = id.isAuto();
 				break;
 			}
 		}
 		Log.d(TAG, "clazz:" + this.clazz + " tableName:" + this.tableName
 				+ " idColumn:" + this.idColumn);
 	}
 
 	@SuppressWarnings("rawtypes")
 	public long insert(T entity) {
 		long flag = -1L;
 		if (entity == null) {
 			return flag;
 		}
 		String sql = getInsertSQL();
 		Log.i(TAG, sql);
 		List list = getValueList(entity);
 		Connection con = null;
 		try {
 			con = this.rdb.getConnection();
 			PreparedStatement stmt = con.prepareStatement(sql);
 			for (int i = 1; i <= list.size(); i++) {
 				stmt.setObject(i, list.get(i - 1));
 			}
 			flag = stmt.executeUpdate();
 			if (con != null) {
 				con.close();
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "[rawQuery] from DB Exception");
 			e.printStackTrace();
 		}
 		return flag;
 	}
 
 	public void delete(int id) {
 		String sql="DELETE FROM "+tableName+" WHERE "+idColumn+" ='"+id+"'";
 		execSql(sql, null);
 	}
 
 	public void delete(Integer... ids) {
 		if (ids.length > 0) {
 			StringBuffer sb = new StringBuffer();
 			for (int i = 0; i < ids.length; i++) {
 				sb.append('?').append(',');
 			}
 			sb.deleteCharAt(sb.length() - 1);
 			String sql = "delete from " + this.tableName + " where "
 					+ this.idColumn + " in (" + sb + ")";
 			Log.d(TAG,"[delete]: " + sql);
 			execSql(sql, (Object[]) ids);
 		}
 	}
 
 	public void update(T entity) {
 		if (entity == null) {
 			return;
 		}
 		String sql = getUpdateSql(entity);
 		execSql(sql, null);
 	}
 
 	private String getUpdateSql(T entity) {
 		StringBuffer sql = new StringBuffer();
 		  sql.append("UPDATE ");
 		  sql.append(tableName);
 		  sql.append(" SET ");
 		  String id="";
 		  for (int i = 0; i < allFields.size(); i++) {
 			Field field = allFields.get(i);
 			Column column = (Column) field.getAnnotation(Column.class);
 			field.setAccessible(true);
 			if (column.name().equals(idColumn)) {
 				try {
 					id=""+ field.get(entity);
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				}
 				continue;
 			}
 		   sql.append(column.name());
 		   try {
 			sql.append("='"+field.get(entity)+"'");
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		   if (i < allFields.size() - 1) {
 		    sql.append(",");
 		   }
 		  }
 		  sql.append(" WHERE ");
 		  sql.append(idColumn);
 		  sql.append("="+id);
 		  return sql.toString();
 	}
 
 	public T get(int id) {
 		String selection = this.idColumn + " = ?";
 		String[] selectionArgs = { Integer.toString(id) };
 		List<T> list = find(null, selection, selectionArgs, null, null, null,
 				null);
 		if ((list != null) && (list.size() > 0)) {
 			return (T) list.get(0);
 		}
 		return null;
 	}
 
 	public List<T> rawQuery(String sql, String[] selectionArgs) {
 		String s = getCommonSQL(sql, selectionArgs);
 		List<T> lst = new ArrayList<T>();
 		Log.d(TAG, s);
 		Connection con = null;
 		try {
 			con = this.rdb.getConnection();
 			Statement stmt = con.createStatement();
 			ResultSet rset = stmt.executeQuery(s);
 			getListFromResultSet(lst, rset);
 			if (con != null) {
 				con.close();
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "[rawQuery] from DB Exception");
 			e.printStackTrace();
 		}
 		return lst;
 	}
 
 	public List<T> find() {
 		return find(null, null, null, null, null, null, null);
 	}
 
 	@Deprecated
 	/**
 	 * not support in this vision
 	 */
 	public List<Map<String, String>> query2MapList(String sql,
 			String[] selectionArgs) {
 		return null;
 	}
 
 	public void execSql(String sql, Object[] selectionArgs) {
 		String s = getCommonSQL(sql, selectionArgs);
 		Log.e(TAG, s);
 		Connection con = null;
 		try {
 			con = this.rdb.getConnection();
 			Statement stmt = con.createStatement();
 			stmt.execute(s);
 			if (con != null) {
 				con.close();
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "[execSql] from DB Exception");
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public List<T> find(String[] columns, String selection,
 			String[] selectionArgs, String groupBy, String having,
 			String orderBy, String limit) {
 		String sql = getSelectSQL();
 		sql = sql.replace("*", getColumnsSQL(columns));
 		sql += " WHERE " + getSelectionArgs(selection, selectionArgs);
 		sql += getPariBySQL("GROUP BY", groupBy);
 		sql += getPariBySQL("HAVEING", having);
 		sql += getPariBySQL("ORDER BY", orderBy);
 		sql += getPariBySQL("", limit);
		if (sql.endsWith(" WHERE ")) {
			sql = sql.substring(0, sql.lastIndexOf(" WHERE")) + ";";
		}
 		Log.i(TAG, sql);
 		List<T> list = new ArrayList<T>();
 		Connection con = null;
 		try {
 			con = this.rdb.getConnection();
 			Statement stmt = con.createStatement();
 			ResultSet rst = stmt.executeQuery(sql);
 			getListFromResultSet(list, rst);
 			if (con != null) {
 				con.close();
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "[find] from DB Exception");
 			e.printStackTrace();
 		}
 		return list;
 	}
 
 	private void getListFromResultSet(List<T> list, ResultSet rs)
 			throws IllegalAccessException, InstantiationException,
 			IllegalArgumentException, SecurityException, SQLException {
 		while (rs.next()) {
 			T entity = this.clazz.newInstance();
 
 			for (Field field : this.allFields) {
 				Column column = null;
 				if (field.isAnnotationPresent(Column.class)) {
 					column = (Column) field.getAnnotation(Column.class);
 
 					field.setAccessible(true);
 					Class<?> fieldType = field.getType();
 					int c = -1;
 					try {
 						c = rs.findColumn(column.name());
 					} catch (SQLException e) {
 						continue;
 					} finally {
 
 					}
 					if (c < 0) {
 						continue; // ѭ¸ֵ
 					} else if ((Integer.TYPE == fieldType)
 							|| (Integer.class == fieldType)) {
 						field.set(entity, rs.getInt(c));
 					} else if (String.class == fieldType) {
 						field.set(entity, rs.getString(c));
 					} else if ((Long.TYPE == fieldType)
 							|| (Long.class == fieldType)) {
 						field.set(entity, Long.valueOf(rs.getLong(c)));
 					} else if ((Float.TYPE == fieldType)
 							|| (Float.class == fieldType)) {
 						field.set(entity, Float.valueOf(rs.getFloat(c)));
 					} else if ((Short.TYPE == fieldType)
 							|| (Short.class == fieldType)) {
 						field.set(entity, Short.valueOf(rs.getShort(c)));
 					} else if ((Double.TYPE == fieldType)
 							|| (Double.class == fieldType)) {
 						field.set(entity, Double.valueOf(rs.getDouble(c)));
 					} else if (Blob.class == fieldType) {
 						field.set(entity, rs.getBlob(c));
 					} else if (byte[].class == fieldType) {
 						field.set(entity, rs.getBlob(c));
 					} else if (Character.TYPE == fieldType) {
 						String fieldValue = rs.getString(c);
 
 						if ((fieldValue != null) && (fieldValue.length() > 0)) {
 							field.set(entity,
 									Character.valueOf(fieldValue.charAt(0)));
 						}
 					}
 				}
 			}
 
 			list.add((T) entity);
 		}
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private List getValueList(T obj) {
 		List res = new ArrayList();
 		for (Field field : this.allFields) {
 			if (field.isAnnotationPresent(Column.class)) {
 				field.setAccessible(true);
 				if (field.isAnnotationPresent(Id.class) && isAutoId) {
 					continue;
 				}
 				try {
 					res.add(field.get(obj));
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return res;
 	}
 
 	private String getSelectSQL() {
 		StringBuffer sql = new StringBuffer();
 		sql.append("SELECT * FROM ");
 		sql.append("" + tableName + " ");
 		return sql.toString();
 	}
 
 	private String getColumnsSQL(String columns[]) {
 		if (columns == null || columns.length == 0) {
 			return "*";
 		}
 		StringBuffer sql = new StringBuffer();
 		for (String c : columns) {
 			sql.append(c + ",");
 		}
 		return sql.substring(0, sql.length() - 1);
 	}
 
 	private String getSelectionArgs(String selection, String[] selectionArgs) {
 		if (selection == null || selection.length() == 0) {
 			return "";
 		}
 		StringBuffer sb = new StringBuffer();
 		if (selectionArgs == null || selectionArgs.length == 0) {
 			return selection;
 		}
 		sb.append(selection);
 		int index = 0;
 		while (sb.indexOf("?") > 0 && index < selectionArgs.length) {
 			sb.replace(sb.indexOf("?"), sb.indexOf("?") + 1, "'"
 					+ selectionArgs[0] + "'");
 			index++;
 		}
 		sb.append(" ");
 		return sb.toString().replace("?", "*");
 	}
 
 	private String getPariBySQL(String SQL, String value) {
 		if (value == null || value.length() == 0) {
 			return "";
 		} else {
 			return SQL + " " + value + " ";
 		}
 	}
 
 	private String getInsertSQL() {
 		StringBuffer sql = new StringBuffer();
 		sql.append("INSERT INTO ");
 		sql.append(tableName);
 		sql.append(" (");
 		for (int i = 0; i < allFields.size(); i++) {
 			Field field = allFields.get(i);
 			Column column = (Column) field.getAnnotation(Column.class);
 			field.setAccessible(true);
 			String cname = column.name();
 			if (cname.equals(idColumn) && isAutoId) {
 				continue;
 			}
 			sql.append("" + cname + "");
 			if (i < allFields.size() - 1) {
 				sql.append(",");
 			}
 		}
 		sql.append(") ");
 		sql.append(" VALUES(");
 		for (int i = 0; i < allFields.size(); i++) {
 			Field field = allFields.get(i);
 			Column column = (Column) field.getAnnotation(Column.class);
 			field.setAccessible(true);
 			String cname = column.name();
 			if (cname.equals(idColumn) && isAutoId) {
 				continue;
 			}
 			sql.append("?");
 			if (i < allFields.size() - 1) {
 				sql.append(",");
 			}
 		}
 		sql.append(") ");
 		return sql.toString();
 	}
 
 	private String getCommonSQL(String sql, Object[] selectionArgs) {
 		if (sql == null || sql.length() == 0) {
 			return "";
 		}
 		StringBuffer sb = new StringBuffer();
 		if (selectionArgs == null || selectionArgs.length == 0) {
 			return sql;
 		}
 		sb.append(sql);
 		int index = 0;
 		while (sb.indexOf("?") > 0 && index < selectionArgs.length) {
 			sb.replace(sb.indexOf("?"), sb.indexOf("?") + 1, "'"
 					+ selectionArgs[index] + "'");
 			index++;
 		}
 		sb.append(" ");
 		return sb.toString().replace("?", "*");
 	}
 
 }
