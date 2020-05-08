 package com.venky.core.util.pkg;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Stack;
 
 public class DirectoryIntrospector extends PackageIntrospector{
 	private final File root ; 
 	public DirectoryIntrospector(File root){
 		this.root = root;
 	}
 
 	@Override
 	public List<String> getClasses(String path) {
         Stack<File> sFiles = new Stack<File>();
         sFiles.push(root);
         List<String> classes = new ArrayList<String>();
         while (!sFiles.isEmpty()){
             File f = sFiles.pop();
             String pathRelativeToRoot = f.getPath().length() > root.getPath().length() ? 
                     f.getPath().substring(root.getPath().length()+1) : "";
             
            pathRelativeToRoot = ((File.separatorChar != '/') ? pathRelativeToRoot.replace(File.separatorChar, '/') : pathRelativeToRoot) ;
             
             if (f.isDirectory()){
                 sFiles.addAll(Arrays.asList(f.listFiles()));
             }else if (pathRelativeToRoot.startsWith(path)){
                 addClassName(classes, pathRelativeToRoot);
             }
         }
         return classes;
 	}
 	
 }
