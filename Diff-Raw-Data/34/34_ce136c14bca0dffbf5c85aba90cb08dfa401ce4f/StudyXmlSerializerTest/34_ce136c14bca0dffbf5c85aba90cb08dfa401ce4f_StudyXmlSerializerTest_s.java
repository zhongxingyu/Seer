 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
 import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
 import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.QName;
 import static org.easymock.EasyMock.expect;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collections;
 import static java.util.Collections.singletonList;
 
 public class StudyXmlSerializerTest extends StudyCalendarXmlTestCase {
     private StudyXmlSerializer serializer;
     private Study study;
     private Element element;
     private PlannedCalendar calendar;
     private StudyDao studyDao;
     private PopulationXmlSerializer populationSerializer;
     private Population population;
     private Element eCalendar, ePopulation;
     private PlannedCalendarXmlSerializer plannedCalendarSerializer;
     private AmendmentXmlSerializer amendmentSerializer;
     private Amendment amendment;
     private Element eAmendment;
     private DeltaService deltaService;
     private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 
 
     protected void setUp() throws Exception {
         super.setUp();
 
         element = registerMockFor(Element.class);
         studyDao = registerDaoMockFor(StudyDao.class);
         amendmentSerializer = registerMockFor(AmendmentXmlSerializer.class);
         populationSerializer = registerMockFor(PopulationXmlSerializer.class);
         plannedCalendarSerializer = registerMockFor(PlannedCalendarXmlSerializer.class);
         deltaService = registerMockFor(DeltaService.class);
 
         serializer = new StudyXmlSerializer() {
             protected PlannedCalendarXmlSerializer getPlannedCalendarXmlSerializer(Study study) {return plannedCalendarSerializer;}
             protected PopulationXmlSerializer getPopulationXmlSerializer(Study study) {return populationSerializer;}
             protected AmendmentXmlSerializer getAmendmentSerializer(Study study) {return amendmentSerializer;}
         };
         serializer.setStudyDao(studyDao);
         serializer.setDeltaService(deltaService);
 
         calendar = setGridId("grid1", new PlannedCalendar());
         population = createPopulation("MP", "My Population");
         amendment =  createAmendment("Amendment A", createDate(2008, Calendar.JANUARY, 2), true);
 
         study = createNamedInstance("Study A", Study.class);
         study.setPlannedCalendar(calendar);
         study.addPopulation(population);
         study.setAmendment(amendment);
 
         QName qCalendar = DocumentHelper.createQName("planned-calendar", AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
         eCalendar = DocumentHelper.createElement(qCalendar);
         eCalendar.addAttribute("id", calendar.getGridId());
 
         QName qPopulation = DocumentHelper.createQName("population", AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
         ePopulation = DocumentHelper.createElement(qPopulation);
         ePopulation.addAttribute("abbreviation", population.getAbbreviation());
         ePopulation.addAttribute("name", population.getName());
 
         QName qAmendment = DocumentHelper.createQName("amendment", AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
         eAmendment = DocumentHelper.createElement(qAmendment);
         eAmendment.addAttribute("name", amendment.getName());
         eAmendment.addAttribute("date", formatter.format(amendment.getDate()) );
         eAmendment.addAttribute("mandatory", Boolean.toString(amendment.isMandatory()));
     }
 
     public void testReadElementWhereElementIsNew() {
         expect(element.attributeValue("assigned-identifier")).andReturn("Study A");
         expect(studyDao.getByAssignedIdentifier("Study A")).andReturn(null);
 
         expect(element.elements("population")).andReturn(singletonList(element));
         expect(populationSerializer.readElement(element)).andReturn(population);
 
         expect(element.element("planned-calendar")).andReturn(element);
 
         expect(element.elements("amendment")).andReturn(Collections.singletonList(element));
         expect(amendmentSerializer.readElement(element)).andReturn(amendment);
 
         // Need to cast calendar to PlanTreeNode because of EasyMockBug
         expect(plannedCalendarSerializer.readElement(element)).andReturn((PlanTreeNode) calendar);
         replayMocks();
 
         Study actual = serializer.readElement(element);
         verifyMocks();
 
         assertEquals("Wrong assigned identifier", "Study A", actual.getAssignedIdentifier());
         assertSame("PlannedCalendar should be the same", calendar, actual.getPlannedCalendar());
         assertSame("Populations should be the same", population, actual.getPopulations().iterator().next());
         assertSame("Amendment should be the same", amendment,  actual.getAmendment());
     }
 
     public void testReadElementWhereElementExists() {
         expect(element.attributeValue("assigned-identifier")).andReturn("Study A");
         expect(studyDao.getByAssignedIdentifier("Study A")).andReturn(study);
         replayMocks();
 
         Study actual = serializer.readElement(element);
         verifyMocks();
 
         assertSame("Wrong Study", study, actual);
     }
 
     public void testCreateElement() {
         expectChildrenSerializers();
         replayMocks();
 
         Element actual = serializer.createElement(study);
         verifyMocks();
 
         assertEquals("Wrong attribute size", 1, actual.attributeCount());
         assertEquals("Wrong assigned identifier", "Study A", actual.attribute("assigned-identifier").getValue());
         assertEquals("Should have planned calendar child and population child nodes", 3, actual.nodeCount());
         assertNotNull("Planned calendar should exist", actual.element("planned-calendar"));
         assertNotNull("Population should exist", actual.element("population"));
         assertNotNull("Amendment should exist", actual.element("amendment"));
     }
 
     public void testCreateDocument() throws Exception {
         expectChildrenSerializers();
         replayMocks();
 
         Document actual = serializer.createDocument(study);
         verifyMocks();
 
         assertEquals("Wrong assigned identifier", "Study A", actual.getRootElement().attribute("assigned-identifier").getValue());
         assertEquals("Wrong planned calendar grid id", "grid1", actual.getRootElement().element("planned-calendar").attributeValue("id"));
         assertNotNull("Population element should exist", actual.getRootElement().element("population"));
         assertNotNull("Amendment element should exist", actual.getRootElement().element("amendment"));
     }
 
     public void testCreateDocumentString() throws Exception {
         StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append(format("<study assigned-identifier=\"{0}\"", study.getAssignedIdentifier()))
                .append(format("       {0}=\"{1}\""     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
                .append(format("       {0}=\"{1} {2}\""     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
                .append(format("       {0}=\"{1}\">"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
                .append(format(  "<planned-calendar id=\"{0}\"/>", calendar.getGridId()))
                .append(format(  "<population abbreviation=\"{0}\" name=\"{1}\"/>", population.getAbbreviation(), population.getName()))
                .append(format(  "<amendment name=\"{0}\" date=\"{1}\" mandatory=\"{2}\"/>", eAmendment.attributeValue("name"), eAmendment.attributeValue("date"), eAmendment.attributeValue("mandatory")))
                .append(       "</study>");

        expectChildrenSerializers();
        replayMocks();

        String actual = serializer.createDocumentString(study);
        verifyMocks();

        assertXMLEqual(expected.toString(), actual);
     }
 
     private void expectChildrenSerializers() {
         expect(populationSerializer.createElement(population)).andReturn(ePopulation);
         expect(plannedCalendarSerializer.createElement(calendar)).andReturn(eCalendar);
         expect(amendmentSerializer.createElement(amendment)).andReturn(eAmendment);
     }
 }
