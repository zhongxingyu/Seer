 package widgets;
 
 import cansocket.*;
 
 import java.lang.reflect.*;
 
 public class NodeEnableLabel extends NodeStateLabel
 {
 	protected final int id;
	protected static final int length = 1<<13;	// 8192: must be a power of two
 	protected final int[] times = new int[length];
 
 	protected int next_out, next_in;
 	protected int last_time;
 
 	public NodeEnableLabel(String name, int bit)
 		throws NoSuchFieldException, IllegalAccessException
 	{
 		super(name, bit);
 		id = CanBusIDs.class.getField(name).getInt(null);
 		update(0);
 	}
 
	private int wrap(int index)
	{
		return index & (length-1);		// (in % length)
	}

 	public void message(CanMessage msg)
 	{
 		super.message(msg);
 
 		int now = msg.getTimestamp();
 		// Ignore FC or ground-generated messages.
 		if(now == 0)
 			return;
 
		// If time has gone backwards or next_in overflowed, flush and start over.
		if(now < last_time || next_in < 0)
			next_out = next_in = 0;
 
 		// If this is *our* message, note another instance.
 		if(msg.getId() == id)
			times[wrap(next_in++)] = now;
 
 		// Repaint every quarter second.
 		if(now - last_time >= 25 || now < last_time)
 		{
 			update(now);
 			last_time = now;
 		}
 	}
 
 	private void update(int now)
 	{
		// average msgs/time over at most the last three seconds
 		final int tenths;
		while(next_out != next_in && now - times[wrap(next_out)] >= 300)
 			++next_out;
 
		if (now > times[wrap(next_out)])
		{
			tenths = (int) (1000f * (next_in - next_out) / (now - times[wrap(next_out)]));
			setDetail("" + (tenths / 10f) + "/sec");
		}
 		else
			setDetail("0/sec");
 	}
 }
