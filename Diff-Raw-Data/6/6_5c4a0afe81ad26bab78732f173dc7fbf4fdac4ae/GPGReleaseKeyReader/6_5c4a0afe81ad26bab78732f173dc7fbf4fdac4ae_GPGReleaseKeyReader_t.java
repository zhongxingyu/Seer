 import java.io.File;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class GPGReleaseKeyReader extends RootPomXmlFilter {
 
 	public GPGReleaseKeyReader(File root) {
 		super(root);
 	}
 
 	@Override
 	public void process(File f, Document doc) throws Exception {
 		Element profileNode = (Element) findNode(doc,
 				"/project/profiles/profile/id[text()='release-sign-artifacts']/..");
		if (profileNode == null) {
			return;
		}
 		Element pluginsNode = (Element) findNode(profileNode, "build/plugins");
 		Element plugin = doc.createElement("plugin");
 		Element groupId = doc.createElement("groupId");
 		Element version = doc.createElement("version");
 		Element artifactId = doc.createElement("artifactId");
 
 		Element executions = doc.createElement("executions");
 		Element execution = doc.createElement("execution");
 		Element phase = doc.createElement("phase");
 		Element goals = doc.createElement("goals");
 		Element goal = doc.createElement("goal");
 		Element configuration = doc.createElement("configuration");
 		Element files = doc.createElement("files");
 		Element file = doc.createElement("file");
 
 		groupId.setTextContent("org.codehaus.mojo");
 		artifactId.setTextContent("properties-maven-plugin");
 		version.setTextContent("1.0-alpha-2");
 
 		phase.setTextContent("initialize");
 		goal.setTextContent("read-project-properties");
 
 		file.setTextContent("${gpg.passphrase.file}");
 
 		goals.appendChild(goal);
 
 		configuration.appendChild(files);
 		files.appendChild(file);
 
 		executions.appendChild(execution);
 		execution.appendChild(phase);
 		execution.appendChild(goals);
 		execution.appendChild(configuration);
 
 		plugin.appendChild(groupId);
 		plugin.appendChild(artifactId);
 		plugin.appendChild(version);
 		plugin.appendChild(executions);
 		// <plugin>
 		// <groupId>org.codehaus.mojo</groupId>
 		// <artifactId>properties-maven-plugin</artifactId>
 		// <version>1.0-alpha-2</version>
 		// <executions>
 		// <execution>
 		// <phase>initialize</phase>
 		// <goals>
 		// <goal>read-project-properties</goal>
 		// </goals>
 		// <configuration>
 		// <files>
 		// <file>${gpg.passphrase.file}</file></files></configuration></execution></executions></plugin>
 		pluginsNode.appendChild(plugin);

 		updateFile(f, doc);
 	}
 }
