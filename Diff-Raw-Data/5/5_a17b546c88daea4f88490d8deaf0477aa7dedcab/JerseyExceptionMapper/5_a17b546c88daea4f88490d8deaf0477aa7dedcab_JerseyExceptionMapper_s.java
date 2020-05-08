 /*
  * The MIT License
  *
  * Copyright 2013 Gravidence.
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.gravidence.gravifon.exception.mapper;
 
 import javax.ws.rs.NotAllowedException;
 import javax.ws.rs.NotFoundException;
 import javax.ws.rs.WebApplicationException;
 import org.gravidence.gravifon.resource.message.StatusResponse;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 import org.gravidence.gravifon.exception.error.GravifonError;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Mapper that handles {@link WebApplicationException Jersey exceptions}.<p>
  * Logs an exception and produces response according to exception details.<p>
  * Such exceptions should occur rarely, in special cases like
  * {@link Status#NOT_FOUND 404 Not Found} or {@link Status#METHOD_NOT_ALLOWED 405 Method Not Allowed}.
  * All other predictable cases should be covered by {@link GravifonExceptionMapper}.
  * 
  * @see WebApplicationException
  * @see DefaultExceptionMapper
  * @see GravifonExceptionMapper
  * 
  * @author Maksim Liauchuk <maksim_liauchuk@fastmail.fm>
  */
 @Provider
 public class JerseyExceptionMapper implements ExceptionMapper<WebApplicationException> {
     
     private static final Logger LOGGER = LoggerFactory.getLogger(JerseyExceptionMapper.class);
 
     @Override
     public Response toResponse(WebApplicationException exception) {
         LOGGER.error("Jersey exception captured", exception);
         
         StatusResponse entity;
         if (exception instanceof NotFoundException) {
             entity = new StatusResponse(exception.getResponse().getStatus(), "Resource not found.");
         }
         else if (exception instanceof NotAllowedException) {
             entity = new StatusResponse(exception.getResponse().getStatus(), "Resource is unable to handle request.");
         }
         else {
             entity = new StatusResponse(GravifonError.UNEXPECTED.getErrorCode(), "An unexpected internal error.");
         }
         
         return Response
                 .status(exception.getResponse().getStatus())
                 .type(MediaType.APPLICATION_JSON_TYPE)
                 .entity(entity)
                 .build();
     }
     
 }
