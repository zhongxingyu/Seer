 /*
     StatCvs - CVS statistics generation 
     Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
     http://statcvs.sf.net/
     
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
     
 	$RCSfile: CommitLogReport.java,v $
	$Date: 2003-06-24 17:45:39 $ 
 */
 package net.sf.statcvs.output.xml.report;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
import net.sf.statcvs.I18n;
 import net.sf.statcvs.model.Author;
 import net.sf.statcvs.model.Commit;
 import net.sf.statcvs.model.CommitListBuilder;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.CvsRevision;
 import net.sf.statcvs.model.RevisionIterator;
 import net.sf.statcvs.model.RevisionSortIterator;
 import net.sf.statcvs.output.ConfigurationOptions;
 import net.sf.statcvs.output.WebRepositoryIntegration;
 import net.sf.statcvs.renderer.FileCollectionFormatter;
 import net.sf.statcvs.util.DateUtils;
 
 import org.jdom.Element;
 
 /**
  * CommitLogElement
  * 
  * @author Tammo van Lessen
  */
 public class CommitLogReport extends ReportElement {
 
 	private Element commitsEl;
 	private Map revisions = new HashMap();
 	
 	/**
 	 * Renders the commitlog to xml.
 	 */
 	private CommitLogReport() 
 	{
		super(I18n.tr("Commit Log"));
 		commitsEl = new Element("commitlog");
 		addContent(commitsEl);		
 	}
 
 	/**
 	 * Returns the top 'maxCommits' entries from the incomming List
 	 *  
 	 * @param commits
 	 * @param maxCommits
 	 * @return
 	 */
 	public CommitLogReport(CvsContent content, int maxCommits)
 	{
 		this();
 
 		RevisionIterator revisionsIt =
 			new RevisionSortIterator(content.getRevisionIterator());
 		
 		// copy and reverse commitlist
 		List commits = new ArrayList
 			(new CommitListBuilder(revisionsIt).createCommitList());
 		Collections.reverse(commits);
 		
 		if (maxCommits >= 0) {
 			addCommits(commits.subList(0, maxCommits));
 		}
 		else {
 			addCommits(commits);
 		}
 	}
 
 	/**
 	 * Creates the commit list from CvsContent
 	 * 
 	 * @param content
 	 * @return List of Commit
 	 */
 	public CommitLogReport(CvsContent content)
 	{
 		this(content, -1);
 	}
 
 	/**
 	 * Returns a List of Commits made by the Author
 	 * 
 	 * @param author
 	 * @return
 	 */
 	public CommitLogReport(Author author)
 	{
 		this();
 
 		CommitListBuilder builder 
 			= new CommitListBuilder(author.getRevisionIterator());
 		List commits = builder.createCommitList();
 
 		addCommits(commits);
 	}
 
 	private void addCommits(List commits)
 	{ 
 		Iterator commIt = commits.iterator();
 		while (commIt.hasNext()) {
 			Commit commit = (Commit) commIt.next();
 
 			// create revision map (filename -> CvsRevision
 			Iterator it = commit.getRevisions().iterator();
 			int locSum = 0;
 			while (it.hasNext()) {
 				CvsRevision each = (CvsRevision) it.next();
 				locSum += each.getLineValue();
 				revisions.put(each.getFile().getFilenameWithPath(), each);
 			}
 
 			Element comEl = new Element("commit");
 			comEl.setAttribute("author", commit.getAuthor().getName());
 			//comel.setAttribute("date", commit.getDate().toString());
 			comEl.setAttribute("date", DateUtils.formatDateAndTime(commit.getDate()));
 			
 			comEl.setAttribute("changedfiles", ""+commit.getChangeCount());						
 			comEl.setAttribute("changedlines", ""+locSum);
 			comEl.addContent(new Element("comment").setText(commit.getComment()));
 
 			comEl.addContent(createFilesElement(commit));
 			
 			commitsEl.addContent(comEl);
 		}
 		
 	}
 
 	/**
 	 * Renders the filelist
 	 * @param commit
 	 * @return
 	 */
 	private Element createFilesElement(Commit commit) {
 		Element files = new Element("files");
 		
 		FileCollectionFormatter formatter =
 			new FileCollectionFormatter(commit.getAffectedFiles());
 				
 		Iterator it = formatter.getDirectories().iterator();
 		while (it.hasNext()) {
 			String directory = (String)it.next();
 			Iterator filesIt = formatter.getFiles(directory).iterator();
 
 			while (filesIt.hasNext()) {
 				Element file = new Element("file");
 				String filename = (String)filesIt.next();
 				file.setAttribute("name", filename);
 				if (!"".equals(directory)) {
 					file.setAttribute("directory", directory);
 				}
 				CvsRevision revision =
 					(CvsRevision) revisions.get(directory + filename);
 		
 				file.setAttribute("revision", revision.getRevision());
 				// links to webrepo				
 				WebRepositoryIntegration webRepository = ConfigurationOptions.getWebRepository();
 				if (webRepository != null) {
 					CvsRevision previous = revision.getPreviousRevision();
 					String url; 
 					if (previous == null) {
 						url = webRepository.getFileViewUrl(revision);
 					} else {
 						url = webRepository.getDiffUrl(previous, revision);
 					}
 					file.setAttribute("url", url);
 				}
 								
 				// check type of commit
 				if (revision.isInitialRevision()) {
 					file.setAttribute("action", "added");
 					if (revision.getFile().isBinary()) {
 						file.setAttribute("type", "binary");
 					} else {
 						file.setAttribute("lines", ""+revision.getLinesOfCode());
 					}
 				} else if (revision.isDead()) {
 					file.setAttribute("action", "deleted");
 				} else {
 					file.setAttribute("action", "changed");
 					file.setAttribute("added", ""+revision.getLinesAdded());
 					file.setAttribute("removed", ""+revision.getLinesRemoved());
 				}
 				files.addContent(file);
 			}
 		}
 		return files; 
 	}
 
 }
 
