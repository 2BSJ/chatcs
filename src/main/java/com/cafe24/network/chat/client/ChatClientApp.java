package com.cafe24.network.chat.client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientApp {
	private static final String SERVER_IP = "192.168.1.5";
	private static final int SERVER_PORT = 7002;

	public static void main(String[] args) {
		String name = null;
		Scanner scanner = new Scanner(System.in);
		Socket socket = null;
		while( true ) {

			System.out.println("대화명을 입력하세요.");
			System.out.print(">>> ");
			name = scanner.nextLine();

			if (name.isEmpty() == false && "".equals(name)==false &&name.contains(" ")==false) {
				break;
			}
			//join프로토콜을 서버로 보내줘야하는 부분

			System.out.println("대화명은 한글자 이상 입력해야 합니다.\n");
			//서버에서 응답이오면 while 끝내고 밑으로
		}


		socket = new Socket();
		try {
			//1.소켓 만들고 연결
			socket.connect(new InetSocketAddress(SERVER_IP,SERVER_PORT));
			
			new ChatWindow(name,socket).show();
			//2.iostream 작업해주고
			PrintWriter pw = new PrintWriter(
					new OutputStreamWriter(socket.getOutputStream(),"utf-8"),true );
			BufferedReader br = new BufferedReader(
				new InputStreamReader(socket.getInputStream(),"utf-8") );
			
			//3.join 프로토콜을 보낸다 join ok가 나오면
			pw.println("JOIN:"+name+":\r\n");

			pw.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
				if(scanner !=null) 
					scanner.close();
		}
					

		//scanner.close();

		//4. chatwindow.show 실행.
		//listen thread로 읽다가 send해주면 보내주고
		
	}

}
