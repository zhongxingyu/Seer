 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.gov.saudecaruaru.bpai.business.controller;
 
 import br.gov.saudecaruaru.bpai.business.model.ProcedimentoRealizado;
import br.gov.saudecaruaru.bpai.data.ProcedimentoRealizadoDAO;
 
 /**
  *
  * @author Albuquerque
  */
 public class ProcedimentoRealizadoController extends BasecController<ProcedimentoRealizado> {
 
     public ProcedimentoRealizadoController() {
        super(new ProcedimentoRealizadoDAO());
     }
     
 }
