 package com.solidstategroup.radar.web.panels;
 
 import com.solidstategroup.radar.model.ClinicalPresentation;
 import com.solidstategroup.radar.model.Demographics;
 import com.solidstategroup.radar.model.Diagnosis;
 import com.solidstategroup.radar.model.DiagnosisCode;
 import com.solidstategroup.radar.model.Karotype;
 import com.solidstategroup.radar.model.sequenced.ClinicalData;
 import com.solidstategroup.radar.service.ClinicalDataManager;
 import com.solidstategroup.radar.service.DemographicsManager;
 import com.solidstategroup.radar.service.DiagnosisManager;
 import com.solidstategroup.radar.web.RadarApplication;
 import com.solidstategroup.radar.web.components.ClinicalPresentationDropDownChoice;
 import com.solidstategroup.radar.web.components.ComponentHelper;
 import com.solidstategroup.radar.web.components.RadarComponentFactory;
 import com.solidstategroup.radar.web.components.RadarDateTextField;
 import com.solidstategroup.radar.web.components.RadarTextFieldWithValidation;
 import com.solidstategroup.radar.web.models.RadarModelFactory;
 import com.solidstategroup.radar.web.pages.PatientPage;
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.extensions.markup.html.form.DateTextField;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.markup.html.form.RadioGroup;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.RangeValidator;
 import org.joda.time.DateTime;
 import org.joda.time.Years;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 
 public class DiagnosisPanel extends Panel {
 
     public static final Long SRNS_ID = new Long(1);
     public static final String OTHER_CONTAINER_ID = "otherContainer";
     public static final Long KAROTYPE_OTHER_ID = new Long(8);
     @SpringBean
     private DiagnosisManager diagnosisManager;
     @SpringBean
     private DemographicsManager demographicsManager;
     @SpringBean
     private ClinicalDataManager clinicalDataManager;
 
     public DiagnosisPanel(String id, final IModel<Long> radarNumberModel) {
         super(id);
         setOutputMarkupId(true);
         setOutputMarkupPlaceholderTag(true);
 
         // Set up model
         // Set up loadable detachable, working for null radar numbers (new patients) and existing
         final CompoundPropertyModel<Diagnosis> model = new CompoundPropertyModel<Diagnosis>(new LoadableDetachableModel<Diagnosis>() {
             @Override
             protected Diagnosis load() {
                 if (radarNumberModel.getObject() != null) {
                     Long radarNumber;
                     try {
                         radarNumber = radarNumberModel.getObject();
                     } catch (ClassCastException e) {
                         Object obj = radarNumberModel.getObject();
                         radarNumber = Long.parseLong((String) obj);
                     }
                     Diagnosis diagnosis = diagnosisManager.getDiagnosisByRadarNumber(radarNumber);
                     if (diagnosis != null) {
                         return diagnosis;
                     } else {
                         return new Diagnosis();
                     }
                 } else {
                     return new Diagnosis();
                 }
             }
         });
 
         // Clinical presentation A - goes here in the file as referenced in form submit
         final DropDownChoice<ClinicalPresentation> clinicalPresentationA =
                 new ClinicalPresentationDropDownChoice("clinicalPresentationA");
         clinicalPresentationA.setOutputMarkupId(true);
         clinicalPresentationA.setOutputMarkupPlaceholderTag(true);
 
         final Form<Diagnosis> form =
                 new Form<Diagnosis>("form", model) {
                     @Override
                     protected void onValidateModelObjects() {
                         super.onValidateModelObjects();
                         Diagnosis diagnosis = getModelObject();
                         ClinicalPresentation presentationA = diagnosis.getClinicalPresentationA();
                         ClinicalPresentation presentationB = diagnosis.getClinicalPresentationB();
 
                         // Validate that the two aren't the same
                         if (presentationA != null && presentationB != null && presentationA.equals(presentationB)) {
                             clinicalPresentationA.error("A and B cannot be the same");
                         }
                     }
 
                     @Override
                     protected void onSubmit() {
                         Diagnosis diagnosis = getModelObject();
                         Date dateOfDiagnosis = diagnosis.getBiopsyDate();
                         Long radarNumber;
                         try {
                             radarNumber = radarNumberModel.getObject();
                         } catch (ClassCastException e) {
                             Object obj = radarNumberModel.getObject();
                             radarNumber = Long.parseLong((String) obj);
                         }
 
                         Demographics demographics = demographicsManager.getDemographicsByRadarNumber(radarNumber);
                         Date dob = demographics.getDateOfBirth();
                         if (dateOfDiagnosis != null && dob != null) {
                             int age = Years.yearsBetween(new DateTime(dob), new DateTime(dateOfDiagnosis)).getYears();
                             diagnosis.setAgeAtDiagnosis(age);
                         }
                         diagnosisManager.saveDiagnosis(diagnosis);
 
                         // additional significant diagnosis needs to carry through to clinical data
                         List<ClinicalData> clinicalDataList = clinicalDataManager.getClinicalDataByRadarNumber(
                                 diagnosis.getRadarNumber());
                         if (clinicalDataList.isEmpty()) {
                             ClinicalData clinicalData = new ClinicalData();
                             clinicalData.setSequenceNumber(1);
                             clinicalData.setRadarNumber(diagnosis.getRadarNumber());
                             clinicalDataList.add(clinicalData);
                         }
 
                         for (ClinicalData clinicalData : clinicalDataList) {
                             clinicalData.setSignificantDiagnosis1(diagnosis.getSignificantDiagnosis1());
                             clinicalData.setSignificantDiagnosis2(diagnosis.getSignificantDiagnosis2());
                             clinicalDataManager.saveClinicalDate(clinicalData);
                         }
 
                     }
                 };
         add(form);
 
         final List<Component> componentsToUpdate = new ArrayList<Component>();
 
         Label successLabel = RadarComponentFactory.getSuccessMessageLabel("successMessage", form, componentsToUpdate);
         Label successLabelDown = RadarComponentFactory.getSuccessMessageLabel("successMessageDown", form,
                 componentsToUpdate);
 
         Label errorLabel = RadarComponentFactory.getErrorMessageLabel("errorMessage", form, componentsToUpdate);
         Label errorLabelDown = RadarComponentFactory.getErrorMessageLabel("errorMessageDown", form, componentsToUpdate);
 
         TextField<Long> radarNumber = new TextField<Long>("radarNumber");
         radarNumber.setEnabled(false);
         form.add(radarNumber);
 
         TextField hospitalNumber = new TextField("hospitalNumber", RadarModelFactory.getHospitalNumberModel(
                 radarNumberModel, demographicsManager));
         form.add(hospitalNumber);
 
         TextField firstName = new TextField("firstName", RadarModelFactory.getFirstNameModel(radarNumberModel,
                 demographicsManager));
         form.add(firstName);
 
         TextField surname = new TextField("surname", RadarModelFactory.getSurnameModel(radarNumberModel,
                 demographicsManager));
         form.add(surname);
 
         TextField dob = new DateTextField("dateOfBirth", RadarModelFactory.getDobModel(radarNumberModel,
                 demographicsManager), RadarApplication.DATE_PATTERN);
         form.add(dob);
 
         DropDownChoice<DiagnosisCode> diagnosisCodeDropDownChoice = new DropDownChoice<DiagnosisCode>("diagnosisCode",
                 diagnosisManager.getDiagnosisCodes(),
                 new ChoiceRenderer<DiagnosisCode>("abbreviation", "id"));
         diagnosisCodeDropDownChoice.setEnabled(false);
         form.add(diagnosisCodeDropDownChoice);
 
 
         form.add(new TextArea("text"));
 
         form.add(new Label("diagnosisOrBiopsy", new LoadableDetachableModel<Object>() {
             @Override
             protected Object load() {
                 Diagnosis diagnosis = model.getObject();
                 if (diagnosis.getDiagnosisCode() != null) {
                     return diagnosis.getDiagnosisCode().getId().equals(SRNS_ID) ? "diagnosis" : "original biopsy";
                 }
                 return "";
             }
         }));
 
         // this field is also used for date of diagnosis
         RadarDateTextField biopsyDate =
                 new RadarDateTextField("biopsyDate", form, componentsToUpdate);
 
         form.add(biopsyDate);
 
         RadarDateTextField esrfDate =
                 new RadarDateTextField("esrfDate", form, componentsToUpdate);
         form.add(esrfDate);
 
         TextField ageAtDiagnosis = new TextField("ageAtDiagnosis");
         ageAtDiagnosis.setOutputMarkupId(true);
         ageAtDiagnosis.setOutputMarkupPlaceholderTag(true);
         form.add(ageAtDiagnosis);
         componentsToUpdate.add(ageAtDiagnosis);
         form.add(new CheckBox("prepubertalAtDiagnosis"));
 
         final RadarTextFieldWithValidation heightAtDiagnosis =
                 new RadarTextFieldWithValidation("heightAtDiagnosis", new RangeValidator<Double>(
                         RadarApplication.MIN_HEIGHT, RadarApplication.MAX_HEIGHT), form,
                         componentsToUpdate);
         form.add(heightAtDiagnosis);
 
         // Clinical presentation B - A is further up the file
         final DropDownChoice<ClinicalPresentation> clinicalPresentationB =
                 new ClinicalPresentationDropDownChoice("clinicalPresentationB");
         clinicalPresentationB.setOutputMarkupId(true);
         clinicalPresentationB.setOutputMarkupPlaceholderTag(true);
 
         form.add(clinicalPresentationA, clinicalPresentationB);
         form.add(new RadarDateTextField("onsetSymptomsDate", form, componentsToUpdate));
 
         ComponentFeedbackPanel clinicalPresentationFeedback =
                 new ComponentFeedbackPanel("clinicalPresentationFeedback", clinicalPresentationA);
         clinicalPresentationFeedback.setOutputMarkupId(true);
         clinicalPresentationFeedback.setOutputMarkupPlaceholderTag(true);
         form.add(clinicalPresentationFeedback);
 
         // Steroid resistance radio groups
         RadioGroup steroidRadioGroup = new RadioGroup("steroidResistance") {
             @Override
             public boolean isVisible() {
                 DiagnosisCode diagnosisCode = model.getObject().getDiagnosisCode();
                 if (diagnosisCode != null) {
                     return diagnosisCode.getId().equals(SRNS_ID);
                 } else {
                     return false;
                 }
 
             }
         };
         steroidRadioGroup.setRequired(true);
         steroidRadioGroup.add(new Radio<Diagnosis.SteroidResistance>("primarySteroidResistance",
                 new Model<Diagnosis.SteroidResistance>(Diagnosis.SteroidResistance.PRIMARY)));
         steroidRadioGroup.add(new Radio<Diagnosis.SteroidResistance>("secondarySteroidResistance",
                 new Model<Diagnosis.SteroidResistance>(Diagnosis.SteroidResistance.SECONDARY)));
         steroidRadioGroup.add(new Radio<Diagnosis.SteroidResistance>("presumedSteroidResistance",
                 new Model<Diagnosis.SteroidResistance>(Diagnosis.SteroidResistance.PRESUMED)));
         steroidRadioGroup.add(new Radio<Diagnosis.SteroidResistance>("biopsyProven",
                 new Model<Diagnosis.SteroidResistance>(Diagnosis.SteroidResistance.BPS)));
         form.add(steroidRadioGroup);
 
 
         // Construct feedback panel
         final ComponentFeedbackPanel steroidFeedbackPanel = new ComponentFeedbackPanel("steroidResistanceFeedback",
                 steroidRadioGroup);
         steroidFeedbackPanel.setOutputMarkupPlaceholderTag(true);
         form.add(steroidFeedbackPanel);
         steroidRadioGroup.setOutputMarkupPlaceholderTag(true);
         steroidRadioGroup.add(steroidFeedbackPanel);
         componentsToUpdate.add(steroidFeedbackPanel);
 
         // Additional significant diagnosis
         form.add(new TextField("significantDiagnosis1"));
         form.add(new TextField("significantDiagnosis2"));
 
         // Biopsy Diagnosis visibilities
         IModel<String> biopsyLabelModel = new LoadableDetachableModel<String>() {
             @Override
             protected String load() {
                 Diagnosis diagnosis = model.getObject();
                 if (diagnosis.getDiagnosisCode() != null) {
                     if (diagnosis.getDiagnosisCode().getId().equals(SRNS_ID)) {
                         return "Biopsy Diagnosis";
                     } else {
                         return "Biopsy Proven Diagnosis";
                     }
                 } else {
                     return "";
                 }
             }
         };
 
         IModel<List> biopsyDiagnosisModel = new LoadableDetachableModel<List>() {
             @Override
             protected List load() {
                 Diagnosis diagnosis = model.getObject();
                 if (diagnosis.getDiagnosisCode() != null) {
                     if (diagnosis.getDiagnosisCode().getId().equals(SRNS_ID)) {
                         return Arrays.asList(Diagnosis.BiopsyDiagnosis.MINIMAL_CHANGE, Diagnosis.BiopsyDiagnosis.FSGS,
                                 Diagnosis.BiopsyDiagnosis.MESANGIAL_HYPERTROPHY, Diagnosis.BiopsyDiagnosis.OTHER);
                     } else {
                         return Arrays.asList(Diagnosis.BiopsyDiagnosis.YES, Diagnosis.BiopsyDiagnosis.NO);
                     }
                 }
                 return Collections.emptyList();
             }
         };
 
         DropDownChoice biopsyDiagnosis = new DropDownChoice("biopsyProvenDiagnosis", biopsyDiagnosisModel,
                 new ChoiceRenderer("label", "id"));
 
         Label biopsyDiagnosisLabel = new Label("biopsyDiagnosisLabel", biopsyLabelModel);
 
         form.add(biopsyDiagnosis, biopsyDiagnosisLabel);
 
 
         Diagnosis diagnosis = model.getObject();
         boolean showOtherDetailsOnInit;
         showOtherDetailsOnInit = diagnosis.getMutationYorN9() == Diagnosis.MutationYorN.Y;
         final IModel<Boolean> otherDetailsVisibilityModel = new Model<Boolean>(showOtherDetailsOnInit);
 
         boolean showMoreDetailsOnInit = false;
         if (diagnosis.getMutationYorN1() == Diagnosis.MutationYorN.Y
                 || diagnosis.getMutationYorN2() == Diagnosis.MutationYorN.Y
                 || diagnosis.getMutationYorN3() == Diagnosis.MutationYorN.Y
                 || diagnosis.getMutationYorN4() == Diagnosis.MutationYorN.Y
                 || diagnosis.getMutationYorN5() == Diagnosis.MutationYorN.Y
                 || diagnosis.getMutationYorN6() == Diagnosis.MutationYorN.Y
                 || diagnosis.getMutationYorN7() == Diagnosis.MutationYorN.Y
                 || diagnosis.getMutationYorN8() == Diagnosis.MutationYorN.Y) {
 
             showMoreDetailsOnInit = true;
         }
 
         final IModel<Boolean> moreDetailsVisibilityModel = new Model<Boolean>(showMoreDetailsOnInit);
 
         WebMarkupContainer geneMutationContainer = new WebMarkupContainer("geneMutationContainer") {
             @Override
             public boolean isVisible() {
                IModel<Boolean> isSrnsModel = RadarModelFactory.getIsSrnsModel(radarNumberModel, diagnosisManager);
                return isSrnsModel.getObject();
             }
         };
 
         WebMarkupContainer mutationContainer = new WebMarkupContainer("mutationContainer");
 
         form.add(geneMutationContainer);
 
         Label geneMutationLabel = new Label("geneMutationLabel", "Gene Mutation");
 
         geneMutationContainer.add(geneMutationLabel);
         geneMutationContainer.add(mutationContainer);
 
         // Gene mutations
         mutationContainer.add(new DiagnosisGeneMutationPanel("nphs1Container", 1, (CompoundPropertyModel) form.getModel(),
                 otherDetailsVisibilityModel, moreDetailsVisibilityModel, componentsToUpdate));
 
         mutationContainer.add(new DiagnosisGeneMutationPanel("nphs2Container", 2, (CompoundPropertyModel) form.getModel(),
                 otherDetailsVisibilityModel, moreDetailsVisibilityModel, componentsToUpdate));
 
         mutationContainer.add(new DiagnosisGeneMutationPanel("nphs3Container", 3, (CompoundPropertyModel) form.getModel(),
                 otherDetailsVisibilityModel, moreDetailsVisibilityModel, componentsToUpdate));
 
         mutationContainer.add(new DiagnosisGeneMutationPanel("wt1Container", 4, (CompoundPropertyModel) form.getModel(),
                 otherDetailsVisibilityModel, moreDetailsVisibilityModel, componentsToUpdate));
 
         mutationContainer.add(new DiagnosisGeneMutationPanel("cd2apContainer", 5, (CompoundPropertyModel) form.getModel(),
                 otherDetailsVisibilityModel, moreDetailsVisibilityModel, componentsToUpdate));
 
         mutationContainer.add(new DiagnosisGeneMutationPanel("trpc6Container", 6, (CompoundPropertyModel) form.getModel(),
                 otherDetailsVisibilityModel, moreDetailsVisibilityModel, componentsToUpdate));
 
         mutationContainer.add(new DiagnosisGeneMutationPanel("actn4Container", 7, (CompoundPropertyModel) form.getModel(),
                 otherDetailsVisibilityModel, moreDetailsVisibilityModel, componentsToUpdate));
 
         mutationContainer.add(new DiagnosisGeneMutationPanel("lamb2Container", 8, (CompoundPropertyModel) form.getModel(),
                 otherDetailsVisibilityModel, moreDetailsVisibilityModel, componentsToUpdate));
 
         mutationContainer.add(new DiagnosisGeneMutationPanel(OTHER_CONTAINER_ID, 9, (CompoundPropertyModel) form.getModel(),
                 otherDetailsVisibilityModel, moreDetailsVisibilityModel, componentsToUpdate));
 
         // Other gene mutation container
         MarkupContainer otherGeneMutationContainer = new WebMarkupContainer("otherGeneMutationContainer") {
 
             @Override
             public boolean isVisible() {
                 return otherDetailsVisibilityModel.getObject();
             }
         };
 
 
         otherGeneMutationContainer.setOutputMarkupId(true);
         otherGeneMutationContainer.setOutputMarkupPlaceholderTag(true);
 
         otherGeneMutationContainer.add(new TextArea("otherGeneMutation"));
 
         geneMutationContainer.add(otherGeneMutationContainer);
 
 
         // more details
         MarkupContainer moreDetailsContainer = new WebMarkupContainer("moreDetailsContainer") {
 
             @Override
             public boolean isVisible() {
                 return moreDetailsVisibilityModel.getObject();
             }
         };
         moreDetailsContainer.setOutputMarkupId(true);
         moreDetailsContainer.setOutputMarkupPlaceholderTag(true);
         moreDetailsContainer.add(new TextArea("moreDetails", new Model()));
         geneMutationContainer.add(moreDetailsContainer);
         componentsToUpdate.add(moreDetailsContainer);
 
 
         componentsToUpdate.add(otherGeneMutationContainer);
 
         boolean showKaroTypeOtherOnInit = false;
         if (diagnosis.getKarotype() != null) {
             showKaroTypeOtherOnInit = diagnosis.getKarotype().getId().equals(KAROTYPE_OTHER_ID);
         }
         final IModel<Boolean> karoTypeOtherVisibilityModel = new Model<Boolean>(showKaroTypeOtherOnInit);
 
         // Add Karotype
         DropDownChoice<Karotype> karotypeDropDownChoice = new DropDownChoice<Karotype>("karotype", diagnosisManager.getKarotypes(),
                 new ChoiceRenderer<Karotype>("description", "id"));
 
         WebMarkupContainer karoTypeContainer = new WebMarkupContainer("karoTypeContainer") {
             @Override
             public boolean isVisible() {
                 Diagnosis diagnosis = model.getObject();
                 if (diagnosis.getDiagnosisCode() != null) {
                     return diagnosis.getDiagnosisCode().getId().equals(SRNS_ID);
                 }
                 return false;
             }
         };
         karoTypeContainer.add(karotypeDropDownChoice);
         form.add(karoTypeContainer);
 
 
         karotypeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 Diagnosis diagnosis = model.getObject();
                 Karotype karotype = diagnosis.getKarotype();
                 if (karotype != null) {
                     karoTypeOtherVisibilityModel.setObject(karotype.getId().equals(KAROTYPE_OTHER_ID));
                     ComponentHelper.updateComponentsIfParentIsVisible(target, componentsToUpdate);
                 }
             }
         });
 
         // karotype other
         MarkupContainer karoTypeOtherContainer = new WebMarkupContainer("karoTypeOtherContainer") {
             @Override
             public boolean isVisible() {
                 return karoTypeOtherVisibilityModel.getObject();
             }
         };
         karoTypeOtherContainer.setOutputMarkupId(true);
         karoTypeOtherContainer.setOutputMarkupPlaceholderTag(true);
         karoTypeOtherContainer.add(new TextArea("karoTypeOtherText"));
         componentsToUpdate.add(karoTypeOtherContainer);
         form.add(karoTypeOtherContainer);
 
         // Parental consanguinity and family history
         form.add(new YesNoDropDownChoice("parentalConsanguinity"));
 
         YesNoDropDownChoice familyHistory = new YesNoDropDownChoice("familyHistory");
 
         boolean showFamilyOnInit = false;
         showFamilyOnInit = diagnosis.getFamilyHistory() == Diagnosis.YesNo.YES;
         final IModel<Boolean> familyVisibilityModel = new Model<Boolean>(showFamilyOnInit);
 
         familyHistory.add(new AjaxFormComponentUpdatingBehavior("onChange") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 Diagnosis diagnosis = model.getObject();
                 if (diagnosis.getFamilyHistory() != null) {
                     familyVisibilityModel.setObject(diagnosis.getFamilyHistory() == Diagnosis.YesNo.YES);
                 }
                 ComponentHelper.updateComponentsIfParentIsVisible(target, componentsToUpdate);
             }
         });
 
         form.add(familyHistory);
         // Family history containers
         form.add(new DiagnosisRelativePanel("relative1Container", 1, (CompoundPropertyModel) form.getModel(),
                 familyVisibilityModel, componentsToUpdate));
         form.add(new DiagnosisRelativePanel("relative2Container", 2, (CompoundPropertyModel) form.getModel(),
                 familyVisibilityModel, componentsToUpdate));
         form.add(new DiagnosisRelativePanel("relative3Container", 3, (CompoundPropertyModel) form.getModel(),
                 familyVisibilityModel, componentsToUpdate));
         form.add(new DiagnosisRelativePanel("relative4Container", 4, (CompoundPropertyModel) form.getModel(),
                 familyVisibilityModel, componentsToUpdate));
         form.add(new DiagnosisRelativePanel("relative5Container", 5, (CompoundPropertyModel) form.getModel(),
                 familyVisibilityModel, componentsToUpdate));
         form.add(new DiagnosisRelativePanel("relative6Container", 6, (CompoundPropertyModel) form.getModel(),
                 familyVisibilityModel, componentsToUpdate));
 
         componentsToUpdate.add(clinicalPresentationFeedback);
 
         Label radarFamilyLabel = new Label("radarFamilyLabel", "RADAR No") {
 
             @Override
             public boolean isVisible() {
                 return familyVisibilityModel.getObject();
             }
         };
         radarFamilyLabel.setOutputMarkupId(true);
         radarFamilyLabel.setOutputMarkupPlaceholderTag(true);
         componentsToUpdate.add(radarFamilyLabel);
         form.add(radarFamilyLabel);
 
 
         DiagnosisAjaxSubmitLink save = new DiagnosisAjaxSubmitLink("save") {
             @Override
             protected List<? extends Component> getComponentsToUpdate() {
                 return componentsToUpdate;
             }
         };
 
         DiagnosisAjaxSubmitLink saveDown = new DiagnosisAjaxSubmitLink("saveDown") {
 
             @Override
             protected List<? extends Component> getComponentsToUpdate() {
                 return componentsToUpdate;
             }
         };
 
         form.add(save, saveDown);
     }
 
 
     @Override
     public boolean isVisible() {
         return ((PatientPage) getPage()).getCurrentTab().equals(PatientPage.CurrentTab.DIAGNOSIS);
     }
 
     private abstract class DiagnosisAjaxSubmitLink extends AjaxSubmitLink {
 
         public DiagnosisAjaxSubmitLink(String id) {
             super(id);
         }
 
         @Override
         public void onSubmit(AjaxRequestTarget target, Form<?> form) {
             ComponentHelper.updateComponentsIfParentIsVisible(target, getComponentsToUpdate());
         }
 
         @Override
         protected void onError(AjaxRequestTarget target, Form<?> form) {
             ComponentHelper.updateComponentsIfParentIsVisible(target, getComponentsToUpdate());
         }
 
         protected abstract List<? extends Component> getComponentsToUpdate();
     }
 
     private class YesNoDropDownChoice extends DropDownChoice<Diagnosis.YesNo> {
         public YesNoDropDownChoice(String id) {
             super(id);
             setChoices(Arrays.asList(Diagnosis.YesNo.values()));
 
             // Capitalise value
             setChoiceRenderer(new IChoiceRenderer<Diagnosis.YesNo>() {
                 public Object getDisplayValue(Diagnosis.YesNo object) {
                     return StringUtils.capitalize(object.toString().toLowerCase());
                 }
 
                 public String getIdValue(Diagnosis.YesNo object, int index) {
                     return String.valueOf(object.getId());
                 }
             });
         }
     }
 }
