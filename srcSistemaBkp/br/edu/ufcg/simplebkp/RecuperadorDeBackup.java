package br.edu.ufcg.simplebkp;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecuperadorDeBackup implements Runnable {
	private static final long TEMPO_PADRAO_ENTRE_RECUPERACOES = 5000;
	// INICIO CORREÇÃO BUG
	protected boolean finalizado = false;
	// FIM CORREÇÃO BUG
	protected boolean ativo = false;
	protected List<TrabalhoDeBackup> trabalhosDeBackup = new ArrayList<TrabalhoDeBackup>();
	private CopiadorDeArquivos copiador;
	protected long tempoEntreRecuperacoes = TEMPO_PADRAO_ENTRE_RECUPERACOES;

	public RecuperadorDeBackup(CopiadorDeArquivos copiador) {
		this.copiador = copiador;
	}

	public synchronized void adicionaTrabalho(TrabalhoDeBackup trab) {
		this.trabalhosDeBackup.add(trab);
		this.notifyAll();
	}

	public synchronized void iniciar() {
		if (ativo) {
			System.err.println("Recuperador já ativo");
			return;
		}
		Thread t = new Thread(this);
		t.start();
	}

	public synchronized boolean estaAtivo() {
		return this.ativo;
	}

	public void run() {
		synchronized (this) {
			// INICIO CORRECAO BUG
			if (!finalizado) {
				this.ativo = true;
			}
			// FIM CORREÇÃO BUG
			while (ativo) {
				for (TrabalhoDeBackup trab : this.trabalhosDeBackup) {
					this.recuperaBackup(trab);
				}
				this.trabalhosDeBackup = new ArrayList<TrabalhoDeBackup>();
				if (!ativo) {
					break;
				}
				try {
					System.out.println("recuperador vai dormir");
					this.wait(this.tempoEntreRecuperacoes);
					System.out.println("Recuperador acordou");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Terminou recuperador");
	}

	protected void recuperaBackup(TrabalhoDeBackup trab) {
		File arquivoOriginal = new File(trab.getCaminhoDoArquivoOriginal());
		File arquivoBkp = this.copiador.getUltimaCopiaDoArquivo(trab
				.getCaminhoDoArquivoOriginal());
		this.recuperarBackupDeArquivo(arquivoBkp, arquivoOriginal);
	}

	private void recuperarBackupDeArquivo(File arquivoBackup,
			File arquivoOriginal) {
		FileReader in = null;
		FileWriter out = null;
		try {
			in = new FileReader(arquivoBackup);
			out = new FileWriter(arquivoOriginal);
			int c;

			while ((c = in.read()) != -1)
				out.write(c);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Não foi possível recuperar backup do arquivo "
					+ arquivoOriginal.getAbsolutePath());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized void parar() {
		this.ativo = false;
		// INICIO CORRECAO BUG
		this.finalizado = true;
		// FIM CORRECAO BUG
		this.notifyAll();
	}

}
