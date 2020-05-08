 /*
  * CVSToolBox IntelliJ IDEA Plugin
  *
  * Copyright (C) 2011, Łukasz Zieliński
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * This plugin uses
  * FAMFAMFAM Silk Icons http://www.famfamfam.com/lab/icons/silk
  */
 
 package org.cvstoolbox.handlers;
 
 import com.intellij.CvsBundle;
 import com.intellij.cvsSupport2.cvshandlers.CommandCvsHandler;
 import com.intellij.cvsSupport2.cvshandlers.CvsHandler;
 import com.intellij.cvsSupport2.cvshandlers.FileSetToBeUpdated;
 import com.intellij.cvsSupport2.cvsoperations.common.CompositeOperation;
 import com.intellij.cvsSupport2.cvsoperations.cvsTagOrBranch.BranchOperation;
 import com.intellij.cvsSupport2.cvsoperations.cvsTagOrBranch.TagOperation;
 import com.intellij.cvsSupport2.cvsoperations.cvsUpdate.UpdateOperation;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.vcs.FilePath;
 
 import java.util.Collection;
 
 /**
  * @author Łukasz Zieliński
  */
 public class MultitagHandler {
 
     public static CvsHandler createTagsHandler(FilePath[] selectedFiles, Collection<String> tagNames,
                                                boolean switchToThisAction, String switchToTag,
                                                boolean overrideExisting,
                                                boolean makeNewFilesReadOnly, Project project) {
         CompositeOperation operation = new CompositeOperation();
         for (String tagName : tagNames) {
            operation.addOperation(new BranchOperation(selectedFiles, tagName, overrideExisting));
         }
         if (switchToThisAction) {
             operation.addOperation(new UpdateOperation(selectedFiles, switchToTag, makeNewFilesReadOnly, project));
         }
         return new CommandCvsHandler(CvsBundle.message("operation.name.create.tag"),
                 operation,
                 FileSetToBeUpdated.selectedFiles(selectedFiles));
     }
 
     public static CvsHandler createBranchesHandler(FilePath[] selectedFiles, Collection<String> branchNames,
                                                    boolean switchToThisAction, String switchToBranch,
                                                    boolean overrideExisting,
                                                    boolean makeNewFilesReadOnly, Project project) {
         CompositeOperation operation = new CompositeOperation();
         boolean allowMoveDelete = overrideExisting;
         for (String branchName : branchNames) {
             operation.addOperation(new BranchOperation(selectedFiles, branchName, overrideExisting));
         }
         if (switchToThisAction) {
             operation.addOperation(new UpdateOperation(selectedFiles, switchToBranch, makeNewFilesReadOnly, project));
         }
         return new CommandCvsHandler(CvsBundle.message("operation.name.create.branch"), operation,
                 FileSetToBeUpdated.selectedFiles(selectedFiles));
     }
 
     public static CvsHandler createTagsHandler(FilePath[] selectedFiles, Collection<String> tagNames,
                                                boolean overrideExisting,
                                                boolean makeNewFilesReadOnly, Project project) {
         if (selectedFiles.length > 0) {
             CompositeOperation operation = new CompositeOperation();
             for (String tagName : tagNames) {
                operation.addOperation(new BranchOperation(selectedFiles, tagName, overrideExisting));
             }
             return new CommandCvsHandler(CvsBundle.message("operation.name.create.tag"),
                     operation,
                     FileSetToBeUpdated.selectedFiles(selectedFiles));
         } else {
             return null;
         }
     }
 
     public static CvsHandler createRemoveTagsAction(FilePath[] selectedFiles, Collection<String> tagNames) {
         CompositeOperation operation = new CompositeOperation();
         for (String tagName : tagNames) {
              operation.addOperation(new TagOperation(selectedFiles, tagName, true, false));
         }
         return new CommandCvsHandler(CvsBundle.message("action.name.delete.tag"),operation, FileSetToBeUpdated.EMPTY);
     }
 }
