 package edu.selu.android.classygames.games;
 
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 /**
  * A generic Board class. All games need to have their own Board class that
  * extends from this one.
  */
 public abstract class GenericBoard
 {
 
 
 	protected final static byte BOARD_INVALID = -1;
 	protected final static byte BOARD_NEW_GAME = 1;
 	protected final static byte BOARD_NEW_MOVE = 2;
 	protected final static byte BOARD_WIN = 3;
 
 
 
 
 	/**
 	 * The number of positions the board has horizontally. This can be thought
 	 * of as the board's X limit.
 	 */
 	protected byte lengthHorizontal;
 
 
 	/**
 	 * The number of positions that the board has vertically. This can be
 	 * thought of as the board's Y limit.
 	 */
 	protected byte lengthVertical;
 
 
 	/**
 	 * JSONObject that represents the game board. This can be null.
 	 */
 	private JSONObject boardJSON;
 
 
 	/**
 	 * This board's positions. This is a two dimensional array that should be
 	 * accessed as [X][Y]. So a position on the board that is (5, 3) - (X = 5
 	 * and Y = 3), would be [5][3].
 	 */
 	private Position[][] positions;
 
 
 	/**
 	 * Boolean indicating whether or not this board is locked. If the board is
 	 * locked then pieces can't be moved.
 	 */
 	protected boolean isBoardLocked;
 
 
 
 
 	/**
 	 * Creates the Board object. Initializes all of the board's positions and
 	 * sets them to this board's defaults.
 	 * 
 	 * @param lengthHorizontal
 	 * The <strong>X length</strong> of the game board.
 	 * 
 	 * @param lengthVertical
 	 * The <strong>Y length</strong> of the game board.
 	 * 
 	 * @throws JSONException
 	 * If a glitch or something happened while trying to create some JSON data
 	 * then this JSONException will be thrown. When using this particular
 	 * constructor this should never happen.
 	 */
 	protected GenericBoard(final byte lengthHorizontal, final byte lengthVertical) throws JSONException
 	{
 		this.lengthHorizontal = lengthHorizontal;
 		this.lengthVertical = lengthVertical;
 
 		reset();
 	}
 
 
 	/**
 	 * Creates the Board object. Initializes all of the board's positions and
 	 * sets them to the data found in the boardJSON JSONObject.
 	 * 
 	 * @param lengthHorizontal
 	 * The <strong>X length</strong> of the game board.
 	 * 
 	 * @param lengthVertical
 	 * The <strong>Y length</strong> of the game board.
 	 * 
 	 * @param boardJSON
 	 * JSONObject that represents the board.
 	 * 
 	 * @throws JSONException
 	 * If a glitch or something happened while trying to create some JSON data
 	 * then this JSONException will be thrown.
 	 */
 	protected GenericBoard(final byte lengthHorizontal, final byte lengthVertical, final JSONObject boardJSON) throws JSONException
 	{
 		this.lengthHorizontal = lengthHorizontal;
 		this.lengthVertical = lengthVertical;
 		this.boardJSON = boardJSON;
 
 		reset();
 	}
 
 
 	/**
 	 * Flips both team's coordinate locations on the board.
 	 */
 	public void flipTeams()
 	{
 		final byte halfLengthVertical = (byte) (lengthVertical / 2);
 
 		for (byte x = 0; x < lengthHorizontal; ++x)
 		{
 			for (byte y = 0; y < halfLengthVertical; ++y)
 			{
 				final Position position = getPosition(x, y);
 				final Position positionInverse = getPosition(lengthHorizontal - 1 - x, lengthVertical - 1 - y);
 
 				final GenericPiece piece = position.getPiece();
 				final GenericPiece pieceInverse = positionInverse.getPiece();
 
 				position.setPiece(pieceInverse);
 				positionInverse.setPiece(piece);
 			}
 		}
 	}
 
 
 	/**
 	 * @return
 	 * Returns whether or not the board is currently locked.
 	 */
 	public boolean getIsBoardLocked()
 	{
 		return isBoardLocked;
 	}
 
 
 	/**
 	 * @return
 	 * Returns the number of positions that the board has horizontally.
 	 */
 	public byte getLengthHorizontal()
 	{
 		return lengthHorizontal;
 	}
 
 
 	/**
 	 * @return
 	 * Returns the number of positions that the board has vertically.
 	 */
 	public byte getLengthVertical()
 	{
 		return lengthVertical;
 	}
 
 
 	/**
 	 * Returns a specific Position on the Board. Think of X and Y as an ordered
 	 * pair. Note that <strong>bound checking is not performed</strong>, so if
 	 * you do something stupid you could get an ArrayIndexOutOfBoundsException.
 	 * 
 	 * @param x
 	 * The X coordinate for the Position that you want.
 	 * 
 	 * @param y
 	 * The Y coordinate for the Position that you want.
 	 * 
 	 * @return
 	 * The Position as specified by the X and Y parameters.
 	 */
 	public Position getPosition(final byte x, final byte y)
 	{
 		return positions[x][y];
 	}
 
 
 	/**
 	 * Returns a specific Position on the Board. Think of X and Y as an ordered
 	 * pair. Note that <strong>bound checking is not performed</strong>, so if
 	 * you do something stupid you could get an ArrayIndexOutOfBoundsException.
 	 * 
 	 * @param x
 	 * The X coordinate for the Position that you want.
 	 * 
 	 * @param y
 	 * The Y coordinate for the Position that you want.
 	 * 
 	 * @return
 	 * The Position as specified by the X and Y parameters.
 	 */
 	public Position getPosition(final int x, final int y)
 	{
 		return getPosition((byte) x, (byte) y);
 	}
 
 
 	/**
 	 * Returns a specific Position on the Board. Note that <strong>bound
 	 * checking is not performed</strong>, so if you do something stupid you
 	 * could get an ArrayIndexOutOfBoundsException.
 	 * 
 	 * @param coordinate
 	 * The single Coordinate object containing the X and Y positions.
 	 * 
 	 * @return
 	 * The Position as specified by the Coordinate's X and Y positions.
 	 */
 	public Position getPosition(final Coordinate coordinate)
 	{
 		return getPosition(coordinate.getX(), coordinate.getY());
 	}
 
 
 	/**
 	 * Initializes the game board using data from the boardJSON variable.
 	 * 
 	 * @throws JSONException
 	 * If a glitch or something happened while trying to create some JSON data
 	 * then this JSONException will be thrown.
 	 */
 	private void initializeBoardFromJSON() throws JSONException
 	{
 		final JSONArray teams = boardJSON.getJSONObject("board").getJSONArray("teams");
		initializeTeamFromJSON(teams.getJSONArray(0), GenericPiece.TEAM_OPPONENT);
		initializeTeamFromJSON(teams.getJSONArray(1), GenericPiece.TEAM_PLAYER);
 	}
 
 
 	/**
 	 * Initializes all of this board's positions.
 	 */
 	private void initializePositions()
 	{
 		positions = new Position[lengthHorizontal][lengthVertical];
 
 		for (byte x = 0; x < lengthHorizontal; ++x)
 		{
 			for (byte y = 0; y < lengthVertical; ++y)
 			{
 				positions[x][y] = new Position(x, y);
 			}
 		}
 	}
 
 
 	/**
 	 * Creates a team of pieces from the given JSONArray.
 	 * 
 	 * @param team
 	 * JSONArray of all of this team's pieces.
 	 * 
 	 * @param whichTeam
 	 * A byte that specifies which team these pieces belong to.
 	 * 
 	 * @throws JSONException
 	 * If a glitch or something happened while trying to create this JSONObject
 	 * then a JSONException will be thrown.
 	 */
 	private void initializeTeamFromJSON(final JSONArray team, final byte whichTeam) throws JSONException
 	{
 		for (int i = 0; i < team.length(); ++i)
 		{
 			final JSONObject piece = team.getJSONObject(i);
 
 			final JSONArray coordinates = piece.getJSONArray("coordinate");
 			final Coordinate coordinate = new Coordinate(coordinates.getInt(0), coordinates.getInt(1));
 
 			final int type = piece.getInt("type");
 
 			final GenericPiece genericPiece = buildPiece(whichTeam, type);
 			getPosition(coordinate).setPiece(genericPiece);
 		}
 	}
 
 
 	/**
 	 * Checks to see if a given position is actually located on a real,
 	 * existing, position on the board.
 	 * 
 	 * @param x
 	 * The X position to be checked.
 	 * 
 	 * @param y
 	 * The Y position to be checked.
 	 * 
 	 * @return
 	 * True if the X, Y position passed in does exist.
 	 */
 	public boolean isPositionValid(final byte x, final byte y)
 	{
 		return x >= 0 && x < lengthHorizontal && y >= 0 && y < lengthVertical;
 	}
 
 
 	/**
 	 * Checks to see if a given position is actually located on a real,
 	 * existing, position on the board.
 	 * 
 	 * @param x
 	 * The X position to be checked.
 	 * 
 	 * @param y
 	 * The Y position to be checked.
 	 * 
 	 * @return
 	 * True if the X, Y position passed in does exist.
 	 */
 	public boolean isPositionValid(final int x, final int y)
 	{
 		return isPositionValid((byte) x, (byte) y);
 	}
 
 
 	/**
 	 * Checks to see if a given position is actually located on a real,
 	 * existing, position on the board.
 	 * 
 	 * @param coordinate
 	 * The X and Y positions to be checked.
 	 * 
 	 * @return
 	 * True if the X, Y position passed in does exist.
 	 */
 	public boolean isPositionValid(final Coordinate coordinate)
 	{
 		return isPositionValid(coordinate.getX(), coordinate.getY());
 	}
 
 
 	/**
 	 * Creates a JSON representation of this GenericBoard object. The
 	 * JSONObject that is created here <strong>is not stored</strong>, so
 	 * please be sure to only run this method when you really need to okay? :)
 	 * 
 	 * @return
 	 * Returns a JSONObject that represents this GenericBoard object.
 	 * 
 	 * @throws JSONException
 	 * If a glitch or something happened while trying to create this JSONObject
 	 * then a JSONException will be thrown.
 	 */
 	public JSONObject makeJSON() throws JSONException
 	{
 		final JSONObject teams = new JSONObject();
 		teams.put("teams", makeJSONTeams());
 
 		final JSONObject board = new JSONObject();
 		board.put("board", teams);
 
 		return board;
 	}
 
 
 	/**
 	 * Creates a JSONArray of both teams (the player's and the opponent's) on
 	 * the board. This JSONArray is actually a JSONArray of two other
 	 * JSONArray's: one is the player's pieces and the other is the opponent's
 	 * pieces.
 	 * 
 	 * @return
 	 * Returns a JSONArray that contains two other JSONArrays: one is the
 	 * player's pieces and the other is the opponent's pieces.
 	 * 
 	 * @throws JSONException
 	 * If a glitch or something happened while trying to create this JSON
 	 * String then a JSONException will be thrown.
 	 */
 	private JSONArray makeJSONTeams() throws JSONException
 	{
 		final JSONArray teamPlayer = makeJSONTeam(GenericPiece.TEAM_PLAYER);
 		final JSONArray teamOpponent = makeJSONTeam(GenericPiece.TEAM_OPPONENT);
 
 		final JSONArray teams = new JSONArray();
 		teams.put(teamPlayer);
 		teams.put(teamOpponent);
 
 		return teams;
 	}
 
 
 	/**
 	 * Creates a JSONArray of one team's pieces. The team to make the pieces
 	 * for is specified by the whichTeam parameter.
 	 * 
 	 * @param whichTeam
 	 * Which team do you want to make a JSONArray for? You should use one of
 	 * the GenericPiece class's TEAM_* public static members for this
 	 * parameter.
 	 * 
 	 * @return
 	 * Returns a JSONArray that contains all of the given team's pieces. This
 	 * has the possibility of being an empty JSONArray.
 	 * 
 	 * @throws JSONException
 	 * If a glitch or something happened while trying to create this JSON
 	 * String then a JSONException will be thrown.
 	 */
 	private JSONArray makeJSONTeam(final byte whichTeam) throws JSONException
 	{
 		final JSONArray team = new JSONArray();
 
 		for (byte x = 0; x < lengthHorizontal; ++x)
 		{
 			for (byte y = 0; y < lengthVertical; ++y)
 			{
 				final Position position = getPosition(x, y);
 
 				if (position.hasPiece() && position.getPiece().isTeam(whichTeam))
 				{
 					team.put(position.makeJSON());
 				}
 			}
 		}
 
 		return team;
 	}
 
 
 	/**
 	 * This will reset the board back to it's original state as created
 	 * immediately after using this class's constructor.
 	 * 
 	 * @throws JSONException
 	 * If a glitch or something happened while trying to create some JSON data
 	 * then this JSONException will be thrown. If the game was not resumed from
 	 * JSON data then this should never happen.
 	 */
 	public void reset() throws JSONException
 	{
 		isBoardLocked = false;
 		initializePositions();
 		resetBoard();
 
 		if (boardJSON == null)
 		{
 			initializeDefaultBoard();
 		}
 		else
 		{
 			initializeBoardFromJSON();
 		}
 	}
 
 
 
 
 	/**
 	 * Creates a GenericPiece object out of the given data.
 	 * 
 	 * @param whichTeam
 	 * The team that this new GenericPiece object should be on.
 	 * 
 	 * @param type
 	 * The type of piece that this GenericPiece object should be. This should
 	 * be something like a king for checkers or a knight for chess.
 	 * 
 	 * @return
 	 * Returns a new GenericPiece object made from the given data.
 	 */
 	protected abstract GenericPiece buildPiece(final byte whichTeam, final int type);
 
 
 	/**
 	 * Checks this GenericBoard object for validity.
 	 * 
 	 * @return
 	 * A byte that represents if the board is valid or not.
 	 */
 	public abstract byte checkValidity();
 
 
 	/**
 	 * Checks this GenericBoard object for validity against the board given in
 	 * the boardJSON parameter.
 	 * 
 	 * @param boardJSON
 	 * The board to check against. This is a newer game board than the one
 	 * stored in this GenericBoard object.
 	 * 
 	 * @return
 	 * A byte that represents if the board is valid or not.
 	 */
 	public abstract byte checkValidity(final JSONObject boardJSON);
 
 
 	/**
 	 * Makes this GenericBoard object a default board. This should only be used
 	 * for a brand new game. If a game is being resumed then this should not be
 	 * used. This should probably be run (in the case that it needs to be)
 	 * immediately after the object is constructed.
 	 */
 	protected abstract void initializeDefaultBoard();
 
 
 	/**
 	 * Checks to see if a move can be performed given the input positions. If
 	 * the given move turns out to be valid then this method will make any
 	 * needed adjustments to the game board and then return true.
 	 * 
 	 * @param previous
 	 * The previous (old) position on the game board that the user clicked on.
 	 * 
 	 * @param current
 	 * The current (new) position on the game board that the user clicked on.
 	 * 
 	 * @return
 	 * Returns true if the given move is a valid one.
 	 */
 	public abstract boolean move(final Position previous, final Position current);
 
 
 	/**
 	 * Allows classes that extend from this one to perform some code when this
 	 * class's reset() method is run.
 	 */
 	protected abstract void resetBoard();
 
 
 }
