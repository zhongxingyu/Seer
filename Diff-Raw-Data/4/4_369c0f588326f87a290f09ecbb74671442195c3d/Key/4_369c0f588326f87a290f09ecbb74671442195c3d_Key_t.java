 package cmg.org.monitor.memcache;
 
 import java.io.Serializable;
 
 public class Key implements Serializable {
 	/**
 	 * 
 	 */
 
 	private static final long serialVersionUID = 1L;
 
 	public static final int FILE_SYSTEM_STORE = 0x001;
 	public static final int CPU__STORE = 0x002;
 	public static final int MEMORY_STORE = 0x003;
 	public static final int ALERT_STORE = 0x004;
 	public static final int JVM_STORE = 0x005;
 	public static final int SERVICE_STORE = 0x006;
 	public static final int SYSTEM_MONITOR_STORE = 0x007;
 	
 	public static final int ALERT_TEMP_STORE = 0x008;
 	public static final int MAIL_STORE = 0x009;
 	public static final int MAIL_CONFIG_STORE = 0x010;
 	
 	public static final int ABOUT_CONTENT = 0x011;
 	public static final int HELP_CONTENT = 0x012;
 	
 	public static final int LIST_GROUP = 0x013;
 	public static final int LIST_ALL_USERS = 0x014;
 	public static final int LIST_USERS_IN_GROUP = 0x015;
	public static final int CHANGE_LOG = 0x022;
	public static final int CHANGE_LOG_COUNT = 0x023;
 	
 	
 	private int type;
 
 	private String sid;
 	
 	private String options;
 	
 	private int memType;
 
 	protected Key() {
 
 	}
 
 	public static Key create(int type) {
 		Key key = new Key();
 		key.type = type;
 		return key;
 	}
 
 	public static Key create(int type, String sid) {
 		Key key = create(type);
 		key.setSid(sid);
 		return key;
 	}
 	
 	public static Key create(int type, String sid, String options) {
 		Key key = create(type, sid);
 		key.setOptions(options);
 		return key;
 	}
 	
 	public static Key create(int type, String sid, int memType) {
 		Key key = create(type, sid);
 		key.setMemType(memType);
 		return key;
 	}
 
 	protected String getSid() {
 		return sid;
 	}
 
 	protected void setSid(String sid) {
 		this.sid = sid;
 	}
 
 	protected int getType() {
 		return type;
 	}
 
 	protected void setType(int type) {
 		this.type = type;
 	}
 
 	public String getOptions() {
 		return options;
 	}
 
 	public void setOptions(String options) {
 		this.options = options;
 	}
 
 	public int getMemType() {
 		return memType;
 	}
 
 	public void setMemType(int memType) {
 		this.memType = memType;
 	}
 }
