 import java.util.*;
 
 /**
  * Maps virtual registers to real registers.
  *
  * How to use:
  *   add each node to graph using addNode().
  *   get mappings using colorGraph().
  */
 public class RegisterAllocator {
    /** 
     * graph is just an easy way to access the vertices. 
     * All of the connections in the graph are stored in the ColorNode objects.
     */
    private Map<Register, ColorNode> graph;
    private Set<ColorNode> realNodes;
    private Map<Integer, List<Register>> colorings;
    private Map<Register, Register> allocations;
 
    static private final int kColors = 25;
 
    public RegisterAllocator() {
       ColorNode realNode;
 
       realNodes = new HashSet<ColorNode>();
       graph = new HashMap<Register, ColorNode>();
       colorings = new HashMap<Integer, List<Register>>();
       allocations = new HashMap<Register, Register>();
 
       /**
        * Graph starts with all the real registers. 
        * These registers all interfere with each other, as
        * they each need a unique color.
        */
       for (Register real : SparcRegisters.all) {
          realNode = new ColorNode(real);
 
          graph.put(real, realNode);
 
          realNode.setReal(true);
          realNodes.add(realNode); // Keep track of these real nodes.
       }
 
       // Now that there is a node for each real register, connect them all.
       for (Register real : SparcRegisters.all) {
          graph.get(real).addEdges(realNodes);
       }
    }
 
    /**
     * Build the graph by adding registers from all the nodes 
     * from all the functions in a program.
     */
    public void buildGraph(GraphTable graphTable) {
       graphTable.topoSort();
       for (Node block : graphTable.allNodes) {
          addNode(block); 
       }
 
       /* DEBUG code but I might need to debug again later.
        * TODO: erase all this.
       for (Register r : graph.keySet()) {
          if (r.toString().charAt(0) != '%') {
             System.out.print(r + ":  ");
 
             for (ColorNode n : graph.get(r).getEdges()) {
                System.out.print(n.getVertex() + ", ");
             }
 
             System.out.println();
          }
       }
       */
    }
 
    /**
     * Fill the graph with all the registers from a block of code.
     * Iterate backwards through instructions.
     */
    private void addNode(Node block) {
       List<Register> srcs;
       List<Register> dests;
       Set<Register> liveSet = new HashSet<Register>(block.getLiveSet());
       List<Instruction> instrs = block.getInstr();
       ListIterator<Instruction> iter = instrs.listIterator(instrs.size());
 
       while (iter.hasPrevious()) { 
          Instruction instr = iter.previous();
          srcs = new ArrayList<Register>(instr.getSources());
          dests = new ArrayList<Register>(instr.getDestinations());
 
          // Edge cases
          if (instr.isCall()) {
             srcs.addAll(SparcRegisters.outputs);
             dests.addAll(SparcRegisters.globals);
             dests.addAll(SparcRegisters.outputs);
          }
 
          if (instr.isConditionalMove()) {
            srcs.add((Register) instr.getOperands().get(1));
            dests.add((Register) instr.getOperands().get(1));
          }
 
          if (instr instanceof PrintInstruction ||
              instr instanceof PrintlnInstruction ||
              instr instanceof ReadInstruction) {
             dests.add(new Register("%i0"));
          }
 
          for (Register dest : dests) {
             liveSet.remove(dest);
             addEdges(dest, liveSet);
          }
             
          for (Register src : srcs) {
             liveSet.add(src); 
          }
       }
    }
 
    /**
     * Used by addNode when creating graph.
     */
    private void addEdges(Register one, Set<Register> many) {
       Set<ColorNode> edges = new HashSet<ColorNode>();
       ColorNode vertex, edge;
 
       // Make sure all edges have a ColorNode and are in the graph.
       for (Register edgeRegister : many) {
          edge = graph.get(edgeRegister);
          if (edge == null) {
             edge = new ColorNode(edgeRegister);
             graph.put(edgeRegister, edge);
          }
 
          edges.add(edge);
       }
 
       // Make sure vertex has a ColorNode and is in the graph.
       vertex = graph.get(one);
       if (vertex == null) {
          vertex = new ColorNode(one);
          graph.put(one, vertex);
       }
 
       // Make sure edges are connected to vertex.
       vertex.addEdges(edges);
       for (ColorNode node : edges) {
          node.addEdge(vertex);
       }
    }
 
    /** 
     * Use the mappings to change the instructions.
     */
    public void transformCode(GraphTable graph) {
       for (Node block : graph.allNodes) {
          for (Instruction instr : block.getInstr()) {
             instr.transformRegisters(allocations);
          }
       }
    }
 
    /**
     * Color the graph, and make a mapping of register to register.
     */
    public void colorGraph() {
       Stack<ColorNode> popped;
       
       popped = deconstructGraph();
       reconstructGraph(popped);
 
       // Everything after this just runs through the graph and makes a map of
       // Virtual Register -> Real Register.
 
       // Initialize coloring map for each color.
       for (int color = 0; color < kColors; color++) {
          colorings.put(color, new LinkedList<Register>());
       }
 
       // Sort of a hacky way to do this. 
       // First map colors (numbers) to a list of virtual registers.
       for (Register key : graph.keySet()) {
          ColorNode node = graph.get(key);
 
          if (!node.isReal() && node.getColor() != -1) {
             colorings.get(node.getColor()).add(node.getVertex());
          }
       }
 
       // Then iterate through the real nodes and look at the list of virtual
       // ones that share a color.
       for (ColorNode realNode : realNodes) {
          int color = realNode.getColor();
          Register real = realNode.getVertex();
          List<Register> shared = colorings.get(color);
 
          if (shared != null) {
             for (Register virtual : colorings.get(color)) {
                allocations.put(virtual, real);
             }
          }
       }
    }
 
    /** Get the ColorNode with the best hueristic for reducing spills. */
    private ColorNode getNextFromGraph() {
       ColorNode best = null, vertex;
       Set<Register> keys = graph.keySet();
 
       for (Register key : keys) {
          vertex = graph.get(key);
 
          if (best == null || vertex.compareTo(best) == 1) {
             best = vertex;
          }
       }
 
       return best;
    }
    
    /**
     * Convert graph into a stack of nodes, where the
     * order of the stack is the order to color.
     */
    private Stack<ColorNode> deconstructGraph() {
       Stack<ColorNode> rStack = new Stack<ColorNode>();
       ColorNode next;
 
       while (!graph.isEmpty()) {
          next = getNextFromGraph();
          rStack.push(next);
          graph.remove(next.getVertex());
       }
          
       return rStack;
    }
 
    /**
     * Reconstruct graph, and assign colors to the nodes.
     */
    private void reconstructGraph(Stack<ColorNode> rStack) {
       ColorNode vertex;
 
       while (!rStack.empty()) {
          vertex = rStack.pop();
 
          graph.put(vertex.getVertex(), vertex);
 
          // Color node based on neighbors that are back in graph.
          boolean goodColor;
          for (int color = 0; color < kColors; color++) {
             goodColor = true;
 
             for (ColorNode edge : vertex.getEdges()) {
                // Is the edge in the graph already?
                if (graph.get(edge.getVertex()) != null) {
                   // Then make sure we don't choose it's color.
 
                   if (color == edge.getColor()) {
                      goodColor = false;
                   }
                }
             }
 
             if (goodColor) {
                vertex.setColor(color);
             }
          }
 
          if (vertex.getColor() == -1) {
             // TODO handle spills.
             String debug = vertex + "\n"; 
             for (ColorNode edge : vertex.getEdges()) {
                debug += edge;
             }
             Evil.warning("Spill for register: " + vertex.getVertex());
          }
       }
    }
 
    /**
     * For debugging purposes only.
     */
    public String toString() {
       String ret = "";
 
       ret +=  "Real registers\n";
       for (ColorNode node : realNodes) {
          ret += node.getVertex() + ", ";
       }
       ret += "\n";
 
       ret += "Colors\n";
       for (Register r: graph.keySet()) {
          ret += r + " -> " + graph.get(r).getColor() + "\n";
       }
       ret += "\n";
 
       ret += "Register mappings\n";
       for (Register virtual : allocations.keySet()) {
          ret += virtual + " -> " + allocations.get(virtual) + "\n";
       }
       ret += "\n";
 
       return ret;
    }
 }
