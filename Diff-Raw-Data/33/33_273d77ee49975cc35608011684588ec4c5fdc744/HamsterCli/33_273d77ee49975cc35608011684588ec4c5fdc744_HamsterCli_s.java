 package com.pivotal.hamster.cli;
 
 import java.io.BufferedInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.net.NetUtils;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.hadoop.yarn.api.ClientRMProtocol;
 import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationReportRequest;
 import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationRequest;
 import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
 import org.apache.hadoop.yarn.api.protocolrecords.SubmitApplicationRequest;
 import org.apache.hadoop.yarn.api.records.ApplicationId;
 import org.apache.hadoop.yarn.api.records.ApplicationReport;
 import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
 import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
 import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
 import org.apache.hadoop.yarn.api.records.LocalResource;
 import org.apache.hadoop.yarn.api.records.LocalResourceType;
 import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
 import org.apache.hadoop.yarn.api.records.Resource;
 import org.apache.hadoop.yarn.api.records.YarnApplicationState;
 import org.apache.hadoop.yarn.api.records.impl.pb.LocalResourcePBImpl;
 import org.apache.hadoop.yarn.conf.YarnConfiguration;
 import org.apache.hadoop.yarn.exceptions.YarnRemoteException;
 import org.apache.hadoop.yarn.factories.RecordFactory;
 import org.apache.hadoop.yarn.factory.providers.RecordFactoryProvider;
 import org.apache.hadoop.yarn.ipc.YarnRPC;
 import org.apache.hadoop.yarn.util.ConverterUtils;
 
 import com.pivotal.hamster.cli.log.LogFetcher;
 import com.pivotal.hamster.cli.parser.HamsterParamBuilder;
 import com.pivotal.hamster.cli.utils.HamsterUtils;
 import com.pivotal.hamster.common.HamsterConfig;
 import com.pivotal.hamster.yarnexecutor.YarnExecutor;
 
 /**
  * glue code and main method of Hamster command line implementation
  */
 public class HamsterCli {
   private static final Log LOG = LogFactory.getLog(HamsterCli.class);
   private final String DEFAULT_HAMSTER_DIR_IN_HDFS = "hamster";
 
   boolean newApplicationCreated;
   ApplicationId appId;
   Configuration conf;
   ClientRMProtocol client;
   RecordFactory recordFactory;
   HamsterParamBuilder paramBuilder;
   
   // home/is-preinstall of ompi
   boolean preInstallOmpiBinary;
   String ompiHome;
 
   public HamsterCli() {
     this.conf = new YarnConfiguration();
     conf.addResource("hamster-site.xml");
     recordFactory = RecordFactoryProvider.getRecordFactory(null);
     client = null;
     appId = null;
     paramBuilder = new HamsterParamBuilder();
     newApplicationCreated = false;
   }
   
   void createYarnRPC() {
     YarnRPC rpc = YarnRPC.create(conf);
     InetSocketAddress rmAddress = NetUtils.createSocketAddr(conf.get(
         YarnConfiguration.RM_ADDRESS, YarnConfiguration.DEFAULT_RM_ADDRESS));
     this.client = (ClientRMProtocol) (rpc.getProxy(ClientRMProtocol.class,
         rmAddress, conf));
   }
 
   /**
    * initialize and setup environment for Hamster client,
    * include but not limited to
    * 1) create rpc proxy and create connection with YARN RM
    * 2) parse and check parameter
    * @throws IOException
    */
   void initialize(String[] args) throws IOException {
     // create YARN rpc
     createYarnRPC();
     
     // parse and check arguments
     paramBuilder.parse(args);
     
     // do set hamster home, etc.
     checkAndSetOmpiHome();
   }
 
   void getNewApplication() throws IOException, YarnRemoteException {
     if (client == null) {
       throw new IOException("should initialize YARN client first.");
     }
     GetNewApplicationRequest request = recordFactory
         .newRecordInstance(GetNewApplicationRequest.class);
     GetNewApplicationResponse newAppResponse = client.getNewApplication(request);
     appId = newAppResponse.getApplicationId();
     newApplicationCreated = true;
   }
   
   String uploadFileToHDFS(String fullPath, FileSystem fs, String dirInHDFS) throws IOException {
   	//parse fullPath to obtain localFilePath and fileName
   	String localFilePath = null;
   	String fileName = null;
   	if (-1 != fullPath.indexOf('#')) {
   		String[] splitPath = fullPath.split("#");
   		localFilePath = splitPath[0];
   		fileName = splitPath[1];
   	} else {
   		localFilePath = fullPath;
   		File f = new File(localFilePath);
   		fileName = f.getName();
   	}
   	
   	//upload local file to HDFS
   	Path filePathInHDFS = new Path(dirInHDFS, fileName);
   	if (paramBuilder.isVerbose()) {
   	  LOG.info("upload file:" + localFilePath + " to path:" + filePathInHDFS);
   	}
   	FSDataOutputStream os = fs.create(filePathInHDFS);
   	BufferedInputStream bis = new BufferedInputStream(new FileInputStream(localFilePath));
   	byte[] buffer = new byte[1024];
   	int len = 0;
   	while (-1 != (len = bis.read(buffer))) {
   		os.write(buffer, 0, len);
   	}
   	os.flush();
   	os.close();
   	bis.close();
   	return fileName;
   }
   
   /** 
    * Find a jar that contains a class of the same name, if any.
    * It will return a jar file, even if that is not the first thing
    * on the class path that has a class with the same name.
    * 
    * @param my_class the class to find.
    * @return a jar file that contains the class, or null.
    * @throws IOException
    */
   static String findContainingJar(Class my_class) {
     ClassLoader loader = my_class.getClassLoader();
     String class_file = my_class.getName().replaceAll("\\.", "/") + ".class";
     try {
       for(Enumeration itr = loader.getResources(class_file);
           itr.hasMoreElements();) {
         URL url = (URL) itr.nextElement();
         if ("jar".equals(url.getProtocol())) {
           String toReturn = url.getPath();
           if (toReturn.startsWith("file:")) {
             toReturn = toReturn.substring("file:".length());
           }
           // URLDecoder is a misnamed class, since it actually decodes
           // x-www-form-urlencoded MIME type rather than actual
           // URL encoding (which the file path has). Therefore it would
           // decode +s to ' 's which is incorrect (spaces are actually
           // either unencoded or encoded as "%20"). Replace +s first, so
           // that they are kept sacred during the decoding process.
           toReturn = toReturn.replaceAll("\\+", "%2B");
           toReturn = URLDecoder.decode(toReturn, "UTF-8");
           return toReturn.replaceAll("!.*$", "");
         }
       }
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
     return null;
   }
   
   LocalResource constructLocalResource(FileSystem fs, String dirInHDFS,
       String filenameInHDFS, LocalResourceType type,
       LocalResourceVisibility visibility) throws IOException {
     LocalResource res = recordFactory.newRecordInstance(LocalResource.class);
     Path path = new Path(dirInHDFS, filenameInHDFS);
     FileStatus fsStatus = fs.getFileStatus(path);
     res.setResource(ConverterUtils.getYarnUrlFromPath(fsStatus.getPath()));
     res.setSize(fsStatus.getLen());
     res.setTimestamp(fsStatus.getModificationTime());
     res.setType(type);
     res.setVisibility(visibility);
     return res;
   }
   
   LocalResource constructLocalResource(FileSystem fs, String dirInHDFS, 
   										String fileNameInHDFS, LocalResourceType type) throws IOException {
     return constructLocalResource(fs, dirInHDFS, fileNameInHDFS, type, LocalResourceVisibility.APPLICATION);
   }
   
   void processHamsterJar(Map<String, LocalResource> localResources, FileSystem fs, Path publicUploadPath) throws IOException {
     // upload our jar to staging area
     boolean needUpload = true;
     String jarPath = findContainingJar(YarnExecutor.class);
 
     if (jarPath == null) {
       LOG.error("cannot find any jar contains YarnExecutor");
       throw new IOException("cannot find any jar contains YarnExecutor");
     }
     File jarFile = new File(jarPath);
     Path uploadFilePath = new Path(publicUploadPath, jarFile.getName());
 
     if (fs.exists(uploadFilePath)) {
       if (!fs.isFile(uploadFilePath)) {
         LOG.error("error when trying to upload a file, but a same name directory already existed in target path:" + uploadFilePath.toUri().toString());
         throw new IOException("error when trying to upload a file, but a same name directory already existed in target path:" + uploadFilePath.toUri().toString());
       }
       FileStatus fsStatus = fs.getFileStatus(uploadFilePath);
       if (fsStatus.getLen() == jarFile.length()) {
         needUpload = false;
       } else {
         fs.delete(uploadFilePath, false);
       }
     }
     
     // will upload this to staging area
     if (needUpload) {
       uploadFileToHDFS(jarFile.getAbsolutePath(), fs, publicUploadPath.toString());
     }
     
     // add file info to localResources
     LocalResource res = this.constructLocalResource(fs, publicUploadPath.toString(),
         jarFile.getName(), LocalResourceType.FILE,
         LocalResourceVisibility.PUBLIC);
     localResources.put("hamster-core.jar", res);
   }
   
   // check and upload if user not specified pre-install hamster package
   void processOmpiBinaryPackage(Map<String, LocalResource> localResources, FileSystem fs, Path publicUploadPath) throws IOException {
     // user specified binary is pre installed, we don't need do anything
     if (preInstallOmpiBinary) {
       return;
     }
     processPublicTarball(localResources, fs, publicUploadPath,
         conf.get(HamsterConfig.OMPI_LOCAL_TARBALL_PATH_KEY),
         HamsterConfig.DEFAULT_OMPI_INSTALL_DIR);
   }
   
   void processPublicTarball(Map<String, LocalResource> localResources,
       FileSystem fs, Path publicUploadPath, String localFile, String resourceKey) throws IOException {
     // get tar-ball path and check if it's existed
     File tarballFile = new File(localFile);
     if (!tarballFile.exists() || !tarballFile.isFile()) {
       LOG.error("user specified hamster binary tar-ball path not exists or not a file, please check:" + tarballFile.getAbsolutePath());
       throw new IOException("user specified hamster binary tar-ball path not exists or not a file, please check:" + tarballFile.getAbsolutePath());
     }
     
     boolean needUpload = true;
     
     // get upload path of it and see if it already exists
     Path uploadFilePath = new Path(publicUploadPath, tarballFile.getName());
     if (fs.exists(uploadFilePath)) {
       if (!fs.isFile(uploadFilePath)) {
         LOG.error("error when trying to upload a file, but a same name directory already existed in target path:" + uploadFilePath.toUri().toString());
         throw new IOException("error when trying to upload a file, but a same name directory already existed in target path:" + uploadFilePath.toUri().toString());
       }
       // ok, it's a file, see if the size changed
       FileStatus fsStatus = fs.getFileStatus(uploadFilePath);
       if (fsStatus.getLen() == tarballFile.length()) {
         // size equals, we will not upload
         needUpload = false;
       } else {
         // otherwise, we will delete this file
         fs.delete(uploadFilePath, false);
       }
     }
     
     // we will upload this to staging area
     if (needUpload) {
       uploadFileToHDFS(localFile, fs, publicUploadPath.toString());
     }
     
     // add file info to localResources
     LocalResource res = this.constructLocalResource(fs,
         publicUploadPath.toString(), tarballFile.getName(),
         LocalResourceType.ARCHIVE, LocalResourceVisibility.PUBLIC);
     
     // put it to local resources
     localResources.put(resourceKey, res);
   }
   
   FileSystem getRemoteFileSystem() throws IOException {
     return FileSystem.get(conf);
   }
   
   void setContainerCtxLocalResources(ContainerLaunchContext ctx) throws IOException {
   	Map<String, LocalResource> localResources = new HashMap<String, LocalResource>();
   	
   	// if the dirInHDFS does not exists, we will create it on HDFS
    	String uploadRootPath = this.conf.get("com.greenplum.hamster.hdfs.dir", DEFAULT_HAMSTER_DIR_IN_HDFS);
    	FileSystem fs = getRemoteFileSystem();
    	
    	// upload path for this app
    	Path appUploadPath = new Path(uploadRootPath, "app_upload_" + appId.getClusterTimestamp() + "_" + appId.getId());
    	
    	// upload path for public resources (like OMPI binary)
    	Path publicUploadPath = new Path(uploadRootPath, "public"); 
    	
     // normalize app upload path if it's not absolute path
     if (!appUploadPath.isAbsolute()) {
       appUploadPath = new Path(fs.getHomeDirectory().toString() + "/" + appUploadPath.toString());
     }
     
     // normalize public upload path if it's not absolute path
     if (!publicUploadPath.isAbsolute()) {
       publicUploadPath = new Path(fs.getHomeDirectory().toString() + "/" + publicUploadPath.toString());
     }
    	
    	if (!fs.exists(appUploadPath)) {
    		fs.mkdirs(appUploadPath);
    	}
    	
    	if (!fs.exists(publicUploadPath)) {
    	  fs.mkdirs(publicUploadPath);
    	}
    	
    	if (paramBuilder.isVerbose()) {
    	  LOG.info("will upload app files to folder:" + appUploadPath.toUri().toString());
    	  LOG.info("will upload public files to folder:" + publicUploadPath.toUri().toString());
    	}
    	
   	List<String> fileList = paramBuilder.getAddFiles();
   	List<String> archiveList = paramBuilder.getAddArchives();
   	
   	//upload a placeholder
   	if (0 == fileList.size() && 0 == archiveList.size()) {
   		String placeHolderName = "Hamster_place_holder";
   		Path filePathInHDFS = new Path(appUploadPath, placeHolderName);
   		if (!fs.exists(filePathInHDFS)) {
   			FSDataOutputStream os = fs.create(filePathInHDFS);
   			os.write(new byte[1024]);
   			os.flush();
   			os.close();
   		}
   		LocalResource res = constructLocalResource(fs, appUploadPath.toString(), placeHolderName, LocalResourceType.FILE);
   		localResources.put(placeHolderName, res);
   	}
   	
   	//obtain file from fileList one by one, and then upload it to HDFS 
   	Iterator<String> fileIt = fileList.iterator();
   	while (fileIt.hasNext()) {
   		String fullPath = fileIt.next();
   		String fileNameInHDFS = uploadFileToHDFS(fullPath, fs, appUploadPath.toString());
   		LocalResource res = constructLocalResource(fs, appUploadPath.toString(), fileNameInHDFS, LocalResourceType.FILE);
   		localResources.put(fileNameInHDFS, res);
   	}
   	
   	//obtain archive from archiveList one by one, and then upload it to HDFS
   	Iterator<String> archIt = archiveList.iterator();
   	while (archIt.hasNext()) {
   		String fullPath = archIt.next();
   		String archiveNameInHDFS = uploadFileToHDFS(fullPath, fs, appUploadPath.toString());
   		LocalResource res = constructLocalResource(fs, appUploadPath.toString(), archiveNameInHDFS, LocalResourceType.ARCHIVE);
   		//remove postfix from archive file name as the key
   		String key = null;
   		if (archiveNameInHDFS.endsWith(".tar.gz")) {
   			key = archiveNameInHDFS.substring(0, archiveNameInHDFS.indexOf(".tar.gz"));
   		} else if (archiveNameInHDFS.endsWith(".zip")) {
   			key = archiveNameInHDFS.substring(0, archiveNameInHDFS.indexOf(".zip"));
   		} else if (archiveNameInHDFS.endsWith(".tar")) {
   			key = archiveNameInHDFS.substring(0, archiveNameInHDFS.indexOf(".tar"));
   		} 
   		localResources.put(key, res);
   	}
   	
   	// check if we need to upload hamster binary package and add it to local resources
   	processOmpiBinaryPackage(localResources, fs, publicUploadPath);
   	
   	// check if we need to upload hamster jar and add it to local resources
   	processHamsterJar(localResources, fs, publicUploadPath);
   	
   	// serialize local resources for AM
   	serializeLocalResourceToFile(localResources, fs, appUploadPath);
   	
   	ctx.setLocalResources(localResources);
   }
   
   /**
    * user specified files/archives may not only needed by mpirun, but also needed by launched processes,
    * so we will searilize resources map to a file, upload it to staging area, and it will be read by AM,
    * AM will deserialize it and put add it to launche context when launch user specified processes
    * @throws IOException 
    */
   void serializeLocalResourceToFile(Map<String, LocalResource> resources, FileSystem fs, Path appUploadPath) throws IOException {
     /*
      * file format,
      * 4 bytes (n), number of resources
      * <following is n parts of key/value
      * 4 bytes (key-len), length of key, followed by key-len bytes is content of key (java style, need add \0)
      * 4 bytes (value-len), length of value, following by value-len bytes is serialized pb LocalResource
      */
     String filename = HamsterConfig.DEFAULT_LOCALRESOURCE_SERIALIZED_FILENAME + "." + System.currentTimeMillis();
     File file = new File(filename);
     file.deleteOnExit();
     
     if (!file.createNewFile()) {
       LOG.error("create file for local-resource pb serialize failed, filename:" + filename);
       throw new IOException("create file for local-resource pb serialize failed, filename:" + filename);
     }
     
     DataOutputStream os = new DataOutputStream(new FileOutputStream(file));
     
     // write n;
     os.writeInt(resources.size());
     
     for (Entry<String, LocalResource> e : resources.entrySet()) {
       String key = e.getKey();
       // write key-len
       os.writeInt(key.length());
       
       // write key-content
       os.write(key.getBytes());
       
       LocalResourcePBImpl value = (LocalResourcePBImpl)e.getValue();
       
       // write value-len
       os.writeInt(value.getProto().getSerializedSize());
       
       // write value-content
       os.write(value.getProto().toByteArray());
     }
     
     os.close();
     
     // upload it to staging-are
     String fileNameInStagingArea = uploadFileToHDFS(filename, fs, appUploadPath.toString());
     LocalResource res = constructLocalResource(fs, appUploadPath.toString(), fileNameInStagingArea, LocalResourceType.FILE);
     
     // add this to local-resource, but this file doesn't need to be added to serialized file
     resources.put(HamsterConfig.DEFAULT_LOCALRESOURCE_SERIALIZED_FILENAME, res);
   }
 	
   void setContainerCtxCommand(ContainerLaunchContext ctx) {
     String command = paramBuilder.getUserCli(ctx);
     List<String> cmds = new ArrayList<String>();
     
     if (paramBuilder.isVerbose()) {
       LOG.info("YARN AM's command line:" + command);
     }
     
     // set it to 
     cmds.add(command);
     ctx.setCommands(cmds);
   }
   
   void setContainerCtxResource(ContainerLaunchContext ctx) {
     Resource resource = recordFactory.newRecordInstance(Resource.class);
    int mem = paramBuilder.getHamsterMemory();
     
     // we will use user specified memory to mpirun, by default, we will use 1024M
    if (mem >= 0) {
      resource.setMemory(mem);
    } else {
      resource.setMemory(HamsterConfig.DEFAULT_HAMSTER_MEM);
    }
     
     // set virtual cores, by default we will use 1 core
    int cpu = paramBuilder.getHamsterCPU();
    if (cpu >= 0) {
      resource.setVirtualCores(cpu);
    } else {
      resource.setVirtualCores(HamsterConfig.DEFAULT_HAMSTER_CPU);
    }
     
     ctx.setResource(resource);
   }
   
   ApplicationSubmissionContext createAppSubmissionCtx() throws IOException {
     // get application submission context
     ApplicationSubmissionContext val = recordFactory
         .newRecordInstance(ApplicationSubmissionContext.class);
 
     // get container launch context (for command, local resource, etc.)
     ContainerLaunchContext ctx = recordFactory
         .newRecordInstance(ContainerLaunchContext.class);
     
     // set app-id to app context
     val.setApplicationId(appId);
 
     // set resource spec to container launch context
     setContainerCtxResource(ctx);
     
     // set local resource 
     setContainerCtxLocalResources(ctx);
 
     // set env to ctx
     setContainerCtxEnvs(ctx);
     
     // set command for container launch context
     setContainerCtxCommand(ctx);
     
     // set application name
     val.setApplicationName("hamster");
     
     // set container launch context to app context
     val.setAMContainerSpec(ctx);
    
     return val;
   }
   
   private void setHamsterDebugEnvs(Map<String, String> envs) {
     if (paramBuilder.isVerbose()) {
       // add default debug env
       envs.put("OMPI_MCA_state_base_verbose", "5");
       envs.put("OMPI_MCA_plm_base_verbose", "5");
       envs.put("OMPI_MCA_rmaps_base_verbose", "5");
       envs.put("OMPI_MCA_dfs_base_verbose", "5");
       envs.put("OMPI_MCA_ess_base_verbose", "5");
       envs.put("OMPI_MCA_routed_base_verbose", "5");
       envs.put("OMPI_MCA_odls_base_verbose", "5");
       envs.put("OMPI_MCA_ras_base_verbose", "5");
       envs.put("OMPI_MCA_rml_base_verbose", "5");
       envs.put("OMPI_MCA_grpcomm_base_verbose", "5");
       envs.put("OMPI_MCA_rmaps_base_display_map", "1");
       envs.put("OMPI_MCA_errmgr_base_verbose", "5");
       envs.put("OMPI_MCA_nidmap_verbose", "10");
     }
   }
   
   /**
    * this method will read from conf/cli, and get/set the final hamster home for
    * AppMaster(mpirun) and launched MPI processes, and will report error when sth.
    * occured.
    * @return hamster home in NM (for mpirun and launched processes)
    * @throws IOException 
    */
   void checkAndSetOmpiHome() throws IOException {
     preInstallOmpiBinary = conf.getBoolean(HamsterConfig.OMPI_PREINSTALL_PROPERTY_KEY, false);
     if (preInstallOmpiBinary) {
       // when user specified pre install
       ompiHome = conf.get(HamsterConfig.OMPI_HOME_PROPERTY_KEY);
       if (null == ompiHome) {
         LOG.error("user specified hamster is pre-installed in NM, but not set hamster home");
         throw new IOException("user specified hamster is pre-installed in NM, but not set hamster home");
       }
     } else {
       String tarballPath = conf.get(HamsterConfig.OMPI_LOCAL_TARBALL_PATH_KEY);
       if (null == tarballPath) {
         LOG.error("user specified hamster is not preInstalled, but not specify local tarball path");
         throw new IOException("user specified hamster is not preInstalled, but not specify local tarball path");
       }
       ompiHome = HamsterConfig.DEFAULT_OMPI_INSTALL_DIR;
     }
   }
   
   void setHamsterModuleMcaEnvs(Map<String, String> envs) {
     envs.put("OMPI_MCA_plm", "yarn");
     envs.put("OMPI_MCA_ras", "yarn");
     envs.put("OMPI_MCA_odls", "yarn");
   }
   
   void setContainerCtxEnvs(ContainerLaunchContext ctx) throws IOException {
     Map<String, String> envs = new HashMap<String, String>();
    
     // put hamster home
     if (ompiHome != null) {
       envs.put(HamsterConfig.HAMSTER_HOME_ENV_KEY, ompiHome);
     }
     
     // set path & ld_library_path & dyld_library_path, etc
     String pathEnvar = "";
     String ldlibEnvar = "";
     String dyldlibEnvar = "";
 
     // add debug envs
     setHamsterDebugEnvs(envs);
     
     // add mca modules for selects
     setHamsterModuleMcaEnvs(envs);
 
     // set $PATH, $LD_LIBRARY_PATH, etc.
     pathEnvar = HamsterUtils.appendEnv(pathEnvar, ompiHome + "/bin");
     pathEnvar = HamsterUtils.appendEnv(pathEnvar, "./");
     ldlibEnvar = HamsterUtils.appendEnv(ldlibEnvar, ompiHome + "/lib");
     ldlibEnvar = HamsterUtils.appendEnv(ldlibEnvar, ompiHome
         + "/lib/openmpi");
     ldlibEnvar = HamsterUtils.appendEnv(ldlibEnvar, "./");
     dyldlibEnvar = new String(ldlibEnvar); // dyldlibEnvar should be a copied
                                            // value of ldlib envar
     
     // set OPAL_PREFIX if preinstall is false
     if (!preInstallOmpiBinary) {
       envs.put("OPAL_PREFIX", "./" + HamsterConfig.DEFAULT_OMPI_INSTALL_DIR);
     }
 
     // append system envs to path/library
     pathEnvar = HamsterUtils.appendEnv(pathEnvar,
         ((System.getenv("PATH") != null) ? System.getenv("PATH") : ""));
     ldlibEnvar = HamsterUtils.appendEnv(ldlibEnvar, ((System
         .getenv("LD_LIBRARY_PATH") != null) ? System.getenv("LD_LIBRARY_PATH")
         : ""));
     dyldlibEnvar = HamsterUtils.appendEnv(
         dyldlibEnvar,
         ((System.getenv("DYLD_LIBRARY_PATH") != null) ? System
             .getenv("DYLD_LIBRARY_PATH") : ""));
     
     // put it to envs
     envs.put("PATH", pathEnvar);
     envs.put("LD_LIBRARY_PATH", ldlibEnvar);
     envs.put("DYLD_LIBRARY_PATH", dyldlibEnvar);
     envs.put("JAVA_HOME", System.getenv("JAVA_HOME") !=null ? System.getenv("JAVA_HOME") : "");
     
     // process user specified envs
     for (Entry<String, String> entry : paramBuilder.getUserSpecifiedEnvs().entrySet()) {
       if (envs.containsKey(entry.getKey())) {
         String newValue = HamsterUtils.appendEnv(envs.get(entry.getKey()), entry.getValue());
         envs.put(entry.getKey(), newValue);
       } else {
         envs.put(entry.getKey(), entry.getValue());
       }
     }
     
     int mem = paramBuilder.getHamsterMemory();
     if (mem >= 0) {
       envs.put(HamsterConfig.HAMSTER_MEM_ENV_KEY, String.valueOf(mem));
     }
     
     int cpu = paramBuilder.getHamsterCPU();
     if (cpu >= 0) {
       envs.put(HamsterConfig.HAMSTER_CPU_ENV_KEY, String.valueOf(cpu));
     }
     
     // set pb file env 
     envs.put(HamsterConfig.YARN_PB_FILE_ENV_KEY, ompiHome + "/etc/protos/hamster_protos.pb");
     
     // add yarn, hdfs, and related jars to $CLASSPATH
     addClasspathToEnv(envs);
     
     // process folder for pid
     String pidRoot = conf.get(HamsterConfig.DEFAULT_PID_ROOT_DIR, HamsterConfig.DEFAULT_PID_ROOT_DIR);
     envs.put("HAMSTER_PID_ROOT", pidRoot);
     
     if (paramBuilder.isVerbose()) {
       envs.put("HAMSTER_VERBOSE", "yes");
     }
     
     if (paramBuilder.isVerbose()) {
       // print env out
       LOG.info("all set envs");
       for (Entry<String, String> e : envs.entrySet()) {
         LOG.info("{" + e.getKey() + "=" + e.getValue() + "}");
       }
     }
     
     ctx.setEnvironment(envs);
   }
   
   void addClasspathToEnv(Map<String, String> envs) {
     String classpath = envs.get("CLASSPATH");
     if (classpath == null || classpath.isEmpty()) {
       classpath = "./*";
     }
     for (String cp : YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH) {
       classpath = classpath + ":" + cp;
     }
     classpath = classpath + ":hamster-core.jar";
     envs.put("CLASSPATH", classpath);
   }
   
   FinalApplicationStatus waitForApplicationTerminated() throws IOException, InterruptedException {
     // query request
     GetApplicationReportRequest reportRequest;
     ApplicationReport report;
     YarnApplicationState state;
     YarnApplicationState preState = YarnApplicationState.NEW;
     String trackingUrl = null;
     
     reportRequest = recordFactory.newRecordInstance(GetApplicationReportRequest.class);
     reportRequest.setApplicationId(appId);
     
     // poll RM, get AM state
     report = client.getApplicationReport(reportRequest).getApplicationReport();
     state = report.getYarnApplicationState();
     while (true) {
       report = client.getApplicationReport(reportRequest).getApplicationReport();
       preState = state;
       if (report.getTrackingUrl() != null && (!report.getTrackingUrl().isEmpty())) {
         if (trackingUrl == null) {
           trackingUrl = report.getTrackingUrl();
           LOG.info("tracking URL is: http://" + trackingUrl);
         }
       }
       state = report.getYarnApplicationState();
       
       // state changed
       if (preState != state) {
         LOG.info("yarn application state transfered from [" + preState.name() + "] to [" + state.name() + "]");
       }
       
       // application terminated
       if (state == YarnApplicationState.FAILED || state == YarnApplicationState.FINISHED || state == YarnApplicationState.KILLED) {
         break;
       }
       Thread.sleep(100);
     }
     
     FinalApplicationStatus finalStatus = report.getFinalApplicationStatus();
     if (finalStatus != FinalApplicationStatus.SUCCEEDED) {
       LOG.error("Final state of AppMaster is," + finalStatus.name());
     } else {
       LOG.info("AppMaster is successfully finished.");
     }
     
     return finalStatus;
   }
 
   ApplicationSubmissionContext submitApplication() throws IOException,
       YarnRemoteException, InterruptedException {
     if (client == null) {
       throw new IOException("should initialize YARN client first.");
     }
 
     if (!newApplicationCreated) {
       throw new IOException("should getNewApplication before submit.");
     }
 
     // submit application
     SubmitApplicationRequest submitRequest = recordFactory.newRecordInstance(SubmitApplicationRequest.class);
     ApplicationSubmissionContext submissionCtx = createAppSubmissionCtx();
     submitRequest.setApplicationSubmissionContext(submissionCtx);
     client.submitApplication(submitRequest);
 
     // wait for application get started
     FinalApplicationStatus finalStatus = waitForApplicationTerminated();
     
     // read output log if log aggregation is set 
     LogFetcher fetcher = new LogFetcher(
         finalStatus == FinalApplicationStatus.SUCCEEDED,
         appId, 
         conf, 
         UserGroupInformation.getLoginUser().getUserName(),
         FileSystem.get(conf),
         paramBuilder.getNp());
     if (fetcher.checkLogFetchable()) {
       fetcher.readAll(finalStatus);
     }
     
     LOG.info("HamsterCli completed.");
     return submissionCtx;
   }
   
   public static void main(String[] args) throws YarnRemoteException,
       IOException, InterruptedException {
     LOG.info("start submit job");
     HamsterCli cli = new HamsterCli();
     
     cli.initialize(args);
     cli.getNewApplication();
     cli.submitApplication();
   }
 }
