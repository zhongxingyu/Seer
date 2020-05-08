 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.steps.rainbowkit.transifex;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.filters.po.POFilterWriter;
 import net.sf.okapi.filters.rainbowkit.Manifest;
 import net.sf.okapi.filters.rainbowkit.MergingInfo;
 import net.sf.okapi.filters.transifex.Project;
 import net.sf.okapi.lib.transifex.TransifexClient;
 import net.sf.okapi.steps.rainbowkit.common.BasePackageWriter;
 
 public class TransifexPackageWriter extends BasePackageWriter {
 
 	private POFilterWriter potWriter;
 	private POFilterWriter trgWriter;
 
 	public TransifexPackageWriter () {
 		super(Manifest.EXTRACTIONTYPE_TRANSIFEX);
 	}
 	
 	@Override
 	protected void processStartBatch () {
 		manifest.setSubDirectories("original", "uploads", "downloads", "done", null, true);
 		setTMXInfo(false, null, null, null, null);
 		super.processStartBatch();
 	}
 
 	@Override
 	protected void processEndBatch () {
 		super.processEndBatch();
 		XMLWriter report = null;
 		PrintWriter pw = null;
 		
 		// Get the parameters/options for the Transifex project
 		Parameters options = new Parameters();
 		// Get the options from the parameters
 		if ( !Util.isEmpty(params.getWriterOptions()) ) {
 			options.fromString(params.getWriterOptions());
 		}
 
 		try {
 			// Start the TXP file
 			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
 				manifest.getPackageRoot()+options.getProjectId()+".txp"), "UTF-8"));
 			pw.println(Project.HOST + "=" + options.getServer());
 			pw.println(Project.USER + "=" + options.getUser());
 			pw.println(Project.PROJECTID + "=" + options.getProjectId());
 			pw.println(Project.SOURCELOCALE + "=" + manifest.getSourceLocale().toString());
 			pw.println(Project.TARGETLOCALE + "=" + manifest.getTargetLocale().toString());
 	
 			// Start HTML page with links
 			String reportPath = manifest.getPackageRoot()+"linksToTransifex.html";
 			report = new XMLWriter(reportPath);
 			report.writeStartDocument();
 			report.writeRawXML("<h1>Transifex Package Summary</h1>");
 			report.writeLineBreak();
 			report.writeRawXML(String.format("<p>Resources uploaded to Transifex in the project "
 				+ "<b><a target='_blank' href='%s'>%s</a></b></p>",
 				options.getServer() + "projects/p/" + options.getProjectId() + "/",
 				options.getProjectName()));
 			report.writeLineBreak();
 			report.writeRawXML("<table border='1' cellspacing='0' cellpadding='5'>");
 			report.writeRawXML("<tr><th>Transifex Resource</th><th>Original Source File</th></tr>");
 			report.writeLineBreak();
 	
 			// Create the Transifex client and initialize it
 			TransifexClient cli = new TransifexClient(options.getServer());
 			cli.setProject(options.getProjectId());
 			cli.setCredentials(options.getUser(), options.getPassword());
 			
 			// Create the project
 			String[] res1 = cli.createProject(options.getProjectId(), options.getProjectName(), "TODO short desc", "TODO Long desc");
 			if ( res1[0] == null ) {
 				// Could not create the project
 				logger.severe(res1[1]);
 				return;
 			}
 			for ( int id : manifest.getItems().keySet() ) {
 				MergingInfo info = manifest.getItem(id);
 				String poPath = manifest.getSourceDirectory() + info.getRelativeInputPath() + ".po";
 				
 				// Compute the resource filename to use in Transifex
 				String resourceFile = Util.getFilename(poPath, true);
 				String subdir = Util.getDirectoryName(info.getRelativeInputPath());
 				if ( !subdir.isEmpty() ) {
 					resourceFile = Util.makeId(subdir) + "_" + resourceFile;
 				}
 				
 				res1 = cli.putSourceResource(poPath, manifest.getSourceLocale(), resourceFile);
 	 			if ( res1[0] == null ) {
 					logger.severe(res1[1]);
 					return;
 				}
 				// Else: set the resource id
 				info.setResourceId(res1[1]);
 				
 				// write the resource in the TXP file
 				pw.println(res1[1]);
 
 				// Write the link to the resource in the HTML file
 				report.writeRawXML(String.format("<tr><td><a target='_blank' href=\"%s\">%s</a></td>",
 					options.getServer()+res1[0].substring(1), resourceFile));
 				report.writeRawXML(String.format("<td>%s</td></tr>", info.getRelativeInputPath()));
 				report.writeLineBreak();
 				
 				// Try to put the translated resource
 				poPath = makeTargetPath(info);
 				String[] res2 = cli.putTargetResource(poPath, manifest.getTargetLocale(), res1[1], resourceFile);
 				if ( res2[0] == null ) {
 					logger.severe(res2[1]);
 				}
 			
 			}
 			
 			report.writeRawXML("</table>");
 			report.writeRawXML("<p>For more information about this package, see: "
 				+ "<a target='_blank' href='http://www.opentag.com/okapi/wiki/index.php?title=Rainbow_TKit_-_Transifex_Project'>"
 				+ "Rainbow TKit - Transifex Project</a>.");
 			report.writeRawXML("<p><font size='2'>Note: This report was generated when creating the translation package, "
 				+ "the Transifex project may have been updated with other files since.</font></p>");
 			report.close();
 			
 			// Save the manifest again (for the esourceId)
 			if ( params.getOutputManifest() ) {
 				manifest.Save();
 			}
 	
 			Util.openURL("file:///"+reportPath);
 			
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error at the end of the batch.\n"+e.getMessage(), e);
 		}
 		finally {
 			if ( report != null ) report.close();
 			if ( pw != null ) pw.close();
 		}
 		
 	}
 	
 	@Override
 	protected void processStartDocument (Event event) {
 		super.processStartDocument(event);
 
 		// Set the source POT file
 		potWriter = new POFilterWriter();
 		net.sf.okapi.filters.po.Parameters params = (net.sf.okapi.filters.po.Parameters)potWriter.getParameters();
 		params.setOutputGeneric(true);
 		potWriter.setMode(true, true, true);
 		potWriter.setOptions(manifest.getSourceLocale(), "UTF-8");
 
 		MergingInfo item = manifest.getItem(docId);
 		String path = manifest.getSourceDirectory() + item.getRelativeInputPath() + ".po";
 		potWriter.setOutput(path);
 
 		// Set the target PO file
 		trgWriter = new POFilterWriter();
 		params = (net.sf.okapi.filters.po.Parameters)trgWriter.getParameters();
 		params.setOutputGeneric(true);
 		trgWriter.setMode(true, false, false);
 		trgWriter.setOptions(manifest.getTargetLocale(), "UTF-8");
 		
 		path = makeTargetPath(item);
 		trgWriter.setOutput(path);
 		
 		potWriter.handleEvent(event);
 		trgWriter.handleEvent(event);
 	}
 	
 	@Override
 	protected void processEndDocument (Event event) {
 		potWriter.handleEvent(event);
 		trgWriter.handleEvent(event);
 		if ( potWriter != null ) {
 			potWriter.close();
 			potWriter = null;
 		}
 		if ( trgWriter != null ) {
 			trgWriter.close();
 			trgWriter = null;
 		}
 		
 		// Call the base method, in case there is something common to do
 		super.processEndDocument(event);
 	}
 
 	@Override
 	protected void processStartSubDocument (Event event) {
 		potWriter.handleEvent(event);
 		trgWriter.handleEvent(event);
 	}
 	
 	@Override
 	protected void processEndSubDocument (Event event) {
 		potWriter.handleEvent(event);
 		trgWriter.handleEvent(event);
 	}
 	
 	@Override
 	protected void processTextUnit (Event event) {
 		// Skip non-translatable
 		ITextUnit tu = event.getTextUnit();
 		if ( !tu.isTranslatable() ) return;
 		
 		potWriter.handleEvent(event);
 		trgWriter.handleEvent(event);
 		writeTMXEntries(event.getTextUnit());
 	}
 
 	@Override
 	public void close () {
 		if ( potWriter != null ) {
 			potWriter.close();
 			potWriter = null;
 		}
 		if ( trgWriter != null ) {
 			trgWriter.close();
 			trgWriter = null;
 		}
 	}
 
 	@Override
 	public String getName () {
 		return getClass().getName();
 	}
 
 	private String makeTargetPath (MergingInfo item) {
 		String ex = Util.getExtension(item.getRelativeInputPath());
 		String sd = Util.getDirectoryName(item.getRelativeInputPath());
 		String fn = Util.getFilename(item.getRelativeInputPath(), false);
 		
 		return manifest.getSourceDirectory()
 			+ ( sd.isEmpty() ? "" : sd + "/" )
 			+ fn + "_" + manifest.getTargetLocale().toPOSIXLocaleId()
 			+ ex + ".po";
 	}
 
 }
