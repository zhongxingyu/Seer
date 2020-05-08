 /*******************************************************************************
  * Copyright (c) 28 nov. 2012 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.screens.editing;
 
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.command.CommandParameter;
 import org.eclipse.emf.edit.command.CreateChildCommand;
 import org.eclipse.emf.edit.command.PasteFromClipboardCommand;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 
 import com.netxforge.netxstudio.edit.CreateChildFromPoolCommand;
 import com.netxforge.netxstudio.library.NodeType;
 import com.netxforge.netxstudio.operators.Node;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.screens.editing.actions.WarningDeleteCommand;
 import com.netxforge.netxstudio.screens.editing.actions.WarningNWBDeleteCommand;
 
 /**
  * Customized version of the standard a.f. editing domain. The following
  * commands are add or customized:
  * <ul>
  * <li>{@link WarningDeleteCommand} => Warns which objects in the containment
  * hierarchy will be deleted and which references will be impacted</li>
  * <li>{@link CreateChildCommand} => Changes the default behaviour for certain
  * object types.</li>
  * <li>{@link PasteFromClipboardCommand} => Supports pasting in a TableViewer.</li>
  * </ul>
  * 
  * @author Christophe Bouhier
  * 
  */
 public class ScreensAdapterFactoryEditingDomain extends
 		AdapterFactoryEditingDomain {
 
 	public ScreensAdapterFactoryEditingDomain(AdapterFactory adapterFactory,
 			CommandStack commandStack) {
 		super(adapterFactory, commandStack);
 	}
 
 	@Override
 	public Command createCommand(Class<? extends Command> commandClass,
 			CommandParameter commandParameter) {
 
 		Object owner = commandParameter.getOwner();
 
 		// SPECIALIZED WARNING COMMAND.
 		if (commandClass == WarningDeleteCommand.class) {
 			return new WarningDeleteCommand(this,
 					commandParameter.getCollection());
 		}
 		// SPECIALIZED WARNING DELETE WITH NO UNDO, DOMAIN is DISCARDED IN THIS
 		// COMMAND.
 		if (commandClass == WarningNWBDeleteCommand.class) {
 			return new WarningNWBDeleteCommand(commandParameter.getCollection());
 		}
 
 		// SPECIALIED POOL COMMAND. (NOT USED).
 		else if (owner != null
 				&& commandClass == CreateChildFromPoolCommand.class) {
 			// If there is an adapter of the correct type...
 			//
 			IEditingDomainItemProvider editingDomainItemProvider = (IEditingDomainItemProvider) adapterFactory
 					.adapt(owner, IEditingDomainItemProvider.class);
 
 			return editingDomainItemProvider != null ? editingDomainItemProvider
 					.createCommand(owner, this, commandClass, commandParameter)
 					: new ItemProviderAdapter(null).createCommand(owner, this,
 							commandClass, commandParameter);
 		}
 		// SPECIALIZED CREATION OF EQUIPMENT UNDER A NODE WHICH IS ADDED TO A
 		// NODETYPE.
 		else if (owner != null && owner instanceof Node
 				&& commandClass == CreateChildCommand.class) {
 
 			Node n = (Node) owner;
 
 			// A creation command for Node, should be faked as for Node Type.
 			if (n.eIsSet(OperatorsPackage.Literals.NODE__NODE_TYPE)) {
 				NodeType nt = n.getNodeType();
 
 				IEditingDomainItemProvider editingDomainItemProvider = (IEditingDomainItemProvider) adapterFactory
 						.adapt(nt, IEditingDomainItemProvider.class);
 				commandParameter.setOwner(nt);
 				return editingDomainItemProvider != null ? editingDomainItemProvider
 						.createCommand(nt, this, commandClass, commandParameter)
 						: new ItemProviderAdapter(null).createCommand(nt, this,
 								commandClass, commandParameter);
 			}
 
 		}
 
 		Command nativeCommand = super.createCommand(commandClass,
 				commandParameter);
 
 		// SPECIALED PASTE COMMAND TO PASTE INTO TABLES.
 		// For the paste command, we like to paste into the parent resource for
 		// flat views
 		// like tables.
 		// FIXME, The ugly thing is that the selection is still the copied
 		// object buh...
		if (commandClass == PasteFromClipboardCommand.class
				&& !nativeCommand.canExecute()) {
 			if (commandParameter.getOwner() instanceof CDOObject) {
 				CDOObject oOwner = (CDOObject) commandParameter.getOwner();
 				if (oOwner.eContainer() != null) {
 					// Is it contained, we shoudn't touch this creation.
 				}
 
 				if (oOwner.eResource() != null) {
 					return new PasteFromClipboardCommand(this,
 							oOwner.eResource(), commandParameter.getFeature(),
 							commandParameter.getIndex(), getOptimizeCopy());
 				}
 			}
 		}
 
 		return nativeCommand;
 	}
 }
