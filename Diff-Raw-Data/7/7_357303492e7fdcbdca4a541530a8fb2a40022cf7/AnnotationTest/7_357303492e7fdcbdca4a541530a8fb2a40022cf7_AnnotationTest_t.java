 package pl.psnc.dl.wf4ever.model.AO;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.model.BaseTest;
 import pl.psnc.dl.wf4ever.model.EvoBuilder;
 import pl.psnc.dl.wf4ever.model.SnapshotBuilder;
 import pl.psnc.dl.wf4ever.model.RDF.Thing;
 
 /**
  * Test class for AO.Annotation model.
  * 
  * @author pejot
  * 
  */
 public class AnnotationTest extends BaseTest {
 
     @Override
     @Before
     public void setUp()
             throws Exception {
         super.setUp();
     }
 
 
     @Test
     public void testAnnotation() {
         Annotation annotation = new Annotation(userProfile, dataset, true, researchObject, researchObject.getUri()
                 .resolve("some-uri"));
         Assert.assertEquals(annotation.getResearchObject(), researchObject);
     }
 
 
     @Test
     public void testCreateAnnotationWithExternalBody()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation annotation = Annotation.create(builder, researchObject,
             researchObject.getUri().resolve("new-annotation"), is);
         Set<Thing> expectedTarges = new HashSet<Thing>();
         expectedTarges.add(researchObject);
 
         Assert.assertEquals(annotation.getUri(), researchObject.getUri().resolve("new-annotation"));
         Assert.assertEquals(annotation.getBody().getUri(), URI.create("http://example.org/external.txt"));
         Assert.assertEquals(annotation.getName(), "new-annotation");
         Assert.assertEquals(annotation.getAnnotated(), expectedTarges);
     }
 
 
     @Test
     public void testCreateAnnotationWithInteralBody()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann2.rdf");
         Annotation annotation = Annotation.create(builder, researchObject,
             researchObject.getUri().resolve("new-annotation"), is);
         Set<Thing> expectedTarges = new HashSet<Thing>();
         expectedTarges.add(researchObject);
         Assert.assertEquals(annotation.getUri(), researchObject.getUri().resolve("new-annotation"));
         Assert.assertEquals(annotation.getBody().getUri(), researchObject.getUri().resolve("ann2-body.txt"));
         Assert.assertEquals(annotation.getName(), "new-annotation");
         Assert.assertEquals(annotation.getAnnotated(), expectedTarges);
     }
 
 
     @Test
     public void testCreateAnnotationWithManyTargets()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/multi-targets.rdf");
         Annotation annotation = Annotation.create(builder, researchObject,
             researchObject.getUri().resolve("new-annotation"), is);
         Set<Thing> expectedTargets = new HashSet<Thing>();
 
         expectedTargets.add(builder.buildResource(researchObject.getUri().resolve("a%20workflow.t2flow"),
             researchObject, userProfile, null));
         expectedTargets.add(researchObject);
 
         Assert.assertEquals(annotation.getUri(), researchObject.getUri().resolve("new-annotation"));
         Assert.assertEquals(annotation.getBody().getUri(), URI.create("http://example.org/external.txt"));
         Assert.assertEquals(annotation.getName(), "new-annotation");
         Assert.assertEquals(annotation.getAnnotated(), expectedTargets);
 
     }
 
 
     @Test(expected = ConflictException.class)
     public void testCreateAnnotationWithThisSameUri()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation annotation = Annotation.create(builder, researchObject,
             researchObject.getUri().resolve("new-annotation"), is);
         researchObject.aggregate(annotation.getUri());
         is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation conflictedAnnotation = Annotation.create(builder, researchObject,
             researchObject.getUri().resolve("new-annotation"), is);
         researchObject.aggregate(conflictedAnnotation.getUri());
     }
 
 
     @Test(expected = NullPointerException.class)
     public void testCreateAnnotationWithNoContent()
             throws BadRequestException {
         Annotation.create(builder, researchObject, researchObject.getUri().resolve("new-annotation"), null);
     }
 
 
     @Test
     public void testCreateAnnotationWithNotExistentInternalBody()
             throws BadRequestException {
         // URI suggests an internal resource which does not exist but this is acceptable because it may be added later
         InputStream is = getClass().getClassLoader().getResourceAsStream(
             "model/ao/annotation/annotation-wrong-body.rdf");
         Annotation.create(builder, researchObject, researchObject.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test(expected = BadRequestException.class)
     public void testCreateAnnotationWithNoBody()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/no-body.rdf");
         Annotation.create(builder, researchObject, researchObject.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test(expected = BadRequestException.class)
     public void testCreateAnnotationWithNoTargets()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/no-targets.rdf");
         Annotation.create(builder, researchObject, researchObject.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test(expected = BadRequestException.class)
     public void testCreateAnnotationWithNoBodyNoTargets()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/no-body-no-targets.rdf");
         Annotation.create(builder, researchObject, researchObject.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test(expected = BadRequestException.class)
     public void testCreateAnnotationWithNoRDF()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/no-rdf.rdf");
         Annotation.create(builder, researchObject, researchObject.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test(expected = BadRequestException.class)
     public void testCreateEmptyAnnotation()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/empty.rdf");
         Annotation.create(builder, researchObject, researchObject.getUri().resolve("new-annotation"), is);
     }
 
 
     @Test
     public void testCopyWithExternalBody()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation annotation = Annotation.create(builder, researchObject,
             researchObject.getUri().resolve("new-annotation"), is);
         EvoBuilder evoBuilder = new SnapshotBuilder();
         Annotation annotationCopy = annotation.copy(builder, evoBuilder, researchObject2);
         Assert.assertEquals(annotationCopy.getUri().relativize(researchObject2.getUri()), (researchObject2.getUri()));
         Assert.assertEquals(annotationCopy.getBody().getUri(), (annotation.getBody().getUri()));
     }
 
 
     @Test
     public void testCopyWithInternalBody()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann2.rdf");
         Annotation annotation = Annotation.create(builder, researchObject,
             researchObject.getUri().resolve("new-annotation"), is);
         EvoBuilder evoBuilder = new SnapshotBuilder();
         Annotation annotationCopy = annotation.copy(builder, evoBuilder, researchObject2);
         Assert.assertEquals(annotationCopy.getUri().relativize(researchObject2.getUri()), (researchObject2.getUri()));
         Assert.assertEquals(annotationCopy.getBody().getUri().relativize(researchObject2.getUri()),
             (researchObject2.getUri()));
     }
 
 
     @Test
     public void testDelete() {
         Set<Thing> annotated = new HashSet<Thing>();
         annotated.add(researchObject);
        Annotation annotation = builder.buildAnnotation(URI.create("new-annotation-save-test"), researchObject,
            builder.buildThing(URI.create("body")), annotated);
         annotation.save();
         Assert.assertTrue(researchObject.getAnnotations().containsKey(annotation.getUri()));
         annotation.delete();
         Assert.assertFalse(researchObject.getAnnotations().containsKey(annotation.getUri()));
     }
 
 
     @Test
     public void testAssemble()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation annotation = Annotation.assemble(builder, researchObject,
             researchObject.getUri().resolve("assemble-test"), is);
         Set<Thing> expectedTarges = new HashSet<Thing>();
         expectedTarges.add(researchObject);
 
         Assert.assertEquals(annotation.getUri(), researchObject.getUri().resolve("assemble-test"));
         Assert.assertEquals(annotation.getBody().getUri(), URI.create("http://example.org/external.txt"));
         Assert.assertEquals(annotation.getName(), "assemble-test");
         Assert.assertEquals(annotation.getAnnotated(), expectedTarges);
     }
 
 
     @Test
     public void testUpdate()
             throws BadRequestException {
         InputStream is = getClass().getClassLoader().getResourceAsStream("model/ao/annotation/ann1.rdf");
         Annotation annotation = Annotation.assemble(builder, researchObject,
             researchObject.getUri().resolve("assemble-test"), is);
         Annotation newAnnotation = new Annotation(userProfile, dataset, false, researchObject,
                 URI.create("new-annotation"));
         Set<Thing> annotated = new HashSet<Thing>();
         annotated.add(builder.buildThing(researchObject.getUri().resolve("a%20workflow.t2flow")));
         newAnnotation.setAnnotated(annotated);
         Thing newBody = builder.buildThing(URI.create("http://example.org/external"));
         newAnnotation.setBody(newBody);
         annotation.update(newAnnotation);
         Assert.assertEquals(annotation.getBody(), newBody);
         Assert.assertEquals(annotation.getAnnotated(), annotated);
 
     }
 
 
     @Test
     public void testIsSpecialResource() {
         Annotation ordinaryAnnotation1 = builder.buildAnnotation(
             URI.create("http://www.example.com/ROs/ro/annotations/ordinaryAnnotation1"), researchObject,
             builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/manifest.rdf/ordinary")),
             new HashSet<Thing>(Arrays.asList(researchObject)));
 
         Annotation ordinaryAnnotation2 = builder.buildAnnotation(
             URI.create("http://www.example.com/ROs/ro/annotations/ordinaryAnnotation1"), researchObject,
             builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/evo_info.ttlordinary")),
             new HashSet<Thing>(Arrays.asList(researchObject)));
 
         Annotation ordinaryAnnotation3 = builder.buildAnnotation(
             URI.create("http://www.example.com/ROs/ro/annotations/evo_info.ttl/annotation"), researchObject,
             builder.buildThing(URI.create("http://www.example.com/ROs/ro/annotations/evo_info.ttl/annotation")),
             new HashSet<Thing>(Arrays.asList(researchObject)));
 
         Annotation specialAnnotation1 = builder.buildAnnotation(
             URI.create("http://www.example.com/ROs/ro/annotations/annotation"), researchObject,
             builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/manifest.rdf")),
             new HashSet<Thing>(Arrays.asList(researchObject)));
 
         Annotation specialAnnotation2 = builder.buildAnnotation(
             URI.create("http://www.example.com/ROs/ro/annotations/annotation"), researchObject,
             builder.buildThing(URI.create("http://www.example.com/ROs/ro/.ro/evo_info.ttl")),
             new HashSet<Thing>(Arrays.asList(researchObject)));
 
         Annotation specialAnnotation3 = builder.buildAnnotation(URI
                 .create("http://www.example.com/ROs/ro/annotations/annotation"), researchObject, builder.buildThing(URI
                 .create("http://www.example.com/ROs/ro/.ro/evo_info.ttl/")),
             new HashSet<Thing>(Arrays.asList(researchObject)));
 
         Assert.assertFalse(ordinaryAnnotation1.isSpecialResource());
         Assert.assertFalse(ordinaryAnnotation2.isSpecialResource());
         Assert.assertFalse(ordinaryAnnotation3.isSpecialResource());
         Assert.assertTrue(specialAnnotation1.isSpecialResource());
         Assert.assertTrue(specialAnnotation2.isSpecialResource());
         Assert.assertTrue(specialAnnotation3.isSpecialResource());
     }
 }
