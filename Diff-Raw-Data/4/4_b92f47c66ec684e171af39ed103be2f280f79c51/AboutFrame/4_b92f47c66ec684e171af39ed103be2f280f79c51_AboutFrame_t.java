 package wombat.gui.frames;
 
import java.util.Calendar;
 import java.util.GregorianCalendar;
 import javax.swing.*;
 
 /**
  * About frame.
  */
 public class AboutFrame extends JFrame {
 	private static final long serialVersionUID = -4920859302858551323L;
 
 	static AboutFrame me;
 	/**
 	 * AboutFrame constructor
 	 * contains the information for the JLabel that is displayed
 	 * Symbol, creator, committees, and licensing
 	 * @return a Close object
 	 */
     private AboutFrame () {
         setTitle("About Wombat");
         setSize(600, 400);
         setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
         GregorianCalendar cal = new GregorianCalendar();        
         JLabel license = new JLabel(
     "<html><style type=\"text/css\"> body { text-align:center}</style>"+
     "<div>"+"\n" +
     "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@</span><br />"+
     "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@<br />"+
     "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@<br />"+
     "@@@@@@@0000000000000000000@@@@@@@@@@@@@@@<br />"+
     "@@@0000000000000000000000000000000@@@@@@@@@@@@<br />"+
     "@@00000000000000000000000000000000000000000@@@@@@@<br />"+
     "@0000000000000000000000000000000000000000000000@@@@@<br />"+
     "@000000000000000000000000000000000000000000000000@@@@<br />"+
     "@000000000000000000000000000000000000000000000000000@@<br />"+
     "@@000000000000000000000000000000000000000000000000000@<br />"+
 	"@@0000000000000000000000000000000000000000000000@@@@<br />"+
 	"@@@0000000000000000@@@@@0000000000000@@@@@@@@<br />"+
 	"@@@@@000@@@@00@@@@@@00@@@00000@@@@@@@@<br />"+
 	"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@<br />"+
 	"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@<br />"+
 	"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@<br />"+
 	
     "<br /><br />"+
     "<span>Wombat - Scheme IDE</span><br />" +
     "Version: "+ wombat.Wombat.VERSION   
    +"<br />"+"Copyright (C) 2011-" + cal.get(Calendar.YEAR) + " JP Verkamp<br />" +
     "<br /><br />"+
     "This program is free software: you can redistribute it and/or modify<br />" +
     "it under the terms of the GNU General Public License as published by <br />" +
     "the Free Software Foundation, either version 3 of the License, or<br />" +
     "(at your option) any later version.<br />" +
     "<br />" +
     "This program is distributed in the hope that it will be useful,<br />" +
     "but WITHOUT ANY WARRANTY; without even the implied warranty of<br />" +
     "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br />" +
     "GNU General Public License for more details.<br />" +
     "<br />" +
     "You should have received a copy of the GNU General Public License<br />" +
     "along with this program.  If not, see <http://www.gnu.org/licenses/>."+
     "<br /><br /><br />"+ 
     "Libraries<br />"+
     "Infonode Docking windows<br />"+
     "Petite Chez Scheme<br />"+
     "Base64"+"</div><html/>",JLabel.CENTER);
             license.setHorizontalTextPosition(JLabel.CENTER);
            
             
             JScrollPane scroll = new JScrollPane(license);
             
         
             add(scroll);
         }
     /**
 	 * Shows the About Frame
 	 * @return void
 	 * @see JFrame
 	 */
     	public static void showMe() {
             if (me == null)
                 me = new AboutFrame();
 
             me.setVisible(true);
         }
 }
