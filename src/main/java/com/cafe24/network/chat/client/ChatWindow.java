package com.cafe24.network.chat.client;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;

public class ChatWindow {

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private Socket socket;

	public ChatWindow(String name,Socket socket) {
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		this.socket = socket;
		new ChatClientThread(socket).start();
	}
	
	private PrintWriter pw = null;
	private BufferedReader br = null;

	public void show() {
		// Button
		
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent actionEvent ) {
				sendMessage(); //메세지 받는부분
			}
		});

		// Textfield
		textField.setColumns(80);
		//추가해준것
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
			
			//	키값에 관한 이벤트 처리가능함.
				char keyCode = e.getKeyChar();
				if(keyCode == KeyEvent.VK_ENTER) {//엔터눌렀을때 감지함
					sendMessage();
				}
			
			}
			
		});

		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				
				if(pw==null) { //윈도우 열리자마자 닫으면 프린터라이터 객체가 없으니까
					try {
						pw = new PrintWriter(
								new OutputStreamWriter(socket.getOutputStream(),"utf-8"),true );
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				finish(pw);//System.exit(0);
			}
		});
		frame.setVisible(true);
		frame.pack();
		
		//thread 생성
		//요구사항 6번은 quit으로 하지말고 x버튼누르거나 해서 소켓닫힌거 확인하면 보내주는걸로
	}
	
	private void finish(PrintWriter pw) {
		//socket정리
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//pw.println("EXIT:\r\n");
		System.exit(0);
	}
	
	//화면에 뜬게 frame이고 밑에 입력받는곳은 pannel이다.
	//pannel 의 자식윈도우인 button이 있고 pannel 안에 textfield가 있음.
	//thread에서 데이터 받으면 textarea에 추가해주고 textfield에 데이터치면 socket으로 보냄
	private void updateTextArea(String message) {//무조건 쓰레드에서 부를것!!
		
		textArea.append(message);
		textArea.append("\n");
	}
	
	private void sendMessage() { //소켓받은거로 프린트 롸이트 해주면댐
		try {
			pw = new PrintWriter(
					new OutputStreamWriter(socket.getOutputStream(),"utf-8"),true );
			String message = textField.getText();
			if("".equals(message)) { //엔터들어오면 프린트라이트를 따로 출력해줌
				pw.println("MSG: \r\n");
			}
			else
				pw.println("MSG:" + message+"\r\n");
			//서버가 받고 브로드캐스팅해준다.
			
			textField.setText("");//메세지를 전송했으면 텍스트창을 비워줌.
			textField.requestFocus();//커서를 표시해줌 다시
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		
		
		
		//test
		
		//updateTextArea(message);
	}
	
	
	private class ChatClientThread extends Thread{
	    Socket socket = null;
	      
	    ChatClientThread(Socket socket){
	      this.socket = socket;
	    }
	  
	    public void run() {
	      try {
	        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
	        while(true) {
	          String message = br.readLine();
	  
	          updateTextArea(message);
	        }
	      }
	      catch (SocketException e) {
	    	
	      }
	      catch (IOException e) {
	        e.printStackTrace();
	      }

	    }
	  }
	
	
	}
	
	
	

