 package controller;
 
 import model.ParsePDF;
 import view.View;
 /**
  * 
  * @author Fadi M. H. Asbih
  * @email fadi_asbih@yahoo.de
  * @copyright 2014
  * 
  * TERMS AND CONDITIONS:
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 public class StartApp {
         
         public static void main(String[] args) throws Exception {
         	
<<<<<<< HEAD
         ParsePDF pdf = new ParsePDF(); // Call the parse Model
         View view = new View(pdf); // Call the View window
         pdf.addObserver(view); // interaction between Model and View.
=======
    		Version version = new Version(1, 4, 0); // The App Version.
            ParsePDF pdf = new ParsePDF(); // Call the parse Model
            View view = new View(pdf, version); // Call the View window
            pdf.addObserver(view); // interaction between Model and View.
>>>>>>> ff7b6da993dee49fcd7d0b7f716ba904f9ff1ac2
                     
         }
 
 }
