 package sk.folki.aspectlogger.core;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 // TODO Continue here
 // TODO Refactor
 class LoggableMethodLoggerConfig {
 	private Level level;
 
 	public LoggableMethodLoggerConfig(Logger logger) {
		this.level = logger.getLevel();
 	}
 
 	public Level getLevel() {
 		return level;
 	}
 }
