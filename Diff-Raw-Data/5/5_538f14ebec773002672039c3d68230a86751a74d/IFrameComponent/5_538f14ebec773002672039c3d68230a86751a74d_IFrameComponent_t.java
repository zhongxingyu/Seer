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
 import java.util.HashSet;
 import java.util.Set;
 
 import om.*;
 import om.question.ActionParams;
 import om.stdquestion.*;
 
 import org.w3c.dom.*;
 
 import util.misc.*;
 import util.xml.XML;
 import util.xml.XMLException;
 
 /**
 Represents an iframe
 <p/>
 <h2>XML usage</h2>
 &lt;iframe id="myFrame" src="http://www.site/page.html" width="100" height="80"/&gt;
 <p/>
 or (for an html file contained in question jar):
 <p/>
 &lt;iframe id="myFrame" src="page.html" width="100" height="80" showResponse="yes"/&gt;
 <p/>
 <h2>Properties</h2>
 <table border="1">
 <tr><th>Property</th><th>Values</th><th>Effect</th></tr>
 <tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
 <tr><td>width</td><td>(int)</td><td>default displayed width in pixels (optional)</td></tr>
 <tr><td>height</td><td>(int)</td><td>default displayedheight in pixels (optional)</td></tr>
 <tr><td>src</td><td>(string)</td><td>html src contents of iframe</td></tr>
 </table>
 */
 public class IFrameComponent extends QComponent
 {
 	public static final String PROPERTY_ACTION="action";
 
 	private static final String PROPERTY_SRC="src";
 	private static final String PROPERTY_WIDTH="width";
 	private static final String PROPERTY_HEIGHT="height";
 	private static final String PROPERTY_SHOWRESPONSE="showResponse";///w
 	private static final String BUTTON_LABEL=" Enter answer";
 
 	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
 	public static String getTagName()
 	{
 		return "iframe";
 	}
 
 	/** path to image file */
 	private String sSrc=null; // Currently-loaded file
 	private int iWidth = 300;
 	private int iHeight = 300;
 	private byte[] movieData;
 	private String sMimeType;
 
 	/** Current (most recently set) value */
 	private String sValue;
 
 	/** Random token used to check when user goes to different window */
 	private String sToken;
 
 	/**
 	 * Keep track of resources we added to users so we can save SOAP time by
 	 * not transferring them again.
 	 */
 	private Set<String> sAddedResources=new HashSet<String>();
 
 	/** True if there was whitespace before or after the &lt;flash&gt; tag */
 	private boolean bSpaceBefore,bSpaceAfter;
 
 	/** Specifies attributes required */
 	@Override
 	protected String[] getRequiredAttributes()
 	{
 		return new String[] {PROPERTY_SRC, PROPERTY_WIDTH,PROPERTY_HEIGHT};
 	}
 
 	/** Specifies possible attributes */
 	@Override
 	protected void defineProperties() throws OmDeveloperException
 	{
 		super.defineProperties();
 		defineString(PROPERTY_ACTION);
 		defineString(PROPERTY_SRC);
 		defineInteger(PROPERTY_WIDTH);
 		defineInteger(PROPERTY_HEIGHT);
 		defineString(PROPERTY_SHOWRESPONSE);///w
 		
 
 	}
 
 	/** parses internals of tag to create java component*/
 	@Override
 	protected void initChildren(Element eThis) throws OmException
 	{
 		Node nPrevious=eThis.getPreviousSibling();
 		if(nPrevious!=null && nPrevious instanceof Text)
 		{
 			String sText=((Text)nPrevious).getData();
 			if(sText.length()>0 && Character.isWhitespace(sText.charAt(sText.length()-1)))
 				bSpaceBefore=true;
 		}
 		Node nAfter=eThis.getNextSibling();
 		if(nAfter!=null && nAfter instanceof Text)
 		{
 			String sText=((Text)nAfter).getData();
 			if(sText.length()>0 && Character.isWhitespace(sText.charAt(0)))
 				bSpaceAfter=true;
 		}
 	}
 
 	boolean external, showResponse;///w
 
 	@Override
 	protected void initSpecific(Element eThis) throws OmException
 	{
 		sToken="t"+getQuestion().getRandom().nextInt()+getID().hashCode();
 
 		showResponse=!getString(PROPERTY_SHOWRESPONSE).equalsIgnoreCase("no");///w
 		
 		external=getString(PROPERTY_SRC).substring(0,4).equalsIgnoreCase("http");
 		/* external means the page is not in the question jar, we want to keep this definition
 		 * because internal pages can send data back to OM, externals cant - SECURITY!! 
 		 */
 		if(!external && showResponse)getQuestion().checkCallback(getString(PROPERTY_ACTION));///w
 	}
 
 	/** @return SRC of Image
 	 * @throws OmDeveloperException */
 	public String getSRC() throws OmDeveloperException
 	{
 		return getString(PROPERTY_SRC);
 	}
 
 	/**
 	 * Sets the src file path.
 	 * <p>
 	 * @param sSrc New value for src
 	 * @throws OmDeveloperException -- when?
 	 */
 	public void setSrc(String sSrc) throws OmDeveloperException
 	{
 		setString(PROPERTY_SRC, sSrc);
 	}
 
 	@Override
 	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
 	{
 
 		double dZoom=getQuestion().getZoom();
 
 			iWidth = getInteger("width");
 			iHeight = getInteger("height");
 
 			sSrc=getString(PROPERTY_SRC);
 			
 			// iframe tag
 			int
 			iActualWidth=(int)(iWidth*dZoom+0.5),
 			iActualHeight=(int)(iHeight*dZoom+0.5);
 			
 			Element eInput=qc.createElement("input");
 			eInput.setAttribute("type","hidden");
 
 			String sInputID=QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID();
 	
 			eInput.setAttribute("name",sInputID);
 			eInput.setAttribute("id",sInputID);
 			qc.addInlineXHTML(eInput);
 	
 			/*if not external, set sup tag that contains information that is passed from
 			user to OM. we dont do this if frame source is not in the question jar
 			
 			only accept input from the containing url if NOT external, this is a security 
 			feature to prevent response string being hijacked. If change this ensure other
 			 security precautions are taken
 			
 			*
 			*/
 			
 			if(isPropertySet(PROPERTY_ACTION) && !external && showResponse)///w
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
 			/* setting up the tag */
 			if(bInit && !external)		{
 				try
 				{
 					qc.addResource(sSrc,"html", getQuestion().loadResource(sSrc));
 				}
 				catch(IllegalArgumentException e)
 				{
 					throw new OmException("Error loading html",e);
 				}
 				catch(IOException e)
 				{
 					throw new OmException("Error loading html",e);
 				}
 			}
 
 			Element eEnsureSpaces=qc.createElement("iframe");
 			eEnsureSpaces.setAttribute("src",(external?"":"%%RESOURCES%%/")+sSrc);
 			eEnsureSpaces.setAttribute("id","IF"+getID());
 			eEnsureSpaces.setAttribute("height",""+iActualHeight);
 			eEnsureSpaces.setAttribute("width",""+iActualWidth);
 			
 			eEnsureSpaces.setAttribute("marginwidth","0");///w
 			eEnsureSpaces.setAttribute("marginheight","0");///w
 			
 			eEnsureSpaces.setAttribute("frameborder","0");
 			qc.addInlineXHTML(eEnsureSpaces);
 			
			/* setting up enter answer button tag for passing information 
 			if(!external && showResponse){///w
 				qc.addTextEquivalent("<br/>");
 				Element okTag=qc.createElement("input");
 				okTag.setAttribute("type","submit");
 				okTag.setAttribute("id","enterB");
 				okTag.setAttribute("value",BUTTON_LABEL);
 				okTag.setAttribute("onclick", 
 						"if(this.hasSubmitted) { return false; } this.hasSubmitted=true; "+
 						"sendResponse('"
 					+sInputID+"','"+QDocument.ID_PREFIX+"',"
 					+"'IF"+getID()+"'"
 					+");"
 					);
 				if(!isEnabled()) okTag.setAttribute("disabled","yes");
 
 				qc.addInlineXHTML(okTag);
			}*/
 
 			// If there's a space before, add one here too (otherwise IE eats it)
 			if(bSpaceBefore)
 				XML.createText(eEnsureSpaces," ");
 
 			if(bSpaceAfter)
 				XML.createText(eEnsureSpaces," ");
 	}
 
 	public String getResponse()
 	{
 		return util.misc.IO.CleanString(sValue);
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
 	
 	
 } // end of iframe Component class
