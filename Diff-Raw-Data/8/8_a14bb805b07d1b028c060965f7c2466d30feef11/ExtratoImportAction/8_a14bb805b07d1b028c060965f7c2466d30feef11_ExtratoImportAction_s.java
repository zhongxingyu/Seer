 package br.zero.controlefinanceiro.actions;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import br.zero.controlefinanceiro.commandlineparser.ExtratoImportSwitches;
 import br.zero.controlefinanceiro.model.Conta;
 import br.zero.controlefinanceiro.model.ContaDAO;
 import br.zero.controlefinanceiro.model.ExtratoParser;
 import br.zero.controlefinanceiro.model.extrato.Arquivo;
 import br.zero.controlefinanceiro.model.extrato.ArquivoDAO;
 import br.zero.controlefinanceiro.model.extrato.ExtratoLancamento;
 import br.zero.controlefinanceiro.model.extrato.ExtratoLancamentoDAO;
 import br.zero.tinycontroller.Action;
 
 public class ExtratoImportAction implements Action {
 
 	private ExtratoImportSwitches switches;
 
 	public class ExtratoImportException extends Exception {
 
 		public ExtratoImportException(String message) {
 			super(message);
 		}
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -3059453211748572183L;
 
 	}
 
 	@Override
 	public void run(Object param) throws Exception {
 		switches = checkParamValid(param);
 
 		Conta conta = getConta(switches.getNomeConta());
 
 		BufferedReader file = getFile(switches.getNomeArquivo());
 
 		try {
 			doImport(file, conta);
 
 		} finally {
 			file.close();
 		}
 	}
 
 	private void doImport(BufferedReader file, Conta conta) throws IOException, ExtratoImportException {
 		String line;
 		
 		ExtratoParser ep = conta.getParser();
 		
 		if (ep == null) {
 			throw new ExtratoImportException("Conta \"" + conta.getNome() + "\" não possui um parser de extrato registrado.");
 		}
 		
 		Arquivo arquivo = new Arquivo();
 
 		List<ExtratoLancamento> lancamentoList = new ArrayList<ExtratoLancamento>();
 		
 		while ((line = file.readLine()) != null) {
 			ep.parse(line);
 			
 			String message;
 			
 			if (!ep.isTransferLine()) {
 				message = "[ FAIL ] \"" + line + "\"";
 				if (ep.getThrewException() != null) {
 					message += " ==> " + ep.getThrewException().getMessage();
 				}
 			} else {
 				message = "[  OK  ] \"" + line + "\"";
 			}
 			
 			ExtratoLancamento lancamento = new ExtratoLancamento();
 
 			lancamento.setBanco(conta);
 			lancamento.setOriginal(line);
 			lancamento.setTransfer(ep.isTransferLine());
 			lancamento.setArquivo(arquivo);
 
 			lancamentoList.add(lancamento);
 			
 			
 			System.out.println(message);
 		}
 		
 		persistEverything(arquivo, lancamentoList);
 	}
 
 	private void persistEverything(Arquivo arquivo, List<ExtratoLancamento> lancamentoList) {
 		ArquivoDAO arquivoDAO = new ArquivoDAO();
 		
 		arquivoDAO.inserir(arquivo);
 
 		
 		ExtratoLancamentoDAO lancamentoDAO = new ExtratoLancamentoDAO();
 
 		for (ExtratoLancamento el : lancamentoList) {
 			lancamentoDAO.inserir(el);
 		}
 	}
 
 	private BufferedReader getFile(String nomeArquivo) throws FileNotFoundException {
 		return new BufferedReader(new FileReader(nomeArquivo));
 	}
 
 	private Conta getConta(String nomeConta) throws ExtratoImportException {
 		ContaDAO dao = new ContaDAO();
 
 		Conta conta = dao.getByNome(nomeConta);
 
 		if (conta == null) {
 			throw new ExtratoImportException("Conta não encontrada: \"" + nomeConta + "\".");
 		}
 
 		return conta;
 	}
 
 	private ExtratoImportSwitches checkParamValid(Object param) throws ExtratoImportException {
 		if (!(param instanceof ExtratoImportSwitches)) {
 			throw new ExtratoImportException("Parâmetro deve ser um ExtratoImportSwitches.");
 		}
 
 		ExtratoImportSwitches switches = (ExtratoImportSwitches) param;
 
 		if (switches.getNomeConta() == null) {
 			throw new ExtratoImportException("Nome da conta deve ser informada.");
 		}
 
 		if (switches.getNomeArquivo() == null) {
 			throw new ExtratoImportException("Nome do arquivo deve ser informado.");
 		}
 
 		return switches;
 	}
 }
