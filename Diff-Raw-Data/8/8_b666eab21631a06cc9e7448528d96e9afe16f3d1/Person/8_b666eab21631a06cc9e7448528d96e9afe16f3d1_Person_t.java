 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.beans.common;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 
 /**
  * List element of the list held by the <code>PersonManager</code>
  */
 public class Person extends AbstractBean {
 
 	/**
 	 * Property name of the first name property ("firstname").
 	 */
 	public static final String PROPERTY_FIRSTNAME = "firstname"; //$NON-NLS-1$
 	/**
 	 * Property name of the first name property ("lastname").
 	 */
 	public static final String PROPERTY_LASTNAME = "lastname"; //$NON-NLS-1$
 	/**
 	 * Property name of the number property ("number").
 	 */
 	public static final String PROPERTY_NUMBER = "number"; //$NON-NLS-1$
 	/**
 	 * Property name of the birthday property ("birthday").
 	 */
 	public static final String PROPERTY_BIRTHDAY = "birthday"; //$NON-NLS-1$
 	/**
 	 * Property name of the birthplace property ("birthplace").
 	 */
 	public static final String PROPERTY_BIRTHPLACE = "birthplace"; //$NON-NLS-1$
 	/**
 	 * Property name of the eye color property ("eyeColor").
 	 */
 	public static final String PROPERTY_EYE_COLOR = "eyeColor"; //$NON-NLS-1$
 	/**
 	 * Property name of the gender property ("{@value} ").
 	 */
 	public static final String PROPERTY_GENDER = "gender"; //$NON-NLS-1$
 	/**
 	 * Property name of the pets property ("{@value} ").
 	 */
 	public static final String PROPERTY_PETS = "pets"; //$NON-NLS-1$
	/**
	 * Property name of the address property ("{@value} ").
	 */
	public static final String PROPERTY_ADDRESS = "address"; //$NON-NLS-1$
 
 	/**
 	 * Constant for <code>MALE</code> gender value ("male").
 	 */
 	public final static String MALE = "male"; //$NON-NLS-1$
 	/**
 	 * Constant for <code>FEMALE</code> gender value ("female").
 	 */
 	public final static String FEMALE = "female"; //$NON-NLS-1$
 
 	/**
 	 * Types of Pets a person can have.
 	 */
 	public static enum Pets {
 		CAT, DOG, FISH
 	}
 
 	private Integer number;
 	private String lastname;
 	private String firstname;
 	private String gender;
 	private boolean hasDog;
 	private boolean hasCat;
 	private boolean hasFish;
 	private String birthday;
 	private String birthplace;
 	private Address address;
 	private int eyeColor;
 
 	/**
 	 * constructor.
 	 * 
 	 * @param lastname
 	 * @param firstname
 	 */
 	public Person(String lastname, String firstname) {
 		super();
 
 		this.lastname = lastname;
 		this.firstname = firstname;
 		number = 0;
 		birthday = ""; //$NON-NLS-1$
 		birthplace = ""; //$NON-NLS-1$
 		gender = MALE;
 		address = new Address();
 	}
 
 	/**
 	 * @return last name
 	 */
 	public String getLastname() {
 		return lastname;
 	}
 
 	/**
 	 * @return first name
 	 */
 	public String getFirstname() {
 		return firstname;
 	}
 
 	/**
 	 * @return eye color
 	 */
 	public Integer getEyeColor() {
 		return Integer.valueOf(eyeColor);
 	}
 
 	/**
 	 * @param lastname
 	 */
 	public void setLastname(String lastname) {
 		String oldLastname = this.lastname;
 		this.lastname = lastname;
 
 		firePropertyChanged(PROPERTY_LASTNAME, oldLastname, lastname);
 	}
 
 	/**
 	 * @param firstname
 	 */
 	public void setFirstname(String firstname) {
 		String oldFirstname = this.firstname;
 		this.firstname = firstname;
 
 		firePropertyChanged(PROPERTY_FIRSTNAME, oldFirstname, firstname);
 	}
 
 	/**
 	 * @param newEyeColor
 	 */
 	public void setEyeColor(Integer newEyeColor) {
 		if (newEyeColor != null) {
 			setEyeColor(newEyeColor.intValue());
 		}
 	}
 
 	/**
 	 * @param newEyeColor
 	 */
 	public void setEyeColor(int newEyeColor) {
 		int oldEyeColor = eyeColor;
 		eyeColor = newEyeColor;
 
 		firePropertyChanged(PROPERTY_EYE_COLOR, Integer.valueOf(oldEyeColor), Integer.valueOf(eyeColor));
 	}
 
 	/**
 	 * @param value
 	 */
 	public void setEyeColorGreen(boolean value) {
 		if (value) {
 			setEyeColor(0);
 		}
 	}
 
 	/**
 	 * @param value
 	 */
 	public void setEyeColorGray(boolean value) {
 		if (value) {
 			setEyeColor(1);
 		}
 	}
 
 	/**
 	 * @param value
 	 */
 	public void setEyeColorBlue(boolean value) {
 		if (value) {
 			setEyeColor(2);
 		}
 	}
 
 	/**
 	 * @param value
 	 */
 	public void setEyeColorBrown(boolean value) {
 		if (value) {
 			setEyeColor(3);
 		}
 	}
 
 	/**
 	 * @return gender
 	 */
 	public String getGender() {
 		return gender;
 	}
 
 	/**
 	 * @param gender
 	 */
 	public void setGender(String gender) {
 		Assert.isLegal(MALE.equals(gender) || FEMALE.equals(gender));
 		if (gender != this.gender) {
 			String oldValue = this.gender;
 			this.gender = gender;
 			firePropertyChanged(PROPERTY_GENDER, oldValue, this.gender);
 		}
 	}
 
 	/**
 	 * @return the hasDaughter.
 	 */
 	public boolean isHasDog() {
 		return hasDog;
 	}
 
 	/**
 	 * @param hasDog
 	 *            The hasDog to set.
 	 */
 	public void setHasDog(boolean hasDog) {
 		if (this.hasDog != hasDog) {
 			this.hasDog = hasDog;
 			firePropertyChanged(PROPERTY_PETS, null, getPets());
 		}
 	}
 
 	/**
 	 * @return the hasSon.
 	 */
 	public boolean isHasCat() {
 		return hasCat;
 	}
 
 	/**
 	 * @param hasCat
 	 *            The hasCat to set.
 	 */
 	public void setHasCat(boolean hasCat) {
 		if (this.hasCat != hasCat) {
 			this.hasCat = hasCat;
 			firePropertyChanged(PROPERTY_PETS, null, getPets());
 		}
 	}
 
 	/**
 	 * @return the hasFish.
 	 */
 	public boolean isHasFish() {
 		return hasFish;
 	}
 
 	/**
 	 * @param hasFish
 	 *            The hasFish to set.
 	 */
 	public void setHasFish(boolean hasFish) {
 		if (this.hasFish != hasFish) {
 			this.hasFish = hasFish;
 			firePropertyChanged(PROPERTY_PETS, null, getPets());
 		}
 	}
 
 	public List<Pets> getPets() {
 		List<Pets> result = new ArrayList<Pets>();
 		if (hasCat) {
 			result.add(Pets.CAT);
 		}
 		if (hasDog) {
 			result.add(Pets.DOG);
 		}
 		if (hasFish) {
 			result.add(Pets.FISH);
 		}
 		return result;
 	}
 
 	public void setPets(List<Pets> pets) {
 		setHasCat(pets.contains(Pets.CAT));
 		setHasDog(pets.contains(Pets.DOG));
 		setHasFish(pets.contains(Pets.FISH));
 		firePropertyChanged(PROPERTY_PETS, null, getPets());
 	}
 
 	@Override
 	public String toString() {
 		return lastname + ", " + firstname; //$NON-NLS-1$
 	}
 
 	/**
 	 * Return object for presentation in a list.
 	 * 
 	 * @return a string representing object as list entry.
 	 */
 	public String getListEntry() {
 		return lastname + " - " + firstname; //$NON-NLS-1$
 	}
 
 	/**
 	 * @return the birthday.
 	 */
 	public String getBirthday() {
 		return birthday;
 	}
 
 	/**
 	 * @param birthday
 	 *            The birthday to set.
 	 */
 	public void setBirthday(String birthday) {
 		Object oldValue = this.birthday;
 		this.birthday = birthday;
 		firePropertyChanged(PROPERTY_BIRTHDAY, oldValue, birthday);
 	}
 
 	public void setBirthplace(String birthplace) {
 		Object oldValue = this.birthplace;
 		this.birthplace = birthplace;
 		firePropertyChanged(PROPERTY_BIRTHPLACE, oldValue, birthplace);
 	}
 
 	public String getBirthplace() {
 		return birthplace;
 	}
 
 	public void setAddress(Address address) {
		Address oldValue = this.address;
 		this.address = address;
		firePropertyChanged(PROPERTY_ADDRESS, oldValue, address);
 	}
 
 	public Address getAddress() {
 		return address;
 	}
 
 	public void setNumber(Integer number) {
 		Object oldValue = this.number;
 		this.number = number;
 		firePropertyChanged(PROPERTY_NUMBER, oldValue, number);
 	}
 
 	public Integer getNumber() {
 		return number;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((address == null) ? 0 : address.hashCode());
 		result = prime * result + ((birthday == null) ? 0 : birthday.hashCode());
 		result = prime * result + ((birthplace == null) ? 0 : birthplace.hashCode());
 		result = prime * result + eyeColor;
 		result = prime * result + ((firstname == null) ? 0 : firstname.hashCode());
 		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
 		result = prime * result + (hasCat ? 1231 : 1237);
 		result = prime * result + (hasDog ? 1231 : 1237);
 		result = prime * result + (hasFish ? 1231 : 1237);
 		result = prime * result + ((lastname == null) ? 0 : lastname.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		Person other = (Person) obj;
 		if (address == null) {
 			if (other.address != null) {
 				return false;
 			}
 		} else if (!address.equals(other.address)) {
 			return false;
 		}
 		if (birthday == null) {
 			if (other.birthday != null) {
 				return false;
 			}
 		} else if (!birthday.equals(other.birthday)) {
 			return false;
 		}
 		if (birthplace == null) {
 			if (other.birthplace != null) {
 				return false;
 			}
 		} else if (!birthplace.equals(other.birthplace)) {
 			return false;
 		}
 		if (eyeColor != other.eyeColor) {
 			return false;
 		}
 		if (firstname == null) {
 			if (other.firstname != null) {
 				return false;
 			}
 		} else if (!firstname.equals(other.firstname)) {
 			return false;
 		}
 		if (gender == null) {
 			if (other.gender != null) {
 				return false;
 			}
 		} else if (!gender.equals(other.gender)) {
 			return false;
 		}
 		if (hasCat != other.hasCat) {
 			return false;
 		}
 		if (hasDog != other.hasDog) {
 			return false;
 		}
 		if (hasFish != other.hasFish) {
 			return false;
 		}
 		if (lastname == null) {
 			if (other.lastname != null) {
 				return false;
 			}
 		} else if (!lastname.equals(other.lastname)) {
 			return false;
 		}
 		return true;
 	}
 }
