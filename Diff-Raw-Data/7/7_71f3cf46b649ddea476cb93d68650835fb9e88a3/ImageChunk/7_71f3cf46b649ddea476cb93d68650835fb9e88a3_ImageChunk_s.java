 /*******************************************************************************
  * Copyright (c) 2009 The Eclipse Foundation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.examples.slideshow.core;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * An ImageChunk represents an image. An alignment of the image can be provided
  * (and is used as a hint by the renderer), along with an optional width and
  * height (to use the image's width and/or height, specify <code>-1</code> for
  * either or both of these values. 
  * <p>
  * The instance needs to know the {@link Slide} that it's part of so that it can
  * use the slide to gain access to the {@link SlideDeck} and get the
  * {@link SlideDeck#baseUrl} in order to form a complete {@link URL} for the
  * image. By using a URL format, we give ourselves a lot of flexibility in terms
  * of where the image can actually be stored. We can access images in the
  * Eclipse workspace, or configuration using a URL; we can also access remote
  * resources using an &quot;http://&quot;-prefixed URL. The best part is that
  * the mechanism for obtaining the resource is completely disconnected from all
  * of our code (and it's relatively easy to provide alternative URL protocols).
  * At display time, it is the renderer that takes responsibility for obtaining
  * the image and managing any resources associated with it.
  * 
  * @see BlockChunk#ALIGN_LEFT
  * @see BlockChunk#ALIGN_RIGHT
  * @see BlockChunk#ALIGN_NONE
  */
 public class ImageChunk extends BlockChunk {
 
 	private final String url;
 	private final int width;
 	private final int height;
 	
 	public ImageChunk(Slide slide, String url, int align, int width, int height) {
 		super(slide, align);
 		this.url = url;
 		this.width = width;
 		this.height = height;
 	}
 
 	@Override
 	public String getText() {
 		return "";
 	}
 
 	@Override
 	public boolean hasText() {
 		return false;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
	public URL getFullUrl() throws MalformedURLException {
		if (getBaseUrl() == null) return new URL(url);
		return new URL(getBaseUrl(), getUrl());
	}

	URL getBaseUrl() {
 		return getSlideDeck().getBaseUrl();
 	}
 
 	public int getWidth() {
 		return width;
 	}
 	
 	public int getHeight() {
 		return height;
 	}
 }
