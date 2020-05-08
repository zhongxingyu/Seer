 package edu.berkeley.gamesman.game;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.PrimitiveValue;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * Cuboids...
  * @author Jeremy Fleischman
  */
 public class Cuboid extends Game<CubeState> {
 	final int WIDTH, HEIGHT, DEPTH;
 	final int[] VALID_DIRS;
 	final String[] VALID_FACES;
 	//as you look at the F face of the cuboid,
 	//     H  +-----+
 	//    T  /     /|
 	//   P  /     / |
 	//  E  /  U  /  |
 	// D  /     /   |
 	// H +-----+ R  +
 	// E |     |   /
 	// I |     |  /
 	// G |  F  | /
 	// H |     |/
 	// T +-----+
 	//    WIDTH 
 
 	/**
 	 * Constructs a Cuboid 
 	 * @param conf the configuration
 	 */
 	public Cuboid(Configuration conf) {
 		super(conf);
 		WIDTH = Integer.parseInt(conf.getProperty("gamesman.game.width", "2"));
 		HEIGHT = Integer.parseInt(conf.getProperty("gamesman.game.height", "2"));
 		DEPTH = Integer.parseInt(conf.getProperty("gamesman.game.depth", "2"));
 		VALID_FACES = conf.getProperty("gamesman.game.faces", "FUR").split(", *");
 		VALID_DIRS = Util.parseInts(conf.getProperty("gamesman.game.dirs", "1, 2").split(", *"));
 		// TODO - generalize to arbitrary cuboids!
 		// TODO should we treat a 2x2x3 as different from a 3x2x2
 		// we could require that WIDTH <= HEIGHT <= DEPTH,
 		// or cycle them to make that true
 	}
 
 	@Override
 	public String describe() {
 		return String.format("%dx%dx%d cuboid (legal faces: %s, legal dirs: %s)", WIDTH, HEIGHT, DEPTH, Arrays.toString(VALID_FACES), Arrays.toString(VALID_DIRS));
 	}
 
 	@Override
 	public String displayState(CubeState pos) {
 		return pos.display(false);
 	}
 	
 	@Override
 	public String displayHTML(CubeState pos) {
 		return pos.display(true);
 	}
 
 	@Override
 	public int getDefaultBoardHeight() {
 		//		Util.fatalError("Cuboids don't have a height!");
 		return -1;
 	}
 
 	@Override
 	public int getDefaultBoardWidth() {
 		//		Util.fatalError("Cuboids don't have a width!");
 		return -1;
 	}
 
 	@Override
 	public char[] pieces() {
 //		Util.fatalError("Cuboids don't have pieces!");
 		return null;
 	}
 
 	private static final int pieceCount = 8;
 	private static final BigInteger[] THREE_TO_X = new BigInteger[pieceCount], FACTORIAL = new BigInteger[pieceCount];
 	{ //memoize some useful values for (un)hashing
 		THREE_TO_X[0] = BigInteger.ONE;
 		FACTORIAL[0] = BigInteger.ONE;
 		for(int i = 1; i < pieceCount; i++) {
 			THREE_TO_X[i] = BigInteger.valueOf(3).multiply(THREE_TO_X[i-1]);
 			FACTORIAL[i] = BigInteger.valueOf(i).multiply(FACTORIAL[i-1]);
 		}
 	}
 
 	@Override
 	public BigInteger stateToHash(CubeState state) {
 		BigInteger hash = BigInteger.ZERO;
 		ArrayList<Integer> pieces = new ArrayList<Integer>(Arrays.asList(state.pieces));
 		pieces.remove(pieces.size() - 1); //we don't care about the last piece because it will always be fixed
 		for(int i = 0; i < 6; i++) {
 			int pos = pieces.indexOf(i);
 			pieces.remove(pos);
 			hash = hash.add(FACTORIAL[pieces.size()].multiply(BigInteger.valueOf(pos)));
 		}
 
 		hash = hash.multiply(THREE_TO_X[6]);
 		for(int i = 0; i < 6; i++)
 			hash = hash.add(BigInteger.valueOf(state.orientations[i]).multiply(THREE_TO_X[i]));
 		return hash;
 	}
 
 	@Override
 	public BigInteger lastHash() {
 		return THREE_TO_X[6].multiply(FACTORIAL[7]);
 	}
 
 	@Override
 	public CubeState hashToState(final BigInteger inhash) {
 		BigInteger hash = inhash;
 		Integer[] orientations = new Integer[pieceCount];
 		int totalorient = 0;
 		for(int i = 0; i < 6; i++) {
 			BigInteger[] div_rem = hash.divideAndRemainder(BigInteger.valueOf(3));
 			orientations[i] = div_rem[1].intValue();
 			hash = div_rem[0];
 			totalorient += orientations[i];
 		}
 		orientations[6] = Util.positiveModulo((3-totalorient),3);
 		orientations[7] = 0;
 		ArrayList<Integer> pieces = new ArrayList<Integer>(pieceCount);
 		for(int i = 0; i < 7; i++) {
 			int location = hash.divide(FACTORIAL[pieces.size()]).mod(BigInteger.valueOf(i+1)).intValue();
 			pieces.add(location, 6-i);
 		}
 		pieces.add(7);
 		return new CubeState(pieces.toArray(new Integer[0]), orientations);
 	}
 
 	@Override
 	public PrimitiveValue primitiveValue(CubeState pos) {
 		if(pos.isSolved())
 			return PrimitiveValue.WIN;
 		return PrimitiveValue.UNDECIDED;
 	}
 
 	@Override
 	public Collection<CubeState> startingPositions() {
 		return Arrays.asList(new CubeState());
 //		Collection<Pair<String, CubeState>> moves = validMoves(new CubeState());
 //		return Arrays.asList(moves.iterator().next().cdr);
 	}
 
 	@Override
 	public String stateToString(CubeState pos) {
 		return Util.join(",", pos.pieces) + ";" + Util.join(",", pos.orientations);
 	}
 
 	@Override
 	public CubeState stringToState(String pos) {
 		String[] pieces_orientations = pos.split(";");
 		return new CubeState(parseArray(pieces_orientations[0].split(",")), parseArray(pieces_orientations[1].split(",")));
 	}
 	private Integer[] parseArray(String[] arr) {
 		Integer[] nums = new Integer[arr.length];
 		for(int i = 0; i < nums.length; i++) 
 			nums[i] = Integer.parseInt(arr[i]);
 		return nums;
 	}
 	
 	private void cycle_pieces(int p1, int p2, Integer[] pieces) {
 		Integer temp = pieces[p1];
 		pieces[p1] = pieces[p2-4];
 		pieces[p2-4] = pieces[p2];
 		pieces[p2] = pieces[p1+4];
 		pieces[p1+4] = temp;
 	}
 
 	private static final String dirToString = "  2'";
 	@Override
 	public Collection<Pair<String, CubeState>> validMoves(CubeState pos) {
 		ArrayList<Pair<String, CubeState>> next = new ArrayList<Pair<String,CubeState>>();
 		for(int times : VALID_DIRS) {
 			for(String face : VALID_FACES) {
 				Integer[] pieces = pos.pieces.clone();
 				Integer[] orientations = pos.orientations.clone();
 				for(int i = 0; i < times; i++) {
 					if(face.equals("F")) {
 						cycle_pieces(0, 5, pieces);
 						cycle_pieces(0, 5, orientations);
 						orientations[0] = (orientations[0] + 2) % 3;
 						orientations[4] = (orientations[4] + 1) % 3;
 						orientations[5] = (orientations[5] + 2) % 3;
 						orientations[1] = (orientations[1] + 1) % 3;
 					} else if(face.equals("U")) {
 						Integer temp = pieces[0];
 						pieces[0] = pieces[2];
 						pieces[2] = pieces[3];
 						pieces[3] = pieces[1];
 						pieces[1] = temp;
 
 						temp = orientations[0];
 						orientations[0] = orientations[2];
 						orientations[2] = orientations[3];
 						orientations[3] = orientations[1];
 						orientations[1] = temp;
 					} else if(face.equals("R")) {
 						cycle_pieces(2, 4, pieces);
 						cycle_pieces(2, 4, orientations);
 						orientations[2] = (orientations[2] + 2) % 3;
 						orientations[6] = (orientations[6] + 1) % 3;
 						orientations[4] = (orientations[4] + 2) % 3;
 						orientations[0] = (orientations[0] + 1) % 3;
 					}
 				}
 				next.add(new Pair<String, CubeState>(face + ("" + dirToString.charAt(times)).trim(), new CubeState(pieces, orientations)));
 			}
 		}
 		return next;
 	}
 }
 
 class CubeState {
 	private static final Integer[] SOLVED_PIECES = 			new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7 };
 	private static final Integer[] SOLVED_ORIENTATIONS = 	new Integer[] { 0, 0, 0, 0, 0, 0, 0, 0 };
 	Integer[] pieces, orientations;
 	public CubeState(Integer[] pieces, Integer[] orientations) {
 		this.pieces = pieces;
 		this.orientations = orientations;
 	}
 	public CubeState() {
 		pieces = SOLVED_PIECES;
 		orientations = SOLVED_ORIENTATIONS;
 	}
 
 	public boolean isSolved() {
 		return Arrays.equals(pieces, SOLVED_PIECES) && Arrays.equals(orientations, SOLVED_ORIENTATIONS);
 	}
 
 	private static final char[][] solved_cube = {{'U','R','F'},{'U','F','L'},{'U','B','R'},{'U','L','B'},{'D','F','R'},{'D','L','F'},{'D','R','B'},{'D','B','L'}};
 	/**
 	 * Takes in an array of pieces and their orientations and return a char[][] containing the 
 	 * stickers on each side.
 	 */
 	private char[][] spit_out_colors() {
 		char[][] actual_colors = new char[8][];
 		int location = 0;
 		for(int i=0; i<pieces.length; i++){
 			int piece = pieces[i], orientation = orientations[i];
 			char[] current_chunk = new char[3];
 			current_chunk[0] = (solved_cube[piece][(orientation)%3]); //top piece
 			current_chunk[1] = (solved_cube[piece][(1+orientation)%3]); //right piece
 			current_chunk[2] = (solved_cube[piece][(2+orientation)%3]); //left piece
 			actual_colors[location] = current_chunk;
 			location++;
 		}
 		return actual_colors;
 	}
 
 	private static final HashMap<String, String> COLOR_SCHEME = new HashMap<String, String>();
 	static {
 		COLOR_SCHEME.put("F", "green");
		COLOR_SCHEME.put("U", "gray");
 		COLOR_SCHEME.put("R", "red");
 		COLOR_SCHEME.put("B", "blue");
 		COLOR_SCHEME.put("L", "orange");
 		COLOR_SCHEME.put("D", "yellow");
 	}
 	
 	private static String myFormat(String format, Object... args) {
 		//nasty hack so the cube is readable below
 		return String.format(format.replaceAll("@", "%c"), args);
 	}
 	
 	public String display(boolean dotty) {
 		String nl = dotty ? "<br align=\"left\" />" : "\n";
 		char[][] current_state = spit_out_colors();
 		String cube_string = "";
 		cube_string += myFormat("                                   ___________%s", nl);
 		cube_string += myFormat("                                  |     |     |%s", nl);
 		cube_string += myFormat("                   ____________   |  @  |  @  |%s", current_state[3][2], current_state[2][1], nl);
 		cube_string += myFormat("   /|             /  @  /  @  /|  |_____|_____|%s", current_state[3][0], current_state[2][0], nl);
 		cube_string += myFormat("  / |            /_____/_____/ |  |     |     |%s", nl);
		cube_string += myFormat(" /| |           /  @  /  @  /| |  |  @  |  @  |%s", current_state[1][0], current_state[0][0], current_state[7][1], current_state[6][2], nl);
 		cube_string += myFormat("/ |@|          /_____/_____/ |@|  |_____|_____|%s", current_state[3][1], current_state[2][2], nl);
 		cube_string += myFormat("|@| |          |     |     |@| |     Back (mirror)%s", current_state[1][2], current_state[0][1], nl);
 		cube_string += myFormat("| |/|          |  @  |  @  | |/|%s", current_state[1][1], current_state[0][2], nl);
 		cube_string += myFormat("|/|@|          |_____|_____|/|@|%s", current_state[7][2], current_state[6][1], nl);
 		cube_string += myFormat("|@| |          |     |     |@| |%s", current_state[5][1], current_state[4][2], nl);
 		cube_string += myFormat("| |/           |  @  |  @  | |/%s", current_state[5][2], current_state[4][1], nl);
 		cube_string += myFormat("|/Left (mirror)|_____|_____|/%1$s%1$s%1$s", nl);
 		cube_string += myFormat("                   ____________%s", nl);
 		cube_string += myFormat("                  /  @  /  @  /%s", current_state[7][0], current_state[6][0], nl);
 		cube_string += myFormat("                 /_____/_____/%s", nl);
 		cube_string += myFormat("                /  @  /  @  /%s", current_state[5][0], current_state[4][0], nl);
 		cube_string += myFormat("               /_____/_____/%s", nl);
 		cube_string += myFormat("               Down (mirror)%s", nl);
 		if(dotty) {
 			for(String face : COLOR_SCHEME.keySet())
 				cube_string = cube_string.replaceAll(face, "<font color=\"" + COLOR_SCHEME.get(face) + "\">" + face + "</font>");
 //			cube_string = "<table><tr><td bgcolor=\"gray\">" + cube_string + "</td></tr></table>";
 		}
 		return cube_string;
 	}
 }
