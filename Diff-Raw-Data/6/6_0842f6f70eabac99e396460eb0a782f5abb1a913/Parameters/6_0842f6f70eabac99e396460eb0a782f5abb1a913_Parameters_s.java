 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.steps.codesremoval;
 
 import net.sf.okapi.common.BaseParameters;
 import net.sf.okapi.common.EditorFor;
 import net.sf.okapi.common.ParametersDescription;
 import net.sf.okapi.common.uidescription.EditorDescription;
 import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
 import net.sf.okapi.common.uidescription.ListSelectionPart;
 
 @EditorFor(Parameters.class)
 public class Parameters extends BaseParameters implements IEditorDescriptionProvider {
 	
 	public static final int REMOVECODE_KEEPCONTENT = 0;
 	public static final int KEEPCODE_REMOVECONTENT = 1;
 	public static final int REMOVECODE_REMOVECONTENT = 2;
 	
 	private static final String STRIPSOURCE = "stripSource";
 	private static final String STRIPTARGET = "stripTarget";
 	private static final String MODE = "mode";
 	private static final String INCLUDENONTRANSLATABLE = "includeNonTranslatable";
 	
 	private boolean stripSource;
 	private boolean stripTarget;
 	private int mode;
 	private boolean includeNonTranslatable;
 	
 	public Parameters () {
 		reset();
 	}
 	
 	public boolean getStripSource () {
 		return stripSource;
 	}
 	
 	public void setStripSource (boolean stripSource) {
 		this.stripSource = stripSource;
 	}
 
 	public boolean getStripTarget () {
 		return stripTarget;
 	}
 	
 	public void setStripTarget (boolean stripTarget) {
 		this.stripTarget = stripTarget;
 	}
 
 	public int getMode () {
 		return mode;
 	}
 	
 	public void setMode (int mode) {
 		this.mode = mode;
 	}
 
 	public boolean getIncludeNonTranslatable () {
 		return includeNonTranslatable;
 	}
 
 	public void setIncludeNonTranslatable (boolean includeNonTranslatable) {
 		this.includeNonTranslatable = includeNonTranslatable;
 	}
 
 	public void reset() {
 		stripSource = true;
 		stripTarget = true;
 		mode = REMOVECODE_REMOVECONTENT;
 		includeNonTranslatable = true;
 	}
 
 	public void fromString (String data) {
 		reset();
 		buffer.fromString(data);
 		stripSource = buffer.getBoolean(STRIPSOURCE, stripSource);
 		stripTarget = buffer.getBoolean(STRIPTARGET, stripTarget);
 		mode = buffer.getInteger(MODE, mode);
 		includeNonTranslatable = buffer.getBoolean(INCLUDENONTRANSLATABLE, includeNonTranslatable);
 	}
 
 	@Override
 	public String toString() {
 		buffer.reset();
 		buffer.setBoolean(STRIPSOURCE, stripSource);
 		buffer.setBoolean(STRIPTARGET, stripTarget);
 		buffer.setInteger(MODE, mode);
 		buffer.setBoolean(INCLUDENONTRANSLATABLE, includeNonTranslatable);
 		return buffer.toString();
 	}
 
 	@Override
 	public ParametersDescription getParametersDescription () {
 		ParametersDescription desc = new ParametersDescription(this);
 		desc.add(MODE, "What to remove", "Select what parts of the inline codes to remove");
 		desc.add(STRIPSOURCE, "Strip codes in the source text", null);
 		desc.add(STRIPTARGET, "Strip codes in the target text", null);
 		desc.add(INCLUDENONTRANSLATABLE, "Apply to non-translatable text units", null);
 		return desc;
 	}
 	
 	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
 		EditorDescription desc = new EditorDescription("Codes Removal", true, false);
 
 		String[] values = {"0", "1", "2"};
 		String[] labels = {
			"Remove code marker, but keep code content  (\"<ph id='1'>[X]</ph>\" ==> \"[X]\")",
			"Remove code content, but keep code marker  (\"<ph id='1'>[X]</ph>\" ==> \"<ph id='1'/>\")",
			"Remove code marker and code content  (\"<ph id='1'>[X]</ph>\" ==> \"\")",
 		};
 		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(MODE), values);
 		lsp.setChoicesLabels(labels);
 
 		desc.addCheckboxPart(paramDesc.get(STRIPSOURCE));
 		desc.addCheckboxPart(paramDesc.get(STRIPTARGET));
 		desc.addCheckboxPart(paramDesc.get(INCLUDENONTRANSLATABLE));
 		
 		return desc;
 	}
 
 }
