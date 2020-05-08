 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.jsf2.annotation;
 
 import com.thoughtworks.qdox.JavaDocBuilder;
 import com.thoughtworks.qdox.model.Annotation;
 import com.thoughtworks.qdox.model.JavaClass;
 import com.thoughtworks.qdox.model.JavaSource;
 import org.apache.myfaces.scripting.api.AnnotationScanListener;
 import org.apache.myfaces.scripting.api.AnnotationScanner;
 import org.apache.myfaces.scripting.core.util.ProxyUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.myfaces.scripting.core.scanEvents.events.AnnotatedArtefactRemovedEvent;
 import org.apache.myfaces.scripting.refresh.FileChangedDaemon;
 import org.apache.myfaces.scripting.refresh.ReloadingMetadata;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  *          <p/>
  *          Source path annotation scanner for java it scans all sources in the specified source paths
  *          recursively for additional information
  *          and then adds the id/name -> class binding information to the correct factory locations,
  *          wherever possible
  */
 
 public class JavaAnnotationScanner extends BaseAnnotationScanListener implements AnnotationScanner {
 
     List<AnnotationScanListener> _listeners = new LinkedList<AnnotationScanListener>();
     JavaDocBuilder _builder = new JavaDocBuilder();
     Map<String, String> _registeredAnnotations = new HashMap<String, String>();
     LinkedList<String> _sourcePaths = new LinkedList<String>();
 
     public JavaAnnotationScanner() {
         initDefaultListeners();
     }
 
     public JavaAnnotationScanner(String... sourcePaths) {
 
         for (String source : sourcePaths) {
             _sourcePaths.addFirst(source);
         }
         initDefaultListeners();
     }
 
     public void addScanPath(String sourcePath) {
         _sourcePaths.addFirst(sourcePath);
     }
 
     Collection<Annotation> filterAnnotations(Annotation[] anns) {
         List<Annotation> retVal = new ArrayList<Annotation>(anns.length);
         if (anns == null) {
             return retVal;
         }
         for (Annotation ann : anns) {
            if (ann.getType().getValue().startsWith("javax.faces")) {
                 retVal.add(ann);
             }
         }
         return retVal;
     }
 
     Collection<java.lang.annotation.Annotation> filterAnnotations(java.lang.annotation.Annotation[] anns) {
         List<java.lang.annotation.Annotation> retVal = new ArrayList<java.lang.annotation.Annotation>(anns.length);
         if (anns == null) {
             return retVal;
         }
         for (java.lang.annotation.Annotation ann : anns) {
            if (ann.annotationType().getName().startsWith("javax.faces")) {
                 retVal.add(ann);
             }
 
         }
         return retVal;
     }
 
     public void scanClass(Class clazz) {
         //java.lang.annotation.Annotation[] anns = clazz.getAnnotations();
 
         java.lang.annotation.Annotation[] anns = clazz.getAnnotations();
         Collection<java.lang.annotation.Annotation> annCol = filterAnnotations(anns);
         if (!annCol.isEmpty()) {
             addOrMoveAnnotations(clazz);
         } else {
             removeAnnotations(clazz);
         }
     }
 
 
     private void initSourcePaths() {
         _builder = new JavaDocBuilder();
         for (String sourcePath : _sourcePaths) {
             File source = new File(sourcePath);
             if (source.exists()) {
 
                 _builder.addSourceTree(source);
 
             }
         }
     }
 
     private void initDefaultListeners() {
         _listeners.add(new BeanImplementationListener());
         _listeners.add(new BehaviorImplementationListener());
         _listeners.add(new ComponentImplementationListener());
         _listeners.add(new ConverterImplementationListener());
         _listeners.add(new RendererImplementationListener());
         _listeners.add(new ValidatorImplementationListener());
     }
 
     /**
      * builds up the parsing chain and then notifies its observers
      * on the found data
      */
     public void scanPaths() {
         initSourcePaths();
         JavaSource[] sources = _builder.getSources();
         for (JavaSource source : sources) {
             JavaClass[] classes = source.getClasses();
             for (JavaClass clazz : classes) {
                 Annotation[] anns = clazz.getAnnotations();
                 if (anns != null && anns.length > 0) {
                     addOrMoveAnnotations(clazz, anns);
                 } else {
                     removeAnnotations(clazz);
                 }
             }
 
         }
     }
 
     /**
      * add or moves a class level annotation
      * to a new place
      *
      * @param clazz
      * @param anns
      */
     private void addOrMoveAnnotations(JavaClass clazz, Annotation[] anns) {
         for (Annotation ann : anns) {
             for (AnnotationScanListener listener : _listeners) {
                 if (listener.supportsAnnotation(ann.getType().getValue())) {
                     listener.registerSource(
                             clazz, ann.getType().getValue(), ann.getPropertyMap());
 
                     _registeredAnnotations.put(clazz.getFullyQualifiedName(), ann.getType().getValue());
                     ReloadingMetadata metaData = FileChangedDaemon.getInstance().getClassMap().get(clazz.getFullyQualifiedName());
                     if (metaData != null) {
                         metaData.setAnnotated(true);
                     }
                 }
             }
         }
     }
 
     /**
      * add or moves a class level annotation
      * to a new place
      *
      * @param clazz
      */
     private void addOrMoveAnnotations(Class clazz) {
         java.lang.annotation.Annotation[] anns = clazz.getAnnotations();
         for (java.lang.annotation.Annotation ann : anns) {
             for (AnnotationScanListener listener : _listeners) {
                 if (listener.supportsAnnotation(ann.annotationType().getName())) {
                     listener.register(clazz, ann);
 
                     _registeredAnnotations.put(clazz.getName(), ann.annotationType().getName());
 
                     ReloadingMetadata metaData = FileChangedDaemon.getInstance().getClassMap().get(clazz.getName());
                     if (metaData != null) {
                         metaData.setAnnotated(true);
                     }
                 }
             }
         }
     }
 
     /**
      * use case annotation removed
      * we have to entirely remove the annotation
      * from our internal registry and the myfaces registry
      *
      * @param clazz
      */
     private void removeAnnotations(JavaClass clazz) {
         String registeredAnnotation = _registeredAnnotations.get(clazz.getFullyQualifiedName());
         if (registeredAnnotation != null) {
             for (AnnotationScanListener listener : _listeners) {
                 if (listener.supportsAnnotation(registeredAnnotation)) {
                     listener.purge(clazz.getFullyQualifiedName());
                     _registeredAnnotations.remove(clazz.getFullyQualifiedName());
                     ProxyUtils.getEventProcessor().dispatchEvent(new AnnotatedArtefactRemovedEvent(clazz.getFullyQualifiedName()));
                     FileChangedDaemon.getInstance().getClassMap().remove(clazz.getFullyQualifiedName());
                 }
             }
         }
     }
 
     /**
      * use case annotation removed
      * we have to entirely remove the annotation
      * from our internal registry and the myfaces registry
      *
      * @param clazz
      */
     private void removeAnnotations(Class clazz) {
         String registeredAnnotation = _registeredAnnotations.get(clazz.getName());
         if (registeredAnnotation != null) {
             for (AnnotationScanListener listener : _listeners) {
                 if (listener.supportsAnnotation(registeredAnnotation)) {
                     listener.purge(clazz.getName());
                     _registeredAnnotations.remove(clazz.getName());
                     FileChangedDaemon.getInstance().getClassMap().remove(clazz.getName());
                 }
             }
         }
     }
 
 
     /**
      * use case annotation moved
      * we have to remove the annotation from the myfaces registry of the old
      * listeners place, the new entry is done in the first step add or move
      *
      * @param clazz
      * @param ann
      * @param listener
      */
     private void annotationMoved(JavaClass clazz, Annotation ann, AnnotationScanListener listener) {
         //case class exists but it has been moved to anoter annotation
         String registeredAnnotation = _registeredAnnotations.get(clazz.getFullyQualifiedName());
         if (registeredAnnotation != null && registeredAnnotation.equals(ann.getType().getValue())) {
             removeAnnotations(clazz);
         }
     }
 
     private void annotationMoved(Class clazz, java.lang.annotation.Annotation ann, AnnotationScanListener listener) {
         //case class exists but it has been moved to anoter annotation
         String registeredAnnotation = _registeredAnnotations.get(clazz.getName());
         if (registeredAnnotation != null && registeredAnnotation.equals(ann.getClass().getName())) {
             removeAnnotations(clazz);
         }
     }
 
 
     public void clearListeners() {
         _listeners.clear();
     }
 
     public void addListener(AnnotationScanListener listener) {
         _listeners.add(listener);
     }
 
 }
