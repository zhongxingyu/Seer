 /*******************************************************************************
  * Copyright (c) 9 jan. 2014 NetXForge.
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
 package com.netxforge.netxstudio.screens.editing.actions;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.UnexecutableCommand;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.command.CommandParameter;
 import org.eclipse.emf.edit.command.PasteFromClipboardCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.ui.action.PasteAction;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TreeViewer;
 
 /**
  * The command creation will determine if the object can be pasted into the
  * selection.
  * 
  * We override for non-executabe to paste in the eResource, which doesn't always
  * make sense.
  * 
  * @author Christophe Bouhier
  */
 public class SourceAwarePasteAction extends PasteAction {
 
 	public class PasteStrategy implements ICommandStrategy {
 
 		private Object source;
 
 		public static final int PASTE_INTO_TABLE = 100;
 		public static final int PASTE_INTO_TREE = 200;
 
 		public void source(Object source) {
 			this.source = source;
 		}
 
 		/**
 		 * Delegate to the strategy from an editing domain.
 		 * 
 		 * @param commandClass
 		 * @param commandParameter
 		 * @return
 		 */
 		public Command createCommand(EditingDomain domain,
 				Class<? extends Command> commandClass,
 				CommandParameter commandParameter, boolean optimizedCopy) {
 
 			// Check if we can execute first, if not create with the owner for a
 			// TableViewer.
 			Command cmd = new SourceAwarePasteFromClipboardCommand(domain,
 					commandParameter.getOwner(), commandParameter.getFeature(),
 					commandParameter.getIndex(), optimizedCopy);
 
 			if (!cmd.canExecute()) {
 
 				if (source instanceof StructuredViewer) {
 					if (source instanceof TableViewer) {
						EObject oOwner = (EObject) commandParameter.getOwner();
						if (oOwner.eResource() != null) {
							// Produce a regular command with a different owner,
							// being the resource.
							return new PasteFromClipboardCommand(domain,
									oOwner.eResource(),
									commandParameter.getFeature(),
									commandParameter.getIndex(), optimizedCopy);
 						}

 					} else if (source instanceof TreeViewer) {
						System.out.println("Paste strategy for TreeViewer ");

 					}
 				}
 			}
 			return cmd;
 		}
 	}
 
 	/**
 	 * A Command creations strategy.
 	 */
 	private PasteStrategy strategy = new PasteStrategy();
 
 	public void updateSource(Object o) {
 		strategy.source(o);
 	}
 
 	@Override
 	public Command createCommand(Collection<?> selection) {
 
 		if (selection.size() == 1) {
 			return SourceAwarePasteFromClipboardCommand.create(domain,
 					selection.iterator().next(), null, strategy);
 		} else {
 			return UnexecutableCommand.INSTANCE;
 		}
 
 	}
 }
