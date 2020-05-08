 /***********************************************************************************************************************
  *
  * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  *
  **********************************************************************************************************************/
 package eu.stratosphere.sopremo.query;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import eu.stratosphere.nephele.util.StringUtils;
 import eu.stratosphere.sopremo.AbstractSopremoType;
 import eu.stratosphere.sopremo.ISopremoType;
 import eu.stratosphere.sopremo.io.SopremoFormat;
 import eu.stratosphere.sopremo.operator.Internal;
 import eu.stratosphere.sopremo.operator.Operator;
 import eu.stratosphere.sopremo.packages.BuiltinProvider;
 import eu.stratosphere.sopremo.packages.ConstantRegistryCallback;
 import eu.stratosphere.sopremo.packages.DefaultConstantRegistry;
 import eu.stratosphere.sopremo.packages.DefaultFunctionRegistry;
 import eu.stratosphere.sopremo.packages.DefaultTypeRegistry;
 import eu.stratosphere.sopremo.packages.IConstantRegistry;
 import eu.stratosphere.sopremo.packages.IFunctionRegistry;
 import eu.stratosphere.sopremo.packages.ITypeRegistry;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.util.reflect.ReflectUtil;
 
 /**
  * @author Arvid Heise
  */
 public class PackageInfo extends AbstractSopremoType implements ISopremoType, ParsingScope {
 	private final transient PackageClassLoader classLoader;
 
 	/**
 	 * Initializes PackageInfo.
 	 * 
 	 * @param packageName
 	 * @param packagePath
 	 */
 	public PackageInfo(String packageName, ClassLoader classLoader, NameChooserProvider nameChooserProvider) {
 		this.packageName = packageName;
 		this.classLoader = new PackageClassLoader(classLoader);
 		this.operatorRegistry = new DefaultConfObjectRegistry<Operator<?>>(
 			nameChooserProvider.getOperatorNameChooser(), nameChooserProvider.getPropertyNameChooser());
 		this.fileFormatRegistry = new DefaultConfObjectRegistry<SopremoFormat>(
 			nameChooserProvider.getFormatNameChooser(), nameChooserProvider.getPropertyNameChooser());
 		this.constantRegistry = new DefaultConstantRegistry(nameChooserProvider.getConstantNameChooser());
 		this.functionRegistry = new DefaultFunctionRegistry(nameChooserProvider.getFunctionNameChooser());
 		this.typeRegistry = new DefaultTypeRegistry(nameChooserProvider.getTypeNameChooser());
 	}
 
 	/**
 	 * Initialiszes PackageInfo.
 	 * 
 	 * @param packageName2
 	 */
 	public PackageInfo(String packageName, NameChooserProvider nameChooserProvider) {
 		this(packageName, new PackageClassLoader(ClassLoader.getSystemClassLoader()), nameChooserProvider);
 	}
 
 	private final IConfObjectRegistry<Operator<?>> operatorRegistry;
 
 	private final IConfObjectRegistry<SopremoFormat> fileFormatRegistry;
 
 	private final IConstantRegistry constantRegistry;
 
 	private final IFunctionRegistry functionRegistry;
 
 	private final ITypeRegistry typeRegistry;
 
 	private String packageName;
 
 	private File packagePath;
 
 	public String getPackageName() {
 		return this.packageName;
 	}
 
 	public File getPackagePath() {
 		return this.packagePath;
 	}
 
 	public List<File> getRequiredJarPaths() {
 		return this.classLoader.getFiles();
 	}
 
 	/**
 	 * Returns the typeRegistry.
 	 * 
 	 * @return the typeRegistry
 	 */
 	@Override
 	public ITypeRegistry getTypeRegistry() {
 		return this.typeRegistry;
 	}
 
 	/**
 	 * Returns the fileFormatRegistry.
 	 * 
 	 * @return the fileFormatRegistry
 	 */
 	@Override
 	public IConfObjectRegistry<SopremoFormat> getFileFormatRegistry() {
 		return this.fileFormatRegistry;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void importClass(String className) {
 		Class<?> clazz;
 		try {
 			clazz = this.classLoader.loadClass(className);
 			if(clazz.getAnnotation(Internal.class) != null)
 				return;
 			if (Operator.class.isAssignableFrom(clazz) && (clazz.getModifiers() & Modifier.ABSTRACT) == 0) {
 				clazz = Class.forName(className, true, this.classLoader);
 				QueryUtil.LOG.trace("adding operator " + clazz);
 				this.getOperatorRegistry().put((Class<? extends Operator<?>>) clazz);
 			} else if (SopremoFormat.class.isAssignableFrom(clazz)
 				&& (clazz.getModifiers() & Modifier.ABSTRACT) == 0) {
 				clazz = Class.forName(className, true, this.classLoader);
 				QueryUtil.LOG.trace("adding operator " + clazz);
 				this.getFileFormatRegistry().put((Class<? extends SopremoFormat>) clazz);
 			} else if (BuiltinProvider.class.isAssignableFrom(clazz)) {
 				clazz = Class.forName(className, true, this.classLoader);
 				this.addFunctionsAndConstants(clazz);
 			} else if (IJsonNode.class.isAssignableFrom(clazz))
 				this.getTypeRegistry().put((Class<? extends IJsonNode>) clazz);
		} catch (Exception e) {
 			QueryUtil.LOG.warn("could not load operator " + className + ": " + StringUtils.stringifyException(e));
		} 
 	}
 
 	public void importFromProject() {
 		Queue<File> directories = new LinkedList<File>();
 		directories.add(this.packagePath);
 		while (!directories.isEmpty())
 			for (File file : directories.poll().listFiles())
 				if (file.isDirectory())
 					directories.add(file);
 				else if (file.getName().endsWith(".class") && !file.getName().contains("$"))
 					this.importFromFile(file, this.packagePath);
 	}
 
 	private void importFromFile(File file, File packagePath) {
 		String classFileName = file.getAbsolutePath().substring(packagePath.getAbsolutePath().length() + 1);
 		String className = classFileName.replaceAll(".class$", "").replaceAll("/|\\\\", ".").replaceAll("^\\.", "");
 		this.importClass(className);
 	}
 
 	private void addFunctionsAndConstants(Class<?> clazz) {
 		this.getFunctionRegistry().put(clazz);
 		if (ConstantRegistryCallback.class.isAssignableFrom(clazz))
 			((ConstantRegistryCallback) ReflectUtil.newInstance(clazz)).registerConstants(this.getConstantRegistry());
 	}
 
 	public void importFromJar(File jarFileLocation) throws IOException {
 		final JarFile jarFile = new JarFile(jarFileLocation);
 		try {
 			this.classLoader.addJar(jarFileLocation);
 
 			Enumeration<JarEntry> entries = jarFile.entries();
 			while (entries.hasMoreElements()) {
 				JarEntry jarEntry = entries.nextElement();
 				final String entryName = jarEntry.getName();
 				if (entryName.endsWith(".class") && !entryName.contains("$")) {
 					String className =
 						entryName.replaceAll(".class$", "").replaceAll("/|\\\\", ".").replaceAll("^\\.", "");
 					this.importClass(className);
 				}
 			}
 		} finally {
 			jarFile.close();
 		}
 	}
 
 	@Override
 	public void appendAsString(Appendable appendable) throws IOException {
 		appendable.append("Package ").append(this.packageName);
 		appendable.append("\n  ");
 		this.operatorRegistry.appendAsString(appendable);
 		appendable.append("\n  ");
 		this.functionRegistry.appendAsString(appendable);
 		appendable.append("\n  ");
 		this.constantRegistry.appendAsString(appendable);
 		appendable.append("\n  ");
 		this.fileFormatRegistry.appendAsString(appendable);
 	}
 
 	@Override
 	public IConfObjectRegistry<Operator<?>> getOperatorRegistry() {
 		return this.operatorRegistry;
 	}
 
 	@Override
 	public IConstantRegistry getConstantRegistry() {
 		return this.constantRegistry;
 	}
 
 	@Override
 	public IFunctionRegistry getFunctionRegistry() {
 		return this.functionRegistry;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return this.getPackageName();
 	}
 
 	/**
 	 * @param packageName
 	 * @param packagePath
 	 */
 	public void importFrom(File packagePath, String packageName) throws IOException {
 		this.packagePath = packagePath.getAbsoluteFile();
 		if (packagePath.getName().endsWith(".jar"))
 			this.importFromJar(this.packagePath);
 		// FIXME hack for testing in eclipse
 		else {
 			File jarFileInParentDirectory = getJarFileInParentDirectory(packagePath, packageName);
 			if (jarFileInParentDirectory.exists()) {
 				this.packagePath = jarFileInParentDirectory;
 				this.importFromJar(jarFileInParentDirectory);
 			} else
 				throw new IllegalStateException("Cannot import non-jar " + packagePath);
 		}
 	}
 
 	private File getJarFileInParentDirectory(File packagePath, final String packageName) {
 		final File[] jarsInParentDir = packagePath.getParentFile().listFiles(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return new File(dir, name).isFile() &&
 					name.toLowerCase().endsWith(".jar") &&
 					name.contains(PackageManager.getJarFileNameForPackageName(packageName));
 			}
 		});
 		return jarsInParentDir.length == 0 ? null : jarsInParentDir[0];
 	}
 }
