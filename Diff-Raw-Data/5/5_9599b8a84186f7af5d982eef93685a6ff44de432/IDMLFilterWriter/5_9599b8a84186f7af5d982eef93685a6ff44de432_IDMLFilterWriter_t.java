 /*===========================================================================
   Copyright (C) 2010-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.idml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Stack;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.IReferenceable;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 public class IDMLFilterWriter implements IFilterWriter {
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 
 	private final DocumentBuilder docBuilder;
 	private String outputPath;
 	private LocaleId trgLoc;
 	private ZipFile zipOriginal;
 	private ZipOutputStream zipOutStream;
 	private File tempFile;
 	private byte[] buffer;
 	private Transformer xformer;
 	private ZipEntry entry;
 	private Document doc;
 	private int group;
 	private Stack<IReferenceable> referents;
 	private ArrayList<String> storiesLeft;
 	
 	public IDMLFilterWriter () {
         try {
         	DocumentBuilderFactory docFact = DocumentBuilderFactory.newInstance();
     		docFact.setValidating(false);
     		docBuilder = docFact.newDocumentBuilder();
 			xformer = TransformerFactory.newInstance().newTransformer();
 		}
 		catch ( Throwable e ) {
 			throw new OkapiIOException("Error initializing.\n"+e.getMessage(), e);
 		}
 	}
 	
 	@Override
 	public void cancel () {
 		// TODO
 	}
 
 	@Override
 	public void close () {
 		try {
 			zipOriginal = null;
 			if ( zipOutStream == null ) return; // Was closed already
 			
 			// Close the output
 			zipOutStream.close();
 			zipOutStream = null;
 			buffer = null;
 
 			// If it was in a temporary file, copy it over the existing one
 			// If the IFilter.close() is called before IFilterWriter.close()
 			// this should allow to overwrite the input.
 			if ( tempFile != null ) {
 				Util.copy(new FileInputStream(tempFile), outputPath);
 			}
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error closing IDML outpiut.\n"+e.getMessage(), e);
 		}
 	}
 
 	@Override
 	public EncoderManager getEncoderManager () {
 		return null;
 	}
 
 	@Override
 	public ISkeletonWriter getSkeletonWriter () {
 		return null;
 	}
 
 	@Override
 	public String getName () {
 		return "IDMLFilterWriter";
 	}
 
 	@Override
 	public IParameters getParameters () {
 		return null; // Not used
 	}
 
 	@Override
 	public Event handleEvent (Event event) {
 		switch ( event.getEventType() ) {
 		case START_DOCUMENT:
 			processStartDocument(event.getStartDocument());
 			break;
 		case END_DOCUMENT:
 			processEndDocument();
 			break;
 		case START_GROUP:
 			processStartGroup(event.getStartGroup());
 			break;
 		case END_GROUP:
 			processEndGroup(event.getEndGroup());
 			break;
 		case TEXT_UNIT:
 			processTextUnit(event.getTextUnit());
 			break;
 		}
 		return event;
 	}
 
 	@Override
 	public void setOptions (LocaleId locale,
 		String defaultEncoding)
 	{
 		trgLoc = locale;
 		// Ignore encoding. We always use UTF-8
 	}
 
 	@Override
 	public void setOutput (String path) {
 		outputPath = path;
 	}
 
 	@Override
 	public void setOutput (OutputStream output) {
 		// Not supported for this filter
 		throw new UnsupportedOperationException("setOutput(OutputStream) is not supported for this class.");
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		// Not used
 	}
 
 	private void processStartDocument (StartDocument res) {
 		try {
 			// Get the original ZIP file
 			// This will be used throughout the writting
 			IDMLSkeleton skel = (IDMLSkeleton)res.getSkeleton();
 			zipOriginal = skel.getOriginal();
 			group = 0;
 			referents = new Stack<IReferenceable>();
 			storiesLeft = new ArrayList<String>();
 		
 			// Create the output stream from the path provided
 			tempFile = null;			
 			boolean useTemp = false;
 			File f = new File(outputPath);
 			if ( f.exists() ) {
 				// If the file exists, try to remove
 				useTemp = !f.delete();
 			}
 			if ( useTemp ) {
 				// Use a temporary output if we cannot overwrite for now
 				// If it's the input file, IFilter.close() will free it before we
 				// call close() here (that is if IFilter.close() is called correctly!)
 				tempFile = File.createTempFile("idmlTmpZip", null);
 				zipOutStream = new ZipOutputStream(new FileOutputStream(tempFile.getAbsolutePath()));
 			}
 			else { // Make sure the directory exists
 				Util.createDirectories(outputPath);
 				zipOutStream = new ZipOutputStream(new FileOutputStream(outputPath));
 			}
 			
 			// Create buffer for transfer
 			buffer = new byte[2048];
 			
 			// Copy all entries of the original ZIP file into the output,
 			// except for the stories entries.
 			Enumeration<? extends ZipEntry> entries = zipOriginal.entries();
 			while( entries.hasMoreElements() ) {
 				ZipEntry entry = entries.nextElement();
 				if ( entry.getName().endsWith(".xml") ) {
 					if ( entry.getName().startsWith("Stories/") ) {
 						storiesLeft.add(entry.getName());
 						continue; // Not yet
 					}
 				}
 				// Else: copy the entry into the output ZIP file
 				zipOutStream.putNextEntry(new ZipEntry(entry.getName()));
 				InputStream input = zipOriginal.getInputStream(entry); 
 				int len;
 				while ( (len = input.read(buffer)) > 0 ) {
 					zipOutStream.write(buffer, 0, len);
 				}
 				input.close();
 				zipOutStream.closeEntry();
 			}
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error creating output IDML.\n"+e.getMessage(), e);
 		}
 	}
 
 	private void processEndDocument () {
 		try {
 			if ( storiesLeft != null ) {
 				Enumeration<? extends ZipEntry> entries = zipOriginal.entries();
 				while( entries.hasMoreElements() ) {
 					ZipEntry entry = entries.nextElement();
 					if ( storiesLeft.contains(entry.getName()) ) {
 						// Copy the entry into the output ZIP file
 						zipOutStream.putNextEntry(new ZipEntry(entry.getName()));
 						InputStream input = zipOriginal.getInputStream(entry); 
 						int len;
 						while ( (len = input.read(buffer)) > 0 ) {
 							zipOutStream.write(buffer, 0, len);
 						}
 						input.close();
 						zipOutStream.closeEntry();
 						storiesLeft.remove(entry.getName());
 					}
 				}
 			}
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error writting out non-extracted stories.", e);
 		}
 		close();
 	}
 
 	private void processTextUnit (ITextUnit tu) {
 		// If it's a referent, just store it for now.
 		// It'll be merged when the inline code with the reference to it is merged
 		if ( tu.isReferent() ) {
 			referents.push(tu);
 		}
 		else { // Otherwise: merge the text unit now
 			while ( !referents.isEmpty() ) {
 				mergeTextUnit((ITextUnit)referents.pop());
 			}
 			mergeTextUnit(tu);
 		}
 	}
 	
 	private void mergeTextUnit (ITextUnit tu) {
 		IDMLSkeleton skel = (IDMLSkeleton)tu.getSkeleton();
 		
 		// Get the target content, or fall back to the source
 		// Make a copy to not change the original in the resource
 		TextContainer tc = tu.getTarget(trgLoc);
 		if ( tc == null ) tc = tu.getSource();
 		TextFragment tf = tc.getUnSegmentedContentCopy();
 		if ( tf.isEmpty() && !tu.getSource().isEmpty() ) {
 			tf = tu.getSource().getUnSegmentedContentCopy();
 		}
 		
 		// Escape the text of the content to XML
 		// inline codes are still in XML so we don't touch them
 		String ctext = tf.getCodedText();
 		ctext = Util.escapeToXML(ctext, 3, false, null);
 		// Set the modified coded text
 		tf.setCodedText(ctext);
 		
 		// Now the whole content is true XML, it can be parsed as a fragment
 		StringBuilder xml = new StringBuilder("<r>");
 		// If there were moved inline codes: we put them back, so we have valid XML
 		String[] res = skel.getMovedParts();
 		if (( res != null ) && ( res[0] != null )) {
 			xml.append(res[0]);
 		}
 		xml.append(tf.toText());
 		if (( res != null ) && ( res[1] != null )) {
 			xml.append(res[1]);
 		}
 		xml.append("</r>");
 		
 		try {
 			Document tmpDoc =  docBuilder.parse(new InputSource(new StringReader(xml.toString())));
 			Document docWhereToImport = skel.getScopeNode().getOwnerDocument();
 			DocumentFragment docFrag = docWhereToImport.createDocumentFragment();
 			
 			Node imp = docWhereToImport.importNode(tmpDoc.getDocumentElement(), true);
 			while ( imp.hasChildNodes() ) {
 				docFrag.appendChild(imp.removeChild(imp.getFirstChild()));
 			}
 
 			// Get live nodes
 			HashMap<String, Node> map = collectLiveReferents(skel); 
 			
 			// Remove the old content
 			// (Reference nodes have been cloned in the skeleton)
 			Node node = skel.getScopeNode();
 			while( node.hasChildNodes() ) {
 				node.removeChild(node.getFirstChild());  
 			}
 			
 			// Attach the new content
 			node.appendChild(docFrag);
 			
 			// If needed, re-inject the nodes referenced in the inline codes.
 			if ( map != null ) {
 				// Re-inject the reference nodes using the skeleton copies
 				NodeList list = ((Element)node).getElementsByTagName(IDMLSkeleton.NODEREMARKER);
 				// The list is dynamic, so replacing the node, decrease the list
 				while ( list.getLength() > 0 ) {
 					Element marker = (Element)list.item(0);
 					String key = marker.getAttribute("id");
 					Node original = map.get(key);
 					if ( original == null ) {
 						logger.severe(String.format("Missing original node for a reference in text unit id='%s'.", tu.getId()));
 						break; // Break now or we'll be in an infinite loop
 					}
 					Element parent = (Element)marker.getParentNode();
 					parent.replaceChild(original, marker);
 				}
 			}
 		}
 		catch ( Throwable e ) {
 			logger.severe(String.format("Error when parsing XML of text unit id='%s'.\n"+e.getMessage(), tu.getId()));
 		}
 	}
 	
 	private HashMap<String, Node> collectLiveReferents (IDMLSkeleton skel) {
 		if ( !skel.hasReferences() ) return null;
 		// Create an empty list to store the live nodes
 		HashMap<String, Node> map = new HashMap<String, Node>();
 		Element elem = (Element)skel.getTopNode();
 		// Get the list of references
 		HashMap<String, NodeReference> refs = skel.getReferences();
 		for ( String key : refs.keySet() ) {
 			NodeReference ref = refs.get(key);
 			NodeList list = elem.getElementsByTagName(ref.name);
 			Node ori = list.item(ref.position).cloneNode(true);
 			map.put(key, ori);
 		}
 		return map;
 	}
 	
 	private void processStartGroup (StartGroup res) {
 		group++;
 		IDMLSkeleton skel = (IDMLSkeleton)res.getSkeleton();
 		if ( skel == null ) return; // Not a story group
 		// Store the entry data to process the text units
 		// Everything will be written at the end group.
 		entry = skel.getEntry();
 		doc = skel.getDocument();
 	}
 	
 	private void processEndGroup (Ending ending) {
		// Merge any remaining TU
		while ( !referents.isEmpty() ) {
			mergeTextUnit((ITextUnit)referents.pop());
		}
		// Process the group
 		group--;
 		try {
 			if ( group != 1 ) {
 				// Not a story group
 				return;
 			}
 			// This is where we output the modified story
 			// Prepare the DOM document for writing
 	        Source source = new DOMSource(doc);
 	        // Prepare the output file
 			zipOutStream.putNextEntry(new ZipEntry(entry.getName()));
 	        Result result = new StreamResult(zipOutStream);
 	        // Write the DOM document to the file
 	        xformer.transform(source, result);
 			zipOutStream.closeEntry();
 			// This story is done
 			storiesLeft.remove(entry.getName());
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error writing out the story.\n"+e.getMessage(), e);
 		}
 		catch ( TransformerConfigurationException e ) {
 			throw new OkapiIOException("Error transforming the story output.\n"+e.getMessage(), e);
 		}
 		catch ( TransformerFactoryConfigurationError e ) {
 			throw new OkapiIOException("Transform configuration error.\n"+e.getMessage(), e);
 		}
 		catch ( TransformerException e ) {
 			throw new OkapiIOException("Transformation error.\n"+e.getMessage(), e);
 		}
 	}
 
 }
