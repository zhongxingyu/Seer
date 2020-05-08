 /*
  * Copyright 2011 Vasily Shiyan
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
 
 package see.properties.impl;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import see.parser.grammar.PropertyAccess;
 import see.properties.PartialResolver;
 import see.properties.PropertyResolver;
 
 import static see.properties.PropertyResolvers.universalResolver;
 
 public class AggregatingResolver implements PropertyResolver {
 
     private final Iterable<PartialResolver> resolvers;
 
     private final PartialResolver defaultResolver;
 
    public AggregatingResolver(Iterable<PartialResolver> resolvers, PropertyResolver defaultResolver) {
        this.resolvers = resolvers;
         this.defaultResolver = universalResolver(defaultResolver);
     }
 
     @Override
     public Object get(final Object bean, final PropertyAccess property) {
         PropertyResolver resolver = Iterables.find(resolvers, new Predicate<PartialResolver>() {
             @Override
             public boolean apply(PartialResolver input) {
                 return input.canGet(bean, property);
             }
         }, defaultResolver);
 
         return resolver.get(bean, property);
     }
 
     @Override
     public void set(final Object bean, final PropertyAccess property, final Object value) {
         PropertyResolver resolver = Iterables.find(resolvers, new Predicate<PartialResolver>() {
             @Override
             public boolean apply(PartialResolver input) {
                 return input.canSet(bean, property, value);
             }
         }, defaultResolver);
 
         resolver.set(bean, property, value);
     }
 }
