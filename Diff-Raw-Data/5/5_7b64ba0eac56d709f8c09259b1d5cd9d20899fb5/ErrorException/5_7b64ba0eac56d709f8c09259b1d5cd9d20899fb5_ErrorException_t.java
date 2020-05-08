 /*  copyit-server
  *  Copyright (C) 2013  Toon Schoenmakers
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.mms_projects.copy_it.api.http.pages.exceptions;
 
 import io.netty.handler.codec.http.HttpResponseStatus;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
 
 public class ErrorException extends Exception {
    protected static final String ERROR_MESSAGES = "errors";
 
     public ErrorException(String message) {
         super();
         errors = new ArrayList<String>();
         errors.add(message);
         status = INTERNAL_SERVER_ERROR;
     }
 
     public void addError(String message) {
         errors.add(message);
     }
 
     public String getMessage() {
         final StringBuilder output = new StringBuilder();
         for (int i = 0; i < errors.size(); i++)
             output.append(errors.get(i) + "\n");
         return output.toString();
     }
 
     public String toString() {
         return toJSON().toString();
     }
 
     public JSONObject toJSON() {
         final JSONObject json = new JSONObject();
         try {
             json.put(ERROR_MESSAGES, errors);
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return json;
     }
 
     public HttpResponseStatus getStatus() {
         return status;
     }
 
     public void setStatus(HttpResponseStatus status) {
         this.status = status;
     }
 
     protected final List<String> errors;
     protected HttpResponseStatus status;
 }
