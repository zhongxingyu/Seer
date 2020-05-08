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
 
 package org.eclipse.virgo.kernel.deployer.core.internal;
 
 import java.util.List;
 
 import org.osgi.framework.Version;
 
 
 import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeInclosure;
 import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.internal.AbstractInstallArtifact;
 import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
 import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
 import org.eclipse.virgo.util.common.Tree;
 import org.eclipse.virgo.util.common.Tree.ExceptionThrowingTreeVisitor;
 
 /**
  * {@link PlanResolver} adds the immediate child nodes to a plan node.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This class is thread safe.
  * 
  */
 public class PlanResolver implements Transformer {
 
     private static final String SCOPE_SEPARATOR = "-";
 
     private final InstallArtifactTreeInclosure installArtifactTreeInclosure;
 
     public PlanResolver(InstallArtifactTreeInclosure installArtifactTreeInclosure) {
         this.installArtifactTreeInclosure = installArtifactTreeInclosure;
     }
 
     /**
      * {@InheritDoc}
      */
     public void transform(Tree<InstallArtifact> installTree, final InstallEnvironment installEnvironment) throws DeploymentException {
         installTree.visit(new ExceptionThrowingTreeVisitor<InstallArtifact, DeploymentException>() {
 
             public boolean visit(Tree<InstallArtifact> tree) throws DeploymentException {
                 PlanResolver.this.operate(tree.getValue());
                 return true;
             }
         });
     }
 
     private void operate(InstallArtifact installArtifact) throws DeploymentException {
         if (installArtifact instanceof PlanInstallArtifact) {
             PlanInstallArtifact planInstallArtifact = (PlanInstallArtifact) installArtifact;
             if (planInstallArtifact.getTree().getChildren().isEmpty()) {
                 String scopeName = getArtifactScopeName(planInstallArtifact);
                 Tree<InstallArtifact> tree = planInstallArtifact.getTree();
                 List<ArtifactSpecification> artifactSpecifications = planInstallArtifact.getArtifactSpecifications();
                 for (ArtifactSpecification artifactSpecification : artifactSpecifications) {
                     Tree<InstallArtifact> childInstallArtifactTree = createInstallArtifactTree(artifactSpecification, scopeName);     
                     TreeUtils.addChild(tree, childInstallArtifactTree);
                     
                     // Put child into the INSTALLING state as Transformers (like this) are after the "begin install" pipeline stage.
                     InstallArtifact childInstallArtifact = childInstallArtifactTree.getValue();
                     ((AbstractInstallArtifact) childInstallArtifact).beginInstall();
                 }
             }
         }
     }        
 
     /**
      * Returns the scope name of the given {@link InstallArtifact} or <code>null</code> if the given InstallArtifact
      * does not belong to a scope.
      * 
      * @param installArtifact the <code>InstallArtiface</code> whose scope name is required
      * @return the scope name or <code>null</code> if the given InstallArtifact does not belong to a scope
      */
     private String getArtifactScopeName(InstallArtifact installArtifact) {
         if (installArtifact instanceof PlanInstallArtifact) {
             PlanInstallArtifact planInstallArtifact = (PlanInstallArtifact) installArtifact;
             boolean scoped = planInstallArtifact.isScoped();
             if (scoped) {
                 return planInstallArtifact.getName() + SCOPE_SEPARATOR + versionToShortString(planInstallArtifact.getVersion());
             }
         }
        return installArtifact.getScopeName();
     }
 
     private static String versionToShortString(Version version) {
         String result = version.toString();
         while (result.endsWith(".0")) {
             result = result.substring(0, result.length() - 2);
         }
         return result;
     }
 
     private Tree<InstallArtifact> createInstallArtifactTree(ArtifactSpecification artifactSpecification, String scopeName) throws DeploymentException {
         return this.installArtifactTreeInclosure.createInstallTree(artifactSpecification, scopeName);
     }        
 }
