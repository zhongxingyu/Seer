 package org.remus;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.mpstore.MPStore;
 
 
 public class RemusPath {
 
 	RemusApp parent;
 
 	private String url = null;
 
 	private String pipelineName = null;
 	private String appletView = null;
 	private String appletName = null;
 	private String appletPortName = null;
 	private String instance = null;
 	private String key = null;
 	private String attachName = null;
 	private int input_type;
 
 	public static final int AppletInput = 0;
 	public static final int DynamicInput = 1;
 	public static final int AttachInput = 2;
 	public static final int StaticInput = 2;
 
 	static final Pattern appletSub = Pattern.compile("(\\:\\w+)\\.(\\w+)$");
 	static final Pattern pipelineAttachment = Pattern.compile("^/([^/]*)/(.*)$");
 	//static final Pattern instancePat = Pattern.compile("^([^/]*)/([^/]*)$");
 	//static final Pattern instanceKeyPat = Pattern.compile("^([^/]*)/([^/]*)/(.*)$");
 
 
 	public RemusPath(RemusPath ref, RemusInstance instance) {
 		this.parent = ref.parent;
 		this.instance = instance.toString();
 		if ( ref.getInputType() == DynamicInput ) {
 			String submitPath = null;
 			MPStore ds = ref.parent.getApplet( ref.getAppletPath() ).getDataStore();
			for ( Object path : ds.get( ref.getAppletPath() + "@submit", RemusInstance.STATIC_INSTANCE_STR, instance.toString() ) ) {
				submitPath = (String)path;
 			}
 			if ( submitPath != null ) {
 				ref = new RemusPath(ref.parent, submitPath);
 			}
 			this.instance = ref.instance;
 		}
 		this.pipelineName = ref.pipelineName;
 		this.appletName = ref.appletName;
 		this.appletPortName = ref.appletPortName;
 		this.appletView = ref.appletView;
 		this.input_type = ref.input_type;
 		this.key = ref.key;
 		this.url = getInstancePath(); 
 	}
 
 	public RemusPath( RemusApp parent, String pathinfo ) {
 		appletView = null;
 		instance = null;
 		appletPortName = null;
 		key = null;
 		this.parent = parent;
 		if (pathinfo == null)
 			return;
 		String [] tmp = pathinfo.split("@");
 		String path = tmp[0];
 		if ( tmp.length > 1 ) {
 			String [] tmp3 = tmp[1].split("/");			
 			appletView = tmp3[0];
 			if ( tmp3.length > 1 ) {
 				instance = tmp3[1];
 			}
 			if ( tmp3.length > 2 ) {				
 				try {
 					key = URLDecoder.decode( tmp3[2], "UTF-8" ) ;
 				} catch (UnsupportedEncodingException e) {
 				}
 			}
 			if ( tmp3.length > 3 ) {		
 				try {
 					attachName = URLDecoder.decode( tmp3[3], "UTF-8" ) ;
 				} catch (UnsupportedEncodingException e) {
 				}				 
 			}
 			if ( key != null && key.length() == 0 )
 				key = null;
 			if ( appletView != null && appletView.length() == 0 )
 				appletView = null;
 			Matcher m = appletSub.matcher( tmp[0] );
 			String appletPath = null;
 			if ( m.find() ) {
 				appletPath = m.group(1);
 				appletPortName = m.group(2);
 				appletPath = m.replaceAll(appletPath);
 			} else {
 				appletPath = tmp[0];
 			}
 			appletPath = (new File(appletPath)).getAbsolutePath();
 			String [] tmp2 = appletPath.split(":");
 			if ( tmp2.length == 2) {
 				appletName = tmp2[1];
 				pipelineName = tmp2[0].replaceFirst("^/", "");
 			} else if ( tmp2.length == 1 ) {
 				appletName = null;
 				pipelineName = tmp2[0].replaceFirst("^/", "");;
 				if ( pipelineName.length() == 0 )
 					pipelineName = null;
 			}
 			url = pathinfo;
 		} else {
 			String tmpPath = (new File(pathinfo)).getAbsolutePath();
 			Matcher m = pipelineAttachment.matcher(tmpPath);
 			if ( m.matches() ) {
 				pipelineName = m.group(1);
 				attachName = m.group(2);
 
 			}
 			url = pathinfo;
 		}
 	}
 
 
 	public RemusPath(RemusApp parent, String inputStr, String pipelineID, String appletID) throws FileNotFoundException {
 		this.parent = parent;
 		if ( inputStr.startsWith(":") ) {
 			if ( inputStr.contains("@") ) {
 				String [] tmp = inputStr.split("@");
 				inputStr = tmp[0];
 				appletView = tmp[1];					
 			} else {
 				appletView = "data";
 			}			
 			Matcher m = appletSub.matcher( inputStr );
 			if ( m.find() ) {
 				appletName = m.group(1).replaceFirst("^:", "");
 				appletPortName = m.group(2);
 			} else {
 				appletName = inputStr.replaceFirst("^:", "");
 				appletPortName = null;
 			}			
 			pipelineName = pipelineID;
 			input_type=AppletInput;
 		} else if ( inputStr.compareTo("?") == 0) {
 			appletName = appletID;
 			pipelineName = pipelineID;
 			url = "?";
 			input_type=DynamicInput;
 		} else if ( inputStr.compareTo("$") == 0) {
 			appletName = appletID;
 			pipelineName = pipelineID;
 			url = "$";
 			input_type=StaticInput;
 		} else {
 			url = "/" + pipelineID + "/" + inputStr; 
 			input_type=AttachInput;
 		}
 
 	}
 
 
 
 
 	public long getKeyCount( MPStore ds, int maxCount ) {
 		if ( key != null )
 			return 1;
 		return ds.keyCount( getViewPath(), instance, maxCount );			
 	}
 
 	public Iterable<String> listKeys( MPStore ds ) {
 		if ( key != null ) {
 			return Arrays.asList(key);
 		}
 		return ds.listKeys(getViewPath(), instance);
 	}
 
 
 	public int getInputType() {
 		return input_type;
 	}
 
 	public String getPortPath() {
 		if ( appletPortName != null )
 			return "/" + pipelineName + ":" + appletName + "." + appletPortName;
 		return "/" + pipelineName + ":" + appletName;
 	}
 
 	public String getViewPath() {
 		if ( appletPortName != null )
 			return "/" + pipelineName + ":" + appletName + "." + appletPortName + "@" + appletView;
 		return "/" + pipelineName + ":" + appletName + "@" + appletView;
 	}
 
 	public String getInstancePath() {
 		return getViewPath() + "/" + instance;
 	}
 
 	public boolean isPortName() {
 		if ( appletPortName == null )
 			return false;
 		return true;
 	}
 
 	public String getPath() {
 		return url;
 	}
 
 
 	public String getAppletPath() {
 		return "/" + pipelineName + ":" + appletName;
 	}
 
 
 	public String getView() {
 		return appletView;
 	}
 
 	public String getPortName() {
 		return appletPortName;
 	}
 
 
 	public String getInstance() {
 		return instance;
 	}
 
 
 	public String getKey() {
 		return key;
 	}
 
 	public String getApplet() {
 		return appletName;
 	}
 
 	public String getPipeline() {
 		return pipelineName;
 	}
 
 	public String getAttachment() {
 		// TODO Auto-generated method stub
 		return attachName;
 	}
 
 
 }
