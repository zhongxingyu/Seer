 package org.opensixen.model;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.math.BigDecimal;
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLData;
 import java.sql.SQLException;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 
 import org.compiere.model.PO;
 import org.compiere.model.POInfo;
 import org.compiere.util.CLogger;
 import org.compiere.util.CPreparedStatement;
 import org.compiere.util.DB;
 import org.compiere.util.DisplayType;
 import org.compiere.util.Env;
 import org.opensixen.util.NameObjectPair;
 
 import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;
 
 public class POFactory {
 
 		
 	private static CLogger s_log = CLogger.getCLogger(POFactory.class);
 	
 	private static final Object NULL = new Object();
 	
 	
 	private static void setColToStatement(POInfo p_info, CPreparedStatement pp, int statementColumnIndex, String columnName, int columnIndex, int dt, Class c, Object value) throws SQLException
 	{
 		if (value == null || value.equals (NULL)) {
 			pp.setNull(statementColumnIndex, Types.NULL);
 			return ;
 		}
 		
 		if (DisplayType.isLookup(dt)) 
 		{
 			ResultSet rs = pp.getConnection().getMetaData().getColumns(null, null, p_info.getTableName().toLowerCase(), p_info.getColumnName(columnIndex).toLowerCase());
 			if (rs.next()) { 
 				int jdbcType = rs.getInt("DATA_TYPE");
 				int colSize = rs.getInt("COLUMN_SIZE");
 				Object x = CastToJdbcType(value, jdbcType);
 				
 				if (x.getClass() == String.class && x.toString().length() > colSize)
 					s_log.log(Level.SEVERE, "length() > colSize");
 				
 				pp.setObject(statementColumnIndex, x, jdbcType);
 				return;
 			}
 		}		
 		
 		if(dt == DisplayType.Binary)
 			pp.setBytes(statementColumnIndex,(byte[])value);
 		else if (c == Object.class) //  may have need to deal with null values differently
 			pp.setString(statementColumnIndex, value.toString());
 		else if (value instanceof Integer)
 			pp.setInt(statementColumnIndex, ((Integer)value).intValue());
 		else if (value instanceof BigDecimal)
 			pp.setBigDecimal(statementColumnIndex, (BigDecimal) value);
 		else if (c == Boolean.class)
 		{
 			boolean bValue = false;
 			if (value instanceof Boolean)
 				bValue = ((Boolean)value).booleanValue();
 			else
 				bValue = "Y".equals(value);
 			pp.setString(statementColumnIndex, (bValue ? "Y" : "N"));
 		}
 		else if (value instanceof Timestamp)
 			pp.setTimestamp(statementColumnIndex, (Timestamp)value); // ((DB.TO_DATE ((Timestamp)value, p_info.getColumnDisplayType (columnIndex) == DisplayType.Date)));
 		else if (c == String.class)
 			pp.setString(statementColumnIndex, (String)value);
 		else if (DisplayType.isLOB(dt))
 			pp.setNull(statementColumnIndex, Types.NULL);		//	no db dependent stuff here
 		else
 			pp.setString(statementColumnIndex, value.toString());
 			
 	}
 	
 
 		
 	private static void setColToStatement(POInfo p_info, CPreparedStatement pp, int statementColumnIndex, QueryParam param) throws SQLException
 	{
 		setColToStatement(
 				p_info,
 				pp, 
 				statementColumnIndex, 
 				param.getColumnName(), 
 				param.getColumnIndex(),
 				param.getColumnDataType(),
 				param.getValueClass(),
 				param.getValue());
 	}
 
 	private static Object CastToJdbcType(Object value, int jdbcType) throws SQLException {
 		try {
 			switch (jdbcType) {
 				case Types.BIGINT:
 				case Types.NUMERIC:
 				case Types.REAL:
 				case Types.DECIMAL:
 				case Types.DOUBLE:
 				case Types.FLOAT:
 					return (value instanceof BigDecimal) ? value : new BigDecimal(value.toString());
 					
 				case Types.SMALLINT:
 				case Types.INTEGER:
 				case Types.TINYINT:
 					return (value instanceof Integer) ? value : new Integer(value.toString());
 					
 				case Types.VARCHAR:
 				case Types.CHAR:
 				case Types.LONGVARCHAR:
 					return value.toString();
 					
 				case Types.DATE:
 					return (Date)value;
 					
 				case Types.TIME:
 					return (Time)value;
 					
 				case Types.TIMESTAMP:
 					return (Timestamp)value;
 					
 				case Types.JAVA_OBJECT:
 					return (SQLData)value;
 					
 				default:
 					s_log.log(Level.SEVERE, "CastToJdbcType: tipo sin definir");
 					return value;
 			}
 		} catch (NumberFormatException e) {
 			s_log.log(Level.SEVERE, "CastToJdbcType: value: " + value.toString() + " jdbcType: " + jdbcType, e);
 			throw e;
 		}
 	}	
 
 	
 	public static <T extends PO> List<T> getList(Properties ctx, Class<T> clazz, QParam[] params, String[] order, String trxName)	{
 		try {
 			return getList_EX(ctx, clazz, params, order, trxName);
 		}
 		catch (POException e)	{
 			return null;
 		}
 	}
 	
 	/**
 	 * Return a ResultSet with the content of the query
 	 * select * from tableName 
 	 * @param params
 	 * @param trxName
 	 * @return ResultSet with the result of the query.
 	 * @throws POException
 	 */	
 	public static <T extends PO> List<T> getList_EX(Properties ctx, Class<T> clazz, QParam[] params, String[] order, String trxName)	throws POException{
 			T	po = null;
 		
 		Constructor<T> po_constr;
 		try {
 			po_constr = clazz.getDeclaredConstructor(new Class[] { Properties.class, int.class, String.class });
 			po	=  po_constr.newInstance(new Object[] { ctx, 0, trxName });
 		} catch (Exception e) {
 			throw new POException("No se puede instanciar el objeto", e);
 		} 
         
 			
 		POInfo p_info = po.get_POInfo();
 		
 		/** Lista de parametros que se pasaran a pstmt	*/
 		ArrayList paramsList = new ArrayList();
 		/** Contador de parametros */
 		int p_index = 1;
 		
 		/** Consulta									*/
 		StringBuffer sql = new StringBuffer();
 		
 		
 		sql.append("SELECT * from ");
 		sql.append(p_info.getTableName()); // Todo: Se debera pasar como parametro.
 		
 		// Recorremos los parametros
 		if (params != null && params.length > 0)	{
 			sql.append(" WHERE ");
 			for (int i = 0; i < params.length; i++)	{
 				
 				// Comprobamos si el parametro es de tipo cadena
 				if (params[i].isCustom())	{
 					sql.append(" ").append(params[i].getParam_str((i==0))).append(" ");
 					continue;
 				}
 				
 				// Comprobamos que el campo exista en la tabla
 				int index = p_info.getColumnIndex(params[i].getName());
 				if (index == -1)	{
 					throw new POException("The column is not present in PO: " + params[i].getName());
 				}
 				// Comprobamos si se trata de filtrar por una columna virtual
 				if (p_info.isVirtualColumn(index))
 				{
 					throw new POException ("Can not use a virtual column as a param: " + params[i].getName());
 				}
 				
 				// Si es el primero, no lleva condicion
 				if (i == 0 )	{
 					sql.append(params[i].getParam_str(true));	
 				}
 				else {
 					sql.append(" ").append(params[i].getParam_str());
 				}
 				
 				
 				// Añadimos el QueryParam para asignarle despues el valor. 
 				Class c = p_info.getColumnClass(index);
 				int dt = p_info.getColumnDisplayType(index);
 				String columnName = p_info.getColumnName(index); 
 				
 				paramsList.add(new QueryParam(columnName,params[i].getValue() , c, p_index++, dt));
 
 			}
 			
 		}
 
 		// Orden del array
 		if (order != null)	{
 			sql.append(" ORDER BY ");
 			for (int i=0; i < order.length; i++)	{
 				sql.append(order[i]);
 				
 				if (i != order.length -1)	{
 					sql.append(" ,");
 				}
 			}
 		}
 		
 		ArrayList items = new ArrayList();
 		ResultSet rs;
 		s_log.fine("SQL: " + sql.toString());
 		
 		try {
 			CPreparedStatement pstmt = DB.prepareStatement(sql.toString(), trxName);
 //			 Añadimos los parametros a la consulta
 			for (int i = 0; i < paramsList.size(); i++)	{
 				QueryParam p = (QueryParam) paramsList.get(i);
 				
 				try {
 					setColToStatement(p_info, pstmt, p.getColumnIndex(), p);
 				}
 				catch (SQLException e)	{
 					throw new POException("Can not set parm: " + p.getColumnName(), e);
 				}
 			}
 			
 			
 			rs = pstmt.executeQuery();
 			ArrayList<T> poItems = new ArrayList<T>();
 			while (rs.next())	{
 				try {
 	            Constructor<T>	constructor	= clazz.getDeclaredConstructor(new Class[] { Properties.class, ResultSet.class, String.class });
 	            T	record	= constructor.newInstance(new Object[] { ctx, rs, trxName });
 	            poItems.add(record);
 				}
 				catch (Exception e)	{
 					throw new POException("No se puede instanciar el objeto", e);
 				}
 
 			}
 			rs.close();
 			pstmt.close();
 			return poItems;
 			
 		}
 		catch (SQLException e)	{
 			throw new POException ("Can not execute query: " + sql.toString(), e);
 		}
 
 	}
 	
 	/**
 	 * Get a record
 	 * @param <T>
 	 * @param clazz
 	 * @param params
 	 * @return
 	 */
 	public static <T extends PO> T get( Class<T> clazz, QParam[] params)	{
 		return get(Env.getCtx(), clazz, params, null);
 	}
 	
 	
 	public static <T extends PO> T get( Class<T> clazz, QParam param)	{
 		QParam[] params = {param};
 		return get(Env.getCtx(), clazz, params, null);
 	}
 	
 	/**
 	 * Get a record
 	 * @param <T>
 	 * @param clazz
 	 * @param params
 	 * @param trxName
 	 * @return
 	 */
 	public static <T extends PO> T get( Class<T> clazz, QParam[] params, String trxName)	{
 		return get(Env.getCtx(), clazz, params, trxName);
 	}
 	
 	/**
 	 * Get a record 
 	 * @param ctx
 	 * @param param
 	 * @param trxName
 	 * @return
 	 * @throws POException
 	 */
 	
 	public static <T extends PO> T get(Properties ctx, Class<T> clazz, QParam[] params, String trxName)	{
 		
 		List<T> items = getList(ctx, clazz, params, trxName);
 		
		if (items.size() == 0)	{
			return null;
		}
		
 		if (items.size() > 1)	{
 			s_log.severe("Multiple ocurrences when only one expected.\n" + QParam.debugParams(params));
 			return null;
 		}
 		
 		return items.get(0);
 	}
 
 	/**
 	 * Get an List of records
 	 * @param <T>
 	 * @param ctx
 	 * @param clazz
 	 * @param param
 	 * @return
 	 * @throws POException
 	 */
 	public static <T extends PO> List<T> getList(Properties ctx, Class<T> clazz, QParam param)	{
 		QParam[] params = {param};
 		return getList(ctx, clazz, params, null, null);
 	}
 
 	
 	
 	/**
 	 * Get an List of records
 	 * @param <T>
 	 * @param ctx
 	 * @param clazz
 	 * @param param
 	 * @return
 	 * @throws POException
 	 */
 	public static <T extends PO> List<T> getList(Properties ctx, Class<T> clazz, QParam[] param)	{
 		return getList(ctx, clazz, param, null, null);
 	}
 	
 	/**
 	 * Get an List of records
 	 * @param <T>
 	 * @param ctx
 	 * @param clazz
 	 * @return
 	 * @throws POException
 	 */
 	public static <T extends PO> List<T> getList(Properties ctx, Class<T> clazz)	{
 		return getList(ctx, clazz, null, null, null);
 	}
 	
 	/**
 	 * Get an List of records
 	 * @param <T>
 	 * @param ctx
 	 * @param clazz
 	 * @param trxName
 	 * @return
 	 * @throws POException
 	 */
 	public static <T extends PO> List<T> getList(Properties ctx, Class<T> clazz, String trxName)	{
 		return getList(ctx, clazz, null, null, trxName);
 	}
 	
 	/**
 	 * Get an List of records
 	 * @param <T>
 	 * @param ctx
 	 * @param clazz
 	 * @param param
 	 * @param trxName
 	 * @return
 	 * @throws POException
 	 */
 	public static <T extends PO> List<T> getList(Properties ctx, Class<T> clazz, QParam[] param, String trxName)	{
 		return getList(ctx, clazz, param, null, trxName);
 	}
 	
 	/**
 	 * Get an List of records
 	 * @param ctx
 	 * @param clazz
 	 * @param param
 	 * @param order
 	 * @param trxName
 	 * @return
 	 * @throws POException
 	 *//*
 	public static <T extends PO> List<T> getList(Properties ctx, Class<T> clazz, QParam[] param, String[] order, String trxName)	{
 		ArrayList<T> items = new ArrayList<T>();
 		try{
 			
             Constructor<T>	po_constr	= clazz.getDeclaredConstructor(new Class[] { Properties.class, int.class, String.class });
             T	po	=  po_constr.newInstance(new Object[] { ctx, 0, trxName });
 			
 			ResultSet rs = get_Records(po, param, order, trxName); 
 			while (rs.next())	{
 	            Constructor<T>	constructor	= clazz.getDeclaredConstructor(new Class[] { Properties.class, ResultSet.class, String.class });
 	            T	record	= constructor.newInstance(new Object[] { ctx, rs, trxName });
 	            items.add(record);
 
 			}
 	
 			return items;
 		}
 		catch (NoSuchMethodException e)	{throw new RuntimeException ("Error instanciando objetos.", e); }
 		catch (SQLException e)	{ throw new RuntimeException ("Error obteniendo el array de elmentos.", e);	}
 		catch (Exception e)	{ throw new RuntimeException(e);	}			
 	}
 	*/
 	/**
 	 * Get record with this ID
 	 * @param <T>
 	 * @param clazz
 	 * @param id
 	 * @return
 	 */
 	public static <T extends PO> T get(Class<T> clazz, int id)	{
 		return get(Env.getCtx(), clazz, id, null);
 	}
 	
 	/**
 	 * Get record with this ID
 	 * @param <T>
 	 * @param ctx
 	 * @param clazz
 	 * @param id
 	 * @return
 	 */
 	public static <T extends PO> T get(Properties ctx, Class<T> clazz, int id)	{
 		return get(ctx, clazz, id, null);
 	}
 	
 	/**
 	 * Get record with this ID
 	 * @param <T>
 	 * @param ctx
 	 * @param clazz
 	 * @param id
 	 * @param trxName
 	 * @return
 	 */
 	public static <T extends PO> T get(Properties ctx, Class<T> clazz, int id, String trxName)	{
         Constructor<T> po_constr;
 		try {
 			po_constr = clazz.getDeclaredConstructor(new Class[] { Properties.class, int.class, String.class });
 			T	po	=  po_constr.newInstance(new Object[] { ctx, 0, trxName });
 			return po;
 		} catch (NoSuchMethodException e)	{throw new RuntimeException ("Error instanciando objetos.", e); }
 		catch (Exception e)	{ throw new RuntimeException(e);	}			
 	}
 
 	
 }
