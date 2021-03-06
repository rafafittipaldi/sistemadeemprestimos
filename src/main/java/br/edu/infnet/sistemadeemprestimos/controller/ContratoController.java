package br.edu.infnet.sistemadeemprestimos.controller;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.edu.infnet.sistemadeemprestimos.model.Cliente;
import br.edu.infnet.sistemadeemprestimos.model.Coletor;
import br.edu.infnet.sistemadeemprestimos.model.Emprestimo;
import br.edu.infnet.sistemadeemprestimos.model.Pagamento;
import br.edu.infnet.sistemadeemprestimos.model.TipoPagamento;
import br.edu.infnet.sistemadeemprestimos.service.ClienteService;
import br.edu.infnet.sistemadeemprestimos.service.ColetorService;
import br.edu.infnet.sistemadeemprestimos.service.EmprestimoService;
import br.edu.infnet.sistemadeemprestimos.service.PagamentoService;
import br.edu.infnet.sistemadeemprestimos.service.TipoPagamentoService;
import br.edu.infnet.sistemadeemprestimos.util.Util;

@Controller
public class ContratoController {
	
	@Autowired
	private EmprestimoService emprestimoService;
	
	@Autowired
	private ColetorService coletorService;
	
	@Autowired
	private ClienteService clienteService;
	
	@Autowired
	private PagamentoService pagamentoService;
	
	@Autowired
	private TipoPagamentoService tipoPagamentoService;
	
	@RequestMapping(value="/", method = RequestMethod.GET )
	public String listarContratos(Model model) {
		List<Emprestimo> emprestimo = emprestimoService.listarTodosEmprestimos();
		
		model.addAttribute("emprestimo", emprestimo);
		
		return "/home";
	}	
	
	@RequestMapping(value="/novo", method = RequestMethod.GET )
	public String form(Model model) {
		
		List<Coletor> coletor = coletorService.listarTodosColetores();
		List<Cliente> cliente = clienteService.listarTodosClientes();
		
		model.addAttribute("tipoForm", "Novo");
		model.addAttribute("coletor",  coletor);
		model.addAttribute("cliente",  cliente);
		
		return "/formEmprestimo";
	}
	
	@Transactional
	@RequestMapping(value="/salvar", method = RequestMethod.POST)
	public String form(Model model, Emprestimo emprestimo) {

		//Alterar
		if(emprestimo.getNumeroDoContrato() != null) {
			Emprestimo emprestimoOld = emprestimoService.getEmprestimo(emprestimo.getNumeroDoContrato().toString());
			emprestimoOld.setObservacoes(emprestimo.getObservacoes());
			
			emprestimoService.salvar(emprestimoOld);
			return "redirect:/";
		}
		
		emprestimo.setCliente                   (clienteService.getCliente(emprestimo.getCliente().getNumeroDoCliente()));
		emprestimo.setColetor                   (coletorService.getColetor(emprestimo.getColetor().getNumeroDoColetor()));
		emprestimo.setMontanteDoEmprestimoDevido(emprestimo.getMontanteDoEmprestimo().doubleValue());
		emprestimo.setDataProximoVencimento     (Util.adicionarMes(emprestimo.getDataInicioContrato(), 1));
		emprestimo.setDataFimContrato           (Util.adicionarMes(emprestimo.getDataInicioContrato(), emprestimo.getQuantidadeDeParcelas()));
	
		//Novo
		emprestimoService.salvar(emprestimo);
		
		BigDecimal bgParcela              = Util.calcularValorParcela(emprestimo);
		BigDecimal diffValorUltimaParcela = emprestimo.getMontanteDoEmprestimo().subtract(bgParcela.multiply(new BigDecimal(emprestimo.getQuantidadeDeParcelas())));
		
		IntStream.range(0, emprestimo.getQuantidadeDeParcelas()).forEach(n -> {
			
			BigDecimal bgUltParcela = null;
			boolean    isUltParcela = (n == emprestimo.getQuantidadeDeParcelas().intValue() - 1);
			
			//Se for última parcela, soma a diferença de arredondamento
			if(isUltParcela) {
				bgUltParcela = bgParcela.subtract(diffValorUltimaParcela.multiply(new BigDecimal("-1")));
			}
			
			pagamentoService.salvar(
					new Pagamento((isUltParcela ? bgUltParcela : bgParcela), 
							Util.adicionarMes(emprestimo.getDataInicioContrato(), ++n),
							Util.calcularTaxaDeJuros(bgParcela, emprestimo.getColetor().getTaxaDeJuros()), 
							"", emprestimo));

	    });
			
		return "redirect:/";
	}

	
	@RequestMapping(value="/formedit/{numeroDoContrato}", method = RequestMethod.GET )
	public String formEdit(@PathVariable("numeroDoContrato") String id,  Model model) {
		
		Emprestimo emprestimo = emprestimoService.getEmprestimo(id);
		
		model.addAttribute("emprestimo", emprestimo);
		model.addAttribute("tipoForm",   "Editar");
		
		return "/formEmprestimo";
	}
	
	
	@RequestMapping(value="/formpag/{numeroDoContrato}", method = RequestMethod.GET )
	public String formpag(@PathVariable("numeroDoContrato") String id,  Model model) {
		
		Emprestimo          emprestimo    = emprestimoService.getEmprestimo(id);
		List<TipoPagamento> tipoPagamento = tipoPagamentoService.listarTodosTipoPagamento();
		
		model.addAttribute("emprestimo",    emprestimo);
		model.addAttribute("tipoPagamento", tipoPagamento);
		model.addAttribute("tipoForm",      "Pagamentos");
		
		return "/formListaPagamento";
	}
	
