 package ceid.netcins.json;
 
 import java.util.Map;
 
 import org.eclipse.jetty.util.ajax.JSON.Convertor;
 import org.eclipse.jetty.util.ajax.JSON.Output;
 
 import ceid.netcins.catalog.ContentCatalogEntry;
 import ceid.netcins.catalog.UserCatalogEntry;
 import ceid.netcins.content.ContentProfile;
 
 public class ContentCatalogEntryJSONConvertor implements Convertor {
 	protected static final UserCatalogEntryJSONConvertor ucjc = new UserCatalogEntryJSONConvertor();
 	protected static final ContentProfileJSONConvertor cpjc = new ContentProfileJSONConvertor();
 
 	public ContentCatalogEntryJSONConvertor() {
 	}
 
 	@Override
 	public void toJSON(Object obj, Output out) {
 		if (obj == null) {
 			out.add(null);
 			return;
 		}
		UserCatalogEntry uce = (UserCatalogEntry)obj;
 		ucjc.toJSON(uce, out);
		cpjc.toJSON(uce.getUserProfile(), out);
 	}
 
 	@Override
 	@SuppressWarnings("rawtypes")
 	public Object fromJSON(Map object) {
 		UserCatalogEntry uce = (UserCatalogEntry)ucjc.fromJSON(object);
 		ContentProfile cp = (ContentProfile)cpjc.fromJSON(object);
 		return new ContentCatalogEntry(uce.getUID(), cp, uce.getUserProfile());
 	}
 }
