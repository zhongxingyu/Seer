 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.JComboBox;
 import javax.swing.text.JTextComponent;
 
 
 public class HP33120aControl implements ActionListener, FocusListener{
 	private HP33120aInterface view;
 	private TCPClient tcpClient;
 	private JComboBox combo;
 	private HP33120a hp33120a;
 	private String test;
 	
 	private boolean VALIDATION_FLAG = false;
 	
 	public HP33120aControl(HP33120aInterface view, TCPClient socketClient, HP33120a hp33120a){
 		this.tcpClient = socketClient;
 		this.view = view;
 		this.hp33120a = hp33120a;
 	}
 
 	@Override
 	public void focusGained(FocusEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void focusLost(FocusEvent event) {
 		// TODO Change the if... for a case structure
 				System.out.println("A system has lost its focus! ");
 				final JTextComponent c = (JTextComponent) event.getSource();
 				String name = c.getName();
 				String text = c.getText();
 				if (name.equals(HP33120aInterface.AMPLITUDE)){
 					System.out.println("Here should be called the data-validation method for Amplitude");
 				}else if (name.equals(HP33120aInterface.FREQUENCY)){
 					System.out.println("Here should be called the data-validation method for Frequency");
 					VALIDATION_FLAG = hp33120a.frequencyValidation(text);
 					if(VALIDATION_FLAG){
 						// There is some error in the frequency
 						System.out.println("Frequency is higher than expected");
 						view.disableExecutionButton();
 						view.setDataValidationMessage(hp33120a.getDataValidationMessage());
 					}
 					else{
 						view.enableExecutionButton();
 						view.disableDataValidationLabel();
 					}				
 				} else if (name.equals(HP33120aInterface.OFFSET)){
 					System.out.println("Here should be called the data-validation method for Offset");
 				}else if (name.equals(HP33120aInterface.RAMP_SYMMETRY)){
 					System.out.println("Here should be called the data-validation method for Ramp Symmetry");
 				}else if (name.equals(HP33120aInterface.DUTY_CYCLE_SQUARE)){
 					System.out.println("Here should be called the data-validation method for Duty Cycle Square");
 				}else if (name.equals(HP33120aInterface.DUTY_CYCLE_PULSE)){
 					System.out.println("Here should be called the data-validation method for Duty Cycle Pulse");
 				}else if (name.equals(HP33120aInterface.MODULATING_FREQUENCY)){
 					System.out.println("Here should be called the data-validation method for Modulating Frequency");
 				}else if (name.equals(HP33120aInterface.AM_DEPTH)){
 					System.out.println("Here should be called the data-validation method for AM Depth");
 				}else if (name.equals(HP33120aInterface.FM_DEVIATION)){
 					System.out.println("Here should be called the data-validation method for FM Deviation");
 				}else if (name.equals(HP33120aInterface.HOP_FREQUENCY)){
 					System.out.println("Here should be called the data-validation method for Hop Frequency");
 				}else if (name.equals(HP33120aInterface.DEVIATION_PWM)){
 					System.out.println("Here should be called the data-validation method for Int Deviation PWM");
 				}else if (name.equals(HP33120aInterface.PHASE_DEVIATION_PM)){
 					System.out.println("Here should be called the data-validation method for Phase Deviation");
 				}else if (name.equals(HP33120aInterface.BURST_RATE)){
 					System.out.println("Here should be called the data-validation method for Burst Rate");
 				}else if (name.equals(HP33120aInterface.BURST_COUNT)){
 					System.out.println("Here should be called the data-validation method for Burst Count");
 				}else if (name.equals(HP33120aInterface.BURST_PHASE)){
 					System.out.println("Here should be called the data-validation method for Burst Phase");
 				}
 				System.out.println("Text : " +text);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		System.out.println("HP33120a Event Triggered");
 		if(event.getActionCommand().equals(HP33120aInterface.TYPE_OF_SIGNAL)){	
 			combo = view.getTypeOfSignal();
 			if (combo.getSelectedItem().equals(HP33120aInterface.SIGNAL)){
 				System.out.println("Disabling Buttons");
 				view.disableModulationbuttons();
 			} else if (combo.getSelectedItem().equals(HP33120aInterface.MODULATION)){
 				view.enableModulationButtons();
 				System.out.println("Enabling Buttons");
 			}
 		}
		if(event.getActionCommand().equals(WaveFormInterface.CONFIG)){
 			// Configuration Button has been pressed, we have to read all the fields
 			// create a request and send it to the server. We are going to use the CSV format
 			System.out.println("Do it!! Button has been presed");	
 			readFields();
 			// TODO: Fix the next three lines:
 			hp33120a.setFrame();
 			// Test print for WaveformGen
 			System.out.println(hp33120a.getFrame());
 			}
 		if(event.getActionCommand().equals(WaveFormInterface.FREQUENCY)){
 			System.out.println("A system has lost its focus! ");
 		}		
 	}
 	
 	private void readFields(){
 		combo = view.getTypeOfSignal();
 		// Start reading signal fields
 		System.out.println(view.getTypeOfSignal().getSelectedItem());
 		hp33120a.setTypeOfSignal(view.getTypeOfSignal().getSelectedIndex());
 		
 		// TODO: We need a catalog to know what each number means
 		hp33120a.setSignalShape(view.getSignalShape().getSelectedIndex());
 		hp33120a.setSignalFreq(Float.parseFloat(view.getFrequency()));
 		hp33120a.setSignalAmp(Float.parseFloat(view.getAmplitude()));
 		hp33120a.setSignalOff(Float.parseFloat(view.getOffset()));
 		hp33120a.setRampSymm(Integer.parseInt(view.getRampSymmetry()));
 		hp33120a.setDutyCycleSq(Integer.parseInt(view.getDutyCycleSquare()));
 		hp33120a.setDutyCyclePuls(Integer.parseInt(view.getDutyCyclePulse()));
 		
 		if(combo.getSelectedItem().equals(WaveFormInterface.MODULATION)){
 			// We also read the modulation fields
 			hp33120a.setModType(view.getModType().getSelectedIndex());
 			hp33120a.setModWfmShape(view.getModWfmShape().getSelectedIndex());
 			hp33120a.setAmDepth(Integer.parseInt(view.getAmDepth()));
 			hp33120a.setDeviationFM(Float.parseFloat(view.getFmDeviation()));
 			hp33120a.setHopFrequency(Float.parseFloat(view.getHopFrequency()));
 			hp33120a.setInternalDeviation(Float.parseFloat(view.getIntDeviationPWM()));
 			hp33120a.setPhaseDeviation(Float.parseFloat(view.getPhaseDeviationPM()));
 			hp33120a.setBurstRate(Float.parseFloat(view.getBurstRate()));
 			hp33120a.setBurstCount(Integer.parseInt(view.getBurstCount()));
 			hp33120a.setBurstPhase(Integer.parseInt(view.getBurstPhase()));
 		}
 	}
 }
