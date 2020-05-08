 import java.util.*;
 
 /* for NFA */
 class Node{
 	static int id = 0;
 	int nodeId;
 	ArrayList<Path> paths;
 	boolean end;
 	
 	public Node(boolean end){
 		this.end = end;
 		paths = new ArrayList<Path>();
 		this.nodeId = Node.id++;
 	}
 	
 	public Node(){
 		this(false);
 	}
 	
 	public void connect(Node b){
 		Path p = new Path();
 		p.dest = b;
 		this.paths.add(p);
 	}
 	
 	/* for debug */
 	public String toString(){
 		return "Node:" + this.nodeId;
 	}
 	
 	/* for HashSet */
 	public int hashCode(){
 		return this.nodeId;
 	}
 }
 
 /* for NFA */
 class Path{
 	boolean empty;
 	Node dest;
 	
 	public Path(){
 		empty = true;
 	}
 	
 	public boolean matches(char c){
 		return false;
 	}
 }
 
 class CharacterPath extends Path{
 	
 	String allowed;
 	
 	public CharacterPath(String allowed){
 		this.allowed = allowed;
 		this.empty = false;
 	}
 	
 	public boolean matches(char c){
 		
 		for(int x=0; x < allowed.length(); x++){
 			if( allowed.charAt(x) == c){
 				return true;
 			}
 		}
 		
 		return false;
 	}
 }
 
 class WildcardPath extends Path{
 	public WildcardPath(){
 		super();
 		this.empty = false;
 	}
 	
 	public boolean matches(char c){
 		return true;
 	}
 }
 
 class InvertedPath extends CharacterPath{
 	
 	public InvertedPath(String notAllowed){
 		super(notAllowed);
 		this.empty = false;
 	}
 	
 	public boolean matches(char c){
 		return !super.matches(c);
 	}
 }
 
 class RangePath extends Path{
 	char a, b;
 	
 	public RangePath(char a, char b){
 		this.empty = false;
 		this.a = a;
 		this.b = b;
 	}
 	
 	public boolean matches(char c){
 		return this.a <= c && c <= this.b;
 	}
 }
 
 /* NFA wrapper */
 class AutomataFrag{
 	Node in, out;
 	
 	public AutomataFrag(){
 		this.in = new Node();
 		this.out = new Node();
 	}
 	
 	public AutomataFrag(Node in, Node out){
 		this.in = in;
 		this.out = out;
 	}
 	
 	/* unused */
 	public AutomataFrag(Path p){
 		this();
 		this.in.paths.add(p);
 		p.dest = this.out;
 	}
 	
 	public void connect(){
 		this.in.connect(this.out);
 	}
 	
 	public void attach(AutomataFrag f){
 		this.out.connect(f.in);
 	}
 }
 
 class LiteralFrag extends AutomataFrag{
 	public LiteralFrag(String allowed){
 		super(new CharacterPath(allowed));
 	}
 }
 
 class InvertedLiteralFrag extends AutomataFrag{
 	public InvertedLiteralFrag(String notAllowed){
 		super(new InvertedPath(notAllowed));
 	}
 }
 
 class WildcardFrag extends AutomataFrag{
 	public WildcardFrag(){
 		super(new WildcardPath());
 	}
 }
 
 class RegexEngine {
 	
 	private AutomataFrag regexAutomata;
 	
 	public RegexEngine(String regex){
 		regex = regexPreprocess(regex, 0, regex.length(), false);
 		
 		System.out.println("regex: " + regex);
 		
 		regexAutomata = regexToAutomata(regex, 0, regex.length());
 		regexAutomata.out.end = true;
 	}
 	
 	public boolean matches(String string){
 		
 		HashSet<Node> occupied = new HashSet<Node>();
 		DFSOccupyBlankNode(occupied, regexAutomata.in);
 		
 		Node end = regexAutomata.out;
 		
 		//System.out.println(occupied);
 		
 		for(char c : string.toCharArray()){
 			occupied = stepAutomata(occupied, c);
 			//System.out.println(occupied);
 		}
 		
 		return occupied.contains(end);
 	}
 	
 	public static String regexPreprocess(String regex, int start, int end, boolean orTokens){
 		
 		Stack<String> tokenStack = new Stack<String>();
 		
 		boolean escaped = false;				
 		
 		for(int i=start; i<end; i++){
 			
 			char c = regex.charAt(i);
 			
 			if(c == '\\' && !escaped){
 				escaped = true;
 				continue;
 			}
 			
 			if(escaped){
 				tokenStack.push("\\" + c);
 			}
 			
 			else if(c == '('){
 				int index = mateIndex(regex, i);
 				tokenStack.push("(" + regexPreprocess(regex, i+1, index, false) + ")");
 				i=index;
 			}
 			
 			else if(c == '['){
 				int index = mateIndex(regex, i);
 				tokenStack.push(regexPreprocess(regex, i+1, index, true));
 				i=index;	
 			}
 			
 			else if(c == '{'){
 				int index = mateIndex(regex, i);
 				tokenStack.push(preprocessRange(regex.substring(i+1, index), tokenStack.pop()));
 				i=index;
 			}
 			
 			else if(c=='*' || c == '?' || c == '+'){
 				String last = tokenStack.pop();
 				last += c;
 				tokenStack.push(last);
 			}
 			
 			else if(c == '|'){
 				if(orTokens){
 					continue;
 				}
 				else{
 					tokenStack.push(Character.toString(c));
 				}
 			}
 			
 			else{
 				tokenStack.push(Character.toString(c));
 			}
 			
 			escaped = false;
 		}
 		
 		StringBuilder builder = new StringBuilder();
 		
 		/*
 		 * or all tokens together
 		 */
 		if(orTokens){
 			
 			int size = tokenStack.size();
 			
 			builder.append('(');
 			
 			for(String s : tokenStack){
 				builder.append(s);
 				if(--size>0){
 					builder.append('|');
 				}
 			}
 			
 			builder.append(')');
 		}
 		else{
 			for(String s : tokenStack){
 				builder.append(s);
 			}
 		}
 		
 		return builder.toString();
 	}
 	
 	/*
 	 * index of nested mate
 	 */
 	public static int mateIndex(String str, int start){
 		char a=0, b=0;
 		int depth = 0;
 		
 		switch(str.charAt(start)){
 		case '(': a = '('; b = ')'; break;
 		case '[': a = '['; b = ']'; break;
 		case '{': a = '{'; b = '}'; break;
 		}
 		
 		for(int x = start; x < str.length(); x++){
 			char c = str.charAt(x);
 			
 			if(c == a)
 				depth++;
 			
 			else if(c == b)
 				depth--;
 			
 			if(depth == 0){
 				return x;
 			}
 		}
 		
 		return -1;
 	}
 	
 	public static String preprocessRange(String arg, String prevToken)
 	{	
 		StringBuilder builder = new StringBuilder();
 		
 		int commaIndex = arg.indexOf(",");
 		
 		/*
 		 * f{3} -> fff
 		 */
 		if(commaIndex < 0){
 			int times = Integer.parseInt(arg);
 			
 			while(times-->0)
 				builder.append(prevToken);
 		}
 		else{
 			String minStr = arg.substring(0, commaIndex);
 			String maxStr = arg.substring(commaIndex+1);
 			int min = minStr.length()==0 ? 0 : Integer.parseInt(minStr);
 			int max = maxStr.length()==0 ? 0 : Integer.parseInt(maxStr);
 			
 			/*
 			 * f{2,} -> ff+ 
 			 */
 			if(max == 0){
 				while(min-->0)
 					builder.append(prevToken);
 				
 				builder.append('+');
 			}
 			/*
 			 * f{0,3} -> (|f|ff)
 			 */
 			else{
 				builder.append('(');
 				
 				for(int x=min; x < max; x++){
 					for(int y=0; y < x; y++)
 						builder.append(prevToken);
 					
 					if( x < max-1)
 						builder.append('|');
 				}
 				
 				builder.append(')');
 			}
 		}
 		
 		return builder.toString();
 	}
 	
 	/* processes more complex regex syntax into ()?+*| components */
	public static AutomataFrag regexToAutomata(String regex, int start, int end)
 	{	
 		Stack<AutomataFrag> frags = new Stack<AutomataFrag>();
 		AutomataFrag root = new AutomataFrag();
 		root.connect();
 		frags.push(root);
 		
 		boolean escaped = false;
 		
 		for(int i=start; i<end; i++){
 			char c = regex.charAt(i);
 			
 			if(c == '\\' && !escaped){
 				escaped = true;
 				continue;
 			}
 			
 			if(escaped){
 				AutomataFrag next = null;
 				
 				switch(c){
 				case 'w': next = new LiteralFrag("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuwvxyz0123456789_"); break;
 				case 'W': next = new InvertedLiteralFrag("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"); break;
 				case 'a': next = new LiteralFrag("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"); break;
 				case 'd': next = new LiteralFrag("0123456789"); break;
 				case 'D': next = new InvertedLiteralFrag("0123456789"); break;
 				case 'U': next = new LiteralFrag("ABCDEFGHIJKLMNOPQRSTUVWXYZ");	break;
 				case 'l': next = new LiteralFrag("abcdefghijklmnopqrstuvwxyz");	break;
 				default: next = new LiteralFrag(Character.toString(c));
 				}
 				
 				AutomataFrag last = frags.peek();
 				last.attach(next);
 				frags.push(next);
 			}
 			else if(c == '.'){
 				AutomataFrag last = frags.peek();
 				AutomataFrag next = new WildcardFrag();
 				last.attach(next);
 				frags.push(next);
 			}
 			
 			else if(c == '|'){
 				AutomataFrag pathA = new AutomataFrag(frags.firstElement().in, frags.peek().out);
 				AutomataFrag pathB = regexToAutomata(regex, i+1, end);
 				
 				frags.clear();
 				
 				AutomataFrag frag = new AutomataFrag();
 				
 				frag.in.connect(pathA.in);
 				frag.in.connect(pathB.in);
 				
 				pathA.out.connect(frag.out);
 				pathB.out.connect(frag.out);
 				
 				frags.push(frag);
 				break;
 			}
 			
 			else if(c == '?' && frags.size() > 0){
 				frags.peek().connect();				
 			}
 			
 			else if(c == '+' && frags.size() > 0){
 				AutomataFrag last = frags.peek();
 				last.out.connect(last.in);
 			}
 			
 			else if(c == '*' && frags.size() > 0){
 				AutomataFrag last = frags.peek();
 				last.in.connect(last.out);
 				last.out.connect(last.in);
 			}
 			
 			else if(c == '('){
 				int index = mateIndex(regex, i);
 				AutomataFrag frag = regexToAutomata(regex, i+1, index);
 				AutomataFrag last = frags.peek();
 				last.attach(frag);
 				frags.push(frag);				
 				i=index;
 			}
 			else{
 				AutomataFrag last = frags.peek();
 				AutomataFrag next = new LiteralFrag(Character.toString(c));
 				last.attach(next);
 				frags.push(next);
 			}
 			
 			escaped = false;
 		}
 		
 		return new AutomataFrag(frags.firstElement().in, frags.peek().out);
 	}
 	
 	/* one automata step for char c */
 	public static HashSet<Node> stepAutomata(HashSet<Node> occupied, char c)
 	{
 		HashSet<Node> nextOccupied = new HashSet<Node>();
 		
 		/* move all occupied */
 		for(Node node : occupied){
 			for(Path path : node.paths){
 				if(path.matches(c)){
 					nextOccupied.add(path.dest);
 				}
 			}
 		}
 		
 		/* concurrent modification is bad */
 		HashSet<Node> blankSpan = new HashSet<Node>();
 		
 		/* occupy all blank paths */
 		for(Node node : nextOccupied){
 			for(Path path : node.paths){
 				if(path.empty){
 					DFSOccupyBlankNode(blankSpan, path.dest);
 				}
 			}
 		}
 		
 		nextOccupied.addAll(blankSpan);
 		
 		return nextOccupied;
 	}
 	
 	/* occupy all blank nodes accessible from root*/
	public static void DFSOccupyBlankNode(HashSet<Node> blankSpan, Node node){
 		
 		Stack<Node> nodes = new Stack<Node>();
 		nodes.push(node);
 		
 		while(!nodes.isEmpty()){
 			Node currNode = nodes.pop();
 			blankSpan.add(currNode);
 			
 			for(Path path : currNode.paths){
 				if(path.empty && !blankSpan.contains(path.dest)){
 					nodes.push(path.dest);
 				}
 			}
 		}
 	}
 }
 
 public class Regex{
 	
 	/*
 	 * supported operations:
 	 * ?*+()[]|.
 	 * {x} {x,} {,y} {x,y}
 	 * \w \W \a \d \D \U \l
 	 * 
 	 * Note:
 	 * behavior is undefined for invalid expressions
 	 * There is not error handeling
 	 * 
 	 */
 	public static void main(String[] args){
 		
 		RegexEngine engine = new RegexEngine("\\w+@\\w+\\.[\\l\\U]{2,}(\\.[\\l\\U]{2,})?");	
 		System.out.println(engine.matches("reid_horuff@gmail.co.uk"));
 		
 		engine = new RegexEngine("\\W\\|{2,4}");
 		System.out.println(engine.matches("|||"));
 	}
 }
 
