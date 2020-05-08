 package business.EJB.documents;
 
 import java.util.List;
 
 import datatype.SimpleDate;
 
 import persistence.dto.Autor;
 import persistence.dto.CodiceCaixa;
 import persistence.dto.DTO;
 import persistence.dto.Documento;
 import persistence.dto.PalavraChave;
 import persistence.dto.TipoDocumento;
 import persistence.dto.UserAccount;
 import persistence.exceptions.UpdateEntityException;
 import webview.worker.SearchWorker;
 import business.DAO.document.AutorDAO;
 import business.DAO.document.CodiceCaixaDAO;
 import business.DAO.document.DocumentoDAO;
 import business.DAO.document.PalavraChaveDAO;
 import business.DAO.document.TipoDocumentoDAO;
 import business.exceptions.documents.AuthorNotFoundException;
 import business.exceptions.documents.CodiceCaixaNotFoundException;
 import business.exceptions.documents.DocumentNotFoundException;
 import business.exceptions.documents.DocumentTypeNotFoundException;
 import business.exceptions.documents.DuplicatedAuthorException;
 import business.exceptions.documents.KeywordNotFoundException;
 import business.exceptions.login.UnreachableDataBaseException;
 
 public class DocumentEJB {
 	
 	private final DocumentoDAO docDao;
 	
 	private final static String default_query = "FROM DocumentoMO WHERE ";
 	
 	private final static String caixa_base = "CAIXA-0000", caixa_fim = "CAIXA-ZZZZ", codice_base = "CODICE-0000", codice_fim = "CODICE-ZZZZ";
 
 	public DocumentEJB() {
 		docDao = new DocumentoDAO();
 	}
 	
 	public List<DTO> findDocuments(
 			String tipoCodiceCaixa,
 			String codCodiceCaixaDe,
 			String codCodiceCaixaAte,
 			String tituloCodiceCaixa,
 			String anoInicioCodiceCaixa,
 			String anoFimCodiceCaixa,
 			
 			String tipoCodDocumento,
 			String codDocumento,
 			
 			SimpleDate dataDocIni,
 			SimpleDate dataDocFim, 
 			String autor,
 			String ocupacaoAutor,
 			
 			String destinatario,
 			String ocupacaoDestinatario,
 			
 			String tipoDocumento,
 			String local, 
 			String resumo,
 			String palavraChave1,	String palavraChave2,	String palavraChave3) throws DocumentNotFoundException, UnreachableDataBaseException{
 		
 		boolean continue_query = false;
 		String query = new String(default_query);
 		
 
 		//codice/caixa revisado 
 		boolean init_de = isInit(codCodiceCaixaDe), init_ate = isInit(codCodiceCaixaAte), init_tipo = isInit(tipoCodiceCaixa);
 		if(init_de && init_ate) {
 			if(init_tipo) {
 				String codDe = (tipoCodiceCaixa + "-" + String.format("%04d", Integer.parseInt(codCodiceCaixaDe))).trim();
 				String codAte = (tipoCodiceCaixa + "-" + String.format("%04d", Integer.parseInt(codCodiceCaixaAte))).trim();
 				query += "codiceCaixa.cod BETWEEN '" + codDe + "' AND '" + codAte + "'";
 			}
 			else {
 				String cdDe = ("CODICE-" + String.format("%04d", Integer.parseInt(codCodiceCaixaDe))).trim();
 				String cdAte = ("CODICE-" + String.format("%04d", Integer.parseInt(codCodiceCaixaAte))).trim();
 				String cxDe = ("CAIXA-" + String.format("%04d", Integer.parseInt(codCodiceCaixaDe))).trim();
 				String cxAte = ("CAIXA-" + String.format("%04d", Integer.parseInt(codCodiceCaixaAte))).trim();
 				query += "codiceCaixa.cod BETWEEN '" + cdDe + "' AND '" + cdAte + "' " +
 						"OR codiceCaixa.cod BETWEEN '" + cxDe + "' AND '" + cxAte + "'";
 			}
 			continue_query = true;
 		}
 		else if(init_de) {
 			if(init_tipo) {
 				String codDe = (tipoCodiceCaixa + "-" + String.format("%04d", Integer.parseInt(codCodiceCaixaDe))).trim();
 				String limite = (tipoCodiceCaixa.equals("CODICE") ? codice_fim : caixa_fim);
 				query += "codiceCaixa.cod >= '" + codDe + "' " +
 						"AND codiceCaixa.cod <= '" + limite + "'";
 			}
 			else {
 				String cdDe = ("CODICE-" + String.format("%04d", Integer.parseInt(codCodiceCaixaDe))).trim();
 				String cxDe = ("CAIXA-" + String.format("%04d", Integer.parseInt(codCodiceCaixaDe))).trim();
 				query += "(codiceCaixa.cod >= '" + cdDe + "' AND codiceCaixa.cod <= '" + codice_fim + "') "
 						+ "OR (codiceCaixa.cod >= '" + cxDe + "' AND codiceCaixa.cod <= '" + caixa_fim + "') ";
 			}
 			continue_query = true;
 		}
 		else if(init_ate) {
 			if(init_tipo) {
 				String codAte = (tipoCodiceCaixa + "-" + String.format("%04d", Integer.parseInt(codCodiceCaixaAte))).trim();
 				String limite = (tipoCodiceCaixa.equals("CODICE") ? codice_base : caixa_base);
 				query += "codiceCaixa.cod <= '" + codAte + "' " +
 						"AND codiceCaixa.cod >= '" + limite + "'";
 			}
 			else {
 				String cdAte = ("CODICE-" + String.format("%04d", Integer.parseInt(codCodiceCaixaAte))).trim();
 				String cxAte = ("CAIXA-" + String.format("%04d", Integer.parseInt(codCodiceCaixaAte))).trim();
 				query += "(codiceCaixa.cod <= '" + cdAte + "' AND codiceCaixa.cod >= '" + codice_base + "') "
 						+ "OR (codiceCaixa.cod <= '" + cxAte + "' AND codiceCaixa.cod >= '" + caixa_base + "') ";
 			}
 			continue_query = true;
 		}
 		else if(init_tipo) {
 			if(tipoCodiceCaixa.equals("CAIXA")) {
 				query += " SUBSTRING(codiceCaixa.cod, 1, 5) = 'CAIXA'";
 				continue_query = true;
 			}
 			else if(tipoCodiceCaixa.equals("CODICE")) {
 				query += " SUBSTRING(codiceCaixa.cod, 1, 6) = 'CODICE'";
 				continue_query = true;
 			}
 		}
 		// Fim do código revisado
 		
 		if(tituloCodiceCaixa != null && !tituloCodiceCaixa.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += " codiceCaixa.titulo LIKE '%" + tituloCodiceCaixa + "%'";
 			continue_query = true;
 		}
 		
 		if(anoInicioCodiceCaixa != null && !anoInicioCodiceCaixa.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			if(anoFimCodiceCaixa == null || anoFimCodiceCaixa.isEmpty()){
 				query += " codiceCaixa.anoInicio >= " + anoInicioCodiceCaixa
 						+ " OR codiceCaixa.anoFim >=" + anoInicioCodiceCaixa;
 			}
 			else
 				query += " codiceCaixa.anoInicio >= '" + anoInicioCodiceCaixa + "' "
 							+"AND codiceCaixa.anoFim <= " + anoFimCodiceCaixa;
 
 			continue_query = true;
 		}
 		else{
 			if(anoFimCodiceCaixa != null && !anoFimCodiceCaixa.isEmpty()){
 				if(continue_query == true){
 					query += " AND ";
 				}
 				query += " codiceCaixa.anoInicio <= " + anoFimCodiceCaixa
 						+ " OR codiceCaixa.anoFim <=" + anoFimCodiceCaixa;
 				continue_query = true;
 			}
 
 		}
 		
 		if(autor != null && !autor.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += " autor.nome LIKE '%" + autor + "%'";
 			continue_query = true;
 		}
 		
 		if(ocupacaoAutor != null && !ocupacaoAutor.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += " autor.ocupacao LIKE '%" + ocupacaoAutor + "%'";
 			continue_query = true;
 		}
 
 		if(destinatario != null && !destinatario.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += " destinatario.nome LIKE '%" + destinatario + "%'";
 			continue_query = true;
 		}
 		
 		if(ocupacaoDestinatario != null && !ocupacaoDestinatario.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += " destinatario.ocupacao LIKE '%" + ocupacaoDestinatario + "%'";
 			continue_query = true;
 		}
 		
 		if(codDocumento != null && !codDocumento.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
			query += " cod LIKE '" + (tipoCodDocumento != null && !tipoCodDocumento.isEmpty() ? tipoCodDocumento + "-" : "%") + String.format("%04d", Integer.parseInt(codDocumento)) + "%'";
 			continue_query = true;
 		}
 		else if(tipoCodDocumento != null && !tipoCodDocumento.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += " cod LIKE '" + tipoCodDocumento + "-" + "%'";
 			continue_query = true;
 		}
 		
 		if(local != null && !local.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += " local LIKE '%" + local + "%'";
 			continue_query = true;
 		}
 		
 		if(resumo != null && !resumo.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += " resumo LIKE '%" + resumo + "%'";
 			continue_query = true;
 		}
 		
 		if(tipoDocumento != null && !tipoDocumento.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += " tipoDocumento.nome = '" + tipoDocumento + "'";
 			continue_query = true;
 		}
 		
 		if(!dataDocIni.format().equals(SearchWorker.getMinData()) || !dataDocFim.format().equals(SearchWorker.getMaxData())){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += " (data BETWEEN '" + dataDocIni + "' AND '" + dataDocFim + "')";
 			continue_query = true;
 		}
 		
 		if(palavraChave1 != null && !palavraChave1.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += "palavraChave1.palavra = '" + palavraChave1 + "'";
 			continue_query = true;
 		}
 
 		if(palavraChave2 != null && !palavraChave2.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += "palavraChave2.palavra = '" + palavraChave2 + "'";
 			continue_query = true;
 		}
 
 		if(palavraChave3 != null && !palavraChave3.isEmpty()){
 			if(continue_query == true){
 				query += " AND ";
 			}
 			query += "palavraChave3.palavra = '" + palavraChave3 + "'";
 			continue_query = true;
 		}
 		
 		if(query.equals(default_query)) {
 			query = " FROM DocumentoMO ";
 		}
 		
 		query += " ORDER BY codiceCaixa.cod, cod, titulo ";
 		
 //		System.out.println(query); // Debug
 		
 		return docDao.findDocumentByQuery(query);
 	}
 	
 	public synchronized void registerNewDocument (
 			// Documento
 			String tipoCodDocumento, // APEP/SEQ
 			String codDocumento,
 			String local,
 			String url,
 			String resumo,
 			SimpleDate data,
 			UserAccount uploader,
 			// Códice e caixa
 			String codCodiceCaixa,
 			String tituloCodiceCaixa,
 			String anoInicioCodiceCaixa,
 			String anoFimCodiceCaixa,
 			// Autor documento
 			String autor,
 			String ocupacaoAutor,
 			// Destinatário (AutorMO)
 			String destinatario,
 			String ocupacaoDestinatario,
 			// Tipo Documento
 			String tipoDocumento,
 			String descricaoDoTipoDocumento,
 			// Palavra chave 1
 			String palavraChave1,
 			// Palavra chave 2
 			String palavraChave2,
 			// Palavra chave 3
 			String palavraChave3) throws UnreachableDataBaseException, IllegalArgumentException{
 
 		List<DTO> check;
 		CodiceCaixaDAO codiceCaixaDAO = new CodiceCaixaDAO();
 		TipoDocumentoDAO tipoDocumentoDAO = new TipoDocumentoDAO();
 		PalavraChaveDAO palavraChaveDAO = new PalavraChaveDAO();
 		AutorDAO autorDAO = new AutorDAO();
 		
 		Documento newDoc;
 		CodiceCaixa codCaixa = null;
 		PalavraChave[] palavraChave = new PalavraChave[3];
 		TipoDocumento tipoDoc = null;
 		Autor author = null;
 		Autor addressee = null;
 		
 		// Verificação de existência da origem do documento no banco
 		try {
 			codCaixa = codiceCaixaDAO.findExactCodiceCaixa(codCodiceCaixa);
 		} catch (CodiceCaixaNotFoundException e1) {
 			throw new IllegalArgumentException("Nenhum Códice/Caixa selecionado");
 			/*try {
 				codCaixa = codiceCaixaDAO.addCodiceCaixa(
 								codCodiceCaixa, 
 								tituloCodiceCaixa, 
 								Integer.parseInt(anoInicioCodiceCaixa), 
 								Integer.parseInt(anoFimCodiceCaixa)	);
 			} catch (DuplicateCodiceCaixaException e) {
 				e.printStackTrace();
 			}*/
 		}
 
 		// Verificação de existência do tipo de documento no banco
 		try {
 			check = tipoDocumentoDAO.findDocumentTypeByString(tipoDocumento);
 			for(DTO dto : check){
 				if(((TipoDocumento) dto).getNome().equals(tipoDocumento))
 					tipoDoc = (TipoDocumento) dto;
 			}
 		} catch (DocumentTypeNotFoundException e1) {	
 			tipoDoc = tipoDocumentoDAO.addDocumentType(tipoDocumento, descricaoDoTipoDocumento);
 		}
 		
 		// Verificação de existência das palavras chaves documento no banco
 		if(palavraChave1 != null && !palavraChave1.isEmpty())
 		{	
 			try {
 				check = palavraChaveDAO.findKeyWordByString(palavraChave1);
 				for(DTO dto : check){
 					if(((PalavraChave) dto).getPalavra().equals(palavraChave1))
 						palavraChave[0] = (PalavraChave) dto;
 				}
 			} catch (KeywordNotFoundException e1) {
 				throw new IllegalArgumentException("Palavra-chave inexistente");
 				/*palavraChave[0] = palavraChaveDAO.addKeyWord(
 								palavraChave1,
 								temaPalavraChave1
 								);*/
 			}
 		}
 
 		if(palavraChave2 != null && !palavraChave2.isEmpty())
 		{
 			try {
 				check = palavraChaveDAO.findKeyWordByString(palavraChave2);
 				for(DTO dto : check){
 					if(((PalavraChave) dto).getPalavra().equals(palavraChave2))
 						palavraChave[1] = (PalavraChave) dto;
 				}
 			} catch (KeywordNotFoundException e1) {
 				/*palavraChave[1] = palavraChaveDAO.addKeyWord(
 								palavraChave2,
 								temaPalavraChave2
 								);*/
 			}
 		}
 
 		if(palavraChave3 != null && !palavraChave3.isEmpty())
 		{
 			try {
 				check = palavraChaveDAO.findKeyWordByString(palavraChave3);
 				for(DTO dto : check){
 					if(((PalavraChave) dto).getPalavra().equals(palavraChave3))
 						palavraChave[2] = (PalavraChave) dto;
 				}
 			} catch (KeywordNotFoundException e1) {
 				/*palavraChave[2] = palavraChaveDAO.addKeyWord(
 								palavraChave3,
 								temaPalavraChave3
 								);*/
 			}
 		}
 		
 		if(autor != null && !autor.isEmpty()){
 			if(ocupacaoAutor.isEmpty())	ocupacaoAutor = null;
 			try{
 				author = autorDAO.findAuthorByNameAndOccupation(autor, ocupacaoAutor);
 			} catch (AuthorNotFoundException e){
 				try {
 					author = autorDAO.addAutor(autor, ocupacaoAutor);
 				} catch (DuplicatedAuthorException e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 		else author = null;
 		
 		if(destinatario != null && !destinatario.isEmpty()){
 			if(ocupacaoDestinatario.isEmpty())	ocupacaoDestinatario = null;
 			try{
 				addressee = autorDAO.findAuthorByNameAndOccupation(destinatario, ocupacaoDestinatario);
 			} catch (AuthorNotFoundException e){
 				try {
 					addressee = autorDAO.addAutor(destinatario, ocupacaoDestinatario);
 				} catch (DuplicatedAuthorException e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 		else addressee = null;
 		
 		codDocumento = tipoCodDocumento + "-" + codDocumento;
 		
 		newDoc = new Documento(codDocumento, local, url, resumo, 
 				codCaixa, tipoDoc, author, addressee, 
 				palavraChave[0], palavraChave[1], palavraChave[2], uploader, data);
 		docDao.addDocument(newDoc);
 	}
 	
 	public synchronized void modifyDocument(Documento doc) throws IllegalArgumentException, UnreachableDataBaseException, UpdateEntityException{
 		docDao.updateDocument(doc);
 	}
 	
 	public synchronized void modifyDocument(
 			// Busca do documento a ser modificado
 			String tipoCodDocumentoAntigo,
 			String codDocumentoAntigo,
 			// Documento
 			String tipoCodDocumento,
 			String codDocumento,
 			String local,
 			String url,
 			String resumo,
 			SimpleDate data,
 			// Códice e caixa
 			String codCodiceCaixa,
 			// Autor documento
 			String autor,
 			String ocupacaoAutor,
 			// Destinatário (AutorMO)
 			String destinatario,
 			String ocupacaoDestinatario,
 			// Tipo Documento
 			String tipoDocumento,
 			String palavraChave1,
 			String palavraChave2,
 			String palavraChave3) throws UnreachableDataBaseException, DocumentNotFoundException, IllegalArgumentException, UpdateEntityException{
 
 		List<DTO> check;
 		CodiceCaixaDAO codiceCaixaDAO = new CodiceCaixaDAO();
 		TipoDocumentoDAO tipoDocumentoDAO = new TipoDocumentoDAO();
 		PalavraChaveDAO palavraChaveDAO = new PalavraChaveDAO();
 		AutorDAO autorDAO = new AutorDAO();
 		
 		Documento doc = null;
 		CodiceCaixa codCaixa = null;
 		PalavraChave[] palavraChave = new PalavraChave[3];
 		TipoDocumento tipoDoc = null;
 		Autor author = null;
 		Autor addressee = null;
 		
 		doc = findSingleDocument(tipoCodDocumentoAntigo, codDocumentoAntigo);
 		
 		
 		doc.setCod(tipoCodDocumento+"-"+codDocumento); 
 		
 		
 		doc.setLocal(local);
 		doc.setResumo(resumo);
 		doc.setData(data);
 		
 		// Verificação de existência da origem do documento no banco
 		try {
 			codCaixa = codiceCaixaDAO.findExactCodiceCaixa(codCodiceCaixa);
 		} catch (CodiceCaixaNotFoundException e1) {
 			throw new IllegalArgumentException();
 		}
 		doc.setCodiceCaixa(codCaixa);
 
 		// Verificação de existência do tipo de documento no banco
 		try {
 			check = tipoDocumentoDAO.findDocumentTypeByString(tipoDocumento);
 			for(DTO dto : check){
 				if(((TipoDocumento) dto).getNome().equals(tipoDocumento))
 					tipoDoc = (TipoDocumento) dto;
 			}
 		} catch (DocumentTypeNotFoundException e1) {	
 			throw new IllegalArgumentException();
 		}
 		doc.setTipoDocumento(tipoDoc);
 		
 		// Verificação de existência das palavras chaves documento no banco
 		if(palavraChave1 != null && !palavraChave1.isEmpty())
 		{	
 			try {
 				check = palavraChaveDAO.findKeyWordByString(palavraChave1);
 				for(DTO dto : check){
 					if(((PalavraChave) dto).getPalavra().equals(palavraChave1))
 						palavraChave[0] = (PalavraChave) dto;
 				}
 			} catch (KeywordNotFoundException e1) {
 				throw new IllegalArgumentException("Palavra chave inexistente");
 			}
 		}
 		doc.setPalavraChave1(palavraChave[0]);
 
 		if(palavraChave2 != null && !palavraChave2.isEmpty())
 		{
 			try {
 				check = palavraChaveDAO.findKeyWordByString(palavraChave2);
 				for(DTO dto : check){
 					if(((PalavraChave) dto).getPalavra().equals(palavraChave2))
 						palavraChave[1] = (PalavraChave) dto;
 				}
 			} catch (KeywordNotFoundException e1) {
 				throw new IllegalArgumentException("Palavra chave inexistente");
 				/*palavraChave[1] = palavraChaveDAO.addKeyWord(
 								palavraChave2,
 								temaPalavraChave2
 								);*/
 			}
 		}
 		doc.setPalavraChave2(palavraChave[1]);
 
 		if(palavraChave3 != null && !palavraChave3.isEmpty())
 		{
 			try {
 				check = palavraChaveDAO.findKeyWordByString(palavraChave3);
 				for(DTO dto : check){
 					if(((PalavraChave) dto).getPalavra().equals(palavraChave3))
 						palavraChave[2] = (PalavraChave) dto;
 				}
 			} catch (KeywordNotFoundException e1) {
 				throw new IllegalArgumentException("Palavra chave inexistente");
 				/*palavraChave[2] = palavraChaveDAO.addKeyWord(
 								palavraChave3,
 								temaPalavraChave3
 								);*/
 			}
 		}
 		doc.setPalavraChave3(palavraChave[2]);
 		
 		if(autor != null && !autor.isEmpty()){
 			if(ocupacaoAutor.isEmpty())	ocupacaoAutor = null;
 			try{
 				author = autorDAO.findAuthorByNameAndOccupation(autor, ocupacaoAutor);
 			} catch (AuthorNotFoundException e){
 				try {
 					author = autorDAO.addAutor(autor, ocupacaoAutor);
 				} catch (DuplicatedAuthorException e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 		else author = null;
 		doc.setAutor(author);
 		
 		if(destinatario != null && !destinatario.isEmpty()){
 			if(ocupacaoDestinatario.isEmpty())	ocupacaoDestinatario = null;
 			try{
 				addressee = autorDAO.findAuthorByNameAndOccupation(destinatario, ocupacaoDestinatario);
 			} catch (AuthorNotFoundException e){
 				try {
 					addressee = autorDAO.addAutor(destinatario, ocupacaoDestinatario);
 				} catch (DuplicatedAuthorException e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 		else addressee = null;
 		doc.setDestinatario(addressee);
 
 		docDao.updateDocument(doc);
 	}
 	
 	public synchronized void removeDocument(Documento document) throws UnreachableDataBaseException {
 		docDao.removeDocument(document);
 	}
 	
 	public Documento findSingleDocument(String type, String code) throws DocumentNotFoundException, UnreachableDataBaseException, IllegalArgumentException{
 		if(code == null || code.isEmpty()) throw new IllegalArgumentException("Code is empty");
 		String query = default_query;
 		query += " cod = '" + type+"-"+code + "'";
 		List<DTO> resultSet = docDao.findDocumentByQuery(query);
 		if(resultSet == null) throw new DocumentNotFoundException("Documento não encontrado");
 		else return (Documento) resultSet.get(0);
 	}
 	
 	public Documento findSingleDocument(String code) throws DocumentNotFoundException, UnreachableDataBaseException, IllegalArgumentException{
 		if(code == null || code.isEmpty()) throw new IllegalArgumentException("Code is empty");
 		String query = default_query;
 		query += " cod = '" + code + "'";
 		List<DTO> resultSet = docDao.findDocumentByQuery(query);
 		for(DTO dto : resultSet){
 			if(((Documento)dto).getCod().equals(code))	return (Documento) dto;
 		}
 		throw new DocumentNotFoundException("Documento não encontrado");
 	}
 	
 	public List<DTO> findByDocumentType(String type) throws UnreachableDataBaseException, DocumentNotFoundException{
 
 		String query = new String(default_query);
 
 		query += "tipoDocumento.nome LIKE '%" + type + "%'";
 
 		List<DTO> list = docDao.findDocumentByQuery(query);
 		if(list == null) throw new DocumentNotFoundException();
 		return list;
 	}
 	
 	public List<DTO> findByCodiceCaixa(String codCodiceCaixa) throws UnreachableDataBaseException, DocumentNotFoundException{
 
 		String query = new String(default_query);
 
 		query += "codiceCaixa.cod = '" + codCodiceCaixa + "'";
 
 		List<DTO> list = docDao.findDocumentByQuery(query);
 		if(list == null) throw new DocumentNotFoundException();
 		return list;
 	}
 	
 	public List<DTO> findByKeyWord(String keyWord) throws UnreachableDataBaseException, DocumentNotFoundException{
 
 		String query = new String(default_query);
 
 		query += "palavraChave1.palavra = '" + keyWord + "' OR "
 				+ "palavraChave2.palavra = '" + keyWord + "' OR "
 				+ "palavraChave3.palavra = '" + keyWord + "'";
 
 		List<DTO> list = docDao.findDocumentByQuery(query);
 		if(list == null) throw new DocumentNotFoundException();
 		return list;
 	}
 	
 	private boolean isInit(String s) {
 		return s != null && !s.isEmpty();
 	}
 	
 }
