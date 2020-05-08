 package br.edu.ufcg.pra.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 
 
 
 
 import java.sql.Time;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.apache.bcel.generic.RETURN;
 
 
 
 
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestBuilder.Method;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DefaultDateTimeFormatInfo;
 import com.google.gwt.cell.client.DateCell;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsonUtils;
 import com.google.gwt.dom.builder.shared.FieldSetBuilder;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.logical.shared.OpenEvent;
 import com.google.gwt.event.logical.shared.OpenHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.datepicker.client.DateBox;
 import com.google.gwt.user.datepicker.client.DatePicker;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 
 class ListaPedidos extends JavaScriptObject {                              // (1)
 	  // Overlay types always have protected, zero argument constructors.
 	  protected ListaPedidos() {}                                              // (2)
 
 	  // JSNI methods to get stock data.
 	  public final native String status() /*-{ return this.status; }-*/; // (3)
 	  public final native String[] getPedidos() /*-{ return this.pedidos; }-*/;
 
 	 
 	}
 
 
 class ListaPermissoes extends JavaScriptObject {                              // (1)
 	  // Overlay types always have protected, zero argument constructors.
 	  protected ListaPermissoes() {}                                              // (2)
 
 	  // JSNI methods to get stock data.
 	  public final native String status() /*-{ return this.status; }-*/; // (3)
 	  public final native String[] getPermissoes() /*-{ return this.permissoes; }-*/;
 
 	 
 	}
 
 class ListaPedidosForTable extends JavaScriptObject {                              // (1)
 	  // Overlay types always have protected, zero argument constructors.
 	  protected ListaPedidosForTable() {}                                              // (2)
 
 	  // JSNI methods to get stock data.
 	  public final native String getStatus() /*-{ return this.status; }-*/; // (3)
 	  public final native String getCursor() /*-{ return this.cursor; }-*/; // (3)
 	  public final native Pedido[] getPedidos() /*-{ return this.pedidos; }-*/;
 
 
 	 
 	}
 
 class LegalidadeDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected LegalidadeDados() {}  
     
     public final native String getParecer() /*-{ return this.parecer; }-*/;
     public final native String getDataEnvio() /*-{ return this.data_envio; }-*/;
     public final native String getDataRetorno() /*-{ return this.data_retorno; }-*/;
 } 
 
 class AutorizacaoDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected AutorizacaoDados() {}  
     
     public final native String getParecer() /*-{ return this.parecer; }-*/;
 } 
 
 class CorretudeDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected CorretudeDados() {}  
     
     public final native String getDescricao() /*-{ return this.descricao; }-*/;
     public final native String getQuantitativo() /*-{ return this.quantitativo; }-*/;
     public final native String getCotacao() /*-{ return this.cotacao; }-*/;
     public final native String getData() /*-{ return this.data; }-*/;
 } 
 
 class MinutaDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected MinutaDados() {}  
     
     public final native String[] getParecer() /*-{ return this.parecer; }-*/;
     public final native String[] getDataInicio() /*-{ return this.data_inicio; }-*/;
     public final native String[] getDataEnvio() /*-{ return this.data_envio; }-*/;
     public final native String[] getDataRetorno() /*-{ return this.data_retorno; }-*/;
     
     public final String getParecer(int i) {
     	if(i < getParecer().length && i > -1) return getParecer()[i];
     	else return new String("");
     }
     public final String getDataInicio(int i) {
     	if(i < getDataInicio().length && i > -1) return getDataInicio()[i];
     	else return new String("");
     }
     public final String getDataEnvio(int i) {
     	if(i < getDataEnvio().length && i > -1) return getDataEnvio()[i];
     	else return new String("");
     }
     public final String getDataRetorno(int i) {
     	if(i < getDataRetorno().length && i > -1) return getDataRetorno()[i];
     	else return new String("");
     }
 
 	public final int indiceAtual() {
 		return Math.max(Math.max(Math.max(getParecer().length, getDataInicio().length), getDataEnvio().length), getDataRetorno().length)-1;
 	}
     
     
 } 
 
 class PregaoDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected PregaoDados() {}  
     
     public final native String[] getParecer() /*-{ return this.parecer; }-*/;
     public final native String[] getData() /*-{ return this.data; }-*/;
     public final native String[] getNumero() /*-{ return this.numero; }-*/;
     public final native String[] getLicitacaoData() /*-{ return this.licitacao_data; }-*/;
     public final native String getIndiceTexto() /*-{ return this.indice; }-*/;
     
     public final int getIndice(){
     	return Integer.parseInt(getIndiceTexto());
     }
     
     public final String getParecer(int i) {
     	if(i < getParecer().length && i > -1 ) return getParecer()[i];
     	else return new String("");
     }
     public final String getData(int i) {
     	if(i < getData().length && i > -1 ) return getData()[i];
     	else return new String("");
     }
     public final String getNumero(int i) {
     	if(i < getNumero().length && i > -1) return getNumero()[i];
     	else return new String("");
     }
     public final String getLicitacaoData(int i) {
     	if(i < getLicitacaoData().length && i > -1) return getLicitacaoData()[i];
     	else return new String("");
     }
 
 	public final int indiceAtual() {
 		return getIndice();
 	}
     
 }
 
 class AdjudicacaoDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected AdjudicacaoDados() {}  
     
     public final native String getData() /*-{ return this.data; }-*/;
 }
 
 class HomologacaoDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected HomologacaoDados() {}  
     
     public final native String getData() /*-{ return this.data; }-*/;
 }
 
 class PublicacaoDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected PublicacaoDados() {}  
     
     public final native String getData() /*-{ return this.data; }-*/;
 }
 class DetalhamentoDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected DetalhamentoDados() {}  
     
     public final native String getParecer() /*-{ return this.parecer; }-*/;
     public final native String getData() /*-{ return this.data; }-*/;
 }
 
 class EmpenhoDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected EmpenhoDados() {}  
     
     public final native String getData() /*-{ return this.data; }-*/;
 }
 class NotaAlmoxarifadoDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected NotaAlmoxarifadoDados() {}  
     
     public final native String getData() /*-{ return this.data; }-*/;
 }
 class PatrimonioDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected PatrimonioDados() {}  
     
     public final native String getData() /*-{ return this.data; }-*/;
 }
 
 class NotaContabilidadeDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected NotaContabilidadeDados() {}  
     
     public final native String getData() /*-{ return this.data; }-*/;
 }
 class LiquidacaoDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected LiquidacaoDados() {}  
     
     public final native String getData() /*-{ return this.data; }-*/;
 }
 class PagamentoDados extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected PagamentoDados() {}  
     
     public final native String getData() /*-{ return this.data; }-*/;
 }
 
 class Historico extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected Historico() {}  
     
     public final native HistoricoDados getHistorico() /*-{ return this.historico; }-*/;
 }
 
 
 class HistoricoDados extends JavaScriptObject {
 	
 	protected HistoricoDados() {}
 	
 	public final native String[] getData() /*-{ return this.data; }-*/;
 	public final native String[] getInfo() /*-{ return this.info; }-*/;
 	public final native String[] getUser() /*-{ return this.user; }-*/;
 
 	public final int size() {
 		return getUser().length;
 	}
 }
 
 class Temp {
 	public static HistoricoDados temp;
 	public Temp(HistoricoDados temp) {
 		this.temp = temp;
 	}
 }
 
 
 class Pedido extends JavaScriptObject {                              // (1)
     // Overlay types always have protected, zero argument constructors.
     protected Pedido() {}                                              // (2)
 
     // JSNI methods to get stock data.
     public final native String getDemandante() /*-{ return this.demandante; }-*/; // (3)
     public final native String getNumero() /*-{ return this.numero; }-*/;
     public final native String getEmail() /*-{ return this.email_demandante; }-*/;
     public final native String getLocal() /*-{ return this.local; }-*/;
     public final native String getData() /*-{ return this.data_entrada; }-*/;
     
     public final String getDataFormatada() {
          DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");
          DateTimeFormat format2 = DateTimeFormat.getFormat("dd/MM/yyyy' 'HH:mm:ss");
          return format2.format(format.parse(getData()));
     }
     
     public final native String getDescricao() /*-{ return this.descricao; }-*/;
 
    
     
     public final native LegalidadeDados getLegalidade() /*-{ return this.legalidade; }-*/;
     
     public final native AutorizacaoDados getAutorizacao() /*-{ return this.autorizacao; }-*/;
 
     public final native CorretudeDados getCorretude() /*-{ return this.corretude; }-*/;
    
     public final native MinutaDados getMinuta() /*-{ return this.minuta; }-*/;
 
     public final native PregaoDados getPregao() /*-{ return this.pregao; }-*/;
 
     public final native AdjudicacaoDados getAdjudicacao() /*-{ return this.adjudicacao; }-*/;
 
     public final native HomologacaoDados getHomologacao() /*-{ return this.homologacao; }-*/;
 
     public final native PublicacaoDados getPublicacao() /*-{ return this.publicacao; }-*/;
 
     public final native DetalhamentoDados getDetalhamento() /*-{ return this.detalhamento; }-*/;
 
     public final native EmpenhoDados getEmpenho() /*-{ return this.empenho; }-*/;
 
     public final native NotaAlmoxarifadoDados getNotaAlmoxarifado() /*-{ return this.nota_almoxarifado; }-*/;
 
     public final native PatrimonioDados getPatrimonio() /*-{ return this.patrimonio; }-*/;
 
     public final native NotaContabilidadeDados getNotaContabilidade() /*-{ return this.nota_contabilidade; }-*/;
 
     public final native LiquidacaoDados getLiquidacao() /*-{ return this.liquidacao; }-*/;
 
     public final native PagamentoDados getPagamento() /*-{ return this.pagamento; }-*/;
 
	public final void getHistorico(final 	SimplePanel content) {
 		
 		
 		
 		 RequestBuilder builder5 = new RequestBuilder(RequestBuilder.GET, "gethistorico?q="+getNumero());
 
 		 try {
 			Request request = builder5.sendRequest(null, new RequestCallback() {
 
 				@Override
 				public void onResponseReceived(Request request, Response response) {
 					if (200 == response.getStatusCode()) {
 						Historico historico = JsonUtils.safeEval(response.getText()).cast();
						HistoricoDados dados = historico.getHistorico();
						String historicoListagem = "";
				        for(int i = 0; i < dados.size(); i++){
				        	 String data = dados.getData()[i];
				             DateTimeFormat format1 = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");
				             DateTimeFormat format2 = DateTimeFormat.getFormat("dd/MM/yyyy' 'HH:mm:ss");
				        	 historicoListagem += format2.format(format1.parse(data.substring(0, 19))) + " " + dados.getInfo()[i] + " por " + dados.getUser()[i] + "<br>";
				        
				         }
				        content.clear();
				        content.add(new HTML(historicoListagem)) ;
 			         	 }
 					
 				}
 
 				@Override
 				public void onError(Request request, Throwable exception) {
 					// TODO Auto-generated method stub
 					
 				}
 				 
 			 });
 		} catch (RequestException e) {
 			
 		}
 	}

	
 	
   }
 
 class Pedido2 extends JavaScriptObject {                              // (1)
 	  // Overlay types always have protected, zero argument constructors.
 	  protected Pedido2() {}                                              // (2)
 
 	  // JSNI methods to get stock data.
 	  public final native String getDemandante() /*-{ return this.demandante; }-*/; // (3)
 	  public final native String getNumero() /*-{ return this.numero; }-*/;
 	  public final native String getDescricao() /*-{ return this.descricao; }-*/;
 	  public final native String getData() /*-{ return this.data_entrada; }-*/;
 	  	
 	}
 
 
 
 class User extends JavaScriptObject {                              // (1)
 	  // Overlay types always have protected, zero argument constructors.
 	  protected User() {}                                              // (2)
 
 	  // JSNI methods to get stock data.
 	  public final native String getEmail() /*-{ return this.user_email; }-*/; // (3)
 	  public final native String getNickname() /*-{ return this.user_nickname; }-*/;
 	  public final native String getId() /*-{ return this.user_user_id; }-*/;
 	  public final native String getStatus() /*-{ return this.status; }-*/;
 	  public final native String getUrl() /*-{ return this.url; }-*/;
 }
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Sistema_PRA implements EntryPoint {
 	
 	protected String url = "";
 	protected String status = "";
 	
 	
 		  
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 
 
 			 //     new Contact("John", new Date(80, 4, 12), "123 Fourth Avenue"),
 			  //    new Contact("Joe", new Date(85, 2, 22), "22 Lance Ln"),
 			  //    new Contact("George", new Date(46, 6, 6), "1600 Pennsylvania Avenue"));
 			
 		
 		geraLista("", "");
 		
 		HTML carregando = new HTML("<div id=\"carregando\"><center>Carregando...</center></div>");
 		RootPanel.get().add(carregando);
 		final Label nameField = new Label();
 		nameField.setText("");
 
 		// Add the nameField and sendButton to the RootPanel
 		// Use RootPanel.get() to get the entire body element
 		RootPanel.get("user_on_top_bar").add(nameField);
 		
 		//pedidosPanel();
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/LoginHandler");
 		//RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/teste.json");
 		
         try {
           Request request = builder.sendRequest(null, new RequestCallback() {
             
 
 		
 
 			public void onError(Request request, Throwable exception) {
             }
 
             public void onResponseReceived(Request request, Response response) {
               if (200 == response.getStatusCode()) {
             	  Anchor sendAnchor;
                   ClickHandler clickAnchor = new ClickHandler() {
                   	public void onClick(ClickEvent event) {
           		    	  Window.Location.replace(url);
                   	}
                   };
             	 User user = JsonUtils.safeEval(response.getText()).cast();
             	 status = user.getStatus();
             	 if(status.equals("Connected")){
             		 sendAnchor = new Anchor("Logout");
             		 nameField.setText(user.getEmail());
             		 url = user.getUrl();
             	 }
             	 else{	
             		 url = user.getUrl();
             	 	 sendAnchor = new Anchor("Login");
             	 }
             	sendAnchor.addClickHandler(clickAnchor);
                 RootPanel.get("user_on_top_bar").add(sendAnchor);
 
               } else {
             	  
               }
               
             }
           });
         } catch (RequestException e) {
         }		
         
         RequestBuilder builder6 = new RequestBuilder(RequestBuilder.GET, "/Permissoes");
 
         try {
           Request request = builder6.sendRequest(null, new RequestCallback() {
             
 
 			private String[] listaa;
 
 			public void onError(Request request, Throwable exception) {
             }
 
             public void onResponseReceived(Request request, Response response) {
               if (200 == response.getStatusCode()) {
             	 ListaPermissoes lista = JsonUtils.safeEval(response.getText()).cast();
             	 listaa = lista.getPermissoes();
             	 HorizontalPanel hPanel = new HorizontalPanel();
             	 final String[] listaa = lista.getPermissoes();
               	 /*for (int i = 0; i < listaa.length; i++) {
               		 if(listaa[i].equals("acompanhar_qualquer")||listaa[i].equals("acompanhar_proprios")){
 	              		 hPanel.add(new Button("Pedidos",  new ClickHandler() {
 	           		        public void onClick(ClickEvent event) {
 	              		    	RootPanel.get("main_right").clear();
 	              		    	RootPanel.get("main_left").clear();	
 	              		    	pedidosPanel();
 	           		        }}));
               		} else if(listaa[i].equals("criacao")) {
               			hPanel.add(new Button("Cadastrar pedido",  new ClickHandler() {
 	           		        public void onClick(ClickEvent event) {
 	              		    	RootPanel.get("main_right").clear();
 	              		    	RootPanel.get("main_left").clear();	
 	              		    	cadastraPanel();
 	           		        }}));
               			
               			
               		} else hPanel.add(new Button(listaa[i]));
               		
               	 RootPanel.get("top_bar").add(hPanel);
               	}*/
               	
             	 
             	 
               } else {
             	  
               }
             }
           });
         } catch (RequestException e) {
         }
 		
         
 
 		RootPanel.get().remove(carregando);
 		
 		
 	}
 
 	private void geraLista(String search, String cursor) {
 		RequestBuilder builder5;
 		if(cursor.equals("")) 
 			builder5 = new RequestBuilder(RequestBuilder.GET, "searchpedido?q="+search);
 		else
 			builder5 = new RequestBuilder(RequestBuilder.GET, "searchpedido?q="+search+"&cursor="+cursor);
         try {
           Request request = builder5.sendRequest(null, new RequestCallback() {
             
 
 			private String[] listaa;
 
 			public void onError(Request request, Throwable exception) {
             }
 
             public void onResponseReceived(Request request, Response response) {
               if (200 == response.getStatusCode()) {
             	 ListaPedidosForTable lista = JsonUtils.safeEval(response.getText()).cast();
             	 boolean connected = lista.getStatus().equals("Connected")? true: false;
             	 Pedido[] cada = lista.getPedidos();
             	 final ArrayList<Pedido> listaa = new ArrayList<Pedido>();
             	 for(Pedido pedido: cada) listaa.add(pedido);
             	 
             	 CriaExibeTable(listaa, connected, lista.getCursor());
               	
             	 
             	 
               } else {
             	  
               }
             }
           });
         } catch (RequestException e) {
         }
 	}
 	
 	
 	
 	
 
 	      
 
 
 
 	private void CriaExibeTable(final ArrayList<Pedido> listaa, final boolean connected, final String cursor) {
 		
 		// Create a CellTable.
 	    final CellTable<Pedido> table = new CellTable<Pedido>();
 	    //table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
 	    RootPanel.get("main_bottom").clear();
 	    RootPanel.get("main_bottom_in").clear();
 	    // Add a text column to show the name.
 	    TextColumn<Pedido> nameColumn = new TextColumn<Pedido>() {
 	   
 		@Override
 		public String getValue(Pedido object) {
 			// TODO Auto-generated method stub
 			return object.getDemandante();
 		}
 	    };
 	    table.addColumn(nameColumn, "Nome do demandante");
 
 	   /* // Add a date column to show the birthday.
 	    DateCell dateCell = new DateCell();
 	    Column<Contact, Date> dateColumn = new Column<Contact, Date>(dateCell) {
 	      @Override
 	      public Date getValue(Contact object) {
 	        return object.birthday;
 	      }
 	    };
 	    table.addColumn(dateColumn, "Birthday");
 */
 	    // Add a text column to show the address.
 	    TextColumn<Pedido> addressColumn = new TextColumn<Pedido>() {
 	      @Override
 	      public String getValue(Pedido object) {
 	        return object.getNumero();
 	      }
 	    };
 	    table.addColumn(addressColumn, "Número");
 
 
 	    // Add a text column to show the address.
 	    TextColumn<Pedido> descricaoColumn = new TextColumn<Pedido>() {
 	      @Override
 	      public String getValue(Pedido object) {
 	        return object.getDescricao().substring(0, 20);
 	      }
 	    };
 	    table.addColumn(descricaoColumn, "Descrição");	
 
 
 	    // Add a text column to show the address.
 	    TextColumn<Pedido> dataColumn = new TextColumn<Pedido>() {
 	      @Override
 	      public String getValue(Pedido object) {
 	    	  DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");
 		         DateTimeFormat format2 = DateTimeFormat.getFormat("dd/MM/yyyy");
 		         return format2.format(format.parse(object.getData())); 
 	      }
 	    };
 	    table.addColumn(dataColumn, "Data");
 	    
 	    //ProgressBar exampleBar1 = new ProgressBar(30, 100, 30);
 	    
 	    // Add a selection model to handle user selection.
 	    final SingleSelectionModel<Pedido> selectionModel = new SingleSelectionModel<Pedido>();
 	    table.setSelectionModel(selectionModel);
 	    selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 	      public void onSelectionChange(SelectionChangeEvent event) {
 	       Pedido selected = selectionModel.getSelectedObject();
 	        if (selected != null) {
 	        	if(!connected)
 	        		exibeDialogBox(selected);
 	        	else 
 	        		exibeTelaCompleta(selected);
 	        	    
 	       }
 	      }
 
 	      		private void exibeDialogBox(final Pedido selected) {
 			
 			RequestBuilder builder6 = new RequestBuilder(RequestBuilder.GET, "getpedido?q="+selected.getNumero());
             try {
 	            Request request = builder6.sendRequest(null, new RequestCallback() {
 	             
 	  			public void onError(Request request, Throwable exception) {
 	              }
 
 	              public void onResponseReceived(Request request, Response response) {
 	                if (200 == response.getStatusCode()) {
 	                Pedido todo = JsonUtils.safeEval(response.getText()).cast();
 	                	// Window.alert("You selected: Pedido " + selected.getNumero());
 	       	         Widget barra = barrinhaSemNomes(todo);
 	       	         // Create the dialog box
 	       	         final DialogBox dialogBox = new DialogBox(true);
 	       	         dialogBox.center();
 	       	         dialogBox.setText("Detalhes do pedido");
 	       	         // Create a table to layout the content
 	       	         VerticalPanel dialogContents = new VerticalPanel();
 	       	         dialogContents.setSpacing(4);
 	       	         dialogBox.setWidget(dialogContents);
 	       	         // Add some text to the top of the dialog
 	       	         dialogContents.add(barra);
 	       	         dialogContents.add(new Label("Número do pedido: "+todo.getNumero()));
 	       	         dialogContents.add(new Label("Nome do demandante: "+todo.getDemandante()));
 	       	         dialogContents.add(new Label("Email do demandante: "+todo.getEmail()));
 	       	         dialogContents.add(new Label("Data de entrada: "+todo.getDataFormatada()));
 	       	         dialogContents.add(new Label("Local atual: "+todo.getLocal()));
 	       	         dialogContents.add(new Label("Descrição do pedido: "+todo.getDescricao()));
 	       	         dialogContents.setCellHorizontalAlignment(
 	       	             barra, HasHorizontalAlignment.ALIGN_CENTER);
 
 	       	         dialogBox.setGlassEnabled(true);
 	       	         dialogBox.setAnimationEnabled(true);
 	       	         dialogBox.show();
 	                	
 	                	
 	                	
 	                } else {
 	              	  
 	                }
 	              }
 
 	            });
 	          } catch (RequestException e) {
 	          }
             
 			 
 			
 		
 			
 		}
 	    });
 
 	    table.setWidth("100%");
 	    // Set the total row count. This isn't strictly necessary, but it affects
 	    // paging calculations, so its good habit to keep the row count up to date.
 	    table.setRowCount(listaa.size(), true);
 
 	    // Push the data into the widget.
 	    table.setRowData(0, listaa);
 
 	    
 	    table.setPageSize(listaa.size());
 	   // table.setVisibleRange(0, listaa.size());
 	    // Add it to the root panel.
 	    
 	    
 	    RootPanel.get("main_bottom").add(table);
 	   // RootPanel.get("main_bottom").add(exampleBar1);
 	    
 		
 		
 	    // Create a Pager to control the table.
 	    //SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
 	    //SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
 	    //pager.setDisplay(table);
 	    HTML sobra = new HTML("<br><br><br>");
 	    final MaisButton mais = new MaisButton(cursor, listaa, table, sobra);
 	    /*		
 	    		//new Button("Mais Resultados");
 	    mais.addClickHandler(new ClickHandler(
 	    		) {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				
 				RequestBuilder builder5;
 				builder5 = new RequestBuilder(RequestBuilder.GET, "searchpedido?cursor="+mais.getCursorInterno());
 		        try {
 		          Request request = builder5.sendRequest(null, new RequestCallback() {
 		            
 
 					public void onError(Request request, Throwable exception) {
 		            }
 
 		            public void onResponseReceived(Request request, Response response) {
 		              if (200 == response.getStatusCode()) {
 		            	 ListaPedidosForTable lista = JsonUtils.safeEval(response.getText()).cast();
 		            	 
 		            	 boolean connected = lista.getStatus().equals("Connected")? true: false;
 		            	 Pedido[] cada = lista.getPedidos();
 		            	 
 		            	 
 		            	 mais.removeFromParent();
 		            	 RootPanel.get("main_bottom").add(new MaisButton(lista.getCursor()));
 		            	 
 		            	 
 		            	 ArrayList<Pedido> cada2 = new ArrayList<Pedido>();
 		            	 for (int i = 0; i < cada.length; i++) {
 		            		 listaa.add(cada[i]);
 						
 		            		 table.setWidth("100%");
 		     			    // Set the total row count. This isn't strictly necessary, but it affects
 		     			    // paging calculations, so its good habit to keep the row count up to date.
 		     			 table.setRowCount(listaa.size(), true);
 
 		     			    // Push the data into the widget.
 		     			 table.setRowData(0, listaa);
 		     			 	table.setPageSize(listaa.size());
 		     				//geraLista("", cursor);
 		     				
 		            	 }
 		            		 
 		              	
 		            	 
 		            	 
 		              } else {
 		            	  
 		              }
 		            }
 		          });
 		        } catch (RequestException e) {
 		        }
 				
 				
 				
 				
 				
 				
 				
 				
 				
 				
 				
 			}
 		});*/
 	    
 	    RootPanel.get("main_bottom").add(mais);
 	   // RootPanel.get("main_bottom").add(new HTML("<br><br><br>"));
 	    FlowPanel hPanel = new FlowPanel();
 	    HorizontalPanel cadastrarPanel = new HorizontalPanel();
 	    
 	   // hPanel.setStyleName("input-append");
 	    final TextBox campoPesquisa = new TextBox();
 	    campoPesquisa.setSize("450px", "30px");
 	    campoPesquisa.addKeyDownHandler(new KeyDownHandler() {
 			
 	    	@Override
 			public void onKeyDown(KeyDownEvent event) {
 				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 					 geraLista(campoPesquisa.getText(), ""); 
 					
 				}}
 		});
 	   // campoPesquisa.setStyleName("span2");
 	   // DOM.setElementAttribute(campoPesquisa.getElement(), "id", "appendedInputButton");
 	    Button botaoPesquisa = new Button("<img src=\"images/search.png\"></a> Pesquisar", new ClickHandler() {
 		        public void onClick(ClickEvent event) { 
 		        	final SimplePanel buscando =  new SimplePanel();
 		        	buscando.addStyleName("buscando");
 		        	buscando.add(new HTML("<img src=\"images/415.gif\" width=\"4%\"></a>"));
 		        	RootPanel.get().add(buscando);
 		        	geraLista(campoPesquisa.getText(), "");
 		        	Timer timer = new Timer() {
 						
 						@Override
 						public void run() {
 							buscando.removeFromParent();
 						}
 					};
 					timer.schedule(500);
 		        	}});
 	    //botaoPesquisa.setStyleName("btn");
 	    botaoPesquisa.setHeight("30px");
 	    hPanel.add(campoPesquisa);
 	    hPanel.add(botaoPesquisa);
 	    if(status.equals("Connected")){
 	    Button cadastrarButton = new Button("Cadastrar pedido",  new ClickHandler() {
 	        public void onClick(ClickEvent event) {
 	        	RootPanel.get("main_bottom").clear();
 	        	RootPanel.get("main_bottom_in").clear();
   		    	cadastraPanel();
 	        }});
 	    cadastrarButton.setSize("140px", "30px");
 	    cadastrarPanel.add(cadastrarButton);
 	    }
 	    /*Button alteraLegalidadeButton = new Button("Legalidade",  new ClickHandler() {
             public void onClick(ClickEvent event) {
                     RootPanel.get("main_bottom").clear();
                     geraListaLegalidade("legalidade:indefinida");
             }});
 	    alteraLegalidadeButton.setSize("140px", "30px");
 	    hPanel.add(alteraLegalidadeButton);*/
 	    RootPanel.get("main_top").clear();
 	    RootPanel.get("main_cadastrar").clear();
 		RootPanel.get("main_top").add(hPanel);
 		RootPanel.get("main_cadastrar").add(cadastrarPanel);
 	}
 	
 	
 	
 	private void alteraPedidoPanel() {
 		final VerticalPanel vPanel = new VerticalPanel();
 		
 		vPanel.add(new HTML("Carregando pedidos...<br>"));
 		
 		RequestBuilder builder2 = new RequestBuilder(RequestBuilder.GET, "/Pedido");
       try {
         Request request = builder2.sendRequest(null, new RequestCallback() {
           
 
 			public void onError(Request request, Throwable exception) {
           }
 
           public void onResponseReceived(Request request, Response response) {
             if (200 == response.getStatusCode()) {
           	 ListaPedidos lista = JsonUtils.safeEval(response.getText()).cast();
           	 vPanel.clear();
           	vPanel.add(new HTML("Pedidos<br>"));
           	 final String[] listaa = lista.getPedidos();
           	 for (int i = 0; i < listaa.length; i++) {
           		 final RequestBuilder builder3 = new RequestBuilder(RequestBuilder.GET, "/getpedido?q="+listaa[i]);
           		 vPanel.add(new Button("Pedido "+ listaa[i], new ClickHandler() {
        		        public void onClick(ClickEvent event) {
        		    	RootPanel.get("main_right").clear();
        		    	final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
        		    	simplePopup.ensureDebugId("cwBasicPopup-simplePopup");
        		    	simplePopup.setWidth("150px");
        		     	simplePopup.setWidget(new HTML("Carregando <br> por favor, espere!"));
        		     	// Reposition the popup relative to the button
        	            Widget source = (Widget) event.getSource();
        	            int left = source.getAbsoluteLeft() + 10;
        	            int top = source.getAbsoluteTop() + 10;
        	            simplePopup.setPopupPosition(left, top);
 
        	            // Show the popup
        	            simplePopup.show();
        		    	  
        		      try {
        		        Request request = builder3.sendRequest(null, new RequestCallback() {
        		          
 
        					public void onError(Request request, Throwable exception) {
        		          }
 
        		          public void onResponseReceived(Request request, Response response) {
        		            if (200 == response.getStatusCode()) {
        		            	simplePopup.hide();
        		            	RootPanel.get("main_right").clear();
        		            	
        		            	DecoratorPanel decPanel = new DecoratorPanel();
        		            	final FlexTable layout = new FlexTable();
        		            	layout.setCellSpacing(6);
        		            	User user = JsonUtils.safeEval(response.getText()).cast();
        		            	Pedido pedido = JsonUtils.safeEval(response.getText()).cast();
        		            	Label Nome = new Label();
        		            	Nome.setText(pedido.getDemandante());
        		            	Label numero = new Label();
        		            	numero.setText(pedido.getNumero());
        		            	Label email = new Label();
        		            	email.setText(pedido.getEmail());
        		            	Label local = new Label();
        		            	local.setText(pedido.getLocal());
        		            	Label descricao = new Label();
        		            	descricao.setText(pedido.getDescricao());
        		     		    layout.setWidget(0, 1, numero);
        		     		    layout.setWidget(1, 1, Nome);
        		     		    layout.setWidget(2, 1, email);
        		     		    layout.setWidget(3, 1, local);
        		     		    layout.setWidget(4, 1, descricao);
        		            	decPanel.setWidget(layout);
        		            	RootPanel.get("main_right").add(decPanel);
        		          	 
        		            } else {
        		          	  
        		            }
        		          }
        		        });
        		      } catch (RequestException e) {
        		      }
        		    	  
        		    	  
        		    	  
        		    	  
     		        }
     		      }));
           		
           		 
           	 }
           	 
             } else {
           	  
             }
           }
         });
       } catch (RequestException e) {
       }
 		
 		
 		
 		
 		
 		RootPanel.get("main_left").add(vPanel);
 	}
 	
 	
 	
 	
 	/*
 	private HTML barrinha(Pedido p){
        	
        	
        	
        	
        	
        	
        	
        	String barraProgresso = "<table border='0' bordecolor='#000000' " +
        							 "style='background-color:#999999' width='100%' " +
        							 "cellpadding='10' cellspacing='1'><tr>";
        	
        	//LEGALIDADE
        	if (p.getLegalidade().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Legalidade: em andamento'>1</td>";
        	}
        	else if (p.getLegalidade().equals("legal")) {
        		barraProgresso += "<td style='background-color:#33CC33' title='Legalidade: legal'>1</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#FF0000' title='Legalidade: ilegal'>1</td>";
        	}
        	
        	//AUTORIZACAO
        	if (p.getAutorizacao().equals("indefinido") && p.getLegalidade().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Autorizacao: indefinido'>2</td>";
        	}
        	else if (p.getAutorizacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Autorizacao: em andamento'>2</td>";
        	}
        	else if (p.getAutorizacao().equals("autorizado")) {
        		barraProgresso += "<td style='background-color:#33CC33' title='Autorizacao: autorizado'>2</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#FF0000' title='Autorizacao: nao autorizado'>2</td>";
        	}
        	
        	//CONFERENCIA
        	if (p.getConferencia().equals("indefinido") && p.getAutorizacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Conferencia: indefinido'>3</td>";
        	}
        	else if (p.getConferencia().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Conferencia: em andamento'>3</td>";
        	}
        	else if (p.getConferencia().equals("completo")) {
        		barraProgresso += "<td style='background-color:#33CC33' title='Conferencia: completo'>3</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#FF0000' title='Conferencia: incompleto'>3</td>";
        	}
        	
        	//MINUTA
        	if (p.getMinuta().equals("indefinido") && p.getConferencia().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Minuta: indefinido'>4</td>";
        	}
        	else if (p.getMinuta().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Minuta: em andamento'>4</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Minuta: elaborada'>4</td>";
        	}
        	
        	//LEGALIDADE DOS MATERIAIS
        	if (p.getLegalidadeMateriais().equals("indefinido") && p.getMinuta().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Legalidade dos materiais: indefinido'>5</td>";
        	}
        	else if (p.getLegalidadeMateriais().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Legalidade dos materiais: em andamento'>5</td>";
        	}
        	else if (p.getLegalidadeMateriais().equals("legal")) {
        		barraProgresso += "<td style='background-color:#33CC33' title='Legalidade dos materiais: legal'>5</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#FF0000' title='Legalidade dos materiais: ilegal'>5</td>";
        	}
        	
        	//PREGAO
        	if (p.getPregao().equals("indefinido") && p.getLegalidadeMateriais().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Pregao: indefinido'>6</td>";
        	}
        	else if (p.getPregao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Pregao: em andamento'>6</td>";
        	}
        	else if (p.getPregao().equals("informado no SIASE e Comprasnet")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Pregao: informado no SIASE e Comprasnet'>6</td>";
        	}
        	else if (p.getPregao().equals("Edital publicado")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Pregao: edital publicado'>6</td>";
        	}
        	else if (p.getPregao().equals("Data marcada")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Pregao: data marcada'>6</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Pregao: realizado'>6</td>";
        	}
        	
        	//LICITACAO
        	if (p.getLicitacao().equals("indefinido") && p.getPregao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Licitacao: indefinido'>7</td>";
        	}
        	else if (p.getLicitacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Licitacao: em andamento'>7</td>";
        	}
        	else if (p.getLicitacao().equals("em avaliacao")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Licitacao: em avaliacao'>7</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Licitacao: concluida'>7</td>";
        	}
        	
        	//ADJUDICACAO
        	if (p.getAdjudicacao().equals("indefinido") && p.getLicitacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Adjudicacao: indefinido'>8</td>";
        	}
        	else if (p.getAdjudicacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Adjudicacao: em andamento'>8</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Adjudicacao: termo elaborado'>8</td>";
        	}
        	
        	//HOMOLOGACAO
        	if (p.getHomologacao().equals("indefinido") && p.getAdjudicacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Homologacao: indefinido'>9</td>";
        	}
        	else if (p.getHomologacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Homologacao: em andamento'>9</td>";
        	}
        	else if (p.getHomologacao().equals("homologado")) {
        		barraProgresso += "<td style='background-color:#33CC33' title='Homologacao: homologado'>9</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#FF0000' title='Homologacao: nao homologado'>9</td>";
        	}
        	
        	//PUBLICACAO
        	if (p.getPublicacao().equals("indefinido") && p.getHomologacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Publicacao: indefinido'>10</td>";
        	}
        	else if (p.getPublicacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Publicacao: em andamento'>10</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Publicacao: publicado'>10</td>";
        	}
        	
        	//MINUTA DE EMPENHO
        	if (p.getMinutaEmpenho().equals("indefinido") && p.getPublicacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Minuta de empenho: indefinido'>11</td>";
        	}
        	else if (p.getMinutaEmpenho().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Minuta de empenho: em andamento'>11</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Minuta de empenho: elaborada'>11</td>";
        	}
        	
        	//DETALHAMENTO
        	if (p.getDetalhamento().equals("indefinido") && p.getMinutaEmpenho().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Detalhamento: indefinido'>12</td>";
        	}
        	else if (p.getDetalhamento().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Detalhamento: em andamento'>12</td>";
        	}
        	else if (p.getDetalhamento().equals("no SEPLAN")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Detalhamento: no SEPLAN'>12</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Detalhamento: detalhado'>12</td>";
        	}
        	
        	//EMPENHO
        	if (p.getEmpenho().equals("indefinido") && p.getDetalhamento().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Empenho: indefinido'>13</td>";
        	}
        	else if (p.getEmpenho().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Empenho: em andamento'>13</td>";
        	}
        	else if (p.getEmpenho().equals("recebido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Empenho: recebido'>13</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Empenho: empenhado'>13</td>";
        	}
        	
        	//NOTA DO ALMOXARIFADO
        	if (p.getNotaAlmoxarifado().equals("indefinido") && p.getEmpenho().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Nota do almoxarifado: indefinido'>14</td>";
        	}
        	else if (p.getNotaAlmoxarifado().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Nota do almoxarifado: em andamento'>14</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Nota do almoxarifado: enviada'>14</td>";
        	}
        	
        	//TOMBAMENTO
        	if (p.getTombamento().equals("indefinido") && p.getNotaAlmoxarifado().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Tombamento: indefinido'>15</td>";
        	}
        	else if (p.getTombamento().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Tombamento: em andamento'>15</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Tombamento: tombado'>15</td>";
        	}
        	
        	//NOTA DA CONTABILIDADE
        	if (p.getContabilidade().equals("indefinido") && p.getTombamento().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Nota da contabilidade: indefinido'>16</td>";
        	}
        	else if (p.getContabilidade().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Nota da contabilidade: em andamento'>16</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Nota da contabilidade: enviada'>16</td>";
        	}
        	
        	//LIQUIDACAO
        	if (p.getLiquidacao().equals("indefinido") && p.getContabilidade().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Liquidacao: indefinido'>17</td>";
        	}
        	else if (p.getLiquidacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Liquidacao: em andamento'>17</td>";
        	}
        	else if (p.getLiquidacao().equals("servico atestado")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Liquidacao: servico atestado'>17</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Liquidacao: liquidado</td>";
        	}
        	
        	//PAGAMENTO
        	if (p.getPagamento().equals("indefinido") && p.getLiquidacao().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#999999' title='Pagamento: indefinido'>18</td>";
        	}
        	else if (p.getPagamento().equals("indefinido")) {
        		barraProgresso += "<td style='background-color:#FFFF33' title='Pagamento: em andamento'>18</td>";
        	}
        	else {
        		barraProgresso += "<td style='background-color:#33CC33' title='Pagamento: pago'>18</td>";
        	}
        	
        	barraProgresso += "</tr></table><br>";
        	
        	
        	return new HTML(barraProgresso);
        	
 	}
 	*/
 
 	private VerticalPanel pedidosPanel() {
 		final VerticalPanel vPanel = new VerticalPanel();
 		
 		vPanel.add(new HTML("Carregando pedidos...<br>"));
 		
 		RequestBuilder builder2 = new RequestBuilder(RequestBuilder.GET, "/getpedido");
       try {
         Request request = builder2.sendRequest(null, new RequestCallback() {
           
 
 			public void onError(Request request, Throwable exception) {
           }
 
           public void onResponseReceived(Request request, Response response) {
             if (200 == response.getStatusCode()) {
           	 ListaPedidos lista = JsonUtils.safeEval(response.getText()).cast();
           	 vPanel.clear();
           	vPanel.add(new HTML("Pedidos<br>"));
           	 final String[] listaa = lista.getPedidos();
           	 for (int i = 0; i < listaa.length; i++) {
           		 final RequestBuilder builder3 = new RequestBuilder(RequestBuilder.GET, "/getpedido?q="+listaa[i]);
           		 vPanel.add(new Button("Pedido "+ listaa[i], new ClickHandler() {
        		        public void onClick(ClickEvent event) {
        		    	RootPanel.get("main_right").clear();
        		    	final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
        		    	simplePopup.ensureDebugId("cwBasicPopup-simplePopup");
        		    	simplePopup.setWidth("150px");
        		     	simplePopup.setWidget(new HTML("Carregando <br> por favor, espere!"));
        		     	// Reposition the popup relative to the button
        	            Widget source = (Widget) event.getSource();
        	            int left = source.getAbsoluteLeft() + 10;
        	            int top = source.getAbsoluteTop() + 10;
        	            simplePopup.setPopupPosition(left, top);
 
        	            // Show the popup
        	            simplePopup.show();
        		    	  
        		      try {
        		        Request request = builder3.sendRequest(null, new RequestCallback() {
        		          
 
        					public void onError(Request request, Throwable exception) {
        		          }
 
        		          public void onResponseReceived(Request request, Response response) {
        		            if (200 == response.getStatusCode()) {
        		            	simplePopup.hide();
        		            	RootPanel.get("main_right").clear();
        		            	DecoratorPanel decPanel = new DecoratorPanel();
        		            	final FlexTable layout = new FlexTable();
        		            	layout.setCellSpacing(6);
        		            	Pedido pedido = JsonUtils.safeEval(response.getText()).cast();
        		            	Label Nome = new Label();
        		            	Nome.setText(pedido.getDemandante());
        		            	Label numero = new Label();
        		            	numero.setText(pedido.getNumero());
        		            	Label email = new Label();
        		            	email.setText(pedido.getEmail());
        		            	Label local = new Label();
        		            	local.setText(pedido.getLocal());
        		            	Label descricao = new Label();
        		            	descricao.setText(pedido.getDescricao());
        		     		    layout.setWidget(0, 1, numero);
        		     		    layout.setWidget(1, 1, Nome);
        		     		    layout.setWidget(2, 1, email);
        		     		    layout.setWidget(3, 1, local);
        		     		    layout.setWidget(4, 1, descricao);
        		            	decPanel.setWidget(layout);
        		            	RootPanel.get("main_right").add(decPanel);
        		          	 
        		            } else {
        		          	  
        		            }
        		          }
        		        });
        		      } catch (RequestException e) {
        		      }
        		    	  
        		    	  
        		    	  
        		    	  
     		        }
     		      }));
           		
           		 
           	 }
           	 
             } else {
           	  
             }
           }
         });
       } catch (RequestException e) {
       }
 		
 		
 		
 		
 		
 		RootPanel.get("main_left").add(vPanel);
 		return vPanel;
 	}
 	
 
 	
 	
 	private void cadastraPanel() {
 		 
 		
 					
 		VerticalPanel vPanel = new VerticalPanel();
 			final TextBox numero = new TextBox();
             numero.setWidth("250");
 			HorizontalPanel hnumero = new HorizontalPanel();
             hnumero.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
             hnumero.add(new HTML("<p>Número do processo:*</p>"));
 			hnumero.add(numero);
 			final TextBox demandante = new TextBox();
             demandante.setWidth("250");
 			HorizontalPanel hdemandante = new HorizontalPanel();
 			hdemandante.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 			hdemandante.add(new HTML("<p>Nome do demandante:* </p>"));
 			hdemandante.add(demandante);
 			final DateBox data_entrada = new DateBox();
             data_entrada.setWidth("250");
 			HorizontalPanel hdata_entrada = new HorizontalPanel();
             hdata_entrada.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
             hdata_entrada.add(new HTML("<p>Data de entrada:* </p>"));
 			hdata_entrada.add(data_entrada);
 			final TextArea descricao = new TextArea();
             descricao.setWidth("250");
 			HorizontalPanel hdescricao = new HorizontalPanel();
             hdescricao.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
             hdescricao.add(new HTML("<p>Descrição:* </p>"));
 			hdescricao.add(descricao);
 			final TextBox email_demandante = new TextBox();
             email_demandante.setWidth("250");
 			HorizontalPanel hemail = new HorizontalPanel();
             hemail.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
             hemail.add(new HTML("<p>Email do demandante:* </p>"));
 			hemail.add(email_demandante);
 			vPanel.add(new HTML("Cadastro de Pedidos<br>"));
 			HorizontalPanel hObrigatorio = new HorizontalPanel();
 			hObrigatorio.add(new HTML("* = campos obrigatórios"));
             vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 			vPanel.add(hnumero);
 			vPanel.add(hdemandante);
 			vPanel.add(hemail);
 				vPanel.add(hdata_entrada);
 				vPanel.add(hdescricao);
 				vPanel.add(hObrigatorio);
 				vPanel.add(new Button("Enviar", new ClickHandler() {
        		        public void onClick(ClickEvent event) {
 
        		        	if(numero.getText().equals("") || demandante.getText().equals("") || descricao.getText().equals("") || email_demandante.getText().equals("") || data_entrada.getValue() == null){
            		        	final DialogBox aviso = new DialogBox();
            		        	aviso.setAutoHideEnabled(true);
            		        	aviso.center();
            		        	aviso.add(new Label("Todos os campos obrigatorios devem ser preenchidos"));
            		        	aviso.setGlassEnabled(true);
            		            aviso.setAnimationEnabled(true);
            		            aviso.show();	
        		        	}else{
        		        	DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");
        		        	String parameters = "numero="+numero.getText()+
        		        			"&demandante="+demandante.getText()+
        		        			"&data_entrada="+format.format(data_entrada.getValue())+
        		        			"&descricao="+descricao.getText()+
        		        			"&email_demandante="+email_demandante.getText();
        		        	enviaForm(parameters, numero.getText());}}}));
 
        		        	
 				RootPanel.get("main_bottom").clear();
 				RootPanel.get("main_bottom_in").clear();
 				RootPanel.get("main_bottom").add(vPanel);
 				
 				
 		
        		     
        		    	  
        		    	  
        		
 	}
 	
 	
 	private HTML barrinha(Pedido p) {
         
         String barraProgresso = "<table border='0' bordecolor='#000000' " +
                                                          "style='background-color:#999999' width='100%' " +
                                                          "cellpadding='10' cellspacing='1'><tr>";
        
         //LEGALIDADE
         if (p.getLegalidade().getParecer().equals("null")) {
                 barraProgresso += "<td style='background-color:#FFFF33'><a href='#legalidade' id='anchorBarra' title='Legalidade parecer: em andamento'>Legalidade</a></td>";
         }
         else if (p.getLegalidade().getParecer().equals("true")) {
                 barraProgresso += "<td style='background-color:#33CC33'><a href='#legalidade'id='anchorBarra' title='Legalidade parecer: legal. Data de envio a PJ: " + p.getLegalidade().getDataEnvio() + " Data de retorno da PJ: " + p.getLegalidade().getDataRetorno() + "'>Legalidade</a></td>";
                 
         }
         else {
                 barraProgresso += "<td style='background-color:#FF0000'><a href='#legalidade' id='anchorBarra' title='Legalidade parecer: ilegal. Data de envio a PJ: " +
                 p.getLegalidade().getDataEnvio() + " Data de retorno da PJ: " + p.getLegalidade().getDataRetorno() + "'>Legalidade</a></td>";
         }
        
         //AUTORIZACAO
         if (p.getAutorizacao().getParecer().equals("null") && p.getLegalidade().getParecer().equals("null")) {
                 barraProgresso += "<td style='background-color:#999999'><a href='#autorizacao' id='anchorBarra' title='Autorizacao parecer: indefinido'>Autorização</a></td>";
         }
         else if (p.getAutorizacao().getParecer().equals("null")) {
                 barraProgresso += "<td style='background-color:#FFFF33'><a href='#autorizacao' id='anchorBarra' title='Autorizacao parecer: em andamento'>Autorização</a></td>";
         }
         else if (p.getAutorizacao().getParecer().equals("true")) {
                 barraProgresso += "<td style='background-color:#33CC33'><a href='#autorizacao' id='anchorBarra' title='Autorizacao parecer: autorizado'>Autorização</a></td>";
         }
         else {
                 barraProgresso += "<td style='background-color:#FF0000'><a href='#autorizacao' id='anchorBarra' title='Autorizacao parecer: nao autorizado'>Autorização</a></td>";
         }
        
         //CORRETUDE
         
  
         if (p.getCorretude().getDescricao().equals("true") && p.getCorretude().getQuantitativo().equals("true") && p.getCorretude().getCotacao().equals("true")) {
                 barraProgresso += "<td style='background-color:#33CC33'><a href='#corretude' id='anchorBarra' title='Corretude: completo. Data da definicao: " +
                 p.getCorretude().getData() + "'>Corretude</a></td>";
         }
         else if ((p.getCorretude().getDescricao().equals("false") == false && p.getCorretude().getQuantitativo().equals("false") == false && p.getCorretude().getCotacao().equals("false") == false)) {
                 if (p.getAutorizacao().getParecer().equals("null"))
                         barraProgresso += "<td style='background-color:#999999'><a href='#corretude' id='anchorBarra' title='Corretude: indefinida'>Corretude</a></td>";
                 else
                         barraProgresso += "<td style='background-color:#FFFF33'><a href='#corretude' id='anchorBarra' title='Corretude: em andamento'>Corretude</a></td>";
         }
         else {
         		String statuss = "";
                 if (p.getCorretude().getDescricao().equals("true"))
                 	 statuss += "Corretude da descricao: correto <br>";
                 else
                 	statuss += "Corretude da descricao: incorreto<br>";
  
                 if (p.getCorretude().getQuantitativo().equals("true"))
                 	statuss += "Corretude do quantitativo: correto<br>";
                 else
                 	statuss +="Corretude do quantitativo: incorreto<br>";
  
                 if (p.getCorretude().getCotacao().equals("true"))
                 	statuss += "Corretude da cotacao: correto<br>";
                 else
                 	statuss += "Corretude da cotacao: incorreto<br>";
                 barraProgresso += "<td style='background-color:#FF0000'><a href='#corretude' id='anchorBarra' title='"+statuss+"'>Corretude</a></td>";
              
         }
        
         //MINUTA
         
         
         int iInicio = p.getMinuta().getDataInicio().length - 1;
         int iEnvio = p.getMinuta().getDataEnvio().length - 1;
         int iRetorno = p.getMinuta().getDataRetorno().length - 1;
         int iParecerMinuta = p.getMinuta().getParecer().length - 1;
         
         //gambiarra {
         String dataMinutaInicio = "";
         if (p.getMinuta().getDataInicio().length > 0) dataMinutaInicio = p.getMinuta().getDataInicio()[iInicio];
         String dataMinutaEnvio = "";
         if (p.getMinuta().getDataEnvio().length > 0) dataMinutaEnvio = p.getMinuta().getDataEnvio()[iEnvio];
         String dataMinutaRetorno = "";
         if (p.getMinuta().getDataRetorno().length > 0) dataMinutaRetorno = p.getMinuta().getDataRetorno()[iRetorno];
         //} gambiarra
         
         if (p.getMinuta().getParecer().length > 0) {
             if (p.getMinuta().getParecer()[iParecerMinuta].equals("null") && p.getCorretude().getCotacao().equals("null")) {
                     barraProgresso += "<td style='background-color:#999999'><a href='#minuta' id='anchorBarra' title='Minuta parecer: indefinido'>Minuta</a></td>";
             }
             else if (p.getMinuta().getParecer()[iParecerMinuta].equals("null")) {
                     barraProgresso += "<td style='background-color:#FFFF33'><a href='#minuta' id='anchorBarra' title='Minuta parecer: em andamento'>Minuta</a></td>";
             }
             else if (p.getMinuta().getParecer()[iParecerMinuta].equals("true")) {
                     barraProgresso += "<td style='background-color:#33CC33'><a href='#minuta' id='anchorBarra' title='Minuta parecer: de acordo. Data do inicio da elaboracao: " +
                     dataMinutaInicio + " Data de envio a PJ: " + dataMinutaEnvio + " Data de retorno: " +
                     dataMinutaRetorno + "'>Minuta</a></td>";
             }
             else {
                     barraProgresso += "<td style='background-color:#FF0000'><a href='#minuta' id='anchorBarra' title='Minuta parecer: nao de acordo. Data do inicio da elaboracao" +
                     ": " + dataMinutaInicio + " Data de envio a PJ: " + dataMinutaEnvio +
                     " Data de retorno: " + dataMinutaRetorno + "'>Minuta</a></td>";
             }
         }
         else {
         	barraProgresso += "<td style='background-color:#999999'><a href='#minuta' id='anchorBarra' title='Minuta parecer: indefinido'>Minuta</a></td>";
         }
        
         //PREGAO
         
         int iParecerPregao = p.getPregao().getParecer().length - 1;
         int iParecerData = p.getPregao().getData().length - 1;
         
         //TODO gambiarra {
         String dataPregao = "";
         if (p.getPregao().getData().length > 0) dataPregao = p.getPregao().getData()[iParecerData];
         //TODO } gambiarra
  
         if (p.getPregao().getParecer().length > 0) {
             if (p.getPregao().getParecer()[iParecerPregao].equals("null") && p.getMinuta().getParecer()[iParecerMinuta].equals("null")) {
                     barraProgresso += "<td style='background-color:#999999'><a href='#pregao' id='anchorBarra' title='Pregao parecer: indefinido'>Pregão</a></td>";
             }
             else if (p.getPregao().getParecer()[iParecerPregao].equals("null")) {
                     barraProgresso += "<td style='background-color:#FFFF33'><a href='#pregao' id='anchorBarra' title='Pregao parecer: em andamento'>Pregão</a></td>";
             }
             else if (p.getPregao().getParecer()[iParecerPregao].equals("true")) {
                     barraProgresso += "<td style='background-color:#33CC33'><a href='#pregao' id='anchorBarra' title='Pregao parecer: realizado. Data de realizacao: " +
                     dataPregao + "'>Pregão</a></td>";
             }
             else {
                     barraProgresso += "<td style='background-color:#FF0000'><a href='#pregao' id='anchorBarra' title='Pregao parecer: nao realizado'>Pregão</a></td>";
             }
         }
         else {
         	barraProgresso += "<td style='background-color:#999999'><a href='#pregao' id='anchorBarra' title='Pregao parecer: indefinido'>Pregão</a></td>";
         }
        
         //ADJUDICACAO
         if (p.getAdjudicacao().getData().equals("") && (p.getPregao().getParecer(iParecerPregao).equals("null") || p.getPregao().getParecer(iParecerPregao).equals("")) ) {
                 barraProgresso += "<td style='background-color:#999999'><a href='#adjudicacao' id='anchorBarra' title='Adjudicacao: indefinida'>Adjudicação</a></td>";
         }
         else if (p.getAdjudicacao().getData().equals("")) {
                 barraProgresso += "<td style='background-color:#FFFF33'><a href='#adjudicacao' id='anchorBarra' title='Adjudicacao: em andamento'>Adjudicação</a></td>";
         }
         else {
                 barraProgresso += "<td style='background-color:#33CC33'><a href='#adjudicacao' id='anchorBarra' title='Adjudicacao: concluida Data: " +
                 p.getAdjudicacao().getData() + "'>Adjudicação</a></td>";
         }
  	
         //HOMOLOGACAO
         if (p.getHomologacao().getData().equals("") && p.getAdjudicacao().getData().equals("")) {
                 barraProgresso += "<td style='background-color:#999999'><a href='#homologacao' id='anchorBarra' title='Homologacao: indefinida'>Homologação</a></td>";
         }
         else if (p.getHomologacao().getData().equals("")) {
                 barraProgresso += "<td style='background-color:#FFFF33'><a href='#homologacao' id='anchorBarra' title='Homologacao: em andamento'>Homologação</a></td>";
         }
         else {
                 barraProgresso += "<td style='background-color:#33CC33'><a href='#homologacao' id='anchorBarra' title='Homologacao: concluida Data: " +
                 p.getHomologacao().getData() + "'>Homologação</a></td>";
         }
  
         //PUBLICACAO
         if (p.getPublicacao().getData().equals("") && p.getHomologacao().getData().equals("")) {
                 barraProgresso += "<td style='background-color:#999999'><a href='#publicacao' id='anchorBarra' title='Publicacao: indefinida'>Publicação</a></td>";
         }
         else if (p.getPublicacao().getData().equals("")) {
                 barraProgresso += "<td style='background-color:#FFFF33'><a href='#publicacao' id='anchorBarra' title='Publicacao: em andamento'>Publicação</a></td>";
         }
         else {
                 barraProgresso += "<td style='background-color:#33CC33'><a href='#publicacao' id='anchorBarra' title='Publicacao: concluida Data: " +
                 p.getPublicacao().getData() + "'>Publicação</a></td>";
         }
         
       //LIQUIDACAO
         if (p.getLiquidacao().getData().equals("") && p.getPublicacao().getData().equals("")) {
                 barraProgresso += "<td style='background-color:#999999'><a href='#bottom' id='anchorBarra' title='Liquidacao: indefinida'>Liquidação</a></td>";
         }
         else if (p.getLiquidacao().getData().equals("")) {
                 barraProgresso += "<td style='background-color:#FFFF33'><a href='#bottom' id='anchorBarra' title='Liquidacao: em andamento'>Liquidação</a></td>";
         }
         else {
                 barraProgresso += "<td style='background-color:#33CC33'><a href='#bottom' id='anchorBarra' title='Liquidacao: concluida Data: " +
                 p.getHomologacao().getData() + "'>Liquidação</a></td>";
         }
  
         
       //PAGAMENTO
         if (p.getPagamento().getData().equals("") && p.getLiquidacao().getData().equals("")) {
                 barraProgresso += "<td style='background-color:#999999'><a href='#bottom' id='anchorBarra' title='Pagamento: indefinida'>Pagamento</a></td>";
         }
         else if (p.getPagamento().getData().equals("")) {
                 barraProgresso += "<td style='background-color:#FFFF33'><a href='#bottom' id='anchorBarra' title='Pagamento: em andamento'>Pagamento</a></td>";
         }
         else {
                 barraProgresso += "<td style='background-color:#33CC33'><a href='#bottom' id='anchorBarra' title='Pagamento: concluido Data: " +
                 p.getHomologacao().getData() + "'>Pagamento</a></td>";
         }
  
         
         barraProgresso += "</tr></table><br>";
        
         return new HTML(barraProgresso);
         }
 
 	
 	
 	
 
     private HTML barrinhaSemNomes(Pedido p) {
                      
             String barraProgresso = "<table border='0' bordecolor='#000000' " +
                                                              "style='background-color:#999999' width='100%' " +
                                                              "cellpadding='10' cellspacing='1'><tr>";
            
             //LEGALIDADE
             if (p.getLegalidade().getParecer().equals("null")) {
                     barraProgresso += "<td style='background-color:#FFFF33' title='Legalidade parecer: em andamento'>1</td>";
             }
             else if (p.getLegalidade().getParecer().equals("true")) {
                     barraProgresso += "<td style='background-color:#33CC33' title='Legalidade parecer: legal<br>Data de envio a PJ: " + p.getLegalidade().getDataEnvio() + "<br>Data de retorno da PJ: " + p.getLegalidade().getDataRetorno() + "'>1</td>";
             }
             else {
                     barraProgresso += "<td style='background-color:#FF0000' title='Legalidade parecer: ilegal<br>Data de envio a PJ: " +
                     p.getLegalidade().getDataEnvio() + "<br>Data de retorno da PJ: " + p.getLegalidade().getDataRetorno() + "'>1</td>";
             }
            
             //AUTORIZACAO
             if (p.getAutorizacao().getParecer().equals("null") && p.getLegalidade().getParecer().equals("null")) {
                     barraProgresso += "<td style='background-color:#999999' title='Autorizacao parecer: indefinido'>2</td>";
             }
             else if (p.getAutorizacao().getParecer().equals("null")) {
                     barraProgresso += "<td style='background-color:#FFFF33' title='Autorizacao parecer: em andamento'>2</td>";
             }
             else if (p.getAutorizacao().getParecer().equals("true")) {
                     barraProgresso += "<td style='background-color:#33CC33' title='Autorizacao parecer: autorizado'>2</td>";
             }
             else {
                     barraProgresso += "<td style='background-color:#FF0000' title='Autorizacao parecer: nao autorizado'>2</td>";
             }
            
             //CORRETUDE
             
      
             if (p.getCorretude().getDescricao().equals("true") && p.getCorretude().getQuantitativo().equals("true") && p.getCorretude().getCotacao().equals("true")) {
                     barraProgresso += "<td style='background-color:#33CC33' title='Corretude: completo<br>Data da definicao: " +
                     p.getCorretude().getData() + "'>3</td>";
             }
             else if ((p.getCorretude().getDescricao().equals("false") == false && p.getCorretude().getQuantitativo().equals("false") == false && p.getCorretude().getCotacao().equals("false") == false)) {
                     if (p.getAutorizacao().getParecer().equals("null"))
                             barraProgresso += "<td style='background-color:#999999' title='Corretude: indefinida'>3</td>";
                     else
                             barraProgresso += "<td style='background-color:#FFFF33' title='Corretude: em andamento'>3</td>";
             }
             else {
             		String statuss = "";
                     if (p.getCorretude().getDescricao().equals("true"))
                     	 statuss += "Corretude da descricao: correto <br>";
                     else
                     	statuss += "Corretude da descricao: incorreto<br>";
      
                     if (p.getCorretude().getQuantitativo().equals("true"))
                     	statuss += "Corretude do quantitativo: correto<br>";
                     else
                     	statuss +="Corretude do quantitativo: incorreto<br>";
      
                     if (p.getCorretude().getCotacao().equals("true"))
                     	statuss += "Corretude da cotacao: correto<br>";
                     else
                     	statuss += "Corretude da cotacao: incorreto<br>";
                     barraProgresso += "<td style='background-color:#FF0000' title='"+statuss+"'>3</td>";
                  
             }
            
             //MINUTA
             
             
             int iInicio = p.getMinuta().getDataInicio().length - 1;
             int iEnvio = p.getMinuta().getDataEnvio().length - 1;
             int iRetorno = p.getMinuta().getDataRetorno().length - 1;
             int iParecerMinuta = p.getMinuta().getParecer().length - 1;
             
             //gambiarra {
             String dataMinutaInicio = "";
             if (p.getMinuta().getDataInicio().length > 0) dataMinutaInicio = p.getMinuta().getDataInicio()[iInicio];
             String dataMinutaEnvio = "";
             if (p.getMinuta().getDataEnvio().length > 0) dataMinutaEnvio = p.getMinuta().getDataEnvio()[iEnvio];
             String dataMinutaRetorno = "";
             if (p.getMinuta().getDataRetorno().length > 0) dataMinutaRetorno = p.getMinuta().getDataRetorno()[iRetorno];
             //} gambiarra
             
             if (p.getMinuta().getParecer().length > 0) {
 	            if (p.getMinuta().getParecer()[iParecerMinuta].equals("null") && p.getCorretude().getCotacao().equals("null")) {
 	                    barraProgresso += "<td style='background-color:#999999' title='Minuta parecer: indefinido'>4</td>";
 	            }
 	            else if (p.getMinuta().getParecer()[iParecerMinuta].equals("null")) {
 	                    barraProgresso += "<td style='background-color:#FFFF33' title='Minuta parecer: em andamento'>4</td>";
 	            }
 	            else if (p.getMinuta().getParecer()[iParecerMinuta].equals("true")) {
 	                    barraProgresso += "<td style='background-color:#33CC33' title='Minuta parecer: de acordo<br>Data do inicio da elaboracao: " +
 	                    dataMinutaInicio + "<br>Data de envio a PJ: " + dataMinutaEnvio + "<br>Data de retorno: " +
 	                    dataMinutaRetorno + "'>4</td>";
 	            }
 	            else {
 	                    barraProgresso += "<td style='background-color:#FF0000' title='Minuta parecer: nao de acordo<br>Data do inicio da elaboracao" +
 	                    ": " + dataMinutaInicio + "<br>Data de envio a PJ: " + dataMinutaEnvio +
 	                    "<br>Data de retorno: " + dataMinutaRetorno + "'>4</td>";
 	            }
             }
             else {
             	barraProgresso += "<td style='background-color:#999999' title='Minuta parecer: indefinido'>4</td>";
             }
            
             //PREGAO
             
             int iParecerPregao = p.getPregao().getParecer().length - 1;
             int iParecerData = p.getPregao().getData().length - 1;
             
             //TODO gambiarra {
             String dataPregao = "";
             if (p.getPregao().getData().length > 0) dataPregao = p.getPregao().getData()[iParecerData];
             //TODO } gambiarra
      
             if (p.getPregao().getParecer().length > 0) {
 	            if (p.getPregao().getParecer()[iParecerPregao].equals("null") && p.getMinuta().getParecer()[iParecerMinuta].equals("null")) {
 	                    barraProgresso += "<td style='background-color:#999999' title='Pregao parecer: indefinido'>5</td>";
 	            }
 	            else if (p.getPregao().getParecer()[iParecerPregao].equals("null")) {
 	                    barraProgresso += "<td style='background-color:#FFFF33' title='Pregao parecer: em andamento'>5</td>";
 	            }
 	            else if (p.getPregao().getParecer()[iParecerPregao].equals("true")) {
 	                    barraProgresso += "<td style='background-color:#33CC33' title='Pregao parecer: realizado<br>Data de realizacao: " +
 	                    dataPregao + "'>5</td>";
 	            }
 	            else {
 	                    barraProgresso += "<td style='background-color:#FF0000' title='Pregao parecer: nao realizado'>5</td>";
 	            }
             }
             else {
             	barraProgresso += "<td style='background-color:#999999' title='Minuta parecer: indefinido'>4</td>";
             }
            
             //ADJUDICACAO
             if (p.getAdjudicacao().getData().equals("") && (p.getPregao().getParecer(iParecerPregao).equals("null") || p.getPregao().getParecer(iParecerPregao).equals("")) ) {
                     barraProgresso += "<td style='background-color:#999999' title='Adjudicacao: indefinida'>6</td>";
             }
             else if (p.getAdjudicacao().getData().equals("")) {
                     barraProgresso += "<td style='background-color:#FFFF33' title='Adjudicacao: em andamento'>6</td>";
             }
             else {
                     barraProgresso += "<td style='background-color:#33CC33' title='Adjudicao: concluida<br>Data: " +
                     p.getAdjudicacao().getData() + "'>6</td>";
             }
      	
             //HOMOLOGACAO
             if (p.getHomologacao().getData().equals("") && p.getAdjudicacao().getData().equals("")) {
                     barraProgresso += "<td style='background-color:#999999' title='Homologacao: indefinida'>7</td>";
             }
             else if (p.getHomologacao().getData().equals("")) {
                     barraProgresso += "<td style='background-color:#FFFF33' title='Homologacao: em andamento'>7</td>";
             }
             else {
                     barraProgresso += "<td style='background-color:#33CC33' title='Homologacao: concluida<br>Data: " +
                     p.getHomologacao().getData() + "'>7</td>";
             }
      
             //PUBLICACAO
             if (p.getPublicacao().getData().equals("") && p.getHomologacao().getData().equals("")) {
                     barraProgresso += "<td style='background-color:#999999' title='Publicacao: indefinida'>8</td>";
             }
             else if (p.getPublicacao().getData().equals("")) {
                     barraProgresso += "<td style='background-color:#FFFF33' title='Publicacao: em andamento'>8</td>";
             }
             else {
                     barraProgresso += "<td style='background-color:#33CC33' title='Publicacao: concluida<br>Data: " +
                     p.getPublicacao().getData() + "'>8</td>";
             }
             
           //LIQUIDACAO
             if (p.getLiquidacao().getData().equals("") && p.getPublicacao().getData().equals("")) {
                     barraProgresso += "<td style='background-color:#999999' title='Liquidacao: indefinida'>9</td>";
             }
             else if (p.getLiquidacao().getData().equals("")) {
                     barraProgresso += "<td style='background-color:#FFFF33' title='Liquidacao: em andamento'>9</td>";
             }
             else {
                     barraProgresso += "<td style='background-color:#33CC33' title='Liquidacao: concluida<br>Data: " +
                     p.getHomologacao().getData() + "'>9</td>";
             }
      
             
           //PAGAMENTO
             if (p.getPagamento().getData().equals("") && p.getLiquidacao().getData().equals("")) {
                     barraProgresso += "<td style='background-color:#999999' title='Pagamento: indefinida'>10</td>";
             }
             else if (p.getPagamento().getData().equals("")) {
                     barraProgresso += "<td style='background-color:#FFFF33' title='Pagamento: em andamento'>10</td>";
             }
             else {
                     barraProgresso += "<td style='background-color:#33CC33' title='Pagamento: concluido<br>Data: " +
                     p.getHomologacao().getData() + "'>10</td>";
             }
      
             
             barraProgresso += "</tr></table><br>";
            
             return new HTML(barraProgresso);
             }
 
 
     private void exibeTelaCompleta(Pedido selected) {
 	         // Create the dialog box
     	final SimplePanel buscando =  new SimplePanel();
     	buscando.addStyleName("buscando");
     	buscando.add(new HTML("<img src=\"images/495.gif\" width=\"4%\"></a>"));
     	RootPanel.get().add(buscando);
     	
     
   	  RequestBuilder builder5 = new RequestBuilder(RequestBuilder.GET, "getpedido?q="+selected.getNumero());
 
         try {
           Request request = builder5.sendRequest(null, new RequestCallback() {
            
 			public void onError(Request request, Throwable exception) {
             }
 
             @SuppressWarnings("deprecation")
 			public void onResponseReceived(Request request, Response response) {
               if (200 == response.getStatusCode()) {
             	 final Pedido pedido = JsonUtils.safeEval(response.getText()).cast();
             	 VerticalPanel vPanel = new VerticalPanel();
             	 SimplePanel folha = new SimplePanel();
             	 folha.setStyleName("folha");
             	 VerticalPanel pedacos = new VerticalPanel();
             	 pedacos.setSpacing(10);
             	 folha.add(pedacos);
             	 RootPanel.get("main_bottom_in").clear();
             	 RootPanel.get("main_bottom_in").add(folha);
             	 DecoratorPanel decPanel = new DecoratorPanel();
 		         //DADOS BASICOS
 		         SubFolhaPanel subfolha = new SubFolhaPanel("Dados básicos");
 		         pedacos.add(subfolha);
 		         subfolha.add(new Label(pedido.getNumero()), "Número do pedido");
 		         subfolha.add(createTextBox("demandante", pedido.getDemandante(), pedido.getNumero()), "Nome do demandante");
 		         subfolha.add(createTextBox("email_demandante",pedido.getEmail(), pedido.getNumero()), "Email do demandante");
 		        subfolha.add(createTextBox("descricao",pedido.getDescricao(), pedido.getNumero()), "Descrição do pedido");
 		     
 		         DateTimeFormat dateFormat = DateTimeFormat.getMediumDateTimeFormat();
 		         final DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");
 		        
 		          
 		         subfolha.add(criaDatePicker(pedido.getData(), "setpedido?numero="+pedido.getNumero()+"&data_entrada="), "Data de entrada");
 		         
 		       
 		         
 		         /*
 		         final ListBox lb = new ListBox();
                lb.addItem("legal");
                lb.addItem("ilegal");
                lb.addChangeHandler(new ChangeHandler() {
 						
 						@Override
 						public void onChange(ChangeEvent event) {
 							if (lb.getItemText(lb.getSelectedIndex()).equals("legal"))
 								AlteraEstado("parecer=True", pedido.getNumero());
 	                        else if (lb.getItemText(lb.getSelectedIndex()).equals("ilegal"))
 	                        	AlteraEstado("parecer=False", pedido.getNumero());
 						}
 					});
                //Set as dropdown
                lb.setVisibleItemCount(lb.getItemCount());
                lb.setSelectedIndex(pedido.getLegalidade().getParecer().equals("false")?1:0);
               	*/
               HorizontalPanel legalidade = createRadioGroup(pedido, "LegalidadeHandler", "LegalidadeHandler", pedido.getLegalidade().getParecer(), "parecer", "Legal", "Ilegal");
               HorizontalPanel autorizacao = createRadioGroup(pedido, "AutorizacaoHandler",  "AutorizacaoHandler",pedido.getAutorizacao().getParecer(), "parecer", "Legal", "Ilegal");
               HorizontalPanel corretudeDescricao = createRadioGroup(pedido, "CorretudeHandler", "CorretudeDescHandler",pedido.getCorretude().getDescricao(), "descricao", "Correta", "Incorreta");
               HorizontalPanel corretudeQuantitativo = createRadioGroup(pedido, "CorretudeHandler", "CorretudeQuantHandler",pedido.getCorretude().getQuantitativo(), "quantitativo", "Correta", "Incorreta");
               HorizontalPanel corretudeCotacao = createRadioGroup(pedido, "CorretudeHandler", "CorretudeCotHandler",pedido.getCorretude().getCotacao(), "cotacao", "Correta", "Incorreta");
 		        
               subfolha = new SubFolhaPanel("Legalidade");
 		      pedacos.add(subfolha);
               subfolha.add(legalidade, "Parecer de Legalidade");
               subfolha.add(criaDatePicker(pedido.getLegalidade().getDataEnvio(), "LegalidadeHandler?pedido="+pedido.getNumero()+"&data_envio="), "Data de envio da Legalidade");
               
               	 subfolha.add(criaDatePicker(pedido.getLegalidade().getDataRetorno(), "LegalidadeHandler?pedido="+pedido.getNumero()+"&data_retorno="), "Data de retorno da Legalidade");
 		        
               	 //AUTORIZACAO
               	 subfolha = new SubFolhaPanel("Autorização");
               	 pedacos.add(subfolha);
 		        
 		         subfolha.add(autorizacao, "Parecer de Autorização");
 		        
 		         subfolha = new SubFolhaPanel("Corretude");
               	 pedacos.add(subfolha);
               	 VerticalPanel hl = new VerticalPanel();
 		         VerticalPanel v1 = new VerticalPanel();
 		         v1.add(new Label("Descrição:"));
 		         v1.add(corretudeDescricao);
 		         VerticalPanel v2 = new VerticalPanel();
 		         v2.add(new Label("Quantitativo:"));
 		         v2.add(corretudeQuantitativo);
 		         VerticalPanel v3 = new VerticalPanel();
 		         v3.add(new Label("Cotação:"));
 		         v3.add(corretudeCotacao);
 		        // hl.setBorderWidth(1);
 		         hl.add(v1);
 		         hl.add(v2);
 		         hl.add(v3);
 		         
 		         
 		         
 		         subfolha.add(hl, "Parecer de Corretude");
 		        
 		         
 		         //CORRETUDE
 		         
 		         subfolha.add(criaDatePicker(pedido.getCorretude().getData(), "CorretudeHandler?pedido="+pedido.getNumero()+"&data="), "Data de definição da corretude");
 		        
 		       
 		         //MINUTA DO EDITAL
 		         int indiceMinuta = pedido.getMinuta().indiceAtual();
 		         
 		        
 		         HorizontalPanel minuta = createRadioGroup(pedido, "MinutaHandler", "Minuta", pedido.getMinuta().getParecer(indiceMinuta), "parecer", "Legal", "Ilegal");
 		        
 		         subfolha = new SubFolhaPanel("Minuta do Edital");
               	 pedacos.add(subfolha);
               	 subfolha.add(minuta, "Parecer da Minuta");
 		         
 		         subfolha.add(criaDatePicker(pedido.getMinuta().getDataInicio(indiceMinuta), "MinutaHandler?pedido="+pedido.getNumero()+"&data_inicio="), "Data de inicio de elaboração da Minuta");
               	 
 		         subfolha.add(criaDatePicker(pedido.getMinuta().getDataEnvio(indiceMinuta), "MinutaHandler?pedido="+pedido.getNumero()+"&data_envio="), "Data de envio da Minuta");
 		        
 		         
 		         subfolha.add(criaDatePicker(pedido.getMinuta().getDataRetorno(indiceMinuta), "MinutaHandler?pedido="+pedido.getNumero()+"&data_retorno="), "Data de retorno");
 		         
 		         int indicePregao = pedido.getPregao().indiceAtual();
 		         
 		         for(int i = 0; i <=  indicePregao+1; i++) {
 		      
 		        	 
 		         subfolha = new SubFolhaPanel("Pregão");
 		         boolean enabled = true;
 				if(i!=indicePregao+1) enabled = false;
               	 pedacos.add(subfolha);
 		         //PREGAO
 		        
 		         final HorizontalPanel pregao = createRadioGroup(pedido, enabled, "PregaoHandler", "Pregao"+i, pedido.getPregao().getParecer(i), "parecer", "Comprado", "Não comprado");
 		         
 		         final TextBox t = new TextBox();
 		         t.setEnabled(enabled);
 		         t.setText(pedido.getPregao().getNumero(i));
 		         t.addChangeHandler(new ChangeHandler() {
 					
 					@Override
 					public void onChange(ChangeEvent event) {
 						RequestBuilder builder6 = new RequestBuilder(RequestBuilder.POST, "PregaoHandler?pedido="+pedido.getNumero()+"&numero="+ t.getText());
 		                try {
 		    	            Request request = builder6.sendRequest(null, new RequestCallback() {
 		    	             
 		    	  			public void onError(Request request, Throwable exception) {
 		    	              }
 
 		    	              public void onResponseReceived(Request request, Response response) {
 		    	                if (200 == response.getStatusCode()) {
 		    	              	 
 		    	                } else {
 		    	              	  
 		    	                }
 		    	              }
 
 		    	            });
 		    	          } catch (RequestException e) {
 		    	          }
 					
 					}
 				});
 		         subfolha.add(t, "Número do Pregão");
 		        
 		         subfolha.add(pregao, "Parecer do Pregão");
 		         
 		         subfolha.add(criaDatePicker(pedido.getPregao().getData(i), enabled,  "PregaoHandler?pedido="+pedido.getNumero()+"&data="), "Data de definição do Pregão");
 		        
 		         
 		         
 		         subfolha.add(criaDatePicker(pedido.getPregao().getLicitacaoData(i),  enabled, "PregaoHandler?pedido="+pedido.getNumero()+"&licitacao_data="), "Data de abertura do Pregão");
 		         
 		         final Button adicionaPregao = new Button("Adicionar Pregão");
 		         adicionaPregao.addClickHandler(new ClickHandler() {
 					
 					@Override
 					public void onClick(ClickEvent event) {
 						RequestBuilder builder5 = new RequestBuilder(RequestBuilder.GET, "AdicionaPregao?q="+pedido.getNumero());
 
 				          
 				           try {
 								builder5.sendRequest(null, new RequestCallback() {
 								 
 								public void onError(Request request2, Throwable exception) {
 								  }
 
 								  @SuppressWarnings("deprecation")
 								public void onResponseReceived(Request request2, Response response) {
 								    if (200 == response.getStatusCode()) {
 								    	Pedido pedido2 = JsonUtils.safeEval(response.getText()).cast();
 								    	exibeTelaCompleta(pedido2);
 								    }
 								  }});
 							} catch (RequestException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						
 					}
 				});
 		         
 		         
 		         Timer timer = new Timer() {
 
 						@Override
 						public void run() {
 							if((!((RadioButton) pregao.getWidget(0)).getValue())) {
 								adicionaPregao.setEnabled(false);
 							} else{
 								adicionaPregao.setEnabled(true);
 							}
 
 						}
 					};
 			timer.scheduleRepeating(1000);
 		         
 		         if(i == indicePregao+1)
 		          subfolha.add(adicionaPregao);
 		         
 		         }
 		         
 		         
 		         subfolha = new SubFolhaPanel("Adjudicação");
               	 pedacos.add(subfolha);
 		        
               	 
               	 subfolha.add(criaDatePicker(pedido.getAdjudicacao().getData(), "AdjudicacaoHandler?pedido="+pedido.getNumero()+"&data="), "Data de Adjudicação");
 		       
               	subfolha = new SubFolhaPanel("Homologação");
              	 pedacos.add(subfolha);
 		        
              	 
              	 subfolha.add(criaDatePicker(pedido.getHomologacao().getData(), "HomologacaoHandler?pedido="+pedido.getNumero()+"&data="), "Data de Homologação");
 		         
              	subfolha = new SubFolhaPanel("Publicação");
             	 pedacos.add(subfolha);
 		        
             	 subfolha.add(criaDatePicker(pedido.getPublicacao().getData(), "PublicacaoHandler?pedido="+pedido.getNumero()+"&data="), "Data de Publicação");
 		     
 		         
             	  
               	subfolha = new SubFolhaPanel("Crédito Orçamentário");
              	 pedacos.add(subfolha);
 		         
              	 
              
 		         //DETALHAMENTO
 		         HorizontalPanel detalhamento = createRadioGroup(pedido, "DetalhamentoHandler", "Detalhamento", pedido.getDetalhamento().getParecer(), "parecer", "Autorizado", "Não autorizado");
 		    	 subfolha.add(detalhamento, "Parecer do Detalhamento de Crédito");
 		    	 
 		    	 subfolha.add(criaDatePicker(pedido.getDetalhamento().getData(), "DetalhamentoHandler?pedido="+pedido.getNumero()+"&data="), "Data de Detalhamento de Crédito");
 		         
 		         
 		    	 subfolha = new SubFolhaPanel("Empenho");
              	 pedacos.add(subfolha);
 		         
              	 subfolha.add(criaDatePicker(pedido.getEmpenho().getData(), "EmpenhoHandler?pedido="+pedido.getNumero()+"&data="), "Data de Empenho");
 		    	 
 		         subfolha.add(criaDatePicker(pedido.getNotaAlmoxarifado().getData(), "NotaAlmoxarifadoHandler?pedido="+pedido.getNumero()+"&data="),"Data de envio do Empenho ao almoxarifado" );
 		         
 		        subfolha.add(criaDatePicker(pedido.getPatrimonio().getData(), "PatrimonioHandler?pedido="+pedido.getNumero()+"&data="), "Data de envio do Empenho ao Patrimônio");
 		        
 		        subfolha = new SubFolhaPanel("Pagamento");
             	 pedacos.add(subfolha);
 		        
 		         subfolha.add(criaDatePicker(pedido.getNotaContabilidade().getData(), "NotaContabilidadeHandler?pedido="+pedido.getNumero()+"&data="), "Data de envio da nota a Contabilidade");
 		        
 		         subfolha.add(criaDatePicker(pedido.getLiquidacao().getData(), "LiquidacaoHandler?pedido="+pedido.getNumero()+"&data="), "Data de Liquidação");
 		         
 		         subfolha.add(criaDatePicker(pedido.getPagamento().getData(), "PagamentoHandler?pedido="+pedido.getNumero()+"&data="), "Data de Pagamento" );
 		        
 		       
 		         subfolha = new SubFolhaHistoricoPanel(pedido);
             	 pedacos.add(subfolha);
		         /*
 		         Tree to = new Tree();
 		        // HTML title = new HTML();
 		        // final TreeItem root = new TreeItem(title);
 		         
 		         TreeItem item = to.addTextItem("Histórico de alterações no pedido");
 
 		         // Temporarily add an item so we can expand this node
 		         item.addTextItem("");
 		         
 		       //HISTORICO
 		        //to.addItem(root);
 		        // final TreeItem item = new TreeItem();
 		         //root.addItem(item);
 		         to.addOpenHandler(new OpenHandler<TreeItem>() {
 					
 					@Override
 					public void onOpen(OpenEvent<TreeItem> event) {
 						TreeItem item = event.getTarget();
 				        if (item.getChildCount() == 1) {
 				          // Close the item immediately
 				          item.setState(false, false);
 
 				         
 				          String historico = "";
 					         for(int i = 0; i < pedido.getHistorico().size(); i++){
 					        	 String data = pedido.getHistorico().getData()[i];
 					             DateTimeFormat format1 = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");
 					             DateTimeFormat format2 = DateTimeFormat.getFormat("dd/MM/yyyy' 'HH:mm:ss");
 					        	 historico += format2.format(format1.parse(data.substring(0, 19))) + " " + pedido.getHistorico().getInfo()[i] + " por " + pedido.getHistorico().getUser()[i] + "<br>";
 					        
 					         }
 					         historico += "<br><br><br>";
 					         
 					         item.addItem(new TreeItem(new HTML(historico)));
 
 				          // Remove the temporary item when we finish loading
 				          item.getChild(0).remove();
 
 				          // Reopen the item
 				          item.setState(true, false);
 				        }
 						
						
 						if(open.equals(root))  {
 							String historico = "";
 					         for(int i = 0; i < pedido.getHistorico().size(); i++){
 					        	 String data = pedido.getHistorico().getData()[i];
 					             DateTimeFormat format1 = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");
 					             DateTimeFormat format2 = DateTimeFormat.getFormat("dd/MM/yyyy' 'HH:mm:ss");
 					        	 historico += format2.format(format1.parse(data.substring(0, 19))) + " " + pedido.getHistorico().getInfo()[i] + " por " + pedido.getHistorico().getUser()[i] + "<br>";
 					        
 					         }
 					         historico += "<br><br><br>";
 					         
 							item.setWidget(new HTML(historico));
							}
 					}
 				});			
 		         
 		         pedacos.add(to);
		         */
 		         pedacos.add(new HTML("<br><br><br>"));
 		         decPanel.setWidget(vPanel);
 		         //folha.add(vPanel);
 		         RootPanel.get("main_bottom").clear();
             	 //RootPanel.get("main_bottom_in").add(decPanel);
             	RootPanel.get("main_top").clear();
 		         RootPanel.get("main_cadastrar").clear();
 		         
 		         Timer autoAtualizar = new Timer() {
 					
 					@Override
 					public void run() {
 						 
 						
 						
 						RequestBuilder builder6 = new RequestBuilder(RequestBuilder.GET, "getpedido?q="+pedido.getNumero());
 						try {
 							Request request = builder6.sendRequest(null, new RequestCallback() {
 							       
 									public void onError(Request request, Throwable exception) {
 							        }
 
 							        @SuppressWarnings("deprecation")
 									public void onResponseReceived(Request request, Response response) {
 							          if (200 == response.getStatusCode()) {
 							        	 final Pedido pedido = JsonUtils.safeEval(response.getText()).cast();
 							        	 RootPanel.get("main_top").clear();
 							        	 RootPanel.get("main_top").add(barrinha(pedido));
 							          }}});
 						} catch (RequestException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 					}
 				};
 		         
 		         autoAtualizar.scheduleRepeating(1000);
 		         
 		         
 		         RootPanel.get("main_top").add(barrinha(pedido));
 		         buscando.removeFromParent();
 		     	
               } else {
             	  
               }
             }
             
             private HorizontalPanel createRadioGroup(final Pedido pedido, final String handler, String group,  String atual, final String dado, String valorVerdade, String valorFalso) {
             	return createRadioGroup(pedido, true, handler, group, atual, dado, valorVerdade, valorFalso);
             }
 
 			private HorizontalPanel createRadioGroup(final Pedido pedido, boolean enabled, final String handler, String group,  String atual, final String dado, String valorVerdade, String valorFalso) {
 				final HorizontalPanel vp = new HorizontalPanel();
                RadioButton radioLegal = new RadioButton(group, valorVerdade);
                radioLegal.setEnabled(enabled);
                final SimplePanel uploading = new SimplePanel();
 			   uploading.add(new HTML("<img src=\"images/up.gif\"></a>"));
 					
                radioLegal.addClickHandler(new ClickHandler(){
                		                @Override
                		                public void onClick(ClickEvent event) {
                		                	vp.add(uploading);
                		                	AlteraEstado(dado+"=True", pedido.getNumero(), handler, uploading);
                		                	
                		                	
                		                	
                	                }
                	            });
                RadioButton radioIlegal = new RadioButton(group, valorFalso);
                radioIlegal.setEnabled(enabled);
                radioIlegal.addClickHandler(new ClickHandler(){
 	                @Override
 	                public void onClick(ClickEvent event) {
 	                	vp.add(uploading);
 	                	AlteraEstado(dado+"=False", pedido.getNumero(), handler, uploading);
                }
                });
                if(atual.equals("null") || atual == null || atual.equals("")){
               	 radioLegal.setValue(false);
               	 radioIlegal.setValue(false);
                }
                else if(atual.equals("false")){
               	 radioLegal.setValue(false);
               	 radioIlegal.setValue(true);
                }else{
               	 radioLegal.setValue(true);
               	 radioIlegal.setValue(false);
                }
                vp.add(radioIlegal);
                vp.add(radioLegal);
              
 				return vp;
 			}
 			private HorizontalPanel criaDatePicker(String dataAtual, final String parameters) {
 				return criaDatePicker(dataAtual, true, parameters);
 			}
 
 			private HorizontalPanel criaDatePicker(String dataAtual, boolean enabled, final String parameters) {
 				DateTimeFormat dateFormat = DateTimeFormat.getMediumDateTimeFormat();
 	             final HorizontalPanel novo = new HorizontalPanel();
 	            //DefaultDateTimeFormatInfo info = new DefaultDateTimeFormatInfo();
 	             //DateTimeFormat datef =  new DateTimeFormat("dd-MM-YYYY", info) {};
 	             //Date teste = datef.parse("31-10-2013");
 	             final SimplePanel uploading = new SimplePanel();
 			     uploading.add(new HTML("<img src=\"images/up.gif\"></a>"));
 					
 	             DateBox dateBox = new DateBox();
 	             dateBox.setEnabled(enabled);
 	             novo.add(dateBox);
 	             dateBox.setFormat(new DateBox.DefaultFormat (DateTimeFormat.getFormat("dd/MM/yyyy"))); 
 	             //dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
 				// dateBox.setValue(teste);
 	             final DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");
 	             try {  
 	               Date selDate = (Date)format.parse(dataAtual); 
 	               dateBox.getDatePicker().setValue(selDate, true);
 	             } catch(Exception pe){
 	              // setting current date
 	            	 dateBox.getDatePicker().setValue(null, true);
 	             }
 	            // final Label teste =  new Label(format.format(dateBox.getValue()));
 	             //dialogContents.add(teste);
 	             //dateBox.setValue(new Date(2013, 12, 31));
 	             dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
 		        	 
 						@Override
 						public void onValueChange(ValueChangeEvent<Date> event) {
 							novo.add(uploading);
 							 RequestBuilder builder6 = new RequestBuilder(RequestBuilder.POST, parameters+format.format(event.getValue()));
 				                try {
 				    	            Request request = builder6.sendRequest(null, new RequestCallback() {
 				    	             
 				    	  			public void onError(Request request, Throwable exception) {
 				    	              }
 
 				    	              public void onResponseReceived(Request request, Response response) {
 				    	                if (200 == response.getStatusCode()) {
 				    	                	uploading.clear();
 				    	                	uploading.add(new HTML("<img src=\"images/ok.gif\" width=\"20px\"></a>"));
 				    	                	Timer timer = new Timer() {
 												
 												@Override
 												public void run() {
 													uploading.removeFromParent();
 													
 												}
 											};
 											timer.schedule(2000);
 				    	                	
 				    	                	
 				    	                } else {
 				    	              	  
 				    	                }
 				    	              }
 
 				    	            });
 				    	          } catch (RequestException e) {
 				    	          }
 							
 							
 							
 							
 							
 						}
 					});
 				return novo;
 			}
 
 			private HorizontalPanel createTextBox( final String parameter,  String text, final String numPedido) {
 				 
 				final HorizontalPanel novo = new HorizontalPanel();
 				final SimplePanel uploading = new SimplePanel();
 				uploading.add(new HTML("<img src=\"images/up.gif\"></a>"));
 				
 				final TextBox tal = new TextBox();
 				novo.add(tal);
 				
 		         tal.setText(text);
 		         //tal.setReadOnly(true);
 		         tal.addClickHandler(new ClickHandler() {
 					
 					@Override
 					public void onClick(ClickEvent event) {
 						//tal.setReadOnly(false);
 					}
 				});
 		         tal.addChangeHandler(new ChangeHandler() {
 					
 					@Override
 					public void onChange(ChangeEvent event) {
 							novo.add(uploading);
 			               // tal.setReadOnly(true);
 			                RequestBuilder builder6 = new RequestBuilder(RequestBuilder.POST, "setpedido?numero="+numPedido+"&"+parameter+"="+tal.getText());
 			                try {
 			    	            Request request = builder6.sendRequest(null, new RequestCallback() {
 			    	             
 			    	  			public void onError(Request request, Throwable exception) {
 			    	              }
 
 			    	              public void onResponseReceived(Request request, Response response) {
 			    	                if (200 == response.getStatusCode()) {
 			    	                	uploading.clear();
 			    	                	uploading.add(new HTML("<img src=\"images/ok.gif\" width=\"20px\"></a>"));
 			    	                	Timer timer = new Timer() {
 											
 											@Override
 											public void run() {
 												uploading.removeFromParent();
 												
 											}
 										};
 										timer.schedule(2000);
 			    	                } else {
 			    	              	  
 			    	                }
 			    	              }
 
 			    	            });
 			    	          } catch (RequestException e) {
 			    	          }
 			                
 			                
 			                
 			                
 			                
 			                
 			                
 			                
 			                
 			            
 					}
 				});
 		         return novo;
 			}
           });
         } catch (RequestException e) {
         }
   	  
   	  
   	  
   	  
   	  
 		}  
     
     
     
   private void AlteraEstado(String parameter, String numPedido, String handler, final SimplePanel uploading){
 	
   	 RequestBuilder builder6 = new RequestBuilder(RequestBuilder.POST, handler+"?pedido="+numPedido+"&"+parameter);
        try {
            Request request = builder6.sendRequest(null, new RequestCallback() {
             
  			public void onError(Request request, Throwable exception) {
              }
 
              public void onResponseReceived(Request request, Response response) {
                if (200 == response.getStatusCode()) {
             	   uploading.clear();
                	uploading.add(new HTML("<img src=\"images/ok.gif\" width=\"20px\"></a>"));
                	Timer timer = new Timer() {
 						
 						@Override
 						public void run() {
 							uploading.removeFromParent();
 							
 						}
 					};
 					timer.schedule(2000);
                } else {
              	  
                }
              }
 
            });
          } catch (RequestException e) {
          }
   }   
     
 
 	
     
     
     
 		
 	public void enviaForm(String parameters, final String numero) {
 		final DialogBox dialogBox = new DialogBox(true);
 		 try {
 		        
 		    	
 		    	dialogBox.setText("Aguarde");
 		    	VerticalPanel dialogContents = new VerticalPanel();
 		    	dialogBox.setWidget(dialogContents);
 		    	dialogBox.setGlassEnabled(true);
 		    	dialogBox.center();
 		    	dialogBox.setAnimationEnabled(true);  
 		    	dialogBox.show();
 		    	HTML details = new HTML("Enviando...");
 		    	dialogContents.add(details);
 		    	dialogContents.setCellHorizontalAlignment(
 		    	        details, HasHorizontalAlignment.ALIGN_CENTER);
 		    	  
 		    	final RequestBuilder builder3 = new RequestBuilder(RequestBuilder.POST, "/setpedido"); 
 		    	builder3.setHeader("Content-type", "application/x-www-form-urlencoded");
 		    
 				Request request = builder3.sendRequest(parameters, new RequestCallback() {
 		          
 
 				public void onError(Request request, Throwable exception) {
 		          }
 
 		          public void onResponseReceived(Request request, Response response) {
 		            if (200 == response.getStatusCode()) {
 		            	
 		            	RootPanel.get("main_bottom").clear();
 		            	RootPanel.get("main_bottom_in").clear();
 		            	
 		            	
 		            	
 		            	
 		          	 
 		            } else {
 		          	  
 		            }
 		          }
 		        });
 		      } catch (RequestException e) {
 		      }
 		dialogBox.clear();
      	dialogBox.add(new HTML("Enviado! Carregando seu pedido recém criado...."));
      	
 		 
 		 Timer t = new Timer() {
 		      @Override
 		      public void run() {
 		    	  RequestBuilder builder5 = new RequestBuilder(RequestBuilder.GET, "getpedido?q="+numero);
 
 		          
 		           try {
 						builder5.sendRequest(null, new RequestCallback() {
 						 
 						public void onError(Request request2, Throwable exception) {
 						  }
 
 						  @SuppressWarnings("deprecation")
 						public void onResponseReceived(Request request2, Response response) {
 						    if (200 == response.getStatusCode()) {
 						    	dialogBox.hide();
 						    	Pedido pedido = JsonUtils.safeEval(response.getText()).cast();
 						    	exibeTelaCompleta(pedido);
 						    }
 						  }});
 					} catch (RequestException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 		      }
 		    };
 		 
         t.schedule(1000);
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 }
