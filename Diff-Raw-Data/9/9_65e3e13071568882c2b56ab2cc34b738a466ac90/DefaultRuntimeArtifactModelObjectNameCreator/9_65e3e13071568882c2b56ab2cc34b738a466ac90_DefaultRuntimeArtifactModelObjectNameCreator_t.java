 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.model.management.internal;
 
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 
 import org.eclipse.equinox.region.Region;
 import org.eclipse.virgo.kernel.model.Artifact;
 import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
 import org.eclipse.virgo.nano.serviceability.NonNull;
 import org.osgi.framework.Version;
 
 /**
  * The default implementation of {@link RuntimeArtifactModelObjectNameCreator}. This implementation creates names based
  * on the following pattern:
  * <p />
  * <code>&lt;domain&gt;:type=ArtifactModel,artifact-type=&lt;type&gt;,name=&lt;name&gt;,version=&lt;version&gt;,region=&lt;region&gt;</code>
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Threadsafe
  * 
  */
 public final class DefaultRuntimeArtifactModelObjectNameCreator implements RuntimeArtifactModelObjectNameCreator {
 
     private static final String ALL_ARTIFACTS_FORMAT = "%s:type=ArtifactModel,*";
 
     private static final String ARTIFACTS_OF_TYPE_FORMAT = "%s:type=ArtifactModel,artifact-type=%s,*";
 
     private static final String ARTIFACTS_OF_TYPE_AND_NAME_FORMAT = "%s:type=ArtifactModel,artifact-type=%s,name=%s,*";
     
     private static final String ARTIFACT_FORMAT = "%s:type=ArtifactModel,artifact-type=%s,name=%s,version=%s,region=%s";
 
     private static final String KEY_NAME = "name";
 
     private static final String KEY_VERSION = "version";
 
     private static final String KEY_REGION = "region";
    
    private static final String NULL_REGION_NAME = "global";
 
     private final String domain;
 
     public DefaultRuntimeArtifactModelObjectNameCreator(String domain) {
         this.domain = domain;
     }
 
     /**
      * {@inheritDoc}
      */
     public ObjectName createArtifactModel(@NonNull Artifact artifact) {
         return createArtifactModel(artifact.getType(), artifact.getName(), artifact.getVersion(), artifact.getRegion());
     }
 
     /**
      * {@inheritDoc}
      */
     public ObjectName createArtifactModel(String type, String name, Version version, Region region) {
        return createObjectName(String.format(ARTIFACT_FORMAT, this.domain, this.quoteValueIfNeeded(type), this.quoteValueIfNeeded(name), this.quoteValueIfNeeded(version.toString()), this.quoteValueIfNeeded(getRegionName(region))));
    }

    private String getRegionName(Region region) {
        return region == null ? NULL_REGION_NAME : region.getName();
     }
 
     /**
      * {@inheritDoc}
      */
     public ObjectName createArtifactsOfTypeQuery(String type) {
         return createObjectName(String.format(ARTIFACTS_OF_TYPE_FORMAT, this.domain, this.quoteValueIfNeeded(type)));
     }
 
     /**
      * {@inheritDoc}
      */
     public ObjectName createArtifactVersionsQuery(String type, String name) {
         return createObjectName(String.format(ARTIFACTS_OF_TYPE_AND_NAME_FORMAT, this.domain, this.quoteValueIfNeeded(type), this.quoteValueIfNeeded(name)));
     }
 
     /**
      * {@inheritDoc}
      */
     public ObjectName createAllArtifactsQuery() {
         return createObjectName(String.format(ALL_ARTIFACTS_FORMAT, this.domain));
     }
 
     /**
      * {@inheritDoc}
      */
 	public String getRegion(ObjectName objectName) {
 		return objectName.getKeyProperty(KEY_REGION);
 	}
 
     /**
      * {@inheritDoc}
      */
     public String getName(ObjectName objectName) {
         return objectName.getKeyProperty(KEY_NAME);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getVersion(ObjectName objectName) {
         return objectName.getKeyProperty(KEY_VERSION);
     }
     
     private String quoteValueIfNeeded(String value){
     	if(value.contains(":") || value.contains(",") || value.contains("=") || value.contains("\"")){
     		value = String.format("%s%s%s", "\"", value.replace("\"", "\\\""), "\"");
     	}
     	return value;
     }
 
     private ObjectName createObjectName(String objectName) {
         try {
             return new ObjectName(objectName);
         } catch (MalformedObjectNameException e) {
             throw new RuntimeException(String.format("Unable to create object name '%s'", objectName), e);
         } catch (NullPointerException e) {
             throw new RuntimeException(String.format("Unable to create object name '%s'", objectName), e);
         }
     }
 
 }
