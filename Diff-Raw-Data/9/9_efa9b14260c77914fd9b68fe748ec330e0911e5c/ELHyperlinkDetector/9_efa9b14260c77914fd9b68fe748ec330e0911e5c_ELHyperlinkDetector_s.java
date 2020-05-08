 /*******************************************************************************
  * Copyright (c) 2011 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.text.ext.hyperlink;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.jboss.tools.common.el.core.ELReference;
 import org.jboss.tools.common.el.core.model.ELExpression;
 import org.jboss.tools.common.el.core.model.ELInvocationExpression;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELResolution;
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.el.core.resolver.ELSegment;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 
 public class ELHyperlinkDetector extends AbstractHyperlinkDetector{
 
 	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
 			IRegion region, boolean canShowMultipleHyperlinks) {
 		
 		IFile file = getFile();
 		if(file == null)
 			return null;
 		
 		ELContext context = PageContextFactory.createPageContext(file);
 		if(context == null)
 			return null;
 		Set<IHyperlink> links = new HashSet<IHyperlink>();
 		ELReference reference = context.getELReference(region.getOffset());
 		if(reference != null){
 			ELInvocationExpression expression = findInvocationExpressionByOffset(reference, region.getOffset());
 			if(expression != null){
 				ELResolver[] resolvers = context.getElResolvers();
 				for(ELResolver resolver : resolvers){
 					ELResolution resolution = resolver.resolve(context, expression, region.getOffset());
 					if(resolution != null){
 						ELSegment segment = resolution.findSegmentByOffset(region.getOffset()-reference.getStartPosition());
 	
 						if(segment != null && segment.isResolved()){
 //							return new IHyperlink[]{new ELHyperlink(textViewer.getDocument(), reference, segment)};
 							links.add(new ELHyperlink(textViewer.getDocument(), reference, segment));
 						}
 						
 					}
 				}
 			}
 			
 		}
 		if (links.size() == 0)
 			return null;
 		return (IHyperlink[])links.toArray(new IHyperlink[links.size()]);
 	}
 	
 	/**
 	 * Returns EL Invocation Expression of EL reference by offset
 	 * 
 	 * @param reference
 	 * @param offset
 	 * @return
 	 */
 	public static ELInvocationExpression findInvocationExpressionByOffset(ELReference reference, int offset){
 		ELExpression[] expressions = reference.getEl();
 		for(ELExpression expression : expressions){
 			if(reference.getStartPosition()+expression.getStartPosition() <= offset && reference.getStartPosition()+expression.getEndPosition() > offset){
 				ELInvocationExpression invocation = findInvocationExpressionByOffset(reference, expression, offset);
 				if(invocation != null)
 					return invocation;
 			}
 		}
 		return null;
 	}
 	
 	private static ELInvocationExpression findInvocationExpressionByOffset(ELReference reference, ELExpression expression, int offset){
 		List<ELInvocationExpression> invocations = expression.getInvocations();
 		for(ELInvocationExpression invocation : invocations){
 			if(reference.getStartPosition()+invocation.getStartPosition() <= offset && reference.getStartPosition()+invocation.getEndPosition() > offset)
				return invocation;
 		}
		return null;
 	}
 	
 	private IFile getFile(){
 		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(part != null){
 			IEditorInput input = part.getEditorInput();
 			if(input instanceof FileEditorInput)
 				return ((FileEditorInput)input).getFile();
 		}
 		return null;
 	}
 	
 }
