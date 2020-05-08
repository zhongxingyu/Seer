 /*******************************************************************************
  * Copyright 2013 Gabriel Siemoneit
  * 
  * This file is part of TEI2Latex.
  * 
  * TEI2Latex is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  *   
  * TEI2Latex is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *   
  * You should have received a copy of the GNU General Public License
  * along with TEI2Latex.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package org.tei2latex;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.liblatexdoc.LatexDocument;
 import org.libteidoc.AbstractNoIgnoreHandler;
 import org.libteidoc.TEIContentHandler;
 import org.libteidoc.TEIDocument;
 import org.tei2latex.tei.EmphHandler;
 import org.tei2latex.tei.MilestoneHandler;
 import org.tei2latex.tei.NoteHandler;
 import org.tei2latex.tei.PbHandler;
 
 public class TEI2Latex {
 	
 	private static Logger logger = LogManager.getLogger(TEI2Latex.class.getName());
 
 	public static void main(String[] args) {
 		
 		logger.info("Starting TEI2Latex...");
 		
 		LatexDocument latexDoc = new LatexDocument("/home/gabriel/Dokumente/Dissertation/Supplement/Text/Buch1/Freinsheim1.Curt.latex");
 		
 		latexDoc.usePackage("morefloats");
 		latexDoc.usePackage("fontenc", "T1");
 		latexDoc.usePackage("microtype");
 		
 		latexDoc.usePackage("eledmac");
 		latexDoc.addPreambleLine("\\footparagraph{A}");
 		latexDoc.addPreambleLine("\\setlength{\\skip\\Afootins}{10mm}");
 		latexDoc.beginDocument();
 		
 		
 		latexDoc.write("\\beginnumbering\\pstart\n");
 		
 		
 		TEIDocument doc = new TEIDocument("/home/gabriel/Dokumente/Dissertation/Supplement/Text/Buch1/Freinsheim1.Curt.xml");
 		doc.setTEIContentHandler(new TEIContentHandler(latexDoc));
 		doc.setElementHandler("milestone", new MilestoneHandler(latexDoc));
		doc.setElementHandler("orig", new AbstractNoIgnoreHandler(latexDoc));
 		doc.setElementHandler("emph", new EmphHandler(latexDoc));
 		doc.setElementHandler("note", new NoteHandler(latexDoc));
 		doc.setElementHandler("pb", new PbHandler(latexDoc));
 		doc.parse();
 		
 		latexDoc.write("\\pend\n");
 		latexDoc.write("\\endnumbering");
 		latexDoc.endDocument();
 	}
 }
