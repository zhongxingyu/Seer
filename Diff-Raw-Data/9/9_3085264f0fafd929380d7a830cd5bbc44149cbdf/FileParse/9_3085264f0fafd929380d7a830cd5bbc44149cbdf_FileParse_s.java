 package ajeetmurty.reference.java.regex;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class FileParse {
 	private final Logger logp = LoggerFactory.getLogger(this.getClass().getName());
	private final String inputLogFilePath = "src/main/resources/regex.log";
 	private final String regex = "^(\\S+)\\s(.*)$";
 
 	public static void main(String[] args) {
 		new FileParse();
 	}
 
 	private FileParse() {
 		logp.info("start");
 		parseFile();
 		logp.info("stop");
 	}
 
 	private void parseFile() {
 		try {
 			logp.info("input log file path: " + inputLogFilePath);
 			File inputFile = new File(inputLogFilePath);
 			if (inputFile.isFile() && inputFile.canRead()) {
 				Pattern pattern = Pattern.compile(regex);
 				Matcher matcher = pattern.matcher("");
 
 				BufferedReader br = new BufferedReader(new FileReader(inputFile));
 				long linesTotal = 0, linesMatched = 0;
 				String line = null;
 				while ((line = br.readLine()) != null) {
 					linesTotal++;
 					if (matcher.reset(line).matches()) {
 						linesMatched++;
 						logp.info(String.format("line matched & host extracted: >%1$s<", matcher.group(1)));
 					} else {
 						logp.error("line not matched: " + line);
 					}
 				}
 				br.close();
 
 				logp.info("total lines in file: " + linesTotal);
 				logp.info("total matches: " + linesMatched);
 				logp.info("match failed: " + (linesTotal - linesMatched));
 			} else {
 				logp.error("input log file not found / not readable");
 			}
 		} catch (Exception e) {
 			logp.error(e.getMessage(), e);
 		}
 	}
 }
