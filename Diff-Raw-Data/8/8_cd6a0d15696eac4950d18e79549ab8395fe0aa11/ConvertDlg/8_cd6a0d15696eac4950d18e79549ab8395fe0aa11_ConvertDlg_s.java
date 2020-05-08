 import java.awt.*;
 
 /********************************************************************************
 ** The dialog used to enter the conversion parameters.
 */
 
 public class ConvertDlg extends ModalDialog
 {
 	/********************************************************************************
 	** Constructor.
 	*/
 
 	public ConvertDlg(Component oParent)
 	{
 		super(oParent, "Conversion Options", OK_CANCEL);
 
 		// Create the child panels.
 		m_pnlSrcFields.add(m_lblX, m_ebX);
 		m_pnlSrcFields.add(m_lblY, m_ebY);
 		m_pnlSrcFields.add(m_lblW, m_ebW);
 		m_pnlSrcFields.add(m_lblH, m_ebH);
 
 		m_pnlDstFields.add(m_lblSplit,  m_ckSplit);
 		m_pnlDstFields.add(m_lblCommas, m_ckCommas);
 		m_pnlDstFields.add(m_lblPerRow, m_ebPerRow);
 
 		// Create the dialog panel.
 		m_pnlSrcGrpBox.add(BorderLayout.CENTER, m_pnlSrcFields);
 		m_pnlDstGrpBox.add(BorderLayout.CENTER, m_pnlDstFields);
 
 		m_pnlControls.add(BorderLayout.NORTH, m_pnlSrcGrpBox);
 		m_pnlControls.add(BorderLayout.SOUTH, m_pnlDstGrpBox);
 	}
 
 	/********************************************************************************
 	** The dialog is about to be shown. Load the controls.
 	*/
 
 	public void show()
 	{
 		m_ebX.setText(String.valueOf(m_rcSrc.x));
 		m_ebY.setText(String.valueOf(m_rcSrc.y));
 		m_ebW.setText(String.valueOf(m_rcSrc.width));
 		m_ebH.setText(String.valueOf(m_rcSrc.height));
 
 		m_ckSplit.setState(m_bSplit);
 		m_ckCommas.setState(m_bCommas);
 		m_ebPerRow.setText(String.valueOf(m_nPerRow));
 
 		// Not implemented.
 		m_ckSplit.setEnabled(false);
 
 		super.show();
 	}
 
 	/********************************************************************************
 	** "OK" button handler.
 	*/
 
 	public void onOK()
 	{
 		try
 		{
 			// Fetch the controls values.
 			m_rcSrc.x      = Integer.parseInt(m_ebX.getText());
 			m_rcSrc.y      = Integer.parseInt(m_ebY.getText());
 			m_rcSrc.width  = Integer.parseInt(m_ebW.getText());
 			m_rcSrc.height = Integer.parseInt(m_ebH.getText());
 
 			m_bSplit  = m_ckSplit.getState();
 			m_bCommas = m_ckCommas.getState();
 			m_nPerRow = Integer.parseInt(m_ebPerRow.getText());
 
 			super.onOK();
 		}
 		catch (Exception e)
 		{
 			MsgBox.alert(this, Img2Src.APP_NAME, e.toString());
 		}
 	}
 
 	/********************************************************************************
 	** Constants.
 	*/
 
 	/********************************************************************************
 	** Members.
 	*/
 
 	// Child panels.
 	private GroupPanel		m_pnlSrcGrpBox = new GroupPanel("Source Rectangle");
 	private DualColPanel	m_pnlSrcFields = new DualColPanel();
 	private GroupPanel		m_pnlDstGrpBox = new GroupPanel("Destination Options");
 	private DualColPanel	m_pnlDstFields = new DualColPanel();
 
 	// Child controls.
 	private Label		m_lblX = new Label("X:", Label.RIGHT);
 	private TextField	m_ebX  = new TextField(5);
 	private Label		m_lblY = new Label("Y:", Label.RIGHT);
 	private TextField	m_ebY  = new TextField(5);
 	private Label		m_lblW = new Label("Width:", Label.RIGHT);
 	private TextField	m_ebW  = new TextField(5);
 	private Label		m_lblH = new Label("Height:", Label.RIGHT);
 	private TextField	m_ebH  = new TextField(5);
 
 	private Label		m_lblSplit  = new Label("Split Channels?", Label.RIGHT);
 	private Checkbox	m_ckSplit   = new Checkbox();
 	private Label		m_lblCommas = new Label("Comma Separated?", Label.RIGHT);
 	private Checkbox	m_ckCommas  = new Checkbox();
 	private Label		m_lblPerRow = new Label("Pixels/Line:", Label.RIGHT);
 	private TextField	m_ebPerRow  = new TextField(5);
 
 	// Data members.
 	public Rectangle	m_rcSrc   = new Rectangle();
 	public boolean		m_bSplit  = false;
 	public boolean		m_bCommas = true;
 	public int			m_nPerRow = 8;
 }
