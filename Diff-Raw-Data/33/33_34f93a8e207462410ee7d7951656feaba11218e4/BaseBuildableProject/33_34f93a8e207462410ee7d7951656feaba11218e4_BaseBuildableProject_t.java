 /*******************************************************************************
  *
  * Copyright (c) 2011 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *    Anton Kozak
  *
  *******************************************************************************/
 package hudson.model;
 
 import hudson.Functions;
 import hudson.model.Descriptor.FormException;
 import hudson.tasks.BuildStep;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildWrapper;
 import hudson.tasks.BuildWrappers;
 import hudson.tasks.Builder;
 import hudson.tasks.Publisher;
 import hudson.triggers.Trigger;
 import hudson.util.CascadingUtil;
 import hudson.util.DescribableList;
 import hudson.util.DescribableListUtil;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.ServletException;
 import net.sf.json.JSONObject;
 import org.eclipse.hudson.api.model.IBaseBuildableProject;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 /**
  * Base buildable project.
  *
  * @author Anton Kozak.
  */
 public abstract class BaseBuildableProject<P extends BaseBuildableProject<P, B>, B extends AbstractBuild<P, B>>
         extends AbstractProject<P, B>
         implements Saveable, BuildableItemWithBuildWrappers, IBaseBuildableProject {
 
     public static final String BUILDERS_PROPERTY_NAME = "builders";
     public static final String BUILDWRAPPERS_PROPERTY_NAME = "buildwrappers";
     public static final String PUBLISHERS_PROPERTY_NAME = "publishers";
     
     /**
      * List of active {@link Builder}s configured for this project.
      *
      * @deprecated as of 2.2.0 don't use this field directly, logic was moved to
      * {@link org.eclipse.hudson.api.model.IProjectProperty}. Use getter/setter
      * for accessing to this field.
      */
     @Deprecated
     private DescribableList<Builder, Descriptor<Builder>> builders =
             new DescribableList<Builder, Descriptor<Builder>>(this);
     /**
      * List of active {@link Publisher}s configured for this project.
      *
      * @deprecated as of 2.2.0 don't use this field directly, logic was moved to
      * {@link org.eclipse.hudson.api.model.IProjectProperty}. Use getter/setter
      * for accessing to this field.
      */
     @Deprecated
     private DescribableList<Publisher, Descriptor<Publisher>> publishers =
             new DescribableList<Publisher, Descriptor<Publisher>>(this);
     /**
      * List of active {@link BuildWrapper}s configured for this project.
      *
      * @deprecated as of 2.2.0 don't use this field directly, logic was moved to
      * {@link org.eclipse.hudson.api.model.IProjectProperty}. Use getter/setter
      * for accessing to this field.
      */
     @Deprecated
     private DescribableList<BuildWrapper, Descriptor<BuildWrapper>> buildWrappers =
             new DescribableList<BuildWrapper, Descriptor<BuildWrapper>>(this);
 
     /**
      * Creates a new project.
      *
      * @param parent parent {@link ItemGroup}.
      * @param name the name of the project.
      */
     public BaseBuildableProject(ItemGroup parent, String name) {
         super(parent, name);
     }
 
     @Override
     public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
         super.onLoad(parent, name);
 
         getBuildersList().setOwner(this);
         getPublishersList().setOwner(this);
         getBuildWrappersList().setOwner(this);
     }
 
     @Override
     protected void buildProjectProperties() throws IOException {
         super.buildProjectProperties();
         convertBuildersProjectProperty();
         convertBuildWrappersProperties();
         convertPublishersProperties();
     }
 
     protected void buildDependencyGraph(DependencyGraph graph) {
         getPublishersList().buildDependencyGraph(this, graph);
         getBuildersList().buildDependencyGraph(this, graph);
         getBuildWrappersList().buildDependencyGraph(this, graph);
     }
 
     @Override
     protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
         super.submit(req, rsp);
         JSONObject json = req.getSubmittedForm();
         buildBuildWrappers(req, json, BuildWrappers.getFor(this));
         setBuilders(DescribableListUtil.buildFromHetero(this, req, json, "builder", Builder.all()));
         buildPublishers(req, json, BuildStepDescriptor.filter(Publisher.all(), this.getClass()));
     }
 
     @Override
     @SuppressWarnings("unchecked")
     protected List<Action> createTransientActions() {
         List<Action> r = super.createTransientActions();
 
         for (BuildStep step : getBuildersList()) {
             r.addAll(step.getProjectActions(this));
         }
         for (BuildStep step : getPublishersList()) {
             r.addAll(step.getProjectActions(this));
         }
         for (BuildWrapper step : getBuildWrappersList()) {
             r.addAll(step.getProjectActions(this));
         }
         for (Trigger trigger : getTriggersList()) {
             r.addAll(trigger.getProjectActions(this));
         }
         return r;
     }
     
     /**
      * Adds a new {@link BuildStep} builder to this {@link Project} and saves the
      * configuration.
      *
      * @param builder builder.
      * @throws java.io.IOException exception.
      */
     @SuppressWarnings("unchecked")
     public void addBuilder(Builder builder) throws IOException {
         CascadingUtil.getExternalProjectProperty(this,
                 builder.getDescriptor().getJsonSafeClassName()).setValue(builder);
         save();
     }
     
      /**
      * Removes a {@link BuildStep} builder from this project, if it's active.
      *
      * @param builder builder.
      * @throws java.io.IOException exception.
      */
     @Override
     public void removeBuilder(Descriptor<Builder> builder) throws IOException {
         removeProjectProperty(builder.getJsonSafeClassName());
         save();
     }
     
     @Override
     public Builder getBuilder(Descriptor<Builder> descriptor) {
         return (Builder) CascadingUtil.getExternalProjectProperty(this, descriptor.getJsonSafeClassName()).getValue();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public List<Builder> getBuilders() {
         return getBuildersList().toList();
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setBuilders(DescribableList<Builder, Descriptor<Builder>> builders) {
         CascadingUtil.getDescribableListProjectProperty(this, BUILDERS_PROPERTY_NAME).setValue(builders);
     }
     
     /**
      * @inheritDoc
      * 
      * @deprecated as of 2.2.0 do not use this field directly. Use other methods such as getBuilders, addBuilder & removeBuilder
      */
     @SuppressWarnings("unchecked")
     @Deprecated
     @Override
     public DescribableList<Builder, Descriptor<Builder>> getBuildersList() {
         return CascadingUtil.getDescribableListProjectProperty(this, BUILDERS_PROPERTY_NAME).getValue();
     }
     
     /**
      * Adds a new {@link BuildStep} to this {@link Project} and saves the
      * configuration.
      *
      * @param publisher publisher.
      * @throws java.io.IOException exception.
      */
     @SuppressWarnings("unchecked")
     @Override
     public void addPublisher(Publisher publisher) throws IOException {
         CascadingUtil.getExternalProjectProperty(this,
                 publisher.getDescriptor().getJsonSafeClassName()).setValue(publisher);
         save();
     }
 
     /**
      * Removes a publisher from this project, if it's active.
      *
      * @param publisher publisher.
      * @throws java.io.IOException exception.
      */
     @Override
     public void removePublisher(Descriptor<Publisher> publisher) throws IOException {
         removeProjectProperty(publisher.getJsonSafeClassName());
         save();
     }
 
     @Override
     public Publisher getPublisher(Descriptor<Publisher> descriptor) {
         return (Publisher) CascadingUtil.getExternalProjectProperty(this, descriptor.getJsonSafeClassName()).getValue();
     }
     
      /**
      * @inheritDoc
      */
     @Override
     public Map<Descriptor<Publisher>, Publisher> getPublishers() {
         return getPublishersList().toMap();
     }
     
     /**
      * @inheritDoc
      * 
      */
     @Override
     public void setPublishers(DescribableList<Publisher, Descriptor<Publisher>> publishers) {
         CascadingUtil.getDescribableListProjectProperty(this, PUBLISHERS_PROPERTY_NAME).setValue(publishers);
     }
 
     /**
      * Returns the list of the publishers available in the hudson.
      *
      * @return the list of the publishers available in the hudson.
      * * @deprecated as of 2.2.0 do not use this field directly. Use other methods such as getPublishers, addPublisher & removePublisher
      */
     @Deprecated
     @Override
     public DescribableList<Publisher, Descriptor<Publisher>> getPublishersList() {
         return DescribableListUtil.convertToDescribableList(Functions.getPublisherDescriptors(this), this);
     }
 
     /**
      * Adds a new {@link BuildStep} to this {@link Project} and saves the
      * configuration.
      *
      * @param buildWrapper buildWrapper.
      * @throws java.io.IOException exception.
      */
     @SuppressWarnings("unchecked")
     @Override
     public void addBuildWrapper(BuildWrapper buildWrapper) throws IOException {
         CascadingUtil.getExternalProjectProperty(this,
                 buildWrapper.getDescriptor().getJsonSafeClassName()).setValue(buildWrapper);
         save();
     }
     
      /**
      * Removes a buildWrapper from this project, if it's active.
      *
      * @param buildWrapper buildWrapper.
      * @throws java.io.IOException exception.
      */
      @Override
     public void removeBuildWrapper(Descriptor<BuildWrapper> buildWrapper) throws IOException {
         removeProjectProperty(buildWrapper.getJsonSafeClassName());
         save();
     }
     
     @Override
     public BuildWrapper getBuildWrapper(Descriptor<BuildWrapper> descriptor) {
         return (BuildWrapper) CascadingUtil.getExternalProjectProperty(this, descriptor.getJsonSafeClassName()).getValue();
     }
 
     /**
      * @inheritDoc
      */
      @Override
     public Map<Descriptor<BuildWrapper>, BuildWrapper> getBuildWrappers() {
         return getBuildWrappersList().toMap();
     }
     
      /**
      * @inheritDoc
      * 
      */
     @Override
     public void setBuildWrappers(DescribableList<BuildWrapper, Descriptor<BuildWrapper>> buildWrappers) {
         CascadingUtil.getDescribableListProjectProperty(this, BUILDWRAPPERS_PROPERTY_NAME).setValue(buildWrappers);
     }
     /**
      * @inheritDoc
      * 
      * @deprecated as of 2.2.0 don't use this field directly. Use other methods such as getBuildWrappers, addBuildWrapper & removeBuildWrapper
      */
     @Override
     @Deprecated
     public DescribableList<BuildWrapper, Descriptor<BuildWrapper>> getBuildWrappersList() {
         return DescribableListUtil.convertToDescribableList(Functions.getBuildWrapperDescriptors(this), this);
     }
 
     /**
      * Builds publishers.
      *
      * @param req {@link StaplerRequest}
      * @param json {@link JSONObject}
      * @param descriptors list of descriptors.
      * @throws hudson.model.Descriptor.FormException if any.
      */
     protected void buildPublishers(StaplerRequest req, JSONObject json, List<Descriptor<Publisher>> descriptors)
             throws FormException {
         CascadingUtil.buildExternalProperties(req, json, descriptors, this);
     }
 
     /**
      * Builds BuildWrappers.
      *
      * @param req {@link StaplerRequest}
      * @param json {@link JSONObject}
      * @param descriptors list of descriptors.
      * @throws hudson.model.Descriptor.FormException if any.
      */
     protected void buildBuildWrappers(StaplerRequest req, JSONObject json, List<Descriptor<BuildWrapper>> descriptors)
             throws FormException {
         CascadingUtil.buildExternalProperties(req, json, descriptors, this);
     }
     
     protected void buildBuilders(StaplerRequest req, JSONObject json, List<Descriptor<Builder>> descriptors)
             throws FormException {
         CascadingUtil.buildExternalProperties(req, json, descriptors, this);
     }
 
     protected void convertPublishersProperties() {
        if ((null != publishers) && !publishers.isEmpty()) {
             putAllProjectProperties(DescribableListUtil.convertToProjectProperties(publishers, this), false);
         }
        publishers = null;
     }
 
     protected void convertBuildWrappersProperties() {
        if ((null != buildWrappers) && !buildWrappers.isEmpty() ) {
             putAllProjectProperties(DescribableListUtil.convertToProjectProperties(buildWrappers, this), false);
         }
        buildWrappers = null;
     }
 
     protected void convertBuildersProperties() {
        if ((null != builders) && !builders.isEmpty() && (null == getProperty(BUILDERS_PROPERTY_NAME))) {
             putAllProjectProperties(DescribableListUtil.convertToProjectProperties(builders, this), false);
         }
        builders = null;
     }
     
     protected void convertBuildersProjectProperty() {
        if ((null != builders) && !builders.isEmpty() && (null == getProperty(BUILDERS_PROPERTY_NAME))) {
             setBuilders(builders);
         }
        builders = null;
     }
     
 }
