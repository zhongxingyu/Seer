  /*******************************************************************************
   * Copyright (c) 2007 Red Hat, Inc.
   * Distributed under license by Red Hat, Inc. All rights reserved.
   * This program is made available under the terms of the
   * Eclipse Public License v1.0 which accompanies this distribution,
   * and is available at http://www.eclipse.org/legal/epl-v10.html
   *
   * Contributors:
   *     Red Hat, Inc. - initial API and implementation
   ******************************************************************************/
 package org.jboss.tools.seam.internal.core.validation;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jdt.internal.ui.text.FastJavaPartitionScanner;
 import org.eclipse.jdt.ui.text.IJavaPartitions;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.Token;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.jboss.tools.common.el.core.model.ELExpression;
 import org.jboss.tools.common.el.core.model.ELInstance;
 import org.jboss.tools.common.el.core.model.ELInvocationExpression;
 import org.jboss.tools.common.el.core.model.ELModel;
 import org.jboss.tools.common.el.core.parser.ELParser;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.el.core.parser.SyntaxError;
 import org.jboss.tools.common.el.core.resolver.ElVarSearcher;
 import org.jboss.tools.common.el.core.resolver.TypeInfoCollector;
 import org.jboss.tools.common.el.core.resolver.Var;
 import org.jboss.tools.common.util.FileUtil;
 import org.jboss.tools.seam.core.ISeamContextVariable;
 import org.jboss.tools.seam.core.ISeamProject;
 import org.jboss.tools.seam.core.SeamCoreMessages;
 import org.jboss.tools.seam.core.SeamCorePlugin;
 import org.jboss.tools.seam.core.SeamPreferences;
 import org.jboss.tools.seam.internal.core.el.SeamELCompletionEngine;
 import org.jboss.tools.seam.internal.core.el.SeamELOperandResolveStatus;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * EL Validator
  * @author Alexey Kazakov
  */
 public class SeamELValidator extends SeamValidator {
 
 	protected static final String UNKNOWN_EL_VARIABLE_NAME_MESSAGE_ID = "UNKNOWN_EL_VARIABLE_NAME"; //$NON-NLS-1$
 	protected static final String UNKNOWN_EL_VARIABLE_PROPERTY_NAME_MESSAGE_ID = "UNKNOWN_EL_VARIABLE_PROPERTY_NAME"; //$NON-NLS-1$
 	protected static final String UNPAIRED_GETTER_OR_SETTER_MESSAGE_ID = "UNPAIRED_GETTER_OR_SETTER"; //$NON-NLS-1$
 	protected static final String SYNTAX_ERROR_MESSAGE_ID = "EL_SYNTAX_ERROR"; //$NON-NLS-1$
 
 	protected static final String VALIDATING_EL_FILE_MESSAGE_ID = "VALIDATING_EL_FILE";
 
 	private SeamELCompletionEngine engine;
 	private List<Var> varListForCurentValidatedNode = new ArrayList<Var>();
 	private ElVarSearcher elVarSearcher;
 
 	public SeamELValidator(SeamValidatorManager validatorManager,
 			SeamContextValidationHelper coreHelper, IReporter reporter,
 			SeamValidationContext validationContext, ISeamProject project) {
 		super(validatorManager, coreHelper, reporter, validationContext, project);
 		engine = new SeamELCompletionEngine(project);
 		elVarSearcher = new ElVarSearcher(engine);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.seam.internal.core.validation.ISeamValidator#isEnabled()
 	 */
 	public boolean isEnabled() {
 		return SeamPreferences.shouldValidateEL(project);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.seam.internal.core.validation.ISeamValidator#validate(java.util.Set)
 	 */
 	public IStatus validate(Set<IFile> changedFiles) throws ValidationException {
 		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
 		Set<IPath> files = validationContext.getElResourcesForValidation(changedFiles);
 		validationContext.removeLinkedElResources(files);
 		for (IPath path : files) {
 			if(!reporter.isCancelled()) {
 				validationContext.removeUnnamedElResource(path);
 				IFile file = wsRoot.getFile(path);
 				if(file.exists()) {
 					reporter.removeMessageSubset(validationManager, file, ISeamValidator.MARKED_SEAM_RESOURCE_MESSAGE_GROUP);
 					validateFile(file);
 				}
 			}
 		}
 
 		validationContext.clearOldVariableNameForElValidation();
 		return OK_STATUS;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.seam.internal.core.validation.ISeamValidator#validateAll()
 	 */
 	public IStatus validateAll() throws ValidationException {
 		validationContext.clearElResourceLinks();
 		Set<IFile> files = validationContext.getRegisteredFiles();
 		for (IFile file : files) {
 			if(!reporter.isCancelled()) {
 				if(file.exists()) {
 					validateFile(file);
 				} else {
 					validationContext.removeUnnamedElResource(file.getFullPath());
 				}
 			}
 		}
 		return OK_STATUS;
 	}
 
 	private void validateFile(IFile file) {
 		displaySubtask(VALIDATING_EL_FILE_MESSAGE_ID, new String[]{projectName, file.getName()});
 		elVarSearcher.setFile(file);
 		String ext = file.getFileExtension();
 		String content = null;
 		try {
 			if(!file.isSynchronized(IResource.DEPTH_ZERO)) {
 				// The resource is out of sync with the file system
 				// Just ignore this resource.
 				return;
 			}
 			content = FileUtil.readStream(file.getContents());
 		} catch (CoreException e) {
 			SeamCorePlugin.getPluginLog().logError(e);
 			return;
 		}
 		if(ext.equalsIgnoreCase("java")) { //$NON-NLS-1$
 			validateJava(file, content);
 		} else {
 			validateDom(file, content);
 		}
 	}
 
 	private void validateJava(IFile file, String content) {
 		try {
 			FastJavaPartitionScanner scaner = new FastJavaPartitionScanner();
 			Document document = new Document(content);
 			scaner.setRange(document, 0, document.getLength());
 			IToken token = scaner.nextToken();
 			while(token!=null && token!=Token.EOF && !reporter.isCancelled()) {
 				if(IJavaPartitions.JAVA_STRING.equals(token.getData())) {
 					int length = scaner.getTokenLength();
 					int offset = scaner.getTokenOffset();
 					String value = document.get(offset, length);
 					if(value.indexOf('{')>-1) {
 						validateString(file, value, offset);
 					}
 				}
 				token = scaner.nextToken();
 			}
 		} catch (BadLocationException e) {
 			SeamCorePlugin.getDefault().logError(SeamCoreMessages.SEAM_EL_VALIDATOR_ERROR_VALIDATING_SEAM_EL, e);
 		}
 	}
 
 	private void validateDom(IFile file, String content) {
 		varListForCurentValidatedNode.clear();
 		IModelManager manager = StructuredModelManager.getModelManager();
 		if(manager == null) {
 			// this can happen if plugin org.eclipse.wst.sse.core 
 			// is stopping or uninstalled, that is Eclipse is shutting down.
 			// there is no need to report it, just stop validation.
 			return;
 		}
 		IStructuredModel model = null;		
 		try {
 			model = manager.getModelForRead(file);
 			if (model instanceof IDOMModel) {
 				IDOMModel domModel = (IDOMModel) model;
 				IDOMDocument document = domModel.getDocument();
 				validateChildNodes(file, document);
 			}
 		} catch (CoreException e) {
 			SeamCorePlugin.getDefault().logError(SeamCoreMessages.SEAM_EL_VALIDATOR_ERROR_VALIDATING_SEAM_EL, e);
         } catch (IOException e) {
         	SeamCorePlugin.getDefault().logError(SeamCoreMessages.SEAM_EL_VALIDATOR_ERROR_VALIDATING_SEAM_EL, e);
 		} finally {
 			if (model != null) {
 				model.releaseFromRead();
 			}
 		}
 		return;
 	}
 
 	private void validateChildNodes(IFile file, Node parent) {
 		String preferenceValue = SeamPreferences.getProjectPreference(project, SeamPreferences.CHECK_VARS);
 		NodeList children = parent.getChildNodes();
 		for(int i=0; i<children.getLength() && !reporter.isCancelled(); i++) {
 			Node curentValidatedNode = children.item(i);
 			Var var = null;
 			if(Node.ELEMENT_NODE == curentValidatedNode.getNodeType()) {
 				if (SeamPreferences.ENABLE.equals(preferenceValue)) {
 					var = elVarSearcher.findVar(curentValidatedNode);
 				}
 				if(var!=null) {
 					varListForCurentValidatedNode.add(var);
 				}
 				validateNodeContent(file, ((IDOMNode)curentValidatedNode).getFirstStructuredDocumentRegion(), DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE);
 			} else if(Node.TEXT_NODE == curentValidatedNode.getNodeType()) {
 				validateNodeContent(file, ((IDOMNode)curentValidatedNode).getFirstStructuredDocumentRegion(), DOMRegionContext.XML_CONTENT);
 			}
 			validateChildNodes(file, curentValidatedNode);
 			if(var!=null) {
 				varListForCurentValidatedNode.remove(var);
 			}
 		}
 	}
 
 	private void validateNodeContent(IFile file, IStructuredDocumentRegion node, String regionType) {
 		ITextRegionList regions = node.getRegions();
 		for(int i=0; i<regions.size(); i++) {
 			ITextRegion region = regions.get(i);
 			if(region.getType() == regionType) {
 				String text = node.getFullText(region);
 				if(text.indexOf("{")>-1) { //$NON-NLS-1$
 					int offset = node.getStartOffset() + region.getStart();
 					validateString(file, text, offset);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param offset - offset of string in file
 	 * @param length - length of string in file
 	 */
 	private void validateString(IFile file, String string, int offset) {
 		int startEl = string.indexOf("#{"); //$NON-NLS-1$
 		if(startEl>-1) {
 			ELParser parser = ELParserUtil.getJbossFactory().createParser();
 			ELModel model = parser.parse(string);
 			List<SyntaxError> errors = model.getSyntaxErrors();
 			if(errors.size() > 0) {
 				for (SyntaxError error: errors) {
 					//TODO 1) make message more informative
 					//     2) create other preference 
 					addError(SYNTAX_ERROR_MESSAGE_ID, SeamPreferences.EL_SYNTAX_ERROR, new String[]{"" + error.getProblem()}, 1, offset + error.getPosition(), file);
 				}
 				
 			}
 			List<ELInstance> is = model.getInstances();
 			for (ELInstance i : is) {
 				if (reporter.isCancelled()) {
 					return;
 				}
 				if(i.getErrors().size() > 0) {
 					//Already reported syntax problem in this piece of EL.
 					continue;
 				}
 				validateEl(file, i.getExpression(), offset);
 			}
 		}
 	}
 
 	private void validateEl(IFile file, ELExpression el, int offset) {
 		if(el == null) return;
 		List<ELInvocationExpression> es = el.getInvocations();
 		for (ELInvocationExpression token: es) {
 			validateElOperand(file, token, offset);
 		}
 	}
 
 	private void validateElOperand(IFile file, ELInvocationExpression operandToken, int documnetOffset) {
 		String operand = operandToken.getText();
 		String varName = operand;
 		int offsetOfVarName = documnetOffset + operandToken.getFirstToken().getStart();
 		int lengthOfVarName = varName.length();
		boolean unresolvedTokenIsVariable = false;
 		try {
 			int offset = operand.length();
 			if (!operand.endsWith(".")) { //$NON-NLS-1$
 				SeamELOperandResolveStatus status = 
 					engine.resolveELOperand(file, operandToken, true, varListForCurentValidatedNode, elVarSearcher);
 
 				if(status.getUsedVariables().size()==0 && status.isError()) {
 					// Save resources with unknown variables names
 					validationContext.addUnnamedElResource(file.getFullPath());
 				} else {
 					// Save links between resource and used variables names
 					for(ISeamContextVariable variable: status.getUsedVariables()) {
 						validationContext.addLinkedElResource(variable.getName(), file.getFullPath());
 					}
 				}
 
 				// Check pair for getter/setter
 				if(status.getUnpairedGettersOrSetters().size()>0) {
 					TypeInfoCollector.MethodInfo unpairedMethod = status.getUnpairedGettersOrSetters().values().iterator().next();
 					String methodName = unpairedMethod.getName();
 					String propertyName = status.getUnpairedGettersOrSetters().keySet().iterator().next();
 					String missingMethodName = SeamCoreMessages.SEAM_EL_VALIDATOR_SETTER;
 					String existedMethodName = SeamCoreMessages.SEAM_EL_VALIDATOR_GETTER;
 					if(methodName.startsWith("s")) { //$NON-NLS-1$
 						missingMethodName = existedMethodName;
 						existedMethodName = SeamCoreMessages.SEAM_EL_VALIDATOR_SETTER;
 					}
 					addError(UNPAIRED_GETTER_OR_SETTER_MESSAGE_ID, SeamPreferences.UNPAIRED_GETTER_OR_SETTER, new String[]{propertyName, existedMethodName, missingMethodName}, operand.length(), documnetOffset, file);
 				}
 
 				if (status.isOK()) {
 					// It's valid EL.
 					return;
 				}
 				
 				ELInvocationExpression ts = status.getUnresolvedTokens();
 				
 				varName = ts.getMemberName();
 				if(varName == null) {
 					//This is syntax error case. Reported by parser.
 					return;						
 				}
 				offsetOfVarName = documnetOffset + ts.getInvocationStartPosition();
 				lengthOfVarName = varName == null ? 0 : varName.length();
				if(status.getUsedVariables().isEmpty()) {
					unresolvedTokenIsVariable = true;
				}
 			}
 		} catch (BadLocationException e) {
 			SeamCorePlugin.getDefault().logError(SeamCoreMessages.SEAM_EL_VALIDATOR_ERROR_VALIDATING_SEAM_EL, e);
 		} catch (StringIndexOutOfBoundsException e) {
 			SeamCorePlugin.getDefault().logError(SeamCoreMessages.SEAM_EL_VALIDATOR_ERROR_VALIDATING_SEAM_EL, e);
 		}
 		// Mark invalid EL
 		if(unresolvedTokenIsVariable) {
 			addError(UNKNOWN_EL_VARIABLE_NAME_MESSAGE_ID, SeamPreferences.UNKNOWN_EL_VARIABLE_NAME, new String[]{varName}, lengthOfVarName, offsetOfVarName, file);
 		} else {
 			addError(UNKNOWN_EL_VARIABLE_PROPERTY_NAME_MESSAGE_ID, SeamPreferences.UNKNOWN_EL_VARIABLE_PROPERTY_NAME, new String[]{varName}, lengthOfVarName, offsetOfVarName, file);
 		}
 	}
 
 }
