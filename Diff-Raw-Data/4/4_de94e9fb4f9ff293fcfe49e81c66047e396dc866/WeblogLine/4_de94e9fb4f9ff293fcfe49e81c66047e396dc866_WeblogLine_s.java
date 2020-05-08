 package hu.sztaki.ilab.longneck.process.block;
 
 import hu.sztaki.ilab.longneck.Record;
 import hu.sztaki.ilab.longneck.process.CheckError;
 import hu.sztaki.ilab.longneck.process.VariableSpace;
 import hu.sztaki.ilab.longneck.process.constraint.CheckResult;
 import hu.sztaki.ilab.longneck.weblog.parser.processor.LogParser;
 import hu.sztaki.ilab.longneck.weblog.parser.processor.LogParserFactory;
 import java.util.InputMismatchException;
 
 /**
  * Processes a log line specified in the apply-to attribute.
  *
  * @author Molnár Péter <molnarp@ilab.sztaki.hu>
  */
 public class WeblogLine extends AbstractAtomicBlock {
 
     /** Log parser factory to create log parser. */
     private LogParserFactory logParserFactory;
     /** The log parser used to split and process the log line. */
     private LogParser logParser;
 
     public void afterPropertiesSet() {
         logParser = logParserFactory.getLogParser();
     }
 
     @Override
     public void apply(Record record, VariableSpace parentScope) throws CheckError {
         for (String fieldName : applyTo) {
 
             String value = BlockUtils.getValue(fieldName, record, parentScope);
 
             try {
                 logParser.doProcess(record, value);
              } catch (InputMismatchException e) {
                 logParser.LOG.error(e);
 
                 String executedPattern = logParser.appliedPattern.pattern();
                 throw new CheckError(
                        new CheckResult(this, false, this.getClass().getName().toString(), executedPattern,
                        String.format("<Weblog-parser-source> is not able to parse line: '%1$s'", value)));
             }
         }
     }
 
     @Override
     public WeblogLine clone() {
         return (WeblogLine) super.clone();
     }
 
     public LogParserFactory getLogParserFactory() {
         return logParserFactory;
     }
 
     public void setLogParserFactory(LogParserFactory logParserFactory) {
         this.logParserFactory = logParserFactory;
     }
 }
