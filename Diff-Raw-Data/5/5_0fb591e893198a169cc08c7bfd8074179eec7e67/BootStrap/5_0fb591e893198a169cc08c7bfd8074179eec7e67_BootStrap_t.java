 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.main.guice.AppModule;
 import com.chinarewards.utils.appinfo.AppInfo;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.persist.PersistService;
 import com.google.inject.persist.jpa.JpaPersistModule;
 
 /**
  * Bootstrap class contains bootstrapping code.
  * <p>
  * 
  * The important methods are the {@link BootStrap#run()} and {@link #shutdown()}
  * , which should be called during application startup and termination
  * respectively.
  * 
  * @author cyril
  * @since 1.0.0
  */
 public class BootStrap {
 
 	Logger log = LoggerFactory.getLogger(getClass());
 
 	/**
 	 * Container for all dependency injection required objects.
 	 */
 	Injector injector;
 
 	/**
 	 * Command line arguments (raw).
 	 */
 	final String[] args;
 
 	/**
 	 * Contains parsed command line arguments, from <code>args</code>.
 	 */
 	CommandLine cl;
 
 	/**
 	 * Creates an instance of BootStrap.
 	 * 
 	 * @param args
 	 */
 	public BootStrap(String... args) {
 		this.args = args;
 	}
 
 	/**
 	 * Obtains the Guice injector created.
 	 * 
 	 * @return
 	 */
 	public Injector getInjector() {
 		return injector;
 	}
 
 	protected void printAppVersion() {
 		// print application version.
 		AppInfo appInfo = AppInfo.getInstance();
 		System.out
				.println("QQ Group Buying Validation POS Network Server version " + appInfo.getVersionString());
 
 	}
 
 	/**
 	 * Starts the bootstrap sequence.
 	 */
 	public void run() throws Exception {
 
 		// parse command line arguments.
 		parseCmdArgs();
 
 		printAppVersion();
 
 		log.info("Bootstrapping...");
 
 		// create the dependency injection environment.
 		createGuice();
 
 		// save the command line arguments
 		initAppPreference();
 
 		// start the persistence services
 		PersistService ps = injector.getInstance(PersistService.class);
 		ps.start();
 
 		log.info("Bootstrapping completed");
 
 	}
 
 	/**
 	 * Call this method to shutdown the system, including any system initialized
 	 * in the {@link #run()} method.
 	 * <p>
 	 */
 	public void shutdown() {
 
 		log.info("Shutdown sequence began");
 
 		// shut down in reverse order as found in method run()!
 
 		shutdownJpa();
 
 		log.info("Shutdown sequence done");
 	}
 
 	protected void shutdownJpa() {
 
 		PersistService ps = injector.getInstance(PersistService.class);
 		try {
 			log.info("Shutting down persistence service");
 			ps.stop();
 		} catch (Throwable t) {
 			log.warn("Error occurred when shutting down persistence service", t);
 		}
 
 	}
 
 	/**
 	 * Parse command line arguments.
 	 */
 	protected void parseCmdArgs() {
 
 		Options opts = buildCmdArgOpts();
 		CommandLineParser parser = new SimpleParser();
 		try {
 			cl = parser.parse(opts, args);
 		} catch (ParseException e) {
 			System.err.println(e.getMessage());
 			printHelp(opts);
 			System.exit(1);
 		}
 
 		// help message
 		if (cl.hasOption("help")) {
 			printHelp(opts);
 			System.exit(0);
 		}
 
 		// no argument is given.
 		if (cl.getOptions().length == 0) {
 			printHelp(opts);
 			System.exit(1);
 		}
 
 		// version
 		if (cl.hasOption("version")) {
 			AppInfo appInfo = AppInfo.getInstance();
			System.out.println("QQ Group Buying Validation POS Network Server version "
 					+ appInfo.getVersionString());
 			System.exit(0);
 		}
 
 		if (cl.getOptionValue("reply-per-loop") != null) {
 			Pattern pattern = Pattern.compile("^[-+]?[0-9]*");
 			Matcher isNum = pattern
 					.matcher(cl.getOptionValue("reply-per-loop"));
 			if (!isNum.matches()) {
 				System.err
 						.println("appPreference replyPerLoop is not numeric!");
 				printHelp(opts);
 				System.exit(1);
 			} else {
 				if (Integer.parseInt(cl.getOptionValue("reply-per-loop")) < 0) {
 					System.err
 							.println("appPreference replyPerLoop is less then or equal zero");
 					System.exit(1);
 				}
 			}
 		}
 
 		if (cl.getOptionValue("pause-per-reply") != null) {
 			Pattern pattern = Pattern.compile("^[-+]?[0-9]*");
 			Matcher isNum = pattern.matcher(cl
 					.getOptionValue("pause-per-reply"));
 			if (!isNum.matches()) {
 				System.err
 						.println("appPreference pausePerReply is not numeric!");
 				printHelp(opts);
 				System.exit(1);
 			} else {
 				if (Integer.parseInt(cl.getOptionValue("pause-per-reply")) < 0) {
 					System.err
 							.println("appPreference pausePerReply is less then or equal zero");
 					System.exit(1);
 				}
 			}
 		}
 	}
 
 	/**
 	 * United way to print command usage.
 	 * 
 	 * @param options
 	 */
 	protected void printHelp(Options options) {
 		HelpFormatter f = new HelpFormatter();
 		f.printHelp("java -jar microblogger.jar", options, true);
 	}
 
 	@SuppressWarnings("static-access")
 	protected Options buildCmdArgOpts() {
 
 		// for usage, see link: http://commons.apache.org/cli/usage.html
 
 		Options main = new Options();
 
 		// create Options object
 		{
 			// Options options = new Options();
 			Options options = main;
 
 			options.addOption(OptionBuilder.withLongOpt("db").isRequired()
 					.withArgName("database").hasArg().withDescription("数据库名称")
 					.create());
 			options.addOption(OptionBuilder.withLongOpt("dbtype").isRequired()
 					.withArgName("database_type").hasArg()
 					.withDescription("数据库类型。目前支持：mysql").create());
 			options.addOption(OptionBuilder.withLongOpt("dbhost").isRequired()
 					.withArgName("host").hasArg().withDescription("数据库主机/IP地址")
 					.create());
 			options.addOption(OptionBuilder.withLongOpt("dbuser").isRequired()
 					.withArgName("username").hasArg().withDescription("数据库用户名")
 					.create());
 			options.addOption(OptionBuilder.withLongOpt("dbpass").isRequired()
 					.withArgName("password").hasArg().withDescription("数据库密码")
 					.create());
 			// Sina microblog related
 			options.addOption(OptionBuilder.withLongOpt("sinauser")
 					.withArgName("username").isRequired().hasArg()
 					.withDescription("新浪微博用户名").create());
 			options.addOption(OptionBuilder.withLongOpt("sinapass")
 					.withArgName("password").isRequired().hasArg()
 					.withDescription("新浪微博密码").create());
 			//
 			options.addOption(OptionBuilder.withLongOpt("reply-per-loop")
 					.hasArg().withDescription("每个循环的最大数量的回复")
 					.withType(new Integer(1)).create());
 
 			//
 			options.addOption(OptionBuilder.withLongOpt("pause-per-reply")
 					.hasArg().withDescription("回复间隔时间")
 					.withType(new Integer(1)).create());
 
 			OptionGroup group = new OptionGroup();
 			// --comment
 			group.addOption(OptionBuilder.withLongOpt("comment")
 					.withArgName("text").isRequired().hasArg()
 					.withDescription("该评论内容将回复所有微博").create("c"));
 
 			// FIXME add a parameter --commentfile <path>, which accepts a text
 			// file,
 			// each line contains one reply.
 			// this argument should be mutually exclusive to the '--comment'
 			// argument,
 			// only one of them should be present.
 			// apache Commons CLI - OptionGroup
 
 			group.addOption(OptionBuilder.withLongOpt("commentfile")
 					.withArgName("path").isRequired().hasArg()
 					.withDescription("评论文件路径").create("cf"));
 			options.addOptionGroup(group);
 
 		}
 
 		// optional parameters
 		{
 			// --verbose
 			main.addOption(OptionBuilder.withLongOpt("verbose").hasArg()
 					.withDescription("详细级别.支持0,1,小数等调试信息。默认为0").create());
 
 		}
 
 		{
 			Option help = OptionBuilder.withArgName("help").withLongOpt("help")
 					.withDescription("打印消息").create('h');
 			Options options = main;
 			options.addOption(help);
 		}
 
 		// version
 		main.addOption(OptionBuilder.withLongOpt("version")
 				.withDescription("显示版本").create());
 
 		return main;
 	}
 
 	/**
 	 * Initialize the app preference. This must be done AFTER the Guice injector
 	 * is created.
 	 */
 	protected void initAppPreference() {
 
 		AppPreference pref = injector.getInstance(AppPreference.class);
 
 		// initialize the Application Preference object using command line
 		// arguments.
 		pref.setWeiboUsername(cl.getOptionValue("sinauser"));
 		pref.setWeiboPassword(cl.getOptionValue("sinapass"));
 		pref.setDb(cl.getOptionValue("db"));
 		pref.setDbType(cl.getOptionValue("dbtype"));
 		pref.setDbUsername(cl.getOptionValue("dbuser"));
 		pref.setDbPassword(cl.getOptionValue("dbpass"));
 		// TODO allow command line arguments for specifying province and city
 		pref.setSinaProvinceId("44");
 		pref.setSinaCityId("3");
 		if (cl.getOptionValue("reply-per-loop") != null) {
 			pref.setReplyPerLoop(new Integer(cl
 					.getOptionValue("reply-per-loop")));
 		}
 		if (cl.getOptionValue("pause-per-reply") != null) {
 			pref.setPausePerReply(new Integer(cl
 					.getOptionValue("pause-per-reply")));
 		}
 
 		// set comment and commentfile
 		if (cl.getOptionValue("comment") != null) {
 			pref.setMicroblogComments(cl.getOptionValues("comment"));
 		}
 		if (cl.getOptionValue("commentfile") != null) {
 			pref.setCommentFile(cl.getOptionValue("commentfile"));
 		}
 
 		// verbose level
 		{
 			String raw = cl.getOptionValue("verbose");
 			int level = 0;
 			if (raw != null) {
 				try {
 					level = Integer.parseInt(raw);
 				} catch (NumberFormatException e) {
 					// use default
 				}
 				if (level < 0 || level > 1) {
 					level = 0;
 				}
 			}
 
 			com.chinarewards.qqgbvpn.main.LogConfig l = injector
 					.getInstance(com.chinarewards.qqgbvpn.main.LogConfig.class);
 			l.setVerboseLevel(level);
 		}
 
 	}
 
 	/**
 	 * Create the guice injector environment.
 	 */
 	protected void createGuice() {
 
 		log.info("Initializing dependency injection environment...");
 
 		//
 		// JPA
 		//
 
 		String db = cl.getOptionValue("db");
 		String dbType = cl.getOptionValue("dbtype");
 		String dbUsername = cl.getOptionValue("dbuser");
 		String dbPassword = cl.getOptionValue("dbpass");
 		@SuppressWarnings("unused")
 		String dbHost = cl.getOptionValue("dbhost");
 
 		// prepare the persistence module
 		JpaPersistModule jpaModule = new JpaPersistModule("microblogger");
 		Properties props = buildJpaProperties(dbType, dbUsername, dbPassword,
 				db);
 		jpaModule.properties(props);
 
 		// prepare Guice injector
 		log.debug("Bootstraping Guice injector...");
 		injector = Guice.createInjector(new AppModule(), jpaModule);
 
 	}
 
 	protected Properties buildJpaProperties(String dbType, String dbUsername,
 			String dbPassword, String db) {
 
 		Properties props = new Properties();
 		props.setProperty("hibernate.hbm2ddl.auto", "update");
 		props.setProperty("hibernate.connection.username", dbUsername);
 		props.setProperty("hibernate.connection.password", dbPassword);
 		// props.setProperty("hibernate.show_sql", "true");
 
 		// determine the db type
 		if ("mysql".equals(dbType)) {
 			props.setProperty("hibernate.dialect",
 					"org.hibernate.dialect.MySQL5Dialect");
 			props.setProperty("hibernate.connection.driver_class",
 					"com.mysql.jdbc.Driver");
 			// necessary to add the parameters in the end of
 			// URL related to encoding
 			props.setProperty(
 					"hibernate.connection.url",
 					"jdbc:mysql://"
 							+ cl.getOptionValue("dbhost")
 							+ ":3306/"
 							+ db
 							+ "?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8");
 		} else if ("hsql".equals(dbType)) {
 			props.setProperty("hibernate.dialect",
 					"org.hibernate.dialect.HSQLDialect");
 			props.setProperty("hibernate.connection.driver_class",
 					"org.hsqldb.jdbcDriver");
 			props.setProperty("hibernate.connection.url", "jdbc:hsqldb:.");
 		}
 
 		return props;
 
 	}
 
 }
