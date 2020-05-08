 package edu.selu.android.classygames;
 
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.selu.android.classygames.utilities.GCMUtilities;
 import edu.selu.android.classygames.utilities.GameUtilities;
 import edu.selu.android.classygames.utilities.Utilities;
 
 
 /**
  * Servlet implementation class NewGame
  */
 public class NewGame extends HttpServlet
 {
 
 
 	private final static byte RUN_STATUS_NO_ERROR = 0;
 	private final static byte RUN_STATUS_UNSUPPORTED_ENCODING = 1;
 	private final static byte RUN_STATUS_NO_SUCH_ALGORITHM = 2;
 
 	private final static long serialVersionUID = 1L;
 
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public NewGame()
 	{
 		super();
 	}
 
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(final HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		response.setContentType(Utilities.CONTENT_TYPE_JSON);
 		PrintWriter printWriter = response.getWriter();
 		printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_NOT_DETECTED));
 	}
 
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(final HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		response.setContentType(Utilities.CONTENT_TYPE_JSON);
 		PrintWriter printWriter = response.getWriter();
 
 		final String user_challenged_parameter = request.getParameter(Utilities.POST_DATA_USER_CHALLENGED);
 		final String user_challenged_name = request.getParameter(Utilities.POST_DATA_NAME);
 		final String user_creator_parameter = request.getParameter(Utilities.POST_DATA_USER_CREATOR);
 		String board = request.getParameter(Utilities.POST_DATA_BOARD);
 
 		if (user_challenged_parameter == null || user_challenged_parameter.isEmpty() || user_challenged_name == null || user_challenged_name.isEmpty()
 			|| user_creator_parameter == null || user_creator_parameter.isEmpty() || board == null || board.isEmpty())
 		// check for invalid inputs
 		{
 			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_MALFORMED));
 		}
 		else
 		{
 			final Long user_challenged = Long.valueOf(user_challenged_parameter);
 			final Long user_creator = Long.valueOf(user_creator_parameter);
 
 			if (user_challenged.longValue() < 0 || user_creator.longValue() < 0)
 			// check for invalid inputs
 			{
 				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_MALFORMED));
 			}
 			else if (GameUtilities.checkBoardValidityAndStatus(board) == Utilities.BOARD_NEW_GAME)
 			{
 				Connection sqlConnection = null;
 				PreparedStatement sqlStatement = null;
 
 				try
 				{
 					sqlConnection = Utilities.getSQLConnection();
 
 					if (Utilities.ensureUserExistsInDatabase(sqlConnection, user_challenged, user_challenged_name))
 					{
 						if (Byte.valueOf(GameUtilities.checkBoardValidityAndStatus(board)) == Utilities.BOARD_NEW_GAME)
 						{
 							board = GameUtilities.flipTeams(board);
 
 							byte runStatus = RUN_STATUS_NO_ERROR;
 							String digest = null;
 
 							for (boolean continueToRun = true; continueToRun; )
 							// This loop does a ton of stuff. First a digest is created to be used as this new game's game ID. Then we
 							// check to see if this ID is already in the database. If the ID is already in the database, we check to
 							// see if the game it belongs to is a finished game. If it is a finished game, then we can safely replace
 							// the data from that game with the data from our new game. If it is not a finished game, this whole loop
 							// will have to restart as we're going to have to create a new ID (we somehow managed to create an SHA-256
 							// digest that clashed with another one. The odds of this happening are extremely unlikely but we still
 							// have to check for it.)
 							// But back to the ifs and such: if we created an ID that does not already exist in the database, then we
 							// can simply insert our new game data safely into it.
 							{
 								// prepare a String to hold a digest in
 								digest = null;
 
 								try
 								{
 									// create a digest to use as the Game ID
 									digest = createDigest
 									(
 										user_challenged.toString().getBytes(Utilities.UTF8),
 										user_creator.toString().getBytes(Utilities.UTF8),
 										board.toString().getBytes(Utilities.UTF8)
 									);
 								}
 								catch (final NoSuchAlgorithmException e)
 								// the algorithm we tried to use to create a digest was invalid
 								{
 									runStatus = RUN_STATUS_NO_SUCH_ALGORITHM;
 								}
 								catch (final UnsupportedEncodingException e)
 								// the character set we tried to use in digest creation was invalid
 								{
 									runStatus = RUN_STATUS_UNSUPPORTED_ENCODING;
 								}
 
 								if (runStatus != RUN_STATUS_NO_ERROR || digest == null || digest.isEmpty())
 								// check to see if we encountered any of the exceptions above or if our digest is broken
 								{
 									continueToRun = false;
 								}
 								else
 								// no exceptions were encountered. let's continue. Once past this point we no longer have to check on or
 								// modify the runStatus variable
 								{
 									// prepare a SQL statement to be run on the database
 									String sqlStatementString = "SELECT " + Utilities.DATABASE_TABLE_GAMES_COLUMN_FINISHED + " FROM " + Utilities.DATABASE_TABLE_USERS + " WHERE " + Utilities.DATABASE_TABLE_GAMES_COLUMN_ID + " = ?";
 									sqlStatement = sqlConnection.prepareStatement(sqlStatementString);
 
 									// prevent SQL injection by inserting data this way
 									sqlStatement.setString(1, digest);
 
 									// run the SQL statement and acquire any return information
 									final ResultSet sqlResult = sqlStatement.executeQuery();
 
 									if (sqlResult.next())
 									// the digest we created to use as an ID already exists in the games table
 									{
 										if (sqlResult.getByte(Utilities.DATABASE_TABLE_GAMES_COLUMN_FINISHED) == Utilities.DATABASE_TABLE_GAMES_FINISHED_TRUE)
 										// Game with the digest we created already exists, AND has been finished. Because of this, we can
 										// safely replace that game's data with our new game's data
 										{
 											// prepare a SQL statement to be run on the database
 											sqlStatementString = "UPDATE " + Utilities.DATABASE_TABLE_GAMES + " SET " + Utilities.DATABASE_TABLE_GAMES_COLUMN_USER_CREATOR + " = ?, " + Utilities.DATABASE_TABLE_GAMES_COLUMN_USER_CHALLENGED + " = ?, " + Utilities.DATABASE_TABLE_GAMES_COLUMN_BOARD + " = ?, " + Utilities.DATABASE_TABLE_GAMES_COLUMN_TURN + " = ?, " + Utilities.DATABASE_TABLE_GAMES_COLUMN_FINISHED + " = ? WHERE " + Utilities.DATABASE_TABLE_GAMES_COLUMN_ID + " = ?";
 											sqlStatement = sqlConnection.prepareStatement(sqlStatementString);
 
 											// prevent SQL injection by inserting data this way
 											sqlStatement.setLong(1, user_creator);
 											sqlStatement.setLong(2, user_challenged);
 											sqlStatement.setString(3, board);
 											sqlStatement.setByte(4, Utilities.DATABASE_TABLE_GAMES_TURN_CHALLENGED);
 											sqlStatement.setByte(5, Utilities.DATABASE_TABLE_GAMES_FINISHED_FALSE);
 
 											// run the SQL statement
 											sqlStatement.executeUpdate();
 
 											continueToRun = false;
 										}
 									}
 									else
 									// the digest that we created to use as an ID DOES NOT already exist in the games table. We we can now
 									// just simply insert this new game's data into the table
 									{
 										// prepare a SQL statement to be run on the database
 										sqlStatementString = "INSERT INTO " + Utilities.DATABASE_TABLE_GAMES + " " + Utilities.DATABASE_TABLE_GAMES_FORMAT + " " + Utilities.DATABASE_TABLE_GAMES_VALUES;
 										sqlStatement = sqlConnection.prepareStatement(sqlStatementString);
 
 										// prevent SQL injection by inserting data this way
 										sqlStatement.setString(1, digest);
 										sqlStatement.setLong(2, user_creator.longValue());
 										sqlStatement.setLong(3, user_challenged.longValue());
 										sqlStatement.setString(4, board);
 										sqlStatement.setByte(5, Utilities.DATABASE_TABLE_GAMES_TURN_CHALLENGED);
										sqlStatement.setInt(6, Utilities.DATABASE_TABLE_GAMES_FINISHED_FALSE);
 
 										// run the SQL statement
 										sqlStatement.executeUpdate();
 
 										continueToRun = false;
 									}
 								}
 							}
 
 							switch (runStatus)
 							// we may have hit an error in the above loop
 							{
 								case RUN_STATUS_NO_SUCH_ALGORITHM:
 									printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_COULD_NOT_CREATE_GAME_ID));
 									break;
 
 								case RUN_STATUS_UNSUPPORTED_ENCODING:
 									printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_COULD_NOT_CREATE_GAME_ID));
 									break;
 
 								default:
 									GCMUtilities.sendMessage(sqlConnection, digest, user_challenged.longValue(), user_challenged_name, Utilities.POST_DATA_TYPE_NEW_GAME);
 									printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_GAME_ADDED_TO_DATABASE));
 									break;
 							}
 						}
 						else
 						{
 							printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_BOARD_INVALID));
 						}
 					}
 					else
 					{
 						printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_INVALID_CHALLENGER));
 					}
 				}
 				catch (final SQLException e)
 				{
 					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_CONNECT));
 				}
 				finally
 				{
 					Utilities.closeSQL(sqlConnection, sqlStatement);
 				}
 			}
 			else
 			{
 				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_BOARD_INVALID));
 			}
 		}
 	}
 
 
 	private String createDigest(final byte[] user_challenged_bytes, final byte[] user_creator_bytes, final byte[] board_bytes) throws NoSuchAlgorithmException, UnsupportedEncodingException
 	// huge hash creation algorithm magic below
 	{
 		// create a digest to use as the Game ID. We are going to be using the Utilities.MESSAGE_DIGEST_ALGORITHM as our
 		// hash generation algorithm. At the time of this writing it's SHA-256, but plenty more algorithms are available.
 		final MessageDigest digest = MessageDigest.getInstance(Utilities.MESSAGE_DIGEST_ALGORITHM);
 
 		// Build the digest. As can be seen, we're using a bunch of different variables here. The more data we use here
 		// the better our digest will be.
 		digest.update(user_challenged_bytes);
 		digest.update(user_creator_bytes);
 		digest.update(board_bytes);
 		digest.update(new Integer(Utilities.getRandom().nextInt()).toString().getBytes(Utilities.UTF8));
 
 		StringBuilder digestBuilder = new StringBuilder(new BigInteger(digest.digest()).abs().toString(Utilities.MESSAGE_DIGEST_RADIX));
 
 		for (int nibble = 0; digestBuilder.length() < Utilities.MESSAGE_DIGEST_LENGTH; )
 		// we want a digest that's Utilities.MESSAGE_DIGEST_LENGTH characters in length. At the time of this writing, we
 		// are aiming for 64 characters long. Sometimes the digest algorithm will give us a bit less than that. So here
 		// we're making up for that shortcoming by continuously adding random characters to the digest until we get 64
 		{
 			do
 			// we don't want a negative number. keep generating random ints until we get one that's positive
 			{
 				// don't allow the random number we've generated to be above 15
 				nibble = Utilities.getRandom().nextInt() % 16;
 			}
 			while (nibble < 0);
 
 			switch (nibble)
 			// add a hexadecimal character onto the end of the StringBuilder
 			{
 				case 0:
 					digestBuilder.append('0');
 					break;
 
 				case 1:
 					digestBuilder.append('1');
 					break;
 
 				case 2:
 					digestBuilder.append('2');
 					break;
 
 				case 3:
 					digestBuilder.append('3');
 					break;
 
 				case 4:
 					digestBuilder.append('4');
 					break;
 
 				case 5:
 					digestBuilder.append('5');
 					break;
 
 				case 6:
 					digestBuilder.append('6');
 					break;
 
 				case 7:
 					digestBuilder.append('7');
 					break;
 
 				case 8:
 					digestBuilder.append('8');
 					break;
 
 				case 9:
 					digestBuilder.append('9');
 					break;
 
 				case 10:
 					digestBuilder.append('a');
 					break;
 
 				case 11:
 					digestBuilder.append('b');
 					break;
 
 				case 12:
 					digestBuilder.append('c');
 					break;
 
 				case 13:
 					digestBuilder.append('d');
 					break;
 
 				case 14:
 					digestBuilder.append('e');
 					break;
 
 				default:
 					digestBuilder.append('f');
 					break;
 			}
 		}
 
 		while (digestBuilder.length() > Utilities.MESSAGE_DIGEST_LENGTH)
 		// ensure that our digest is only MESSAGE_DIGEST_LENGTH characters long. At the time of this
 		// writing that value is 64. This will delete the very last character from the StringBuilder
 		// one at a time until we're at a length of 64
 		{
 			digestBuilder.deleteCharAt(digestBuilder.length() - 1);
 		}
 
 		return digestBuilder.toString();
 	}
 
 
 }
