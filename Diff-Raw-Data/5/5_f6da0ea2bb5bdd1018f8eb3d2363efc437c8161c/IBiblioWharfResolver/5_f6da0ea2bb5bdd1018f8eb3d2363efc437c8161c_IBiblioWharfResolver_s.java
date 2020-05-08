 /*
  *
  *  Copyright (C) 2010 JFrog Ltd.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  * /
  */
 
 package org.jfrog.wharf.ivy.resolver;
 
 import org.apache.ivy.core.IvyPatternHelper;
 import org.apache.ivy.core.cache.CacheMetadataOptions;
 import org.apache.ivy.core.cache.RepositoryCacheManager;
 import org.apache.ivy.core.module.descriptor.Artifact;
 import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
 import org.apache.ivy.core.module.id.ModuleRevisionId;
 import org.apache.ivy.core.resolve.ResolveData;
 import org.apache.ivy.core.resolve.ResolvedModuleRevision;
 import org.apache.ivy.plugins.repository.Repository;
 import org.apache.ivy.plugins.repository.Resource;
 import org.apache.ivy.plugins.resolver.IBiblioResolver;
 import org.apache.ivy.plugins.resolver.util.ResolvedResource;
 import org.apache.ivy.util.ContextualSAXHandler;
 import org.apache.ivy.util.Message;
 import org.apache.ivy.util.XMLHelper;
 import org.jfrog.wharf.ivy.cache.ModuleMetadataManager;
 import org.jfrog.wharf.ivy.cache.WharfCacheManager;
 import org.jfrog.wharf.ivy.model.ArtifactMetadata;
 import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;
 import org.jfrog.wharf.ivy.repository.WharfURLRepository;
 import org.jfrog.wharf.ivy.util.WharfUtils;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * @author Tomer Cohen
  */
 public class IBiblioWharfResolver extends IBiblioResolver implements WharfResolver {
 
     protected CacheTimeoutStrategy snapshotTimeout = DAILY;
 
     public IBiblioWharfResolver() {
         WharfUtils.hackIvyBasicResolver(this);
     }
 
     @Override
     public void setRepository(Repository repository) {
         super.setRepository(repository);
     }
 
     public WharfURLRepository getWharfUrlRepository() {
         return (WharfURLRepository) super.getRepository();
     }
 
     @Override
     public Artifact fromSystem(Artifact artifact) {
         return super.fromSystem(artifact);
     }
 
     @Override
     public void setChecksums(String checksums) {
         getWharfUrlRepository().setChecksums(checksums);
     }
 
     public boolean supportsWrongSha1() {
         return getWharfUrlRepository().supportsWrongSha1();
     }
 
     @Override
     public String[] getChecksumAlgorithms() {
         return getWharfUrlRepository().getChecksumAlgorithms();
     }
 
     @Override
     public ResolvedResource getArtifactRef(Artifact artifact, Date date) {
         ResolvedResource artifactRef = super.getArtifactRef(artifact, date);
         return WharfUtils.convertToWharfResource(artifactRef);
     }
 
     @Override
     public ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data) {
         ResolvedResource ivyFileRef = null;
         if (isSnapshot(dd) && hasModuleMetadataManager()) {
             ModuleRevisionId mrid = dd.getDependencyRevisionId();
             String snapshotVersion = findSnapshotVersion(mrid);
             if (snapshotVersion != null) {
                 ModuleRevisionMetadata metadata = getCacheProperties(mrid);
                 if (metadata != null && snapshotTimeout.isCacheTimedOut(getLastResolvedTime(metadata))) {
                     for (ArtifactMetadata artifactMetadata : metadata.getArtifactMetadata()) {
                        if (artifactMetadata.location.contains(snapshotVersion)) {
                             ivyFileRef = WharfUtils.convertToWharfResource(this, artifactMetadata, snapshotVersion);
                             break;
                         }
                     }
                 }
             }
         }
         if (ivyFileRef == null) {
             ivyFileRef = WharfUtils.convertToWharfResource(super.findIvyFileRef(dd, data));
         }
         return ivyFileRef;
     }
 
     /**
      * Returns the timeout strategy for a Maven Snapshot in the cache
      */
     public CacheTimeoutStrategy getSnapshotTimeout() {
         return snapshotTimeout;
     }
 
     /**
      * Sets the time in ms a Maven Snapshot in the cache is not checked for a newer version
      *
      * @param snapshotLifetime The lifetime in ms
      */
     public void setSnapshotTimeout(long snapshotLifetime) {
         this.snapshotTimeout = new Interval(snapshotLifetime);
     }
 
     /**
      * Sets a timeout strategy for a Maven Snapshot in the cache
      *
      * @param cacheTimeoutStrategy The strategy
      */
     public void setSnapshotTimeout(CacheTimeoutStrategy cacheTimeoutStrategy) {
         this.snapshotTimeout = cacheTimeoutStrategy;
     }
 
     @Override
     public long get(Resource resource, File dest) throws IOException {
         return super.get(resource, dest);
     }
 
     @Override
     protected ResolvedModuleRevision findModuleInCache(DependencyDescriptor dd, ResolveData data) {
         boolean isSnapshot = isSnapshot(dd);
         if (isSnapshot && hasModuleMetadataManager()) {
             ModuleRevisionId mrid = dd.getDependencyRevisionId();
             String snapshotVersion = findSnapshotVersion(mrid);
             if (snapshotVersion != null) {
                 ModuleRevisionMetadata metadata = getCacheProperties(mrid);
                 if (metadata != null && snapshotTimeout.isCacheTimedOut(getLastResolvedTime(metadata))) {
                     for (ArtifactMetadata artifactMetadata : metadata.getArtifactMetadata()) {
                         if (artifactMetadata.location.contains(snapshotVersion)) {
                             // Let the find Ivy ref do it's job...
                             // TODO: Means double POM to IVY parsing!!!
                             return null;
                         }
                     }
                 }
             }
         }
         setChangingPattern(null);
         ResolvedModuleRevision moduleRevision = WharfUtils.findModuleInCache(this, dd, data);
         if (moduleRevision == null) {
             setChangingPattern(".*-SNAPSHOT");
             return null;
         }
         if (isSnapshot && snapshotTimeout.isCacheTimedOut(getLastResolvedTime(moduleRevision.getId()))) {
             setChangingPattern(".*-SNAPSHOT");
             return null;
         } else {
             return moduleRevision;
         }
     }
 
     private String findSnapshotVersion(ModuleRevisionId mrid) {
         InputStream metadataStream = null;
         try {
             String metadataLocation = IvyPatternHelper.substitute(
                     getRoot() + "[organisation]/[module]/[revision]/maven-metadata.xml", convertM2IdForResourceSearch(mrid));
             Resource metadata = getRepository().getResource(metadataLocation);
             if (metadata.exists()) {
                 metadataStream = metadata.openStream();
                 final StringBuffer timestamp = new StringBuffer();
                 final StringBuffer buildNumer = new StringBuffer();
                 XMLHelper.parse(metadataStream, null, new ContextualSAXHandler() {
                     public void endElement(String uri, String localName, String qName)
                             throws SAXException {
                         if ("metadata/versioning/snapshot/timestamp".equals(getContext())) {
                             timestamp.append(getText());
                         }
                         if ("metadata/versioning/snapshot/buildNumber"
                                 .equals(getContext())) {
                             buildNumer.append(getText());
                         }
                         super.endElement(uri, localName, qName);
                     }
                 }, null);
                 if (timestamp.length() > 0) {
                     // we have found a timestamp, so this is a snapshot unique version
                     String rev = mrid.getRevision();
                     rev = rev.substring(0, rev.length() - "SNAPSHOT".length());
                     rev = rev + timestamp.toString() + "-" + buildNumer.toString();
 
                     return rev;
                 }
             } else {
                 Message.verbose("\tmaven-metadata not available: " + metadata);
             }
         } catch (IOException e) {
             Message.verbose(
                     "impossible to access maven metadata file, ignored: " + e.getMessage());
         } catch (SAXException e) {
             Message.verbose(
                     "impossible to parse maven metadata file, ignored: " + e.getMessage());
         } catch (ParserConfigurationException e) {
             Message.verbose(
                     "impossible to parse maven metadata file, ignored: " + e.getMessage());
         } finally {
             if (metadataStream != null) {
                 try {
                     metadataStream.close();
                 } catch (IOException e) {
                     // ignored
                 }
             }
         }
         return null;
     }
 
     private boolean isSnapshot(DependencyDescriptor dd) {
         if (dd == null) {
             return false;
         }
         String revision = dd.getAttribute("revision");
         return revision != null && revision.endsWith("-SNAPSHOT");
     }
 
     public ResolvedModuleRevision basicFindModuleInCache(DependencyDescriptor dd, ResolveData data, boolean anyResolver) {
         return super.findModuleInCache(dd, data, anyResolver);
     }
 
     @Override
     public CacheMetadataOptions getCacheOptions(ResolveData data) {
         return super.getCacheOptions(data);
     }
 
     @Override
     public long getAndCheck(Resource resource, File dest) throws IOException {
         return WharfUtils.getAndCheck(this, resource, dest);
     }
 
     private ModuleRevisionMetadata getCacheProperties(ModuleRevisionId mrid) {
         WharfCacheManager cacheManager = (WharfCacheManager) getRepositoryCacheManager();
         return cacheManager.getMetadataHandler().getModuleRevisionMetadata(mrid);
     }
 
     private long getLastResolvedTime(ModuleRevisionMetadata mrm) {
         String lastResolvedProp = mrm.getLatestResolvedTime();
         return lastResolvedProp != null ? Long.parseLong(lastResolvedProp) : 0L;
     }
 
     private long getLastResolvedTime(ModuleRevisionId mrid) {
         RepositoryCacheManager cacheManager = getRepositoryCacheManager();
         if (cacheManager instanceof ModuleMetadataManager) {
            return ((ModuleMetadataManager)cacheManager).getLastResolvedTime(mrid);
         }
         return 0L;
     }
 
     private boolean hasModuleMetadataManager() {
         return getRepositoryCacheManager() instanceof ModuleMetadataManager;
     }
 
     @Override
     public void setRoot(String root) {
         super.setRoot(root);
 
         URI rootUri;
         try {
             rootUri = new URI(root);
         } catch (URISyntaxException e) {
             throw new RuntimeException(e);
         }
         if (rootUri.getScheme().equalsIgnoreCase("file")) {
             setSnapshotTimeout(ALWAYS);
         } else {
             setSnapshotTimeout(DAILY);
         }
     }
 
     public interface CacheTimeoutStrategy {
         boolean isCacheTimedOut(long lastResolvedTime);
     }
 
     public static class Interval implements CacheTimeoutStrategy {
         private long interval;
 
         public Interval(long interval) {
             this.interval = interval;
         }
 
         public boolean isCacheTimedOut(long lastResolvedTime) {
             return System.currentTimeMillis() - lastResolvedTime > interval;
         }
     }
 
     public static final CacheTimeoutStrategy NEVER = new CacheTimeoutStrategy() {
         public boolean isCacheTimedOut(long lastResolvedTime) {
             return false;
         }
     };
 
     public static final CacheTimeoutStrategy ALWAYS = new CacheTimeoutStrategy() {
         public boolean isCacheTimedOut(long lastResolvedTime) {
             return true;
         }
     };
 
     public static final CacheTimeoutStrategy DAILY = new CacheTimeoutStrategy() {
         public boolean isCacheTimedOut(long lastResolvedTime) {
             Calendar calendarCurrent = Calendar.getInstance();
             calendarCurrent.setTime(new Date());
             int dayOfYear = calendarCurrent.get(Calendar.DAY_OF_YEAR);
             int year = calendarCurrent.get(Calendar.YEAR);
 
             Calendar calendarLastResolved = Calendar.getInstance();
             calendarLastResolved.setTime(new Date(lastResolvedTime));
             if (calendarLastResolved.get(Calendar.YEAR) == year &&
                     calendarLastResolved.get(Calendar.DAY_OF_YEAR) == dayOfYear) {
                 return false;
             }
             return true;
         }
     };
 
     @Override
     public int hashCode() {
         int result = getName().hashCode();
         result = 31 * result + getRoot().hashCode();
         result = 31 * result + snapshotTimeout.hashCode();
         result = 31 * result + getPattern().hashCode();
         result = 31 * result + (isUsepoms() ? 1 : 0);
         result = 31 * result + (isAlwaysCheckExactRevision() ? 1 : 0);
         result = 31 * result + (isM2compatible() ? 1 : 0);
         result = 31 * result + getWharfUrlRepository().getChecksums().hashCode();
         return result;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof IBiblioWharfResolver)) return false;
 
         IBiblioWharfResolver that = (IBiblioWharfResolver) o;
 
         if (!getName().equals(that.getName())
                 || !getRoot().equals(that.getRoot())
                 || !snapshotTimeout.equals(that.snapshotTimeout)
                 || !getPattern().equals(that.getPattern())
                 || isUsepoms() != that.isUsepoms()
                 || isUseMavenMetadata() != that.isUseMavenMetadata()
                 || isAlwaysCheckExactRevision() != that.isAlwaysCheckExactRevision()
                 || isM2compatible() != that.isM2compatible()
                 || !getWharfUrlRepository().getChecksums().equals(that.getWharfUrlRepository().getChecksums()))
             return false;
         return true;
     }
 }
