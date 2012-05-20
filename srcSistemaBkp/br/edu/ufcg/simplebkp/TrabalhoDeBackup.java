package br.edu.ufcg.simplebkp;

public class TrabalhoDeBackup {
	private String caminhoDoArquivoOriginal;
	private int numeroDaCopia;
	
	public TrabalhoDeBackup(String caminhoArqOriginal, int numeroDaCopia){
		this.caminhoDoArquivoOriginal = caminhoArqOriginal;
		this.numeroDaCopia = numeroDaCopia;
	}
	
	public String getCaminhoDoArquivoOriginal() {
		return caminhoDoArquivoOriginal;
	}
	public void setCaminhoDoArquivoOriginal(String caminhoDoArquivoOriginal) {
		this.caminhoDoArquivoOriginal = caminhoDoArquivoOriginal;
	}
	public int getNumeroDaCopia() {
		return numeroDaCopia;
	}
	public void setNumeroDaCopia(int numeroDaCopia) {
		this.numeroDaCopia = numeroDaCopia;
	}

}
