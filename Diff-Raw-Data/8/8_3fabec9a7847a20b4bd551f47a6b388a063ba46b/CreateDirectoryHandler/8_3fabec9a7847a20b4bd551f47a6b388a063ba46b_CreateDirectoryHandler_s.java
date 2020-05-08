 /*******************************************************************************
  * Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *    Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
  *    may be used to endorse or promote products derived from this software without
  *    specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package org.cyclades.nyxlet.admin.actionhandler;
 
 import java.util.List;
 import java.util.Map;
 import javax.xml.stream.XMLStreamWriter;
 import org.cyclades.annotations.AHandler;
 import org.cyclades.engine.NyxletSession;
 import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
 import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
 import org.cyclades.engine.stroma.STROMAResponseWriter;
 import org.cyclades.io.FileUtils;
 import org.cyclades.nyxlet.admin.util.Auth;
 import org.cyclades.nyxlet.admin.util.Resource;
 import org.cyclades.nyxlet.admin.util.StatusCodeEnum;
 
 /**
 * ActionHandler to handle a directory copy.
  */
 @AHandler({"createdirectory"})
 public class CreateDirectoryHandler extends ActionHandler {
 
     public CreateDirectoryHandler (STROMANyxlet parentNyxlet) {
         super(parentNyxlet);
     }
 
     @Override
     public void init () throws Exception {
         Auth.addPasswordValidation(this);
     }
 
     @Override
     public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "CopyDirectoryHandler.handle: ";
         try {
             StatusCodeEnum statusCodeEnum = StatusCodeEnum.SUCCESS;
             String message;
             String resourceURI = Resource.getRequestResourcePath(nyxletSession, baseParameters, 1);
             if (resourceURI == null) {
                 statusCodeEnum = StatusCodeEnum.REQUEST_FORMAT_ERROR;
                message = "Cannot detect path source (uri)";
             } else {
                 //String absoluteResourceBaseURI = new StringBuilder(Resource.getDocumentRoot(this)).append("/").toString();
                 StringBuilder sb = new StringBuilder();
                 if (baseParameters.containsKey(Resource.SOURCE_PARAMETER)) {
                     FileUtils.copyDirectoryContents(parentNyxlet.getEngineContext().getCanonicalEngineApplicationBaseDirectoryPath(baseParameters.get(Resource.SOURCE_PARAMETER).get(0)),
                             parentNyxlet.getEngineContext().getCanonicalEngineApplicationBaseDirectoryPath(resourceURI), true);
                 } else {
                     FileUtils.verifyOutputDirectory(parentNyxlet.getEngineContext().getCanonicalEngineApplicationBaseDirectoryPath(resourceURI));
                 }
                 sb.append(resourceURI);
                 message = sb.toString();
             }
             if (nyxletSession.rawResponseRequested()) nyxletSession.getHttpServletResponse().setStatus(Integer.parseInt(statusCodeEnum.getCode()));
             XMLStreamWriter streamWriter = stromaResponseWriter.getXMLStreamWriter();
             streamWriter.writeStartElement("entity");
             streamWriter.writeAttribute("status-code", statusCodeEnum.getCode());
             streamWriter.writeAttribute("message", message);
             streamWriter.writeEndElement();
         } catch (Exception e) {
             getParentNyxlet().logStackTrace(e);
             handleException(nyxletSession, stromaResponseWriter, eLabel, e);
         } finally {
             stromaResponseWriter.done();
         }
     }
 }
