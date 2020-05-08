 package Presentation;
 
 import Model.TipoPagamento;
 import Persistence.RepositorioTiposPagamento;
 import eapli.util.Console;
 import java.util.Scanner;
 
 public class DefMeiosPagamento {
     public void mainLoop() {
         System.out.println("* * *  NOVO MEIO DE PAGAMENTO  * * *\n");
         
         RepositorioTiposPagamento rep = new RepositorioTiposPagamento();
         
         rep.ListarTiposPagamento();
         
         int escolha = Console.readInteger("Escolha um dos Tipos de Pagamento: ");
         
         TipoPagamento tipo = rep.getLista_tipos().get(escolha-1);
         
         String descricao = Console.readLine("Descrição: ");
         
        //DefMeioPagamentoController controller = new DefMeioPagamentoController();
        //controller.NovoMeioPagamento(tipo,descricao);
         
         System.out.println("Novo Tipo de Pagamento adicionado com sucesso!");
     }
 }
