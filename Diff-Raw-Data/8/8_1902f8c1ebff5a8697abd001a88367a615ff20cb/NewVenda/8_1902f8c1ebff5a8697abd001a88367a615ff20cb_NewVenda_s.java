 package view.venda;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 
 import model.Pessoa;
 import model.Produto;
 import model.Venda;
 import view.pessoa.ListaPessoa;
 import view.produto.ListaProduto;
 import controller.PessoaController;
 import controller.ProdutosController;
 import controller.Sessao;
 import controller.VendasController;
 import dao.VendaDAO;
 import exception.PermissaoNegadaException;
 import exception.SQLException;
 
 @SuppressWarnings( "serial" )
 public class NewVenda extends FormVenda {
 	public NewVenda() throws Exception {
 		super( new Venda() );
 
 		try {
 			Sessao.getInstance().temPermissaoCriarVenda();
 		} catch ( final PermissaoNegadaException e1 ) {
 			this.dispose();
 		}
 
 		this.setTitle( "NOVA VENDA" );
 		this.saveButton.addActionListener( new ActionListener() {
 			@Override
 			public void actionPerformed( final ActionEvent e ) {
 				try {
 					NewVenda.this.salvarVenda();
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 		} );
 
 		final JButton searchProduto = new JButton( "Adicionar produto" );
 		searchProduto.addActionListener( new ActionListener() {
 			@Override
 			public void actionPerformed( final ActionEvent e ) {
 				NewVenda.this.adicionarProduto();
 			}
 		} );
 		this.addButton( searchProduto );
 
 		final JButton searchCliente = new JButton( "Selecionar cliente" );
 		searchCliente.addActionListener( new ActionListener() {
 			@Override
 			public void actionPerformed( final ActionEvent arg0 ) {
 				NewVenda.this.selecionarCliente();
 			}
 		} );
 		this.addButton( searchCliente );
 	}
 
 	private void adicionarProduto() {
 		System.out.println( "adding produtos to venda" );
 		try {
 			final ListaProduto lista = new ListaProduto() {
 				@Override
 				protected void doubleClicked() throws Exception {
 					final Integer id = ( Integer ) this.table.getValueAt( this.table.getSelectedRow(), 0 );
 					final Produto p = new Produto();
 					final Produto copia = ProdutosController.getInstance().get( id );
 
 					p.setId( copia.getId() );
 					p.setDescricao( copia.getDescricao() );
 					p.setValor( copia.getValor() );
 					p.setQuantidade( Integer.parseInt( JOptionPane.showInputDialog( "Informe a quantidade" ) ) );
 
 					NewVenda.this.venda.addProduto( p );
 					VendaDAO.getInstance().refresh();
 					this.dispose();
 				}
 			};
 			lista.setVisible( true );
 		} catch ( final InstantiationException e ) {
 			e.printStackTrace();
 		} catch ( final IllegalAccessException e ) {
 			e.printStackTrace();
 		}
 	}
 
 	private void clearVenda() throws Exception {
 		NewVenda.this.venda = new Venda();
 		this.NewVenda();
 		VendaDAO.getInstance().refresh();
 	}
 
 	private void salvarVenda() throws Exception {
 		try {
 			final Venda v = this.parseEntity();
 			v.setProdutos( this.venda.getProdutos() );
 			v.setCliente( this.venda.getCliente() );
 
 			Double total = new Double( 0 );
 			for ( final Produto p : v.getProdutos() ) {
 				System.out.println( " > valor " + p.getValor() );
 				System.out.println( " > quant " + p.getQuantidade() );
 				total = total + p.getValor() * p.getQuantidade();
 			}
 			v.setValorTotal( total );
 
 			System.out.println( "valor total " + total );
 
 			VendasController.getInstance().create( v );
 			JOptionPane.showMessageDialog( this, "Venda cadastrado!" );
 		} catch ( final Exception ex ) {
 			JOptionPane.showMessageDialog( this, ">> ERRO AO CADASTRAR VENDA" );
 		}
 
 		this.clear();
 		this.clearVenda();
 	}
 
 	private void selecionarCliente() {
 		System.out.println( "connecting cliente to venda" );
 		try {
 			final ListaPessoa lista = new ListaPessoa() {
 				@Override
 				protected void doubleClicked() throws SQLException {
 					final Integer id = ( Integer ) this.table.getValueAt( this.table.getSelectedRow(), 0 );
 					final Pessoa pessoa = PessoaController.getInstance().get( id );
 					NewVenda.this.venda.setCliente( pessoa );
 					NewVenda.this.parseCliente( pessoa.getNome(), "cliente" );
 					VendaDAO.getInstance().refresh();
 					this.dispose();
 				}
 			};
 			lista.setVisible( true );
 		} catch ( final InstantiationException e ) {
 			e.printStackTrace();
 		} catch ( final IllegalAccessException e ) {
 			e.printStackTrace();
 		}
 	}
 }
