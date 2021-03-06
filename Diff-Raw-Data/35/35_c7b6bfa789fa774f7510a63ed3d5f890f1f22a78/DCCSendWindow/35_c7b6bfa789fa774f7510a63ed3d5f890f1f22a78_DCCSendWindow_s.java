 /*
  * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.dcc;
 
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.addons.dcc.actions.DCCActions;
 
 import com.dmdirc.ui.swing.components.TextFrame;
 import com.dmdirc.ui.swing.components.TextLabel;
 
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import javax.swing.JButton;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * This class links DCC Send objects to a window.
  *
  * @author Shane 'Dataforce' McCormack
  * @version $Id: DCC.java 969 2007-04-30 18:38:20Z ShaneMcC $
  */
 public class DCCSendWindow extends DCCFrame implements DCCSendInterface, ActionListener {
 	/** The DCCSend object we are a window for */
 	private final DCCSend dcc;
 	
 	/** My Nickname */
 	private final String nickname;
 	
 	/** Other Nickname */
 	private final String otherNickname;
 	
 	/** Total data transfered */
 	private long transferCount = 0;
 	
 	/** Time Started */
 	private long timeStarted = 0;
 	
 	/** Progress Bar */
 	private final JProgressBar progress = new JProgressBar();
 	
 	/** Status Label */
 	private final JLabel status = new JLabel("Status: Waiting");
 	
 	/** Speed Label */
 	private final JLabel speed = new JLabel("Speed: Unknown");
 	
 	/** Time Label */
 	private final JLabel remaining = new JLabel("Time Remaining: Unknown");
 	
 	/** Time Taken */
 	private final JLabel taken = new JLabel("Time Taken: 00:00");
 	
 	/** Button */
 	private final JButton button = new JButton("Cancel");
 	
 	/**
 	 * Creates a new instance of DCCSendWindow with a given DCCSend object.
 	 *
 	 * @param plugin the DCC Plugin responsible for this window
 	 * @param dcc The DCCSend object this window wraps around
 	 * @param title The title of this window
 	 * @param nick My Current Nickname
 	 * @param targetNick Nickname of target
 	 */
 	public DCCSendWindow(final DCCPlugin plugin, final DCCSend dcc, final String title, final String nick, final String targetNick) {
		super(plugin, title, dcc.getType() == DCCSend.TransferType.SEND ? 
            "dcc-send-inactive" : "dcc-receive-inactive");
 		this.dcc = dcc;
 		dcc.setHandler(this);
 		nickname = nick;
 		otherNickname = targetNick;
 		
 		getContentPane().setLayout(new MigLayout());
 		
 		progress.setMinimum(0);
 		progress.setMaximum(100);
 		progress.setStringPainted(true);
 		progress.setValue(0);
 		
 		if (dcc.getType() == DCCSend.TransferType.SEND) {
 			getContentPane().add(new JLabel("Sending: "+dcc.getShortFileName()), "wrap");
 			getContentPane().add(new JLabel("To: "+targetNick), "wrap");
 		} else {
 			getContentPane().add(new JLabel("Recieving: "+dcc.getShortFileName()), "wrap");
 			getContentPane().add(new JLabel("From: "+targetNick), "wrap");
 		}
 		getContentPane().add(status, "wrap");
 		getContentPane().add(speed, "wrap");
 		getContentPane().add(remaining, "wrap");
 		getContentPane().add(taken, "wrap");
 		getContentPane().add(progress, "growx, wrap");
 		
 		button.addActionListener(this);
 		getContentPane().add(button, "wrap, align right");
 		
 		plugin.addWindow(this);
 	}
 	
 	/**
 	 * Get the DCCSend Object associated with this window
 	 *
 	 * @return The DCCSend Object associated with this window
 	 */
 	public DCCSend getDCC() {
 		return dcc;
 	}
 	
 	/** {@inheritDoc} */
 	public void actionPerformed(final ActionEvent e) {
 		if (e.getActionCommand().equals("Cancel")) {
 			button.setEnabled(false);
 			dcc.close();
 		}
 	}
 	
 	/**
 	 * Called when data is sent/recieved
 	 *
 	 * @param dcc The DCCSend that this message is from
 	 * @param bytes The number of new bytes that were transfered
 	 */
 	public void dataTransfered(final DCCSend dcc, final int bytes) {
 		transferCount += bytes;
 		final double percent = (100.00 / dcc.getFileSize()) * (transferCount + dcc.getFileStart());
 		
 		if (dcc.getType() == DCCSend.TransferType.SEND) {
 			status.setText("Status: Sending");
 		} else {
 			status.setText("Status: Recieving");
 		}
 		
 		updateSpeedAndTime();
 		
 		progress.setValue((int)Math.floor(percent));
 		
 		ActionManager.processEvent(DCCActions.DCC_SEND_DATATRANSFERED, null, this, bytes);
 	}
 	
 	/**
 	 * Update the transfer speed, time remaining and time taken labels.
 	 */
 	public void updateSpeedAndTime() {
 		final long time = (System.currentTimeMillis() - timeStarted) / 1000;
 		final double bytesPerSecond = (time > 0) ? (transferCount / time) : 0;
 		
 		if (bytesPerSecond > 1048576) {
 			speed.setText(String.format("Speed: %.2f MB/s", (bytesPerSecond/1048576)));
 		} else if (bytesPerSecond > 1024) {
 			speed.setText(String.format("Speed: %.2f KB/s", (bytesPerSecond/1024)));
 		} else {
 			speed.setText(String.format("Speed: %f B/s", bytesPerSecond));
 		}
 		
 		final long remaningBytes = dcc.getFileSize() - dcc.getFileStart() - transferCount;
 		final double remainingSeconds = (bytesPerSecond > 0) ? (remaningBytes / bytesPerSecond) : 1;
 		
 		remaining.setText(String.format("Time Remaining: %s", duration((int)Math.floor(remainingSeconds))));
 		taken.setText(String.format("Time Taken: %s", duration(time)));
 	}
 	
 	/**
 	 * Get the duration in seconds as a string.
 	 *
 	 * @param secondsInput to get duration for
 	 * @return Duration as a string
 	 */
 	private String duration(final long secondsInput) {
 		final StringBuilder result = new StringBuilder();
 		final long hours = (secondsInput / 3600);
 		final long minutes = (secondsInput / 60 % 60);
 		final long seconds = (secondsInput % 60);
 		
 		if (hours > 0) { result.append(hours+":"); }
 		result.append(String.format("%0,2d:%0,2d",minutes,seconds));
 		
 		return result.toString();
 	}
 	
 	/**
 	 * Called when the socket is closed
 	 *
 	 * @param dcc The DCCSend that this message is from
 	 */
 	@Override
 	public void socketClosed(final DCCSend dcc) {
 		ActionManager.processEvent(DCCActions.DCC_SEND_SOCKETCLOSED, null, this);
 		button.setEnabled(false);
 		if (transferCount == dcc.getFileSize()) {
 			status.setText("Status: Transfer Compelete.");
 			progress.setValue(100);
 		} else {
 			status.setText("Status: Transfer Failed.");
 		}
 		updateSpeedAndTime();
        setIcon(dcc.getType() == DCCSend.TransferType.SEND ? 
            "dcc-send-active" : "dcc-receive-active");
 	}
 	
 	/**
 	 * Called when the socket is opened
 	 *
 	 * @param dcc The DCCSend that this message is from
 	 */
 	@Override
 	public void socketOpened(final DCCSend dcc) {
 		ActionManager.processEvent(DCCActions.DCC_SEND_SOCKETOPENED, null, this);
 		status.setText("Status: Socket Opened");
 		timeStarted = System.currentTimeMillis();
        setIcon(dcc.getType() == DCCSend.TransferType.SEND ? 
            "dcc-send-inactive" : "dcc-receive-inactive");
 	}
 	
 	/**
 	 * Closes this container (and it's associated frame).
 	 */
 	@Override
 	public void windowClosing() {
 		dcc.close();
 		super.windowClosing();
 	}
 }
