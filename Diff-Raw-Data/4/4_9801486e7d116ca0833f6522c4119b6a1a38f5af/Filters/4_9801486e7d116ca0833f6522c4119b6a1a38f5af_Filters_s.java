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
 
 package org.cvstoolbox.filter;
 
 import com.intellij.cvsSupport2.CvsUtil;
 import com.intellij.cvsSupport2.util.CvsVfsUtil;
 import com.intellij.openapi.vcs.FilePath;
 import com.intellij.openapi.vfs.VirtualFile;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Łukasz Zieliński
  */
 public class Filters {
 
 
     public static FilePath[] pruneLocallyAdded(FilePath[] toPrune) {
         List<FilePath> paths = new ArrayList<FilePath>(Arrays.asList(toPrune));
         List<FilePath> toRemove = new ArrayList<FilePath>(paths.size());
         for (FilePath path : paths) {
             if (CvsUtil.fileIsLocallyAdded(path.getIOFile())) {
                 toRemove.add(path);
             }
         }
         paths.removeAll(toRemove);
         return paths.toArray(new FilePath[paths.size()]);
     }
 
     public static VirtualFile[] pruneEmptyDirectories(VirtualFile[] toPrune) {
         return pruneEmptyDirectories(Arrays.asList(toPrune));
     }
 
     public static VirtualFile[] pruneEmptyDirectories(Collection<VirtualFile> toPrune) {
         List<VirtualFile> files = new ArrayList<VirtualFile>(toPrune);
         List<VirtualFile> toRemove = new ArrayList<VirtualFile>(files.size());
         for (VirtualFile file : files) {
            if (file.isDirectory() && file.getChildren().length == 1 &&
                    file.getChildren()[0].getName().equals(CvsUtil.CVS)) {
                 toRemove.add(file);
             }
         }
         files.removeAll(toRemove);
         return files.toArray(new VirtualFile[files.size()]);
     }
 }
