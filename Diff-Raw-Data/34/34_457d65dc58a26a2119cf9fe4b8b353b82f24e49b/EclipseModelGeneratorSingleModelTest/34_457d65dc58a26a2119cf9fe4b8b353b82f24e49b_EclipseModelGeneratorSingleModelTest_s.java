 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: EclipseModelGeneratorSingleModelTest.java,v 1.31 2011/10/25 22:48:55 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.modelgenerator.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.texo.modelgenerator.test.models.TestModel;
 
 /**
  * Runs {@link EclipseModelGeneratorTest} for a single model file.
  * 
  * @see TestModel
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.31 $
  */
 
 public class EclipseModelGeneratorSingleModelTest extends EclipseModelGeneratorTest {
 
   @Override
   protected List<String> getModelFileRelativePaths() {
     final List<String> modelFiles = new ArrayList<String>();
     //    modelFiles.add("issues/bz369476.ecore"); //$NON-NLS-1$
     //    modelFiles.add("samples/extlibrary.ecore"); //$NON-NLS-1$
     //    modelFiles.add("samples/library.ecore"); //$NON-NLS-1$
     //    modelFiles.add("issues/bz371509.ecore"); //$NON-NLS-1$
     //    modelFiles.add("samples/schoollibrary.ecore"); //$NON-NLS-1$
     //    modelFiles.add("issues/bz331009.ecore"); //$NON-NLS-1$
     //        modelFiles.add("texo/TexoDataRequest.xsd"); //$NON-NLS-1$
     //    modelFiles.add("issues/bz379815.ecore"); //$NON-NLS-1$
     //    modelFiles.add("samples/jpamixed.ecore"); //$NON-NLS-1$
     modelFiles.add("samples/kdm.ecore"); //$NON-NLS-1$
     //    modelFiles.add("schemaconstructs/ListUnion.xsd"); //$NON-NLS-1$
     return modelFiles;
   }
 
   @Override
   protected boolean useSharedEPackageRegistry() {
     return true;
   }
 }
