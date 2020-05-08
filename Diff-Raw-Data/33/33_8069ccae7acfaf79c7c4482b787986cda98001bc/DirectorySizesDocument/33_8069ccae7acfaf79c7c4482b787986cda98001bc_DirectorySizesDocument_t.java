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
     
 	$RCSfile: DirectorySizesDocument.java,v $ 
	Created on $Date: 2003-06-17 23:33:53 $ 
 */package net.sf.statcvs.output.xml;
 
 import java.util.Iterator;
 
 import net.sf.statcvs.Messages;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.CvsRevision;
 import net.sf.statcvs.model.Directory;
 import net.sf.statcvs.model.RevisionIterator;
 import net.sf.statcvs.renderer.Chart;
 import net.sf.statcvs.renderer.PieChart;
 import net.sf.statcvs.util.IntegerMap;
 
 import org.jdom.Element;
 
 /**
  * DirectorySizesDocument
  * 
  * @author Tammo van Lessen
  * @version $id: $
  */
 public class DirectorySizesDocument extends StatCvsDocument {
 
 	private CvsContent docContent;
 	
 	/**
 	 * @param element
 	 * @param filename
 	 */
 	public DirectorySizesDocument(CvsContent content) {
		super(new Element("document"), "dir_sizes");
 		docContent = content;
		// set doc title
		getRootElement().setAttribute("title", "Module sizes");
		Element report = new Element("report");
		getRootElement().addContent(report);
		report.setAttribute("title", "Module Sizes");
		report.addContent(new Element("img")
 			.setAttribute("src", "module_sizes.png"));
		report.addContent(getModulesElement(content));		
 	}
 
 	/**
 	 * @see net.sf.statcvs.output.xml.StatCvsDocument#getCharts()
 	 */
 	public Chart[] getCharts() {
 		return new Chart[] {new PieChart(docContent, docContent.getModuleName(),
 			Messages.getString("PIE_MODSIZE_SUBTITLE"),
 			"module_sizes.png", null, PieChart.FILTERED_BY_REPOSITORY)};
 	}
 
 	private static Element getModulesElement(CvsContent content) {
 		RevisionIterator revs = content.getRevisionIterator();
 		IntegerMap dirChanges = new IntegerMap();
 		IntegerMap dirLoC = new IntegerMap();
 		Element list = new Element("modules");		
 		while (revs.hasNext()) {
 			CvsRevision rev = revs.next();
 			Directory dir = rev.getFile().getDirectory();
 			dirChanges.addInt(dir, 1);
 			dirLoC.addInt(dir, rev.getLineValue()); 			
 		}
 		Iterator it = dirLoC.iteratorSortedByValueReverse();
 		while (it.hasNext()) {
 			Directory key = (Directory)it.next();
 			Element el = new Element("module");
 			// TODO: Add link to module page
 			el.setAttribute("name", key.isRoot() ? "/" : key.getPath());
 			el.setAttribute("changes", ""+dirChanges.get(key));
 			el.setAttribute("lines", ""+dirLoC.get(key));
 			el.setAttribute("changesPerLine", ""+(double)dirLoC.get(key) / dirChanges.get(key));
 			el.setAttribute("changesPercent", ""+dirChanges.getPercent(key));
 			el.setAttribute("linesPercent", ""+dirLoC.getPercent(key));
 			list.addContent(el);			
 		}
 		return list;
 	}
 
 }
