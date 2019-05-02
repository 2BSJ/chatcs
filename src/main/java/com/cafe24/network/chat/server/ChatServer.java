package com.cafe24.network.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

	private static final int SERVER_PORT = 7002;
	
	public static void main(String[] args) {
		
		ServerSocket serverSocket = null;
		List<PrintWriter> pwList = new ArrayList<PrintWriter>();
		try {
			//1.serverSocket으로 서버소켓 생성.
			serverSocket = new ServerSocket();
			//2.소켓연결을 위한 주소바인딩 설정
			serverSocket.bind(new InetSocketAddress("0.0.0.0", SERVER_PORT));
			
			while(true) {
			//3.accept로 클라이언트 요청이 들어올때까지 코드를 블락해놓음
				
				Socket socket = serverSocket.accept();//안들어오면 아직 blocking 상태
				
				Thread thread = new ChatServerThread(socket,pwList);
				thread.start();
				
			}
			
			
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			try {
				if(serverSocket != null && serverSocket.isClosed() == false)
					serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void log(String log)
	{
		System.out.println("[server#"+Thread.currentThread().getId()+ " ]"+ log);
	}

}
