 /*
  *  BlinzCore - core library of audio, video, and other essential classes.
  *  Copyright (C) 2009-2010  BlinzProject <gtalent2@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.blinz.graphics;
 
 import java.io.IOException;
 import java.util.Vector;
 import org.blinz.util.Clients;
 
 /**
  * Has static methods used for loading Images. 
  * @author Blinz Project
  */
 public final class ImageLoader {
 
     /**
      * Catalog of existing ImageStubs
      */
     private static final Vector<ImageStub> stubs = new Vector<ImageStub>();
 
     /**
      * Loads an image object associated from the given path.
      * @param path the path to the image
      * @return an Image object representing the file at the given path
      */
     public final static Image loadImage(final String path) throws IOException {
 
         for (final ImageStub s : stubs) {
             if (s.getPath().equals(path) && s.type == ImageStub.SourceType.LOCAL) {
                 s.incrementClient(Clients.localProcess());
                 return new Image(s);
             }
         }
 
         final ImageStub stub = new ImageStub(path, ImageStub.SourceType.LOCAL);
         stubs.add(stub);
         return new Image(stub);
     }
 
     /**
      * Loads an image object associated from the given URL.
      * @param url the URL of the image
      * @return  an image object associated with the given URL 
      */
     public final static Image loadImageHTTP(final String url) throws IOException {
         for (final ImageStub s : stubs) {
             if (s.getPath().equals(url) && s.type == ImageStub.SourceType.HTTP) {
                 s.incrementClient(Clients.localProcess());
                 return new Image(s);
             }
         }
 
         final ImageStub stub = new ImageStub(url, ImageStub.SourceType.HTTP);
         stubs.add(stub);
         return new Image(stub);
     }
 
     /**
      * Removes Images that are no longer in use.
      */
     static final void clearImages() {
         for (int i = 0; i < stubs.size();) {
            if (stubs.get(i).dependents() == 0) {
                 stubs.remove(i).dumpImage();
             } else {
                 i++;
             }
         }
     }
 
     /**
      * Dumps the image data stored in memory.
      */
     static final void dumpImageData() {
         for (final ImageStub stub : stubs) {
             stub.dumpImage();
         }
     }
 }
