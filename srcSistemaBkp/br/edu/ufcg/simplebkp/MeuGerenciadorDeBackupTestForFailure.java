package br.edu.ufcg.simplebkp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import br.edu.ufcg.threadcontrol.ListOfThreadConfigurations;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;

public class MeuGerenciadorDeBackupTestForFailure {
	@Test
	public void testaBackupMultiplosUsuarios() throws IOException {
		File dirParaFazerBackup1 = new File("expBkp1");
		File dirParaFazerBackup2 = new File("expBkp2");

		cleanDir(dirParaFazerBackup1);
		cleanDir(dirParaFazerBackup2);

		dirParaFazerBackup1.mkdir();
		dirParaFazerBackup2.mkdir();

		File arq1 = new File(dirParaFazerBackup1.getAbsolutePath()
				+ File.separator + "arquivoTeste1.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(arq1));
		writer.write("Arquivo de teste1");
		writer.close();

		File arq2 = new File(dirParaFazerBackup2.getAbsolutePath()
				+ File.separator + "arquivoTeste2.txt");
		writer = new BufferedWriter(new FileWriter(arq2));
		writer.write("Arquivo de teste2");
		writer.close();

		ThreadControl tc = new ThreadControl();
		ListOfThreadConfigurations sysConfig = buildConfigurationOfCopiadorAndRecuperadorProblematicoWaiting();
		ThreadConfiguration tcUserThreadsFinished = new ThreadConfiguration(
				RequisicaoDeCopia.class.getCanonicalName(),
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		sysConfig.addThreadConfiguration(tcUserThreadsFinished);
		tc.prepare(sysConfig);

		MeuGerenciadorDeBackup backup = new MeuGerenciadorDeBackup();
		backup.setRecuperador(new RecuperadorDeBackupProblematico(backup
				.getServicoCopiador()));

		assertFalse(backup.estaAtivo());
		assertFalse(backup.existeCopiaDoArquivo(arq1.getAbsolutePath()));
		assertFalse(backup.existeCopiaDoArquivo(arq2.getAbsolutePath()));
		backup.iniciar();
		RequisicaoDeCopia req1 = new RequisicaoDeCopia(backup,
				dirParaFazerBackup1, arq1);
		RequisicaoDeCopia req2 = new RequisicaoDeCopia(backup,
				dirParaFazerBackup2, arq2);
		req1.start();
		req2.start();
		tc.waitUntilStateIsReached();

		assertTrue(backup.existeCopiaDoArquivo(arq1.getAbsolutePath()));
		assertTrue(backup.existeCopiaDoArquivo(arq2.getAbsolutePath()));

		sysConfig = buildConfigurationOfCopiadorAndRecuperadorProblematicoWaiting();
		ThreadConfiguration tcRequisicoesRecuperacaoFinished = new ThreadConfiguration(
				RequisicaoDeRecuperacao.class.getCanonicalName(),
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		sysConfig.addThreadConfiguration(tcRequisicoesRecuperacaoFinished);
		tc.prepare(sysConfig);
		tc.proceed();

		arq1.delete();
		arq2.delete();

		RequisicaoDeRecuperacao req3 = new RequisicaoDeRecuperacao(backup, arq1);
		RequisicaoDeRecuperacao req4 = new RequisicaoDeRecuperacao(backup, arq2);
		req3.start();
		req4.start();
		tc.waitUntilStateIsReached();

		assertTrue(arq1.exists());
		assertTrue(arq2.exists());

		tc.prepare(buildConfigurationOfCopiadorAndRecuperadorProblematicoFinished());
		tc.proceed();
		backup.terminar();
		tc.waitUntilStateIsReached();
		assertTrue(backup.estaTotalmenteInativo());
		tc.proceed();

		cleanDir(dirParaFazerBackup1);

		cleanDir(dirParaFazerBackup2);

	}

	private ListOfThreadConfigurations buildConfigurationOfCopiadorAndRecuperadorProblematicoWaiting() {
		ThreadConfiguration tconfig1 = new ThreadConfiguration(
				RecuperadorDeBackupProblematico.class.getCanonicalName(),
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		ThreadConfiguration tconfig2 = new ThreadConfiguration(
				CopiadorDeArquivos.class.getCanonicalName(),
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		ListOfThreadConfigurations config = new ListOfThreadConfigurations();
		config.addThreadConfiguration(tconfig1);
		config.addThreadConfiguration(tconfig2);
		return config;
	}

	private ListOfThreadConfigurations buildConfigurationOfCopiadorAndRecuperadorProblematicoFinished() {
		ThreadConfiguration tconfig1 = new ThreadConfiguration(
				RecuperadorDeBackupProblematico.class.getCanonicalName(),
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		ThreadConfiguration tconfig2 = new ThreadConfiguration(
				CopiadorDeArquivos.class.getCanonicalName(),
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		ListOfThreadConfigurations config = new ListOfThreadConfigurations();
		config.addThreadConfiguration(tconfig1);
		config.addThreadConfiguration(tconfig2);
		return config;
	}

	private void cleanDir(File dir) {
		if (dir.exists()) {
			for (File f : dir.listFiles()) {
				if (f.isDirectory()) {
					for (File b : f.listFiles()) {
						b.delete();
					}
				}
				f.delete();
			}
			assertTrue(dir.delete());
		}
	}

	//@Test
	public void testaBackupMultiplosUsuariosVariasVezes() throws IOException {
		for (int k = 0; k < 10000; k++) {
			System.out.println("K:" + k);
			this.testaBackupMultiplosUsuarios();
		}
	}

}

class RequisicaoDeCopia extends Thread {
	MeuGerenciadorDeBackup backup;
	File bkpDir;
	File bkpFile;

	public RequisicaoDeCopia(MeuGerenciadorDeBackup b, File bDir, File bFile) {
		this.backup = b;
		this.bkpDir = bDir;
		this.bkpFile = bFile;
	}

	public void run() {
		// Collection <String> dirPaths = new ArrayList<String>();
		// dirPaths.add(bkpDir.getAbsolutePath());
		// backup.alteraDiretoriosParaControleDeBackup(dirPaths);
		backup.adicionaDiretorioParaControleDeBackup(bkpDir.getAbsolutePath());
	}
}

class RequisicaoDeRecuperacao extends Thread {
	MeuGerenciadorDeBackup backup;
	File bkpFile;

	public RequisicaoDeRecuperacao(MeuGerenciadorDeBackup b, File bFile) {
		this.backup = b;
		this.bkpFile = bFile;
	}

	public void run() {
		Collection<String> arquivos = new ArrayList<String>();
		arquivos.add(bkpFile.getAbsolutePath());
		backup.recuperarBackups(arquivos);
	}
}
