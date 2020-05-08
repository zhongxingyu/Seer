 /****************************************************************************/
 /*  File:       FunctionalTests.java                                        */
 /*  Author:     F. Georges - H2O Consulting                                 */
 /*  Date:       2010-12-01                                                  */
 /*  Tags:                                                                   */
 /*      Copyright (c) 2010 Florent Georges (see end of file.)               */
 /* ------------------------------------------------------------------------ */
 
 
 package functional.run;
 
 import java.io.File;
 import org.junit.Test;
 import static org.junit.Assert.fail;
 
 /**
  * TODO: ...
  *
  * @author Florent Georges
  * @date   2010-12-01
  */
 public class FunctionalTest
 {
    public static final String HELLO_XAR_OLD = "../../misc/hello-pkg/hello-1.1.xar";
    public static final String HELLO_XAR_NEW = "../../misc/hello-pkg/hello-1.2.xar";
    public static final String TMP_REPO_DIR  = "../../tmp/repo";
 
     @Test
     public void runFunctionalTests()
             throws Throwable
     {
         // Initialize a new temporary repo.
         File repo_dir = new File(TMP_REPO_DIR);
         if ( repo_dir.exists() ) {
             fail("The directory exists: " + TMP_REPO_DIR);
         }
         if ( ! repo_dir.mkdirs() ) {
             fail("Error creating the directory: " + TMP_REPO_DIR);
         }
 
         // Run the actual tests...
         InstallPackage test = new InstallPackage();
         test.testInstall();
 
         // Tear down the temporary repo.
         recursiveDelete(repo_dir);
     }
 
 
     static private void recursiveDelete(File file)
     {
         if ( file.isDirectory() ) {
             for ( File child : file.listFiles() ) {
                 recursiveDelete(child);
             }
         }
         if ( ! file.delete() ) {
             fail("Error deleting the file/dir: " + file);
         }
     }
 }
 
 
 /* ------------------------------------------------------------------------ */
 /*  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT.               */
 /*                                                                          */
 /*  The contents of this file are subject to the Mozilla Public License     */
 /*  Version 1.0 (the "License"); you may not use this file except in        */
 /*  compliance with the License. You may obtain a copy of the License at    */
 /*  http://www.mozilla.org/MPL/.                                            */
 /*                                                                          */
 /*  Software distributed under the License is distributed on an "AS IS"     */
 /*  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See    */
 /*  the License for the specific language governing rights and limitations  */
 /*  under the License.                                                      */
 /*                                                                          */
 /*  The Original Code is: all this file.                                    */
 /*                                                                          */
 /*  The Initial Developer of the Original Code is Florent Georges.          */
 /*                                                                          */
 /*  Contributor(s): none.                                                   */
 /* ------------------------------------------------------------------------ */
