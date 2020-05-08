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
 
 import java.io.IOException;
 
 import om.*;
 import om.question.ActionParams;
 import om.stdquestion.*;
 
 import org.w3c.dom.Element;
 
 import util.misc.IO;
 import util.xml.XML;
 
 /**
 Inserts a pushbutton that users can press to open the JME (Java Molecular
 Editor) component. A popup window will appear, containing the JME itself plus
 'OK' button.
 <p>
 If the user clicks 'OK', the internal SMILES value is updated and
 (if there is an action set) the form will be submitted.
 <p>
 The popup remains visible during one question. If the user begins a new
 question (or restarts the same one) it will vanish. It will also vanish if they
 navigate to another website.
 <p>
 Note that the JME applet itself is not OU software, but apparently we have
 an agreement to use it (for free, I think; it is normally free only for
 noncommercial use). <a href="http://www.molinspiration.com/jme/">JME website</a>.
 <h2>XML usage</h2>
 &lt;jme id='myjme' action='actionSubmit'/&gt;
 <h2>Properties</h2>
 <table border="1">
 <tr><th>Property</th><th>Values</th><th>Effect</th></tr>
 <tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
 <tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
 <tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates all children</td></tr>
 <tr><td>lang</td><td>(string)</td><td>Specifies the language of the content, like the HTML lang attribute. For example 'en' = English, 'el' - Greek, ...</td></tr>
 <tr><td>label</td><td>(string)</td><td>Label for button (default is 'Draw molecule')</td></tr>
 <tr><td>action</td><td>(string)</td><td>Name of method in question class that
   is called after user clicks OK. Optional, default is to not submit form, but
   you probably want to do it.</td></tr>
 </table>
 */
 public class JMEComponent extends QComponent
 {
 	/** Name of method in question class that is called after user clicks OK */
 	public static final String PROPERTY_ACTION="action";
 	/** Label for button. */
 	public static final String PROPERTY_LABEL="label";
 
 	/** Current (most recently set) value */
 	private String sValue;
 
 	/** Random token used to check when user goes to different window */
	private String sToken="t"+Math.random();
 
 	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
 	public static String getTagName()
 	{
 		return "jme";
 	}
 
 	@Override
 	protected void defineProperties() throws OmDeveloperException
 	{
 		super.defineProperties();
 		defineString(PROPERTY_LABEL);
 		defineString(PROPERTY_ACTION);
 		setString(PROPERTY_LABEL,"Draw molecule");
 	}
 
 	@Override
 	protected void initChildren(Element eThis) throws OmException
 	{
 		if(XML.getChildren(eThis).length!=0)
 			throw new OmFormatException("<jme>: Cannot contain child components");
 	}
 
 	@Override
 	protected void initSpecific(Element eThis) throws OmException
 	{
 		getQuestion().checkCallback(getString(PROPERTY_ACTION));
 	}
 
 	@Override
 	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
 	{
 		Element eScript=qc.createElement("script");
 		eScript.setAttribute("type","text/javascript");
 		XML.createText(eScript,"jmeInit('"+sToken+"');");
 		qc.addInlineXHTML(eScript);
 
 		Element eInput=qc.createElement("input");
 		eInput.setAttribute("type","hidden");
 		String sInputID=QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID();
 		eInput.setAttribute("name",sInputID);
 		eInput.setAttribute("id",sInputID);
 		qc.addInlineXHTML(eInput);
 
 		if(isPropertySet(PROPERTY_ACTION))
 		{
 			eInput=qc.createElement("input");
 			eInput.setAttribute("type","hidden");
 			String sActionID=QDocument.ID_PREFIX+QDocument.ACTION_PREFIX+getID();
 			eInput.setAttribute("name",sActionID);
 			eInput.setAttribute("id",sActionID);
 			eInput.setAttribute("value","submit");
 			eInput.setAttribute("disabled","disabled"); // Disabled unless submitted this way
 			qc.addInlineXHTML(eInput);
 		}
 
 		eInput=qc.createElement("input");
 		String sButtonID=QDocument.ID_PREFIX+getID()+"_button";
 		eInput.setAttribute("id",sButtonID);
 		eInput.setAttribute("type","button");
 		eInput.setAttribute("value",getString(PROPERTY_LABEL));
 		if(!isEnabled()) eInput.setAttribute("disabled","disabled");
 		eInput.setAttribute("onclick","jmeClick('%%RESOURCES%%','"+getID()+"','"+QDocument.ID_PREFIX+"')");
 		qc.addInlineXHTML(eInput);
 		if(isEnabled())	qc.informFocusable(sButtonID,bPlain);
 
 		if(bInit)
 		{
 			try
 			{
 				qc.addResource("jme.jar","application/java-archive",
 					IO.loadResource(JMEComponent.class,"jme.jar"));
 			}
 			catch(IllegalArgumentException e)
 			{
 				throw new OmException("Error loading jme.jar",e);
 			}
 			catch(IOException e)
 			{
 				throw new OmException("Error loading jme.jar",e);
 			}
 		}
 	}
 
 	/**
 	 * @return SMILES string that was set, or empty string if none was set.
 	 */
 	public String getSMILES()
 	{
 		return sValue;
 	}
 
 	@Override
 	protected void formSetValue(String newValue,ActionParams ap) throws OmException
 	{
 		this.sValue=newValue;
 	}
 
 	@Override
 	protected void formCallAction(String newValue,ActionParams ap) throws OmException
 	{
 		getQuestion().callback(getString(PROPERTY_ACTION));
 	}
 }
