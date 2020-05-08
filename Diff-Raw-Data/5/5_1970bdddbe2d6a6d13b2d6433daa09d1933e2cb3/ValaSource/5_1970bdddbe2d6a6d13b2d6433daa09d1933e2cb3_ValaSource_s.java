 /**
  * Copyright (C) 2008  Andrew Flegg <andrew@bleb.org>
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package valable.model;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Status;
 
 /**
  * Encapsulate information about a Vala source file. A source file can
  * consist of a number of types.
  */
 public class ValaSource {
 	private static final Pattern USING = Pattern.compile("^\\s*using (\\S+)\\s*;\\s*$");
 	
 	private final ValaProject     project;
 	private final IFile           source;
 	private final Set<ValaPackage> uses = new LinkedHashSet<ValaPackage>();
 	private final Map<String, ValaType> types = new HashMap<String, ValaType>();
 	
 	
 	/**
 	 * Create a new instance for the given source file within a 
 	 * project.
 	 * 
 	 * @param source
 	 */
 	public ValaSource(ValaProject project, IFile source) {
 		super();
 		
 		if (!(source.getFileExtension().equals("vala") || source
 				.getFileExtension().equals("vapi")))
 			throw new IllegalArgumentException("Only .vala / .vapi files can be represented");
 
 		this.project = project;
 		this.source  = source;
 		this.project.getSources().add(this);
 	}
 	
 	
 	/**
 	 * Parse the source file to build up a tree of types, uses etc.
 	 * 
 	 * <p>Current implementation is a mix of file parsing and {@code ctags}:
 	 * in future this should use an <acronym title="Abstract Source Tree">AST</a>.
 	 * </p>
 	 * 
 	 * @throws CoreException if the file is unparseable 
 	 */
 	public List<String> parse() throws CoreException {
 		uses.clear();
 		types.clear();
 		
 		// -- Find usings...
 		//
 		InputStream contents;
 		try {
 			contents = source.getContents();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		Scanner      scanner = new Scanner(contents);
 		List<String> lines   = new ArrayList<String>(); 
 		while (scanner.hasNextLine()) {
 			String  line    = scanner.nextLine();
 			Matcher matcher = USING.matcher(line);
 
 			lines.add(line);
 			if (matcher.matches()) {
 				Set<ValaPackage> pkgs = ValaProject.getAvailablePackages().get(matcher.group(1));
 				if (pkgs != null)
 					uses.addAll(pkgs);
 			}
 		}
 		scanner.close();
 		
 		// -- Parse types...
 		//
 		Process output = null;
 		try {
 			output = Runtime.getRuntime().exec(
 					new String[] { "anjuta-tags", "-f", "-", "-n", "--sort=no",
 							"--fields=akifSt",
 							source.getLocation().toOSString() });
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new CoreException(Status.CANCEL_STATUS);
 		}
 		
 		System.out.println("\n\n+++ ctags for " + source);
 		scanner = new Scanner(output.getInputStream());
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 			String[] cols = line.split("\\t");
 			System.out.println(line);
 			
 			// -- Parse the ctags output...
 			//
 			String name = cols[0];
			//String file = cols[1];
			int    lineNumber = Integer.parseInt(cols[2].replaceAll("\\D", ""));
 			char   type = cols[3].charAt(0);
 			
 			// Use a map for extra data in the form 'key:value'
 			Map<String, String> extraData = new HashMap<String, String>();
 			for (int i = 4; i < cols.length; i++) {
 				String[] keyValue = cols[i].split(":", 2);
 				extraData.put(keyValue[0], keyValue[1]);
 			}
 			
 			// -- Build up the model...
 			//
 			SourceReference sourceRef = new SourceReference(lineNumber, 0);
 			if (type == 'c') {
 				// TODO What about private classes?
 				ValaType typeDefn = project.getType(name);
 				typeDefn.setSourceReference(sourceRef);
 				typeDefn.reset();
 				
 				if (extraData.containsKey("inherits"))
 					for (String superType : extraData.get("inherits").split(",\\s*"))
 						typeDefn.getInherits().add(project.getType(superType));
 				
 				types.put(name, typeDefn);
 			
 			} else if (type == 's') {
 				// TODO What about private struct?
 				ValaType typeDefn = project.getType(name);
 				typeDefn.setSourceReference(sourceRef);
 				typeDefn.reset();
 				
 				if (extraData.containsKey("inherits"))
 					for (String superType : extraData.get("inherits").split(",\\s*"))
 						typeDefn.getInherits().add(project.getType(superType));
 				
 				types.put(name, typeDefn);
 				
 			} else if (type == 'f') {
 				ValaType  typeDefn  = findTypeForLine(lineNumber);
 				ValaField fieldDefn = new ValaField(name);
 				fieldDefn.setSourceReference(sourceRef);
 				fieldDefn.getModifiers().addAll(Arrays.asList(extraData.get("access").split(",\\s*")));
 				fieldDefn.setType(typeFromLine(lines.get(lineNumber - 1), name));
 				
 				// typeDefn may be null
 				if (typeDefn != null)
 					typeDefn.getFields().add(fieldDefn);
 				
 			} else if (type == 'm' && extraData.get("access") != null) {
 				// Method invocations also included unless we check for "access:"
 				ValaType   typeDefn   = findTypeForLine(lineNumber);
 				ValaMethod methodDefn = new ValaMethod(name);
 				methodDefn.setSourceReference(sourceRef);
 				methodDefn.getModifiers().addAll(Arrays.asList(extraData.get("access").split(",\\s*")));
 				methodDefn.setType(typeFromLine(lines.get(lineNumber - 1), name));
 				// TODO Signature
 				
 				// typeDefn may be null
 				if (typeDefn != null)
 					typeDefn.getMethods().add(methodDefn);
 				
 			} else if (type == 'l') {
 				ValaType   typeDefn   = findTypeForLine(lineNumber);
 				ValaMethod methodDefn = typeDefn.findMethodForLine(lineNumber);
 				ValaLocalVariable varDefn = new ValaLocalVariable(name);
 				varDefn.setSourceReference(sourceRef);
 				varDefn.setType(typeFromLine(lines.get(lineNumber - 1), name));
 				methodDefn.getLocalVariables().add(varDefn);
 			}
 		}
 		scanner.close();
 		
 		return lines;
 	}
 
 
 	/**
 	 * Find the type which is being created for the given line number.
 	 * Loops over all {@link #types} and finds the one where
 	 * <var>lineNumber</var> > {@link SourceReference#getLine()}
 	 * and <var>lineNumber</var> < {@link SourceReference#getLine()} for
 	 * the next class.
 	 * 
 	 * @param lineNumber
 	 * @return ValaType enclosing the given line number.
 	 */
 	public ValaType findTypeForLine(int lineNumber) {
 		SortedSet<ValaType> sortedTypes = new TreeSet<ValaType>(ValaEntity.SOURCE_ORDER);
 		sortedTypes.addAll(types.values());
 
 		ValaType lastType = null;
 		for (ValaType type : sortedTypes) {
 			if (lastType != null && lineNumber >= lastType.getSourceReference().getLine() &&
 					lineNumber < type.getSourceReference().getLine())
 				return lastType;
 			
 			lastType = type;
 		}
 		
 		return lastType;
 	}
 	
 	
 	/**
 	 * Find the declaration of <var>name</var> on the given line and
 	 * return the immediately preceding identifier, assuming that this
 	 * will be the type.
 	 * 
 	 * <p>This is obviously prone to error, but may work in the majority
 	 * of cases until a {@code libvala} wrapper is available.</p>
 	 * 
 	 * @param line
 	 * @param name
 	 * @return
 	 */
 	private String typeFromLine(String line, String name) {
 		Pattern pattern = Pattern.compile(".*?\\b(" + ValaEntity.IDENTIFIER.pattern() + ")\\s+" + name + "\\b.*");
 		Matcher matcher = pattern.matcher(line);
 		
 		return matcher.matches() ? matcher.group(1) : "";
 	}
 
 
 	/**
 	 * @return the source
 	 */
 	public IFile getSource() {
 		return source;
 	}
 
 
 	/**
 	 * @return the types
 	 */
 	public Map<String, ValaType> getTypes() {
 		return types;
 	}
 
 	public Set<ValaPackage> getUses() {
 		return uses;
 	}
 	
 	public ValaPackage getUse(String name) {
 		for (ValaPackage use : getUses()) {
 			if (use.getName().equals(name)) {
 				return use;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public final boolean equals(Object arg) {
 		if (arg == null || !(arg instanceof ValaSource))
 			return false;
 		
 		ValaSource other = (ValaSource)arg;
 		return source.getName().equals(other.source.getName());
 	}
 	
 	
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return source.getName().hashCode();
 	}
 
 	
 	
 	/**
 	 * Encode information on an entities source location.
 	 */
 	public class SourceReference {
 		private final int line;
 		private final int column;
 		
 		/**
 		 * Create a new reference to the given source position
 		 * in this {@link ValaSource}.
 		 * 
 		 * @param line Line number, starts at 1.
 		 * @param column Column number, starts at 1.
 		 */
 		public SourceReference(int line, int column) {
 			super();
 			this.line = line;
 			this.column = column;
 		}
 
 		/**
 		 * @return the column
 		 */
 		public int getColumn() {
 			return column;
 		}
 
 		/**
 		 * @return the line
 		 */
 		public int getLine() {
 			return line;
 		}
 		
 		
 		/**
 		 * @return the source file containing this entity.
 		 */
 		public ValaSource getSource() {
 			return ValaSource.this;
 		}
 		
 		
 		/**
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public final boolean equals(Object arg) {
 			if (arg == null || !(arg instanceof SourceReference))
 				return false;
 			
 			SourceReference other = (SourceReference)arg;
 			return line == other.line && column == other.column && getSource().equals(other.getSource());
 		}
 		
 		
 		/**
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			return line * 1000 + column + ValaSource.this.hashCode();
 		}
 	}
 }
