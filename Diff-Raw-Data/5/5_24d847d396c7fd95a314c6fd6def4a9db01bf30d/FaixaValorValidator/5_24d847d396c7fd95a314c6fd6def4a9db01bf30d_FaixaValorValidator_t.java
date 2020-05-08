 package br.com.faddvm.util.validator;
 
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
 import br.com.faddvm.dao.FaixaValorDao;
 import br.com.faddvm.dao.PacienteDao;
 import br.com.faddvm.model.FaixaValor;
 
 public class FaixaValorValidator implements Validator {
 
 	FaixaValorDao dao;
 	@Override
 	public boolean supports(Class<?> classe) {
 		return FaixaValor.class.equals(classe);
 	}
 
 	@Override
 	public void validate(Object obj, Errors errors) {
 
 		FaixaValor faixa = (FaixaValor) obj;
 
 		if (validaFaixa(faixa, errors)) {
 			return;
 		}
 
 		if (validaRange(faixa, errors)) {
 			return;
 		}
 
 	}
 	
 	public FaixaValorValidator(FaixaValorDao dao) {
 		super();
 		this.dao = dao;
 	}
 
 	private boolean validaFaixa(FaixaValor f, Errors e) {
 
 		if (f.getId() == null) {
			if (dao.getByDescricaoAndVariavel(f.getDescricao(), new Long(1)) != null && f.getVariavel().getId() == 1) {
 				e.reject(null, "Esta ocorrência já existe");
 			}
			if (dao.getByDescricaoAndVariavel(f.getDescricao(),new Long(2)) != null && f.getVariavel().getId() == 2) {
 				e.reject(null, "Esta intercorrência já existe");
 			}
 
 		}
 		if (f.getPeso() == null) {
 			e.reject(null, "Peso é obrigatório");
 			return true;
 		}
 
 		if (f.getValorMin() == null) {
 			e.reject(null, "Valor mínimo é obrigatório.");
 			return true;
 		}
 
 		if (f.getValorMax() == null) {
 			e.reject(null, "Valor máximo é obrigatório.");
 			return true;
 		}
 
 		if (f.getValorMin() > f.getValorMax()) {
 			e.reject(null,
 					"Valor mínimo deve ser menor ou igual a valor máximo.");
 			return true;
 		}
 
 		if (f.getValorMin() < 0 || f.getValorMax() < 0) {
 			e.reject(null, "Valor mínimo deve ser maior ou igual a 0");
 			return true;
 		}
 		if (f.getValorMax() < 0) {
 			e.reject(null, "Valor máximo deve ser maior ou igual a 0");
 			return true;
 		}
 		if (f.getPeso() < 0) {
 			e.reject(null, "Peso deve ser maior ou igual a 0");
 			return true;
 		}
 		if (f.getValorMin() > 100000 || f.getValorMax() > 100000) {
 			e.reject(null, "Valor mínimo deve ser menor ou igual a 100000");
 			return true;
 		}
 		if (f.getValorMax() > 100000) {
 			e.reject(null, "Valor máximo deve ser menor ou igual a 100000");
 			return true;
 		}
 		if (f.getPeso() > 100000) {
 			e.reject(null, "Peso deve ser menor ou igual a 100000");
 			return true;
 		}
 
 		if (f.getDescricao() == null) {
 			e.reject(null, "Descrição é obrigatória.");
 			return true;
 		}
 		f.setDescricao(f.getDescricao().trim());
 		if (f.getDescricao().length() < 3 || f.getDescricao().length() > 250) {
 			e.reject(null, "Descrição deve ter 3 caracteres ou mais");
 			return true;
 		}
 
 		return false;
 	}
 	private boolean validaRange(FaixaValor faixa, Errors errors) {
 		Integer vValorMin = faixa.getVariavel().getValorMin();
 		Integer vValorMax = faixa.getVariavel().getValorMax();
 
 		boolean vIniciada = vValorMax != null && vValorMin != null;
 		char vTipo = faixa.getVariavel().getTipo();
 		if (vIniciada && vTipo == 'R') {
 			if (faixa.getValorMax() > vValorMax
 					&& faixa.getValorMin() != (vValorMax + 1)) {
 				errors.reject(null,
 						"Faixa Inválida, valor mínimo pode iniciar em "
 								+ (vValorMax + 1));
 				return true;
 			}
 
 			if (faixa.getValorMin() < vValorMin
 					&& faixa.getValorMax() != (vValorMin - 1)) {
 				errors.reject(null,
 						"Faixa Inválida, valor Máximo pode terminar em "
 								+ (vValorMin - 1));
 				return true;
 			}
 
 			if (faixa.getValorMax() == vValorMax
 					|| faixa.getValorMin() == vValorMin
 					|| (faixa.getValorMin() >= vValorMin && faixa.getValorMax() <= vValorMax)) {
 				errors.reject(null, "Você não pode criar essa Faixa");
 				return true;
 			}
 
 		}
 		return false;
 	}
 }
