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
 
 package org.eclipse.virgo.kernel.shell.internal.formatting;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.List;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Version;
 
 import org.eclipse.virgo.kernel.model.BundleArtifact;
 import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
 import org.eclipse.virgo.kernel.module.ModuleContext;
 import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
 import org.eclipse.virgo.kernel.shell.state.QuasiLiveBundle;
 import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
 import org.eclipse.virgo.kernel.shell.state.StateService;
 import org.eclipse.virgo.util.common.StringUtils;
 
 public final class BundleInstallArtifactCommandFormatter implements InstallArtifactCommandFormatter<ManageableArtifact> {
 
     private static final int MAX_LINE_LENGTH = 80;
 
     private static final String ID_COLUMN_NAME = "Id";
 
     private static final int ID_COLUMN_MIN_WIDTH = ID_COLUMN_NAME.length();
 
     private static final String SPRING_POWERED_COLUMN_NAME = " ";
 
     private static final int SPRING_POWERED_COLUMN_WIDTH = SPRING_POWERED_COLUMN_NAME.length();
 
     private static final String NAME_COLUMN_NAME = "Name";
 
     private static final int NAME_COLUMN_MIN_WIDTH = NAME_COLUMN_NAME.length();
 
     private static final String VERSION_COLUMN_NAME = "Version";
 
     private static final int VERSION_COLUMN_MIN_WIDTH = VERSION_COLUMN_NAME.length();
 
     private static final String STATE_COLUMN_NAME = "State";
 
     /**
      * longest state name from {@link BundleArtifact#mapBundleState()}
      */
     private static final int STATE_COLUMN_MIN_WIDTH = 11;
 
     private final StateService stateService;
 
     private final ModuleContextAccessor moduleContextAccessor;
 
    public BundleInstallArtifactCommandFormatter(StateService stateService, ModuleContextAccessor moduleContextAccessor) {
         this.stateService = stateService;
         this.moduleContextAccessor = moduleContextAccessor;
     }
 
     public List<String> formatList(List<ManageableArtifact> artifacts) {
         List<ArtifactHolder> artifactHolders = getArtifactHolders(artifacts);
         Collections.sort(artifactHolders);
 
         int maxIdLength = ID_COLUMN_MIN_WIDTH;
         int maxNameLength = NAME_COLUMN_MIN_WIDTH;
         int maxVersionLength = VERSION_COLUMN_MIN_WIDTH;
         for (ArtifactHolder artifact : artifactHolders) {
             final int idLength = artifact.getId().toString().length();
             maxIdLength = idLength > maxIdLength ? idLength : maxIdLength;
             final int nameLength = artifact.getName().length();
             maxNameLength = nameLength > maxNameLength ? nameLength : maxNameLength;
             final int versionLength = artifact.getVersion().length();
             maxVersionLength = versionLength > maxVersionLength ? versionLength : maxVersionLength;
         }
 
         List<String> lines = new ArrayList<String>();
         int stateLength = MAX_LINE_LENGTH - (4 + maxIdLength + SPRING_POWERED_COLUMN_WIDTH + maxNameLength + maxVersionLength);
         if (stateLength < STATE_COLUMN_MIN_WIDTH) {
             stateLength = STATE_COLUMN_MIN_WIDTH;
         }
 
         final String singleLineFormat = String.format("%%-%ds %%s %%-%ds %%-%ds %%%ds", maxIdLength, maxNameLength, maxVersionLength, stateLength);
         lines.add(String.format(singleLineFormat, ID_COLUMN_NAME, SPRING_POWERED_COLUMN_NAME, NAME_COLUMN_NAME, VERSION_COLUMN_NAME,
             STATE_COLUMN_NAME));
 
         for (ArtifactHolder artifact : artifactHolders) {
             lines.add(String.format(singleLineFormat, artifact.getId(), artifact.getSpringPowered() ? "S" : " ", artifact.getName(),
                 artifact.getVersion(), artifact.getState()));
         }
 
         return lines;
     }
 
     public List<String> formatExamine(ManageableArtifact artifact) {
         List<String> lines = new ArrayList<String>();
 
         List<QuasiBundle> quasiBundles = this.stateService.getAllBundles(null);
         ArtifactHolder artifactHolder = getArtifactHolder(artifact, quasiBundles);
 
         if (artifactHolder == null) {
             lines.add("No artifact found of type '" + artifact.getType() + "' name '" + artifact.getName() + "' and version '"
                 + artifact.getVersion() + "'");
         } else {
             lines.add(String.format("Id:              %s", artifactHolder.getId()));
             lines.add(String.format("Name:            %s", artifactHolder.getName()));
             lines.add(String.format("Version          %s", artifactHolder.getVersion()));
             lines.add(String.format("State:           %s", artifactHolder.getState()));
             lines.add(String.format("Spring Powered:  %s", artifactHolder.getSpringPowered()));
 
             String bundleLocation = artifactHolder.getBundleLocation();
             lines.add(String.format("Bundle Location: %s", bundleLocation == null ? "" : bundleLocation));
 
             lines.addAll(formatImportedPackages(artifactHolder));
             lines.addAll(formatExportedPackages(artifactHolder));
             lines.addAll(formatPublishedServices(artifactHolder));
             lines.addAll(formatConsumedServices(artifactHolder));
             lines.addAll(formatFragments(artifactHolder));
             lines.addAll(formatHost(artifactHolder));
         }
         return lines;
     }
 
     private List<String> formatConsumedServices(ArtifactHolder artifactHolder) {
         List<String> lines = new ArrayList<String>();
 
         lines.add("");
         lines.add(String.format("Consumed services:"));
         List<QuasiLiveService> consumedServices = artifactHolder.getConsumedServices();
         if (consumedServices.isEmpty()) {
             lines.add(String.format("    None"));
         } else {
             for (QuasiLiveService consumedService : consumedServices) {
                 String objectClass = extractFirstObjectClass(consumedService.getProperties().get(Constants.OBJECTCLASS));
                 lines.add(String.format("    %3s %s", consumedService.getServiceId(), objectClass));
                 lines.add(String.format("        published by %s", formatBundleSummary(consumedService.getProvider())));
             }
         }
         return lines;
     }
 
     private List<String> formatPublishedServices(ArtifactHolder artifactHolder) {
         List<String> lines = new ArrayList<String>();
 
         lines.add("");
         lines.add(String.format("Published services:"));
         List<QuasiLiveService> publishedServices = artifactHolder.getPublishedServices();
         if (publishedServices.isEmpty()) {
             lines.add(String.format("    None"));
         } else {
             for (QuasiLiveService publishedService : publishedServices) {
                 String objectClass = extractFirstObjectClass(publishedService.getProperties().get(Constants.OBJECTCLASS));
                 lines.add(String.format("    %3s %s", publishedService.getServiceId(), objectClass));
                 List<QuasiLiveBundle> consumers = publishedService.getConsumers();
                 for (QuasiLiveBundle consumer : consumers) {
                     lines.add(String.format("        consumed by %s", formatBundleSummary(consumer)));
                 }
             }
         }
         return lines;
     }
 
     private String extractFirstObjectClass(Object objectClass) {
         String[] objectClasses;
 
         if (objectClass instanceof String) {
             objectClasses = StringUtils.commaDelimitedListToStringArray((String) objectClass);
         } else if (objectClass instanceof Object[]) {
             objectClasses = (String[]) objectClass;
         } else {
             objectClasses = StringUtils.commaDelimitedListToStringArray(objectClass.toString());
         }
 
         if (objectClasses.length > 0) {
             return objectClasses[0];
         } else {
             return "<no object classes>";
         }
     }
 
     private List<String> formatHost(ArtifactHolder artifactHolder) {
         List<String> lines = new ArrayList<String>();
 
         List<QuasiBundle> hosts = artifactHolder.getHosts();
         if (hosts != null && !hosts.isEmpty()) {
             lines.add("");
             lines.add(String.format("Host:"));
             QuasiBundle host = hosts.get(0);
             lines.add(String.format("    %s", formatBundleSummary(host)));
         }
 
         return lines;
     }
 
     private List<String> formatFragments(ArtifactHolder artifactHolder) {
         List<String> lines = new ArrayList<String>();
 
         lines.add("");
         lines.add(String.format("Fragments:"));
         List<QuasiBundle> fragments = artifactHolder.getFragments();
         if (fragments.isEmpty()) {
             lines.add(String.format("    None"));
         } else {
             for (QuasiBundle fragment : fragments) {
                 lines.add(String.format("    %s", formatBundleSummary(fragment)));
             }
         }
 
         return lines;
     }
 
     private List<String> formatExportedPackages(ArtifactHolder artifactHolder) {
         List<String> lines = new ArrayList<String>();
 
         lines.add("");
         lines.add(String.format("Exported Packages:"));
         List<QuasiExportPackage> exportPackages = artifactHolder.getExportPackages();
         if (exportPackages.isEmpty()) {
             lines.add(String.format("    None"));
         } else {
             for (QuasiExportPackage exportPackage : exportPackages) {
                 lines.add(String.format("    %s %s", exportPackage.getPackageName(), exportPackage.getVersion().toString()));
                 for (QuasiImportPackage consumer : exportPackage.getConsumers()) {
                     QuasiBundle bundle = consumer.getImportingBundle();
                     lines.add(String.format("        imported by %s", formatBundleSummary(bundle)));
                 }
             }
         }
 
         return lines;
     }
 
     private List<String> formatImportedPackages(ArtifactHolder artifactHolder) {
         List<String> lines = new ArrayList<String>();
 
         lines.add("");
         lines.add(String.format("Imported Packages:"));
         List<QuasiImportPackage> importPackages = artifactHolder.getImportPackages();
         if (importPackages.isEmpty()) {
             lines.add(String.format("    None"));
         } else {
             for (QuasiImportPackage importPackage : importPackages) {
                 if (importPackage.isResolved()) {
                     lines.add(String.format("    %s %s", importPackage.getPackageName(), importPackage.getVersionConstraint().toString()));
                     QuasiBundle bundle = importPackage.getProvider().getExportingBundle();
                     lines.add(String.format("        exported by %s", formatBundleSummary(bundle)));
                 }
             }
         }
 
         return lines;
     }
 
     private String formatBundleSummary(QuasiBundle bundle) {
         return String.format("%s %s [%s]", bundle.getSymbolicName(), bundle.getVersion(), bundle.getBundleId());
     }
 
     public List<String> formatDiag(QuasiBundle bundle, List<QuasiResolutionFailure> resolverReport) {
         if (bundle == null) {
             return Arrays.asList("Unable to locate bundle");
         }
 
         if (resolverReport.size() == 0) {
             return Arrays.asList("No resolution errors found");
         }
 
         List<String> lines = new ArrayList<String>();
         for (QuasiResolutionFailure quasiResolutionFailure : resolverReport) {
             lines.add(String.format("%s", quasiResolutionFailure.getDescription()));
         }
         return lines;
     }
 
     public List<String> formatHeaders(QuasiBundle bundle) {
         if (bundle == null) {
             return Arrays.asList("Unable to locate bundle");
         }
 
         List<String> lines = new ArrayList<String>();
 
         Dictionary<String, String> headers = bundle.getBundle().getHeaders();
         Enumeration<String> keys = headers.keys();
         while (keys.hasMoreElements()) {
             String key = keys.nextElement();
             lines.add(String.format("%s: ", key));
             lines.addAll(formatHeaderValue(headers.get(key)));
         }
 
         return lines;
     }
 
     private List<ArtifactHolder> getArtifactHolders(List<ManageableArtifact> artifacts) {
         List<ArtifactHolder> artifactHolders = new ArrayList<ArtifactHolder>(artifacts.size());
 
         List<QuasiBundle> bundles = this.stateService.getAllBundles(null);

         for (ManageableArtifact artifact : artifacts) {
             ArtifactHolder artifactHolder = getArtifactHolder(artifact, bundles);
             if (artifactHolder != null) {
                 artifactHolders.add(artifactHolder);
             }
         }
 
         return artifactHolders;
     }
 
     private ArtifactHolder getArtifactHolder(ManageableArtifact artifact, List<QuasiBundle> bundles) {
         Version v = new Version(artifact.getVersion());
         for (QuasiBundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(artifact.getName()) && bundle.getVersion().equals(v)) {
                 return new ArtifactHolder(artifact, bundle, moduleContextAccessor);
             }
         }
         return null;
     }
 
     private List<String> formatHeaderValue(String value) {
         List<String> lines = new ArrayList<String>();
 
         Reader reader = new StringReader(value);
         char[] buffer = new char[71];
         int length = 0;
         try {
             while ((length = reader.read(buffer)) != -1) {
                 lines.add(String.format(" %s", new String(buffer, 0, length)));
             }
         } catch (IOException e) {
             // Do nothing
         }
         return lines;
     }
 
     private static class ArtifactHolder implements Comparable<ArtifactHolder> {
 
         private final ModuleContextAccessor moduleContextAccessor;
 
         private final ManageableArtifact artifact;
 
         private final QuasiBundle bundle;
 
         public ArtifactHolder(ManageableArtifact artifact, QuasiBundle bundle, ModuleContextAccessor moduleContextAccessor) {
             this.artifact = artifact;
             this.bundle = bundle;
             this.moduleContextAccessor = moduleContextAccessor;
         }
 
         public Long getId() {
             return this.bundle.getBundleId();
         }
 
         public String getName() {
             return artifact.getName();
         }
 
         public String getState() {
             return artifact.getState();
         }
 
         public String getVersion() {
             return artifact.getVersion();
         }
 
         public String getBundleLocation() {
             return this.bundle.getBundle().getLocation();
         }
 
         public boolean getSpringPowered() {
             Bundle realBundle = bundle.getBundle();
             if (realBundle != null) {
                 ModuleContext moduleContext = moduleContextAccessor.getModuleContext(realBundle);
                 if (moduleContext != null) {
                     return true;
                 }
             }
             return false;
         }
 
         public List<QuasiExportPackage> getExportPackages() {
             return bundle.getExportPackages();
         }
 
         public List<QuasiImportPackage> getImportPackages() {
             return bundle.getImportPackages();
         }
 
         public List<QuasiBundle> getFragments() {
             return bundle.getFragments();
         }
 
         public List<QuasiBundle> getHosts() {
             return bundle.getHosts();
         }
 
         public List<QuasiLiveService> getPublishedServices() {
             QuasiLiveBundle liveBundle = (QuasiLiveBundle) bundle;
             return liveBundle.getExportedServices();
         }
 
         public List<QuasiLiveService> getConsumedServices() {
             QuasiLiveBundle liveBundle = (QuasiLiveBundle) bundle;
             return liveBundle.getImportedServices();
         }
 
         public int compareTo(ArtifactHolder o) {
             return getId().compareTo(o.getId());
         }
 
         @Override
         public int hashCode() {
             return getId().hashCode();
         }
 
         @Override
         public boolean equals(Object obj) {
             return getId().equals(((ArtifactHolder) obj).getId());
         }
     }
 }
