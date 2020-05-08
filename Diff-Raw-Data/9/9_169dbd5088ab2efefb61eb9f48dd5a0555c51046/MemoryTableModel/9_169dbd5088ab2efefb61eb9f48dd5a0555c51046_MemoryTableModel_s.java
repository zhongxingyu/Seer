 package elw.dp.app;
 
 import elw.dp.mips.Memory;
 import elw.dp.mips.asm.Data;
 
 import javax.swing.table.AbstractTableModel;
 
 public class MemoryTableModel extends AbstractTableModel {
     public static final String COL_ADDR = "Addr";
     private static final String COL_BIN = "Bin8";
     private static final String COL_HEX_WORD = "Hex32";
     private static final String COL_DEC_WORD = "Dec32";
     public static final String COL_ACC = InstructionsTableModel.COL_ACC;
 
     private final String[] columns = new String[]{COL_ACC, COL_ADDR, COL_HEX_WORD, COL_DEC_WORD, COL_BIN};
     private final Memory memory;
 
     public MemoryTableModel(Memory memory) {
         this.memory = memory;
     }
 
     public String getColumnName(int column) {
         return columns[column];
     }
 
     public int getColumnCount() {
         return columns.length;
     }
 
     public int getRowCount() {
         return memory.getSize();
     }
 
     public Object getValueAt(int rowIndex, int columnIndex) {
         final String colName = columns[columnIndex];
         final int address = memory.getAddressAt(rowIndex);
 
         if (COL_ADDR.equals(colName)) {
             return Data.str(address, 16, 8);
         } else if (COL_BIN.equals(colName)) {
             //Look at this hack on the next line! This should be done inside Data.str()
             return Data.str((long) memory.getByteInternal(address) & 0xFF, 2, 8);
         } else if (COL_HEX_WORD.equals(colName)) {
             if (!memory.hasWord(address)) {
                 return "?";
             }
            return Data.str((long) memory.getWordInternal(address) & 0xFFFFFFFF, 16, 8);
         } else if (COL_DEC_WORD.equals(colName)) {
             if (!memory.hasWord(address)) {
                 return "?";
             }
             return Data.str(memory.getWordInternal(address), 10, 0);
         } else if (COL_ACC.equals(colName)) {
             return getAccessMod(address);
         }
 
         return "";
     }
 
     private String getAccessMod(int address) {
         StringBuffer accessMod = new StringBuffer();
 
         if (memory.getReadAdresses().contains(address)) {
             accessMod.append("r");
         }
 
         if (memory.getWriteAdresses().contains(address)) {
             accessMod.append("w");
         }
 
         return accessMod.toString();
     }
 }
