 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package layarq.Objetos;
 
 import java.util.ArrayList;
 import javax.swing.table.AbstractTableModel;
 
 /**
  *
  * @author cristian.oliveira
  */
 public class LayoutTableModel extends AbstractTableModel{
    private String[] colunas = {"Descrição","Pos. Ini","Pos Fim","Tamanho","Valor"};
     private ArrayList<Layout> dados;
     
     @Override
     public int getRowCount() {
         return dados.size();
     }
 
     @Override
     public int getColumnCount() {
         return colunas.length;
     }
      
     @Override
     public String getColumnName(int pColuna) {
         return colunas[pColuna];
     }
     
     @Override
     public Object getValueAt(int pLinha, int pColuna) {
         Layout dado = dados.get(pLinha);
         
         switch(pColuna)
         {
             case 0: return dado.descricao;
             case 1: return dado.posicaoInicial;
             case 2: return dado.posicaoFinal;
            case 3: return dado.tamanho;
            case 4: return dado.getValor();
                 default: return null;
         }
     }
     
     @Override
     public void setValueAt(Object value, int pLinha, int pColuna) {
         dados.set(pLinha, (Layout) value);
         fireTableCellUpdated(pLinha, pColuna);
     }
     
     public void setLayouts(ArrayList<Layout> pLayouts)
     {
         dados = pLayouts;
     }
 }
