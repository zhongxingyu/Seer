 /**
  * 
  */
 package calabash.java;
 
 import static calabash.java.CalabashLogger.error;
 import static calabash.java.CalabashLogger.info;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.jruby.RubyArray;
 import org.jruby.embed.PathType;
 import org.jruby.embed.ScriptingContainer;
 
 /**
  * 
  *
  */
 public final class CalabashWrapper {
 
 	private final ScriptingContainer container = new ScriptingContainer();
 	private final File rbScriptsDir;
 	private final File projectDir;
 	private final File gemsDir;
 	private final CalabashConfiguration configuration;
 
 	public CalabashWrapper(File rbScriptsDir, File projectDir,
 			CalabashConfiguration configuration) throws CalabashException {
 		this.configuration = configuration;
 		if (!rbScriptsDir.isDirectory())
 			throw new CalabashException("Invalid ruby scripts directory");
 		if (!projectDir.isDirectory())
 			throw new CalabashException("Invalid project directory");
 
 		this.rbScriptsDir = rbScriptsDir;
 		this.gemsDir = new File(rbScriptsDir, "gems");
 		this.projectDir = projectDir;
 		this.initializeScriptingContainer();
 	}
 
 	public void setup() throws CalabashException {
 		try {
 			info("Setting up calabash for project: %s", projectDir.getAbsolutePath());
 			info("Gems directory: %s", gemsDir.getAbsolutePath());
 			container.put("ARGV", new String[] { "setup",
 					projectDir.getAbsolutePath() });
 			String calabashIOS = new File(
 					getCalabashGemDirectory(), "bin/calabash-ios")
 					.getAbsolutePath();
 			container.runScriptlet(PathType.ABSOLUTE, calabashIOS);
 		} catch (Exception e) {
 			error("Failed to setup calabash for project: %s", e, projectDir.getAbsolutePath());
 			throw new CalabashException(String.format(
 					"Failed to setup calabash. %s", e.getMessage()));
 		}
 	}
 
 	public void start() throws CalabashException {
 		try {
 			info("Starting the iOS application - %s", projectDir.getAbsolutePath());
 			info("Gems directory: %s", gemsDir.getAbsolutePath());
 			
 			container.clear();
 			String launcherScript = new File(rbScriptsDir, "launcher.rb")
 					.getAbsolutePath();
 			container.runScriptlet(PathType.ABSOLUTE, launcherScript);
 		} catch (Exception e) {
 			error("Could not start the iOS application: %s ", e, projectDir.getAbsolutePath());
 			String message = removeUnWantedDetailsFromException(e);
 			throw new CalabashException(String.format(
 					"Failed to start iOS application. %s", message));
 		}
 	}
 
 	private String removeUnWantedDetailsFromException(Exception e) {
 		String message = e.getMessage();
 		message = message.replace("Make sure you are running this command from your project directory, \n", "");
 		message = message.replace("i.e., the directory containing your .xcodeproj file.\n", "");
 		message = message.replace("In features/support/01_launch.rb set APP_BUNDLE_PATH to\n", "set APP_BUNDLE_PATH to\n");
 		return message;
 	}
 
 	public RubyArray query(String query, String... args)
 			throws CalabashException {
 		try {
 			info("Executing query - %s", query);
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core");
 
 			container.put("cjQueryString", query);
 			container.put("cjQueryArgs", args);
 
 			RubyArray queryResults = null;
 			if (args != null && args.length > 0)
 				queryResults = (RubyArray) container
 						.runScriptlet("query(cjQueryString, *cjQueryArgs)");
 			else
 				queryResults = (RubyArray) container
 						.runScriptlet("query(cjQueryString)");
 
 			return queryResults;
 		} catch (Exception e) {
 			error("Execution of query: %s, failed", e, query);
 			throw new CalabashException(String.format(
 					"Failed to execute '%s'. %s", query, e.getMessage()));
 		}
 	}
 
 	public void touch(String query) throws CalabashException {
 		try {
 			info("Touching - %s", query);
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core",
 					"Calabash::Cucumber::Operations");
 			container.put("cjQueryString", query);
 			container.runScriptlet("touch(cjQueryString)");
 		} catch (Exception e) {
 			error("Failed to touch on: %s", e, query);
 			throw new CalabashException(String.format(
 					"Failed to touch on: %s. %s", query, e.getMessage()));
 		}
 	}
 
 	public void flash(String query) throws CalabashException {
 		try {
 			info("Flashing: %s", query);
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core");
 			container.put("cjQueryString", query);
 			container.runScriptlet("flash(cjQueryString)");
 		} catch (Exception e) {
 			error("Failed to flash on: %s", e, query);
 			throw new CalabashException(String.format(
 					"Failed to flash on: %s. %s", query, e.getMessage()));
 		}
 	}
 
 	public void scroll(String query, ScrollDirection direction)
 			throws CalabashException {
 		try {
 			info("Scrolling: %s", query);
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core");
 			container.put("cjQueryString", query);
 			container.put("cjDirection", direction.getDirection());
 			container.runScriptlet("scroll(cjQueryString, cjDirection)");
 		} catch (Exception e) {
 			error("Failed to scroll: %s", e, query);
 			throw new CalabashException(String.format(
 					"Failed to scroll: %s. %s", query, e.getMessage()));
 		}
 	}
 
 	public void rotate(String direction) throws CalabashException {
 		try {
 			info("Rotating to %s", direction);
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core",
 					"Calabash::Cucumber::TestsHelpers");
 			container.put("cjDirection", direction);
 			container.runScriptlet("rotate(cjDirection.to_sym)");
 		} catch (Exception e) {
 			error("Failed to rotate to: %s", e, direction);
 			throw new CalabashException(String.format(
 					"Failed to rotate to: %s. %s", direction, e.getMessage()));
 		}
 	}
 
 	public void exit() throws CalabashException {
 		try {
 			info("Exiting iOS application");
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core");
 			container.runScriptlet("calabash_exit");
 		} catch (Exception e) {
 			error("Exitting iOS application failed.", e);
 			throw new CalabashException(String.format(
 					"Failed to exit application. %s", e.getMessage()));
 		}
 	}
 
 	public void serverVersion() throws CalabashException {
 		try {
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core");
 			container.runScriptlet("server_version");
 		} catch (Exception e) {
 			throw new CalabashException(String.format(
 					"Failed to check server version. %s", e.getMessage()));
 		}
 	}
 
 	public void takeScreenShot(File dir, String fileName)
 			throws CalabashException {
 		try {
 			info("Taking screenshot");
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core",
 					"Calabash::Cucumber::TestsHelpers");
 			container.put("cjPrefix", dir.getAbsolutePath() + "/");
 			container.put("cjFileName", fileName);
 			container
 					.runScriptlet("screenshot_embed(options={:prefix => cjPrefix, :name => cjFileName})");
 		} catch (Exception e) {
 			error("Failed to take screenshot.", e);
 			throw new CalabashException(String.format(
 					"Failed to take screenshot. %s", e.getMessage()));
 		}
 	}
 
 	public void enterText(String text) throws CalabashException {
 		try {
 			info("Entering text - %s", text);
 			container.clear();
			addRequiresAndIncludes("Calabash::Cucumber::Core");
 			container.put("cjTextToEnter", text);
 			container.runScriptlet("keyboard_enter_text(cjTextToEnter)");
 		} catch (Exception e) {
 			error("Failed to enter text: %s", e, text);
 			throw new CalabashException(String.format(
 					"Failed to enter text: %s. %s", text, e.getMessage()));
 		}
 	}
 
 	public void enterChar(String text) throws CalabashException {
 		try {
 			info("Entering character '%s'", text);
 			container.clear();
			addRequiresAndIncludes("Calabash::Cucumber::Core");
 			container.put("cjCharToEnter", text);
 			container.runScriptlet("keyboard_enter_char(cjCharToEnter)");
 		} catch (Exception e) {
 			error("Failed to enter character: %s", e, text);
 			throw new CalabashException(String.format(
 					"Failed to enter text: %s. %s", text, e.getMessage()));
 		}
 	}
 
 	public void done() throws CalabashException {
 		try {
 			info("Pressing done button");
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core");
 			container.runScriptlet("done");
 		} catch (Exception e) {
 			error("Failed to press done button", e);
 			throw new CalabashException(String.format(
 					"Failed to press 'done'. %s", e.getMessage()));
 		}
 	}
 
 	public void waitFor(ICondition condition, WaitOptions options)
 			throws CalabashException, OperationTimedoutException {
 		try {
 			info("Waiting for condition");
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core",
 					"Calabash::Cucumber::WaitHelpers");
 			container.put("cjWaitCondition", condition);
 			String waitOptionsHash = getWaitOptionsHash(options);
 			if (waitOptionsHash == null)
 				container.runScriptlet("wait_for { cjWaitCondition.test }");
 			else {
 				container.runScriptlet(String.format(
 						"wait_for(%s) { cjWaitCondition.test }",
 						waitOptionsHash));
 			}
 		} catch (Exception e) {
 			handleWaitException(e, options);
 		}
 	}
 
 	public void waitForElementsExist(String[] queries, WaitOptions options)
 			throws OperationTimedoutException, CalabashException {
 		try {
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core",
 					"Calabash::Cucumber::WaitHelpers");
 			container.put("cjWaitQueries", queries);
 			String waitOptionsHash = getWaitOptionsHash(options);
 			if (waitOptionsHash == null)
 				container
 						.runScriptlet("wait_for_elements_exist(cjWaitQueries.to_a)");
 			else
 				container.runScriptlet(String.format(
 						"wait_for_elements_exist(cjWaitQueries.to_a, %s)",
 						waitOptionsHash));
 		} catch (Exception e) {
 			handleWaitException(e, options);
 		}
 	}
 
 	public void waitForElementsToNotExist(String[] queries, WaitOptions options)
 			throws OperationTimedoutException, CalabashException {
 		try {
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core",
 					"Calabash::Cucumber::WaitHelpers");
 			container.put("cjWaitQueries", queries);
 			String waitOptionsHash = getWaitOptionsHash(options);
 			if (waitOptionsHash == null)
 				container
 						.runScriptlet("wait_for_elements_do_not_exist(cjWaitQueries.to_a)");
 			else
 				container
 						.runScriptlet(String
 								.format("wait_for_elements_do_not_exist(cjWaitQueries.to_a, %s)",
 										waitOptionsHash));
 		} catch (Exception e) {
 			handleWaitException(e, options);
 		}
 	}
 
 	public void scrollToRow(String query, int row) throws CalabashException {
 		try {
 			info("Scrolling to row '%d' for query - %s", row, query);
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core");
 			container.put("cjQueryString", query);
 			container.put("cjRow", row);
 			container.runScriptlet("scroll_to_row(cjQueryString, cjRow)");
 		} catch (Exception e) {
 			error("Failed to scroll to row '%d' for query - %s", e, row, query);
 			throw new CalabashException(String.format(
 					"Failed to scroll to row '%d' for query '%s'. %s", row,
 					query, e.getMessage()));
 		}
 	}
 
 	public void scrollToCell(String query, ScrollOptions options)
 			throws CalabashException {
 		try {
 			info("Scrolling to a cell for query - %s", query);
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core");
 			container.runScriptlet(String.format("scroll_to_cell(%s)",
 					getScrollOptionsHash(query, options)));
 		} catch (Exception e) {
 			error("Failed to scroll to a cell for query - %s", e, query);
 			throw new CalabashException(String.format(
 					"Failed to scroll to cell for query '%s'. %s", query,
 					e.getMessage()));
 		}
 	}
 
 	public void scrollThroughEachCell(String query, ScrollOptions options,
 			CellIterator callback) throws CalabashException {
 		try {
 			info("Starting to scroll through each cells for query - %s", query);
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core");
 			container.put("cjCallback", new ScrollThroughEachCellCallback(
 					callback, this));
 			String scrollOptionsHash = getScrollOptionsHash(query, options);
 			String script = "each_cell(%s) do |row, sec|\n"
 					+ "q = \"tableViewCell indexPath:#{row},#{sec} label\"\n"
 					+ "element = query(q)\n"
 					+ "cjCallback.onEachCell(row, sec, q, element)\n" + "end";
 			container.runScriptlet(String.format(script, scrollOptionsHash));
 		} catch (Exception e) {
 			error("Failed to scroll through each cells for query - %s", e, query);
 			throw new CalabashException(String.format(
 					"Failed to scroll through each cell for query '%s'. %s",
 					query, e.getMessage()));
 		}
 	}
 
 	public class ScrollThroughEachCellCallback {
 
 		private final CellIterator iterator;
 		private final CalabashWrapper wrapper;
 
 		public ScrollThroughEachCellCallback(CellIterator iterator,
 				CalabashWrapper wrapper) {
 			this.iterator = iterator;
 			this.wrapper = wrapper;
 		}
 
 		public void onEachCell(int row, int section, String query,
 				RubyArray array) throws Exception {
 			UIElement element = null;
 			try {
 				UIElements elements = new UIElements(array, query, wrapper);
 				if (elements.size() > 0)
 					element = elements.get(0);
 			} catch (CalabashException e) {
 				element = null;
 			}
 
 			iterator.onEachCell(row, section, element);
 		}
 
 	}
 
 	private void handleWaitException(Exception e, WaitOptions options)
 			throws OperationTimedoutException, CalabashException {
 		if (e.toString().contains("Calabash::Cucumber::WaitHelpers::WaitError")) {
 			String message = null;
 			if (options != null)
 				message = options.getTimeoutMessage();
 
 			error("Timedout waiting");
 			throw new OperationTimedoutException(
 					message == null ? "Timed out waiting..." : message);
 		} else {
 			error("Failed to wait for condition. %s", e, e.getMessage());
 			throw new CalabashException(String.format(
 					"Failed to wait for condition. %s", e.getMessage()));
 		}
 	}
 
 	private String getWaitOptionsHash(WaitOptions options) {
 		if (options == null)
 			return null;
 		else {
 			container.put("cjWaitTimeout", options.getTimeoutInSec());
 			container.put("cjWaitRetryFreq", options.getRetryFreqInSec());
 			container.put("cjWaitPostTimeout", options.getPostTimeoutInSec());
 			container.put("cjWaitTimeoutMessage", options.getTimeoutMessage());
 			container.put("cjWaitShouldTakeScreenshot",
 					options.shouldScreenshotOnError());
 			return "{:timeout => cjWaitTimeout, :retry_frequency => cjWaitRetryFreq, :post_timeout => cjWaitPostTimeout, :timeout_message => cjWaitTimeoutMessage, :screenshot_on_error => cjWaitShouldTakeScreenshot}";
 		}
 	}
 
 	private String getScrollOptionsHash(String query, ScrollOptions options) {
 		container.put("cjQueryString", query);
 		if (options != null) {
 			container.put("cjScrollRow", options.getRow());
 			container.put("cjScrollSection", options.getSection());
 			container.put("cjScrollPosition", options.getDirection()
 					.getDirection());
 			container.put("cjScrollAnimate", options.shouldAnimate());
 			return "{:query => cjQueryString, :row => cjScrollRow, :section => cjScrollSection, :scroll_position => cjScrollPosition.to_sym, :animate => cjScrollAnimate}";
 		}
 		return "{:query => cjQueryString}";
 	}
 
 	public void waitForNoneAnimating() throws CalabashException {
 		try {
 			info("Waiting for all the animations to finish");
 			container.clear();
 			addRequiresAndIncludes("Calabash::Cucumber::Core",
 					"Calabash::Cucumber::WaitHelpers");
 			container.runScriptlet("wait_for_none_animating");
 		} catch (Exception e) {
 			error("Error waiting for all the animations to finish", e);
 			throw new CalabashException(String.format("Failed to wait. %s",
 					e.getMessage()));
 		}
 	}
 
 	private void addRequiresAndIncludes(String... modules) {
 		StringBuilder script = new StringBuilder(
 				"require 'calabash-cucumber'\n");
 		for (String module : modules) {
 			script.append("include " + module);
 			script.append("\n");
 		}
 
 		// HACK - Calabash ruby calls embed method when there is a error.
 		// This is from cucumber and won't be available in the Jruby
 		// environment. So just defining a function to suppress the error
 		if (configuration != null && configuration.getScreenshotListener() != null) {
 			container.put("@cjScreenshotCallback",
 					configuration.getScreenshotListener());
 			script.append("def embed(path,image_type,file_name)\n @cjScreenshotCallback.screenshotTaken(path, image_type, file_name)\n end\n");
 		} else {
 			script.append("def embed(path,image_type,file_name)\nend\n");
 		}
 
 		container.runScriptlet(script.toString());
 	}
 
 	public String getGemsDir() {
 		return gemsDir.getAbsolutePath();
 	}
 
 	private final void initializeScriptingContainer() throws CalabashException {
 		HashMap<String, String> environmentVariables = new HashMap<String, String>();
 		environmentVariables.put("PROJECT_DIR", projectDir.getAbsolutePath());
 		environmentVariables.put("HOME", System.getProperty("user.home"));
 		if (configuration != null) {
 			environmentVariables.put("SCREENSHOT_PATH", configuration
 					.getScreenshotsDirectory().getAbsolutePath() + "/");
 
 			if (configuration.getDevice() != null
 					&& configuration.getDevice().length() != 0)
 				environmentVariables.put("DEVICE", configuration.getDevice());
 
 			if (configuration.getAppBundlePath() != null
 					&& configuration.getAppBundlePath().length() != 0)
 				environmentVariables.put("APP_BUNDLE_PATH",
 						configuration.getAppBundlePath());
 
 			if (configuration.getDeviceEndPoint() != null)
 				environmentVariables.put("DEVICE_ENDPOINT", configuration
 						.getDeviceEndPoint().toString());
 
 			if (configuration.getNoLaunch())
 				environmentVariables.put("NO_LAUNCH", "1");
 		}
 
 		// Adding all system defined env variables
 		environmentVariables.putAll(System.getenv());
 
 		container.setEnvironment(environmentVariables);
 
 		// Load paths points to the gem directory
 		container.getLoadPaths().addAll(getLoadPaths());
 
 		// No stderr
 		container.setErrorWriter(new StringWriter());
 	}
 
 	private List<String> getLoadPaths() throws CalabashException {
 		ArrayList<String> loadPaths = new ArrayList<String>();
 		File[] gems = gemsDir.listFiles(new FileFilter() {
 
 			@Override
 			public boolean accept(File arg0) {
 				return arg0.isDirectory();
 			}
 		});
 
 		if (gems == null || gems.length == 0)
 			throw new CalabashException("Couldn't find any gems inside "
 					+ gemsDir.getAbsolutePath());
 
 		for (File gem : gems) {
 			File libPath = new File(gem, "lib");
 			loadPaths.add(libPath.getAbsolutePath());
 		}
 
 		return loadPaths;
 	}
 
 	private File getCalabashGemDirectory() throws CalabashException {
 		File[] calabashGemPath = gemsDir.listFiles(new FileFilter() {
 			public boolean accept(File pathname) {
 				return pathname.isDirectory()
 						&& pathname.getName().startsWith("calabash-cucumber");
 			}
 		});
 
 		if (calabashGemPath.length == 0)
 			throw new CalabashException(String.format(
 					"Error finding 'calabash-cucumber' in the gempath : %s",
 					gemsDir.getAbsolutePath()));
 
 		if (calabashGemPath.length > 1)
 			throw new CalabashException(
 					String.format(
 							"Multiple matches for 'calabash-cucumber' in the gempath : %s",
 							gemsDir.getAbsolutePath()));
 
 		return calabashGemPath[0];
 	}
 
 }
