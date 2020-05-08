 /*
  * Person.java
  *
  * Copyright (C) 2009 Nicola Roberto Viganò
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gestionecassa;
 
 /**
  * Base class that rapresent every actor interacting with this application
  *
  * @author ben
  */
 public class Person {
 
     /**
      * id that identifies a unique Person.
      */
     final int id;
 
     /**
      * Username used by this Person
      */
     final String username;
 
     /**
      * Password used by this Person
      */
     final String password;
 
     /**
      * Tells whether this person is enabled or not
      */
     boolean enabled;
 
     /**
      * Default constructor
      */
     public Person() {
         this(0, "", "",true);
     }
 
     /**
      * Copy construcotr
      * 
      * @param old The person to copy from
      */
     public Person(Person old) {
         this(old.id, old.username, old.password, old.enabled);
     }
 
     /**
      * Explicit construcotr
      *
      * @param id 
      * @param password
      * @param username
     * @param b 
      */
     public Person(int id, String username, String password, boolean b) {
         this.id = id;
        this.password = password;
        this.username = username;
         this.enabled = b;
     }
 
     /**
      * Getter for the id
      * @return An integer containing the id of this Person
      */
     public int getId() {
         return id;
     }
 
     /**
      * Getter for the password field
      * @return A String containing the password
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * Getter for the username field
      * @return A String containing the username
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * Checks if this <code>Person</code> is enabled.
      * @return <code>true</code> if enabled, <code>false</code> otherwise
      */
     public boolean isEnabled() {
         return enabled;
     }
 
     /**
      * Sets a new value for <code>{@link enabled}</code>
      * @param enabled The new value
      */
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     @Override
     public boolean equals(Object obj) {
         return ((obj instanceof Person) && 
                 (this.id == ((Person)obj).id) &&
                 (this.username.equals(((Person)obj).username)) &&
                 (this.password.equals(((Person)obj).password)));
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 97 * hash + this.id;
         hash = 97 * hash + (this.username != null ? this.username.hashCode() : 0);
         return hash;
     }
 }
