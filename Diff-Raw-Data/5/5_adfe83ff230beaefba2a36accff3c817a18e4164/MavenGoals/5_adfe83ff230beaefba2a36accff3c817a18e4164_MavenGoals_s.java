 package com.atlassian.maven.plugins.refapp;
 
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.PluginManager;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.*;
 
 /**
  * Executes specific maven goals
  */
 public class MavenGoals {
     private final MavenProject project;
     private final MavenSession session;
     private final PluginManager pluginManager;
     private final Log log;
     private final Map<String, String> pluginArtifactIdToVersionMap;
 
     private final Map<String, Container> idToContainerMap = new HashMap<String, Container>()
     {{
             put("tomcat5x", new Container("tomcat5x", "https://m2proxy.atlassian.com/repository/public/org/apache/tomcat/apache-tomcat/5.5.25/apache-tomcat-5.5.25.zip"));
             put("tomcat6x", new Container("tomcat6x", "http://apache.mirror.aussiehq.net.au/tomcat/tomcat-6/v6.0.18/bin/apache-tomcat-6.0.18.zip"));
             put("resin3x", new Container("resin3x", "http://www.caucho.com/download/resin-3.0.26.zip"));
             put("jboss42x", new Container("jboss42x", "http://internode.dl.sourceforge.net/sourceforge/jboss/jboss-4.2.3.GA.zip"));
             put("jetty6x", new Container("jetty6x"));
 
         }};
 
     private final Map<String,String> defaultArtifactIdToVersionMap = new HashMap<String,String>()
     {{
             put("maven-cli-plugin", "0.6.2");
             put("cargo-maven2-plugin", "1.0-beta-2");
             put("atlassian-pdk", "2.1.5");
 
         }};
 
     public MavenGoals(MavenProject project, MavenSession session, PluginManager pluginManager, Log log) {
         this(project, session, pluginManager, log, Collections.<String, String>emptyMap());
     }
     public MavenGoals(MavenProject project, MavenSession session, PluginManager pluginManager, Log log, Map<String,String> pluginToVersionMap) {
         this.project = project;
         this.session = session;
         this.pluginManager = pluginManager;
         this.log = log;
 
         Map<String,String> map = new HashMap<String, String>(defaultArtifactIdToVersionMap);
         map.putAll(pluginToVersionMap);
         this.pluginArtifactIdToVersionMap = Collections.unmodifiableMap(map);
 
     }
 
     public void startCli(int port) throws MojoExecutionException {
 
         List<Element> configs = new ArrayList<Element>();
         configs.add(element(name("commands"),
                             element(name("pi"), "resources compile com.atlassian.maven.plugins:maven-refapp-plugin:copy-bundled-dependencies jar com.atlassian.maven.plugins:maven-refapp-plugin:install"),
                             element(name("pu"), "com.atlassian.maven.plugins:maven-refapp-plugin:uninstall")));
         if (port > 0)
         {
             configs.add(element(name("port"), String.valueOf(port)));
         }
         executeMojo(
                 plugin(
                         groupId("org.twdata.maven"),
                         artifactId("maven-cli-plugin"),
                         version(pluginArtifactIdToVersionMap.get("maven-cli-plugin"))
                 ),
                 goal("execute"),
                 configuration(configs.toArray(new Element[0])),
                 executionEnvironment(project, session, pluginManager)
         );
     }
     public void copyBundledDependencies() throws MojoExecutionException {
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-dependency-plugin")
                 ),
                 goal("copy-dependencies"),
                 configuration(
                     element(name("includeScope"), "runtime"),
                     element(name("excludeScope"), "provided"),
                     element(name("excludeScope"), "test"),
                     element(name("type"), "jar"),
                     element(name("outputDirectory"), "${project.build.outputDirectory}/META-INF/lib")
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void filterPluginDescriptor() throws MojoExecutionException {
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-resources-plugin")
                 ),
                 goal("copy-resources"),
                 configuration(
                     element(name("resources"),
                             element(name("resource"),
                                     element(name("directory"), "src/main/resources"),
                                     element(name("filtering"), "true"),
                                     element(name("includes"),
                                             element(name("include"), "atlassian-plugin.xml"))
                                     )
                             ),
                     element(name("outputDirectory"), "${project.build.outputDirectory}")
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void runUnitTests() throws MojoExecutionException {
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-surefire-plugin")
                 ),
                 goal("test"),
                 configuration(
                     element(name("excludes"),
                             element(name("exclude"), "it/**"),
                             element(name("exclude"), "**/*$*"))
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public File copyRefappWar(String refappVersion) throws MojoExecutionException {
         File refappWarFile = new File(new File("target"), "refapp-original.war");
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-dependency-plugin")
                 ),
                 goal("copy"),
                 configuration(
                     element(name("artifactItems"),
                             element(name("artifactItem"),
                                     element(name("groupId"), "com.atlassian.refapp"),
                                     element(name("artifactId"), "atlassian-refapp"),
                                     element(name("type"), "war"),
                                     element(name("version"), refappVersion),
                                     element(name("destFileName"), refappWarFile.getName()))),
                     element(name("outputDirectory"), "${project.build.directory}")
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
         return refappWarFile;
     }
 
     public void copyPlugins(File pluginsDir, List<PluginArtifact> pluginArtifacts) throws MojoExecutionException {
         Element[] items = new Element[pluginArtifacts.size()];
         for (int x=0; x<pluginArtifacts.size(); x++)
         {
             PluginArtifact artifact = pluginArtifacts.get(x);
             items[x] = element(name("artifactItem"),
                             element(name("groupId"), artifact.getGroupId()),
                             element(name("artifactId"), artifact.getArtifactId()),
                             element(name("version"), artifact.getVersion()));
         }
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-dependency-plugin")
                 ),
                 goal("copy"),
                 configuration(
                     element(name("artifactItems"), items),
                     element(name("outputDirectory"), pluginsDir.getPath())
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     public int startRefapp(File refappWar, String containerId, int httpPort, String jvmArgs) throws MojoExecutionException {
         int rmiPort = pickFreePort(0);
         int actualHttpPort = pickFreePort(httpPort);
         Container container = findContainer(containerId);
         if (jvmArgs == null)
         {
             jvmArgs = "";
         }
         jvmArgs += " -Dosgi.cache=${project.build.directory}/osgi-cache";//"-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005";
 
         log.info("Starting refapp on the " + container.getId() + " container on ports "
                 + actualHttpPort + " (http) and " + rmiPort + " (rmi)");
 
         executeMojo(
                 plugin(
                     groupId("org.codehaus.cargo"),
                     artifactId("cargo-maven2-plugin"),
                     version(pluginArtifactIdToVersionMap.get("cargo-maven2-plugin"))
                 ),
                 goal("start"),
                 configuration(
                     element(name("wait"), "false"),
                     element(name("container"),
                             element(name("containerId"), container.getId()),
                             element(name("type"), container.getType()),
                             element(name("zipUrlInstaller"),
                                     element(name("url"), container.getUrl())
                             ),
                             //element(name("output"), "${project.build.directory}/"+container.getId()+"/output-"+identifier+".log"),
                             //element(name("log"), "${project.build.directory}/"+container.getId()+"/cargo-"+identifier+".log"),
                             element(name("systemProperties"),
                                     element(name("org.apache.commons.logging.Log"), "org.apache.commons.logging.impl.SimpleLog")
                             )
                     ),
                     element(name("configuration"),
                             element(name("home"), "${project.build.directory}/" + container.getId() + "/server"),
                             element(name("properties"),
                                     element(name("cargo.servlet.port"), String.valueOf(actualHttpPort)),
                                     element(name("cargo.rmi.port"), String.valueOf(rmiPort)),
                                     element(name("cargo.jvmargs"), jvmArgs)
                             ),
                             element(name("deployables"),
                                     element(name("deployable"),
                                             element(name("groupId"), "com.atlassian.refapp"),
                                             element(name("artifactId"), "atlassian-refapp"),
                                             element(name("type"), "war"),
                                             element(name("location"), refappWar.getPath())
                                             //element(name("properties"),
                                             //        element(name("context"), "/")
                                             //)
                                     )
                             )
                     )
                 ),
                 executionEnvironment(project, session, pluginManager)
             );
         return actualHttpPort;
     }
 
     public void runTests(String containerId, String functionalTestPattern, int httpPort, String pluginJar) throws MojoExecutionException {
         executeMojo(
                 plugin(
                         groupId("org.apache.maven.plugins"),
                         artifactId("maven-surefire-plugin")
                 ),
                 goal("test"),
                 configuration(
                         element(name("includes"),
                                 element(name("include"), functionalTestPattern)
                         ),
                         element(name("excludes"),
                                 element(name("exclude"), "**/*$*")
                         ),
                         element(name("systemProperties"),
                                 element(name("property"),
                                         element(name("name"), "http.port"),
                                         element(name("value"), String.valueOf(httpPort))
                                 ),
                                 element(name("property"),
                                         element(name("name"), "plugin.jar"),
                                         element(name("value"), pluginJar)
                                 )
                         ),
                         element(name("reportsDirectory"), "${project.build.directory}/" + containerId + "/surefire-reports")
                 ),
                 executionEnvironment(project, session, pluginManager)
         );
     }
 
     private Container findContainer(String containerId)
     {
         Container container = idToContainerMap.get(containerId);
         if (container == null)
         {
             throw new IllegalArgumentException("Container " + containerId + " not supported");
         }
         return container;
     }
 
     private int pickFreePort(int requestedPort)
     {
         if (requestedPort > 0)
         {
             return requestedPort;
         }
         ServerSocket socket = null;
         try
         {
             socket = new ServerSocket(0);
             return socket.getLocalPort();
         }
         catch (IOException e)
         {
             throw new RuntimeException("Error opening socket", e);
         }
         finally
         {
             if (socket != null)
             {
                 try
                 {
                     socket.close();
                 }
                 catch (IOException e)
                 {
                     throw new RuntimeException("Error closing socket", e);
                 }
             }
         }
     }
 
     public void stopRefapp(String containerId) throws MojoExecutionException {
         Container container = findContainer(containerId);
         executeMojo(
             plugin(
                 groupId("org.codehaus.cargo"),
                 artifactId("cargo-maven2-plugin"),
                 version(pluginArtifactIdToVersionMap.get("cargo-maven2-plugin"))
             ),
             goal("stop"),
             configuration(
                     element(name("container"),
                             element(name("containerId"), container.getId()),
                             element(name("type"), container.getType())
                     ),
                     element(name("configuration"),
                             element(name("home"), "${project.build.directory}/" + container.getId() + "/server")
                     )
             ),
             executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void installPlugin(String pluginKey, int port) throws MojoExecutionException {
         executeMojo(
             plugin(
                 groupId("com.atlassian.maven.plugins"),
                 artifactId("atlassian-pdk"),
                 version(pluginArtifactIdToVersionMap.get("atlassian-pdk"))
             ),
             goal("install"),
             configuration(
                     element(name("username"), "admin"),
                     element(name("password"), "admin"),
                    element(name("serverUrl"), "http://localhost:"+port),
                     element(name("pluginKey"), pluginKey)
             ),
             executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void uninstallPlugin(String pluginKey, int port) throws MojoExecutionException {
         executeMojo(
             plugin(
                 groupId("com.atlassian.maven.plugins"),
                 artifactId("atlassian-pdk"),
                 version(pluginArtifactIdToVersionMap.get("atlassian-pdk"))
             ),
             goal("uninstall"),
             configuration(
                     element(name("username"), "admin"),
                     element(name("password"), "admin"),
                    element(name("serverUrl"), "http://localhost:"+port),
                     element(name("pluginKey"), pluginKey)
             ),
             executionEnvironment(project, session, pluginManager)
         );
     }
 
     public void installIdeaPlugin() throws MojoExecutionException {
         executeMojo(
                 plugin(
                         groupId("org.twdata.maven"),
                         artifactId("maven-cli-plugin"),
                         version(pluginArtifactIdToVersionMap.get("maven-cli-plugin"))
                 ),
                 goal("idea"),
                 configuration(),
                 executionEnvironment(project, session, pluginManager)
         );    
     }
 
     private static class Container
     {
         private final String id;
         private final String type;
         private final String url;
 
         public Container(String id, String url)
         {
             this.id = id;
             this.type = "installed";
             this.url = url;
         }
 
         public Container(String id)
         {
             this.id = id;
             this.type = "embedded";
             this.url = null;
         }
 
         public String getId()
         {
             return id;
         }
 
         public String getType()
         {
             return type;
         }
 
         public String getUrl()
         {
             return url;
         }
     }
 }
