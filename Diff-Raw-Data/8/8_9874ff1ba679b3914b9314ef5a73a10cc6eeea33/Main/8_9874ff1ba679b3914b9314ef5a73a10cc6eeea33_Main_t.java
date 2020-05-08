 package com.ngdata.jajc;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.apache.log4j.Logger;
 import org.jclouds.compute.RunScriptOnNodesException;
 import org.jclouds.compute.domain.ExecResponse;
 import org.jclouds.compute.domain.NodeMetadata;
 import org.jclouds.io.Payloads;
 import org.jclouds.scriptbuilder.ScriptBuilder;
 import org.jclouds.scriptbuilder.domain.Statements;
 import org.jclouds.ssh.SshClient;
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.collect.Iterables;
 import com.ngdata.jajc.bo.Roles;
 import com.ngdata.jajc.exception.JajcException;
 import com.ngdata.jajc.providers.Provider;
 import com.ngdata.jajc.providers.AbstractProvider;
 import com.ngdata.jajc.puppetconfiguration.Cdh3PuppetConfiguration;
 import com.ngdata.jajc.puppetconfiguration.ConfigurationManager;
 import com.ngdata.jajc.puppetconfiguration.JavaPuppetConfiguration;
 import com.ngdata.jajc.puppetconfiguration.LilyPuppetConfiguration;
 import com.ngdata.jajc.puppetconfiguration.MCollectiveConfiguration;
 import com.ngdata.jajc.puppetconfiguration.PuppetConfiguration;
 
 public class Main 
 {	
 	
 	static Logger log = Logger.getLogger(Main.class.getName());
 	
 	private Iterable<NodeMetadata> nodes;
 	private NodeMetadata pm;
 	private Provider provider;
 	private String java_home;
 	
 	
 	public Main(String configFileName, String moduledir) throws Exception {
 		
 		IConfig configuration = YamlConfigReader.createYAMLConfig(configFileName);
 		
 		if (configuration.getInstances().size() == 0)
 			throw new com.ngdata.jajc.exception.JaJcLogicalConfigException("No instances defined in " + configFileName);
 		
 		if (Roles.getInstances("puppetmaster") == null || Roles.getInstances("puppetmaster").size() > 1 )
 			throw new com.ngdata.jajc.exception.JaJcLogicalConfigException("Need at least one puppetmaster" + configFileName);
 		
 		provider = AbstractProvider.createProvider(configuration);
 		nodes = provider.getNodes();
 		
 		
 		pm = Iterables.filter(nodes,new Predicate<NodeMetadata>() {
 			@Override public boolean apply(NodeMetadata nd) {
 				return AbstractProvider.extractRolesFromNode(nd).contains("puppetmaster");
 			} } ).iterator().next();
 		log.debug("Puppet master: " + pm.getHostname());
 		
 		log.info("Installing necessary components for puppet");
 		initializeAllNodes();
 		log.info("Configuring puppet master");
 		initializePuppetMaster(moduledir);
 		log.info("Starting puppet agents");
 		startPuppetAgents();
 		
 		provider.closeContext();
 			
 	}
 	
 	private void startPuppetAgents() throws RunScriptOnNodesException, JajcException {
 		Map<? extends NodeMetadata, ExecResponse> resp = AbstractProvider.getInstance().runScriptOnNodesMatching(Predicates.<NodeMetadata>alwaysTrue(), 
 				Statements.exec("export JAVA_HOME=" + java_home + " && /var/lib/gems/1.8/bin/puppet agent"));
 		
 		log.debug("Results of starting the puppet agents on all the nodes:");
 		for (NodeMetadata nm : resp.keySet()){
 				log.debug(nm.getHostname() + " : " + resp.get(nm).getExitStatus());
 		}
 		
 	}
 
 	private void initializeAllNodes() throws RunScriptOnNodesException {
 		
 		//Create puppet.conf
 		Collection<String> lines = new ArrayList<String>();
 		lines.add("[master]");
 		lines.add("certname=" + pm.getHostname());
 		lines.add("[main]");
 		lines.add("logdir = /var/lib/puppet/log");
 		lines.add("vardir = /var/lib/puppet");
 		lines.add("factpath = $vardir/lib/facter");
 		lines.add("ssldir = /var/lib/puppet/ssl");
 		lines.add("rundir = /var/run/puppet");
 		lines.add("server = " + pm.getHostname());
 		//lines.add("listen = true"); //support for puppet kick
 		//lines.add("pluginsync = true");
 		
 		//Script to run
 		ScriptBuilder sb = new ScriptBuilder();
 		sb.addStatement(Statements.exec("apt-get install -y rubygems"));
 		sb.addStatement(Statements.exec("apt-get install -y tar"));
 		sb.addStatement(Statements.exec("mkdir -p /opt/gems"));
 		sb.addStatement(Statements.exec("/usr/bin/gem install puppet --no-ri --no-rdoc")); // --install-dir /opt/gems"));
 		sb.addStatement(Statements.exec("groupadd puppet"));
 		sb.addStatement(Statements.exec("useradd -g puppet -G admin puppet"));
 		sb.addStatement(Statements.exec("mkdir -p /etc/puppet"));
 		sb.addStatement(Statements.createOrOverwriteFile("/etc/puppet/puppet.conf", lines));
 		sb.addStatement(Statements.exec("apt-get install -y libopenssl-ruby"));
 		
 		
 		Map<? extends NodeMetadata, ExecResponse> resp = provider.runScriptOnNodesMatching(Predicates.<NodeMetadata>alwaysTrue(), sb);
 		log.debug("Results of installing puppet on the nodes in the cluster:");
 		for (NodeMetadata nm : resp.keySet()){
 			log.debug(nm.getHostname() + " : " + resp.get(nm).getExitStatus());
 		}
 		
 	}
 	
 	private void initializePuppetMaster(String moduledir) throws JajcException, IOException {
 		ScriptBuilder master = new ScriptBuilder();
 		master.addStatement(Statements.exec("mkdir -p /etc/puppet/manifests"));
 		master.addStatement(Statements.createOrOverwriteFile("/etc/puppet/manifests/site.pp", ManifestBuilder.createPuppetManifest(nodes)));
 		master.addStatement(Statements.createOrOverwriteFile("/etc/puppet/autosign.conf", Arrays.asList("*") ));
 		provider.runScriptOnNode(pm.getId(), master);
 					
 		
 		ConfigurationManager cm = ConfigurationManager.getConfigurationManager();
 		cm.add(new MCollectiveConfiguration());
 		cm.add(new JavaPuppetConfiguration());
 		cm.add(new Cdh3PuppetConfiguration());
 		cm.add(new LilyPuppetConfiguration());
 		cm.build();
 		Map<String,PuppetConfiguration> pcs = cm.getConfigurations();
 		JavaPuppetConfiguration jpc = (JavaPuppetConfiguration) cm.getConfigurations().get("java");
 		java_home = jpc.getPathName();
 		log.debug("JAVA_HOME set to " + java_home);
 		
 		log.debug("Creating /tmp/modules.tgz");
 		String moduleTgzLocation = "/tmp/modules.tgz";
 		Util.createTgz(moduledir , moduleTgzLocation);
 		
 		SshClient ssh = AbstractProvider.getInstance().getSshClient(pm);
 		
 		try {
 			ssh.connect();

 			log.debug("Transferring modules.tgz");
 			ssh.put("/tmp/modules.tgz", Payloads.newFilePayload(new File(moduleTgzLocation)));
 			log.debug("Unpacking modules.tgz");
 			ssh.exec("sudo tar xzf /tmp/modules.tgz -C /etc/puppet/");
 			
 			if (jpc.getBinFilename() != null) {
 				log.debug("Transferring java binary: " + jpc.getBinFilename());
 				ssh.put("/tmp/java_binary",Payloads.newFilePayload(new File(jpc.getBinFilename())));
 				ssh.exec("sudo mv /tmp/java_binary /etc/puppet/modules/java/files/" + jpc.getBinFilename());
 			}
 			
 			log.debug("Transferring ActiveMQ .tgz file");
 			ssh.put("/tmp/activemq.tgz",Payloads.newFilePayload(new File("activemq.tgz")));
 			ssh.exec("mv /tmp/activemq.tgz /etc/puppet/modules/mcollective/files/activemq.tgz");
 			
 			
 			log.debug("Transferring the generated environment.pp files");

 			ssh.put("/tmp/modules.tgz", Payloads.newFilePayload(new File(System.getProperty("user.dir"), "modules.tgz")));
 			System.out.println(ssh.exec("sudo tar xzf /tmp/modules.tgz -C /etc/puppet/"));

 			for ( String module : pcs.keySet() ) {
 				log.debug("Installing environment.pp for " + module + " module");
 				ssh.put("/tmp/environment.pp",Payloads.newStringPayload( pcs.get(module).getEnvironmentContent()));
 				ssh.exec("sudo cp /tmp/environment.pp /etc/puppet/modules/" + module +"/manifests/");
 			}
 			log.debug("Starting Puppet Master  (exitcode: " + ssh.exec("sudo /var/lib/gems/1.8/bin/puppet master") + ")");
 			
 		}
 		finally {
 			if (ssh != null)
 				ssh.disconnect();
 		}
 	}
 
 	public static void main( String[] args ) throws Exception {
 		String ymlfile = "setup.yml";
 		String moduledir = "../puppet/modules";
 		CommandLineParser clp = new PosixParser();
 		CommandLine line = null;	
 		Options options = new Options();
 		options.addOption("f", "yml-file", true, "The .yml that contains the cluster configuration, defaults to setup.yml");
 		options.addOption("m" , "moduledir" , true , "Location of the puppet modules directory that needs to be installed on the puppet master, defaults to ../puppet/modules");
 		line = clp.parse(options, args);
 		
 		if (line.hasOption("yml-file"))
 			ymlfile = line.getOptionValue("yml-file");
 		if (line.hasOption("moduledir"))
 			moduledir = line.getOptionValue("moduledir");
 		
 		new Main(ymlfile , moduledir);
 		
     }
 	
 	
     
 }
