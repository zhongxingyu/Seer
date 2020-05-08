 package edu.illinois.cs.mapreduce;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.rmi.AlreadyBoundException;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.text.NumberFormat;
import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.healthmarketscience.rmiio.RemoteInputStream;
 import com.healthmarketscience.rmiio.RemoteOutputStream;
 
 import edu.illinois.cs.dfs.FileSystem;
 import edu.illinois.cs.dfs.FileSystemAdapter;
 import edu.illinois.cs.dfs.FileUtil;
 import edu.illinois.cs.dfs.LocalFileSystem;
 import edu.illinois.cs.dfs.Path;
 import edu.illinois.cs.dfs.RemoteFileSystem;
 import edu.illinois.cs.dfs.RemoteFileSystemAdapter;
 import edu.illinois.cs.mapreduce.Job.JobID;
 import edu.illinois.cs.mapreduce.Task.TaskID;
 import edu.illinois.cs.mapreduce.api.InputFormat;
 import edu.illinois.cs.mapreduce.api.Partition;
 import edu.illinois.cs.mapreduce.api.Partitioner;
 
 /**
  * TODO recover from common failures TODO synchronize properly
  */
 public class JobManager implements RemoteJobManager {
 
     private static final NumberFormat NF = NumberFormat.getInstance();
     static {
         NF.setMinimumIntegerDigits(5);
         NF.setGroupingUsed(false);
     }
 
     private final ID nodeId;
     private final ID[] nodeIds;
     private final Map<ID, TaskManager> taskManagers;
     private final Map<ID, FileSystem> fileSystems;
     private final AtomicInteger counter; // TODO persist and resume
     private final Map<JobID, Job> jobs;
 
     JobManager(ID id, Map<ID, TaskManager> taskManagers, Map<ID, FileSystem> fileSystems) {
         this.nodeId = id;
         this.nodeIds = taskManagers.keySet().toArray(new ID[0]);
         this.taskManagers = taskManagers;
         this.fileSystems = fileSystems;
         this.counter = new AtomicInteger(0);
         this.jobs = new ConcurrentHashMap<JobID, Job>();
     }
 
     @Override
     public JobID submitJob(File jarFile, File inputFile) throws IOException {
         // 1. create job
         JobID jobId = new JobID(nodeId, counter.incrementAndGet());
         Path jar = new Path(jobId).append(jarFile.getName());
         Job job = new Job(jobId, jar);
         jobs.put(jobId, job);
         // 2. submit tasks
         submitTasks(job, jarFile, inputFile);
         return jobId;
     }
 
     private void submitTasks(Job job, File jarFile, File inputFile) throws IOException {
         Path inputDir = job.getPath().append("input");
         Path outputDir = job.getPath().append("output");
         JobDescriptor descriptor = JobDescriptor.read(jarFile);
         ClassLoader cl = new URLClassLoader(new URL[] {jarFile.toURI().toURL()});
         InputFormat<?, ?, ?> inputFormat;
         try {
             inputFormat = (InputFormat<?, ?, ?>)cl.loadClass(descriptor.getInputFormatClass()).newInstance();
         } catch (Exception e) {
             throw new IOException(e);
         }
 
         InputStream is = new FileInputStream(inputFile);
         try {
             Partitioner partitioner = inputFormat.createPartitioner(is, descriptor.getProperties());
             Set<ID> nodesWithJar = new HashSet<ID>();
             int num = 0;
             while (!partitioner.isEOF()) {
                 // 1. chose task manager
                 // current selection policy: robin.
                 // TODO: try capacity-based policy here
                 ID nodeId = nodeIds[num % nodeIds.length];
 
                 // 2. initialize partition paths
                 String partitionPath = "partition-" + NF.format(num);
                 Path inputPath = inputDir.append(partitionPath);
                 Path outputPath = outputDir.append(partitionPath);
 
                 // 3. write partition to task manager's file system
                 FileSystem fs = fileSystems.get(nodeId);
                 OutputStream os = fs.write(inputPath);
                 Partition partition;
                 try {
                     partition = partitioner.writePartition(os);
                 } finally {
                     os.close();
                 }
 
                 // 4. write job file if not already written
                 if (!nodesWithJar.contains(nodeId)) {
                     fs.copy(job.getJar(), jarFile);
                     nodesWithJar.add(nodeId);
                 }
 
                 // 5. create task
                 TaskID taskId = new TaskID(job.getId(), num);
                 Task task = new Task(taskId, partition, inputPath, outputPath, job.getJar(), descriptor);
                 job.getTasks().add(task);
 
                 // 6. submit task
                 TaskManager taskManager = taskManagers.get(nodeId);
                 taskManager.submitTask(task);
 
                 num++;
             }
         } finally {
             is.close();
         }
     }
 
     public static void main(String[] args) throws AlreadyBoundException, IOException {
         // TODO better command parsing
         Configuration config;
         if (args.length > 1 && args[0].equals("-config"))
             config = Configuration.load(new File(args[1]));
         else
             config = Configuration.load();
 
         // get registry
         String registryHost = config.getRmiRegistryHost();
         int registryPort = config.getRmiRegistryPort();
         Registry registry = LocateRegistry.getRegistry(registryHost, registryPort);
 
         // node ID
         ID id = new ID(config.getId());
        ID peerId = new ID(config.getPeerId());
 
         // file systems
         File localDir = config.getLocalDir();
         FileUtil.ensureDirExists(localDir);
         FileSystem localFS = new LocalFileSystem(new File(localDir, id.toString()));
        FileSystem remoteFS = new FileSystemAdapter(new LazyRemoteFileSystem(registry, "/" + peerId + "/file-system"));
         Map<ID, FileSystem> fileSystems = new HashMap<ID, FileSystem>();
         fileSystems.put(id, localFS);
        fileSystems.put(peerId, remoteFS);
 
         // task managers
        TaskManager localTM = new LocalTaskManager(localFS);
        TaskManager remoteTM = new LazyRemoteTaskManager(registry, "/" + peerId + "/task-manager");
         Map<ID, TaskManager> taskManagers = new HashMap<ID, TaskManager>();
        taskManagers.put(id, localTM);
        taskManagers.put(peerId, remoteTM);
 
         RemoteJobManager manager = new JobManager(peerId, taskManagers, fileSystems);
 
         int port = config.getRmiPort();
        rebind(registry, "/" + id + "/file-system", new RemoteFileSystemAdapter(localFS), port);
        rebind(registry, "/" + id + "/task-manager", localTM, ++port);
        rebind(registry, "/" + id + "/job-manager", manager, ++port);
 
        System.out.println("job manager "+id+" started");
     }
 
     private static void rebind(Registry registry, String name, Remote obj, int port) throws IOException {
         Remote remote = UnicastRemoteObject.exportObject(obj, port);
         registry.rebind(name, remote);
     }
 
     static class LazyRemoteFileSystem implements RemoteFileSystem {
 
         private final Registry registry;
         private final String name;
         private RemoteFileSystem delegate;
 
         public LazyRemoteFileSystem(Registry registry, String name) {
             this.registry = registry;
             this.name = name;
         }
 
         @Override
         public RemoteInputStream read(Path path) throws RemoteException, IOException {
             return getDelegate().read(path);
         }
 
         @Override
         public RemoteOutputStream write(Path path) throws RemoteException, IOException {
             return getDelegate().write(path);
         }
 
         @Override
         public void copy(Path dest, RemoteInputStream src) throws RemoteException, IOException {
             getDelegate().copy(dest, src);
         }
 
         public RemoteFileSystem getDelegate() {
             if (delegate == null)
                 try {
                     delegate = (RemoteFileSystem)registry.lookup(name);
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
             return delegate;
         }
     }
 
     static class LazyRemoteTaskManager implements TaskManager {
         private final Registry registry;
         private final String name;
         private TaskManager delegate;
 
         public LazyRemoteTaskManager(Registry registry, String name) {
             this.registry = registry;
             this.name = name;
         }
 
         @Override
         public void submitTask(Task task) throws RemoteException {
             getDelegate().submitTask(task);
         }
 
         public TaskManager getDelegate() {
             if (delegate == null)
                 try {
                     delegate = (TaskManager)registry.lookup(name);
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
             return delegate;
         }
     }
 }
