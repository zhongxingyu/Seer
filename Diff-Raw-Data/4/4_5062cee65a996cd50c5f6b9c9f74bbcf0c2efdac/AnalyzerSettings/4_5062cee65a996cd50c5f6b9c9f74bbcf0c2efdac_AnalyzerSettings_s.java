 package jp.ac.osaka_u.ist.sdl.ectec.analyzer;
 
 import jp.ac.osaka_u.ist.sdl.ectec.settings.Language;
 import jp.ac.osaka_u.ist.sdl.ectec.settings.MessagePrintLevel;
 import jp.ac.osaka_u.ist.sdl.ectec.settings.VersionControlSystem;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 
 /**
  * A class to store settings of the analyzer
  * 
  * @author k-hotta
  * 
  */
 final class AnalyzerSettings {
 
 	/**
 	 * the path of the repository to be analyzed
 	 */
 	private final String repositoryPath;
 
 	/**
 	 * the path of the db file that stores the results of the analysis
 	 */
 	private final String dbPath;
 
 	/**
 	 * the additional path for analyzing a part of the repository
 	 */
 	private final String additionalPath;
 
 	/**
 	 * the target programming language
 	 */
 	private final Language language;
 
 	/**
 	 * the number of maximum threads
 	 */
 	private final int threads;
 
 	/**
 	 * the user name used to access the repository
 	 */
 	private final String userName;
 
 	/**
 	 * the password used to access the repository
 	 */
 	private final String passwd;
 
 	/**
 	 * the identifier of the start revision
 	 */
 	private final String startRevisionIdentifier;
 
 	/**
 	 * the identifier of the end revision
 	 */
 	private final String endRevisionIdentifier;
 
 	/**
 	 * the level of verbose output
 	 */
 	private final MessagePrintLevel verboseLevel;
 
 	/**
 	 * the version control system that manages the target repository
 	 */
 	private final VersionControlSystem versionControlSystem;
 
 	/**
 	 * the path of the properties file
 	 */
 	private final String propertiesFilePath;
 
 	private AnalyzerSettings(final String repositoryPath, final String dbPath,
 			final String additionalPath, final Language language,
 			final int threads, final String userName, final String passwd,
 			final String startRevisionIdentifier,
 			final String endRevisionIdentifier,
 			final MessagePrintLevel verboseLevel,
 			final VersionControlSystem versionControlSystem,
 			final String propertiesFilePath) {
 		this.repositoryPath = repositoryPath;
 		this.dbPath = dbPath;
 		this.additionalPath = additionalPath;
 		this.language = language;
 		this.threads = threads;
 		this.userName = userName;
 		this.passwd = passwd;
 		this.startRevisionIdentifier = startRevisionIdentifier;
 		this.endRevisionIdentifier = endRevisionIdentifier;
 		this.verboseLevel = verboseLevel;
 		this.versionControlSystem = versionControlSystem;
 		this.propertiesFilePath = propertiesFilePath;
 	}
 
 	/*
 	 * getttes follow
 	 */
 
 	final String getRepositoryPath() {
 		return repositoryPath;
 	}
 
 	final String getDbPath() {
 		return dbPath;
 	}
 
 	final String getAdditionalPath() {
 		return additionalPath;
 	}
 
 	final Language getLanguage() {
 		return language;
 	}
 
 	final int getThreads() {
 		return threads;
 	}
 
 	final String getUserName() {
 		return userName;
 	}
 
 	final String getPasswd() {
 		return passwd;
 	}
 
 	final String getStartRevisionIdentifier() {
 		return startRevisionIdentifier;
 	}
 
 	final String getEndRevisionIdentifier() {
 		return endRevisionIdentifier;
 	}
 
 	final MessagePrintLevel getVerboseLevel() {
 		return verboseLevel;
 	}
 
 	final VersionControlSystem getVersionControlSystem() {
 		return versionControlSystem;
 	}
 
 	final String getPropertiesFilePath() {
 		return propertiesFilePath;
 	}
 
 	static AnalyzerSettings parseArgs(final String[] args) throws Exception {
 		final Options options = defineOptions();
 
 		final CommandLineParser parser = new PosixParser();
 		final CommandLine cmd = parser.parse(options, args);
 
 		final String propertiesFilePath = (cmd.hasOption("p")) ? cmd
 				.getOptionValue("p") : null;
 		final DefaultAnalyzerSettingsLoader defaultLoader = (propertiesFilePath == null) ? DefaultAnalyzerSettingsLoader
 				.load() : DefaultAnalyzerSettingsLoader
 				.load(propertiesFilePath);
 
 		final String repositoryPath = cmd.getOptionValue("r");
 
 		final String dbPath = cmd.getOptionValue("d");
 
 		final String additionalPath = (cmd.hasOption("a")) ? cmd
 				.getOptionValue("a") : defaultLoader.getAdditionalPath();
 
 		final Language language = (cmd.hasOption("l")) ? Language
 				.getCorrespondingLanguage(cmd.getOptionValue("l"))
 				: defaultLoader.getLanguage();
 
		final int threads = (cmd.hasOption("th")) ? Integer.parseInt("th")
				: defaultLoader.getThreads();
 
 		final String userName = (cmd.hasOption("u")) ? cmd.getOptionValue("u")
 				: defaultLoader.getUserName();
 
 		final String passwd = (cmd.hasOption("pw")) ? cmd.getOptionValue("pw")
 				: defaultLoader.getPasswd();
 
 		final String startRevisionIdentifier = (cmd.hasOption("s")) ? cmd
 				.getOptionValue("s") : defaultLoader
 				.getStartRevisionIdentifier();
 
 		final String endRevisionIdentifier = (cmd.hasOption("e")) ? cmd
 				.getOptionValue("e") : defaultLoader.getEndRevisionIdentifier();
 
 		final MessagePrintLevel verboseLevel = (cmd.hasOption("v")) ? MessagePrintLevel
 				.getCorrespondingLevel(cmd.getOptionValue("v")) : defaultLoader
 				.getVerboseLevel();
 
 		final VersionControlSystem versionControlSystem = (cmd.hasOption("vc")) ? VersionControlSystem
 				.getCorrespondingVersionControlSystem(cmd.getOptionValue("vc"))
 				: defaultLoader.getVersionControlSystem();
 
 		return new AnalyzerSettings(repositoryPath, dbPath, additionalPath,
 				language, threads, userName, passwd, startRevisionIdentifier,
 				endRevisionIdentifier, verboseLevel, versionControlSystem,
 				propertiesFilePath);
 	}
 
 	/**
 	 * define options
 	 * 
 	 * @return
 	 */
 	private static Options defineOptions() {
 		final Options options = new Options();
 
 		{
 			final Option r = new Option("r", "repository", true, "repository");
 			r.setArgs(1);
 			r.setRequired(true);
 			options.addOption(r);
 		}
 
 		{
 			final Option d = new Option("d", "db", true, "database");
 			d.setArgs(1);
 			d.setRequired(true);
 			options.addOption(d);
 		}
 
 		{
 			final Option a = new Option("a", "additional", true,
 					"additional path");
 			a.setArgs(1);
 			a.setRequired(false);
 			options.addOption(a);
 		}
 
 		{
 			final Option l = new Option("l", "language", true, "language");
 			l.setArgs(1);
 			l.setRequired(false);
 			options.addOption(l);
 		}
 
 		{
 			final Option th = new Option("th", "threads", true,
 					"the number of maximum threads");
 			th.setArgs(1);
 			th.setRequired(false);
 			options.addOption(th);
 		}
 
 		{
 			final Option u = new Option("u", "user", true, "user name");
 			u.setArgs(1);
 			u.setRequired(false);
 			options.addOption(u);
 		}
 
 		{
 			final Option pw = new Option("pw", "password", true, "password");
 			pw.setArgs(1);
 			pw.setRequired(false);
 			options.addOption(pw);
 		}
 
 		{
 			final Option s = new Option("s", "start", true, "start revision");
 			s.setArgs(1);
 			s.setRequired(false);
 			options.addOption(s);
 		}
 
 		{
 			final Option e = new Option("e", "end", true, "end revision");
 			e.setArgs(1);
 			e.setRequired(false);
 			options.addOption(e);
 		}
 
 		{
 			final Option v = new Option("v", "verbose", true, "verbose output");
 			v.setArgs(1);
 			v.setRequired(false);
 			options.addOption(v);
 		}
 
 		{
 			final Option vc = new Option("vc", "version-control", true,
 					"version control system");
 			vc.setArgs(1);
 			vc.setRequired(false);
 			options.addOption(vc);
 		}
 
 		{
 			final Option p = new Option("p", "properties", true,
 					"properties file");
 			p.setArgs(1);
 			p.setRequired(false);
 			options.addOption(p);
 		}
 
 		return options;
 	}
 }
