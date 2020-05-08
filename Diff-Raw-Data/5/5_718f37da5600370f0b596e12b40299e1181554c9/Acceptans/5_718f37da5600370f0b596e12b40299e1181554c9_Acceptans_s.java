 package test.unit.main;
 
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Properties;
 
 import main.InputFileHandler;
 import members.Competitor;
 
 import org.junit.*;
 
 import test.TestUtil;
 
 public class Acceptans {
 
 	private static final String EXPECTED_PATH = "src/test/unit/main/";
 	private static final String TEST_PATH = "src/config_test_files_";
 	private static final String TEMP_PATH = "src/test/tmp/";
 	private static final String CONFIG = "config.properties";
 	private InputFileHandler in;
 
 	@Test
 	public void testStandard() throws Exception {
		String path = TEST_PATH + "standard/";
 		in = new InputFileHandler(path);
 
 		Properties prop = new Properties();
 		prop.load(new FileInputStream(path + CONFIG));
 
 		Map<Integer, Competitor> map = null;
 		map = in.parseInputFiles(in.getInputFiles(prop), prop);
 		ArrayList<Competitor> list = new ArrayList<Competitor>(map.values());
 		new InputFileHandler(TEMP_PATH).printResults(prop, list);
 
		TestUtil.testResultFiles(EXPECTED_PATH + "standard.txt", TEMP_PATH
 				+ "result.txt");
 	}
 
 	@Test
 	public void testBinary() throws Exception {
 		String path = TEST_PATH + "binary/";
 		in = new InputFileHandler(path);
 
 		Properties prop = new Properties();
 		prop.load(new FileInputStream(path + CONFIG));
 
 		Map<Integer, Competitor> map = null;
 		map = in.parseInputFiles(in.getInputFiles(prop), prop);
 		ArrayList<Competitor> list = new ArrayList<Competitor>(map.values());
 		new InputFileHandler(TEMP_PATH).printResults(prop, list);
 
 		TestUtil.testResultFiles(EXPECTED_PATH + "binary.txt", TEMP_PATH
 				+ "result.txt");
 	}
 
 	@Test
 	public void testFullBinary() throws Exception {
 		String path = TEST_PATH + "binary_full/";
 		in = new InputFileHandler(path);
 
 		Properties prop = new Properties();
 		prop.load(new FileInputStream(path + CONFIG));
 
 		Map<Integer, Competitor> map = null;
 		map = in.parseInputFiles(in.getInputFiles(prop), prop);
 		ArrayList<Competitor> list = new ArrayList<Competitor>(map.values());
 		new InputFileHandler(TEMP_PATH).printResults(prop, list);
 
 		TestUtil.testResultFiles(EXPECTED_PATH + "binary_full.txt", TEMP_PATH + "result.txt");
 	}
 
 	@Test
 	public void testFullLap() throws Exception {
 		String path = TEST_PATH + "lap_full/";
 		in = new InputFileHandler(path);
 
 		Properties prop = new Properties();
 		prop.load(new FileInputStream(path + CONFIG));
 
 		Map<Integer, Competitor> map = null;
 		map = in.parseInputFiles(in.getInputFiles(prop), prop);
 		ArrayList<Competitor> list = new ArrayList<Competitor>(map.values());
 		new InputFileHandler(TEMP_PATH).printResults(prop, list);
 
 		TestUtil.testResultFiles(EXPECTED_PATH + "lap_full.txt", TEMP_PATH + "result.txt");
 		TestUtil.testResultFiles(EXPECTED_PATH + "lap_full_sort.txt", TEMP_PATH + "sortresult.txt");
 	}
 
 }
