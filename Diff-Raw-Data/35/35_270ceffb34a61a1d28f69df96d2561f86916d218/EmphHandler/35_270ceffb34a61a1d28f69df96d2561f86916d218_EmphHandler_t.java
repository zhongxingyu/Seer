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
 
 public class EmphHandler extends AbstractNoIgnoreHandler {
 	
 	private static Logger logger = LogManager.getLogger(EmphHandler.class.getName());
 
 	public EmphHandler(LatexDocument doc) {
 		super(doc);
 		
 	}
 	
 	@Override
	public void start(String qn) {
		super.start(qn);
		
 		document.write("\\emph{");
 	}
 	
 	public void end() {
 		document.write("}");
 	}
 }
