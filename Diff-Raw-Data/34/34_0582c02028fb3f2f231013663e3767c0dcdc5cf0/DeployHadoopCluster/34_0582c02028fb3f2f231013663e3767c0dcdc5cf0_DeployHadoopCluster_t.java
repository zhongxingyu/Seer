 package cn.edu.sjtu.se.dslab.haiercloud.deploy;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.springframework.scheduling.annotation.Async;
 import org.w3c.dom.Document;
 
 //部署一个hadoop集群
 public class DeployHadoopCluster implements DeployCluster {
 
 	private String shellPath = "";// 固定路径!!!!这里是测试路径
 	// private String jdkPath=""; //固定路径!!!!
 	private String jdk32Path = "";
 	private String jdk64Path = "";
 	private final int jdk32bits = 32;
 	private final int jdk64bits = 64;
 	private String hadoopPath = ""; // 固定路径!!!!
 	private String hostsPath = ""; // 目录与hadoop名相关
 	private String nodesPath = ""; // 目标节点目录
 	private String hadoopClusterLogPath = "";
 	private String idRsaPubFilePath = "";
 	private String userName = "";// 安装hadoop的用户名
 	private int nThreads = 5;
 	private String tmpPath = "";
 	private int hdfsPort = 9000;
 	private int mapredPort = 9001;
 	private int dfsReplication = 1;
 	private String masterIP = null;
 	private String masterPassword = null;
 	private String secondaryNameNodeIP = null;
 	private String secondaryNameNodePassword = null;
 	private String clusterName;
 	private Map<String, String> nodesMap = null;
 	private Set<String> nodesSet = null;
 	private Iterator<String> iterator = null;
 	private FileWriter logWriter = null;
 
 	private void output(String str) {
 		try {
 			logWriter.write(str + "\n");
 			logWriter.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void readBuffer(final InputStream input, final InputStream error) {
 		new Thread() {
 
 			public void run() {
 				BufferedReader inputReader = new BufferedReader(
 						new InputStreamReader(input));
 				try {
 					while (inputReader.readLine() != null)
 						;
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} finally {
 					try {
 						input.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}.start();
 
 		new Thread() {
 			public void run() {
 				BufferedReader errorReader = new BufferedReader(
 						new InputStreamReader(error));
 				try {
 					while (errorReader.readLine() != null)
 						;
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} finally {
 					try {
 						error.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}.start();
 	}
 
 	// 在master节点上生成密钥对
 	private void installSSHtoMaster() {
 		// 先对master安装ssh
 		output("Creat rsa on the master machine:");
 		try {
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/installSSH-master.sh" + " " + masterIP + " "
 							+ masterPassword);
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			// 对Master安装ssh异常进行处理：
 			e.printStackTrace();
 		}
 	}
 
 	// 将master节点的公钥拷贝至各slave节点
 	private void installSSHtoSlave(String nodeIP, String nodePassword) {
 		// 对slave安装ssh
 		try {
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/installSSH-slave.sh" + " " + masterIP + " "
 							+ " " + masterPassword + " " + tmpPath + " "
 							+ nodeIP + " " + nodePassword);
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			// 对slave安装ssh异常进行处理：
 			e.printStackTrace();
 		}
 	}
 
 	// 在集群内的所有节点上安装jdk
 	private void installJDK(String nodeIP, String nodePassword) {
 		output("Start to copy jdk files to the machine whose ip is " + nodeIP);
 		boolean flag = true;
 		try {
 			// 1、先检测机器的位数
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/getMachineBit.sh" + " " + nodeIP + " "
 							+ nodePassword);
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					p.getInputStream()));
 			String res = null;
 			String jdkPath = "";
 			while ((res = br.readLine()) != null) {
 				if (res.equals(String.valueOf(jdk32bits)))
 					jdkPath = jdk32Path;
 				else if (res.equals(String.valueOf(jdk64bits)))
 					jdkPath = jdk64Path;
 			}
 			p.waitFor();
 
 			// 2、copy文件
 			Process pp = Runtime.getRuntime().exec(
 					shellPath + "/copyDirTo.sh" + " " + nodeIP + " "
 							+ nodePassword + " " + jdkPath + " " + nodesPath);
 			br = new BufferedReader(new InputStreamReader(pp.getInputStream()));
 			while ((res = br.readLine()) != null)
 				;
 			pp.waitFor();
 		} catch (Exception e) {
 			output("Fail to copy jdk files to the machine whose ip is "
 					+ nodeIP + "!");
 			flag = false;
 		}
 		if (flag)
 			output("Success to copy jdk files to the machine whose ip is "
 					+ nodeIP + "!");
 	}
 
 	// 修改本地的hadoop配置文件
 	private void modifyConfigureFiles() {
 		output("Start to modify hadoop configure files:");
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 
 			// 对core-site.xml更改，即fs.default.name的值应改为masterIP加上相应的端口
 			File coreFile = new File(hadoopPath + "/conf/core-site.xml");
 			Document doc = builder.parse(coreFile);
 			doc.getElementsByTagName("value").item(0)
 					.setTextContent(nodesPath + "/hadooptmp");
 			// 指明fs.default.name所在
 			doc.getElementsByTagName("value").item(1)
 					.setTextContent("hdfs://" + masterIP + ":" + hdfsPort);
 			DOMSource domSource = new DOMSource(doc);
 			TransformerFactory tf = TransformerFactory.newInstance();
 			Transformer transformer = tf.newTransformer();
 			transformer.transform(domSource, new StreamResult(
 					new FileOutputStream(coreFile)));
 
 			// 对mapred-site.xml更改，即mapred.job.tracker的ip和port
 			File mapredFile = new File(hadoopPath + "/conf/mapred-site.xml");
 			doc = builder.parse(mapredFile);
 			doc.getElementsByTagName("value").item(0)
 					.setTextContent(masterIP + ":" + mapredPort);
 			domSource = new DOMSource(doc);
 			transformer.transform(domSource, new StreamResult(
 					new FileOutputStream(mapredFile)));
 
 			// 对hdfs-site.xml更改，即dfs.replication的值
 			File hdfsFile = new File(hadoopPath + "/conf/hdfs-site.xml");
 			doc = builder.parse(hdfsFile);
 			doc.getElementsByTagName("value").item(0)
 					.setTextContent(String.valueOf(dfsReplication));
 			domSource = new DOMSource(doc);
 			transformer.transform(domSource, new StreamResult(
 					new FileOutputStream(hdfsFile)));
 
 			// 对masters更改，即ＳecondaryＮameＮode,暂且不考虑部署在其它机器
 			FileWriter masterWriter = new FileWriter(hadoopPath
 					+ "/conf/masters");
 			masterWriter.write(secondaryNameNodeIP + "\n");
 			masterWriter.flush();
 			masterWriter.close();
 
 			// 对slaves更改，即所有的slave所在的机器ip
 			FileWriter slaveWriter = new FileWriter(hadoopPath + "/conf/slaves");
 			iterator = nodesSet.iterator();
 			while (iterator.hasNext()) {
 				String ip = iterator.next();
 				if (!(ip.equals(masterIP)))
 					slaveWriter.write(ip + "\n");
 			}
 			slaveWriter.flush();
 			slaveWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		output("Finish modify hadoop configure file!");
 	}
 
 	// copy　hadoop文件至各节点
 	private void copyHadoopFiles(String nodeIP, String nodePassword) {
 		// copy hadoop files to all nodes
 		boolean flag = true;
 		output("Start to copy hadoop files to the machine whose ip is "
 				+ nodeIP);
 		try {
 			Process p = Runtime
 					.getRuntime()
 					.exec(shellPath + "/copyDirTo.sh" + " " + nodeIP + " "
 							+ nodePassword + " " + hadoopPath + " " + nodesPath);
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			output("Fail to copy hadoop files to the machine whose ip is "
 					+ nodeIP);
 		}
 		if (flag)
 			output("Success to copy hadoop files to the machine whose ip is "
 					+ nodeIP);
 	}
 
 	// 在master节点上启动hadoop
 	private void startHadoop() {
 		output("Start hadoop!");
 		boolean flag = true;
 		try {
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/startHadoop.sh" + " " + masterIP + " "
 							+ masterPassword + " " + nodesPath);
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 			output("Fail to start hadoop!");
 			flag = true;
 		}
 		if (flag)
 			output("Success to start hadoop!");
 	}
 
 	// 修改hosts文件
 	private void modifyHostsFile(String nodeIP) {
 		try {
 			FileWriter hostsWriter = new FileWriter(hostsPath);
 			hostsWriter.write(nodeIP + "\r");
 			hostsWriter.flush();
 			hostsWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// 在各节点上新建dfs.tmp文件夹
 	private void makeTmpDir(String nodeIP, String nodePassword) {
 		output("Make dfs.tmp file on every node!");
 		try {
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/makeDir.sh" + " " + nodeIP + " "
 							+ nodePassword + " " + nodesPath + "/hadooptmp");
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// 拷贝hosts文件至各节点，覆盖原来的文件
 	private void copyHostsFile(String nodeIP, String nodePassword) {
 		try {
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/copyFileTo.sh" + " " + nodeIP + " "
 							+ nodePassword + " " + hostsPath + " "
 							+ "/etc/hosts");
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// 设置副本的数量
 	public void setDfsReplication(int num) {
 		dfsReplication = num;
 	}
 
 	// 初始化
 	private void init(ClusterArguments clusterArgs) {
 		/*
 		 * Properties prop=new Properties(); InputStream
 		 * in=getClass().getResourceAsStream("/paths.properties"); try { //
 		 * InputStream in=new FileInputStream(new
 		 * File("/home/hadoop/workspace/Cloud/paths.properties"));
 		 * prop.load(in); } catch (IOException e) { e.printStackTrace(); }
 		 */
 		/*shellPath = prop.getProperty("shellPath");
 		jdk32Path = prop.getProperty("jdk32Path");
 		jdk64Path = prop.getProperty("jdk64Path");
 		hadoopPath = prop.getProperty("hadoopPath");
 		nodesPath = prop.getProperty("nodesPath");
 		hadoopClusterLogPath = prop.getProperty("hadoopClusterLogPath");
 		idRsaPubFilePath = prop.getProperty("idRsaPubFilePath");
 		userName = prop.getProperty("userName");
 		nThreads = Integer.valueOf(prop.getProperty("nThreads"));
 		tmpPath = prop.getProperty("tmpPath");*/
 
 		this.masterIP = clusterArgs.getMasterIP();
 		this.masterPassword = clusterArgs.getHm().get(masterIP);
 		// 暂且将master和secondary部署在同一台机器上
 		this.secondaryNameNodeIP = this.masterIP;
 		this.secondaryNameNodePassword = this.masterPassword;
 		this.clusterName = clusterArgs.getClusterName();
 		this.nodesMap = clusterArgs.getHm();
 		this.nodesSet = clusterArgs.getHm().keySet();
 
 		String currTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
 		String logName = clusterName + "-" + currTime + ".log";
 		try {
 			logWriter = new FileWriter(new File(hadoopClusterLogPath + "/"
 					+ logName), true);
 		} catch (Exception e) {
 			e.printStackTrace();
 			output("unable to creat HadoopCluster's log file!");
 		}
 	}
 
 	// 增加hadoop集群
 	public boolean addCluster(ClusterArguments clusterArgs) {
 

 		// 初始化 １、建立日志文件
 		init(clusterArgs);
 
 		String currTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
 				.format(new Date());
 		output(currTime + " " + "Deploy Hadoop Cluster" + "\n");
 
 		// ２、修改hadoop的配置文件
 		modifyConfigureFiles();
 
 		// 3、取得master节点的id_rsa.pub文件
 		// installSSHtoMaster();
 
 		// 4、拷贝master节点的id_rsa.pub文件
 		getMasterPubRsa();
 
 		// 并行完成以下各项工作
 		// ５、将master节点的id_rsa.pub拷贝到各slave节点
 		// ６、拷贝jdk文件至各节点
 		// ７、拷贝hadoop文件至节点
 		parallelDeploy();
 
 		// 8、启动hadoop
 		startHadoop();
 
 		// 9、并行检测各节点是否正常启动
 		boolean flag = parallelCheckClusterStatus();
 
 		return flag;

 	}
 
 	private void getMasterPubRsa() {
 		try {
 			output("Copy id_rsa.pub file from master machine!");
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/copyFileFrom.sh" + " " + masterIP + " "
 							+ masterPassword + " " + "/home/" + userName
 							+ "/.ssh/id_rsa.pub" + " " + idRsaPubFilePath);
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 			output("在获取master节点的id_rsa.pub文件过程中出现错误！");
 		}
 	}
 
 	private void parallelDeploy() {
 		ExecutorService deployThreadPool = Executors
 				.newFixedThreadPool(nThreads);
 		iterator = nodesSet.iterator();
 		class Task implements Callable<Object> {
 
 			public Object call() throws Exception {
 				deployNode();
 				return null;
 			}
 		}
 		Task[] tasks = new Task[nodesSet.size()];
 		for (int i = 0; i < nodesSet.size(); i++) {
 			tasks[i] = new Task();
 		}
 		try {
 			deployThreadPool.invokeAll(Arrays.asList(tasks));
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		deployThreadPool.shutdown();
 		if (deployThreadPool.isTerminated())
 			output("Deploy successfully!");
 	}
 
 	// 部署集群的几个重要步骤，用于并行执行的进程
 	private void deployNode() {
 		while (iterator.hasNext()) {
 			String nodeIP = iterator.next();
 			String nodePassword = nodesMap.get(nodeIP);
 			// 4、集群内机器实现ssh无密码登录
 			output("Make master login the node whose ip is " + nodeIP
 					+ " without password!");
 			/*
 			 * if(!nodeIP.equals(masterIP)){
 			 * installSSHtoSlave(nodeIP,nodePassword); }
 			 */
 
 			// 5、拷贝jdk到各节点
 			installJDK(nodeIP, nodePassword);
 
 			// 6、拷贝hadoop到各节点
 			copyHadoopFiles(nodeIP, nodePassword);
 
 			// 7、建立tmp文件夹
 			makeTmpDir(nodeIP, nodePassword);
 		}
 	}
 
 	private boolean parallelCheckClusterStatus() {
 		boolean flag = true;
 		ExecutorService checkThreadPool = Executors
 				.newFixedThreadPool(nThreads);
 		class Task implements Callable<Boolean> {
 			public Boolean call() throws Exception {
 				return checkNodesStatus();
 			}
 		}
 		Task[] tasks = new Task[nodesSet.size()];
 		for (int i = 0; i < nodesSet.size(); i++) {
 			tasks[i] = new Task();
 		}
 		try {
 			iterator = nodesSet.iterator();
 			List<Future<Boolean>> futures = checkThreadPool.invokeAll(Arrays
 					.asList(tasks));
 			Iterator<Future<Boolean>> futuresIterator = futures.iterator();
 			while (futuresIterator.hasNext())
 				if (futuresIterator.next().get().booleanValue() != true) {
 					flag = false;
 					break;
 				}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		checkThreadPool.shutdown();
 		checkThreadPool.isTerminated();
 		return flag;
 	}
 
 	private boolean checkNodesStatus() {
 		boolean flag = false;
 		while (iterator.hasNext()) {
 			String nodeIP = iterator.next();
 			String nodePassword = nodesMap.get(nodeIP);
 			ArrayList<String> nodeNames = new ArrayList<String>();
 			if (nodeIP.equals(masterIP)) {
 				nodeNames.add("NameNode");
 				nodeNames.add("SecondaryNameNode");
 				nodeNames.add("JobTracker");
 			} else {
 				nodeNames.add("DataNode");
 				nodeNames.add("TaskTracker");
 			}
 
 			int ct = 0;
 			try {
 				Process p = Runtime.getRuntime().exec(
 						shellPath + "/check.sh" + " " + nodeIP + " "
 								+ nodePassword + " " + nodesPath);
 				BufferedReader br = new BufferedReader(new InputStreamReader(
 						p.getInputStream()));
 				String line;
 				while ((line = br.readLine()) != null) {
 					for (int i = 0; i < nodeNames.size(); i++)
 						if (line.endsWith(" " + nodeNames.get(i))) {
 							ct++;
 							output(nodeNames.get(i) + " on the " + nodeIP
 									+ " is running!");
 						}
 				}
 				br.close();
 				p.waitFor();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			if (ct == nodeNames.size())
 				flag = true;
 			else
 				flag = false;
 			break;
 		}
 		return flag;
 	}
 
 	private void addSlavesFile(String nodeIP) {
 		// 首先从master获取slaves文件，然后将nodeIP加入到slaves末尾
 		try {
 			FileWriter slavesWriter = new FileWriter(hadoopPath
 					+ "/conf/slaves", true);
 			slavesWriter.write(nodeIP + "\n");
 			slavesWriter.flush();
 			slavesWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// 将修改后的slaves文件拷贝到集群的mater节点
 		try {
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/copyFileTo.sh" + " " + masterIP + " "
 							+ masterPassword + " "
 							+ (hadoopPath + "/conf/slaves") + " " + nodesPath
 							+ "/hadoop-1.2.0/conf/");
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// 启动某一节点服务
 
 	private void startNode() {
 		while (iterator.hasNext()) {
 			String nodeIP = iterator.next();
 			String nodePassword = nodesMap.get(nodeIP);
 			output("Start node whose ip is " + nodeIP);
 			try {
 				Process p = Runtime.getRuntime().exec(
 						shellPath + "/startNode.sh" + " " + nodeIP + " "
 								+ nodePassword + " " + nodesPath);
 				readBuffer(p.getInputStream(), p.getErrorStream());
 				p.waitFor();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void parallelStartNodes() {
 		ExecutorService startThreadPool = Executors
 				.newFixedThreadPool(nThreads);
 		iterator = nodesSet.iterator();
 		class Task implements Callable<Object> {
 
 			public Object call() throws Exception {
 				startNode();
 				return null;
 			}
 		}
 		Task[] tasks = new Task[nodesSet.size()];
 		for (int i = 0; i < nodesSet.size(); i++) {
 			tasks[i] = new Task();
 		}
 		try {
 			startThreadPool.invokeAll(Arrays.asList(tasks));
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		startThreadPool.shutdown();
 	}
 
 	// 均衡处理
 	private void balance(String nodeIP, String nodePassword) {
 		try {
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/balance.sh" + " " + nodeIP + " "
 							+ nodePassword + " " + nodesPath);
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// 增加节点
 	public void addNode(ClusterArguments clusterArgs) {
 
 		// １、初始化
 		init(clusterArgs);
 		nodesMap.remove(masterIP);
 		nodesSet = nodesMap.keySet();
 
 		String currTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
 				.format(new Date());
 		output(currTime + " " + "Add slave nodes" + "\n");
 
 		// ２、先从master节点获得slaves文件，再修改slaves文件
 		// ３、拷贝slaves文件至master节点
 		modifySlavesFile();
 
 		// ４、并行拷贝id_rsa.pub、jdk、hadoop等文件至各节点
 		parallelDeploy();
 
 		// ５、master节点执行refresh操作
 		refresh();
 
 		// ６、启动各节点
 		parallelStartNodes();
 
 		output("Finished!" + "\n");
 		try {
 			logWriter.flush();
 			logWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// ７、均衡处理（待处理！！！！！！！）
 	}
 
 	private void modifySlavesFile() {
 		// 首先从master获取slaves文件，然后将nodeIP加入到slaves末尾
 		output("Get slaves file from master node,modify it and copy to master node!");
 		Process p;
 		try {
 			p = Runtime.getRuntime().exec(
 					shellPath + "/copyFileFrom.sh" + " " + masterIP + " "
 							+ masterPassword + " "
 							+ (nodesPath + "/hadoop-1.2.0/conf/slaves") + " "
 							+ tmpPath);
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		iterator = nodesSet.iterator();
 		try {
 			FileWriter slavesWriter = new FileWriter(tmpPath + "/slaves", true);
 			while (iterator.hasNext()) {
 				slavesWriter.write(iterator.next() + "\n");
 			}
 			slavesWriter.flush();
 			slavesWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// 将修改后的slaves文件拷贝到集群的mater节点
 		try {
 			Process pp = Runtime.getRuntime().exec(
 					shellPath + "/copyFileTo.sh" + " " + masterIP + " "
 							+ masterPassword + " " + (tmpPath + "/slaves")
 							+ " " + nodesPath + "/hadoop-1.2.0/conf/");
 			readBuffer(pp.getInputStream(), pp.getErrorStream());
 			pp.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void refresh() {
 		output("Refresh master node!");
 		try {
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/refreshNameNode.sh" + " " + masterIP + " "
 							+ masterPassword + " " + nodesPath);
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// 从hadoop集群中删除一个节点
 	public void deleteNode(ClusterArguments clusterArgs) {
 		init(clusterArgs);
 		nodesMap.remove(masterIP);
 		nodesSet = nodesMap.keySet();
 
 		String currTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
 				.format(new Date());
 		output(currTime + " " + "Delete slave nodes" + "\n");
 
 		// １、新建excludedNodes文件，文件中指定需要删除的节点，一个节点一行
 		createExcludedNodesFile();
 
 		// ２、将excludededNodes文件拷贝到master节点
 		copyExcludedNodesToMaster();
 
 		// 3、先从master节点得到slaves文件，修改后再拷贝到master节点
 		deleteSlavesFile();
 
 		// 4、在master节点上refresh
 		refresh();
 
 		// 5、停止节点退出集群
 		parallelStopNodes();
 
 		// 6、删除master节点上的excludedNodes文件
 		deleteExcludedNodesFile();
 
 		// 7、clear所有数据
 		parallelClearMachine();
 
 		output("Finished!" + "\n");
 		try {
 			logWriter.flush();
 			logWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void deleteSlavesFile() {
 		try {
 			output("Get slaves file from master node!");
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/copyFileFrom.sh" + " " + masterIP + " "
 							+ masterPassword + " " + nodesPath
 							+ "/hadoop-1.2.0/conf/slaves" + " " + tmpPath);
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 
 			BufferedReader br = new BufferedReader(new FileReader(new File(
 					tmpPath + "/slaves")));
 			ArrayList<String> slaveList = new ArrayList<String>();
 			String ip;
 			while ((ip = br.readLine()) != null) {
 				if (!nodesSet.contains(ip))
 					slaveList.add(ip);
 			}
 			br.close();
 
 			output("Modify slaves file!");
 			FileWriter slavesWriter = new FileWriter(new File(tmpPath
 					+ "/slaves"));
 			int num = slaveList.size();
 			for (int i = 0; i < num; i++) {
 				slavesWriter.write(slaveList.get(i) + "\n");
 			}
 			slavesWriter.flush();
 			slavesWriter.close();
 
 			Process pp = Runtime.getRuntime().exec(
 					shellPath + "/copyFileTo.sh" + " " + masterIP + " "
 							+ masterPassword + " " + (tmpPath + "/slaves")
 							+ " " + nodesPath + "/hadoop-1.2.0/conf/");
 			readBuffer(pp.getInputStream(), pp.getErrorStream());
 			pp.waitFor();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void deleteExcludedNodesFile() {
 		try {
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/deleteFile.sh" + " " + masterIP + " "
 							+ masterPassword + " " + nodesPath
 							+ "/hadoop-1.2.0/conf/excludedNodes");
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void parallelStopNodes() {
 		output("Stop nodes you want!");
 		ExecutorService stopThreadPool = Executors.newFixedThreadPool(nThreads);
 		class Task implements Callable<Object> {
 
 			public Object call() throws Exception {
 				stopNode();
 				return null;
 			}
 		}
 		Task[] tasks = new Task[nodesSet.size()];
 		for (int i = 0; i < nodesSet.size(); i++) {
 			tasks[i] = new Task();
 		}
 		try {
 			iterator = nodesSet.iterator();
 			stopThreadPool.invokeAll(Arrays.asList(tasks));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		stopThreadPool.shutdown();
 	}
 
 	private void stopNode() {
 		try {
 			while (iterator.hasNext()) {
 				String nodeIP = iterator.next();
 				String nodePassword = nodesMap.get(nodeIP);
 				Process p = Runtime.getRuntime().exec(
 						shellPath + "/stopNode.sh" + " " + nodeIP + " "
 								+ nodePassword + " " + nodesPath);
 				readBuffer(p.getInputStream(), p.getErrorStream());
 				p.waitFor();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void copyExcludedNodesToMaster() {
 		try {
 			// 将exculdedNodes拷贝到namenode
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/copyFileTo.sh" + " " + masterIP + " "
 							+ masterPassword + " "
 							+ (tmpPath + "/excludedNodes") + " " + nodesPath
 							+ "/hadoop-1.2.0/conf/");
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void createExcludedNodesFile() {
 		output("Wirte the excluded nodes in the file!");
 		try {
 			iterator = nodesSet.iterator();
 			FileWriter excludedNodesWriter = new FileWriter(tmpPath
 					+ "/excludedNodes");
 			while (iterator.hasNext()) {
 				excludedNodesWriter.write(iterator.next() + "\n");
 			}
 			excludedNodesWriter.flush();
 			excludedNodesWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void clearMachine() {
 		try {
 			while (iterator.hasNext()) {
 				String nodeIP = iterator.next();
 				String nodePassword = nodesMap.get(nodeIP);
 				Process p = Runtime.getRuntime().exec(
 						shellPath + "/clearMachine.sh" + " " + nodeIP + " "
 								+ nodePassword + " " + nodesPath);
 				readBuffer(p.getInputStream(), p.getErrorStream());
 				p.waitFor();
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void parallelClearMachine() {
 		output("Clear all machines in the cluster!");
 		ExecutorService clearThreadPool = Executors
 				.newFixedThreadPool(nThreads);
 		class Task implements Callable<Object> {
 
 			public Object call() throws Exception {
 				clearMachine();
 				return null;
 			}
 		}
 		Task[] tasks = new Task[nodesSet.size()];
 		for (int i = 0; i < nodesSet.size(); i++) {
 			tasks[i] = new Task();
 		}
 		try {
 			iterator = nodesSet.iterator();
 			clearThreadPool.invokeAll(Arrays.asList(tasks));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		clearThreadPool.shutdown();
 		clearThreadPool.isTerminated();
 	}
 
 	// 删除一个集群
 	public void deleteCluster(ClusterArguments clusterArgs) {
 		init(clusterArgs);
 
 		String currTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
 				.format(new Date());
 		output(currTime + " " + "Delete Cluster" + "\n");
 
 		try {
 			output("Stop the cluster first!");
 			Process p = Runtime.getRuntime().exec(
 					shellPath + "/stopCluster.sh" + " " + masterIP + " "
 							+ masterPassword + " " + nodesPath);
 			readBuffer(p.getInputStream(), p.getErrorStream());
 			p.waitFor();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		parallelClearMachine();
 
 		output("Finished!" + "\n");
 		try {
 			logWriter.flush();
 			logWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getShellPath() {
 		return shellPath;
 	}
 
 	public void setShellPath(String shellPath) {
 		this.shellPath = shellPath;
 	}
 
 	public String getJdk32Path() {
 		return jdk32Path;
 	}
 
 	public void setJdk32Path(String jdk32Path) {
 		this.jdk32Path = jdk32Path;
 	}
 
 	public String getJdk64Path() {
 		return jdk64Path;
 	}
 
 	public void setJdk64Path(String jdk64Path) {
 		this.jdk64Path = jdk64Path;
 	}
 
 	public String getHadoopPath() {
 		return hadoopPath;
 	}
 
 	public void setHadoopPath(String hadoopPath) {
 		this.hadoopPath = hadoopPath;
 	}
 
 	public String getNodesPath() {
 		return nodesPath;
 	}
 
 	public void setNodesPath(String nodesPath) {
 		this.nodesPath = nodesPath;
 	}
 
 	public String getHadoopClusterLogPath() {
 		return hadoopClusterLogPath;
 	}
 
 	public void setHadoopClusterLogPath(String hadoopClusterLogPath) {
 		this.hadoopClusterLogPath = hadoopClusterLogPath;
 	}
 
 	public String getIdRsaPubFilePath() {
 		return idRsaPubFilePath;
 	}
 
 	public void setIdRsaPubFilePath(String idRsaPubFilePath) {
 		this.idRsaPubFilePath = idRsaPubFilePath;
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 
 	public int getnThreads() {
 		return nThreads;
 	}
 
 	public void setnThreads(int nThreads) {
 		this.nThreads = nThreads;
 	}
 
 	public String getTmpPath() {
 		return tmpPath;
 	}
 
 	public void setTmpPath(String tmpPath) {
 		this.tmpPath = tmpPath;
 	}
 }
