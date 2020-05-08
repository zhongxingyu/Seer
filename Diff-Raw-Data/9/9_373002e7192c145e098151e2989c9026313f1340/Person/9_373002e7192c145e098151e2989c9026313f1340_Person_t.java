 package org.lecharpentier.api.allocine;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The Class Person represents the data for each person related to a film.
  * 
  * @author Adrien Lecharpentier <adrien@lecharpentier.org>
  */
 public final class Person {
 
 	private long code = -1;
 	private String name, firstName, img;
 
 	private Person() {
 	}
 
 	private Person(String firstName, String name) {
 		this.name = name;
 		this.firstName = firstName;
 	}
 
 	/**
 	 * Gets the name.
 	 * 
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Gets the first name.
 	 * 
 	 * @return the first name
 	 */
 	public String getFirstName() {
 		return firstName;
 	}
 
 	/**
 	 * Gets the code.
 	 * 
 	 * @return the code
 	 */
 	public long getCode() {
 		return code;
 	}
 
 	/**
 	 * Gets the img.
 	 * 
 	 * @return the img
 	 */
 	public String getImg() {
 		return img;
 	}
 
 	private void setName(String name) {
 		this.name = name;
 	}
 
 	private void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	private void setCode(long code) {
 		this.code = code;
 	}
 
 	private void setImg(String img) {
 		this.img = img;
 	}
 
 	@Override
 	public String toString() {
 		return firstName + " " + name;
 	}
 
 	/**
 	 * A factory for creating Person objects.
 	 */
 	public static class PersonFactory {
 		private Person p;
 
 		/**
 		 * Creates an empty instance of Person.
 		 */
 		public void create() {
 			p = new Person();
 		}
 
 		/**
 		 * Parses the string that should represents the name of the person.
 		 * 
 		 * @param value
 		 *            the value
 		 * @throws IllegalStateException
 		 *             the illegal state exception
 		 */
 		public void parseString(String value) {
			Matcher m = Pattern.compile("^([\\p{Punct}\\p{Blank}\\p{javaLetterOrDigit}]*) (.*)$").matcher(value);
 			if (m.matches()) {
 				p.setName(m.group(2));
 				p.setFirstName(m.group(1));
 			} else {
 				m = Pattern.compile("^([\\w]*)$").matcher(value);
 				if (m.matches()) {
 					p.setName(m.group());
 				} else {
 					throw new IllegalStateException("The value does not seem to represent a person name : " + value);
 				}
 			}
 		}
 
 		/**
 		 * Sets the image of the person.
 		 * 
 		 * @param img
 		 *            the new img
 		 */
 		public void setImg(String img) {
 			p.setImg(img);
 		}
 
 		/**
 		 * Sets the code.
 		 * 
 		 * @param code
 		 *            the new code
 		 */
 		public void setCode(long code) {
 			p.setCode(code);
 		}
 
 		/**
 		 * Enable to get the created and filled Person instance. If this method
 		 * is called twice before calling the create method, the second call
 		 * will return a null reference.
 		 * 
 		 * @return the person
 		 */
 		public Person validate() {
 			Person t = p;
 			p = null;
 			return t;
 		}
 	}
 
 }
