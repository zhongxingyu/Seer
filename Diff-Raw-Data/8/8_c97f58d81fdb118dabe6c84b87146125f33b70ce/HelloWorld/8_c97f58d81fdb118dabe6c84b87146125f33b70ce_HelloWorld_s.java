 package org.wyona.yarep.examples;
 
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.NodeType;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.RepositoryFactory;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class HelloWorld {
 
     private static Category log = Category.getInstance(HelloWorld.class);
 
     /**
      *
      */
     public static void main(String[] args) {
 
         /* NOTE: When removing these comments, then also switch the configuration within src/test/java/yarep.properties
         testRepoNewVFSFulltextSearch();
         testRepoNewVFSPropertySearch();
         if (true) return;
         */
 
         RepositoryFactory repoFactory = getRepositoryFactory();
         System.out.println(repoFactory);
 
         Repository repoA;
         Repository repoC;
         Repository repoB;
         Repository repoD;
         Repository repoJCR;
         Repository repoNewFS;
         try {
             repoA = repoFactory.newRepository("example1");
             repoC = repoFactory.newRepository("hugo");
             repoJCR = repoFactory.newRepository("jcr");
             repoNewFS = repoFactory.newRepository("fs");
             //repoNewFS = repoFactory.newRepository("new-vfs");
     
             // Add more repositories to repository factory
             repoB = repoFactory.newRepository("vanya", new File("orm-example/repository-config.xml"));
             repoD = repoFactory.newRepository("vfs-example", new File("vfs-example/repository.xml"));
         } catch (Exception e) {
             System.err.println(e);
             return;
         }
 
         System.out.println(repoFactory);
 
 
 
 
         // Test YarepUtil ...
         Path path;
         try {
             path = new Path("/example2/hello.txt");
             org.wyona.yarep.util.RepoPath rp = new org.wyona.yarep.util.YarepUtil().getRepositoryPath(path, repoFactory);
             System.out.println("YarepUtil: " + rp.getRepo() + " " + rp.getPath());
             path = new Path("/pele/hello/");
             rp = new org.wyona.yarep.util.YarepUtil().getRepositoryPath(path, repoFactory);
             System.out.println("YarepUtil: " + rp.getRepo() + " " + rp.getPath());
         } catch (RepositoryException e) {
             System.err.println(e);
             return;
         }
 
         Path worldPath = new Path("/hello/world.txt");
 
         // Write content to repository
         try {
             System.out.println("\nWrite content to repository " + repoA.getName() + " (repoA) ...");
             Writer writerA = repoA.getWriter(worldPath);
             writerA.write("Hello World!\n...");
             writerA.close();
 
             repoA.addSymbolicLink(worldPath, new Path("/hello-world-link.txt"));
 
             System.out.println("\nRead and Write content from/to repository " + repoJCR.getName() + " (repoJCR) ...");
             //String jcrNodePathExample = "/profiles/michael-wechner.rdf";
             String jcrNodePathExample = "/";
 
             repoJCR.getNode(jcrNodePathExample).setProperty("my-message", "Hello Hugo!");
             System.out.println(repoJCR.getNode(jcrNodePathExample).getProperty("my-message"));
 
             repoJCR.getNode(jcrNodePathExample).setMimeType("text/xml");
             Writer writerJCR = repoJCR.getWriter(new Path(jcrNodePathExample));
             writerJCR.write("<test>Hello (hello) JCR</test>");
             writerJCR.close();
 
             if (repoJCR.existsNode(jcrNodePathExample)) {
                 Reader readerJCR = repoJCR.getReader(new Path(jcrNodePathExample));
                 BufferedReader brJCR = new BufferedReader(readerJCR);
                 System.out.println("Very first line of JCR node content: " + brJCR.readLine());
                 brJCR.close();
             } else {
                 System.err.println("No such node: " + jcrNodePathExample);
             }
             String query = "Hello";
             Node[] result = repoJCR.search(query);
             if (result.length == 0) {
                 System.out.println("Your search \"" + query + "\" did not match any node!");
             } else {
                 System.out.println(result.length + " results have been found:");
             }
             for (int i = 0; i < result.length ; i++) {
                 System.out.println("Result " + i + ": " + result[i].getPath());
             }
 
 
 
 
             System.out.println("\nWrite content to repository " + repoB.getName() + " (repoB) ...");
             Writer writerB = repoB.getWriter(worldPath);
 
 // TODO: See TODO.txt re VFS implementation
 /*
             System.out.println("\nWrite content to repository " + repoD.getName() + "...");
             Writer writerD = repoD.getWriter(new Path("/hello/vfs-example.txt"));
             writerD.write("Hello VFS example!\n...");
             writerD.close();
 */
         } catch (Exception e) {
             log.error(e, e);
         }
 
         try {
             repoJCR.close();
         } catch (Exception e) {
             System.err.println(e);
         }
 
         // DEBUG ...
 	//if(true) return;
 
 
 
         // Read content from repository
         System.out.println("\nRead content from repository " + repoA.getName() + " (repoA) ...");
         try {
             Reader readerA = repoA.getReader(worldPath);
             BufferedReader br = new BufferedReader(readerA);
             String line = br.readLine();
             StringWriter strWriter = new StringWriter();
             while (line != null) {
                 strWriter.write(line + "\n");
                 //System.out.println(line);
                 line = br.readLine();
             }
             System.out.println(strWriter.toString());
             strWriter.close();
             br.close();
             readerA.close();
 
             Path vfsSamplePath = new Path("/hello/vfs-example.txt");
             System.out.println("\nRead content from repository " + repoD.getName() + " (repoD, path: " + vfsSamplePath + ") ...");
             Reader readerD = repoD.getReader(vfsSamplePath);
             br = new BufferedReader(readerD);
             System.out.println("Very first line: " + br.readLine());
             readerD.close();
             // Read directory!
             readerD = repoD.getReader(new Path(vfsSamplePath.getParent().toString()));
             br = new BufferedReader(readerD);
             System.out.println("Very first line: " + br.readLine());
             readerD.close();
             readerD = repoD.getReader(new Path("/"));
             br = new BufferedReader(readerD);
             System.out.println("Very first line: " + br.readLine());
             readerD.close();
 
             System.out.println("\nUSECASE: Read content from node without a UID \"/no/uid/example.txt\" from repository \"" + repoA.getName() + " (" + repoA.getID() + ")\" ...");
             readerA = repoA.getReader(new Path("/no/uid/example.txt"));
             br = new BufferedReader(readerA);
             System.out.println("Very first line: " + br.readLine());
             readerA.close();
         } catch (Exception e) {
             System.err.println(e);
         }
 
         try {
 
             // List children
             System.out.println("\nUSECASE: List children of path \"/hello\" from repository \"" + repoNewFS.getName() + " (" + repoNewFS.getID() + ")\" ...");
             //System.out.println("\nUSECASE: List children of path \"/hello\" from repository \"" + repoA.getName() + " (" + repoA.getID() + ")\" ...");
             Path helloPath = new Path("/hello");
 
             Path[] children = repoNewFS.getChildren(helloPath);
             //Path[] children = repoA.getChildren(helloPath);
             for (int i = 0; i < children.length; i++) {
                 System.out.println("Child: " + children[i]);
             }
 
 	    //if (true) return;
 
             // Delete collection with deprecated method
             System.out.println("\nUSECASE: Try to delete \"" + helloPath + "\" from repository \"" + repoA.getName() + " (" + repoA.getID() + ")\" with deprecated method Repository.delete(Path, boolean) ...");
             if (repoA.delete(helloPath, false)) {
                 System.out.println("Node '" + helloPath + "' has been deleted.");
             } else {
                 System.err.println("Node '" + helloPath + "' could not be deleted!");
             }
 
             // Delete resource
             System.out.println("\nUSECASE: Try to delete \"" + worldPath + "\" from repository \"" + repoA.getName() + " (" + repoA.getID() + ")\" with deprecated method Repository.delete(Path) ...");
             if (repoA.delete(worldPath)) {
                 System.out.println("Node '" + worldPath + "' has been deleted.");
             } else {
                 System.err.println("Node '" + worldPath + "' could not be deleted!");
             }
 
             // Delete collection
             System.out.println("\nUSECASE: Try to delete \"" + helloPath + "\" from repository \"" + repoA.getName() + " (" + repoA.getID() + ")\" ...");
             Node helloNode = repoA.getNode(helloPath.toString());
             try {
                 helloNode.delete();
             } catch(Exception e) {
                 System.err.println(e);
             }
             if (!repoA.existsNode(helloPath.toString())) {
                 System.out.println("Node '" + helloPath + "' has been deleted.");
             } else {
                 System.err.println("Node '" + helloPath + "' could not be deleted!");
             }
 
         } catch (Exception e) {
             System.err.println(e);
         }
 
 
         try {
             String sampleNodePathToIndex = "/another-directory/index.html";
             if (!repoNewFS.existsNode(sampleNodePathToIndex)) {
                 log.warn("Node will be created: " + repoNewFS.getRootNode().addNode("another-directory", NodeType.COLLECTION).addNode("index.html", NodeType.RESOURCE).getPath());
             }
             Node node = repoNewFS.getNode(sampleNodePathToIndex);
             node.setMimeType("application/xhtml+xml");
             Writer writer = repoNewFS.getWriter(new Path(sampleNodePathToIndex));
             //OutputStream out = repoNewFS.getNode(sampleNodePathToIndex).getOutputStream();
             writer.write("Hello Yarep!");
             writer.close();
 
             String query = "yarep";
             System.out.println("\nUSECASE: Search for \"" + query + "\" within fulltext of repository '" + repoNewFS.getName() + " (" + repoNewFS.getConfigFile() + ")'");
             Node[] result = repoNewFS.search(query);
             if (result == null || result.length == 0) {
                 System.out.println("Your search \"" + query + "\" did not match any node!");
             } else {
                 System.out.println(result.length + " results have been found for '" + query + "' within fulltext:");
                 for (int i = 0; i < result.length ; i++) {
                     System.out.println("Result " + i + ": " + result[i].getPath());
                 }
             }
 
             String property = "title";
             node.setProperty(property, "Ingwer");
             query = "Ingwer";
             System.out.println("\nUSECASE: Search for \"" + query + "\" within properties of repository '" + repoNewFS.getName() + " (" + repoNewFS.getConfigFile() + ")'");
             result = repoNewFS.searchProperty("title", query, "/");
             if (result == null || result.length == 0) {
                 System.out.println("Your search \"" + query + "\" did not match any node!");
             } else {
                 System.out.println(result.length + " results have been found for '" + query + "' within property '" + property + "':");
                 for (int i = 0; i < result.length ; i++) {
                     System.out.println("Result " + i + ": " + result[i].getPath());
                 }
             }
         } catch (Exception e) {
             System.err.println(e.getMessage());
             log.error(e, e);
         }
     }
 
     /**
      *
      */
     public static void testRepoNewVFSPropertySearch() {
         try {
             Repository repo = getRepositoryFactory().newRepository("new-vfs");
 
             String propertyName = "title";
             String propertyValue = "Ingwer";
             repo.getNode("/hello-world.txt").setProperty(propertyName, propertyValue);
 
             String query = propertyValue;
             System.out.println("\nUSECASE: Search for \"" + query + "\" within properties of repository '" + repo.getName() + " (" + repo.getConfigFile() + ")'");
             Node[] result = repo.searchProperty(propertyName, query, "/");
             if (result == null || result.length == 0) {
                 System.out.println("Your search for \"" + query + "\" (Property: '" + propertyName + "') within repository '" + repo.getName() + "' did not match any node!");
             } else {
                 System.out.println(result.length + " results have been found for '" + query + "' within property '" + propertyName + "':");
                 for (int i = 0; i < result.length ; i++) {
                     System.out.println("Result " + i + ": " + result[i].getPath());
                 }
             }
         } catch (Exception e) {
             System.err.println(e.getMessage());
             log.error(e, e);
         }
     }
 
     /**
      *
      */
     public static void testRepoNewVFSFulltextSearch() {
         try {
             Repository repo = getRepositoryFactory().newRepository("new-vfs");
 
             String nodeName = "hello-ezra.txt";
             String nodePath = "/" + nodeName;
             if (!repo.existsNode(nodePath)) {
                 log.warn("Node will be created: " + repo.getRootNode().addNode(nodeName, NodeType.RESOURCE).getPath());
             }
             Node node = repo.getNode(nodePath);
            node.setMimeType("application/xhtml+xml");
             Writer writer = repo.getWriter(new Path(nodePath));
             //OutputStream out = repo.getNode(nodeName).getOutputStream();
             writer.write("Hello Ezra, brother of Levi and Vanya!");
             writer.close();
 
             String query = "Ezra";
             System.out.println("\nUSECASE: Search for \"" + query + "\" within fulltext of repository '" + repo.getName() + " (" + repo.getConfigFile() + ")'");
             Node[] result = repo.search(query);
             if (result == null || result.length == 0) {
                 System.out.println("Your search for \"" + query + "\" within repository '" + repo.getName() + "' did not match any node!");
             } else {
                 System.out.println(result.length + " results have been found for '" + query + "' within fulltext:");
                 for (int i = 0; i < result.length ; i++) {
                     System.out.println("Result " + i + ": " + result[i].getPath());
                 }
             }
         } catch (Exception e) {
             System.err.println(e.getMessage());
             log.error(e, e);
         }
     }
 
     /**
      *
      */
     public static RepositoryFactory getRepositoryFactory() {
         try {
             return new RepositoryFactory();
             // return RepositoryFactory("my-yarep.properties");
         } catch (Exception e) {
             System.err.println(e);
             log.error(e, e);
             return null;
         }
     }
 }
