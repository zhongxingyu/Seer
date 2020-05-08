 // Copyright (C) 2000, 2001, 2002, 2003 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.console.swingui;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import net.grinder.console.common.ConsoleException;
 import net.grinder.console.common.Resources;
 import net.grinder.console.model.ConsoleProperties;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 class SamplingControlPanel extends JPanel {
   private final JSlider m_intervalSlider  = new JSlider(100, 10000, 100);
   private final IntegerField m_collectSampleField =
     new IntegerField(0, 999999);
   private final IntegerField m_ignoreSampleField = new IntegerField(1, 999999);
 
   private final String m_sampleIntervalString;
   private final String m_ignoreSampleString;
   private final String m_collectSampleZeroString;
   private final String m_collectSampleString;
   private final String m_msUnit;
   private final String m_msUnits;
   private final String m_sampleUnit;
   private final String m_sampleUnits;
 
   private ConsoleProperties m_properties = null;
 
   public SamplingControlPanel(Resources resources) {
     m_sampleIntervalString =
       resources.getString("sampleInterval.label") + " ";
 
     m_ignoreSampleString = resources.getString("ignoreCount.label") + " ";
 
     m_collectSampleZeroString =
       resources.getString("collectCountZero.label", false);
     m_collectSampleString =
       resources.getString("collectCount.label") + " ";
 
     m_msUnit = " " + resources.getString("ms.unit");
     m_msUnits = " " + resources.getString("ms.units");
     m_sampleUnit = " " + resources.getString("sample.unit");
     m_sampleUnits = " " + resources.getString("sample.units");
 
     m_intervalSlider.setMajorTickSpacing(1000);
     m_intervalSlider.setMinorTickSpacing(100);
     m_intervalSlider.setPaintTicks(true);
     m_intervalSlider.setSnapToTicks(true);
 
     final JLabel intervalLabel = new JLabel();
 
     m_intervalSlider.addChangeListener(
       new ChangeListener() {
         public void stateChanged(ChangeEvent event) {
          final int value = m_intervalSlider.getValue();
 
           setIntervalLabelText(intervalLabel, value);
 
           if (m_properties != null) {
             try {
               m_properties.setSampleInterval(value);
             }
             catch (ConsoleException e) {
               assertionFailure(e);
             }
           }
         }
       }
       );
 
     final JLabel ignoreSampleLabel = new JLabel();
 
     m_ignoreSampleField.addChangeListener(
       new ChangeListener() {
         public void stateChanged(ChangeEvent event) {
           final int value = m_ignoreSampleField.getValue();
 
           setIgnoreSampleLabelText(ignoreSampleLabel, value);
 
           if (m_properties != null) {
             try {
               m_properties.setIgnoreSampleCount(value);
             }
             catch (ConsoleException e) {
               assertionFailure(e);
             }
           }
         }
       }
       );
 
     final JLabel collectSampleLabel = new JLabel();
 
     m_collectSampleField.addChangeListener(
       new ChangeListener() {
         public void stateChanged(ChangeEvent event) {
           final int value = m_collectSampleField.getValue();
 
           setCollectSampleLabelText(collectSampleLabel, value);
 
           if (m_properties != null) {
             try {
               m_properties.setCollectSampleCount(value);
             }
             catch (ConsoleException e) {
               assertionFailure(e);
             }
           }
         }
       }
       );
 
     final JPanel textFieldLabelPanel = new JPanel(new GridLayout(0, 1, 0, 1));
     textFieldLabelPanel.add(ignoreSampleLabel);
     textFieldLabelPanel.add(collectSampleLabel);
 
     final JPanel textFieldControlPanel =
       new JPanel(new GridLayout(0, 1, 0, 1));
     textFieldControlPanel.add(m_ignoreSampleField);
     textFieldControlPanel.add(m_collectSampleField);
 
     final JPanel textFieldPanel = new JPanel(new BorderLayout());
     textFieldPanel.add(textFieldLabelPanel, BorderLayout.CENTER);
     textFieldPanel.add(textFieldControlPanel, BorderLayout.EAST);
 
     setLayout(new GridLayout(0, 1));
     add(intervalLabel);
     add(m_intervalSlider);
     add(textFieldPanel);
   }
 
   public void setProperties(ConsoleProperties properties) {
     // Disable updates to associated properties.
     m_properties = null;
 
     m_intervalSlider.setValue(properties.getSampleInterval());
     m_ignoreSampleField.setValue(properties.getIgnoreSampleCount());
     m_collectSampleField.setValue(properties.getCollectSampleCount());
 
     // Enable updates to new associated properties.
     m_properties = properties;
   }
 
   public void refresh() {
     setProperties(m_properties);
   }
 
   private void setIntervalLabelText(JLabel label, int sampleInterval) {
     label.setText(m_sampleIntervalString + sampleInterval +
                   (sampleInterval == 1 ? m_msUnit : m_msUnits));
   }
 
   private void setIgnoreSampleLabelText(JLabel label, int ignoreSample) {
     label.setText(m_ignoreSampleString + ignoreSample +
                   (ignoreSample == 1 ? m_sampleUnit : m_sampleUnits));
   }
 
   private void setCollectSampleLabelText(JLabel label, int collectSample) {
     if (collectSample == 0 && m_collectSampleZeroString != null) {
       label.setText(m_collectSampleZeroString);
     }
     else {
       label.setText(m_collectSampleString + collectSample +
                     (collectSample == 1 ? m_sampleUnit : m_sampleUnits));
     }
   }
 
   private void assertionFailure(ConsoleException e) {
     e.printStackTrace();
     throw new RuntimeException(e.getMessage());
   }
 }
 
