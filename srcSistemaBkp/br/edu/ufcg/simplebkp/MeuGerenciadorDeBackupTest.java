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
import br.edu.ufcg.threadcontrol.SystemConfiguration;
import br.edu.ufcg.threadcontrol.ThreadConfiguration;
import br.edu.ufcg.threadcontrol.ThreadControl;
import br.edu.ufcg.threadcontrol.ThreadState;



public class MeuGerenciadorDeBackupTest {
	//@Test Exemplo de teste com falso positivo por problema de espera
	public void testaInicializacao(){
		MeuGerenciadorDeBackup backup =  new MeuGerenciadorDeBackup();
		assertFalse(backup.estaAtivo());
		backup.iniciar();		
		assertTrue(backup.estaAtivo());
		backup.terminar();
	}

	//@Test Exemplo de teste com falso positivo por problema de espera
	public void testaInicializacao_comSleep(){
		MeuGerenciadorDeBackup backup =  new MeuGerenciadorDeBackup();
		assertFalse(backup.estaAtivo());
		backup.iniciar();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertTrue(backup.estaAtivo());
		backup.terminar();
	}
	
	@Test
	public void testaInicializacaoComThreadControl(){
		ThreadControl tc = new ThreadControl();
		SystemConfiguration config = buildConfigurationOfThreadsWaiting();
		tc.prepare(config);
		MeuGerenciadorDeBackup backup =  new MeuGerenciadorDeBackup();
		assertFalse(backup.estaAtivo());
		backup.iniciar();		
		tc.waitUntilStateIsReached();
		assertTrue(backup.estaAtivo());
		tc.proceed();
		backup.terminar();
	}
	@Test
	public void testaFinalizacaoComThreadControl(){
		ThreadControl tc = new ThreadControl();
		SystemConfiguration config = buildConfigurationOfThreadsWaiting();
		tc.prepare(config);
		MeuGerenciadorDeBackup backup =  new MeuGerenciadorDeBackup();
		assertFalse(backup.estaAtivo());
		backup.iniciar();		
//		System.out.println("Esperar que threads parem");
		tc.waitUntilStateIsReached();
//		System.out.println("Esperou que parassem..."+Thread.currentThread());
		assertTrue(backup.estaAtivo());
//		System.out.println("Viu que estava ativo");
		tc.prepare(buildConfigurationOfThreadsFinished());
//		System.out.println("VAi prosseguir");
		tc.proceed();
//		System.out.println("prosseguiu e vai mandar terminar");
		backup.terminar();
//		System.out.println("Esperar que threads terminem");
		tc.waitUntilStateIsReached();
//		System.out.println("Esperou que terminassem");
		assertTrue(backup.estaAtivo() == false);
		tc.proceed();
		
	}
	
	@Test
	public void testaSoFinalizacaoComThreadControl(){
		ThreadControl tc = new ThreadControl();
		SystemConfiguration config =  buildConfigurationOfThreadsFinished();
		tc.prepare(config);
		MeuGerenciadorDeBackup backup =  new MeuGerenciadorDeBackup();
		assertFalse(backup.estaAtivo());
		backup.iniciar();		
		backup.terminar();
//		System.out.println("Esperar que threads terminem");
		tc.waitUntilStateIsReached();
//		System.out.println("Esperou que terminassem");
		assertTrue(backup.estaTotalmenteInativo());
		tc.proceed();
		
	}

	//@Test
	public void testaSoFinalizacaoComSleep(){
		MeuGerenciadorDeBackup backup =  new MeuGerenciadorDeBackup();
		assertFalse(backup.estaAtivo());
		backup.iniciar();		
		backup.terminar();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue(backup.estaTotalmenteInativo());
		
	}
	
