## WindowChatting 과제

#### 명령어

/w [아이디] \[내용\]

#### ChatServer

----
#### ChatServerThread

* UP키 누를시 자신이 쳤던 채팅 다시 볼 수 있음
* 귓속말 구현 : /w [아이디] \[내용\]
* 귓속말시 없는 아이디는 귓속말 실패로 출력 || 자기 자신에게 귓속말 보내기 금지
* Join 메서드안에 addWriter에 아이디 중복검사후 중복일시 클라종료
  클라종료시 ChatWindow에서 finish 메서드를 통해 소켓 close
  

  
----

#### ChatWindow

* sendMessage()에서 엔터오류 수정

----
#### ChatClientApp

* 닉네임 입력받을때 공백 금지 엔터 금지

----