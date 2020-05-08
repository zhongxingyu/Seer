 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
  * --------------------------------------------------------------------------------------
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
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.web.rpc;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.InvocationException;
 
 import org.mule.galaxy.web.client.ErrorPanel;
 
 public abstract class AbstractCallback<T> implements AsyncCallback<T> {
     private ErrorPanel errorPanel;
 
     public AbstractCallback(ErrorPanel panel) {
         super();
         this.errorPanel = panel;
     }
 
     public void onFailureDirect(Throwable caught) {
         String msg = caught.getMessage();
         
         GWT.log("Error communicating with server: ", caught);
        if (caught instanceof InvocationException) {
             // happens after server is back online, and got a forward to a login page
             // typically would be displayed with a session killed dialog
             errorPanel.setMessage("Current session has been killed, please re-login.");
         } else if (msg != null || !"".equals(msg)) {
             errorPanel.setMessage("Error communicating with server: " + caught.getMessage() + "");
         } else {
             errorPanel.setMessage("There was an error communicating with the server. Please try again." + caught.getClass().getName());
         }
     }
 
     public void onFailure(Throwable caught) {
         onFailureDirect(caught);
     }
     
     
 }
