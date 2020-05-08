 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.gov.saudecaruaru.bpai.business.controller;
 
 
 import br.gov.saudecaruaru.bpai.data.GestorCompetenciaDAO;
 import java.io.Serializable;
 
 /**
  *
  * @author Albuquerque
  */
public class GestorCompetenciaController extends BasecController<Serializable> 
 
 {
 
     public GestorCompetenciaController() {
         super(new GestorCompetenciaDAO());
     }
     
     
 }
