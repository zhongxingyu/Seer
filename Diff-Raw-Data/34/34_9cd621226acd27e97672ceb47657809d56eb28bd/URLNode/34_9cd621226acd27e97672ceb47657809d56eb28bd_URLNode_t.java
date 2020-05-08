 package rxr.component.doc;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.util.regex.*;
 
 public abstract class URLNode implements Comparable<URLNode>
 {
 	URL url;
 
 	String[] path;
 
 	ArrayList<URLNode> children;
 
 	URLNode root;
 
 	String title;
 
 	int index = Integer.MAX_VALUE;
 
 	URLNode redirect = null;
 
 	URLNode parent;
 
 	public URLNode(URL url, URLNode root, URLNode parent)
 	{
 		this.url = url;
 		this.root = root;
 		this.parent = parent;
 		try
 		{
 			path = URLDecoder.decode(url.getPath(), "UTF-8").split("/");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		children = new ArrayList<URLNode>();
 		if(root == null)
 		{
 			this.root = this;
 		}
 	}
 
 	public void init()
 	{
 		if(isDir())
 		{
 			//find .index file
 			try
 			{
 				URL newurl = new URL(url.toExternalForm() + ".index");
				BufferedReader in = new BufferedReader(new InputStreamReader(newurl.openStream(),"Cp1252"));
 
 				String line;
 				while((line = in.readLine()) != null)
 				{
 					line = line.trim();
 					String[] tok = line.split("=", 2);
 					if(tok[0].trim().equals("index"))
 					{
 						index = Integer.parseInt(tok[1]);
 					}
 					else if(tok[0].trim().equals("show"))
 					{
 						redirect = getNodeForURL(new URL(url.toExternalForm() + tok[1].trim()));
 					}
 					else if(tok[0].trim().equals("title"))
 					{
 						title = tok[1].trim();
 					}
 				}
 
 				in.close();
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			//parse index and title
 			try
 			{
 				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "Cp1252"));
 
 				Pattern titlePattern = Pattern.compile("(?i)<title>(.*?)</title>");
 				Pattern indexPattern = Pattern.compile("(?i)<!--\\s*index\\s*=\\s*(\\d+)\\s*-->");
 
 				boolean foundTitle = false;
 				boolean foundIndex = false;
 
 				String line;
 				while((line = in.readLine()) != null)
 				{
 					if(!foundTitle)
 					{
 						Matcher m = titlePattern.matcher(line);
 						if(m.find())
 						{
 							title = m.group(1);
 							foundTitle = true;
 						}
 					}
 					if(!foundTitle)
 					{
 						Matcher m = indexPattern.matcher(line);
 						if(m.find())
 						{
 							index = Integer.parseInt(m.group(1));
 							foundIndex = true;
 						}
 					}
 					if(foundTitle && foundIndex)
 					{
 						break;
 					}
 				}
 
 				in.close();
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 
 		for(URLNode n : children)
 		{
 			n.init();
 		}
 		Collections.sort(children);
 	}
 
 	public URLNode getNodeForURL(URL url)
 	{
 		if(this.url.equals(url))
 		{
 			return this;
 		}
 		for(URLNode n : children)
 		{
 			URLNode result = n.getNodeForURL(url);
 			if(result != null)
 			{
 				return result;
 			}
 		}
 		return null;
 	}
 
 	public abstract void fillChildren();
 
 	public boolean isDir()
 	{
 		return url.getPath().endsWith("/");
 	}
 
 	public String getRelativePath()
 	{
 		StringBuilder sb = new StringBuilder();
 		for(int i = root.path.length; i < path.length; i++)
 		{
 			sb.append(path[i]);
 			sb.append('/');
 		}
 		if(!isDir())
 		{
 			sb.deleteCharAt(sb.length() - 1);
 		}
 		return sb.toString();
 	}
 
 	@Override
 	public String toString()
 	{
 		if(title != null)
 		{
 			return title;
 		}
 		return path[path.length - 1];
 	}
 
 	public ArrayList<URLNode> getChildren()
 	{
 		return children;
 	}
 
 	@Override
 	public int compareTo(URLNode o)
 	{
 		if(index == o.index)
 		{
 			return toString().compareTo(o.toString());
 		}
 		else
 		{
 			return index < o.index ? -1 : (index == o.index ? 0 : 1);
 		}
 	}
 
 	public static URLNode makeRoot(URL url)
 	{
 		if(url.getProtocol() == "jar")
 		{
 			return new URLJarNode(url, null, null);
 		}
 		else if(url.getProtocol() == "file")
 		{
 			return new URLFileNode(url, null, null);
 		}
 		else
 		{
 			System.out.println("Unknown protocol: " + url.getProtocol());
 			return null;
 		}
 	}
 }
