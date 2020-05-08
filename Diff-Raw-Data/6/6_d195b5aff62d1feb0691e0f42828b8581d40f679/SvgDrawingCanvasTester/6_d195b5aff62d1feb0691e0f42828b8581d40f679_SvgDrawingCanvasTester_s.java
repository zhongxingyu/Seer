 /*
 
 $Source: /share/content/gforge/webcgh/webgenome/src/org/rti/webcgh/drawing/unit_test/SvgDrawingCanvasTester.java,v $
$Revision: 1.2 $
$Date: 2006-05-12 17:16:23 $
 
 The Web CGH Software License, Version 1.0
 
 Copyright 2003 RTI. This software was developed in conjunction with the National 
 Cancer Institute, and so to the extent government employees are co-authors, any 
 rights in such works shall be subject to Title 17 of the United States Code, 
 section 105.
 
 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this 
 list of conditions and the disclaimer of Article 3, below. Redistributions in 
 binary form must reproduce the above copyright notice, this list of conditions 
 and the following disclaimer in the documentation and/or other materials 
 provided with the distribution.
 
 2. The end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment:
 
 "This product includes software developed by the RTI and the National Cancer 
 Institute."
 
 If no such end-user documentation is to be included, this acknowledgment shall 
 appear in the software itself, wherever such third-party acknowledgments 
 normally appear.
 
 3. The names "The National Cancer Institute", "NCI", 
 Research Triangle Institute, and "RTI" must not be used to endorse or promote 
 products derived from this software.
 
 4. This license does not authorize the incorporation of this software into any 
 proprietary programs. This license does not authorize the recipient to use any 
 trademarks owned by either NCI or RTI.
 
 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL 
 CANCER INSTITUTE, RTI, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 */
 
 
 package org.rti.webcgh.drawing.unit_test;
 
 import java.awt.Color;
 import java.awt.Point;
 
 import junit.framework.TestCase;
 
 import org.rti.webcgh.deprecated.RendererTesterUtils;
 import org.rti.webcgh.drawing.DrawingCanvas;
 import org.rti.webcgh.drawing.GraphicCircle;
 import org.rti.webcgh.drawing.GraphicLine;
 import org.rti.webcgh.drawing.GraphicPolyline;
 import org.rti.webcgh.drawing.GraphicRect;
 import org.rti.webcgh.drawing.GraphicText;
 import org.rti.webcgh.drawing.HorizontalAlignment;
 import org.rti.webcgh.drawing.SvgDrawingCanvas;
 import org.rti.webcgh.drawing.SvgElementFactory;
 import org.rti.webcgh.drawing.SvgGraphicText;
import org.rti.webcgh.plot.unit_test.*;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Tester for <code>SvgDrawingCanvas</code>
  */
 public class SvgDrawingCanvasTester extends TestCase {
 	
 	
 	protected Document doc = null;
 	protected DrawingCanvas canvas = null;
 	protected SvgElementFactory factory = null;
 	protected GraphicCircle circle = null;
 	protected GraphicLine line = null;
 	protected GraphicPolyline poly = null;
 	protected GraphicRect rect = null;
 	protected GraphicText text = null;
 	
 	
 	/**
 	 * @throws Exception
 	 */
 	public void setUp() throws Exception {
 		doc = RendererTesterUtils.newTestDocument();
 		factory = new SvgElementFactory(doc);
 		Element e = RendererTesterUtils.getInsertionPoint(doc);
 		canvas = new SvgDrawingCanvas(doc);
 		circle = new GraphicCircle(50, 50, 10, Color.green);
 		line = new GraphicLine(10, 10, 50, 50, 5, Color.red);
 		poly = new GraphicPolyline(1, 10, Color.blue);
 		rect = new GraphicRect(70, 70, 50, 15, Color.black);
 		text = new SvgGraphicText("Hello", 100, 100, 12, HorizontalAlignment.CENTERED, Color.cyan);
 		poly.add(new Point(10, 10));
 		poly.add(new Point(10, 100));
 		poly.add(new Point(50, 100));
 	}
 	
 	
 	/**
 	 * @throws Exception
 	 */
 	public void testAddPrimitives() throws Exception {
 		canvas.add(circle);
 		canvas.add(line);
 		canvas.add(poly);
 		canvas.add(rect);
 		canvas.add(text);
 		RendererTesterUtils.writeDocument(doc, "SvgDrawingCanvasTester_testAddPrimitives.svg");
 	}
 	
 	
 	/**
 	 * @throws Exception
 	 */
 	public void testAddCanvas() throws Exception {
 		DrawingCanvas tile = canvas.newTile();
 		tile.add(circle);
 		tile.add(line);
 		tile.add(poly);
 		canvas.add(tile, 300, 300);
 		canvas.add(rect);
 		canvas.add(text);
 		RendererTesterUtils.writeDocument(doc, "SvgDrawingCanvasTester_testAddTile.svg");
 	}
 }
