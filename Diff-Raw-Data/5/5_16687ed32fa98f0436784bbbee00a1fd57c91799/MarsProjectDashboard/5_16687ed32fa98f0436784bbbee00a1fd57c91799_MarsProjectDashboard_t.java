 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.ibpworkbench.comp;
 
 import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.ibpworkbench.Message;
 import org.generationcp.ibpworkbench.actions.FieldBookUploadSucceededListener;
 import org.generationcp.ibpworkbench.actions.FileUploadFailedListener;
 import org.generationcp.ibpworkbench.actions.LaunchWorkbenchToolAction;
 import org.generationcp.ibpworkbench.actions.LaunchWorkbenchToolAction.ToolEnum;
 import org.generationcp.ibpworkbench.comp.window.FileUploadWindow;
 import org.generationcp.middleware.pojos.workbench.Project;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.themes.BaseTheme;
 
 @Configurable
 public class MarsProjectDashboard extends VerticalLayout implements InitializingBean, InternationalizableComponent {
 
     private static final long serialVersionUID = 1L;
     
     private static final String DOWN_ARROW_THEME_RESOURCE = "images/blc-arrow-d.png";
 
     private Project project;
 
     private Label dashboardTitle;
 
     // Breeding Management controls
     private Label breedingManagementTitle;
 
     private Label projectPlanningTitle;
     private Label fieldTrialManagementTitle;
     private Label genotypingTitle;
 
     private Label loadDataSetsTitle;
     private Label phenotypicAnalysisTitle;
     private Label genotypicAnalysisTitle;
     private Label qtlAnalysisTitle;
 
     private Label plantSelectionTitle;
 
     // private Link fieldBookLink;
     private Button fieldBookButton;
     private Button uploadFieldBookDataButton; // NOTE: We can remove this later
 
     private Label populationDevelopmentTitle;
     private Button browseGermplasmButton;
     private Button browseStudiesButton;
     private Button gdmsButton;
 
     private Button breedingViewButton;
 
     private Button optimasButton;
 
     // Marker Trait Analysis controls
     private Label markerTraitAnalysisTitle;
     private Label markerTraitAnalysisAreaTitle;
 
     // Marker Implementation controls
     private Label markerImplementationTitle;
 
     private Label recombinationCycleTitle;
 
     private Button breedingManagerButton;
     
     private Embedded downArrow11;
     private Embedded downArrow12;
     private Embedded downArrow13;
     
     private Embedded downArrow31;
     private Embedded downArrow32;
     private Embedded downArrow33;
     
     @Autowired
     private SimpleResourceBundleMessageSource messageSource;
 
     public MarsProjectDashboard(Project project) {
         super();
         this.project = project;
     }
     
     @Override
     public void afterPropertiesSet() {
         assemble();
     }
 
     protected void initializeComponents() {
         // dashboard title
         dashboardTitle = new Label();
         dashboardTitle.setStyleName("gcp-content-title");
 
         // breeding management
         breedingManagementTitle = new Label("Breeding Management");
         breedingManagementTitle.setStyleName("gcp-section-title");
 
         // project planning
         projectPlanningTitle = new Label("Project Planning");
         projectPlanningTitle.setStyleName("gcp-section-title");
         projectPlanningTitle.setSizeUndefined();
 
         populationDevelopmentTitle = new Label("Population Development");
         populationDevelopmentTitle.setStyleName("gcp-section-title");
         populationDevelopmentTitle.setSizeUndefined();
 
         fieldTrialManagementTitle = new Label("Field Trial Management");
         fieldTrialManagementTitle.setStyleName("gcp-section-title");
         fieldTrialManagementTitle.setSizeUndefined();
 
         genotypingTitle = new Label("Genotyping");
         genotypingTitle.setStyleName("gcp-section-title");
         genotypingTitle.setSizeUndefined();
 
         loadDataSetsTitle = new Label("Load Datasets");
         loadDataSetsTitle.setStyleName("gcp-section-title");
         loadDataSetsTitle.setSizeUndefined();
 
         phenotypicAnalysisTitle = new Label("Phenotypic Analysis");
         phenotypicAnalysisTitle.setStyleName("gcp-section-title");
         phenotypicAnalysisTitle.setSizeUndefined();
 
         genotypicAnalysisTitle = new Label("Genotypic Analysis");
         genotypicAnalysisTitle.setStyleName("gcp-section-title");
         genotypicAnalysisTitle.setSizeUndefined();
 
         qtlAnalysisTitle = new Label("QTL Analysis");
         qtlAnalysisTitle.setStyleName("gcp-section-title");
         qtlAnalysisTitle.setSizeUndefined();
 
         // fieldBookLink = new Link("Field Book", new
         // ExternalResource("http://localhost:10080/ibfb/master.jnlp"));
         // fieldBookLink.setSizeUndefined();
 
         fieldBookButton = new Button("Field Book");
         fieldBookButton.setStyleName(BaseTheme.BUTTON_LINK);
         fieldBookButton.setSizeUndefined();
         fieldBookButton.setDescription("Click to launch Fieldbook");
 
         uploadFieldBookDataButton = new Button("Upload Field Book Data");
         uploadFieldBookDataButton.setStyleName(BaseTheme.BUTTON_LINK);
         uploadFieldBookDataButton.setSizeUndefined();
 
         browseGermplasmButton = new Button("Browse Germplasm Information");
         browseGermplasmButton.setStyleName(BaseTheme.BUTTON_LINK);
         browseGermplasmButton.setSizeUndefined();
         browseGermplasmButton.setDescription("Click to launch Germplasm Browser");
 
         browseStudiesButton = new Button("Browse Studies and Datasets");
         browseStudiesButton.setStyleName(BaseTheme.BUTTON_LINK);
         browseStudiesButton.setSizeUndefined();
         browseStudiesButton.setDescription("Click to launch Study Browser");
 
         gdmsButton = new Button("Manage Genotyping Data");
         gdmsButton.setStyleName(BaseTheme.BUTTON_LINK);
         gdmsButton.setSizeUndefined();
         gdmsButton.setDescription("Click to launch GDMS");
 
         breedingViewButton = new Button("Breeding View");
         breedingViewButton.setStyleName(BaseTheme.BUTTON_LINK);
         breedingViewButton.setSizeUndefined();
         breedingViewButton.setDescription("Click to launch Breeding View");
 
         optimasButton = new Button("OptiMAS");
         optimasButton.setStyleName(BaseTheme.BUTTON_LINK);
         optimasButton.setSizeUndefined();
         optimasButton.setDescription("Click to launch OptiMAS");
 
         // marker trait analysis
         markerTraitAnalysisTitle = new Label("Marker Trait Analysis");
         markerTraitAnalysisTitle.setStyleName("gcp-section-title");
         markerTraitAnalysisTitle.setSizeUndefined();
 
        markerTraitAnalysisAreaTitle = new Label("Marker Trait Analysis");
         markerTraitAnalysisAreaTitle.setStyleName("gcp-section-title");
         markerTraitAnalysisAreaTitle.setSizeUndefined();
 
         // marker implementation
         markerImplementationTitle = new Label("Marker Implementation");
         markerImplementationTitle.setStyleName("gcp-section-title");
 
         plantSelectionTitle = new Label("Plant Selection");
         plantSelectionTitle.setStyleName("gcp-section-title");
         plantSelectionTitle.setSizeUndefined();
 
         recombinationCycleTitle = new Label("Recombination Cycle");
         recombinationCycleTitle.setStyleName("gcp-section-title");
         recombinationCycleTitle.setSizeUndefined();
 
         breedingManagerButton = new Button("Breeding Manager");
         breedingManagerButton.setStyleName(BaseTheme.BUTTON_LINK);
         breedingManagerButton.setSizeUndefined();
         breedingManagerButton.setDescription("Click to launch Nursery Manager");
 
         downArrow11 = new Embedded(null, new ThemeResource(DOWN_ARROW_THEME_RESOURCE));
         downArrow12 = new Embedded(null, new ThemeResource(DOWN_ARROW_THEME_RESOURCE));
         downArrow13 = new Embedded(null, new ThemeResource(DOWN_ARROW_THEME_RESOURCE));
         
         downArrow31 = new Embedded(null, new ThemeResource(DOWN_ARROW_THEME_RESOURCE));
         downArrow32 = new Embedded(null, new ThemeResource(DOWN_ARROW_THEME_RESOURCE));
         downArrow33 = new Embedded(null, new ThemeResource(DOWN_ARROW_THEME_RESOURCE));
     }
 
     protected void initializeLayout() {
         setSpacing(true);
         setMargin(true);
         setWidth("1100px");
 
         dashboardTitle.setSizeUndefined();
         addComponent(dashboardTitle);
 
         Component workFlowArea = layoutWorkflowArea();
         workFlowArea.setSizeUndefined();
         addComponent(workFlowArea);
 
     }
 
     protected Component layoutWorkflowArea() {
         Panel panel = new Panel();
 
         HorizontalLayout layout = new HorizontalLayout();
         layout.setMargin(true);
         layout.setSpacing(true);
         layout.setWidth("1000px");
 
         Component breedingManagementArea = layoutBreedingManagementArea();
         breedingManagementArea.setHeight("100%");
         layout.addComponent(breedingManagementArea);
 
         Component markerTraitAnalysisArea = layoutMarkerTraitAnalysis();
         markerTraitAnalysisArea.setHeight("100%");
         layout.addComponent(markerTraitAnalysisArea);
 
         Component markerImplementationArea = layoutMarkerImplementation();
         markerImplementationArea.setHeight("100%");
         layout.addComponent(markerImplementationArea);
 
         panel.setContent(layout);
         return panel;
     }
 
     protected Component layoutBreedingManagementArea() {
         Panel panel = new Panel();
 
         VerticalLayout layout = new VerticalLayout();
         layout.setHeight("630px");
         layout.setMargin(true);
         layout.setSpacing(false);
 
         breedingManagementTitle.setSizeUndefined();
         layout.addComponent(breedingManagementTitle);
         layout.setComponentAlignment(breedingManagementTitle, Alignment.TOP_CENTER);
 
         Component projectPlanningArea = layoutProjectPlanning();
         layout.addComponent(projectPlanningArea);
         
         layout.addComponent(downArrow11);
         layout.setComponentAlignment(downArrow11, Alignment.MIDDLE_CENTER);
 
         Component populationManagementArea = layoutPopulationDevelopment();
         layout.addComponent(populationManagementArea);
         
         layout.addComponent(downArrow12);
         layout.setComponentAlignment(downArrow12, Alignment.MIDDLE_CENTER);
 
         Component fieldTrialArea = layoutFieldTrialManagement();
         layout.addComponent(fieldTrialArea);
         
         layout.addComponent(downArrow13);
         layout.setComponentAlignment(downArrow13, Alignment.MIDDLE_CENTER);
 
         Component genotypingArea = layoutGenotyping();
         layout.addComponent(genotypingArea);
 
         panel.setContent(layout);
         return panel;
     }
 
     protected Component layoutProjectPlanning() {
         VerticalLayout layout = new VerticalLayout();
         configureWorkflowStepLayout(layout);
 
         layout.addComponent(projectPlanningTitle);
         layout.setComponentAlignment(projectPlanningTitle, Alignment.TOP_CENTER);
         layout.setExpandRatio(projectPlanningTitle, 0);
 
         Label emptyLabel = new Label(" ");
         emptyLabel.setWidth("100%");
         emptyLabel.setHeight("20px");
         layout.addComponent(emptyLabel);
         layout.setExpandRatio(emptyLabel, 100);
 
         layout.addComponent(browseGermplasmButton);
         browseGermplasmButton.setHeight("20px");
         layout.setComponentAlignment(browseGermplasmButton, Alignment.TOP_CENTER);
         layout.setExpandRatio(browseGermplasmButton, 0);
 
         layout.addComponent(browseStudiesButton);
         layout.setComponentAlignment(browseStudiesButton, Alignment.TOP_CENTER);
         layout.setExpandRatio(browseStudiesButton, 0);
 
         return layout;
     }
 
     protected Component layoutPopulationDevelopment() {
         VerticalLayout layout = new VerticalLayout();
         configureWorkflowStepLayout(layout);
 
         layout.addComponent(populationDevelopmentTitle);
         layout.setComponentAlignment(populationDevelopmentTitle, Alignment.TOP_CENTER);
         layout.setExpandRatio(populationDevelopmentTitle, 0);
 
         Label emptyLabel = new Label(" ");
         emptyLabel.setWidth("100%");
         emptyLabel.setHeight("20px");
         layout.addComponent(emptyLabel);
         layout.setExpandRatio(emptyLabel, 100);
 
         layout.addComponent(breedingManagerButton);
         layout.setComponentAlignment(breedingManagerButton, Alignment.TOP_CENTER);
         layout.setExpandRatio(breedingManagerButton, 0);
 
         return layout;
     }
 
     protected Component layoutFieldTrialManagement() {
         VerticalLayout layout = new VerticalLayout();
         configureWorkflowStepLayout(layout);
 
         layout.addComponent(fieldTrialManagementTitle);
         layout.setComponentAlignment(fieldTrialManagementTitle, Alignment.TOP_CENTER);
         layout.setExpandRatio(fieldTrialManagementTitle, 0);
 
         Label emptyLabel = new Label(" ");
         emptyLabel.setWidth("100%");
         emptyLabel.setHeight("20px");
         layout.addComponent(emptyLabel);
         layout.setExpandRatio(emptyLabel, 100);
 
         layout.addComponent(fieldBookButton);
         layout.setComponentAlignment(fieldBookButton, Alignment.TOP_CENTER);
         layout.setExpandRatio(fieldBookButton, 0);
 
         return layout;
     }
 
     protected Component layoutGenotyping() {
         VerticalLayout layout = new VerticalLayout();
         configureWorkflowStepLayout(layout);
 
         layout.addComponent(genotypingTitle);
         layout.setComponentAlignment(genotypingTitle, Alignment.TOP_CENTER);
         layout.setExpandRatio(genotypingTitle, 0);
 
         Label emptyLabel = new Label(" ");
         emptyLabel.setWidth("100%");
         emptyLabel.setHeight("20px");
         layout.addComponent(emptyLabel);
         layout.setExpandRatio(emptyLabel, 100);
 
         layout.addComponent(gdmsButton);
         layout.setComponentAlignment(gdmsButton, Alignment.TOP_CENTER);
         layout.setExpandRatio(gdmsButton, 0);
 
         return layout;
     }
 
     protected Component layoutMarkerTraitAnalysis() {
         Panel panel = new Panel();
 
         VerticalLayout layout = new VerticalLayout();
         layout.setHeight("630px");
         layout.setMargin(true);
         layout.setSpacing(true);
 
         layout.addComponent(markerTraitAnalysisTitle);
         layout.setComponentAlignment(markerTraitAnalysisTitle, Alignment.TOP_CENTER);
         layout.setExpandRatio(markerTraitAnalysisTitle, 0);
 
         Component markerTraitAnalysisArea = layoutMarkerTraitAnalysisButton();
         layout.addComponent(markerTraitAnalysisArea);
 
         Label emptyLabel = new Label(" ");
         emptyLabel.setWidth("100%");
         emptyLabel.setHeight("20px");
         layout.addComponent(emptyLabel);
         layout.setExpandRatio(emptyLabel, 100);
 
         panel.setContent(layout);
         return panel;
     }
 
     protected Component layoutMarkerTraitAnalysisButton() {
         VerticalLayout layout = new VerticalLayout();
         configureWorkflowStepLayout(layout);
 
         layout.addComponent(markerTraitAnalysisAreaTitle);
         layout.setComponentAlignment(markerTraitAnalysisAreaTitle, Alignment.TOP_CENTER);
         layout.setExpandRatio(markerTraitAnalysisAreaTitle, 0);
 
         Label emptyLabel = new Label(" ");
         emptyLabel.setWidth("100%");
         emptyLabel.setHeight("20px");
         layout.addComponent(emptyLabel);
         layout.setExpandRatio(emptyLabel, 100);
 
         layout.addComponent(breedingViewButton);
         layout.setComponentAlignment(breedingViewButton, Alignment.TOP_CENTER);
         layout.setExpandRatio(breedingViewButton, 0);
 
         return layout;
     }
 
     protected Component layoutMarkerImplementation() {
         Panel panel = new Panel();
 
         VerticalLayout layout = new VerticalLayout();
         layout.setHeight("630px");
         layout.setMargin(true);
         layout.setSpacing(true);
 
         markerImplementationTitle.setSizeUndefined();
         layout.addComponent(markerImplementationTitle);
         layout.setComponentAlignment(markerImplementationTitle, Alignment.TOP_CENTER);
         layout.setExpandRatio(markerImplementationTitle, 0);
 
         // Component ideotypeDesignArea = createPanel("9. Ideotype Design",
         // "Selection Index");
         // layout.addComponent(ideotypeDesignArea);
         
         Component plantSelectionArea = layoutPlantSelection();
         layout.addComponent(plantSelectionArea);
         layout.setExpandRatio(plantSelectionArea, 0);
         
         layout.addComponent(downArrow31);
         layout.setComponentAlignment(downArrow31, Alignment.MIDDLE_CENTER);
         
         Component recombinationCycleArea = layoutRecombinationCycle();
         layout.addComponent(recombinationCycleArea);
         layout.setExpandRatio(recombinationCycleArea, 0);
 
         layout.addComponent(downArrow32);
         layout.setComponentAlignment(downArrow32, Alignment.MIDDLE_CENTER);
         
        Component projectCompletionArea = createPanel("Project Completion");
         layout.addComponent(projectCompletionArea);
         layout.setExpandRatio(recombinationCycleArea, 0);
 
         panel.setContent(layout);
         return panel;
     }
 
     protected Component layoutPlantSelection() {
         VerticalLayout layout = new VerticalLayout();
         configureWorkflowStepLayout(layout);
 
         layout.addComponent(plantSelectionTitle);
         layout.setComponentAlignment(plantSelectionTitle, Alignment.TOP_CENTER);
         layout.setExpandRatio(plantSelectionTitle, 0);
 
         Label emptyLabel = new Label(" ");
         emptyLabel.setWidth("100%");
         emptyLabel.setHeight("20px");
         layout.addComponent(emptyLabel);
         layout.setExpandRatio(emptyLabel, 100);
 
         layout.addComponent(optimasButton);
         layout.setComponentAlignment(optimasButton, Alignment.TOP_CENTER);
         layout.setExpandRatio(optimasButton, 0);
 
         return layout;
     }
 
     protected Component layoutRecombinationCycle() {
         VerticalLayout layout = new VerticalLayout();
         configureWorkflowStepLayout(layout);
 
         layout.addComponent(recombinationCycleTitle);
         layout.setComponentAlignment(recombinationCycleTitle, Alignment.TOP_CENTER);
         layout.setExpandRatio(recombinationCycleTitle, 0);
 
         Label emptyLabel = new Label(" ");
         emptyLabel.setWidth("100%");
         emptyLabel.setHeight("20px");
         layout.addComponent(emptyLabel);
         layout.setExpandRatio(emptyLabel, 100);
 
         return layout;
     }
 
     protected Component createPanel(String caption, String... buttonCaptions) {
         VerticalLayout layout = new VerticalLayout();
         configureWorkflowStepLayout(layout);
 
         Label titleLabel = new Label(caption);
         titleLabel.setStyleName("gcp-section-title");
         titleLabel.setSizeUndefined();
 
         layout.addComponent(titleLabel);
         layout.setComponentAlignment(titleLabel, Alignment.TOP_CENTER);
         layout.setExpandRatio(titleLabel, 0);
 
         Label emptyLabel = new Label(" ");
         emptyLabel.setWidth("100%");
         emptyLabel.setHeight("20px");
         layout.addComponent(emptyLabel);
         layout.setExpandRatio(emptyLabel, 100);
 
         for (String buttonCaption : buttonCaptions) {
             Button button = new Button(buttonCaption);
             button.setStyleName(BaseTheme.BUTTON_LINK);
 
             layout.addComponent(button);
             layout.setComponentAlignment(button, Alignment.TOP_CENTER);
             layout.setExpandRatio(button, 0);
         }
 
         return layout;
     }
 
     protected void configureWorkflowStepLayout(VerticalLayout layout) {
         layout.setWidth("270px");
         layout.setHeight("110px");
         layout.setStyleName("gcp-mars-workflow-step");
         layout.setMargin(true, true, true, true);
     }
 
     protected void initializeActions() {
         uploadFieldBookDataButton.addListener(new ClickListener() {
 
             private static final long serialVersionUID = 1L;
 
             @Override
             public void buttonClick(ClickEvent event) {
                 FileUploadWindow fileUploadWindow = new FileUploadWindow();
                 fileUploadWindow.setWidth("380px");
                 fileUploadWindow.setHeight("240px");
                 fileUploadWindow.setModal(true);
 
                 // set allowed mime types
                 fileUploadWindow.getUpload().addAllowedMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                 fileUploadWindow.getUpload().addAllowedMimeType("application/vnd.ms-excel");
 
                 // set the upload listeners
                 fileUploadWindow.getUpload().addListener(new FieldBookUploadSucceededListener(fileUploadWindow));
                 fileUploadWindow.getUpload().addListener(new FileUploadFailedListener());
 
                 getWindow().addWindow(fileUploadWindow);
                 fileUploadWindow.center();
             }
         });
 
         browseGermplasmButton.addListener(new LaunchWorkbenchToolAction(ToolEnum.GERMPLASM_BROWSER));
         browseStudiesButton.addListener(new LaunchWorkbenchToolAction(ToolEnum.STUDY_BROWSER));
 
         gdmsButton.addListener(new LaunchWorkbenchToolAction(ToolEnum.GDMS));
 
         fieldBookButton.addListener(new LaunchWorkbenchToolAction(ToolEnum.FIELDBOOK));
 
         optimasButton.addListener(new LaunchWorkbenchToolAction(ToolEnum.OPTIMAS));
 
         breedingManagerButton.addListener(new LaunchWorkbenchToolAction(ToolEnum.BREEDING_MANAGER));
 
         breedingViewButton.addListener(new LaunchWorkbenchToolAction(ToolEnum.BREEDING_VIEW));
     }
 
     protected void assemble() {
         initializeComponents();
         initializeLayout();
         initializeActions();
     }
     
     @Override
     public void attach() {
         super.attach();
         
         updateLabels();
     }
     
     @Override
     public void updateLabels() {
         messageSource.setValue(dashboardTitle, Message.project_title, project.getProjectName());
     }
 }
