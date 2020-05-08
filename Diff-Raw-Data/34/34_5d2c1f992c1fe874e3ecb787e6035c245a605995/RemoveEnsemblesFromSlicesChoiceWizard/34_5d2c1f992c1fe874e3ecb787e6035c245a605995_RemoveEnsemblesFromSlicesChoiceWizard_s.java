 /*
  * License (BSD Style License):
  * Copyright (c) 2012
  * Software Engineering
  * Department of Computer Science
  * Technische Universitiät Darmstadt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * - Neither the name of the Software Engineering Group or Technische
  * Universität Darmstadt nor the names of its contributors may be used to
  * endorse or promote products derived from this software without specific
  * prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package de.opalproject.vespucci.ui.wizards;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 
 import de.opalproject.vespucci.datamodel.Constraint;
 import de.opalproject.vespucci.datamodel.Ensemble;
 import de.opalproject.vespucci.sliceEditor.features.dark.DarkSliceUpdateFeature;
 import de.opalproject.vespucci.ui.Activator;
 import de.opalproject.vespucci.ui.utils.EmfService;
 
 /**
  * Wizard for renaming existing ensembles
  * 
  * @author Marius-d
  * 
  */
 public class RemoveEnsemblesFromSlicesChoiceWizard extends Wizard {
 
 	/**
 	 * Page belonging to this wizard.
 	 */
 	protected RemoveEnsemblesFromSlicesChoicePage page;
 
 	private final List<Ensemble> ensembleList;
 
 	/**
 	 * Default Constructor. Create a new EnsembleWizardRename.
 	 */
 	public RemoveEnsemblesFromSlicesChoiceWizard() {
 		super();
 		ensembleList = null;
 		setNeedsProgressMonitor(true);
 	}
 
 	public RemoveEnsemblesFromSlicesChoiceWizard(List<Ensemble> ensembleList) {
 		super();
 		this.ensembleList = ensembleList;
 		setNeedsProgressMonitor(true);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.wizard.Wizard#addPages()
 	 */
 	@Override
 	public void addPages() {
 		page = new RemoveEnsemblesFromSlicesChoicePage(ensembleList);
 		addPage(page);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
 	 */
 	@Override
 	public boolean performFinish() {
 		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
 			@Override
 			protected void execute(IProgressMonitor progressMonitor) {
 				try {
 					TransactionalEditingDomain editingDomain = TransactionalEditingDomain.Registry.INSTANCE
 							.getEditingDomain("de.opalproject.vespucci.navigator.domain.DatamodelEditingDomain");
 
 					DarkSliceUpdateFeature operation = new DarkSliceUpdateFeature(
							editingDomain, ensembleList);
 					editingDomain.getCommandStack().execute(operation);
 
 					Command delete = new RecordingCommand(editingDomain) {
 
 						@Override
 						protected void doExecute() {
 							// Iterate over every ensemble which should be
 							// deleted
 							for (final Ensemble ensemble : ensembleList) {
 								// Remove every constraint which used the
 								// deleted
 								// ensemble
 								// as source or target
 								for (Constraint constraint : ensemble
 										.getConstraints()) {
 									EcoreUtil.delete(constraint);
 								}
 
 								EcoreUtil.delete(ensemble);
 							}
 						}
 					};
 
 					editingDomain.getCommandStack().execute(delete);
 					EmfService.save(editingDomain);
 
 				} catch (Exception exception) {
 					Activator
 							.getDefault()
 							.getLog()
 							.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 									Arrays.toString(exception.getStackTrace())));
 
 				} finally {
 					progressMonitor.done();
 				}
 			}
 		};
 
 		try {
 			getContainer().run(false, false, operation);
 		} catch (InvocationTargetException | InterruptedException exception) {
 			Activator
 					.getDefault()
 					.getLog()
 					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Arrays
 							.toString(exception.getStackTrace())));
 		}
 
 		return true;
 
 	}
 }
