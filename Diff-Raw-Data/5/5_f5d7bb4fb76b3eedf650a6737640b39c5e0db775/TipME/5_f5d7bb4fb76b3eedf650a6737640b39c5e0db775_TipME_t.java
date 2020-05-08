 /*
  * @(#)TipME.java
  *
  * Copyright (c) 2004, Erik C. Thauvin (http://www.thauvin.net/erik/)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * Neither the name of the authors nor the names of its contributors may be
  * used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * $Id$
  *
  */
 package net.thauvin.j2me.tipme;
 
 import javax.microedition.lcdui.*;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 
 
 /**
  * The <code>TipME</code> class implements a simple Tip calculator.
  *
  * @author <a href="http://www.thauvin.net/erik/">Erik C. Thauvin</a>
  * @version $Revision$, $Date$
  *
  * @created September 16, 2004
  * @since 1.0
  */
 public class TipME extends MIDlet implements CommandListener
 {
 	/**
 	 * The application name.
 	 */
 	protected /* final */ String appName = "TipME";
 
 	/**
 	 * The application version.
 	 */
 	protected /* final */ String appVersion = "0.1";
 
 	/**
 	 * The <code>About</code> command.
 	 */
 	protected /* final */ Command aboutCommand = new Command("About", Command.SCREEN, 4);
 
 	/**
 	 * The <code>Back</code> command.
 	 */
 	protected /* final */ Command backCommand = new Command("Back", Command.BACK, 2);
 
 	/**
 	 * The <code>Calculate</code> command.
 	 */
 	protected /* final */ Command calcCommand = new Command("Calculate", Command.SCREEN, 2);
 
 	/**
 	 * The <code>Clear</code> command.
 	 */
 	protected /* final */ Command clearCommand = new Command("Clear", Command.SCREEN, 3);
 
 	/**
 	 * The <code>Exit</code> command.
 	 */
 	protected /* final */ Command exitCommand = new Command("Exit", Command.EXIT, 2);
 
 	/**
 	 * The tax amount.
 	 */
 	protected String taxAmount;
 
 	/**
 	 * The tip rate.
 	 */
 	protected String tipRate;
 
 	/**
 	 * The total amount.
 	 */
 	protected String totalAmount;
 	private Display display;
 
 	/**
	 * The main screen.
 	 */
 	private /* final */ MainScreen mainScreen;
 
 	/**
	 * The result screen.
 	 */
 	private /* final */ ResultScreen resultScreen;
 	private int billTotal = 0;
 	private int subTotal = 0;
 	private int taxTotal = 0;
 	private int tipTotal = 0;
 
 	/**
 	 * Creates a new TipME object.
 	 */
 	public TipME()
 	{
 		super();
 
 		mainScreen = new MainScreen(this);
 		resultScreen = new ResultScreen(this);
 	}
 
 	/**
 	 * Performs a command.
 	 *
 	 * @param c The command action.
 	 * @param d The diplayable screen.
 	 */
 	public void commandAction(Command c, Displayable d)
 	{
 		if (c == exitCommand)
 		{
 			exit();
 		}
 		else if (c == clearCommand)
 		{
 			mainScreen.taxFld.setString("");
 			mainScreen.totalFld.setString("");
 			display.setCurrentItem(mainScreen.taxFld);
 		}
 		else if (c == aboutCommand)
 		{
 			/* final */ Alert about =
 				new Alert("About " + appName,
 						  appName + ' ' + appVersion + "\nCopyright 2004\nErik C. Thauvin\nerik@thauvin.net", null,
 						  AlertType.INFO);
 			about.setTimeout(Alert.FOREVER);
 			display.setCurrent(about, d);
 		}
 		else if (c == calcCommand)
 		{
 			totalAmount = mainScreen.totalFld.getString();
 			taxAmount = mainScreen.taxFld.getString();
 			tipRate = mainScreen.tipPopup.getString(mainScreen.tipPopup.getSelectedIndex());
 
 			if ((totalAmount.length() == 0) || (totalAmount.charAt(0) == '0'))
 			{
 				error("Please specify a valid bill amount.", d);
 			}
 			else
 			{
 				calcTip();
 				resultScreen.subtotalItem.setText(getSubTotalAmount() + '\n');
 				resultScreen.taxItem.setText(getTaxAmount() + '\n');
 				resultScreen.tipItem.setText(getTipAmount() + " (" + tipRate + "%)\n");
 				resultScreen.totalItem.setText(getBillAmount());
 				display.setCurrent(resultScreen);
 				display.setCurrentItem(resultScreen.tipItem);
 			}
 		}
 		else if (c == backCommand)
 		{
 			display.setCurrent(mainScreen);
 			display.setCurrentItem(mainScreen.tipPopup);
 		}
 	}
 
 	/**
 	 * Returns to total bill amount after tip.
 	 *
 	 * @return The bill amount.
 	 */
 	protected String getBillAmount()
 	{
 		return parseStr(billTotal);
 	}
 
 	/**
 	 * Returns the subtotal amount after calculation.
 	 *
 	 * @return The subtotal amount.
 	 */
 	protected String getSubTotalAmount()
 	{
 		return parseStr(subTotal);
 	}
 
 	/**
 	 * Returns the tax amount after calculation.
 	 *
 	 * @return the tax amount.
 	 */
 	protected String getTaxAmount()
 	{
 		if (taxTotal > 0)
 		{
 			return parseStr(taxTotal);
 		}
 
 		return "0.00";
 	}
 
 	/**
 	 * Returns the tip amount after calculation.
 	 *
 	 * @return The tip amount.
 	 */
 	protected String getTipAmount()
 	{
 		return parseStr(tipTotal);
 	}
 
 	/**
 	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
 	 */
 	protected void destroyApp(boolean b)
 					   throws MIDletStateChangeException
 	{
 		notifyDestroyed();
 	}
 
 	/**
 	 * @see javax.microedition.midlet.MIDlet#pauseApp()
 	 */
 	protected void pauseApp()
 	{
 		;
 	}
 
 	/**
 	 * @see javax.microedition.midlet.MIDlet#startApp()
 	 */
 	protected void startApp()
 					 throws MIDletStateChangeException
 	{
 		display = Display.getDisplay(this);
 		display.setCurrent(mainScreen);
 	}
 
 	// Calulates the tip.
 	private void calcTip()
 	{
 		taxTotal = parseInt(taxAmount);
 		subTotal = parseInt(totalAmount) - taxTotal;
 		tipTotal = (subTotal * Integer.parseInt(tipRate)) / 100;
 		billTotal = subTotal + tipTotal + taxTotal;
 
 		if ((billTotal % 100) < 50)
 		{
 			while ((billTotal % 100) != 0)
 			{
 				tipTotal--;
 				billTotal = subTotal + tipTotal + taxTotal;
 			}
 		}
 		else
 		{
 			while ((billTotal % 100) != 0)
 			{
 				tipTotal++;
 				billTotal = subTotal + tipTotal + taxTotal;
 			}
 		}
 	}
 
 	// Displays an error dialog.
 	private void error(String msg, Displayable d)
 	{
 		/* final */ Alert error = new Alert("Error", msg, null, AlertType.ERROR);
 		error.setTimeout(Alert.FOREVER);
 		display.setCurrent(error, d);
 	}
 
 	// Exits the application.
 	private void exit()
 	{
 		try
 		{
 			destroyApp(false);
 		}
 		catch (MIDletStateChangeException e)
 		{
 			; // Do nothing 
 		}
 	}
 
 	// Parses a given string to an int, the decimal point is removed.
 	private int parseInt(String s)
 	{
 		/* final */ int dec = s.lastIndexOf('.');
 		int len = s.length();
 
 		if (dec == -1)
 		{
 			return Integer.parseInt(s + "00");
 		}
 		else if (dec == (len - 1))
 		{
 			return Integer.parseInt(s.substring(0, dec) + "00");
 		}
 		else if (dec == (len - 2))
 		{
 			return Integer.parseInt(s.substring(0, dec) + s.substring(dec + 1) + '0');
 		}
 		else
 		{
 			return Integer.parseInt(s.substring(0, dec) + s.substring(dec + 1, dec + 3));
 		}
 	}
 
 	// Parse the given into to a string, the decimal point is added.
 	private String parseStr(int i)
 	{
 		/* final */ String s = String.valueOf(i);
 
 		return s.substring(0, s.length() - 2) + '.' + s.substring(s.length() - 2);
 	}
 }
