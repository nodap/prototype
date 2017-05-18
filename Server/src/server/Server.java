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
	
	/* Coordinator가 변경될 때 수행*/
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
		
		/* NodeManager 초기화 */
		nm = new NodeManager(ip, coordinator);
		nm.start();
		
		/* ResourceManager 초기화 */
		if(ip.equals(coordinator)) {
			rm = new ResourceManager(ip);
			rm.start();
		}
	}
		
	/* 자신의 index를 구함 */
	public void setIndex() {
		for(int i = 0 ; i < list.size(); i++) {
			if(ip.equals(list.get(i).toString())) {
				index = i;
				return;
			}
		}
	}
	
	/* Server Farm 내의 각 서버에 대한 주소를 list.txt 파일에서 읽어 초기화 */
	public void load_list() {
		list = new ArrayList<String>();
		String str ="";
		
		/* ipAddress 리스트 생성 */
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
			System.out.println("로컬 서버 ip를 목록에서 찾을 수 없습니다.");
			return;
		}
		
		em = new ElectionManager(ip, list, index);
		em.start();
		
		while(!Thread.currentThread().interrupted()) {
			/* Coordinator는 Up, Slave만 Down -> NameNode는 이를 감안하고 DataNode에 작업 분배 및 종합할 수 있어야 함.*/
			/* if((nn != null) && ~ )*/
			if(!coordinator.equals(em.getCoordinator())) {
				setCoordinator(em.getCoordinator());
				make_manager();
			}
			if((nm != null) && nm.isAlive() && !nm.getIsMasterAlive()) {
				/* 브로커에 Coordinator가 Down됨을 알림 */
				/* update_brokers() */
				/* 클라이언트 프록시는 서버 프록시에 다시 서비스를 요청 */
				/* update_clients() */
				/* Coordinator를 선정하기 위해 일렉션을 실행*/
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