 /* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
    This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
    http://www.cs.umass.edu/~mccallum/mallet
    This software is provided under the terms of the Common Public License,
    version 1.0, as published by http://www.opensource.org.  For further
    information, see the file `LICENSE' included with this distribution. */
 package cc.mallet.extract.test;
 
 import cc.mallet.extract.CRFExtractor;
 import cc.mallet.extract.DocumentViewer;
 import cc.mallet.extract.Extraction;
 import cc.mallet.fst.CRF;
 import cc.mallet.fst.CRFTrainerByLabelLikelihood;
 import cc.mallet.fst.tests.TestCRF;
 import cc.mallet.fst.tests.TestMEMM;
 import cc.mallet.pipe.Pipe;
 import cc.mallet.pipe.iterator.ArrayIterator;
 import cc.mallet.types.InstanceList;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import java.io.File;
 import java.io.IOException;
 
 
 /**
  * Created: Mar 30, 2005
  *
  * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
  * @version $Id: TestDocumentViewer.java,v 1.1 2007/10/22 21:38:02 mccallum Exp $
  */
 public class TestDocumentViewer extends TestCase {
 
     public TestDocumentViewer(String name) {
         super(name);
     }
 
     public static Test suite() {
         return new TestSuite(TestDocumentViewer.class);
     }
 
     private File outputDir = new File("extract");
    //TODO: Remove the placeholder test once, tests are fixed.
    public void testFake() {
 
    }

    //FIXME: (Need to understand Mallet Pipes...) In the code instance.getData() is expected to be of any-type (CharSequence, Tokenization, TokenSequence or FeatureVectorSequence)
    public void donotTestSpaceViewer() throws IOException {
         Pipe pipe = TestMEMM.makeSpacePredictionPipe();
         String[] data0 = {TestCRF.data[0]};
         String[] data1 = {TestCRF.data[1]};
 
         InstanceList training = new InstanceList(pipe);
         training.addThruPipe(new ArrayIterator(data0));
         InstanceList testing = new InstanceList(pipe);
         testing.addThruPipe(new ArrayIterator(data1));
 
         CRF crf = new CRF(pipe, null);
         crf.addFullyConnectedStatesForLabels();
         CRFTrainerByLabelLikelihood crft = new CRFTrainerByLabelLikelihood(crf);
         crft.trainIncremental(training);
 
         CRFExtractor extor = TestLatticeViewer.hackCrfExtor(crf);
         Extraction extraction = extor.extract(new ArrayIterator(data1));
 
         if (!outputDir.exists()) outputDir.mkdir();
         DocumentViewer.writeExtraction(outputDir, extraction);
     }
 
 
     public static void main(String[] args) throws Throwable {
         TestSuite theSuite;
         if (args.length > 0) {
             theSuite = new TestSuite();
             for (int i = 0; i < args.length; i++) {
                 theSuite.addTest(new TestDocumentViewer(args[i]));
             }
         } else {
             theSuite = (TestSuite) suite();
         }
 
         junit.textui.TestRunner.run(theSuite);
     }
 
 }
