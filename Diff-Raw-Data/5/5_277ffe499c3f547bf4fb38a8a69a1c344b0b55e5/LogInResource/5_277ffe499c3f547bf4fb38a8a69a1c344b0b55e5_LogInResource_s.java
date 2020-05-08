 package com.syncup.service.resources;
 
 /**
  * Copyright (c) 2012, aditya
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  * disclaimer.Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
  * following disclaimer in the documentation and/or other materials provided with the distribution.
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import com.syncup.service.core.LogInRequest;
 import com.syncup.service.core.LogInResponse;
 import com.syncup.service.core.User;
 import com.syncup.service.db.UserDAO;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.util.List;
 import java.util.UUID;
 
 import com.google.common.cache.Cache;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.lang.RandomStringUtils;
 
 @Path("/login")
 @Produces(MediaType.APPLICATION_JSON)
 public class LogInResource {
 
     private final UserDAO userDAO;
     private final Cache<String, String> cache;
 
     public LogInResource (UserDAO userDAO, Cache<String, String> cache) {
         this.userDAO = userDAO;
         this.cache = cache;
     }
 
     @POST
     public LogInResponse logIn(LogInRequest request) {
 
         if (request.getLoginId() == null || request.getPassword() == null) {
             throw new WebApplicationException(400);
         }
 
         User user = userDAO.findByLoginId(request.getLoginId());
 
         if (user == null)
            throw new WebApplicationException(401);
 
         String password = DigestUtils.sha256Hex(request.getPassword() + user.getSalt()).toString();
 
         if (!password.equals(user.getPassword()))
             throw new WebApplicationException(404);
 
         LogInResponse response = new LogInResponse();
         response.setLoginId(user.getLoginId());
         response.setNonce(request.getNonce());
         String sessionKey;
         if (!cache.asMap().containsKey(user.getLoginId())) {
             UUID uuid = UUID.randomUUID();
             sessionKey = uuid.toString();
             cache.put(user.getLoginId(), sessionKey);
         }
 
         sessionKey = cache.asMap().get(user.getLoginId());
         response.setSessionKey(sessionKey);
 
         return response;
     }
 
 
 }
