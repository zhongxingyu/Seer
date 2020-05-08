 /*******************************************************************************
  * Copyright (c) 2009 The Eclipse Foundation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.examples.slideshow.jdt;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringBufferInputStream;
 import java.io.StringReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownServiceException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.ToolFactory;
 import org.eclipse.jdt.core.formatter.CodeFormatter;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.text.edits.MalformedTreeException;
 import org.eclipse.text.edits.TextEdit;
 import org.osgi.service.url.AbstractURLStreamHandlerService;
 
 public class JavaSourceStreamHandlerService extends AbstractURLStreamHandlerService {
 	
 	public static final String PROTOCOL = "java";
 
 	@Override
 	public URLConnection openConnection(URL u) throws IOException {
 		return new URLConnection(u) {
 			private Map<String,List<String>> headers;
 
 			@Override
 			public void connect() throws IOException {
 				headers = new HashMap<String, List<String>>();
				// TODO Figure out the right MIME Type.
				headers.put("content-type", Collections.singletonList("x-application/java"));
 			}
 			
 			@Override
 			public Map<String, List<String>> getHeaderFields() {
 				return headers;
 			}
 			
 			@Override
 			public Object getContent(Class[] classes) throws IOException {
 				for(int index=0;index<classes.length;index++) {
 					if (classes[index] == String.class) return getContent();
 				}
 				throw new UnknownServiceException();
 			}
 			
 			// TODO This isn't in the right place. At least I don't think it is.
 			// TODO Returning error messages intended for the end user in the exception is convenient, but is it correct?
 			// TODO Probably better to return a Document containing formatting
 			@Override
 			public InputStream getInputStream() throws IOException {
 				return new StringBufferInputStream(getSource());
 			}
 
 			private String getSource() throws IOException {
 				String projectName = getURL().getHost();
 				if (projectName.length() == 0) throw new IOException("Project name must be provided!");
 				
 				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 				if (!project.exists()) throw new IOException(String.format("Project \"%1$s\"not found!", projectName));
 				
 				IJavaProject javaProject = JavaCore.create(project);
 				if (!javaProject.exists()) throw new IOException(String.format("Project \"%1$s\" is not a Java Project!", projectName));
 				
 				IType type = null;
 				// The path will include a preceding slash; we strip it.
 				if (getURL().getPath().length() <= 1) throw new IOException("Type name must be provided!");
 				String typeName = getURL().getPath().substring(1);
 				try {
 					type = javaProject.findType(typeName);
 				} catch (JavaModelException e) {
 					// We've covered off most of the potential problems spots, this shouldn't happen.
 					throw new IOException(e.getMessage());
 				}
 				if (type == null || !type.exists()) throw new IOException(String.format("Type \"%1$s\" not found!", typeName));
 				
 				if (getURL().getRef() == null) 
 				try {
 					return type.getSource();
 				} catch (JavaModelException e) {
 					throw new IOException(e.getMessage());
 				}
 				
 				// separate the method name from the parameters.
 				Matcher matcher = Pattern.compile("([^\\(]*)\\((.*)\\)").matcher(getURL().getRef());
 				
 				if (!matcher.matches()) {
 					throw new IOException(String.format("Method format, \"%1$s\", incorrect, method([parameter [, parameter*]]) expected!", getURL().getRef()));
 				}
 				
 				String methodName = matcher.group(1);
 				
 				// Splitting returns an array containing a single empty string if the receiver is empty. See Bug 272381.
 				String allParameters = matcher.group(2);
 				String[] parameters = allParameters.trim().isEmpty() ? new String[0] : allParameters.split(",");
 	
 				// TODO permit user to enter user-sensible parameters, e.g. "String[]" instead of "[QString;"
 				IMethod method = type.getMethod(methodName, parameters);
 				if (!method.exists()) throw new IOException(String.format("Method \"%1$s\" not found!", methodName));
 				try {
 					return getSourceCodeFor(method);
 				} catch (JavaModelException e) {
 					throw new IOException(e.getMessage());
 				} catch (MalformedTreeException e) {
 					throw new IOException(e.getMessage());
 				} catch (BadLocationException e) {
 					throw new IOException(e.getMessage());
 				}
 			}
 
 			private String getSourceCodeFor(IMethod method)
 					throws JavaModelException, BadLocationException {
 				String source = method.getSource();
 				TextEdit format = ToolFactory.createCodeFormatter(null).format(CodeFormatter.K_UNKNOWN + CodeFormatter.F_INCLUDE_COMMENTS, source, 0, source.length(), 0, null);
 				Document document = new Document();
 				document.set(source);
 				format.apply(document);
 				return document.get();
 			}			
 		};
 	}
 
 }
