 package com.xingcloud.xa.secondaryindex;
 
 import java.io.*;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 public abstract class Tail {
 
 
     /**
      * /data/log/stream.log projectid \t inneruid \t eventStr \t value \t
      * timestamp 没有空格 \t分隔 文件名变为stream.log.2012-11-06 这种
      *
      * @throws Exception
      */
     public void start() throws Exception {
 
         while (true) {
             try {
                 this.readProcess();
                 long t1 = System.currentTimeMillis();
                 this.tail();
                 long t2 = System.currentTimeMillis();
                 System.out.println("config<" + this.configPath + ">:send log file " + this.day + " used time:" + (t2 - t1) + " ms");
                 this.saveProcessFile();
 
                 this.rollDay();
                 this.writeConfig();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
     }
 
     public void setBatchSize(int size) {
         this.batchSize = size;
     }
 
     public int getBatchSize() {
         return this.batchSize;
     }
 
     public void setNologSleepTime(int sleep) {
         this.nologSleepTime = sleep;
     }
 
     public long getNologSleepTime() {
         return this.nologSleepTime;
     }
 
     public void setLogProcessPerBatch(boolean logprocess) {
         this.logProcessPerBatch = logprocess;
     }
 
     public boolean isLogProcessPerBatch() {
         return this.logProcessPerBatch;
     }
 
     public void tail() throws Exception {
 
         String fileName = this.getLogDataFile(this.day);
         long jumpln = this.line;
         // 实现一个tail 并滚动到新的文件
         // 把日志按照行 批量发送到服务端分析
 
         File f = new File(fileName);
         while (!f.exists()) {
             System.out.println("LOG FILE NOT FOUND:" + f.getAbsolutePath());
             Thread.sleep(60000L);
         }
         BufferedTailReader reader = new BufferedTailReader(new InputStreamReader(
                 new FileInputStream(f), "UTF-8"), 1024 * 1024);
 
         final int size = this.getBatchSize();
         final long sleepTime = this.getNologSleepTime();
 
         List<String> pool = new LinkedList<String>();
 
         // jump lines
         for (int i = 0; i < jumpln; i++) {
             reader.readLine();
         }
         long count = 0;
         long allSentLog = jumpln;
         try {
             while (true) {
 
                 String line = reader.readLine();
                 if (line == null) {
                     if (pool.size() > 0) {
                         int ksize = pool.size();
                         this.sendLog(pool, allSentLog);
                         allSentLog += ksize;
                         pool.clear();
                         this.writeProcess(this.day, allSentLog);
 
                     }
                     if (this.hasNextLogDataFile()) {
                         //存在新的日志文件
                         break;
                     }
                     Thread.sleep(sleepTime);
                 } else {
                     //如果readline拿到的line不是以\n结尾，说明这一行没有读完整；继续读直到出现\n的line，合成一行
                     StringBuffer bufferSb = null;
                     while (!line.endsWith("\n")) {
                         if (bufferSb == null) {
                             bufferSb = new StringBuffer();
                             bufferSb.append(line);
                         }
                         String nextLine = reader.readLine();
                         if (nextLine != null) {
                             bufferSb.append(nextLine);
                             line = bufferSb.toString();
                         }
                     }
                     // send to pool的一行替换掉\n
                     line = line.replace("\n", "");
    
                     pool.add(line);
 
                     if (pool.size() >= size) {
                         // rpc send
                         int ksize = pool.size();
                         this.sendLog(pool, allSentLog);
                         allSentLog += ksize;
                         //如果是userlog当天log的最后一条，sendprocess里面的记录行数要+1，保证重启时这一行会被略过
                         pool.clear();
 
                         if (this.isLogProcessPerBatch()) {
                             this.writeProcess(this.day, allSentLog);
                         } else {
                             count++;
                             if (count >= 100) {
                                 this.writeProcess(this.day, allSentLog);
                                 count = 0;
                             }
                         }
                     }
                 }
             }
 
         } catch (Throwable e) {
             e.printStackTrace();
         } finally {
             int ksize = pool.size();
             this.sendLog(pool, allSentLog);
             allSentLog += ksize;
             pool.clear();
 
             this.writeProcess(this.day, allSentLog);
             reader.close();
         }
 
     }
 
 
     public abstract void send(List<String> logs, long day);
 
     /**
      * 发送日志
      *
      * @param logs
      * @return
      */
     public void sendLog(List<String> logs, long alreadySentLog) {
         while (true) {
             try {
                 this.send(logs, this.day);
             } catch (Throwable e) {
                 e.printStackTrace();
                 // 休息
                 try {
                     this.writeProcess(this.day, alreadySentLog);
                     System.out.println(new Date() + " send exception,then will sleep 15s");
                     Thread.sleep(15000L);
                 } catch (Throwable e1) {
                     e1.printStackTrace();
                 }
                 continue;
             }
             break;
         }
     }
 
     public Tail(String configPath) {
         this.configPath = configPath;
         try {
             Properties prop = readConfig();
             this.datadir = prop.getProperty(PROP_DATADIR, "/data/log/");
             this.datafile = prop.getProperty(PROP_DATAFILE, "stream.log");
             this.day = Long.valueOf(prop.getProperty(PROP_DAY, "" + TimeUtil.getDay(System.currentTimeMillis())));
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         this.dayStartTime = TimeUtil.dayToTptime(this.day);
 
     }
 
     //const
     static final String PROP_DATADIR = "datadir";
     static final String PROP_DATAFILE = "datafile";
     static final String PROP_DAY = "day";
 
     private int batchSize = 1000;
     private long nologSleepTime = 20*1000L;
     private boolean logProcessPerBatch = false;
 
     protected String datadir = "/data/log/";
     protected String datafile = "stream.log";
     protected String configPath = "no_setting";
     protected String config = "config.properties";
     protected String sendlogprocess = "sendlog.process";
     protected long day = 0;
     protected long line = 0;
 
     protected long dayStartTime = 0;
 
     FileWriter processStream = null;
 
     public String getLogDataFile(long yyyyMMdd) {
         String ymd = yyyyMMdd + "";
         String path = this.datadir + File.separator + this.datafile + "." + ymd.substring(0, 4) + "-" + ymd.substring(4, 6) + "-" + ymd.substring(6);
         File logdata = new File(path);
         if (logdata.exists()) {
             return path;
         } else {
             if (TimeUtil.getToday() == yyyyMMdd) {
                 return this.datadir + File.separator + this.datafile;
             }
             return null;
         }
     }
 
     public boolean hasNextLogDataFile() {
         if (this.day < TimeUtil.getToday()) {
             String ymd = this.day + "";
             String path = this.datadir + File.separator + this.datafile + "." + ymd.substring(0, 4) + "-" + ymd.substring(4, 6) + "-" + ymd.substring(6);
             File logdata = new File(path);
             if (logdata.exists()) {
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     //日期向下滚动一天
     public void rollDay() {
         this.day = TimeUtil.nextDay(this.day);
         this.dayStartTime = TimeUtil.dayToTptime(this.day);
     }
 
     //保存进度文件，实际是做了个重命名操作
     public void saveProcessFile() {
         if (this.processStream != null) {
             try {
                 this.processStream.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             this.processStream = null;
         }
 
         File process = new File(this.configPath + File.separator + this.sendlogprocess);
         if (process.exists()) {
             File newBackupFile = new File(this.configPath + File.separator + this.sendlogprocess + "." + this.day);
             process.renameTo(newBackupFile);
         }
 
     }
 
     public void writeProcess(long day, long line) throws IOException {
 
         if (this.processStream == null) {
             File process = new File(this.configPath + File.separator + this.sendlogprocess);
             boolean append = true;
             this.processStream = new FileWriter(process, append);
         }
         this.processStream.write(day + "\t" + line + "\t" + new Date() + "\n");
         this.processStream.flush();
     }
 
     public void readProcess() {
 
         File process = new File(configPath + File.separator + sendlogprocess);
         if (process.exists()) {
             try {
                 BufferedReader reader = new BufferedReader(new InputStreamReader(
                         new FileInputStream(process)), 1024 * 1024);
                 String rdline = null;
                 String tmpline = null;
                 while ((tmpline = reader.readLine()) != null) {
                     rdline = tmpline;
                 }
 
                 if (rdline == null) {
                     this.line = 0;
                 } else {
                     int idxtab_f = 0;
                     int idxtab_t = rdline.indexOf('\t');
                     this.day = Long.valueOf(rdline.substring(idxtab_f, idxtab_t));
                     idxtab_f = idxtab_t + 1;
                     idxtab_t = rdline.indexOf('\t', idxtab_f);
                     this.line = Long.valueOf(rdline.substring(idxtab_f, idxtab_t));
                 }
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
 
         } else {
             this.line = 0;
         }
 
     }
 
     public Properties readConfig() throws Exception {
         Properties prop = new Properties();
         FileReader reader = new FileReader(configPath + File.separator + config);
         prop.load(reader);
         reader.close();
         return prop;
     }
 
     public void writeConfig() throws Exception {
         Properties prop = new Properties();
         prop.setProperty(PROP_DATADIR, datadir);
         prop.setProperty(PROP_DATAFILE, datafile);
         prop.setProperty(PROP_DAY, String.valueOf(day));
         FileWriter writer = new FileWriter(configPath + File.separator + config);
         prop.store(writer, "");
         writer.close();
     }
 
 
 }
 
