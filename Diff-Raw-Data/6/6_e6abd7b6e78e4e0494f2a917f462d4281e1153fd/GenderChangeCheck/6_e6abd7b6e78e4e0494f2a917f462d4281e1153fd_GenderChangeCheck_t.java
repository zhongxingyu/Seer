 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2004  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 
 package net.sf.clirr.checks;
 
 import net.sf.clirr.event.ApiDifference;
 import net.sf.clirr.event.Severity;
 import net.sf.clirr.framework.AbstractDiffReporter;
 import net.sf.clirr.framework.ApiDiffDispatcher;
 import net.sf.clirr.framework.ClassChangeCheck;
 import org.apache.bcel.classfile.JavaClass;
 
 /**
  * Detects gender changes (a class became an interface or vice versa).
  *
  * @author lkuehne
  */
 public final class GenderChangeCheck
         extends AbstractDiffReporter
         implements ClassChangeCheck
 {
 
     /**
      * Create a new instance of this check.
      * @param dispatcher the diff dispatcher that distributes the detected changes to the listeners.
      */
     public GenderChangeCheck(ApiDiffDispatcher dispatcher)
     {
         super(dispatcher);
     }
 
 
     /** {@inheritDoc} */
     public boolean check(JavaClass baseLine, JavaClass current)
     {
         if (baseLine.isClass() != current.isClass())
         {
             getApiDiffDispatcher().fireDiff(new ApiDifference(
                     "Changed Gender of " + baseLine.getClassName(), Severity.ERROR,
                     baseLine.getClassName(), null, null)
             );
         }
 
         return true;
     }
 }
