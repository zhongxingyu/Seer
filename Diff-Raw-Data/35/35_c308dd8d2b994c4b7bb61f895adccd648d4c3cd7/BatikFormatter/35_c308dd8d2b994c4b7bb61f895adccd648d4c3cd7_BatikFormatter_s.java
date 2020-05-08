 /*
  * Copyright (c) 2001-2004, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.lagoon.producer;
 
 import java.io.*;
 import java.net.URL;
 
 import org.xml.sax.*;
 import org.apache.batik.transcoder.*;
 import org.apache.batik.transcoder.image.*;
 import org.apache.batik.dom.svg.*;
 
 import nu.staldal.lagoon.core.*;
 import nu.staldal.lagoon.util.*;
 import nu.staldal.util.Utils;
 import nu.staldal.xmlutil.*;
 
 /**
  * Uses Apache Batik version 1.5.1.
  */
 public class BatikFormatter extends Format
 {
	private static boolean DEBUG = true;
 
 	private ImageTranscoder transcoder;
 	
     public void init() throws LagoonException
     {
         String format = getParam("format");
 		if (format == null) 
 			throw new LagoonException("Image format must be specified");
 		
 		if (format.equalsIgnoreCase("jpeg"))
 		{
 			transcoder = new JPEGTranscoder();
 			Float quality;
 			String q = getParam("quality");
 			try {
 				quality = new Float(q);
 			} catch (NumberFormatException e) {
 				throw new LagoonException("Quality must be a number");
 			} catch (NullPointerException e) {
 				quality = new Float(0.8);
 			}
         	transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, quality);
 		}
 		else if (format.equalsIgnoreCase("png"))
 		{
 			transcoder = new PNGTranscoder();
 		}
 		else if (format.equalsIgnoreCase("tiff"))
 		{
 			transcoder = new TIFFTranscoder();
 		}
 		else
 			throw new LagoonException("Unknown image format: " + format);
     }
 
     public void start(OutputStream out, final Target target)
         throws IOException, SAXException
     {
 		URL sourceURL;
 		String _sourceURL = getSourceMan().getSourceURL();
 		if (Utils.absoluteURL(_sourceURL))
 			sourceURL = new URL(_sourceURL);
 		else if (Utils.pseudoAbsoluteURL(_sourceURL))
 			sourceURL = new java.net.URL(getContext().getSourceRootDir().toURL(),
 				_sourceURL.substring(1));
 		else
 			sourceURL = new java.net.URL(getContext().getSourceRootDir().toURL(),
 				_sourceURL);
 		if (DEBUG) System.out.println("The source URL: " + sourceURL.toString());
 
 		TranscoderInput input = new TranscoderInput(sourceURL.toString());
         input.setXMLReader(
 			new XMLReaderImpl() {
 					public void parse(InputSource is) 
 						throws SAXException, IOException
 					{
 						getNext().start(contentHandler, target);							
 					}					
				});			
 
         TranscoderOutput output = new TranscoderOutput(out);
         try {
 			if (DEBUG) System.out.println("about to transcode");
 			transcoder.transcode(input, output);
 			if (DEBUG) System.out.println("transcoding complete");
 		} catch(TranscoderException e) {
 			throw new SAXException(e);
 		}
 	}
 
     public boolean hasBeenUpdated(long when)
         throws LagoonException, IOException
     {
         return getNext().hasBeenUpdated(when);
     }
 
 }
 
