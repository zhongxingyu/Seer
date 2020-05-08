 package com.lexin.db;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.ResultSetHandler;
 
 import spider.util.SpiderUtil;
 import util.SpiderConfiguration;
 import util.Util;
 
 import com.lexin.bean.Feed;
 import com.lexin.bean.Regular;
 import com.lexin.bean.RegularTypeEnum;
 import com.lexin.bean.Seed;
 import com.lexin.bean.SiteProperties;
 import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
 
 public class DBUtil {
 
   private QueryRunner qr;
   private MysqlDataSource ds;
   private Properties prop;
   private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 
   public DBUtil() throws IOException {
     this.prop = Util.getProperties(SpiderConfiguration.getDBConfigFilePath());
     init();
 
   }
 
   public DBUtil(Properties prop) {
     this.prop = prop;
     init();
   }
 
   private void init() {
     ds = new MysqlDataSource();
     ds.setUrl(prop.getProperty(SpiderConfiguration.DB_URL));
     ds.setUser(prop.getProperty(SpiderConfiguration.DB_USER));
     ds.setPassword(prop.getProperty(SpiderConfiguration.DB_PASSWD));
     qr = new QueryRunner(ds);
   }
 
   public QueryRunner getQueryRunner() {
     return qr;
   }
 
   public void save(Feed feed) throws SQLException {
     StringBuffer sql = new StringBuffer(
         "replace into feed(title,date,author,content,hypelink,refer,md5,`like`,unlike,collect,`comment`,`type`)values(");
     sql.append("'" + feed.getTitle() + "',");
     sql.append("'" + feed.getDate() + "',");
     sql.append("'" + feed.getAuthor() + "',");
     sql.append("'" + feed.getContent().replaceAll("'", "''") + "',");
     sql.append("'" + feed.getHypelink() + "',");
     sql.append("'" + feed.getRefer() + "',");
     sql.append("'" + feed.getMd5() + "',");
     sql.append("'" + feed.getLike() + "',");
     sql.append("'" + feed.getUnlike() + "',");
     sql.append("'" + feed.getCollect() + "',");
     sql.append("'" + feed.getComment() + "',");
     sql.append("'" + feed.getType() + "')");
     qr.update(sql.toString());
   }
 
   public void save(Seed seed) throws SQLException {
     StringBuffer sql = new StringBuffer();
     sql.append("replace into seed(url,crawled_at,created_at)values(");
     sql.append("'" + seed.getUrl() + "',");
     sql.append("'" + seed.getCrawledAt() + "',");
     sql.append("'" + seed.getCreatedAt() + "')");
     qr.update(sql.toString());
   }
 
   public void save(Regular regular) throws SQLException {
     StringBuffer sql = new StringBuffer();
     sql.append("replace into reguler(host,reguler,created_at,md5,type)values(");
     sql.append("'" + regular.getHost() + "',");
     sql.append("'" + specialSingal(regular.getRegular()) + "',");
     sql.append("'" + regular.getCreatedAt() + "',");
     sql.append("'" + regular.getMd5() + "',");
     sql.append("'" + regular.getType() + "')");
     qr.update(sql.toString());
   }
   public void save(SiteProperties sp) throws SQLException{
     String sql="replace into siteprop(host,encoding)values('"+sp.getHost()+"','"+sp.getEncoding()+"');";
     qr.update(sql);
   }
   public void saveSeeds(List<Seed> seeds) throws SQLException {
     for (Seed seed : seeds) {
       save(seed);
     }
   }
   public String specialSingal(String text){
     if(null==text||"".equals(text.trim())){
       return "";
     }
     return text.replace("'","''").replace("\\", "\\\\");
   }
   public void saveRegulars(List<Regular> regulars) throws SQLException {
     for (Regular regular : regulars) {
       save(regular);
     }
   }
 
   public List<Seed> getCrawlingSeeds(int interval) throws SQLException {
     String sql = "select id,url,crawled_at,created_at,times from seed where times=0 or crawled_at <date_sub(now(), interval "
         + interval + " day) limit 10;";
     return qr.query(sql, new ResultSetHandler<List<Seed>>() {
 
       @Override
       public List<Seed> handle(ResultSet rs) throws SQLException {
         List<Seed> seeds = new ArrayList<Seed>();
         while (rs.next()) {
           Seed seed = new Seed();
           seed.setId(rs.getInt("id"));
          seed.setCrawledAt(rs.getString("crawled_at"));
          seed.setUrl(rs.getString("url"));
          seed.setCreatedAt(rs.getString("created_at"));
           seed.setTimes(rs.getInt("times"));
           seeds.add(seed);
         }
         return seeds;
       }
 
     });
   }
 
   public List<Regular> getCrawlerRegulars(int type) throws SQLException {
     String sql = "select id,host,reguler,created_at from reguler where type=" + type + ";";
     return qr.query(sql, new ResultSetHandler<List<Regular>>() {
       @Override
       public List<Regular> handle(ResultSet rs) throws SQLException {
         List<Regular> regulars = new ArrayList<Regular>();
         while (rs.next()) {
           Regular regular = new Regular();
           regular.setId(rs.getInt("id"));
           regular.setHost(rs.getString("host"));
           regular.setCreatedAt(rs.getString("created_at"));
          regular.setRegular(rs.getString("reguler"));
           regulars.add(regular);
         }
         return regulars;
       }
     });
   }
 
   public void updateCrawledTimes(List<Seed> seeds) throws SQLException {
     String ids = null;
     for (Seed seed : seeds) {
       String id = String.valueOf(seed.getId());
       if (null == ids) {
         ids = id;
       } else {
         ids = ids + "," + id;
       }
     }
     String sql = "update seed set times=times+1,crawled_at=now() where id in(" + ids + ");";
     qr.update(sql);
   }
 
   public void saveFeeds(List<Feed> feeds) {
     try {
       for (Feed feed : feeds) {
         save(feed);
       }
     } catch (SQLException e) {
       e.printStackTrace();
     }
   }
 
   public List<SiteProperties> getSiteProperties() throws SQLException {
     String sql = "select host,encoding from siteprop";
     return qr.query(sql, new ResultSetHandler<List<SiteProperties>>() {
 
       @Override
       public List<SiteProperties> handle(ResultSet rs) throws SQLException {
         List<SiteProperties> props = new ArrayList<SiteProperties>();
         while (rs.next()) {
           SiteProperties sp = new SiteProperties();
           sp.setHost(rs.getString("host"));
           sp.setEncoding(rs.getString("encoding"));
           props.add(sp);
         }
         return props;
       }
     });
   }
 
   public static void main(String[] args) {
     try {
       DBUtil db = new DBUtil();
       String sql = "select id,name from content";
       String[] results = db.getQueryRunner().query(sql, new ResultSetHandler<String[]>() {
         @Override
         public String[] handle(ResultSet rs) throws SQLException {
           if (!rs.next()) {
             return null;
           }
           int cols = rs.getMetaData().getColumnCount();
           String[] results = new String[cols];
           for (int i = 0; i < cols; i++) {
             results[i] = rs.getString(i + 1);
           }
           return results;
         }
       });
 
       for (int i = 0; i < results.length; i++) {
         System.out.println("RESULT[" + i + "]:" + results[i]);
       }
     } catch (IOException e) {
       e.printStackTrace();
     } catch (SQLException e) {
       e.printStackTrace();
     }
 
   }
   public void saveConfiguration(String seedUrl,String encoding,String nextPattern,String contentPattern){
     String date=sdf.format(new Date());
     String host=SpiderUtil.getHost(seedUrl);
     Seed seed=new Seed();
     seed.setTimes(0);
     seed.setUrl(seedUrl);
     seed.setCreatedAt(date);
     seed.setCrawledAt(date);
     
     Regular regular=new Regular();
     regular.setCreatedAt(date);
     regular.setHost(host);
     regular.setRegular(contentPattern);
     regular.setType(RegularTypeEnum.CONTENT.ordinal());
     
     Regular nextpage=new Regular();
     nextpage.setCreatedAt(date);
     nextpage.setHost(host);
     nextpage.setRegular(nextPattern);
     nextpage.setType(RegularTypeEnum.NEXTPAGE.ordinal());
     
     SiteProperties sp=new SiteProperties();
     sp.setEncoding(encoding);
     sp.setHost(host);
     try {
       save(seed);
       save(nextpage);
       save(regular);
       save(sp);
     } catch (SQLException e) {
       e.printStackTrace();
     }
   }
 }
