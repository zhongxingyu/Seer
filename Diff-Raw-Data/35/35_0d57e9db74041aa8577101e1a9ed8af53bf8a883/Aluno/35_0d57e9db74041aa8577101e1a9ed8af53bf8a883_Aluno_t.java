 package br.ueg.si.sige;
 
 import java.util.Date;
 import java.io.Serializable;
 import java.text.*;
 
 
 /**
  * <p>Title: </p>
  *
  * <p>Description: </p>
  *
  * <p>Copyright: Copyright (c) 2005</p>
  *
  * <p>Company: </p>
  *
  * @author not attributable
  * @version 1.0
  */
 public class Aluno implements Serializable {
 	private static final long serialVersionUID = 2247853499430367103L;
 	private int codigo;
     private String nome;
     private String endereco;
     private String telefone;
     private String pai;
     private String mae;
     private String responsavel;
     private Date dataDeNascimento;
     private boolean documentos;
     private boolean ativo;
     private String nacionalidade;
     private String naturalidade;
     private int escolaridade;
     private Entidade entidade;
     private boolean status;
 
 
     public Entidade getEntidade() {
         return entidade;
     }
 
     public String getNaturalidade() {
         return naturalidade;
     }
 
     public String getNacionalidade() {
         return nacionalidade;
     }
 
     public boolean isAtivo() {
         return ativo;
     }
 
     public Date getDataDeNascimento() {
         return dataDeNascimento;
     }
 
     public String getDataDeNascimentoFormatada() {
         DateFormat df = DateFormat.getDateInstance();
         return df.format(dataDeNascimento);
     }
 
     public void setDataDeNascimentoFormatada(String data) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
         try {
             dataDeNascimento = df.parse(data);
         } catch (ParseException ex) {
             ex.printStackTrace();
         }
     }
 
     public String getResponsavel() {
         return responsavel;
     }
 
     public String getMae() {
         return mae;
     }
 
     public String getPai() {
         return pai;
     }
 
     public String getTelefone() {
         return telefone;
     }
 
     public String getTelefoneFormatado() {
         String retorno;
         if (telefone.length() == 10) {
             retorno = "(" + telefone.substring(0, 2) + ") " +
                       telefone.substring(2, 6) + " - " + telefone.substring(6);
         } else {
             retorno = "(" + telefone.substring(0, 2) + ") " +
                       telefone.substring(2, 5) + " - " + telefone.substring(5);
         }
         return retorno;
     }
 
 
     public String getEndereco() {
         return endereco;
     }
 
     public String getNome() {
         return nome;
     }
 
     public void setCodigo(int codigo) {
         this.codigo = codigo;
     }
 
     public void setEntidade(Entidade entidade) {
         this.entidade = entidade;
     }
 
     public void setNaturalidade(String naturalidade) {
         this.naturalidade = naturalidade;
     }
 
     public void setNacionalidade(String nacionalidade) {
         this.nacionalidade = nacionalidade;
     }
 
     public void setAtivo(boolean ativo) {
         this.ativo = ativo;
     }
 
     public void setDataDeNascimento(Date dataDeNascimento) {
         this.dataDeNascimento = dataDeNascimento;
     }
 
     public void setResponsavel(String responsavel) {
         this.responsavel = responsavel;
     }
 
     public void setMae(String mae) {
         this.mae = mae;
     }
 
     public void setPai(String pai) {
         this.pai = pai;
     }
 
     public void setTelefone(String telefone) {
         this.telefone = telefone;
     }
 
     public void setEndereco(String endereco) {
         this.endereco = endereco;
     }
 
     public void setNome(String nome) {
         this.nome = nome;
     }
 
     public void setEscolaridade(int escolaridade) {
         this.escolaridade = escolaridade;
     }
 
     public void setDocumentos(boolean documentos) {
         this.documentos = documentos;
     }
 
     public int getCodigo() {
         return codigo;
     }
 
     public int getEscolaridade() {
         return escolaridade;
     }
 
     public boolean isDocumentos() {
         return documentos;
     }
 
     public boolean isStatus() {
         return status;
     }
 
     public void setStatus(boolean status) {
         this.status = status;
     }
 }
