 package rxr.component.doc;
 
 import java.net.*;
 import java.util.*;
 import java.util.jar.*;
 import java.util.regex.*;
 
 public class URLJarNode extends URLNode
 {
 	public URLJarNode(URL url, URLNode root, URLNode parent)
 	{
 		super(url, root, parent);
 	}
 
 	public void fillChildren()
 	{
 		if(!isDir())
 		{
 			//System.out.println("[" + this + "]: I'm a file - returning");
 			return;
 		}
 
 		try
 		{
 			Pattern urlPattern = Pattern.compile("^jar:file:(.*?)!/(.*)$");
 
 			//we're running from a jar file, so look inside it
 			Matcher m = urlPattern.matcher(url.toExternalForm());
 			m.find();
 
 			//System.out.println(m.group(2));
 
			JarFile jf = new JarFile(m.group(1));
 
 			Enumeration<JarEntry> jes = jf.entries();
 
 			while(jes.hasMoreElements())
 			{
 				JarEntry je = jes.nextElement();
 				//System.out.println(je);
 				if(je.getName().startsWith(m.group(2)) && (je.getName().length() > m.group(2).length()))
 				{
 					//System.out.println("a: " + je.getName().substring(m.group(2).length()));
 					if(je.getName().substring(m.group(2).length()).matches(".+/.+"))
 					{
 						continue;
 					}
 					String baseUrl = url.toExternalForm();
 					baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf('!') + 2);
 					URLJarNode n = new URLJarNode(new URL(baseUrl + je.getName().replace(" ", "%20")), root, this);
 					if(!n.isDir() && !je.getName().endsWith(".html"))
 					{
 						continue;
 					}
 					children.add(n);
 					n.fillChildren();
 				}
 			}
 			jf.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 }
