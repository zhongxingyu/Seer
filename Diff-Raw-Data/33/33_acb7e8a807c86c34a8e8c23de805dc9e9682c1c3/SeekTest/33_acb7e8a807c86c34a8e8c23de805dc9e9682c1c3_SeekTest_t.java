 package redis.seek.test;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.pool.impl.GenericObjectPool.Config;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisShardInfo;
 import redis.seek.Entry;
 import redis.seek.Index;
 import redis.seek.Result;
 import redis.seek.Search;
 import redis.seek.Seek;
 import redis.seek.ShardField;
 import redis.seek.Text;
 import redis.seek.Search.Order;
 
 public class SeekTest extends Assert {
     private Jedis jedis;
 
     @Before
     public void setUp() throws IOException {
         jedis = new Jedis("localhost");
         jedis.flushAll();
         jedis.quit();
         jedis.disconnect();
         List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
         shards.add(new JedisShardInfo("localhost"));
         Config config = new Config();
         Seek.configure(config, shards);
     }
 
     @After
     public void tearDown() {
         Seek.getPool().destroy();
     }
 
     @Test
     public void indexAndSearch() {
         addEntry("MLA98251174", 1287278019);
         Result result = search(0, 0, 50);
 
         assertEquals(1, result.getTotalCount());
         assertEquals("MLA98251174", result.getIds().get(0));
     }
 
     @Test
     public void searchAndPaginate() {
         addEntry("MLA98251174", 1287278019);
         addEntry("MLA98251175", 1287278020);
         addEntry("MLA98251176", 1287278021);
 
         Result result = search(0, 0, 0);
 
         assertEquals(3, result.getTotalCount());
         assertEquals(1, result.getIds().size());
         assertEquals("MLA98251176", result.getIds().get(0));
     }
 
     @Test
     public void reindex() {
         addEntry("MLA98251174", 1287278019);
         addEntry("MLA98251174", 1287278019);
         Result result = search(0, 0, 50);
 
         assertEquals(1, result.getTotalCount());
         assertEquals("MLA98251174", result.getIds().get(0));
     }
 
     @Test
     public void dontCacheByDefault() {
         addEntry("MLA98251174", 1287278019);
         search(0, 0, 50);
         Set<String> keys = jedis.keys("*result*");
         assertEquals(0, keys.size());
     }
 
     @Test
     public void cache() throws InterruptedException {
         addEntry("MLA98251174", 1287278019);
         Seek seek = new Seek();
         Search search = seek.search("items", "start_date", new ShardField(
                 "seller_id", "84689862"));
         search.text("title", "ipod 160");
         Result result = search.run(1, 0, 50, Order.DESC);
         Set<String> keys = jedis.keys("*result*");
         assertEquals(1, result.getTotalCount());
         assertEquals(1, keys.size());
         Thread.sleep(2000);
         keys = jedis.keys("*result*");
         assertEquals(0, keys.size());
     }
 
     @Test
     public void remove() {
         addEntry("MLA98251174", 1287278019);
         Seek seek = new Seek();
         seek.index("items").remove("MLA98251174",
                 new ShardField("seller_id", "84689862"));
         Result result = search(0, 0, 1);
         assertEquals(0, result.getTotalCount());
     }
 
     @Test
     public void stopwords() {
         Set<String> stopwords = new HashSet<String>();
         stopwords.add("un");
         stopwords.add("para");
         Text text = new Text("Un collar para un perro", stopwords);
         Set<String> words = text.getWords();
         assertEquals(2, words.size());
     }
 
    @Test
    public void searchWithTags() {
        Seek seek = new Seek();
        Entry e = seek.index("items").add("123");
        e.shardBy("seller_id");
        e.addField("seller_id", "2");
        e.addField("status", "active");
        e.addField("type", "normal");
        e.addText("title", "titulin");
        e.addOrder("start_time", System.currentTimeMillis());
        e.addTag("tagged");
        e.save();

        Search search = seek.search("items", "start_time", new ShardField(
                "seller_id", "2"));
        search.field("status", "active");
        search.tag("tagged");
        Result run = search.run();

        assertEquals(1, run.getTotalCount());
    }

     private Result search(int cache, int start, int end) {
         Seek seek = new Seek();
         Search search = seek.search("items", "start_date", new ShardField(
                 "seller_id", "84689862"));
         search.field("category_id", "MLA31594", "MLA39056");
         search.tag("buy_it_now", "promotion");
         search.text("title", "ipod 160");
         return search.run(cache, start, end, Search.Order.DESC);
     }
 
     private Seek addEntry(String id, double timestamp) {
         Seek seek = new Seek();
         Index index = seek.index("items");
         Entry entry = index.add(id);
         entry.addField("category_id", "MLA31594");
         entry.addField("seller_id", "84689862");
         entry.addTag("buy_it_now");
         entry.addText("title",
                 "Apple Ipod Classic 160gb 160 8° Generacion 40.000 Canciones!");
         entry.addOrder("start_date", timestamp);
         entry.shardBy("seller_id");
         entry.save();
         return seek;
     }
 }
