 /*
  File: AboutPanel.java
 
  Copyright 2010 - The Cytoscape Consortium (www.cytoscape.org)
  
  Code written by: Layla Oesper
  Authors: Layla Oesper, Ruth Isserlin, Daniele Merico
  
  This library is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public License
  along with this project.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cytoscape.csplugins.wordcloud;
 import java.awt.*;
 import javax.swing.*;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.html.HTMLEditorKit;
 import cytoscape.*;
 
 /**
  * This class handles about WordCloud popup from the WordCloud menu.
  * @version 1.0
  */
 
 public class AboutPanel extends JDialog {
 
 	private static final long serialVersionUID = 5805773796102997091L;
 	
	String pluginUrl = "http://www.baderlab.org/Software/WordCloud";
 
     public AboutPanel() {
         super(Cytoscape.getDesktop(), "About WordCloud", false);
         setResizable(false);
 
         //main panel for dialog box
         JEditorPane editorPane = new JEditorPane();
         editorPane.setMargin(new Insets(10,10,10,10));
         editorPane.setEditable(false);
         editorPane.setEditorKit(new HTMLEditorKit());
         editorPane.addHyperlinkListener(new HyperlinkAction(editorPane));
 
         editorPane.setText(
                 "<html><body>"+
                 "<table border='0'><tr>" +
                 "<td width='125'></td>"+
                 "<td width='200'>"+
                 "<p align=center><b>WordCloud"+ "</b><BR>" + 
                 "A Cytoscape Plugin<BR>" +
                 "<BR></p>" +
                 "</td>"+
                 "<td width='125'><div align='right'>" + //<img height='77' width='125' src=\""+
                 //"\" ></div></td>"+
                 "</tr></table>" +
                 "<p align=center>WordCloud is a Cytoscape plugin that generates a word tag cloud<BR>"+
                 "from a user-define node selection, summarizing an attribute of choice.<BR>" +
                 "<BR>" +
                 "by Layla Oesper, Daniele Merico, Ruth Isserlin and Gary Bader<BR>" +
                 "(<a href='http://www.baderlab.org/'>Bader Lab</a>, University of Toronto)<BR>" +
                 "<BR>" +
                 "Plugin Homepage:<BR>" +
                 "<a href='" + pluginUrl + "'>" + pluginUrl + "</a><BR>" +
                 "<BR>" +
                 "<font size='-1'>" + "</font>" +
                 "</p></body></html>"
             );
         setContentPane(editorPane);
     }
 
     private class HyperlinkAction implements HyperlinkListener {
         @SuppressWarnings("unused")
         JEditorPane pane;
 
         public HyperlinkAction(JEditorPane pane) {
             this.pane = pane;
         }
 
         public void hyperlinkUpdate(HyperlinkEvent event) {
             if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                 cytoscape.util.OpenBrowser.openURL(event.getURL().toString());
             }
         }
     }	
 }
