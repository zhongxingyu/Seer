 package jp.skypencil.pmd.slf4j.example;
 
 import jp.skypencil.pmd.slf4j.example.UsingStaticLogger;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class UsingUnknownThrowableToLogNotCompiled extends RuntimeException {
 	@SuppressWarnings("unused")
	private final Logger LOGGER = LoggerFactory.getLogger(UsingUnknownThrowableToLogNotCompiled.class);
 
 	public void method() {
 		UsingUnknownThrowableToLogNotCompiled t = new UsingUnknownThrowableToLogNotCompiled();
		logger.info("UsingUnknownThrowableToLogNotCompiled, {}", t);
 	}
 }
