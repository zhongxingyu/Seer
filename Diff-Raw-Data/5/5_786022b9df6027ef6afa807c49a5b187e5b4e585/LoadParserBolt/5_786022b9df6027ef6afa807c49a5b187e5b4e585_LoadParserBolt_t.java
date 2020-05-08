 package tutorial.bolts;
 
 import java.util.Map;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseRichBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
 
 public class LoadParserBolt extends BaseRichBolt {
 	OutputCollector _collector;
 	
 	@Override
 	public void prepare(Map stormConf, TopologyContext context,
 			OutputCollector collector) {
 		_collector = collector;
 	}
 
 	@Override
 	public void execute(Tuple input) {
 		if (input.getString(0).length() > 0)
 		{
 			String loadAveragesPart = input.getString(0)
					.split("load average: ")[1].toString();
 			
			String[] loadAverages = loadAveragesPart.split(", ");
 			
 			_collector.emit(input, new Values(
 					loadAverages[0], loadAverages[1], loadAverages[2])
 			);
 		}
 		_collector.ack(input);
 	}
 
 	@Override
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
 		declarer.declare(
 				new Fields(
 						"loadaverage1", 
 						"loadaverage5", 
 						"loadaverage15"
 				)
 		);
 	}
 
 }
