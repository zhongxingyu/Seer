 /*
  * utils - AndValidator.java - Copyright © 2011 David Roden
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
 
 package net.pterodactylus.util.validation;
 
 /**
  * {@link Validator} implementation that requires a number of other
  * {@link Validator}s to validate an object.
  *
  * @param <T>
  *            The type of the object being validated
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class AndValidator<T> extends ValidatorContainer<T> {
 
 	/**
 	 * Creates a new “AND” validator.
 	 */
 	public AndValidator() {
 		super();
 	}
 
 	/**
 	 * Creates a new “AND” validator.
 	 *
 	 * @param validators
 	 *            The required validators
 	 */
 	public AndValidator(Validator<T>... validators) {
 		super(validators);
 	}
 
 	//
 	// VALIDATOR METHODS
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean validate(T value) {
 		for (Validator<T> validator : getValidators()) {
 			if (!validator.validate(value)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	//
 	// OBJECT METHODS
 	//
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 		StringBuffer string = new StringBuffer().append('(');
 		for (Validator<T> validator : getValidators()) {
			if (string.length() > 1) {
 				string.append(" && ");
 			}
 			string.append(validator);
 		}
 		return string.append(')').toString();
 	}
 
 }
