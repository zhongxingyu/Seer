 package mirantis;
 
 import backtype.storm.spout.SpoutOutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseRichSpout;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Values;
 import backtype.storm.utils.Utils;
 
 import java.util.Map;
 import java.util.Random;
 
 /**
  * @author slukjanov
  */
 public class RandSentenceGenerator extends BaseRichSpout {
 
     private SpoutOutputCollector collector;
     private Random random;
 
     private String[] sentences;
 
     @Override
     public void open(Map map, TopologyContext ctx, SpoutOutputCollector collector) {
         this.collector = collector;
         this.random = new Random();
         this.sentences =
                ("test hello world example" +
                        " sample hello another" +
                         " twitter storm one framework"
                ).split("\\s");
     }
 
     @Override
     public void nextTuple() {
         Utils.sleep(10);
         String sentence = sentences[random.nextInt(sentences.length)];
         collector.emit(new Values(sentence));
     }
 
     @Override
     public void declareOutputFields(OutputFieldsDeclarer declarer) {
         declarer.declare(new Fields("sentence"));
     }
 
 }
