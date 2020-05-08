 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.templates;
 
import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.templates.DocumentTemplateContext;
 import org.eclipse.jface.text.templates.Template;
 import org.eclipse.jface.text.templates.TemplateBuffer;
 import org.eclipse.jface.text.templates.TemplateContextType;
 import org.eclipse.jface.text.templates.TemplateException;
 
 public class ScriptTemplateContext extends DocumentTemplateContext {
 	private ISourceModule sourceModule;
 
 	protected ScriptTemplateContext(TemplateContextType type,
 			IDocument document, int completionOffset, int completionLength,
 			ISourceModule sourceModule) {
 		super(type, document, completionOffset, completionLength);
 
 		if (sourceModule == null) {
 			throw new IllegalArgumentException();
 		}
 
 		this.sourceModule = sourceModule;
 	}
 
 	public final ISourceModule getSourceModule() {
 		return sourceModule;
 	}
 
 	protected static String calculateIndent(IDocument document, int offset) {
 		try {
 			IRegion region = document.getLineInformationOfOffset(offset);
 			String indent = document.get(region.getOffset(), offset
 					- region.getOffset());
 
 			StringBuffer sb = new StringBuffer();
 			for (int i = 0; i < indent.length(); ++i) {
 				char ch = indent.charAt(i);
 				if (ch != ' ' && ch != '\t') {
 					sb.append(' ');
 				} else {
 					sb.append(ch);
 				}
 			}
 
 			return sb.toString();
 		} catch (BadLocationException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
 		}
 
 		return "";
 	}
 
 	public TemplateBuffer evaluate(Template template)
 			throws BadLocationException, TemplateException {
 		if (!canEvaluate(template)) {
 			return null;
 		}
 
 		final String indentTo = calculateIndent(getDocument(), getStart());
 
 		String delimeter = TextUtilities.getDefaultLineDelimiter(getDocument());
 		String[] lines = template.getPattern().split("\n");
 
 		if (lines.length > 1 && indentTo != null && indentTo.length() > 0) {
 			StringBuffer buffer = new StringBuffer(lines[0]);
 
 			// Except first line
 			for (int i = 1; i < lines.length; i++) {
 				buffer.append(delimeter);
 				buffer.append(indentTo);
 				buffer.append(lines[i]);
 			}
 
 			template = new Template(template.getName(), template
 					.getDescription(), template.getContextTypeId(), buffer
 					.toString(), template.isAutoInsertable());
 		}
 
 		return super.evaluate(template);
 	}
 }
