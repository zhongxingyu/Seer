 package com.solidstategroup.radar.web.panels;
 
 import com.solidstategroup.radar.model.Plasmapheresis;
 import com.solidstategroup.radar.model.Relapse;
 import com.solidstategroup.radar.model.enums.KidneyTransplantedNative;
 import com.solidstategroup.radar.model.enums.RemissionAchieved;
 import com.solidstategroup.radar.web.pages.PatientPage;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.extensions.markup.html.form.DateTextField;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.markup.html.form.RadioGroup;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.Model;
 
 public class PatientRelapsePanel extends Panel {
     public PatientRelapsePanel(String id) {
         super(id);
         setOutputMarkupId(true);
         setOutputMarkupPlaceholderTag(true);
 
         Form<Relapse> form = new Form<Relapse>("form", new CompoundPropertyModel<Relapse>(new Relapse()));
 
         form.add(new DateTextField("dateOfRelapse"));
 
         // Transplanted / native radio options
         RadioGroup<KidneyTransplantedNative> transplantedNative =
                 new RadioGroup<KidneyTransplantedNative>("transplantedNative");
         transplantedNative.add(new Radio<KidneyTransplantedNative>("tx",
                 new Model<KidneyTransplantedNative>(KidneyTransplantedNative.TRANSPLANTED)));
         transplantedNative.add(new Radio<KidneyTransplantedNative>("native",
                 new Model<KidneyTransplantedNative>(KidneyTransplantedNative.NATIVE)));
         form.add(transplantedNative);
 
         // Triggers
         form.add(new TextField("viralTrigger"));
         form.add(new TextField("immunisationTrigger"));
         form.add(new TextField("otherTrigger"));
 
         // Drugs
         form.add(new TextField("drug1"));
         form.add(new TextField("drug2"));
         form.add(new TextField("drug3"));
 
         // Inner form for plasmapheresis
         Form<Plasmapheresis> plasmapheresisForm =
                 new Form<Plasmapheresis>("form", new CompoundPropertyModel<Plasmapheresis>(new Plasmapheresis()));
 
        plasmapheresisForm.add(new DateTextField("startDate"));
        plasmapheresisForm.add(new DateTextField("endDate"));
         plasmapheresisForm.add(new DropDownChoice("plasmapheresisExchanges"));
         plasmapheresisForm.add(new DropDownChoice("response"));
 
         form.add(plasmapheresisForm);
 
         // Remission radio group
         RadioGroup<RemissionAchieved> remissionAchieved = new RadioGroup<RemissionAchieved>("remissionAchieved");
         remissionAchieved.add(new Radio<RemissionAchieved>("complete",
                 new Model<RemissionAchieved>(RemissionAchieved.COMPLETE)));
         remissionAchieved
                 .add(new Radio<RemissionAchieved>("partial", new Model<RemissionAchieved>(RemissionAchieved.PARTIAL)));
         remissionAchieved
                 .add(new Radio<RemissionAchieved>("none", new Model<RemissionAchieved>(RemissionAchieved.NONE)));
         form.add(remissionAchieved);
 
         form.add(new DateTextField("dateOfRemission"));
 
         form.add(new AjaxSubmitLink("submit") {
             @Override
             protected void onSubmit(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                 // Todo: Implement
             }
 
             @Override
             protected void onError(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                 // Todo: Implement
             }
         });
 
         add(form);
 
     }
 
     @Override
     public boolean isVisible() {
         return ((PatientPage) getPage()).getCurrentTab().equals(PatientPage.CurrentTab.RELAPSE);
     }
 }
