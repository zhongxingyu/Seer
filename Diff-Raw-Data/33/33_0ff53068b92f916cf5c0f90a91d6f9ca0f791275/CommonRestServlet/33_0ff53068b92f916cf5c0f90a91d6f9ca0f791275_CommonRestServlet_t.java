 package edu.weblibrary.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
import java.util.Collection;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.cayenne.CayenneDataObject;
 import org.apache.cayenne.DataObjectUtils;
 import org.apache.cayenne.ObjectContext;
 import org.apache.cayenne.access.DataContext;
 import org.apache.cayenne.exp.ExpressionFactory;
 import org.apache.cayenne.map.DeleteRule;
 import org.apache.cayenne.map.ObjEntity;
 import org.apache.cayenne.map.ObjRelationship;
 import org.apache.cayenne.map.SQLResult;
 import org.apache.cayenne.query.SQLTemplate;
 import org.apache.cayenne.query.SelectQuery;
 import org.apache.cayenne.query.SortOrder;
 import org.apache.cayenne.validation.BeanValidationFailure;
 import org.apache.cayenne.validation.ValidationException;
 import org.apache.cayenne.validation.ValidationFailure;
 import org.apache.log4j.Logger;
 
 import com.smartgwt.client.rpc.RPCResponse;
 
 import edu.weblibrary.shared.ResponseStatusEx;
 
 /**
  * <p>Сервлет, реализующий удаленную работу с одной таблицей базы данных при помощи Apache Cayenne.
  * 
  * <p>Формат запросов и ответов определен требованиями к источнику данных:
  * {@link com.smartgwt.client.data.RestDataSource} 
  * 
  * <p>При инициализации сервлета нужно указать класс хранимого объекта:
  * <pre>
  * {@code
  * <servlet>
  *  ... 
  *  <init-param>
  *   <param-name>DataObjectClass</param-name>
  *   <param-value>edu.gallery.persistent.Painting</param-value>  		
  *  </init-param>  		
  * </servlet> 
  * }
  * </pre>
  * @author mmdw
  * @see com.smartgwt.client.data.RestDataSource 
  *
  */
 @SuppressWarnings("serial")
 public class CommonRestServlet extends HttpServlet {
 	public static final Logger LOG = Logger.getLogger(CommonRestServlet.class);
 	
 	private enum Operation {
 		FETCH, ADD, REMOVE, UPDATE
 	}
 	
 	private SQLTemplate countQuery;
 	protected ObjectContext context;
 	private TreeSet<String> attributeNames;
 	protected Class<? super CayenneDataObject> DataObjectClass;
 	
 	private String ID_PK_COLUMN;
 	private static SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 	
 	protected Number totalRows()	{
 		return (Number) DataObjectUtils.objectForQuery(context, countQuery);
 	}
 	
 	/**
 	 * Извлекает отображение [имя атрибута->значение атрибута] из параметров запроса.
 	 * @param req запрос
 	 * @return Отображение имен атрибутов хранимого объекта в их значения.
 	 */
 	private Map<String, String> getAttributesFromRequest(HttpServletRequest req) {
 		Map<String, String> result = new TreeMap<String, String>();
 		
 		for (String key: attributeNames)		
 			if (req.getParameterMap().containsKey(key))			
 				result.put(key, req.getParameter(key));
 		
 		return result;		
 	}
 	
 	/**
 	 * Записывает в хранимый объект значения атрибутов. 
 	 * @param cdo хранимый объект 
 	 * @param attributes отображение: [имя атрибута->значение атрибута]
 	 */
 	protected void writeAttributes(CayenneDataObject cdo, Map<String, String> attributes) {
 		for (String name: attributes.keySet())	{
 			String val = attributes.get(name);
 			if (val != null && val.equals("null")) val = null;
 			cdo.writeProperty(name, val);
 		}		
 	}
 	
 	/**
 	 * Читает из хранимого объекта отображение: [имя атрибута->значение атрибута]
 	 * @param cdo хранимый объект
 	 * @return отображение: [имя атрибута->значение атрибута]
 	 */
 	protected Map<String, String> readAttributes(CayenneDataObject cdo) {
 		Map<String, String> attributeMap = new TreeMap<String, String>();
 		for (String name: attributeNames)	{					
 			Object val = cdo.readProperty(name);
 			
 			if (val instanceof Date)
 				val = ISO8601FORMAT.format(val);
 			
 			if (val != null)
 				attributeMap.put(name, val.toString());				
 		}	
 		
 		return attributeMap;		
 	}
 	
 	/**
 	 * Сервлет, обрабатывающий POST запрос и посылающий XML ответ в соответствии с требованиями 
 	 * к источнику данных RestDataSource. 
 	 * @see com.smartgwt.client.data.RestDataSource
 	 */
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		
 		resp.setCharacterEncoding("utf8");
 		resp.setContentType("text/xml");
 		PrintWriter out = resp.getWriter();
 		
 		StringWriter sw = new StringWriter();
 		sw.write("\n");
 		
 		for (Enumeration<String> e = req.getParameterNames(); e.hasMoreElements();) {
 			String param = e.nextElement();	
 			
 			sw.write(param + " =");			
 			for(String s: req.getParameterValues(param))
 				sw.write(" ".concat(s));
 			sw.write("\n");
 		}
 		
 		LOG.debug(sw.toString());
 				
 		Operation op = Operation.valueOf(req.getParameter("_operationType").toUpperCase());		
 	
 		switch (op) {
 			case FETCH: 
 				fetch(out, 
 					  Integer.parseInt(req.getParameter("_startRow")),
 					  Integer.parseInt(req.getParameter("_endRow")),
 					  req.getParameter("_sortBy"));
 				break;
 			case UPDATE:
 				update(out, getAttributesFromRequest(req));
 				break;
 			case REMOVE: 
 				remove(out, getAttributesFromRequest(req)); 
 				break;
 			case ADD:
 				add(out, getAttributesFromRequest(req));									
 		}					
    }
 
 	/**
 	 * Удаляет запись из БД.
 	 * @param out поток, в который пишется XML ответ
 	 * @param map атрибуты хранимого объекта, извлеченные из параметров запроса
 	 * @see com.smartgwt.client.data.RestDataSource 
 	 */
 	protected void remove(PrintWriter out, Map<String, String> map) {	
 		LOG.debug("*** REMOVE started");
 		SelectQuery query = 
 			new SelectQuery(DataObjectClass, ExpressionFactory.matchExp(ID_PK_COLUMN, map.get(ID_PK_COLUMN)));		
 		
 		CayenneDataObject a = (CayenneDataObject) context.performQuery(query).get(0);		
 		XMLResponse resp = new XMLResponse();
 		
		
 		for (ObjRelationship rel: a.getObjEntity().getRelationships()) {
 			if (rel.getDeleteRule() == DeleteRule.DENY) {
				if (((Collection)a.readProperty(rel.getName())).size() != 0) {
					resp.setStatus(ResponseStatusEx.STATUS_DELETE_DENY_ERROR);
					resp.writeToStream(out);				
					return;
				}
 			}
 		}			
 		
 		try {
 			context.deleteObject(a);
 			context.commitChanges();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println(e.getMessage());
 			
			resp.setStatus(RPCResponse.STATUS_VALIDATION_ERROR);			
 			context.rollbackChanges();
 			return;
 		}		
 		
 		resp.writeToStream(out);
 		LOG.debug("*** REMOVE finished");
 	}
 
 	/**
 	 * Обновляет запись в БД.
 	 * @param out поток, в который пишется XML ответ
 	 * @param map атрибуты хранимого объекта, извлеченные из параметров запроса
 	 * @see com.smartgwt.client.data.RestDataSource 
 	 */
 	protected void update(PrintWriter out, Map<String, String> attributes) {	
 		LOG.debug("*** UPDATE started");
 		SelectQuery updateQuery = new SelectQuery(DataObjectClass, 
 				ExpressionFactory.matchExp(ID_PK_COLUMN, attributes.get(ID_PK_COLUMN)));
 		
 		CayenneDataObject dataObject = (CayenneDataObject) context.performQuery(updateQuery).get(0);
 		XMLResponse xmlResponse = new XMLResponse();
 		
 		writeAttributes(dataObject, attributes);
 		
 		try	{		
 			context.commitChanges();
 		} catch (ValidationException e) {				
			xmlResponse.setStatus(RPCResponse.STATUS_SUCCESS);	
 			
 			for(ValidationFailure vf: e.getValidationResult().getFailures())			
 				xmlResponse.addErrorMessage(((BeanValidationFailure)vf).getProperty(), (String) vf.getError());
 			
 			context.rollbackChanges();
 			xmlResponse.writeToStream(out);
 			return;
 		}
 		
		xmlResponse.setStatus(RPCResponse.STATUS_SUCCESS);
 		xmlResponse.writeToStream(out);
 		LOG.debug("*** UPDATE finished");
 	}
 
 	protected void fetch(PrintWriter out, int startRow, int endRow, String sortBy) {		
 		LOG.debug("*** FETCH started");
 		SelectQuery query = new SelectQuery(DataObjectClass);
 		query.setFetchLimit(endRow - startRow + 1);
 		query.setFetchOffset(startRow);
 		
 		if (sortBy != null)	{
 			if (sortBy.charAt(0) == '-')
 				query.addOrdering(sortBy.substring(1), SortOrder.ASCENDING);
 			else			
 				query.addOrdering(sortBy, SortOrder.DESCENDING);							
 		}
 		
 		List<CayenneDataObject> dataList = context.performQuery(query);
 		
 		int total = totalRows().intValue();
 		if (endRow > total - 1)	
 			endRow = total - 1;		
 		XMLResponse xmlResponse = new XMLResponse();
 		xmlResponse.setStatus(RPCResponse.STATUS_SUCCESS);
 		xmlResponse.setFetchInfo(startRow, endRow, total, sortBy);
 		
 		for (CayenneDataObject dataObject: dataList)			
 			xmlResponse.addRecord(readAttributes(dataObject));
 		
 		xmlResponse.writeToStream(out);
 
 		LOG.debug("*** FETCH finished");
 	}
 	/**
 	 * Добавляет новую запись в БД.
 	 * @param out поток, в который выводится XML ответ
 	 * @param attributes атрибуты хранимого объекта, извлеченные из параметров запроса
 	 * @see com.smartgwt.client.data.RestDataSource
 	 */
 	protected void add(PrintWriter out, Map<String, String> attributes) throws ServletException
 	{			
 		LOG.debug("*** ADD started");
 		XMLResponse xmlResponse = new XMLResponse();		
 		
 		CayenneDataObject rel = (CayenneDataObject) context.newObject(DataObjectClass);
 		writeAttributes(rel, attributes);	
 		
 		try	{		
 			context.commitChanges();			
 		} catch (ValidationException e) {	
			xmlResponse.setStatus(RPCResponse.STATUS_VALIDATION_ERROR);			
 			
 			for(ValidationFailure vf: e.getValidationResult().getFailures())			
 				xmlResponse.addErrorMessage(((BeanValidationFailure)vf).getProperty(), (String) vf.getError());
 			
 			context.rollbackChanges();
 			xmlResponse.writeToStream(out);
 			return;				
 		} catch (Exception ex) {	
 			ex.printStackTrace();
 			
			xmlResponse.setStatus(RPCResponse.STATUS_VALIDATION_ERROR);
 			context.rollbackChanges();
 			xmlResponse.writeToStream(out);
 			return;		
 		}
 			
 		Map<String, String> record = new TreeMap<String, String>();
 		
 		for (String i: attributeNames) {
 			Object value = rel.readProperty(i);
 			if (value == null) 
 				value = new String("null");
 			record.put(i, value.toString());			
 		}	
		xmlResponse.setStatus(RPCResponse.STATUS_SUCCESS);
 		xmlResponse.addRecord(record);		
 		xmlResponse.writeToStream(out);
 		LOG.debug("*** ADD finished");
 	}
 	
 	public void init(ServletConfig config) throws ServletException	{	
 		try {
 			DataObjectClass =
 				(Class<? super CayenneDataObject>) Class.forName((String) config.getInitParameter("DataObjectClass"));
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		context = DataContext.createDataContext();		
 		
 		ObjEntity entity = context.getEntityResolver().lookupObjEntity(DataObjectClass);
 		attributeNames = new TreeSet<String>(entity.getAttributeMap().keySet());		
 		
 		countQuery = new SQLTemplate(DataObjectClass, "select count(*) from " + entity.getDbEntityName());		
 		SQLResult resultDescriptor = new SQLResult();
 		resultDescriptor.addColumnResult("S");
 		countQuery.setResult(resultDescriptor);
 		
 		try {			
 			ID_PK_COLUMN = (String) DataObjectClass.getField("ID_PK_COLUMN").get(null);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
