 // Copyright FreeHEP, 2005-2007.
 package org.freehep.maven.nar;
 
 /**
  * Fortran compiler tag
  *
  * @author <a href="Mark.Donszelmann@slac.stanford.edu">Mark Donszelmann</a>
 * @version $Id: plugin/src/main/java/org/freehep/maven/nar/Fortran.java 631dc18040bb 2007/07/17 14:21:11 duns $
  */
 public class Fortran extends Compiler {
   
	Fortran() {
 	}
 	
     public String getName() {
         return "fortran";
     }         
 }
