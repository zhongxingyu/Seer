 /*******************************************************************************
  * Copyright (c) 2006 Eclipse.org
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package org.eclipse.gmf.internal.xpand.util;
 
 import java.io.IOException;
 import java.io.Reader;
 
 import org.eclipse.gmf.internal.xpand.Activator;
 import org.eclipse.gmf.internal.xpand.ResourceManager;
 import org.eclipse.gmf.internal.xpand.model.XpandResource;
 import org.eclipse.gmf.internal.xpand.xtend.ast.XtendResource;
 
 // FIXME it's not a good idea to parse file on every proposal computation
 public abstract class ResourceManagerImpl implements ResourceManager {
 
 	public XtendResource loadXtendResource(String fullyQualifiedName) {
 		Reader r = null;
 		try {
 			r = resolve(fullyQualifiedName, XtendResource.FILE_EXTENSION);
			if (r == null) {
				return null;
			}
 			return loadXtendResource(r, fullyQualifiedName);
 		} catch (IOException ex) {
 			Activator.logError(ex);
 		} catch (ParserException ex) {
 			handleParserException(fullyQualifiedName, ex);
 		} finally {
 			if (r != null) {
 				try {
 					r.close();
 				} catch (Exception ex) {/*IGNORE*/}
 			}
 		}
 		return null;
 	}
 
 	public XpandResource loadXpandResource(String fullyQualifiedName) {
 		Reader r = null;
 		try {
 			r = resolve(fullyQualifiedName, XpandResource.TEMPLATE_EXTENSION);
			if (r == null) {
				return null;
			}
 			return loadXpandResource(r, fullyQualifiedName);
 		} catch (IOException ex) {
 			Activator.logError(ex);
 		} catch (ParserException ex) {
 			handleParserException(fullyQualifiedName, ex);
 		} finally {
 			if (r != null) {
 				try {
 					r.close();
 				} catch (Exception ex) {/*IGNORE*/}
 			}
 		}
 		return null;
 	}
 
 	protected void handleParserException(String name, ParserException ex) {
 		Activator.logWarn(name + ":" + ex.getClass().getName());
 	}
 
 	protected abstract Reader resolve(String fullyQualifiedName, String extension) throws IOException;
 
 	protected XtendResource loadXtendResource(Reader reader, String fullyQualifiedName) throws IOException, ParserException {
 		return new XtendResourceParser().parse(reader, fullyQualifiedName);
 	}
 
 	protected XpandResource loadXpandResource(Reader reader, String fullyQualifiedName) throws IOException, ParserException {
 		return new XpandResourceParser().parse(reader, fullyQualifiedName);
 	}
 
 }
