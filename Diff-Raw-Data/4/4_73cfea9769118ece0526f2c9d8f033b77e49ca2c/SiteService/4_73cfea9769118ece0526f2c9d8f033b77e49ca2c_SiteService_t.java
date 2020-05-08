 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
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
 
 package com.orangeleap.tangerine.service;
 
 import com.orangeleap.tangerine.domain.AbstractEntity;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.customization.FieldDefinition;
 import com.orangeleap.tangerine.domain.customization.FieldRequired;
 import com.orangeleap.tangerine.domain.customization.FieldValidation;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.type.PageType;
 import org.springframework.security.GrantedAuthority;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 public interface SiteService {
 
     public List<Site> readSites();
 
     public Site readSite(String siteName);
 
     public Site createSiteAndUserIfNotExist(String siteName);
 
     public GrantedAuthority[] readDistinctRoles();
 
     /**
      * Populate field maps on entity for non-gui processes.
      * There must be a current 'logged-in' user defined in TangerineUserHelper for this to function correctly.
      *
      * @return
      */
     public AbstractEntity populateDefaultEntityEditorMaps(AbstractEntity entity);
 
     /**
      * Return field required
      *
      * @param pageType the page type to search
      * @param roles    the roles of the current user
      * @return
      */
     public Map<String, FieldRequired> readRequiredFields(PageType pageType, List<String> roles);
 
     /**
      * Return field labels
      *
      * @param pageType the page type to search
      * @param roles    the roles of the current user
      * @param locale   the user locale
      * @return
      */
     public Map<String, String> readFieldLabels(PageType pageType, List<String> roles, Locale locale);
 
     /**
      * Return field validations
      *
      * @param pageType the page type to search
      * @param roles    the roles of the current user
      * @return
      */
     public Map<String, FieldValidation> readFieldValidations(PageType pageType, List<String> roles);
 
     /**
      * Return field values
      *
      * @param pageType the page type to search
      * @param roles    the roles of the current user
      * @param object
      * @return
      */
     public Map<String, Object> readFieldValues(PageType pageType, List<String> roles, Object object);
 
     /**
      * Return field types
      *
      * @param pageType the page type to search
      * @param roles    the roles of the current user
      * @param object
      * @return
      */
     public Map<String, FieldDefinition> readFieldTypes(PageType pageType, List<String> roles);
 
     /**
      * Return section fields
      * @param pageType
      * @param roles
      * @return
      */
 	List<SectionField> getSectionFields(PageType pageType, List<String> roles);
 
	void readFieldInfo(PageType pageType, List<String> roles, Locale locale, AbstractEntity entity);

	Object getDefaultValue(EntityDefault entityDefault, BeanWrapper bean, String key);
 }
