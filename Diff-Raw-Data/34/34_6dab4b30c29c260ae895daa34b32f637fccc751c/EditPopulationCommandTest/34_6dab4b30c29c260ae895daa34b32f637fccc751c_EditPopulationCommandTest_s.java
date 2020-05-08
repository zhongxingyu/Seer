 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
 import edu.northwestern.bioinformatics.studycalendar.service.PopulationService;
 import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
 import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createAmendments;
 
 import java.util.Set;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ArrayList;
 
 import static org.easymock.EasyMock.expect;
 
 /**
  * @author Nataliya Shurupova
  */
 public class EditPopulationCommandTest extends StudyCalendarTestCase {
     private Population population;
     private Population originalPopulation;
 
 
     private PopulationService populationService;
     private AmendmentService amendmentService;
     private PopulationDao populationDao;
     private Study study;
     private static final String STUDY_NAME = "NU-1066";
     private Amendment a1, a2;
 
     private EditPopulationCommand command;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         originalPopulation = Fixtures.createPopulation("Abbreviation", "Name");
         amendmentService = registerMockFor(AmendmentService.class);
         populationService = registerMockFor(PopulationService.class);
         populationDao = registerDaoMockFor(PopulationDao.class);
 
 
         originalPopulation = Fixtures.createPopulation("Abbr", "name");
         originalPopulation.setId(10);
         Set<Population> pops = new HashSet<Population>();
         pops.add(originalPopulation);
 
         study = setId(100, Fixtures.createBasicTemplate());
         study.setName(STUDY_NAME);
 
         a2 = setId(2, createAmendments("A0", "A1", "A2"));
         a1 = setId(1, a2.getPreviousAmendment());
 
         study.setAmendment(a2);
         study.setDevelopmentAmendment(a1);
 
         study.setPopulations(pops);
         initCommand();
     }
 
     private void initCommand() {
         command = new EditPopulationCommand(originalPopulation, populationService, amendmentService, populationDao, study);
     }
 
     public void testIsEdit() throws Exception {
         assertFalse("Population is not in edit state", command.isEdit());
     }
 
 
     public void testIsEditTrue() throws Exception {
         command.getPopulation().setId(25);
         assertTrue("Population is in edit state", command.isEdit());
     }
 
     public void testApply() throws Exception {
         List<Change> changes = new ArrayList<Change>();
         Change changeName = PropertyChange.create("name", originalPopulation.getName(), command.getPopulation().getName());
         Change changeAbbreviation = PropertyChange.create("abbreviation", originalPopulation.getAbbreviation(), command.getPopulation().getAbbreviation());
         changes.add(changeName);
         changes.add(changeAbbreviation);
 
         expect(amendmentService.updateDevelopmentAmendmentForStudyAndSave(originalPopulation, study, changes.toArray(new Change[changes.size()]))).andReturn(originalPopulation).anyTimes();
 
         replayMocks();
         command.apply();
         verifyMocks();
     }
 }
