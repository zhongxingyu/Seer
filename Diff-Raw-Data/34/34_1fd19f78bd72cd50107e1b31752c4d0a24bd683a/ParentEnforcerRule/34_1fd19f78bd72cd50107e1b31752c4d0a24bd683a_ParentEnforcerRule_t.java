 /**
 *
 *                                Parent enforcer rule
 *                             =========================
 *
 * Please visit the Remote Process web site for more information:
 *
 *   * http://jeluard.github.com/remote-process
  *
  * Copyright 2012 Julien Eluard
  *
 * Julien Eluard licenses this product to you under the Apache License, version 2.0 (the
 * "License"); you may not use this product except in compliance with the License.
 * You may obtain a copy of the License at:
  *
 *    http://www.apache.org/licenses/LICENSE-2.0
  *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Also, please refer to each LICENSE.<component>.txt file, which is located in
 * the 'licenses' directory of the distribution file, for the license terms of the
 * components that this product depends on.
  */
 package com.github.jeluard.maven;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.apache.maven.enforcer.rule.api.EnforcerRule;
 import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
 import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Parent;
 import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 
 /**
  * {@link EnforcerRule} implementation checking parent artifacts.
  */
 public class ParentEnforcerRule implements EnforcerRule {
 
   private static final String POM_ARTIFACT_TYPE = "pom";
 
   @Override
   public void execute(final EnforcerRuleHelper helper) throws EnforcerRuleException {
     final MavenProject project;
     try {
       project = (MavenProject) helper.evaluate("${project}");
     } catch (ExpressionEvaluationException e) {
       throw new EnforcerRuleException("Failed to access ${project} variable", e);
     }
 
     final String type = project.getArtifact().getType();
     if (!ParentEnforcerRule.POM_ARTIFACT_TYPE.equals(type)) {
       helper.getLog().debug("Skipping non "+ParentEnforcerRule.POM_ARTIFACT_TYPE+" artifact.");
 
       return;
     }
 
     final Parent parent = new Parent();
     parent.setGroupId(project.getGroupId());
     parent.setArtifactId(project.getArtifactId());
     parent.setVersion(project.getVersion());
     try {
       validateSubModules(extractRootFolder(project), project.getModel(), parent);
     } catch (IOException e) {
       throw new EnforcerRuleException("Failed to process one of project's module", e);
     }
   }
 
   protected final File extractRootFolder(final MavenProject project) {
     return new File(project.getFile().getParentFile().getPath());
   }
 
   protected final void validateModel(final File rootFolder, final Model model, final Parent parent) throws IOException {
     if (!isParentValid(model.getParent(), parent)) {
         throw new IllegalArgumentException("Parent for <"+model+"> is <"+model.getParent()+"> but must be <"+parent+">");
     }
 
     validateSubModules(rootFolder, model, parent);
   }
 
   protected final void validateSubModules(final File rootFolder, final Model model, final Parent parent) throws IOException {
     //Validate all modules of pom type modules.
     if (ParentEnforcerRule.POM_ARTIFACT_TYPE.equals(model.getPackaging())) {
       final Parent newParent = new Parent();
       newParent.setGroupId(model.getGroupId());
       newParent.setArtifactId(model.getArtifactId());
       newParent.setVersion(parent.getVersion());//Model version might be inherited from Parent (thus not set at the model level). Rely on Parent#getVersion().
       for (final String module : model.getModules()) {
         final Model moduleModel = loadModel(rootFolder, module+"/pom.xml");
         validateModel(new File(rootFolder, module), moduleModel, newParent);
       }
     }
   }
 
   protected final boolean isParentValid(final Parent modelParent, final Parent parent) {
     return parent.getArtifactId().equals(modelParent.getArtifactId()) && parent.getGroupId().equals(modelParent.getGroupId()) && parent.getVersion().equals(modelParent.getVersion());
   }
 
   protected final Model loadModel(final File rootFolder, final String moduleFileName) throws IOException {
     FileReader fileReader = null;
     BufferedReader bufferedReader = null;
     try
     {
       fileReader = new FileReader(new File(rootFolder, moduleFileName));
       bufferedReader = new BufferedReader(fileReader);
       final MavenXpp3Reader reader = new MavenXpp3Reader();
       return reader.read(bufferedReader);
     } catch (XmlPullParserException e) {
         final IOException ioe = new IOException(e);
         throw ioe;
     } finally {
       if (bufferedReader != null) {
         bufferedReader.close();
       }
       if (fileReader != null) {
         fileReader.close();
       }
     }
   }
 
   @Override
   public boolean isCacheable() {
     return false;
   }
 
   @Override
   public boolean isResultValid(final EnforcerRule cachedRule) {
     return false;
   }
 
   @Override
   public String getCacheId() {
     return "0";
   }
 
 }
