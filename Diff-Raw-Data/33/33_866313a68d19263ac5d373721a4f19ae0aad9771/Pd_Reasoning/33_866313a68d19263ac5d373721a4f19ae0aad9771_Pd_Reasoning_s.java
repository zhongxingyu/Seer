 package ensemble.apps.pd_testing;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import ensemble.*;
 import ensemble.apps.pd_testing.Pd_Constants;
 import ensemble.clock.TimeUnit;
 import ensemble.memory.*;
 
 import org.puredata.core.*;
 
 /*
  * This ensemble.Reasoning extension
  * uses a pure-data patch as a source
  * of audio samples and other data.
  * 
  * It is able to receive bangs, floats and
  * messages from patches.
  */
 public class Pd_Reasoning extends Reasoning
 {
 	private HashMap< String, Pd_Actuator > actuators = new HashMap<String, Pd_Actuator> ( );
 	private HashMap< String, Memory > actuator_memories = new HashMap<String, Memory> ( );
 	
 	private HashMap< String, Pd_Sensor > sensors = new HashMap<String, Pd_Sensor> ( );
 	private HashMap< String, Memory > sensor_memories = new HashMap<String, Memory> ( );
 	
 	private HashMap< String, Pd_Event > senses = new HashMap< String, Pd_Event > ( );
 
     private Pd_Audio_Server sampler;
     
 	private int patch;
 	private String subpatch;
 	private String agent_name;
 	private Pd_Receiver receiver;
 	
     private void process_ensemble_control_messages ( )
     {
     	CopyOnWriteArrayList< Pd_Message > messages = receiver.get_messages ( );
     	for ( Pd_Message message : messages )
     	{
     		String source = message.get_source ( );
     		if ( source.equals ( Pd_Constants.SUBSCRIPTION ) )
     		{
 				System.err.println ( "PURE_DATA: REGISTERED_USER_SYMBOL: " + message.get_symbol ( ) );
     			receiver.register_symbol ( agent_name + message.get_symbol ( ) );
     		}
     		else if ( source.equals ( Pd_Constants.UNSUBSCRIPTION ) )
     		{
 				System.err.println ( "PURE_DATA: DEREGISTERED_USER_SYMBOL: " + message.get_symbol ( ) );
     			receiver.deregister_symbol ( agent_name + message.get_symbol ( ) );
     		}
     	}
     }
 	/*
 	 * User can implement message checking here.
 	 */
     private void process_pd_messages ( ) 
     { 
     	CopyOnWriteArrayList< Pd_Message > messages = receiver.get_messages ( );
     	CopyOnWriteArrayList< Pd_Float > floats = receiver.get_floats ( );
     	CopyOnWriteArrayList< String > bangs = receiver.get_bangs ( );
     	CopyOnWriteArrayList< String > user_symbols = receiver.get_symbols ( );
     	int array_size;
 		for ( String symbol : user_symbols )
 		{		
 	    	for ( Pd_Message message : messages )
 	    	{
 				if ( message.get_source ( ).equals( symbol ) )
 				{
 					System.err.println ( "PURE_DATA: MESSAGE: SRC=" + message.get_source ( ) + " SYM=" + message.get_symbol ( ) );
 					for ( Object argument : message.get_arguments ( ) )
 					{
 						System.err.println ( "\tARGUMENT: " + argument );	
 					}
 				}
 	    	}
 	    	for ( Pd_Float sent_float : floats )
 	    	{
 				if ( sent_float.get_source ( ).equals( symbol ) )
 				{
 					System.err.println ( "PURE_DATA: FLOAT: SRC=" + sent_float.get_source ( ) + " NUM=" + sent_float.get_value ( ) );
 				}
 	    	}
 	    	for ( String sent_bang : bangs )
 	    	{
 				if ( sent_bang.equals( symbol ) )
 				{
 					System.err.println ( "PURE_DATA: BANG: SRC=" + sent_bang );
 				}
 	    	}
 	    	array_size = PdBase.arraySize ( symbol );
     		if ( array_size > 0 )
     		{
     			System.err.println ( "Array Name: " + symbol + " Size: " + array_size );
     			float[ ] samples = new float[ array_size ];
     			PdBase.readArray ( samples, 0, symbol, 0, array_size );
     		}
 		}
     }
 	@Override
 	public boolean init ( ) 
 	{
 		agent_name = getAgent ( ).getAgentName ( );
 
 		sampler = Pd_Audio_Server.get_instance ( );
 		receiver = Pd_Receiver.get_instance ( );
 
 		if ( parameters.get ( Pd_Constants.PATCH_ARGUMENT ) != null )
 		{
 			try 
 			{
 				patch = sampler.open_patch( parameters.get ( Pd_Constants.PATCH_ARGUMENT ) );
 				System.err.println ( "PURE_DATA: PATCH_ID=" + patch + " PATH=" + "\"" + parameters.get( Pd_Constants.PATCH_ARGUMENT ) + "\"" );
 				process_ensemble_control_messages ( );
				//process_pd_messages ( );
 			} 
 			catch ( IOException e ) 
 			{
 				e.printStackTrace ( );
 			}
 		}
 		else if ( parameters.containsKey( Pd_Constants.SUBPATCH ) )
 		{
 			subpatch = parameters.get ( Pd_Constants.SUBPATCH ) + Pd_Constants.SEPARATOR;
 			process_ensemble_control_messages ( );
			//process_pd_messages ( );
 		}
 		else
 		{
			System.err.println ( "REASONING ERROR: NO PATCH/SUBPATCH DEFINED!" );
 		}
 		return true;
 	}
 	@Override
 	public void process ( ) 
 	{
 		for ( String sensor : senses.keySet ( ) )
 		{
 			Pd_Event event = senses.get ( sensor );
 			String type = event.get_type ( );
 			Object content = event.get_content ( );
 			
 			System.err.println ( getAgent ( ).getAgentName ( ) + ":" + sensor );
 			System.err.print ( "Received Event from: " );
 			if ( type.equals( Pd_Constants.BANG ) )
 			{
 				System.err.println ( "Type: Bang\n" + "Source: " + content );
 			}
 			else if ( type.equals( Pd_Constants.MESSAGE ) )
 			{
 				String message_source = ( ( Pd_Message ) content ).get_source ( );
 				String symbol = ( ( Pd_Message ) content ).get_symbol ( );
 				Object[ ] arguments = ( ( Pd_Message ) content ).get_arguments ( );
 				System.err.println ( "Type: Message\n" + "Source: " + message_source + " Symbol: " + symbol );
 				if ( symbol.equals ( "access_kb" ) )
 				{
 					String value = getAgent ( ).getKB ( ).readFact ( ( String ) arguments[ 0 ] );
 					if ( value != null )
 					{
 						for ( Actuator actuator : actuators.values ( ) )
 						{
 							String[ ] target_agent = message_source.split ( Pd_Constants.SEPARATOR );
 							String[ ] actuator_target = actuator.getParameter ( Pd_Constants.SCOPE ).split ( Pd_Constants.SEPARATOR );
 							if ( actuator_target[ 0 ].equals ( target_agent[ 0 ] ) ||
 									actuator_target[ 0 ].equals ( Pd_Constants.GLOBAL_KEY ) )
 							{
 								Pd_Message new_message = new Pd_Message ( agent_name + Pd_Constants.SEPARATOR + actuator.getComponentName ( ), value );
 								Pd_Event pd_event = new Pd_Event ( Pd_Constants.MESSAGE, new_message );
 								try 
 								{
 									actuator_memories.get ( actuator.getComponentName ( ) ).writeMemory ( pd_event );
 									actuator.act ( );
 								} 
 								catch ( MemoryException e ) 
 								{
 									e.printStackTrace ( );
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		senses.clear ( );
 	}
 	@Override
 	protected void eventHandlerRegistered ( EventHandler event_handler ) 
 	{
 		if ( event_handler instanceof Actuator )
 		{
 			actuators.put ( event_handler.getComponentName ( ), ( Pd_Actuator ) event_handler );
 			actuator_memories.put ( event_handler.getComponentName ( ), getAgent ( ).getKB ( ).getMemory ( event_handler.getComponentName ( ) ) );
 		}
 		else if ( event_handler instanceof Sensor )
 		{
 			event_handler.registerListener( this );
 			sensors.put ( event_handler.getComponentName ( ), ( Pd_Sensor ) event_handler );
 			sensor_memories.put ( event_handler.getComponentName ( ), getAgent ( ).getKB ( ).getMemory ( event_handler.getComponentName ( ) ) );
 		}
 	}
 	public void newSense ( Sensor source, double instant, double duration ) throws Exception 
 	{
 		Memory source_memory = sensor_memories.get( source.getComponentName ( ) );
 		Pd_Event event = ( Pd_Event ) source_memory.readMemory ( instant, duration, TimeUnit.SECONDS );
 		
 		senses.put ( source.getComponentName ( ), event );
 
 	}
 }
