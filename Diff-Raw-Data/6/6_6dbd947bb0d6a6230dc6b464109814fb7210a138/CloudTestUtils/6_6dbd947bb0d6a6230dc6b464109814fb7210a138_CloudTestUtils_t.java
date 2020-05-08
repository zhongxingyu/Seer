 package test.cli.cloudify;
 
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import framework.tools.SGTestHelper;
 import framework.utils.LogUtils;
 import framework.utils.WebUtils;
 
 
 public class CloudTestUtils {
 
 	public static final String SGTEST_MACHINE_PREFIX = SGTestHelper.getSuiteName() + SGTestHelper.getSuiteId() + "_";
 
 	public static final String WEBUI_PORT = String.valueOf(8099); 
 	public static final String REST_PORT = String.valueOf(8100); 
 
 	public static final String IP_REGEX= "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"; 
	public static final String WEBUI_URL_REGEX= "Webui service is available at: (http[s]*://(.*):" + WEBUI_PORT +")";
	public static final String REST_URL_REGEX= "Rest service is available at: (http[s]*://(.*):" + REST_PORT + ")";
 	
 	public static final String EC2_MANAGEMENT_CONSOLE_URL = "https://console.aws.amazon.com";
 	public static final String HPCLOUD_MANAGEMENT_CONSOLE_URL = "https://manage.hpcloud.com";
 
 	public final static long OPERATION_TIMEOUT = 5 * 60 * 1000;	
 
 	public static int getNumberOfMachines(URL machinesRestAdminUrl) throws Exception {
 		String json = WebUtils.getURLContentSwallowExceptions(machinesRestAdminUrl);
 		LogUtils.log("-- getNumberOfMachines received json text: " + json);
 		Matcher matcher = Pattern.compile("\"Size\":\"([0-9]+)\"").matcher(json);
 		if (matcher.find()) {
 			String rawSize = matcher.group(1);
 			int size = Integer.parseInt(rawSize);
 			return size;
 		} else {
 			return 0;
 		}
 	}
 
 }
