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
 package om.stdquestion;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.*;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 
 import om.OmDeveloperException;
 import om.OmUnexpectedException;
 import om.question.Resource;
 
 import org.w3c.dom.*;
 
 import util.xml.XML;
 
 /**
  * Represents current content provided by a question component for use in
  * output to user.
  */
 public class QContent
 {
 	/** Output document, used for creating elements etc */
 	private Document dOutput;
 
 	/** Element that marks root of stuff which is added inline */
 	private Element eInlineRoot;
 
 	/** First node that was added to top level */
 	private Node nFirstTopLevel=null;
 
 	/**
 	 * Stack of Element; top item is current parent that new nodes should be
 	 * added to.
 	 */
 	private Stack<Element> sParents=new Stack<Element>();
 
 	/** List of returned resources (QResource) */
 	private List<Resource> lResources=new LinkedList<Resource>();
 
 	/** Stack of text-mode StringBuffers, zero-length if not in text mode */
 	private Stack<StringBuffer> sTextMode=new Stack<StringBuffer>();
 
 	/** MIME type for PNG images */
 	public final static String MIME_PNG="image/png";
 
 	/** Regex matcher for legal filenames */
 	private final static Pattern FILENAMES=Pattern.compile("^[a-z0-9-_.]+$");
 
 	/**
 	 * Constructs blank question content.
 	 * @param dOutput Output document, used for creating elements etc
 	 */
 	public QContent(Document dOutput)
 	{
 		this.dOutput=dOutput;
 		eInlineRoot=dOutput.createElement("div");
 		eInlineRoot.setAttribute("id","om");
 		eInlineRoot.setAttribute("class","om");
 		sParents.push(eInlineRoot);
 	}
 
 	/** @return Output document, used for creating elements etc */
 	public Document getOutputDocument()
 	{
 		return dOutput;
 	}
 
 	/**
 	 * Creates element using output document. (Just a shortcut.)
 	 * @param sTagName Tag name of desired element
 	 * @return New element
 	 */
 	public Element createElement(String sTagName)
 	{
 		return dOutput.createElement(sTagName);
 	}
 
 	/** @return Output XHTML element (div tag w/ id='om') */
 	public Element getXHTML()
 	{
 		return eInlineRoot;
 	}
 
 	/** @return List of added resources */
 	public Resource[] getResources()
 	{
 		return lResources.toArray(new Resource[0]);
 	}
 
 	/**
 	 * Checks that a node comes from the right document.
 	 * @param n Input node
 	 * @throws OmDeveloperException If the node came from somewhere else
 	 */
 	private void checkNode(Node n) throws OmDeveloperException
 	{
 		if(n.getOwnerDocument()!=getOutputDocument())
 			throw new IllegalArgumentException(
 				"Node must be created with owner document from QContent");
 	}
 
 	/**
 	 * Call to add an XHTML node that will be placed in the document flow at the place
 	 * where this component is positioned.
 	 * @param n Root node of content, which should've been created with respect
 	 *   to the getDocument(). Content will not be cloned, so be sure to create it
 	 *   afresh each time.
 	 * @throws OmDeveloperException
 	 */
 	public void addInlineXHTML(Node n) throws OmDeveloperException
 	{
 		if(!sTextMode.empty()) return; // Ignore in text mode
 		checkNode(n);
 		Element eParent=sParents.peek();
 		if(eParent==eInlineRoot && nFirstTopLevel!=null)
 		{
 			// Make sure it stays before the 'at end' stuff
 			eParent.insertBefore(n,nFirstTopLevel);
 		}
 		else
 		{
 			eParent.appendChild(n);
 		}
 	}
 
 	/**
 	 * Sets the parent for any calls from now on. After this call, ensure that
 	 * unsetParent is always called precisely once, unless an exception is
 	 * thrown.
 	 * @param e New parent element (must have been added already as part of
 	 *   addInlineXHTML call!)
 	 */
 	public void setParent(Element e)
 	{
 		if(!sTextMode.empty()) return; // Ignore in text mode
 		sParents.push(e);
 	}
 
 	/**
 	 * Unsets the parent, returning to previous parent.
 	 */
 	public void unsetParent()
 	{
 		if(!sTextMode.empty()) return; // Ignore in text mode
 		sParents.pop();
 	}
 
 	/**
 	 * Call to set XHTML that will be placed at the top level in the output
 	 * document (i.e. at the end of, and a direct child of, the question div).
 	 * @param n Root node of content, which should've been created with respect
 	 *   to the getDocument(). Content will not be cloned, so be sure to create it
 	 *   afresh each time.
 	 * @throws OmDeveloperException
 	 */
 	public void addTopLevelXHTML(Node n) throws OmDeveloperException
 	{
 		if(!sTextMode.empty()) return; // Ignore in text mode
 		checkNode(n);
 		eInlineRoot.appendChild(n);
 		if(nFirstTopLevel==null) nFirstTopLevel=n;
 	}
 
 	/**
 	 * Adds a resource to the output.
 	 * <p>
 	 * A question does not need to add the same resource more than once within its
 	 * lifecycle, i.e. resources do not need to be added with every request.
 	 * You may not add a second resource with the same name as a first, unless it
 	 * is identical.
 	 * @param sFilename Resource filename
 	 * @param sMimeType MIME type of resource (see QContent.MIME* constants)
 	 * @param abContent Content of resource (QContent.convert*() or QContent.loadResource() may help here)
 	 * @throws IllegalArgumentException If the filename isn't valid
 	 */
 	public void addResource(
 		String sFilename,String sMimeType,byte[] abContent) throws IllegalArgumentException
 	{
 		if(!sTextMode.empty()) return; // Ignore in text mode, don't need resources
 		if(!FILENAMES.matcher(sFilename).matches())
 			throw new IllegalArgumentException("Not a valid resource filename: "+sFilename);
 
		for(Iterator i=lResources.iterator();i.hasNext();)
 		{
			Resource r=(Resource)i.next();
 			if(r.getFilename().equals(sFilename))
 			{
 				if(!Arrays.equals(abContent,r.getContent()))
 				{
 					throw new IllegalArgumentException(
 						"A different resource has already been added with this filename: "+sFilename);
 				}
 				// OK it was the same, don't add it again
 				return;
 			}
 		}
 		lResources.add(new Resource(sFilename,sMimeType,abContent));
 	}
 
 	/**
 	 * Adds a resource to the output, loading it from the same location as the
 	 * specified reference class file. (This is a shortcut for calling loadResource
 	 * and the other addResource method.)
 	 * <p>
 	 * A question does not need to add the same resource more than once within its
 	 * lifecycle, i.e. resources do not need to be added with every request.
 	 * You may not add a second resource with the same name as a first, unless it
 	 * is identical.
 	 * @param sq Question to load resource from
 	 * @param sFilename Resource filename or path
 	 * @param sMimeType MIME type of resource (see QContent.MIME* constants)
 	 * @throws IOException
 	 * @throws IllegalArgumentException If the filename isn't valid
 	 */
 	public void addResource(
 		StandardQuestion sq,String sFilename,String sMimeType)
 	 	throws IOException,IllegalArgumentException
 	{
 		addResource(sFilename,sMimeType,sq.loadResource(sFilename));
 	}
 
 	/**
 	 * Informs the system that a given XHTML element may be focused on page load.
 	 * (This causes appropriate Javascript to be written so that the first such
 	 * candidate is focused. The point of doing it here is that components don't
 	 * need to know whether they are first or not.)
 	 * @param sID ID that will be stored along with the object (for use in JS)
 	 * @param sJSObjectExpression JS expression that will obtain object
 	 *   e.g. document.getElementById('..') - but use the other method if you
 	 *   just want that behaviour. Expression may contain single, but not double,
 	 *   quotes (unless you escape double quotes).
 	 * @param bPlain True if in plain mode (causes nothing to happen)
 	 * @throws OmDeveloperException
 	 */
 	public void informFocusableFullJS(String sID,String sJSObjectExpression,boolean bPlain) throws OmDeveloperException
 	{
 		if(!sTextMode.empty()) return; // Ignore in text mode, definitely not focusable
 		if(bPlain) return;
 
 		Element eScript=createElement("script");
 		eScript.setAttribute("type","text/javascript");
 		XML.createText(eScript,"addFocusable('"+sID+"','"+
 			sJSObjectExpression.replaceAll("'","\\\\'")+"');");
 		addInlineXHTML(eScript);
 	}
 
 	/**
 	 * Informs the system that a given XHTML element may be focused on page load.
 	 * (This causes appropriate Javascript to be written so that the first such
 	 * candidate is focused. The point of doing it here is that components don't
 	 * need to know whether they are first or not.)
 	 * @param sXHTMLID ID (within XHTML id attribute) of the element that should
 	 *   be focused
 	 * @param bPlain True if in plain mode (causes nothing to happen)
 	 * @throws OmDeveloperException
 	 */
 	public void informFocusable(String sXHTMLID,boolean bPlain) throws OmDeveloperException
 	{
 		informFocusableFullJS(sXHTMLID,"document.getElementById('"+sXHTMLID+"')",bPlain);
 	}
 
 	/**
 	 * Converts an image to a PNG file.
 	 * @param bi BufferedImage to convert
 	 * @return PNG data
 	 * @throws OmUnexpectedException Any error in conversion (shouldn't happen, but...)
 	 */
 	public static byte[] convertPNG(BufferedImage bi) throws OmUnexpectedException
 	{
 		try
 		{
 			ByteArrayOutputStream baos=new ByteArrayOutputStream();
 			ImageIO.setUseCache(false);
 			if(!ImageIO.write(bi,"png",baos))
 				throw new IOException("No image writer for PNG");
 			return baos.toByteArray();
 		}
 		catch(IOException ioe)
 		{
 			throw new OmUnexpectedException(ioe);
 		}
 	}
 
 	/**
 	 * Converts an image to a .jpg file using default compression level.
 	 * @param bi BufferedImage to convert
 	 * @return JPG data
 	 * @throws OmUnexpectedException Any error in conversion (shouldn't happen, but...)
 	 */
 	public static byte[] convertJPG(BufferedImage bi) throws OmUnexpectedException
 	{
 		try
 		{
 			ByteArrayOutputStream baos=new ByteArrayOutputStream();
 			ImageIO.setUseCache(false);
 			if(!ImageIO.write(bi,"jpg",baos))
 				throw new IOException("No image writer for JPG");
 			return baos.toByteArray();
 		}
 		catch(IOException ioe)
 		{
 			throw new OmUnexpectedException(ioe);
 		}
 	}
 
 	/**
 	 * Turns on text mode, in which any XHTML changes are ignored, and
 	 * we start building up the text-equivalent string instead.
 	 * <p>
 	 * Nesting text mode (calling beginTextMode when already in text mode) is
 	 * OK, this starts a separate nested text-equivalent buffer. Begin and end
 	 * calls must be paired.
 	 */
 	public void beginTextMode()
 	{
 		sTextMode.push(new StringBuffer());
 	}
 
 	/**
 	 * Call to add text equivalent. All components should support text equivalents
 	 * by using this method as well as addInlineXHTML().
 	 * <p>
 	 * The system will automatically add whitespace at the end of this string as
 	 * necessary so that text from different components doesn't run together.
 	 * @param s Text to add
 	 */
 	public void addTextEquivalent(String s)
 	{
 		// Ignore unless in text mode
 		if(sTextMode.empty()) return;
 
 		// Ignore empty strings
 		if(s.length()==0) return;
 
 		StringBuffer sb=sTextMode.peek();
 
 		// Add string followed by space if it hasn't got one (so sbTextMode always
 		// ends with whitespace - we trim this later)
 		sb.append(s);
 		if(!Character.isWhitespace(s.charAt(s.length()-1)))
 			sb.append(" ");
 	}
 
 	/**
 	 * Turns off text mode. From this point on, XHTML changes will be included
 	 * and text-equivalent strings ignored again. (Unless it's nested in text
 	 * mode more than once.)
 	 * @return The trimmed contents of the text-equivalent string that was
 	 *   built up since the beginTextMode() call
 	 * @throws OmDeveloperException If you aren't in text mode
 	 */
 	public String endTextMode() throws OmDeveloperException
 	{
 		if(sTextMode.empty()) throw new OmDeveloperException("Not in text mode");
 
 		StringBuffer sb=sTextMode.pop();
 		return sb.toString().trim().replaceAll("\\s+"," ");
 	}
 
 }
