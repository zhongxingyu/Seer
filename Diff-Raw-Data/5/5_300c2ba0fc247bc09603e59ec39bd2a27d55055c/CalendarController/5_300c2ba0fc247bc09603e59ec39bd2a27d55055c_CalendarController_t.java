 /**
  *
  */
 package org.esupportail.opi.web.controllers.references;
 
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.beans.NormeSI;
 import org.esupportail.opi.domain.beans.references.calendar.Calendar;
 import org.esupportail.opi.domain.beans.references.calendar.CalendarCmi;
 import org.esupportail.opi.domain.beans.references.calendar.CalendarIns;
 import org.esupportail.opi.domain.beans.references.calendar.ReunionCmi;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.web.beans.BeanCalendar;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.beanEnum.WayfEnum;
 import org.esupportail.opi.web.beans.pojo.CalendarRechPojo;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.AbstractContextAwareController;
 import org.springframework.util.StringUtils;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 import java.util.*;
 import java.util.Map.Entry;
 
 
 /**
  * @author cleprous
  */
 public class CalendarController extends AbstractContextAwareController {
 
 
     /**
      * The serialization id.
      */
     private static final long serialVersionUID = -385061645426193790L;
 
 	
 	/*
      ******************* PROPERTIES ******************* */
 
 
     /**
      * The beanCalendar.
      */
     private BeanCalendar beanCalendar;
 
     /**
      * The actionEnum.
      */
     private ActionEnum actionEnum;
 
     /**
      * All calendars.
      */
 
     private List<Calendar> calendars;
 
     /**
      * Filtered calendars.
      */
     private List<Calendar> filteredCalendars;
 
     /**
      * The key is type of calendar,
      * and the value is label for this type.
      */
     private Map<String, String> calendarType;
 
     /**
      * Pojo for the search of calendars.
      */
     private CalendarRechPojo calendarRechPojo;
 
     /**
      * THe reunionCmi.
      */
     private List<ReunionCmi> reunions;
 
     /**
      * The reunion to remove in reunions.
      */
     private ReunionCmi reunionToRemove;
 
     /**
      * The CommissionController.
      */
     private CommissionController commissionController;
 
     /**
      * A logger.
      */
     private final Logger log = new LoggerImpl(getClass());
 	
 	/*
 	 ******************* INIT ************************* */
 
 
     /**
      * Constructors.
      */
     public CalendarController() {
         super();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
      */
     @Override
     public void reset() {
         super.reset();
         beanCalendar = new BeanCalendar();
         calendars = new ArrayList<Calendar>();
         calendarRechPojo = new CalendarRechPojo();
         actionEnum = new ActionEnum();
         reunions = new ArrayList<ReunionCmi>();
         reunionToRemove = null;
 
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#afterPropertiesSetInternal()
      */
     @Override
     public void afterPropertiesSetInternal() {
         Assert.notNull(this.commissionController,
                 "property commissionController of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.calendarType,
                 "property calendarType of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notEmpty(this.calendarType,
                 "property calendarType of class " + this.getClass().getName()
                         + " can not be empty");
         reset();
     }
 	
 	/*
 	 ******************* CALLBACK ********************** */
 
     /**
      * Callback to calendar list.
      *
      * @return String
      */
     public String goSeeAllCal() {
         reset();
         calendars = getParameterService().getCalendars(null, null);
         Collections.sort(calendars, new ComparatorString(Calendar.class));
         return NavigationRulesConst.MANAGED_CAL;
     }
 
     /**
      * Callback to calendar add.
      *
      * @return String
      */
     public String goAddCall() {
         reset();
         commissionController.reset();
         commissionController.getWayfEnum().setWhereAreYouFrom(WayfEnum.CALENDAR_VALUE);
        commissionController.getActionEnum().setWhatAction(ActionEnum.SEE_SELECT_CMI);
         actionEnum.setWhatAction(ActionEnum.ADD_ACTION);
         return NavigationRulesConst.ADD_CAL;
     }
 
     /**
      * Callback to update calendar.
      *
      * @return String
      */
     public String goUpdateCal() {
         commissionController.reset();
         if (beanCalendar.getCalendar() instanceof CalendarIns) {
             CalendarIns cal = (CalendarIns) beanCalendar.getCalendar();
             //initialize the proxy hibernate
 			getDomainService().initOneProxyHib(cal, cal.getCommissions(), Set.class);
             commissionController.setSelectedCommissions(new ArrayList<Commission>(cal.getCommissions()));
         } else if (beanCalendar.getCalendar() instanceof CalendarCmi) {
             CalendarCmi cal = (CalendarCmi) beanCalendar.getCalendar();
             beanCalendar.setTypeSelected(getTypCalCommission());
             setReunions(new ArrayList<ReunionCmi>(cal.getReunions()));
             if (cal.getCommission() != null) {
                 //initialize the proxy hibernate
 				getDomainService().initOneProxyHib(cal, cal.getCommission(), Set.class);
                 commissionController.getCommission().setId(cal.getCommission().getId());
             }
         }
 
         commissionController.getWayfEnum().setWhereAreYouFrom(WayfEnum.CALENDAR_VALUE);
         return NavigationRulesConst.UPDATE_CAL;
     }
 
     /**
      * Callback to calendar delete.
      *
      * @return
      */
     public String goDeleteCal() {
         if (beanCalendar.getCalendar() instanceof CalendarCmi) {
             CalendarCmi calCmi = (CalendarCmi) beanCalendar.getCalendar();
             if (calCmi.getCommission() != null) {
                 addErrorMessage(null, "COMMISSION.CAL_WITH_COMM");
                 return null;
             }
         }
         actionEnum.setWhatAction(ActionEnum.DELETE_ACTION);
         return null;
     }
 
     /**
      * Callback to see a calendar.
      *
      * @return String
      */
     public String goSeeOneCal() {
         if (beanCalendar.getCalendar() instanceof CalendarIns) {
             CalendarIns cal = (CalendarIns) beanCalendar.getCalendar();
 
             //initialize the proxy hibernate
 			getDomainService().initOneProxyHib(cal, cal.getCommissions(), Set.class);
             commissionController.setSelectedCommissions(new ArrayList<Commission>(cal.getCommissions()));
 
         } else if (beanCalendar.getCalendar() instanceof CalendarCmi) {
             CalendarCmi cal = (CalendarCmi) beanCalendar.getCalendar();
             //initialize the proxy hibernate
 			getDomainService().initOneProxyHib(cal, cal.getCommission(), Commission.class);
             setReunions(new ArrayList<ReunionCmi>(cal.getReunions()));
         }
         return NavigationRulesConst.SEE_ONE_CAL;
     }
 
 
     /**
      * @return String
      */
     public String goSeeCalCmi() {
         if (commissionController.getCommission().getCalendarCmi() != null) {
 
 			getDomainService().initOneProxyHib(
 					commissionController.getCommission(), 
 					commissionController.getCommission().getCalendarCmi(), CalendarCmi.class);
             CalendarCmi cal = commissionController.getCommission().getCalendarCmi();
             //initialize the proxy hibernate
             getDomainService().initOneProxyHib(cal, cal.getCommission(), Commission.class);
             setReunions(new ArrayList<ReunionCmi>(cal.getReunions()));
 
             beanCalendar.setCalendar(cal);
             return NavigationRulesConst.SEE_CAL_CMI;
         }
         addInfoMessage(null, "COMMISSION.WITHOUT.CAL_CMI");
         return null;
 
     }
 	
 	/*
 	 ******************* METHODS ********************** */
 
 
     /**
      * Items of all type calendar.
      *
      * @return List< SelectItem>
      */
     public List<SelectItem> getTypeCalItems() {
         List<SelectItem> list = new ArrayList<SelectItem>();
         for (String type : calendarType.keySet()) {
             list.add(new SelectItem(type, calendarType.get(type)));
         }
         return list;
     }
 
     /**
      * Add a calendar to the dataBase.
      *
      * @return String
      */
     public String add() {
         if (log.isDebugEnabled()) {
             log.debug("enterind add with calendar = " + beanCalendar);
         }
         if (ctrlEnter(beanCalendar.getCalendar())) {
             beanCalendar.setCalendar(
                     (Calendar) getDomainService().add(
                             beanCalendar.getCalendar(), getCurrentGest().getLogin()));
 
 
             if (beanCalendar.getCalendar() instanceof CalendarIns) {
                 CalendarIns c = (CalendarIns) beanCalendar.getCalendar();
                 c.setCommissions(new HashSet<Commission>(commissionController.getSelectedCommissions()));
 
 
                 getParameterService().addCalendar(beanCalendar.getCalendar());
             } else if (beanCalendar.getCalendar() instanceof CalendarCmi) {
                 getParameterService().addCalendar(beanCalendar.getCalendar());
                 CalendarCmi c = (CalendarCmi) beanCalendar.getCalendar();
 
                 for (ReunionCmi r : reunions) {
                     if (ctrlReunionCmi(r)) {
                         ReunionCmi reu = (ReunionCmi) getDomainService().add(
                                 r, getCurrentGest().getLogin());
                         reu.setCalendar(c);
                         getParameterService().addReunionCmi(reu);
                     }
                 }
 
                 commissionController.setCommission(
                         getParameterService().getCommission(
                                 commissionController.getCommission().getId(), null));
                 commissionController.getCommission().setCalendarCmi(c);
                 getParameterService().updateCommission((Commission) getDomainService()
                         .update(commissionController.getCommission(),
                                 getCurrentGest().getLogin()));
             }
 
 
             reset();
             commissionController.reset();
             addInfoMessage(null, "INFO.ENTER.SUCCESS");
             return NavigationRulesConst.MANAGED_CAL;
         }
         if (log.isDebugEnabled()) {
             log.debug("leaving add");
         }
         return null;
 
     }
 
     /**
      * Update a calendar to the dataBase.
      *
      * @return String
      */
     public String update() {
         if (log.isDebugEnabled()) {
             log.debug("enterind update with calendar = " + beanCalendar);
         }
         if (ctrlEnter(beanCalendar.getCalendar())) {
 
             //update cmi
             beanCalendar.setCalendar(
                     (Calendar) getDomainService().update(
                             beanCalendar.getCalendar(), getCurrentGest().getLogin()));
 
 
             if (beanCalendar.getCalendar() instanceof CalendarIns) {
                 CalendarIns c = (CalendarIns) beanCalendar.getCalendar();
                 c.setCommissions(new HashSet<Commission>(commissionController.getSelectedCommissions()));
 
                 getParameterService().updateCalendar(beanCalendar.getCalendar());
 
             } else if (beanCalendar.getCalendar() instanceof CalendarCmi) {
                 CalendarCmi c = (CalendarCmi) beanCalendar.getCalendar();
 
                 List<ReunionCmi> reuToDelete = new ArrayList<ReunionCmi>();
                 for (ReunionCmi r : c.getReunions()) {
                     if (!reunions.contains(r)) {
                         reuToDelete.add(r);
                     }
                 }
 
                 for (ReunionCmi r : reunions) {
                     if (ctrlReunionCmi(r)) {
                         if (r.getId().equals(0)) {
                             ReunionCmi reu = (ReunionCmi) getDomainService().add(
                                     r, getCurrentGest().getLogin());
                             reu.setCalendar(c);
                             getParameterService().addReunionCmi(reu);
                         } else {
                             ReunionCmi reu = (ReunionCmi) getDomainService().update(
                                     r, getCurrentGest().getLogin());
                             reu.setCalendar(c);
                             getParameterService().updateReunionCmi(reu);
                         }
                     }
                 }
 
                 c.getReunions().removeAll(reuToDelete);
                 getParameterService().deleteReunionCmi(reuToDelete);
 
                 // mise à jour de la commission sélectionné
 //				commissionController.setCommission(
 //						getParameterService().getCommission(
 //								commissionController.getCommission().getId(), null));
 //				commissionController.getCommission().setCalendarCmi(c);
 //				getParameterService().updateCommission((Commission) getDomainService()
 //						.update(commissionController.getCommission(), 
 //								getCurrentGest().getLogin()));
 
                 getParameterService().updateCalendar(c);
 
                 // si changement de commission, maj de celle ci
 //				if (!c.getCommission().getId().equals(commissionController.getCommission().getId())) {
 //					Commission comm = c.getCommission();
 //					comm.setCalendarCmi(null);
 //					getParameterService().updateCommission((Commission) getDomainService()
 //							.update(comm, getCurrentGest().getLogin()));
 //				}
             }
 
 
             reset();
             addInfoMessage(null, "INFO.ENTER.SUCCESS");
             return NavigationRulesConst.MANAGED_CAL;
         }
 
         if (log.isDebugEnabled()) {
             log.debug("leaving update");
         }
         return null;
 
     }
 
 
     /**
      * Delete a calendar to the dataBase.
      */
     public void delete() {
         if (log.isDebugEnabled()) {
             log.debug("enterind delete with calendar = " + beanCalendar);
         }
 
         getParameterService().deleteCalendar(beanCalendar.getCalendar());
         reset();
 
         addInfoMessage(null, "INFO.DELETE.SUCCESS");
 
         if (log.isDebugEnabled()) {
             log.debug("leaving delete");
         }
     }
 
 
     /**
      * The selected calendar type.
      *
      * @param event
      */
     public void selectType(final ValueChangeEvent event) {
 
         String value = (String) event.getNewValue();
         beanCalendar.setTypeSelected(value);
         selectType();
         FacesContext.getCurrentInstance().renderResponse();
 
     }
 
     /**
      * The selected calendar type.
      */
     public void selectType() {
         if (beanCalendar.getTypeSelected().equals(Calendar.TYPE_CAL_INSCRIPTION)) {
             beanCalendar.setCalendar(new CalendarIns());
         } else if (beanCalendar.getTypeSelected().equals(Calendar.TYPE_CAL_COMMISSION)) {
             beanCalendar.setCalendar(new CalendarCmi());
             addReunion();
         }
     }
 
 
     /**
      * Add a reunion in attribut reunions.
      */
     public void addReunion() {
         reunions.add(new ReunionCmi());
     }
 
     /**
      * Remove a reunion in attribut reunions.
      */
     public void removeReunion() {
         reunions.remove(reunionToRemove);
     }
 	
 	/* ### ALL CONTROL ####*/
 
     /**
      * Control Commission attributes for the adding and updating.
      *
      * @param c
      * @return Boolean
      */
     private Boolean ctrlEnter(final Calendar c) {
         Boolean ctrlOk = true;
         if (!StringUtils.hasText(c.getCode())) {
             addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.CODE"));
             ctrlOk = false;
         } else {
             if (!getParameterService().calendarCodeIsUnique(c)) {
                 ctrlOk = false;
                 addErrorMessage(null, "ERROR.FIELD.NOT_UNIQUE", getString("FIELD_LABEL.CODE"));
             }
         }
         if (!StringUtils.hasText(c.getLibelle())) {
             addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.LONG_LIB"));
             ctrlOk = false;
         }
         if (beanCalendar.getCalendar() instanceof CalendarIns) {
             CalendarIns calIns = (CalendarIns) beanCalendar.getCalendar();
             if (commissionController.getSelectedCommissions().isEmpty()) {
                 addErrorMessage(null, "ERROR.LIST.EMPTY", getString("COMMISSIONS"));
                 ctrlOk = false;
             }
             if (calIns.getStartDate() == null) {
                 addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.START_DATE"));
                 ctrlOk = false;
             }
             if (calIns.getEndDate() == null) {
                 addErrorMessage(null, Constantes.I18N_EMPTY, getString("FIELD_LABEL.END_DATE"));
                 ctrlOk = false;
             }
             if (ctrlOk) {
                 if (calIns.getEndDate().before(calIns.getStartDate())) {
                     addErrorMessage(null, "ERROR.FIELD.DAT_FIN.VAL");
                     ctrlOk = false;
                 }
             }
 
 
         } else if (beanCalendar.getCalendar() instanceof CalendarCmi) {
             CalendarCmi calcmi = (CalendarCmi) beanCalendar.getCalendar();
             Integer idCmi = commissionController.getCommission().getId();
             if (idCmi == null || idCmi.equals(0)) {
                 addErrorMessage(null, Constantes.I18N_EMPTY, getString("COMMISSIONS"));
                 ctrlOk = false;
             }
 //			Comment 01/03/2010 : A la création, peut être vide
 //			if (calcmi.getEndDatConfRes() == null) {
 //				addErrorMessage(null, Constantes.I18N_EMPTY, 
 //								getString("CALENDAR.CMI.END_DAT_CONF_RES"));
 //				ctrlOk = false;
 //			}
             if (calcmi.getDatEndBackDossier() == null) {
                 addErrorMessage(null, Constantes.I18N_EMPTY,
                         getString("COMMISSION.DAT_END_BACK_DOS_SHORT"));
                 ctrlOk = false;
             }
         }
 
         if (log.isDebugEnabled()) {
             log.debug("leaving ctrlAdd return = " + ctrlOk);
         }
         return ctrlOk;
     }
 
     /**
      * @param reunion
      * @return Boolean true if reunion one field is not empty.
      */
     private Boolean ctrlReunionCmi(final ReunionCmi reunion) {
         if (reunion.getDate() == null
                 && reunion.getHeure() == null
                 && !StringUtils.hasText(reunion.getLieu())) {
             return false;
         }
         return true;
     }
 
     /**
      * @return a list of {@link SelectItem} for each calendar type.
      */
     public List<SelectItem> getCalendarTypes() {
         List<SelectItem> result = new ArrayList<SelectItem>();
         result.add(new SelectItem("", ""));
         for (Entry<String, String> entry : getCalendarType().entrySet()) {
             result.add(new SelectItem(entry.getKey(), entry.getValue()));
         }
         return result;
     }
 	
 	/*
 	 ******************* ACCESSORS ******************** */
 
 
     /**
      * Defaut date to calendar cmi.
      *
      * @return String
      */
     public String getDateBackDefault() {
         return getParameterService().getDateBackDefault();
     }
 
     /**
      * All calendars.
      *
      * @return List < Calendar >
      */
     public List<Calendar> getCalendars() {
         if (calendars == null || calendars.isEmpty()) {
             calendars = getParameterService().getCalendars(null, null);
             Collections.sort(calendars, new ComparatorString(Calendar.class));
         }
         return calendars;
     }
 
     /**
      * @return the filteredCalendars
      */
     public List<Calendar> getFilteredCalendars() {
         return filteredCalendars;
     }
 
     /**
      * @param filteredCalendars the filteredCalendars to set
      */
     public void setFilteredCalendars(List<Calendar> filteredCalendars) {
         this.filteredCalendars = filteredCalendars;
     }
 
     /**
      * List of commissions without calendarCmi by right in currentUser.
      *
      * @return Set< Commission>
      */
     public Set<Commission> getCmiWithoutCalCmiItems() {
         Set<Commission> c = commissionController.getAllCommissionsItemsByRight();
         Set<Commission> co = new TreeSet<Commission>(new ComparatorString(NormeSI.class));
         for (Commission cmi : c) {
             if (cmi.getCalendarCmi() == null) {
                 co.add(cmi);
             }
         }
         Integer idCmi = commissionController.getCommission().getId();
         if (idCmi != null && idCmi != 0) {
             //on rajoute la commission du calendrier en modif
             co.add(getParameterService().getCommission(idCmi, null));
         }
         return co;
     }
 
 
     /**
      * @return the beanCalendar
      */
     public BeanCalendar getBeanCalendar() {
         return beanCalendar;
     }
 
     /**
      * @param calendar the beanCalendar to set
      */
     public void setBeanCalendar(final BeanCalendar calendar) {
         this.beanCalendar = calendar;
     }
 
     /**
      * @return the actionEnum
      */
     public ActionEnum getActionEnum() {
         return actionEnum;
     }
 
     /**
      * @param actionEnum the actionEnum to set
      */
     public void setActionEnum(final ActionEnum actionEnum) {
         this.actionEnum = actionEnum;
     }
 
     /**
      * @return the calendarType
      */
     public Map<String, String> getCalendarType() {
         return calendarType;
     }
 
     /**
      * @param calendarType the calendarType to set
      */
     public void setCalendarType(final Map<String, String> calendarType) {
         this.calendarType = calendarType;
     }
 
 
     /**
      * @param commissionController the commissionController to set
      */
     public void setCommissionController(final CommissionController commissionController) {
         this.commissionController = commissionController;
     }
 
     /**
      * @return the calendarRechPojo
      */
     public CalendarRechPojo getCalendarRechPojo() {
         return calendarRechPojo;
     }
 
     /**
      * @param calendarRechPojo the calendarRechPojo to set
      */
     public void setCalendarRechPojo(final CalendarRechPojo calendarRechPojo) {
         this.calendarRechPojo = calendarRechPojo;
     }
 
     /**
      * @return the reunions
      */
     public List<ReunionCmi> getReunions() {
         return reunions;
     }
 
     /**
      * @param reunions the reunions to set
      */
     public void setReunions(final List<ReunionCmi> reunions) {
         this.reunions = reunions;
     }
 
 	
 	/*----------------------------------------
 	 *  GETTERS POUR JSF
 	 */
 
     /**
      * @return the TYPE_CAL_INSCRIPTION
      */
     public String getTypCalInscription() {
         return Calendar.TYPE_CAL_INSCRIPTION;
     }
 
     /**
      * @return the MUST_BE_ADD_GEST
      */
     public String getTypCalCommission() {
         return Calendar.TYPE_CAL_COMMISSION;
     }
 
     /**
      * @return the reunionToRemove
      */
     public ReunionCmi getReunionToRemove() {
         return reunionToRemove;
     }
 
     /**
      * @param reunionToRemove the reunionToRemove to set
      */
     public void setReunionToRemove(final ReunionCmi reunionToRemove) {
         this.reunionToRemove = reunionToRemove;
     }
 
 
}
