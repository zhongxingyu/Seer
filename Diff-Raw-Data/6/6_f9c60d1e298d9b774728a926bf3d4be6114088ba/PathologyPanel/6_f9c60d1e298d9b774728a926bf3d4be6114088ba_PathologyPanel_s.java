 package com.solidstategroup.radar.web.panels;
 
 import com.solidstategroup.radar.model.enums.KidneyTransplantedNative;
 import com.solidstategroup.radar.model.sequenced.Pathology;
 import com.solidstategroup.radar.service.DemographicsManager;
 import com.solidstategroup.radar.service.DiagnosisManager;
 import com.solidstategroup.radar.service.PathologyManager;
 import com.solidstategroup.radar.web.components.RadarComponentFactory;
 import com.solidstategroup.radar.web.components.RadarRequiredDateTextField;
 import com.solidstategroup.radar.web.components.RadarTextFieldWithValidation;
 import com.solidstategroup.radar.web.models.RadarModelFactory;
 import com.solidstategroup.radar.web.pages.PatientPage;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.markup.html.form.RadioGroup;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.RangeValidator;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class PathologyPanel extends Panel {
 
     @SpringBean
     private PathologyManager pathologyManager;
     @SpringBean
     private DemographicsManager demographicsManager;
     @SpringBean
     private DiagnosisManager diagnosisManager;
 
     public PathologyPanel(String id, final IModel<Long> radarNumberModel) {
         super(id);
         setOutputMarkupId(true);
         setOutputMarkupPlaceholderTag(true);
 
         // Model for tha pathology container, the pathology ID
         final IModel<Pathology> pathologyModel = new Model<Pathology>();
 
         IModel pathologiesListModel = new AbstractReadOnlyModel<List>() {
             @Override
             public List getObject() {
                 if (radarNumberModel.getObject() != null) {
                     List list = pathologyManager.getPathologyByRadarNumber(radarNumberModel.getObject());
                     return !list.isEmpty() ? list : Collections.emptyList();
                 }
                 return Collections.emptyList();
             }
         };
 
         // Container for form, so we can hide and first then show
         final MarkupContainer pathologyContainer = new WebMarkupContainer("pathologyContainer");
         pathologyContainer.setVisible(false);
         pathologyContainer.setOutputMarkupId(true);
         pathologyContainer.setOutputMarkupPlaceholderTag(true);
         add(pathologyContainer);
 
         // Switcheroo
         final DropDownChoice<Pathology> pathologySwitcher =
                 new DropDownChoice<Pathology>("pathologySwitcher", pathologyModel, pathologiesListModel,
                         new ChoiceRenderer<Pathology>("biopsyDate", "id"));
         pathologySwitcher.setOutputMarkupId(true);
         pathologySwitcher.setOutputMarkupPlaceholderTag(true);
         add(pathologySwitcher);
 
         // Add new
         add(new AjaxLink("addNew") {
             @Override
             public void onClick(AjaxRequestTarget target) {
                 pathologyContainer.setVisible(true);
                 pathologyModel.setObject(new Pathology());
                 pathologySwitcher.clearInput();
                 target.add(pathologyContainer, pathologySwitcher);
             }
         });
 
         // Add Ajax behaviour to switch the container on change
         pathologySwitcher.add(new AjaxFormComponentUpdatingBehavior("onchange") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 pathologyContainer.setVisible(true);
                 target.add(pathologyContainer);
             }
         });
 
         final List<Component> componentsToUpdate = new ArrayList<Component>();
         componentsToUpdate.add(pathologySwitcher);
 
         // Set up model
         Form<Pathology> form = new Form<Pathology>("form", new CompoundPropertyModel<Pathology>(pathologyModel)) {
             @Override
             protected void onSubmit() {
                 Pathology pathology = getModelObject();
                 pathology.setRadarNumber(radarNumberModel.getObject());
                 pathologyManager.savePathology(pathology);
             }
         };
         pathologyContainer.add(form);
 
         RadarComponentFactory.getSuccessMessageLabel("successMessage", form, componentsToUpdate);
         RadarComponentFactory.getSuccessMessageLabel("successMessageDown", form, componentsToUpdate);
 
         RadarComponentFactory.getErrorMessageLabel("errorMessage", form, componentsToUpdate);
         RadarComponentFactory.getErrorMessageLabel("errorMessageDown", form, componentsToUpdate);
 
         // General details
         TextField<Long> radarNumber = new TextField<Long>("radarNumber", radarNumberModel);
         radarNumber.setEnabled(false);
         form.add(radarNumber);
 
         form.add(new TextField("hospitalNumber", RadarModelFactory.getHospitalNumberModel(radarNumberModel,
                 demographicsManager)));
 
         form.add(new TextField("diagnosis", new PropertyModel(RadarModelFactory.getDiagnosisCodeModel(radarNumberModel,
                 diagnosisManager), "abbreviation")));
 
         form.add(new TextField("firstName", RadarModelFactory.getFirstNameModel(radarNumberModel, demographicsManager)));
         form.add(new TextField("surname", RadarModelFactory.getSurnameModel(radarNumberModel, demographicsManager)));
         form.add(new TextField("dob", RadarModelFactory.getDobModel(radarNumberModel, demographicsManager)));
 
         // Add inputs
         form.add(new RadarRequiredDateTextField("biopsyDate", form, componentsToUpdate));
 
         RadioGroup<KidneyTransplantedNative> kideneyTransplant =
                 new RadioGroup<KidneyTransplantedNative>("KidneyTransplantedNative");
 
         kideneyTransplant.add(new Radio<KidneyTransplantedNative>("native",
                 new Model<KidneyTransplantedNative>(KidneyTransplantedNative.NATIVE)));
         kideneyTransplant.add(new Radio<KidneyTransplantedNative>("txKidney",
                 new Model<KidneyTransplantedNative>(KidneyTransplantedNative.TRANSPLANTED)));
 
         form.add(kideneyTransplant);
 
         RadioGroup<Pathology.Side> side = new RadioGroup<Pathology.Side>("side");
         side.add(new Radio<Pathology.Side>("left", new Model<Pathology.Side>(Pathology.Side.LEFT)));
         side.add(new Radio<Pathology.Side>("right", new Model<Pathology.Side>(Pathology.Side.RIGHT)));
         form.add(side);
 
         form.add(new TextField("sampleLabNumber"));
         form.add(new TextArea("interstitalInflmatoryInfilitrate"));
         form.add(new TextArea("arterialAbnormalities"));
         form.add(new TextArea("immunohistologicalFindings"));
         form.add(new TextArea("electronMicroscopicFindings"));
 
        form.add(new TextField("estimatedTubules"));
        form.add(new TextField("measuredTubules"));
         form.add(new TextArea("tubulesOtherFeature"));
 
         form.add(new TextField("imageUrl1"));
         form.add(new TextField("imageUrl2"));
         form.add(new TextField("imageUrl3"));
         form.add(new TextField("imageUrl4"));
         form.add(new TextField("imageUrl5"));
 
         form.add(new RadarTextFieldWithValidation("totalNumber", new RangeValidator<Integer>(0, 150), form,
                 componentsToUpdate));
         form.add(new RadarTextFieldWithValidation("numberSclerosed", new RangeValidator<Integer>(0, 150), form,
                 componentsToUpdate));
         form.add(new RadarTextFieldWithValidation("numberSegmentallySclerosed", new RangeValidator<Integer>(0, 150),
                 form, componentsToUpdate));
         form.add(new RadarTextFieldWithValidation("numberCellularCrescents", new RangeValidator<Integer>(0, 150), form,
                 componentsToUpdate));
         form.add(new RadarTextFieldWithValidation("numberFibrousCrescents", new RangeValidator<Integer>(0, 150), form,
                 componentsToUpdate));
         form.add(new RadarTextFieldWithValidation("numberEndocapillaryHypercelluarity",
                 new RangeValidator<Integer>(0, 150), form, componentsToUpdate));
         form.add(new RadarTextFieldWithValidation("numberFibrinoidNecrosis", new RangeValidator<Integer>(0, 150), form,
                 componentsToUpdate));
 
         form.add(new TextArea("otherFeature"));
 
         form.add(new TextArea("histologicalSummary"));
 
         form.add(new PathologySubmitLink("save", form) {
             @Override
             protected List<Component> getComponentsToUpdate() {
                 return componentsToUpdate;
             }
         });
 
         form.add(new PathologySubmitLink("saveDown", form) {
             @Override
             protected List<Component> getComponentsToUpdate() {
                 return componentsToUpdate;
             }
         });
     }
 
     @Override
     public boolean isVisible() {
         return ((PatientPage) getPage()).getCurrentTab().equals(PatientPage.CurrentTab.PATHOLOGY);
     }
 
     private abstract class PathologySubmitLink extends AjaxSubmitLink {
 
         protected PathologySubmitLink(String id, Form<?> form) {
             super(id, form);
         }
 
         @Override
         protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
             target.add(getComponentsToUpdate().toArray(new Component[getComponentsToUpdate().size()]));
         }
 
         @Override
         protected void onError(AjaxRequestTarget target, Form<?> form) {
             target.add(getComponentsToUpdate().toArray(new Component[getComponentsToUpdate().size()]));
             ComponentFeedbackPanel a = (ComponentFeedbackPanel) getParent().get("totalNumberFeedback");
             a.getFeedbackMessages();
         }
 
         protected abstract List<Component> getComponentsToUpdate();
     }
 }
