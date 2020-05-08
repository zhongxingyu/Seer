 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 package org.netbeans.modules.javafx.editor.completion;
 
 public class ReportedIssuesTest extends CompletionTestBase {
 
     public ReportedIssuesTest(String testName) {
         super(testName);
     }
 
     public void testIssue175333() throws Exception {
         checkCompletion("Iz175333", "ba^", "iz175333.pass");
     }
 
     /* Currently the completion returns what it should for this case, but also
      * adds some garbage (packages) for some reason, which causes this test to
      * fail, thus disabled temporarily. */
     public void DISABLED_testIssue173358() throws Exception {
         checkCompletion("Iz173358", "print^", "iz173358.pass");
     }
 
     public void testIssue165374() throws Exception {
         checkCompletion("Iz165374", "var c = ^", "iz165374.pass");
     }
 
     public void testIssue159678() throws Exception {
         // XXX: seeems that we need JavaBinaryIndexer to be registered during
         // test run. Do not know how to do that correctly yet.
         // checkCompletion("Iz159678", "var color = Color^.", "iz159678beforeDot.pass");
         checkCompletion("Iz159678", "Color.^", "iz159678afterDot.pass");
     }
 
     public void testIssue171484() throws Exception {
         checkCompletion("Iz171484", "var bbb = a^", "iz171484.pass");
     }
 
     /** This code completion case used to throw NPE. Now it works at least for
      * a partially written identifier (i.e. when the VariableTree is recognized).
      * More correct test case would be to use just "override var ^".
      */
     public void testIssue171185() throws Exception {
         checkCompletion("Iz171185", "override^", " var a", "iz171185.pass");
     }
 
     /** There is a bug in modifiers completion that forces repetition
      * of the last modifier in completion. Separate issue but visible
      */
     public void DISABLED_testOverrideOverride() throws Exception {
         checkCompletion("Iz171185", "override^", " ", "iz171185_override.pass");
     }
 
     public void testIssue167875() throws Exception {
         checkCompletion("Iz167875", "data: LineChart.^", " ", "iz167875.pass");
     }
 
     public void testIssue150039() throws Exception {
         checkCompletion("Iz150039", "    func: ^", "iz150039.pass");
     }
 
     public void testIssue173838() throws Exception {
         checkCompletion("Iz173838", "class B extends^", "iz173838.pass");
     }
 
    public void testIssue156041() throws Exception {
        checkCompletion("Iz156041", "var a = ^", "iz156041.pass");
    }

 }
 
