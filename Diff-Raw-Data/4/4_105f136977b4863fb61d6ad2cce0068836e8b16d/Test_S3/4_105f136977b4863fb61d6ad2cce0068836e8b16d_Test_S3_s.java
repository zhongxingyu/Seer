 public class Test_S3
 {
 	private static Tape tape = null;
 
 	private static void testMainMachines ()
 	{
 		try
 		{
 			final String cTestTapes[] = {"101 10", "101 0", "0 10", "0 0", "10 10", "10 11", "100 10"};
 			final String cMethods[] = {"add", "substract", "multiply", "divide"};
 
 			for (int i = 0; i < cTestTapes.length; i++)
 			{
 				String cTape = cTestTapes[i];
 				String cTapes[] = cTape.split (" ");
 				int iTapes[] = {Integer.parseInt(cTapes[0], 2), Integer.parseInt (cTapes[1], 2)};
 				int iOp[] = {iTapes[0] + iTapes[1],	// add
 						iTapes[0] - iTapes[1],		// substract
 						iTapes[0] * iTapes[1],		// multiply
 						iTapes[1] != 0 ? iTapes[0] / iTapes[1] : 0,		// divide (/!\ we can't x/0)
 						iTapes[1] != 0 ? iTapes[0] % iTapes[1] : 0};	// modulo (/!\ we can't x%0)
 				if (iOp[1] < 0) // substract: min 0
 					iOp[1] = 0;
 				System.out.println ("\n" + cTape + " => base10: " + iTapes[0] + " " + iTapes[1]);
 
 				for (int j = 0; j < cMethods.length; j++)
 				{
 					String cMeth = cMethods[j];
 					System.out.print (cMeth + ": ");
 
 					if (i == 1 && j == 3)
 					{
 						System.out.println ("infinite loop \t => ERROR");
 						continue; // avoid inf. loop => from calculability: we can't detect a inf. loop :)
 					}
 
 					tape = new TapeA_B (cTape);
 					boolean bException = false;
 					try
 					{
 						MTA_B.class.getDeclaredMethod (cMeth, new Class[]{Tape.class}).invoke (null, tape);
 					} catch (java.lang.reflect.InvocationTargetException e)
 					{
 						System.out.print ("Exception... ");
 						bException = true;
 					}
 					String cResult = tape.toString ();
 					String cExpectedResult = Integer.toString (iOp[j], 2);
 
 					if (j == 3) // with modulo
 					{
 						if (bException && iTapes[1] == 0) // exception only if there is a division by 0
 						{
 							System.out.println ("\t => Exception OK");
 							continue;
 						}
 						System.out.print (cResult + " expected: " + cExpectedResult + " ");
 
 						// cResult contains at least '[ ]'
 						int iPosSpace = cResult.indexOf (' ');
 						String cResultBinaryDiv = cResult.substring (0, iPosSpace); // result
 						int iPosBracket = cResult.indexOf ('[');
 						String cResultBinaryMod = null;
 						if (iPosBracket >= iPosSpace)
 							cResultBinaryMod = cResult.substring (iPosSpace + 1, cResult.indexOf ('[')); // reste
 
 						String cExpectedResultMod = Integer.toString (iOp[j+1], 2);
 						System.out.print (cExpectedResultMod + " => base10: "
 							+ iOp[j] + " " + iOp[j+1]);
 						if (cExpectedResult.compareTo (cResultBinaryDiv) != 0 || 
 									cExpectedResultMod.compareTo (cResultBinaryMod) != 0)
 							System.out.println ("\t => ERROR");
 						else
 							System.out.println ("\t => OK");
 					}
 					else
 					{
 						System.out.print (cResult + " expected: " + cExpectedResult + " ");
 						String cResultBinary = cResult.substring (0, cResult.indexOf ('[')); // before the head
 						System.out.print ("=> base10: " + iOp[j]);
 						if (cExpectedResult.compareTo (cResultBinary) != 0)
 							System.out.println ("\t => ERROR");
 						else
 							System.out.println ("\t => OK");
 					}
 				}
 			}
 		} catch (Exception e) // if exception in TapeA_B
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private static void testSubMachinesConvToUnary () throws Exception
 	{
 		String cIn, cOut, cExpected;
 		int iExpected;
 
 		String cInputsConvToUnary[] = {"101", "0", "00"}; // should receive valid binary: only 1 or 0 (not only "B")
 		for (int i = 0; i < cInputsConvToUnary.length; i++)
 		{
 			String cInput = cInputsConvToUnary[i];
 
 			// convertToUnaryLeft
 			tape = new TapeA_B (cInput);
 			cIn = tape.toString ();
 			MTA_B.convertToUnaryLeft (tape);
 			cOut = tape.toString ();
 			iExpected = Integer.parseInt (cInput, 2); // cInput: binary
 			cExpected = "";
 			while (iExpected-- > 0)
 				cExpected += "1"; // toUnary: 1 x iExpected
 			cExpected += "[ ]"; // at the end
 			printSubMachineTest ("convertToUnaryLeft", cIn, cOut, cExpected);
 
 			// convertToUnaryRight
 			tape = new TapeA_B (cInput);
 			MTA_B.findFirstBlankOnTheLeft (tape); //[ ]00
 			cIn = tape.toString ();
 			MTA_B.convertToUnaryRight (tape);
 			cOut = tape.toString ();
 			iExpected = Integer.parseInt (cInput, 2); // cInput: binary
 			cExpected = "[ ]"; // at the beginning
 			while (iExpected-- > 0)
 				cExpected += "1"; // toUnary: 1 x iExpected
 			printSubMachineTest ("convertToUnaryRight", cIn, cOut, cExpected);
 		}
 	}
 
 	private static void testSubMachinesConvToBinary () throws Exception
 	{
 		String cIn, cOut, cExpected;
 
 		String cInputsConvToBinar[] = {"11111", "1", ""}; // should receive valid unary: only 1 or 'B'
 		for (int i = 0; i < cInputsConvToBinar.length; i++)
 		{
 			String cInput = cInputsConvToBinar[i];
 
 			// convertToBinaryLeft
 			tape = new TapeA_B (cInput);
 			cIn = tape.toString ();
 			MTA_B.convertToBinaryLeft (tape);
 			cOut = tape.toString ();
 			cExpected = Integer.toBinaryString (cInput.length ()) + "[ ]"; // cInput = unary => number of chars
 			printSubMachineTest ("convertToBinaryLeft", cIn, cOut, cExpected);
 
 			// convertToBinaryRight
 			tape = new TapeA_B (cInput);
 			MTA_B.findFirstBlankOnTheLeft (tape); //[ ]00
 			cIn = tape.toString ();
 			MTA_B.convertToBinaryRight (tape);
 			cOut = tape.toString ();
 			cExpected = "[ ]" + Integer.toBinaryString (cInput.length ()); // cInput = unary => number of chars
 			printSubMachineTest ("convertToBinaryRight", cIn, cOut, cExpected);
 		}
 	}
 
 	private static void testSubMachinesErase () throws Exception
 	{
 		String cIn, cOut, cExpected;
 
 		String cInputs[] = {"1111", "11 11", ""};
 		int i;
 		for (String cInput : cInputs)
 		{
 			tape = new TapeA_B (cInput);
 			cIn = tape.toString ();
 			MTA_B.findFirstBlankOnTheLeft (tape);
 
 			cExpected = "[ ]";
 			if ((i = cInput.lastIndexOf (' ')) > -1) // two numbers, we only want to remove the first one
 			{
 				MTA_B.findFirstBlankOnTheLeft (tape); // more interesting to move left
 				for (int j = 0; j <= i; j++)
 					cExpected += " ";
 				cExpected += cInput.substring (i+1);
 			}
 			MTA_B.eraseRight (tape);
 			cOut = tape.toString ();
 			printSubMachineTest ("eraseRight", cIn, cOut, cExpected);
 		}
 	}
 
 	private static void testSubMachinesCopy () throws Exception
 	{
 		String cIn, cOut, cExpected;
 
 		String cInputs[] = {"1111", "11 11", ""};
 		int i;
 		for (String cInput : cInputs)
 		{ // idem right
 			tape = new TapeA_B (cInput);
 			cIn = tape.toString ();
 			MTA_B.copyLeftOfLeftSequence (tape);
 			cOut = tape.toString ();
 			if ((i = cInput.lastIndexOf (' ')) > -1) // two numbers, we only want to remove the first one
 				cExpected = cInput.substring (i+1) + cIn;
 			else
 				cExpected = cInput + (cInput.length () > 0 ? " " : "") + cIn;
 			printSubMachineTest ("copyLeftOfLeftSequence", cIn, cOut, cExpected);
 		}
 	}
 
 	private static void testSubMachinesShift () throws Exception
 	{
 		String cIn, cOut, cExpected;
 
 		String cInputs[] = {"1111 1", "11 11", "", "111", "11 111"};
 		int i;
 		for (String cInput : cInputs)
 		{
 			tape = new TapeA_B (cInput);
 			MTA_B.findFirstBlankOnTheLeft (tape);
 			cIn = tape.toString ();
 			MTA_B.shiftSubstract (tape);
 			cOut = tape.toString ();
 
 			if ((i = cInput.lastIndexOf (' ')) > -1) // two numbers, we only want to remove the first one
 			{
 				String cInputSplit[] = cInput.split (" ");
 				int iLengths[] = new int [2]; // only 2...
 				iLengths[1] = cInputSplit[1].length ();
 				iLengths[0] = cInputSplit[0].length() - iLengths[1];
 				cExpected = "";
 				while (iLengths[0]-- > 0)
 					cExpected += "1";
 				cExpected += cIn.substring (i);
 			}
 			else
 				cExpected = cIn;
 			printSubMachineTest ("shiftSubstract", cIn, cOut, cExpected);
 		}
 	}
 
 	private static void testSubMachines ()
 	{
 		try
 		{
 			/* no need to test addOneBlankAfterTheFirstBlankOnTheLeft,
 			 * findFirstBlankOnTheLeft, findFirstBlankOnTheRight and
 			 * incrementLeft: very basic...
 			 */
 
 			//_____________________________________ TO UNARY
 			testSubMachinesConvToUnary ();
 			System.out.println ();
 
 			//_____________________________________ TO BINARY
 			testSubMachinesConvToBinary ();
 			System.out.println ();
 
 			//_____________________________________ ERASE
 			testSubMachinesErase ();
 			System.out.println ();
 
 			//_____________________________________ Copy
 			testSubMachinesCopy ();
 			System.out.println ();
 
			//_____________________________________ Copy
 			testSubMachinesShift ();
 			System.out.println ();
 			
 		} catch (Exception e) // if exception in TapeA_B
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private static void printSubMachineTest (String cMeth, String cIn, String cOut, String cExpected)
 	{
 		boolean bSame = cOut.compareTo (cExpected) == 0;
 		System.out.print (cMeth + ":\tIn: '" + cIn + "' ;\tOut: '" + cOut + "'\t=> expected: '" + cExpected + "'\t=> ");
 		if (bSame)
 			System.out.println ("OK");
 		else
 			System.out.println ("ERROR");
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main (String[] args)
 	{
 		System.out.println ("Test1: machines");
 		testMainMachines ();
 
 		System.out.println ("\n\nTest 2: sub-machines\n");
 		testSubMachines ();
 	}
 }

