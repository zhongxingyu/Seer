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
 package org.eclipse.examples.slideshow.runner;
 
 import org.eclipse.examples.slideshow.core.Slide;
 import org.eclipse.examples.slideshow.core.SlideDeck;
 import org.eclipse.examples.slideshow.resources.ITemplate;
 import org.eclipse.examples.slideshow.resources.ResourceManager;
 import org.eclipse.examples.slideshow.viewer.SlideViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 // TODO Implements IRunner?
 public class ScreenRunner {
 	final Display display;
 	final int style;
 	int slideIndex;
 	
 	SlideDeck slideshow;
 	Shell shell;
 	private boolean isOpen;
 	private ResourceManager resourceManager;
 
 	private ITemplate template;
 
 	private SlideViewer viewer;
 
 	public ScreenRunner(Display display, int style) {
 		this.slideIndex = 0;
 		this.display = display;
 		this.style = style;		
 	}		
 
 	public void setSlideshow(SlideDeck slideshow) {
 		this.slideshow = slideshow;		
 	}
 
 	public void open() {
 		isOpen = true;
 		shell = new Shell(display, SWT.SHELL_TRIM);
 				
 		//shell.setBounds(0, 0, 1024, 768);
 		shell.setLayout(new FillLayout());
 		viewer = new SlideViewer(shell, SWT.NONE);
 		viewer.setResourceManager(resourceManager);
 		viewer.setSlide(getSlide());
 		viewer.setTemplate(template);
 		
 		viewer.getControl().addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent e) {
 				if (e.button == 1) advance();
 				if (e.button == 3) goback();
 			}			
 		});
 		viewer.getControl().addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				switch (e.keyCode) {
 				case SWT.ARROW_RIGHT:
 				case SWT.ARROW_DOWN:
 				case SWT.PAGE_DOWN:
 				case ' ':
 					advance();
 					break;
 				case SWT.ARROW_LEFT:
 				case SWT.ARROW_UP:
 				case SWT.PAGE_UP:
 					goback();
 					break;
 				case SWT.ESC:
 					close();
 					break;
 				default:
 				}
 			}
 		});
 		shell.open();
 		shell.setFullScreen(true);
 	}
 
 	private void close() {
 		shell.dispose();
 		shell = null;
 		isOpen = false;		
 	}
 
 	protected void advance() {
 		if (++slideIndex >= slideshow.getSlideCount()) close(); 
 		else viewer.setSlide(getSlide());
 	}
 
 	protected void goback() {
 		if (--slideIndex < 0) slideIndex = 0; 
 		else  viewer.setSlide(getSlide());
 	}
 	
 	private Slide getSlide() {
 		return slideshow.getSlide(slideIndex);
 	}
 
 	public boolean isRunning() {
 		return isOpen;
 	}
 
 	// TODO What happens after the window has opened?
 	public void setResourceManager(ResourceManager resourceManager) {
 		this.resourceManager = resourceManager;		
 	}
 
 	// TODO What happens after the window has opened?
 	public void setTemplate(ITemplate template) {
 		this.template = template;		
 	}
 }
