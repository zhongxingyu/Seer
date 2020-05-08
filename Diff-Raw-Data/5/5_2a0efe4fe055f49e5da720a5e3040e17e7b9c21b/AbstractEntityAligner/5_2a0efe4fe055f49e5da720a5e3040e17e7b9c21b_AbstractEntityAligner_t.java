 /*
  * Copyright (C) 2012  John May and Pablo Moreno
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package uk.ac.ebi.mdk.tool;
 
 import org.apache.log4j.Logger;
 import uk.ac.ebi.mdk.domain.entity.Entity;
 import uk.ac.ebi.mdk.tool.match.EntityAligner;
 import uk.ac.ebi.mdk.tool.match.EntityMatcher;
 
 import java.util.*;
 
 /**
  * Entity resolver controls matching of query entities to a reference using a stack of matching
  * methods. Methods at the top of the stack have the highest priority and are tested first
  *
  * @author John May
  */
 public abstract class AbstractEntityAligner<E extends Entity> implements EntityAligner<E> {
 
     private static final Logger LOGGER = Logger.getLogger(AbstractEntityAligner.class);
 
     protected Stack<EntityMatcher<E, ?>> matchers = new Stack<EntityMatcher<E, ?>>();
     protected Collection<E> references;
     private Map<E, List<E>> matched = new HashMap<E, List<E>>();
 
     /**
      * Whether matches should be cached for future calls.
      */
     private Boolean cached = Boolean.TRUE;
 
     /**
      * Whether the matching is greedy (will collect all matches)
      */
     private Boolean greedy = Boolean.FALSE;
 
 
     public AbstractEntityAligner(Boolean cached, Boolean greedy) {
         this(new ArrayList<E>(), cached, greedy);
     }
 
     public AbstractEntityAligner(Collection<E> references, Boolean cached, Boolean greedy) {
         // take a shallow copy
         this.references = new ArrayList<E>(references);
         this.setCached(cached);
         this.setGreedy(greedy);
     }
 
     /**
      * Add a reference entity to the reference collection
      *
      * @param reference
      */
     public void addReference(E reference) {
         this.references.add(reference);
     }
 
     /**
      * Add multiple references
      *
      * @param references
      */
     public void addReferences(Collection<? extends E> references) {
         this.references.addAll(references);
     }
 
     /**
      * Add a method to the top of the stack (highest priority)
      *
      * @param matcher new method
      *
      * @see Stack#push(Object)
      */
     public void push(EntityMatcher<E, ?> matcher) {
         matchers.push(matcher);
     }
 
     /**
      * Remove a matcher from the top of the stack (highest priority)
      *
      * @return
      *
      * @see java.util.Stack#pop()
      */
     public EntityMatcher<E, ?> pop() {
         return matchers.pop();
     }
 
     /**
      * Peak at the matcher at the top of the stack (highest priority)
      *
      * @return matcher at the top of the stack
      *
      * @see java.util.Stack#peek()
      */
     public EntityMatcher<E, ?> peek() {
         return matchers.peek();
     }
 
     @Override
     public List<E> getMatches(E entity) {
 
         if (cached && matched.containsKey(entity)) {
             return matched.get(entity);
         }
 
        Set<E> seen = new HashSet<E>(); // keep track of those we have seen (maintain list order in 'matches')
         List<E> matches = new ArrayList<E>(0);
 
         for (int i = 0; i < matchers.size(); i++) {
 
 
             for (E matchedEntity : getMatching(entity, matchers.get(i))) {
                 if (!seen.contains(matchedEntity)) {
                     matches.add(matchedEntity);
                     seen.add(matchedEntity);
                 }
             }
 
             // check for greedy match
             if (!greedy && !matches.isEmpty()) {
                 break;
             }
 
         }
 
         // store in the cache
         if (cached) {
             matched.put(entity, matches);
         }
 
         return matches;
     }
 
 
     public Boolean getCached() {
         return cached;
     }
 
     public void setCached(Boolean cached) {
         if (cached == null)
             throw new NullPointerException("Cached value cannot be null");
         this.cached = cached;
     }
 
     public Boolean getGreedy() {
         return greedy;
     }
 
     /**
      * Set whether the matching is greedy. When greedy all matches will be used
      *
      * @param greedy
      */
     public void setGreedy(Boolean greedy) {
        if (greedy == null)
             throw new NullPointerException("Greedy value cannot be null");
         this.greedy = greedy;
     }
 
     /**
      * Clear the method stack
      */
     public void clear() {
         matchers.clear();
     }
 
     public abstract List<E> getMatching(E entity, EntityMatcher matcher);
 
 }
