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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChatServerThread extends Thread{

	private String nickname=null;
	private Socket socket=null;
	private List<PrintWriter> pwList = null;
	private Map<String,PrintWriter> userList = null;
	private int idDuplicationCount = 0;
	private List<String> contentList = null;
	private int listSize = 0;
	private int currentCurser = 0;
	public ChatServerThread(Socket socket,List pwList,Map userList) {
		this.socket=socket;
		this.pwList=pwList;
		this.userList=userList;
	}

	@Override
	public void run() {

		InetSocketAddress inetRemoteSocketAddress = (InetSocketAddress)socket.getRemoteSocketAddress();
		String remoteHostAddress = inetRemoteSocketAddress.getAddress().getHostAddress(); //ip주소 반환
		int remotePort = inetRemoteSocketAddress.getPort(); //port주소 반환
		ChatServer.log("connected by client[" + remoteHostAddress + ":" + remotePort + "]");
		contentList = new ArrayList<String>();
		
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
					System.out.println(tokens[1]);
					Join(tokens[1],pw);
					
				}
				else if("MSG".equals(tokens[0])) { //메세지를 보냈을때.
						
					Msg(tokens[1]);
					contentList.add(tokens[1]);
					listSize+=1;
					currentCurser = listSize-1;
				}
				else if("WP".equals(tokens[0])) { //귓속말 // WP:nickname:/w  승준    하이
					if(this.nickname.equals(tokens[1])) {//[0]   [1]    [2]
						Wp(tokens[1],tokens[2]);
					}
				}
				else if("COMMAND_UP".equals(tokens[0])) { // up키가 들어왔을때엔
					if(currentCurser==0) {
						pw.println("/re" + "");
					}
					else {
					String recontent = contentList.get(currentCurser);
					currentCurser-=1;
					pw.println("/re" + recontent);
					}
					
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
		idDuplicationCount = addWriter(pw,nickname); // pwList와 userList맵에 추가
		if(idDuplicationCount==1)
			broadcast(msg + "님이 입장하셨습니다");
		
		
		//pw.println("join:ok");
		//pw.flush();
	}
	private void Msg(String msg) {
		broadcast(this.nickname + ":" + msg);
	}
	private void Exit(PrintWriter pw) {
		if(idDuplicationCount==1)
			broadcast(this.nickname + "님이 퇴장하셨습니다.");
		removeWriter(pw);
	}
	private void Wp(String myname,String msg) { //  /w 양승준 ㅎㅇ
		String[] token = msg.split(" ");
		//token[0] /w
		//token[1] 상대방 nickname
		//token[2] 내용
		PrintWriter Wppw1=userList.get(myname);
		if(myname.equals(token[1])) { // 귓속말이 자기 자신이라면
			Wppw1.println("자기 자신에겐 귓속말을 보낼 수 없습니다.");
		}
		else if(userList.containsKey(token[1])) { //귓속말 보낼 아이디가 있으면
			PrintWriter Wppw2=userList.get(token[1]);
			Wppw1.println(msg + "(성공)");
			Wppw2.println(myname + ">>" + token[2]);
		}
		else { //없으면
			Wppw1.println(msg + "(실패)(보내실 아이디를 다시확인해주세요)");
		}
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
	private int addWriter(PrintWriter pw,String nickname) {
		
		synchronized(userList) {
			if(userList.containsKey(nickname)) {//추가하려는데 중복되는 아이디가 있을시 종료 프로토콜
				ChatServer.log("아이디가 중복이므로 클라이언트를 종료합니다");
				pw.println("equals");
				return -1;
			}
			else {
				
			userList.put(nickname, pw);
			
			
			}
		}
		
		synchronized(pwList) {
			pwList.add(pw);
			return 1;
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
		synchronized(userList) {
			userList.remove(nickname);
		}
	}
}
