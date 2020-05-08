 /**
  * author: Maxim Dybarskiy
  * date:   25.02.2010
  * time:   11:48:06
  */
 package max.gmail.notify.options;
 
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import javax.swing.JTextField;
 
 public  class NumberFormatter extends KeyAdapter
 {
     private NumberFormat format;
     private JTextField field;
 
     public NumberFormatter( JTextField field, NumberFormat format )
     {
         super();
         this.field = field;
         this.format = format;
     }
 
     @Override
     public void keyPressed(final KeyEvent e)
     {
         int begin, end, caret;
         if( field.getCaret().getMark() > field.getCaret().getDot() )
         {
             begin = field.getCaret().getDot();
             end = field.getCaret().getMark();
             caret=field.getCaretPosition();
         }
         else
         {
             begin = field.getCaret().getMark();
             end = field.getCaret().getDot();
             caret=field.getCaretPosition() - (end-begin);
         }
 
         if( e.getKeyCode() == KeyEvent.VK_DELETE ||
             e.getKeyCode() == KeyEvent.VK_BACK_SPACE )
         {
             if( e.getKeyCode() == KeyEvent.VK_BACK_SPACE && begin == end )
             {
                 if( caret == 0 )
                         return;
                 field.setText( field.getText().substring( 0,field.getCaretPosition()-1 ) +
                              field.getText().substring( field.getCaretPosition(), field.getText().length() ));
                 field.setCaretPosition( caret-1 );
                 return;
             }
             if( e.getKeyCode() == KeyEvent.VK_DELETE && begin == end )
             {
                 if( caret == field.getText().length() )
                         return;
                 field.setText( field.getText().substring( 0,field.getCaretPosition() ) +
                              field.getText().substring( field.getCaretPosition()+1, field.getText().length() ));
                 field.setCaretPosition( caret );
                 return;
             }
             field.setText( field.getText().substring(0,begin)+
                          field.getText().substring(end,field.getText().length()));
             if( caret > field.getText().length() )
                 caret = field.getText().length();
             field.setCaretPosition( caret );
         }
         if ( ( e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9 ) ||
              (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9) ||
              ( (e.getKeyCode() == KeyEvent.VK_PERIOD || e.getKeyCode() == KeyEvent.VK_DECIMAL ) &&
          !format.isParseIntegerOnly() &&
            field.getText().indexOf(( e.getKeyChar() ) ) == -1 ) )
         {
             if( e.getKeyChar() == '>' || e.getKeyChar() == '<' )
                     return;
             if( begin == end )
                 field.setText( field.getText().substring( 0,field.getCaretPosition() ) +
                              e.getKeyChar() +
                              field.getText().substring( field.getCaretPosition(), field.getText().length()) );
             else
                 field.setText( field.getText().substring( 0,begin ) +
                              e.getKeyChar() +
                              field.getText().substring( end, field.getText().length()) );
             if( caret+1 > field.getText().length() )
                 caret = field.getText().length()-1;
             field.setCaretPosition( caret+1 );
         }
     }
 
     @Override
     public void keyTyped( KeyEvent e )
     {
         field.getCaret().setVisible( true );
     }
 
     public Number getValue() {
         try {
            return format.parse(field.getText());
         } catch (ParseException ex) {}
         return null;
     }
 }
