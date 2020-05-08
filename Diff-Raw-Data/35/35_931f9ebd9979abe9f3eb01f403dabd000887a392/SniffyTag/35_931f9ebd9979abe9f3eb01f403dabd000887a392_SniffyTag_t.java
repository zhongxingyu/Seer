 /**
  * InstructionsTag.java, (c) 2011, Immanuel Albrecht; Dresden University of
  * Technology, Professur für die Psychologie des Lernen und Lehrens
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.tu_dresden.psy.efml;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import javax.naming.OperationNotSupportedException;
 
 /**
  * implements class for &lt;sniffy> for adding the sniffy button
  * @author immanuel
  *
  */
 public class SniffyTag implements AnyTag {
 	
 	
 	
 	private String text;
 	private String inform;
 	
 	public SniffyTag() {
 		
 	
 		this.text="";
 		this.inform="";
 	}
 
 	@Override
 	public void open(Writer writer) throws IOException {
 		writer.write("<script type=\"text/javascript\">");
 		writer.write("new SniffyButton(");
 		
 		if (this.text.isEmpty() && this.inform.isEmpty()) {
			writer.write(").WriteHtml();");
 		} else {
 			if (this.text.isEmpty()==false) {
 				writer.write("\""+StringEscape.escapeToJavaScript(this.text)+"\"");
 			} else {
 				writer.write("undefined");
 			}
 			if (this.inform.isEmpty()==false){
 				writer.write(",\""+StringEscape.escapeToJavaScript(this.inform)+"\"");
 			}
			writer.write(").WriteHtml();");
 		}
 		
 		writer.write("</script>");
 	}
 
 	@Override
 	public void close(Writer writer) throws IOException {
 		/**
 		 * no need to do anything
 		 */
 
 	}
 
 	@Override
 	public void encloseTag(AnyTag innerTag)
 			throws OperationNotSupportedException {
 		if (innerTag.getClass() == PlainContent.class) {
 		
 		} else if (innerTag.getClass() == UnusedTag.class) {
 			this.inform += ((UnusedTag) innerTag).getPlainContent();
 		} else if (innerTag.getClass() == LabelTag.class) {
 			this.text += ((LabelTag) innerTag).getPlainContent();
 		} 
 		else
 			throw new OperationNotSupportedException("<sniffy> cannot enclose "
 					+ innerTag.getClass().toString());
 
 	}
 
 }
