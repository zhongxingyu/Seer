 package org.goobs.tests;
 
 import org.goobs.stats.ExpectedSufficientStatistics;
 import org.goobs.stats.Gaussian;
 import org.junit.Test;
 
 import java.lang.reflect.Type;
 import java.util.Random;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Gabor Angeli (angeli at cs.stanford)
  */
 public class StatsTest {
 
 	private double check(double x, double s, double u){
 		return (1.0) / (s*Math.sqrt(2.0*Math.PI)) * Math.exp( -(x-u)*(x-u) / (2.0*s*s) );
 	}
 
 	@Test
 	public void gaussianPDF(){
 		//-- N(0, 1)
 		Gaussian simple = new Gaussian(0,1);
 		assertEquals(check(0.0 ,1,0.0), simple.prob(0.0), 1e-5);
 		assertEquals(check(1.0 ,1,0.0), simple.prob(1.0), 1e-5);
 		assertEquals(check(-1.0,1,0.0), simple.prob(-1.0), 1e-5);
 		assertEquals(check(10.0,1,0.0), simple.prob(10.0), 1e-5);
 		assertEquals(check(-0.3,1,0.0), simple.prob(-0.3), 1e-5);
 		//-- N(-2.5, 1)
 		Gaussian left = new Gaussian(-2.5,1);
 		assertEquals(check(0.0 ,1,-2.5), left.prob(0.0), 1e-5);
 		assertEquals(check(1.0 ,1,-2.5), left.prob(1.0), 1e-5);
 		assertEquals(check(-1.0,1,-2.5), left.prob(-1.0), 1e-5);
 		assertEquals(check(10.0,1,-2.5), left.prob(10.0), 1e-5);
 		assertEquals(check(-0.3,1,-2.5), left.prob(-0.3), 1e-5);
 		//-- N(0.0, 2.3)
 		Gaussian modStdev = new Gaussian(0.0,2.3);
 		assertEquals(check(0.0 ,2.3,0.0), modStdev.prob(0.0), 1e-5);
 		assertEquals(check(1.0 ,2.3,0.0), modStdev.prob(1.0), 1e-5);
 		assertEquals(check(-1.0,2.3,0.0), modStdev.prob(-1.0), 1e-5);
 		assertEquals(check(10.0,2.3,0.0), modStdev.prob(10.0), 1e-5);
 		assertEquals(check(-0.3,2.3,0.0), modStdev.prob(-0.3), 1e-5);
 	}
 
 	@Test
 	public void gaussianCDF(){
 		//-- N(0, 1)
 		Gaussian simple = new Gaussian(0,1);
 		for(double x=-3.0; x<=3.0; x+=0.01){
 			assertEquals(zValue(x), simple.cdf(x), 1e-4);
 		}
 		//-- N(0, 1)
 		Gaussian shifted = new Gaussian(-0.5,2);
 		for(double x=-3.0; x<=3.0; x+=0.01){
 			assertEquals(zValue((x + 0.5) / 2.0), shifted.cdf(x), 1e-2);
 		}
 	}
 
 	@Test
 	public void gaussianEM(){
 		//--Simple Case
 		//(priors)
 		Gaussian simple = new Gaussian(0,1);
 		Gaussian prior = new Gaussian(1.0,1.0);
 		//(e-step)
 		ExpectedSufficientStatistics<Double, Gaussian> ess = simple.newStatistics(prior);
 		ess.updateEStep(0.5, 1.0);
 		ess.updateEStep(1.0, 1.0);
 		ess.updateEStep(1.5, 1.0);
 		//(m-step)
 		Gaussian posterior = ess.runMStep();
 		//(check)
 		assertEquals(1.0, posterior.mu, 1e-5);
 		//-- "Real" Data --
 		double mu = 3.2;
 		double sigma = 0.3;
 		Gaussian dist = new Gaussian(mu,sigma);
 		//--Correct Start
 		Gaussian correctStart = new Gaussian(mu,sigma);
 		Gaussian correctPrior = new Gaussian(mu,sigma);
 		ess = correctStart.newStatistics(correctPrior);
 		Random r = new Random(44);
 		for(int i=0; i<10000; i++){
 			ess.updateEStep(dist.sample(r), new Random().nextDouble());
 			ess.updateEStep(dist.sample(r), 1.0);
 		}
 		posterior = ess.runMStep();
 		assertEquals(mu, posterior.mu, 0.1);
 		assertEquals(sigma, posterior.sigma, 0.1);
 		//--Data Push
 		Gaussian nullPrior = new Gaussian(0.0,1.0);
 		//(small data)
 		ess = correctStart.newStatistics(nullPrior);
 		for(int i=0; i<10; i++){
 			ess.updateEStep(dist.sample(r), r.nextDouble());
 		}
 		Gaussian smallData = ess.runMStep();
 		//(large data)
 		ess = correctStart.newStatistics(nullPrior);
 		for(int i=0; i<10000; i++){
 			ess.updateEStep(dist.sample(r), r.nextDouble());
 		}
 		Gaussian largeData = ess.runMStep();
 		//(check convergence)
 		assertTrue(  "target="+mu+ " smallData.mu="+smallData.mu + " largeData.mu=" + largeData.mu,
 				Math.abs(smallData.mu - mu) > Math.abs(largeData.mu - mu));
 		assertTrue( Math.abs(smallData.sigma - sigma) > Math.abs(largeData.sigma - sigma));
 		//(check converged value)
 		assertEquals(mu, largeData.mu, 0.1);
 		assertEquals(sigma, largeData.sigma, 0.1);
 	}
 
 	@Test
 	public void gaussianEMCornerCases(){
 		//--One Data Point
 		Gaussian start = new Gaussian(0.0,1.0);
 		Gaussian prior = new Gaussian(0.0,1.0);
 		ExpectedSufficientStatistics<Double, Gaussian> ess = start.newStatistics(prior);
 		ess.updateEStep(0.2, 1.0);
 		Gaussian singleSample = ess.runMStep();
 		assertEquals(0.2, singleSample.mu, 1e-5);
 		assertEquals(0.0, singleSample.sigma, 1e-5);
 		//--Two Data Points
 		ess = start.newStatistics(prior);
 		ess.updateEStep(0.2, 1.0);
 		ess.updateEStep(0.2, 1.0);
 		Gaussian twoSamples = ess.runMStep();
 		assertEquals(0.2, twoSamples.mu, 1e-5);
 		assertEquals(0.0, twoSamples.sigma, 1e-5);
 		//--Fixed Sigma
 		ess = start.newStatistics(prior, 1.0);
 		ess.updateEStep(0.2, 1.0);
 		ess.updateEStep(0.2, 1.0);
 		Gaussian fixedSigma = ess.runMStep();
 		assertEquals(0.1333333333, fixedSigma.mu, 1e-5);
 		assertEquals(1.0, fixedSigma.sigma, 1e-5);
 	}
 	
 	@Test
 	public void gaussianDecodeString(){
		new Gaussian(0,0,1.0).decode("[0.0,1.0]", new Type[0]);
		new Gaussian(0,0,1.0).decode("[0,1]", new Type[0]);
		new Gaussian(0,0,1.0).decode("N[0.0,1.0]", new Type[0]);
		new Gaussian(0,0,1.0).decode("N(0.0,1.0)", new Type[0]);
		new Gaussian(0,0,1.0).decode("N(0,1)", new Type[0]);
 	}
 
 
 	public static double zValue(double val){
 		int index = (int) Math.round(val*100);
 		if(index >= 0){
 			return 0.5 + ZTABLE[index];
 		} else {
 			return 0.5 - ZTABLE[-index];
 		}
 	}
 	public static final double[] ZTABLE = new double[]{
 			0.0000,
 			0.0040,
 			0.0080,
 			0.0120,
 			0.0160,
 			0.0199,
 			0.0239,
 			0.0279,
 			0.0319,
 			0.0359,
 			0.0398,
 			0.0438,
 			0.0478,
 			0.0517,
 			0.0557,
 			0.0596,
 			0.0636,
 			0.0675,
 			0.0714,
 			0.0753,
 			0.0793,
 			0.0832,
 			0.0871,
 			0.0910,
 			0.0948,
 			0.0987,
 			0.1026,
 			0.1064,
 			0.1103,
 			0.1141,
 			0.1179,
 			0.1217,
 			0.1255,
 			0.1293,
 			0.1331,
 			0.1368,
 			0.1406,
 			0.1443,
 			0.1480,
 			0.1517,
 			0.1554,
 			0.1591,
 			0.1628,
 			0.1664,
 			0.1700,
 			0.1736,
 			0.1772,
 			0.1808,
 			0.1844,
 			0.1879,
 			0.1915,
 			0.1950,
 			0.1985,
 			0.2019,
 			0.2054,
 			0.2088,
 			0.2123,
 			0.2157,
 			0.2190,
 			0.2224,
 			0.2257,
 			0.2291,
 			0.2324,
 			0.2357,
 			0.2389,
 			0.2422,
 			0.2454,
 			0.2486,
 			0.2517,
 			0.2549,
 			0.2580,
 			0.2611,
 			0.2642,
 			0.2673,
 			0.2704,
 			0.2734,
 			0.2764,
 			0.2794,
 			0.2823,
 			0.2852,
 			0.2881,
 			0.2910,
 			0.2939,
 			0.2967,
 			0.2995,
 			0.3023,
 			0.3051,
 			0.3078,
 			0.3106,
 			0.3133,
 			0.3159,
 			0.3186,
 			0.3212,
 			0.3238,
 			0.3264,
 			0.3289,
 			0.3315,
 			0.3340,
 			0.3365,
 			0.3389,
 			0.3413,
 			0.3438,
 			0.3461,
 			0.3485,
 			0.3508,
 			0.3531,
 			0.3554,
 			0.3577,
 			0.3599,
 			0.3621,
 			0.3643,
 			0.3665,
 			0.3686,
 			0.3708,
 			0.3729,
 			0.3749,
 			0.3770,
 			0.3790,
 			0.3810,
 			0.3830,
 			0.3849,
 			0.3869,
 			0.3888,
 			0.3907,
 			0.3925,
 			0.3944,
 			0.3962,
 			0.3980,
 			0.3997,
 			0.4015,
 			0.4032,
 			0.4049,
 			0.4066,
 			0.4082,
 			0.4099,
 			0.4115,
 			0.4131,
 			0.4147,
 			0.4162,
 			0.4177,
 			0.4192,
 			0.4207,
 			0.4222,
 			0.4236,
 			0.4251,
 			0.4265,
 			0.4279,
 			0.4292,
 			0.4306,
 			0.4319,
 			0.4332,
 			0.4345,
 			0.4357,
 			0.4370,
 			0.4382,
 			0.4394,
 			0.4406,
 			0.4418,
 			0.4429,
 			0.4441,
 			0.4452,
 			0.4463,
 			0.4474,
 			0.4484,
 			0.4495,
 			0.4505,
 			0.4515,
 			0.4525,
 			0.4535,
 			0.4545,
 			0.4554,
 			0.4564,
 			0.4573,
 			0.4582,
 			0.4591,
 			0.4599,
 			0.4608,
 			0.4616,
 			0.4625,
 			0.4633,
 			0.4641,
 			0.4649,
 			0.4656,
 			0.4664,
 			0.4671,
 			0.4678,
 			0.4686,
 			0.4693,
 			0.4699,
 			0.4706,
 			0.4713,
 			0.4719,
 			0.4726,
 			0.4732,
 			0.4738,
 			0.4744,
 			0.4750,
 			0.4756,
 			0.4761,
 			0.4767,
 			0.4772,
 			0.4778,
 			0.4783,
 			0.4788,
 			0.4793,
 			0.4798,
 			0.4803,
 			0.4808,
 			0.4812,
 			0.4817,
 			0.4821,
 			0.4826,
 			0.4830,
 			0.4834,
 			0.4838,
 			0.4842,
 			0.4846,
 			0.4850,
 			0.4854,
 			0.4857,
 			0.4861,
 			0.4864,
 			0.4868,
 			0.4871,
 			0.4875,
 			0.4878,
 			0.4881,
 			0.4884,
 			0.4887,
 			0.4890,
 			0.4893,
 			0.4896,
 			0.4898,
 			0.4901,
 			0.4904,
 			0.4906,
 			0.4909,
 			0.4911,
 			0.4913,
 			0.4916,
 			0.4918,
 			0.4920,
 			0.4922,
 			0.4925,
 			0.4927,
 			0.4929,
 			0.4931,
 			0.4932,
 			0.4934,
 			0.4936,
 			0.4938,
 			0.4940,
 			0.4941,
 			0.4943,
 			0.4945,
 			0.4946,
 			0.4948,
 			0.4949,
 			0.4951,
 			0.4952,
 			0.4953,
 			0.4955,
 			0.4956,
 			0.4957,
 			0.4959,
 			0.4960,
 			0.4961,
 			0.4962,
 			0.4963,
 			0.4964,
 			0.4965,
 			0.4966,
 			0.4967,
 			0.4968,
 			0.4969,
 			0.4970,
 			0.4971,
 			0.4972,
 			0.4973,
 			0.4974,
 			0.4974,
 			0.4975,
 			0.4976,
 			0.4977,
 			0.4977,
 			0.4978,
 			0.4979,
 			0.4979,
 			0.4980,
 			0.4981,
 			0.4981,
 			0.4982,
 			0.4982,
 			0.4983,
 			0.4984,
 			0.4984,
 			0.4985,
 			0.4985,
 			0.4986,
 			0.4986,
 			0.4987,
 			0.4987,
 			0.4987,
 			0.4988,
 			0.4988,
 			0.4989,
 			0.4989,
 			0.4989,
 			0.4990,
 			0.4990
 	};
 }
