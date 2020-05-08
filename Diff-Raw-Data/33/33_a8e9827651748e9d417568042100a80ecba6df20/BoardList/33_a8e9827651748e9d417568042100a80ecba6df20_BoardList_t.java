 public class BoardList {
 	Node[] cornerPointers;
 	Node[] homePointers;
 	Node[] startPointers;
 
 	public BoardList() {
 		cornerPointers = new Node[4];
 		homePointers = new Node[4];
 		startPointers = new Node[4];
 		cornerPointers[0] = new Node(null, null);
 		cornerPointers[1] = buildQuarterBoard(cornerPointers[0],
 				Piece.COLOR.red, 0);
 		cornerPointers[2] = buildQuarterBoard(cornerPointers[1],
 				Piece.COLOR.blue, 1);
 		cornerPointers[3] = buildQuarterBoard(cornerPointers[2],
 				Piece.COLOR.yellow, 2);
 		Node ret = buildQuarterBoard(cornerPointers[3], Piece.COLOR.green, 3);
 		cornerPointers[0].setPrevious(ret.getPrevious());
 		ret.getPrevious().setNext(cornerPointers[0]);
 	}
 
 	public BoardList(String load) {
 		cornerPointers = new Node[4];
 		homePointers = new Node[4];
 		startPointers = new Node[4];
 		cornerPointers[0] = new Node(null, null);
 		cornerPointers[1] = buildQuarterBoard(cornerPointers[0],
 				Piece.COLOR.red, 0);
 		cornerPointers[2] = buildQuarterBoard(cornerPointers[1],
 				Piece.COLOR.blue, 1);
 		cornerPointers[3] = buildQuarterBoard(cornerPointers[2],
 				Piece.COLOR.yellow, 2);
 		Node ret = buildQuarterBoard(cornerPointers[3], Piece.COLOR.green, 3);
 		cornerPointers[0].setPrevious(ret.getPrevious());
 		ret.getPrevious().setNext(cornerPointers[0]);
 		String[] nodes = load.split("\\|");
 		Node start = cornerPointers[0].getNext();
 		Node temp = null;
 		for (int i = 0; i < nodes.length; i++) {
 			String num = nodes[i].substring(nodes[i].length() - 1);
 			int numb = 0;
 			try {
 				numb = (Integer.parseInt(num));
 			} catch (Exception e) {
 			}
 			Piece.COLOR col;
 			col = getColor(nodes[i]);
 			Piece[] pieces = new Piece[1];
 			if (numb > 0) {
 				pieces = new Piece[numb];
 			}
 			for (; numb > 0; numb--) {
 				pieces[numb - 1] = new Piece(col);
 			}
 			if (start instanceof SlideNode) {
 				start.setPieces(pieces);
 				if (((SlideNode) start).getSafeNode() != null) {
 					temp = start;
 					start = ((SlideNode) start).getSafeNode();
 				} else {
 					start = start.getNext();
 				}
 			} else if (start.getNext() == null || start.getPrevious() == null) {
 				if (pieces.length == 1) {
 					pieces = new Piece[4];
 				}
 				((MultiNode) start).setPieces(pieces);
 				start = temp.getNext();
 			} else {
 				start.setPieces(pieces);
 				start = start.getNext();
 			}
 		}
 	}
 
 	protected Piece.COLOR getColor(String s) {
 		Piece.COLOR col = Piece.COLOR.colorless;
 		switch (s.charAt(0)) {
 		case 'r':
 			col = Piece.COLOR.red;
 			break;
 		case 'g':
 			col = Piece.COLOR.green;
 			break;
 		case 'h':
 			switch (s.charAt(1)) {
 			case 'r':
 				col = Piece.COLOR.red;
 				break;
 			case 'g':
 				col = Piece.COLOR.green;
 				break;
 			case 'b':
 				col = Piece.COLOR.blue;
 				break;
 			case 'y':
 				col = Piece.COLOR.yellow;
 				break;
 			}
 			break;
 		case 'b':
 			col = Piece.COLOR.blue;
 			break;
 		case 'y':
 			col = Piece.COLOR.yellow;
 			break;
 		default:
 			col = Piece.COLOR.colorless;
 			break;
 		}
 		return col;
 	}
 
 	public Piece[] newGame() {
 		int j = 0;
 		Piece[] ret = new Piece[16];
 		for (int i = 0; i < 4; i++) {
 			Piece[] pieces = new Piece[4];
 			for (int n = 0; n < 4; n++) {
 				pieces[n] = new Piece(startPointers[i].getColor());
 				ret[j++] = pieces[n];
 			}
 			startPointers[i].setPieces(pieces);
 		}
 		return ret;
 	}
 
 	public String toString() {
 		return toString(cornerPointers[0], cornerPointers[0].getNext())
 				.toString();
 	}
 
 	public Node[] getHomePointers() {
 		return homePointers;
 	}
 
 	public Node[] getStartPointers() {
 		return startPointers;
 	}
 
 	public Node[] getCornerPointers() {
 		return cornerPointers;
 	}
 
 	private StringBuilder toString(Node start, Node next) {
 		StringBuilder ret = new StringBuilder();
 		ret.append(next.toString());
 		if (next.getNext() == null)
 			return ret;
 		if (start == next)
 			return ret;
 		if (next instanceof SlideNode) {
 			if (((SlideNode) next).getSafeNode() != null) {
 				if (((SlideNode) next).getSafeNode() instanceof MultiNode)
 					ret.append(toString(((SlideNode) next).getSafeNode(),
 							((SlideNode) next).getSafeNode()));
 				else
 					ret.append(toString(start, ((SlideNode) next).getSafeNode()));
 			}
 		}
 		return ret.append(toString(start, next.getNext()));
 
 	}
 
 	/**
 	 * Creates a copy of this board and returns it.
 	 */
 	public BoardList clone() {
		return new BoardList(this.toString());
 	}
 
 	/*
 	 * public String toString(Node start){ Node temp = start; String ret = "";
 	 * while(temp.next != start){
 	 * 
 	 * } }
 	 */
 	protected Node buildQuarterBoard(Node previous, Piece.COLOR col, int pos) {
 		SlideNode one = new SlideNode(null, previous, null, col);
 		one.head = true;
 		previous.setNext(one);
 		SlideNode two = new SlideNode(null, one, null, col); // This now goes to
 		one.setNext(two); // safety
 		Node three = new Node(null, two, col);
 		two.setSafeNode(three);
 		Node four = new Node(null, three, col);
 		three.setNext(four);
 		Node five = new Node(null, four, col);
 		four.setNext(five);
 		Node six = new Node(null, five, col);
 		five.setNext(six);
 		Node seven = new Node(null, six, col);
 		six.setNext(seven);
 		MultiNode eight = new MultiNode(null, seven, col);
 		homePointers[pos] = eight;
 		seven.setNext(eight);
 		SlideNode nine = new SlideNode(null, two, null, col);
 		two.setNext(nine);
 		SlideNode ten = new SlideNode(null, nine, null, col);
 		nine.setNext(ten);
 		MultiNode eleven = new MultiNode(ten, null, col);
 		startPointers[pos] = eleven;
 		ten.setSafeNode(eleven);
 		Node twelve = new Node(null, ten);
 		ten.setNext(twelve);
 		Node thirteen = new Node(null, twelve);
 		twelve.setNext(thirteen);
 		Node fourteen = new Node(null, thirteen);
 		thirteen.setNext(fourteen);
 		Node fifteen = new Node(null, fourteen);
 		fourteen.setNext(fifteen);
 		SlideNode sixteen = new SlideNode(null, fifteen, null, col);
 		fifteen.setNext(sixteen);
 		sixteen.head = true;
 		SlideNode seventeen = new SlideNode(null, sixteen, null, col);
 		sixteen.setNext(seventeen);
 		SlideNode eighteen = new SlideNode(null, seventeen, null, col);
 		seventeen.setNext(eighteen);
 		SlideNode nineteen = new SlideNode(null, eighteen, null, col);
 		eighteen.setNext(nineteen);
 		SlideNode twenty = new SlideNode(null, nineteen, null, col);
 		nineteen.setNext(twenty);
 		Node twentyOne = new Node(null, twenty);
 		twenty.setNext(twentyOne);
 		Node twentyTwo = new Node(null, twentyOne);
 		twentyOne.setNext(twentyTwo);
 		return twentyTwo;
 	}
 }
