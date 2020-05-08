 package gui;
 
 import java.util.*;
 import javax.swing.*;
 
 /**
  * About frame.
  */
 public class AboutFrame extends JFrame {
 	private static final long serialVersionUID = -4920859302858551323L;
 
 	static AboutFrame me;
     
     private AboutFrame () {
         setTitle("About Wombat");
         setSize(600, 400);
         setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
         
        Scanner s = new Scanner(getClass().getResourceAsStream("/License.txt"));
        StringBuilder sb = new StringBuilder();
        while (s.hasNextLine())
        {
            sb.append(s.nextLine());
            sb.append("\n");
        }
        s.close();
        
         JTextArea license = new JTextArea("Wombat - Scheme IDE\n" +
 "Copyright (C) 2011 JP Verkamp\n" +
 "\n" +
 "This program is free software: you can redistribute it and/or modify\n" +
 "it under the terms of the GNU General Public License as published by\n" +
 "the Free Software Foundation, either version 3 of the License, or\n" +
 "(at your option) any later version.\n" +
 "\n" +
 "This program is distributed in the hope that it will be useful,\n" +
 "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
 "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
 "GNU General Public License for more details.\n" +
 "\n" +
 "You should have received a copy of the GNU General Public License\n" +
 "along with this program.  If not, see <http://www.gnu.org/licenses/>.");
         JScrollPane scroll = new JScrollPane(license);
         
         license.setEditable(false);
         
         add(scroll);
     }
     
     public static void showMe() {
         if (me == null)
             me = new AboutFrame();
 
         me.setVisible(true);
     }
 }
