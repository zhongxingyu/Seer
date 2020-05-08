 package panels;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 import visualize.DateRange;
 import actions.RedrawAction;
 import actions.SwitchSidePanelAction;
 import actions.ViewSelectAction;
 
 public class ViewPanel {
 	
 	//JScrollPane sidePanel;
 	public MasterPanel m;
 	public JPanel panel;
 	public DateRange range;
 	String title;
 	
 	
 	int offset = 0;
 	Dimension d;
 	Color c;
 	
 	public JRadioButton yearsButton = new JRadioButton("Year");
 	public JRadioButton monthsButton = new JRadioButton("Month");
 	public JRadioButton daysOfWeekButton = new JRadioButton("Day");
 	public JRadioButton hoursButton = new JRadioButton("Hour");
 	public JRadioButton minutesButton = new JRadioButton("Minute");
 	public JRadioButton secondsButton = new JRadioButton("Second");
 	
 	
 	public JList yearList;
 	public JList monthList;
 	public JList dayList;
 	public JList hourList;
 	public JList minuteList;
 	public JList secondList;
 	public JTextField regex;
 	
 	
 	public ViewPanel(MasterPanel master, DateRange r, Dimension d, Color c, String s, ViewPanel prev)
 	{
 		
 		m = master;
 		this.d = d;
 		this.c = c;
 		range = r;
 		title = s;
 		
 
 		
 		panel = new JPanel();
 		panel.setLayout(new GridBagLayout());
 		panel.setPreferredSize(d);
         panel.setBackground(c);
         
         
       
         
         GridBagConstraints constraints = getConstraints();
 
         ButtonGroup g = new ButtonGroup();
         g.add(yearsButton);
         g.add(monthsButton);
         g.add(daysOfWeekButton);
         g.add(hoursButton);
         g.add(minutesButton);
         g.add(secondsButton);
         if(prev != null) 
         {
         	yearsButton = prev.yearsButton;
         	monthsButton = prev.monthsButton;
         	daysOfWeekButton = prev.daysOfWeekButton;
         	hoursButton = prev.hoursButton;
         	minutesButton = prev.minutesButton;
         	secondsButton = prev.secondsButton;
        	}
         else yearsButton.setSelected(true);
 
        
         
 
         
         
         
         ////////////// start of button panel
         
         constraints.gridy = offset++;
         constraints.gridx = 0;
         constraints.weighty = 0;
         constraints.insets = new Insets(0,10,0,10);
         
         panel.add(new JLabel(" "), constraints);
         
         
         if(title.indexOf("Single")==-1)
         	createComboBox();
         
         constraints.gridy = offset++;
         panel.add(new JLabel(" "), constraints);
         
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(new GridBagLayout());
         buttonPanel.setBackground(c);
         
         Border line = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
         TitledBorder border = BorderFactory.createTitledBorder(line,
         		"View data by...",
         		TitledBorder.LEFT,
         		TitledBorder.TOP);
         buttonPanel.setBorder( border);
         
         constraints.gridy = offset++;
         constraints.gridx = 0;
         constraints.weighty = 1;
 		panel.add(buttonPanel, constraints);
         
 		
 		
         addYears(buttonPanel);
         addMonths(buttonPanel);
         addDaysOfWeek(buttonPanel);
         addHours(buttonPanel);
         addMinutes(buttonPanel);
         addSeconds(buttonPanel);
         
         
 		
 		
 		
 		///////////////// BUFFER between button and list panels
         
         constraints.gridy = offset++;
         constraints.gridx = 0;
         constraints.weighty = 0;
         panel.add(new JLabel(" "), constraints);
         
         
         
         ///////////////// start of list panel
         
         
         JPanel listPanel = new JPanel();
         listPanel.setLayout(new GridBagLayout());
         listPanel.setBackground(c);
         
         Border line2 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
         TitledBorder border2 = BorderFactory.createTitledBorder(line2,
         		"Select data to include, by...",
         		TitledBorder.LEFT,
         		TitledBorder.TOP);
         listPanel.setBorder( border2);
         
         constraints.gridy = offset++;
         constraints.gridx = 0;
         constraints.weighty = 1;
         panel.add(listPanel, constraints);
         
         
         int[] selected = new int[] {0};
         if(prev==null)
         {
         	addYearList(listPanel,selected);
         	addMonthsList(listPanel, selected);
         	addDaysList(listPanel, selected);
         	addHoursList(listPanel, selected);
         	addMinutesList(listPanel, selected);
         	addSecondsList(listPanel, selected);
         	addRegexBox(listPanel, "");
         }
         else
         {
         	addYearList(listPanel,prev.yearList.getSelectedIndices());
         	addMonthsList(listPanel, prev.monthList.getSelectedIndices());
         	addDaysList(listPanel, prev.dayList.getSelectedIndices());
         	addHoursList(listPanel, prev.hourList.getSelectedIndices());
         	addMinutesList(listPanel, prev.minuteList.getSelectedIndices());
         	addSecondsList(listPanel, prev.secondList.getSelectedIndices());
         	addRegexBox(listPanel, prev.regex.getText());
         }
                 
         constraints.gridx = 0;
         //constraints.gridy = offset++; // commented to make room for filter files
 		constraints.anchor = GridBagConstraints.CENTER;
 		
 		
 /////////////// BUTTON SECTION
 		
 		
 		//constraints.weighty = 0;
 		panel.add(new JLabel(" "), constraints);
 		constraints.gridy = offset++;
 		
 		constraints.insets = new Insets(0,110,0,110);
         final JButton bSubmit = new JButton("Apply");
         bSubmit.addActionListener(new RedrawAction(m));
         panel.add(bSubmit, constraints);
         
         constraints.gridy = offset++;
         panel.add(new JLabel(" "), constraints);
 		
         
         // Add border to the main panel.
         /*
         Border line = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
         TitledBorder border = BorderFactory.createTitledBorder(line,"Select filters");
         panel.setBorder( border);
         */
         
 	}
 	
 	
 	public void createComboBox()
 	{
 		GridBagConstraints c = new GridBagConstraints();
 		String[] choices = new String[2];
 		choices[0] = "Top Graph";
 		choices[1] = "Bottom Graph";
 		
 		JButton graph1 = new JButton(choices[0]);
 		JButton graph2 = new JButton(choices[1]);
 		
 		if(title.indexOf("1") != -1)
 		{
 			graph1.setEnabled(false);
 			graph2.addActionListener(new SwitchSidePanelAction(m));
 		}
 		else if(title.indexOf("2") != -1)
 		{
 			graph2.setEnabled(false);
 			graph1.addActionListener(new SwitchSidePanelAction(m));
 		}
 		
 		JPanel wrapper = new JPanel();
 		
 		wrapper.setLayout(new GridBagLayout());
 		wrapper.setBackground(this.c);
 		c.gridx = 0;
 		c.gridy = 1;
 		wrapper.add(graph1, c);
 		c.gridx = 1;
 		wrapper.add(graph2,c);
 		
 		
 		c.gridx = 0;
 		//c.gridy = offset++; // Removed to make room for filter files
 		c.insets = new Insets(0,10,0,10);
 		panel.add(wrapper, c);
 		
 	}
 	
 	
 	public void addRegexBox(JPanel panel, String prev)
 	{
 		JTextField re = new JTextField(10);
		re.setText(prev);
 		regex = re;
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 5;
 		c.gridwidth = 3;
 	
 		panel.add(new JLabel("Filter files (Regex)"), c);
 			
 		c.insets = new Insets(0,5,10,5);
 		c.gridy = 6;
 		
 		panel.add(re, c);
 	}
 	
 	public void addSecondsList(JPanel panel, int[] ind)
 	{
 		String[] sec = new String[range.seconds.length+1];
 		sec[0] = "Select all";
 		for(int i = 1; i < sec.length; i++)
 		{
 			sec[i] = ""+(i-1);
 		}
 		JList list = new JList(sec);
 		list.setVisibleRowCount(7);
 		list.setSelectedIndices(ind);
 		secondList = list;
 		JScrollPane sp = new JScrollPane(list);
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 2;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.insets = new Insets(10,5,0,5);
 
 		
 		panel.add(new JLabel("Second"),c);
 		c.insets = new Insets(0,5,10,5);
 		c.gridy = 4;
 
 		panel.add(sp, c);
 	}
 	
 	
 	public void addMinutesList(JPanel panel, int[] ind)
 	{
 		String[] min = new String[range.minutes.length+1];
 		min[0] = "Select all";
 		for(int i = 1; i < min.length; i++)
 		{
 			min[i] = ""+(i-1);
 		}
 		JList list = new JList(min);
 		list.setVisibleRowCount(7);
 
 		list.setSelectedIndices(ind);
 		minuteList = list;
 		JScrollPane sp = new JScrollPane(list);
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		
 		c.gridx = 1;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.insets = new Insets(10,5,0,5);
 
 		
 		panel.add(new JLabel("Minute"),c);
 		c.insets = new Insets(0,5,10,5);
 		//c.anchor = GridBagConstraints.WEST;
 
 		c.gridy = 4;
 		
 		panel.add(sp, c);
 	}
 	
 	
 	public void addHoursList(JPanel panel, int[] ind)
 	{
 		String[] hrs = new String[range.hours.length+1];
 		hrs[0] = "Select all";
 		for(int i = 1; i < hrs.length; i++)
 		{
 			hrs[i] = ""+(i-1);
 		}
 		JList list = new JList(hrs);
 		list.setVisibleRowCount(7);
 
 		list.setSelectedIndices(ind);
 		hourList = list;
 		JScrollPane sp = new JScrollPane(list);
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.insets = new Insets(10,5,0,5);
 
 		
 		panel.add(new JLabel("Hour"),c);
 		c.insets = new Insets(0,5,10,5);
 		//c.anchor = GridBagConstraints.WEST;
 
 		c.gridy = 4;
 		
 		panel.add(sp, c);
 	}
 	
 	
 	public void addDaysList(JPanel panel, int[] ind)
 	{
 		String[] days = new String[range.daysOfWeek.length+1];
 		days[0] = "Select all";
 		for(int i = 1; i < days.length; i++)
 		{
 			days[i] = ""+DateRange.getDay(i-1).substring(0,3);
 		}
 		JList list = new JList(days);
 		list.setVisibleRowCount(7);
 
 		list.setSelectedIndices(ind);
 		dayList = list;
 		JScrollPane sp = new JScrollPane(list);
 		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		
 		GridBagConstraints c = new GridBagConstraints();
 
 		
 		c.gridx = 2;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(10,5,0,5);
 
 		
 		panel.add(new JLabel("Day"),c);
 		c.insets = new Insets(0,5,0,5);
 		//c.anchor = GridBagConstraints.WEST;
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.CENTER;
 
 		panel.add(sp, c);
 	}
 	
 	
 	public void addMonthsList(JPanel panel, int[] ind)
 	{
 		String[] mos = new String[range.months.length+1];
 		mos[0] = "Select all";
 		for(int i = 1; i < mos.length; i++)
 		{
 			mos[i] = DateRange.getMonth(i-1).substring(0,3);
 		}
 		JList list = new JList(mos);
 		list.setVisibleRowCount(7);
 
 		list.setSelectedIndices(ind);
 		monthList = list;
 		JScrollPane sp = new JScrollPane(list);
 
 		GridBagConstraints c = new GridBagConstraints();
 
 		
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(10,5,0,5);
 
 		
 		panel.add(new JLabel("Month"),c);
 		//c.anchor = GridBagConstraints.WEST;
 		c.insets = new Insets(0,5,0,5);
 		c.gridy = 2;
 		
 		panel.add(sp, c);
 	}
 	
 	
 	public void addYearList(JPanel panel, int[] ind)
 	{
 		String[] yrs = null;
 		
 		if(range.years==null) yrs = new String[1];
 		else yrs = new String[range.years.length+1];
 		int year = range.minYear;
 		yrs[0] = "Select all";
 		for(int i = 1; i < yrs.length; i++)
 		{
 			yrs[i] = ""+year;
 			year++;
 		}
 		JList list = new JList(yrs);
 		
 		list.setVisibleRowCount(7);
 		list.setSelectedIndices(ind);
 		yearList = list;
 		JScrollPane sp = new JScrollPane(list);
 		
 		GridBagConstraints c = new GridBagConstraints();
 
 		c.gridy = 1;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		c.insets = new Insets(10,5,0,5);
 		
 		panel.add(new JLabel("Year"),c);
 		
 		c.insets = new Insets(0,5,0,5);
 		
 		c.gridy = 2;
 		panel.add(sp, c);
 		
 	}
 	
 
 	public GridBagConstraints getConstraints()
 	{
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		
 		c.weightx =.5;
 		
 		return c;
 	}
 	
 	
 
 	public void addSeconds(JPanel panel)
 	{
 		GridBagConstraints c = new GridBagConstraints();
 	
 		c.gridx = 2;
 		c.gridy = 2;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.anchor = GridBagConstraints.WEST;
 		c.insets = new Insets(0,10,10,10);
 		secondsButton.setBackground(this.c);
 		secondsButton.addActionListener(new ViewSelectAction(secondsButton, range, DateRange.Grouping.SECONDS));
 		panel.add(secondsButton, c);
 	
 		
 	}	
 	
 	public void addMinutes(JPanel panel)
 	{
 		GridBagConstraints c = new GridBagConstraints();
 	
 		c.gridx = 1;
 		c.gridy =2;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.anchor = GridBagConstraints.WEST;
 		c.insets = new Insets(0,10,10,10);
 		minutesButton.setBackground(this.c);
 		minutesButton.addActionListener(new ViewSelectAction(minutesButton, range, DateRange.Grouping.MINUTES));
 		panel.add(minutesButton, c);
 	}	
 	
 	public void addHours(JPanel panel)
 	{
 		GridBagConstraints c = new GridBagConstraints();
 	
 		c.gridx = 0;
 		c.gridy = 2;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.anchor = GridBagConstraints.WEST;
 		c.insets = new Insets(0,10,10,10);
 		hoursButton.setBackground(this.c);
 		hoursButton.addActionListener(new ViewSelectAction(hoursButton, range, DateRange.Grouping.HOURS));
 		panel.add(hoursButton, c);
 	}	
 	
 	public void addDaysOfWeek(JPanel panel)
 	{
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 2;
 		c.gridy =1;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.anchor = GridBagConstraints.WEST;
 		c.insets = new Insets(10,10,0,10);
 		daysOfWeekButton.setBackground(this.c);
 		daysOfWeekButton.addActionListener(new ViewSelectAction(daysOfWeekButton, range, DateRange.Grouping.DAYS_OF_WEEK));
 		panel.add(daysOfWeekButton, c);
 	}
 
 	public void addMonths(JPanel panel)
 	{
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 1;
 		c.gridy = 1;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.anchor = GridBagConstraints.WEST;
 		c.insets = new Insets(10,10,0,10);
 		monthsButton.setBackground(this.c);
 		monthsButton.addActionListener(new ViewSelectAction(monthsButton, range, DateRange.Grouping.MONTHS));
 		panel.add(monthsButton, c);
 
 	}
 
 	public void addYears(JPanel panel)
 	{
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 0;
 		c.gridy = 1;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.anchor = GridBagConstraints.WEST;
 		c.insets = new Insets(10,10,0,10);
 		//yearsButton.setSelected(true);
 		yearsButton.setBackground(this.c);
 		yearsButton.addActionListener(new ViewSelectAction(yearsButton, range, DateRange.Grouping.YEARS));
 		panel.add(yearsButton, c);
 	}
 	
 	
 
 }
