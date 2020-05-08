 package Modules;
 
 import Engine.Constants;
 import Engine.Module;
 import Engine.Pipe;
 
 /**
  * @author orpheon
  *
  */
 public class Oscillator extends Module
 {
 	private double frequency = 1;
 	private double phase_offset = 0.0;
	private double amplitude = 1.0;
 	private double detune = 0;
 	
 	private double period = Engine.Constants.pi_times_2;
 	private double current_position = 0.0;
 	
 	public static final int FREQUENCY_PIPE = 0;
 	public static final int PHASE_PIPE = 1;
 	
 	public static final int OUTPUT_PIPE = 0;
 	
 	public static final int SINE_WAVE = 0;
 	public static final int SAW_WAVE = 1;
 	public static final int SQUARE_WAVE = 2;
 	
 	private int osc_type = SINE_WAVE;
 	
 	public Oscillator(Container container, double frequency, double phase_offset, double detune, int osc_type)
 	{		
 		super(container);
 		
 		NUM_INPUT_PIPES = 2;
 		NUM_OUTPUT_PIPES = 1;
 		
 		input_pipes = new Pipe[NUM_INPUT_PIPES];
 		output_pipes = new Pipe[NUM_OUTPUT_PIPES];
 		
 		type = Engine.Constants.MODULE_OSCILLATOR;
 		
 		current_position = 0.0;
 		set_frequency(frequency);
 		set_period(1/frequency);
 		set_phase(phase_offset);
 		set_detune(detune);
 		set_osctype(osc_type);
 	}
 	
 	public Oscillator(Container container)
 	{		
 		super(container);
 		
 		NUM_INPUT_PIPES = 2;
 		NUM_OUTPUT_PIPES = 1;
 		
 		input_pipes = new Pipe[NUM_INPUT_PIPES];
 		output_pipes = new Pipe[NUM_OUTPUT_PIPES];
 		
 		current_position = 0.0;
 	}
 
 	public void run()
 	{
 		for (int i=0; i<Engine.Constants.SNAPSHOT_SIZE; i++)
 		{
 			if (input_pipes[FREQUENCY_PIPE] != null)
 			{
 				set_frequency(input_pipes[FREQUENCY_PIPE].inner_buffer[i] + detune);
 			}
 			if (input_pipes[PHASE_PIPE] != null)
 			{
 				set_phase(input_pipes[PHASE_PIPE].inner_buffer[i]);
 			}
 			current_position += frequency * 1/Constants.SAMPLING_RATE;
			// FIXME: Find a way to make this entire method cleaner and more efficient by restructuring stuff
			// And check the period setting stuff etc, not sure whether it checks the sign and can't take care of it now.
			while (Math.abs(current_position) > 1)
			{
				current_position -= Math.signum(current_position);
			}
 
 			output_pipes[OUTPUT_PIPE].inner_buffer[i] = get_value(current_position) * amplitude;
 		}
 	}
 	
 	protected double get_value(double position)
 	{
 		switch (osc_type)
 		{
 			case SINE_WAVE:
 				return Math.sin(position*Constants.pi_times_2);
 			
 			case SAW_WAVE:
 				return position;
 				
 			case SQUARE_WAVE:
 				if (position < 0.5)
 				{
 					return 0.0;
 				}
 				else
 				{
 					return 1.0;
 				}
 				
 			default:
 				System.out.println("ERROR: Oscillator "+index+" has the invalid osc_type "+osc_type+"!");
 				return 0;
 		}
 	}
 
 	public double get_frequency()
 	{
 		return frequency;
 	}
 
 	public void set_frequency(double frequency)
 	{
 		this.frequency = frequency;
 		set_period(1/frequency);
 	}
 
 	public double get_phase()
 	{
 		return phase_offset;
 	}
 
 	public void set_phase(double phase_offset)
 	{
 		// Move the current_pos by the same offset
 		current_position += (phase_offset - this.phase_offset);
 		while (current_position >= 1)
 		{
 			current_position -= 1;
 		}
 
 		this.phase_offset = phase_offset;
 	}
 
 	public double get_period()
 	{
 		return period;
 	} 
 
 	public void set_period(double period)
 	{
 		this.period = period;
 		while (current_position > 1)
 		{
 			current_position -= 1;
 		}
 	}
 	
 	public void set_osctype(int type)
 	{
 		osc_type = type;
 	}
 	
 	public int get_osctype()
 	{
 		return osc_type;
 	}
 
 	public double get_amplitude()
 	{
 		return amplitude;
 	}
 
 	public void set_amplitude(double amplitude)
 	{
 		this.amplitude = amplitude;
 	}
 
 	public double get_detune()
 	{
 		return detune;
 	}
 
 	public void set_detune(double detune)
 	{
 		this.detune = detune;
 	}
 }
