 package com.ngdata.sep.demo;
 
 import com.ngdata.sep.EventListener;
 import com.ngdata.sep.SepEvent;
 import com.ngdata.sep.SepModel;
 import com.ngdata.sep.impl.SepEventSlave;
 import com.ngdata.sep.impl.SepModelImpl;
import com.ngdata.sep.util.zookeeper.ZkUtil;
import com.ngdata.sep.util.zookeeper.ZooKeeperItf;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.common.SolrInputDocument;
 
 /**
  * A consumer that indexes to Solr.
  */
 public class SolrIndexingConsumer {
     public static void main(String[] args) throws Exception {
         new SolrIndexingConsumer().run();
     }
 
     public void run() throws Exception {
         Configuration conf = HBaseConfiguration.create();
         conf.setBoolean("hbase.replication", true);
 
         ZooKeeperItf zk = ZkUtil.connect("localhost", 20000);
         SepModel sepModel = new SepModelImpl(zk, conf);
 
         if (!sepModel.hasSubscription("index1")) {
             sepModel.addSubscription("index1");
         }
 
         HttpSolrServer solr = new HttpSolrServer("http://localhost:8983/solr");
         TikaSolrCellHBaseMapper mapper = new TikaSolrCellHBaseMapper("http://localhost:8983/solr");
 
         SepEventSlave eventSlave = new SepEventSlave("index1", System.currentTimeMillis(),
                 new Indexer(solr, mapper), 10, "localhost", zk, conf);
 
         eventSlave.start();
         System.out.println("Started");
 
         while (true) {
             Thread.sleep(Long.MAX_VALUE);
         }
     }
 
     private static class Indexer implements EventListener {
         private SolrServer solrServer;
         private TikaSolrCellHBaseMapper mapper;
 
         public Indexer(SolrServer solrServer, TikaSolrCellHBaseMapper mapper) {
             this.solrServer = solrServer;
             this.mapper = mapper;
         }
 
         @Override
         public void processEvent(SepEvent event) {
             System.out.println("Received event for row " + Bytes.toString(event.getRow()));
             try {
                 SolrInputDocument solrDoc = mapper.map(new Result(event.getKeyValues()));
                 solrServer.add(solrDoc);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
 
             // TODO: we should:
             //  - handle delete events
             //  - have an alternative that goes back to hbase to read the (full) row
         }
     }
 }
