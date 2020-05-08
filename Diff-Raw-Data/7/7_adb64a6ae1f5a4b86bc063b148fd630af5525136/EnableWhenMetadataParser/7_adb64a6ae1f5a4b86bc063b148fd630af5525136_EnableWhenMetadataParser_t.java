 /**
  *   Copyright (c) 2013 Atos
  *   All rights reserved. This program and the accompanying materials
  *   are made available under the terms of the Eclipse Public License v1.0
  *   which accompanies this distribution, and is available at
  *   http://www.eclipse.org/legal/epl-v10.html
  *  
  *   Contributors:
  *       Arthur Daussy - initial implementation
  */
 package org.eclipse.escriptmonkey.scripting.ui.metadata;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.eclipse.escriptmonkey.scripting.storedscript.metada.AbstractRegexMetadataParser;
 
 
 /**
  * Used to parse Enable When metadata
 * 
  * @author adaussy
 * 
  */
 public class EnableWhenMetadataParser extends AbstractRegexMetadataParser {
 
 
 	@Override
 	protected Pattern createPattern() {
		return Pattern.compile("EnableWhen::(.*)::", Pattern.DOTALL);
 	}
 
 	@Override
 	public List<String> parserMetadata(String header) {
 		String newHeader = header.replace("\n", "");
 		return super.parserMetadata(newHeader);
 	}
 
 
 }
