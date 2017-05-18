package server;

public class Timer extends Thread implements Constant {

	private int type;
	private float elapsed_time;
	private float expire_time;
	private Object Manager;
	
	public Timer(Object Manager, int type) {
		this.Manager = Manager;
		this.type = type;
		
		switch (type) {
			case Election :
				expire_time = ElectionTimeout;
				System.out.println("Timer(Election)");
				break;
			case Ok :
				System.out.println("Timer(Ok)");
				expire_time = OkTimeout;
				break;
			case HeartBeating :
				System.out.println("Timer(HeartBeating)");
				break;
		}
		
		elapsed_time = 0.0f;
	}
	
	public void HeartBeatingTimer() {
		((ResourceManager)Manager).update_node();
	}
	
	public void ElectionTimer(int type) {
		elapsed_time += TimerPeriod;

		if(elapsed_time >= expire_time) {
			Thread.currentThread().interrupt();	
			((ElectionManager)Manager).timeout(type);
		}
	}
	
	public void run() {
		while(!Thread.currentThread().interrupted()) {
			try {
				Thread.sleep(TimerPeriod);
				
				switch (type) {
					case Election : /* Coordinator 설정 */
						ElectionTimer(Election);
						break;
					case Ok : /* Election 재요청 */
						ElectionTimer(Ok);
						break;
					case HeartBeating :
						HeartBeatingTimer();
						break;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
