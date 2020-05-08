 /*
  * @(#)PersonAutoCompleteProvider.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: João Figueiredo, Luis Cruz
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Organization Module.
  *
  *   The Organization Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Organization Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Organization Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package module.organization.presentationTier.renderers.providers;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import module.organization.domain.Party;
 import module.organization.domain.Person;
 import pt.ist.bennu.core.domain.MyOrg;
 import pt.ist.bennu.core.presentationTier.renderers.autoCompleteProvider.AutoCompleteProvider;
 import pt.utl.ist.fenix.tools.util.StringNormalizer;
 
 /**
  * 
  * @author João Antunes
  * @author João Figueiredo
  * 
  */
 public class PersonAutoCompleteProvider implements AutoCompleteProvider {
 
     @Override
     public Collection getSearchResults(Map<String, String> argsMap, String value, int maxCount) {
 	final List<Person> persons = new ArrayList<Person>();
 
 	final String trimmedValue = value.trim();
 	String[] values = StringNormalizer.normalize(value).toLowerCase().split(" ");
 
 	for (final Person person : getPersons(argsMap, value)) {
 	    final String normalizedName = StringNormalizer.normalize(person.getName()).toLowerCase();
	    if (person.getUser() == null)
	        continue;
 	    if (hasMatch(values, normalizedName)) {
 		persons.add(person);
 	    }
	    if (person.getUser().getUsername().indexOf(value) >= 0) {
 		persons.add(person);
 	    }
 	    if (persons.size() >= maxCount) {
 		break;
 	    }
 	}
 
 	Collections.sort(persons, Party.COMPARATOR_BY_NAME);
 
 	return persons;
     }
 
     /**
      * Should be overridden by subclasses to allow filtering of the Search
      * Results
      */
     protected Collection<Person> getPersons(Map<String, String> argsMap, String value) {
 	return MyOrg.getInstance().getPersonsSet();
     }
 
     private boolean hasMatch(final String[] input, final String unitNameParts) {
 	for (final String namePart : input) {
 	    if (unitNameParts.indexOf(namePart) == -1) {
 		return false;
 	    }
 	}
 	return true;
     }
 
 }
