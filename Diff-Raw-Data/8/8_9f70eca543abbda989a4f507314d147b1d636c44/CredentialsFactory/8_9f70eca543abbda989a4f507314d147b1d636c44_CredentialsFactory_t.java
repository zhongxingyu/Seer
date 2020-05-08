 /**
     This file is part of Bibbla.
 
     Bibbla is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Bibbla is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Bibbla.  If not, see <http://www.gnu.org/licenses/>.    
  **/
 
 package dat255.grupp06.bibbla;
 
 import dat255.grupp06.bibbla.model.Credentials;
import dat255.grupp06.bibbla.model.CredentialsMissingException;
 
 /**
  * Creates session objects.
  * If class PrivateCredentials is present, uses its details for session creation.
  * If not, creates an anonymous session.
  * 
  * @author Niklas Logren
  */
 public class CredentialsFactory {
 
 	/**
 	 * Returns a normal session if PrivateCredentials class is present,
 	 * and returns an anonymous session if PrivateCredentials is missing.
 	 * 
 	 * Note: 
 	 * All login-needed test code should automatically return success if 
 	 * the session is anonymous!
 	 */
 	public static Credentials getCredentials() {
 
 		// Try to use PrivateCredentials.
 		Class<?> c;
 		String name="", code="", pin="";
 		try {
 			// Look for PrivateCredentials.
 			c = Class.forName("dat255.grupp06.bibbla.utils.PrivateCredentials");
 			// Private credentials found! Extract values.
 			name = (String) c.getField("name").get(null);
 			code = (String) c.getField("code").get(null);
 			pin = (String) c.getField("pin").get(null);
 		} catch (Exception e) {
 			// PrivateCredentials / its fields not found? Return an anonymous session.
 			return null;
 		}
 
 		// Create a new normal Session, which keeps track of our session cookies.
		try {
			return new Credentials(name, code, pin);
		} catch (CredentialsMissingException e) {
			return null;
		}
 	}
 
 }
