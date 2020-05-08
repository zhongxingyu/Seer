 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.web.controller.section;
 
 import java.util.List;
 
 import org.jtalks.common.validation.EntityValidator;
 import org.jtalks.common.validation.ValidationResult;
 import org.jtalks.poulpe.model.entity.BranchSectionVisitable;
 import org.jtalks.poulpe.model.entity.BranchSectionVisitor;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.jtalks.poulpe.service.BranchService;
 import org.jtalks.poulpe.service.SectionService;
 import org.jtalks.poulpe.web.controller.DialogManager;
 import org.jtalks.poulpe.web.controller.SelectedEntity;
 import org.jtalks.poulpe.web.controller.WindowManager;
 
 /**
  * This class is used as Presenter layer in Model-View-Presenter pattern for managing PoulpeSection entities
  * 
  * @author Konstantin Akimov
  * @author Vahluev Vyacheslav
  * @author Grigorev Alexey
  * @author Vyacheslav Zhivaev
  */
 public class SectionPresenter {
 
     public static final String ERROR_LABEL_SECTION_NAME_ALREADY_EXISTS = "sections.error.section_name_already_exists";
     public static final String ERROR_LABEL_SECTION_NAME_CANT_BE_VOID = "sections.error.section_name_cant_be_void";
     public static final String ERROR_LABEL_SECTION_NAME_VERY_LONG = "sections.editsection.name.err";
 
     public static final String BRANCH_PERMISSION_MANAGEMENT_ZUL = "/sections/BranchPermissionManagement.zul";
 
     // injected
     private SectionService sectionService;
     private BranchService branchService;
     private SectionView sectionView;
     private ZkSectionTreeComponent currentSectionTreeComponent;
     private DialogManager dialogManager;
     private WindowManager windowManager;
     private EntityValidator entityValidator;
     private SelectedEntity<PoulpeBranch> selectedEntity;
 
     private DeleteSectionDialogPresenter deleteSectionDialogPresenter;
 
     /**
      * Creates actions to be executed for creating, deleting, editing Sections and Branches after user presses 'YES' in
      * dialog manager
      */
     private PerfomableFactory perfomableFactory = new PerfomableFactory(this);
 
     /**
      * initialize main view SectionView instance
      * 
      * @param view instance
      */
     public void initView(SectionView view) {
         perfomableFactory.setSectionView(view);
         this.sectionView = view;
         updateView();
     }
 
     /**
      * Use when need update view
      * */
     public void updateView() {
         List<PoulpeSection> sections = sectionService.getAll();
         sectionView.addSections(sections);
     }
 
     /**
      * Remove section from sectionView
      * 
      * @param section which will be removed from view
      */
     public void removeSectionFromView(PoulpeSection section) {
         sectionView.removeSection(section);
     }
 
     /**
      * This method is used to show dialog for editing section or branch
      * 
      * @param currentSectionTreeComponentImpl from this instance we get selected object
      */
     public void openEditDialog(ZkSectionTreeComponent currentSectionTreeComponentImpl) {
         BranchSectionVisitable visitable = currentSectionTreeComponentImpl.getSelectedObject();
         if (visitable != null) {
             visitable.apply(editVisitor);
         }
 
         // TODO: get rid of it! Why it's needed here?
         setCurrentSectionTreeComponentImpl(currentSectionTreeComponentImpl);
     }
 
     private BranchSectionVisitor editVisitor = new BranchSectionVisitor() {
         @Override
         public void visitSection(PoulpeSection section) {
             sectionView.openEditSectionDialog(section);
         }
 
         @Override
         public void visitBranch(PoulpeBranch branch) {
             sectionView.openEditBranchDialog(branch);
         }
     };
 
     /**
      * Method used for delete section or branch.
      * 
      * @param object can be PoulpeSection or PoulpeBranch instance
      */
     public void openDeleteDialog(BranchSectionVisitable visitable) {
         if (visitable != null) {
             visitable.apply(deleteVisitor);
         }
     }
 
     private BranchSectionVisitor deleteVisitor = new BranchSectionVisitor() {
         /** {@inheritDoc} */
         @Override
         public void visitSection(PoulpeSection section) {
             deleteSectionDialogPresenter.show(section);
         }
 
         /** {@inheritDoc} */
         @Override
         public void visitBranch(PoulpeBranch branch) {
             dialogManager.confirmDeletion(branch.getName(), perfomableFactory.deleteBranch(branch));
         }
     };
 
     /**
      * This method is used to show new branch dialog
      * 
      * @param sectionTreeComponentImpl method save reference on this instance
      */
     public void openNewBranchDialog(ZkSectionTreeComponent sectionTreeComponentImpl) {
         this.currentSectionTreeComponent = sectionTreeComponentImpl;
         sectionView.openNewBranchDialog();
     }
 
     /**
      * This method is used to show new section dialog
      */
     public void openNewSectionDialog() {
         sectionView.openNewSectionDialog();
     }
 
     public void openBranchPermissionsDialog(PoulpeBranch branch) {
         selectedEntity.setEntity(branch);
         windowManager.open(BRANCH_PERMISSION_MANAGEMENT_ZUL);
     }
 
     /**
      * This method is invoked when the user saves editions and push edit button
      * 
      * @param name is edited section name
      * @param description is edited section description
      */
     public boolean editSection(String name, String description) {
         PoulpeSection section = (PoulpeSection) currentSectionTreeComponent.getSelectedObject();
         section.setName(name);
         section.setDescription(description);
 
         if (validate(section)) {
             dialogManager.confirmEdition(name, perfomableFactory.updateSection(section));
             sectionView.closeEditSectionDialog();
             return true;
         } else {
             return false;
         }
 
     }
 
     /**
      * Create new PoulpeSection object and save it if there no any Sections with the same name in other cases (the name
      * is already) should display error dialog
      * 
      * @param name section
      * @param description section
      */
     public boolean addNewSection(String name, String description) {
         PoulpeSection section = new PoulpeSection(name, description);
 
         if (validate(section)) {
             dialogManager.confirmCreation(name, perfomableFactory.saveSection(section));
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Delete section via sectionService
      * 
      * @param recipient if specified than all branches should be add as children to this section. If null then all
      * children should be also deleted
      */
     public void deleteSection(PoulpeSection recipient) {
         BranchSectionVisitable selectedObject = currentSectionTreeComponent.getSelectedObject();
 
         if (!(selectedObject instanceof PoulpeSection)) {
             return;
         }
 
         PoulpeSection victim = (PoulpeSection) selectedObject;
         dialogManager.confirmDeletion(victim.getName(), perfomableFactory.deleteSection(victim, recipient));
         removeSectionFromView(victim);
 
     }
 
     /**
      * Method used to manage moderation dialog
      * 
      * @param branch
      */
     public void openModerationDialog(PoulpeBranch branch) {
         sectionView.openModerationDialog(branch);
     }
 
     public boolean validate(PoulpeSection section) {
         ValidationResult result = entityValidator.validate(section);
 
         if (result.hasErrors()) {
             sectionView.validationFailure(result);
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Save section
      * 
      * @param section the section to save
      */
     public void saveSection(PoulpeSection section) {
         sectionService.saveSection(section);
     }
 
     /**
      * Opens dialog for moderating a selected branch
      * 
      * @param selectedObject
      */
     public void openModerationWindow(BranchSectionVisitable selectedObject) {
         selectedObject.apply(openModeratorDialogVisitor);
     }
 
     /**
      * Visitor which opens moderation dialog only for branches
      */
     private BranchSectionVisitor openModeratorDialogVisitor = new BranchSectionVisitor() {
         /** {@inheritDoc} */
         @Override
         public void visitSection(PoulpeSection section) {
             // do nothing because moderators windows is not applicable for sections
         }
 
         /** {@inheritDoc} */
         @Override
         public void visitBranch(PoulpeBranch branch) {
             openModerationDialog(branch);
         }
     };
 
     /**
      * @param entityValidator the entityValidator to set
      */
     public void setEntityValidator(EntityValidator entityValidator) {
         this.entityValidator = entityValidator;
     }
 
     /**
      * @param service set section service instance
      */
     public void setSectionService(SectionService service) {
         perfomableFactory.setSectionService(service);
         this.sectionService = service;
     }
 
     public void setBranchService(BranchService branchService) {
         perfomableFactory.setBranchService(branchService);
         this.branchService = branchService;
     }
 
     /**
      * @param dialogManager manager to manage interaction with user
      */
     public void setDialogManager(DialogManager dialogManager) {
         this.dialogManager = dialogManager;
     }
 
     /**
      * @param currentSectionTreeComponent is current <code>ZkSectionTreeComponent</code> that will process actions from
      * presenter
      */
     public void setCurrentSectionTreeComponentImpl(ZkSectionTreeComponent currentSectionTreeComponent) {
         perfomableFactory.setCurrentSectionTreeComponent(currentSectionTreeComponent);
         this.currentSectionTreeComponent = currentSectionTreeComponent;
     }
 
     /**
      * @param perfomableFactory instance to set
      */
     public void setPerfomableFactory(PerfomableFactory perfomableFactory) {
         this.perfomableFactory = perfomableFactory;
     }
 
     /**
      * @param deleteSectionDialogPresenter delete dialog presenter
      */
     public void setDeleteSectionDialogPresenter(DeleteSectionDialogPresenter deleteSectionDialogPresenter) {
         this.deleteSectionDialogPresenter = deleteSectionDialogPresenter;
         this.deleteSectionDialogPresenter.setSectionPresenter(this);
     }
 
     /**
      * @param selectedEntity the selectedEntity to set
      */
     public void setSelectedEntity(SelectedEntity<PoulpeBranch> selectedEntity) {
         this.selectedEntity = selectedEntity;
     }
 
 }
