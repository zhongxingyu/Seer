 package edu.selu.android.classygames.games;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 /**
  * Class representing a single GenericPiece. Each game will need to extend this
  * GenericPiece class with it's own Piece class.
  */
 public abstract class GenericPiece
 {
 
 
 	/**
 	 * This variable represents which team this GenericPiece object is on.
 	 */
 	protected byte team;
 	private final static byte TEAM_NULL = 0;
 	public final static byte TEAM_PLAYER = 1;
 	public final static byte TEAM_OPPONENT = 2;
 
 
 	/**
 	 * This variable represents which type of GenericPiece object that this is.
 	 */
 	protected byte type;
 	private final static byte TYPE_NULL = 0;
 
 
 	/**
 	 * Default constructor that creates an empty Piece.
 	 */
 	protected GenericPiece()
 	{
 		team = TEAM_NULL;
 		type = TYPE_NULL;
 	}
 
 
 	/**
 	 * Constructor that creates a Piece object. Both passed in arguments will
 	 * be checked for validity. If (and only if) they are valid arguments then
 	 * this Piece object be created using those parameters. 
 	 * 
 	 * @param team
 	 * The Piece's team. Should be either TEAM_PLAYER or TEAM_OPPONENT.
 	 * 
 	 * @param type
 	 * The Piece's type. In checkers this could be a normal piece or a king
 	 * piece. In chess this could be a pawn, knight, rook...
 	 */
 	protected GenericPiece(final byte team, final byte type)
 	{
 		this();
 
 		if (checkIfTeamIsValid(team) && checkIfTypeIsValid(type))
 		{
 			this.team = team;
 			this.type = type;
 		}
 	}
 
 
 	/**
 	 * Returns this GenericPiece's team. You may want to make a comparison of
 	 * the returned byte with this class's public final static TEAM_* bytes.
 	 * 
 	 * @return
 	 * Returns this GenericPiece's team.
 	 */
 	public byte getTeam()
 	{
 		return team;
 	}
 
 
 	/**
 	 * Returns this GenericPiece's type. In a game of checkers, the type could
 	 * be a a normal piece or a king piece. In chess, the type could be a pawn,
 	 * rook, knight, king... You may want to make a comparison of the returned
 	 * byte with the Piece class's public final static TYPE_* bytes.
 	 * 
 	 * @return
 	 * Returns a byte that represents this GenericPiece's type.
 	 */
 	public byte getType()
 	{
 		return type;
 	}
 
 
 	/**
 	 * Checks to see if this GenericPiece object is alive. This method really
 	 * just returns the inverse result of the isDead() method, as that checks
 	 * for basically the very same thing. A GenericPiece object is alive if
 	 * if it's team is not equal to TEAM_NULL and it's type is not equal to
 	 * TYPE_NULL.
 	 * 
 	 * @return
 	 * Returns true if this GenericPiece is alive.
 	 */
 	public boolean isAlive()
 	{
 		return !isDead();
 	}
 
 
 	/**
 	 * Checks to see if this GenericPiece object is alive. A GenericPiece
 	 * object is alive as long as it's team does not equal TEAM_NULL and type
 	 * does not equal TYPE_NULL.
 	 * 
 	 * @return
 	 * Returns true if this GenericPiece is dead.
 	 */
 	public boolean isDead()
 	{
 		return team == TEAM_NULL && team == TYPE_NULL;
 	}
 
 
 	/**
 	 * Checks to see if this GenericPiece's team is the same as the passed in
 	 * team.
 	 * 
 	 * @param whichTeam
 	 * The team to check this GenericPiece's team against.
 	 * 
 	 * @return
 	 * Returns true if this GenericPiece's team is the same as the passed in
 	 * team.
 	 */
 	public boolean isTeam(final byte whichTeam)
 	{
 		return team == whichTeam;
 	}
 
 
 	/**
 	 * Checks to see if this GenericPiece is an opponent's piece.
 	 * 
 	 * @return
 	 * True if this GenericPiece is an opponent's piece.
 	 */
 	public boolean isTeamOpponent()
 	{
 		return team == TEAM_OPPONENT;
 	}
 
 
 	/**
 	 * Checks to see if this GenericPiece is a player's piece.
 	 * 
 	 * @return
 	 * Returns true if this GenericPiece is a player's piece.
 	 */
 	public boolean isTeamPlayer()
 	{
 		return team == TEAM_PLAYER;
 	}
 
 
 	/**
 	 * Kills this GenericPiece object. Seriously though, this method just sets
 	 * this GenericPiece object's team to TEAM_NULL and type to TYPE_NULL.
 	 */
 	public void kill()
 	{
 		team = TEAM_NULL;
 		type = TYPE_NULL;
 	}
 
 
 
 
 	/**
 	 * Checks to see if the passed in team byte is a valid team byte.
 	 * 
 	 * @param team
 	 * A byte representing this Piece's team.
 	 * 
 	 * @return
 	 * Returns true if the passed in team byte is a valid team byte.
 	 */
 	private static boolean checkIfTeamIsValid(final byte team)
 	{
 		switch (team)
 		{
 			case TEAM_PLAYER:
 			case TEAM_OPPONENT:
 				return true;
 
 			default:
 				return false;
 		}
 	}
 
 
 
 
 	/**
 	 * Tests to see if the passed in type is actually a valid type.
 	 * 
 	 * @param type
 	 * The type to test.
 	 * 
 	 * @return
 	 * True if the given type is valid.
 	 */
 	protected abstract boolean checkIfTypeIsValid(final byte type);
 
 
 }
