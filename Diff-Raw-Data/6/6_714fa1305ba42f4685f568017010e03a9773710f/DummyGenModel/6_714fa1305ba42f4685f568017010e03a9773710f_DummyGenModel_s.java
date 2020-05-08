 /*
  * Copyright (c) 2006, 2007 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.bridge.genmodel;
 
 import java.util.Collection;
 import java.util.Collections;
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.codegen.util.CodeGenUtil;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.jdt.core.JavaConventions;
 
 /**
  * @see org.eclipse.emf.codegen.ecore.Generator#run(java.lang.Object) (-ecore2GenModel key)
  * @author artem
  */
 public class DummyGenModel {
 	protected final EPackage myModel;
 	protected final Collection<EPackage> myAdditionalPackages;
 	private String myPluginID;
 
 	public DummyGenModel(EPackage primaryPackage, Collection<EPackage> additionalPackages) {
 		assert primaryPackage != null;
 		myModel = primaryPackage;
 		myAdditionalPackages = additionalPackages;
 	}
 
 	public GenModel create() {
 		GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
 		if (myAdditionalPackages == null) {
 			genModel.initialize(Collections.singleton(myModel));
 		} else {
			HashSet<EPackage> allPacks = new HashSet<EPackage>();
 			allPacks.add(myModel);
 			allPacks.addAll(myAdditionalPackages);
 			genModel.initialize(allPacks);
 		}
 		final String pluginID = getPluginID();
 		genModel.setModelName(getModelName());
 		genModel.setModelPluginID(pluginID);
 		genModel.setModelDirectory("/" + pluginID + "/src/"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		// need different prefix to avoid name collisions with code generated
 		// for domain model
 		final String basePackage = asValidPackageName(pluginID);
 		for (Iterator it = genModel.getGenPackages().iterator(); it.hasNext();) {
 			GenPackage genPackage = (GenPackage) it.next();
 			if (basePackage != null) {
 				if (basePackage.endsWith('.' + genPackage.getEcorePackage().getName())) {
 					genPackage.setBasePackage(basePackage.substring(0, basePackage.lastIndexOf('.')));
 				} else {
 					genPackage.setBasePackage(basePackage);
 				}
 			}
 			genPackage.setPrefix(constructGenPackagePrefix(genPackage));
 		}
 		return genModel;
 	}
 
 	protected String constructGenPackagePrefix(GenPackage genPackage) {
 		return CodeGenUtil.capName(genPackage.getEcorePackage().getName());
 	}
 
 	protected String getModelName() {
 		return CodeGenUtil.capName(myModel.getName());
 	}
 
 	public void setPluginID(String pluginID) {
 		myPluginID = pluginID;
 	}
 
 	private String getPluginID() {
 		if (myPluginID == null) {
 			return "org.sample." + getModelName().toLowerCase(); //$NON-NLS-1$
 		}
 		return myPluginID;
 	}
 
 	private String asValidPackageName(String pluginID) {
 		String rv = pluginID.toLowerCase();
 		final String complianceLevel = "1.3"; //$NON-NLS-1$
 		if (JavaConventions.validatePackageName(rv, complianceLevel, complianceLevel).getSeverity() != IStatus.ERROR) {
 			return rv;
 		}
 		StringBuilder sb = new StringBuilder(rv.length());
 		StringTokenizer st = new StringTokenizer(rv, "."); //$NON-NLS-1$
 		while (st.hasMoreTokens()) {
 			if (appendOnlyJavaChars(sb, st.nextToken())) {
 				sb.append('.');
 			}
 		}
 		if (sb.length() == 0) {
 			return null;
 		}
 		sb.setLength(sb.length() - 1); // strip last dot
 		if (!Character.isJavaIdentifierStart(sb.charAt(0))) {
 			sb.insert(0, 'a');
 		}
 		assert JavaConventions.validatePackageName(sb.toString(), complianceLevel, complianceLevel).isOK();
 		return sb.toString();
 	}
 
 	private static boolean appendOnlyJavaChars(StringBuilder sb, String rv) {
 		boolean added = false;
 		for (int i = 0; i < rv.length(); i++) {
 			if (Character.isJavaIdentifierPart(rv.charAt(i))) {
 				sb.append(rv.charAt(i));
 				added = true;
 			}
 		}
 		return added;
 	}
 }
