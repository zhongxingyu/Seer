 package com.dottydingo.hyperion.service.persistence.query;
 
 import com.dottydingo.hyperion.service.persistence.PathIterator;
 import cz.jirutka.rsql.parser.model.Comparison;
 
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.From;
 import javax.persistence.criteria.Predicate;
 
 /**
  */
 public class DefaultQueryBuilder<T> extends AbstractQueryBuilder<T>
 {
     private String propertyPath;
     private String propertyName;
 
     public void setPropertyPath(String propertyPath)
     {
         this.propertyPath = propertyPath;
     }
 
     public void setPropertyName(String propertyName)
     {
         this.propertyName = propertyName;
     }
 
 
     @Override
     public Predicate buildPredicate(From root, CriteriaBuilder cb, Comparison operator, String argument)
     {
         From from = getFrom(root,PathIterator.getPath(propertyPath));
 
         Object parsed =  argumentParser.parse(argument,from.get(propertyName).getJavaType());
        return createPredicate(from,cb,propertyName,operator,parsed);
     }
 
     protected From getFrom(From from, PathIterator path)
     {
         if(path.hasNext())
             return getFrom(from.join(path.next()),path);
         return from;
     }
 }
