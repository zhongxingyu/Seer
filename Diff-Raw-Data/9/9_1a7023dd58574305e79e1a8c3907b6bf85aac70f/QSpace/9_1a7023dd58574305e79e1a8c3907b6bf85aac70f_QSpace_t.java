 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.diffraction;
 
 import javax.vecmath.Vector3d;
 
 
 /**
  * Q-space calculations
  * 
  * q = k_f - k_i where |k| = 2 pi / lambda and |q| = 4 pi sin(theta)/lambda where
  * lambda is wavelength (in Angstroms) and 2*theta is the scattering angle
  */
 public class QSpace {
	private static final double QSCALE_DEFAULT = 2. * Math.PI;
 	private DetectorProperties detProps;
 	private double kmod; // wave number
 	private Vector3d ki; // initial wave vector
 	private double qScale;
 
 	public QSpace(DetectorProperties detprops, DiffractionCrystalEnvironment diffexp,double scale) {
 		detProps = detprops;
 		qScale=scale;
		setDiffractionCrystalEnvironment(diffexp);
 	}
 	
 	public QSpace(DetectorProperties detprops, DiffractionCrystalEnvironment diffexp) {
		this(detprops, diffexp, QSCALE_DEFAULT);
 	}
 
 	private void calculateInitalWavevector() {
 		ki = new Vector3d(detProps.getBeamVector());
 		ki.scale(kmod);
 	}
 
 	public void setDiffractionCrystalEnvironment(DiffractionCrystalEnvironment diffexp) {
 		kmod = qScale/diffexp.getWavelength();
 		calculateInitalWavevector();
 	}
 
 	/**
 	 * @return wavelength in Angstroms
 	 */
 	public double getWavelength() {
 		return qScale/kmod;
 	}
 
 	public void setDetectorProperties(DetectorProperties detprops) {
 		detProps = detprops;
 		calculateInitalWavevector();
 	}
 
 	public DetectorProperties getDetectorProperties() {
 		return detProps;
 	}
 
 	public Vector3d getInitialWavevector() {
 		return ki;
 	}
 
 	/**
 	 * Work out q from pixel coordinates 
 	 * @param x
 	 * @param y
 	 * @param q
 	 */
 	public void qFromPixelPosition(final double x, final double y, Vector3d q) {
 		detProps.pixelPosition(x, y, q);
 		q.normalize();
 		q.scale(kmod);
 		q.sub(ki);
 	}
 
 	/**
 	 * Work out q from pixel coordinates 
 	 * @param x
 	 * @param y
 	 * @param q
 	 */
 	public void qFromPixelPosition(final int x, final int y, Vector3d q) {
 		qFromPixelPosition((double) x, (double) y, q);
 	}
 
 	/**
 	 * @return q vector in inverse Angstroms
 	 */
 	public Vector3d qFromPixelPosition(final double x, final double y) {
 		Vector3d q = new Vector3d();
 		qFromPixelPosition(x, y, q);
 		return q;
 	}
 
 	/**
 	 * @return q vector in inverse Angstroms
 	 */
 	public Vector3d qFromPixelPosition(final int x, final int y) {
 		return qFromPixelPosition((double) x, (double) y);
 	}
 
 	/**
 	 * @return max |q| that detector can see
 	 */
 	public double maxModQ() {
 		double twotheta = detProps.getMaxScatteringAngle();
 		return 2.*kmod*Math.sin(0.5*twotheta);
 	}
 
 	/**
 	 * Calculate pixel position from given q
 	 * @param q
 	 * @param p output position vector in reference frame
 	 * @param t output vector (x and y components are pixel coordinates)
 	 */
 	public void pixelPosition(final Vector3d q, Vector3d p, Vector3d t) {
 		t.set(q);
 		t.add(ki);
 		t.normalize();
 		detProps.intersect(t, p);
 		detProps.pixelCoords(p, t);
 	}
 
 	/**
 	 * Calculate pixel position from given q
 	 * @param q
 	 * @param t output vector (x and y components are pixel coordinates)
 	 */
 	public void pixelPosition(final Vector3d q, Vector3d t) {
 		Vector3d p = new Vector3d();
 		pixelPosition(q, p, t);
 	}
 
 	/**
 	 * Calculate pixel position from given q
 	 * @param q
 	 * @return position of pixel
 	 */
 	public int[] pixelPosition(final Vector3d q) {
 		Vector3d t = new Vector3d(q);
 		t.add(ki);
 		t.normalize();
 		return detProps.pixelCoords(detProps.intersect(t));
 	}
 
 	/**
 	 * Calculate scattering angle corresponding to given q vector
 	 * @param q
 	 * @return scattering angle (two-theta) in radians
 	 */
 	public double scatteringAngle(final Vector3d q) {
 		return 2.*Math.asin(0.5*q.length()/kmod);
 	}
 }
