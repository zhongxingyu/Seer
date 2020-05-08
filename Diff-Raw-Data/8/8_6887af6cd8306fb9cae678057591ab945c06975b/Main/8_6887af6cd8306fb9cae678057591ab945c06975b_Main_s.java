 /**
 * Copyright © 2012 The Project Lombok Authors.
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE. */
 package com.zwitserloot.stubber;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.jar.JarFile;
 
 import lombok.Cleanup;
 import lombok.val;
 
 import com.zwitserloot.cmdreader.CmdReader;
 import com.zwitserloot.cmdreader.Description;
 import com.zwitserloot.cmdreader.FullName;
 import com.zwitserloot.cmdreader.InvalidCommandLineException;
 import com.zwitserloot.cmdreader.Sequential;
 import com.zwitserloot.cmdreader.Shorthand;
 import com.zwitserloot.stubber.reader.DependencySweeper;
 import com.zwitserloot.stubber.writer.StubJarWriter;
 
 public class Main {
 	static class CmdArgs {
 		@Shorthand("t")
 		@Description("Include the provided type name as a root point.")
 		@FullName("type")
 		List<String> types = new ArrayList<String>();
 		
 		@Shorthand("c")
		@Description("Add to the classpath without scouring them for public classes to use as API roots. Use '/a/b/c/*' to grab all jar files in /a/b/c.")
 		List<String> classpath = new ArrayList<String>();
 		
 		@Shorthand("i")
 		@Description("class name prefix (example: \"java/\"; that one is added by default); any class with this prefix is not considered part of the API and is not scoured for further dependencies.")
 		List<String> ignore = new ArrayList<String>();
 		
 		@Sequential
		@Description("Include all public and protected class files in the provided directory or jar file as root points. Use '/a/b/c/*' to grab all jar files in /a/b/c.")
 		@FullName("root")
 		List<String> roots = new ArrayList<String>();
 		
 		@Shorthand("o")
 		@Description("Write a jar containing stubs to this file.")
 		String out;
 		
 		@Shorthand("v")
 		@Description("Show each class as it is stubbed. Implied if `out` is missing.")
 		boolean verbose;
 		
 		@Description("Show version number and exit.")
 		boolean version;
 		
 		@Shorthand("h")
 		@Description("Show this command line help and exit.")
 		boolean help;
 	}
 	
 	public static void main(String[] rawArgs) throws Exception {
 		val reader = CmdReader.of(CmdArgs.class);
 		CmdArgs args;
 		try {
 			args = reader.make(rawArgs);
 		} catch (InvalidCommandLineException e) {
 			System.err.println("Invalid command line options: " + e.getMessage());
 			System.err.println(reader.generateCommandLineHelp("java -jar stubber.jar"));
 			System.exit(5);
 			return;
 		}
 		
 		if (args.help) {
 			System.out.println(reader.generateCommandLineHelp("java -jar stubber.jar"));
 			System.exit(0);
 			return;
 		}
 		
 		if (args.version) {
 			System.out.println(Version.getVersion());
 			System.exit(0);
 			return;
 		}
 		
 		val urls = new ArrayList<URL>();
 		for (String cp : args.classpath) addClasspathEntry(urls, cp);
 		for (String rt : args.roots) addClasspathEntry(urls, rt);
 		
 		ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]));
 		val sweeper = new DependencySweeper(cl);
 		for (String ignore : args.ignore) sweeper.addExclusionPrefix(ignore);
 		val startingPoints = new HashSet<String>();
 		startingPoints.addAll(args.types);
 		for (String rt : args.roots) {
 			for (String f : asClasspathEntry(rt)) {
 				startingPoints.addAll(findAllTypesIn(f));
 			}
 		}
 		if (startingPoints.isEmpty()) {
 			System.err.println("No types specified and no types found in any roots either.");
 			System.exit(1);
 			return;
 		}
 		sweeper.fill(startingPoints);
 		val typesToStub = sweeper.getTypeNames();
 		if (typesToStub.isEmpty()) {
 			System.out.println("Zero classes needed to stub; no stub file generated.");
 			System.exit(0);
 			return;
 		}
 		if (args.verbose || args.out == null) printAll(typesToStub, System.out);
 		if (args.out != null) new StubJarWriter(cl).write(typesToStub, args.out);
 	}
 	
 	private static Collection<String> findAllTypesIn(String rt) throws IOException {
 		File path = new File(rt);
 		if (path.isDirectory()) return findAllTypesIn(path);
 		if (path.isFile()) {
 			try {
 				@Cleanup JarFile jf = new JarFile(path);
 				return findAllTypesIn(jf);
 			} catch (IOException e) {
 				System.err.println("Error in file: " + rt);
 				throw e;
 			}
 		}
 		
 		throw new IllegalArgumentException("Not found: " + rt);
 	}
 	
 	private static Collection<String> findAllTypesIn(JarFile jf) {
 		val col = new HashSet<String>();
 		val entries = jf.entries();
 		while (entries.hasMoreElements()) {
 			val entry = entries.nextElement();
 			String path = entry.getName();
 			if (path.endsWith(".class") && !entry.isDirectory()) {
 				col.add(path.substring(0, path.length() - ".class".length()));
 			}
 		}
 		return col;
 	}
 	
 	private static Collection<String> findAllTypesIn(File path) {
 		Collection<String> col = new HashSet<String>();
 		findAllTypesIn_(col, path);
 		return col;
 	}
 	
 	private static void findAllTypesIn_(Collection<String> col, File path) {
 		for (File file : path.listFiles()) {
 			if (file.isDirectory()) findAllTypesIn_(col, file);
 			else if (file.isFile()) {
 				String pathName = file.getPath();
 				if (pathName.endsWith(".class")) {
 					col.add(pathName.substring(0, pathName.length() - ".class".length()));
 				}
 			}
 		}
 	}
 	
 	private static void printAll(Collection<String> typesToStub, PrintStream out) {
 		for (String type : typesToStub) out.println(type);
 	}
 	
 	private static List<String> asClasspathEntry(String rt) {
 		val out = new ArrayList<String>();
		if (rt.endsWith("*")) {
 			File file = new File(rt.substring(0, rt.length() - 1));
 			if (file.isDirectory()) {
 				for (File f : file.listFiles()) {
 					if (f.getName().endsWith(".jar") && f.isFile()) {
 						out.add(f.getAbsolutePath());
 					}
 				}
 			}
 		} else {
 			out.add(rt);
 		}
 		return out;
 	}
 	
 	private static void addClasspathEntry(ArrayList<URL> urls, String cp) throws MalformedURLException {
 		for (String e : asClasspathEntry(cp)) urls.add(toURL(e));
 	}
 	
 	private static URL toURL(String path) throws MalformedURLException {
 		return new File(path).toURI().toURL();
 	}
 }
