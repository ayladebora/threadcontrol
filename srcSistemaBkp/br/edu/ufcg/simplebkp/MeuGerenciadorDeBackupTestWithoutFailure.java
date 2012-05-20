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

public class MeuGerenciadorDeBackupTestWithoutFailure {
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
		ListOfThreadConfigurations sysConfig = buildConfigurationOfCopiadorAndRecuperadorWaiting();
		ThreadConfiguration tcUserThreadsFinished = new ThreadConfiguration(
				RequisicaoDeCopia2.class.getCanonicalName(),
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		sysConfig.addThreadConfiguration(tcUserThreadsFinished);
		tc.prepare(sysConfig);

		MeuGerenciadorDeBackup backup = new MeuGerenciadorDeBackup();

		assertFalse(backup.estaAtivo());
		assertFalse(backup.existeCopiaDoArquivo(arq1.getAbsolutePath()));
		assertFalse(backup.existeCopiaDoArquivo(arq2.getAbsolutePath()));
		backup.iniciar();
		RequisicaoDeCopia2 req1 = new RequisicaoDeCopia2(backup,
				dirParaFazerBackup1, arq1);
		RequisicaoDeCopia2 req2 = new RequisicaoDeCopia2(backup,
				dirParaFazerBackup2, arq2);
		req1.start();
		req2.start();
		tc.waitUntilStateIsReached();

		assertTrue(backup.existeCopiaDoArquivo(arq1.getAbsolutePath()));
		assertTrue(backup.existeCopiaDoArquivo(arq2.getAbsolutePath()));

		sysConfig = buildConfigurationOfCopiadorAndRecuperadorWaiting();
		ThreadConfiguration tcRequisicoesRecuperacaoFinished = new ThreadConfiguration(
				RequisicaoDeRecuperacao2.class.getCanonicalName(),
				ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		sysConfig.addThreadConfiguration(tcRequisicoesRecuperacaoFinished);
		tc.prepare(sysConfig);
		tc.proceed();

		arq1.delete();
		arq2.delete();

		RequisicaoDeRecuperacao2 req3 = new RequisicaoDeRecuperacao2(backup, arq1);
		RequisicaoDeRecuperacao2 req4 = new RequisicaoDeRecuperacao2(backup, arq2);
		req3.start();
		req4.start();
		tc.waitUntilStateIsReached();

		assertTrue(arq1.exists());
		assertTrue(arq2.exists());

		tc.prepare(buildConfigurationOfCopiadorAndRecuperadorFinished());
		tc.proceed();
		backup.terminar();
		tc.waitUntilStateIsReached();
		assertTrue(backup.estaTotalmenteInativo());
		tc.proceed();

		cleanDir(dirParaFazerBackup1);

		cleanDir(dirParaFazerBackup2);

	}

	private ListOfThreadConfigurations buildConfigurationOfCopiadorAndRecuperadorWaiting() {
		ThreadConfiguration tconfig1 = new ThreadConfiguration(
				RecuperadorDeBackup.class.getCanonicalName(),
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		ThreadConfiguration tconfig2 = new ThreadConfiguration(
				CopiadorDeArquivos.class.getCanonicalName(),
				ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		ListOfThreadConfigurations config = new ListOfThreadConfigurations();
		config.addThreadConfiguration(tconfig1);
		config.addThreadConfiguration(tconfig2);
		return config;
	}

	private ListOfThreadConfigurations buildConfigurationOfCopiadorAndRecuperadorFinished() {
		ThreadConfiguration tconfig1 = new ThreadConfiguration(
				RecuperadorDeBackup.class.getCanonicalName(),
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

class RequisicaoDeCopia2 extends Thread {
	MeuGerenciadorDeBackup backup;
	File bkpDir;
	File bkpFile;

	public RequisicaoDeCopia2(MeuGerenciadorDeBackup b, File bDir, File bFile) {
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

class RequisicaoDeRecuperacao2 extends Thread {
	MeuGerenciadorDeBackup backup;
	File bkpFile;

	public RequisicaoDeRecuperacao2(MeuGerenciadorDeBackup b, File bFile) {
		this.backup = b;
		this.bkpFile = bFile;
	}

	public void run() {
		Collection<String> arquivos = new ArrayList<String>();
		arquivos.add(bkpFile.getAbsolutePath());
		backup.recuperarBackups(arquivos);
	}
}

