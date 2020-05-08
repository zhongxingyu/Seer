 package org.lacassandra.smooshyfaces.persistence.cassandra;
 
 import io.cassandra.sdk.StatusMessageModel;
 import io.cassandra.sdk.data.DataAPI;
 import io.cassandra.sdk.data.DataBulkModel;
 import io.cassandra.sdk.data.DataColumn;
 import io.cassandra.sdk.data.DataMapModel;
 import io.cassandra.sdk.data.DataRowkey;
 import io.cassandra.sdk.exception.CassandraIoException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.log4j.Logger;
 import org.lacassandra.smooshyfaces.entity.Book;
 import org.lacassandra.smooshyfaces.persistence.BookDAO;
 import org.lacassandra.smooshyfaces.persistence.TagsDAO;
 
 import com.google.common.base.Joiner;
 import com.netflix.astyanax.serializers.AnnotatedCompositeSerializer;
 import com.welflex.util.CassandraSetUp;
 
 public class CassandraTagsDAO  implements TagsDAO {
 
 	private static Logger log = Logger.getLogger(CassandraTagsDAO.class);
 	
 	private DataAPI dataAPI; 
 	private String keySpaceName;
 	private String columnFamilyName;
 	
     public final static String COLUMN_NAME_TAGS = "tags";
     public final static String COLUMN_NAME_ISBN = "isbn";
     public final static String COLUMN_NAME_TITLE = "tags";
     public final static String COLUMN_NAME_SAMPLER = "sampler";
 
     protected final static AnnotatedCompositeSerializer<BookCommentEntry> BOOK_COMMENT_SERIALIZER =
             new AnnotatedCompositeSerializer<BookCommentEntry>(BookCommentEntry.class);
 
    
 
 	public DataAPI getDataAPI() {
 		return dataAPI;
 	}
 
 	public void setDataAPI(DataAPI dataAPI) {
 		this.dataAPI = dataAPI;
 	}
 
 	public void setColumnFamilyName(String columnFamilyName) {
 		this.columnFamilyName = columnFamilyName;
 	}
 
 	public String getKeySpaceName() {
 		return keySpaceName;
 	}
 
 	public void setKeySpaceName(String keySpaceName) {
 		this.keySpaceName = keySpaceName;
 	}
 
 	public String getColumnFamilyName() {
 		return columnFamilyName;
 	}
 
 	@Override
 	public void saveTag(String tag, String bookIsbn) throws CassandraIoException {
 		List<String> bookIsbns = findByTag(tag);
 		if (!bookIsbns.contains(bookIsbn) )
 		{
 			bookIsbns.add(bookIsbn);
 			Map<String, String> params = new HashMap<String, String>();
 			params.put(CassandraSetUp.CF_TAGS_COL1, Joiner.on(",").join(bookIsbns));
 			dataAPI.postData(keySpaceName, columnFamilyName, tag, params , 12000);
 		}
 	}
 
 	@Override
 	public List<String> findByTag(String tag) throws CassandraIoException {
 		// TODO Auto-generated method stub
 		return convert(dataAPI.getData(keySpaceName, columnFamilyName, tag, 0, null));
 	}
 
 	private List<String> convert(DataMapModel data) {
 		// TODO Auto-generated method stub
 		String foo = data.get(CassandraSetUp.CF_TAGS_COL1);
 		if (foo == null)
 		{
 			foo = "";
 		}
		return Arrays.asList(foo.split(","));
 	}
     
 }
