 /*
  *  StatCvs-XML - XML output for StatCvs.
  *
  *  Copyright by Steffen Pingel, Tammo van Lessen.
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  version 2 as published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package de.berlios.statcvs.xml.report;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import net.sf.statcvs.model.Author;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.CvsRevision;
 import net.sf.statcvs.util.FileUtils;
 import de.berlios.statcvs.xml.I18n;
 import de.berlios.statcvs.xml.output.Report;
 import de.berlios.statcvs.xml.output.ReportElement;
 import de.berlios.statcvs.xml.output.ReportSettings;
 import de.berlios.statcvs.xml.output.TableElement;
 import de.berlios.statcvs.xml.output.TextElement;
 import de.berlios.statcvs.xml.util.FileHelper;
 import de.berlios.statcvs.xml.util.StringHelper;
 
 /**
  * AuthorInfoReport
  * 
  * @author Tammo van Lessen
  */
 public class AuthorDetailsReport {
 
 	private static final Logger logger = 
 		Logger.getLogger("de.berlios.statcvs.xml.report.AuthorDetailsReport");
 	private static final String DEFAULT_PIC = "resources" + File.separator + "dummy.png";
 	
 	public static Report generate(CvsContent content, ReportSettings settings) 
 	{
 		return new Report(new AuthorInfoElement(settings, 
 												  I18n.tr("Author Details")));
 	}
 	
 	public static class AuthorInfoElement extends ReportElement
 	{
 	
 		private URL pictureURL = null;
 		private String pictureSource = null;
 		private String pictureFilename = null;
 		
 		public AuthorInfoElement(ReportSettings settings, String name)
 		{
 			super(settings, name);
 
 			if (settings.getForEachObject() instanceof Author) {
 				Author author = (Author)settings.getForEachObject();
 
 				// calc data
 				int loc = 0;
 				int locAdded = 0;
 				for (Iterator it = author.getRevisions().iterator();
 					it.hasNext();) {
 					CvsRevision rev = (CvsRevision)it.next(); 
 					loc += rev.getLinesDelta();
 					locAdded += rev.getNewLines(); 		
 				}
 
 				// create details
 				TextElement text = new TextElement(settings, "authorinfo")
 					.addValue("login", author.getName(), I18n.tr("Login"))
 					.addValue("fullname", settings.getFullname(author), 
 								I18n.tr("Fullname"))
 					.addValue("revcount", author.getRevisions().size(), 
 								I18n.tr("Revisions"))
 					.addValue("loc", loc, I18n.tr("Lines of Code"))
 					.addValue("locAdded", locAdded, I18n.tr("Added Lines of Code"))
 					.addValue("locPerRevision", (double)loc / author.getRevisions().size(), 
 								I18n.tr("Lines of Code per Change"));
 
 				if (settings.getBoolean("showImages", true)) {
 					// add a table with image and details
 					TableElement table = new TableElement(settings, null);
 
 					calculatePictureFilename(settings.getAuthorPic(author, DEFAULT_PIC));
 					table.addRow().addImage("authorPicture", pictureFilename)
 					  			  .addContent(text);
 					
 					addContent(table);
 				} 
 				else {
 					// add details only (showImages == false)
 					addContent(text);					
 				}
 			} 
 			else {
 				logger.warning("This report can only be used in author-foreach environments.");
 			}
 			
 		}
 		
 		private void calculatePictureFilename(String source)
 		{
 			try {
 				pictureURL = new URL(source);
 				pictureFilename = StringHelper.lastToken(pictureURL.getPath(), "/");
 				if (pictureFilename.length() == 0) {
 					throw new MalformedURLException();
 				}
 				return;
 			} 
 			catch (MalformedURLException e) {
 				// no url, try interpretation as a filename 
 			}
 			
 			File file = new File(source);
 			
			pictureSource = source;
 			if (!file.exists() && !source.equals(DEFAULT_PIC)) {
 				logger.info(I18n.tr("Picture file {0} not found, using dummy instead.", source));
 				pictureSource = DEFAULT_PIC;
 			}
			
 			pictureFilename = FileUtils.getFilenameWithoutPath(pictureSource);
 		}
 		
 		/**
 		 *  @see de.berlios.statcvs.xml.output.ReportElement#saveResources(java.io.File)
 		 */
 		public void saveResources(File outputPath) throws IOException 
 		{
 			if (pictureFilename != null) {
 				if (pictureURL != null) {
 					FileHelper.copyResource(pictureURL, outputPath, pictureFilename);
 				}
 				else if (pictureSource != null) {
 					FileHelper.copyResource(pictureSource, outputPath, pictureFilename);
 				}
 				// else { images turned off }
 			}
 		}
 
 	}
 
 }
