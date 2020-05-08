 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.gov.saudecaruaru.bpai.business.controller;
 
 import br.gov.saudecaruaru.bpai.business.model.BIProcedimentoRealizado;
 import br.gov.saudecaruaru.bpai.business.service.SProcedimentoRealizado;
 import br.gov.saudecaruaru.bpai.business.service.SUsuarioDesktop;
 import br.gov.saudecaruaru.bpai.business.service.ServicoControllerPortType;
 import br.gov.saudecaruaru.bpai.business.service.ServicoControllerPortTypeProxy;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import org.apache.log4j.Logger;
 
 /**
  * Contém os métodos necessários para o envioe  atualização
  * dos procedimentosRealizados ao servidor central
  * @author Albuquerque
  */
 public class SProcedimentoRealizadoController {
     
     private static final Logger logger=  Logger.getLogger(SProcedimentoRealizadoController.class);
     private ServicoControllerPortType servico;
     private BIProcedimentoRealizadoController bIProcedimentoRealizadoController=new BIProcedimentoRealizadoController();
 
     public SProcedimentoRealizadoController() {
         this.servico=new ServicoControllerPortTypeProxy();
     }
 
     public SProcedimentoRealizadoController(ServicoControllerPortType servico) {
         this.servico = servico;
     }
     
     
     
     public Thread enviarSProcedimentoRealizado(final SProcedimentoRealizado sProcedimentoRealizado, final SUsuarioDesktop sUsuarioDesktop){
         if(this.existeConnection()){
             Thread thread= new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     //inicia as variáveis
                     ServicoControllerPortType ser=SProcedimentoRealizadoController.this.servico;
                     BIProcedimentoRealizadoController control=bIProcedimentoRealizadoController;
                     
                     SProcedimentoRealizado spr=sProcedimentoRealizado;
                     SUsuarioDesktop sud=sUsuarioDesktop;
                     
                     //vai tentar enviar
                     BIProcedimentoRealizado proc= new BIProcedimentoRealizado(sProcedimentoRealizado);
                     
                     try {
                         //se salvou com sucesso, vai atualizar no banco o estado
                         if(ser.enviarProcedimentoRealizado(spr, sud)){
                            logger.error("Método enviarSProcedimentoRealizado executado com sucesso!");
                             proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                             proc.setAtualizado(BIProcedimentoRealizado.ATUALIZADO);
                         }
                         //deu erro
                         else{
                             logger.error("Método enviarSProcedimentoRealizado => Não foi possível enviar o procedimentoRealizado");
                             proc.setEnviado(BIProcedimentoRealizado.NAO_ENVIADO);
                             proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                         }
                     } catch (RemoteException ex) {
                         logger.error("Método enviarSProcedimentoRealizado => "+ex.getMessage());
                         proc.setEnviado(BIProcedimentoRealizado.NAO_ENVIADO);
                         proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                     }catch(Exception e){
                         logger.error("Método enviarSProcedimentoRealizado => "+e.getMessage());
                         proc.setEnviado(BIProcedimentoRealizado.NAO_ENVIADO);
                         proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                     }
                     //muda o status do objeto para enviado
                     finally{
                         control.merge(proc);
                     }
                 }
             });
 
             //inicia o envio do procedimento
             thread.start();
             return thread;
         }
         return null;
     }
     
     public Thread enviarSProcedimentoRealizado(List<SProcedimentoRealizado> sProcedimentoRealizados, final SUsuarioDesktop sUsuarioDesktop){
         final SProcedimentoRealizado[] sProcedimentoRealizadoVect=sProcedimentoRealizados.toArray(new SProcedimentoRealizado[0]);
         
         if(this.existeConnection()){
             Thread thread= new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     //inicia as variáveis
                     ServicoControllerPortType ser=SProcedimentoRealizadoController.this.servico;
                     BIProcedimentoRealizadoController control=bIProcedimentoRealizadoController;
                     SUsuarioDesktop sud=sUsuarioDesktop;
                     HashSet<BIProcedimentoRealizado> hashSet= new HashSet<BIProcedimentoRealizado>();
                     //armazena os procedimentos que não foram enviados com sucesso
                     SProcedimentoRealizado[] vect= null;
                     
                     try {
                         //se salvou com sucesso, vai atualizar no banco o estado
                         vect=ser.enviarEmLoteProcedimentoRealizado(sProcedimentoRealizadoVect, sud);
                         
                         if( vect == null ? false : vect.length>0){
                             //vai adicionar os procedimentoRealizados que não foram enviados com sucesso
                             for(int i=0; i<vect.length; i++){
                                 logger.error("Método enviarSProcedimentoRealizado [list] => Não foi possível enviar o procedimentoRealizado =>"+vect[i]);
                                 BIProcedimentoRealizado proc= new BIProcedimentoRealizado(vect[i]);
                                 proc.setEnviado(BIProcedimentoRealizado.NAO_ENVIADO);
                                 proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                                 hashSet.add(proc);
                             }
                         }
                         //todos foram enviados com sucesso, caso algum não tenha sido enviado
                         //já foi adicionado ao hashSet já possui o objeto
                         else{
                             for(int i=0; i<sProcedimentoRealizadoVect.length; i++){
                                 BIProcedimentoRealizado proc= new BIProcedimentoRealizado(sProcedimentoRealizadoVect[i]);
                                 proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                                 proc.setAtualizado(BIProcedimentoRealizado.ATUALIZADO);
                                 hashSet.add(proc);
                             }
                         }
                     } catch (RemoteException ex) {
                         logger.error("Método enviarSProcedimentoRealizado [list] => "+ex.getMessage());
                         
                     }catch(Exception e){
                         logger.error("Método enviarSProcedimentoRealizado [list]=> "+e.getMessage());
                     }
                     //muda o status do objeto para enviado
                     finally{
                         for(int i=0; i<sProcedimentoRealizadoVect.length; i++){
                                 BIProcedimentoRealizado proc= new BIProcedimentoRealizado(sProcedimentoRealizadoVect[i]);
                                 proc.setEnviado(BIProcedimentoRealizado.NAO_ENVIADO);
                                 proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                                 if(hashSet.add(proc)){
                                     logger.error("Método enviarSProcedimentoRealizado [list] => Não foi possível enviar o procedimentoRealizado =>"+proc);  
                                 }
                         }
                         control.merge(new ArrayList<BIProcedimentoRealizado>(hashSet));
                     }
                 }
             });
 
             //inicia o envio do procedimento
             thread.start();
             return thread;
         }        
         return null;
     }
     
     public Thread enviarSProcedimentoRealizado(final SProcedimentoRealizado[] sProcedimentoRealizados, final SUsuarioDesktop sUsuarioDesktop){
         if(this.existeConnection()){
             Thread thread= new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     //inicia as variáveis
                     ServicoControllerPortType ser=SProcedimentoRealizadoController.this.servico;
                     BIProcedimentoRealizadoController control=bIProcedimentoRealizadoController;
                     SUsuarioDesktop sud=sUsuarioDesktop;
                     HashSet<BIProcedimentoRealizado> hashSet= new HashSet<BIProcedimentoRealizado>();
                     //armazena os procedimentos que não foram enviados com sucesso
                     SProcedimentoRealizado[] vect= null;
                     
                     try {
                         //se salvou com sucesso, vai atualizar no banco o estado
                         vect=ser.enviarEmLoteProcedimentoRealizado(sProcedimentoRealizados, sud);
                         
                         if( vect == null ? false : vect.length>0){
                             //vai adicionar os procedimentoRealizados que não foram enviados com sucesso
                             for(int i=0; i<vect.length; i++){
                                 logger.error("Método enviarSProcedimentoRealizado [SProcedimentoRealizado[]] => Não foi possível enviar o procedimentoRealizado =>"+vect[i]);
                                 BIProcedimentoRealizado proc= new BIProcedimentoRealizado(vect[i]);
                                 proc.setEnviado(BIProcedimentoRealizado.NAO_ENVIADO);
                                 proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                                 hashSet.add(proc);
                             }
                         }
                         //todos foram enviados com sucesso, caso algum não tenha sido enviado
                         //já foi adicionado ao hashSet já possui o objeto
                         else{
                             for(int i=0; i<sProcedimentoRealizados.length; i++){
                                 BIProcedimentoRealizado proc= new BIProcedimentoRealizado(sProcedimentoRealizados[i]);
                                 proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                                 proc.setAtualizado(BIProcedimentoRealizado.ATUALIZADO);
                                 hashSet.add(proc);
                             }
                         }
                     } catch (RemoteException ex) {
                         logger.error("Método enviarSProcedimentoRealizado [SProcedimentoRealizado[]] => "+ex.getMessage());
                         
                     }catch(Exception e){
                         logger.error("Método enviarSProcedimentoRealizado [SProcedimentoRealizado[]]=> "+e.getMessage());
                     }
                     //muda o status do objeto para enviado
                     finally{
                         for(int i=0; i<sProcedimentoRealizados.length; i++){
                                 BIProcedimentoRealizado proc= new BIProcedimentoRealizado(sProcedimentoRealizados[i]);
                                 proc.setEnviado(BIProcedimentoRealizado.NAO_ENVIADO);
                                 proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                                 if(hashSet.add(proc)){
                                     logger.error("Método enviarSProcedimentoRealizado [SProcedimentoRealizado[]] => Não foi possível enviar o procedimentoRealizado =>"+proc);
                                 }
                         }
                         control.merge(new ArrayList<BIProcedimentoRealizado>(hashSet));
                     }
                 }
             });
 
             //inicia o envio do procedimento
             thread.start();
             return thread;
         }
         return null;
     }
     
     public Thread atualizarSProcedimentoRealizado(final SProcedimentoRealizado sProcedimentoRealizado, final SUsuarioDesktop sUsuarioDesktop){
         if(this.existeConnection()){
             Thread thread= new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     //inicia as variáveis
                     ServicoControllerPortType ser=SProcedimentoRealizadoController.this.servico;
                     BIProcedimentoRealizadoController control=bIProcedimentoRealizadoController;
                     
                     SProcedimentoRealizado spr=sProcedimentoRealizado;
                     SUsuarioDesktop sud=sUsuarioDesktop;
                     
                     //vai tentar enviar
                     BIProcedimentoRealizado proc= new BIProcedimentoRealizado(sProcedimentoRealizado);
                     
                     try {
                         //se salvou com sucesso, vai atualizar no banco o estado
                         if(ser.atualizarProcedimentoRealizado(spr, sud)){
                            logger.error("Método atualizarSProcedimentoRealizado executado com sucesso!");
                             proc.setAtualizado(BIProcedimentoRealizado.ATUALIZADO);
                             proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                         }
                         //deu erro
                         else{
                             logger.error("Método atualizarSProcedimentoRealizado => Não foi possível enviar o procedimentoRealizado");
                             proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                             proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                         }
                     } catch (RemoteException ex) {
                         logger.error("Método atualizarSProcedimentoRealizado => "+ex.getMessage());
                         proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                         proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                     }catch(Exception e){
                         logger.error("Método atualizarSProcedimentoRealizado => "+e.getMessage());
                         proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                         proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                     }
                     //muda o status do objeto para enviado
                     finally{
                         control.merge(proc);
                     }
                 }
             });
 
             //inicia o envio do procedimento
             thread.start();
             return thread;
         }
         return null;
     }
     
     public Thread atualizarSProcedimentoRealizado(List<SProcedimentoRealizado> sProcedimentoRealizados, final SUsuarioDesktop sUsuarioDesktop){
         final SProcedimentoRealizado[] sProcedimentoRealizadoVect=sProcedimentoRealizados.toArray(new SProcedimentoRealizado[0]);
         
         if(this.existeConnection()){
             Thread thread= new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     //inicia as variáveis
                     ServicoControllerPortType ser=SProcedimentoRealizadoController.this.servico;
                     BIProcedimentoRealizadoController control=bIProcedimentoRealizadoController;
                     SUsuarioDesktop sud=sUsuarioDesktop;
                     HashSet<BIProcedimentoRealizado> hashSet= new HashSet<BIProcedimentoRealizado>();
                     //armazena os procedimentos que não foram enviados com sucesso
                     SProcedimentoRealizado[] vect= null;
                     
                     try {
                         //se salvou com sucesso, vai atualizar no banco o estado
                         vect=ser.enviarEmLoteProcedimentoRealizado(sProcedimentoRealizadoVect, sud);
                         
                         if( vect == null ? false : vect.length>0){
                             //vai adicionar os procedimentoRealizados que não foram enviados com sucesso
                             for(int i=0; i<vect.length; i++){
                                 logger.error("Método eatualizarSProcedimentoRealizado [list] => Não foi possível enviar o procedimentoRealizado =>"+vect[i]);
                                 BIProcedimentoRealizado proc= new BIProcedimentoRealizado(vect[i]);
                                 proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                                 proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                                 hashSet.add(proc);
                             }
                         }
                         //todos foram enviados com sucesso, caso algum não tenha sido enviado
                         //já foi adicionado ao hashSet já possui o objeto
                         else{
                             for(int i=0; i<sProcedimentoRealizadoVect.length; i++){
                                 BIProcedimentoRealizado proc= new BIProcedimentoRealizado(sProcedimentoRealizadoVect[i]);
                                 proc.setAtualizado(BIProcedimentoRealizado.ATUALIZADO);
                                 proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                                 hashSet.add(proc);
                             }
                         }
                     } catch (RemoteException ex) {
                         logger.error("Método enviarSProcedimentoRealizado [list] => "+ex.getMessage());
                         
                     }catch(Exception e){
                         logger.error("Método enviarSProcedimentoRealizado [list]=> "+e.getMessage());
                         
                     }
                     //muda o status do objeto para enviado
                     finally{
                         for(int i=0; i<sProcedimentoRealizadoVect.length; i++){
                             BIProcedimentoRealizado proc= new BIProcedimentoRealizado(sProcedimentoRealizadoVect[i]);
                             proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                             proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                             if(hashSet.add(proc)){
                                logger.error("Método eatualizarSProcedimentoRealizado [list] => Não foi possível enviar o procedimentoRealizado =>"+proc);
                             }
                         }
                         control.merge(new ArrayList<BIProcedimentoRealizado>(hashSet));
                     }
                 }
             });
 
             //inicia o envio do procedimento
             thread.start();
             return thread;
         }    
         return null;
     }
     
     public Thread atualizarSProcedimentoRealizado(final SProcedimentoRealizado[] sProcedimentoRealizados, final SUsuarioDesktop sUsuarioDesktop){
         if(this.existeConnection()){
             Thread thread= new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     //inicia as variáveis
                     ServicoControllerPortType ser=SProcedimentoRealizadoController.this.servico;
                     BIProcedimentoRealizadoController control=bIProcedimentoRealizadoController;
                     SUsuarioDesktop sud=sUsuarioDesktop;
                     HashSet<BIProcedimentoRealizado> hashSet= new HashSet<BIProcedimentoRealizado>();
                     //armazena os procedimentos que não foram enviados com sucesso
                     SProcedimentoRealizado[] vect= null;
                     
                     try {
                         //se salvou com sucesso, vai atualizar no banco o estado
                         vect=ser.enviarEmLoteProcedimentoRealizado(sProcedimentoRealizados, sud);
                         
                         if( vect == null ? false : vect.length>0){
                             //vai adicionar os procedimentoRealizados que não foram enviados com sucesso
                             for(int i=0; i<vect.length; i++){
                                 logger.error("Método atualizarSProcedimentoRealizado [SProcedimentoRealizado[]] => Não foi possível enviar o procedimentoRealizado => "+vect[i]);
                                 BIProcedimentoRealizado proc= new BIProcedimentoRealizado(vect[i]);
                                 proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                                 proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                                 hashSet.add(proc);
                             }
                         }
                         //todos foram enviados com sucesso, caso algum não tenha sido enviado
                         //já foi adicionado ao hashSet já possui o objeto
                         else{
                             for(int i=0; i<sProcedimentoRealizados.length; i++){
                                 BIProcedimentoRealizado proc= new BIProcedimentoRealizado(sProcedimentoRealizados[i]);
                                 proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                                 proc.setAtualizado(BIProcedimentoRealizado.ATUALIZADO);
                                 hashSet.add(proc);
                             }
                         }
                     } catch (RemoteException ex) {
                         logger.error("Método atualizarSProcedimentoRealizado [SProcedimentoRealizado[]] => "+ex.getMessage());
                         
                     }catch(Exception e){
                         logger.error("Método atualizarSProcedimentoRealizado [SProcedimentoRealizado[]]=> "+e.getMessage());
                     }
                     //muda o status do objeto para enviado
                     finally{
                         for(int i=0; i<sProcedimentoRealizados.length; i++){
                                 BIProcedimentoRealizado proc= new BIProcedimentoRealizado(sProcedimentoRealizados[i]);
                                 proc.setEnviado(BIProcedimentoRealizado.ENVIADO);
                                 proc.setAtualizado(BIProcedimentoRealizado.NAO_ATUALIZADO);
                                 if(hashSet.add(proc)){
                                     logger.error("Método atualizarSProcedimentoRealizado [SProcedimentoRealizado[]] => Não foi possível enviar o procedimentoRealizado => "+proc);
                                 }
                             }
                         control.merge(new ArrayList<BIProcedimentoRealizado>(hashSet));
                     }
                 }
             });
 
             //inicia o envio do procedimento
             thread.start();
             return thread;
         }
         return null;
     }
     private boolean existeConnection(){
         return true;
     }
     
     public boolean login(SUsuarioDesktop sUsuarioDesktop){
         return true;
     }
     
     public boolean logout(SUsuarioDesktop sUsuarioDesktop){
         return true;
     }
 }
