 package com.hifiremote.jp1;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class LearnedSignalTimingSummaryDialog.
  */
 public class LearnedSignalTimingSummaryDialog extends JDialog implements ActionListener
 {
   public static void showDialog( Component locationComp, RemoteConfiguration config )
   {
     if ( dialog == null )
       dialog = new LearnedSignalTimingSummaryDialog( locationComp );
     dialog.config = config;   
     dialog.generateSummary();
     dialog.pack();
     dialog.setLocationRelativeTo( locationComp );
     dialog.setVisible( true );
   }
   
   public static void reset()
   {
     if ( dialog != null )
     {
       dialog.dispose();
       dialog = null;
     }
   }
 
   private LearnedSignalTimingSummaryDialog( Component c )
   {
     super( ( JFrame )SwingUtilities.getRoot( c ) );
     setTitle( "Learned Signal Timing Summary" );
     setModal( true );
 
     JComponent contentPane = ( JComponent )getContentPane();
     contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
 
     summaryTextArea.setEditable( false );
     summaryTextArea.setLineWrap( false );
     JScrollPane scrollPane = new JScrollPane( summaryTextArea );
     scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Timing Summary" ), scrollPane.getBorder() ) );
     contentPane.add( scrollPane, BorderLayout.CENTER );
        
     Box bottomBox = Box.createVerticalBox();
     contentPane.add( bottomBox, BorderLayout.PAGE_END );
     
     // Add the action buttons
     JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
     bottomBox.add( buttonPanel );
     
     Box notes = Box.createVerticalBox();
     notes.add( new JLabel( "Notes: Rounding here does not override selected analysis rounding." ) );
     notes.add( new JLabel( "Also analyzed signals will only if the new rounding yields a valid analysis." ) );
 
     buttonPanel.add( notes );
     buttonPanel.add( new JLabel( "  Round To: " ) );
     buttonPanel.add( burstRoundBox );
     buttonPanel.add( new JLabel( "   ") );
     burstRoundBox.setColumns( 4 );
     burstRoundBox.getDocument().addDocumentListener(new DocumentListener() {
       public void changedUpdate(DocumentEvent e) {
         generateSummary();
       }
       public void removeUpdate(DocumentEvent e) {
         generateSummary();
       }
       public void insertUpdate(DocumentEvent e) {
         generateSummary();
       }
     });
     
     okButton.addActionListener( this );
     okButton.setToolTipText( "Close the Summary" );
     buttonPanel.add( okButton );
   }
   
   private void appendDurations( StringBuilder summary, String[] durationStrings, String intro )
   {
     boolean first = true;
     for ( String d: durationStrings )
     {
       if ( first )
       {
         first = false;
         summary.append( intro );
       }
       else
         summary.append( "\n\t\t\t\t\tMore:\t" );
       summary.append(d);
     }
   }
   
   private void generateSummary()
   {
     int r = 1;
     String roundText = burstRoundBox.getText();
     boolean roundingSet = false; 
     if ( roundText != null && !roundText.isEmpty() )
     {
       try
       {
         r = Integer.parseInt( roundText );
         roundingSet = true;
       }
       catch (NumberFormatException e)
       {
         r = 1;
       }
     }
     
     List<LearnedSignal> signals = this.config.getLearnedSignals();
     Remote remote = this.config.getRemote();
     
     StringBuilder summary = new StringBuilder();
     summary.append( "LEARNED SIGNALS:\nLEARNED RAW DATA:\n" );
     summary.append( "#\tDevice\tKey\tNotes\tFreq\tRaw Timing Data\n" );
     int i = 1;
     for ( LearnedSignal s: signals )
     {
       UnpackLearned ul = s.getUnpackLearned();
       summary.append( i++ );
       summary.append( '\t' );
       summary.append( remote.getDeviceButton( s.getDeviceButtonIndex() ).getName() );
       summary.append( '\t' );
       summary.append( remote.getButtonName( s.getKeyCode() ) );
       summary.append( '\t' );
       summary.append( (s.getNotes() == null ? "" : s.getNotes()) );
       if ( ul.ok )
       {
         summary.append( '\t' );
         summary.append( ul.frequency );
         summary.append( '\t' );
         
         if ( roundingSet && !s.getTimingAnalyzer().getSelectedAnalyzer().getIsRoundingLocked() )
           s.getTimingAnalyzer().getSelectedAnalyzer().setRoundTo( r );
         LearnedSignalTimingAnalysis analysis = s.getTimingAnalyzer().getSelectedAnalysis();
         
         if ( ul.oneTime > 0 && ul.extra > 0 && ul.repeat == 0 )
         {
           appendDurations( summary, analysis.getOneTimeDurationStringList(), "Once:\t" );
          appendDurations( summary, analysis.getExtraDurationStringList(), "\n\t\t\t\t\tMore:\t" );
         }
         else
         {
           if ( ul.oneTime > 0 )
             appendDurations( summary, analysis.getOneTimeDurationStringList(), "Once:\t" );
 
           if ( ul.repeat > 0 )
             appendDurations( summary, analysis.getRepeatDurationStringList(), "Repeat:\t" );
 
           if ( ul.extra > 0 )
             appendDurations( summary, analysis.getExtraDurationStringList(), "Extra:\t" );
         }
       }
       else
        summary.append( "** No signal **" );
      summary.append( '\n' );
     }
     
     summaryTextArea.setText( summary.toString() );
   }
   
   
   /*
    * (non-Javadoc)
    * 
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed( ActionEvent event )
   {
     Object source = event.getSource();
     if ( source == okButton )
     {
       setVisible( false );
     }
   }
   
   private RemoteConfiguration config = null;
 
   private JTextField burstRoundBox = new JTextField();
   
   /** The ok button. */
   private JButton okButton = new JButton( "OK" );
 
   private JTextArea summaryTextArea = new JTextArea( 30, 80 );
 
   /** The dialog. */
   private static LearnedSignalTimingSummaryDialog dialog = null;
     
 }
