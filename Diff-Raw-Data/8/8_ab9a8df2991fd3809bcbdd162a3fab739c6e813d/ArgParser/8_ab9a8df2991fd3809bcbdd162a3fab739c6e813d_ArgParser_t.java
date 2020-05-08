 package org.tsaikd.java.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 public class ArgParser {
 
 	static Log log = LogFactory.getLog(ArgParser.class);
 	private String version;
 	private LinkedList<ArgParser.Option> opts = new LinkedList<>();
 	private HashSet<Class<?>> optcSet = new HashSet<>();
 	private CommandLine cmd;
 	private int helpWidth = 80;
 	private HashMap<String, Object> optMap = new HashMap<>();
 
 	static public class Option {
 		String opt = null;
 		String longOpt = null;
 		boolean hasArg = false;
 		Object defaultArg = null;
 		String description = null;
 
 		public Option(String opt, String longOpt, boolean hasArg, Object defaultArg, String description) {
 			this.opt = opt;
 			this.longOpt = longOpt;
 			this.hasArg = hasArg;
 			this.defaultArg = defaultArg;
 
 			String desc;
 			if (description != null) {
 				desc = description;
 			} else {
 				desc = "";
 			}
 
 			if (hasArg) {
 				String confArg;
 				if (defaultArg == null) {
 					confArg = ConfigUtils.get(longOpt);
 				} else {
 					confArg = ConfigUtils.get(longOpt, defaultArg.toString());
 				}
 
 				if (confArg != null) {
 					if (desc.length() > 0) {
 						desc += "\n";
 					}
 					if (confArg.isEmpty()) {
 						desc += "Default: \"\"";
 					} else {
 						desc += "Default: " + confArg;
 					}
 				}
 			}
 			if (desc.length() < 1) {
 				desc = null;
 			}
 			this.description = desc;
 		}
 
 		public Option(String opt, String longOpt, String description) {
 			this.opt = opt;
 			this.longOpt = longOpt;
 			this.description = description;
 		}
 
 	}
 
 	public static String getPomVersion() {
 		String version = ConfigUtils.getSearchBaseVersion();
 		if (version != null) {
 			return version;
 		}
 		File filePom = ConfigUtils.searchPropFromFile("pom.xml");
 		if (filePom != null) {
 			try {
 				Document docPom = XPathUtils.parseDocumentr(filePom);
 				if (docPom != null) {
 					Node node = XPathUtils.selectSingleNode(docPom, "/project/version/text()");
 					if (node != null) {
 						return node.getNodeValue();
 					}
 				}
 			} catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	public ArgParser() {
 		addOpt(new Option("h", "help", false, false, "Show help message"));
 		this.version = getPomVersion();
 	}
 
 	public ArgParser(String version) {
 		addOpt(new Option("h", "help", false, false, "Show help message"));
 		this.version = version;
 	}
 
 	public ArgParser(Class<?> optc) {
 		addOpt(new Option("h", "help", false, false, "Show help message"));
 		this.version = getPomVersion();
 		addOpt(optc);
 	}
 
 	public ArgParser(String version, Class<?> optc) {
 		addOpt(new Option("h", "help", false, false, "Show help message"));
 		this.version = version;
 		addOpt(optc);
 	}
 
 	public ArgParser addOpt(Option opt) {
 		this.opts.add(opt);
 		return this;
 	}
 
 	public ArgParser addOpt(Option[] opts) {
 		for (Option opt : opts) {
 			addOpt(opt);
 		}
 		return this;
 	}
 
 	public ArgParser addOpt(Class<?> optc) {
 		if (optcSet.contains(optc)) {
 			return this;
 		}
 		optcSet.add(optc);
 
 		Field fopts;
 		ArgParser.Option[] opts = null;
 
 		try {
 			fopts = optc.getDeclaredField("opts");
 			opts = (ArgParser.Option[]) fopts.get(optc);
 		} catch (NoSuchFieldException e) {
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		if (opts != null) {
 			addOpt(opts);
 		}
 
 		Field foptDep;
 		Class<?>[] optDep2 = null;
 
 		try {
 			foptDep = optc.getDeclaredField("optDep");
 			optDep2 = (Class<?>[]) foptDep.get(optc);
 		} catch(NoSuchFieldException e) {
 		} catch(IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		if (optDep2 != null) {
 			addOpt(optDep2);
 		}
 
 		return this;
 	}
 
 	public ArgParser addOpt(Class<?>[] optDep) {
 		for (Class<?> optc : optDep) {
 			addOpt(optc);
 		}
 		return this;
 	}
 
 	public Object getOpt(String key) {
 		return optMap.get(key);
 	}
 
 	public String getOptString(String key) {
 		if (optMap.containsKey(key)) {
 			Object value = optMap.get(key);
 			if (value != null) {
 				return value.toString();
 			}
 		}
 		return null;
 	}
 
 	public Integer getOptInt(String key) {
 		String value = getOptString(key);
 		if (value != null) {
 			return Integer.parseInt(value);
 		}
 		return null;
 	}
 
 	public Long getOptLong(String key) {
 		String value = getOptString(key);
 		if (value != null) {
 			return Long.parseLong(value);
 		}
 		return null;
 	}
 
 	public Boolean getOptBoolean(String key) {
 		String value = getOptString(key);
 		if (value != null) {
 			return Boolean.parseBoolean(value);
 		}
		return false;
 	}
 
 	public ArgParser parse(String[] args, boolean writeConfigUtils) throws ParseException {
 		Options options = new Options();
 
 		for (Option opt : opts) {
 			options.addOption(opt.opt, opt.longOpt, opt.hasArg, opt.description);
 		}
 
 		cmd = new PosixParser().parse(options, args);
 
 		if (cmd.hasOption('h') || cmd.hasOption("help")) {
 			printHelp(options, null);
 			System.exit(0);
 			return this;
 		}
 
 		String key;
 		for (Option opt : opts) {
 			key = opt.opt;
 			if (key != null) {
 				if (cmd.hasOption(key)) {
 					if (opt.hasArg) {
 						optMap.put(key, cmd.getOptionValue(key));
 					} else {
 						optMap.put(key, true);
 					}
 				} else if (!optMap.containsKey(key)) {
 					optMap.put(key, opt.defaultArg);
 				}
 				if (writeConfigUtils) {
					ConfigUtils.set(key, String.valueOf(optMap.get(key)));
 				}
 			}
 
 			key = opt.longOpt;
 			if (key != null) {
 				if (cmd.hasOption(key)) {
 					if (opt.hasArg) {
 						optMap.put(key, cmd.getOptionValue(key));
 					} else {
 						optMap.put(key, true);
 					}
 				} else if (!optMap.containsKey(key)) {
 					optMap.put(key, opt.defaultArg);
 				}
 				if (writeConfigUtils) {
					ConfigUtils.set(key, String.valueOf(optMap.get(key)));
 				}
 			}
 		}
 
 		return this;
 	}
 
 	public ArgParser parse(String[] args) throws ParseException {
 		return parse(args, true);
 	}
 
 	public String getParsedMap() {
 		HashMap<String, Object> outmap = new HashMap<>();
 		String key;
 		for (Option opt : opts) {
 			key = opt.opt;
 			if (key != null) {
 				if (opt.hasArg) {
 					if (optMap.get(key) != opt.defaultArg) {
 						outmap.put(key, optMap.get(key));
 					}
 				} else {
 					if (optMap.get(key) == Boolean.TRUE) {
 						outmap.put(key, optMap.get(key));
 					}
 				}
 			}
 
 			key = opt.longOpt;
 			if (key != null) {
 				if (opt.hasArg) {
 					if (optMap.get(key) != opt.defaultArg) {
 						outmap.put(key, optMap.get(key));
 					}
 				} else {
 					if (optMap.get(key) == Boolean.TRUE) {
 						outmap.put(key, optMap.get(key));
 					}
 				}
 			}
 		}
 		return outmap.toString();
 	}
 
 	public void printHelp(Options options, String footer) {
 		HelpFormatter helpFmt = new HelpFormatter();
 		helpFmt.setWidth(getHelpWidth());
 
 		String appName = null;
 		for (int i=2 ; i<5 ; i++) {
 			appName = ClassUtils.getClassName(false, i);
 			if (!appName.equals(ArgParser.class.getSimpleName())) {
 				break;
 			}
 		}
 		helpFmt.printHelp(appName, "Version: "
 			+ version, options, footer, true);
 	}
 
 	public void printHelp(String footer) {
 		Options options = new Options();
 
 		for (Option opt : opts) {
 			options.addOption(opt.opt, opt.longOpt, opt.hasArg, opt.description);
 		}
 
 		printHelp(options, footer);
 	}
 
 	public void printHelp() {
 		printHelp(null);
 	}
 
 	public CommandLine getCmd() {
 		return cmd;
 	}
 
 	public int getHelpWidth() {
 		String cols = ConfigUtils.get("COLUMNS");
 		if (cols != null && !cols.isEmpty()) {
 			return Integer.parseInt(cols);
 		}
 		return helpWidth;
 	}
 
 	public void setHelpWidth(int helpWidth) {
 		this.helpWidth = helpWidth;
 	}
 
 }
