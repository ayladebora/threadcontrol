package br.edu.ufcg.simplebkp;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class CopiadorDeArquivos implements Runnable {

	public static long TEMPO_PADRAO_ENTRE_COPIAS = 4000;
	private Collection<String> diretoriosAGerenciar;
	private String backupDir;
	private boolean ativo = false;
	// INICIO CORREÇÃO BUG
	private boolean finalizado = false;
	// FIM CORREÇÃO BUG
	public long tempoEntreCopias;

	public CopiadorDeArquivos() {
		this.tempoEntreCopias = TEMPO_PADRAO_ENTRE_COPIAS;
		this.backupDir = MeuGerenciadorDeBackup.DIRETORIO_PADRAO_DE_BACKUP;
		this.diretoriosAGerenciar = new ArrayList<String>();
	}

	public CopiadorDeArquivos(Collection<String> dirsAGerenciar, String bkpDir) {
		this.diretoriosAGerenciar = dirsAGerenciar;
		this.backupDir = bkpDir;
	}

	public synchronized boolean estaAtivo() {
		return this.ativo;
	}

	public synchronized void iniciar() {
		if (this.ativo) {
			System.err.println("Copiador ja iniciado");
			return;
		}
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {
		// TODO
		synchronized (this) {
			// INICIO CORREÇÃO BUG
			if (!finalizado) {
				System.out.println("Ativou copiador");
				this.ativo = true;
			}
			// FIM CORREÇÃO BUG
			while (ativo) {
				try {
					this.criarCopiasDeArquivosGerenciados();
					this.wait(tempoEntreCopias);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Terminou copiador");
	}

	public synchronized void criarCopiasDeArquivosGerenciados() {
		for (String dir : this.diretoriosAGerenciar) {
			File diretorio = new File(dir);
			File[] arquivosDoDiretorio = diretorio.listFiles();
			for (File arq : arquivosDoDiretorio) {
				if (arq.isFile()) {
					criarBackupDeArquivo(diretorio, arq);
				}
			}
		}
	}

	public synchronized boolean existeCopiaDoArquivo(String nomeArquivo) {
		File arquivo = this.getUltimaCopiaDoArquivo(nomeArquivo);
		return arquivo != null;
	}
	
	public synchronized File getUltimaCopiaDoArquivo(String nomeArquivo) {
		File arquivo = new File(nomeArquivo);
		File bkpDir = new File(arquivo.getParentFile().getAbsoluteFile()
				+ File.separator + this.backupDir);
		File bkpFile = null;
		if (bkpDir.exists()) {
			for (File arq : bkpDir.listFiles()) {
				if (arq.getName().equals(arquivo.getName()+".bkp")) {
					bkpFile = arq;
				}
			}
		}
		return bkpFile;
	}

	private void criarBackupDeArquivo(File diretorio, File arq) {
		FileReader in = null;
		FileWriter out = null;
		try {
			in = new FileReader(arq);
			File bkpDirFile = new File(diretorio.getAbsolutePath()
			+ File.separator + this.backupDir);
			
			File arqBkp = new File(bkpDirFile.getAbsolutePath() + File.separator
					+ arq.getName()+".bkp");
			if (!bkpDirFile.exists()){
				bkpDirFile.mkdirs();
			} else {
				if (arqBkp.exists()){
					arqBkp.delete();
				}
			}
			out = new FileWriter(arqBkp);
			int c;

			while ((c = in.read()) != -1)
				out.write(c);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Não foi possível criar cópia do arquivo "
					+ arq.getAbsolutePath());
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
		System.out.println("Parou copiador");
		// INICIO CORREÇÃO BUG
		this.finalizado = true; // INJEÇÃO DE BUG 1 -> comentar esta linha
		// FIM CORREÇÃO BUG
		this.notifyAll();
	}

	public synchronized void adicionarDiretorio(String novoDir) {
		this.diretoriosAGerenciar.add(novoDir);
		this.notifyAll();
	}

	public synchronized void alterarDiretoriosAGerenciar(Collection<String> dirs) {
		this.diretoriosAGerenciar = dirs;
		this.notifyAll();
	}

	public synchronized void alterarNomeDiretorioDeBackup(String novoNomeBkpDir) {
		this.backupDir = novoNomeBkpDir;
	}

}
