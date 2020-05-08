 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import org.cloudifysource.quality.iTests.framework.utils.AssertUtils;
 import org.cloudifysource.quality.iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.framework.utils.WebUtils;
 
 import java.net.MalformedURLException;
 import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public class CloudTestUtils {
 
 
 	public static final String WEBUI_URL_REGEX= "Webui service is available at: (http[s]*://(.*):[0-9]*)";
 	public static final String REST_URL_REGEX= "Rest service is available at: (http[s]*://(.*):[0-9]*)";
 
     // unbelievably retarded. but works. regex is evil.
     public static final String WEBUI_PRIVATE_URL_REGEX = " - Webui service is available at: (http[s]*://(.*):[0-9]*)";
     public static final String REST_PRIVATE_URL_REGEX = " - Rest service is available at: (http[s]*://(.*):[0-9]*)";
 
 	public final static long OPERATION_TIMEOUT = 5 * 60 * 1000;	
 
 	public static int getNumberOfMachines(URL machinesRestAdminUrl) throws Exception {
 		String json = WebUtils.getURLContent(machinesRestAdminUrl);
 		LogUtils.log("-- getNumberOfMachines received json text: " + json);
 		return parseNumberOfMachines(json);
 	}
 
 	public static int getNumberOfMachines(URL machinesRestAdminUrl, String username, String password) throws Exception {
 		String json = WebUtils.getURLContent(machinesRestAdminUrl, username, password);
 		LogUtils.log("-- getNumberOfMachines received json text: " + json);
 		return parseNumberOfMachines(json);
 	}
 	
 	private static int parseNumberOfMachines(String json) {
 		Matcher matcher = Pattern.compile("\"Size\":\"([0-9]+)\"").matcher(json);
 		if (matcher.find()) {
 			String rawSize = matcher.group(1);
 			int size = Integer.parseInt(rawSize);
 			return size;
 		} else {
 			return 0;
 		}
 	}
 
     public static URL[] extractPublicRestUrls(final String cliOutput,
                                               final int numberOfManagementMachines) throws MalformedURLException {
        Set<URL> privateUrls = asSet(extractPrivateRestUrls(cliOutput, numberOfManagementMachines));
        Set<URL> allUrls = asSet(extractAllMatchingUrls(cliOutput, numberOfManagementMachines, CloudTestUtils.REST_URL_REGEX));
        allUrls.removeAll(privateUrls);
         URL[] publicUrls = new URL[numberOfManagementMachines];
         return allUrls.toArray(publicUrls);
     }
 
     public static URL[] extractPublicWebuiUrls(final String cliOutput,
                                                final int numberOfManagementMachines) throws MalformedURLException {
        Set<URL> privateUrls = asSet(extractPrivateWebuiUrls(cliOutput, numberOfManagementMachines));
        Set<URL> allUrls = asSet(extractAllMatchingUrls(cliOutput, numberOfManagementMachines, CloudTestUtils.WEBUI_URL_REGEX));
        allUrls.removeAll(privateUrls);
         URL[] publicUrls = new URL[numberOfManagementMachines];
         return allUrls.toArray(publicUrls);


     }
 
     private static URL[] extractUrls(final String cliOutput, int numberOfManagementMachines, String regex)
             throws MalformedURLException {
         URL[] webuiUrls = new URL[numberOfManagementMachines];
 
         Pattern webUIPattern = Pattern.compile(regex);
         Matcher webUIMatcher = webUIPattern.matcher(cliOutput);
 
         // This is sort of hack.. currently we are outputting this over ssh and locally with different results
         for (int i = 0; i < numberOfManagementMachines; i++) {
             AssertUtils.assertTrue("Could not find actual webui url", webUIMatcher.find());
             String rawWebUIUrl = webUIMatcher.group(1);
             webuiUrls[i] = new URL(rawWebUIUrl);
         }
 
         return webuiUrls;
 
     }
 
     private static URL[] extractAllMatchingUrls(final String cliOutput, int numberOfManagementMachines, String regex) throws MalformedURLException {
 
         URL[] webuiUrls = new URL[numberOfManagementMachines * 2];
 
         Pattern webUIPattern = Pattern.compile(regex);
         Matcher webUIMatcher = webUIPattern.matcher(cliOutput);
 
         // This is sort of hack.. currently we are outputting this over ssh and locally with different results
         for (int i = 0; i < numberOfManagementMachines * 2; i++) {
             AssertUtils.assertTrue("Could not find actual webui url", webUIMatcher.find());
             String rawWebUIUrl = webUIMatcher.group(1);
             webuiUrls[i] = new URL(rawWebUIUrl);
         }
 
         return webuiUrls;
     }
 
     private static URL[] extractPrivateWebuiUrls(final String cliOutput, int numberOfManagementMachines)
             throws MalformedURLException {
         return extractUrls(cliOutput, numberOfManagementMachines, CloudTestUtils.WEBUI_PRIVATE_URL_REGEX);
     }
 
     private static URL[] extractPrivateRestUrls(final String cliOutput, int numberOfManagementMachines)
             throws MalformedURLException {
         return extractUrls(cliOutput, numberOfManagementMachines, CloudTestUtils.REST_PRIVATE_URL_REGEX);
     }
 
 
    private static Set<URL> asSet(URL[] array) {
        return new HashSet<URL>(Arrays.asList(array));
     }
 
 }
