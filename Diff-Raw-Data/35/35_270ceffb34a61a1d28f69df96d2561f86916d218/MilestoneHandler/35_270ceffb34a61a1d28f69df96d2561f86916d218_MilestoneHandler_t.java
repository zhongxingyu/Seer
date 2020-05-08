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
 package org.tei2latex.tei;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.liblatexdoc.LatexDocument;
 import org.libteidoc.TEIElementHandler;
 
 public class MilestoneHandler extends TEIElementHandler {
 	
 	private static Logger logger = LogManager.getLogger(MilestoneHandler.class.getName());
 	
 	protected LatexDocument latexDocument;
 	protected final String lineSeparator = System.getProperty("line.separator");
 	
 	private boolean firstChapter = true;
 	
 	public MilestoneHandler(LatexDocument doc) {
 		latexDocument = doc;
 	}
 	
 	@Override
	public void start(String qn) {
		super.start(qn);
		
 		latexDocument.write(lineSeparator);
 		
 		if (attributes.getValue("unit").equals("section")) {
 			
 			logger.debug("Section "+attributes.getValue("n"));
 			
 			if (!attributes.getValue("n").equals("1")) {
 				latexDocument.write("${}^{"+attributes.getValue("n")+"}$~");
 			}
 		}
 		else
 		if (attributes.getValue("unit").equals("chapter")) {
 			
 			logger.debug("Chapter "+attributes.getValue("n"));
 			
 			if (firstChapter) {
 				latexDocument.write("\\noindent \\textbf{"+attributes.getValue("n")+"} \\hspace{2mm}");
 				firstChapter = false;
 			}
 			else {
 				//latexDocument.write("\\pend\\newpage\n");
 				latexDocument.write("\\pend\\vspace{5mm}\n");
 				latexDocument.write("\\pstart\\noindent \\textbf{"+attributes.getValue("n")+"} \\hspace{2mm}");
 			}			
 		}
 	}
 }
