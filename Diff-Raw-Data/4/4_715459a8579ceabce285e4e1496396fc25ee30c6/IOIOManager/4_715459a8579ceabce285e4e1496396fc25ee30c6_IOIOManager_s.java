 // Copyright 2011-2012, Art Hare
 // This file is part of WifiLapper.
 
 //WifiLapper is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 
 //WifiLapper is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 
 //You should have received a copy of the GNU General Public License
 //along with WifiLapper.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.artsoft.wifilapper;
 
 import java.io.Closeable;
 
 import android.content.SharedPreferences;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 import com.artsoft.wifilapper.Utility.MultiStateObject.STATE;
 
 import ioio.lib.api.*;
 import ioio.lib.api.IOIO.VersionType;
 import ioio.lib.api.PulseInput.ClockRate;
 import ioio.lib.api.PulseInput.PulseMode;
 import ioio.lib.api.TwiMaster.Rate;
 import ioio.lib.api.Uart.Parity;
 import ioio.lib.api.Uart.StopBits;
 import ioio.lib.api.exception.ConnectionLostException;
 import ioio.lib.api.exception.IncompatibilityException;
 import ioio.lib.api.exception.OutOfResourceException;
 
 public class IOIOManager 
 {
 	public static class PinParams implements Parcelable
 	{
 		public static final int FILTERTYPE_NONE = 0;
 		public static final int FILTERTYPE_WHEELSPEED = 1;
 		public static final int FILTERTYPE_WHEELSPEEDRPM = 2;
 		public static final int FILTERTYPE_944COOLANT = 3;
 		public static final int FILTERTYPE_944OILPRES = 4;
 		public static final int FILTERTYPE_944FUELLEVEL = 5;
 		public static final int FILTERTYPE_944ALTERNATOR = 6;
 		public static final int FILTERTYPE_TACHOMETER = 7;
 		
 		public PinParams(int iPin, int iPeriod, int iFilterType, double dParam1, double dParam2, int iCustomType)
 		{
 			this.iPin = iPin;
 			this.iPeriod = iPeriod;
 			this.iFilterType = iFilterType;
 			this.dParam1 = dParam1;
 			this.dParam2 = dParam2;
 			this.iCustomType = iCustomType;
 		}
 		public PinParams(Parcel in)
 		{
 			iPin = in.readInt();
 			iPeriod = in.readInt();
 			iFilterType = in.readInt();
 			dParam1 = in.readDouble();
 			dParam2 = in.readDouble();
 			iCustomType = in.readInt();
 		}
 		static float DoFilter(int iType, double dParam1, double dParam2, float flValue)
 		{
 			switch(iType)
 			{
 			case PinParams.FILTERTYPE_NONE: return flValue;
 			case PinParams.FILTERTYPE_WHEELSPEED: return (float)(dParam2 * (flValue / dParam1)); // dParam1 = # of pulses per rev.  dParam2 = wheel diameter
 			case PinParams.FILTERTYPE_WHEELSPEEDRPM: return (float)((flValue / dParam1)); // dParam1 = # of pulses per rev.  flValue = # of pulses detected
 			case PinParams.FILTERTYPE_944COOLANT: return 203.3f - 68.6f*flValue;
			case PinParams.FILTERTYPE_944OILPRES: return -4.63f * 3.00f*flValue + 0.747f*(float)Math.pow(flValue-2.375f,2);
			case PinParams.FILTERTYPE_944FUELLEVEL: return -0.55f + 0.55f*flValue - 0.157f*(float)Math.pow(flValue-2.07f, 2);
 			case PinParams.FILTERTYPE_944ALTERNATOR: return 5.19f*flValue;
 			case PinParams.FILTERTYPE_TACHOMETER: return (float)(flValue / dParam1);
 			default: return flValue;
 			}
 		}
 		static String BuildDesc(int iType, double dParam1, double dParam2, boolean fShort)
 		{
 			switch(iType)
 			{
 			case PinParams.FILTERTYPE_NONE:
 				return "None";
 			case PinParams.FILTERTYPE_WHEELSPEED:
 				return fShort ? "Wheelspeed" : "Wheelspeed (" + (int)(dParam1+0.5) + " pulses per rev, " + Utility.FormatFloat((float)dParam2,0) + "mm)";
 			case PinParams.FILTERTYPE_WHEELSPEEDRPM:
 				return fShort ? "Wheelspeed-RPM" : "Wheelspeed-RPM (" + (int)(dParam1+0.5) + " pulses per rev)";
 			case PinParams.FILTERTYPE_944COOLANT:
 				return "944 Coolant";
 			case PinParams.FILTERTYPE_944OILPRES:
 				return "944 Oil pressure";
 			case PinParams.FILTERTYPE_944FUELLEVEL:
 				return "944 fuel level";
 			case PinParams.FILTERTYPE_944ALTERNATOR:
 				return "944 alternator voltage";
 			case PinParams.FILTERTYPE_TACHOMETER:
 				return "Tachometer";
 				
 			}
 			return "";
 		}
 		
 		double dParam1;
 		double dParam2; // params.  Interpretation depends on this guy's filter type
 		int iFilterType;
 		int iPin;
 		int iPeriod; // milliseconds between samples
 		int iCustomType; // from the constants defined in LapAccumulator.DataChannel
 		@Override
 		public int describeContents() 
 		{
 			return 0;
 		}
 		@Override
 		public void writeToParcel(Parcel arg0, int arg1) 
 		{
 			arg0.writeInt(iPin);
 			arg0.writeInt(iPeriod);
 			arg0.writeInt(iFilterType);
 			arg0.writeDouble(dParam1);
 			arg0.writeDouble(dParam2);
 			arg0.writeInt(iCustomType);
 		}
 		public static final Parcelable.Creator<PinParams> CREATOR
 		        = new Parcelable.Creator<PinParams>() {
 		    public PinParams createFromParcel(Parcel in) {
 		        return new PinParams(in);
 		    }
 		
 		    public PinParams[] newArray(int size) {
 		        return new PinParams[size];
 		    }
 		};
 	}
 	private Utility.MultiStateObject m_pStateMan;
 	private PinParams m_rgAnalPins[];
 	private PinParams m_rgPulsePins[];
 	private int m_iButtonPin;
 	
 	private IOIOListener m_listener;
 	
 	private QueryThread m_thd;
 	// rdAnalIn - the i'th element indicates we want to use the i'th analog input
 	public IOIOManager(IOIOListener listener, Utility.MultiStateObject pStateMan, PinParams rgAnalPins[], PinParams rgPulsePins[], int iButtonPin)
 	{
 		m_pStateMan = pStateMan;
 		
 		m_iButtonPin = iButtonPin;
 		m_rgAnalPins = new PinParams[48];
 		m_rgPulsePins = new PinParams[48];
 		
 		for(int x = 0;x < 48; x++)
 		{
 			m_rgAnalPins[x] = null;
 			m_rgPulsePins[x] = null;
 		}
 		for(int x = 0; x < rgAnalPins.length; x++)
 		{
 			m_rgAnalPins[rgAnalPins[x].iPin] = rgAnalPins[x];
 		}
 		for(int x = 0;x < rgPulsePins.length; x++)
 		{
 			m_rgPulsePins[rgPulsePins[x].iPin] = rgPulsePins[x];
 		}
 		
 		m_listener = listener;
 		
 		// do nothing.  All initialization will occur on the other thread to avoid blocking the UI
 		m_thd = new QueryThread();
 		m_thd.start();
 	}
 	
 	public void Shutdown()
 	{
 		m_thd.Shutdown();
 	}
 	public interface IOIOListener
 	{
 		public abstract void NotifyIOIOValue(int pin, int iCustomType, float flValue);
 		public abstract void NotifyIOIOButton();
 	}
 	
 	private class QueryThread extends Thread implements Runnable
 	{
 		private boolean m_fContinue = true;
 		public QueryThread()
 		{
 			
 		}
 		
 		public void Shutdown()
 		{
 			m_fContinue = false;
 		}
 		
 		@Override
 		public void run()
 		{
 			m_pStateMan.SetState(IOIOManager.class, STATE.TROUBLE_GOOD, "Loading IOIO");
 
 			PulseInput pulseIn[] = new PulseInput[48];
 			AnalogInput analIn[] = new AnalogInput[48];
 			do
 			{
 
 				IOIO pIOIO = null;
 				try // for catching most IOIO errors
 				{
 					Thread.sleep(1000);
 					
 					pIOIO = IOIOFactory.create();
 					//pIOIO = new FakeIOIO();
 					if(pIOIO == null) 
 					{
 						m_pStateMan.SetState(IOIOManager.class, STATE.TROUBLE_BAD, "Could not connect to IOIO");
 						continue;
 					}
 					m_pStateMan.SetState(IOIOManager.class, STATE.TROUBLE_GOOD, "Waiting for IOIO connection");
 					pIOIO.waitForConnect();
 					m_pStateMan.SetState(IOIOManager.class, STATE.TROUBLE_GOOD, "Connected to IOIO, loading pins");
 					for(int x = 1;x < 47; x++)
 					{
 						if(m_rgAnalPins[x] != null)
 						{
 							analIn[x] = pIOIO.openAnalogInput(x);
 						}
 					}
 					for(int x = 1; x < 47; x++)
 					{
 						if(m_rgPulsePins[x] != null)
 						{
 							DigitalInput.Spec spec = new DigitalInput.Spec(x);
 							spec.mode = DigitalInput.Spec.Mode.FLOATING;
 							
 							pulseIn[x] = pIOIO.openPulseInput(spec, ClockRate.RATE_62KHz, PulseMode.FREQ, false);
 						}
 					}
 					
 					DigitalInput buttonPin = null;
 					if(m_iButtonPin >= 1 && m_iButtonPin < 30)
 					{
 						buttonPin = pIOIO.openDigitalInput(m_iButtonPin);
 					}
 					
 					// everything is loaded, so let's continue
 					m_pStateMan.SetState(IOIOManager.class, STATE.ON, "IOIO Loaded");
 					
 					final int MS_PER_LOOP = 100;
 					
 					int rgSpinsUntilQuery[] = new int[analIn.length];
 					int rgResetSpinsUntilQuery[] = new int[analIn.length];
 					for(int x = 0;x < m_rgPulsePins.length; x++)
 					{
 						if(m_rgPulsePins[x] != null)
 						{
 							rgResetSpinsUntilQuery[x] = m_rgPulsePins[x].iPeriod / MS_PER_LOOP;
 						}
 					}
 					for(int x = 0;x < m_rgAnalPins.length; x++)
 					{
 						if(m_rgAnalPins[x] != null)
 						{
 							rgResetSpinsUntilQuery[x] = m_rgAnalPins[x].iPeriod / MS_PER_LOOP;
 						}
 					}
 					
 					// the loop runs at 10hz.
 					// if rgSpinsUntilQuery[x] == 0, then we query the pin and reset the count.  Else, we decrement the counter
 					// So a 0.1hz pin will always get reset to 100, 1hz gets 10, and 10hz gets 1
 					
 					boolean fLastButton = buttonPin != null ? buttonPin.read() : false;
 					while(m_fContinue)
 					{
 						for(int x = 0; x < analIn.length; x++)
 						{
 							if(analIn[x] != null)
 							{
 								if(rgSpinsUntilQuery[x] == 0)
 								{
 									float flValue = analIn[x].getVoltage();
 									flValue = PinParams.DoFilter(m_rgAnalPins[x].iFilterType, m_rgAnalPins[x].dParam1, m_rgAnalPins[x].dParam2, flValue);
 									m_listener.NotifyIOIOValue(x, m_rgAnalPins[x].iCustomType, flValue);
 									rgSpinsUntilQuery[x] = rgResetSpinsUntilQuery[x];
 								}
 								else
 								{
 									rgSpinsUntilQuery[x]--; // subtract the count
 								}
 							}
 							if(pulseIn[x] != null)
 							{
 								if(rgSpinsUntilQuery[x] == 0)
 								{
 									float flValue = pulseIn[x].getFrequency();
 									flValue = PinParams.DoFilter(m_rgPulsePins[x].iFilterType, m_rgPulsePins[x].dParam1, m_rgPulsePins[x].dParam2, flValue);
 									m_listener.NotifyIOIOValue(x, m_rgPulsePins[x].iCustomType, flValue);
 									rgSpinsUntilQuery[x] = rgResetSpinsUntilQuery[x];
 								}
 								else
 								{
 									rgSpinsUntilQuery[x]--;
 								}
 							}
 						}
 						
 						if(buttonPin != null)
 						{
 							boolean fValue = buttonPin.read();
 							if(fValue != fLastButton && fValue)
 							{
 								m_listener.NotifyIOIOButton();
 							}
 							fLastButton = fValue;
 						}
 						
 						Thread.sleep(MS_PER_LOOP); // max 10hz sampling
 					}
 				}
 				catch(ConnectionLostException e)
 				{
 					m_pStateMan.SetState(IOIOManager.class, STATE.TROUBLE_BAD, "Failed to connect to IOIO.  This can happen if you fried your IOIO like I did...  Retrying...");
 				}
 				catch (InterruptedException e) 
 				{
 					m_pStateMan.SetState(IOIOManager.class, STATE.TROUBLE_BAD, "Unexpected read failure.  Retrying...");
 				} catch (IncompatibilityException e) 
 				{
 					m_pStateMan.SetState(IOIOManager.class, STATE.TROUBLE_BAD, "Incompatible IOIO version.  Yell at WifiLapper's author to fix it");
 				}
 				
 				try
 				{
 					for(int x = 0;x < analIn.length; x++) {if(analIn[x] != null) analIn[x].close();}
 					for(int x = 0;x < pulseIn.length; x++) {if(pulseIn[x] != null) pulseIn[x].close();}
 					if(pIOIO != null)
 					{
 						pIOIO.disconnect();
 						pIOIO = null;
 					}
 				}
 				catch(Exception e)
 				{
 					
 				}
 				
 			} while(true);
 		}
 	}
 	
 }
 
 class FakeIOIO implements IOIO
 {
 	public void waitForConnect() throws ConnectionLostException,
 			IncompatibilityException
 	{
 		try
 		{
 			Thread.sleep(500);
 		}
 		catch(InterruptedException e)
 		{
 			
 		}
 		return;
 	}
 	
 	public void disconnect()
 	{
 		return;
 	}
 	public void waitForDisconnect() throws InterruptedException
 	{
 		return;
 	}
 	public void softReset() throws ConnectionLostException
 	{
 		return;
 	}
 	public void hardReset() throws ConnectionLostException
 	{
 		return;
 	}
 	public String getImplVersion(VersionType v) throws ConnectionLostException
 	{
 		return "";
 	}
 	public DigitalInput openDigitalInput(DigitalInput.Spec spec)
 			throws ConnectionLostException
 	{
 		return null;
 	}
 	public DigitalInput openDigitalInput(int pin)
 			throws ConnectionLostException
 	{
 		return null;
 	}
 	public DigitalInput openDigitalInput(int pin, DigitalInput.Spec.Mode mode)
 			throws ConnectionLostException
 	{
 		return null;
 	}
 	public DigitalOutput openDigitalOutput(DigitalOutput.Spec spec,
 			boolean startValue) throws ConnectionLostException
 	{
 		return null;
 	}
 	public DigitalOutput openDigitalOutput(int pin,
 			DigitalOutput.Spec.Mode mode, boolean startValue)throws ConnectionLostException
 	{
 		return null;
 	}
 			
 	public DigitalOutput openDigitalOutput(int pin, boolean startValue)
 			throws ConnectionLostException
 	{
 		return null;
 	}
 	public DigitalOutput openDigitalOutput(int pin)
 			throws ConnectionLostException
 	{
 		return null;
 	}
 	public AnalogInput openAnalogInput(int pin) throws ConnectionLostException
 	{
 		return new FakeAnalogInput(pin);
 	}
 	public PwmOutput openPwmOutput(DigitalOutput.Spec spec, int freqHz)
 			throws ConnectionLostException
 	{
 		return null;
 	}
 	public PwmOutput openPwmOutput(int pin, int freqHz)
 			throws ConnectionLostException
 	{
 		return null;
 	}
 	public PulseInput openPulseInput(DigitalInput.Spec spec,
 			PulseInput.ClockRate rate, PulseInput.PulseMode mode,
 			boolean doublePrecision) throws ConnectionLostException
 	{
 		return null;
 	}
 	public PulseInput openPulseInput(int pin, PulseMode mode)
 			throws ConnectionLostException
 	{
 		return new FakePulseInput(pin);
 	}
 	public Uart openUart(DigitalInput.Spec rx, DigitalOutput.Spec tx, int baud,
 			Parity parity, StopBits stopbits) throws ConnectionLostException
 	{
 		return null;
 	}
 	public Uart openUart(int rx, int tx, int baud, Parity parity,
 			StopBits stopbits) throws ConnectionLostException
 	{
 		return null;
 	}
 	public SpiMaster openSpiMaster(DigitalInput.Spec miso,
 			DigitalOutput.Spec mosi, DigitalOutput.Spec clk,
 			DigitalOutput.Spec[] slaveSelect, SpiMaster.Config config)throws ConnectionLostException
 	{
 		return null;
 	}
 			
 	public SpiMaster openSpiMaster(int miso, int mosi, int clk,
 			int[] slaveSelect, SpiMaster.Rate rate)
 			throws ConnectionLostException
 	{
 		return null;
 	}
 
 	public SpiMaster openSpiMaster(int miso, int mosi, int clk,
 			int slaveSelect, SpiMaster.Rate rate)
 			throws ConnectionLostException
 	{
 		return null;
 	}
 	public TwiMaster openTwiMaster(int twiNum, Rate rate, boolean smbus)
 			throws ConnectionLostException
 	{
 		return null;
 	}
 	public IcspMaster openIcspMaster() throws ConnectionLostException
 	{
 		return null;
 	}
 	
 	private class FakeAnalogInput implements AnalogInput
 	{
 		private int pin;
 		public FakeAnalogInput(int pin)
 		{
 			this.pin = pin;
 		}
 		@Override
 		public void close() {}
 
 		@Override
 		public float getReference() {return 0;}
 
 		@Override
 		public float getVoltage() throws InterruptedException,ConnectionLostException 
 		{
 			double dTime = (System.currentTimeMillis()%10000) / 10000.0;
 			return (float)(Math.sin(dTime * pin) * 2 + 2.5);
 		}
 
 		@Override
 		public float read() throws InterruptedException, ConnectionLostException 
 		{
 			double dTime = (System.currentTimeMillis()%10000) / 10000.0;
 			return (float)(Math.sin(dTime * pin) * 2 + 2.5);
 		}
 	}
 	private class FakePulseInput implements PulseInput
 	{
 		private int pin;
 		public FakePulseInput(int pin)
 		{
 			this.pin = pin;
 		}
 		@Override
 		public void close() {}
 		@Override
 		public float getDuration() throws InterruptedException,ConnectionLostException 
 		{
 			return 0;
 		}
 		@Override
 		public float getFrequency() throws InterruptedException,ConnectionLostException 
 		{
 			return (float)(((System.currentTimeMillis()%10000) / 10000.0) * pin);
 		}
 		@Override
 		public float waitPulseGetDuration() throws InterruptedException,ConnectionLostException 
 		{
 			return 0;
 		}
 	}
 }
