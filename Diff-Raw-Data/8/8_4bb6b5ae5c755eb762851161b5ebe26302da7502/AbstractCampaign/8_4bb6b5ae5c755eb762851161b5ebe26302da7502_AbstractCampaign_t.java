 /*
  * Copyright (c) 2010-2012 Matthias Klass, Johannes Leimer,
  *               Rico Lieback, Sebastian Gabriel, Lothar Gesslein,
  *               Alexander Rampp, Kai Weidner
  *
  * This file is part of the Physalix Enrollment System
  *
  * Foobar is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Foobar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package hsa.awp.admingui.edit;
 
 import hsa.awp.admingui.controller.IAdminGuiController;
 import hsa.awp.admingui.util.AbstractSortedListSelectorPanel;
 import hsa.awp.campaign.model.Campaign;
 import hsa.awp.campaign.model.Procedure;
 import hsa.awp.event.model.Event;
 import hsa.awp.event.model.Term;
 import hsa.awp.event.util.EventSorter;
 import hsa.awp.user.model.StudyCourse;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.extensions.yui.calendar.DateTimeField;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.*;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.EmailAddressValidator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import static hsa.awp.event.util.EventFormattingUtils.formatDetailInformation;
 import static hsa.awp.event.util.EventFormattingUtils.formatEventId;
 
 /**
  * AbstractCampaign class used for storing methods both for {@link AlterCampaignPanel} and {@link CreateCampaignPanel}.
  *
  * @author klassm
  */
 public abstract class AbstractCampaign extends Panel {
   /**
    * unique serialization id.
    */
   private static final long serialVersionUID = -1948215271267672824L;
 
   /**
    * Controller which feeds the class with data.
    */
   @SpringBean(name = "admingui.controller")
   private transient IAdminGuiController controller;
 
   /**
    * endShow represents the endShow attribute in campaign.
    */
   private DateTimeField endShow;
 
   /**
    * Selector for {@link Event}s.
    */
   private EventListSelectorPanel eventListSelector;
 
   /**
    * Feedback panel to display success or errors.
    */
   private FeedbackPanel feedbackPanel;
 
   /**
    * Form object to put attributes.
    */
   private Form<Object> form;
 
   protected IModel<Term> termModel;
 
   /**
    * Logger.
    */
   private transient Logger logger;
 
   /**
    * name of the {@link Campaign}.
    */
   private TextField<String> name = new TextField<String>("name", new Model<String>());
 
   private TextField<String> email;
 
   private TextArea detailText;
 
   /**
    * Selector for {@link Procedure}s.
    */
   private ProcedureListSelectorPanel procedureListSelector;
 
   private StudyCourseListSelectorPanel studyCourseListSelector;
 
   /**
    * startShow represents the startshow attribute in campaign.
    */
   private DateTimeField startShow = new DateTimeField("startShow", new Model<Date>());
 
   /**
    * Creates a new {@link AbstractCampaign}.
    *
    * @param id panelId.
    */
   public AbstractCampaign(String id) {
 
     super(id);
     logger = LoggerFactory.getLogger(this.getClass());
     termModel = new Model<Term>();
   }
 
   /**
    * Getter for the {@link Campaign} with which the {@link AbstractCampaign} shall work.
    *
    * @return {@link Campaign} to work with.
    */
   protected abstract Campaign getCampaign();
 
   /**
    * Getter for controller.
    *
    * @return the controller
    */
   protected IAdminGuiController getController() {
 
     return controller;
   }
 
   /**
    * returns the {@link FeedbackPanel}.
    *
    * @return the {@link FeedbackPanel}.
    */
   protected FeedbackPanel getFeedbackPanel() {
 
     return feedbackPanel;
   }
 
   /**
    * Getter for the Logger.
    *
    * @return Logger.
    */
   protected Logger getLogger() {
 
     return logger;
   }
 
   /**
    * Renders the form page. This should be called after setting the {@link Campaign} to work with.
    */
   protected void renderPage() {
 
     form = new Form<Object>("form");
     form.setOutputMarkupId(true);
 
     studyCourseListSelector = new StudyCourseListSelectorPanel("studyCourseListSelector", getStudyCourseItems(),
         getSelectedStudyCourseItems());
     form.add(studyCourseListSelector);
 
     // add ProcedureListSelector
     procedureListSelector = new ProcedureListSelectorPanel("procedureListSelector", getProcedureModelItems(),
         getProcedureSelectedItems());
     form.add(procedureListSelector);
 
     // add EventListSelector
     eventListSelector = new EventListSelectorPanel("eventListSelector", getEventModelItems(), getEventSelectedItems());
     final WebMarkupContainer eventSelectorBox = new WebMarkupContainer("eventSelectorBox");
     eventSelectorBox.setOutputMarkupId(true);
     eventSelectorBox.add(eventListSelector);
     form.add(eventSelectorBox);
 
     feedbackPanel = new FeedbackPanel("feedbackPanel");
     feedbackPanel.setOutputMarkupId(true);
 
     form.add(feedbackPanel);
 
     endShow = new DateTimeField("endShow", new Model<Date>());
     endShow.setModelObject(getCampaign().getEndShow().getTime());
 
     startShow = new DateTimeField("startShow", new Model<Date>());
     startShow.setModelObject(getCampaign().getStartShow().getTime());
 
     email = new TextField<String>("email", new Model<String>());
     email.setModelObject(getCampaign().getCorrespondentEMail());
     email.setRequired(true);
     email.add(EmailAddressValidator.getInstance());
 
     name = new TextField<String>("name", new Model<String>());
     name.setModelObject(getCampaign().getName());
 
     detailText = new TextArea("detailText", new Model<String>());
     detailText.setModelObject(getCampaign().getDetailText());
 
     List<Term> termChoices = controller.getTermsByMandator(getSession());
 
     DropDownChoice<Term> termDropDown = new DropDownChoice<Term>("term", termModel, termChoices, new IChoiceRenderer<Term>() {
 
       @Override
       public Object getDisplayValue(Term object) {
         if (object != null) {
           return object.toString();
         } else {
           return "Alle";
         }
       }
 
       @Override
       public String getIdValue(Term object, int index) {
         if (object != null) {
           return object.toString();
         } else {
           return String.valueOf(index);
         }
       }
     });
 
     termDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
 
       @Override
       protected void onUpdate(AjaxRequestTarget target) {
         eventSelectorBox.remove(eventListSelector);
         eventListSelector = new EventListSelectorPanel("eventListSelector", getEventModelItems(), getEventSelectedItems());
         eventSelectorBox.add(eventListSelector);
         target.addComponent(eventSelectorBox);
       }
     });
 
     form.add(endShow);
     form.add(startShow);
     form.add(name);
     form.add(email);
     form.add(detailText);
     form.add(termDropDown);
 
     add(form);
 
     form.add(new AjaxButton("submit") {
       /**
        * unique serialization id.
        */
       private static final long serialVersionUID = -545654550066678262L;
 
 
       @Override
       protected void onError(final AjaxRequestTarget target, final Form<?> form) {
 
         target.addComponent(feedbackPanel);
       }
 
       @Override
       protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 
         target.addComponent(form);
         target.addComponent(feedbackPanel);
 
         List<String> errors = AbstractCampaign.this.validate(name.getModelObject(), eventListSelector.getSelected(),
             procedureListSelector.getSelected(), startShow.getModelObject(), endShow.getModelObject(), studyCourseListSelector.getSelected());
         if (errors.size() == 0) {
           Calendar calStartShow = Calendar.getInstance();
           calStartShow.setTime(startShow.getModelObject());
 
           Calendar calEndShow = Calendar.getInstance();
           calEndShow.setTime(endShow.getModelObject());
 
           workResult(name.getModelObject(), email.getModelObject(), eventListSelector.getSelected(), procedureListSelector.getSelected(),
               calStartShow, calEndShow, studyCourseListSelector.getSelected(), (String) detailText.getModelObject());
           //TODO: Sprache
           feedbackPanel.info(getSuccessText());
           this.setVisible(false);
         } else {
           for (String error : errors) {
             feedbackPanel.fatal(error);
           }
         }
       }
     });
   }
 
   protected abstract List<StudyCourse> getSelectedStudyCourseItems();
 
   protected abstract List<StudyCourse> getStudyCourseItems();
 
   protected List<Event> getEventsSelectedByTerm(Term term) {
     return getEventsSelectedByTerm(term, controller.getEventsByMandator(getSession()));
   }
 
   protected List<Event> getEventsSelectedByTerm(Term term, List<Event> list) {
     List<Event> selectedEvents = new ArrayList<Event>();
 
     for (Event event : list) {
       if (term.equals(event.getTerm())) {
         selectedEvents.add(event);
       }
     }
     return selectedEvents;
   }
 
   /**
    * Getter for the elements that shall be viewed as available items in the procedureSelector..
    *
    * @return available elements.
    */
   protected abstract List<Procedure> getProcedureModelItems();
 
   /**
    * Getter for the elements that shall be viewed as selected items in the procedureSelector.
    *
    * @return selected elements.
    */
   protected abstract List<Procedure> getProcedureSelectedItems();
 
   /**
    * Getter for the elements that shall be viewed as available items in the eventSelector.
    *
    * @return available elements.
    */
   protected abstract List<Event> getEventModelItems();
 
   /**
    * Getter for the elements that shall be viewed as selected items in the eventSelector.
    *
    * @return selected elements.
    */
   protected abstract List<Event> getEventSelectedItems();
 
   /**
    * Method that validates the input values.
    *
    * @param name       name of the {@link hsa.awp.campaign.model.Campaign}.
    * @param events     selected events of the {@link hsa.awp.campaign.model.Campaign}.
    * @param procedures selected procedures of the {@link hsa.awp.campaign.model.Campaign}.
    * @param startShow  startShow of the {@link hsa.awp.campaign.model.Campaign}.
    * @param endShow    endShow of the {@link hsa.awp.campaign.model.Campaign}.
    * @return List of errors. If no errors occured, the list will be empty (size = 0)
    */
   protected List<String> validate(String name, List<Event> events, List<Procedure> procedures, Date startShow, Date endShow, List<StudyCourse> studyCourses) {
 
     List<String> error = new LinkedList<String>();
 
     if (name == null || name.equals("")) {
       error.add("Bitte einen Namen eingeben.");
     } else {
       Campaign found = getController().getCampaignByNameAndMandator(name, getSession());
       if (found != null && !found.getId().equals(getCampaign().getId())) {
         error.add("Der Name existiert bereits.");
       }
     }
 
     if (startShow == null) {
       error.add("Kein Startdatum eingegeben");
     }
     if (endShow == null) {
       error.add("Kein Enddatum eingegeben");
     }
     if (endShow != null && startShow != null && startShow.compareTo(endShow) >= 0) {
       error.add("Startdatum muss vor dem Enddatum sein.");
     }
 
     if (studyCourses.size() <= 0) {
       error.add("Keinen Studiengang ausgewählt");
     }
 
     List<ErrorProcedureElement> procErrors = new LinkedList<ErrorProcedureElement>();
     for (Procedure proc : procedures) {
       for (Procedure innerProc : procedures) {
         if (proc.equals(innerProc)) {
           continue;
         }
 
         ErrorProcedureElement errorTemp = new ErrorProcedureElement(proc, innerProc);
         if (proc.isOverlapping(innerProc) && !procErrors.contains(errorTemp)) {
           error.add(proc.getName() + " is overlapping " + innerProc.getName());
           procErrors.add(errorTemp);
         }
       }
     }
 
     return error;
   }
 
   /**
    * Method that will be called when the validation was successful.
    *
    * @param name         name of the {@link hsa.awp.campaign.model.Campaign}.
    * @param events       selected events of the {@link hsa.awp.campaign.model.Campaign}.
    * @param procedures   selected procedures of the {@link hsa.awp.campaign.model.Campaign}.
    * @param startShow    startShow of the {@link hsa.awp.campaign.model.Campaign}.
    * @param endShow      endShow of the {@link hsa.awp.campaign.model.Campaign}.
    * @param studyCourses
    */
   protected abstract void workResult(String name, String email, List<Event> events, List<Procedure> procedures, Calendar startShow,
                                      Calendar endShow, List<StudyCourse> studyCourses, String detailText);
 
   /**
    * Getter for the text that shall be viewed on success.
    *
    * @return success text.
    */
   protected abstract String getSuccessText();
 
   /**
    * Dummy element for saving already overlapping Procedures.
    *
    * @author klassm
    */
   private class ErrorProcedureElement {
     /**
      * First {@link Procedure}.
      */
     private Procedure item1;
 
     /**
      * Second {@link Procedure}.
      */
     private Procedure item2;
 
     /**
      * Creates a new {@link ErrorProcedureElement}.
      *
      * @param item1 first {@link Procedure}.
      * @param item2 second {@link Procedure}.
      */
     public ErrorProcedureElement(Procedure item1, Procedure item2) {
 
       this.item1 = item1;
       this.item2 = item2;
     }
 
     @Override
     public boolean equals(Object o) {
 
       if (!(o instanceof ErrorProcedureElement)) {
         return false;
       }
       ErrorProcedureElement error = (ErrorProcedureElement) o;
 
       if (error.item1.equals(item1) && error.item2.equals(item2)) {
         return true;
       } else if (error.item1.equals(item2) && error.item2.equals(item1)) {
         return true;
       }
       return false;
     }
 
     @Override
     public int hashCode() {
 
       return super.hashCode();
     }
   }
 
   /**
    * {@link AbstractSortedListSelectorPanel} for {@link Event}s.
    *
    * @author klassm
    */
   private class EventListSelectorPanel extends AbstractSortedListSelectorPanel<Event> {
     /**
      * unique serialization id.
      */
     private static final long serialVersionUID = -601669556663027272L;
 
     /**
      * Constructor for {@link EventListSelectorPanel}.
      *
      * @param id       wicket id.
      * @param list     list of elements to add.
      * @param selected selected elements.
      */
     public EventListSelectorPanel(String id, List<Event> list, List<Event> selected) {
 
       super(id, list, selected);
     }
 
     @Override
     protected Comparator<Event> getComparator() {
      return EventSorter.alphabeticalEventName();
     }
 
     @Override
     public String renderName(Event event) {
 
       if (event.getSubject() == null) {
         throw new IllegalArgumentException("event requires a Subject!");
       }
       return event.getSubject().getName() + " " + formatDetailInformation(event);
     }
 
     @Override
     public String renderPrefix(Event event) {
 
       return formatEventId(event);
     }
   }
 
   /**
    * {@link AbstractSortedListSelectorPanel} for {@link Event}s.
    *
    * @author klassm
    */
   private class StudyCourseListSelectorPanel extends AbstractSortedListSelectorPanel<StudyCourse> {
 
     /**
      * {@link Comparator} determining the order of elements.
      */
     private transient Comparator<StudyCourse> comparator;
 
     /**
      * Constructor for {@link EventListSelectorPanel}.
      *
      * @param id       wicket id.
      * @param list     list of elements to add.
      * @param selected selected elements.
      */
     public StudyCourseListSelectorPanel(String id, List<StudyCourse> list, List<StudyCourse> selected) {
 
       super(id, list, selected);
     }
 
     @Override
     protected Comparator<StudyCourse> getComparator() {
 
       if (comparator == null) {
         comparator = new Comparator<StudyCourse>() {
           @Override
           public int compare(StudyCourse o1, StudyCourse o2) {
 
             return o1.getName().compareTo(o2.getName());
           }
         };
       }
       return comparator;
     }
 
     @Override
     public String renderName(StudyCourse object) {
 
       return object.getName();
     }
   }
 
   /**
    * {@link AbstractSortedListSelectorPanel} for {@link Procedure}s.
    *
    * @author klassm
    */
   private class ProcedureListSelectorPanel extends AbstractSortedListSelectorPanel<Procedure> {
     /**
      * unique serialization id.
      */
     private static final long serialVersionUID = 5002964343426885938L;
 
     /**
      * {@link Comparator} determining the order of elements.
      */
     private transient Comparator<Procedure> comparator;
 
     /**
      * Constructor for {@link ProcedureListSelectorPanel}.
      *
      * @param id       wicket id.
      * @param list     list of elements to add.
      * @param selected selected elements.
      */
     public ProcedureListSelectorPanel(String id, List<Procedure> list, List<Procedure> selected) {
 
       super(id, list, selected);
     }
 
     @Override
     protected Comparator<Procedure> getComparator() {
 
       if (comparator == null) {
         comparator = new Comparator<Procedure>() {
           @Override
           public int compare(Procedure o1, Procedure o2) {
 
             return o1.getStartDate().compareTo(o2.getStartDate());
           }
         };
       }
       return comparator;
     }
 
     @Override
     public String renderDescription(Procedure object) {
 
       SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
       StringBuffer sb = new StringBuffer();
 
       sb.append(format.format(object.getStartDate().getTime()));
       sb.append(" bis ");
       sb.append(format.format(object.getEndDate().getTime()));
 
       return sb.toString();
     }
 
     @Override
     public String renderName(Procedure object) {
 
       return object.getName();
     }
 
     @Override
     public boolean vetoUnselect(Procedure object) {
 
       Calendar now = Calendar.getInstance();
       if (object.getStartDate().before(now)) {
         return true;
       }
       return false;
     }
 
     @Override
     public boolean vetoSelect(Procedure object) {
       // veto if the newly selected Procedure overlaps an already selected one.
       for (Procedure proc : procedureListSelector.getSelected()) {
         if (proc.isOverlapping(object)) {
           return true;
         }
       }
       return false;
     }
   }
 }
