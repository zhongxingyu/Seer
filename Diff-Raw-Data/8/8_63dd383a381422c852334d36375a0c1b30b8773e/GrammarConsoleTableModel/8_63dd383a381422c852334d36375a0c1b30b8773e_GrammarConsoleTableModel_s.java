 package de.unisiegen.gtitool.ui.model;
 
 
 import java.util.ArrayList;
 
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableModel;
 
import com.sun.java_cup.internal.symbol;

 import de.unisiegen.gtitool.core.entities.NonterminalSymbol;
 import de.unisiegen.gtitool.core.entities.Production;
 import de.unisiegen.gtitool.core.entities.ProductionWordMember;
 import de.unisiegen.gtitool.core.entities.Symbol;
 import de.unisiegen.gtitool.core.exceptions.NonterminalSymbolInvolvedException;
 import de.unisiegen.gtitool.core.exceptions.ProductionInvolvedException;
 import de.unisiegen.gtitool.core.exceptions.ProductionWordMembersInvolvedException;
 import de.unisiegen.gtitool.core.exceptions.grammar.GrammarException;
 import de.unisiegen.gtitool.core.parser.style.PrettyString;
 
 
 /**
  * The table model for the warning and error tables.
  * 
  * @author Benjamin Mies
  * @author Christian Fehler
  * @version $Id:DefaultTableModel.java 305 2007-12-06 19:55:14Z mies $
  */
 public final class GrammarConsoleTableModel extends AbstractTableModel
 {
 
   /**
    * The console table entry.
    * 
    * @author Christian Fehler
    */
   private final class ConsoleTableEntry
   {
 
     /**
      * The message.
      */
     public PrettyString message;
 
 
     /**
      * The description.
      */
     public PrettyString description;
 
 
     /**
     * The {@link Production} list.
      */
     public ArrayList < Production > productionList;
 
 
     /**
     * The {@link symbol}s.
      */
     public ArrayList < ProductionWordMember > productionWordMember;
 
 
     /**
      * The {@link NonterminalSymbol}.
      */
     public NonterminalSymbol nonterminalSymbol;
 
 
     /**
      * Allocates a new {@link ConsoleTableEntry}.
      * 
      * @param message The message.
      * @param description The description.
      * @param productionList The {@link Production} list.
      * @param nonterminalSymbol The {@link NonterminalSymbol}.
      * @param symbols The {@link Symbol}s.
      */
     public ConsoleTableEntry ( PrettyString message, PrettyString description,
         ArrayList < Production > productionList,
         NonterminalSymbol nonterminalSymbol,
         ArrayList < ProductionWordMember > symbols )
     {
       this.message = message;
       this.description = description;
       this.productionList = productionList;
       this.productionWordMember = symbols;
       this.nonterminalSymbol = nonterminalSymbol;
     }
   }
 
 
   /**
    * The serial version uid.
    */
   private static final long serialVersionUID = -7684497578193097976L;
 
 
   /**
    * The message column
    */
   public final static int MESSAGE_COLUMN = 0;
 
 
   /**
    * The description column
    */
   public final static int DESCRIPTION_COLUMN = 1;
 
 
   /**
    * The {@link Production} column
    */
   public final static int PRODUCTION_COLUMN = 2;
 
 
   /**
    * The column count
    */
   public final static int COLUMN_COUNT = 3;
 
 
   /**
    * The data of this table model
    */
   private ArrayList < ConsoleTableEntry > data;
 
 
   /**
    * Allocates a new {@link GrammarConsoleTableModel}.
    */
   public GrammarConsoleTableModel ()
   {
     this.data = new ArrayList < ConsoleTableEntry > ();
   }
 
 
   /**
    * Add a row to this data model
    * 
    * @param grammarException the GrammarException containing the data for the
    *          new row
    */
   public final void addRow ( GrammarException grammarException )
   {
     ArrayList < Production > production = null;
     NonterminalSymbol nonterminalSymbol = null;
     ArrayList < ProductionWordMember > productionWordMember = null;
 
     if ( grammarException instanceof ProductionInvolvedException )
     {
       production = ( ( ProductionInvolvedException ) grammarException )
           .getProduction ();
     }
 
     if ( grammarException instanceof NonterminalSymbolInvolvedException )
     {
       nonterminalSymbol = ( ( NonterminalSymbolInvolvedException ) grammarException )
           .getNonterminalSymbol ();
     }
 
     if ( grammarException instanceof ProductionWordMembersInvolvedException )
     {
       productionWordMember = ( ( ProductionWordMembersInvolvedException ) grammarException )
           .getProductionWordMember ();
     }
 
     this.data.add ( new ConsoleTableEntry ( grammarException
         .getPrettyMessage (), grammarException.getPrettyDescription (),
         production, nonterminalSymbol, productionWordMember ) );
     fireTableRowsInserted ( this.data.size () - 1, this.data.size () - 1 );
   }
 
 
   /**
    * Clear the data of this table model
    */
   public final void clearData ()
   {
     this.data.clear ();
     fireTableDataChanged ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see AbstractTableModel#getColumnClass(int)
    */
   @Override
   public final Class < ? > getColumnClass ( int columnIndex )
   {
     switch ( columnIndex )
     {
       case MESSAGE_COLUMN :
       {
         return PrettyString.class;
       }
       case DESCRIPTION_COLUMN :
       {
         return PrettyString.class;
       }
       case PRODUCTION_COLUMN :
       {
         return ArrayList.class;
       }
       default :
       {
         return Object.class;
       }
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see TableModel#getColumnCount()
    */
   public final int getColumnCount ()
   {
     return COLUMN_COUNT;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see AbstractTableModel#getColumnName(int)
    */
   @Override
   public final String getColumnName ( int columnIndex )
   {
     switch ( columnIndex )
     {
       case MESSAGE_COLUMN :
       {
         return "Message"; //$NON-NLS-1$
       }
       case DESCRIPTION_COLUMN :
       {
         return "Description"; //$NON-NLS-1$
       }
       case PRODUCTION_COLUMN :
       {
         return "Productions"; //$NON-NLS-1$
       }
       default :
       {
         return ""; //$NON-NLS-1$
       }
     }
   }
 
 
   /**
    * Returns the {@link NonterminalSymbol} of the given row index.
    * 
    * @param rowIndex The given row index.
    * @return The {@link NonterminalSymbol} of the given row index.
    */
   public final NonterminalSymbol getNonterminalSymbol ( int rowIndex )
   {
     return this.data.get ( rowIndex ).nonterminalSymbol;
   }
 
 
   /**
    * Returns the {@link Production} list of the given row index.
    * 
    * @param rowIndex The given row index.
    * @return The {@link Production} list of the given row index.
    */
   public final ArrayList < Production > getProduction ( int rowIndex )
   {
     return this.data.get ( rowIndex ).productionList;
   }
 
 
   /**
    * Returns the {@link ProductionWordMember}s of the given row index.
    * 
    * @param rowIndex The given row index.
    * @return The {@link NonterminalSymbol} of the given row index.
    */
   public final ArrayList < ProductionWordMember > getProductionWordMember (
       int rowIndex )
   {
     return this.data.get ( rowIndex ).productionWordMember;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see TableModel#getRowCount()
    */
   public final int getRowCount ()
   {
     return this.data.size ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see TableModel#getValueAt(int, int)
    */
   public final Object getValueAt ( int rowIndex, int columnIndex )
   {
     switch ( columnIndex )
     {
       case MESSAGE_COLUMN :
       {
         return this.data.get ( rowIndex ).message;
       }
       case DESCRIPTION_COLUMN :
       {
         return this.data.get ( rowIndex ).description;
       }
       case PRODUCTION_COLUMN :
       {
         return this.data.get ( rowIndex ).productionList;
       }
       default :
       {
         return null;
       }
     }
   }
 }
