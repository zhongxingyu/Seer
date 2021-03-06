 package com.eviware.soapui.support.swing;
 
 import com.eviware.soapui.support.UISupport;
 import org.jdesktop.swingx.JXTable;
 
 import javax.swing.JTable;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableModel;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 
 /**
  *  Factory class responsible for creation of JTable instances with a common style.
  */
 public abstract class JTableFactory
 {
 
 	public abstract JTable makeJTable(TableModel tableModel);
 
 	public abstract JXTable makeJXTable(TableModel tableModel);
 
 	public static JTableFactory getInstance() {
 		 return new DefaultJTableFactory();
 	}
 
 	private static class DefaultJTableFactory extends JTableFactory
 	{
 		@Override
 		public JTable makeJTable( TableModel tableModel )
 		{
 			return UISupport.isMac() ? makeStripedTable( tableModel ) : new JTable( tableModel );
 		}
 
 		@Override
 		public JXTable makeJXTable( TableModel tableModel )
 		{
 			return UISupport.isMac() ? makeStripedJXTable( tableModel ) : new JXTable( tableModel );
 		}
 
 		private JXTable makeStripedJXTable( final TableModel tableModel )
 		{
 			JXTable stripedJxTable = new JXTable( tableModel )
 			{
 				@Override
 				public Component prepareRenderer( TableCellRenderer renderer, int row, int column )
 				{
 					Component defaultRenderer = super.prepareRenderer( renderer, row, column );
 					applyStripesToRenderer( row, defaultRenderer );
 					return defaultRenderer;
 				}
 
 				@Override
 				public boolean getShowVerticalLines()
 				{
 					return false;
 				}
 			};
 			setGridAttributes( stripedJxTable );
 			return stripedJxTable;
 		}
 
 		private JTable makeStripedTable( final TableModel tableModel )
 		{
 			JTable stripedTable = new JTable( tableModel )
 			{
 				@Override
 				public Component prepareRenderer( TableCellRenderer renderer, int row, int column )
 				{
 					Component defaultRenderer = super.prepareRenderer( renderer, row, column );
 					applyStripesToRenderer( row, defaultRenderer );
 					return defaultRenderer;
 				}
 
 				@Override
 				public boolean getShowVerticalLines()
 				{
 					return false;
 				}
 			};
 			setGridAttributes( stripedTable );
 			return stripedTable;
 		}
 
 	}
 
 	public static void setGridAttributes( JTable stripedTable )
 	{
 		stripedTable.setShowGrid( false );
 		stripedTable.setIntercellSpacing( new Dimension(0, 0) );
 	}
 
 	public static void applyStripesToRenderer( int row, Component defaultRenderer )
 	{
		Color fontColor = defaultRenderer.getForeground();
		if( row % 2 == 1 )
 		{
 			defaultRenderer.setBackground( new Color( 241, 244, 247 ) );
 		}
 		else
 		{
 			defaultRenderer.setBackground( Color.WHITE );
 		}
		defaultRenderer.setForeground( fontColor);
 	}
 }
