 package zcu.launcher;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 /**
  * 启动工具类,主要功能是构建类路径 <br>
  * 使用配置文件为jar文件同目录同名的 properties文件。配置文件除app.mainclass app.classpath外的属性设置为系统属性<br>
  * app.mainclass 为启动类名. app.classpath 为类路径,分隔字符为 ';' 如: path1;path2;path3<br>
  * path为目录时增加了该目录下的jar，以 "*"结束的path表示该目录及子目录的所有 文件<br>
  * app.mainclass=org.mortbay.xml.XmlConfiguration <br>
  * app.classpath=${jetty.home}/lib/*;${xutils.home}/classes<br>
  * 
  * @author <a href="mailto:zxiao@yeepay.com">xiao zaichu</a>
  */
 public final class Main implements FilenameFilter {
 	private List<File> elements = new ArrayList<File>();
 
 	public void addComponent(String pattern) throws IOException {
 		boolean all = (pattern = pattern.trim().replace('/', File.separatorChar)).endsWith("*");
 		File file = new File(all ? pattern.substring(0, pattern.length() - 1) : pattern);
 		addComponent(file);
 		if (file.isDirectory()) {
 			if (all)
 				addAll(file);
 			else {
 				for (File item : file.listFiles(this))
 					addComponent(item);
 			}
 		}
 	}
 
 	@Override
 	public boolean accept(File dir, String name) {
 		return name.endsWith(".jar");
 	}
 
 	private void addComponent(File file) throws IOException {
 		if (!elements.contains(file = file.getCanonicalFile()))
 			elements.add(file);
 	}
 
 	private void addAll(File dir) throws IOException {
 		for (File f : dir.listFiles()) {
 			if (f.isDirectory()) {
 				addComponent(f);
 				addAll(f);
 			} else if (f.getName().endsWith(".jar"))
 				addComponent(f);
 		}
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder cp = new StringBuilder(1024);
 		for (int i = 0, len = elements.size(); i < len; i++)
 			(i > 0 ? cp.append(File.pathSeparatorChar) : cp).append(elements.get(i).getPath());
 		return cp.toString();
 	}
 
 	private URL[] getUrls(int begin) throws MalformedURLException {
 		int len = elements.size() - begin;
 		URL[] urls = new URL[len];
 		while (--len >= 0)
 			urls[len] = elements.get(begin + len).toURI().toURL();
 		return urls;
 	}
 
 	private static Method init() throws Exception {
 		URL location = Main.class.getResource("Main.class");
 		String s = location.getPath();
 		Properties prop = new Properties();
		FileReader reader =new FileReader(s.substring("file:".length(), s.lastIndexOf('!') - 3) + "properties");
		prop.load(reader);
		reader.close();
 		s = prop.getProperty("app.mainclass");
 		String[] paths = prop.getProperty("app.classpath").split(";");
 		prop.remove("app.mainclass");
 		prop.remove("app.classpath");
 		String javapath = System.getProperty("java.class.path");
 		System.getProperties().putAll(prop);
 		Thread thread = Thread.currentThread();
 		ClassLoader loader = thread.getContextClassLoader();
 		Main builder = new Main();
 		for (String p : javapath.split(File.pathSeparator))
 			builder.addComponent(p);
 		int begin = builder.elements.size();
 		for (String p : paths)
 			builder.addComponent(p);
 		URL[] urls = builder.getUrls(begin);
 		System.setProperty("java.class.path", javapath = builder.toString());
 		thread.setContextClassLoader(loader = new URLClassLoader(urls, loader));
 		System.out.println(javapath);
 		return loader.loadClass(s).getDeclaredMethod("main", String[].class);
 	}
 
 	public static void main(String[] args) {
 		try {
			init().invoke(null, (Object) args);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("set app.mainclass app.classpath in properties file,classpath split by ';'");
 			System.exit(-1);
 		}
 	}
 }
