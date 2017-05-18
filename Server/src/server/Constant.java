package server;

public interface Constant {
	
	/* Port Number ���� */
	public final int ElectionManagerPort = 10001;
	public final int ResourceManagerPort = 10002;
	public final int NodeManagerPort = 10005;
	
	/* Period ����(milliseconds) */
	public final int TimerPeriod = 200;
	public final int HeartBeatingPeriod = 10000;
	
	/* Timeout(milliseconds) ����  */
	public final int SocketTimeout = 200;
	public final int ElectionTimeout = 2000;
	public final int OkTimeout = 10000;
	public final int HeartBeatingTimeout = 60000;
	
	/* ElectionManager �޽��� ���� */
	public final int Election = 0;
	public final int Ok = 1;
	public final int Coordinator = 2;
	
	/* NodeManager, ResourceManager �޽��� ���� */
	public final int HeartBeating = 10;
	
	/* File ����*/
	public final String ListFile = "list.txt";

}
