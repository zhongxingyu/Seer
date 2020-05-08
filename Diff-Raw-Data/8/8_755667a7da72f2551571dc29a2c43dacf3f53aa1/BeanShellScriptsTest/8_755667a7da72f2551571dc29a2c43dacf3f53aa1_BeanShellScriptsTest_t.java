 /*
  * Copyright (C) 2009
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Derived from Travian world" logo. If the display of the logo is not
  * reasonably feasible for technical reasons, the Appropriate Legal Notices must
  * display the words "Derived from Travian world".
  */
 
 package com.funambol.lanciadelta;
 
 import bsh.Interpreter;
 import bsh.TargetError;
 import java.io.File;
 import junit.framework.AssertionFailedError;
 import junit.framework.TestCase;
 import junit.framework.TestResult;
 
 /**
  *
  * @author ste
  */
 public class BeanShellScriptsTest
 extends TestCase
 implements Constants {
 
     private final String SCRIPT_PATH = "src/test/resources";
 
     private Interpreter bsh;
 
     TestResult result;
     
     public BeanShellScriptsTest() throws Exception {
         LanciaDeltaShell shell = new LanciaDeltaShell();
 
         bsh = shell.getInterpreter();
 
         bsh.set("_test", this);
     }
 
     @Override
     public void run(TestResult result) {
         this.result = result;
         super.run(result);
     }
 
 
     public void testAll() {
         File[] scripts = new File(SCRIPT_PATH).listFiles();
        
         for (File script: scripts) {
             if (script.getName().endsWith(".bsh")) {
                 try {
                     bsh.source(script.getAbsolutePath());
                 } catch (TargetError e) {
                     Throwable t = e.getTarget();
                     if ((t != null) && (t instanceof BeanShellAssertionException)) {
                         result.addFailure(this, new AssertionFailedError(t.getMessage()));
                     } else {
                         result.addError(this, t);
                     }
                 } catch (Throwable t) {
                     result.addError(this, t);
                 }
             }
         }
     }
 
     
 
 }
