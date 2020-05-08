 package fr.meijin.run4win.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 
 import fr.meijin.run4win.converter.IdentityConverter;
 import fr.meijin.run4win.model.Tournament;
 
 public class DocumentUtils {
 
 	public static File exportTournamentData(Tournament tournament) throws Exception {
 		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
 		String date = format.format(new Date());
 		
 		String fileTitle = "Run4Win_"+tournament.name+"_"+date+".html";
 		
 		StringBuilder finalName = new StringBuilder();
 		for (char c : fileTitle.toCharArray()) {
 			  if (c=='.' || Character.isJavaIdentifierPart(c)) {
 				  finalName.append(c);
 			  }
 			}
 		File file = new File(finalName.toString());
 
 		Properties properties = new Properties();
 		properties.put("input.encoding", "utf-8");
 		properties.setProperty("resource.loader", "class");
 		properties.setProperty("class.resource.loader.class",
 			"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
 
 		Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("rounds", tournament.roundsList);
 		parameters.put("name", tournament.name);
 		parameters.put("players", tournament.players);
 		parameters.put("date", date);
 		BufferedWriter out = new BufferedWriter(new FileWriter(file));
 
 		VelocityEngine engine = new VelocityEngine();
 		engine.init(properties);
 		Template template = engine.getTemplate("/template.html");
 
 		// Create a context and add parameters
 		VelocityContext context = new VelocityContext(parameters);
 		context.put("identityConverter", new IdentityConverter());
 
 		// Render the template for the context into a string
 		StringWriter stringWriter = new StringWriter();
 		template.merge(context, stringWriter);
 		String content = stringWriter.toString();
 			
 		out.write(content);
 		out.close();
 
 		return file;
 	}
 }
