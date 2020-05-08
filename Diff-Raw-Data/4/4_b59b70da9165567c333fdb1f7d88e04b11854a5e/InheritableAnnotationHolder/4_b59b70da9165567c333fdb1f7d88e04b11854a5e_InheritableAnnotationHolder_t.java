 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.reflect.plugins;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jboss.reflect.spi.AnnotatedInfo;
 import org.jboss.reflect.spi.AnnotationValue;
 import org.jboss.util.JBossObject;
 
 /**
  * An annotation holder
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public abstract class InheritableAnnotationHolder extends JBossObject implements AnnotatedInfo, Serializable
 {
    /** serialVersionUID */
    private static final long serialVersionUID = 3257290210164289843L;
    
    /** Unknown annotations map */
    static final Map UNKNOWN_ANNOTATIONS_MAP = Collections.unmodifiableMap(new HashMap());
    
    /** Unknown annotations */
    static final AnnotationValue[] UNKNOWN_ANNOTATIONS = new AnnotationValue[0];
    
    /** Declared annotations Map<String, AnnotationValue> */
    protected Map declaredAnnotations = UNKNOWN_ANNOTATIONS_MAP;
    
    /** All annotations Map<String, AnnotationValue> */
    protected Map allAnnotations = UNKNOWN_ANNOTATIONS_MAP;
    
    /** All annotations */
    protected AnnotationValue[] allAnnotationsArray = UNKNOWN_ANNOTATIONS;
    
    /** Declared annotations */
    protected AnnotationValue[] declaredAnnotationsArray = UNKNOWN_ANNOTATIONS;
 
    /** The annotated element */
    protected Object annotatedElement;
    
    /** The annotation helper */
    protected AnnotationHelper annotationHelper;
    
    /**
     * Create a new InheritableAnnotationHolder.
     */
    public InheritableAnnotationHolder()
    {
    }
 
    /**
     * Set the annotation helper
     * 
     * @param helper the helper
     */
    public void setAnnotationHelper(AnnotationHelper helper)
    {
       this.annotationHelper = helper;
    }
 
    public void setAnnotatedElement(Object annotatedElement)
    {
       this.annotatedElement = annotatedElement;
    }
    
    /**
     * Get the declared annotations
     * 
     * @return the declared annotations
     */
    public AnnotationValue[] getDeclaredAnnotations()
    {
       if (declaredAnnotationsArray == UNKNOWN_ANNOTATIONS)
          setupAnnotations(annotationHelper.getAnnotations(annotatedElement));
       return declaredAnnotationsArray;
    }
 
    public AnnotationValue[] getAnnotations()
    {
       if (allAnnotationsArray == UNKNOWN_ANNOTATIONS)
          setupAnnotations(annotationHelper.getAnnotations(annotatedElement));
       return allAnnotationsArray;
    }
 
    public AnnotationValue getAnnotation(String name)
    {
       if (allAnnotations == UNKNOWN_ANNOTATIONS_MAP)
          setupAnnotations(annotationHelper.getAnnotations(annotatedElement));
       return (AnnotationValue) allAnnotations.get(name);
    }
 
    public boolean isAnnotationPresent(String name)
    {
       if (allAnnotations == UNKNOWN_ANNOTATIONS_MAP)
          setupAnnotations(annotationHelper.getAnnotations(annotatedElement));
       return allAnnotations.containsKey(name);
    }
 
    /**
     * Set up the annotations
     * 
     * @param annotations the annotations
     */
    public void setupAnnotations(AnnotationValue[] annotations)
    {
       InheritableAnnotationHolder superHolder = getSuperHolder();
      if (superHolder == null)
      {
         return;
      }
       AnnotationValue[] superAllAnnotations = superHolder.getAnnotations();
       
       
       if (annotations != null && annotations.length > 0)
       {
          declaredAnnotations = new HashMap();
          declaredAnnotationsArray = annotations;
          for (int i = 0; i < annotations.length; i++)
             declaredAnnotations.put(annotations[i].getAnnotationType().getName(), annotations[i]);
          allAnnotations = new HashMap();
          
          if (superHolder != null && superAllAnnotations != null && superAllAnnotations.length != 0)
          {
             for (int i = 0; i < superAllAnnotations.length; i++)
             {
                AnnotationValue av = superAllAnnotations[i];
                if (av.getAnnotationType().isAnnotationPresent("java.lang.annotation.Inherited"));
                   allAnnotations.put(av.getAnnotationType().getName(), av);
             }
          }
          else
             allAnnotationsArray = declaredAnnotationsArray;
 
          for (int i = 0; i < annotations.length; i++)
             allAnnotations.put(annotations[i].getAnnotationType().getName(), annotations[i]);
 
          allAnnotationsArray = (AnnotationValue[])allAnnotations.values().toArray(new AnnotationValue[allAnnotations.size()]);
       }
       else
       {
          if (superHolder != null)
          {
             allAnnotations = superHolder.getAllAnnotations();
             allAnnotationsArray = superAllAnnotations;
          }
       }
    }
    
    /**
     * Get all the annotations as a map
     * 
     * @return the map
     */
    protected Map getAllAnnotations()
    {
       if (allAnnotations == UNKNOWN_ANNOTATIONS_MAP)
          setupAnnotations(annotationHelper.getAnnotations(annotatedElement));
       return allAnnotations;
    }
    
    /**
     * Get the super holder of annoations
     * 
     * @return the super holder
     */
    protected abstract InheritableAnnotationHolder getSuperHolder();
 }
