package br.edu.ufcg.simplebkp;
import java.util.Collection;


public class MeuGerenciadorDeBackup {
	public static String DIRETORIO_PADRAO_DE_BACKUP = "bkp";
	private RecuperadorDeBackup recuperador;
	private CopiadorDeArquivos servicoCopiador;
	
	
	public MeuGerenciadorDeBackup(){
		this.servicoCopiador = new CopiadorDeArquivos();
		this.recuperador = new RecuperadorDeBackup(servicoCopiador);
	}
	
	public RecuperadorDeBackup getRecuperador() {
		return recuperador;
	}

	public void setRecuperador(RecuperadorDeBackup recuperador) {
		this.recuperador = recuperador;
	}

	public CopiadorDeArquivos getServicoCopiador() {
		return servicoCopiador;
	}

	public void setServicoCopiador(CopiadorDeArquivos servicoCopiador) {
		this.servicoCopiador = servicoCopiador;
	}

	public synchronized void iniciar(){
		this.servicoCopiador.iniciar();
		this.recuperador.iniciar();
	}
	
	public synchronized void terminar(){
		this.servicoCopiador.parar();
		this.recuperador.parar();
	}
	
	public synchronized void adicionaDiretorioParaControleDeBackup(String dirPath){
		this.servicoCopiador.adicionarDiretorio(dirPath);
	}
	
	public synchronized void alteraDiretoriosParaControleDeBackup(Collection <String> dirPaths){
		this.servicoCopiador.alterarDiretoriosAGerenciar(dirPaths);
	}

	public synchronized boolean existeCopiaDoArquivo(String nomeArquivo){
		return this.servicoCopiador.existeCopiaDoArquivo(nomeArquivo);
	}
	public synchronized void recuperarBackups(Collection <String> arquivos){
		for (String arq: arquivos){
			TrabalhoDeBackup trab = new TrabalhoDeBackup(arq, 1);
			this.recuperador.adicionaTrabalho(trab);
		}
	}
	public synchronized boolean estaAtivo(){
		return this.recuperador.estaAtivo() && this.servicoCopiador.estaAtivo();
	}

	public synchronized boolean estaTotalmenteInativo(){
		return !this.recuperador.estaAtivo() && !this.servicoCopiador.estaAtivo();
	}
	
	public static void main(String args[]){
		MeuGerenciadorDeBackup bkp = new MeuGerenciadorDeBackup();
		bkp.iniciar();
		bkp.terminar();
		System.out.println("fim");
	}
	
}
