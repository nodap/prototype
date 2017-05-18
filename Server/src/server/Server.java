package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends Thread implements Constant {
	
	private String ip;
	private String coordinator;

	private ArrayList<String> list;
	private int index;
	
	private ElectionManager em;
	private NodeManager nm;
	private ResourceManager rm;
	
	public Server() {
		this.coordinator = "";
		this.index = -1;
		try {
			ip = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/* Coordinator�� ����� �� ����*/
	public void setCoordinator(String coordinator) {
		System.out.println("Coordinator is " + coordinator);
		this.coordinator = coordinator;
	}
	
	public void elect_coordinator() {
		System.out.println("elect_coordinator()");
		if((nm != null) && nm.isAlive()) nm.close();
		if((rm != null) && rm.isAlive()) rm.close();
		em.send_election();
	}
	
	public void make_manager() {	
		if((nm != null) && nm.isAlive()) nm.close();
		if((rm != null) && rm.isAlive()) rm.close();
		
		/* NodeManager �ʱ�ȭ */
		nm = new NodeManager(ip, coordinator);
		nm.start();
		
		/* ResourceManager �ʱ�ȭ */
		if(ip.equals(coordinator)) {
			rm = new ResourceManager(ip);
			rm.start();
		}
	}
		
	/* �ڽ��� index�� ���� */
	public void setIndex() {
		for(int i = 0 ; i < list.size(); i++) {
			if(ip.equals(list.get(i).toString())) {
				index = i;
				return;
			}
		}
	}
	
	/* Server Farm ���� �� ������ ���� �ּҸ� list.txt ���Ͽ��� �о� �ʱ�ȭ */
	public void load_list() {
		list = new ArrayList<String>();
		String str ="";
		
		/* ipAddress ����Ʈ ���� */
		try {
			BufferedReader br = new BufferedReader(new FileReader(ListFile));
			while((str = br.readLine()) != null) {
				list.add(str);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		
		load_list();
		setIndex();
		
		if(index == -1) {
			System.out.println("���� ���� ip�� ��Ͽ��� ã�� �� �����ϴ�.");
			return;
		}
		
		em = new ElectionManager(ip, list, index);
		em.start();
		
		while(!Thread.currentThread().interrupted()) {
			/* Coordinator�� Up, Slave�� Down -> NameNode�� �̸� �����ϰ� DataNode�� �۾� �й� �� ������ �� �־�� ��.*/
			/* if((nn != null) && ~ )*/
			if(!coordinator.equals(em.getCoordinator())) {
				setCoordinator(em.getCoordinator());
				make_manager();
			}
			if((nm != null) && nm.isAlive() && !nm.getIsMasterAlive()) {
				/* ���Ŀ�� Coordinator�� Down���� �˸� */
				/* update_brokers() */
				/* Ŭ���̾�Ʈ ���Ͻô� ���� ���Ͻÿ� �ٽ� ���񽺸� ��û */
				/* update_clients() */
				/* Coordinator�� �����ϱ� ���� �Ϸ����� ����*/
				elect_coordinator();
			}
		}
		
		if((em != null) && em.isAlive()) em.close();
		if((nm != null) && nm.isAlive()) nm.close();
		if((rm != null) && rm.isAlive()) rm.close();
		
		System.out.println("Server Down");
	}
	
	public static void main (String[] args) {
		Server server = new Server();
		
		try {
			server.start();
			server.join();
		} catch (InterruptedException e) {
			
		}
		
		System.out.println("Bye");
	}
}