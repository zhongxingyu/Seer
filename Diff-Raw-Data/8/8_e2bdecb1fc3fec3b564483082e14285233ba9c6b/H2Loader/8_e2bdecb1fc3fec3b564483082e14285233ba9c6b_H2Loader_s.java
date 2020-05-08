 package us.yuxin.ingest.jdbc;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.PathMatcher;
 import java.nio.file.Paths;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import us.yuxin.sa.transformer.JsonDecomposer;
 import us.yuxin.sa.transformer.Transformer;
 
 public class H2Loader {
   protected Connection connection;
   protected String tableName;
   protected Set<String> columns = new HashSet<>();
 
   protected List<String> inserts = new LinkedList<>();
   protected List<String> appendColumns = new LinkedList<>();
 
   protected JsonDecomposer decomposer;
   protected Properties conf;
 
 
   public void executeBatchFlush() throws SQLException {
 
     if (inserts.size() == 0 && appendColumns.size() == 0)
       return;
 
     Statement stmt = connection.createStatement();
     for (String query : appendColumns) {
       stmt.addBatch(query);
     }
     for (String query : inserts) {
       stmt.addBatch(query);
     }
     stmt.executeBatch();
     stmt.close();
     connection.commit();
 
     appendColumns.clear();
     inserts.clear();
 
   }
 
 
   public void createTable() throws SQLException {
     // Connection co = connection;
     // Statement stmt = co.createStatement();
     // stmt.execute("DROP TABLE IF EXISTS " + tableName);
 
     String dstmt = "DROP TABLE IF EXISTS " + tableName;
     String cstmt = "CREATE MEMORY TABLE " + tableName +
       " (id INT AUTO_INCREMENT PRIMARY KEY";
 
     for (String cn : columns) {
       cstmt += ",\n" + cn + " VARCHAR(255)";
     }
     cstmt += "\n)";
 
     execute(dstmt);
     execute(cstmt);
   }
 
 
   public void addColumn(String columnName) throws SQLException {
     String astmt = "ALTER TABLE " + tableName + " ADD COLUMN " +
       "(" + columnName + " VARCHAR(255))";
 
     execute(astmt);
     columns.add(columnName);
     System.out.println("Add column: " + columnName);
   }
 
 
   public boolean execute(String query) throws SQLException {
     boolean res;
     Statement stmt = connection.createStatement();
     res = stmt.execute(query);
     stmt.close();
     return res;
   }
 
   public void open(String url, String username, String password) throws ClassNotFoundException, SQLException {
     connection = DriverManager.getConnection(url, username, password);
   }
 
   public void close() throws SQLException {
     if (connection != null) {
       connection.close();
       connection = null;
     }
     return;
   }
 
 
   public void setTableName(String name) {
     this.tableName = name;
   }
 
 
   public void setBaseColumns(String columns[]) {
     this.columns = new HashSet<>();
     for (String c : columns) {
       this.columns.add(c);
     }
   }
 
 
   public void addJsonFile(File path) throws IOException, SQLException {
     connection.setAutoCommit(false);
     System.out.println("reading " + path + " ...");
     int count = 0;
 
     BufferedReader reader = new BufferedReader(new FileReader(path));
 
     while (true) {
       String line = reader.readLine();
 
       if (line == null)
         break;
       if (line.length() == 0)
         continue;
 
       Map<String, Object> map = decomposer.readValue(line.getBytes("utf8"));
       addJsonDataBatch(map);
       count++;
 
       if (count % 2500 == 0) {
         executeBatchFlush();
       }
 
       if (count % 10000 == 0) {
         System.out.println("" + count + " ... " + new Date().toString());
       }
     }
 
     executeBatchFlush();
     System.out.println("" + count + " ... " + new Date().toString());
     reader.close();
   }
 
   public void addJsonDir(String basePath, String globPattern) throws IOException, SQLException {
     Finder finder = new Finder(globPattern);
 
     Files.walkFileTree(Paths.get(basePath), finder);
     finder.done();
 
     List<Path> paths = finder.getPahts();
 
     for (Path path: paths) {
       File file = path.toFile();
       if (file.isDirectory())
         continue;
       addJsonFile(path.toFile());
     }
   }
 
   public void addJsonData(Map<String, Object> map) throws SQLException {
     StringBuilder keyStr = new StringBuilder();
     StringBuilder valStr = new StringBuilder();
 
     boolean first = true;
 
     for (Map.Entry<String, Object> e : map.entrySet()) {
       String key = e.getKey();
       Object val = e.getValue();
 
       if (val == null)
         continue;
       if (val instanceof String && ((String) val).length() == 0)
         continue;
 
       if (key.startsWith("__")) {
         continue;
       }
 
       if (!first) {
         keyStr.append(",");
         valStr.append(",");
       } else {
         keyStr.append("INSERT INTO " + tableName + " (");
         valStr.append(") VALUES (");
         first = false;
       }
 
       key = key.toLowerCase();
 
       if (!columns.contains(key)) {
         addColumn(key);
       }
 
       keyStr.append(e.getKey());
       String vals = val.toString().replace("'", "''");
       valStr.append("'").append(vals).append("'");
     }
 
     keyStr.append(valStr).append(")");
     execute(keyStr.toString());
   }
 
   public void addJsonDataBatch(Map<String, Object> map) throws SQLException {
     StringBuilder keyStr = new StringBuilder();
     StringBuilder valStr = new StringBuilder();
 
     boolean first = true;
 
     for (Map.Entry<String, Object> e : map.entrySet()) {
       String key = e.getKey();
       Object val = e.getValue();
 
       if (val == null)
         continue;
       if (val instanceof String && ((String) val).length() == 0)
         continue;
 
       if (key.startsWith("__")) {
         continue;
       }
 
       if (!first) {
         keyStr.append(",");
         valStr.append(",");
       } else {
         keyStr.append("INSERT INTO " + tableName + " (");
         valStr.append(") VALUES (");
         first = false;
       }
 
       key = key.toLowerCase();
 
       if (!columns.contains(key)) {
         addColumn(key);
       }
 
       keyStr.append(e.getKey());
       String vals = val.toString().replace("'", "''");
       valStr.append("'").append(vals).append("'");
     }
 
     keyStr.append(valStr).append(")");
     inserts.add(keyStr.toString());
   }
 
 
   public void setDecomposer(JsonDecomposer decomposer) {
     this.decomposer = decomposer;
     decomposer.start();
   }
 
 
   public void setup(Properties conf) throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
     this.conf = conf;
 
     Class.forName(conf.getProperty("jdbc.driver", "org.h2.Driver"));
     setTableName(conf.getProperty("jdbc.tablename", "sa"));
    open(conf.getProperty("jdbc.url", "jdbc:h2://localhost/mem:sa"),
       conf.getProperty("jdbc.username", "sa"),
       conf.getProperty("jdbc.password", ""));
 
     Transformer transformer = null;
     if (conf.getProperty("transformer") != null) {
       transformer = (Transformer)Class.forName(conf.getProperty("transformer")).newInstance();
     }
     decomposer = new JsonDecomposer(transformer);
   }
 
   public static void main(String[] args) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
 
     String confPath = "loader.properties";
 
     if (args.length > 1) {
       confPath = args[0];
     }
 
     Properties conf = new Properties();
 
     FileReader cfReader = new FileReader(new File(confPath));
     conf.load(cfReader);
     cfReader.close();
 
     H2Loader loader = new H2Loader();
     loader.setup(conf);
    loader.createTable();
     loader.addJsonDir(
       conf.getProperty("path", "datas"),
       conf.getProperty("glob", "*"));
     loader.close();
   }
 
 
   // ----
   public static class Finder extends SimpleFileVisitor<Path> {
     List<Path> paths;
 
     private final PathMatcher matcher;
     private int numMatches = 0;
 
     Finder(String pattern) {
       matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
       paths = new LinkedList<>();
     }
 
     // Compares the glob pattern against
     // the file or directory name.
     void find(Path file) {
       Path name = file.getFileName();
       if (name != null && matcher.matches(name)) {
         numMatches++;
         paths.add(file);
       }
     }
 
     // Prints the total number of
     // matches to standard out.
     void done() {
       // System.out.println("Matched: " + numMatches);
     }
 
     // Invoke the pattern matching
     // method on each file.
     @Override
     public FileVisitResult
     visitFile(Path file, BasicFileAttributes attrs) {
       find(file);
       return FileVisitResult.CONTINUE;
     }
 
     // Invoke the pattern matching
     // method on each directory.
     @Override
     public FileVisitResult
     preVisitDirectory(Path dir, BasicFileAttributes attrs) {
       find(dir);
       return FileVisitResult.CONTINUE;
     }
 
     @Override
     public FileVisitResult
     visitFileFailed(Path file, IOException exc) {
       System.err.println(exc);
       return FileVisitResult.CONTINUE;
     }
 
     List<Path> getPahts() {
       return paths;
     }
   }
 
 
 }
