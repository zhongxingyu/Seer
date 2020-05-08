 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.feature;
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import org.geotools.factory.FactoryRegistryException;
 import org.geotools.factory.Hints;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.FeatureTypes;
 import org.geotools.feature.IllegalAttributeException;
 import org.geotools.feature.SchemaException;
 import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.ReferencingFactoryFinder;
 import org.opengis.filter.Filter;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform2D;
 import org.opengis.referencing.operation.OperationNotFoundException;
 import org.opengis.referencing.operation.TransformException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 
 /**
  * Decorating feature collection which reprojects feature geometries to a particular coordinate
  * reference system on the fly.
  * <p>
  * The coordinate reference system of feature geometries is looked up using
  * {@link com.vividsolutions.jts.geom.Geometry#getUserData()}.
  * </p>
  * <p>
  * The {@link #defaultSource} attribute can be set to specify a coordinate refernence system
  * to transform from when one is not specified by teh geometry itself. Leaving the property
  * null specifies that the geometry will not be transformed.
  * </p>
  * @author Justin Deoliveira, The Open Planning Project
  *
  */
 public class ReprojectingFeatureCollection extends DecoratingFeatureCollection {
     /**
      * The schema of reprojected features
      */
     FeatureType schema;
 
     /**
      * The target coordinate reference system
      */
     CoordinateReferenceSystem target;
 
     /**
      * Coordinate reference system to use when one is not
      * specified on an encountered geometry.
      */
     CoordinateReferenceSystem defaultSource;
 
     /**
      * MathTransform cache, keyed by source CRS
      */
     HashMap /*<CoordinateReferenceSystem,GeometryCoordinateSequenceTransformer>*/ transformers;
 
     /**
      * Transformation hints
      */
     Hints hints = new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
 
     public ReprojectingFeatureCollection(FeatureCollection delegate,
         CoordinateReferenceSystem target)
         throws SchemaException, OperationNotFoundException, FactoryRegistryException,
             FactoryException {
         super(delegate);
 
         this.target = target;
        this.schema = FeatureTypes.transform(delegate.getFeatureType(), target);
 
         //create transform cache
         transformers = new HashMap();
 
         //cache "default" transform
        CoordinateReferenceSystem source = delegate.getFeatureType().getDefaultGeometry()
                                                    .getCoordinateSystem();
 
         if (source != null) {
             MathTransform2D tx = (MathTransform2D) ReferencingFactoryFinder.getCoordinateOperationFactory(hints)
                                                                            .createOperation(source,
                     target).getMathTransform();
 
             GeometryCoordinateSequenceTransformer transformer = new GeometryCoordinateSequenceTransformer();
             transformer.setMathTransform(tx);
             transformers.put(source, transformer);
         } else {
             //throw exception?
         }
     }
 
     public void setDefaultSource(CoordinateReferenceSystem defaultSource) {
         this.defaultSource = defaultSource;
     }
 
     public FeatureIterator features() {
         return new ReprojectingFeatureIterator(delegate.features());
     }
 
     public Iterator iterator() {
         return new ReprojectingIterator(delegate.iterator());
     }
 
     public void close(FeatureIterator iterator) {
         if (iterator instanceof ReprojectingFeatureIterator) {
             delegate.close(((ReprojectingFeatureIterator) iterator).getDelegate());
         }
 
         iterator.close();
     }
 
     public void close(Iterator iterator) {
         if (iterator instanceof ReprojectingIterator) {
             delegate.close(((ReprojectingIterator) iterator).getDelegate());
         }
     }
 
     public FeatureType getFeatureType() {
         return schema;
     }
 
     public FeatureType getSchema() {
         return schema;
     }
 
     public FeatureCollection subCollection(Filter filter) {
         FeatureCollection sub = delegate.subCollection(filter);
 
         if (sub != null) {
             try {
                 ReprojectingFeatureCollection wrapper = new ReprojectingFeatureCollection(sub,
                         target);
                 wrapper.setDefaultSource(defaultSource);
 
                 return wrapper;
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
 
         return null;
     }
 
     public Object[] toArray() {
         Object[] array = delegate.toArray();
 
         for (int i = 0; i < array.length; i++) {
             try {
                 array[i] = reproject((Feature) array[i]);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
 
         return array;
     }
 
     public Object[] toArray(Object[] a) {
         Object[] array = delegate.toArray(a);
 
         for (int i = 0; i < array.length; i++) {
             try {
                 array[i] = reproject((Feature) array[i]);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
 
         return array;
     }
 
     public ReferencedEnvelope getBounds() {
         Envelope bounds = new Envelope();
         Iterator i = iterator();
 
         try {
             if (!i.hasNext()) {
                 bounds.setToNull();
 
                 return ReferencedEnvelope.reference(bounds);
             } else {
                 Feature first = (Feature) i.next();
                 bounds.init(first.getBounds());
             }
 
             for (; i.hasNext();) {
                 Feature f = (Feature) i.next();
                 bounds.expandToInclude(f.getBounds());
             }
         } finally {
             close(i);
         }
 
         return ReferencedEnvelope.reference(bounds);
     }
 
     public FeatureCollection collection() throws IOException {
         return this;
     }
 
     Feature reproject(Feature feature) throws IOException {
         Object[] attributes = new Object[schema.getAttributeCount()];
 
         for (int i = 0; i < attributes.length; i++) {
             AttributeType type = schema.getAttributeType(i);
 
             Object object = feature.getAttribute(type.getName());
 
             if (object instanceof Geometry) {
                 //check for crs
                 Geometry geometry = (Geometry) object;
                 CoordinateReferenceSystem crs = (CoordinateReferenceSystem) geometry.getUserData();
 
                 if (crs == null) {
                     // no crs specified on geometry, check default
                     if (defaultSource != null) {
                         crs = defaultSource;
                     }
                 }
 
                 if (crs != null) {
                     //if equal, nothing to do
                     if (!crs.equals(target)) {
                         GeometryCoordinateSequenceTransformer transformer = (GeometryCoordinateSequenceTransformer) transformers
                             .get(crs);
 
                         if (transformer == null) {
                             transformer = new GeometryCoordinateSequenceTransformer();
 
                             MathTransform2D tx;
 
                             try {
                                 tx = (MathTransform2D) ReferencingFactoryFinder.getCoordinateOperationFactory(hints)
                                                                                .createOperation(crs,
                                         target).getMathTransform();
                             } catch (Exception e) {
                                 String msg = "Could not transform for crs: " + crs;
                                 throw (IOException) new IOException(msg).initCause(e);
                             }
 
                             transformer.setMathTransform(tx);
                             transformers.put(crs, transformer);
                         }
 
                         //do the transformation
                         try {
                             object = transformer.transform(geometry);
                         } catch (TransformException e) {
                             String msg = "Error occured transforming " + geometry.toString();
                             throw (IOException) new IOException(msg).initCause(e);
                         }
                     }
                 }
             }
 
             attributes[i] = object;
         }
 
         try {
             return schema.create(attributes, feature.getID());
         } catch (IllegalAttributeException e) {
             String msg = "Error creating reprojeced feature";
             throw (IOException) new IOException(msg).initCause(e);
         }
     }
 
     class ReprojectingFeatureIterator implements FeatureIterator {
         FeatureIterator delegate;
 
         public ReprojectingFeatureIterator(FeatureIterator delegate) {
             this.delegate = delegate;
         }
 
         public FeatureIterator getDelegate() {
             return delegate;
         }
 
         public boolean hasNext() {
             return delegate.hasNext();
         }
 
         public Feature next() throws NoSuchElementException {
             Feature feature = delegate.next();
 
             try {
                 return reproject(feature);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
 
         public void close() {
             delegate = null;
         }
     }
 
     class ReprojectingIterator implements Iterator {
         Iterator delegate;
 
         public ReprojectingIterator(Iterator delegate) {
             this.delegate = delegate;
         }
 
         public Iterator getDelegate() {
             return delegate;
         }
 
         public void remove() {
             delegate.remove();
         }
 
         public boolean hasNext() {
             return delegate.hasNext();
         }
 
         public Object next() {
             Feature feature = (Feature) delegate.next();
 
             try {
                 return reproject(feature);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 }
