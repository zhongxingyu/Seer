 /**
  * This file is part of the CheMet library
  *
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * CheMet is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package uk.ac.ebi.annotation.meta;
 
 import junit.framework.TestCase;
 import uk.ac.ebi.annotation.AuthorAnnotation;
 import uk.ac.ebi.interfaces.Annotation;
 
 
 /**
  * @author johnmay
  */
 public class AuthorAnnotationTest extends TestCase {
 
     public AuthorAnnotationTest(String testName) {
         super(testName);
     }
 
 
     public void testToString() {
 
         System.out.println("testToString()");
 
         Annotation annotation = new AuthorAnnotation("Added during gap filling");
 
        String expected = "@" + System.getProperty("user.name") + " Added during gap filling";
 
         assertEquals(expected, annotation.toString());
 
     }
 
 
 }
 
