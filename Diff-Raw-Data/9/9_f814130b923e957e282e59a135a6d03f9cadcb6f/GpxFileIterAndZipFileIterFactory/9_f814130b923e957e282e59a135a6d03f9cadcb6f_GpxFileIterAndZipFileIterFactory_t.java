 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.xmlimport.gpx;
 
 import com.google.code.geobeagle.gpx.zip.ZipInputStreamFactory;
 import com.google.code.geobeagle.xmlimport.GpxToCache.Aborter;
 import com.google.code.geobeagle.xmlimport.gpx.gpx.GpxFileOpener;
 import com.google.code.geobeagle.xmlimport.gpx.zip.ZipFileOpener;
 import com.google.code.geobeagle.xmlimport.gpx.zip.ZipFileOpener.ZipInputFileTester;
 
 import java.io.IOException;
 
 /**
 * @author sng
 * 
 * Takes a filename and returns an IGpxReaderIter based on the
 * extension: 
 * 
 * zip: ZipFileIter
 * gpx/loc: GpxFileIter
  */
 public class GpxFileIterAndZipFileIterFactory {
     private final Aborter mAborter;
     private final ZipInputFileTester mZipInputFileTester;
 
     public GpxFileIterAndZipFileIterFactory(ZipInputFileTester zipInputFileTester, Aborter aborter) {
         mAborter = aborter;
         mZipInputFileTester = zipInputFileTester;
     }
 
     public IGpxReaderIter fromFile(String filename) throws IOException {
         if (filename.endsWith(".zip")) {
             return new ZipFileOpener(GpxAndZipFiles.GPX_DIR + filename,
                     new ZipInputStreamFactory(), mZipInputFileTester, mAborter).iterator();
         }
         return new GpxFileOpener(GpxAndZipFiles.GPX_DIR + filename, mAborter).iterator();
     }
 
     public void resetAborter() {
         mAborter.reset();
     }
 }
