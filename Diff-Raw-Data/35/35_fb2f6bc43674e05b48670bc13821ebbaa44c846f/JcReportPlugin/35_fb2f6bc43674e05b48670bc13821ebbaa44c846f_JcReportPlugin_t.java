 package org.jcoderz.phoenix.sonar;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.sonar.api.SonarPlugin;
 
 public class JcReportPlugin extends SonarPlugin 
 {
 
	public List<Class<JcReportDecorator>> getExtensions() 
 	{
		final List<Class<JcReportDecorator>> extensions = new ArrayList<Class<JcReportDecorator>>();
 		extensions.add(JcReportDecorator.class);
 		return extensions;
 	}
 
 }
