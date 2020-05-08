 package com.winvector.sample.impl;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Random;
 
 import com.winvector.sample.def.RandomVariable;
 
 /**
  * see: http://en.wikipedia.org/wiki/Log-normal_distribution
  * @author johnmount
  *
  */
 public final class LogNormal implements RandomVariable {
 	private double u = 1.0;
 	private double sigma = 1.0;
 
 	public LogNormal(final double u, final double sigma) {
 		this.u = u;
 		this.sigma = sigma;
 	}
 	
 	@Override
 	public double logProb(final double v) {
 		final double diff = Math.log(v) - u;
 		return -(diff*diff/(2*sigma*sigma)) - Math.log(v*Math.sqrt(2.0*Math.PI)*sigma);
 	}
 
 	@Override
 	public double generate(final Random rand) {
 		final double n = rand.nextGaussian();
 		return Math.exp(u + n*sigma);
 	}
 
 	public void setExpectation(final double v, final double sigma) {		
 		this.sigma = Math.max(0.0,sigma);
 		u = Math.log(v) - this.sigma*this.sigma/2.0;
 	}
 	
 	@Override
 	public double expectation() {
 		return Math.exp(u + sigma*sigma/2);
 	}
 
 	@Override
 	public void setExpectation(final double v) {
 		// sets to maximum entropy dist with this expectation
 		setExpectation(v,1.0);
 	}
 	
 	@Override
 	public String toString() {
 		final NumberFormat nf = new DecimalFormat("#.##");
 		return "log-normal(u=" + nf.format(u) + ",\\sigma=" + nf.format(sigma) + ")";
 	}
 
 	@Override
 	public LogNormal copy() {
 		return new LogNormal(u,sigma);
 	}
 }
