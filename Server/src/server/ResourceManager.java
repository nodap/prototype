package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ResourceManager extends Thread implements Constant {

	private Server server;
	private String ip;
	
	private HashMap<String, Integer> node;
	private int nodeCount;
	
	private ServerSocket serverSocket;
	private Socket socket;
	
	private DataInputStream dis;
	private DataOutputStream dos;
	
	private Timer timer;
	
	public ResourceManager(String ip) {
		this.ip = ip;
		this.node = new HashMap<String, Integer>();
		try {
			serverSocket = new ServerSocket(ResourceManagerPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		nodeCount = 0;
		timer = new Timer(this, HeartBeating);
		timer.start();
	}
	
	public void close() {
		try {
			if(!serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {
			
		}
		this.interrupt();
		if((timer != null) && timer.isAlive()) timer.interrupt();
	}
	
	public void update_node() {
		for(Map.Entry<String, Integer> entry : node.entrySet()) {
			entry.setValue(entry.getValue() + TimerPeriod);
			//System.out.println(entry.getKey() + " " +entry.getValue());
			if(entry.getValue() > HeartBeatingTimeout) {
				node.remove(entry.getKey());
				nodeCount = node.size();
				System.out.println(entry.getKey() + " Down");
			}
		}
	}
	
	public void respond_heartbeating() {
		String ip = socket.getInetAddress().toString().replaceAll("/", "");
		//System.out.println("respond_heartbeating() " + ip);
		node.put(ip, 0);
		nodeCount = node.size();
	}
	
	public void run() {
		while(!Thread.currentThread().interrupted()) {
			try {
				socket = serverSocket.accept();
				dis = new DataInputStream(socket.getInputStream());
				
				switch(dis.readInt()) {
					case HeartBeating :
						respond_heartbeating();
						break;
				}

				dis.close();
				socket.close();
			} catch (IOException e) {
				
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			
		}
		
		System.out.println("ResourceManager Down");
	}
	
}
