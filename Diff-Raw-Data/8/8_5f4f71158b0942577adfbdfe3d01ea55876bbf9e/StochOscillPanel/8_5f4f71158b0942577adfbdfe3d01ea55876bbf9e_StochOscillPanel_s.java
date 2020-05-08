 package edu.brown.cs32.atian.crassus.gui.indicatorwindows;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.undo.UndoManager;
 
 import edu.brown.cs32.atian.crassus.indicators.Indicator;
 import edu.brown.cs32.atian.crassus.indicators.StochasticOscillator;
 import edu.brown.cs32.atian.crassus.backend.Stock;
 import edu.brown.cs32.atian.crassus.backend.StockFreqType;
 import edu.brown.cs32.atian.crassus.gui.WindowCloseListener;
 
 public class StochOscillPanel extends JPanel {
 
 
 	private static final long serialVersionUID = 12154087L;
 	
 	private WindowCloseListener closeListener;
 	private Stock stock;
 	private JDialog parent;
 	private JTextField period;
 	private JLabel yearlyGain = new JLabel("Yearly: (Not tested)");
 	private JLabel monthlyGain = new JLabel("Monthly: (Not tested)");
 	private JLabel weeklyGain = new JLabel("Weekly: (Not tested)");
 	private String periodtt = "<html>The number of days to use when calculating the high-low range.</html>";
 
 	public StochOscillPanel(WindowCloseListener closeListener, JDialog parent, Stock stock)
 	{
 		this.closeListener = closeListener;
 		this.parent = parent;
 		this.stock = stock;
 		
 		final UndoManager undoM = new UndoManager();
 		
 		NumberVerifier inputValidator = new NumberVerifier(this);
 		
 		//top panel
 		JLabel periodLabel = new JLabel("Period:");
 		periodLabel.setToolTipText(periodtt);
 		
 		period = new CrassusTextField("20", periodtt, inputValidator, undoM);
 		
 		JPanel periodInput = new JPanel();
 		periodInput.setLayout(new FlowLayout());
 		periodInput.add(periodLabel);
 		periodInput.add(period);
 		
 		JPanel parameters = new JPanel();
 		parameters.setLayout(new BoxLayout(parameters, BoxLayout.Y_AXIS));
 		parameters.add(periodInput);
 		
 		ExpectedGainsPanel ep = new ExpectedGainsPanel(weeklyGain, monthlyGain, yearlyGain);
 		
 		//middle panel
 		JPanel buttons = new JPanel();
 		buttons.setLayout(new FlowLayout());
 		JButton ok = new JButton("Ok");
 		ok.addActionListener(new OkListener());
 		JButton test = new JButton("Test");
 		test.addActionListener(new TestListener());
 		JButton cancel = new JButton("Cancel");
 		cancel.addActionListener(new CancelListener(parent));
 		buttons.add(ok);
 		buttons.add(test);
 		buttons.add(cancel);
 		
 		
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		this.add(parameters);
 		this.add(buttons);
 		this.add(ep);
 
 		
 	}
 	
 	public void setPeriod(String period)
 	{
 		this.period.setText(period);
 	}
 
 	class OkListener extends AbstractOkListener
 	{
 
 		public OkListener() 
 		{
 			super(parent);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) 
 		{
 			String periodArg = period.getText();
 			
 			if(periodArg == null)
 			{
 				showErrorDialog("You must enter a value.");
 			}
 			else
 			{
 				try
 				{
 					Indicator ind = new StochasticOscillator(stock.getStockPriceData(stock.getCurrFreq()), Integer.parseInt(periodArg));
 					parent.dispose();
 					closeListener.windowClosedWithEvent(ind);
 				}
 				catch(NumberFormatException nfe)
 				{
 					showErrorDialog();
 				}
 				catch(IllegalArgumentException iae)
 				{
 					showErrorDialog(iae.getMessage());
 				}
 			}
 			
 		}
 		
 		
 	}
 	
 	class TestListener extends AbstractTestListener
 	{
 		public TestListener()
 		{
 			super(parent, yearlyGain, monthlyGain, weeklyGain);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) 
 		{
 			String periodArg = period.getText();
 			
 			if(periodArg == null)
 			{
 				showErrorDialog("You must enter a value.");
 			}
 			else
 			{
 				try
 				{
 					Indicator indDaily = new StochasticOscillator(stock.getStockPriceData(StockFreqType.DAILY), Integer.parseInt(periodArg));
 					
 					
 					Indicator indWeekly = new StochasticOscillator(stock.getStockPriceData(StockFreqType.WEEKLY), Integer.parseInt(periodArg));
 					
 					
 					Indicator indMonthly = new StochasticOscillator(stock.getStockPriceData(StockFreqType.MONTHLY), Integer.parseInt(periodArg));
 					
 					
 					test(indDaily, indWeekly, indMonthly);
 				}
 				catch(NumberFormatException nfe)
 				{
 					showErrorDialog();
 				}
 				catch(IllegalArgumentException iae)
 				{
 					showErrorDialog(iae.getMessage());
 				}
 			}
 			
 		}
 		
 	}
 	
 	public String toString()
 	{
 		return "Stochastic Oscillator Event";
 	}
 
 }
