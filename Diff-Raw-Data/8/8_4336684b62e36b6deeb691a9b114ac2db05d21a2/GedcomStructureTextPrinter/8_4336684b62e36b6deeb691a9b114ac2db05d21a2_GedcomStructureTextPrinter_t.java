 /**
  *    Copyright 2013 Thomas Naeff (github.com/thnaeff)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package ch.thn.gedcom.printer;
 
 import ch.thn.gedcom.GedcomFormatter;
 import ch.thn.gedcom.data.GedcomLine;
 import ch.thn.util.tree.printable.PrintableTreeNode;
 import ch.thn.util.tree.printable.printer.SimpleTreePrinter;
 import ch.thn.util.tree.printable.printer.TreePrinter;
import ch.thn.util.tree.printable.printer.TreePrinterLineContent;
import ch.thn.util.tree.printable.printer.TreePrinterLineContent.ContentType;
 
 /**
  * A printer which prints the gedcom structure in text format. The output 
  * of this printer can be saved in a gedcom textfile to import the gedcom data 
  * into a software which supports the gedcom standard.
  * 
  * @author Thomas Naeff (github.com/thnaeff)
  *
  */
 public class GedcomStructureTextPrinter extends SimpleTreePrinter<String, GedcomLine> {
 
 	
 	public GedcomStructureTextPrinter() {
 		super(true, true);
 		
 		HEAD = "";
 		FIRST_CHILD = "";
 		START = "";
 		END = "";
 		INTERMEDIATE = "";
 		THROUGH = "";
 		AFTEREND = "";
 		ADDITIONALLINETHROUGH = "";
 		ADDITIONALLINEAFTEREND = "";
 		START_OF_LINE = "";
 		END_OF_LINE = "" + TreePrinter.LINE_SEPARATOR;
 		
 	}
 	
 	@Override
	public TreePrinterLineContent[] getNodeValue(
 			PrintableTreeNode<String, GedcomLine> currentNode,
 			int currentNodeLevel, int treeNodeIndex, int childNodeIndex,
 			boolean lastChildNode) {
 		
 		StringBuilder sb = new StringBuilder();
 		int level = getNodeLevel(currentNode);
 		
 		sb.append(GedcomFormatter.makeInset(level));
 		sb.append(level);
 		sb.append(" ");
 		sb.append(currentNode.print());
 		
		return new TreePrinterLineContent[] {new TreePrinterLineContent(ContentType.VALUE, sb.toString())};
 		
 	}
 	
 	
 }
