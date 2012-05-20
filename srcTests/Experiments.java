import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;


public class Experiments {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>();
		System.out.println("Testando 1 2 3");
		
		Semaphore sem = new Semaphore(1, true);
			
		
		MyThread myThread = new MyThread(blockingQueue, sem);
		myThread.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MyThreadConsumer myThreadConsumer = new MyThreadConsumer(blockingQueue, sem);
		(myThreadConsumer).start();

	}

}
class MyThread extends Thread{
	BlockingQueue bq;
	Semaphore sem;
	MyThread(BlockingQueue bq, Semaphore sem){
		this.bq = bq;
		this.sem = sem;
	}
	public void run(){
		try {
			sem.acquire();
	
			for (int k=0; k<5; k++){
				System.out.println("-->"+k);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		    sem.release();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		try {
//			bq.put("teste1");
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	

		
		
	}
}
class MyThreadConsumer extends Thread{
	BlockingQueue bq;
	Semaphore sem;
	MyThreadConsumer(BlockingQueue bq, Semaphore sem){
		this.bq = bq;
		this.sem = sem;
	}
	public void run(){
		System.out.println("Starting consumer");
		try {
			//System.out.println(bq.take());
			sem.acquire();
			System.out.println("consumer adquiriu");
			sem.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finished");
		
	}
}