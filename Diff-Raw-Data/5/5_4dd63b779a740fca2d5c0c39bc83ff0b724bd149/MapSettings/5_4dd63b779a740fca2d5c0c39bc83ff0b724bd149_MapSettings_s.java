 package lightbeam.editor;
 
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import core.GameObjects;
 import core.tilestate.ITileState;
 
 
 public class MapSettings extends GameObjects
 {
 	private JPanel panelSettings	= new JPanel();
 	
 	private JTextField txtRows		= new JTextField();
 	private JTextField txtCols		= new JTextField();
 	
 	private ITileState oldTileState	= null;
 	private ITileState curTileState	= null;
 	
 	private MapArea maparea			= null;
 	
 	private int lblWidth			= 59;
 	private int txtWidth			= 30;
 	private int btnWidth			= 16;
 	private int btnHeight			= 16;
 	
 	private int margin_left			= 15;
 	private int margin_right		= 8;
 	private int margin_top			= 15;
 	private int margin_bottom		= 8;
 	private int ctrlHeight			= 32;
 	
 	public MapSettings( MapArea maparea, int rows, int cols )
 	{
 		this.maparea		= maparea;
 		
 		panelSettings.setBorder( new TitledBorder( "Einstellungen:" ) );
 		panelSettings.setLayout( null );
 		
 		JPanel pnlRows		= new JPanel();		
 		JPanel pnlCols		= new JPanel();
 		
 		pnlRows.setLayout( null );
 		pnlCols.setLayout( null );
 		
 		JLabel lblRows	= new JLabel( "Zeilen:" );
 		lblRows.setBounds( margin_left, margin_top, lblWidth, ctrlHeight );
 		
 		JLabel lblCols	= new JLabel( "Spalten:" );
 		lblCols.setBounds( margin_left, lblRows.getBounds().y + lblRows.getBounds().height + 5, lblWidth, ctrlHeight );
 
 		this.txtRows.setText( rows + "" );
 		this.txtRows.setBounds( margin_left + lblWidth, margin_top, txtWidth, ctrlHeight );
 		this.txtRows.setHorizontalAlignment( JTextField.CENTER );
 		this.txtRows.setEditable( false );
 		
 		this.txtCols.setText( cols + "" );
 		this.txtCols.setBounds( margin_left + lblWidth, lblCols.getBounds().y , txtWidth, ctrlHeight );
 		this.txtCols.setHorizontalAlignment( JTextField.CENTER );
 		this.txtCols.setEditable( false );
 
 		// Spielfeldzeile hinzufgen: 
 		JButton btnRowsUp	= new JButton();
 		btnRowsUp.setIcon( new ImageIcon( "./src/fx/Lightbeam/editor/palette/btnUp.png" ) );		
 		btnRowsUp.setBounds( 2 * margin_left + txtRows.getBounds().x, txtRows.getBounds().y, btnWidth, btnHeight );
 		btnRowsUp.addMouseListener( new MouseAdapter(){public void mouseReleased(MouseEvent e){
 			oldTileState	= MapSettings.this.eTileset.getSelected();
 			curTileState	= MapSettings.this.eTileset.tile( 1 );
 
 			MapSettings.this.eTileset.setSelected( curTileState );
 			MapSettings.this.maparea.addRow();
 			MapSettings.this.eTileset.setSelected( oldTileState );
 			
 			int amountRows	= Integer.parseInt( MapSettings.this.txtRows.getText() ) + 1;
 			MapSettings.this.txtRows.setText( amountRows + "" );
 		}});
 
 		// Spielfeldzeile entfernen:
 		JButton btnRowsDown	= new JButton();
 		btnRowsDown.setIcon( new ImageIcon( "./src/fx/Lightbeam/editor/palette/btnDown.png" ) );
 		btnRowsDown.setBounds( 2 * margin_left + txtRows.getBounds().x, txtRows.getBounds().y + txtRows.getBounds().height - btnHeight, btnWidth, btnHeight );
 		btnRowsDown.addMouseListener( new MouseAdapter(){public void mouseClicked(MouseEvent e){
 			if( MapSettings.this.maparea.delRow() == true )
 			{
 				int amountRows	= Integer.parseInt( MapSettings.this.txtRows.getText() ) - 1;
 				MapSettings.this.txtRows.setText( amountRows + "" );
 			}
 		}});
 
 		JButton btnColsUp	= new JButton();
 		btnColsUp.setIcon( new ImageIcon( "./src/fx/Lightbeam/editor/palette/btnUp.png" ) );		
 		btnColsUp.setBounds( 2 * margin_left + txtCols.getBounds().x, txtCols.getBounds().y, btnWidth, btnHeight );
 		btnColsUp.addMouseListener( new MouseAdapter(){public void mouseClicked(MouseEvent e){
 			oldTileState	= MapSettings.this.eTileset.getSelected();
 			curTileState	= MapSettings.this.eTileset.tile( 1 );
 			
 			MapSettings.this.eTileset.setSelected( curTileState );
 			MapSettings.this.maparea.addCol();
 			MapSettings.this.eTileset.setSelected( oldTileState );
 			
 			int amountCols	= Integer.parseInt( MapSettings.this.txtCols.getText() ) + 1;
 			MapSettings.this.txtCols.setText( amountCols + "" );
 		}});
 
 		JButton btnColsDown	= new JButton();
 		btnColsDown.setIcon( new ImageIcon( "./src/fx/Lightbeam/editor/palette/btnDown.png" ) );
 		btnColsDown.setBounds( 2 * margin_left + txtCols.getBounds().x, txtCols.getBounds().y + txtRows.getBounds().height - btnHeight, btnWidth, btnHeight );
 		btnColsDown.addMouseListener( new MouseAdapter(){public void mouseClicked(MouseEvent e){
 			if( MapSettings.this.maparea.delCol() == true )
 			{
 				int amountCols	= Integer.parseInt( MapSettings.this.txtCols.getText() ) - 1;
 				MapSettings.this.txtCols.setText( amountCols + "" );
 			}
 		}});
 		
 		int iWidth	= margin_left + lblWidth + txtWidth + btnWidth + margin_right;
 		int iHeight	= txtCols.getBounds().y + txtCols.getBounds().height + margin_bottom;
 		
 		panelSettings.add( lblRows );
 		panelSettings.add( txtRows );
 		panelSettings.add( btnRowsUp );
 		panelSettings.add( btnRowsDown );
 		
 		panelSettings.add( lblCols );
 		panelSettings.add( txtCols );
 		panelSettings.add( btnColsUp );
 		panelSettings.add( btnColsDown );
 		
 		panelSettings.setBounds( 5, 5 , iWidth, iHeight );	
 	}
 	
 	public JPanel panel() { return this.panelSettings; }
 	
 	public void resetSettings( int rows, int cols )	
 	{
 		this.oldTileState	= null;
 		this.curTileState	= null;
		
 		this.txtRows.setText( rows + "" );
		this.txtRows.setText( cols + "" );
 	}
 }
