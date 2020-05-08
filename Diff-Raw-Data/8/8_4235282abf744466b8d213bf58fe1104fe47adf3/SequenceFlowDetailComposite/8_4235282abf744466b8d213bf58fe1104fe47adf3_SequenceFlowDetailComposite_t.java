 /******************************************************************************* 
  * Copyright (c) 2011, 2012 Red Hat, Inc. 
  *  All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  *
  * @author Innar Made
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.ui.property.connectors;
 
 import org.eclipse.bpmn2.Expression;
 import org.eclipse.bpmn2.FlowNode;
 import org.eclipse.bpmn2.FormalExpression;
 import org.eclipse.bpmn2.SequenceFlow;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractBpmn2PropertySection;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractDetailComposite;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractPropertiesProvider;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.PropertiesCompositeFactory;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 
 public class SequenceFlowDetailComposite extends AbstractDetailComposite {
 
 	private Button addRemoveConditionButton;
 	private Button setDefaultFlowCheckbox;
 	
 	public SequenceFlowDetailComposite(Composite parent, int style) {
 		super(parent, style);
 	}
 
 	/**
 	 * @param section
 	 */
 	public SequenceFlowDetailComposite(AbstractBpmn2PropertySection section) {
 		super(section);
 	}
 
 	@Override
 	public AbstractPropertiesProvider getPropertiesProvider(EObject object) {
 		if (propertiesProvider==null) {
 			propertiesProvider = new AbstractPropertiesProvider(object) {
 				String[] properties = new String[] {
 						"language",
 						"body",
 						"evaluatesToTypeRef"
 				};
 				
 				@Override
 				public String[] getProperties() {
 					return properties; 
 				}
 			};
 		}
 		return propertiesProvider;
 	}
 
 	@Override
 	public void createBindings(final EObject be) {
 		
 		bindAttribute(be, "name");
 		
 		if (isModelObjectEnabled("SequenceFlow", "conditionExpression")) {
 			
 			final SequenceFlow sequenceFlow = (SequenceFlow) be;
 			
 			GridData data;
 
 			addRemoveConditionButton = getToolkit().createButton(this, "", SWT.PUSH);
 			addRemoveConditionButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
 			addRemoveConditionButton.addSelectionListener(new SelectionAdapter() {
 				
 				public void widgetSelected(SelectionEvent e) {
 					TransactionalEditingDomain domain = getDiagramEditor().getEditingDomain();
 					domain.getCommandStack().execute(new RecordingCommand(domain) {
 						@Override
 						protected void doExecute() {
 							if (sequenceFlow.getConditionExpression()!=null)
 								sequenceFlow.setConditionExpression(null);
 							else {
 								Expression exp = createModelObject(FormalExpression.class);
 								sequenceFlow.setConditionExpression(exp);
 								setDefault(sequenceFlow,null);
 							}
 							setBusinessObject(be);
 						}
 					});
 				}
 			});
 			
 			Expression exp = (Expression) sequenceFlow.getConditionExpression();
 			
 			if (exp != null) {
 				addRemoveConditionButton.setText("Remove Condition");
 				AbstractDetailComposite composite = PropertiesCompositeFactory.createDetailComposite(Expression.class, this, SWT.BORDER);
 				composite.setBusinessObject(exp);
				// force the property page to resize to adjust for new expression section
				Point size = composite.computeSize(-1, -1);
				GridData gd = (GridData)composite.getLayoutData();
				gd.widthHint = size.x;
				gd.heightHint = size.y;
				composite.setBusinessObject(exp);
 				composite.setTitle("Condition Expression");
 			}
 			else {
 				addRemoveConditionButton.setText("Add Condition");
 				if (sequenceFlow.getSourceRef() instanceof FlowNode) {
 					FlowNode flowNode = (FlowNode)sequenceFlow.getSourceRef();
 					String objectName = flowNode.getName();
 					if (objectName!=null && objectName.isEmpty())
 						objectName = null;
 					String typeName = ModelUtil.getLabel(flowNode);
 					if (allowDefault(sequenceFlow)) {
 						setDefaultFlowCheckbox = getToolkit().createButton(this, "", SWT.CHECK);
 						data = new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1);
 						if (!allowDefault(sequenceFlow)) {
 							data.exclude = true;
 							setDefaultFlowCheckbox.setVisible(false);
 						}
 						setDefaultFlowCheckbox.setLayoutData(data);
 						setDefaultFlowCheckbox.addSelectionListener(new SelectionAdapter() {
 							
 							public void widgetSelected(SelectionEvent e) {
 								TransactionalEditingDomain domain = getDiagramEditor().getEditingDomain();
 								domain.getCommandStack().execute(new RecordingCommand(domain) {
 									@Override
 									protected void doExecute() {
 										if (getDefault(sequenceFlow) != sequenceFlow)
 											setDefault(sequenceFlow,sequenceFlow);
 										else
 											setDefault(sequenceFlow,null);
 									}
 								});
 							}
 						});
 						setDefaultFlowCheckbox.setSelection( getDefault(sequenceFlow) == sequenceFlow );
 						setDefaultFlowCheckbox.setText("Default Flow for "+ typeName +
 								(objectName==null ? "" : (" \"" + objectName + "\"")));
 					}
 				}
 			}
 		}
 	}
 	
 	private boolean allowDefault(SequenceFlow sf) {
 		EObject obj = sf.getSourceRef();
 		if (obj!=null) {
 			EStructuralFeature feature = obj.eClass().getEStructuralFeature("default");
 			if (feature==null || !isModelObjectEnabled(obj.eClass(),feature)) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	private void setDefault(SequenceFlow sf, EObject target) {
 		EObject obj = sf.getSourceRef();
 		if (obj!=null) {
 			EStructuralFeature feature = obj.eClass().getEStructuralFeature("default");
 			if (feature!=null && obj.eGet(feature)!=target) {
 				obj.eSet(feature, target);
 			}
 		}
 	}
 	
 	private EObject getDefault(SequenceFlow sf) {
 		EObject obj = sf.getSourceRef();
 		if (obj!=null) {
 			EStructuralFeature feature = obj.eClass().getEStructuralFeature("default");
 			if (feature!=null) {
 				return (EObject) obj.eGet(feature);
 			}
 		}
 		return null;
 	}
 }
