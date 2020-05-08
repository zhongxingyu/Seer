 /**
  * 
  */
 package eu.larkc.iris.exports;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cascading.flow.Flow;
 import cascading.flow.FlowConnector;
 import cascading.operation.Identity;
 import cascading.pipe.Each;
 import cascading.pipe.Pipe;
 import cascading.tap.Hfs;
 import cascading.tap.Tap;
 import cascading.tuple.Fields;
 import cascading.tuple.Tuple;
 import cascading.tuple.TupleEntryIterator;
 import eu.larkc.iris.Configuration;
 import eu.larkc.iris.storage.FactsFactory;
 import eu.larkc.iris.storage.IRIWritable;
 import eu.larkc.iris.storage.StringTermWritable;
 
 /**
  * @author valer
  *
  */
 public class Exporter {
 
 	private static final Logger logger = LoggerFactory.getLogger(Exporter.class);
 	
 	public void exportToRdf(Configuration configuration, String project, String storageId, String resultsName) {
 		Tap source= new Hfs(new Fields(0, 1, 2), project + "/inferences/" + resultsName, true );
 		
 		Tap sink = FactsFactory.getInstance(storageId).getFacts();
 		
 		Map<String, Tap> sources = new HashMap<String, Tap>();
 		sources.put("source", source);
 
 		Map<String, Tap> sinks = new HashMap<String, Tap>();
 		sinks.put("sink", sink);
 		//sinks.put("sink1", sink1);
 
 		Pipe sourcePipe = new Pipe("source");
 		sourcePipe = new Each(sourcePipe, Fields.ALL, new Identity());
 		Pipe identity = new Pipe("sink", sourcePipe);
 		//identity = new Each(identity, source.getSourceFields(), new Identity(source.getSourceFields()));
 		//Pipe identity1 = new Pipe("sink1", sourcePipe);
 		//identity1 = new Each(identity1, source.getSourceFields(), new Identity(source.getSourceFields()));
 		
 		Flow aFlow = new FlowConnector(configuration.flowProperties).connect(sources, sink, identity);
 		aFlow.complete();
 	}
 
 	public void exportToFile(Configuration configuration, String project, String outPath, String resultsName) {
 		processNTriple(configuration, outPath, project, resultsName);
 	}
 	
 	public void processNTriple(Configuration configuration, String outPath, String project, String resultsName) {
 		File file = new File(outPath);
 		BufferedWriter bw = null;
 		try {
 			bw = new BufferedWriter(new FileWriter(file));
 		} catch (IOException e) {
 			logger.error("io exception opening out file", e);
 			throw new RuntimeException("io exception out file", e);
 		}
 		
 		Tap source= new Hfs(new Fields(0, 1, 2), project + "/inferences/" + resultsName );
 		TupleEntryIterator tei;
 		try {
 			tei = source.openForRead(configuration.jobConf);
 		} catch (IOException e) {
 			logger.error("io exception opening source", e);
 			throw new RuntimeException("io exception opening source", e);
 		}
 		while (tei.hasNext()) {
 			StringBuilder sb = new StringBuilder();
 			Tuple tuple = tei.next().getTuple();
 			for (int i = 0; i < tuple.size(); i++) {
 				Object value = tuple.getObject(i);
 				if (value instanceof IRIWritable) {
 					IRIWritable iri = (IRIWritable) value;
					sb.append("<" + iri.getValue() + "> ");
 				} else if (value instanceof StringTermWritable) {
 					StringTermWritable stringTerm = (StringTermWritable) value;
 					sb.append(stringTerm.getValue() + " ");
 				}
 			}
 			sb.append(".\n");
 			try {
 				bw.write(sb.toString());
 			} catch (IOException e) {
 				logger.error("io exception writing line", e);
 				throw new RuntimeException("io exception writing line", e);
 			}
 		}
 		try {
 			bw.close();
 		} catch (IOException e) {
 			logger.error("exception closing writer", e);
 		}
 	}
 	
 }
