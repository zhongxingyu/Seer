 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package jackrabbit.app;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 
 import jackrabbit.node.NodeCopier;
 import jackrabbit.query.Querier;
 import jackrabbit.repository.RepositoryFactory;
 import jackrabbit.repository.RepositoryFactoryImpl;
 import jackrabbit.repository.RepositoryManager;
 import jackrabbit.session.SessionFactory;
 import jackrabbit.session.SessionFactoryImpl;
 
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.SimpleCredentials;
 import javax.jcr.query.RowIterator;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.jackrabbit.api.JackrabbitRepository;
 import org.apache.jackrabbit.commons.cnd.ParseException;
 
 public class App {
 	
 	protected static Log log=LogFactory.getLog(App.class);
 		
 	private static String srcRepoDir="";
 	private static String srcRepoPath="/";
 	private static String destRepoDir="";
 	private static String destRepoPath="/";
 	private static String srcConf="";
 	private static String destConf="";
 	private static String cndPath="";
 	private static String query="";
 	private static String queryType="";
 	private static String srcUser="";
 	private static String srcPasswd="";
 	private static String destUser="";
 	private static String destPasswd="";
 	private static long nodeLimit;
 	private static final String VERSION="0.1";
 	
 	
     public static void main(String[] args) {
     	if (args.length == 0 || args.length == 1 && args[0].equals("-h")) {
     		System.out.println("Usage: java -jar ackrabbit-migration-query-tool-"+VERSION+"-jar-with-dependencies.jar " + 
     				"--src src --src-conf conf [--src-repo-path path] [--src-user src_user] [--src-passwd src_pw] "+ 
     				"[--dest-user dest_user] [--dest-passwd dest_pw] [--dest dest] [--dest-conf conf] [--dest-repo-path path] " +
     				"[--cnd cnd] [--node-limit limit] " +
     				"[--query-type type] [--query query]");
     		System.out.println("\t --src source repository directory");
     		System.out.println("\t --src-conf source repository configuration file");
     		System.out.println("\t --src-repo-path path to source node to copy from; default is \"/\"");
     		System.out.println("\t --src-user source repository login");
    		System.out.println("\t --src-password source repository password");
     		System.out.println("\t --dest destination repository directory");    		
     		System.out.println("\t --dest-conf destination repository configuration file");    		
     		System.out.println("\t --dest-repo-path path to destination node to copy to; default is \"/\"");
     		System.out.println("\t --dest-user destination repository login");
    		System.out.println("\t --dest-password destination repository password");
     		System.out.println("\t --node-limit size to partition nodes with before copying. If it is not supplied, no partitioning is performed");
     		System.out.println("\t --cnd common node type definition file");
     		System.out.println("\t --query query to run in src. If --query is specified, then --dest, --dest-conf, --dest-repo-path and --cnd will be ignored.");
     		System.out.println("\t --query-type query type (sql, xpath, JCR-SQL2); default is JCR-SQL2");
     		return;
     	}
     	for (int i=0;i<args.length;i=i+2) {
     		if (args[i].equals("--src") && i+1<args.length) {
     			srcRepoDir=args[i+1];
     		} else if (args[i].equals("--dest") && i+1<args.length) {
     			destRepoDir=args[i+1];
     		} else if (args[i].equals("--src-conf") && i+1<args.length) {
     			srcConf=args[i+1];
     		} else if (args[i].equals("--dest-conf") && i+1<args.length) {
     			destConf=args[i+1];
     		} else if (args[i].equals("--src-repo-path") && i+1<args.length) {
     			srcRepoPath=args[i+1];
     		} else if (args[i].equals("--dest-repo-path") && i+1<args.length) {
     			destRepoPath=args[i+1];
     		} else if (args[i].equals("--src-user") && i+1<args.length) {
     			srcUser=args[i+1];
     		} else if (args[i].equals("--src-passwd") && i+1<args.length) {
     			srcPasswd=args[i+1];
     		} else if (args[i].equals("--dest-user") && i+1<args.length) {
     			destUser=args[i+1];
     		} else if (args[i].equals("--dest-passwd") && i+1<args.length) {
     			destPasswd=args[i+1];
     		} else if (args[i].equals("--node-limit") && i+1<args.length) {
     			nodeLimit=Long.parseLong(args[i+1]);
     		} else if (args[i].equals("--cnd") && i+1<args.length) {
     			cndPath=args[i+1];
     		}  else if (args[i].equals("--query") && i+1<args.length) {
     			query=args[i+1];
     		} else if (args[i].equals("--query-type") && i+1<args.length) {
     			queryType=args[i+1];
     		}
     	}
     	boolean missingArgs=false;
     	
     	if (srcRepoDir.isEmpty()) {
     		missingArgs=true;
     		log.error("Please specify the --src option.");
     	}
     	if (destRepoDir.isEmpty() && !destConf.isEmpty()) {
     		missingArgs=true;
     		log.error("Please specify the --dest option.");
     	}
     	if (srcConf.isEmpty()) {
     		missingArgs=true;
     		log.error("Please specify the --src-conf option.");
     	}
     	if (destConf.isEmpty() && !destRepoDir.isEmpty()) {
     		missingArgs=true;
     		log.error("Please specify the --dest-conf option.");
     	}
     	
     	if (missingArgs) return;
        	    	
     	SimpleCredentials credentials=new SimpleCredentials(srcUser, srcPasswd.toCharArray());
     	SimpleCredentials destCredentials=new SimpleCredentials(destUser, destPasswd.toCharArray());
     	
     	JackrabbitRepository dest=null;
     	RepositoryFactory destRf=null;    	
 		RepositoryFactory srcRf=new RepositoryFactoryImpl(srcConf, srcRepoDir);
 		if (!destConf.isEmpty()) {
 			destRf=new RepositoryFactoryImpl(destConf, destRepoDir);
 		}
     	
 		
     	try {
     		final JackrabbitRepository src=srcRf.getRepository();
 	    	SessionFactory srcSf=new SessionFactoryImpl(src, credentials);
 	    	final Session srcSession=srcSf.getSession();	  
 	    	Runtime.getRuntime().addShutdownHook(new Thread() {
     			public void run() {
     			    srcSession.logout();
     	    		src.shutdown();
     			}
     		});
 	    	if (destConf.isEmpty()) {//query mode
 	    		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
 	    		if (query.isEmpty()) {	    			
 	    			while (true) {
 	    				System.out.print(">");
 		    			String line=in.readLine();
 		    			if (line==null || line.isEmpty() || line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
 		    				break;
 		    			}
 		    			try {
 		    				runQuery(srcSession, line, queryType);
 		    			} catch (RepositoryException e) {
 		    				log.error(e.getMessage(), e);
 		    			}
 		    		}
     			} else {
     				try {
     					runQuery(srcSession, query, queryType);		
     				} catch (RepositoryException e) {
 	    				log.error(e.getMessage(), e);
 	    			}
     			}
 	    		return;
 	    	}
 	    	
 	    	dest=destRf.getRepository();
 	    	SessionFactory destSf=new SessionFactoryImpl(dest, destCredentials); 
 	    	Session destSession=destSf.getSession();
 	    	
 	    	try {
 	    		RepositoryManager.registerCustomNodeTypes(destSession, cndPath);
 	    		if (nodeLimit == 0)
 	    			NodeCopier.copy(srcSession, destSession, srcRepoPath, destRepoPath, true);
 	    		else
 	    			NodeCopier.copy(srcSession, destSession, srcRepoPath, destRepoPath, nodeLimit, true);
 				log.info("Copying "+srcSession.getWorkspace().getName()+" to "+destSession.getWorkspace().getName()+ " for "+srcRepoDir + " done.");
 	    	} catch (ParseException e) {
 				log.error(e.getMessage(), e);
 			} catch (IOException e) {
 				log.error(e.getMessage(), e);
 			} catch (PathNotFoundException e) {
 				log.error(e.getMessage(), e);
 			} catch (RepositoryException e) {
 				log.error(e.getMessage(), e);
 			} 
 	
 	    	List<String> destWkspaces=RepositoryManager.getDestinationWorkspaces(srcSession, destSession);
 	    	
 	    	for (String workspace:destWkspaces) {
 	    		Session wsSession=srcSf.getSession(workspace);
 	    		Session wsDestSession=destSf.getSession(workspace);
 	    		try {
 	    			RepositoryManager.registerCustomNodeTypes(wsDestSession, cndPath);
 	    			if (nodeLimit == 0)
 	    				NodeCopier.copy(wsSession, wsDestSession, srcRepoPath, destRepoPath, true);
 	    			else {
 	    				NodeCopier.copy(wsSession, wsDestSession, srcRepoPath, destRepoPath, nodeLimit, true);
 	    			}	    				
 	    			log.info("Copying "+wsSession.getWorkspace().getName()+" to "+wsDestSession.getWorkspace().getName()+ " for " + srcRepoDir + " done.");
 	    		} catch (IOException e) {
 	    			log.error(e.getMessage(), e);
 	    		} catch (ParseException e) {
 	    			log.error(e.getMessage(), e);
 	    		} catch (PathNotFoundException e) {
 	    			log.error(e.getMessage(), e);
 	    		} catch (RepositoryException e) {
 	    			log.error(e.getMessage(), e);
 	    		} finally {
 	    			wsSession.logout();
 	    			wsDestSession.logout();
 	    		}
 	    	}
     	} catch (IOException e) {
     		log.error(e.getMessage(), e);
 		} catch (PathNotFoundException e) {
 			log.error(e.getMessage(), e);
 		} catch (RepositoryException e) {
 			log.error(e.getMessage(), e);
 		} finally {
 			if (dest!=null) dest.shutdown();
 		}
     }
     
     private static void runQuery(Session srcSession, String query, String queryType) throws RepositoryException {
     	long start=System.currentTimeMillis();
 		RowIterator rowIt=Querier.queryBySQLRow(srcSession, query, queryType);
 		System.out.println(Querier.formatQueryResults(rowIt));
 		System.out.println("Time: "+String.valueOf(System.currentTimeMillis()-start) +" milliseeconds");
     }
 }
