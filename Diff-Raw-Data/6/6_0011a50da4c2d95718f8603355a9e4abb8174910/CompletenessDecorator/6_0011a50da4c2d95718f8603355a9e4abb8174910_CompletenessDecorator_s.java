 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.provider.labelprovider.decorators;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.viewers.IDecoration;
 import org.eclipse.jubula.client.core.businessprocess.problems.IProblem;
 import org.eclipse.jubula.client.core.businessprocess.problems.ProblemFactory;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.provider.labelprovider.decorators.AbstractLightweightLabelDecorator;
 
 /**
  * @author BREDEX GmbH
  * @created 10.05.2005
  */
 public class CompletenessDecorator extends AbstractLightweightLabelDecorator {
     /** {@inheritDoc} */
     public void decorate(Object element, IDecoration decoration) {
         final INodePO node = (INodePO) element;
        if (ProblemFactory.hasProblem(node)) {
            IProblem worstProblem = ProblemFactory.getWorstProblem(
                    node.getProblems());
             switch (worstProblem.getStatus().getSeverity()) {
                 case IStatus.WARNING:
                     decoration.addOverlay(
                             IconConstants.WARNING_IMAGE_DESCRIPTOR);
                     break;
                 case IStatus.ERROR:
                     decoration.addOverlay(
                             IconConstants.ERROR_IMAGE_DESCRIPTOR);
                     break;
                 default:
                     break;
             }
         }
     }
 }
