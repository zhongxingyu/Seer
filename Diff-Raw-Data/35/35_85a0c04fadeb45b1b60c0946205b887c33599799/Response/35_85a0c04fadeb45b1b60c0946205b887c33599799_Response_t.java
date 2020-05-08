 /*
  * Metro allows customers from any affiliate library to join any other member library.
  *    Copyright (C) 2013  Edmonton Public Library
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301, USA.
  *
  */
 package mecard;
 
 import mecard.customer.Customer;
 
 /**
  * Response object container for Metro. Contains the response code from the 
  * request, the response message which may be empty and customer information
  * which may be 'null'.
  * @author Andrew Nisbet <anisbet@epl.ca>
  */
 public final class Response
 {
     protected ResponseTypes code;
     protected String responseMessage;
     protected Customer customer;
     
     public Response()
     {
         code = ResponseTypes.INIT;
         responseMessage = "";
         customer = null;
     }
     
     /**
      * 
      * @param rt response type.
      */
     public Response(ResponseTypes rt)
     {
         code = rt;
         responseMessage = "";
         customer = null;
     }
     
     /**
      * 
      * @param s sets the response string.
      */
     public void setResponse(String s)
     {
        if (this.responseMessage.length() > 0) // There  is already a message to be passed back so add to it.
        {
            this.responseMessage += Protocol.DEFAULT_DELIMITER + s;
        }
        else
        {
            this.responseMessage = s;
        }
     }
     
     /**
      * Sets the responses customer object.
      * @param c customer object.
      */
     public void setCustomer(Customer c)
     {
         this.customer = c;
     }
 
     /**
      * Sets the response code, but only if the ordinal of the argument code 
      * is higher than the current code. SUCCESS can be overwritten by PIN_CHANGE_REQUIRED
      * but not the other way around.
      * @param code 
      */
     public void setCode(ResponseTypes code) 
     {
         // Smaller codes like OK can be overwritten by PIN_CHANGE_REQUIRED
         // but PIN_CHANGE_REQUIRED can't be overwritten by SUCCESS.
         if (code.ordinal() > this.code.ordinal())
         {
             this.code = code;
         }
     }
     
     @Override
     public String toString()
     {
         StringBuilder sb = new StringBuilder();
         sb.append("[\"");
         sb.append(this.code.name());
         sb.append("\", \"");
         sb.append(this.responseMessage);
         if (customer != null)
         {
             sb.append("\", \"");
             sb.append(this.customer.toString());
         }
         sb.append("\"]");
         return sb.toString();
     }
 
     /**
      * Gets the response code.
      * @return response code.
      */
     public ResponseTypes getCode()
     {
         return code;
     }
 
     /**
      * Gets the customer object if any.
      * @return Customer object.
      */
     public Customer getCustomer()
     {
         return customer;
     }
 
     /**
      * Gets the message of the response, used to display messages to the user.
      * @return message to the user.
      */
     public String getMessage()
     {
         return responseMessage;
     }
 }
