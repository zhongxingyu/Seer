 package fedora.server.storage.lowlevel;
 import java.io.File;
 import fedora.server.Server;
 import fedora.server.errors.LowlevelStorageException;
 import fedora.server.errors.InitializationException;
 class Configuration {
 
 	private final boolean backslashIsEscape;
 	private final String separator;
 	private final String objectStoreBase;
 	private final String[] objectStoreBases;
 	private final String datastreamStoreBase;
 	private final String[] datastreamStoreBases;	
 	private final String tempStoreBase;
 	private final String[] tempStoreBases;
 	//private final boolean useSingleRegistry;
 	private final String algorithmClass;
 	
 	private final String registryClass;
 
 	private final String objectRegistryTableName = "objectpaths";
	private final String datastreamRegistryTableName = "temppaths";
	private final String tempRegistryTableName = "datastreampaths";
 	
 	private final String fileSystemClass;
 	
 	private static boolean testConfig = false; 
 	static {
 		String temp = System.getProperty("store.lowlevel.mode");
 		if ((temp != null) && temp.equals("test")) {
 			testConfig = true;
 		}
 	}
 	
 	private static final Server s_server;
 	static {
 		Server temp = null;
 		if (! testConfig) {
 			try {
 				temp = Server.getInstance(new File(System.getProperty("fedora.home")));
 			} catch (InitializationException ie) {
 				System.err.println(ie.getMessage());				
 			}
 		}
 		s_server = temp;
 	}
 
 	private static final Configuration singleInstance;
 	static {
 		Configuration temp = null;
 		try {
 			temp = new Configuration();
 		} catch (LowlevelStorageException e) {
 			System.err.println("didn't conf: " + e.getMessage());
 		} finally {
 			singleInstance = temp;
 		}
 	}
 	
 	public static final Configuration getInstance() {
 		return singleInstance;
 	}
 
 	private static final String FCFG_BACKSLASH_IS_ESCAPE = "backslash_is_escape";
 	
 	private static final String FCFG_FILE_SYSTEM_CLASS = "file_system";
 	private static final String FCFG_PATH_ALGORITHM_CLASS = "path_algorithm";
 	private static final String FCFG_REGISTRY_CLASS = "registry";
 
 	private static final String FCFG_OBJECT_STORE_BASE = "object_store_base";
 	private static final String FCFG_DATASTREAM_STORE_BASE = "datastream_store_base";
 	private static final String FCFG_TEMP_STORE_BASE = "temp_store_base";
 
 	private static final String FCFG_OBJECT_TABLE_NAME = "object_table_name";	
 	private static final String FCFG_DATASTREAM_TABLE_NAME = "datastream_table_name";
 	private static final String FCFG_TEMP_TABLE_NAME = "temp_table_name";
 
 	private Configuration () throws LowlevelStorageException {
 
 		{
 			String algorithmClassTemp = testConfig ? "fedora.server.storage.lowlevel.TimestampPathAlgorithm" :
 				s_server.getParameter(FCFG_PATH_ALGORITHM_CLASS);
 			if (algorithmClassTemp == null) {
 				throw new LowlevelStorageException(true,"must configure " + FCFG_PATH_ALGORITHM_CLASS);
 			}			
 			algorithmClass = algorithmClassTemp;
 		}
 
 		{
 			String registryClassTemp = testConfig ? "fedora.server.storage.lowlevel.DBPathRegistry" :
 				s_server.getParameter(FCFG_REGISTRY_CLASS);
 			if (registryClassTemp == null) {
 				throw new LowlevelStorageException(true,"must configure " + FCFG_REGISTRY_CLASS);
 			}
 			registryClass = registryClassTemp;
 		}
 
 		/*
 		{
 			String tableName = testConfig ? "objectpaths" :
 				s_server.getParameter(FCFG_OBJECT_TABLE_NAME);
 			if ((tableName == null) || tableName.equals("")) {
 				throw new LowlevelStorageException(true,"must configure " + FCFG_OBJECT_TABLE_NAME);
 			}
 			objectRegistryTableName = tableName;
 		}
 		
 		{
 			String tableName = testConfig ? "temppaths" :
 				s_server.getParameter(FCFG_TEMP_TABLE_NAME);
 			if ((tableName == null) || tableName.equals("")) {
 				throw new LowlevelStorageException(true,"must configure " + FCFG_TEMP_TABLE_NAME);
 			}
 			tempRegistryTableName = tableName;	
 		}
 		
 		{
 			String tableName = testConfig ? "datastreampaths" :
 				s_server.getParameter(FCFG_DATASTREAM_TABLE_NAME);
 			if ((tableName == null) || tableName.equals("")) {
 				throw new LowlevelStorageException(true,"must configure " + FCFG_DATASTREAM_TABLE_NAME);
 			}
 			datastreamRegistryTableName = tableName;
 		}
 		*/
 
 		{
 			String fileSystemClassTemp = testConfig ? "fedora.server.storage.lowlevel.GenericFileSystem" :
 				s_server.getParameter(FCFG_FILE_SYSTEM_CLASS);
 			if (fileSystemClassTemp == null) {
 				throw new LowlevelStorageException(true,"must configure " + FCFG_FILE_SYSTEM_CLASS);
 			}
 			fileSystemClass = fileSystemClassTemp;
 		}
 		
 		{
 			String backslashIsEscapeString = testConfig ? "yes" :
 				s_server.getParameter(FCFG_BACKSLASH_IS_ESCAPE);
 			if (backslashIsEscapeString == null) {
 				throw new LowlevelStorageException(true,"must configure " + FCFG_BACKSLASH_IS_ESCAPE);
 			}
 			backslashIsEscapeString = backslashIsEscapeString.toUpperCase();
 			if (! (backslashIsEscapeString.equals("YES") || backslashIsEscapeString.equals("NO")) ) {
 				throw new LowlevelStorageException(true,"must configure " + FCFG_BACKSLASH_IS_ESCAPE + " as yes/no");
 			}
 			backslashIsEscape = backslashIsEscapeString.equals("YES");
 		}
 		
 		{
 			String objectStoreBaseTemp = testConfig ? "C:\\fedora_objects" :
 				s_server.getParameter(FCFG_OBJECT_STORE_BASE);
 			String datastreamStoreBaseTemp = testConfig ? "C:\\fedora_datastreams" :
 				s_server.getParameter(FCFG_DATASTREAM_STORE_BASE);
 			String tempStoreBaseTemp = testConfig ? "C:\\fedora_temp" :
 				s_server.getParameter(FCFG_TEMP_STORE_BASE);
 			if (objectStoreBaseTemp == null) {
 				throw new LowlevelStorageException(true,"must configure " + FCFG_OBJECT_STORE_BASE);
 			}
 			if (datastreamStoreBaseTemp == null) {
 				throw new LowlevelStorageException(true,"must configure " + FCFG_DATASTREAM_STORE_BASE);
 			}
 			if (tempStoreBaseTemp == null) {
 				throw new LowlevelStorageException(true,"must configure " + FCFG_TEMP_STORE_BASE);
 			}
         
 // FIXME: thinks c:\temp and c:\temp2 overlap
 			if (objectStoreBaseTemp.startsWith(datastreamStoreBaseTemp)
 			||  objectStoreBaseTemp.startsWith(tempStoreBaseTemp)
 			||  datastreamStoreBaseTemp.startsWith(objectStoreBaseTemp)
 			||  datastreamStoreBaseTemp.startsWith(tempStoreBaseTemp)
 			||  tempStoreBaseTemp.startsWith(objectStoreBaseTemp)			
 			||  tempStoreBaseTemp.startsWith(datastreamStoreBaseTemp)) {
 				throw new LowlevelStorageException(true, FCFG_OBJECT_STORE_BASE + ", " + FCFG_DATASTREAM_STORE_BASE + ", and " + FCFG_TEMP_STORE_BASE + " cannot overlap");
 			}
 //		if (! backslashIsEscape) {
 			objectStoreBase = objectStoreBaseTemp;
 			datastreamStoreBase = datastreamStoreBaseTemp;
 			tempStoreBase = tempStoreBaseTemp;
 			separator = File.separator;
 /*
 		} else {
 			StringBuffer buffer = new StringBuffer();
 			String backslash = "\\";
 			String escapedBackslash = "\\\\";
 			for (int i = 0; i < storeBaseTemp.length(); i++) {
 				String s = storeBaseTemp.substring(i,i+1);
 				buffer.append(s.equals(backslash) ? escapedBackslash : s);
 			}
 			storeBase = buffer.toString();
 			if (File.separator.equals(backslash)) {
 				separator = escapedBackslash;
 			} else {
 				separator = File.separator;
 			}
 		}
 */
 			objectStoreBases = new String[] {objectStoreBase};
 			datastreamStoreBases = new String[] {datastreamStoreBase};
 			tempStoreBases = new String[] {tempStoreBase};
 		}
 	}
 /*
 	public final String getSeparator() {
 		return separator;
 	}
 	*/
 	public final String getObjectStoreBase() {
 		return objectStoreBase;
 	}
 	public final String[] getObjectStoreBases() {
 		return objectStoreBases;
 	}
 	public final String getDatastreamStoreBase() {
 		return datastreamStoreBase;
 	}
 	public final String[] getDatastreamStoreBases() {
 		return datastreamStoreBases;
 	}
 	public final String getTempStoreBase() {
 		return tempStoreBase;
 	}
 	public final String[] getTempStoreBases() {
 		return tempStoreBases;
 	}
 	public final String getAlgorithmClass() {
 		return algorithmClass;
 	}
 	public final String getRegistryClass() {
 		return registryClass;
 	}	
 	public final String getObjectRegistryTableName() {
 		return objectRegistryTableName;
 	}
 	public final String getDatastreamRegistryTableName() {
 		return datastreamRegistryTableName;
 	}	
 	public final String getTempRegistryTableName() {
 		return tempRegistryTableName;
 	}
 	
 	public final String getFileSystemClass() {
 		return fileSystemClass;
 	}
 	public final boolean getBackslashIsEscape() {
 		return backslashIsEscape;
 	}
 	
 	public static final boolean getTestConfig() {
 		return testConfig;
 	}
 }
