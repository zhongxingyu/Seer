 package interdroid.vdb.persistence.content;
 
 import android.net.Uri;
 
 import interdroid.vdb.Authority;
 import interdroid.vdb.content.EntityUriBuilder;
 import interdroid.vdb.content.avro.AvroProviderRegistry;
 import interdroid.vdb.content.metadata.DatabaseFieldType;
 import interdroid.vdb.content.orm.DbEntity;
 import interdroid.vdb.content.orm.DbField;
 import interdroid.vdb.content.orm.ORMGenericContentProvider;
 
 public class PeerRegistry extends ORMGenericContentProvider {
 
 	public static final String NAME = "peer";
 	public static final String NAMESPACE = "interdroid.vdb.persistence.content";
 	public static final String FULL_NAME = NAMESPACE + "." + NAME;
 
 	public static final String KEY_EMAIL = "email";
 	public static final String KEY_NAME = "name";
 	public static final String KEY_DEVICE = "device";
 	public static final String KEY_STATE = "state";
 
 
 	@DbEntity(name=NAME,
 			itemContentType = "vnd.android.cursor.item/" + FULL_NAME,
 			contentType = "vnd.android.cursor.dir/" + FULL_NAME)
 	public static class Peer {
 
 		private Peer() {}
 
 		public static final int STATE_NEW = 0;
 		public static final int STATE_ADDED = 1;
 		public static final int STATE_REJECTED = 2;
 
 		/**
 		 * The default sort order for this table
 		 */
 		public static final String DEFAULT_SORT_ORDER = "modified DESC";
 
 		public static final Uri CONTENT_URI =
			Uri.withAppendedPath(EntityUriBuilder.branchUri(Authority.VDB, AvroProviderRegistry.NAMESPACE, "master"), AvroProviderRegistry.NAME);
 
 		@DbField(isID=true, dbType=DatabaseFieldType.INTEGER)
 		public static final String _ID = "_id";
 
 		@DbField(dbType=DatabaseFieldType.TEXT)
 		public static final String NAME = KEY_NAME;
 
 		@DbField(dbType=DatabaseFieldType.TEXT)
 		public static final String DEVICE = KEY_DEVICE;
 
 		@DbField(dbType=DatabaseFieldType.INTEGER)
 		public static final String STATE = KEY_STATE;
 
 		@DbField(dbType=DatabaseFieldType.TEXT)
 		public static final String EMAIL = KEY_EMAIL;
 
 	}
 
 	public PeerRegistry() {
 		super(NAMESPACE, Peer.class);
 	}
 
 	public static final Uri URI = Uri.withAppendedPath(EntityUriBuilder.branchUri(Authority.VDB,
 			NAMESPACE, "master"), NAME);
 }
