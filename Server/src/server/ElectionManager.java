package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ElectionManager extends Thread implements Constant {
		
	private String ip;
	private String coordinator;
	private boolean isElected;
	private boolean isOk;
	
	private ArrayList<String> list;
	private int index;
	
	private ServerSocket serverSocket;
	private Socket socket;
	
	private DataInputStream dis;
	private DataOutputStream dos;
	
	private Timer timer;

	public ElectionManager(String ip, ArrayList<String> list, int index) {
		try {
			this.ip = ip;
			this.list = list;
			this.index = index;
			this.coordinator = "";
			this.isElected = false;
			this.isOk = false;
			serverSocket = new ServerSocket(ElectionManagerPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setCoordinator(String coordinator) {
		this.coordinator = coordinator;
		isElected = true;
	}
	
	public String getCoordinator() {
		return this.coordinator;
	}
	
	public void close() {
		try {
			if(!serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {
			
		}
		this.interrupt();
	}
	
	/* Election 메시지를 보내고 응답이 없으면 자신이 Coordinator가 됨
	 * Ok 메시지를 받고 Coordinator 메시지를 일정 시간 내에 받지 못하면 Election을 다시 시작 */
	public void timeout(int type) {
		switch (type) {
			case Election :
				System.out.println("timeout(Election)");
				send_coordinator();
				break;
			case Ok :
				System.out.println("timeout(Ok)");
				send_election();
				break;
		}
	}
	
	/* 자신의 index보다 작은 서버에 Coordinator 메시지를 전송 */
	public void send_coordinator() {
		System.out.println("send_coordinator()");
		Socket sock = null;

		for(int i = 0 ; i <= index; i++) {
			try {
				sock = new Socket();
				sock.connect(new InetSocketAddress(list.get(i), ElectionManagerPort), SocketTimeout);				
				
				dos = new DataOutputStream(sock.getOutputStream());
				dos.writeInt(Coordinator);
				dos.close();
				sock.close();
			} catch (IOException e) {
				try {
					sock.close();
					//System.out.println("send_coordinator() " + list.get(i) + " is not running");
				} catch (IOException _e) {
					
				}
			} 
		}
	}
	
	/* Coordinator 메시지를 받으면  해당 서버를  Coordinator로 설정 */ 
	public void respond_coordinator() {
		System.out.println("respond_coordinator()");
		//isOk = false; isElected = true;
		if((timer != null) && timer.isAlive()) timer.interrupt();
		setCoordinator(socket.getInetAddress().toString().replaceAll("/", ""));
	}
	
	/* Ok 메시지를 받으면 Coordinator를 포기하고 Coordinator 메시지를 대기 */
	public void respond_ok() {
		System.out.println("respond_ok()");
		// isOk = true;
		if((timer != null) && timer.isAlive()) timer.interrupt();
		timer = new Timer(this, Ok);
		timer.start();
	}
	
	/* Election 메시지를 받으면 Ok 메시지를 전송하고, 자신의 index보다 큰 서버에 Election 메시지를 전송 */
	public void respond_election() {
		
		/* if(isOk == false) */
		
		System.out.println("respond_election()");
		Socket sock = null;
		String ip = socket.getInetAddress().toString().replaceAll("/", "");
		
		try {
			sock = new Socket();
			sock.connect(new InetSocketAddress(ip, ElectionManagerPort), SocketTimeout);				

			dos = new DataOutputStream(sock.getOutputStream());
			dos.writeInt(Ok);
			dos.close();
			sock.close();
		} catch (IOException e) {
			try {
				sock.close();
				System.out.println("respond_election() " + ip.toString() + " is not running");
			} catch (IOException _e) {
				
			}
		}
		
		send_election();
	}
	
	/* 자신의 index보다 큰 서버에 Election 메시지를 전송 */
	public void send_election() {
		System.out.println("send_election()");
		Socket sock = null;
		
		//isElected = false; isOk = false;
		
		for(int i = index + 1 ; i < list.size(); i++) {
			try {
				sock = new Socket();
				sock.connect(new InetSocketAddress(list.get(i), ElectionManagerPort), SocketTimeout);				
				dos = new DataOutputStream(sock.getOutputStream());
				dos.writeInt(Election);
				dos.close();
				sock.close();
			} catch (IOException e) {
				try {
					sock.close();
					//System.out.println("send_election() " + list.get(i) + " is not running");
				} catch (IOException _e) {
					
				}
			}
		}
		
		if((timer != null) && timer.isAlive()) timer.interrupt();
		timer = new Timer(this, Election);
		timer.start();
	}
	
	public void run() {
		
		send_election();
			
		while(!Thread.currentThread().interrupted()) {
			try {
				socket = serverSocket.accept();
				dis = new DataInputStream(socket.getInputStream());
				
				String ip = socket.getInetAddress().toString().replaceAll("/", "");
				switch(dis.readInt()) {
					case Election :
						System.out.println(ip + " send Election");
						//if (isOk == false)
						respond_election();
						break;
					case Ok :
						System.out.println(ip + " send Ok");
						respond_ok();
						break;
					case Coordinator :
						System.out.println(ip + " send Coordinator");
						respond_coordinator();
						break;
				}
				
				dis.close();
				socket.close();
			} catch (IOException e) {
				
			}
		}
		
		if((timer != null) && timer.isAlive()) timer.interrupt();
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			
		}
		
		System.out.println("ElectionManager Down");
	}
}
