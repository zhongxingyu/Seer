 package actions;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import panels.MasterPanel;
 import panels.ViewPanel;
 import visualize.DateRange;
 
 public class RedrawAction implements ActionListener{
 
 	MasterPanel panel;
 	DateRange view;
 	
 	public RedrawAction(MasterPanel m) 
 	{
 		panel = m;
 	}
 
 	public void actionPerformed(ActionEvent e) 
 	{
 
 		
 			ViewPanel pane = panel.prefPanel;
 			DateRange dr = panel.view[0];
 			
 			if(panel.numCharts==2 && panel.sidePanel==1)
 			{
 				pane = panel.prefPanel1;
 				dr = panel.view[1];
 			}
 			else if(panel.numCharts==2 && panel.sidePanel==2)
 			{
 				pane = panel.prefPanel2;
 				dr = panel.view[2];
 			}
 			
 			// Filter years down...
 			if(pane.yearList.getSelectedIndex() != 0)
 			{
 				int[] selected = pane.yearList.getSelectedIndices();
 				boolean[] newYrs = new boolean[dr.years.length];
 				
 				for(int i = 0; i < selected.length; i++)
 				{
 					newYrs[selected[i]-1] = true;
 				}
 				
 				dr.years = newYrs;
 			}
 			else
 			{
 				for(int i = 0; i < dr.years.length; i++)
 					dr.years[i] = true;
 			}
 			
 			
 			
 			//Filter down months
 			if(pane.monthList.getSelectedIndex() != 0)
 			{
 				int[] selected = pane.monthList.getSelectedIndices();
 				boolean[] newMos = new boolean[12];
 				
 				for(int i = 0; i < selected.length; i++)
 				{
 					newMos[selected[i]-1] = true;
 				}
 				
 				dr.months = newMos;
 			}
 			else
 			{
 				for(int i = 0; i < dr.months.length; i++)
 					dr.months[i] = true;
 			}
 			
 			
 			
 			//Filter down days of the week...
 			if(pane.dayList.getSelectedIndex() != 0)
 			{
 				int[] selected = pane.dayList.getSelectedIndices();
 				boolean[] newDays = new boolean[7];
 				
 				for(int i = 0; i < selected.length; i++)
 				{
 					newDays[selected[i]-1] = true;
 				}
 				
 				dr.daysOfWeek = newDays;
 			}
 			else
 			{
 				for(int i = 0; i < dr.daysOfWeek.length; i++)
 					dr.daysOfWeek[i] = true;
 			}
 			
 			
 			
 			
 			//Filter down hours...
 			if(pane.hourList.getSelectedIndex() != 0)
 			{
 				int[] selected = pane.hourList.getSelectedIndices();
 				boolean[] newHrs = new boolean[24];
 				
 				for(int i = 0; i < selected.length; i++)
 				{
 					newHrs[selected[i]-1] = true;
 				}
 				
 				dr.hours = newHrs;
 			}
 			else
 			{
 				for(int i = 0; i < dr.hours.length; i++)
 					dr.hours[i] = true;
 			}
 			
 			
 			
 			
 			//Filter down minutes...
 			if(pane.minuteList.getSelectedIndex() != 0)
 			{
 				int[] selected = pane.minuteList.getSelectedIndices();
 				boolean[] newMin = new boolean[60];
 				
 				for(int i = 0; i < selected.length; i++)
 				{
 					newMin[selected[i]-1] = true;
 				}
 				
 				dr.minutes = newMin;
 			}
 			else
 			{
 				for(int i = 0; i < dr.minutes.length; i++)
 					dr.minutes[i] = true;
 			}
 			
 			
 			
 			
 			
 			
 			//Filter down seconds...
 			if(pane.secondList.getSelectedIndex() != 0)
 			{
 				int[] selected = pane.secondList.getSelectedIndices();
 				boolean[] newSec = new boolean[60];
 				
 				for(int i = 0; i < selected.length; i++)
 				{
 					newSec[selected[i]-1] = true;
 				}
 				
 				dr.seconds = newSec;
 			}
 			else
 			{
 				for(int i = 0; i < dr.seconds.length; i++)
 					dr.seconds[i] = true;
 			}
 
 			//Filter by filename regex...
 			if(pane.regex.getText() != null && !pane.regex.getText().equals("")) {
 				dr.regex = pane.regex.getText();
 			}
 			else {
 				dr.regex = null;
 			}
 			
 		panel.redraw();
 	}
 	
 	
 
 }
