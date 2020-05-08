 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.lnf;
 
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Resource;
import org.eclipse.ui.PlatformUI;
 
 /**
  * Wrapper for resource color.
  */
 public class ColorLnfResource extends AbstractLnfResource {
 
 	private RGB rgb;
 
 	/**
 	 * @param red
 	 *            - the amount of red in the color
 	 * @param green
 	 *            - the amount of green in the color
 	 * @param blue
 	 *            - the amount of blue in the color
 	 */
 	public ColorLnfResource(int red, int green, int blue) {
 		this(new RGB(red, green, blue));
 	}
 
 	/**
 	 * @param hue
 	 *            - the hue value for the HSB color (from 0 to 360)
 	 * @param saturation
 	 *            - the saturation value for the HSB color (from 0 to 1)
 	 * @param brightness
 	 *            - the brightness value for the HSB color (from 0 to 1)
 	 */
 	public ColorLnfResource(float hue, float saturation, float brightness) {
 		this(new RGB(hue, saturation, brightness));
 	}
 
 	/**
 	 * @param rgb
 	 *            - the RGB values of the desired color
 	 */
 	public ColorLnfResource(RGB rgb) {
 		super();
 		this.rgb = rgb;
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.swt.lnf.AbstractLnfResource#getResource()
 	 */
 	@Override
 	public Color getResource() {
 		return (Color) super.getResource();
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.swt.lnf.ILnfResource#createResource()
 	 */
 	public Resource createResource() {
 		if (rgb == null) {
 			return null;
 		}
		return new Color(PlatformUI.getWorkbench().getDisplay(), rgb);
 	}
 
 }
