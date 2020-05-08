 /*OsmUi is a user interface for Osmosis
     Copyright (C) 2011  Verena Käfer, Peter Vollmer, Niklas Schnelle
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or 
     any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package de.osmui.i18n;
 import static org.junit.Assert.assertEquals;
 
 import javax.swing.JMenu;
 
 import org.junit.Test;
 
 /**
  * @author Niklas Schnelle, Peter Vollmer, Verena käfer
  * 
  * @see I18N
  * 
  */
 
 public class I18NTest {
 	@Test public void get (){
 		Object values = null;
 		JMenu fileMenu = new JMenu(I18N.getString("Menu.file"));
 		assertEquals("getString: ", fileMenu.getActionCommand(), "Datei");
 		
 		fileMenu = new JMenu(I18N.getString("Menu.file", values));
 		assertEquals("getString: ", fileMenu.getActionCommand(), "Datei");
 		};
 }
