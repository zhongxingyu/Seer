 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.io.IOException;
 
 import android.content.Context;
 
 /**
  * Abstract base class for all ECU implementations
  * 
  * @author dgs
  * 
  */
 public abstract class Megasquirt
 {
 	
 	public static final String NEW_DATA = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.NEW_DATA";
 
 	public static final String CONNECTED = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.CONNECTED";
 	public static final String DISCONNECTED = "uk.org.smithfamily.mslogger.ecuDef.Megasquirt.DISCONNECTED";
 
 	protected Context context;
 
 	public abstract String getSignature();
 
 	public abstract byte[] getOchCommand();
 
 	public abstract byte[] getSigCommand();
 
 	public abstract void loadConstants(boolean simulated) throws IOException;
 
 	public abstract void calculate(byte[] ochBuffer) throws IOException;
 
 	public abstract String getLogHeader();
 
 	public abstract String getLogRow();
 
 	public abstract int getBlockSize();
 
 	public abstract int getSigSize();
 
 	public abstract int getPageActivationDelay();
 
 	public abstract int getInterWriteDelay();
 
 	public abstract int getCurrentTPS();
 
 	public abstract void initGauges();
 
 	public abstract String[] defaultGauges();
 
     public abstract void refreshFlags();
 
     public abstract boolean isCRC32Protocol();
 
     public abstract DataPacket getDataPacket();
 	protected byte[] ochBuffer;
 
 	/**
 	 * Shortcut function to access data tables. Makes the INI->Java translation
 	 * a little simpler
 	 * 
 	 * @param i1
 	 *            index into table
 	 * @param name
 	 *            table name
 	 * @return value from table
 	 */
 	protected int table(int i1, String name)
 	{
 		return 0;
 	}
 
 	protected int table(double d, String name)
 	{
 		return 0;
 	}
 
 	/**
 	 * Temperature unit conversion function
 	 * 
 	 * @param t
 	 *            temp in F
 	 * @return temp in C if CELSIUS is set, in F otherwise
 	 */
 	protected double tempCvt(int t)
 	{
 		if (isSet("CELSIUS"))
 		{
 			return (t - 32.0) * 5.0 / 9.0;
 		} else
 		{
 			return t;
 		}
 	}
 
 	/**
 	 * 
 	 * @param c
 	 */
 	public Megasquirt(Context c)
 	{
 		this.context = c;
 	}
 
 	/**
 	 * How long have we been running?
 	 * 
 	 * @return
 	 */
 	protected int timeNow()
 	{
 		return 0;
 	}
 
 	/**
 	 * Use reflection to extract a named value out of the subclass
 	 * 
 	 * @param channel
 	 * @return
 	 */
 	public double getValue(String channel)
 	{
 
 		double value = 0;
 		return value;
 	}
 
 	/**
 	 * Take a wild stab at what this does.
 	 * 
 	 * @param v
 	 * @return
 	 */
 	protected double round(double v)
 	{
 		return Math.floor(v * 100 + .5) / 100;
 	}
 
 	/**
 	 * Returns if a flag has been set in the application
 	 * 
 	 * @param name
 	 * @return
 	 */
 	protected boolean isSet(String name)
 	{
 		return false;
 	}
 
 	public String getTrueSignature()
 	{
 		return "DUMMY";
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isRunning()
 	{
 		return false;
 	}
 
 	/**
 	 * helper method for subclasses
 	 * 
 	 * @param pageNo
 	 * @param pageOffset
 	 * @param pageSize
 	 * @param select
 	 * @param read
 	 * @return
 	 */
	protected byte[] loadPage(int pageNo, int pageOffset, int pageSize, byte[] select, byte[] read) {
 		byte[] buffer = new byte[pageSize];
 		return buffer;
 	}
 
 }
