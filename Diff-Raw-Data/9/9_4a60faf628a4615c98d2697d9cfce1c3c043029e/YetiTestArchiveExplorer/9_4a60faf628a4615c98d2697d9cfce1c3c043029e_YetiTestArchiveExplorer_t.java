 package yeti.experimenter;
 
 /**
 
 YETI - York Extensible Testing Infrastructure
 
 Copyright (c) 2009-2011, Manuel Oriol <manuel.oriol@gmail.com> - University of York
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 3. All advertising materials mentioning features or use of this software
 must display the following acknowledgement:
 This product includes software developed by the University of York.
 4. Neither the name of the University of York nor the
 names of its contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 **/ 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import yeti.YetiLog;
 
 /**
  * This class is an explorer for an archive containing 
  * 
  * @author Manuel Oriol (manuel@cs.york.ac.uk)
  * @date Apr 6, 2011
  *
  */
 public class YetiTestArchiveExplorer {
 	
 	public static boolean INCLUDE_ALL_JARS = true;
 	
 	/**
 	 * All the test archive modules.  
 	 */
 	public Vector<YetiTestArchiveModule> allModules = new Vector<YetiTestArchiveModule>();
 	
 	/**
 	 * All jar files.
 	 */
 	public Vector<String> allJars = new Vector<String>();
 	
 	/**
 	 * All directories files.
 	 */
 	public Vector<String> allDirs = new Vector<String>();
 	
 	/**
 	 * Number of JAR files.
 	 */
 	public int numberOfJarFiles=0;
 
 	/**
 	 * Number of classes.
 	 */
 	public int numberOfClasses=0;
 	
 	/**
 	 * The file where to output the lists of classes.
 	 */
 	public static String outputFile = "PackageList.yexp";
 
 	
 	/**
 	 * The path to start the exploration.
 	 */
 	public String startingPath = System.getProperty("user.dir");
 	
 	
 	/**
 	 * Simple constructor for running an explorer.
 	 * 
 	 * @param startingPath
 	 */
 	public YetiTestArchiveExplorer(String startingPath) {
 		this.startingPath = startingPath;
 	}
 
 	/**
 	 * Writes the file for the directory.
 	 */
 	public void writeFile() {
 		PrintStream ps;
 		try {
 			ps = new PrintStream(outputFile);
 			for (YetiTestArchiveModule tam: allModules) {
 				ps.println(tam.getClassName()+","+tam.getClasspath());
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		
 	}
 
 	
 	/**
 	 * Return all dirs as a path.
 	 * 
 	 * @return a path containing all dirs.
 	 */
 	public String allDirsAsPath() {
 		String alldirpaths = ".";
 		
 		for (String dir: allDirs) {
 			alldirpaths =  alldirpaths+":"+dir;
 		}
 		return alldirpaths;		
 	}
 
 	/**
 	 * Return all Jars as a path.
 	 * 
 	 * @return a path containing all jars
 	 */
 	public String allJarsAsPath() {
 		String alljarpaths = ".";
 		
 		for (String jar: allJars) {
 			alljarpaths =  alljarpaths+":"+jar;
 		}
 		return alljarpaths;		
 	}
 
 	/**
 	 * Writes the files on the command-line.
 	 */
 	public void includeAllJarsAndDirs() {
 		String alljardirpaths = ".";
 		alljardirpaths = alljardirpaths+":" /**+allDirsAsPath()+":"**/  +allJarsAsPath();
 	
 		for (YetiTestArchiveModule tam:allModules) {
 			tam.setClasspath(alljardirpaths);
 		}
 		
 	}
 
 	
 	/**
 	 * Writes the files on the command-line.
 	 */
 	public void print() {
 		for (YetiTestArchiveModule tam: allModules) {
 			System.out.println(tam);
 		}
 		System.out.println("Found "+numberOfClasses+" classes in "+numberOfJarFiles+" file(s)");
 
 		
 	}
 	
 	/**
 	 * Method which explores the directory and the jar files.
 	 * 
 	 * @throws IOException when the path does not refer to a directory.
 	 */
 	public void explore() throws IOException {
 		File f = new File (startingPath);
 		if (!f.isDirectory()) {
 			throw new IOException();
 		}
 		exploreDirectory(f);
 		
 	}
 
 	/**
 	 * Load from file.
 	 * 
 	 * @throws IOException when the file does not refer to a yexp file.
 	 */
 	public void loadFromFile(String fileName) throws IOException {
 		BufferedReader br = new BufferedReader(new FileReader (fileName));
 		String line=null;
 		while ((line=br.readLine())!=null) {
 			String []ses=line.split(",");
 			allModules.add(new YetiTestArchiveModule(ses[0], ses[1]));
 		}
 		
 	}
 
 	/**
 	 * Explore a given directory.
 	 * 
 	 * @param f the directory to explore.
 	 */
 	public void exploreDirectory(File f) {
 		allDirs.add(f.getAbsolutePath());
 		File []allFiles = f.listFiles();
 		for (File floc: allFiles) {
 			if (floc.isDirectory()) {
 				exploreDirectory(floc);
 			} else {
 				if (floc.getName().endsWith(".jar")) {
 					this.exploreJAR(floc);
 				} else if (floc.getName().endsWith(".class")) {
 					String name = floc.getName();
 					name = name.substring(0,name.length()-6);
 					YetiLog.printDebugLog("Class: "+name, this);
 					
 					allModules.add(new YetiTestArchiveModule(name,allDirsAsPath()));
 					numberOfClasses++;
 				}
 			}
 			
 		}
 	}
 	
 	/**
 	 * Explore a JAR file
 	 * 
 	 * @param jarFile the JAR file 
 	 */
 	public void exploreJAR(File jarFile) {
 		numberOfJarFiles++;
 		String classpath = jarFile.getAbsolutePath();
 		allJars.add(classpath);
 		YetiLog.printDebugLog("JAR found: "+classpath, this);
 		JarFile jf = null;
 		try {
 			// we first define a JarFile
 			jf = new JarFile(jarFile);
 			// we open the JarFile
 			Enumeration<JarEntry> jes = jf.entries();
 			// for each element, we open it and extract the name and the classpath of the file
 			for(JarEntry je=jes.nextElement();jes.hasMoreElements();je=jes.nextElement()) {
 				String name = je.getName().replace("/", ".");
 				if (name.endsWith(".class")) {
 					name = name.substring(0,name.length()-6);
 					YetiLog.printDebugLog("Class: "+name, this);
 					allModules.add(new YetiTestArchiveModule(name,classpath));
 					numberOfClasses++;
 				}
 			}
 			
 		} catch (IOException e) {
 			
 			System.err.println("Problem with file: "+jarFile.getName());
 			e.printStackTrace();
 		} finally {
 			try {
 				// we close the file
 				jf.close();
 			} catch (Exception e) {
 				// if this happens, the JarFile is already closed, we ignore it then
 			}
 		}
 		
 		
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		boolean onlyPrint = false;
 		// we check that there are only two arguments maximum
 		if ((args.length>0)&&(args.length<3)&&!(args[0].equals("-h")||args[0].equals("-help"))) {
 			String path = args[0];
 			
 			// if there are two arguments
 			if (args.length ==2) {
 				// if there is a help option
 				if (args[0].equals("-h")||args[0].equals("-help")) {
 					printHelp();
 					return;
 				}
 				if (args[1].equals("-print")) {
 					onlyPrint = true;
 				} else {
 					outputFile = args[1];
 				}
 			}
 			
 			// we perform the exploration
 			YetiTestArchiveExplorer ae = new YetiTestArchiveExplorer(path);
 			try {
 				ae.explore();
 				ae.includeAllJarsAndDirs();
 				if (onlyPrint)
 					ae.print();
 				else
 					ae.writeFile();
 			} catch (IOException e) {
 				System.err.println("The starting path is not a directory.");
 				printHelp();
 			}
 			return;
 		}
 		printHelp();
 		
 	}
 
 
 
 	/**
 	 * 
 	 * Prints a simple help.
 	 * 
 	 */
 	private static void printHelp() {
		System.out.print("Usage:\n  java yeti.experimenter.YetiTestArchiveExplorer path [output_file | -print]");
 	}
 
 }
