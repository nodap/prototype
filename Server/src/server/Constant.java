package server;

public interface Constant {
	
	/* Port Number 정의 */
	public final int ElectionManagerPort = 10001;
	public final int ResourceManagerPort = 10002;
	public final int NodeManagerPort = 10005;
	
	/* Period 정의(milliseconds) */
	public final int TimerPeriod = 200;
	public final int HeartBeatingPeriod = 10000;
	
	/* Timeout(milliseconds) 정의  */
	public final int SocketTimeout = 200;
	public final int ElectionTimeout = 2000;
	public final int OkTimeout = 10000;
	public final int HeartBeatingTimeout = 60000;
	
	/* ElectionManager 메시지 정의 */
	public final int Election = 0;
	public final int Ok = 1;
	public final int Coordinator = 2;
	
	/* NodeManager, ResourceManager 메시지 정의 */
	public final int HeartBeating = 10;
	
	/* File 정의*/
	public final String ListFile = "list.txt";

}
