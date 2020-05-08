 /*
  * #%L
  * Puncta Analyzer is an ImageJ plugin for detecting and quantifying punctate 
  * colocalization in multi-channel images.
  * %%
  * Copyright (C) 2012, Physion Consulting LLC All rights reserved.
  * %%
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * 
  * The views and conclusions contained in the software and documentation are
  * those of the authors and should not be interpreted as representing official
  * policies, either expressed or implied, of any organization.
  * #L%
  */
 
 
 import ij.IJ;
 import ij.ImageJ;
 import ij.ImagePlus;
 import ij.ImageStack;
 import ij.Undo;
 import ij.WindowManager;
 import ij.gui.GUI;
 import ij.gui.GenericDialog;
 import ij.gui.Toolbar;
 import ij.gui.YesNoCancelDialog;
 import ij.measure.Calibration;
 import ij.measure.Measurements;
 import ij.plugin.PlugIn;
 import ij.plugin.frame.PasteController;
 import ij.plugin.frame.PlugInFrame;
 import ij.process.ByteProcessor;
 import ij.process.ImageProcessor;
 import ij.process.ImageStatistics;
 import ij.process.ShortProcessor;
 import ij.process.StackProcessor;
 import java.awt.Button;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Label;
 import java.awt.Panel;
 import java.awt.Scrollbar;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.WindowEvent;
 
 /**
 * Masks threshhold
  * 
  * @author Barry Wark
  */
 public class ThresholdMasker extends PlugInFrame 
    implements PlugIn, Measurements, Runnable, ActionListener, AdjustmentListener, MaskerIF {
        
    protected static final double defaultMinThreshold = 65.0D;
    protected static final double defaultMaxThreshold = 255.0D;
    protected static final String DONE = "Done";
    protected static boolean fill1 = true;
    protected static boolean fill2 = true;
    protected static boolean useBW = true;
    protected static Frame instance;
    protected ThresholdPlot plot = new ThresholdPlot();
    protected Thread thread;
    protected int minValue = -1;
    protected int maxValue = -1;
    protected int sliderRange = 256;
    protected boolean doAutoAdjust;
    protected boolean doReset;
    protected boolean doApplyLut;
    protected boolean doStateChange;
    protected boolean doSet;
    protected Panel panel;
    protected Button autoB;
    protected Button resetB;
    protected Button applyB;
    protected Button stateB;
    protected Button setB;
    protected int previousImageID;
    protected int previousImageType;
    protected double previousMin;
    protected double previousMax;
    protected ImageJ ij;
    protected double minThreshold;
    protected double maxThreshold;
    protected Scrollbar minSlider;
    protected Scrollbar maxSlider;
    protected Label label1;
    protected Label label2;
    protected boolean done;
    protected boolean invertedLut;
    protected boolean blackAndWhite;
    protected int lutColor = 0;
    static final int RESET = 0;
    static final int AUTO = 1;
    static final int HIST = 2;
    static final int APPLY = 3;
    static final int STATE_CHANGE = 4;
    static final int MIN_THRESHOLD = 5;
    static final int MAX_THRESHOLD = 6;
    static final int SET = 7;
 
    public void mask() {
       this.thread.start();
       setVisible(true);
       try
       {
          syncAdjust();
       } catch (InterruptedException e) {
          IJ.error("Masker could not complete because an exception occured.");
       }
       dispose();
    }
 
    public ThresholdMasker() {
       super("Threshold");
       if (instance != null) {
          instance.toFront();
          return;
       }
       instance = this;
       IJ.register(PasteController.class);
       this.ij = IJ.getInstance();
       initGUI();
       this.thread = new Thread(this, "ThresholdAdjuster");
       ImagePlus imp = WindowManager.getCurrentImage();
       if (imp != null) {
          setup(imp);
       }
       this.done = false;
    }
 
    protected void initGUI() {
       Font font = new Font("SansSerif", 0, 10);
       GridBagLayout gridbag = new GridBagLayout();
       GridBagConstraints c = new GridBagConstraints();
       setLayout(gridbag);
 
       int y = 0;
       c.gridx = 0;
       c.gridy = (y++);
       c.gridwidth = 2;
       c.fill = 1;
       c.anchor = 10;
       c.insets = new Insets(10, 10, 0, 10);
       add(this.plot, c);
 
       this.minSlider = new Scrollbar(0, this.sliderRange / 3, 1, 0, this.sliderRange);
       c.gridx = 0;
       c.gridy = (y++);
       c.gridwidth = 1;
       c.weightx = (IJ.isMacintosh() ? 90.0D : 100.0D);
       c.fill = 2;
       c.insets = new Insets(5, 10, 0, 0);
       add(this.minSlider, c);
       this.minSlider.addAdjustmentListener(this);
       this.minSlider.setUnitIncrement(1);
 
       c.gridx = 1;
       c.gridwidth = 1;
       c.weightx = (IJ.isMacintosh() ? 10.0D : 0.0D);
       c.insets = new Insets(5, 0, 0, 10);
       this.label1 = new Label("       ", 2);
       this.label1.setFont(font);
       add(this.label1, c);
 
       this.maxSlider = new Scrollbar(0, this.sliderRange * 2 / 3, 1, 0, this.sliderRange);
       c.gridx = 0;
       c.gridy = (y++);
       c.gridwidth = 1;
       c.weightx = 100.0D;
       c.insets = new Insets(0, 10, 0, 0);
       add(this.maxSlider, c);
       this.maxSlider.addAdjustmentListener(this);
       this.maxSlider.setUnitIncrement(1);
 
       c.gridx = 1;
       c.gridwidth = 1;
       c.weightx = 0.0D;
       c.insets = new Insets(0, 0, 0, 10);
       this.label2 = new Label("       ", 2);
       this.label2.setFont(font);
       add(this.label2, c);
 
       this.panel = new Panel();
 
       this.resetB = new Button("Reset");
       this.resetB.addActionListener(this);
       this.resetB.addKeyListener(this.ij);
       this.panel.add(this.resetB);
       Button doneB = new Button("Done");
       doneB.addActionListener(this);
       doneB.addKeyListener(this.ij);
       this.panel.add(doneB);
 
       c.gridx = 0;
       c.gridy = (y++);
       c.gridwidth = 2;
       c.insets = new Insets(5, 5, 10, 5);
       add(this.panel, c);
 
       addKeyListener(this.ij);
       pack();
       GUI.center(this);
   }
 
   public synchronized void adjustmentValueChanged(AdjustmentEvent e)
   {
      if (e.getSource() == this.minSlider)
         this.minValue = this.minSlider.getValue();
      else
         this.maxValue = this.maxSlider.getValue();
     notifyAll();
   }
 
   public synchronized void actionPerformed(ActionEvent e) {
      Button b = (Button)e.getSource();
      if (b == null)
         return;
      if (b.getActionCommand().equals("Done")) {
         done();
         return;
      }
      if (b == this.resetB) {
         this.doReset = true;
      }
      else if (b == this.autoB) {
         this.doAutoAdjust = true;
      } 
      else if (b == this.applyB) {
         this.doApplyLut = true;
      } 
      else if (b == this.stateB) {
         this.blackAndWhite = this.stateB.getLabel().equals("B&W");
         if (this.blackAndWhite) {
            this.stateB.setLabel("Red");
            this.lutColor = 1;
         } 
         else {
            this.stateB.setLabel("B&W");
            this.lutColor = 0;
         }
         this.doStateChange = true;
      } 
      else if (b == this.setB) {
         this.doSet = true;
     }
     notifyAll();
   }
 
   public ImageProcessor setup(ImagePlus imp)
   {
      int type = imp.getType();
      if (type == 4)
         return null;
      ImageProcessor ip = imp.getProcessor();
      boolean minMaxChange = false;
      if ((type == 1) || (type == 2)) {
         if ((ip.getMin() != this.previousMin) || (ip.getMax() != this.previousMax))
            minMaxChange = true;
         this.previousMin = ip.getMin();
         this.previousMax = ip.getMax();
      }
      int id = imp.getID();
      if ((minMaxChange) || (id != this.previousImageID) || (type != this.previousImageType)) {
         this.invertedLut = imp.isInvertedLut();
         this.minThreshold = ip.getMinThreshold();
         this.maxThreshold = ip.getMaxThreshold();
         if (this.minThreshold == -808080.0D) {
            this.minThreshold = 65.0D;
            this.maxThreshold = 255.0D;
         }
         this.plot.setHistogram(imp);
         scaleUpAndSet(ip, this.minThreshold, this.maxThreshold);
         updateLabels(imp, ip);
         updatePlot();
         updateScrollBars();
         imp.updateAndDraw();
     }
     this.previousImageID = id;
     this.previousImageType = type;
     return ip;
   }
 
   void scaleUpAndSet(ImageProcessor ip, double minThreshold, double maxThreshold)
   {
      if ((!(ip instanceof ByteProcessor)) && (minThreshold != -808080.0D)) {
         double min = ip.getMin();
         double max = ip.getMax();
         if (max > min) {
            minThreshold = min + minThreshold / 255.0D * (max - min);
            maxThreshold = min + maxThreshold / 255.0D * (max - min);
         } 
         else {
            minThreshold = -808080.0D;
         }
      }
      ip.setThreshold(minThreshold, maxThreshold, this.lutColor);
   }
 
   double scaleDown(ImageProcessor ip, double threshold)
   {
      double min = ip.getMin();
      double max = ip.getMax();
      if (max > min) {
         return (threshold - min) / (max - min) * 255.0D;
      }
      return -808080.0D;
   }
 
   double scaleUp(ImageProcessor ip, double threshold)
   {
      double min = ip.getMin();
      double max = ip.getMax();
      if (max > min) {
         return min + threshold / 255.0D * (max - min);
      }
      return -808080.0D;
   }
 
   void updatePlot() {
      this.plot.minThreshold = this.minThreshold;
      this.plot.maxThreshold = this.maxThreshold;
      this.plot.blackAndWhite = this.blackAndWhite;
      this.plot.repaint();
   }
 
   void updateLabels(ImagePlus imp, ImageProcessor ip) {
      double min = ip.getMinThreshold();
      double max = ip.getMaxThreshold();
      if (min == -808080.0D) {
         this.label1.setText("");
         this.label2.setText("");
      } 
      else {
         Calibration cal = imp.getCalibration();
         if (cal.calibrated()) {
            min = cal.getCValue((int)min);
            max = cal.getCValue((int)max);
         }
         if ((((int)min == min) && ((int)max == max)) || ((ip instanceof ShortProcessor))) {
            this.label1.setText("" + (int)min);
            this.label2.setText("" + (int)max);
         } 
         else {
            this.label1.setText("" + IJ.d2s(min, 2));
            this.label2.setText("" + IJ.d2s(max, 2));
         }
      }
   }
 
   void updateScrollBars() {
      this.minSlider.setValue((int)this.minThreshold);
      this.maxSlider.setValue((int)this.maxThreshold);
   }
 
   void doMasking(ImagePlus imp, ImageProcessor ip)
   {
      ImageProcessor mask = imp.getMask();
      if (mask != null)
         ip.reset(mask); 
   }
 
   void adjustMinThreshold(ImagePlus imp, ImageProcessor ip, double value) {
      if (IJ.altKeyDown()) {
         double width = this.maxThreshold - this.minThreshold;
         if (width < 1.0D)
             width = 1.0D;
         this.minThreshold = value;
         this.maxThreshold = (this.minThreshold + width);
         if (this.minThreshold + width > 255.0D) {
            this.minThreshold = (255.0D - width);
            this.maxThreshold = (this.minThreshold + width);
            this.minSlider.setValue((int)this.minThreshold);
         }
         this.maxSlider.setValue((int)this.maxThreshold);
         scaleUpAndSet(ip, this.minThreshold, this.maxThreshold);
         return;
     }
     this.minThreshold = value;
     if (this.maxThreshold < this.minThreshold) {
        this.maxThreshold = this.minThreshold;
        this.maxSlider.setValue((int)this.maxThreshold);
     }
     scaleUpAndSet(ip, this.minThreshold, this.maxThreshold);
   }
 
   void adjustMaxThreshold(ImagePlus imp, ImageProcessor ip, int cvalue) {
      this.maxThreshold = cvalue;
      if (this.minThreshold > this.maxThreshold) {
         this.minThreshold = this.maxThreshold;
         this.minSlider.setValue((int)this.minThreshold);
      }
      scaleUpAndSet(ip, this.minThreshold, this.maxThreshold);
   }
 
   void reset(ImagePlus imp, ImageProcessor ip) {
      this.plot.setHistogram(imp);
      ip.setThreshold(-808080.0D, 0.0D, 0);
      updateScrollBars();
   }
 
   void doSet(ImagePlus imp, ImageProcessor ip) {
      double level1 = ip.getMinThreshold();
      double level2 = ip.getMaxThreshold();
      if (level1 == -808080.0D) {
         level1 = scaleUp(ip, 65.0D);
         level2 = scaleUp(ip, 255.0D);
      }
      GenericDialog gd = new GenericDialog("Set Threshold Levels");
      gd.addNumericField("Lower Threshold Level: ", level1, 0);
      gd.addNumericField("Upper Threshold Level: ", level2, 0);
      gd.showDialog();
      if (gd.wasCanceled())
         return;
      level1 = gd.getNextNumber();
      level2 = gd.getNextNumber();
 
      if (level2 < level1)
         level2 = level1;
      double minDisplay = ip.getMin();
      double maxDisplay = ip.getMax();
      ip.resetMinAndMax();
      double minValue = ip.getMin();
      double maxValue = ip.getMax();
      if (level1 < minValue)
         level1 = minValue;
      if (level2 > maxValue)
         level2 = maxValue;
      boolean outOfRange = (level1 < minDisplay) || (level2 > maxDisplay);
      if (outOfRange)
         this.plot.setHistogram(imp);
      else
         ip.setMinAndMax(minDisplay, maxDisplay);
 
      this.minThreshold = scaleDown(ip, level1);
      this.maxThreshold = scaleDown(ip, level2);
      scaleUpAndSet(ip, this.minThreshold, this.maxThreshold);
      updateScrollBars();
   }
 
   void apply(ImagePlus imp, ImageProcessor ip) {
      boolean not8Bits = !(ip instanceof ByteProcessor);
      if (not8Bits) {
         double min = ip.getMin();
         double max = ip.getMax();
         ip.setMinAndMax(min, max);
         ip = new ByteProcessor(ip.createImage());
      }
 
      boolean useBlackAndWhite = this.stateB.getLabel().equals("Red");
      if (!useBlackAndWhite) {
         GenericDialog gd = new GenericDialog("Apply Lut", this);
         gd.addCheckbox("Set thresholded pixels to foreground color", fill1);
         gd.addCheckbox("Set remaining pixels to background color", fill2);
         gd.addMessage("");
         gd.addCheckbox("Black forground, white background", useBW);
         gd.showDialog();
         if (gd.wasCanceled())
            return;
         fill1 = gd.getNextBoolean();
         fill2 = gd.getNextBoolean();
         useBW = useBlackAndWhite = gd.getNextBoolean();
      } 
      else {
         fill1 = true;
         fill2 = true;
      }
 
      Undo.setup(1, imp);
      ip.snapshot();
      reset(imp, ip);
 
      int savePixel = ip.getPixel(0, 0);
 
      if (useBlackAndWhite)
         ip.setColor(Color.black);
      else
         ip.setColor(Toolbar.getForegroundColor());
      
      ip.drawPixel(0, 0);
      int fcolor = ip.getPixel(0, 0);
      if (useBlackAndWhite)
         ip.setColor(Color.white);
      else
         ip.setColor(Toolbar.getBackgroundColor());
     
      ip.drawPixel(0, 0);
      int bcolor = ip.getPixel(0, 0);
      ip.setColor(Toolbar.getForegroundColor());
      ip.putPixel(0, 0, savePixel);
 
      int[] lut = new int[256];
      
      for (int i = 0; i < 256; i++) {
         if ((i >= this.minThreshold) && (i <= this.maxThreshold))
            lut[i] = (fill1 ? fcolor : (byte)i);
         else
            lut[i] = (fill2 ? bcolor : (byte)i);
      }
      
      if (not8Bits) {
         ip.applyTable(lut);
         new ImagePlus(imp.getTitle(), ip).show();
      }
      if (imp.getStackSize() > 1) {
         ImageStack stack = imp.getStack();
         YesNoCancelDialog d = new YesNoCancelDialog(this, "Entire Stack?", "Apply threshold to all " + stack.getSize() + " slices in the stack?");
 
         if (d.cancelPressed())
            return;
         if (d.yesPressed())
            new StackProcessor(stack, ip).applyTable(lut);
         else
            ip.applyTable(lut);
      } 
      else {
         ip.applyTable(lut);
      }
      imp.changes = true;
      if (this.plot.histogram != null)
         this.plot.setHistogram(imp);
   }
 
   void changeState(ImagePlus imp, ImageProcessor ip)
   {
      scaleUpAndSet(ip, this.minThreshold, this.maxThreshold);
      updateScrollBars();
   }
 
   void autoThreshold(ImagePlus imp, ImageProcessor ip) {
      if (!(ip instanceof ByteProcessor))
         return;
      ImageStatistics stats = imp.getStatistics(24);
      int threshold = ((ByteProcessor)ip).getAutoThreshold();
      if (stats.max - stats.mode < stats.mode - stats.min) {
         this.minThreshold = stats.min;
         this.maxThreshold = threshold;
      } 
      else {
         this.minThreshold = threshold;
         this.maxThreshold = stats.max;
      }
      scaleUpAndSet(ip, this.minThreshold, this.maxThreshold);
      updateScrollBars();
   }
 
   public void run()
   {
      while (!this.done) {
         synchronized (this) {
            try { wait(); } catch (InterruptedException e) {}
         }
         doUpdate();
      }
   }
 
   void doUpdate()
   {
      int min = this.minValue;
      int max = this.maxValue;
      int action;
      if (this.doReset) { 
         action = 0;
      }
      else {
         if (this.doAutoAdjust) { 
            action = 1;
         }
         else {
            if (this.doApplyLut) { 
               action = 3;
            }
            else {
               if (this.doStateChange) {
                  action = 4;
               }
               else {
                  if (this.doSet) { 
                     action = 7;
                  }
                  else {
                     if (this.minValue >= 0)
                        action = 5;
                     else if (this.maxValue >= 0)
                        action = 6;
                     else
                        return; 
                  }
              }
            }
         }
      }
      this.minValue = -1;
      this.maxValue = -1;
      this.doReset = false;
      this.doAutoAdjust = false;
      this.doApplyLut = false;
      this.doStateChange = false;
      this.doSet = false;
      ImagePlus imp = WindowManager.getCurrentImage();
      if (imp == null) {
         IJ.beep();
         IJ.showStatus("No image");
         return;
      }
      
      if (!imp.lock()) {
         imp = null;
         return;
      }
      
      ImageProcessor ip = setup(imp);
      if (ip == null) {
         imp.unlock();
         IJ.beep();
         IJ.showStatus("RGB images cannot be thresolded");
         return;
      }
 
      switch (action)
      {
      case 0:
         reset(imp, ip); break;
      case 1:
         autoThreshold(imp, ip); break;
      case 3:
         apply(imp, ip); break;
      case 4:
         changeState(imp, ip); break;
      case 7:
         doSet(imp, ip); break;
      case 5:
         adjustMinThreshold(imp, ip, min); break;
      case 6:
         adjustMaxThreshold(imp, ip, max);
      case 2: 
      }
      
      updatePlot();
      updateLabels(imp, ip);
      ip.setLutAnimation(true);
      imp.updateAndDraw();
      imp.unlock();
   }
 
   public void processWindowEvent(WindowEvent e) {
      super.processWindowEvent(e);
      if (e.getID() == 201)
         instance = null;
   }
 
   protected synchronized void done()
   {
      this.done = true;
      instance = null;
      notifyAll();
   }
 
   public synchronized void syncAdjust() throws InterruptedException {
      while (!this.done)
      wait();
   }
 }
