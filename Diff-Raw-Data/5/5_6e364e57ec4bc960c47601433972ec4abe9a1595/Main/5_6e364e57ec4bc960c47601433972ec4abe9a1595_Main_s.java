 package tng;
 
 import java.io.File;
 import java.util.List;
 
 import models.Method;
 import models.Pair;
 
 import git.GitController;
 import db.DatabaseConnector;
 import ast.CallGraphGenerator;
 import ast.FolderTraversar;
 
 
 public class Main
 {
 	public static void main(String[] args) {
 		System.out.println("TNG developed by eggnet.");
 		System.out.println();
 		try {
 			if (args.length < 4 ) {
 				System.out.println("Retry: TNG [dbname] [repository] [branch] [configFile] <commitFile> <port>");
 				throw new ArrayIndexOutOfBoundsException();
 			}
 			else {
 				try  {
 					// Set up the resources
 					Resources.dbName = args[0];
 					Resources.repository = args[1];
 					Resources.branch = args[2];
 					Resources.configFile = args[3];
 					setRepositoryName(args[1]);
 					
 					// Set up custom port number
					if(args.length == 6) {
 						Resources.dbPort = args[5];
 					}
 					Resources.dbUrl = Resources.dbUrl + Resources.dbPort + "/";
 					System.out.println(Resources.dbUrl);
 					
 					DatabaseConnector db = new DatabaseConnector();
 					db.connect("eggnet");
 					db.createDatabase(Resources.dbName);
 					
 					NetworkBuilder nb = new NetworkBuilder(db);
 					
					if(args.length == 5)
 						nb.buildNetworksFromCommitFile(args[4]);
 					else
 						nb.buildAllNetworksNoUpdate();
 					
 					db.close();
 				} 
 				catch (Exception e) {
 					e.printStackTrace();
 				} 
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void setRepositoryName(String path) {
 		Resources.repositoryName = path.substring(path.lastIndexOf("/")+1);
 	}
 }
