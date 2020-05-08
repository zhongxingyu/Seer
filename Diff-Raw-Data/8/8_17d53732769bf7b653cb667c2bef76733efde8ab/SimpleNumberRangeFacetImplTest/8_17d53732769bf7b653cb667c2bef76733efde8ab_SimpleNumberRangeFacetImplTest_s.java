 package org.ilrt.wf.facets.impl;
 
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.vocabulary.RDF;
 import org.ilrt.wf.facets.Facet;
 import org.ilrt.wf.facets.FacetException;
 import org.ilrt.wf.facets.FacetQueryService;
 import org.ilrt.wf.facets.FacetState;
 import org.ilrt.wf.facets.constraints.Constraint;
 import org.ilrt.wf.facets.constraints.ValueConstraint;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.junit.Assert.assertEquals;
 
 @RunWith(JMock.class)
 public class SimpleNumberRangeFacetImplTest extends AbstractFacetTest {
 
 
     @Before
     public void setUp() {
         mockFacetQueryService = context.mock(FacetQueryService.class);
         facet = new SimpleNumericRangeFacetImpl();
         facetFactory = new FacetFactoryServiceImpl(mockFacetQueryService, getPrefixMap());
     }
 
     @Test
     public void numericRefinements() {
 
         // pseudo root state
         final FacetStateImpl rootState = new FacetStateImpl();
         rootState.setRoot(true);
 
         // property used in each state
         Property p = ResourceFactory.createProperty(linkProperty);
 
 
         // get the states
         List<FacetState> states =
                 facet.refinements(testRanges, new ValueConstraint(RDF.type,
                         ResourceFactory.createProperty(typeProperty)), p, rootState);
 
         assertEquals("Unexpected label", 6, states.size());
 
         FacetState stateOne = states.get(0);
 
        assertEquals("Unexpected name", "0 - 50,000", stateOne.getName());
         assertEquals("Unexpected parameter value", "0:50000", stateOne.getParamValue());
         assertEquals("Unexpected parent", rootState, stateOne.getParent());
         assertEquals("There should be two constraints", 2, stateOne.getConstraints().size());
 
         FacetState stateFour = states.get(3);
 
        assertEquals("Unexpected name", "250,000 - 500,000", stateFour.getName());
         assertEquals("Unexpected parameter value", "250000:500000", stateFour.getParamValue());
         assertEquals("Unexpected parent", rootState, stateFour.getParent());
         assertEquals("There should be two constraints", 2, stateFour.getConstraints().size());
 
         FacetState stateSix = states.get(5);
 
        assertEquals("Unexpected name", "1,000,000 - 5,000,000", stateSix.getName());
         assertEquals("Unexpected parameter value", "1000000:5000000", stateSix.getParamValue());
         assertEquals("Unexpected parent", rootState, stateSix.getParent());
         assertEquals("There should be two constraints", 2, stateSix.getConstraints().size());
 
     }
 
     @Test
     public void calculateRefinements() throws FacetException {
 
         // create mock states
         final FacetStateImpl mockRootState = new FacetStateImpl(null, null, null, null);
         final FacetState mockRefinementState =
                 new FacetStateImpl("0 - 50,000", mockRootState, "0:50000", new ArrayList<Constraint>());
         mockRootState.getRefinements().add(mockRefinementState);
 
         // service expects a list of current (parent states)
         final List<FacetState> rootStateList = new ArrayList<FacetState>();
         rootStateList.add(mockRootState);
 
         // mock the results returned by the service
         final Map<FacetState, Integer> results = getCounts(rootStateList);
 
         // set the expectations for the service
         context.checking(new Expectations() {{
             oneOf(mockFacetQueryService).getCounts(rootStateList);
             will(returnValue(results));
         }});
 
         // mock the facet and add it to the refinementStateList
         Facet mockFacet = new FacetImpl(facetTestName, mockRootState, facetParamName);
         final List<Facet> facets = new ArrayList<Facet>();
         facets.add(mockFacet);
 
         // sanity check we have constructed the mock objects
         assertEquals("Unexpected facet refinementStateList size", 1, facets.size());
         assertEquals("Unexpected facet", mockFacet, facets.get(0));
         assertEquals("Unexpected facet state", mockRootState, facets.get(0).getState());
         assertEquals("Unexpected refinement state", mockRefinementState,
                 facets.get(0).getState().getRefinements().get(0));
 
         // update the count
         facetFactory.calculateCount(currentStates(facets));
 
         assertEquals("Unexpected count", 5,
                 facets.get(0).getState().getRefinements().get(0).getCount());
     }
 
 
     // ---------- private helper methods
 
     /**
      * @return a map of configuration details needed for an alphanumeric facet
      */
     private Map<String, String> createSimpleNumericRangeFacetConfig() {
 
         Map<String, String> config = new HashMap<String, String>();
         config.put(Facet.FACET_TYPE, Facet.SIMPLE_NUMBER_RANGE_FACET_TYPE);
         config.put(Facet.FACET_TITLE, facetTestName);
         config.put(Facet.LINK_PROPERTY, linkProperty);
         config.put(Facet.CONSTRAINT_TYPE, typeProperty);
         config.put(Facet.PARAM_NAME, facetParamName);
         return config;
     }
 
 
     // ---------- private variables
 
     private SimpleNumericRangeFacetImpl facet;
 
     private String testRanges = "0:50000,50000:100000,100000:250000,250000:500000,"
             + "500000:1000000,1000000:5000000";
 
     private final String linkProperty = "http://example.org/vocab/#value";
     private final String typeProperty = "http://example.org/vocab/#Thingy";
 
     private final String facetTestName = "Grants";
     private final String facetParamName = "grants";
 
     private FacetQueryService mockFacetQueryService;
     private FacetFactoryServiceImpl facetFactory;
 
     private final Mockery context = new JUnit4Mockery();
 }
