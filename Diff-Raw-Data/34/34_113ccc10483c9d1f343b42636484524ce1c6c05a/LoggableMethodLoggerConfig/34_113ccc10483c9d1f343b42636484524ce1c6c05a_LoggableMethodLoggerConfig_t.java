 package sk.folki.aspectlogger.core;
 
import org.apache.log4j.Category;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 // TODO Continue here
 // TODO Refactor
 class LoggableMethodLoggerConfig {
 	private Level level;
 
 	public LoggableMethodLoggerConfig(Logger logger) {
		this.level = getLevelOfLogger(logger);
	}

	private Level getLevelOfLogger(Logger logger) {
		Level level = null;
		Category currentLogger = logger;
		while (currentLogger != null) {
			level = currentLogger.getLevel();
			if (level != null) {
				return level;
			}
			currentLogger = currentLogger.getParent();
		}
		return null;
 	}
 
 	public Level getLevel() {
 		return level;
 	}
 }
