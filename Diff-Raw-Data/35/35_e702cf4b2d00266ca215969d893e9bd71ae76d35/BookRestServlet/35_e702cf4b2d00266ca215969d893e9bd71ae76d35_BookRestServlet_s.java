 package edu.weblibrary.server;
 import edu.weblibrary.server.persistence.cayenne.Author;
 import edu.weblibrary.server.persistence.cayenne.Book;
 
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.cayenne.CayenneDataObject;
 import org.apache.cayenne.exp.Expression;
 import org.apache.cayenne.query.SelectQuery;
 import org.apache.cayenne.query.SortOrder;
 
 import com.smartgwt.client.rpc.RPCResponse;
 
 /**
  * Сервлет, реализующий работу с таблицей book. 
  * В XML ответе на запрос FETCH кроме информации о книге
  * также отправляется имя и фамилия автора.
  * 
  * @author mmdw
  */
 public class BookRestServlet extends CommonRestServlet {
 	static final String AUTHOR_INITIALS_ATTRIBUTE_NAME = "authorInitials";
 
	private String makeAuthorInitial(Author author)
 	{		
 		//SelectQuery authorQuery = new SelectQuery(Author.class, new Ex)
 		String authorName = author.getName();
 		String authorSurame = author.getSurname();	
 		String authorBirthYear = author.getBirthYear().toString();
 		
 		StringBuffer sb = new StringBuffer();
 		sb.append(authorSurame).append(" ").append(authorName);
 		
 		return sb.toString();		
 	}
 	
 	protected void fetch(PrintWriter out, int startRow, int endRow, String sortBy) {		
 		SelectQuery query = new SelectQuery(DataObjectClass);
 		query.setFetchLimit(endRow - startRow + 1);
 		query.setFetchOffset(startRow);
 		
 		if (sortBy != null)	{
 			if (sortBy.charAt(0) == '-')
 				query.addOrdering(sortBy.substring(1), SortOrder.ASCENDING);
 			else			
 				query.addOrdering(sortBy, SortOrder.DESCENDING);							
 		}
 		
 		query.addPrefetch("toAuthor");
 		List<CayenneDataObject> dataList = context.performQuery(query);
 		
 		int total = totalRows().intValue();
 		if (endRow > total - 1)	
 			endRow = total - 1;		
 		XMLResponse xmlResponse = new XMLResponse();
 		xmlResponse.setFetchInfo(startRow, endRow, total, sortBy);	
 		
 		for (CayenneDataObject dataObject: dataList) {
 			Book book = (Book) dataObject;			
 			
 			Map<String, String> attributes = readAttributes(dataObject);
			attributes.put(AUTHOR_INITIALS_ATTRIBUTE_NAME, makeAuthorInitial(book.getToAuthor()));
 			xmlResponse.addRecord(attributes);
 		}
 		
 		xmlResponse.writeToStream(out);
 	}
 	
 	protected XMLResponse add_successResponse(CayenneDataObject cdo) {
 		Map<String, String> record = new TreeMap<String, String>();
 		XMLResponse xmlResponse    = new XMLResponse();
 		
 		for (String i: getAttributeNames()) {
 			Object value = cdo.readProperty(i);
 			if (value == null) 
 				value = new String("null");
 			record.put(i, value.toString());			
 		}	
 		
 		Book book = (Book)cdo;
 		final Expression template = Expression.fromString("id = $id");
 		Map params = new HashMap();		
 		params.put("id", book.readProperty("authorId"));
 		SelectQuery query = new SelectQuery(Author.class, template.expWithParameters(params));
 		List values = context.performQuery(query);
 		Author author = (Author)values.get(0);
 		
		record.put(AUTHOR_INITIALS_ATTRIBUTE_NAME, makeAuthorInitial(author));
 		xmlResponse.setStatus(RPCResponse.STATUS_SUCCESS);
 		xmlResponse.addRecord(record);
 		
 		return xmlResponse;
 	}
 }
