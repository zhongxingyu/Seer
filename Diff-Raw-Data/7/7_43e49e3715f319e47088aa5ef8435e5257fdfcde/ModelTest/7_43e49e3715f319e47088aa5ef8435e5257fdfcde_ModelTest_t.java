 package org.roettig.MLToolbox.test.base;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import junit.framework.TestCase;
 
 import org.roettig.MLToolbox.base.Prediction;
 import org.roettig.MLToolbox.base.impl.DefaultInstanceContainer;
 import org.roettig.MLToolbox.base.instance.PrimalInstance;
 import org.roettig.MLToolbox.kernels.LinearKernel;
 import org.roettig.MLToolbox.kernels.RBFKernel;
 import org.roettig.MLToolbox.model.CSVCModel;
import org.roettig.MLToolbox.model.Model;
 import org.roettig.MLToolbox.model.NuSVCModel;
 import org.roettig.MLToolbox.model.NuSVRModel;
 import org.roettig.MLToolbox.model.OneClassSVM;
 import org.roettig.MLToolbox.test.data.DataSource;
 import org.roettig.MLToolbox.util.InstanceReader;
 import org.roettig.MLToolbox.util.MLHelper;
 import org.roettig.MLToolbox.util.SerialClone;
 import org.roettig.MLToolbox.validation.ModelValidation;
 
 
 public class ModelTest extends TestCase
 {
 	
 	public void testLIBSVM1() throws Exception
 	{
 		RBFKernel rbf = new RBFKernel();
 		rbf.setGamma(0.01);
 		rbf.doNormalize(false);
 		
 		DefaultInstanceContainer<PrimalInstance>  data = InstanceReader.read(DataSource.class.getResourceAsStream("iris.dat"), 5, true);
 
 		CSVCModel<PrimalInstance>                 m  = new CSVCModel<PrimalInstance>(rbf);
 		
 		m.setC(1.0);
 
 		DefaultInstanceContainer<PrimalInstance>  train = new DefaultInstanceContainer<PrimalInstance>();
 		DefaultInstanceContainer<PrimalInstance>  test  = new DefaultInstanceContainer<PrimalInstance>();
 		
 		ModelValidation.getStratifiedSplit(0.5, data, train, test);
 		
 		m.train(train);
 		List<Prediction> preds = m.predict(test);
 		assertEquals(0.9066666666666666,m.getQuality(preds),1e-4);
 		double objs[] = {-13.846634,-7.607551,-36.366686};
 		for(int i=0;i<3;i++)
 		{
 			assertEquals(objs[i],m.getObjectiveValue(i),1e-4);
 		}
 	}
 	
 	public void testLIBSVM2() throws Exception
 	{
 		LinearKernel lk = new LinearKernel();
 		lk.doNormalize(false);
 		
 		DefaultInstanceContainer<PrimalInstance>  data = InstanceReader.read(DataSource.class.getResourceAsStream("iris.dat"), 5, true);
 
 		CSVCModel<PrimalInstance>                 m  = new CSVCModel<PrimalInstance>(lk);
 		
 		m.setC(1.0);
 
 		DefaultInstanceContainer<PrimalInstance>  train = new DefaultInstanceContainer<PrimalInstance>();
 		DefaultInstanceContainer<PrimalInstance>  test  = new DefaultInstanceContainer<PrimalInstance>();
 		
 		ModelValidation.getStratifiedSplit(0.5, data, train, test);
 		
 		m.train(train);
 		List<Prediction> preds = m.predict(test);
 		assertEquals(0.946667,m.getQuality(preds),1e-4);
 		double objs[] = {-0.559475,-0.203684,-8.836843};
 		for(int i=0;i<3;i++)
 		{
 			assertEquals(objs[i],m.getObjectiveValue(i),1e-4);
 		}
 	}
 	
 	public void testLIBSVM3() throws Exception
 	{
 		RBFKernel rbf = new RBFKernel();
 		rbf.setGamma(1.0);
 		
 		DefaultInstanceContainer<PrimalInstance> samples = InstanceReader.read(DataSource.class.getResourceAsStream("sin.dat"), 2, false);
 		
 		NuSVRModel<PrimalInstance>     m  = new NuSVRModel<PrimalInstance>(rbf);
 		m.setC(10.0);
 		m.setNU(0.2);
 		
 		DefaultInstanceContainer<PrimalInstance>  train = new DefaultInstanceContainer<PrimalInstance>();
 		DefaultInstanceContainer<PrimalInstance>  test  = new DefaultInstanceContainer<PrimalInstance>();
 		
 		samples.shuffle(new Random(2204));
 		
 		ModelValidation.getSplit(0.8, samples, train, test);
 		
 		
 		m.train(train);
 		List<Prediction> preds = m.predict(test);
 
 		assertEquals(0.9913,m.getQuality(preds),1e-2);
 		
 		assertEquals(-661.761801,m.getObjectiveValue(0),1e-2);
 	}
 	
 	public void testLIBSVM4() throws Exception
 	{
 		RBFKernel rbf = new RBFKernel();
 		rbf.setGamma(1.0);
 		
 		DefaultInstanceContainer<PrimalInstance>  data = InstanceReader.read(DataSource.class.getResourceAsStream("iris.dat"), 5, true);
 
 		OneClassSVM<PrimalInstance>                 m  = new OneClassSVM<PrimalInstance>(rbf);
 		
 		m.setNu(0.05);
 
 		DefaultInstanceContainer<PrimalInstance>  train = new DefaultInstanceContainer<PrimalInstance>();
 		DefaultInstanceContainer<PrimalInstance>  test  = new DefaultInstanceContainer<PrimalInstance>();
 		
 		ModelValidation.getStratifiedSplit(0.80, data, train, test);
 		
 		m.train(train);
 		assertEquals(1.978206,m.getObjectiveValue(0),1e-5);
 		
 		List<Prediction> preds = m.predict(test);
 		double sens = m.getQuality(preds);
 
 		assertEquals(0.8666666666666667,sens,1e-5);
 	}
 	
 	public void testLIBSVM5() throws Exception
 	{
 		LinearKernel lk = new LinearKernel();
 		lk.doNormalize(false);
 		
 		DefaultInstanceContainer<PrimalInstance>  data = InstanceReader.read(DataSource.class.getResourceAsStream("iris.dat"), 5, true);
 
 		NuSVCModel<PrimalInstance>                 m  = new NuSVCModel<PrimalInstance>(lk);
 		
 		m.setNu(0.2);
 
 		DefaultInstanceContainer<PrimalInstance>  train = new DefaultInstanceContainer<PrimalInstance>();
 		DefaultInstanceContainer<PrimalInstance>  test  = new DefaultInstanceContainer<PrimalInstance>();
 		
 		ModelValidation.getStratifiedSplit(0.5, data, train, test);
 				
 		m.train(train);
 		
 		List<Prediction> preds = m.predict(test);
 		assertEquals(0.946667,m.getQuality(preds),1e-4);
 		
 		double objs[] = {0.275968,0.126828,4.312706};
 		for(int i=0;i<3;i++)
 		{
 			assertEquals(objs[i],m.getObjectiveValue(i),1e-4);
 		}
 		
 	}
 	
 	public void testCSVC() throws Exception
 	{
 		LinearKernel lK = new LinearKernel();
 		
 		DefaultInstanceContainer<PrimalInstance> samples = InstanceReader.read(DataSource.class.getResourceAsStream("iris.dat"), 5, true);
 		
		Model<PrimalInstance>     m  = new CSVCModel<PrimalInstance>(lK);
 		
 		double qual = ModelValidation.CV(5, samples, m);
 		assertEquals(0.96,qual,1e-6);
 		System.out.println(qual);
 	}
 	
 	public void testCSVCcloning() throws Exception
 	{
 		RBFKernel rbf = new RBFKernel();
 		rbf.setGamma(1.0);
 		
 		DefaultInstanceContainer<PrimalInstance> samples = InstanceReader.read(DataSource.class.getResourceAsStream("iris.dat"), 5, true);
 		
 		DefaultInstanceContainer<PrimalInstance>  train = new DefaultInstanceContainer<PrimalInstance>();
 		DefaultInstanceContainer<PrimalInstance>  test  = new DefaultInstanceContainer<PrimalInstance>();
 		
 		ModelValidation.getStratifiedSplit(0.5, samples, train, test);
 		
 		CSVCModel<PrimalInstance>  m  = new CSVCModel<PrimalInstance>(rbf);
 		
		Model<PrimalInstance>  m2 = SerialClone.clone(m);
 		
 		
 		m.train(train);
 		List<Prediction> preds1 = m.predict(test);
 		
 		m2.train(train);
 		List<Prediction> preds2 = m2.predict(test);
 		
 		double q1 = m.getQuality(preds1);
 		double q2 = m.getQuality(preds2);
 		System.out.println("# q1="+q1+" q2="+q2);
 		
 		double qual = ModelValidation.CV(5, samples, m);
 		/*
 		double qual = ModelValidation.CV(5, samples, m);
 		assertEquals(0.96,qual,1e-6);
 		System.out.println("#="+qual);
 		
 		
 		double qual2 = ModelValidation.CV(5, samples, m2);
 		assertEquals(0.96,qual2,1e-6);
 		System.out.println("#="+qual2);
 		*/
 	}
 	
 	
 	public void testNuSVR() throws Exception
 	{
 		RBFKernel rbf = new RBFKernel();
 		rbf.setGamma(1.0);
 		
 		DefaultInstanceContainer<PrimalInstance> samples = InstanceReader.read(DataSource.class.getResourceAsStream("sin.dat"), 2, false);
 		
 		NuSVRModel<PrimalInstance>     m  = new NuSVRModel<PrimalInstance>(rbf);
 		m.setC(10.0);
 		m.setNU(0.2);
 		
 		List<Prediction> preds = new ArrayList<Prediction>();
 		double qual = ModelValidation.CV(5, samples, m, preds);
 		
 		
 		assertEquals(0.9862527328922244,qual,1e-6);
 	}
 }
