 
 package getco;
 import java.util.Collection;
 
 import getco.io.*;
 import getco.io.EquityWriterOrParserFactory;
 
 import getco.model.EquityDefinitionSet;
 import getco.model.EquityTimeSeriesSet;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class EquityMasterApplication
 {
   static final Logger log = LoggerFactory.getLogger(EquityMasterApplication.class);
 
 
   public static void main(String[] args) {
     log.info("EquityMasterApplication version 1.0");
 
     EquityDefinitionParser parserA = EquityWriterOrParserFactory.getEquityDefinitionParser(',', "/vendor_a_eq.txt");
     EquityDefinitionParser parserB = EquityWriterOrParserFactory.getEquityDefinitionParser('|', "/vendor_b_eq.txt");
     
     Collection colA = parserA.parseFile();
     Collection colB = parserB.parseFile();
     EquityDefinitionSet definitionSet = new EquityDefinitionSet();
     definitionSet.addAll(colA);
     definitionSet.addAll(colB);
 
     EquityDefinitionWriter equityWriter = EquityWriterOrParserFactory.getEquityDefinitionWriter(definitionSet, "equitydefinition.txt");
     equityWriter.writeFile();
 
 
     EquityTimeSeriesParser parserC = EquityWriterOrParserFactory.getEquityTimeSeriesParser(',', "/vendor_a_ts.txt");
     EquityTimeSeriesParser parserD = EquityWriterOrParserFactory.getEquityTimeSeriesParser('|', "/vendor_b_ts.txt");
     
     Collection colC = parserC.parseFile();
     Collection colD = parserD.parseFile();
    log.info("colC.size() : " + colC.size());

     EquityTimeSeriesSet timeSeriesSet = new EquityTimeSeriesSet();
     timeSeriesSet.addAll(colC);
     timeSeriesSet.addAll(colD);
 
    log.info("timeSeriesSet.size() : " + timeSeriesSet.size());
     EquityTimeSeriesWriter equityTimeSeriesWriter = EquityWriterOrParserFactory.getEquityTimeSeriesWriter(timeSeriesSet, "equitytimeseries.txt");
     equityTimeSeriesWriter.writeFile();


   }

 }
