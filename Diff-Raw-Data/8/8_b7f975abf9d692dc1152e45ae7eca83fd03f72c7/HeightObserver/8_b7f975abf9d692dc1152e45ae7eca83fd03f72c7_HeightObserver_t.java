 package rocketview;
 
 import cansocket.*;
 import stripchart.*;
 
 import java.util.*;
 import javax.swing.*;
 
 class HeightObserver extends JLabel implements Observer
 {
 	protected int pressHeight = Integer.MAX_VALUE;
 	protected int gpsHeight = Integer.MAX_VALUE;
 
 	public void update(Observable o, Object arg)
 	{
 		CanMessage msg = (CanMessage) arg;
 		switch(msg.getId())
 		{
 		case CanBusIDs.GPSHeight:
			gpsHeight = msg.getData16(0) / 100;
 			break;
 		case CanBusIDs.PressValue:
 			pressHeight = toHeight(msg.getData16(0));
 			break;
 		default:
 			return;
 		}
 		StringBuffer buf = new StringBuffer();
 		if(gpsHeight != Integer.MAX_VALUE)
			buf.append(" gps:").append(gpsHeight).append('m');
 		if(pressHeight != Integer.MAX_VALUE)
			buf.append(" press:").append(pressHeight).append('m');
 		setText(buf.toString());
 	}
 
 	protected int toHeight(short pressure)
 	{
 		return (int) (44331.514 - Math.pow(18411.8956 * pressure, 0.1902632));
 	}
 }
