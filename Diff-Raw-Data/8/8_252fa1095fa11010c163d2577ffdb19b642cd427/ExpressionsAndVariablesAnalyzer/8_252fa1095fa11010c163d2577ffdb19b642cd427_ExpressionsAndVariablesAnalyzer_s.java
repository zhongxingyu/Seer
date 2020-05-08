 package com.github.kanafghan.welipse.webdsl.expressions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.ETypedElement;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 
 import com.github.kanafghan.welipse.webdsl.ActualParameter;
 import com.github.kanafghan.welipse.webdsl.ClassifierOperation;
 import com.github.kanafghan.welipse.webdsl.Expression;
 import com.github.kanafghan.welipse.webdsl.ExternalLink;
 import com.github.kanafghan.welipse.webdsl.Image;
 import com.github.kanafghan.welipse.webdsl.Input;
 import com.github.kanafghan.welipse.webdsl.InternalLink;
 import com.github.kanafghan.welipse.webdsl.Page;
 import com.github.kanafghan.welipse.webdsl.PageElement;
 import com.github.kanafghan.welipse.webdsl.Parameter;
 import com.github.kanafghan.welipse.webdsl.PropertyOperation;
 import com.github.kanafghan.welipse.webdsl.StructuralExp;
 import com.github.kanafghan.welipse.webdsl.Submit;
 import com.github.kanafghan.welipse.webdsl.Text;
 import com.github.kanafghan.welipse.webdsl.VariableDeclaration;
 import com.github.kanafghan.welipse.webdsl.VariableExp;
 import com.github.kanafghan.welipse.webdsl.VariableInitialization;
 import com.github.kanafghan.welipse.webdsl.WebDSLPackage;
 import com.github.kanafghan.welipse.webdsl.parsers.ExpressionsLanguage;
 
 public class ExpressionsAndVariablesAnalyzer {
 	
 	private EditingDomain editingDomain;
 	private EObject element;
 	private String text;
 	private ExpressionVariableKind expressionVariableKind;
 	
 	public ExpressionsAndVariablesAnalyzer(EditingDomain editingDomain, 
 			EObject element,
 			String text,
 			ExpressionVariableKind expressionVariableKind) {
 		this.editingDomain = editingDomain;
 		this.element = element;
 		this.text = text;
 		this.expressionVariableKind = expressionVariableKind;
 	}
 	
 	public void analyze() {
 		final Job job = new Job("Analyzing Expression/Variable: "+ text) {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				String PLUGIN_ID = "com.github.kanafghan.welipse.webdsl";
 				
 				// initialize the parser
 				ExpressionsLanguage parser = ExpressionsLanguage.getInstace();
 				parser.setText(text);
 				
 				try {
 					switch (expressionVariableKind) {
 					case PAGE_PARAMETER:
 					case PARAMETER:
 					case LIST_ITERATOR_VARIABLE:
 						analyzeVariableDeclaration(parser.getParameter(), true);
 						break;
 					case PAGE_VARIABLE:
 					case VARIABLE:
 						analyzeVariableDeclaration(parser.getVariable(), false);
 						break;
 					default:
 						analyzeExpression(parser.getExpression());
 						break;
 					}
 				} catch (Exception e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The expression '"+ text +"' could not be parsed: "+ e.getMessage(), e);
 				} catch (Error e) {
 					return new Status(Status.ERROR, PLUGIN_ID,
 							"The expression '"+ text +"' is not correct: "+ e.getMessage(), e);
 				}
 				
 				monitor.worked(1);
 				return new Status(Status.OK, PLUGIN_ID, "Analyzed successfully.");
 			}
 		};
 		
 		// enqueue the job
 		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
 		job.setUser(true);
 		job.schedule();									
 	}
 
 	protected void analyzeVariableDeclaration(VariableDeclaration variable, boolean isParameter) {
 		if (variable != null) {
 			EClass eClass = element.eClass();
 			
 			// Initialize the variable
 			if (expressionVariableKind == ExpressionVariableKind.PARAMETER 
 					|| expressionVariableKind == ExpressionVariableKind.VARIABLE) {
 				VariableDeclaration oldVar = (VariableDeclaration) element;
 				variable.initialize((Page) oldVar.eContainer());
 				
 				EStructuralFeature sfClassifier = eClass.getEStructuralFeature(WebDSLPackage.VARIABLE_DECLARATION__CLASSIFIER);
 			    EStructuralFeature sfType = eClass.getEStructuralFeature(WebDSLPackage.VARIABLE_DECLARATION__TYPE);
 			    EStructuralFeature sfVar = eClass.getEStructuralFeature(WebDSLPackage.VARIABLE_DECLARATION__VAR);
 			    
 			    // The list of commands that will set the features of the oldVar
 			    List<Command> commandList = new ArrayList<Command>();
 			    commandList.add(SetCommand.create(editingDomain, oldVar, sfClassifier, variable.getClassifier()));
 			    commandList.add(SetCommand.create(editingDomain, oldVar, sfType, variable.getType()));
 			    commandList.add(SetCommand.create(editingDomain, oldVar, sfVar, variable.getVar()));
 				
 				if (expressionVariableKind == ExpressionVariableKind.VARIABLE) {
 					VariableInitialization oldVarInit = (VariableInitialization) oldVar;
 					VariableInitialization newVarInit = (VariableInitialization) variable;
 					analyzeVariable(newVarInit);
 					EStructuralFeature sfInitExp = eClass.getEStructuralFeature(WebDSLPackage.VARIABLE_INITIALIZATION__INIT_EXP);
 					commandList.add(SetCommand.create(editingDomain, oldVarInit, sfInitExp, newVarInit.getInitExp()));
 				}
 				
 				// Execute the commands
 				CompoundCommand command = new CompoundCommand(commandList);
 			    editingDomain.getCommandStack().execute(command);
 			} else if (expressionVariableKind == ExpressionVariableKind.PAGE_PARAMETER 
 					|| expressionVariableKind == ExpressionVariableKind.PAGE_VARIABLE) {
 				variable.initialize((Page) element);
 				
 				// Create and execute the command
 				EStructuralFeature f = null;
 				switch (expressionVariableKind) {
 				case PAGE_PARAMETER:
 					f = eClass.getEStructuralFeature(WebDSLPackage.PAGE__PARAMETERS);					
 					break;
 				case PAGE_VARIABLE:
 				default:
 					VariableInitialization varInit = (VariableInitialization) variable;
 					analyzeVariable(varInit);
 					varInit.setDeclaration(text);
 					f = eClass.getEStructuralFeature(WebDSLPackage.PAGE__VARIABLES);
 					break;
 				}
 			    editingDomain.getCommandStack().execute(AddCommand.create(editingDomain, element, f, variable));
 			} else if (expressionVariableKind == ExpressionVariableKind.LIST_ITERATOR_VARIABLE) {
 				variable.initialize(getPage((PageElement) element));
 				// Create and execute the command
 				EStructuralFeature f = eClass.getEStructuralFeature(WebDSLPackage.LIST__ITERATOR_VARIABLE);
 				editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, f, variable));
 			}
 		} else {
 			throw new Error("The declaration '"+ text +"' could not be analyzed. Probabily, due to a parsing error.");
 		}	
 	}
 
 	private void analyzeVariable(VariableInitialization variable) {
 		EClassifier type = variable.getInitExp().type();
 		if (type == null) {
 			throw new Error("The type of the expresion in the variable declaration '"
 							+ text
 							+ "' is not defined.");
 		}
 		if (!variable.getType().getName().equals(type.getName())) {
 			throw new Error("The type of the variable in '"
 							+ text
 							+ "' is different from the "
 							+ "expression initializing it. (variable type: "
 							+ variable.getType().getName()
 							+ ", expression type: "
 							+ type.getName());
 		}
 	}
 
 	protected void analyzeExpression(Expression expression) {
 		if (expression != null) {
 			// Initialize the expression
 			if (element instanceof PageElement) {
 				expression.initialize(getPage((PageElement) element));
 			} else if (element instanceof ActualParameter) {
 				ActualParameter actualParameter = (ActualParameter) element;
 				InternalLink internalLink = (InternalLink) actualParameter.eContainer();
 				expression.initialize(getPage(internalLink));
 			}
 			
 			// Type check the expression
 			EClassifier type = expression.type();
 			
 			// Create and execute the command
 			EClass eClass = element.eClass();
 		    EStructuralFeature f = null;
 			
 		    if (element instanceof Text) {
 				f = eClass.getEStructuralFeature(WebDSLPackage.TEXT__CONTENT);
 			} else if (element instanceof Image) {
 				f = eClass.getEStructuralFeature(WebDSLPackage.IMAGE__SOURCE);
 			} else if (element instanceof com.github.kanafghan.welipse.webdsl.List) {
 				if (expression instanceof PropertyOperation) {
 					ETypedElement elem = null;
 					if (expression instanceof ClassifierOperation) {
 						elem = ((ClassifierOperation) expression).getOperation();
 					} else if (expression instanceof StructuralExp) {
 						elem = ((StructuralExp) expression).getFeature();
 					}
 					
 					if (elem != null) {
 						if (!(elem.getUpperBound() > 1 || elem.getUpperBound() == -1)) {
 							throw new Error("The expression '"+ text +"' is not a valid expression for the "
 									+ "collection property of the list. It must result in a "
 									+ "list of elements of type "+ type.getName());
 						}
 					}
 				} else {
 					throw new Error("The expression '"+ text +"' is not a valid expression for the "
 								+ "collection property of the list. It must be either a "
 								+ "classifier operation or a structural expression.");
 				}
 				
 				f = eClass.getEStructuralFeature(WebDSLPackage.LIST__COLLECTION);
 			} else if (element instanceof ExternalLink) {
 				f = eClass.getEStructuralFeature(WebDSLPackage.EXTERNAL_LINK__TARGET);
 			} else if (element instanceof Input) {
 				if (expressionVariableKind == ExpressionVariableKind.INPUT_VALUE) {
 			    	// Set the name of the input by deriving it from the expression
 			    	String inputName = "";
 			    	if (expression instanceof StructuralExp) {
 			    		StructuralExp structuralExp = (StructuralExp) expression;
 			    		inputName = structuralExp.getIdentifier();
 			    	} else if (expression instanceof VariableExp) {
 			    		VariableExp variableExp = (VariableExp) expression;
 			    		inputName = variableExp.getVar();
 			    	}
 
 			    	EStructuralFeature sfInputName = eClass.getEStructuralFeature(WebDSLPackage.PAGE_ELEMENT__NAME);
 			    	EStructuralFeature sfInputValue = eClass.getEStructuralFeature(WebDSLPackage.INPUT__VALUE);
 			    	
 			    	// create and execute a compound command
 			    	List<Command> commandsList = new ArrayList<Command>();
 			    	commandsList.add(SetCommand.create(editingDomain, element, sfInputName, inputName));
			    	commandsList.add(SetCommand.create(editingDomain, element, sfInputValue, inputName));
 			    	CompoundCommand command = new CompoundCommand(commandsList);
 			    	editingDomain.getCommandStack().execute(command);
			    } else {
 			    	f = eClass.getEStructuralFeature(WebDSLPackage.INPUT__LABEL);
 			    }
 			} else if (element instanceof Submit) {
 				if (expressionVariableKind == ExpressionVariableKind.SUBMIT_PERFORMER) {
 			    	f = eClass.getEStructuralFeature(WebDSLPackage.SUBMIT__PERFORMER);
 			    } else {
 			    	f = eClass.getEStructuralFeature(WebDSLPackage.SUBMIT__VALIDATOR);
 			    }
 			} else if (element instanceof ActualParameter) {
 				ActualParameter actualParameter = (ActualParameter) element;
 				VariableExp variable;
 				
 				// The expression must be a variable expression
 				if (!(expression instanceof VariableExp)) {
 					throw new Error("The allowed expression for an actual parameter is a variable expression. "
 							+ "You have provided: "+ expression.eClass().getName());
 				} else {
 					variable = (VariableExp) expression;
 				}
 				
 				// Set the formal parameter
 				InternalLink internalLink = (InternalLink) actualParameter.eContainer();
 				PageElement targetElement = internalLink.getTarget();
 				if (targetElement != null) {
 					Page targetPage = getPage((PageElement) targetElement);
 					if (targetPage != null) {
 						EList<Parameter> parameters = targetPage.getParameters();
 						for (Parameter formalParameter : parameters) {
 							if (formalParameter.getVar().equals(actualParameter.getIdentifier())) {
 								// Types must also match
 								if (formalParameter.getType().equals(variable.type())) {
 									EStructuralFeature sfValue = eClass.getEStructuralFeature(WebDSLPackage.ACTUAL_PARAMETER__VALUE);
 									EStructuralFeature sfFormalParameter = eClass.getEStructuralFeature(WebDSLPackage.ACTUAL_PARAMETER__FORMAL_PARAMETER);
 									
 									List<Command> commandList = new ArrayList<Command>();
 									commandList.add(SetCommand.create(editingDomain, element, sfValue, expression));
 									commandList.add(SetCommand.create(editingDomain, element, sfFormalParameter, formalParameter));
 									
 									// Execute the commands
 									CompoundCommand command = new CompoundCommand(commandList);
 								    editingDomain.getCommandStack().execute(command);
 								}
 							}
 						}
 						
 						if (actualParameter.getFormalParameter() == null) {
 							throw new Error("The target page '"+ targetPage.getName() 
 									+"' of the internal link '"+ internalLink.getName() 
 									+"' does not have either a parameter with the name '"+ text 
 									+"' or a parameter with the type '"+ variable.type().getName()
 									+"'. Your model is incomplete at the moment.");
 						}
 					} else {
 						throw new Error("Could not found the target page of the link. Please define the target of the internal link '"
 								+ internalLink.getName() 
 								+"' before defining its actual parameters. Your model is incomplete at the moment.");
 					}
 					
 				} else {
 					throw new Error("Please define the target of the internal link '"+ internalLink.getName() 
 							+"' before defining its actual parameters. Your model is incomplete at the moment.");
 				}
 			}
 			
 			if (f != null) {											
 				editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, element, f, expression));
 			}
 		} else {
 			throw new Error("The expression '"+ text +"' could not be analyzed. Probabily, due to a parsing error.");
 		}
 	}
 
 	/**
 	 * Recursively fetches the page that the given page element belongs to
 	 * 
 	 * @param pageElement
 	 * @return The page that the given page element belongs to
 	 */
 	protected Page getPage(PageElement pageElement) {
 		if (pageElement.getPage() != null) {
 			return pageElement.getPage();
 		}
 		return getPage((PageElement) pageElement.eContainer());
 	}
 	
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public void setEditingDomain(EditingDomain editingDomain) {
 		this.editingDomain = editingDomain;
 	}
 
 	public void setElement(EObject element) {
 		this.element = element;
 	}
 }
