 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ruby.ui.formatter;
 
 import java.util.Map;
 
 import org.eclipse.dltk.ruby.internal.ui.formatting.OldCodeFormatter;
 import org.eclipse.dltk.ui.formatter.AbstractScriptFormatterFactory;
 import org.eclipse.dltk.ui.formatter.IScriptFormatter;
import org.eclipse.jface.text.IDocument;
 import org.eclipse.text.edits.ReplaceEdit;
 import org.eclipse.text.edits.TextEdit;
 
 public class OldRubyFormatterFactory extends AbstractScriptFormatterFactory {
 
 	private static class OldFormatterWorker extends OldCodeFormatter implements
 			IScriptFormatter {
 
 		/**
 		 * @param options
 		 */
 		public OldFormatterWorker(Map options) {
 			super(options);
 		}
 
		public int detectIndentationLevel(IDocument document, int offset) {
			return 0;
		}

 		public TextEdit format(String source, int offset, int length,
 				int indentationLevel) {
 			return new ReplaceEdit(offset, length, formatString(source
 					.substring(offset, length), indentationLevel));
 		}
 
 	}
 
 	public IScriptFormatter createFormatter(String lineDelimiter,
 			Map preferences) {
 		return new OldFormatterWorker(preferences);
 	}
 
 	public boolean isValid() {
 		return OldCodeFormatter.DEBUG;
 	}
 
 	public String getPreferenceQualifier() {
 		return null;
 	}
 
 	public String[] getPreferenceKeys() {
 		return null;
 	}
 
 }
