 package sgcmf.control;
 
 import java.util.ArrayList;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import org.hibernate.HibernateException;
 import org.hibernate.Transaction;
 import sgcmf.hibernate.SGCMFSessionManager;
 import sgcmf.model.dao.CartaoDAO;
 import sgcmf.model.dao.FaltaDAO;
 import sgcmf.model.dao.JogadorDAO;
 import sgcmf.model.hibernate.Cartao;
 import sgcmf.model.hibernate.Falta;
 import sgcmf.model.hibernate.Jogador;
 import sgcmf.model.hibernate.Ocorrencia;
 import sgcmf.model.hibernate.Selecao;
 import sgcmf.model.other.ResultadoOperacao;
 import sgcmf.model.other.TipoResultadoOperacao;
 
 public class CtrFalta
 {
     private CtrMain ctrMain;
 
     public CtrFalta(CtrMain ctrMain)
     {
         this.ctrMain = ctrMain;
     }
 
     public Object[][] queryFaltaByIdJogo(Short idJogo)
     {
         ArrayList<Falta> alFalta;
         Object[][] dadosFalta;
 
         SGCMFSessionManager.abrirSessao();
         alFalta = FaltaDAO.getInstance().queryFaltaByIdJogo(idJogo);
         dadosFalta = arrayList2StringMatrix(alFalta);
         SGCMFSessionManager.fecharSessao();
 
         return dadosFalta;
     }
 
     public Object[][] arrayList2StringMatrix(ArrayList<Falta> alFalta)
     {
         Object[][] dadosFalta;
         Falta f;
         Selecao s;
 
         dadosFalta = new Object[alFalta.size()][6];
         for (int i = 0; i < alFalta.size(); i++)
         {
             f = alFalta.get(i);
             dadosFalta[i][0] = String.valueOf(f.getIdoc());
             dadosFalta[i][1] = String.valueOf(f.getOcorrencia().getInstantetempo());
             s = f.getJogador().getSelecao();
             
             dadosFalta[i][2] = new JLabel(s.getPais(), new ImageIcon(s.getCaminhoimgbandeira()), JLabel.LEFT);
             dadosFalta[i][3] = f.getJogador().getNome();
 
             if (f.getCartao() != null)
             {
                 dadosFalta[i][4] = f.getCartao().getCor();
             }
             else
             {
                 dadosFalta[i][4] = "";
             }
 
             dadosFalta[i][5] = f.getTipo();
         }
 
         return dadosFalta;
     }
 
     public ResultadoOperacao registrarFalta(String min, String seg, Short idJogo, String idJogador,
                                             String tipo, String cartao)
     {
         Short shortIdJogador;
         Transaction tr;
         Falta objFalta;
         Ocorrencia objOcorrencia;
         Cartao objCartao;
         Jogador objJogador;
         String errorMessage;
         ResultadoOperacao result;
 
         errorMessage = validaCampos(min, seg, idJogador, idJogo);
 
         //se nao tiver erros nos campos, entao faz o cadastro
         if (errorMessage.equals(""))
         {
 
             SGCMFSessionManager.abrirSessao();
             tr = SGCMFSessionManager.getCurrentSession().beginTransaction();
             try
             {
                 objOcorrencia = ctrMain.getCtrOcorrenciaJogo().registraOcorrencia(min, seg, idJogo);
 
                 //carrega o jogador autor
                 shortIdJogador = Short.parseShort(idJogador);
 
                 objJogador = new Jogador();
                 objJogador = JogadorDAO.getInstance().carregar(objJogador, shortIdJogador);
 
                 //se tiver jogador assistente, ele eh carregado
                 if (cartao.equals("Nenhum"))
                 {
                     objFalta = new Falta(objOcorrencia.getId(), objOcorrencia, objJogador, tipo);
                 }
                 else
                 {
                     objCartao = ctrMain.getCtrCartao().registraCartao(objOcorrencia, objJogador, cartao);
                     objFalta = new Falta(objOcorrencia.getId(), objOcorrencia, objCartao, objJogador, tipo);
                 }
 
                 FaltaDAO.getInstance().salvar(objFalta);
                 tr.commit();
 
                 result = new ResultadoOperacao("Falta cadastrada com êxito!", TipoResultadoOperacao.EXITO);
             }
             catch (HibernateException hex)
             {
                 tr.rollback();
                 result = new ResultadoOperacao("Erro do Hibernate:\n" + hex.getMessage(), TipoResultadoOperacao.ERRO);
             }
             SGCMFSessionManager.fecharSessao();
         }
         else
         {
             result = new ResultadoOperacao(errorMessage, TipoResultadoOperacao.ERRO);
         }
 
         return result;
     }
 
     private String validaCampos(String min, String seg, String idJogador, Short idJogo)
     {
         String errorMessage;
 
         errorMessage = ctrMain.getCtrOcorrenciaJogo().validaCampos(min, seg, idJogo);
 
         //so faz o outro teste se passou no primeiro teste
         if (errorMessage.equals(""))
         {
             try
             {
                 Short.parseShort(idJogador);
             }
             catch (NumberFormatException nfe)
             {
                 errorMessage = "Jogador: é obrigatório selecionar o jogador que realizou a falta.";
             }
         }
 
         return errorMessage;
     }
 
     public ResultadoOperacao removeFalta(Short idOc)
     {
         ResultadoOperacao result;
         Transaction tr;
         Falta faltaParaRemover;
         Cartao cartaoParaRemover;
         Ocorrencia ocParaRemover;
         String errorMessage;
 
         faltaParaRemover = new Falta();
         ocParaRemover = new Ocorrencia();
 
         SGCMFSessionManager.abrirSessao();
         tr = SGCMFSessionManager.getCurrentSession().beginTransaction();
 
         try
         {
             errorMessage = ctrMain.getCtrOcorrenciaJogo().validaRemocao(ocParaRemover, idOc);
             if (errorMessage.equals(""))
             {
                 FaltaDAO.getInstance().carregar(faltaParaRemover, idOc);
 
                 if (faltaParaRemover.getCartao() != null)
                 {
                     cartaoParaRemover = faltaParaRemover.getCartao();
                 }
                 else
                 {
                     cartaoParaRemover = null;
                 }
                
                FaltaDAO.getInstance().apagar(faltaParaRemover);
                 if (cartaoParaRemover != null)
                 {
                     CartaoDAO.getInstance().apagar(cartaoParaRemover);
                 }
                 ctrMain.getCtrOcorrenciaJogo().removerOcorrencia(ocParaRemover);
 
                 tr.commit();
                 result = new ResultadoOperacao("Falta e cartões associados removidos com êxito!", TipoResultadoOperacao.EXITO);
             }
             else
             {
                 result = new ResultadoOperacao(errorMessage, TipoResultadoOperacao.ERRO);
             }
 
         }
         catch (HibernateException hex)
         {
             tr.rollback();
             result = new ResultadoOperacao("Erro do Hibernate:\n" + hex.getMessage(), TipoResultadoOperacao.ERRO);
            hex.printStackTrace();
         }
         SGCMFSessionManager.fecharSessao();
 
         return result;
     }
 
     public CtrCartao getCtrCartao()
     {
         return ctrMain.getCtrCartao();
     }
     
     public int calculaNumFaltas(Short idSelecao)
     {
         FaltaDAO faltaDao;
         int qtdeFaltas;
         SGCMFSessionManager.abrirSessao();
         faltaDao = FaltaDAO.getInstance();
         qtdeFaltas = faltaDao.queryQtdeFaltasSelecao(idSelecao);
         SGCMFSessionManager.fecharSessao();
 
         return qtdeFaltas;
     }
 }
