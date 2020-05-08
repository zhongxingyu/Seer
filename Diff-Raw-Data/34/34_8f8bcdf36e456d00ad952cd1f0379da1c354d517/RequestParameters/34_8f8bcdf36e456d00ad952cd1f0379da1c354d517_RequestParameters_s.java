 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.http.digest;
 
 import com.google.common.base.Objects;
 import org.apache.commons.lang3.Validate;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public final class RequestParameters extends BaseParameters {
 
     private String response;
 
     public RequestParameters() {
         this("", "", "", "", "", "");
     }
 
     public RequestParameters(final String username, final String realm, final String nonce, final String httpMethod,
         final String requestedUri, final String response) {
         super(username, realm, nonce, httpMethod, requestedUri);
         this.response = response;
     }
 
     public String getResponse() {
         return response;
     }
 
     public void setResponse(final String response) {
         Validate.notNull(response);
         this.response = response;
     }
 
     /**
      * The parameter object is only valid if all parameters are not empty strings because all parameters are required
      * for digest authentication.
      *
      * @return {@code true} if and only if all properties are not empty
      */
     public boolean isValid() {
         return !(getUsername().isEmpty()
                 || getRealm().isEmpty()
                 || getNonce().isEmpty()
                 || getHttpMethod().isEmpty()
                 || getRequestedUri().isEmpty()
                 || getResponse().isEmpty());
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(super.hashCode(), response);
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (!super.equals(obj)) {
             return false;
         }
 
         final RequestParameters other = (RequestParameters) obj;
         return Objects.equal(response, other.response);
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
                 .addValue(super.toString())
                 .add("response", response)
                 .toString();
     }
 }
