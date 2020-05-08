 package org.semanticweb.owlapi.gwt.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.junit.client.GWTTestCase;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import org.semanticweb.owlapi.model.*;
 import org.semanticweb.owlapi.util.CollectionFactory;
 import org.semanticweb.owlapi.vocab.OWL2Datatype;
 import org.semanticweb.owlapi.vocab.OWLFacet;
 import uk.ac.manchester.cs.owl.owlapi.*;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Author: Matthew Horridge<br>
  * Stanford University<br>
  * Bio-Medical Informatics Research Group<br>
  * Date: 19/11/2013
  * <p>
  *     This class name is prefixed with GwtTest so that it is recognized by the maven GWT plugin as a GWTTestCase.
  *     Also, it does not end with "TestCase" so that it is not picked up by the maven sure fire plugin and run
  *     as a regular test case.
  * </p>
  */
 public class GwtTest_OWLObject_Serialization extends GWTTestCase {
 
     public static final int TEST_DELAY_MS = 10000;
 
     @Override
     public String getModuleName() {
         return "org.semanticweb.owlapi.gwt.OWLObjectSerializationTestsJUnit";
     }
 
     public void testShouldSerializeAxiomType() {
         delayTestFinish(TEST_DELAY_MS);
         final AxiomType<?> in = AxiomType.ANNOTATION_ASSERTION;
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testAxiomType(in, new AsyncCallback<AxiomType<?>>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(AxiomType<?> out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeEntityType() {
         delayTestFinish(TEST_DELAY_MS);
         final EntityType<?> in = EntityType.CLASS;
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testEntityType(in, new AsyncCallback<EntityType<?>>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(EntityType<?> out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeClassExpressionType() {
         delayTestFinish(TEST_DELAY_MS);
         final ClassExpressionType in = ClassExpressionType.OWL_CLASS;
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testClassExpressionType(in, new AsyncCallback<ClassExpressionType>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(ClassExpressionType out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
     
     public void testShouldSerializeIRI() {
         delayTestFinish(TEST_DELAY_MS);
         final IRI in = IRI.create("http://stuff.com/A");
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testIRI(in, new AsyncCallback<IRI>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(IRI out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLOntologyID() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLOntologyID in = new OWLOntologyID(IRI.create("http://stuff.com/A"));
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLOntologyId(in, new AsyncCallback<OWLOntologyID>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLOntologyID out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
     
     public void testShouldSerializeOWLClassImpl() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLClassImpl in = new OWLClassImpl(IRI.create("http://org.semanticweb.owlapi.gwt#A"));
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLClassImpl(in, new AsyncCallback<OWLClassImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLClassImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectPropertyImpl() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLObjectPropertyImpl in = new OWLObjectPropertyImpl(IRI.create("http://org.semanticweb.owlapi.gwt#A"));
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectPropertyImpl(in, new AsyncCallback<OWLObjectPropertyImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectPropertyImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
 
     public void testShouldSerializeOWLDataPropertyImpl() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLDataPropertyImpl in = new OWLDataPropertyImpl(IRI.create("http://org.semanticweb.owlapi.gwt#A"));
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDataPropertyImpl(in, new AsyncCallback<OWLDataPropertyImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataPropertyImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLAnnotationPropertyImpl() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLAnnotationPropertyImpl in = new OWLAnnotationPropertyImpl(IRI.create("http://org.semanticweb.owlapi.gwt#A"));
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLAnnotationPropertyImpl(in, new AsyncCallback<OWLAnnotationPropertyImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLAnnotationPropertyImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLNamedIndividualImpl() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLNamedIndividualImpl in = new OWLNamedIndividualImpl(IRI.create("http://org.semanticweb.owlapi.gwt#A"));
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLNamedIndividualImpl(in, new AsyncCallback<OWLNamedIndividualImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLNamedIndividualImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDatatypeImpl() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLDatatypeImpl in = new OWLDatatypeImpl(IRI.create("http://org.semanticweb.owlapi.gwt#A"));
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDatatypeImpl(in, new AsyncCallback<OWLDatatypeImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDatatypeImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLLiteralImplBoolean() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLLiteralImplBoolean in = new OWLLiteralImplBoolean(true);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLLiteralImplBoolean(in, new AsyncCallback<OWLLiteralImplBoolean>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLLiteralImplBoolean out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLLiteralImplDouble() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLLiteralImplDouble in = new OWLLiteralImplDouble(3.3, new OWLDatatypeImpl(IRI.create("http://stuff.com/A")));
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLLiteralImplDouble(in, new AsyncCallback<OWLLiteralImplDouble>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLLiteralImplDouble out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLLiteralImplInteger() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLLiteralImplInteger in = new OWLLiteralImplInteger(3, new OWLDatatypeImpl(IRI.create("http://stuff.com/A")));
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLLiteralImplInteger(in, new AsyncCallback<OWLLiteralImplInteger>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLLiteralImplInteger out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWL2DatatypeImpl() {
         delayTestFinish(TEST_DELAY_MS);
         final OWL2DatatypeImpl in = (OWL2DatatypeImpl) OWL2DatatypeImpl.getDatatype(OWL2Datatype.RDF_PLAIN_LITERAL);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWL2DatatypeImpl(in, new AsyncCallback<OWL2DatatypeImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWL2DatatypeImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLLiteralImplNoCompression() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLLiteralImplNoCompression in = new OWLLiteralImplNoCompression("xyz", null, null);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLLiteralImplNoCompression(in, new AsyncCallback<OWLLiteralImplNoCompression>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
 
             }
 
             @Override
             public void onSuccess(OWLLiteralImplNoCompression out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
 
     public void testShouldSerializeOWLObjectInverseOfImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLObjectPropertyExpression property = dataFactory.getOWLObjectInverseOf(dataFactory.getOWLObjectProperty(IRI.create("http://org.semanticweb.owlapi/prop")));
         final OWLObjectInverseOfImpl in = new OWLObjectInverseOfImpl(property);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectInverseOfImpl(in, new AsyncCallback<OWLObjectInverseOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectInverseOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectIntersectionOfImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         final OWLClassExpression clsA = dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/clsA"));
         final OWLClassExpression clsB= dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/clsB"));
         final OWLClassExpression clsC = dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/clsC"));
         Set<OWLClassExpression> operands = CollectionFactory.createSet(clsA, clsB, clsC);
         final OWLObjectIntersectionOfImpl in = new OWLObjectIntersectionOfImpl(operands);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectIntersectionOfImpl(in, new AsyncCallback<OWLObjectIntersectionOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectIntersectionOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectUnionOfImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         final OWLClassExpression clsA = dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/clsA"));
         final OWLClassExpression clsB= dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/clsB"));
         final OWLClassExpression clsC = dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/clsC"));
         Set<OWLClassExpression> operands = CollectionFactory.createSet(clsA, clsB, clsC);
         final OWLObjectUnionOfImpl in = new OWLObjectUnionOfImpl(operands);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectUnionOfImpl(in, new AsyncCallback<OWLObjectUnionOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectUnionOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectComplementOfImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         final OWLClassExpression clsA = dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/clsA"));
         final OWLObjectComplementOfImpl in = new OWLObjectComplementOfImpl(clsA);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectComplementOfImpl(in, new AsyncCallback<OWLObjectComplementOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectComplementOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectOneOfImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         final OWLIndividual indA = dataFactory.getOWLNamedIndividual(IRI.create("http://org.semanticweb.owlapi/a"));
         final OWLIndividual indB= dataFactory.getOWLNamedIndividual(IRI.create("http://org.semanticweb.owlapi/b"));
         final OWLIndividual indC = dataFactory.getOWLNamedIndividual(IRI.create("http://org.semanticweb.owlapi/c"));
         Set<OWLIndividual> operands = CollectionFactory.createSet(indA, indB, indC);
         final OWLObjectOneOfImpl in = new OWLObjectOneOfImpl(operands);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectOneOfImpl(in, new AsyncCallback<OWLObjectOneOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectOneOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectSomeValuesFromImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLObjectPropertyExpression property = dataFactory.getOWLObjectInverseOf(dataFactory.getOWLObjectProperty(IRI.create("http://org.semanticweb.owlapi/prop")));
         final OWLClassExpression filler = dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/cls"));
         final OWLObjectSomeValuesFromImpl in = new OWLObjectSomeValuesFromImpl(property, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectSomeValuesFromImpl(in, new AsyncCallback<OWLObjectSomeValuesFromImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectSomeValuesFromImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectAllValuesFromImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLObjectPropertyExpression property = dataFactory.getOWLObjectInverseOf(dataFactory.getOWLObjectProperty(IRI.create("http://org.semanticweb.owlapi/prop")));
         final OWLClassExpression filler = dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/cls"));
         final OWLObjectAllValuesFromImpl in = new OWLObjectAllValuesFromImpl(property, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectAllValuesFromImpl(in, new AsyncCallback<OWLObjectAllValuesFromImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectAllValuesFromImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectHasValueImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLObjectPropertyExpression property = dataFactory.getOWLObjectInverseOf(dataFactory.getOWLObjectProperty(IRI.create("http://org.semanticweb.owlapi/prop")));
         final OWLIndividual filler = dataFactory.getOWLNamedIndividual(IRI.create("http://org.semanticweb.owlapi/ind"));
         final OWLObjectHasValueImpl in = new OWLObjectHasValueImpl(property, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectHasValueImpl(in, new AsyncCallback<OWLObjectHasValueImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectHasValueImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectMinCardinalityImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLObjectPropertyExpression property = dataFactory.getOWLObjectInverseOf(dataFactory.getOWLObjectProperty(IRI.create("http://org.semanticweb.owlapi/prop")));
         final OWLClassExpression filler = dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/cls"));
         final OWLObjectMinCardinalityImpl in = new OWLObjectMinCardinalityImpl(property, 3, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectMinCardinalityImpl(in, new AsyncCallback<OWLObjectMinCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectMinCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectMaxCardinalityImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLObjectPropertyExpression property = dataFactory.getOWLObjectInverseOf(dataFactory.getOWLObjectProperty(IRI.create("http://org.semanticweb.owlapi/prop")));
         final OWLClassExpression filler = dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/cls"));
         final OWLObjectMaxCardinalityImpl in = new OWLObjectMaxCardinalityImpl(property, 3, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectMaxCardinalityImpl(in, new AsyncCallback<OWLObjectMaxCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectMaxCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectExactCardinalityImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLObjectPropertyExpression property = dataFactory.getOWLObjectInverseOf(dataFactory.getOWLObjectProperty(IRI.create("http://org.semanticweb.owlapi/prop")));
         final OWLClassExpression filler = dataFactory.getOWLClass(IRI.create("http://org.semanticweb.owlapi/cls"));
         final OWLObjectExactCardinalityImpl in = new OWLObjectExactCardinalityImpl(property, 3, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectExactCardinalityImpl(in, new AsyncCallback<OWLObjectExactCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectExactCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectHasSeltImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLObjectPropertyExpression property = dataFactory.getOWLObjectInverseOf(dataFactory.getOWLObjectProperty(IRI.create("http://org.semanticweb.owlapi/prop")));
         final OWLObjectHasSelfImpl in = new OWLObjectHasSelfImpl(property);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectHasSelfImpl(in, new AsyncCallback<OWLObjectHasSelfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectHasSelfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
 
     public void testShouldSerializeOWLDataIntersectionOfImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         final OWLDatatype dtA = dataFactory.getOWLDatatype(IRI.create("http://org.semanticweb.owlapi/dtA"));
         final OWLDatatype dtB= dataFactory.getOWLDatatype(IRI.create("http://org.semanticweb.owlapi/dtB"));
         final OWLDatatype dtC = dataFactory.getOWLDatatype(IRI.create("http://org.semanticweb.owlapi/dtC"));
        Set<OWLDatatype> operands = CollectionFactory.createSet(dtA, dtB, dtC);
         final OWLDataIntersectionOfImpl in = new OWLDataIntersectionOfImpl(operands);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDataIntersectionOfImpl(in, new AsyncCallback<OWLDataIntersectionOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataIntersectionOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
 
     public void testShouldSerializeOWLDataSomeValuesFromImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLDataPropertyExpression property = dataFactory.getOWLDataProperty(IRI.create("http://org.semanticweb.owlapi/prop"));
         final OWLDataRange filler = dataFactory.getOWLDatatype(IRI.create("http://org.semanticweb.owlapi/cls"));
         final OWLDataSomeValuesFromImpl in = new OWLDataSomeValuesFromImpl(property, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDataSomeValuesFromImpl(in, new AsyncCallback<OWLDataSomeValuesFromImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataSomeValuesFromImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataAllValuesFromImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLDataPropertyExpression property = dataFactory.getOWLDataProperty(IRI.create("http://org.semanticweb.owlapi/prop"));
         final OWLDataRange filler = dataFactory.getOWLDatatype(IRI.create("http://org.semanticweb.owlapi/cls"));
         final OWLDataAllValuesFromImpl in = new OWLDataAllValuesFromImpl(property, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDataAllValuesFromImpl(in, new AsyncCallback<OWLDataAllValuesFromImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataAllValuesFromImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataHasValueImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLDataPropertyExpression property = dataFactory.getOWLDataProperty(IRI.create("http://org.semanticweb.owlapi/prop"));
         final OWLLiteral filler = dataFactory.getOWLLiteral("Test");
         final OWLDataHasValueImpl in = new OWLDataHasValueImpl(property, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDataHasValueImpl(in, new AsyncCallback<OWLDataHasValueImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataHasValueImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataMinCardinalityImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLDataPropertyExpression property = dataFactory.getOWLDataProperty(IRI.create("http://org.semanticweb.owlapi/prop"));
         final OWLDataRange filler = dataFactory.getOWLDatatype(IRI.create("http://org.semanticweb.owlapi/cls"));
         final OWLDataMinCardinalityImpl in = new OWLDataMinCardinalityImpl(property, 3, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDataMinCardinalityImpl(in, new AsyncCallback<OWLDataMinCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataMinCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataMaxCardinalityImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLDataPropertyExpression property = dataFactory.getOWLDataProperty(IRI.create("http://org.semanticweb.owlapi/prop"));
         final OWLDataRange filler = dataFactory.getOWLDatatype(IRI.create("http://org.semanticweb.owlapi/cls"));
         final OWLDataMaxCardinalityImpl in = new OWLDataMaxCardinalityImpl(property, 3, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDataMaxCardinalityImpl(in, new AsyncCallback<OWLDataMaxCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataMaxCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataExactCardinalityImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl(false, false);
         OWLDataPropertyExpression property = dataFactory.getOWLDataProperty(IRI.create("http://org.semanticweb.owlapi/prop"));
         final OWLDataRange filler = dataFactory.getOWLDatatype(IRI.create("http://org.semanticweb.owlapi/cls"));
         final OWLDataExactCardinalityImpl in = new OWLDataExactCardinalityImpl(property, 3, filler);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDataExactCardinalityImpl(in, new AsyncCallback<OWLDataExactCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataExactCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDatatypeRestrictionImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         OWLDatatype datatype = df.getOWLDatatype(IRI.create("http://org.semanticweb.owlapi.gwt#A"));
         Set<OWLFacetRestriction> facetRestrictions = new HashSet<OWLFacetRestriction>();
         facetRestrictions.add(df.getOWLFacetRestriction(OWLFacet.MIN_LENGTH, df.getOWLLiteral("3")));
         facetRestrictions.add(df.getOWLFacetRestriction(OWLFacet.MAX_LENGTH, df.getOWLLiteral("4")));
         final OWLDatatypeRestrictionImpl in = new OWLDatatypeRestrictionImpl(datatype, facetRestrictions);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDatatypeRestrictionImpl(in, new AsyncCallback<OWLDatatypeRestrictionImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDatatypeRestrictionImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
 
     public void testShouldSerializeOWLSubClassOfAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLClassExpression subCls = df.getOWLClass(IRI.create("http://stuff.com/A"));
         OWLClassExpression supCls = df.getOWLClass(IRI.create("http://stuff.com/B"));
         final OWLSubClassOfAxiomImpl ax = new OWLSubClassOfAxiomImpl(subCls, supCls, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLSubClassOfAxiomImpl(ax, new AsyncCallback<OWLSubClassOfAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSubClassOfAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLEquivalentClassesAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLClassExpression clsA = df.getOWLClass(IRI.create("http://stuff.com/A"));
         OWLClassExpression clsB = df.getOWLClass(IRI.create("http://stuff.com/B"));
         OWLClassExpression clsC = df.getOWLClass(IRI.create("http://stuff.com/C"));
         Set<OWLClassExpression> clses = CollectionFactory.createSet(clsA, clsB, clsC);
         final OWLEquivalentClassesAxiomImpl ax = new OWLEquivalentClassesAxiomImpl(clses, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLEquivalentClassesAxiomImpl(ax, new AsyncCallback<OWLEquivalentClassesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLEquivalentClassesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDisjointClassesAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLClassExpression clsA = df.getOWLClass(IRI.create("http://stuff.com/A"));
         OWLClassExpression clsB = df.getOWLClass(IRI.create("http://stuff.com/B"));
         OWLClassExpression clsC = df.getOWLClass(IRI.create("http://stuff.com/C"));
         Set<OWLClassExpression> clses = CollectionFactory.createSet(clsA, clsB, clsC);
         final OWLDisjointClassesAxiomImpl ax = new OWLDisjointClassesAxiomImpl(clses, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDisjointClassesAxiomImpl(ax, new AsyncCallback<OWLDisjointClassesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDisjointClassesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDisjointUnionAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLClassExpression clsA = df.getOWLClass(IRI.create("http://stuff.com/A"));
         OWLClassExpression clsB = df.getOWLClass(IRI.create("http://stuff.com/B"));
         OWLClassExpression clsC = df.getOWLClass(IRI.create("http://stuff.com/C"));
         OWLClass clsX = df.getOWLClass(IRI.create("http://stuff.com/X"));
         Set<OWLClassExpression> clses = CollectionFactory.createSet(clsA, clsB, clsC);
         final OWLDisjointUnionAxiomImpl ax = new OWLDisjointUnionAxiomImpl(clsX, clses, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDisjointUnionAxiomImpl(ax, new AsyncCallback<OWLDisjointUnionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDisjointUnionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLClassAssertionAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLClassExpression clsX = df.getOWLClass(IRI.create("http://stuff.com/X"));
         OWLIndividual indA = df.getOWLNamedIndividual(IRI.create("http://stuff.com/a"));
         final OWLClassAssertionAxiomImpl ax = new OWLClassAssertionAxiomImpl(indA, clsX, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLClassAssertionAxiomImpl(ax, new AsyncCallback<OWLClassAssertionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLClassAssertionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectPropertyAssertionAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression property = df.getOWLObjectProperty(IRI.create("http://stuff.com/prop"));
         OWLIndividual indA = df.getOWLNamedIndividual(IRI.create("http://stuff.com/a"));
         OWLIndividual indB = df.getOWLNamedIndividual(IRI.create("http://stuff.com/a"));
         final OWLObjectPropertyAssertionAxiomImpl ax = new OWLObjectPropertyAssertionAxiomImpl(indA, property, indB, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectPropertyAssertionAxiomImpl(ax, new AsyncCallback<OWLObjectPropertyAssertionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectPropertyAssertionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLNegativeObjectPropertyAssertionAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectProperty property = df.getOWLObjectProperty(IRI.create("http://stuff.com/prop"));
         OWLIndividual indA = df.getOWLNamedIndividual(IRI.create("http://stuff.com/a"));
         OWLIndividual indB = df.getOWLNamedIndividual(IRI.create("http://stuff.com/a"));
         final OWLNegativeObjectPropertyAssertionAxiomImpl ax = new OWLNegativeObjectPropertyAssertionAxiomImpl(indA, property, indB, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLNegativeObjectPropertyAssertionAxiomImpl(ax, new AsyncCallback<OWLNegativeObjectPropertyAssertionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLNegativeObjectPropertyAssertionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataPropertyAssertionAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLDataPropertyExpression property = df.getOWLDataProperty(IRI.create("http://stuff.com/prop"));
         OWLIndividual indA = df.getOWLNamedIndividual(IRI.create("http://stuff.com/a"));
         OWLLiteral lit = df.getOWLLiteral("x");
         final OWLDataPropertyAssertionAxiomImpl ax = new OWLDataPropertyAssertionAxiomImpl(indA, property, lit, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDataPropertyAssertionAxiomImpl(ax, new AsyncCallback<OWLDataPropertyAssertionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataPropertyAssertionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLNegativeDataPropertyAssertionAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLDataProperty property = df.getOWLDataProperty(IRI.create("http://stuff.com/prop"));
         OWLIndividual indA = df.getOWLNamedIndividual(IRI.create("http://stuff.com/a"));
         OWLLiteral lit = df.getOWLLiteral("x");
         final OWLNegativeDataPropertyAssertionAxiomImpl ax = new OWLNegativeDataPropertyAssertionAxiomImpl(indA, property, lit, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLNegativeDataPropertyAssertionAxiomImpl(ax, new AsyncCallback<OWLNegativeDataPropertyAssertionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLNegativeDataPropertyAssertionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLSameIndividualAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLIndividual indA = df.getOWLNamedIndividual(IRI.create("http://stuff.com/a"));
         OWLIndividual indB = df.getOWLNamedIndividual(IRI.create("http://stuff.com/b"));
         OWLIndividual indC = df.getOWLNamedIndividual(IRI.create("http://stuff.com/c"));
         Set<OWLIndividual> inds = CollectionFactory.createSet(indA, indB, indC);
         final OWLSameIndividualAxiomImpl ax = new OWLSameIndividualAxiomImpl(inds, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLSameIndividualAxiomImpl(ax, new AsyncCallback<OWLSameIndividualAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSameIndividualAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDifferentIndividualsAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLIndividual indA = df.getOWLNamedIndividual(IRI.create("http://stuff.com/a"));
         OWLIndividual indB = df.getOWLNamedIndividual(IRI.create("http://stuff.com/b"));
         OWLIndividual indC = df.getOWLNamedIndividual(IRI.create("http://stuff.com/c"));
         Set<OWLIndividual> inds = CollectionFactory.createSet(indA, indB, indC);
         final OWLDifferentIndividualsAxiomImpl ax = new OWLDifferentIndividualsAxiomImpl(inds, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDifferentIndividualsAxiomImpl(ax, new AsyncCallback<OWLDifferentIndividualsAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDifferentIndividualsAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLSubObjectPropertyOfAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         OWLObjectPropertyExpression propB = df.getOWLObjectProperty(IRI.create("http://stuff.com/propB"));
         final OWLSubObjectPropertyOfAxiomImpl ax = new OWLSubObjectPropertyOfAxiomImpl(propA, propB, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLSubObjectPropertyOfAxiomImpl(ax, new AsyncCallback<OWLSubObjectPropertyOfAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSubObjectPropertyOfAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLEquivalentObjectPropertiesAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         OWLObjectPropertyExpression propB = df.getOWLObjectProperty(IRI.create("http://stuff.com/propB"));
         Set<OWLObjectPropertyExpression> props = CollectionFactory.createSet(propA, propB);
         final OWLEquivalentObjectPropertiesAxiomImpl ax = new OWLEquivalentObjectPropertiesAxiomImpl(props, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLEquivalentObjectPropertiesAxiomImpl(ax, new AsyncCallback<OWLEquivalentObjectPropertiesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLEquivalentObjectPropertiesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDisjointObjectPropertiesAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         OWLObjectPropertyExpression propB = df.getOWLObjectProperty(IRI.create("http://stuff.com/propB"));
         Set<OWLObjectPropertyExpression> props = CollectionFactory.createSet(propA, propB);
         final OWLDisjointObjectPropertiesAxiomImpl ax = new OWLDisjointObjectPropertiesAxiomImpl(props, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDisjointObjectPropertiesAxiomImpl(ax, new AsyncCallback<OWLDisjointObjectPropertiesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDisjointObjectPropertiesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLInverseObjectPropertiesAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         OWLObjectPropertyExpression propB = df.getOWLObjectProperty(IRI.create("http://stuff.com/propB"));
         final OWLInverseObjectPropertiesAxiomImpl ax = new OWLInverseObjectPropertiesAxiomImpl(propA, propB, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLInverseObjectPropertiesAxiomImpl(ax, new AsyncCallback<OWLInverseObjectPropertiesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLInverseObjectPropertiesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectPropertyDomainAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         OWLClassExpression clsA = df.getOWLClass(IRI.create("http://stuff.com/ClsA"));
         final OWLObjectPropertyDomainAxiomImpl ax = new OWLObjectPropertyDomainAxiomImpl(propA, clsA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectPropertyDomainAxiomImpl(ax, new AsyncCallback<OWLObjectPropertyDomainAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectPropertyDomainAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectPropertyRangeAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         OWLClassExpression clsA = df.getOWLClass(IRI.create("http://stuff.com/ClsA"));
         final OWLObjectPropertyRangeAxiomImpl ax = new OWLObjectPropertyRangeAxiomImpl(propA, clsA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLObjectPropertyRangeAxiomImpl(ax, new AsyncCallback<OWLObjectPropertyRangeAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectPropertyRangeAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLFunctionalObjectPropertyAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         final OWLFunctionalObjectPropertyAxiomImpl ax = new OWLFunctionalObjectPropertyAxiomImpl(propA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLFunctionalObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLFunctionalObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLFunctionalObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLInverseFunctionalObjectPropertyAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         final OWLInverseFunctionalObjectPropertyAxiomImpl ax = new OWLInverseFunctionalObjectPropertyAxiomImpl(propA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLInverseFunctionalObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLInverseFunctionalObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLInverseFunctionalObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLReflexiveObjectPropertyAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         final OWLReflexiveObjectPropertyAxiomImpl ax = new OWLReflexiveObjectPropertyAxiomImpl(propA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLReflexiveObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLReflexiveObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLReflexiveObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLIrreflexiveObjectPropertyAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         final OWLIrreflexiveObjectPropertyAxiomImpl ax = new OWLIrreflexiveObjectPropertyAxiomImpl(propA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLIrreflexiveObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLIrreflexiveObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLIrreflexiveObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLSymmetricObjectPropertyAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         final OWLSymmetricObjectPropertyAxiomImpl ax = new OWLSymmetricObjectPropertyAxiomImpl(propA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLSymmetricObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLSymmetricObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSymmetricObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLAsymmetricObjectPropertyAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         final OWLAsymmetricObjectPropertyAxiomImpl ax = new OWLAsymmetricObjectPropertyAxiomImpl(propA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLAsymmetricObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLAsymmetricObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLAsymmetricObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLTransitiveObjectPropertyAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLObjectPropertyExpression propA = df.getOWLObjectProperty(IRI.create("http://stuff.com/propA"));
         final OWLTransitiveObjectPropertyAxiomImpl ax = new OWLTransitiveObjectPropertyAxiomImpl(propA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLTransitiveObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLTransitiveObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLTransitiveObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLSubDataPropertyOfAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLDataPropertyExpression propA = df.getOWLDataProperty(IRI.create("http://stuff.com/propA"));
         OWLDataPropertyExpression propB = df.getOWLDataProperty(IRI.create("http://stuff.com/propB"));
         final OWLSubDataPropertyOfAxiomImpl ax = new OWLSubDataPropertyOfAxiomImpl(propA, propB, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLSubDataPropertyOfAxiomImpl(ax, new AsyncCallback<OWLSubDataPropertyOfAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSubDataPropertyOfAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLEquivalentDataPropertiesAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLDataPropertyExpression propA = df.getOWLDataProperty(IRI.create("http://stuff.com/propA"));
         OWLDataPropertyExpression propB = df.getOWLDataProperty(IRI.create("http://stuff.com/propB"));
         Set<OWLDataPropertyExpression> props = CollectionFactory.createSet(propA, propB);
         final OWLEquivalentDataPropertiesAxiomImpl ax = new OWLEquivalentDataPropertiesAxiomImpl(props, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLEquivalentDataPropertiesAxiomImpl(ax, new AsyncCallback<OWLEquivalentDataPropertiesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLEquivalentDataPropertiesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDisjointDataPropertiesAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLDataPropertyExpression propA = df.getOWLDataProperty(IRI.create("http://stuff.com/propA"));
         OWLDataPropertyExpression propB = df.getOWLDataProperty(IRI.create("http://stuff.com/propB"));
         Set<OWLDataPropertyExpression> props = CollectionFactory.createSet(propA, propB);
         final OWLDisjointDataPropertiesAxiomImpl ax = new OWLDisjointDataPropertiesAxiomImpl(props, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDisjointDataPropertiesAxiomImpl(ax, new AsyncCallback<OWLDisjointDataPropertiesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDisjointDataPropertiesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataPropertyDomainAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLDataPropertyExpression propA = df.getOWLDataProperty(IRI.create("http://stuff.com/propA"));
         OWLClassExpression clsA = df.getOWLClass(IRI.create("http://stuff.com/ClsA"));
         final OWLDataPropertyDomainAxiomImpl ax = new OWLDataPropertyDomainAxiomImpl(propA, clsA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDataPropertyDomainAxiomImpl(ax, new AsyncCallback<OWLDataPropertyDomainAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataPropertyDomainAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataPropertyRangeAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLDataPropertyExpression propA = df.getOWLDataProperty(IRI.create("http://stuff.com/propA"));
         OWLDataRange clsA = df.getOWLDatatype(IRI.create("http://stuff.com/TypeA"));
         final OWLDataPropertyRangeAxiomImpl ax = new OWLDataPropertyRangeAxiomImpl(propA, clsA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDataPropertyRangeAxiomImpl(ax, new AsyncCallback<OWLDataPropertyRangeAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataPropertyRangeAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLFunctionalDataPropertyAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLDataPropertyExpression propA = df.getOWLDataProperty(IRI.create("http://stuff.com/propA"));
         final OWLFunctionalDataPropertyAxiomImpl ax = new OWLFunctionalDataPropertyAxiomImpl(propA, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLFunctionalDataPropertyAxiomImpl(ax, new AsyncCallback<OWLFunctionalDataPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLFunctionalDataPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
 
     public void testShouldSerializeOWLSubAnnotationPropertyOfAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLAnnotationProperty propA = df.getOWLAnnotationProperty(IRI.create("http://stuff.com/propA"));
         OWLAnnotationProperty propB = df.getOWLAnnotationProperty(IRI.create("http://stuff.com/propB"));
         final OWLSubAnnotationPropertyOfAxiomImpl ax = new OWLSubAnnotationPropertyOfAxiomImpl(propA, propB, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLSubAnnotationPropertyOfAxiomImpl(ax, new AsyncCallback<OWLSubAnnotationPropertyOfAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSubAnnotationPropertyOfAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLAnnotationPropertyDomainAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLAnnotationProperty propA = df.getOWLAnnotationProperty(IRI.create("http://stuff.com/propA"));
         OWLClass clsA = df.getOWLClass(IRI.create("http://stuff.com/ClsA"));
         final OWLAnnotationPropertyDomainAxiomImpl ax = new OWLAnnotationPropertyDomainAxiomImpl(propA, clsA.getIRI(), annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLAnnotationPropertyDomainAxiomImpl(ax, new AsyncCallback<OWLAnnotationPropertyDomainAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLAnnotationPropertyDomainAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLAnnotationPropertyRangeAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLAnnotationProperty propA = df.getOWLAnnotationProperty(IRI.create("http://stuff.com/propA"));
         OWLClass clsA = df.getOWLClass(IRI.create("http://stuff.com/ClsA"));
         final OWLAnnotationPropertyRangeAxiomImpl ax = new OWLAnnotationPropertyRangeAxiomImpl(propA, clsA.getIRI(), annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLAnnotationPropertyRangeAxiomImpl(ax, new AsyncCallback<OWLAnnotationPropertyRangeAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLAnnotationPropertyRangeAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDatatypeDefinitionAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLDatatype dtA = df.getOWLDatatype(IRI.create("http://stuff.com/DtA"));
         OWLDatatype dtB = df.getOWLDatatype(IRI.create("http://stuff.com/DtB"));
         final OWLDatatypeDefinitionAxiomImpl ax = new OWLDatatypeDefinitionAxiomImpl(dtA, dtB, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDatatypeDefinitionAxiomImpl(ax, new AsyncCallback<OWLDatatypeDefinitionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDatatypeDefinitionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLHasKeyAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLClass cls = df.getOWLClass(IRI.create("http://stuff.com/ClsA"));
         OWLDataProperty dpA = df.getOWLDataProperty(IRI.create("http://stuff.com/propA"));
         OWLDataProperty dpB = df.getOWLDataProperty(IRI.create("http://stuff.com/propB"));
         OWLObjectProperty opA = df.getOWLObjectProperty(IRI.create("http://stuff.com/opPropA"));
         OWLObjectProperty opB = df.getOWLObjectProperty(IRI.create("http://stuff.com/opPropB"));
         Set<OWLPropertyExpression<?,?>> props = CollectionFactory.<OWLPropertyExpression<?,?>>createSet(dpA, dpB, opA, opB);
         final OWLHasKeyAxiomImpl ax = new OWLHasKeyAxiomImpl(cls, props, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLHasKeyAxiomImpl(ax, new AsyncCallback<OWLHasKeyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLHasKeyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDeclarationAxiomImpl() {
         delayTestFinish(TEST_DELAY_MS);
         OWLDataFactory df = new OWLDataFactoryImpl(false, false);
         Set<OWLAnnotation> annos = Collections.singleton(df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Hello")));
         OWLClass cls = df.getOWLClass(IRI.create("http://stuff.com/ClsA"));
         final OWLDeclarationAxiomImpl ax = new OWLDeclarationAxiomImpl(cls, annos);
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLDeclarationAxiomImpl(ax, new AsyncCallback<OWLDeclarationAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDeclarationAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLImportsDeclarationImpl() {
         delayTestFinish(TEST_DELAY_MS);
         final OWLImportsDeclarationImpl decl = new OWLImportsDeclarationImpl(IRI.create("http://stuff.com/ont"));
         OWLObjectSerializationTestsServiceAsync service = GWT.create(OWLObjectSerializationTestsService.class);
         service.testOWLImportsDeclarationImpl(decl, new AsyncCallback<OWLImportsDeclarationImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLImportsDeclarationImpl out) {
                 assertEquals(decl, out);
                 finishTest();
             }
         });
     }
 
 
 }
