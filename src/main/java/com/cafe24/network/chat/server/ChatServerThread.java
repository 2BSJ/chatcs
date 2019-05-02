package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.List;

public class ChatServerThread extends Thread{

	private String nickname=null;
	private Socket socket=null;
	private List<PrintWriter> pwList = null;

	public ChatServerThread(Socket socket,List pwList) {
		this.socket=socket;
		this.pwList=pwList;
	}

	@Override
	public void run() {

		InetSocketAddress inetRemoteSocketAddress = (InetSocketAddress)socket.getRemoteSocketAddress();

		String remoteHostAddress = inetRemoteSocketAddress.getAddress().getHostAddress(); //ip주소 반환
		int remotePort = inetRemoteSocketAddress.getPort(); //port주소 반환
		ChatServer.log("connected by client[" + remoteHostAddress + ":" + remotePort + "]");
		
		try {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(os,"utf-8"),true);
			

			while(true) {
				
				String data = br.readLine();

				//버퍼리더가 버퍼를 읽어오면
				
				
				if(data==null) {//읽어온 버퍼가 null 일때 
					ChatServer.log("closed by client");
					Exit(pw);
					break;
				}
				String[] tokens = data.split(":");
				//System.out.println(tokens[0]);
				if("JOIN".equals(tokens[0])) { //입장했을때 아이디 및 설정
					
					Join(tokens[1],pw);
					
				}
				else if("MSG".equals(tokens[0])) { //메세지를 보냈을때.
						
					Msg(tokens[1]);
				}
//				else if("EXIT".equals(tokens[0])) { //퇴장했을때
//					System.out.println("bb");
//					Exit(pw);
//				}



			}


		} catch (IOException e) { //클라 윈도우에서 강제종료로 IOExceptio이 났을때
			ChatServer.log("sudden closed by client");
			
			//broadcast(this.nickname + "님이 퇴장하셨습니다");
		} 
		
		finally {
			try {
				if(socket != null && socket.isClosed() == false )
					socket.close();
			}
			catch(SocketException e)
			{
				ChatServer.log("sudden closed by client");
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}


	}

	private void Join(String msg,PrintWriter pw) {
		this.nickname = msg;
		broadcast(msg + "님이 입장하셨습니다");
		addWriter(pw);
		//pw.println("join:ok");
		//pw.flush();
	}
	private void Msg(String msg) {
		broadcast(this.nickname + ":" + msg);
	}
	private void Exit(PrintWriter pw) {
		broadcast(this.nickname + "님이 퇴장하셨습니다.");
		removeWriter(pw);
	}
	private void broadcast(String msg) {
		synchronized(pwList) {
			Iterator<PrintWriter> it = pwList.iterator();
			while(it.hasNext()) { //이터돌려서 모든 생성된 소켓의 pw로 다 출력내보냄
				PrintWriter pw = it.next();
				pw.println(msg);
				pw.flush();
			}
		}
	}
	private void addWriter(PrintWriter pw) {
		synchronized(pwList) {
			pwList.add(pw);
		}
	}
	private void removeWriter(PrintWriter pw) {
		int count =0;
		synchronized(pwList) {
			pwList.remove(pw);
			/*Iterator<PrintWriter> it = pwList.iterator();
			while(it.hasNext()) { //이터돌려서 모든 생성된 소켓의 pw로 다 출력내보냄
				PrintWriter comparePw = it.next();
				if(comparePw==pw)
					pwList.remove(count);
				count++;
			}*/
		}
	}
}
