 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.wicket;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
 import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.web.GeoServerApplication;
 import org.geotools.util.logging.Logging;
 
 /**
  * GeoServer specific data provider. In addition to the services provided by a
  * SortableDataProvider it can perform keyword based filtering, enum the model
  * properties used for display and sorting
  * 
  * @author Andrea Aime - OpenGeo
  * 
  * @param <T>
  */
 public abstract class GSDataProvider<T> extends SortableDataProvider {
     static final Logger LOGGER = Logging.getLogger(GSDataProvider.class);
     
     /**
      * Keywords used for filtering data
      */
     protected String[] keywords;
 
     /**
      * Returns the current filtering keywords
      * 
      * @return
      */
     public String[] getKeywords() {
         return keywords;
     }
 
     /**
      * Sets the keywords used for filtering
      * 
      * @param keywords
      */
     public void setKeywords(String[] keywords) {
         this.keywords = keywords;
     }
 
     /**
      * Provides catalog access for the provider (cannot be stored as a field,
      * this class is going to be serialized)
      * 
      * @return
      */
     protected Catalog getCatalog() {
         return GeoServerApplication.get().getCatalog();
     }
 
     public Iterator<T> iterator(int first, int count) {
         List<T> items = getFilteredItems();
 
         // global sorting
         Comparator<T> comparator = getComparator(getSort());
         if (comparator != null)
             Collections.sort(items, comparator);
 
         // in memory paging
         int last = first + count;
         if (last > items.size())
             last = items.size();
         return items.subList(first, last).iterator();
     }
 
     /**
      * Returns a filtered list of items. Subclasses can override if they have a
      * more efficient way of filtering than in memory keyword comparison
      * 
      * @return
      */
     protected List<T> getFilteredItems() {
         List<T> items = getItems();
 
         // if needed, filter
         if (keywords != null && keywords.length > 0) {
             return filterByKeywords(items);
         }
         return items;
     }
 
     /**
      * Returns the size of the filtered item collection
      */
     public int size() {
         return getFilteredItems().size();
     }
     
     /**
      * Returns the global size of the collection, without filtering it
      * @return
      */
     public int fullSize() {
         return getItems().size();
     }
 
     private List<T> filterByKeywords(List<T> items) {
         StringBuilder sb = new StringBuilder();
         List<T> result = new ArrayList<T>();
 
         List<Property<T>> properties = getProperties();
         for (T item : items) {
             // reset the builder
             sb.setLength(0);
 
             // build a single string representation of all the
             // properties we filter on
             for (Property<T> property : properties) {
                 Object value = property.getPropertyValue(item);
                 if (value != null)
                     sb.append(value).append(" ");
             }
             String description = sb.toString();
 
             // brute force check for keywords
             for (String keyword : keywords) {
                 if (description.indexOf(keyword) >= 0) {
                     result.add(item);
                     break;
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Returns the list of properties served by this provider. The property keys
      * are used to establish the layer sorting, whilst the Property itself is
      * used to extract the value of the property from the item.
      * 
      * @return
      */
     protected abstract List<Property<T>> getProperties();
 
     /**
      * Returns a non filtered list of all the items the provider must return
      * 
      * @return
      */
     protected abstract List<T> getItems();

     /**
      * Returns a comparator given the sort property.
      * 
      * @param sort
      * @return
      */
     protected Comparator<T> getComparator(SortParam sort) {
         if (sort == null || sort.getProperty() == null)
             return null;
 
         for (Property<T> property : getProperties()) {
             if (sort.getProperty().equals(property.getName())) {
                 Comparator<T> comparator = property.getComparator();
                 if (comparator != null) {
                     if (!sort.isAscending())
                         return new ReverseComparator<T>(comparator);
                     else
                         return comparator;
                 }
             }
         }
         LOGGER.log(Level.WARNING, "Could not find any comparator "
                 + "for sort property " + sort.getProperty());
 
         return null;
     }
 
     /**
      * Simply models the concept of a property in this provider. A property has
      * a key, that identifies it and can be used for i18n, and can return the
      * value of the property given an item served by the {@link GSDataProvider}
      * 
      * @author Andrea Aime - OpenGeo
      * 
      * @param <T>
      */
     public interface Property<T> extends Serializable {
         public String getName();
 
         public Object getPropertyValue(T item);
 
         public IModel getModel(IModel itemModel);
 
         public Comparator<T> getComparator();
     }
 
     /**
      * A Property implementation that uses BeanUtils to access a bean properties
      * 
      * @author Andrea Aime - OpenGeo
      * 
      * @param <T>
      */
     public static class BeanProperty<T> implements Property<T> {
         String name;
 
         String propertyPath;
 
         public BeanProperty(String key, String propertyPath) {
             super();
             this.name = key;
             this.propertyPath = propertyPath;
         }
 
         public String getName() {
             return name;
         }
 
         public Object getPropertyValue(T bean) {
             // allow rest of the code to assume bean != null
             if (bean == null)
                 return null;
 
             try {
                 return PropertyUtils.getProperty(bean, propertyPath);
             } catch (Exception e) {
                 throw new RuntimeException("Could not find property "
                         + propertyPath + " in " + bean.getClass(), e);
             }
         }
 
         public IModel getModel(IModel itemModel) {
             return new PropertyModel(itemModel, propertyPath);
         }
 
         public Comparator<T> getComparator() {
             return new PropertyComparator(this);
         }
     }
     
     /**
      * Placeholder for a column that does not contain a real property (for example,
      * a column containing commands instead of data). Will return the item model
      * as the model, and as the property value.
      * @author Andrea Aime
      *
      * @param <T>
      */
     public static class PropertyPlaceholder<T> implements Property<T> {
         String name;
         
         public PropertyPlaceholder(String name) {
             this.name = name;
         }
 
         public Comparator<T> getComparator() {
             return null;
         }
 
         public IModel getModel(IModel itemModel) {
             return itemModel;
         }
 
         public String getName() {
             return name;
         }
 
         public Object getPropertyValue(T item) {
             return item;
         }
         
     }
     
     /**
      * Uses {@link Property} to extract the values, and then compares
      * them assuming they are {@link Comparable}
      *
      * @param <T>
      */
     public static class PropertyComparator<T> implements Comparator<T> {
         Property<T> property;
         
         public PropertyComparator(Property<T> property) {
             this.property = property;
         }
 
         public int compare(T o1, T o2) {
             Comparable p1 = (Comparable) property.getPropertyValue(o1);
             Comparable p2 = (Comparable) property.getPropertyValue(o2);
 
             // what if any property is null? We assume null < (not null)
             if (p1 == null)
                 return p2 != null ? -1 : 0;
             else if (p2 == null)
                 return 1;
 
             return p1.compareTo(p2);
         }
         
     }
 
     /**
      * A simple comparator inverter
      * 
      * @author Andrea Aime - OpenGeo
      * 
      * @param <T>
      */
     private static class ReverseComparator<T> implements Comparator<T> {
         Comparator<T> comparator;
 
         public ReverseComparator(Comparator<T> comparator) {
             this.comparator = comparator;
         }
 
         public int compare(T o1, T o2) {
             return comparator.compare(o1, o2) * -1;
         }
 
     }
 
 }
