 package rocketview;
 
 import cansocket.*;
 import widgets.*;
 
 import java.text.*;
 import javax.swing.*;
 import javax.swing.border.*;
 
 /*  layout
  Recovery Node ([-|Safe|2m Armed|Armed])
  
  Power:   [-|External|HAP]
  Charge:  [-|Fast|Trickle|off]
  Battery: [-|n.n]V [-|nnnn]#A
  
  Last DTMF: [-|DTMF tone 0..9,#,* (tone number, tone sequence)]
  
  Timer 1: [-|nnn]s   Pyro 1: [-|Fired(nx)]
  Timer 2: [-|nnn]s   Pyro 2: [-|Fired(nx)]
  Timer 3: [-|nnn]s   Pyro 3: [-|Fired(nx)]
  Timer 4: [-|nnn]s   Pyro 4: [-|Fired(nx)] 
  */
 
 public class RecObserver extends JPanel implements CanObserver
 {
 	protected final DecimalFormat fmt = new DecimalFormat("0.0");
 
 	protected final NameDetailLabel powerLabel = new NameDetailLabel("Power", "-");
 	protected final NameDetailLabel chargeLabel = new NameDetailLabel("Charge", "-");
 	protected final NameDetailLabel batteryLabel = new NameDetailLabel("Battery", "-");
 	protected final NameDetailLabel dtmfLabel = new NameDetailLabel("Last DTMF", "-");
 	protected final NameDetailLabel timerLabel[] = new NameDetailLabel[4];
 
 	protected static final String dtmfChars = "1234567890*#";
 	protected String dtmfTones = "";
 	protected int timer[] = {
 		0,0,0,0
 	};
 	protected int pyro[] = {
 		0,0,0,0
 	};
 
 	public RecObserver(CanDispatch dispatch)
 	{
 		setLayout(new GridBoxLayout());
 		dispatch.add(this);
 
 		add(StateGrid.getLabel("REC"));
 		add(powerLabel);
 		add(chargeLabel);
 		add(batteryLabel);
 		add(dtmfLabel);
 		for (int i=0; i<4; i++)
 		{
			timerLabel[i] = new NameDetailLabel("Timer " + i, "-");
 			add(timerLabel[i]);
 		}
 	}
 
 	public void message(CanMessage msg)
 	{
 		int i;
 		switch(msg.getId())
 		{
 			case CanBusIDs.REC_REPORT_BATTERY:
 				powerLabel.setDetail(powerText(msg.getData8(5)));
 				chargeLabel.setDetail(chargeText(msg.getData8(0)));
 				batteryLabel.setDetail(batteryText(msg));
 				break;
 			case CanBusIDs.REC_REPORT_DTMF:
 				dtmfLabel.setDetail(dtmfText(msg));
 				break;
 			case CanBusIDs.REC_REPORT_TIMER:
 				i = msg.getData8(2) - 1;
 				timer[i] = msg.getData16(0);
 				setPyro(i);
 				break;
 			case CanBusIDs.REC_REPORT_PYRO:
 				if (msg.getData8(1) > 0)
 				{
 					i = msg.getData8(0) - 1;
 					pyro[i]++;
 					setPyro(i);
 				}
 				break;
 			default:
 				return;
 		}
 	}
 	
 	private String stateText(int code)
 	{
 		switch(code)
 		{
 			case 0x30: return "(Safe)";
 			case 0x33: return "(2m Armed)";
 			case 0x3f: return "(Armed)";
 		}
 		return "Unknown: " + code;
 	}
 	private String powerText(int code)
 	{
 		switch(code)
 		{
 			case 0: return "HAP";
 			case 1: return "External";
 		}
 		return "Unknown: " + code;
 	}
 	private String chargeText(int code)
 	{
 		switch(code)
 		{
 			case 0: return "Off";
 			case 1: return "Trickle";
 			case 2: return "Fast";
 		}
 		return "Unknown: " + code;
 	}
 	private String batteryText(CanMessage msg)
 	{
 		// note: bad data alignment defeats our getData16 accessor
 		double V = ((msg.getData8(1)<<8) + msg.getData8(2))*5/1024.0;
 		int    I = ((msg.getData8(3)<<8) + msg.getData8(4));
 		return fmt.format(V) + "V  " + I + "#A";
 	}
 	private String dtmfText(CanMessage msg)
 	{
 		int tone    = msg.getData8(0);
 		int toneNum = msg.getData8(1);
 		int toneSeq = msg.getData8(2);
 
 		if (tone == 12)		// commands always start with '#'
 			dtmfTones = "#";
 		else
 			dtmfTones += dtmfChars.charAt(tone-1);
 		return dtmfTones + " (" + toneNum + "," + toneSeq + ")";
 	}
 	private void setPyro(int i)
 	{
		timerLabel[i].setDetail((timer[i] / 10f) + "s  Pyro " + i + " fired(" + pyro[i] + ")");
 	}
 }
