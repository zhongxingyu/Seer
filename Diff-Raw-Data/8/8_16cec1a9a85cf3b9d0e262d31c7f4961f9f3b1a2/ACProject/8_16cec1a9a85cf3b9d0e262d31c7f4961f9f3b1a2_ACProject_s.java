 /*******************************************************************************
  * Copyright (c) 2013 Instituto Superior Técnico - João Antunes
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Luis Silva - ACGHSync
  *     João Antunes - initial API and implementation
  ******************************************************************************/
 package pt.ist.maidSyncher.domain.activeCollab;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
import java.awt.Label;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.ist.maidSyncher.api.activeCollab.ACCategory;
 import pt.ist.maidSyncher.domain.MaidRoot;
 import pt.ist.maidSyncher.domain.SynchableObject;
 import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
 import pt.ist.maidSyncher.domain.dsi.DSIObject;
 import pt.ist.maidSyncher.domain.dsi.DSIProject;
 import pt.ist.maidSyncher.domain.dsi.DSIRepository;
 import pt.ist.maidSyncher.domain.exceptions.SyncActionError;
 import pt.ist.maidSyncher.domain.github.GHLabel;
 import pt.ist.maidSyncher.domain.github.GHRepository;
 import pt.ist.maidSyncher.domain.sync.EmptySyncActionWrapper;
 import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
 import pt.ist.maidSyncher.domain.sync.SyncEvent;
 import pt.ist.maidSyncher.utils.MiscUtils;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 
 public class ACProject extends ACProject_Base {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ACProject.class);
 
     //PropertyDescriptor s
     protected static final String DSC_STATUS = "status";
     protected static final String DSC_OVERVIEW = "overview";
     protected static final String DSC_NAME = "name";
     protected static final String DSC_TYPE = "type";
     protected static final String DSC_ARCHIVED = "archived";
 
     protected static final String DSC_BUDGET = "budget";
 
     public ACProject() {
         super();
         setAcInstance(MaidRoot.getInstance().getAcInstance());
     }
 
     public static ACProject process(pt.ist.maidSyncher.api.activeCollab.ACProject acProject) {
         return process(acProject, false);
     }
 
     public static ACProject process(pt.ist.maidSyncher.api.activeCollab.ACProject acProject, boolean skipSync) {
         checkNotNull(acProject);
         ACProject projectToReturn =
                 (ACProject) findOrCreateAndProccess(acProject, ACProject.class, MaidRoot.getInstance().getAcObjectsSet(),
                         skipSync);
         return projectToReturn;
     }
 
     public static ACProject findById(long id) {
         return (ACProject) MiscUtils.findACObjectsById(id, ACProject.class);
     }
 
     public static ACProject findByName(final String name) {
         checkArgument(StringUtils.isBlank(name) == false, "Name mustn't be blank");
         MaidRoot maidRoot = MaidRoot.getInstance();
 
         Optional<ACObject> optional = Iterables.tryFind(maidRoot.getAcObjectsSet(), new Predicate<ACObject>() {
             @Override
             public boolean apply(ACObject input) {
                 if (input instanceof ACProject) {
                     ACProject acProject = (ACProject) input;
                     return name.equalsIgnoreCase(acProject.getName());
                 } else
                     return false;
             }
         });
         return (ACProject) (optional.isPresent() ? optional.get() : null);
     }
 
     public static Set<ACProject> getActiveProjects() {
         Set<ACObject> acObjects = MaidRoot.getInstance().getAcObjectsSet();
         return new HashSet(Collections2.filter(acObjects, new Predicate<ACObject>() {
             @Override
             public boolean apply(ACObject input) {
                 if (input == null)
                     return false;
 
                 if (ACProject.class.isAssignableFrom(input.getClass())) {
                     ACProject acProject = (ACProject) input;
                     return acProject.getArchived() == false;
 
                 }
                 return false;
 
             }
         }));
     }
 
     @Override
     public DSIObject getDSIObject() {
         return getDsiObjectProject();
     }
 
     @Override
     public DSIObject findOrCreateDSIObject() {
         DSIObject dsiObject = getDSIObject();
         if (dsiObject == null) {
             dsiObject = new DSIProject();
             setDsiObjectProject((DSIProject) dsiObject);
         }
         return dsiObject;
     }
 
     @Override
     public SyncActionWrapper sync(SyncEvent syncEvent) {
         SyncActionWrapper syncActionWrapperToReturn = null;
         if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.CREATE)) {
 
             //let's try to find the already existing GHLabels that can fit as GHLabels
             //associated with this DSIProject
             MaidRoot maidRoot = MaidRoot.getInstance();
 
             syncActionWrapperToReturn = syncCreateEvent(syncEvent);
 
         } else if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.UPDATE)) {
             syncActionWrapperToReturn = syncUpdateEvent(syncEvent);
         }
 
         else {
             LOGGER.warn("Read and Delete events not supported yet. " + syncEvent);
             syncActionWrapperToReturn = new EmptySyncActionWrapper(syncEvent);
         }
 
         return syncActionWrapperToReturn;
 
     }
 
     private SyncActionWrapper syncUpdateEvent(final SyncEvent syncEvent) {
         final Set<String> tickedDescriptors = new HashSet<>();
         boolean isNowArchivedAux = false;
         boolean nameChangedAux = false;
         for (String changedDescriptor : syncEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
             tickedDescriptors.add(changedDescriptor);
             switch (changedDescriptor) {
             case DSC_PERMALINK:
             case DSC_UPDATED_BY_ID:
             case DSC_UPDATED_ON:
             case DSC_BUDGET:
             case DSC_ID:
             case DSC_CREATED_ON:
             case DSC_OVERVIEW:
             case DSC_URL:
             case DSC_CREATED_BY_ID:
                 break; //the ones above, there'se no sense in changing anything
             case DSC_TYPE:
             case DSC_ARCHIVED:
                 //if archived, got to remove GHLabels
                 isNowArchivedAux = getArchived();
                 break;
             case DSC_NAME:
                 //got to change GHLabels
                 nameChangedAux = true;
                 break;
             case DSC_STATUS:
                 //let's do nothing here
                 break;
             default:
                 tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                 //cases, let's remove it from the ticked descriptors
 
             }
 
         }
 
         final boolean isNowArchived = isNowArchivedAux;
         final boolean nameChanged = nameChangedAux;
         return new SyncActionWrapper<SynchableObject>() {
 
             @Override
             public Set<SynchableObject> sync() throws SyncActionError {
                 Set<SynchableObject> changedObjects = new HashSet<>();
                 DSIProject dsiProject = (DSIProject) getDSIObject();
                 MaidRoot maidRoot = MaidRoot.getInstance();
                 LabelService labelService = new LabelService(maidRoot.getGitHubClient());
                 try {
 
                     if (isNowArchived) {
                         //let us get all the labels, and delete them
                         for (GHLabel ghLabel : dsiProject.getGitHubLabelsSet()) {
                             labelService.deleteLabel(ghLabel.getRepository(), ghLabel.getName());
                             //remove them
                             ghLabel.delete();
                             //TODO issue #26 - solve the changedObjects with the deleted question
                         }
                         dsiProject.getGitHubLabelsSet().clear();
                         return Collections.emptySet();
 
                     } else if (nameChanged) {
                         //let us try to reuse all of the reusable labels,
                         //[i.e. the ones already with the correct name]
                         Set<GHLabel> allExistingCorrectLabels = GHLabel.getAllLabelsWith(GHLabel.PROJECT_PREFIX + getName());
                         Set<GHRepository> allRepositoriesWithAlreadyCorrectLabels =
                                 (Set<GHRepository>) Collections2.transform(allExistingCorrectLabels,
                                         new Function<GHLabel, GHRepository>() {
                                     @Override
                                     public GHRepository apply(GHLabel ghLabel) {
                                         if (ghLabel == null)
                                             return null;
                                         return ghLabel.getRepository();
                                     }
                                 });
 
                         //getting all labels to be edited - from all repositories
                         Set<GHLabel> labelsToBeEdited = new HashSet<>(dsiProject.getGitHubLabelsSet());
                         for (GHLabel labelToBeEdited : labelsToBeEdited) {
                            Label labelToEdit = labelService.getLabel(labelToBeEdited.getRepository(), labelToBeEdited.getName());
                             if (allRepositoriesWithAlreadyCorrectLabels.contains(labelToBeEdited.getRepository())) {
                                 //now we have a GHLabel that corresponds to the ACProject that still
                                 //has the old name. We should be deprecating it and using the already existing label with the correct name
                                 //let us append to this label name, a -DEPRECATED_LABEL
                                 labelToEdit.setName(labelToEdit.getName() + "-DEPRECATED_LABEL");
                                 labelToEdit = labelService.editLabel(labelToBeEdited.getRepository(), labelToEdit);
                             } else {
                                 //we need to edit it
                                 labelToEdit.setName(GHLabel.PROJECT_PREFIX + getName());
                                 labelToEdit = labelService.editLabel(labelToBeEdited.getRepository(), labelToEdit);
                             }
                             //let's apply the changes to the GHLabel
                             try {
                                 labelToBeEdited.copyPropertiesFrom(labelToEdit);
                                 changedObjects.add(labelToBeEdited);
                             } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException
                                     | TaskNotVisibleException e) {
                                 throw new Error("oopsie daisy, do it manually", e);
                             }
                         }
                         //now let us associate the GHLabel with this DSIProject
                         dsiProject.getGitHubLabelsSet().clear();
                         dsiProject.getGitHubLabelsSet().addAll(labelsToBeEdited);
 
                     }
                 } catch (IOException exception) {
                     throw new SyncActionError(exception, changedObjects);
                 }
                 return changedObjects;
             }
 
             @Override
             public SyncEvent getOriginatingSyncEvent() {
                 return syncEvent;
             }
 
             @Override
             public Collection<DSIObject> getSyncDependedDSIObjects() {
                 return Collections.emptySet();
             }
 
             @Override
             public Set<java.lang.Class> getSyncDependedTypesOfDSIObjects() {
                 //say that we depend on all of the repositories being synched
                 return Collections.singleton((Class) DSIRepository.class);
 
             }
 
             @Override
             public Collection<String> getPropertyDescriptorNamesTicked() {
                 return tickedDescriptors;
             };
         };
     }
 
     private SyncActionWrapper syncCreateEvent(final SyncEvent syncEvent) {
         final Set<String> tickedDescriptors = new HashSet<>();
         for (String changedDescriptor : syncEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
             tickedDescriptors.add(changedDescriptor);
             switch (changedDescriptor) {
             case DSC_PERMALINK:
             case DSC_UPDATED_BY_ID:
             case DSC_UPDATED_ON:
             case DSC_BUDGET:
             case DSC_ID:
             case DSC_CREATED_ON:
             case DSC_OVERVIEW:
             case DSC_URL:
             case DSC_CREATED_BY_ID:
                 break; //the ones above, there'se no sense in changing anything
             case DSC_TYPE:
             case DSC_ARCHIVED:
             case DSC_NAME:
             case DSC_STATUS:
                 break;
             default:
                 tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                 //cases, let's remove it from the ticked descriptors
 
             }
 
         }
 
         return new SyncActionWrapper<SynchableObject>() {
 
             @Override
             public Set<SynchableObject> sync() throws SyncActionError {
                 Set<SynchableObject> changedObjects = new HashSet<>();
                 try {
                     if (getArchived())
                         return Collections.emptySet();
                     Set<GHLabel> labelsToAssignToDSIProject = new HashSet<GHLabel>();
                     //let us get all of the repositories we need to create a label for
                     Set<GHRepository> repositoriesWeNeedToCreateLabelsFor =
                             new HashSet(MaidRoot.getInstance().getGhRepositoriesSet());
 
                     final Set<GHLabel> appliableAndAlreadyExistingLabels =
                             GHLabel.getAllLabelsWith(GHLabel.PROJECT_PREFIX + getName());
                     for (GHLabel ghLabel : appliableAndAlreadyExistingLabels) {
                         repositoriesWeNeedToCreateLabelsFor.remove(ghLabel.getRepository());
                         labelsToAssignToDSIProject.add(ghLabel);
                     }
 
                     //let us create the labels we need to
                     LabelService labelService = new LabelService(MaidRoot.getGitHubClient());
                     for (GHRepository ghRepository : repositoriesWeNeedToCreateLabelsFor) {
                         Label newLabel = new Label();
                         newLabel.setName(GHLabel.PROJECT_PREFIX + getName());
                         Label newlyCreatedLabel = labelService.createLabel(ghRepository, newLabel);
                         labelsToAssignToDSIProject.add(GHLabel.process(newlyCreatedLabel, ghRepository.getId(), true));
                     }
 
                     //let us remove the old labels and set the new ones on
                     //the appropriate DSIProject relation
                     DSIProject dsiProject = (DSIProject) getDSIObject();
                     dsiProject.getGitHubLabelsSet().clear();
                     dsiProject.getGitHubLabelsSet().addAll(labelsToAssignToDSIProject);
 
                     changedObjects.addAll(labelsToAssignToDSIProject);
                     //now let's take care of the TaskCategory, i.e., we created a new ACProject
                     //thus we need to create a taskcategory for each repository
                     Set<ACTaskCategory> taskCategoriesDefined = getTaskCategoriesDefinedSet();
                     Set<String> taskCategoriesNames =
                             new HashSet<>(Collections2.transform(taskCategoriesDefined, new Function<ACTaskCategory, String>() {
 
                                 @Override
                                 public String apply(ACTaskCategory acTaskCategory) {
                                     if (acTaskCategory == null)
                                         return null;
                                     return StringUtils.lowerCase(acTaskCategory.getName());
                                 }
                             }));
 
                             Set<ACCategory> taskCategoriesToCreate = new HashSet<>();
 
                             Set<GHRepository> repositoriesToCreateTaskCategoriesFor =
                                     new HashSet(MaidRoot.getInstance().getGhRepositoriesSet());
 
                             Iterator<GHRepository> repoIterator = repositoriesToCreateTaskCategoriesFor.iterator();
                             //let's see if any of them are appropriate to be used
                             while (repoIterator.hasNext()) {
                                 GHRepository ghRepository = repoIterator.next();
                                 String taskCategoryName = StringUtils.lowerCase(ACTaskCategory.REPOSITORY_PREFIX + ghRepository.getName());
                                 if (taskCategoriesNames.contains(taskCategoryName))
                                     repoIterator.remove();
 
                             }
 
                             for (GHRepository ghRepository : repositoriesToCreateTaskCategoriesFor) {
                                 String taskCategoryToCreateName = ACTaskCategory.REPOSITORY_PREFIX + getName();
                                 if (taskCategoriesNames.contains(StringUtils.lowerCase(taskCategoryToCreateName)) == false) {
                                     ACCategory acCategory = new ACCategory();
                                     acCategory.setName(taskCategoryToCreateName);
                                     changedObjects.add(ACTaskCategory.process(
                                             ACCategory.create(acCategory, getId(), ACTaskCategory.class), getId(), true));
                                 }
 
                             }
                 } catch (IOException ex) {
                     throw new SyncActionError(ex, changedObjects);
                 }
 
                 return changedObjects;
             }
 
             @Override
             public Collection<String> getPropertyDescriptorNamesTicked() {
                 return tickedDescriptors;
             }
 
             @Override
             public SyncEvent getOriginatingSyncEvent() {
                 return syncEvent;
             }
 
             @Override
             public Collection<DSIObject> getSyncDependedDSIObjects() {
                 return Collections.emptySet();
             }
 
             @Override
             public Set<Class> getSyncDependedTypesOfDSIObjects() {
                 return Collections.emptySet();
             }
         };
     }
 
 }
