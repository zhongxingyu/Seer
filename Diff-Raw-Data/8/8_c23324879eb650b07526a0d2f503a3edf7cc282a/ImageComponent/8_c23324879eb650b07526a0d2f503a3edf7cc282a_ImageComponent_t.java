 /*
  *   Copyright (C) 2006  The Concord Consortium, Inc.,
  *   25 Love Lane, Concord, MA 01742
  *
  *   This program is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 package org.concord.mw2d.models;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Stroke;
 import java.awt.Toolkit;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 
 import org.concord.modeler.ConnectionManager;
 import org.concord.modeler.text.XMLCharacterEncoder;
 import org.concord.modeler.util.FileUtilities;
 import org.concord.modeler.util.GifDecoder;
 import org.concord.mw2d.MDView;
 import org.concord.mw2d.ViewAttribute;
 
 /**
  * An ImageComponent is a ModelComponent that is an image. The animated GIF format is supported.
  * 
  * @see org.concord.modeler.util.GifDecoder
  * @author Charles Xie
  */
 
 public class ImageComponent implements ModelComponent, Layered, Rotatable {
 
 	/* the handles to rotate this particle */
 	private static Ellipse2D.Float[] rotc = { new Ellipse2D.Float(), new Ellipse2D.Float(), new Ellipse2D.Float(),
 			new Ellipse2D.Float() };
 
 	private GifDecoder gifDecoder;
 	private boolean selected, blinking, marked;
 	private int layer = FRONT;
 	private MDModel model;
 	private Image[] images;
 	private int loopCount = 1000;
 	private double x, y;
 	private float angle, offsetAngle;
 	private double savedX = -1.0, savedY = -1.0;
 	private float savedAngle;
 	private boolean stateStored;
 	private int frameCounter, loopCounter;
 	private long previousFrameTime, currentTime;
 	private String address;
 	private ModelComponent host;
 	private boolean selectedToRotate;
 
 	public ImageComponent(String address) throws IOException {
 		if (address == null)
 			throw new IllegalArgumentException("You must pass in a valid file name");
 		if (ConnectionManager.sharedInstance().isCachingAllowed()) {
 			if (FileUtilities.isRemote(address)) {
 				String s = FileUtilities.httpEncode(address);
 				File file = ConnectionManager.sharedInstance().shouldUpdate(s);
 				if (file == null)
 					file = ConnectionManager.sharedInstance().cache(s);
 				if (file != null)
 					address = file.toString();
 			}
 		}
 		init(address);
 	}
 
 	public ImageComponent(ImageComponent ic) throws IOException {
 		if (ic.address != null)
 			init(ic.address);
 		this.model = ic.model;
 		this.loopCount = ic.loopCount;
 	}
 
 	public void set(Delegate d) {
 		setLoopCount(d.getLoopCount());
 		setLayer(d.getLayer());
 		setLocation(d.getX(), d.getY());
 		setAngle(d.getAngle());
 		setOffsetAngle(d.getOffsetAngle());
 		String s = d.getHostType();
 		if (s != null) {
 			int index = d.getHostIndex();
 			if (s.endsWith("Atom")) {
 				if (model instanceof MolecularModel) {
 					setHost(((MolecularModel) model).getAtom(index));
 				}
 			}
 			else if (s.endsWith("RadialBond")) {
 				if (model instanceof MolecularModel) {
 					MolecularModel mm = (MolecularModel) model;
 					if (mm.bonds != null) {
 						int n = mm.bonds.size();
 						if (index < n && index >= 0)
 							setHost(mm.bonds.get(index));
 					}
 				}
 			}
 			else if (s.endsWith("GayBerneParticle")) {
 				if (model instanceof MesoModel) {
 					setHost(((MesoModel) model).getParticle(index));
 				}
 			}
 			else if (s.endsWith("Obstacle")) {
 				setHost(model.getObstacles().get(index));
 			}
 		}
 	}
 
 	private void init(String address) throws IOException {
 		this.address = address;
 		if (address.toLowerCase().endsWith(".gif")) {
 			gifDecoder = new GifDecoder();
 			gifDecoder.read(address);
 			images = gifDecoder.getImages();
 		}
 		else {
 			images = new Image[1];
 			if (EventQueue.isDispatchThread()) {
 				if (FileUtilities.isRemote(address)) {
 					images[0] = Toolkit.getDefaultToolkit().createImage(new URL(address));
 				}
 				else {
 					images[0] = Toolkit.getDefaultToolkit().createImage(address);
 				}
 			}
 			else {
				//images[0] = new ImageIcon(address).getImage();
				if (FileUtilities.isRemote(address)) {
					images[0] = Toolkit.getDefaultToolkit().createImage(new URL(address));
				}
				else {
					images[0] = Toolkit.getDefaultToolkit().createImage(address);
				}
 			}
 		}
 	}
 
 	public void storeCurrentState() {
 		savedX = x;
 		savedY = y;
 		savedAngle = angle;
 		stateStored = true;
 		HostStateManager.storeCurrentState(host);
 	}
 
 	public void restoreState() {
 		if (!stateStored)
 			return;
 		x = savedX;
 		y = savedY;
 		angle = savedAngle;
 		HostStateManager.restoreState(host);
 		if (selectedToRotate)
 			locateRotationHandles();
 	}
 
 	/** TODO */
 	public void blink() {
 	}
 
 	public void destroy() {
 		if (gifDecoder != null)
 			gifDecoder.dispose();
 		model = null;
 		host = null;
 		images = null;
 	}
 
 	/**
 	 * As an image should always be visible (otherwise why would you add one?), calling this method has no effect
 	 */
 	public void setVisible(boolean b) {
 	}
 
 	/**
 	 * As an image should always be visible (otherwise why would you add one?), this method always returns true
 	 */
 	public boolean isVisible() {
 		return true;
 	}
 
 	public boolean isSelectedToRotate() {
 		return selectedToRotate;
 	}
 
 	public void setSelectedToRotate(boolean b) {
 		selectedToRotate = b;
 		if (b) {
 			locateRotationHandles();
 		}
 	}
 
 	/** set a model component this image should tag after */
 	public void setHost(ModelComponent mc) {
 		if (mc != null)
 			storeCurrentState();
 		host = mc;
 		if (host == null)
 			offsetAngle = 0;
 	}
 
 	/** get a model component this image will tag after */
 	public ModelComponent getHost() {
 		return host;
 	}
 
 	/** return the index of the current frame. */
 	public int getCurrentFrame() {
 		return frameCounter;
 	}
 
 	public void setCurrentFrame(int i) {
 		frameCounter = i;
 	}
 
 	/** return the delay time for the i-th frame, in milliseconds. */
 	public int getDelayTime(int i) {
 		if (gifDecoder == null)
 			return 0;
 		if (images == null)
 			return 0;
 		if (images.length <= 1)
 			return 0;
 		return 10 * gifDecoder.getDelay(i);
 	}
 
 	/** return the number of frames. */
 	public int getFrames() {
 		if (images == null)
 			return 0;
 		return images.length;
 	}
 
 	/** show the next frame of this animated image. */
 	public void nextFrame() {
 
 		int n = images.length;
 		if (n <= 1)
 			return;
 		if (gifDecoder == null)
 			return;
 
 		currentTime = System.currentTimeMillis();
 		if (previousFrameTime != 0L) {
 			if (currentTime - previousFrameTime >= getDelayTime(frameCounter)) {
 				frameCounter++;
 				if (frameCounter == n) {
 					if (loopCount == 0) {
 						frameCounter = 0;
 					}
 					else {
 						if (loopCounter < loopCount - 1) {
 							frameCounter = 0;
 							loopCounter++;
 						}
 						else {
 							frameCounter = n - 1;
 						}
 					}
 				}
 				previousFrameTime = currentTime;
 			}
 		}
 		else {
 			previousFrameTime = currentTime;
 		}
 
 	}
 
 	/**
 	 * render this image or image sets onto a graphics context, with the specified component <code>c</code> as the
 	 * <code>ImageObserver</code>.
 	 */
 	public void paint(Graphics g) {
 		if (images == null)
 			return;
 		int n = images.length;
 		if (n <= 0)
 			return;
 		Graphics2D g2 = (Graphics2D) g;
 		AffineTransform at = null;
 		if (host != null)
 			setLocation(host.getRx() - 0.5 * getLogicalScreenWidth(), host.getRy() - 0.5 * getLogicalScreenHeight());
 		double xc = 0, yc = 0;
 		if (!selectedToRotate) {
 			if (host instanceof RadialBond) {
 				angle = (float) ((RadialBond) host).getAngle();
 			}
 		}
 		float a = angle + offsetAngle;
 		boolean hasAngle = Math.abs(a) > Particle.ZERO;
 		if (hasAngle) {
 			at = g2.getTransform();
 			xc = x + getLogicalScreenWidth() * 0.5;
 			yc = y + getLogicalScreenHeight() * 0.5;
 			g2.rotate(a, xc, yc);
 		}
 		if (n == 1) {
 			if (images[0] != null)
 				g2.drawImage(images[0], (int) x, (int) y, null);
 		}
 		else {
 			g2.drawImage(images[frameCounter < n ? frameCounter : n - 1], (int) getXFrame(), (int) getYFrame(), null);
 		}
 		if (selected && !selectedToRotate && ((MDView) model.getView()).getShowSelectionHalo()) {
 			Stroke oldStroke = g2.getStroke();
 			Color oldColor = g2.getColor();
 			g2.setColor(((MDView) model.getView()).contrastBackground());
 			g2.setStroke(ViewAttribute.THIN_DASHED);
 			g2.drawRect((int) (getXFrame() - 2), (int) (getYFrame() - 2), getWidth() + 4, getHeight() + 4);
 			g2.setColor(oldColor);
 			g2.setStroke(oldStroke);
 		}
 		if (hasAngle) {
 			g2.setTransform(at);
 		}
 		if (selected && selectedToRotate && ((MDView) model.getView()).getShowSelectionHalo()) {
 			Stroke oldStroke = g2.getStroke();
 			Color oldColor = g2.getColor();
 			g2.setStroke(ViewAttribute.THIN);
 			g2.setColor(Color.green);
 			for (Ellipse2D i : rotc)
 				g2.fill(i);
 			g2.setColor(((MDView) model.getView()).contrastBackground());
 			for (Ellipse2D i : rotc)
 				g2.draw(i);
 			g2.setColor(oldColor);
 			g2.setStroke(oldStroke);
 		}
 	}
 
 	/** restart painting from the first frame, and restore the loop count */
 	public void reset() {
 		frameCounter = 0;
 		loopCounter = 0;
 		previousFrameTime = 0;
 	}
 
 	/**
 	 * if the animated gif will loop forever, pass 0, the animated gif will repeat indefinitely, even though it wasn't
 	 * originally designed to do so. If passed a positive integer, the animated gif will play only the frames stored in
 	 * it for the specified times, then freeze at the last frame, until the <code>reset()</code> method is called.
 	 */
 	public void setLoopCount(int i) {
 		loopCount = i;
 	}
 
 	public int getLoopCount() {
 		return loopCount;
 	}
 
 	public void setOffsetAngle(float offsetAngle) {
 		this.offsetAngle = offsetAngle;
 	}
 
 	public float getOffsetAngle() {
 		return offsetAngle;
 	}
 
 	public void setAngle(float angle) {
 		this.angle = angle;
 	}
 
 	public float getAngle() {
 		return angle;
 	}
 
 	/** return the current x coordinate of this image */
 	public double getRx() {
 		return x;
 	}
 
 	/** return the current y coordinate of this image */
 	public double getRy() {
 		return y;
 	}
 
 	/** set the location of this image */
 	public void setLocation(double x, double y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	/** return the location of this image */
 	public Point getLocation() {
 		return new Point((int) x, (int) y);
 	}
 
 	/** return the center of the logical screen. */
 	public Point getCenter() {
 		return new Point((int) (x + getLogicalScreenWidth() * 0.5), (int) (y + getLogicalScreenHeight() * 0.5));
 	}
 
 	/** return the width of the current frame. */
 	public int getWidth() {
 		if (images == null)
 			return 0;
 		if (images.length == 0)
 			return 0;
 		if (images[0] == null)
 			return 0;
 		if (images.length == 1)
 			return images[0].getWidth(null);
 		if (frameCounter < 0)
 			return images[0].getWidth(null);
 		if (frameCounter >= images.length)
 			return images[images.length - 1].getWidth(null);
 		return images[frameCounter].getWidth(null);
 	}
 
 	/** return the height of the current frame. */
 	public int getHeight() {
 		if (images == null)
 			return 0;
 		if (images.length == 0)
 			return 0;
 		if (images[0] == null)
 			return 0;
 		if (images.length == 1)
 			return images[0].getHeight(null);
 		if (frameCounter < 0)
 			return images[0].getHeight(null);
 		if (frameCounter >= images.length)
 			return images[images.length - 1].getHeight(null);
 		return images[frameCounter].getHeight(null);
 	}
 
 	/** @return the logical screen width. If there is only one frame, return the width of the image. */
 	public int getLogicalScreenWidth() {
 		if (images == null)
 			return 0;
 		if (images.length == 0)
 			return 0;
 		if (images[0] == null)
 			return 0;
 		if (images.length == 1)
 			return images[0].getWidth(null);
 		if (gifDecoder == null)
 			return 0;
 		return gifDecoder.getLogicalScreenWidth();
 	}
 
 	/** @return the logical screen height. If there is only one frame, return the height of the image. */
 	public int getLogicalScreenHeight() {
 		if (images == null)
 			return 0;
 		if (images.length == 0)
 			return 0;
 		if (images[0] == null)
 			return 0;
 		if (images.length == 1)
 			return images[0].getHeight(null);
 		if (gifDecoder == null)
 			return 0;
 		return gifDecoder.getLogicalScreenHeight();
 	}
 
 	/** same as <code>setLocation(double, double)</code> */
 	public void translateTo(double x, double y) {
 		setLocation(x, y);
 	}
 
 	public void translateBy(double dx, double dy) {
 		x += dx;
 		y += dy;
 	}
 
 	public int getRotationHandle(int x, int y) {
 		for (int i = 0; i < rotc.length; i++) {
 			if (rotc[i].contains(x, y))
 				return i;
 		}
 		return -1;
 	}
 
 	/**
 	 * rotate to a given direction specified by the position.
 	 * 
 	 * @param px
 	 *            the x coordinate of the hot spot
 	 * @param py
 	 *            the y coordinate of the hot spot
 	 * @param handle
 	 *            the index of one of the four handles
 	 */
 	public void rotateTo(int px, int py, int handle) {
 		double w2 = getLogicalScreenWidth() * 0.5;
 		double h2 = getLogicalScreenHeight() * 0.5;
 		double rx = x + w2;
 		double ry = y + h2;
 		double distance = Math.hypot(rx - px, ry - py);
 		double theta = (px - rx) / distance;
 		theta = py > ry ? Math.acos(theta) - Math.PI : Math.PI - Math.acos(theta);
 		double theta0;
 		switch (handle) {
 		case 0:
 			rx = w2;
 			ry = h2;
 			break;
 		case 1:
 			rx = -w2;
 			ry = h2;
 			break;
 		case 2:
 			rx = -w2;
 			ry = -h2;
 			break;
 		case 3:
 			rx = w2;
 			ry = -h2;
 			break;
 		}
 		distance = Math.hypot(rx, ry);
 		theta0 = rx / distance;
 		theta0 = ry > 0.0 ? Math.acos(theta0) - Math.PI : Math.PI - Math.acos(theta0);
 		theta0 += offsetAngle;
 		setAngle((float) (theta - theta0));
 		locateRotationHandles();
 		if (host instanceof RadialBond) {
 			Molecule m = ((RadialBond) host).getMolecule();
 			m.rotateBondToAngle((RadialBond) host, angle);
 		}
 		model.getView().repaint();
 	}
 
 	private void locateRotationHandles() {
 		double cosTheta = Math.cos(host instanceof Rotatable ? angle : angle + offsetAngle);
 		double sinTheta = Math.sin(host instanceof Rotatable ? angle : angle + offsetAngle);
 		double w2 = getLogicalScreenWidth() * 0.5;
 		double h2 = getLogicalScreenHeight() * 0.5;
 		double rx = x + w2;
 		double ry = y + h2;
 		/* southeast circle */
 		double xold = w2;
 		double yold = h2;
 		double xpos = rx + xold * cosTheta - yold * sinTheta;
 		double ypos = ry + xold * sinTheta + yold * cosTheta;
 		rotc[0].setFrame(xpos - 3, ypos - 3, 6, 6);
 		/* southwest circle */
 		xold = -w2;
 		yold = h2;
 		xpos = rx + xold * cosTheta - yold * sinTheta;
 		ypos = ry + xold * sinTheta + yold * cosTheta;
 		rotc[1].setFrame(xpos - 3, ypos - 3, 6, 6);
 		/* northwest circle */
 		xold = -w2;
 		yold = -h2;
 		xpos = rx + xold * cosTheta - yold * sinTheta;
 		ypos = ry + xold * sinTheta + yold * cosTheta;
 		rotc[2].setFrame(xpos - 3, ypos - 3, 6, 6);
 		/* northeast circle */
 		xold = w2;
 		yold = -h2;
 		xpos = rx + xold * cosTheta - yold * sinTheta;
 		ypos = ry + xold * sinTheta + yold * cosTheta;
 		rotc[3].setFrame(xpos - 3, ypos - 3, 6, 6);
 	}
 
 	public void setSelected(boolean b) {
 		selected = b;
 		if (b) {
 			try {
 				((MDView) model.getView()).setSelectedComponent(this);
 			}
 			catch (Exception e) {
 				e.printStackTrace(System.err);
 			}
 		}
 	}
 
 	public boolean isSelected() {
 		return selected;
 	}
 
 	public void setMarked(boolean b) {
 		marked = b;
 	}
 
 	public boolean isMarked() {
 		return marked;
 	}
 
 	public void setBlinking(boolean b) {
 		blinking = b;
 	}
 
 	public boolean isBlinking() {
 		return blinking;
 	}
 
 	/** @return the x coordinate of the upper-left corner of the current frame. */
 	public double getXFrame() {
 		if (images.length == 1)
 			return x;
 		if (gifDecoder == null)
 			return x;
 		return x + gifDecoder.getXOffset(frameCounter);
 	}
 
 	/** @return the y coordinate of the upper-left corner of the current frame. */
 	public double getYFrame() {
 		if (images.length == 1)
 			return y;
 		if (gifDecoder == null)
 			return y;
 		return y + gifDecoder.getYOffset(frameCounter);
 	}
 
 	public boolean contains(double rx, double ry) {
 		if (images == null)
 			return false;
 		if (images.length == 0)
 			return false;
 		if (images[0] == null)
 			return false;
 		return rx >= getXFrame() && rx <= getXFrame() + images[0].getWidth(null) && ry >= getYFrame()
 				&& ry <= getYFrame() + images[0].getHeight(null);
 	}
 
 	public void setLayer(int i) {
 		layer = i;
 	}
 
 	public int getLayer() {
 		return layer;
 	}
 
 	public void setModel(MDModel model) {
 		this.model = model;
 	}
 
 	public MDModel getHostModel() {
 		return model;
 	}
 
 	public String toString() {
 		return address;
 	}
 
 	public static class Delegate extends LayeredComponentDelegate {
 
 		private String uri;
 		private int loopCount = 1000;
 		private float angle, offsetAngle;
 
 		public Delegate() {
 		}
 
 		public Delegate(ImageComponent ic) {
 			if (ic == null)
 				throw new IllegalArgumentException("arg can't be null");
 			uri = XMLCharacterEncoder.encode(FileUtilities.getFileName(ic.toString()));
 			loopCount = ic.getLoopCount();
 			x = ic.getRx();
 			y = ic.getRy();
 			angle = ic.angle;
 			offsetAngle = ic.offsetAngle;
 			layer = ic.layer;
 			layerPosition = (byte) ((MDView) ic.getHostModel().getView()).getLayerPosition(ic);
 			if (ic.getHost() != null) {
 				hostType = ic.getHost().getClass().toString();
 				if (ic.getHost() instanceof Particle) {
 					hostIndex = ((Particle) ic.getHost()).getIndex();
 				}
 				else if (ic.getHost() instanceof RadialBond) {
 					hostIndex = ((RadialBond) ic.getHost()).getIndex();
 				}
 				else if (ic.getHost() instanceof RectangularObstacle) {
 					hostIndex = ic.getHostModel().getObstacles().indexOf(ic.getHost());
 				}
 			}
 		}
 
 		public void setLoopCount(int i) {
 			loopCount = i;
 		}
 
 		public int getLoopCount() {
 			return loopCount;
 		}
 
 		public void setURI(String s) {
 			uri = s;
 		}
 
 		public String getURI() {
 			return uri;
 		}
 
 		public void setOffsetAngle(float offsetAngle) {
 			this.offsetAngle = offsetAngle;
 		}
 
 		public float getOffsetAngle() {
 			return offsetAngle;
 		}
 
 		public void setAngle(float angle) {
 			this.angle = angle;
 		}
 
 		public float getAngle() {
 			return angle;
 		}
 
 	}
 
 }
