 package org.xcommerce.beans;
 
 // Log4J
 import org.apache.log4j.Logger;
 
 // coisas do hibernate
 import org.xcommerce.db.DBManager;
 import org.hibernate.*;
 import java.io.Serializable;
 import javax.persistence.*;
 
 import java.util.*;
 
 /**
  * .
  * @author Lucas Cuculo Badiale
  * */
 @Entity
 public class Cliente implements Serializable {
     
 	/**
 	 * Logger que sera usado para esta classe.
 	 * */
 	static Logger log = Logger.getLogger("org.xcommerce.beans.Cliente");
 
     @Column
     private String nome;
 
     @Id
 	@Column
 	private String email;
 
 	@Column
 	private String senha;
 
 	@Column
 	private Date nascimento;
 
 	@Column
 	private Date dataCadastro;
 	
 	@OneToMany
	@JoinColumn
 	private Set<Compra> compras;
 
 	/**
 	 * Pojo.
 	 * Hibernate obriga a criacao de um Pojo.
 	 * */
     public Cliente() {
 		nome = null;
 		email = null;
 		senha = null;
 		nascimento = null;
 		dataCadastro = null;
 		compras = new HashSet<Compra>();
 	}
 	
 	/**
 	 * Define o nome do cliente.
 	 * @param nome Novo nome do cliente;
 	 * */
 	public void setNome         (String nome        ) { this.nome        = nome        ; }
 	
 	/**
 	 * Define o emai do cliente.
 	 * @param email Novo email do cliente;
 	 * */
 	public void setEmail        (String email       ) { this.email        = email       ; }
 	
 	/**
 	 * Define a senha do cliente.
 	 * @param senha Nova senha do cliente;
 	 * */
 	public void setSenha        (String senha       ) { this.senha        = senha       ; }
 	
 	/**
 	 * Define a data de nascimento do cliente.
 	 * @param nascimento A nova data de nascimento do cliente;
 	 * */
 	public void setNascimento   (Date   nascimento  ) { this.nascimento   = nascimento  ; }
 	
 	/**
 	 * Pega o nome do cliente
 	 * @return nome do cliente.
 	 * */
 	public String getNome         () { return this.nome        ; }
 	
 	/**
 	 * Pega o email do cliente
 	 * @return email do cliente.
 	 * */
 	public String getEmail        () { return this.email       ; }
 	
 	/**
 	 * Pega a senha do cliente
 	 * @return senha do cliente.
 	 * */
 	public String getSenha        () { return this.senha       ; }
 	
 	/**
 	 * Pega a data de nascimento do cliente
 	 * @return data de nascimento do cliente.
 	 * */
 	public Date   getNascimento   () { return this.nascimento  ; }
 	
 	/**
 	 * Pega a data de cadastro do cliente
 	 * @return data de cadastro do cliente.
 	 * */
 	public Date   getDataCadastro () { return this.dataCadastro; }
 
 	/**
 	 * Pega as compras do cliente.
 	 * @return Set com as compras.
 	 * */
 	public Set<Compra> getCompras() { return this.compras; }
 
 	// metodos dos beans
 	/**
 	 * Insere o cliente no banco de dados.
 	 * */
 	public void insert() {
 		this.dataCadastro = new Date();
 
 		Session session = DBManager.getSession();
 		session.beginTransaction();
 		session.save(this);
 		session.getTransaction().commit();
 	}
 
 	/**
 	 * Remove o cliente do banco.
 	 * */
 	public void remove() {
 		Session session = DBManager.getSession();
 		session.beginTransaction();
 		session.delete(this);
 		session.getTransaction().commit();
 	}
 
 	/**
 	 * Atualiza o cliente no banco.
 	 * */
 	public void update() {
 		Session session = DBManager.getSession();
 		session.beginTransaction();
 		session.update(this);
 		session.getTransaction().commit();
 	}
 	
 	/**
 	 * Encontra um usuario dado seu email.
 	 * */
 	public static Cliente find(String email) {
 		Cliente c = new Cliente();
 
 		Session session = DBManager.getSession();
 		session.beginTransaction();
 		session.load(c, email);
 		session.getTransaction().commit();
 		
 		return c;
 	}
 	
 	/**
 	 * Retorna todos os clientes.<BR>
 	 * <b>Este comando tem que ocorrer dentro de uma transacao.</b>
 	 * @return lista com todos os clientes.
 	 * */
 	public static List findAll() {
 		Session session = DBManager.getSession();
 		return session.createQuery("SELECT c FROM Cliente c").list();
 	}
 	
 	// testes de unidade
 	// testa insert
 	private static void teste01 () {
 		Cliente c = new Cliente();
 		c.setNome("Jose");
 		c.setEmail("ze@email.com");
 		c.setSenha("123");
 		c.setNascimento(new Date(1989, 5, 11));
 
 		Compra compra = new Compra();
 		compra.insert();
 		c.getCompras().add(compra);
 
 		c.insert();
 
 		log.info("Cliente inserido.");
 	}
 
 	// testa find e update 
 	private static void teste02 () {
 		Cliente c = Cliente.find("ze@email.com");
 		log.info("Cliente buscado.");
 
 		c.setSenha("abc123");
 		c.update();
 		log.info("Cliente atualizado");
 	}
 
 	// testa find all
 	private static void teste03() {
 		Session session = DBManager.getSession();
 		session.beginTransaction();
 
 		Iterator i = Cliente.findAll().iterator();
 		log.info("Exibindo todos: ");
 
 		while (i.hasNext()) {
 			Cliente c = (Cliente) i.next();
 			log.info("Nome: " + c.getNome());
 		}
 
 		log.info("Todos exibidos");
 
 		session.getTransaction().commit();
 	}
 
 	// testa remove
 	private static void teste04() {
 		//Cliente.find("ze@email.com").remove();
 		log.info("Cliente removido");
 	}
 
 	/**
 	 * Main para executar os testes de unidade.
 	 * */
 	public static void main (String args[]) {
 		teste01();
 		teste02();
 		teste03();
 		teste04();
 	}
 }
