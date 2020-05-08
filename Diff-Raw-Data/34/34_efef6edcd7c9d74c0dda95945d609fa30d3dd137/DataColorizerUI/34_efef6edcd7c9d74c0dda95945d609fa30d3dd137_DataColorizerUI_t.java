 //
 // DataColorizerUI.java
 //
 
 /*
 SLIMPlugin for combined spectral-lifetime image analysis.
 
 Copyright (c) 2010, UW-Madison LOCI
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the UW-Madison LOCI nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package loci.slim.colorizer;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JFrame;
 
 /**
  * This frame shows a histogram of current lifetime distributions, a color bar
  * used to colorize the lifetime data, and an input panel that allows the user
  * to control the colorization.
  *
  * @author Aivar Grislis grislis at wisc.edu
  */
 public class DataColorizerUI implements IColorizeRangeListener {
     Object m_synchObject = new Object();
     JFrame m_frame;
     IColorizeRangeListener m_listener;
     Histogram m_histogram;
     ColorBar m_colorBar;
     InputPanel m_inputPanel;
     boolean m_auto;
     double m_start;
     double m_stop;
     double m_min;
     double m_max;
 
     public DataColorizerUI(IColorize colorize, IColorizeRangeListener listener) {
         m_listener = listener;
         
         m_auto = true;
         m_start = m_stop = m_min = m_max = 0.0;
 
         m_histogram = new Histogram(320, 160, this);
         m_colorBar = new ColorBar(320, 20, colorize);
         m_inputPanel = new InputPanel(this);
 
         m_frame = new JFrame("Colorize");
        //TODO closes the entire plugin:
        //m_frame.setDefaultCloseOperation(m_frame.EXIT_ON_CLOSE);
         m_frame.setResizable(false);
         m_frame.getContentPane().add(m_histogram, BorderLayout.NORTH);
         m_frame.getContentPane().add(m_colorBar, BorderLayout.CENTER);
         m_frame.getContentPane().add(m_inputPanel, BorderLayout.SOUTH);
         m_frame.pack();
         m_frame.setVisible(true);
     }
 
     /**
      * Gets updates periodically as the fit progresses and the lifetime
      * distribution changes.
      *
      * @param lifetime
      * @param min
      * @param max
      */
     void updateData(double lifetime[], double min, double max) {
         synchronized (m_synchObject) {
             m_min = min;
             m_max = max;
             m_histogram.updateData(lifetime, min, max);
             if (m_auto) {
                 m_start = min;
                 m_stop = max;
             }
             m_colorBar.setRange(m_auto, m_start, m_stop, min, max);
             m_inputPanel.setRange(m_auto, m_start, m_stop, min, max);
         }
     }
 
     /**
      * Responds to external changes in the range settings.  Passes
      * them on to the histogram, color bar, and input panel and any
      * registered listener.
      *
      * @param auto
      * @param start
      * @param stop
      * @param min
      * @param max
      */
     public void setRange(boolean auto, double start, double stop, double min, double max) {
         synchronized (m_synchObject) {
            m_auto = auto;
            m_start = start;
            m_stop = stop;
            m_min = min;
            m_max = max;
 
            m_histogram.setRange(auto, start, stop, min, max);
            m_colorBar.setRange(auto, start, stop, min, max);
            m_inputPanel.setRange(auto, start, stop, min, max);
 
            m_listener.setRange(auto, start, stop, min, max);
         }
     }
 
     /**
      * Makes this UI go away if a fit is cancelled.
      */
     public void quit() {
         m_frame.dispose(); //TODO ? what should I call here
     }
 }