	@Transactional
	@RequestMapping(value="/receberPagamento/{numeroDoPagamento}/{observacaoPg}/{idTipoPagamento}", method = RequestMethod.GET)
	public String receberPagamento(@PathVariable("numeroDoPagamento") Integer idPagamento, 
								   @PathVariable("idTipoPagamento") Integer idTipoPagamento,
								   @PathVariable("observacaoPg") String observacaoPg,  Model model){
		
		Pagamento  pagamento        = pagamentoService.getPagamento(idPagamento);
		Emprestimo emprestimo       = emprestimoService.getEmprestimo(pagamento.getEmprestimo().getNumeroDoContrato().toString());
		TipoPagamento tipoPagamento = tipoPagamentoService.getTipoPagamento(idTipoPagamento);
		
		model.addAttribute("pagamento", pagamento);
		model.addAttribute("emprestimo", emprestimo);
		model.addAttribute("tipoForm",   "Pagamento");
		
		
		pagamento.setDataDoPagamento(new Date());
		pagamento.setObservacoes(observacaoPg);
		pagamento.setTipoPagamento(tipoPagamento);
		
		emprestimo.setMontanteDoEmprestimoDevido(emprestimo.getMontanteDoEmprestimoDevido()-pagamento.getPagamentoDoMontante().doubleValue());
		
		pagamentoService.salvar(pagamento);
		emprestimoService.salvar(emprestimo);
		
		return "/formListaPagamento";
	}
	
	@Transactional
	@RequestMapping(value="/receberJuros/{numeroDoPagamento}/{observacaoPg}/{idTipoPagamento}", method = RequestMethod.GET)
	public String receberJuroso(@PathVariable("numeroDoPagamento") Integer idPagamento, 
								@PathVariable("idTipoPagamento") Integer idTipoPagamento,
								@PathVariable("observacaoPg") String observacaoPg,  Model model){
		
		Pagamento  pagamento        = pagamentoService.getPagamento(idPagamento);
		Emprestimo emprestimo       = emprestimoService.getEmprestimo(pagamento.getEmprestimo().getNumeroDoContrato().toString());
		TipoPagamento tipoPagamento = tipoPagamentoService.getTipoPagamento(idTipoPagamento);
		
		model.addAttribute("pagamento",  pagamento);
		model.addAttribute("emprestimo", emprestimo);
		model.addAttribute("tipoForm",   "Pagamento");
		
		BigDecimal taxaJurosAntiga = pagamento.getPagamentoTaxaDeJuros();
		
		pagamento.setDataDoPagamento     (new Date());
		pagamento.setObservacoes         (observacaoPg);
		pagamento.setTipoPagamento       (tipoPagamento);
		pagamento.setPagamentoTaxaDeJuros(new BigDecimal (0));
			
		pagamentoService.salvar(pagamento);
		
		Date novaDataFimDoContrato = null;
		try {
			novaDataFimDoContrato = new SimpleDateFormat("yyyy-MM-dd").parse(emprestimo.getDataFimContrato().toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		emprestimo.setDataFimContrato(Util.adicionarMes(novaDataFimDoContrato, 1));
		emprestimoService.salvar(emprestimo);
		
		pagamentoService.salvar(
				new Pagamento(pagamento.getPagamentoDoMontante(),
						      Util.adicionarMes(novaDataFimDoContrato, 1),
						      taxaJurosAntiga,
						      "", emprestimo));
		
		
		return "/formListaPagamento";
	}
	
	
	@RequestMapping( value = "/delete/{numeroDoContrato}", method = RequestMethod.GET )
	public String delete(@PathVariable("numeroDoContrato") String id) {	
		emprestimoService.deletar(id);
		return "redirect:/";
	}
	
	@Transactional
	@RequestMapping( value = "/receberAll/{numeroDoContrato}", method = RequestMethod.GET )
	public String receberAll(@PathVariable("numeroDoContrato") String id) {	

		Emprestimo emprestimo = emprestimoService.getEmprestimo(id);
		
		emprestimo.setMontanteDoEmprestimoDevido(0D);
		for (Pagamento pagamento : emprestimo.getPagamentos()) {
			
			pagamento.setDataDoPagamento(new Date());
			pagamento.setObservacoes("Quitado Automaticamente!");
			pagamento.setTipoPagamento(tipoPagamentoService.getTipoPagamento(1));
			pagamentoService.salvar(pagamento);
		}
		emprestimoService.salvar(emprestimo);
		
		return "redirect:/";
	}
}
