 package com.myrontuttle.fin.trade.web.panels;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 import com.myrontuttle.fin.trade.adapt.Candidate;
 import com.myrontuttle.fin.trade.web.data.DBAccess;
 import com.myrontuttle.fin.trade.web.data.SortableCandidateDataProvider;
 import com.myrontuttle.fin.trade.web.service.EvolveAccess;
 import com.myrontuttle.fin.trade.web.service.PortfolioAccess;
 import com.myrontuttle.fin.trade.web.service.WatchlistAccess;
 
 public class CandidateTablePanel extends Panel {
 	
 	private static final long serialVersionUID = 1L;
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public CandidateTablePanel(String id, String groupId) {
 		super(id);
 
 		List<IColumn<Candidate, String>> columns = new ArrayList<IColumn<Candidate, String>>();
 
 		columns.add(new PropertyColumn<Candidate, String>(new Model<String>("ID"), "candidateId", "candidateId"));
 		columns.add(new PropertyColumn(new Model<String>("Watchlist"), "watchlistId"));
 		columns.add(new AbstractColumn<Candidate, String>(new Model<String>("Watch Symbols")) {
 			@Override
 			public void populateItem(Item<ICellPopulator<Candidate>> cellItem,
 					String componentId, IModel<Candidate> model) {
				Candidate candidate = ((Candidate)getParent().getDefaultModelObject());
				String[] symbols;
 				try {
					symbols = WatchlistAccess.getWatchlistService().
										retrieveHoldings(candidate.getCandidateId(), candidate.getWatchlistId());
					cellItem.add(new Label(componentId, Arrays.toString(symbols)));
 				} catch (Exception e) {
					cellItem.add(new Label(componentId, "No symbols available"));
 				}
 			}
 		});
 		columns.add(new PropertyColumn(new Model<String>("Portfolio"), "portfolioId"));
 		columns.add(new AbstractColumn<Candidate, String>(new Model<String>("Portfolio Value")) {
 			@Override
 			public void populateItem(Item<ICellPopulator<Candidate>> cellItem,
 					String componentId, IModel<Candidate> model) {
				Candidate candidate = ((Candidate)getParent().getDefaultModelObject());
				double value;
 				try {
					value = PortfolioAccess.getPortfolioService().
 								getAvailableBalance(candidate.getCandidateId(), candidate.getPortfolioId());
					cellItem.add(new Label(componentId, String.valueOf(value)));
 				} catch (Exception e) {
					cellItem.add(new Label(componentId, "No Portfolio Value"));
 				}
 			}
 		});
 
 		columns.add(new AbstractColumn<Candidate, String>(new Model<String>("Delete")) {
 			@Override
 			public void populateItem(Item<ICellPopulator<Candidate>> cellItem, String componentId,
 				IModel<Candidate> model) {
 				cellItem.add(new DeleteCandidatePanel(componentId, model));
 			}
 		});
 		
 		//columns.add(new PropertyColumn(new Model<String>("Genome"), "genomeString"));
 
 		DataTable dataTable = new DefaultDataTable<Candidate, String>("candidates", columns,
 				new SortableCandidateDataProvider(groupId), 20);
 
 		add(dataTable);
 	}
 	
 	class DeleteCandidatePanel extends Panel {
 		/**
 		 * @param id component id
 		 * @param model model for contact
 		 */
 		public DeleteCandidatePanel(String id, IModel<Candidate> model) {
 			super(id, model);
 
 			final Form<Candidate> form = new Form<Candidate>("deleteCandidateForm", model);
 			form.add(new Button("delete") {
 				public void onSubmit() {
 					Candidate candidate = ((Candidate)getParent().getDefaultModelObject());
 					EvolveAccess.getEvolveService().deleteCandidateExpression(
 							candidate.getGroupId(), candidate.getGenome());
 					DBAccess.getDAO().removeCandidate(candidate.getCandidateId());
 				}
 			});
 			add(form);
 		}
 	}
 
 }
