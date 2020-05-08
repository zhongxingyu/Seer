 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.behavior;
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.ui.behavior.commands.PopulateDiagramCommand;
 import org.eclipse.etrice.ui.common.DiagramAccessBase;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 
 public class DiagramAccess extends DiagramAccessBase {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.etrice.ui.common.DiagramAccessBase#getDiagramName(org.eclipse.etrice.core.room.StructureClass)
 	 */
 	@Override
 	protected String getDiagramName(StructureClass sc) {
 		return "Behavior of "+sc.getName();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.etrice.ui.common.DiagramAccessBase#getDiagramTypeId()
 	 */
 	@Override
 	protected String getDiagramTypeId() {
 		return "room.behavior";
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.etrice.ui.common.DiagramAccessBase#getEditorId()
 	 */
 	@Override
 	protected String getEditorId() {
		return "BehaviorEditor.BEHAVIOR_EDITOR_ID";
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.etrice.ui.common.DiagramAccessBase#getFileExtension()
 	 */
 	@Override
 	protected String getFileExtension() {
 		return ".behavior";
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.etrice.ui.common.DiagramAccessBase#getInitialCommand(org.eclipse.etrice.core.room.StructureClass, org.eclipse.graphiti.mm.pictograms.Diagram, org.eclipse.emf.transaction.TransactionalEditingDomain)
 	 */
 	@Override
 	protected Command getInitialCommand(StructureClass ac, Diagram diagram,
 			TransactionalEditingDomain editingDomain) {
 		return new PopulateDiagramCommand(diagram, (ActorClass) ac, editingDomain);
 	}
 }
