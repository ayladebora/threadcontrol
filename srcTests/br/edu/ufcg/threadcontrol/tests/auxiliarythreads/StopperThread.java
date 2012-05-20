package br.edu.ufcg.threadcontrol.tests.auxiliarythreads;

public class StopperThread extends MonitorableThread{

		LooperThread wlt;
		public StopperThread(LooperThread wlt) {
			super("StopperThread");
			this.wlt = wlt;
		}
		public void run(){
			super.run();
			this.wlt.shutdown();
		}

	}
