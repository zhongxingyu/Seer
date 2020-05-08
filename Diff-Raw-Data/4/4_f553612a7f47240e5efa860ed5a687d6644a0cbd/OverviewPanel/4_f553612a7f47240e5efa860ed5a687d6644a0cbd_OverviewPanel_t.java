 package org.openengsb.openticket.ui.web;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.TextFilteredPropertyColumn;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.openengsb.openticket.model.Ticket;
 import org.openengsb.openticket.model.TicketDataProvider;
 
 @AuthorizeInstantiation("CASEWORKER")
 public class OverviewPanel extends BasePage {
 
     
     public OverviewPanel(){
         List<IColumn<Ticket>> columns=new ArrayList<IColumn<Ticket>>();
   
         columns.add(new TextFilteredPropertyColumn<Ticket,String>(Model.of("Id"),"id","id"));
         columns.add(new TextFilteredPropertyColumn<Ticket,String>(Model.of("Type"),"type","type"));
         
         TicketDataProvider dataProvider=new TicketDataProvider();
         
         FilterForm form=new FilterForm("form",dataProvider);
         
         DefaultDataTable<Ticket> dataTable = new DefaultDataTable<Ticket>("dataTable",columns,dataProvider,10);
         dataTable.addTopToolbar(new FilterToolbar(dataTable,form,dataProvider));
         form.add(dataTable);
         
         add(form);
     }
 }
