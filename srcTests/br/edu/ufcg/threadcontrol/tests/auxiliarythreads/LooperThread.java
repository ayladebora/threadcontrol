package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public interface LooperThread extends NotifiableThread {



	public abstract void shutdown();

	public abstract int getExecutionTimes();

}