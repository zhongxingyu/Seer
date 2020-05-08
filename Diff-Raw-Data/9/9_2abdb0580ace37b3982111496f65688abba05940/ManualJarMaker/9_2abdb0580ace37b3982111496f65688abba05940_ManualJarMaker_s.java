 package net.ayld.facade.bundle.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.JarOutputStream;
 
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.Sets;
 
 import net.ayld.facade.bundle.JarMaker;
 import net.ayld.facade.model.ClassFile;
 
 public class ManualJarMaker implements JarMaker{
 
 	private String facadeJarName = "facade.jar";
 	
 	@Override
 	public JarFile zip(Set<File> classFiles) throws IOException {
 		makeOutputDir();
 		
 		final JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(new File(facadeJarName)));
 		
 		final Set<String> dirEntries = Sets.newHashSet();
 		final Set<String> classEntries = Sets.newHashSet();
 		for (File classFile : classFiles) {
 			
 			final ClassFile clazz = ClassFile.fromFile(classFile);
 			final String simpleName = clazz.qualifiedName().shortName();
 			
			final String packages = clazz.qualifiedName().toString().replaceAll(simpleName, "");
 			final StringBuilder packageDirs = new StringBuilder();
 			
 			for (String packageName : Splitter.on(".").split(packages)) {
 				
 				if (Strings.isNullOrEmpty(packageName)) {
 					continue;
 				}
 				packageDirs.append(packageName).append(File.separator);
 				
 				if (!dirEntries.contains(packageDirs.toString())) {
 					jarOut.putNextEntry(new JarEntry(packageDirs.toString()));
 				}
 				dirEntries.add(packageDirs.toString());
 			}
 			
 			final String classEntry = packageDirs.append(classFile.getName()).toString();
 			
 			if (!classEntries.contains(classEntry)) {
 				jarOut.putNextEntry(new JarEntry(classEntry));
 				
 				final FileInputStream in = new FileInputStream(classFile);
 				
 				int len;
 				final byte[] buf = new byte[1024];
 				while ((len = in.read(buf)) > 0) {
 					jarOut.write(buf, 0, len);
 				}
 				
 				jarOut.closeEntry();
 				in.close();
 			}
 			classEntries.add(classEntry);
 		}
 		jarOut.close();
 		
 		return new JarFile(facadeJarName);
 	}
 
 	private void makeOutputDir() {
 		// ignoring result because it is ok for the parent to exist
 		new File(new File(facadeJarName).getParent()).mkdirs();
 	}
 
 	public void setZippedJarName(String zippedJarName) {
 		this.facadeJarName = zippedJarName;
 	}
 }
