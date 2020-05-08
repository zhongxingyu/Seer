 package nl.cyso.vcloud.client;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Options;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 
 public class Configuration {
 	/**
 	 * Login credentials : username; password; server;
 	 */
 
 	/**
 	 * Mode: mode; listType;
 	 */
 
 	/**
 	 * Selectors: organization, vdc, vapp, vm, catalog
 	 */
 
 	/**
 	 * User input: fqdn; description; template; ip; network;
 	 */
 
 	private static Map<String, Object> configuration = new HashMap<String, Object>();
 
 	private static Options options = null;
 
 	private static boolean has(String key) {
 		return Configuration.configuration.containsKey(key);
 	}
 
 	private static void set(String key, Object value) {
 		Configuration.configuration.put(key, value);
 	}
 
 	private static Object valueOrNull(String key) {
 		if (Configuration.configuration.containsKey(key)) {
 			return Configuration.configuration.get(key);
 		} else {
 			return null;
 		}
 	}
 
 	protected static boolean hasUsername() {
 		return Configuration.has("username");
 	}
 
 	protected static String getUsername() {
 		return (String) Configuration.valueOrNull("username");
 	}
 
 	protected static void setUsername(String username) {
 		Configuration.set("username", username);
 	}
 
 	protected static boolean hasPassword() {
 		return Configuration.has("password");
 	}
 
 	protected static String getPassword() {
 		return (String) Configuration.valueOrNull("password");
 	}
 
 	protected static void setPassword(String password) {
 		Configuration.set("password", password);
 	}
 
 	protected static boolean hasServer() {
 		return Configuration.has("server");
 	}
 
 	protected static String getServer() {
 		return (String) Configuration.valueOrNull("server");
 	}
 
 	protected static void setServer(String server) {
 		Configuration.set("server", server);
 	}
 
 	protected static boolean hasMode() {
 		return Configuration.has("mode");
 	}
 
 	protected static ModeType getMode() {
 		return (ModeType) Configuration.valueOrNull("mode");
 	}
 
 	protected static void setMode(ModeType mode) {
 		Configuration.set("mode", mode);
 	}
 
 	protected static boolean hasVDC() {
 		return Configuration.has("vdc");
 	}
 
 	protected static String getVDC() {
 		return (String) Configuration.valueOrNull("vdc");
 	}
 
 	protected static void setVDC(String vdc) {
 		Configuration.set("vdc", vdc);
 	}
 
 	protected static boolean hasVApp() {
 		return Configuration.has("vapp");
 	}
 
 	protected static String getVApp() {
 		return (String) Configuration.valueOrNull("vapp");
 	}
 
 	protected static void setVApp(String vapp) {
 		Configuration.set("vapp", vapp);
 	}
 
 	protected static boolean hasVM() {
 		return Configuration.has("vm");
 	}
 
 	protected static String getVM() {
 		return (String) Configuration.valueOrNull("vm");
 	}
 
 	protected static void setVM(String vm) {
 		Configuration.set("vm", vm);
 	}
 
 	protected static boolean hasCatalog() {
 		return Configuration.has("catalog");
 	}
 
 	protected static String getCatalog() {
 		return (String) Configuration.valueOrNull("catalog");
 	}
 
 	protected static void setCatalog(String catalog) {
 		Configuration.set("catalog", catalog);
 	}
 
 	protected static boolean hasOrganization() {
 		return Configuration.has("organization");
 	}
 
 	protected static String getOrganization() {
 		return (String) Configuration.valueOrNull("organization");
 	}
 
 	protected static void setOrganization(String organization) {
 		Configuration.set("organization", organization);
 	}
 
 	protected static boolean hasListType() {
 		return Configuration.has("listType");
 	}
 
 	protected static ListType getListType() {
 		return (ListType) Configuration.valueOrNull("listType");
 	}
 
 	protected static void setListType(ListType listType) {
 		Configuration.set("listType", listType);
 	}
 
 	protected static boolean hasFqdn() {
 		return Configuration.has("fqdn");
 	}
 
 	protected static String getFqdn() {
 		return (String) Configuration.valueOrNull("fqdn");
 	}
 
 	protected static void setFqdn(String fqdn) {
 		Configuration.set("fqdn", fqdn);
 	}
 
 	protected static boolean hasDescription() {
 		return Configuration.has("description");
 	}
 
 	protected static String getDescription() {
 		return (String) Configuration.valueOrNull("description");
 	}
 
 	protected static void setDescription(String description) {
 		Configuration.set("description", description);
 	}
 
 	protected static boolean hasTemplate() {
 		return Configuration.has("template");
 	}
 
 	protected static String getTemplate() {
 		return (String) Configuration.valueOrNull("template");
 	}
 
 	protected static void setTemplate(String template) {
 		Configuration.set("template", template);
 	}
 
 	protected static boolean hasIp() {
		return Configuration.has("username");
 	}
 
 	protected static InetAddress getIp() {
 		return (InetAddress) Configuration.valueOrNull("ip");
 	}
 
 	protected static void setIp(InetAddress ip) {
 		Configuration.set("ip", ip);
 	}
 
 	protected static void setIp(String ip) throws UnknownHostException {
 		Configuration.set("ip", InetAddress.getByName(ip));
 	}
 
 	protected static boolean hasNetwork() {
 		return Configuration.has("network");
 	}
 
 	protected static String getNetwork() {
 		return (String) Configuration.valueOrNull("network");
 	}
 
 	protected static void setNetwork(String network) {
 		Configuration.set("network", network);
 	}
 
 	@SuppressWarnings("static-access")
 	protected static Options getOptions() {
 		if (Configuration.options == null) {
 			Options opt = new Options();
 
 			// Configuration file
 			opt.addOption("c", "config", true, "Use this configuration file");
 
 			// Connection options
 			opt.addOption("u", "username", true, "vCloud Director username");
 			opt.addOption("p", "password", true, "vCloud Director password");
 			opt.addOption("s", "server", true, "vCloud Director server URI");
 
 			// Modes
 			OptionGroup modes = new OptionGroup();
 			modes.addOption(new Option("h", "help", false, "Show help and examples"));
 			modes.addOption(new Option("v", "version", false, "Show version information"));
 			modes.addOption(new Option("l", "list", true, "List vCloud objects (org|vdc|vapp|catalog|vm)"));
 			modes.addOption(new Option("a", "add-vm", false, "Add a new VM from a vApp Template to an existing vApp"));
 			modes.addOption(new Option("r", "remove-vm", false, "Remove a VM from an existing vApp"));
 			modes.setRequired(true);
 			opt.addOptionGroup(modes);
 
 			// Selectors
 			opt.addOption(OptionBuilder.withLongOpt("organization").hasArg().withArgName("ORG").withDescription("Select this Organization").create());
 			opt.addOption(OptionBuilder.withLongOpt("vdc").hasArg().withArgName("VDC").withDescription("Select this Virtual Data Center").create());
 			opt.addOption(OptionBuilder.withLongOpt("vapp").hasArg().withArgName("VAPP").withDescription("Select this vApp").create());
 			opt.addOption(OptionBuilder.withLongOpt("vm").hasArg().withArgName("VM").withDescription("Select this VM").create());
 			opt.addOption(OptionBuilder.withLongOpt("catalog").hasArg().withArgName("CATALOG").withDescription("Select this Catalog").create());
 
 			// User input
 			opt.addOption(OptionBuilder.withLongOpt("fqdn").hasArg().withArgName("FQDN").withDescription("Name of object to create").create());
 			opt.addOption(OptionBuilder.withLongOpt("description").hasArg().withArgName("DESC").withDescription("Description of object to create").create());
 			opt.addOption(OptionBuilder.withLongOpt("template").hasArg().withArgName("TEMPLATE").withDescription("Template of object to create").create());
 			opt.addOption(OptionBuilder.withLongOpt("ip").hasArg().withArgName("IP").withDescription("IP of the object to create").create());
 			opt.addOption(OptionBuilder.withLongOpt("network").hasArg().withArgName("NETWORK").withDescription("Network of the object to create").create());
 
 			Configuration.options = opt;
 		}
 
 		return Configuration.options;
 	}
 
 	protected static void load(CommandLine cli) {
 		for (Option opt : cli.getOptions()) {
 			if (cli.hasOption(opt.getLongOpt())) {
 				if (opt.getLongOpt().equals("help")) {
 					Configuration.setMode(ModeType.HELP);
 				} else if (opt.getLongOpt().equals("version")) {
 					Configuration.setMode(ModeType.VERSION);
 				} else if (opt.getLongOpt().equals("list")) {
 					Configuration.setMode(ModeType.LIST);
 					Configuration.setListType(ListType.valueOf(cli.getOptionValue(opt.getLongOpt()).toUpperCase()));
 				} else if (opt.getLongOpt().equals("add-vm")) {
 					Configuration.setMode(ModeType.ADDVM);
 				} else if (opt.getLongOpt().equals("remove-vm")) {
 					Configuration.setMode(ModeType.REMOVEVM);
 				} else if (opt.getLongOpt().equals("ip")) {
 					try {
 						Configuration.setIp(cli.getOptionValue(opt.getLongOpt()));
 					} catch (UnknownHostException uhe) {
 						Configuration.setIp((InetAddress) null);
 					}
 				} else {
 					Configuration.set(opt.getLongOpt(), cli.getOptionValue(opt.getLongOpt()));
 				}
 			}
 		}
 	}
 
 	protected static void loadFile(String filename) {
 		org.apache.commons.configuration.Configuration conf = null;
 		try {
 			conf = new PropertiesConfiguration(filename);
 		} catch (ConfigurationException e) {
 			System.err.println("Failed to load configuration file");
 			System.err.println(e.getLocalizedMessage());
 		}
 
 		Iterator<String> i = conf.getKeys();
 		while (i.hasNext()) {
 			String key = i.next();
 
 			if (key.equals("help")) {
 				Configuration.setMode(ModeType.HELP);
 			} else if (key.equals("version")) {
 				Configuration.setMode(ModeType.VERSION);
 			} else if (key.equals("list")) {
 				Configuration.setMode(ModeType.LIST);
 				Configuration.setListType(ListType.valueOf(conf.getString(key).toUpperCase()));
 			} else if (key.equals("addvm")) {
 				Configuration.setMode(ModeType.ADDVM);
 			} else if (key.equals("removevm")) {
 				Configuration.setMode(ModeType.REMOVEVM);
 			} else if (key.equals("ip")) {
 				try {
 					Configuration.setIp(conf.getString(key));
 				} catch (UnknownHostException uhe) {
 					Configuration.setIp((InetAddress) null);
 				}
 			} else {
 				Configuration.set(key, conf.getString(key));
 			}
 		}
 	}
 }
