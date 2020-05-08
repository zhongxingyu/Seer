 package com.mymita.al.ui.search;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.data.neo4j.annotation.QueryType;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
 import org.springframework.data.neo4j.support.conversion.EntityResultConverter;
 import org.springframework.util.StringUtils;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.gwt.thirdparty.guava.common.base.Objects;
 import com.google.gwt.thirdparty.guava.common.base.Strings;
 import com.mymita.al.domain.Person;
 import com.mymita.al.domain.Person.Gender;
 import com.vaadin.annotations.PreserveOnRefresh;
 import com.vaadin.annotations.Theme;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.event.ShortcutAction.KeyCode;
 import com.vaadin.server.BrowserWindowOpener;
 import com.vaadin.server.ExternalResource;
 import com.vaadin.server.Page;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.shared.ui.MarginInfo;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CustomLayout;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.NativeButton;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Notification.Type;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.Table.Align;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.themes.Reindeer;
 
 @Configurable
 @Theme("default")
 @PreserveOnRefresh
 public class MainUI extends UI {
 
   private static final long   serialVersionUID = -6780876618168616688L;
   private static final Logger LOGGER           = LoggerFactory.getLogger(MainUI.class);
   private static final String FEMALE           = "♀";
   private static final String MALE             = "♂";
 
   private static String name(final String value) {
     final String result = Objects.firstNonNull(value, "").replaceAll("'", "").replaceAll("[^\\p{L}\\p{Nd}\\.\\*]", "");
     if (!Strings.isNullOrEmpty(result)) {
       try {
         Pattern.compile(result);
       } catch (final PatternSyntaxException e) {
         return null;
       }
       if (result.length() < 3) {
         return null;
       }
       if (StringUtils.countOccurrencesOf(result, "*") > 1) {
         return null;
       }
       if (StringUtils.countOccurrencesOf(result, ".") > 1) {
         return null;
       }
     }
     return result;
   }
 
   private static Integer number(final String value) {
     if (!Strings.isNullOrEmpty(value)) {
       try {
         return Integer.valueOf(value);
       } catch (final NumberFormatException e) {
       }
     }
     return null;
   }
 
   @Autowired
   transient Neo4jTemplate template;
 
   private Table           resultTable;
   private CustomLayout    content;
 
   private Table createResultTable() {
     final Table results = new Table() {
       @Override
       protected String formatPropertyValue(final Object rowId, final Object colId, final com.vaadin.data.Property<?> property) {
         if (property.getType() == Gender.class) {
           if (property.getValue() != null) {
             switch ((Gender) property.getValue()) {
             case FEMALE:
               return FEMALE;
             case MALE:
               return MALE;
             }
           }
         }
         return super.formatPropertyValue(rowId, colId, property);
       };
     };
     results.setStyleName(Reindeer.TABLE_BORDERLESS);
     results.addStyleName(Reindeer.TABLE_STRONG);
     results.setWidth(100, Unit.PERCENTAGE);
     results.setHeight(350, Unit.PIXELS);
     results.setSelectable(true);
     results.setMultiSelect(false);
     results.setImmediate(true);
     results.setPageLength(15);
     results.addValueChangeListener(new ValueChangeListener() {
       @Override
       public void valueChange(final ValueChangeEvent event) {
         showResultDetails(content, resultTable, (Person) event.getProperty().getValue());
       }
     });
     return results;
   }
 
   @Override
   protected void init(final VaadinRequest request) {
     getPage().setTitle("Altes Leipzig Suche");
     try {
       content = new CustomLayout(new ClassPathResource("/search.html").getInputStream());
     } catch (final IOException e) {
       return;
     }
     resultTable = createResultTable();
     setResultColumns(resultTable, Lists.<Person> newArrayList());
     final TextField name = new TextField("Name");
     name.setNullSettingAllowed(true);
     name.setNullRepresentation("");
     name.setValue(null);
     name.setStyleName("search");
     name.setWidth("150px");
     name.setRequired(false);
     name.setValidationVisible(false);
     name.focus();
     final TextField yearOfBirth = new TextField("Geburtsjahr");
     yearOfBirth.setStyleName("search");
     yearOfBirth.setWidth("80px");
     final TextField yearOfDeath = new TextField("Sterbejahr");
     yearOfDeath.setStyleName("search");
     yearOfDeath.setWidth("80px");
     final Button search = new NativeButton("Suche starten", new Button.ClickListener() {
 
       @Override
       public void buttonClick(final ClickEvent event) {
         final String nameValue = name(name.getValue());
         final Integer yearOfBirthValue = number(yearOfBirth.getValue());
         final Integer yearOfDeathValue = number(yearOfDeath.getValue());
         final List<String> filter = Lists.newArrayList();
         final Map<String, Object> params = Maps.newHashMap();
         if (!Strings.isNullOrEmpty(nameValue)) {
           filter.add(String.format("(person.birthName =~ '(?i)%1$s' OR person.lastName =~ '(?i)%1$s')", nameValue));
         }
         if (yearOfBirthValue != null) {
           filter.add(String.format("(person.yearOfBirth! = {yearOfBirth})", yearOfBirthValue));
           params.put("yearOfBirth", yearOfBirthValue.toString());
         }
         if (yearOfDeathValue != null) {
           filter.add(String.format("(person.yearOfDeath! = {yearOfDeath})", yearOfDeathValue));
           params.put("yearOfDeath", yearOfDeathValue.toString());
         }
         if (filter.isEmpty()) {
           showHits(Lists.<Person> newArrayList());
         } else {
           final String cypherQuery = "START person=node:__types__(className='Person') WHERE " + Joiner.on(" AND ").join(filter)
               + " RETURN person";
           LOGGER.debug(cypherQuery);
           showHits(Lists.newArrayList(template.getGraphDatabase().queryEngineFor(QueryType.Cypher).query(cypherQuery, params)
               .to(Person.class, new EntityResultConverter<Object, Person>(template.getConversionService(), template)).iterator()));
         }
       }
 
       private void showHits(final List<Person> persons) {
         setResultColumns(resultTable, persons);
         if (persons.isEmpty()) {
           final Notification notification = new Notification("Ihre Suche ergab leider keine Ergebnisse", Type.HUMANIZED_MESSAGE);
           notification.setDelayMsec(2000);
           notification.show(Page.getCurrent());
         } else {
           final Notification notification = new Notification("Ihre Suche ergab " + persons.size() + " Ergebnisse", Type.HUMANIZED_MESSAGE);
           notification.setDelayMsec(2000);
           notification.show(Page.getCurrent());
         }
         showResultDetails(content, resultTable, null);
       }
 
     });
     search.setClickShortcut(KeyCode.ENTER);
     content.addComponent(name, "name");
     content.addComponent(yearOfBirth, "yearOfBirth");
     content.addComponent(yearOfDeath, "yearOfDeath");
     content.addComponent(search, "search");
     content.addComponent(resultTable, "results");
     showResultDetails(content, resultTable, null);
     setContent(content);
   }
 
   private void setResultColumns(final Table results, final List<Person> persons) {
     results.setContainerDataSource(new BeanItemContainer<Person>(Person.class, persons));
     results.setColumnHeader("firstName", "Vorname");
     results.setColumnHeader("birthName", "Geburtsname");
     results.setColumnHeader("lastName", "Nachname");
     results.setColumnHeader("gender", "");
     results.setColumnHeader("yearOfBirth", "Geboren");
     results.setColumnHeader("yearOfDeath", "Gestorben");
     results.setColumnHeader("yearsOfLife", "Alter");
     results.setColumnWidth("firstName", 100);
     results.setColumnWidth("birthName", 100);
     results.setColumnWidth("lastName", 100);
     results.setColumnWidth("gender", 35);
     results.setColumnWidth("yearOfBirth", 80);
     results.setColumnWidth("yearOfDeath", 80);
     results.setColumnWidth("yearsOfLife", 50);
     results.setColumnAlignment("yearOfBirth", Align.CENTER);
     results.setColumnAlignment("yearOfDeath", Align.CENTER);
     results.setColumnAlignment("gender", Align.CENTER);
     results.setVisibleColumns(new Object[] { "lastName", "birthName", "firstName", "gender", "yearOfBirth", "yearOfDeath", "yearsOfLife" });
     results.setItemDescriptionGenerator(new ItemDescriptionGenerator() {
 
       @Override
       public String generateDescription(final Component source, final Object itemId, final Object propertyId) {
         if ("gender".equals(propertyId)) {
           final Gender g = ((Person) itemId).getGender();
           switch (g) {
           case FEMALE:
             return "Frau";
           case MALE:
             return "Mann";
           }
         }
         if ("yearsOfLife".equals(propertyId)) {
           final String yearsOfLife = ((Person) itemId).getYearsOfLife();
           if (yearsOfLife.endsWith("*")) {
             return "Von der Person existiert kein genaues Geburts- bzw. Sterbejahr";
           }
           if (yearsOfLife.equals("<1")) {
             return "Die Person wurde nicht älter als ein Jahr";
           }
         }
         return null;
       }
     });
   }
 
   private void showResultDetails(final CustomLayout content, final Table resultTable, final Person person) {
 
     final Panel descriptionPanel = new Panel();
     descriptionPanel.setStyleName("description");
     descriptionPanel.addStyleName(Reindeer.PANEL_LIGHT);
     descriptionPanel.setCaption("Beschreibung");
     descriptionPanel.setHeight(130, Unit.PIXELS);
     descriptionPanel.setWidth(450, Unit.PIXELS);
     final VerticalLayout infoPanelLayout = new VerticalLayout();
     // infoPanelLayout.setStyleName("description-panel");
     // infoPanelLayout.setSizeFull();
     infoPanelLayout.addComponent(new Label("Code: " + (person != null ? person.getCode() : "")));
     infoPanelLayout.addComponent(new Label(person != null ? person.getDescription() : ""));
     descriptionPanel.setContent(infoPanelLayout);
 
     final Panel imagePanel = new Panel();
     imagePanel.setStyleName(Reindeer.PANEL_LIGHT);
     imagePanel.setCaption("Bild/Vorschau");
     imagePanel.setHeight(100, Unit.PERCENTAGE);
    imagePanel.setWidth(250, Unit.PIXELS);
     final VerticalLayout imagePanelLayout = new VerticalLayout();
     imagePanelLayout.setStyleName("image-panel");
     imagePanelLayout.setSizeFull();
     imagePanel.setContent(imagePanelLayout);
 
     final Panel referencePanel = new Panel();
     referencePanel.setStyleName(Reindeer.PANEL_LIGHT);
     referencePanel.setCaption("Quelle / Ersterwähnung");
     referencePanel.setHeight(40, Unit.PIXELS);
     referencePanel.setWidth(450, Unit.PIXELS);
     final VerticalLayout referencePanelLayout = new VerticalLayout();
     referencePanelLayout.setStyleName("reference-panel");
     referencePanelLayout.setSizeFull();
     referencePanelLayout.addComponent(new Label(person != null ? person.getReference() : ""));
     referencePanel.setContent(referencePanelLayout);
 
     final GridLayout infos = new GridLayout(2, 3);
     infos.setStyleName("result-details");
     infos.setMargin(new MarginInfo(true, false, false, false));
     infos.setSpacing(true);
     infos.setWidth(100, Unit.PERCENTAGE);
 
     infos.addComponent(descriptionPanel, 0, 1);
     infos.addComponent(referencePanel, 0, 2);
     infos.addComponent(imagePanel, 1, 1, 1, 2);
     infos.setComponentAlignment(imagePanel, Alignment.MIDDLE_RIGHT);
     infos.setComponentAlignment(descriptionPanel, Alignment.TOP_LEFT);
     infos.setComponentAlignment(referencePanel, Alignment.BOTTOM_LEFT);
     infos.setRowExpandRatio(0, 0);
 
     content.removeComponent("help");
     final Label icon = new Label("<i class=\"fi-info help\"/>", ContentMode.HTML);
     if (resultTable.size() == 0) {
       final Label hint = new Label("Bitte starten Sie die Suche.");
       hint.setStyleName("hint");
       final HorizontalLayout hintLayout = new HorizontalLayout(icon, hint);
       hintLayout.setSpacing(true);
       hintLayout.setHeight(40, Unit.PIXELS);
       hintLayout.setComponentAlignment(icon, Alignment.MIDDLE_LEFT);
       hintLayout.setComponentAlignment(hint, Alignment.MIDDLE_LEFT);
       content.addComponent(hintLayout, "help");
     } else if (resultTable.size() > 0 && person == null) {
       final Label hint = new Label("Bitte klicken Sie auf ein Ergebnis um weitere Informationen zu erhalten.");
       hint.setStyleName("hint");
       final HorizontalLayout hintLayout = new HorizontalLayout(icon, hint);
       hintLayout.setSpacing(true);
       hintLayout.setHeight(40, Unit.PIXELS);
       hintLayout.setComponentAlignment(icon, Alignment.MIDDLE_LEFT);
       hintLayout.setComponentAlignment(hint, Alignment.MIDDLE_LEFT);
       content.addComponent(hintLayout, "help");
     } else if (resultTable.size() > 0 && person != null) {
       final Label hint = new Label("Klicken Sie bitte hier um weitere Informationen zur gewählten Person zu erfragen");
       hint.setStyleName("hint");
       hint.addStyleName("contact");
       new BrowserWindowOpener(new ExternalResource("mailto:wehlmann@altes-leipzig.de?subject=Detailanfrage für PersonenCode '"
           + person.getCode() + "'")).extend(hint);
       final HorizontalLayout hintLayout = new HorizontalLayout(icon, hint);
       hintLayout.setSpacing(true);
       hintLayout.setHeight(40, Unit.PIXELS);
       hintLayout.setComponentAlignment(icon, Alignment.MIDDLE_LEFT);
       hintLayout.setComponentAlignment(hint, Alignment.MIDDLE_LEFT);
       content.addComponent(hintLayout, "help");
     }
     content.removeComponent("details");
     content.addComponent(infos, "details");
   }
 }
