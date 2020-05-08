 package edu.cshl.schatz.jnomics.manager.server;
 
 import edu.cshl.schatz.jnomics.manager.api.*;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
 import org.apache.thrift.TException;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.util.*;
 
 /**
  * User: james
  * Modeled after HadoopThriftfs
  */
 
 public class JnomicsDataHandler implements JnomicsData.Iface {
 
     private final org.slf4j.Logger log = LoggerFactory.getLogger(JnomicsDataHandler.class);
     private final Map<UUID, JnomicsFsHandle> handleMap = Collections.synchronizedMap(new HashMap<UUID, JnomicsFsHandle>());
     private Properties properties;
 
     private JnomicsServiceAuthentication authenticator;
 
     private final ThreadLocal bufferCache = new ThreadLocal(){
         @Override
         protected Object initialValue() {
             return new byte[2000000];
         }
     };
 
     public JnomicsDataHandler(Properties props){
         properties = props;
         authenticator = new JnomicsServiceAuthentication();
     }
 
     private UUID getUniqueUUID(){
         UUID b;
         while(handleMap.containsKey(b = UUID.randomUUID())){}
         return b;
     }
 
     private FileSystem getFileSystem(String username) throws JnomicsThriftException {
         URI uri;
         String fsName = properties.getProperty("hdfs-default-name");
         try{
             uri = new URI(fsName);
         }catch(Exception e){
             log.error("probleming initializing hdfs");
             e.printStackTrace();
             throw new JnomicsThriftException(e.toString());
         }
 
         FileSystem fs = null;
         /**Check if the user has a home directory**/
         Path userHome = new Path("/user", username);
         try {
             fs = FileSystem.get(uri,new Configuration(), "hdfs");
             FileStatus[] stats = fs.listStatus(userHome);
             if(null == stats){ //create user's home directory
                fs.mkdirs(userHome,new FsPermission("755"));
                 fs.setOwner(userHome,username,"kbase");
             }
             fs.close();
         } catch (Exception e) {
             log.error("Error Creating Filesystem");
             e.printStackTrace();
             throw new JnomicsThriftException(e.toString());
         }
 
         try{
             fs = FileSystem.get(uri, new Configuration(), username);
         } catch(Exception e){
             log.error("Problem creating filesystem");
             e.printStackTrace();
             throw new JnomicsThriftException(e.toString());
         }
 
         return fs;
     }
     
     private void closeFileSystem(FileSystem fs) throws JnomicsThriftException{
         try{
             fs.close();
         }catch(Exception e){
             log.error("Problem closing fs");
             e.printStackTrace();
             throw new JnomicsThriftException(e.toString());
         }
     }
     
     public Map<UUID, JnomicsFsHandle> getHandleMap(){
         return handleMap;
     }
 
     @Override
     public JnomicsThriftHandle create(String path, Authentication auth) throws TException, JnomicsThriftException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         
         log.info("Creating file: " + path + " for user: "+ username);
 
         FileSystem fs = getFileSystem(username);
 
         FSDataOutputStream stream = null;
         try {
             stream = fs.create(new Path(path));
         } catch (IOException e) {
             log.error("Problem creating file " + path);
             e.printStackTrace();
             throw new JnomicsThriftException(e.toString());
         }
         
         UUID nxtUUID = getUniqueUUID();
         handleMap.put(nxtUUID,new JnomicsFsHandle(fs,stream));
         
         return new JnomicsThriftHandle(nxtUUID.toString());
     }
 
     @Override
     public JnomicsThriftHandle open(String path, Authentication auth) throws JnomicsThriftException, TException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
 
         log.info("Opening file: " + path + " for user: " + username);
         
         FileSystem fs = getFileSystem(username);
         FSDataInputStream stream = null;
         try{
             stream = fs.open(new Path(path));
         }catch(Exception e){
             log.error("Problem opening file: " + path);
             e.printStackTrace();
             throw new JnomicsThriftException(e.toString());
         }
 
         UUID nxtUUID = getUniqueUUID();
         handleMap.put(nxtUUID,new JnomicsFsHandle(fs,stream));
         return new JnomicsThriftHandle(nxtUUID.toString());
     }
 
 
     @Override
     public void write(JnomicsThriftHandle handle, ByteBuffer data, Authentication auth) throws TException, JnomicsThriftException {
         JnomicsFsHandle jhandle = handleMap.get(UUID.fromString(handle.getUuid()));
         try {
             jhandle.getOutStream().write(data.array());
         } catch (IOException e) {
             log.error("Problem writing to file");
             e.printStackTrace();
             throw new JnomicsThriftException(e.toString());
         }
         jhandle.updateLastUsed();
     }
 
     @Override
     public ByteBuffer read(JnomicsThriftHandle handle, Authentication auth) throws TException, JnomicsThriftException {
         JnomicsFsHandle jhandle = handleMap.get(UUID.fromString(handle.getUuid()));
 
         byte[] buf = (byte[]) bufferCache.get();
         int bytesRead;
         try{
             bytesRead = jhandle.getInStream().read(buf);
         } catch (IOException e) {
             throw new JnomicsThriftException(e.toString());
         }
         jhandle.updateLastUsed();
         if(-1 == bytesRead)
             return ByteBuffer.allocate(0);
         return ByteBuffer.wrap(buf,0,bytesRead);
     }
 
     @Override
     public void close(JnomicsThriftHandle handle, Authentication auth) throws TException, JnomicsThriftException {
         UUID u = UUID.fromString(handle.getUuid());
         JnomicsFsHandle jhandle = handleMap.get(u);
 
 
         if(jhandle.getOutStream() != null){
             try {
                 jhandle.getOutStream().close();
             } catch (IOException e) {
                 log.error("Problem closing file");
                 e.printStackTrace();
                 throw new JnomicsThriftException(e.toString());
             }
         }else if(jhandle.getInStream() != null){
             try {
                 jhandle.getInStream().close();
             } catch (IOException e) {
                 log.error("Problem closing file");
                 e.printStackTrace();
                 throw new JnomicsThriftException(e.toString());
             }
         }
 
         try {
             closeFileSystem(jhandle.getFileSystem());
         }finally{
             handleMap.remove(u);
         }
     }
 
     @Override
     public List<JnomicsThriftFileStatus> listStatus(String path, Authentication auth) throws TException, JnomicsThriftException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         
         log.info("Getting file status of "+ path + " for user "+ username);
         
         FileSystem fs = getFileSystem(username);
         FileStatus[] stats;
         try{
             stats = fs.listStatus(new Path(path));
         }catch(Exception e){
             log.error("Could not open filesystem in listStatus");
             e.printStackTrace();
             throw new JnomicsThriftException(e.toString());
         }finally{
             closeFileSystem(fs);
         }
 
         if(null == stats)
             return new ArrayList<JnomicsThriftFileStatus>();
 
         JnomicsThriftFileStatus[] thriftStatuses = new JnomicsThriftFileStatus[stats.length];
         FileStatus c;
         for(int i=0; i< stats.length; ++i){
             c = stats[i];
             thriftStatuses[i] = new JnomicsThriftFileStatus(c.isDir(),
                     c.getPath().toString(),
                     c.getOwner(),
                     c.getGroup(),
                     c.getPermission().toString(),
                     c.getReplication(),
                     c.getModificationTime(),
                     c.getBlockSize(),
                     c.getLen()
             );
         }
         
         return Arrays.asList(thriftStatuses);
     }
 
     @Override
     public boolean remove(String path, boolean recursive, Authentication auth) throws JnomicsThriftException, TException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
 
         log.info("Removing path "+ path+ " for user "+ username);
         
         FileSystem fs = getFileSystem(username);
         boolean state = false;
         try{
             state = fs.delete(new Path(path), recursive);
         }catch(Exception e){
             log.error("Problem deleting " + path + " for user: " + username);
             e.printStackTrace();
             throw new JnomicsThriftException(e.toString());
         }finally{
             closeFileSystem(fs);
         }
         return state;
     }
 
 
     @Override
     public boolean mkdir(String path, Authentication auth) throws JnomicsThriftException, TException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         
         log.info("Making directory "+ path + " for user " + username);
         
         FileSystem fs = getFileSystem(username);
         boolean state = false;
         try{
             state = fs.mkdirs(new Path(path));
         }catch(Exception e){
             log.error("Problem making directory "+ path + " for user: " + username);
             e.printStackTrace();
             throw new JnomicsThriftException(e.toString());
         }finally{
             closeFileSystem(fs);
         }
         return state;
     }
 
     @Override
     public boolean mv(String path, String dest, Authentication auth) throws JnomicsThriftException, TException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         
         log.info("Moving "+ path +" to "+ dest +  " for user "+ username);
         
         FileSystem fs = getFileSystem(username);
         boolean state = false;
         try {
             state = fs.rename(new Path(path),new Path(dest));
         } catch (IOException e) {
             throw new JnomicsThriftException(e.toString());
         }finally{
             closeFileSystem(fs);
         }
         return state;
     }
 
     @Override
     public List<String> listGenomes(Authentication auth) throws JnomicsThriftException, TException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         
         log.info("Listing genomes for user "+ username);
         
         FileSystem fs = getFileSystem(username);
         List<String> genomeList = new ArrayList<String>();
         try {
             FileStatus[] stats = fs.listStatus(new Path(properties.getProperty("hdfs-index-repo")));
             for(FileStatus stat: stats){
                 String name = stat.getPath().getName();
                 if(name.contains("_samtools.tar.gz")){
                     genomeList.add(name.substring(0,name.indexOf("_samtools.tar.gz")));
                 }
             }
         } catch (IOException e) {
             throw new JnomicsThriftException(e.toString());
         }finally{
             closeFileSystem(fs);
         }
 
         return genomeList;
     }
 
 
 
 
 }
