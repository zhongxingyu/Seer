 package Engine;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 
 import java.util.LinkedList;
 
 /**
  * @author orpheon
  *
  */
 public class EngineMaster
 {
 	private javax.sound.sampled.SourceDataLine line;
 	
 	private byte[] sound_buffer = new byte[Engine.Constants.SAMPLE_SIZE * Engine.Constants.SNAPSHOT_SIZE];
 	private int sound_buffer_position = sound_buffer.length;
 	private boolean is_playing;
 	
	private Pipe input, output;
 	
 	public LinkedList<Engine.Module> module_list;
 	
	// TODO: This should be part of MidiHandler once implemented
 	private double frequency;
     
     /*
 	 * @throws LineUnavailableException
 	 * @throws InterruptedException
 	 */
 
     public EngineMaster() throws LineUnavailableException, InterruptedException
     {
 		//Open up audio output, using 44100hz sampl2ing rate, 16 bit samples, mono, signed and little endian byte ordering
 		AudioFormat format = new AudioFormat(Engine.Constants.SAMPLING_RATE, Engine.Constants.SAMPLE_SIZE*8, 1, true, false);
 		
 		this.line = (SourceDataLine)AudioSystem.getSourceDataLine(format);
 		this.line.open(format);  
 		this.line.start();
 
 		// First an input pipe to hold the (one) input: Frequency
 		// TODO: Replace this with a MidiHandler object once implemented
 		input = new Pipe();
 		for (int i=0; i<Engine.Constants.SNAPSHOT_SIZE; i++)
 		{
 			input.inner_buffer[i] = frequency;
 		}
 		// Then an output pipe to hold the outputs
 		output = new Pipe();
 		
 		// Also create a general list to hold all modules
 		module_list = new LinkedList<Module>();
     }
     
     // FIXME: This too should be replaced by the MidiHandler once implemented
     public void change_frequency(double new_frequency)
     {
     	frequency = new_frequency;
 		for (int i=0; i<Engine.Constants.SNAPSHOT_SIZE; i++)
 		{
 			input.inner_buffer[i] = frequency;
 		}
     }
     
     public void update()
     {
     	// line.available() changes during execution of the method and that breaks stuff
     	int line_available = line.available();
     	
     	// If we still need to play something
     	if (sound_buffer_position < sound_buffer.length)
     	{
     		// If we can just play what's left,
     		if (sound_buffer.length - sound_buffer_position <= line_available)
     		{
     			// do so
     			line.write(sound_buffer, sound_buffer_position, sound_buffer.length - sound_buffer_position);
     			sound_buffer_position = sound_buffer.length;
     		}
     		else
     		{
     			// play all that we can and update the position counter
     			int delta = line_available;
     			line.write(sound_buffer, sound_buffer_position, delta);
     			sound_buffer_position += delta;
     		}
     	}
     	else
     	{
     		// We already played to end the last chunk
     		// Now either don't do anything if we aren't playing or play the next chunk if we are
     		if (is_playing)
     		{
     			while (true)
     			{
 	    			// Run the entire chain of events
 	    			if (input.get_output() != null)
 	    			{
 	    				// TODO: Think of a system that works if nothing is connected to input
 		    			input.get_output().run();
 		    			byte[] tmp;
 		    			int counter=0;
 		    			
 		    			for (int i=0; i<Engine.Constants.SNAPSHOT_SIZE; i++)
 		    			{
 		    				tmp = Functions.convert_to_16bit_bytearray(output.inner_buffer[i]);
 		    				System.arraycopy(tmp, 0, sound_buffer, counter, 2);
 		    				counter += 2;
 		    			}
 	    				
 		    			// Reset "already_ran" for the next run
 		    			// TODO: Use iterators
 		    			for (int i=0; i<module_list.size(); i++)
 		    			{
 		    				module_list.get(i).already_ran = false;
 		    			}
 		    			
 		    			// And then output it (or atleast as much as we can now)
 		    			counter = line_available;
 		    			if (counter <= sound_buffer.length)
 		    			{
 		    				// If we can fill the entire line
 			    			line.write(sound_buffer, 0, counter);
 			    			sound_buffer_position = counter;
 			    			// Stop for now
 			    			break;
 		    			}
 		    			else
 		    			{
 		    				// If we can't, then output all we have and generate more sound (this is inside a while loop)
 		    				line.write(sound_buffer, 0, sound_buffer.length);
 		    			}
 	    			}
     			}
     		}
     	}
     }
     
     public int add_module(int type)
     {
     	Module m;
     	switch (type)
     	{
     		case Constants.MODULE_OSCILLATOR:
     			m = new Modules.Oscillator(this);
     			break;
     			
     		case Constants.MODULE_MERGER:
     			m = new Modules.Merger(this);
     			break;
     			
     		case Constants.MODULE_SPLITTER:
     			m = new Modules.Splitter(this);
     			break;
     			
     		default:
     			System.out.println("ERROR: Invalid module type requested: "+type);
     			return -1;
     	}
 
     	return m.get_index();
     }
     
     public void start_playing(double frequency)
     {
     	change_frequency(frequency);
     	is_playing = true;
     }
     
     public void stop_playing()
     {
     	is_playing = false;
     }
 	
     public boolean is_playing()
     {
     	return is_playing;
     }
 
     public void close()
     {
 		//Done playing the whole waveform, now wait until the queued samples finish playing, then clean up and exit
 		line.drain();
     	line.close();
     	
     	// Also destroy and disconnect all the modules (who will take care of the pipes themselves)
     	Module m;
     	while (module_list.size() > 0)
     	{
     		m = module_list.getFirst();
     		// Modules disconnect themselves from EngineMaster.module_list automatically
     		m.close(this);
     	}
     }
 }
