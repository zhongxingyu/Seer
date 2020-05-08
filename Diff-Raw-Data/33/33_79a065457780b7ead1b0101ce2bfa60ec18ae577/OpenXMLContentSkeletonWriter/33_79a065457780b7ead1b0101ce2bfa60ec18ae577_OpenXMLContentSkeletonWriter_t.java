 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.openxml; // DWH 4-8-09
 
 import java.util.List;
 
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.filterwriter.ILayerProvider;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 
 // For this to work, expandCodeContent has to be changed to protected in GenericSkeletonWriter
 
 /**
  * <p>Implements ISkeletonWriter for OpenXMLContentFilter, which
  * filters Microsoft Office Word, Excel, and Powerpoint Documents.
  * OpenXML is the format of these documents.
  * 
  * <p>Since OpenXML files are Zip files that contain XML documents,
  * <b>OpenXMLZipFilter</b> handles opening and processing the zip file, and
  * instantiates this skeleton writer to process the XML documents.
  * 
  * <p>This skeleton writer exhibits slightly different behavior depending 
  * on whether the XML file is Word, Excel, Powerpoint, or a chart in Word.
  * If there is was no character style information in the original XML file, 
  * such as <w:r><w:t>text</w:t></w:r>, the tags were not made codes in
  * OpenXMLContentFilter, so these tags need to be reintroduced by this
  * skeleton writer.
  */
 
 public class OpenXMLContentSkeletonWriter extends GenericSkeletonWriter {
 
 	public final static int MSWORD=1;
 	public final static int MSEXCEL=2;
 	public final static int MSPOWERPOINT=3;
 	public final static int MSWORDCHART=4; // DWH 4-16-09
 	public final static int MSEXCELCOMMENT=5; // DWH 5-13-09
 	public final static int MSWORDDOCPROPERTIES=6; // DWH 5-25-09
 	private int configurationType; // DWH 4-10-09
 	private ILayerProvider layer;
 	private EncoderManager encoderManager;
 	private int nTextBoxLevel=0; // DWH 10-27-09
 	private boolean bInBlankText=false; // DWH 10-27-09
 	private int nContentDepth=0;
 
 	
 	public OpenXMLContentSkeletonWriter(int configurationType) // DWH 4-8-09
 	{
 		super();
 		this.configurationType = configurationType; // DWH 4-10-09
 		encoderManager = new EncoderManager(); // DWH 5-14-09
 		encoderManager.setDefaultOptions(null, "utf-8", "\n"); // DWH 5-14-09
 		encoderManager.updateEncoder(MimeTypeMapper.DOCX_MIME_TYPE); // DWH 5-14-09
 	}
 	
 	/**
 	 * Gets the content out of a coded text string.  If the text was "blank", i.e.
                * only surrounded by <w:r><w:t> and </w:t></w:r> in the original
                * input, these tags are restored around the translated text.  Codes are
                * expanded by calling expandCodeContent in GenericSkeletonWriter.
                * Text is not blank if it was surrounded by OPENING and CLOSING codes.
 	 * @param tf TextFragment containing the coded text to expand
 	 * @param langToUse output language to use, in en-US format
 	 * @param context same as context variable in GenericFilterWriter
 	 * @return text with all of the codes expanded and blank text surrounded
 	 */
 @Override
 	public String getContent (TextFragment tf,
 		LocaleId langToUse,
 		int context)
 	{
 		String sTuff; // DWH 4-8-09
 //		String text=tf.toString(); // DWH 5-18-09 commented 10-27-09
 		String text=tf.getCodedText(); // DWH 10-27-09 they changed toString to show all text
 		bInBlankText=false; // DWH 4-8-09
 		boolean bHasBlankInText=false; // DWH 5-28-09
 		int nSurroundingCodes=0; // DWH 4-8-09
 		String sPreserve=""; // DWH 5-28-09 xml:space="preserve" if there is a space
 		// Output simple text
 		if ( !tf.hasCode() ) {
 			if (text.length()>0)
 			{
 				sTuff = text; // DWH 5-22-09
 				if ( encoderManager == null ) // DWH 5-22-09 whole if-else: encode first
 				{
 					if ( layer != null )
 						sTuff = layer.encode(text, context);
 				}
 				else
 				{	
 					if ( layer == null )
 						sTuff = encoderManager.encode(text, context);
 					else
 						sTuff = layer.encode(encoderManager.encode(sTuff, context), context);
 				}
 				if (context==1) // DWH 5-22-09 add unencoded tags if needed
 				{
 					bHasBlankInText = ((text.indexOf(' ')>-1) || (text.indexOf('\u00A0')>-1)); // DWH 5-28-09
 					if (bHasBlankInText)
 						sPreserve = " xml:space=\"preserve\""; // DWH 5-28-09
 					if (configurationType==MSWORD)
 						text = "<w:r><w:t"+sPreserve+">"+sTuff+"</w:t></w:r>"; // DWH 4-8-09
 					else if (configurationType==MSPOWERPOINT)
 						text = "<a:r><a:t"+sPreserve+">"+sTuff+"</a:t></a:r>"; // DWH 4-8-09
 					else
 						text = sTuff;
 				}
 //				else if (context-nTextBoxLevel==1 && configurationType==MSWORD) // DWH 10-27-09 nTextBoxLevel
 				else if (context==nTextBoxLevel+1 && context-nContentDepth==0 && configurationType==MSWORD) // DWH 10-27-09 nTextBoxLevel
 				{ // context has to be one more than nTextBoxLevel; if more, it is in an attribute
 				  
 					bHasBlankInText = ((text.indexOf(' ')>-1) || (text.indexOf('\u00A0')>-1)); // DWH 5-28-09
 					if (bHasBlankInText)
 						sPreserve = " xml:space=\"preserve\""; // DWH 5-28-09
 					text = "<w:r><w:t"+sPreserve+">"+sTuff+"</w:t></w:r>"; // DWH 4-8-09
 				}
 				else
 					text = sTuff; // DWH 5-22-09
 				return text; // DWH 5-22-09
 			}
 			else
 				return ""; // DWH 5-18-09 get nothing, return nothing
 		}
 
 		// Output text with in-line codes
 		List<Code> codes = tf.getCodes();
 		StringBuilder tmp = new StringBuilder();
 		text = tf.getCodedText();
 		Code code;
 		char ch;
 		for ( int i=0; i<text.length(); i++ ) {
 			ch = text.charAt(i);
 			switch ( ch ) {
 			case TextFragment.MARKER_OPENING:
				tmp = addSurroundingCodes(tmp,true,context,nSurroundingCodes,false);
 				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
 				tmp.append(expandCodeContent(code, langToUse, context));
 				nSurroundingCodes++;
 				break;
 			case TextFragment.MARKER_CLOSING:
				tmp = addSurroundingCodes(tmp,true,context,nSurroundingCodes,false);
 				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
 				tmp.append(expandCodeContent(code, langToUse, context));
 				nSurroundingCodes--;
 				break;
 			case TextFragment.MARKER_ISOLATED:
 			case TextFragment.MARKER_SEGMENT:
				tmp = addSurroundingCodes(tmp,true,context,nSurroundingCodes,false);
 				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
 				if (code.getTagType()==TextFragment.TagType.OPENING)
 					nSurroundingCodes++;
 				else if (code.getTagType()==TextFragment.TagType.CLOSING)
 					nSurroundingCodes--;
 				tmp.append(expandCodeContent(code, langToUse, context));
 				break;
 			default:
				tmp = addSurroundingCodes(tmp,false,context,nSurroundingCodes,false);
 				if (!bInBlankText && (nSurroundingCodes<=0))
 				{
 					if (context==1) { // DWH 4-13-09 whole if
 						bInBlankText = true;
 						if (configurationType==MSWORD)
 							tmp.append(encody("<w:r><w:t xml:space=\"preserve\">",context));
 						else if (configurationType==MSPOWERPOINT)
 							tmp.append(encody("<a:r><a:t>",context));
 					}
 //					else if (context-nTextBoxLevel==1 && configurationType==MSWORD) // DWH 10-27-09
 					else if (context==nTextBoxLevel+1 && context-nContentDepth==0 && configurationType==MSWORD) // DWH 10-27-09
 					{ // only add codes around blank text if context is one above nTextBoxLevel, otherwise inside attributes
 						bInBlankText = true;
 						tmp.append(encody("<w:r><w:t xml:space=\"preserve\">",context));						
 					}
 				}
 				if ( Character.isHighSurrogate(ch) ) {
 					int cp = text.codePointAt(i);
 					i++; // Skip low-surrogate
 					if ( encoderManager == null ) {
 						if ( layer == null ) {
 							tmp.append(new String(Character.toChars(cp)));
 						}
 						else {
 							tmp.append(layer.encode(cp, context));
 						}
 					}
 					else {
 						if ( layer == null ) {
 							tmp.append(encoderManager.encode(cp, context));
 						}
 						else {
 							tmp.append(layer.encode(
 								encoderManager.encode(cp, context),
 								context));
 						}
 					}
 				}
 				else { // Non-supplemental case
 					if ( encoderManager == null ) {
 						if ( layer == null ) {
 							tmp.append(ch);
 						}
 						else {
 							tmp.append(layer.encode(ch, context));
 						}
 					}
 					else {
 						if ( layer == null ) {
 							tmp.append(encoderManager.encode(ch, context));
 						}
 						else {
 							tmp.append(layer.encode(
 								encoderManager.encode(ch, context),
 								context));
 						}
 					}
 				}
 				break;
 			}
 		}
		tmp = addSurroundingCodes(tmp,true,context,nSurroundingCodes,false);
 		return tmp.toString();
 	}
 	/**
 	 * Handles layers and encoding of a string to be expanded.
 	 * @param s string to be expanded
 	 * @return context same as context variable in getContent in GenericSkeletonWriter
 	 * @param s string to be expanded
 	 */

	private StringBuilder addSurroundingCodes(StringBuilder tmp, boolean end, int context, 
									int nSurroundingCodes,	boolean preserve)
	{
		String sTheEnd = end ? "/" : "";
		String sPreserve = preserve ? " xml:space=\"preserve\"" : "";
	    boolean bBlankLook = end ? bInBlankText : !bInBlankText;
		if (context==1 && bBlankLook && (nSurroundingCodes<=0))
		{
			bInBlankText = !end;
			if (configurationType==MSWORD)
				tmp.append(encody("<"+sTheEnd+"w:t"+sPreserve+"><"+sTheEnd+"w:r>",context));
			else if (configurationType==MSPOWERPOINT)
				tmp.append(encody("<"+sTheEnd+"a:t><"+sTheEnd+"a:r>",context));
		}
		return tmp;
	}
    private String encody(String s, int context)
 	{
 		return(s); // DWH 5-14-09 no encoding is necessary for tags
 /*
 		if ( encoderManager == null ) {
 			if ( layer == null ) {
 				return s; // DWH 4-8-09 replaced tf.toString() with sTuff
 			}
 			else {
 				return layer.encode(s, context); // DWH 4-8-09 replaced tf.toString() with sTuff
 			}
 		}
 		else {
 			if ( layer == null ) {
 				return encoderManager.encode(s, context); // DWH 4-8-09 replaced tf.toString() with sTuff
 			}
 			else {
 				return layer.encode(
 					encoderManager.encode(s, context), context); // DWH 4-8-09 replaced tf.toString() with sTuff
 			}
 		}
 */
 	}
 	public void setNTextBoxLevel(int nTextBoxLevel)
 	{
 		this.nTextBoxLevel = nTextBoxLevel;
 	}
 	private StringBuilder blankEnd(int context, int nSurroundingCodes, StringBuilder tmp)
 	{
 		if (bInBlankText && (nSurroundingCodes<=0))
 		{
 			if (context==1) { // DWH 4-13-09 whole if
 				bInBlankText = false;
 				if (configurationType==MSWORD)
 					tmp.append(encody("</w:t></w:r>",context));
 				else if (configurationType==MSPOWERPOINT)
 					tmp.append(encody("</a:t></a:r>",context));
 			}
 			else if (context==nTextBoxLevel+1 && context-nContentDepth==0 && configurationType==MSWORD)
 //			else if (context-nTextBoxLevel==1 && configurationType==MSWORD)
 			{ // each TextBox only adds 1 to context
 			  // nContentDepth is how embedded the text is
 				bInBlankText = false;
 				tmp.append(encody("</w:t></w:r>",context));				
 			}
 		}
 		return tmp;
 	}
 	protected String getContent (TextUnit tu,
 			LocaleId locToUse,
 			int context) 
 	{
 		String result;
 		Property prop = tu.getProperty("TextBoxLevel");
 		nTextBoxLevel = 0;
 		if (prop!=null)
 		{
 			try
 			{
 				nTextBoxLevel = Integer.parseInt(prop.getValue());
 			}
 			catch(Exception e) {}
 		}
 		nContentDepth++;
 		result = super.getContent(tu,locToUse,context);
 		nContentDepth--;
 		return result;
 	}
 }
