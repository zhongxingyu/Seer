 package org.eclipse.editor.huppaal;
 
 import static org.eclipse.editor.EditorUtil.nvl;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.editor.editor.Connector;
 import org.eclipse.editor.editor.Diagram;
 import org.eclipse.editor.editor.Edge;
 import org.eclipse.editor.editor.EditorFactory;
 import org.eclipse.editor.editor.EndPoint;
 import org.eclipse.editor.editor.State;
 import org.eclipse.editor.huppaal.model.Component;
 import org.eclipse.editor.huppaal.model.Entry;
 import org.eclipse.editor.huppaal.model.Globalinit;
 import org.eclipse.editor.huppaal.model.Hta;
 import org.eclipse.editor.huppaal.model.Label;
 import org.eclipse.editor.huppaal.model.Location;
 import org.eclipse.editor.huppaal.model.Template;
 import org.eclipse.editor.huppaal.model.Transition;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 
 public class HtaGeneratorTest {
 	HtaGenerator generator;
 
 	@Before
 	public void before() {
 		generator = new HtaGenerator();
 	}
 
 	@Test
 	public void noInitialState() throws Exception {
 		try {
 			generator.generateModel();
 			fail();
 		} catch (Exception e) {
 			assertEquals("Model must have exactly one initial state.", e.getMessage());
 		}
 	}
 
 	@Test
 	public void onlyInitialState() throws Exception {
 		State state = createInitialState("A");
 		state.setInvariant("i < 10");
 		state.setUrgent(true);
 		state.setCommitted(true);
 
 		Hta hta = generator.generateModel(state);
 
 		assertEquals("", nvl(hta.getDeclaration()));
 		assertEquals(1, hta.getTemplate().size());
 
 		Location l = hta.getTemplate().get(0).getLocation().get(0);
 		assertTrue(l.getId().matches("Template.A.\\d+"));
 		assertEquals("A", l.getName().getvalue());
 		assertEquals("i < 10", findByKind(l.getLabel(), "invariant").getvalue());
 		assertNotNull(l.getUrgent());
 		assertNotNull(l.getCommitted());
 
 		assertEquals("template := Template();", hta.getInstantiation());
 		assertEquals("system template;", hta.getSystem());
 		List<Globalinit> globalinits = hta.getGlobalinit();
 		assertEquals(1, globalinits.size());
 		assertEquals("template", globalinits.get(0).getInstantiationname());
 		assertEquals("Template.ENTRY", ((Entry) globalinits.get(0).getRef()).getId());
 	}
 
 	@Test
 	public void twoStatesAndAnEdge() throws Exception {
 		State stateA = createInitialState("A");
 		State stateB = createState("B");
 		createEdge(stateA, stateB);
 
 		Hta hta = generator.generateModel(stateA, stateB);
 		Template template = hta.getTemplate().get(0);
 
 		List<Location> locations = template.getLocation();
 		assertLocations(locations, Arrays.asList("A", "B"));
 
 		List<Transition> transitions = template.getTransition();
 		assertEquals(1, transitions.size());
 		assertTransition(transitions, locations.get(0), locations.get(1));
 	}
 
 	@Test
 	public void twoOutgoingEdges() throws Exception {
 		State stateA = createInitialState("A");
 		State stateB = createState("B");
 		State stateC = createState("C");
 		createEdge(stateA, stateB);
 		createEdge(stateA, stateC);
 
 		Hta hta = generator.generateModel(stateA, stateB);
 		Template template = hta.getTemplate().get(0);
 
 		List<Location> locations = template.getLocation();
 		assertEquals(3, locations.size());
 
 		List<Transition> transitions = template.getTransition();
 		assertEquals(2, transitions.size());
 		assertTransition(transitions, locations.get(0), locations.get(1));
 		assertTransition(transitions, locations.get(0), locations.get(2));
 		assertNull(findByKind(transitions.get(0).getLabel(), "select"));
 		assertNull(findByKind(transitions.get(0).getLabel(), "comments"));
 	}
 
 	@Test
 	public void cyclic() throws Exception {
 		State stateA = createInitialState("A");
 		State stateB = createState("B");
 		createEdge(stateA, stateB);
 		createEdge(stateB, stateA);
 
 		Hta hta = generator.generateModel(stateA, stateB);
 		Template template = hta.getTemplate().get(0);
 
 		List<Location> locations = template.getLocation();
 		assertEquals(2, locations.size());
 
 		List<Transition> transitions = template.getTransition();
 		assertEquals(2, transitions.size());
 		assertTransition(transitions, locations.get(0), locations.get(1));
 		assertTransition(transitions, locations.get(1), locations.get(0));
 	}
 	
 	@Test
 	public void cyclicWithConnectors() throws Exception {
 		State stateA = createInitialState("A");
 		State stateB = createState("B");
 		Diagram subDiagram = createDiagram("sub");
 		Connector connector = createConnector(subDiagram, "C");
 		Connector connector2 = createConnector(subDiagram, "D");
 		createEdge(stateA, connector);
 		createEdge(connector, connector2);
 		createEdge(connector2, stateB);
 		createEdge(stateB, connector);
 		
 		Hta hta = generator.generateModel(stateA, stateB);
 		Template template = hta.getTemplate().get(0);
 		
 		assertEquals(2, hta.getTemplate().size());
 		assertEquals(3, template.getTransition().size());
 		assertEquals(1, hta.getTemplate().get(1).getEntry().size());
 	}
 
 	@Test
 	public void transitionProperties_EverythingIsSet() throws Exception {
 		State stateA = createInitialState("A");
 		State stateB = createState("B");
 		Edge edge = createEdge(stateA, stateB);
 		edge.setGuard("a == 0");
 		edge.setSelect("select");
 		edge.setSync("s?");
 		edge.setUpdate("a = 1");
 		edge.setComments("comment");
 
 		Hta hta = generator.generateModel(stateA, stateB);
 		Template template = hta.getTemplate().get(0);
 
 		List<Location> locations = template.getLocation();
 		Transition t = template.getTransition().get(0);
 
 		assertEquals(locations.get(0), t.getSource().getRef());
 		assertEquals(locations.get(1), t.getTarget().getRef());
 		assertEquals("a == 0", findByKind(t.getLabel(), "guard").getvalue());
 		assertEquals("select", findByKind(t.getLabel(), "select").getvalue());
 		assertEquals("s?", findByKind(t.getLabel(), "synchronisation").getvalue());
 		assertEquals("a = 1", findByKind(t.getLabel(), "assignment").getvalue());
 		assertEquals("comment", findByKind(t.getLabel(), "comments").getvalue());
 	}
 
 	/**
 	 * select and comments must be skipped when they are empty because huppaal
 	 * -> uppaal converter does not support them.
 	 */
 	@Test
 	public void transitionProperties_NothingIsSet() throws Exception {
 		State stateA = createInitialState("A");
 		State stateB = createState("B");
 		createEdge(stateA, stateB);
 
 		Hta hta = generator.generateModel(stateA, stateB);
 		Template template = hta.getTemplate().get(0);
 
 		Transition transition = template.getTransition().get(0);
 		assertNull(findByKind(transition.getLabel(), "select"));
 		assertNull(findByKind(transition.getLabel(), "comments"));
 		assertEquals("", nvl(findByKind(transition.getLabel(), "assignment").getvalue()));
 		assertEquals("", nvl(findByKind(transition.getLabel(), "synchronisation").getvalue()));
 	}
 
 	@Test
 	public void withSubdiagram() throws Exception {
 		State stateA = createInitialState("A");
 		Diagram subDiagram = createDiagram("sub");
 		Connector connector1 = createConnector(subDiagram, "con1");
 		Connector connector2 = createConnector(subDiagram, "con2");
 		State stateB = createState("B");
 		State stateC = createState("C");
 
 		createEdge(stateA, connector1);
 		createEdge(connector1, stateC);
 		createEdge(stateC, connector2);
 		createEdge(connector2, stateB);
 
 		Hta hta = generator.generateModel(stateA, stateB);
 		List<Template> templates = hta.getTemplate();
 		assertEquals(2, templates.size());
 
 		Template template = templates.get(0);
 		List<Location> locations = template.getLocation();
 		List<Transition> transitions = template.getTransition();
 
 		Template subTemplate = templates.get(1);
 		List<Location> subLocations = subTemplate.getLocation();
 		List<Transition> subTransitions = subTemplate.getTransition();
 
 		List<Component> components = template.getComponent();
 		Component component = components.get(0);
 		assertEquals(1, components.size());
 		assertEquals(subTemplate.getName().getvalue(), component.getInstantiates());
 		assertTrue(component.getId().matches("Template.\\d+"));
 
 		assertEquals(1, subTemplate.getEntry().size());
 		assertEquals(1, subTemplate.getExit().size());
 		assertLocations(locations, Arrays.asList("A", "B"));
 
 		assertEquals(2, transitions.size());
 		assertTransition(transitions, locations.get(0), null, components.get(0), subTemplate.getEntry().get(0));
 		assertTransition(transitions, components.get(0), subTemplate.getExit().get(0), locations.get(1), null);
 
 		assertLocations(subLocations, Arrays.asList("C"));
 		assertEquals(0, subTransitions.size());
 		assertEquals(subTemplate.getLocation().get(0), subTemplate.getEntry().get(0).getConnection().get(0).getTarget().getRef());
 		assertEquals(subTemplate.getLocation().get(0), subTemplate.getExit().get(0).getConnection().get(0).getSource().getRef());
 	}
 	
 	@Test
 	public void subDiagramWithMultipleEntriesAndExits() throws Exception {
 		/*
		 *   / C1 - C2 \
		 * A             B
		 *   \ C3 - C4 /
  		 */
 		State stateA = createInitialState("A");
 		Diagram subDiagram = createDiagram("sub");
 		Connector connector1 = createConnector(subDiagram, "con1");
 		Connector connector2 = createConnector(subDiagram, "con2");
 		Connector connector3 = createConnector(subDiagram, "con3");
 		Connector connector4 = createConnector(subDiagram, "con4");
 
 		State stateB = createState("B");
 		State stateC = createState("C");
 
 		createEdge(stateA, connector1);
 		createEdge(connector1, stateC);
 		createEdge(stateC, connector2);
 		createEdge(connector2, stateB);
 		
 		createEdge(stateA, connector3);
 		createEdge(connector3, stateC);
 		createEdge(stateC, connector4);
 		createEdge(connector4, stateB);
 		
 		Hta hta = generator.generateModel(stateA, stateB);
 		List<Template> templates = hta.getTemplate();
 		assertEquals(2, templates.size());
 
 		Template template = templates.get(0);
 		List<Location> locations = template.getLocation();
 		List<Transition> transitions = template.getTransition();
 
 		Template subTemplate = templates.get(1);
 		List<Location> subLocations = subTemplate.getLocation();
 		List<Transition> subTransitions = subTemplate.getTransition();
 
 		List<Component> components = template.getComponent();
 		assertEquals(1, components.size());
 		Component component = components.get(0);
 		assertEquals(subTemplate.getName().getvalue(), component.getInstantiates());
 		assertTrue(component.getId().matches("Template.\\d+"));
 
 		assertEquals(2, subTemplate.getEntry().size());
 		assertEquals(2, subTemplate.getExit().size());
 		assertLocations(locations, Arrays.asList("A", "B"));
 
 		assertEquals(4, transitions.size());
 		assertTransition(transitions, locations.get(0), null, template.getComponent().get(0), subTemplate.getEntry().get(0));
 		assertTransition(transitions, template.getComponent().get(0), subTemplate.getExit().get(0), locations.get(1), null);
 		assertTransition(transitions, locations.get(0), null, template.getComponent().get(0), subTemplate.getEntry().get(1));
 		assertTransition(transitions, template.getComponent().get(0), subTemplate.getExit().get(1), locations.get(1), null);
 
 		assertLocations(subLocations, Arrays.asList("C"));
 		assertEquals(0, subTransitions.size());
 		assertEquals(subTemplate.getLocation().get(0), subTemplate.getEntry().get(0).getConnection().get(0).getTarget().getRef());
 		assertEquals(subTemplate.getLocation().get(0), subTemplate.getExit().get(0).getConnection().get(0).getSource().getRef());
 		assertEquals(subTemplate.getLocation().get(0), subTemplate.getEntry().get(1).getConnection().get(0).getTarget().getRef());
 		assertEquals(subTemplate.getLocation().get(0), subTemplate.getExit().get(1).getConnection().get(0).getSource().getRef());
 	}
 
 	private Label findByKind(Iterable<Label> labels, final String kind) {
 		Iterator<Label> it = Iterables.filter(labels, new Predicate<Label>() {
 			@Override
 			public boolean apply(Label label) {
 				return label.getKind().equals(kind);
 			}
 		}).iterator();
 
 		if (it.hasNext()) {
 			return it.next();
 		}
 
 		return null;
 	}
 
 	private void assertTransition(Iterable<Transition> transitions, final Object from, final Object to) {
 		assertTransition(transitions, from, null, to, null);
 	}
 
 	private void assertTransition(Iterable<Transition> transitions, final Object from, final Object exitRef, final Object to, final Object entryRef) {
 		assertTrue(Iterables.any(transitions, new Predicate<Transition>() {
 			@Override
 			public boolean apply(Transition t) {
 				Object sourceExitRef = t.getSource().getExitref();
 				Object targetEntryRef = t.getTarget().getEntryref();
 
 				if ((exitRef != null && !(exitRef.equals(sourceExitRef))) || (exitRef == null && sourceExitRef != null)) {
 					return false;
 				}
 				
 				if ((entryRef != null && !(entryRef.equals(targetEntryRef))) || (entryRef == null && targetEntryRef != null)) {
 					return false;
 				}
 				
 				return t.getSource().getRef().equals(from) && t.getTarget().getRef().equals(to);
 			}
 		}));
 	}
 
 	private void assertLocations(final Collection<Location> locations, final Collection<String> names) {
 		assertEquals(names.size(), locations.size());
 
 		assertTrue(Iterables.all(names, new Predicate<String>() {
 			@Override
 			public boolean apply(final String name) {
 				return Iterables.any(locations, new Predicate<Location>() {
 					@Override
 					public boolean apply(Location location) {
 						return location.getName().getvalue().equals(name);
 					}
 
 				});
 			}
 		}));
 	}
 
 	private State createInitialState(String value) {
 		State state = createState(value);
 		state.setInitial(true);
 		return state;
 	}
 
 	private State createState(String value) {
 		State state = EditorFactory.eINSTANCE.createState();
 		state.setName(value);
 		return state;
 	}
 
 	private Edge createEdge(EndPoint start, EndPoint end) {
 		Edge edge = EditorFactory.eINSTANCE.createEdge();
 		edge.setStart(start);
 		edge.setEnd(end);
 		return edge;
 	}
 
 	private Connector createConnector(Diagram diagram, String name) {
 		Connector connector = EditorFactory.eINSTANCE.createConnector();
 		connector.setName(name);
 		connector.setDiagram(diagram);
 		return connector;
 	}
 
 	private Diagram createDiagram(String name) {
 		Diagram d = EditorFactory.eINSTANCE.createDiagram();
 		d.setName(name);
 		return d;
 	}
 }