	@Test
	public void testaCriacaoDeBackupComThreadControl() throws IOException{
		File dirParaFazerBackup = new File("expBkp");
		
		cleanDir(dirParaFazerBackup);
		
		dirParaFazerBackup.mkdir();
		File arq = new File(dirParaFazerBackup.getAbsolutePath()+File.separator+"arquivoTeste.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(arq));
		writer.write("Arquivo de teste");
		writer.close();
		ThreadControl tc = new ThreadControl();
		SystemConfiguration config =  buildConfigurationOfThreadsWaiting();
		tc.prepare(config);
		MeuGerenciadorDeBackup backup =  new MeuGerenciadorDeBackup();
		assertFalse(backup.estaAtivo());
		assertFalse(backup.existeCopiaDoArquivo(arq.getAbsolutePath()));
		backup.iniciar();	
		backup.adicionaDiretorioParaControleDeBackup(dirParaFazerBackup.getAbsolutePath());
		tc.waitUntilStateIsReached();
		tc.prepare(buildConfigurationOfCopiadorRunning());
		tc.proceed();
		tc.waitUntilStateIsReached();
		tc.prepare(buildConfigurationOfThreadsWaiting());
		System.out.println("Vai esperar que parem...");
		tc.proceed();
		tc.waitUntilStateIsReached();
		assertTrue(backup.existeCopiaDoArquivo(arq.getAbsolutePath()));
		tc.prepare(buildConfigurationOfThreadsFinished());
		tc.proceed();
		backup.terminar();
		tc.waitUntilStateIsReached();
		assertTrue(backup.estaTotalmenteInativo());
		tc.proceed();
		for (File f: dirParaFazerBackup.listFiles()){
			if (f.isDirectory()){
				for (File b: f.listFiles()){
					b.delete();
				}
			}
			f.delete();
		}
		assertTrue(dirParaFazerBackup.delete());
	}
	@Test
	public void testaRecuperacaoDeBackup() throws IOException{
		System.out.println("Vai começar teste de recuperacao");
		File dirParaFazerBackup = new File("expBkp");
		cleanDir(dirParaFazerBackup);
		dirParaFazerBackup.mkdir();
		File arq = new File(dirParaFazerBackup.getAbsolutePath()+File.separator+"arquivoTeste.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(arq));
		writer.write("Arquivo de teste");
		writer.close();
		ThreadControl tc = new ThreadControl();
		SystemConfiguration config =  buildConfigurationOfThreadsWaiting();
		tc.prepare(config);
		MeuGerenciadorDeBackup backup =  new MeuGerenciadorDeBackup();
		assertFalse(backup.estaAtivo());
		assertFalse(backup.existeCopiaDoArquivo(arq.getAbsolutePath()));
		backup.iniciar();	
		backup.adicionaDiretorioParaControleDeBackup(dirParaFazerBackup.getAbsolutePath());
		System.out.println("Vai esperar que threads parem");
		tc.waitUntilStateIsReached();
		tc.prepare(buildConfigurationOfCopiadorRunning());
		tc.proceed();
		System.out.println("Vai esperar que threads rodem");
		tc.waitUntilStateIsReached();
		tc.prepare(buildConfigurationOfThreadsWaiting());
		System.out.println("Vai esperar que parem...");
		tc.proceed();
		tc.waitUntilStateIsReached();
		assertTrue(backup.existeCopiaDoArquivo(arq.getAbsolutePath()));
		
		arq.delete();
		assertFalse(arq.exists());
		Collection <String> arquivos = new ArrayList<String>();
		arquivos.add(arq.getAbsolutePath());
		tc.prepare(buildConfigurationOfRecuperadorRunning());
		tc.proceed();
		backup.recuperarBackups(arquivos);
		System.out.println("Vai esperar que recuperador rode");
		tc.waitUntilStateIsReached();
		tc.prepare(buildConfigurationOfThreadsWaiting());
		tc.proceed();
		tc.waitUntilStateIsReached();
		assertTrue(arq.exists());
		
		tc.prepare(buildConfigurationOfThreadsFinished());
		tc.proceed();
		backup.terminar();
		System.out.println("Vai esperar que parem");
		tc.waitUntilStateIsReached();
		assertTrue(backup.estaTotalmenteInativo());
		tc.proceed();
		for (File f: dirParaFazerBackup.listFiles()){
			if (f.isDirectory()){
				for (File b: f.listFiles()){
					b.delete();
				}
			}
			f.delete();
		}
		assertTrue(dirParaFazerBackup.delete());
		System.out.println("fim do teste da recuperacao");
		
	}
	
	//@Test
	public void testaCriacaoERecuperacaoDeBackupComThreadControlVariasVezes() throws IOException{
		for (int k=0; k<10000; k++){
			System.out.println("K:"+k);
			this.testaRecuperacaoDeBackup();
		}
	}
	
	//@Test
	public void testaCriacaoDeBackupComThreadControlVariasVezes() throws IOException{
		for (int k=0; k<10000; k++){
			System.out.println("K:"+k);
			this.testaCriacaoDeBackupComThreadControl();
		}
	}
	
	//@Test
	public void testaFinalizacaoComThreadControlVariasVezes(){
		for (int k=0; k<10000; k++){
			System.out.println("K:"+k);
			this.testaSoFinalizacaoComThreadControl();
		}
	}
	
	//@Test
	public void testaFinalizacaoComSleepVariasVezes(){
		for (int k=0; k<10000; k++){
			System.out.println("K:"+k);
			this.testaSoFinalizacaoComSleep();
		}
	}
	
	


	protected static ListOfThreadConfigurations buildConfigurationOfThreadsWaiting() {
		ThreadConfiguration tconfig1 = new ThreadConfiguration(RecuperadorDeBackup.class.getCanonicalName(), ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		ThreadConfiguration tconfig2 = new ThreadConfiguration(CopiadorDeArquivos.class.getCanonicalName(), ThreadState.WAITING, ThreadConfiguration.AT_LEAST_ONCE);
		ListOfThreadConfigurations config = new ListOfThreadConfigurations();
		config.addThreadConfiguration(tconfig1);
		config.addThreadConfiguration(tconfig2);
		return config;
	}
	
	protected static ListOfThreadConfigurations buildConfigurationOfThreadsRunning() {
		ThreadConfiguration tconfig1 = new ThreadConfiguration(RecuperadorDeBackup.class.getCanonicalName(), ThreadState.RUNNING, ThreadConfiguration.AT_LEAST_ONCE);
		ThreadConfiguration tconfig2 = new ThreadConfiguration(CopiadorDeArquivos.class.getCanonicalName(), ThreadState.RUNNING, ThreadConfiguration.AT_LEAST_ONCE);
		ListOfThreadConfigurations config = new ListOfThreadConfigurations();
		config.addThreadConfiguration(tconfig1);
		config.addThreadConfiguration(tconfig2);
		return config;
	}
	
	protected static ListOfThreadConfigurations buildConfigurationOfThreadsFinished() {
		ThreadConfiguration tconfig1 = new ThreadConfiguration(RecuperadorDeBackup.class.getCanonicalName(), ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		ThreadConfiguration tconfig2 = new ThreadConfiguration(CopiadorDeArquivos.class.getCanonicalName(), ThreadState.FINISHED, ThreadConfiguration.AT_LEAST_ONCE);
		ListOfThreadConfigurations config = new ListOfThreadConfigurations();
		config.addThreadConfiguration(tconfig1);
		config.addThreadConfiguration(tconfig2);
		return config;
	}
	
	protected static ListOfThreadConfigurations buildConfigurationOfCopiadorRunning() {
		ThreadConfiguration tconfig1 = new ThreadConfiguration(CopiadorDeArquivos.class.getCanonicalName(), ThreadState.RUNNING, ThreadConfiguration.AT_LEAST_ONCE);
		ListOfThreadConfigurations config = new ListOfThreadConfigurations();
		config.addThreadConfiguration(tconfig1);
		return config;
	}
	
	protected static ListOfThreadConfigurations buildConfigurationOfRecuperadorRunning() {
		ThreadConfiguration tconfig1 = new ThreadConfiguration(RecuperadorDeBackup.class.getCanonicalName(), ThreadState.RUNNING, ThreadConfiguration.AT_LEAST_ONCE);
		ListOfThreadConfigurations config = new ListOfThreadConfigurations();
		config.addThreadConfiguration(tconfig1);
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
}
