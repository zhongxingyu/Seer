 /**
  * Swift Ticket -- Back End
  *
  * Copyright (C) 2013, Jonathan Gillett, Daniel Smullen, and Rayan Alfaheid
  * All rights reserved.
  *
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 

 /**
 * @author 100440203
 *
  */
 public class Delete implements Transaction
 {
     private String username;
     private String type;
     private Double credit;
     private String transaction;
 
 
     /**
      * Constructor for the class. Accepts and populates class attributes with values passed in.
      *
      * @param username String containing the username of the user to delete.
      * @param type String containing the type of the user to delete.
      * @param credit Dollar value containing the credit amount of the user to be deleted.
      * @param transaction String containing the transaction string from the daily transaction file.
      */
     public Delete(String username, String type, Double credit, String transaction)
     {
         this.username = username;
         this.type = type;
         this.credit = credit;
         this.transaction = transaction;
     }
 
     /**
      * execute Performs the delete transaction. Deletes a user account and updates
      * the current user accounts file.
      * 
      * @param cua A reference to the CurrentUserAccounts object used by the back-end.
      * @param atf A reference to the AvailableTickets object used by the back-end.
      * 
      * @throws FailedConstraint Throws a failed constraint under the following circumstances:
      * 			<br>If the user name specified does not exist.
      * 
      * @see Transaction#execute(CurrentUserAccounts, AvailableTickets)
      */
     public void execute(CurrentUserAccounts cua, AvailableTickets atf) throws FailedConstraint
     {
         if (cua.hasUser(username))
         {
         	cua.deleteUser(username);
         }
         else
         {
         	throw new FailedConstraint(ExceptionCodes.UNKNOWN_USER, transaction);
         }
     }
 
 	/* (non-Javadoc)
 	 * @see Transaction#getTransaction()
 	 */
 	public String getTransaction() {
 		return transaction;
 	}
 
 }
