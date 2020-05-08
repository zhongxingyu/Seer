 /*
  * ContactEdit.java
  *
  * Created on August 5, 2007, 8:02 PM
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package com.totsp.gwittir.example.client;
 
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.totsp.gwittir.client.beans.Converter;
 import com.totsp.gwittir.client.ui.AbstractBoundWidget;
 import com.totsp.gwittir.client.ui.BoundWidget;
 import com.totsp.gwittir.client.ui.Button;
 import com.totsp.gwittir.client.ui.Label;
 import com.totsp.gwittir.client.ui.Renderer;
 import com.totsp.gwittir.client.ui.TextBox;
 import com.totsp.gwittir.client.ui.table.BoundTable;
 import com.totsp.gwittir.client.ui.table.Field;
 import com.totsp.gwittir.client.ui.util.BoundWidgetProvider;
 import com.totsp.gwittir.client.ui.util.BoundWidgetTypeFactory;
 import com.totsp.gwittir.example.client.remote.Address;
 import com.totsp.gwittir.example.client.remote.StateLookup;
 import com.totsp.gwittir.example.client.remote.TypeLookup;
 import java.util.List;
 
 /**
  *
  * @author cooper
  */
 public class ContactEdit extends AbstractBoundWidget{
     
     private VerticalPanel p = new VerticalPanel();
     private Field[] addressCols = new Field[6];
     TextBox firstName = new TextBox();
     TextBox lastName = new TextBox();
     TextBox notes = new TextBox();
     BoundTable addresses;
     Button newAddress = new Button("New Address");
     BoundWidgetTypeFactory factory = new BoundWidgetTypeFactory();
     private Field[] phoneCols = new Field[2];
     BoundTable phoneNumbers;
     Button newPhone = new Button("New Phone Number");
     
     /** Creates a new instance of ContactEdit */
     public ContactEdit() {
         super();
         super.initWidget( p );
         FlexTable base = new FlexTable();
         base.setWidth("100%");
         base.setStyleName("example-ContactEdit");
         base.setWidget(0,0, new Label("First Name:") );
         base.setWidget(0,1, firstName );
         base.setWidget(1,0, new Label("LastName:") );
         base.setWidget(1,1, lastName );
         base.setWidget(2, 0, new Label("Notes:") );
         base.getFlexCellFormatter().setColSpan(2,0, 2);
         base.setWidget(3, 0, notes );
         base.getFlexCellFormatter().setColSpan(3,0, 2 );
         p.add( base );
         p.add( new Label("Addresses:") );
         addressCols[0] = new Field( "type", "Type" );
         addressCols[1] = new Field( "address1", "Address" );
         addressCols[2] = new Field( "address2", "" );
         addressCols[3] = new Field( "city", "City");
         addressCols[4] = new Field( "state", "State" );
         addressCols[5] = new Field( "zip", "Zip" );
         
         factory.add( StateLookup.class, new BoundWidgetProvider(){
             public BoundWidget get() {
                 Label label = new Label();
                 label.setRenderer( new Renderer(){
                     public Object render(Object o) {
                         return o == null ? "" : ((StateLookup) o).code;
                     }
                     
                 });
                 return label;
             }
             
         });
         
         factory.add( TypeLookup.class, new BoundWidgetProvider(){
             public BoundWidget get() {
                 TextBox label = new TextBox();
                 label.setRenderer( new Renderer(){
                     public Object render(Object o) {
                         return o == null ? "" : ((TypeLookup) o).name;
                     }
                     
                 });
                 return label;
             }
             
         });
         factory.add( String.class, BoundWidgetTypeFactory.LABEL_PROVIDER );
         factory.add( Address.class, new BoundWidgetProvider(){
             public BoundWidget get() {
                 AddressEdit e = new AddressEdit();
                 e.setAction( new AddressEditAction() );
                 return e;
             }
             
         });
         
         addresses = new BoundTable( BoundTable.HEADER_MASK +
                 BoundTable.SORT_MASK +
                 BoundTable.NO_SELECT_CELL_MASK 
                 + BoundTable.NO_SELECT_COL_MASK
                 + BoundTable.INSERT_WIDGET_MASK
                 , factory  );
         addresses.setColumns( addressCols );
         
         addresses.setWidth("500px");
         p.add( addresses );
         p.add( this.newAddress );
         p.add( new Label( "Phone Numbers: ") );
         
        phoneCols[0] = new Field("type", "Type");
        phoneCols[1] = new Field("number", "Number" );
         
         
         BoundWidgetTypeFactory phoneFactory = new BoundWidgetTypeFactory(true);
         phoneFactory.add( TypeLookup.class, TypeSelectorProvider.INSTANCE );
         
         this.phoneNumbers = new BoundTable( BoundTable.HEADER_MASK + 
                BoundTable.SORT_MASK +
                 BoundTable.NO_SELECT_CELL_MASK +
                 BoundTable.NO_SELECT_COL_MASK, phoneFactory );
         phoneNumbers.setColumns( phoneCols );
         p.add( this.phoneNumbers );
         p.add( this.newPhone );
         
         
         
         
         
         
     }
     
     
     public Object getValue(){
         return this.getModel();
     }
     
     public void setValue(Object value){
         this.setModel( value );
     }
 }
