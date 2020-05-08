 package org.kalibro.tests;
 
 import static org.kalibro.core.Environment.*;
 
 import java.io.File;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.AfterClass;
 import org.junit.rules.Timeout;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.introspector.BeanAccess;
 
 public abstract class IntegrationTest extends UnitTest {
 
 	@AfterClass
	public static void cleanLogs() {
		FileUtils.deleteQuietly(logsDirectory());
 	}
 
 	protected static File samplesDirectory() {
 		return new File(dotKalibro(), "samples");
 	}
 
 	protected static File projectsDirectory() {
 		return new File(dotKalibro(), "projects");
 	}
 
 	protected static File helloWorldDirectory() {
 		return new File(projectsDirectory(), "HelloWorld-1.0");
 	}
 
 	@Override
 	protected Timeout testTimeout() {
 		return new Timeout(8000);
 	}
 
 	protected <T> T loadYaml(String name, Class<T> type) {
 		Yaml yaml = new Yaml();
 		yaml.setBeanAccess(BeanAccess.FIELD);
 		return yaml.loadAs(getClass().getResourceAsStream(name + ".yml"), type);
 	}
 }
