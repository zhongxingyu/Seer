 package org.jcoderz.phoenix.sonar;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.sonar.api.SonarPlugin;
 
 public class JcReportPlugin extends SonarPlugin 
 {
 
	public List getExtensions() 
 	{
		final List extensions = new ArrayList();
 		extensions.add(JcReportDecorator.class);
 		return extensions;
 	}
 
 }
