 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.modules.audiomanager.client;
 
 import java.awt.Color;
 import org.jdesktop.wonderland.modules.audiomanager.common.VolumeConverter;
 
 import java.io.IOException;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.ImageIcon;
 import org.jdesktop.wonderland.client.jme.VMeter;
 import org.jdesktop.wonderland.client.softphone.MicrophoneInfoListener;
 import org.jdesktop.wonderland.client.softphone.SoftphoneControl;
 import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
 import org.jdesktop.wonderland.client.softphone.SoftphoneListener;
 import org.jdesktop.wonderland.client.softphone.SpeakerInfoListener;
 
 /**
  * A microphone level control panel.
  *
  * @author jp
  * @author nsimpson
  */
 public class VuMeterPanel extends javax.swing.JPanel implements
         SoftphoneListener, MicrophoneInfoListener, SpeakerInfoListener, DisconnectListener {
 
     private static final Logger LOGGER =
             Logger.getLogger(VuMeterPanel.class.getName());
     private static final double DEFAULT_WARNING_LIMIT = 0.9d;
     private AudioManagerClient client;
     private VMeter micMeter;
     private VMeter speakerMeter;
     private int count;
     private int speakerCount;
     private VolumeConverter volumeConverter;
     private Color micPanelBackground;
     private Color speakerPanelBackground;
     private Color overLimitColor = Color.RED;
     private double micWarningLimit = DEFAULT_WARNING_LIMIT;
     private double speakerWarningLimit = DEFAULT_WARNING_LIMIT;
     private ImageIcon micMutedIcon;
     private ImageIcon micUnmutedIcon;
     private ImageIcon speakerMutedIcon;
     private ImageIcon speakerUnmutedIcon;
 
     public VuMeterPanel() {
         this(null);
     }
 
     public VuMeterPanel(AudioManagerClient client) {
         this.client = client;
 
         initComponents();
         micPanelBackground = micMeterPanel.getBackground();
         speakerPanelBackground = speakerMeterPanel.getBackground();
         micMutedIcon = new ImageIcon(getClass().getResource(
                 "/org/jdesktop/wonderland/modules/audiomanager/client/" +
                 "resources/UserListMicMuteOn24x24.png"));
         micUnmutedIcon = new ImageIcon(getClass().getResource(
                 "/org/jdesktop/wonderland/modules/audiomanager/client/" +
                 "resources/UserListMicMuteOff24x24.png"));
         speakerMutedIcon = new ImageIcon(getClass().getResource(
                 "/org/jdesktop/wonderland/modules/audiomanager/client/" +
                 "resources/UserListSpeakerMuteOn24x24.png"));
         speakerUnmutedIcon = new ImageIcon(getClass().getResource(
                 "/org/jdesktop/wonderland/modules/audiomanager/client/" +
                 "resources/UserListSpeakerMuteOff24x24.png"));
 
         volumeConverter = new VolumeConverter(micVolumeSlider.getMaximum());
 
         if (client != null) {
             client.addDisconnectListener(this);
         }
 
         // microphone volume meter
         micMeter = new VMeter("");
         micMeter.setBackground(Color.WHITE);
         micMeter.setForeground(Color.DARK_GRAY);
         micMeter.setPreferredSize(micMeterPanel.getPreferredSize());
         micMeter.setShowValue(false);
         micMeter.setShowTicks(false);
         micMeter.setMaxValue(1D);
         micMeter.setWarningValue(micWarningLimit);
         micMeter.setVisible(true);
         micMeterPanel.add(micMeter);
 
         // speaker volume meter
         speakerMeter = new VMeter("");
         speakerMeter.setBackground(Color.WHITE);
         speakerMeter.setForeground(Color.DARK_GRAY);
        speakerMeter.setPreferredSize(micMeterPanel.getPreferredSize());
         speakerMeter.setShowValue(false);
         speakerMeter.setShowTicks(false);
         speakerMeter.setMaxValue(1D);
         speakerMeter.setWarningValue(speakerWarningLimit);
         speakerMeter.setVisible(true);
         speakerMeterPanel.add(speakerMeter);
     }
 
     public void startVuMeter(boolean start) {
 	startMicVuMeter(start);
 	startSpeakerVuMeter(start);
     }
 
     public void disconnected() {
         startMicVuMeter(false);
         startSpeakerVuMeter(false);
     }
 
     public void startMicVuMeter(final boolean startVuMeter) {
         client.removeDisconnectListener(this);
 
         SoftphoneControl sc = SoftphoneControlImpl.getInstance();
 
 	sc.removeSoftphoneListener(this);
         sc.removeMicrophoneInfoListener(this);
 
         if (startVuMeter) {
             client.addDisconnectListener(this);
             sc.addSoftphoneListener(this);
             sc.addMicrophoneInfoListener(this);
 
             try {
                 sc.sendCommandToSoftphone("getMicrophoneVolume");
             } catch (IOException e) {
                 LOGGER.log(Level.WARNING,
                         "Unable to get Microphone volume", e);
             }
         }
 
 	try {
             sc.startMicVuMeter(startVuMeter);
 	} catch (IOException e) {
 	    LOGGER.log(Level.WARNING, 
 		"Unable to start mic VU meter:  " + e.getMessage());
 	}
 
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 setVisible(startVuMeter);
             }
         });
     }
 
     public void startSpeakerVuMeter(final boolean startVuMeter) {
         client.removeDisconnectListener(this);
 
         SoftphoneControl sc = SoftphoneControlImpl.getInstance();
 
 	sc.removeSoftphoneListener(this);
         sc.removeSpeakerInfoListener(this);
 
         if (startVuMeter) {
             client.addDisconnectListener(this);
 	    sc.addSoftphoneListener(this);
             sc.addSpeakerInfoListener(this);
 
             try {
                 sc.sendCommandToSoftphone("getSpeakerVolume");
             } catch (IOException e) {
                 LOGGER.log(Level.WARNING,
                         "Unable to get speaker volume", e);
             }
         }
 
 	try {
 	    sc.startSpeakerVuMeter(startVuMeter);
 	} catch (IOException e) {
 	    LOGGER.log(Level.WARNING,
 		"Unable to start speaker VU Meter:  " + e.getMessage());
 	}
     }
 
     public void softphoneVisible(boolean isVisible) {
     }
 
     private boolean muted;
 
     public void softphoneMuted(boolean muted) {
 	if (this.muted == muted) {
 	    return;
 	}
 
 	this.muted = muted;
 
 	setMicVolumeSlider(muted);
 
         if (muted) {
             micMuteButton.setIcon(micMutedIcon);
 	    setMicrophoneVolume(0);
         } else {
             micMuteButton.setIcon(micUnmutedIcon);
 	    setMicrophoneVolume(previousMicVolumeSliderValue);
         }
     }
 
     public void softphoneConnected(boolean connected) {
 	try {
             //SoftphoneControlImpl.getInstance().startMicVuMeter(connected);
             SoftphoneControlImpl.getInstance().startSpeakerVuMeter(connected);
 	} catch (IOException e) {
 	    LOGGER.log(Level.WARNING, 
 		"Unable to start/stop VU meter:  " + e.getMessage());
 	}
     }
 
     public void softphoneExited() {
     }
 
     public void microphoneGainTooHigh() {
     }
 
     public void microphoneVuMeterValue(String value) {
         double volume = Math.abs(Double.parseDouble(value));
 
         final double v = Math.round(Math.sqrt(volume) * 100) / 100D;
 
 	//System.out.println("Mic value " + value + " volume " + v);
 
 	java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 micMeter.setValue(v);
 
                 if (v > micWarningLimit) {
                     micMeterPanel.setBackground(overLimitColor);
                 } else {
                     micMeterPanel.setBackground(micPanelBackground);
                 }
             }
 	});
     }
 
     public void microphoneVolume(String data) {
         micVolumeSlider.setValue(volumeConverter.getVolume((Float.parseFloat(data))));
     }
 
     public void speakerVuMeterValue(String value) {
         double volume = Math.abs(Double.parseDouble(value));
 
         final double v = Math.round(Math.sqrt(volume) * 100) / 100D;
 
 	//System.out.println("Speaker value " + value + " volume " + v);
 
 	java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 speakerMeter.setValue(v);
 
                 if (v > speakerWarningLimit) {
                     speakerMeterPanel.setBackground(overLimitColor);
                 } else {
                    speakerMeterPanel.setBackground(micPanelBackground);
                 }
             }
         });
     }
 
     public void speakerVolume(String data) {
         speakerVolumeSlider.setValue(volumeConverter.getVolume((Float.parseFloat(data))));
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         micVolumeSlider = new javax.swing.JSlider();
         micMeterPanel = new javax.swing.JPanel();
         micMuteButton = new javax.swing.JButton();
         speakerVolumeSlider = new javax.swing.JSlider();
         speakerMeterPanel = new javax.swing.JPanel();
         speakerMuteButton = new javax.swing.JButton();
 
         setPreferredSize(new java.awt.Dimension(95, 205));
         setLayout(null);
 
         micVolumeSlider.setMinorTickSpacing(10);
         micVolumeSlider.setOrientation(javax.swing.JSlider.VERTICAL);
         micVolumeSlider.setPaintTicks(true);
         micVolumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 micVolumeSliderStateChanged(evt);
             }
         });
         add(micVolumeSlider);
         micVolumeSlider.setBounds(5, 14, 20, 155);
 
         micMeterPanel.setMinimumSize(new java.awt.Dimension(30, 160));
         micMeterPanel.setPreferredSize(new java.awt.Dimension(30, 160));
         add(micMeterPanel);
         micMeterPanel.setBounds(20, 10, 20, 160);
 
         micMuteButton.setFont(new java.awt.Font("Arial", 1, 8));
         micMuteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListMicMuteOff24x24.png"))); // NOI18N
         micMuteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 micMuteButtonActionPerformed(evt);
             }
         });
         add(micMuteButton);
         micMuteButton.setBounds(20, 175, 24, 24);
 
         speakerVolumeSlider.setMinorTickSpacing(10);
         speakerVolumeSlider.setOrientation(javax.swing.JSlider.VERTICAL);
         speakerVolumeSlider.setPaintTicks(true);
         speakerVolumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 speakerVolumeSliderStateChanged(evt);
             }
         });
         add(speakerVolumeSlider);
         speakerVolumeSlider.setBounds(50, 14, 20, 155);
 
         speakerMeterPanel.setMinimumSize(new java.awt.Dimension(30, 160));
         speakerMeterPanel.setPreferredSize(new java.awt.Dimension(30, 160));
         add(speakerMeterPanel);
         speakerMeterPanel.setBounds(65, 10, 20, 160);
 
         speakerMuteButton.setFont(new java.awt.Font("Arial", 1, 8));
         speakerMuteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListSpeakerMuteOff24x24.png"))); // NOI18N
         speakerMuteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 speakerMuteButtonActionPerformed(evt);
             }
         });
         add(speakerMuteButton);
         speakerMuteButton.setBounds(65, 175, 24, 24);
     }// </editor-fold>//GEN-END:initComponents
 
     private int micVolumeSliderValue = 50;
     private int previousMicVolumeSliderValue = 50;
 
     private void micVolumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_micVolumeSliderStateChanged
         SoftphoneControl sc = SoftphoneControlImpl.getInstance();
 
 	previousMicVolumeSliderValue = micVolumeSliderValue;
 	micVolumeSliderValue = micVolumeSlider.getValue();
 
 	if (previousMicVolumeSliderValue == 0) {
             SoftphoneControlImpl.getInstance().mute(false);
 	} else if (micVolumeSliderValue == 0) {
             SoftphoneControlImpl.getInstance().mute(true);
 	}
 
         setMicrophoneVolume(volumeConverter.getVolume(micVolumeSliderValue));
     }//GEN-LAST:event_micVolumeSliderStateChanged
 
     private void setMicrophoneVolume(float volume) {
         try {
             SoftphoneControlImpl.getInstance().sendCommandToSoftphone("microphoneVolume=" + volume);
         } catch (IOException e) {
             LOGGER.log(Level.WARNING,
 		"Unable to send microphone volume command to softphone", e);
         }
     }
 
     private boolean speakerMuted;
     private int speakerVolumeSliderValue = 50;
     private int previousSpeakerVolumeSliderValue = 50;
 
     private void speakerVolumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speakerVolumeSliderStateChanged
 	previousSpeakerVolumeSliderValue = speakerVolumeSliderValue;
 	speakerVolumeSliderValue = speakerVolumeSlider.getValue();
 
         double volume = volumeConverter.getVolume(speakerVolumeSliderValue);
 
 	if (previousSpeakerVolumeSliderValue == 0) {
 	    setSpeakerMutedIcon(false);
 	} else if (speakerVolumeSliderValue == 0) {
 	    setSpeakerMutedIcon(true);
 	}
 
         try {
             SoftphoneControlImpl.getInstance().sendCommandToSoftphone("speakerVolume=" + volume);
         } catch (IOException e) {
             LOGGER.log(Level.WARNING,
 		"Unable to send speaker volume command to softphone", e);
         }
     }//GEN-LAST:event_speakerVolumeSliderStateChanged
 
     private void micMuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_micMuteButtonActionPerformed
         SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
 
         sc.mute(!sc.isMuted());
     }//GEN-LAST:event_micMuteButtonActionPerformed
 
     private void setMicVolumeSlider(final boolean isMuted) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 setMicVolumeSliderLater(isMuted);
             }
         });
     }
 
     private void setMicVolumeSliderLater(boolean isMuted) {
 	if (isMuted) {
 	    micVolumeSlider.setValue(0);
 	} else {
 	    micVolumeSlider.setValue(previousMicVolumeSliderValue);
 	}
     }
 
     private void speakerMuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speakerMuteButtonActionPerformed
 	boolean speakerMuted = !this.speakerMuted;
 
 	if (speakerMuted) {
 	    speakerVolumeSlider.setValue(0);
 	} else {
 	    speakerVolumeSlider.setValue(previousSpeakerVolumeSliderValue);
 	}
 
 	setSpeakerMutedIcon(speakerMuted);
     }//GEN-LAST:event_speakerMuteButtonActionPerformed
 
     private void setSpeakerMutedIcon(boolean speakerMuted) {
 	this.speakerMuted = speakerMuted;
 
 	if (speakerMuted) {
 	    speakerMuteButton.setIcon(speakerMutedIcon);
 	} else {
 	    speakerMuteButton.setIcon(speakerUnmutedIcon);
 	}
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel micMeterPanel;
     private javax.swing.JButton micMuteButton;
     private javax.swing.JSlider micVolumeSlider;
     private javax.swing.JPanel speakerMeterPanel;
     private javax.swing.JButton speakerMuteButton;
     private javax.swing.JSlider speakerVolumeSlider;
     // End of variables declaration//GEN-END:variables
 }
