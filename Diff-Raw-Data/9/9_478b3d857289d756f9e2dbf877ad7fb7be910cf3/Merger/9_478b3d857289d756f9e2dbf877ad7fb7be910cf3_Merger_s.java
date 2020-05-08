 /**
  * 
  */
 package Modules;
 
 import Engine.EngineMaster;
 import Engine.Module;
 import Engine.Pipe;
 import Engine.Constants;
 
 /**
  * @author orpheon
  *
  */
 public class Merger extends Module
 {
 	public static final int ADDITION = 1;
 	public static final int MULTIPLICATION = 2;
 	
 	public static final int OUTPUT_PIPE = 0;
 	
 	private int operation = ADDITION;
 	
 	public Merger(EngineMaster engine)
 	{
 		super(engine);
 		
 		NUM_INPUT_PIPES = 2;
 		NUM_OUTPUT_PIPES = 1;
 		
 		input_pipes = new Pipe[NUM_INPUT_PIPES];
 		output_pipes = new Pipe[NUM_OUTPUT_PIPES];
 		
 		type = Engine.Constants.MODULE_MERGER;
 	}
 	
 	public Merger(EngineMaster engine, int num_inputs)
 	{
 		super(engine);
 		
 		NUM_INPUT_PIPES = num_inputs;
 		NUM_OUTPUT_PIPES = 1;
 		
 		input_pipes = new Pipe[NUM_INPUT_PIPES];
 		output_pipes = new Pipe[NUM_OUTPUT_PIPES];
 	}
 
 	@Override
 	public void get_sound()
 	{
 		if (output_pipes[OUTPUT_PIPE] != null)
 		{
 			int i, j;
 			switch (operation)
 			{
 				case ADDITION:
					int sum;
 					for (i=0; i<Constants.SNAPSHOT_SIZE; i++)
 					{
 						sum = 0;
 						for (j=0; j<NUM_INPUT_PIPES; j++)
 						{
 							if (input_pipes[j] != null)
 							{
 								sum += input_pipes[j].inner_buffer[i];
 							}
 						}
						output_pipes[0].inner_buffer[i] = sum;
 					}
 					break;
 					
 				case MULTIPLICATION:
					int product;
 					for (i=0; i<Constants.SNAPSHOT_SIZE; i++)
 					{
 						product = 1;
 						for (j=0; j<NUM_INPUT_PIPES; j++)
 						{
 							if (input_pipes[j] != null)
 							{
 								product *= input_pipes[j].inner_buffer[i];
 							}
 						}
 						output_pipes[OUTPUT_PIPE].inner_buffer[i] = product;
 					}
 					break;
 					
 				default:
 					System.out.println("ERROR: Merger "+index+" has an invalid operation type: "+operation);
 			}
 		}
 	}
 
 	public int get_operation()
 	{
 		return operation;
 	}
 
 	public void set_operation(int operation)
 	{
 		this.operation = operation;
 	}
 
 	public int get_num_inputs()
 	{
 		return NUM_INPUT_PIPES;
 	}
 	
 	public void set_num_inputs(int num)
 	{
 		// No need to change anything if there's no change
 		if (num != NUM_INPUT_PIPES)
 		{
 			// If we want to lower the number of connections, then disconnect all the superfluous ones
 			if (NUM_INPUT_PIPES > num)
 			{ 
 				for (int i=num; i<NUM_INPUT_PIPES; i++)
 				{
 					if (input_pipes[i] != null)
 					{
 						disconnect_input(i);
 					}
 				}
 			}
 			NUM_INPUT_PIPES = num;
 			Pipe[] tmp = new Pipe[NUM_INPUT_PIPES];
 			System.arraycopy(input_pipes, 0, tmp, 0, Math.min(input_pipes.length, NUM_INPUT_PIPES));
 			input_pipes = tmp;
 		}
 	}
 }
