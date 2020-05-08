 /*
  * Copyright (C) 2013 University of Edinburgh.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.org.ukfederation.mda;
 
import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.annotation.concurrent.ThreadSafe;
 
 import net.shibboleth.metadata.FirstItemIdItemIdentificationStrategy;
 import net.shibboleth.metadata.Item;
 import uk.org.ukfederation.mda.validate.mdrpi.RegistrationAuthority;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 
 /**
  * Item identification strategy for UK federation deployment.
  * 
  * The basic identifier strategy is to use a {@link UKId} if one is present, or
  * fall back to the {@link  FirstItemIdItemIdentificationStrategy} (which in turn
  * falls back to a configurable static value such as "unknown").
  * 
  * To this we add a component based on a {@link RegistrationAuthority} if one of
  * those is present.  This second component can be omitted if present but is a
  * member of a specified blacklist, and it can be mapped to a simpler value if
  * desired.
  */
 @ThreadSafe
 public class UKItemIdentificationStrategy extends FirstItemIdItemIdentificationStrategy {
 
     /**
      * Set of authorities to be ignored.
      */
     @Nonnull private Set<String> ignoredAuthorities = Collections.emptySet();
     
     /**
      * Replacement display names for authorities.
      */
     @Nonnull private Map<String, String> displayNames = Collections.emptyMap();
     
     /**
      * Returns the set of authorities we are ignoring.
      * 
      * @return {@link Set} of authority names.
      */
    @Nonnull public Collection<String> getIgnoredAuthorities() {
         return ignoredAuthorities;
     }
 
     /**
      * Set the set of authorities we are ignoring.
      * 
      * @param authorities {@link Set} of authority names to ignore.
      */
    public void setIgnoredAuthorities(@Nullable final Collection<String> authorities) {
         if (authorities == null || authorities.isEmpty()) {
             ignoredAuthorities = Collections.emptySet();
         } else {
             ignoredAuthorities = ImmutableSet.copyOf(authorities);
         }
     }
 
     /**
      * Returns the map of display names for authorities.
      * 
      * @return {@link Map} of display names for authorities.
      */
     public Map<String, String> getDisplayNames() {
         return displayNames;
     }
 
     /**
      * Sets the map of display names for authorities.
      * 
      * @param names {@link Map} of display names for authorities.
      */
     public void setDisplayNames(Map<String, String> names) {
         if (names == null || names.isEmpty()) {
             displayNames = Collections.emptyMap();
         } else {
             displayNames = ImmutableMap.copyOf(names);
         }
     }
 
     /**
      * Derive a basic identifier for the given entity.
      * 
      * @param item {@link Item} to derive an identifier for.
      * 
      * @return basic identifier as a {@link String}.
      */
     @Nonnull private String basicIdentifier(@Nonnull final Item<?> item) {
         final List<UKId> itemIds = item.getItemMetadata().get(UKId.class);
         if (itemIds != null && !itemIds.isEmpty()) {
             return itemIds.get(0).getId();
         } else {
             return super.getItemIdentifier(item);
         }
     }
     
     /**
      * Derive a display name for an entity's registrar, if it has one.
      * 
      * @param item {@link Item} to derive an identifier for.
      * 
      * @return registrar name, or <code>null</code>.
      */
     @Nullable private String registrationAuthority(@Nonnull final Item<?> item) {
         final List<RegistrationAuthority> regAuths = item.getItemMetadata().get(RegistrationAuthority.class);
         
         // nothing to return if there isn't a registration authority
         if (regAuths == null || regAuths.isEmpty()) {
             return null;
         }
         
         final String regAuth = regAuths.get(0).getRegistrationAuthority();
         
         // nothing to return if it's an ignored authority
         if (ignoredAuthorities.contains(regAuth)) {
             return null;
         }
         
         // handle mapping it to a simpler form if that's available
         final String displayName = displayNames.get(regAuth);
         if (displayName != null) {
             return displayName;
         } else {
             return regAuth;
         }
     }
     
     /** {@inheritDoc} */
     public String getItemIdentifier(@Nonnull final Item<?> item) {
         assert item != null;
         
         // derive a basic identifier
         final String basicID = basicIdentifier(item);
         
         // extend with something indicating the registration authority if one is available
         final String regAuth = registrationAuthority(item);
         if (regAuth != null) {
             return basicID + " (" + regAuth + ")";
         } else {
             return basicID;
         }
     }
 
 }
