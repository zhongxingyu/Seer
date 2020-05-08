 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.stdcomponent;
 
 import java.awt.Color;
 
 import om.*;
 import om.question.ActionParams;
 import om.stdquestion.*;
 
 import org.w3c.dom.Element;
 
 import util.xml.XML;
 
 /**
 A radiobox component. Consists of an actual radiobox with a coloured box that
 surrounds the radiobox and its content. The user can click anywhere in the box
 to toggle the check. (Consequently, the radiobox should not contain components that
 might require user input, as this behaviour is likely to break them.)
 <p/>
 If included within a {@link LayoutGridComponent}, the radiobox can automatically
 size itself to match other radioboxes on the same grid row.
 <h2>XML usage</h2>
 &lt;radiobox &gt;...&lt;/radiobox&gt;
 <h2>Properties</h2>
 <table border="1">
 <tr><th>Property</th><th>Values</th><th>Effect</th></tr>
 <tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
 <tr><td>group</td><td>(string)</td><td>Specifies radio group</td></tr>
 <tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
 <tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates this control</td></tr>
 <tr><td>checked</td><td>(boolean)</td><td>Whether or not the box is checked</td></tr>
 <tr><td>highlight</td><td>(boolean)</td><td>Whether to use the highlight colours for box</td></tr>
 </table>
 */
 
 //TODO Maybe merge radiobox with checkbox?
 //TODO Maybe use label tag for accessibillity etc.
 
 public class RadioBoxComponent extends QComponent
 {
 	/** Property name for being highlighted (boolean) */
 	public static final String PROPERTY_HIGHLIGHT="highlight";
 
 	/** Property name for value of radiobox (boolean) */
 	public final static String PROPERTY_CHECKED="checked";
 
 	/** Property name for radio groups (string) */
 	public final static String PROPERTY_GROUP="group";
 
 	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
 	public static String getTagName()
 	{
 		return "radiobox";
 	}
 
 	@Override
 	protected void defineProperties() throws OmDeveloperException
 	{
 		super.defineProperties();
 
 		defineBoolean(PROPERTY_CHECKED);
 		defineBoolean(PROPERTY_HIGHLIGHT);
 		setBoolean(PROPERTY_CHECKED,false);
 
 		defineString(PROPERTY_GROUP);
 		setString(PROPERTY_GROUP,"_rg");
 	}
 
 	@Override
 	protected void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
 	{
 		Element eOuterBox=qc.createElement("div");
 		qc.addInlineXHTML(eOuterBox);
 
 		if(!bPlain)
 		{
 			eOuterBox.setAttribute("class","radiobox");
 			eOuterBox.setAttribute("id",QDocument.ID_PREFIX+getID());
 			eOuterBox.setAttribute("onclick","radioBoxOnClick('rb_"+getID()+"','"+QDocument.ID_PREFIX+"');");
 
 			if(isHighlight())
 			{
 				eOuterBox.setAttribute("style",
 					"background-color:"+convertHash("innerbg")+";" +
 					"border-color:"+convertHash("text")+";");
 			}
 			else
 			{
 				eOuterBox.setAttribute("style",
 					"background-color:"+convertHash("innerbg")+";" +
 					"border-color:"+convertHash("innerbg")+";");
 			}
 		}
 
 		Element eInput=XML.createChild(eOuterBox,"input");
 		eInput.setAttribute("type","radio");
		eInput.setAttribute("value",QDocument.ID_PREFIX+getID());
		eInput.setAttribute("name",getString("group"));
 		if(!bPlain)
 		{
 			eInput.setAttribute("class","radioboxcheck");
 			eInput.setAttribute("id",QDocument.ID_PREFIX+"rb_"+getID());
 			// eInput.setAttribute("onclick","radioBoxOnClick('rb_"+getID()+"');");
 		}
 		if(isChecked()) eInput.setAttribute("checked","yes");
 		if(!isEnabled()) eInput.setAttribute("disabled","yes");
 
 		if(!bPlain)
 		{
 			Element eContents=XML.createChild(eOuterBox,"div");
 			eContents.setAttribute("class","radioboxcontents");
 			qc.setParent(eContents);
 		}
 		else
 		{
 			// Add a space, then add content direct to outer box (avoids having it
 			// appear on different line, prob. doesn't make any diff for screenreaders
 			// but whatever)
 			XML.createText(eOuterBox," ");
 			qc.setParent(eOuterBox);
 		}
 		qc.addTextEquivalent("[Radio button: ");
 		produceChildOutput(qc,bInit,bPlain);
 		qc.addTextEquivalent("]");
 		qc.unsetParent();
 
 		if(shouldFillParent() && !bPlain)
 		{
 			Element eScript=XML.createChild(eOuterBox,"script");
 			eScript.setAttribute("type","text/javascript");
 			XML.createText(eScript,"addOnLoad(function() { radioBoxFix('"+getID()+"','"+QDocument.ID_PREFIX+"');});");
 		}
 		if(isEnabled()) qc.informFocusable(eInput.getAttribute("id"),bPlain);
 	}
 
 	/** @return True if the radiobox was checked */
 	public boolean isChecked()
 	{
 		try
 		{
 			return getBoolean(PROPERTY_CHECKED);
 		}
 		catch(OmDeveloperException e)
 		{
 			throw new OmUnexpectedException(e);
 		}
 	}
 
 	/**
 	 * Checks the radiobox.
 	 * @param bChecked True to check it, false to uncheck
 	 */
 	public void setChecked(boolean bChecked)
 	{
 		try
 		{
 			setBoolean(PROPERTY_CHECKED,bChecked);
 		}
 		catch(OmDeveloperException e)
 		{
 			throw new OmUnexpectedException(e);
 		}
 	}
 
 	/** @return True if the radiobox is highlighted */
 	public boolean isHighlight()
 	{
 		try
 		{
 			if(isPropertySet(PROPERTY_HIGHLIGHT))
 				return getBoolean(PROPERTY_HIGHLIGHT);
 			else
 				// Default behaviour is to highlight when disabled and checked, this
 				// means that the selected radioboxes are shown more clearly alongside
 				// 'you got this wrong' pages
 				return isChecked() && !isEnabled();
 		}
 		catch(OmDeveloperException e)
 		{
 			throw new OmUnexpectedException(e);
 		}
 	}
 
 	/**
 	 * Sets the highlight value.
 	 * @param bHighlight True to highlight, false to unhighlight
 	 */
 	public void setHighlight(boolean bHighlight)
 	{
 		try
 		{
 			setBoolean(PROPERTY_HIGHLIGHT,bHighlight);
 		}
 		catch(OmDeveloperException e)
 		{
 			throw new OmUnexpectedException(e);
 		}
 	}
 
 	@Override
 	protected void formSetValue(String sValue,ActionParams ap) throws OmException
 	{
 		if(!isEnabled()) return;
 	}
 
 	@Override
 	protected void formAllValuesSet(ActionParams ap) throws OmException
 	{
 		if(!isEnabled()) return;
 
 		String group = getString("group");
 		if(ap.hasParameter(group))
 		{
 			String radioChoice = ap.getParameter(group);
 			boolean checked = radioChoice.equals(getID());
 			setChecked(checked);
 		}
 	}
 
 	@Override
 	protected Color getChildBackground(QComponent qcChild)
 	{
 		try
 		{
 			return convertRGB("innerbg");
 		}
 		catch(OmDeveloperException ode)
 		{
 			throw new OmUnexpectedException(ode);
 		}
 	}
 }
