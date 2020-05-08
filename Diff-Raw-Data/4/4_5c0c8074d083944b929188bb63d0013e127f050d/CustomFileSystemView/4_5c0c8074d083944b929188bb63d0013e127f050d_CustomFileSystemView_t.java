 /*
  * Copyright (c) 2011 Sergey Prilukin
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package jstreamserver.ftp;
 
 import org.apache.ftpserver.ftplet.FileSystemView;
 import org.apache.ftpserver.ftplet.FtpException;
 import org.apache.ftpserver.ftplet.FtpFile;
 
 import java.io.File;
 import java.util.Map;
 
 /**
  * Implementation of {@link FileSystemView}
  * which allows to share only subset of user's folders
  *
  * @author Sergey Prilukin
  */
 public class CustomFileSystemView implements FileSystemView {
     private Map<String, String> rootDirs;
     private FtpFile workingDirectory;
     private RootFtpDir rootFtpDir;
 
     public CustomFileSystemView() {
         /* default constructor */
     }
 
     public CustomFileSystemView(Map<String, String> rootDirs) throws FtpException {
         this.rootDirs = rootDirs;
         this.rootFtpDir = new RootFtpDir(rootDirs);
     }
 
     public void setRootDirs(Map<String, String> rootDirs) {
         this.rootDirs = rootDirs;
         this.rootFtpDir = new RootFtpDir(rootDirs);
     }
 
     @Override
     public FtpFile getHomeDirectory() throws FtpException {
         return new RootFtpDir(rootDirs);
     }
 
     @Override
     public FtpFile getWorkingDirectory() throws FtpException {
         if (workingDirectory == null) {
             return rootFtpDir;
         }
 
         return workingDirectory;
     }
 
     @Override
     public boolean changeWorkingDirectory(String s) throws FtpException {
         try {
             String path = FtpUtils.normalizePath(s, getWorkingDirectory().getAbsolutePath());
             if (path.contains("..")) {
                 return false;
             }
 
             if (RootFtpDir.ROOT_PATH.equals(path)) {
                 workingDirectory = rootFtpDir;
             } else {
                 File file = new File(FtpUtils.getNativePath(path, rootDirs));
                if (!file.exists() || !file.isDirectory()) {
                    return false;
                }

                 workingDirectory = new CustomFtpFile(path, file);
             }
 
             return true;
         } catch (Exception e) {
             return false;
         }
     }
 
     @Override
     public FtpFile getFile(String s) throws FtpException {
         try {
             String path = FtpUtils.normalizePath(s, getWorkingDirectory().getAbsolutePath());
             if (path.contains("..")) {
                 throw new IllegalArgumentException();
             }
 
             if (RootFtpDir.ROOT_PATH.equals(path)) {
                 return rootFtpDir;
             } else {
                 File file = new File(FtpUtils.getNativePath(path, rootDirs));
                 return new CustomFtpFile(path, file);
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public boolean isRandomAccessible() throws FtpException {
         return true;
     }
 
     @Override
     public void dispose() {
         /* do nothing */
     }
 }
