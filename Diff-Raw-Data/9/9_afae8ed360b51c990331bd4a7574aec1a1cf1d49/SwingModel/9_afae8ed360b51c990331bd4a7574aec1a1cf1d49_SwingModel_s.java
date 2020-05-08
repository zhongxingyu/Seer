 import javax.swing.table.AbstractTableModel;
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 class SwingModel {
     static class RegisterModel extends AbstractTableModel {
         private ArrayList<Integer> registers;
         public RegisterModel(){
             super();
             registers = new ArrayList<Integer>();
         }
 
         public void setRegisters(int[] initRegisters){
             for(int i: initRegisters){
                 registers.add(i);
             }
             fireTableStructureChanged();
         }
 
         public void setRegister(int idx, int val){
             if(idx >= registers.size()){
                 for(int i=registers.size(); i<=idx; i++){
                     registers.add(0);
                 }
                 fireTableStructureChanged();
             }
             registers.set(idx, val);
             fireTableCellUpdated(0, idx);
         }
 
         @Override
         public String getColumnName(int idx){
             return "R"+idx;
         }
 
         @Override
         public int getColumnCount(){
             return registers.size();
         }
 
         @Override
         public int getRowCount(){
             return 1;
         }
 
         @Override
         public Object getValueAt(int row, int col){
             if(col >= registers.size() || col < 0)return 0;
             return registers.get(col);
         }
 
 
     }
 
     static class WordModel extends AbstractTableModel {
         private int[] memory;
         private int offset;
         private int arity;
         private String prefix;
         //maps color to row (local scope)
         private Hashtable<Color, Integer> marks;
 
         public WordModel(int[] memory, int offset, int arity){
             this(memory, offset, arity, "");
         }
         public WordModel(int[] memory, int offset, int arity, String prefix){
             super();
             this.memory = memory;
             this.arity = arity;
             this.offset = offset;
             this.prefix = prefix;
             marks = new Hashtable<Color, Integer>();
         }
 
         /** Sets the value of a word and returns true, if a word with index idx
          * is available, otherwise returns false
          * @param idx index of the word
          * @param val new value of the word
          * @return true, if successfully set, otherwise false
          * */
         public boolean setMemory(int idx, int val){
             idx-= offset; //indices provided have global scope, so convert to local model
             if(idx < 0 || idx >= memory.length) return false;
 
             memory[idx] = val;
             int row = idx / arity;
             int col = idx % arity + 1;
             fireTableCellUpdated(row, col);
             return true;
         }
 
         @Override
         public String getColumnName(int idx){
             if(arity <= 1){
                 return idx == 1? "Word": (prefix + " #");
             } else {
                 switch(idx){
                     case 0: return prefix+" #";
                     case 1: return "Instr";
                     default: return "Param"+(idx-1);
                 }
             }
         }
 
         @Override
         public int getColumnCount(){
             return 1+arity;
         }
 
         @Override
         public int getRowCount(){
             return memory.length/arity;
         }
 
         @Override
         public Integer getValueAt(int row, int col){
             if(col==0) return (row*arity)+offset;
             else if(col>arity) return -1;
             else {
                 int pos = row * arity + (col-1);
                 return memory[pos];
             }
         }
 
         public int g2lRow(int globalIndex ){
             globalIndex -= offset;
             globalIndex /= arity;
             return globalIndex;
         }
 
         /**
          * @param pos absolute Adress to set color to 
          * @return true, if set, or false if address not in this scope */
         public boolean setMark(int pos, Color color){
             pos = g2lRow(pos);
             if(pos < 0 || pos >= getRowCount()) {
               Integer prevPos = marks.get(color);
               marks.put(color, -1);
               if(prevPos!=null){
                 fireTableRowsUpdated(prevPos, prevPos);
               }
               fireTableRowsUpdated(pos, pos);
               return false;
             }
 
             Integer prevPos = marks.get(color);
             marks.put(color, pos);
             if(prevPos!=null){
                 fireTableRowsUpdated(prevPos, prevPos);
             }
             fireTableRowsUpdated(pos, pos);
             return true;
         }
 
         public Color getRowColor(int row){
             for(Color color: marks.keySet()){
                 int pos = marks.get(color);
                 if(pos == row) return color;
             }
             return null;
         }
     }
 
     static class HelpDB {
         private static String db[] = {
         "MRI: R%d = %d", "MRR: R%d = R%d", "MRM: R%d = M[R%d]", "MMR: M[R%d] = R%d", 
         "ADD: R%d += R%d", "SUB: R%d -= R%d", "MUL: R%d *= R%d", "DIV: R%d /= R%d", "MOD: R%d %= R%d", 
         "AND: R%d &= R%d", "OR: R%d |= R%d", "XOR: R%d ^= R%d",
         "ISZ: R%d = (R%d == 0)? 1 : 0", "ISP: R%d = (R%d > 0)? 1 : 0", "ISN: R%d = (R%d < 0)? 1 : 0", 
         "JPC: R0 = (R%d != 0)? %d : R0", "SYS: %1$d == 0: R%2$d = read; %1$d == 1: write R%2$d"
         };
 
         public static String query(int instr, int p1, int p2){
             return String.format(db[instr], p1, p2);
         }
     }
 
     static class Line {
         public String[] tokens;
         public int lineCount;
         public int type;
     }
 
     static class Instruction extends Line{
     }
     static class DatInstruction extends Instruction{
         public int size;
     }
     static class Comment extends Line{
     }
     static class Label extends Line{
     }
 
     static public Line parseCodeLine(String line){
         String[] tokens = line.split("\\s+");
         Line lo;
         if(tokens.length == 0){
             return null;
        } else if(tokens[0].startsWith(";")){
             lo = new Comment();
             lo.lineCount = 0;
             lo.type = 2;
         } else if(tokens[0].endsWith(":")){
             lo = new Label();
             lo.lineCount = 0;
             lo.type = 3;
         } else {
             if(tokens[0].equals("DAT")){
                 lo = new DatInstruction();
                 lo.type = 1;
                 int count;
                 try {
                     count = Integer.parseInt(tokens[1].replaceAll("[\\D]", ""));
                 } catch(NumberFormatException nfe){
                     nfe.printStackTrace();
                     count = 3;
                 }
                 ((DatInstruction)lo).size = count;
                 lo.lineCount += count;
             } else {
                 lo = new Instruction();
                 lo.type = 1;
                 lo.lineCount += 3;
             }
         }
         lo.tokens = tokens;
         return lo;
     }
 
     static class LineModel implements Iterable<Line> {
         private ArrayList<Line> lines;
         
         public LineModel(){
             lines = new ArrayList<Line>();
         }
         public int size(){
             return lines.size();
         }
 
         public void add(Line line){
             lines.add(line);
         }
 
         public String getText(Line line, int tok){
             return line.tokens[tok];
         }
 
         public int getTokenCount(Line line){
             return line.tokens.length;
         }
 
         public Iterator<Line> iterator(){
             return lines.iterator();
         }
     }
 }
