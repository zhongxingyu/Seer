 package com.weibogrep.user;
 
 import java.io.*;
 import java.util.*;
 
 import weibo4j.*;
 
 import com.weibogrep.indexer.*;
 
 public class UserMgmt {
     private static final String BASE_DIR = "/tmp/weibogrep";
     private static final File baseDirFile = new File(BASE_DIR);
     private static final String config = "_config";
     private static final String index = "_index";
     private static final String last = "_last";     // last timeline
 
     private File userdir;
     private File configFile;
     private File indexFile;
     private File lastFile;
     private User user;
     private long id = -1;
     private String token;
     private String secret;
 
     public UserMgmt(User u) {
         if (u == null) return;
         this.user = u;
         this.id = u.getId();
         userdir = new File(baseDirFile, "" + id);
         if (exist()) {
             configFile = new File(userdir, config);
             indexFile = new File(userdir, index);
             lastFile = new File(userdir, last);
         }
     }
     
     public UserMgmt(long id) {
     	if (id < 0) return;
         this.id = id;
         userdir = new File(baseDirFile, "" + id);
         if (exist()) {
             configFile = new File(userdir, config);
             indexFile = new File(userdir, index);
             lastFile = new File(userdir, last);
         }
     }
     
     public User getUser() {
     	return user;
     }
 
     public boolean exist() {
         return userdir.exists();
     }
 
     public File getIndexDir() {
         return indexFile;
     }
     
     public int updateLastPost(long postId) {
     	if (!exist()) {
     		return -1;
     	}
         try {
             BufferedWriter out = new BufferedWriter(
                                  new OutputStreamWriter(
                                 new FileOutputStream(configFile)));
             out.write("" + postId);
             out.close();
         } catch (Exception e) {
             e.printStackTrace();
             return -2;
         }
         return 0;
     }
 
     public long getLastPost() {
         if (!exist()) {
             return -1;
         }
         try {
         	BufferedReader br = new BufferedReader(
 				                new InputStreamReader(
 				                new FileInputStream(lastFile)));
         	String s = br.readLine();
         	if (s.length() < 1) {
         		return 0;
         	} else {
         		return Long.parseLong(s);
         	}
         } catch (Exception e) {
             return -2;
         }
     }
 
     public String[] getToken() {
         if (!exist() || configFile == null) {
             return null;
         }
         String [] ret = new String[2];
         if (token != null && secret != null) {
             ret[0] = token;
             ret[1] = secret;
             return ret;
         }
         try {
             BufferedReader br = new BufferedReader(
                                 new InputStreamReader(
                                 new FileInputStream(configFile)));
             ret[0] = br.readLine();
             ret[1] = br.readLine();
             token = ret[0];
             secret = ret[1];
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
         return ret;
     }
 
     public int setup(String token, String secret) {
         this.token = token;
         this.secret = secret;
 
         if (this.exist()) {         // just update token and secret
         	try {
 	            BufferedWriter out = new BufferedWriter(
 	                                 new OutputStreamWriter(
 	                                 new FileOutputStream(configFile)));
 	            out.write(token);
 	            out.newLine();
 	            out.write(secret);
 	            out.close();
         	} catch(Exception e) {
         		e.printStackTrace();
         	}
             return 1;
         }
 
         // create new user profile
 
         boolean b = userdir.mkdir();
         if (!b) {
             return -1;
         }
 
         // setup config file, which contains id, token, secret
         configFile = new File(userdir, config);
         try {
             b = configFile.createNewFile();
             if (!b) {
                 return -2;
             }
         } catch (Exception e) {
             return -2;
         }
         try {
             BufferedWriter out = new BufferedWriter(
                                  new OutputStreamWriter(
                                  new FileOutputStream(configFile)));
             out.write(token);
             out.newLine();
             out.write(secret);
             out.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         // setup index dir
         indexFile = new File(userdir, index);
         b = indexFile.mkdir();
         if (!b) return -3;
 
         // setup last file
         try {
             b = lastFile.createNewFile();
             if (!b) {
                 return -4;
             }
         } catch (Exception e) {
             e.printStackTrace();
             return -4;
         }
         
         return 0;
     }
 
     public int addDoc(List<Status> sts) {
         if (indexFile == null) {
             return -1;
         }
         IndexItem [] items = new IndexItem[sts.size()];
         int i = 0;
         for (Status st: sts) {
             items[i++] = new IndexItem(st.getId(), st.getText(), st.getCreatedAt());
         }
         Indexer.index(items, indexFile);
         return 0;
     }
 }
 
