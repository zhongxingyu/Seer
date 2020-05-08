 package se.svt.api.controller;
 
 import neo.xredsys.api.ObjectLoader;
 import neo.xredsys.api.Publication;
 import neo.xredsys.api.Section;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.web.servlet.ModelAndView;
 import se.svt.api.domain.SectionListItem;
 import se.svt.test.stubs.StubObjectLoaderBuilder;
 import se.svt.test.stubs.StubPublicationBuilder;
 import se.svt.test.stubs.StubSectionBuilder;
 
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 public class SectionControllerTest {
 	private final static String HOST = "http://svt.se";
 	private SectionController sectionController;
 
 	@Before
 	public void setUp() throws Exception {
 		sectionController = new SectionController();
 	}
 
 	@Test
 	public void shouldReturnPublicationRootAsRootSections() throws Exception {
 		Section svtseSection = new StubSectionBuilder().setId(1).setName("svtse").build();
 		Publication svtse = new StubPublicationBuilder().setRootSection(svtseSection).setId(1).setName("svtse").setUrl(String.format("%s/", HOST)).build();
 		Section barnSection = new StubSectionBuilder().setId(2).setName("barn").build();
 		Publication barn = new StubPublicationBuilder().setRootSection(barnSection).setId(2).setName("barn").setUrl(String.format("%s/barn", HOST)).build();
 		Section kunskapskanalenSection = new StubSectionBuilder().setId(3).setName("kunskapskanalen").build();
 		Publication kunskapskanalen = new StubPublicationBuilder().setRootSection(kunskapskanalenSection).setId(3).setName("kunskapskanalen").setUrl(String.format("%s/kunskapskanalen", HOST)).build();
 
 		ObjectLoader objectLoader = new StubObjectLoaderBuilder().addPublication(svtse).addPublication(barn).addPublication(kunskapskanalen).build();
 
 		sectionController.setObjectLoader(objectLoader);
 		ModelAndView modelAndView = sectionController.displayRootSections();
 		//noinspection unchecked
		List<SectionListItem> sections = (List<SectionListItem>) modelAndView.getModel().get("model");
 		assertEquals(3, sections.size());
 		assertEquals("http://svt.se/svtapi/svtse/sections/1", sections.get(0).getUrl());
 		assertEquals(svtse.getName(), sections.get(0).getName());
 		assertEquals("http://svt.se/svtapi/barn/sections/2", sections.get(1).getUrl());
 		assertEquals(barn.getName(), sections.get(1).getName());
 		assertEquals("http://svt.se/svtapi/kunskapskanalen/sections/3", sections.get(2).getUrl());
 		assertEquals(kunskapskanalen.getName(), sections.get(2).getName());
 	}
 
 	@Test
 	public void shouldReturnAModelContainingAllSubSections() throws Exception {
 
 		Publication publication = new StubPublicationBuilder().setId(1).build();
 		int publicationId = publication.getId();
 
 		Section subsection = new StubSectionBuilder().setName("subsection").setId(2).build();
 		Section section = new StubSectionBuilder().setName("section").setId(1).addSubSection(subsection).build();
 
 		ObjectLoader objectLoader = new StubObjectLoaderBuilder().addPublication(publication).setPublicationId(publicationId).addSection(section).addSection(subsection).build();
 		sectionController.setObjectLoader(objectLoader);
 
 		ModelAndView modelAndView = sectionController.displaySections(publication, 1);
 		List<SectionListItem> sections = (List<SectionListItem>) modelAndView.getModel().get("model");
 		assertEquals(1, sections.size());
 		SectionListItem sectionListItem = sections.get(0);
 		assertEquals(subsection.getName(), sectionListItem.getName());
 	}
 }
