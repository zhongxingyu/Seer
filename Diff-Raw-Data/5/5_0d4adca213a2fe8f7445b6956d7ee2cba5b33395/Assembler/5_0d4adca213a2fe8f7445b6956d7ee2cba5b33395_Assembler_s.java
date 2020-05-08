 package hack.bp.assembler;
 
 import java.io.*;
 
 /** Assembler.java **********************************************************************
  * 	This is the implementation of the Hack assembler.
  * 
  * 	@author bp
  * 
  * 	@changes
  * 	0.1 -	Initial implementation (cannot handle C_COMMANDs, L_COMMANDs). Single-pass,
  * 			will translate A_COMMANDs. -bp
  * 	0.2	-	Single pass. Can handle C_COMMANDs.
  *
  ***************************************************************************************/
 public class Assembler 
 {
 	/** main() **************************************************************************
 	 *  Fires off the assembler with run(). This function makes sure that an argument 
 	 *  has been passed-in before running the assembler.
 	 ***********************************************************************************/
 	public static void main( String[] args ) 
 	{
 		// Check if the file is passed in
 		if( args.length > 0 )
 		{
 			if( args[ 0 ].endsWith( ".asm" ) )
 				run( args[ 0 ] );
 			else
 				System.out.println( "Usage: <program> <fileName> " +
 					"\n\t -Can only use file with .asm extension.");
 		}
 		else
 			System.out.println( "Usage: <program> <fileName> " +
 									"\n\t -Enter an .asm file.");
 	}
 	
 	/** run() ***************************************************************************
 	 *  This is the implementation of the assembler. It contains a timer accurate to
 	 *  the nano-second to measure the assembler's performance.
 	 ***********************************************************************************/
 	public static void run( String fileName )
 	{
 		// Start the timer for run()
 		long timerStart = System.nanoTime();
 		
 		// Initialize the parser and default output to write to file
 		Parser parser = new Parser( fileName );
 		String output = "";
 		
 		// Initialize the address size (defined in Hack machine code spec)
 		final int ADDRESS_LENGTH = 15;
 			
 		// Start reading the file
 		while( parser.hasMoreCommands() )
 		{
 			// Grab the next command and store it as the current command
 			parser.advance();
 			
 			// Handle A_COMMAND
 			if( parser.commandType() == Parser.Commands.A_COMMAND )
 			{
 				// Set the current address of the instruction
 				parser.setCurrentLineNumber( parser.getCurrentLineNumber() + 1 );
 				
 				// Initialize the variable to hold the calculated binary string
 				String tempCode = "";
 				
 				// Calculate and store the binary string
 				int decAddress = Integer.parseInt( parser.symbol() );
 				tempCode = Integer.toBinaryString( decAddress );
 				
 				// Format the binary string to meet machine code specs
 				if( ( tempCode.length() < ADDRESS_LENGTH ) && ( tempCode.length() > 0 ) )
 				{
 					int paddingCount = ADDRESS_LENGTH - tempCode.length();
 					
					// append the A_COMMAND prefix
 					output += "0";
 				
 					// Pad the output to conform to the machine code specs
 					for( int i = 0; i < paddingCount; i++ )
 						output += "0";
 					
 					// Create the machine code and start new line for next code
 					output += tempCode;
 					output += "\n";
 				}
 			}
 			
 			// Handle C_COMMAND
 			if( parser.commandType() == Parser.Commands.C_COMMAND )
 			{
 				// Initialize the mnemonic translator
 				Code code = new Code();
 				
 				// Set the current address of the instruction
 				parser.setCurrentLineNumber( parser.getCurrentLineNumber() + 1 );
 				
				// Append the opcode
 				output += "111";
 				
 				// Construct the machine code
 				output += code.comp( parser.comp() ) + 
 						  code.dest( parser.dest() ) + 
 						  code.jump( parser.jump() );
 				
 				output += "\n";	
 			}
 		}
 		
 		// Write to file (<filename-minus-extension>.hack)
 		try
 		{
 			// Create buffered writer
 			FileWriter fileStream = new FileWriter( fileName.replace( ".asm", ".hack" ) );
 			BufferedWriter out = new BufferedWriter( fileStream );
 			
 			// Write to file and then close the writer
 			out.write( output, 0, output.length() );
 			out.close();  
 		}
 		catch (Exception e)
 		{
 			System.err.println( "Error: " + e.getMessage() );
 			e.printStackTrace();
 			System.exit( 1 );
 		}
 		
 		// Print the compilation statistics on screen (timer and success msg)
 		long timerEnd = System.nanoTime();
 		System.out.println( "Assembly completed! (elapsed time: " +
 							( timerEnd - timerStart ) + "ns)\n");
 	}
 }
