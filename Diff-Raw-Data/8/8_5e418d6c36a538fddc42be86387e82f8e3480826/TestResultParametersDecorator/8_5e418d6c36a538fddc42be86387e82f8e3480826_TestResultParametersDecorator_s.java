 /*******************************************************************************
  * Copyright (c) 2004, 2011 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.provider.labelprovider.decorators;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jface.viewers.IDecoration;
 import org.eclipse.jubula.client.core.model.TestResultNode;
 import org.eclipse.jubula.client.core.model.TestResultParameter;
 
 /**
  * @author BREDEX GmbH
  * @created Oct 27, 2011
  */
 public class TestResultParametersDecorator extends
         AbstractLightweightLabelDecorator {
 
     /** separator for Parameter values */
     private static final String SEPARATOR = ", "; //$NON-NLS-1$
     
     /** length of Parameter value separator string */
     private static final int SEPARATOR_LEN = SEPARATOR.length();
     
     /**
      * 
      * {@inheritDoc}
      */
     public void decorate(Object element, IDecoration decoration) {
         if (element instanceof TestResultNode) {
             TestResultNode testResult = (TestResultNode)element;
             StringBuilder paramValueBuilder = new StringBuilder();
            for (TestResultParameter parameter : testResult.getParameters()) {
                 paramValueBuilder
                     .append(StringUtils.defaultString(parameter.getValue()))
                     .append(SEPARATOR);
             }
             if (paramValueBuilder.length() > 0) {
                 int builderLength = paramValueBuilder.length();
                 paramValueBuilder.delete(
                         builderLength - SEPARATOR_LEN, builderLength);
                 paramValueBuilder.insert(0, " ["); //$NON-NLS-1$
                 paramValueBuilder.append("]"); //$NON-NLS-1$
                 decoration.addSuffix(paramValueBuilder.toString());
             }
         }
     }
 
 }
