 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.text.JTextComponent;
 
 import org.jfree.data.xy.XYSeries;
 
 
 public class HP54602bControl implements ActionListener, FocusListener{
 	private TCPClient tcpClient;
 	private HP54602bInterface view;
 	private HP54602b hp54602b;
 	private String auxDataValidationMessage;
 	
 	public HP54602bControl(HP54602bInterface view, TCPClient socketClient, HP54602b hp54602b){
 		this.hp54602b = hp54602b;
 		this.view = view;
 		this.tcpClient = socketClient;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		System.out.println("HP54602B has triggered an event");
 		if(event.getActionCommand().equals(HP54602bInterface.CONFIG)){
 			String[] receivedData = null;
 			System.out.println("Do it! Button has been pressed!");
 			readFields();
 			hp54602b.setFrame();
 			System.out.println(hp54602b.getFrame());
 			try{
 				receivedData = TCPClient.bidirectComm(hp54602b.getFrame(), Globals.HP54602_QUERY_MESSAGE);
 			} catch (Exception e){
 				// TODO: Handle exception
 			}
 			decodeHP54602bResponse(receivedData);
 		}
 	}
 	
 	@Override
 	public void focusGained(FocusEvent arg0) {
 		// Nothing to do! 
 	}
 
 	@Override
 	public void focusLost(FocusEvent event) {
 		final JTextComponent c = (JTextComponent) event.getSource();
 		String name = c.getName();
 		if(name.equals(HP54602bInterface.CH1_POS) || 
 				name.equals(HP54602bInterface.CH1_RANGE) ||
 				name.equals(HP54602bInterface.CH2_POS) ||
 				name.equals(HP54602bInterface.CH2_RANGE) ||
 				name.equals(HP54602bInterface.TIME_DELAY) ||
 				name.equals(HP54602bInterface.TIME_RANGE) ||
 				name.equals(HP54602bInterface.TRIGGER_LEVEL)){
 			dataValidation(readFields());
 		}
 	}
 	
 	private boolean readFields(){
 		auxDataValidationMessage = "";
 		boolean formatError = false;
 		hp54602b.setAutoset(view.getAutoSet().isSelected());
 		hp54602b.setCh1(view.getBtnCh1().isSelected());
 		hp54602b.setCh2(view.getBtnCh2().isSelected());
 		hp54602b.setCh1BW(view.getBtnCh1BW().isSelected());
 		hp54602b.setCh2BW(view.getBtnCh2BW().isSelected());
 		hp54602b.setPositiveNegativeSlope(view.getSlopeButton().isSelected());
 		hp54602b.setCh1Function(view.getFunctionCh1().getSelectedIndex());
 		hp54602b.setCh2Function(view.getFunctionCh2().getSelectedIndex());
 		hp54602b.setCh1Coupling(view.getCouplingCh1().getSelectedIndex());
 		hp54602b.setCh2Coupling(view.getCouplingCh2().getSelectedIndex());
 		hp54602b.setCh1Probe(view.getProbeCh1().getSelectedIndex());
 		hp54602b.setCh2Probe(view.getProbeCh2().getSelectedIndex());
 		
 		if(ch1RangeDataValidation())
 			formatError = true;
 		if(ch2RangeDataValidation())
 			formatError = true;
 		if(ch1PositionDataValidation())
 			formatError = true;
 		if(ch2PositionDataValidation())
 			formatError = true;
 		if(timeRangeDataValidation())
 			formatError = true;
 		if(timeDelayDataValidation())
 			formatError = true;
 		if(triggerDataValidation())
 			formatError = true;
 		return formatError;
 	}
 	
 	private void dataValidation(boolean formatError){
 		if(formatError){
 			view.configExecutionButton(false);
 			view.setDataValidationMessage(auxDataValidationMessage);
 		} else {
 			view.configExecutionButton(true);
 			view.disableDataValidationMessage();
 		}
 	}
 	
 	private boolean ch1RangeDataValidation(){
 		boolean flag = false;
 		try{
 			hp54602b.setCh1Range(Float.parseFloat(view.getRangeCh1().getText()));
 		} catch (NumberFormatException e){
 			flag = true;
 			auxDataValidationMessage += "Ch1 Range must be a float\n";
 		}
 		return flag;
 	}
 	
 	private boolean ch2RangeDataValidation(){
 		boolean flag = false;
 		try{
 			hp54602b.setCh2Range(Float.parseFloat(view.getRangeCh2().getText()));
 		} catch (NumberFormatException e){
 			flag = true;
 			auxDataValidationMessage += "Ch2 Range must be a float.\n";
 		}
 		return flag;
 	}
 	
 	private boolean ch1PositionDataValidation(){
 		boolean flag = false;
 		try{
 			hp54602b.setCh1Pos(Float.parseFloat(view.getPositionCh1().getText()));
 		} catch (NumberFormatException e){
 			flag = true;
 			auxDataValidationMessage += "Ch1 Position must be a float\n";
 		}
 		return flag;
 	}
 	
 	private boolean ch2PositionDataValidation(){
 		boolean flag = false;
 		try{
 			hp54602b.setCh2Pos(Float.parseFloat(view.getPositionCh2().getText()));
 		} catch (NumberFormatException e){
 			flag = true;
 			auxDataValidationMessage += "Ch2 Position must be a float\n";			
 		}
 		return flag;
 	}
 	
 	private boolean timeRangeDataValidation(){
 		boolean flag = false;
 		try{
 			hp54602b.setTimeRange(Float.parseFloat(view.getRangeTime().getText()));
 		} catch (NumberFormatException e){
 			flag = true;
 			auxDataValidationMessage += "Time Range must be a float";
 		}
 		return flag;
 	}
 	
 	private boolean timeDelayDataValidation(){
 		boolean flag = false;
 		try{
 			hp54602b.setTimeDelay(Float.parseFloat(view.getTimeDelay().getText()));
 		} catch (NumberFormatException e){
 			flag = true;
 			auxDataValidationMessage += "Time Delay must be a float";
 		}
 		return flag;
 	}
 	
 	private boolean triggerDataValidation(){
 		boolean flag = false;
 		try{
 			hp54602b.setTriggerLevel(Float.parseFloat(view.getTriggerlevel().getText()));
 		} catch (NumberFormatException e){
 			flag = true;
 			auxDataValidationMessage += "Trigger level must be a float";
 		}
 		return flag;
 	}	
 	
 	// Method to decode HP54602b response
 	private void decodeHP54602bResponse(String[] receivedData){
 		int messageType = Integer.parseInt(receivedData[0]);			
 		switch(messageType){
 		case 41:
 			// Measuring success
 			// If it is needed to do some calculations with the measured value, method should be called here
 			System.out.println("Everything was ok!");
 			decodeOkResponse(receivedData[1]);
 			break;
 		case 43:
 			// Measuring failure
 			System.out.println("Something was wrong!!");
 			break;			
 		}
 	}
 	
 	private void decodeOkResponse(String receivedOkMessage){
 		String[] decodedResponse;
 		XYSeries trace1 = null;
 		XYSeries trace2 = null;
 		decodedResponse = receivedOkMessage.split(",");
 		view.setFunc1MeasuredValue(decodedResponse[0]);
 		view.setFunc2MeasuredValue(decodedResponse[1]);	
 		int trace1Points = Integer.parseInt(decodedResponse[2]);
 		int trace2Points = Integer.parseInt(decodedResponse[3]);
 		System.out.println("Trace 1: " + decodedResponse[2]);
 		System.out.println("Trace 2: " + decodedResponse[3]);
 		if(trace1Points > 0){
 			// We need to draw trace 1
 			trace1 = processTrace1(decodedResponse[4], decodedResponse[5], decodedResponse[6]);
 		}
 		if(trace2Points > 0){
 			// We need to draw trace 2
 			trace2 = processTrace2(decodedResponse[7],decodedResponse[8],decodedResponse[9]);
 		}
 		view.setXYSeries(trace1, trace2);
 	}
 	
 	private XYSeries processTrace1(String t_0, String dtime, String trace){
 		int i;
 		XYSeries series = new XYSeries("Channel 1");
 		Double t0 = Double.parseDouble(t_0);
 		Double dt = Double.parseDouble(dtime);
 		System.out.println(t0);
 		System.out.println(dt);
 		System.out.println("TRACE 1: " + trace);
 		String[] decodedTrace = trace.split(";");
 		for(i=0;i<decodedTrace.length;i++){
 			// We need to add data to XYSeries
 			// t0 is the initial time value and dt is a differential time value
 			series.add((t0 + dt*i), Double.parseDouble(decodedTrace[i]));
 		}
 		return series;
 	}
 	
 	private XYSeries processTrace2(String t_0, String dtime, String trace){
 		XYSeries series = new XYSeries ("Channel 2");
 		int i;
 		Double t0 = Double.parseDouble(t_0);
 		Double dt = Double.parseDouble(dtime);
 		System.out.println("Trace 2: " + trace);
 		String[] decodedTrace = trace.split(";");
 		for(i=0;i<decodedTrace.length;i++){
 			series.add((t0 + dt*i), Double.parseDouble(decodedTrace[i]));
 		}
 		return series;
 	}
 }
