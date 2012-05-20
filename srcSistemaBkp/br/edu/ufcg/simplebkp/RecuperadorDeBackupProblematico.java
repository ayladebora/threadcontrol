package br.edu.ufcg.simplebkp;

import java.util.ArrayList;

public class RecuperadorDeBackupProblematico extends RecuperadorDeBackup {

	public RecuperadorDeBackupProblematico(CopiadorDeArquivos copiador) {
		super(copiador);
	}
	
	public void run() {
		// BUG INJ3-REMOVED: synchronized (this) {
		// INICIO CORRECAO BUG
		if (!finalizado) {
			this.ativo = true;
		}
		// FIM CORREÇÃO BUG
		while (ativo) {
			// BUG INJ3-INSERTED
			synchronized (this) {
				for (TrabalhoDeBackup trab : this.trabalhosDeBackup) {
					this.recuperaBackup(trab);
				}
				// BUG INJ3-INSERTED - colocando só na outra parte temos
				// concurrent modification exception acontecendo rapidamente
			}
			this.trabalhosDeBackup = new ArrayList<TrabalhoDeBackup>();
			if (!ativo) {
				break;
			}
			// BUG INJ3-INSERTED
			synchronized (this) {
				try {
					System.out.println("recuperador vai dormir");
					this.wait(this.tempoEntreRecuperacoes);
					System.out.println("Recuperador acordou");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// BUG INJ3-INSERTED
			}
		}
		// BUG INJ3-REMOVED}

		System.out.println("Terminou recuperador");
	}


}
