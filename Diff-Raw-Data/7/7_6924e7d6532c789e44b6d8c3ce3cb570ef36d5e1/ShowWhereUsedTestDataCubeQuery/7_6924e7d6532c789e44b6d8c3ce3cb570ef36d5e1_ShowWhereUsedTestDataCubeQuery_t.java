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
 package org.eclipse.jubula.client.ui.rcp.search.query;
 
 import java.util.HashSet;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jubula.client.core.businessprocess.TestDataCubeBP;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.ITestDataCubePO;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.tools.constants.StringConstants;
 
 
 /**
  * @author BREDEX GmbH
  * @created Jul 27, 2010
  */
 public class ShowWhereUsedTestDataCubeQuery extends AbstractShowWhereUsedQuery {
     /**
      * <code>m_testDataCube</code>
      */
     private ITestDataCubePO m_testDataCube;
     
     /**
      * @param testDataCube the test data cube to use for this query
      */
     public ShowWhereUsedTestDataCubeQuery(ITestDataCubePO testDataCube) {
         super(null);
         m_testDataCube = testDataCube;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("nls")
     public String getLabel() {
         StringBuilder sb = new StringBuilder();
         sb.append(getTimestamp());
         sb.append(StringConstants.COLON);
         sb.append(StringConstants.SPACE);
         sb.append(Messages.UIJobSearchingTestDataCube);
         sb.append(" \"");
         sb.append(m_testDataCube.getName());
         sb.append("\"");
         return sb.toString();
     }
     
     /**
      * {@inheritDoc}
      */
     public IStatus run(IProgressMonitor monitor) {
        setMonitor(monitor);
         addAll(new HashSet<INodePO>(
                 TestDataCubeBP.getReuser(m_testDataCube)));
         finished();
         return Status.OK_STATUS;
     }
 
 }
