 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.validation.internal;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jst.jsf.common.dom.TagIdentifier;
 import org.eclipse.jst.jsf.common.internal.JSPUtil;
 import org.eclipse.jst.jsf.common.internal.types.CompositeType;
 import org.eclipse.jst.jsf.common.internal.types.TypeComparator;
 import org.eclipse.jst.jsf.common.metadata.Entity;
 import org.eclipse.jst.jsf.common.metadata.Trait;
 import org.eclipse.jst.jsf.common.metadata.query.ITaglibDomainMetaDataModelContext;
 import org.eclipse.jst.jsf.common.metadata.query.TaglibDomainMetaDataQueryHelper;
 import org.eclipse.jst.jsf.common.sets.AxiomaticSet;
 import org.eclipse.jst.jsf.common.sets.ConcreteAxiomaticSet;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IDOMContextResolver;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IStructuredDocumentContextResolverFactory;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.ITaglibContextResolver;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContext;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContextFactory;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.tld.TagIdentifierFactory;
 import org.eclipse.jst.jsf.core.set.constraint.MemberConstraint;
 import org.eclipse.jst.jsf.core.set.mapping.ElementToTagIdentifierMapping;
 import org.eclipse.jst.jsf.core.tagmatcher.EvaluationException;
 import org.eclipse.jst.jsf.core.tagmatcher.InvalidExpressionException;
 import org.eclipse.jst.jsf.core.tagmatcher.XPathMatchingAlgorithm;
 import org.eclipse.jst.jsf.metadataprocessors.MetaDataEnabledProcessingFactory;
 import org.eclipse.jst.jsf.metadataprocessors.features.ELIsNotValidException;
 import org.eclipse.jst.jsf.metadataprocessors.features.IValidELValues;
 import org.eclipse.jst.jsf.metadataprocessors.features.IValidValues;
 import org.eclipse.jst.jsf.metadataprocessors.features.IValidationMessage;
 import org.eclipse.jst.jsf.validation.internal.constraints.ContainsTagConstraint;
 import org.eclipse.jst.jsf.validation.internal.constraints.TagId;
 import org.eclipse.jst.jsf.validation.internal.constraints.TagSet;
 import org.eclipse.jst.jsf.validation.internal.el.ELExpressionValidator;
 import org.eclipse.jst.jsf.validation.internal.el.diagnostics.DiagnosticFactory;
 import org.eclipse.jst.jsf.validation.internal.el.diagnostics.ValidationMessageFactory;
 import org.eclipse.jst.jsp.core.internal.domdocument.DOMModelForJSP;
 import org.eclipse.jst.jsp.core.internal.regions.DOMJSPRegionContexts;
 import org.eclipse.jst.jsp.core.internal.validation.JSPValidator;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionCollection;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
 import org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator;
 import org.eclipse.wst.validation.internal.provisional.core.IMessage;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * A JSP page validator that makes use of the JSF metadata processing framework so that JSP page
  * semantics can be validated.
  * 
  * This implementation currently only validates attribute values. 
  * @author Gerry Kessler - Oracle
  */
 public class JSPSemanticsValidator extends JSPValidator implements ISourceValidator
 {
     // TODO: should the source validator be a separate class in jsp.ui?
     // problem with simple split off is that preference must also be split off
 	static final boolean DEBUG;
 	static {
 		String value = Platform.getDebugOption("org.eclipse.jst.jsf.validation.internal.el/debug/jspsemanticsvalidator"); //$NON-NLS-1$
 		DEBUG = value != null && value.equalsIgnoreCase("true"); //$NON-NLS-1$
 	}
     private final static ElementToTagIdentifierMapping elem2TagIdMapper = new ElementToTagIdentifierMapping();
 	private IDocument fDocument;
 	private int		  containmentValidationCount;  // = 0; 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsp.core.internal.validation.JSPValidator#validateFile(org.eclipse.core.resources.IFile, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	protected void validateFile(IFile file, IReporter reporter) {	
 		IStructuredModel model = null;		
 		if (DEBUG)
 			System.out.println("executing JSPSemanticsValidator.validateFile");
 		try {
 			model = StructuredModelManager.getModelManager().getModelForRead(file);
 
 			if (model instanceof DOMModelForJSP)
 			{
 			    ValidationPreferences prefs= new ValidationPreferences(JSFCorePlugin.getDefault().getPreferenceStore());
 			    prefs.load();
 			    DiagnosticFactory diagnosticFactory = new DiagnosticFactory();
 
 				// zero the containment validation count for each model
 				containmentValidationCount = 0;
     			DOMModelForJSP jspModel = (DOMModelForJSP) model;
     			IStructuredDocument structuredDoc = jspModel.getStructuredDocument();
     			IStructuredDocumentRegion curNode = structuredDoc.getFirstStructuredDocumentRegion();
     			while (null != curNode && !reporter.isCancelled()) {
     				if (curNode.getFirstRegion().getType() == DOMRegionContext.XML_TAG_OPEN ) 
                     {
     					validateTag(curNode, reporter, file, false, prefs, diagnosticFactory);
     				}				
     				curNode = curNode.getNext();
     			}
 			}
 		}
         catch (CoreException e)
         {
             JSFCorePlugin.log("Error validating JSF", e);
         }
 		catch (IOException e) 
         {
             JSFCorePlugin.log("Error validating JSF", e);
 		}
 		finally 
         {
 			if (null != model)
 				model.releaseFromRead();
 			
 			// zero the containment count before exit
 			containmentValidationCount = 0;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator#validate(org.eclipse.jface.text.IRegion, org.eclipse.wst.validation.internal.provisional.core.IValidationContext, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	public void validate(IRegion dirtyRegion, IValidationContext helper, IReporter reporter) {
 		if (DEBUG)
 			System.out.println("exec JSPSemanticsValidator.validateRegion");
 
 		ValidationPreferences prefs = new ValidationPreferences(JSFCorePlugin.getDefault().getPreferenceStore());
 		prefs.load();
 		DiagnosticFactory diagnosticFactory = new DiagnosticFactory();
 		if (fDocument instanceof IStructuredDocument) {
 			IStructuredDocument sDoc = (IStructuredDocument) fDocument;
 			IStructuredDocumentRegion[] regions = sDoc.getStructuredDocumentRegions(dirtyRegion.getOffset(), dirtyRegion.getLength());
 			if (regions != null){
 					validateTag(regions[0], reporter, getFile(helper), true, prefs, diagnosticFactory);
 			}
 		}		
 	}
 
 	private IFile getFile(IValidationContext helper) {
 		String[] uris = helper.getURIs();
 		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
 		if (uris.length > 0) 
 			return wsRoot.getFile(new Path(uris[0]));
 
 		return null;
 	}
 
 
 	/**
 	 * Validates a JSP tag.
 	 * 
 	 * Currently only attribute values with supplied annotation meta-data is being validated.
 	 * Also, only JSF EL is being validated and not JSP EL.
 	 * 
 	 * This method may be extended in the future to validate tag semantics an other cross attribute
 	 * validations.
      * 
 	 * @param container 
 	 * @param reporter 
 	 * @param file 
 	 * @param isIncremental -- true if this validation is "as you type"
 	 *  
 	 */
 	private void validateTag(ITextRegionCollection container, IReporter reporter, IFile file, boolean isIncremental, ValidationPreferences prefs, DiagnosticFactory diagnosticFactory) 
 	{
 		ITextRegionCollection containerRegion = container;
 		Iterator regions = containerRegion.getRegions().iterator();
 		ITextRegion region = null;
 		String uri = null;
 		String tagName = null;
 		String attrName = null;		
 		while (regions.hasNext() && !reporter.isCancelled()) 
 		{
 			region = (ITextRegion) regions.next();
 			String type = region.getType();
 			IDOMContextResolver resolver = null;
 			ITaglibContextResolver tagLibResolver = null; 
 			if (type != null  && (type == DOMRegionContext.XML_TAG_NAME || type == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME || type == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE))
 			{											
 				IStructuredDocumentContext context = IStructuredDocumentContextFactory.INSTANCE.getContext(((IStructuredDocumentRegion)containerRegion).getParentDocument(), containerRegion.getStartOffset() + region.getStart());
 				resolver = IStructuredDocumentContextResolverFactory.INSTANCE.getDOMContextResolver(context);
 				if	(type == DOMRegionContext.XML_TAG_NAME) 
 				{					
 					tagLibResolver = IStructuredDocumentContextResolverFactory.INSTANCE.getTaglibContextResolver(context);
                     Node node = resolver.getNode();
 					tagName = resolver.getNode().getLocalName();
 					uri = tagLibResolver.getTagURIForNodeName(resolver.getNode());
                     
                     if (node instanceof Element && uri != null)
                     {
                         validateContainment((Element)node, uri, tagName, reporter, file, context);
                     }
                     
 					if (DEBUG)
 						System.out.println(addDebugSpacer(1)+"tagName= "+ (tagName!= null ? tagName : "null") +": uri= "+(uri != null ? uri : "null") );
 				} 
 				else if (type == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME)
 				{
 					attrName = resolver.getNode().getNodeName();
 					if (DEBUG)
 						System.out.println(addDebugSpacer(2)+"attrName= "+(attrName != null ? attrName : "null" ));
 					if (uri != null && tagName != null)
                     {
 						// TODO: validateAttribute(context, region, uri, resolver.getNode(), file);
                     }
 				}
 				else if (type == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE)
 				{
                     final String attributeVal = 
                         resolver.getNode().getNodeValue();    
 
                     // if there's  elText then validate it
                     // TODO: this approach will fail with mixed expressions
                     if (!checkIfELAndValidate(region, 
                                               context,
                                               uri,
                                               tagName,
                                               attrName,
                                               attributeVal,
                                               isIncremental,
                                               reporter,
                                               file,
                                               prefs,
                                               diagnosticFactory)
                                               )
                     {
                         // else validate as static attribute value 
                         if (DEBUG)
                             System.out.println(addDebugSpacer(3)+"attrVal= "+(attributeVal != null ? attributeVal : "null") );
 
                         if (uri != null && tagName != null && attrName != null)
                             validateAttributeValue(context, uri, tagName, attrName, attributeVal, reporter, file);
                     }
 				}
 			}
 		}
 	}
 
     /**
      * Checks the region to see if it contains an EL attribute value.  If it
      * does, validates it
      * @return true if validated EL, false otherwise
      */
     private boolean checkIfELAndValidate(ITextRegion region, 
                                          IStructuredDocumentContext context,
                                          String uri,
                                          String tagName,
                                          String attrName,
                                          String attrValue,
                                          boolean isIncremental,
                                          IReporter reporter,
                                          IFile  file,
                                          ValidationPreferences validationPrefs,
                                          DiagnosticFactory  diagnosticFactory)
     {
         if (region instanceof ITextRegionCollection) {
             ITextRegionCollection parentRegion = ((ITextRegionCollection) region);
             if (parentRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE)
             {
                 // look for attribute pattern "#{}"
                 // TODO: need to generalize this for RValue concatenation
                 final ITextRegionList  regionList = parentRegion.getRegions();
                if (regionList.size() >= 3)
                 {
                     ITextRegion  openQuote = regionList.get(0);
                     ITextRegion  openVBLQuote = regionList.get(1);
 
                     if (    (openQuote.getType() == DOMJSPRegionContexts.XML_TAG_ATTRIBUTE_VALUE_DQUOTE
                                 || openQuote.getType() == DOMJSPRegionContexts.XML_TAG_ATTRIBUTE_VALUE_SQUOTE)
                                 && (openVBLQuote.getType() == DOMJSPRegionContexts.JSP_VBL_OPEN))
                     {
                         // we appear to be inside "#{", so next should be a VBL_CONTENT if there's anything
                         // here to validate
                         final ITextRegion content = regionList.get(2);
                         if (content.getType() == DOMJSPRegionContexts.JSP_VBL_CONTENT)
                         {
                             final int contentStart = 
                                 parentRegion.getStartOffset(content);
                             final IStructuredDocumentContext elContext =
                                 IStructuredDocumentContextFactory.INSTANCE.
                                     getContext(context.getStructuredDocument(), 
                                             contentStart);
 
                             final String elText = parentRegion.getText(content);
                             
                             if (DEBUG)
                                 System.out.println(addDebugSpacer(3)+"EL attrVal= "+elText);
 
                             // EL validation is user configurable because
                             // it can be computationally costly.
                             if (checkShouldValidateEL(validationPrefs, isIncremental))
                             {
                                 List elVals = MetaDataEnabledProcessingFactory.getInstance().getAttributeValueRuntimeTypeFeatureProcessors(IValidELValues.class, elContext, uri, tagName, attrName);
                                 validateELExpression(context, 
                                                      elContext, 
                                                      elVals, 
                                                      attrValue,
                                                      elText, 
                                                      reporter, 
                                                      file,
                                                      validationPrefs.getElPrefs());
                             }
                         }
                         else if (content.getType() == DOMJSPRegionContexts.JSP_VBL_CLOSE)
                         {
                             final int offset = parentRegion.getStartOffset(openVBLQuote)+1;
                             final int length = 2;
                             
                             // detected empty EL expression
                             IMessage message = ValidationMessageFactory.createFromDiagnostic(
                                     diagnosticFactory.create_EMPTY_EL_EXPRESSION(),
                                      offset, length, file, validationPrefs.getElPrefs());
                             
                             reportFinding(reporter, message);
                         }
                         
                         boolean foundClosingQuote = false;
                         for (int i = 2; !foundClosingQuote && i < regionList.size(); i++)
                         {
                             ITextRegion  searchRegion = regionList.get(i);
                             if (searchRegion.getType() == DOMJSPRegionContexts.JSP_VBL_CLOSE)
                             {
                                 foundClosingQuote = true;
                             }
                         }
                           
                         if (!foundClosingQuote)
                         {
                            int offset = context.getDocumentPosition();
                             int length = parentRegion.getText().length();
 
                             reportFinding(reporter, 
                                ValidationMessageFactory.
                                   createFromDiagnostic(
                                           diagnosticFactory.create_MISSING_CLOSING_EXPR_BRACKET(), 
                                               offset, length, file, validationPrefs.getElPrefs()));
                         }
                         
                         return true;
                     }
                 }
             }
         }
         return false;
     }
             
 	private void validateELExpression(IStructuredDocumentContext context,
                                      IStructuredDocumentContext elContext, 
                                      List elVals, 
                                      String attributeVal,
                                      String elText, 
                                      IReporter reporter, 
                                      IFile file,
                                      ELValidationPreferences prefs) 
     {
         //Call EL validator which will perform at least the syntactical validation
         final ELExpressionValidator elValidator = 
             new ELExpressionValidator(elContext, elText,file,prefs);
         elValidator.validateXMLNode();
         elValidator.reportFindings(this, reporter);
         
 		CompositeType exprType = elValidator.getExpressionType();
 		if (exprType != null)
         {
 			for (Iterator it=elVals.iterator();it.hasNext();){
 				IValidELValues elval = (IValidELValues)it.next();
 				CompositeType expectedType;
 				IMessage message = null;
 				try {
 					expectedType = elval.getExpectedRuntimeType();
                     
                     if (expectedType != null)
                     {
     					Diagnostic status = TypeComparator.calculateTypeCompatibility
                             (expectedType, exprType);
     					if (status.getSeverity() != Diagnostic.OK){
     						message = createValidationMessage(context, attributeVal, getSeverity(status.getSeverity()), status.getMessage(), file);						
     					}
                     }
 				} catch (ELIsNotValidException e) {
 					message = createValidationMessage(context, attributeVal, IMessage.NORMAL_SEVERITY, e.getMessage(), file);						
 				}
 				if (message != null) {
 					reportFinding(reporter, message);
 				}
 			}
         }
 	}
 
 //    private void validateAttribute(IStructuredDocumentContext context, ITextRegion region, String uri, Node attr, IFile file) {
 		//Not doing anything until the resolver can help me
 		
 		//validate that attribute can be part of the tag
 //		ITaglibContextResolver tagLibResolver = IStructuredDocumentContextResolverFactory.INSTANCE.getTaglibContextResolver(context);
 //		if (tagLibResolver.getTagURIForNodeName(attr) == null){
 //			System.out.println("not ok: "+attr.getNodeName());
 //		}
 //		else
 //			System.out.println("ok");
 		
 //	}
 
 	/**
 	 * Validates an attribute value in context using the JSF metadata processing framework
 	 * 
 	 * @param context
 	 * @param region
 	 * @param uri
 	 * @param tagName
 	 * @param attrName
 	 * @param attributeVal
 	 * @param reporter
 	 * @param file
 	 */
 	private void validateAttributeValue(IStructuredDocumentContext context, String uri, String tagName, String attrName, String attributeVal, IReporter reporter, IFile file) {						
 		List vv = MetaDataEnabledProcessingFactory.getInstance().getAttributeValueRuntimeTypeFeatureProcessors(IValidValues.class, context, uri, tagName, attrName);
 		if (!vv.isEmpty()){
 			for (Iterator it = vv.iterator();it.hasNext();){
 				IValidValues v = (IValidValues)it.next();
 				if (attributeVal == null) attributeVal = "";//ensure to be non-null
 				if (!v.isValidValue(attributeVal.trim())){	
 					if (DEBUG)
 						System.out.println(addDebugSpacer(4)+"NOT VALID ");
 					
 					for (Iterator msgs = v.getValidationMessages().iterator();msgs.hasNext();){
 						IValidationMessage msg = (IValidationMessage)msgs.next();
 						IMessage message = createValidationMessage(context, attributeVal, getSeverity(msg.getSeverity()), msg.getMessage(), file);						
 						if (message != null) {
 							reportFinding(reporter, message);
 						}
 					}
 				}
 				else
 					if (DEBUG)
 						System.out.println(addDebugSpacer(5) + "VALID ");
 			}			
 		}
 		else if (DEBUG)
 			System.out.println(addDebugSpacer(4)+"NO META DATA ");
 	}
 	
 	private IMessage createValidationMessage(IStructuredDocumentContext context, String attributeValue, int severity, String msg, IFile file){
 		IMessage message = new LocalizedMessage(severity, msg, file);						
 		if (message != null) {
 			final int start = context.getDocumentPosition() + 1;
 			final int length = attributeValue.length();
 			
 			int lineNo = 0;
 			try {
 				lineNo = context.getStructuredDocument().getLineOfOffset(start);
 			} catch (BadLocationException e) {
                 //  TODO: C.B why need line number? Length and offset should be
 			    //  sufficient
 			}
 			
 			message.setLineNo(lineNo);
 			message.setOffset(start);
 			message.setLength(length);
 		}
 		return message;
 	}
 	/**
 	 * Maps IStatus codes to IMessage severity
 	 * @param IStatus codesseverity
 	 * @return IMessage severity
 	 */
 	private int getSeverity(int severity) {
 
 		switch (severity){
 			case IStatus.ERROR:
 				return IMessage.HIGH_SEVERITY;
 			case IStatus.WARNING:
 				return IMessage.NORMAL_SEVERITY;
 			case IStatus.INFO:
 				return IMessage.LOW_SEVERITY;
 		}
 		return IMessage.NORMAL_SEVERITY;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator#connect(org.eclipse.jface.text.IDocument)
 	 */
 	public void connect(IDocument document) {
 		fDocument = document;
 		containmentValidationCount = 0; // ensure is zeroed before we start
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator#disconnect(org.eclipse.jface.text.IDocument)
 	 */
 	public void disconnect(IDocument document) {
 		containmentValidationCount = 0; // ensure is zeroed when we are finished
 	}
 	
 	private String addDebugSpacer(int count){
 		String TAB = "\t";
 		StringBuffer ret = new StringBuffer("");
 		for(int i=0;i<=count;i++){
 			ret.append(TAB);
 		}
 		return ret.toString();
 	}
 
     /**
      * @param isIncremental -- true if this is "as-you-type" validation, false
      * if this is "Build" or "Run Validation" validation
      * @return true if user preferences say we should do EL validation,
      * false otherwise
      */
     private boolean checkShouldValidateEL(ValidationPreferences prefs, boolean isIncremental)
     {
         prefs.load();
         if (isIncremental)
         {
             return prefs.getElPrefs().isEnableIncrementalValidation();
         }
 
         return prefs.getElPrefs().isEnableBuildValidation();
     }
     
     private void validateContainment(Element node, String uri, String tagName, IReporter reporter, IFile file, IStructuredDocumentContext context)
     {
     	// don't validate JSP fragments since the necessary containment may existing
     	// in the JSP files that include them
     	// also only validate the first instance of containment violation in a file
     	if (JSPUtil.isJSPFragment(file) || containmentValidationCount > 0)
     	{
     		return;
     	}
     	
         final ITaglibDomainMetaDataModelContext modelContext = 
             TaglibDomainMetaDataQueryHelper.createMetaDataModelContext(file.getProject(), uri);
         final Entity entity = 
         	TaglibDomainMetaDataQueryHelper.getEntity(modelContext, tagName);
         if (entity != null)
         {
             final Trait trait = 
             	TaglibDomainMetaDataQueryHelper.getTrait
                     (entity, "containment-constraint");
             
             if (trait != null)
             {
                 final ContainsTagConstraint tagConstraint = 
                     (ContainsTagConstraint) trait.getValue();
 
                 final String algorithm = tagConstraint.getSetGenerator().getAlgorithm();
                 
                 // TODO: need generalized factory mechanism for registering and constructing
                 // algorithms.
                 if (!"xpath".equals(algorithm))
                 {
                     return;
                 }
                 
                 final String expr = tagConstraint.getSetGenerator().getExpression();
                 
                 // TODO: optimize on the expression and cache for reuse
                 final XPathMatchingAlgorithm  xpathAlg = 
                     new XPathMatchingAlgorithm(expr);
                 
                 AxiomaticSet set = null;
                 
                 try
                 {
                     set = xpathAlg.evaluate(node);
                     // map dom nodes to tag identifiers
                     set = elem2TagIdMapper.map(set);
                 }
                 catch(InvalidExpressionException  e)
                 {
                     JSFCorePlugin.log(e, "Problem with expression: "+expr+" on node "+node);
                     return;
                 }
                 catch (EvaluationException  e)
                 {
                     JSFCorePlugin.log(e, "Problem evaluating expression: "+expr+" on node "+node);
                     return;
                 }
 
                 final TagSet constraintData = tagConstraint.getSatisfiesSet();
                 final AxiomaticSet constraintSet = new ConcreteAxiomaticSet();
                 for (final Iterator it = constraintData.getTags().iterator(); it.hasNext();)
                 {
                     final TagId tagId = (TagId) it.next();
                     constraintSet.add(TagIdentifierFactory.createJSPTagWrapper(tagId.getUri(), tagId.getName()));
                 }
                 final MemberConstraint memberConstraint = new MemberConstraint(constraintSet);
                 final Diagnostic diag = memberConstraint.isSatisfied(set);
 
                 if (diag.getSeverity() != Diagnostic.OK)
                 {
                 	containmentValidationCount++; // found a violation
                 	
                     final String messagePattern = "Tag {0} is missing required parent tag \"{1}\" ({2})";
 
                     List data = diag.getData();
 
                     for (Iterator it = data.iterator(); it.hasNext();)
                     {
                         TagIdentifier missingParent = (TagIdentifier) it.next();
 
                         IMessage message =
                             createTagValidationMessage(context, node.getNodeName(), 
                                 IMessage.NORMAL_SEVERITY, 
                                 MessageFormat.format(messagePattern, new Object[]{node.getNodeName(), missingParent.getTagName(), missingParent.getUri()})
                                 , file);
                         reportFinding(reporter, message);
                     }
                 }
             }
         }
     }
     
     /**
      * @return the test interface
      */
     public IJSPSemanticValidatorTest getTestInterface()
     {
     	return new IJSPSemanticValidatorTest()
     	{
 			public void validateContainment(Element node, String uri,
 					String tagName, IReporter reporter, IFile file,
 					IStructuredDocumentContext context) {
 				
 				JSPSemanticsValidator.this.validateContainment(node, uri, tagName, reporter, file, context);
 			}
     	};
     }
     
     // TODO: need a diagnostic factory
     private IMessage createTagValidationMessage(IStructuredDocumentContext context, String attributeValue, int severity, String msg, IFile file)
     {
         IMessage message = new LocalizedMessage(severity, msg, file);                       
         final int start = context.getDocumentPosition();
         final int length = attributeValue.length();
         
         message.setOffset(start);
         message.setLength(length);
         return message;
     }
     
     private void reportFinding(IReporter reporter, IMessage message)
     {
         if ((message.getSeverity() & IMessage.ALL_MESSAGES) != 0)
         {
             reporter.addMessage(this, message);
         }
     }
 }
