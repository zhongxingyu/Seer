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
 import org.libteidoc.AbstractNoIgnoreHandler;
 
 public class NoteHandler extends AbstractNoIgnoreHandler {
 	
 	protected static Logger logger = LogManager.getLogger(NoteHandler.class.getName());
 
 	public NoteHandler(LatexDocument doc) {
 		super(doc);
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
	public void start() {
 		
 		if (attributes.getValue("type") == null) {
			logger.error("Element 'note' needs 'type' attribute!");
 			return;
 		}
 		
 		switch (attributes.getValue("type")) {
 		case "freinsheim":
 			document.write("\\edtext{}{\\Afootnote{");
 			break;
 		case "testimonia":
 			document.write("\\edtext{}{\\Bfootnote{");
 		}
 	}
 	
 	public void end() {
 		document.write("}}");
 	}
 
 }
