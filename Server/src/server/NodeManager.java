package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class NodeManager extends Thread implements Constant {
	
	private Server server;
	private String ip;
	private String coordinator;
	private boolean isMasterAlive;
	
	private DataInputStream dis;
	private DataOutputStream dos;
	
	public NodeManager(String ip, String coordinator) {
		this.ip = ip;
		this.coordinator = coordinator;
		this.isMasterAlive = true;
	}
	
	public void setIsMasterAlive(boolean isMasterAlive) {
		this.isMasterAlive = isMasterAlive;
	}
	
	public boolean getIsMasterAlive() {
		return this.isMasterAlive;
	}
	
	public void close() {
		this.interrupt();
	}
	
	public void send_heartbeating() {
		//System.out.println("send_heartbeating() " + coordinator);
		Socket sock = null;
		
		try {
			sock = new Socket();
			sock.connect(new InetSocketAddress(coordinator, ResourceManagerPort), SocketTimeout * 10);				
			
			dos = new DataOutputStream(sock.getOutputStream());
			dos.writeInt(HeartBeating);
			dos.close();
			sock.close();
			setIsMasterAlive(true);
		} catch (IOException e) {
			try {
				sock.close();
				System.out.println("Coordinator is not running");
				setIsMasterAlive(false);
			} catch (IOException _e) {
				
			}
		}
	}
	
	public void run() {
		while(!Thread.currentThread().interrupted()) {
			try {
				send_heartbeating();
				Thread.sleep(HeartBeatingPeriod);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		System.out.println("NodeManager Down");
	}
}
