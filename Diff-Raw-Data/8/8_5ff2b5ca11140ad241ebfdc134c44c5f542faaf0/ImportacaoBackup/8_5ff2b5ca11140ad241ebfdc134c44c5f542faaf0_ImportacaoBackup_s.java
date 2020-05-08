 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.gov.saudecaruaru.bpai.gui;
 
 import br.gov.saudecaruaru.bpai.business.model.BIProcedimentoRealizado;
 import br.gov.saudecaruaru.bpai.business.model.Medico;
 import br.gov.saudecaruaru.bpai.business.model.MedicoCboCnes;
 import br.gov.saudecaruaru.bpai.business.model.Paciente;
 import br.gov.saudecaruaru.bpai.data.BIProcedimentoRealizadoDAO;
 import br.gov.saudecaruaru.bpai.data.BIProcedimentoRealizadoXML;
 import br.gov.saudecaruaru.bpai.data.MedicoCboCnesDAO;
 import br.gov.saudecaruaru.bpai.data.MedicoDAO;
 import br.gov.saudecaruaru.bpai.data.PacienteDAO;
 import br.gov.saudecaruaru.bpai.gui.interfaces.IExportacaoStrategy;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Albuquerque
  */
 public class ImportacaoBackup implements IExportacaoStrategy{
 
     private static Logger logger= Logger.getLogger(ExportacaoXML.class);
     private BIProcedimentoRealizadoXML bIProcedimentoRealizadoXML= new BIProcedimentoRealizadoXML();
     private BIProcedimentoRealizadoDAO bIProcedimentoRealizadoDAO= new BIProcedimentoRealizadoDAO();
     private PacienteDAO pacienteDAO= new PacienteDAO();
     private MedicoCboCnesDAO medicoCboCnesDAO= new MedicoCboCnesDAO();
     private MedicoDAO medicoDAO=new MedicoDAO();
     private String filePath;
     private BIProcedimentoRealizado bIProcedimentoRealizado;
 
     public ImportacaoBackup(String filePath, BIProcedimentoRealizado bIProcedimentoRealizado) {
         this.filePath = filePath;
         this.bIProcedimentoRealizado = bIProcedimentoRealizado;
     }
     
     
     
     @Override
     public String execute(String competenciaMovimento, String cnesUnidade) {
         String msg=null;
         Set<MedicoCboCnes> medicoCboCnesSet= new HashSet<MedicoCboCnes>();
         Set<Medico> medicoSet= new HashSet<Medico>();
         Set<Paciente> pacienteSet=new HashSet<Paciente>();
         try{
             this.bIProcedimentoRealizado.getBiProcedimentoRealizadoPK().setCnesUnidade(cnesUnidade);
             this.bIProcedimentoRealizado.setCompetenciaMovimento(competenciaMovimento);
             List<BIProcedimentoRealizado> list=this.bIProcedimentoRealizadoXML.carregar(this.filePath);
             //vai importar os dados do paciente e médico
             for(BIProcedimentoRealizado p:list){
                pacienteSet.add(p.getPaciente());
                 medicoSet.add(new Medico(p));
                 medicoCboCnesSet.add(new MedicoCboCnes(p));
             }
             //salva os pacientes
             this.pacienteDAO.merge(new ArrayList<Paciente>(pacienteSet));
             //salva os médicos
             this.medicoCboCnesDAO.merge(new ArrayList<MedicoCboCnes>(medicoCboCnesSet));
             this.medicoDAO.merge(new ArrayList<Medico>(medicoSet));
             //salva os procedimentos realizados
             this.bIProcedimentoRealizadoDAO.merge(list);
             msg="Importação do arquivo Backup realizada com sucesso.";
         }catch(Exception ex){
             ex.printStackTrace();
             logger.error(ex.getMessage());
             msg= "Falha na importação do arquivo de Backup!. Contacte os dedsenvolvedores do sistema";
         }
         finally{
             return msg;
         }
     }
     
 }
