 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
  Copyright (C) 2001-2007 by:
  Department of Geography, University of Bonn
  http://www.giub.uni-bonn.de/deegree/
  lat/lon GmbH
  http://www.lat-lon.de
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  Contact:
 
  Andreas Poth
  lat/lon GmbH
  Aennchenstr. 19
  53177 Bonn
  Germany
  E-Mail: poth@lat-lon.de
 
  Prof. Dr. Klaus Greve
  Department of Geography
  University of Bonn
  Meckenheimer Allee 166
  53115 Bonn
  Germany
  E-Mail: greve@giub.uni-bonn.de
  ---------------------------------------------------------------------------*/
 
 package org.deegree.igeo.views.swing.style;
 
 import static org.deegree.igeo.i18n.Messages.get;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import org.deegree.igeo.ChangeListener;
 import org.deegree.igeo.ValueChangedEvent;
 import org.deegree.igeo.settings.GraphicOptions;
 import org.deegree.igeo.style.model.GraphicSymbol;
 import org.deegree.igeo.style.model.SldValues;
 import org.deegree.igeo.style.model.Symbol;
 import org.deegree.igeo.style.model.WellKnownMark;
 import org.deegree.igeo.views.swing.style.renderer.SymbolRenderer;
 import org.deegree.igeo.views.swing.util.panels.PanelDialog;
 
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * <code>SymbolPanel</code>
  * 
  * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  * 
  */
 public class SymbolPanel extends JPanel implements ActionListener {
 
     private static final long serialVersionUID = 5235268293678456809L;
 
     private ChangeListener changeListener;
 
     private GraphicOptions graphicOptions;
 
     private JComboBox markCB;
 
     private JButton editSymbolsBt;
 
     /**
      * @param graphicOptions
      *            the graphic options
      */
     public SymbolPanel( GraphicOptions graphicOptions ) {
         this( null, graphicOptions );
     }
 
     /**
      * @param changeListener
      *            the change listener to inform when the selected symbol has changed
      * @param graphicOptions
      *            the graphic options
      */
     public SymbolPanel( ChangeListener changeListener, GraphicOptions graphicOptions ) {
         this.changeListener = changeListener;
         this.graphicOptions = graphicOptions;
         setLayout( new BorderLayout() );
         init();
         updateSymbolMarkCB();
     }
 
     /**
      * selectes the entry with this sld name
      * 
      * @param wellKnownName
      *            the name of the mark
      */
     public void setValue( String wellKnownName ) {
         for ( int i = 0; i < markCB.getItemCount(); i++ ) {
             Object item = markCB.getItemAt( i );
             if ( item instanceof WellKnownMark && ( (WellKnownMark) item ).getSldName().equals( wellKnownName ) ) {
                 markCB.setSelectedIndex( i );
                 break;
             }
         }
     }
 
     public Symbol getValue() {
         return (Symbol) markCB.getSelectedItem();
     }
 
     /**
      * selects the entry with the same url if available, otherwise the URL will be added as Symbol to the settings
      * 
      * @param onlineResource
      *            the url of the external graphic
      */
     public void setValue( URL onlineResource ) {
         boolean isInList = false;
         for ( int i = 0; i < markCB.getItemCount(); i++ ) {
             Object item = markCB.getItemAt( i );
             if ( item instanceof GraphicSymbol
                  && ( (GraphicSymbol) item ).getUrl().toExternalForm().equals( onlineResource.toExternalForm() ) ) {
                 markCB.setSelectedIndex( i );
                 isInList = true;
                 break;
             }
         }
         if ( !isInList ) {
             try {
                 graphicOptions.addSymbolDefinition( onlineResource.getFile(), onlineResource.toExternalForm() );
                 updateSymbolMarkCB();
                 markCB.setSelectedItem( graphicOptions.getSymbolDefinitions().get( onlineResource.getFile() ) );
             } catch ( MalformedURLException e ) {
                 JOptionPane.showMessageDialog( this, get( "$MD10789" ), get( "$DI10017" ), JOptionPane.ERROR_MESSAGE );
             }
         }
     }
 
     private void init() {
         // init
         // well known
         markCB = new JComboBox();
         markCB.setRenderer( new SymbolRenderer() );
         markCB.addActionListener( this );
 
         editSymbolsBt = new JButton( get( "$MD11835" ) );
         editSymbolsBt.addActionListener( this );
 
         // layout
         FormLayout fl = new FormLayout( "fill:default:grow(1)", "$cpheight, $btheight" );
         DefaultFormBuilder builder = new DefaultFormBuilder( fl );
         CellConstraints cc = new CellConstraints();
 
         builder.add( markCB, cc.xy( 1, 1, CellConstraints.CENTER, CellConstraints.FILL ) );
         builder.add( editSymbolsBt, cc.xy( 1, 2, CellConstraints.CENTER, CellConstraints.CENTER ) );
 
         add( builder.getPanel() );
 
     }
 
     private void updateSymbolMarkCB() {
        Object selectedItem = markCB.getSelectedItem();
         markCB.removeAllItems();
         for ( WellKnownMark mark : SldValues.getWellKnownMarks() ) {
             markCB.addItem( mark );
         }
         try {
             Map<String, GraphicSymbol> symbols = graphicOptions.getSymbolDefinitions();
             List<GraphicSymbol> values = new ArrayList<GraphicSymbol>();
             values.addAll( symbols.values() );
             Collections.sort( (List<GraphicSymbol>) values );
             for ( GraphicSymbol gs : values ) {
                 markCB.addItem( gs );
             }
            if ( selectedItem != null )
                markCB.setSelectedItem( selectedItem );
         } catch ( MalformedURLException e ) {
             JOptionPane.showMessageDialog( this, get( "$MD10788" ), get( "$DI10017" ), JOptionPane.ERROR_MESSAGE );
         }
     }
 
     // //////////////////////////////////////////////////////////////////////////////
     // ACTIONLISTENER
     // //////////////////////////////////////////////////////////////////////////////
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed( ActionEvent e ) {
         if ( e.getSource() == markCB && changeListener != null ) {
             changeListener.valueChanged( new SymbolChangedEvent( (Symbol) markCB.getSelectedItem() ) );
         } else if ( e.getSource() == editSymbolsBt ) {
             PanelDialog dlg = new PanelDialog( new EditSymbollibraryPanel( graphicOptions ), false );
             dlg.setLocationRelativeTo( this );
             dlg.setVisible( true );
            updateSymbolMarkCB();
         }
     }
 
     public class SymbolChangedEvent extends ValueChangedEvent {
 
         private Symbol symbol;
 
         public SymbolChangedEvent( Symbol symbol ) {
             this.symbol = symbol;
         }
 
         @Override
         public Object getValue() {
             return symbol;
         }
     }
 }
