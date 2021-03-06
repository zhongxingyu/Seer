 /*
    Copyright 2008-2009 Christian Vest Hansen
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package net.nanopool.benchmark;
 
 import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
 import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import javax.sql.DataSource;
 
 public class Benchmark {
   private static final String DEFAULT_POOLS = "npds,bone,dbcp";
   private static final int CORES = Runtime.getRuntime().availableProcessors();
   private static final boolean PRE_WARM_POOLS =
     Boolean.parseBoolean(System.getProperty("pre-warm", "true"));
   private static final int RUN_TIME =
     Integer.parseInt(System.getProperty("run-time", "650"));
   private static final int WARMUP_TIME =
     Integer.parseInt(System.getProperty("warmup-time", "8000"));
   private static final int TTL =
     Integer.parseInt(System.getProperty("ttl", "900000")); // 15 minutes
   private static final int THR_SCALE =
     Integer.parseInt(System.getProperty("thr-scale", "2"));
   private static final int CON_SCALE =
     Integer.parseInt(System.getProperty("con-scale", "2"));
   private static PoolFactory poolFactory;
   
   public static void main(String[] args) throws InterruptedException {
     System.out.println("--------------------------------");
     System.out.println("  thr = thread");
     System.out.println("  con = connection");
     System.out.println("  cyc = connect-close cycle");
     System.out.println("  tot = total");
     System.out.println("  cor = cpu core");
     System.out.println("  s   = a second");
     System.out.println("--------------------------------");
     
     Map<String, PoolFactory> poolFactories = new HashMap<String, PoolFactory>();
     poolFactories.put("npds", new PoolFactories.NanoPoolFactory());
     poolFactories.put("dbcp", new PoolFactories.DbcpPoolFactory());
     poolFactories.put("mcpm", new PoolFactories.McpmPoolFactory());
     poolFactories.put("bone", new PoolFactories.BoneCpPoolFactory());
     poolFactories.put("c3p0", new PoolFactories.C3p0PoolFactory());
     
     String[] pools = System.getProperty("pools", DEFAULT_POOLS).split(",");
     
     for (String pool : pools) {
       poolFactory = poolFactories.get(pool);
       if (poolFactory == null) {
         System.out.printf("Don't know what kind of pool '%s' is. Skipping.\n",
             pool);
         continue;
       }
       runTestSet();
     }
     
     System.exit(0); // because the exec-maven-plugin ain't no quitter!
   }
   
   private static void runTestSet() throws InterruptedException {
     System.out.println("[thrs] [cons] : "
         + "  [total] [cyc/tot/s] [syc/cor/s] [cyc/thr/s] [cyc/con/s]");
     try {
       benchmark(50, 20, WARMUP_TIME);
       benchmark(2, 1, WARMUP_TIME);
       System.out.println("------Warmup's over-------------");
       for (int connections = 1; connections <= 10; connections++) {
         benchmark(connections, connections, RUN_TIME);
         if (THR_SCALE > 1)
           benchmark(connections * THR_SCALE, connections, RUN_TIME);
         if (CON_SCALE > 1)
           benchmark(connections, connections * CON_SCALE, RUN_TIME);
       }
     } catch (Exception ex) {
       ex.printStackTrace();
       System.err.println("This benchmark test run died.");
     }
     System.out.println("--------------------------------");
   }
   
   private static ConnectionConfiguration newCpds() {
     String db = System.getProperty("db", "derby");
     ConnectionConfiguration config = new ConnectionConfiguration();
     if ("mysql".equals(db)) {
       MysqlConnectionPoolDataSource cpds = new MysqlConnectionPoolDataSource();
       cpds.setUser("root");
       cpds.setPassword("");
       cpds.setDatabaseName("test");
       cpds.setPort(3306);
       cpds.setServerName("localhost");
       config.setCpds(cpds);
       config.setPassword("");
       config.setUrl("jdbc:mysql://localhost:3306/");
       config.setUsername("test");
       config.setDriverClass("com.mysql.jdbc.Driver");
       return config;
     }
     if ("derby".equals(db)) {
       EmbeddedConnectionPoolDataSource cpds =
         new EmbeddedConnectionPoolDataSource();
       cpds.setCreateDatabase("create");
       cpds.setDatabaseName("test");
       config.setCpds(cpds);
       config.setUrl("jdbc:derby:test;create=true");
       config.setDriverClass("org.apache.derby.jdbc.EmbeddedDriver");
       return config;
     }
     throw new RuntimeException("Unknown database: " + db);
   }
   
   private static DataSource buildPool(
       ConnectionConfiguration config, int size) {
     return poolFactory.buildPool(config, size, TTL);
   }
   
   private static void shutdown(DataSource pool) {
     poolFactory.closePool(pool);
   }
   
   private static void benchmark(int threads, int poolSize, int runTime)
       throws InterruptedException {
     Thread.sleep(250); // give CPU some breathing room
     ConnectionConfiguration config = newCpds();
     ExecutorService executor = Executors.newFixedThreadPool(threads);
     CountDownLatch startSignal = new CountDownLatch(1);
     CountDownLatch endSignal = new CountDownLatch(threads);
     DataSource pool = buildPool(config, poolSize);
     
     if (PRE_WARM_POOLS) {
       Connection[] cons = new Connection[poolSize];
       for (int i = 0; i < poolSize; i++) {
         try {
           cons[i] = pool.getConnection();
         } catch (SQLException ex) {
           throw new RuntimeException("Exception while getting connection for "
               + "pre-warming pool.", ex);
         }
       }
       for (int i = 0; i < poolSize; i++) {
         try {
           cons[i].close();
         } catch (SQLException ex) {
           throw new RuntimeException("Exception while closing connection for "
               + "pre-warming pool.", ex);
         }
       }
     }
     
     Worker[] workers = new Worker[threads];
     long stopTime = System.currentTimeMillis() + runTime;
     for (int i = 0; i < threads; i++) {
       Worker worker = new Worker(pool, startSignal, endSignal, stopTime);
       executor.execute(worker);
       workers[i] = worker;
     }
     
     startSignal.countDown();
     try {
       Thread.sleep(runTime);
     } catch (InterruptedException ex) {
       ex.printStackTrace();
     }
     
     endSignal.await(runTime + 200, TimeUnit.MILLISECONDS);
     
     int sumThroughPut = 0;
     for (Worker worker : workers) {
       sumThroughPut += worker.hits;
     }
     double totalThroughput = sumThroughPut / (runTime / 1000.0);
     double throughputPerCore = totalThroughput / CORES;
     double throughputPerThread = totalThroughput / threads;
     double throughputPerCon = totalThroughput / poolSize;
     System.out.printf("%6d %6d : %9d %11.0f " + "%11.0f %11.0f %11.0f\n",
         threads, poolSize, sumThroughPut, totalThroughput, throughputPerCore,
         throughputPerThread, throughputPerCon);
     
     shutdown(pool);
   }
   
   private static class Worker implements Runnable {
     public volatile int hits = 0;
     private final DataSource ds;
     private final CountDownLatch startLatch;
     private final CountDownLatch endLatch;
     private final long stopTime;
     
     public Worker(DataSource ds, CountDownLatch startLatch,
         CountDownLatch endLatch, long stopTime) {
       this.ds = ds;
       this.startLatch = startLatch;
       this.endLatch = endLatch;
       this.stopTime = stopTime;
     }
     
     public void run() {
       int count = 0;
       try {
         startLatch.await();
         while (System.currentTimeMillis() < stopTime) {
           Connection con = ds.getConnection();
           closeSafely(con);
           count++;
         }
       } catch (Exception e) {
         e.printStackTrace();
       }
       hits = count;
       endLatch.countDown();
     }
     
     private void closeSafely(Connection con) {
       try {
         con.close();
       } catch (SQLException ex) {
         ex.printStackTrace();
       }
     }
   }
 }
