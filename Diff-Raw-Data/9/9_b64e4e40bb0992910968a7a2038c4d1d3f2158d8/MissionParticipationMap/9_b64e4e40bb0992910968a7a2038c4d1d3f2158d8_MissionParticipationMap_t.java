 /*
  * @(#)MissionParticipationMap.java
  *
  * Copyright 2011 Instituto Superior Tecnico
  * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Expenditure Tracking Module.
  *
  *   The Expenditure Tracking Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package module.mission.presentationTier.component;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import module.mission.domain.Mission;
 import module.mission.domain.MissionProcess;
 import module.mission.domain.MissionSystem;
 import module.mission.domain.MissionYear;
 import module.mission.presentationTier.action.MissionOrganizationAction;
 import module.organization.domain.Accountability;
 import module.organization.domain.AccountabilityType;
 import module.organization.domain.Person;
 import module.organization.domain.Unit;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import pt.ist.fenixframework.FenixFramework;
 import pt.ist.vaadinframework.annotation.EmbeddedComponent;
 import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
 import pt.utl.ist.fenix.tools.util.excel.Spreadsheet;
import pt.utl.ist.fenix.tools.util.i18n.Language;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.terminal.Resource;
 import com.vaadin.terminal.StreamResource;
 import com.vaadin.terminal.StreamResource.StreamSource;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.InlineDateField;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Select;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window.Notification;
 import com.vaadin.ui.themes.BaseTheme;
 
 @SuppressWarnings("serial")
 @EmbeddedComponent(path = { "MissionParticipationMap" }, args = { "unit", "year", "month" })
 /**
  * 
  * @author Pedro Santos
  * @author Sérgio Silva
  * @author Luis Cruz
  * 
  */
 public class MissionParticipationMap extends CustomComponent implements EmbeddedComponentContainer {
 
     private class ContextForm extends Form {
 
         private class FormButtons extends HorizontalLayout {
 
             private class SubmitButton extends Button implements Button.ClickListener {
 
                 private SubmitButton() {
                     super(MissionSystem.getMessage("label.submit"));
                     final ClickListener listener = this;
                     addListener(listener);
                 }
 
                 @Override
                 public void buttonClick(final ClickEvent event) {
                     final Date date = (Date) yearMonthField.getValue();
                     final Calendar calendar = Calendar.getInstance();
                     calendar.setTime(date);
                     year = calendar.get(Calendar.YEAR);
                     month = calendar.get(Calendar.MONTH) + 1;
 
                     final Collection<AccountabilityType> selectedAccountabilityTypes =
                             (Collection<AccountabilityType>) accountabilityTypesField.getValue();
                     accountabilityTypes.clear();
                     accountabilityTypes.addAll(selectedAccountabilityTypes);
 
                     resetTimeTable();
                 }
             }
 
             private FormButtons() {
                 setSpacing(true);
                 setMargin(false);
                 addComponent(new SubmitButton());
             }
         }
 
         private final InlineDateField yearMonthField = new InlineDateField(MissionSystem.getMessage("label.select.year.month"));
         private final ListSelect accountabilityTypesField = new ListSelect(
                 MissionSystem.getMessage("label.select.accountabilityTypes"));
 
         private ContextForm() {
             setWriteThrough(false);
 
             yearMonthField.setValue(new DateTime(year, month, 1, 0, 0, 0, 0).toDate());
             yearMonthField.setResolution(InlineDateField.RESOLUTION_MONTH);
             addField("yearMonth", yearMonthField);
 
             accountabilityTypesField.addContainerProperty("name", String.class, null);
            accountabilityTypesField.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
            accountabilityTypesField.setItemCaptionPropertyId("name");
             final Set<AccountabilityType> accountabilityTypes =
                     MissionSystem.getInstance().getAccountabilityTypesRequireingAuthorization();
             for (final AccountabilityType accountabilityType : accountabilityTypes) {
                 final Item item = accountabilityTypesField.addItem(accountabilityType);
                 final Property itemProperty = item.getItemProperty("name");
                itemProperty.setValue(accountabilityType.getName().getContent(Language.getLanguage()));
             }
             accountabilityTypesField.setRows(accountabilityTypes.size());
             accountabilityTypesField.setNullSelectionAllowed(false);
             accountabilityTypesField.setMultiSelect(true);
             accountabilityTypesField.setValue(Collections.unmodifiableSet(accountabilityTypes));
             addField("accountabilityTypes", accountabilityTypesField);
         }
 
         private FormButtons getFormButtons() {
             return new FormButtons();
         }
 
     }
 
     private class ExportButton extends Button implements Button.ClickListener, StreamSource {
 
         private ExportButton() {
             super(MissionSystem.getMessage("label.export"));
             setStyleName(BaseTheme.BUTTON_LINK);
             final ClickListener listener = this;
             addListener(listener);
             final Resource icon = new ThemeResource("../../../images/fileManagement/fileicons/odc.png");
             setIcon(icon);
         }
 
         @Override
         public InputStream getStream() {
             final Spreadsheet spreadsheet = timeTable.export();
             final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             try {
                 spreadsheet.exportToXLSSheet(outputStream);
             } catch (final IOException e) {
                 throw new Error(e);
             }
             return new ByteArrayInputStream(outputStream.toByteArray());
         }
 
         @Override
         public void buttonClick(final ClickEvent event) {
             final ExportButton streamSource = this;
             final String filename =
                     MissionSystem.getMessage("label.excel.mission.filename", Integer.toString(year), Integer.toString(month));
             final StreamResource resource = new StreamResource(streamSource, filename, getApplication());
             resource.setMIMEType("application/vnd.ms-excel");
             getWindow().open(resource);
         }
 
     }
 
     private Unit unit;
     private int year;
     private int month;
     private final List<AccountabilityType> accountabilityTypes = new ArrayList<AccountabilityType>();
     private final boolean includeSubUnits = true;
 
     private TimeTable timeTable = null;
 
     public MissionParticipationMap() {
         accountabilityTypes.addAll(MissionSystem.getInstance().getAccountabilityTypesRequireingAuthorization());
     }
 
     @Override
     public boolean isAllowedToOpen(Map<String, String> arguments) {
         return true;
     }
 
     @Override
     public void setArguments(Map<String, String> arguments) {
         final String unitOID = arguments.get("unit");
         final String yearArg = arguments.get("year");
         final String monthArg = arguments.get("month");
 
         unit = FenixFramework.getDomainObject(unitOID);
 
         if (unit == null) {
             return;
         }
 
         year = StringUtils.isEmpty(yearArg) ? new DateTime().getYear() : Integer.parseInt(yearArg);
         month = StringUtils.isEmpty(monthArg) ? new DateTime().getMonthOfYear() : Integer.parseInt(monthArg);
     }
 
     @Override
     public void attach() {
         super.attach();
 
         final VerticalLayout layout = new VerticalLayout();
         setCompositionRoot(layout);
         layout.setSpacing(false);
         layout.setMargin(false);
 
         final Label title = new Label("<h2>" + unit.getPresentationName() + "</h2>", Label.CONTENT_XHTML);
         layout.addComponent(title);
 
         if (MissionOrganizationAction.hasPermission(unit)) {
             final ContextForm contextForm = new ContextForm();
             layout.addComponent(contextForm);
 
             final ContextForm.FormButtons formButtons = contextForm.getFormButtons();
             layout.addComponent(formButtons);
 
             final ExportButton exportButton = new ExportButton();
             layout.addComponent(exportButton);
             layout.setComponentAlignment(exportButton, Alignment.MIDDLE_RIGHT);
 
             setTimeTable();
         } else {
             getWindow().showNotification(MissionSystem.getMessage("label.not.authorized"), null,
                     Notification.TYPE_WARNING_MESSAGE);
         }
     }
 
     private void resetTimeTable() {
         final VerticalLayout layout = (VerticalLayout) getCompositionRoot();
         layout.removeComponent(timeTable);
         setTimeTable();
     }
 
     private void setTimeTable() {
         final VerticalLayout layout = (VerticalLayout) getCompositionRoot();
         timeTable = new TimeTable(year, month, "");
         fillTableInfo(timeTable);
         layout.addComponent(timeTable);
     }
 
     private void fillTableInfo(final TimeTable timeTable) {
         final MissionYear missionYear = MissionYear.findMissionYear(year);
         for (final MissionProcess missionProcess : missionYear.getMissionProcessSet()) {
             if (!missionProcess.isCanceled()) {
                 final Mission mission = missionProcess.getMission();
                 final LocalDate daparture = mission.getDaparture().toLocalDate();
                 final LocalDate arrival = mission.getArrival().toLocalDate();
                 final LocalDate nextDay = arrival.plusDays(1);
                 if (containsMission(daparture, arrival)) {
                     if (missionProcess.areAllParticipantsAuthorized()) {
                         for (LocalDate localDate = daparture; localDate.isBefore(nextDay); localDate = localDate.plusDays(1)) {
                             if (containsDay(localDate)) {
                                 for (final Person person : mission.getParticipantesSet()) {
                                     if (hasParentUnit(person, localDate)) {
                                         timeTable.fillSlot(person.getPartyName().getContent(), localDate.getDayOfMonth());
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private boolean hasParentUnit(final Person person, final LocalDate localDate) {
         for (final Accountability accountability : person.getParentAccountabilitiesSet()) {
             if (accountability.contains(localDate)) {
                 final AccountabilityType accountabilityType = accountability.getAccountabilityType();
                 if (accountabilityTypes.contains(accountabilityType)) {
                     final Unit unit = (Unit) accountability.getParent();
                     if (hasParentUnit(unit, localDate)) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     private boolean hasParentUnit(final Unit unit, final LocalDate localDate) {
         if (unit == this.unit) {
             return true;
         }
         for (final Accountability accountability : unit.getParentAccountabilitiesSet()) {
             if (accountability.contains(localDate)) {
                 final AccountabilityType accountabilityType = accountability.getAccountabilityType();
                 if (MissionSystem.getInstance().getAccountabilityTypesForUnits().contains(accountabilityType)) {
                     final Unit parent = (Unit) accountability.getParent();
                     if (hasParentUnit(parent, localDate)) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     private boolean containsMission(final LocalDate start, final LocalDate end) {
         final int yearStart = start.getYear();
         final int yearEnd = start.getYear();
 
         if (yearStart > year || yearEnd < year) {
             return false;
         }
 
         if (yearStart != yearEnd) {
             return true;
         }
 
         final int monthStart = start.getMonthOfYear();
         final int monthEnd = end.getMonthOfYear();
 
         if (monthStart > month || monthEnd < month) {
             return false;
         }
 
         return true;
     }
 
     private boolean containsDay(final LocalDate localDate) {
         return localDate.getYear() == year && localDate.getMonthOfYear() == month;
     }
 }
