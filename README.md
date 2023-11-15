#개요  
프로토콜(텍스트)을 이용한 클라이언트-서버 request, response로 사칙연산 계산  


# Requirment
< 요구조건 #1: 사칙연산 수행: addition, subtraction, multiplication, division >  
< 요구조건 #2: 프로토콜을 이용하여 request,response를 주고받음. >  
< 요구조건 #3: 연산식 에러가 있다면 에러 메시지 회신. (error types + meaning) 처리 >  
< 요구조건 #4: Threadpool&Runable interface : 서버의 멀티스레딩 기능. >  
< 요구조건 #5: 서버의 ip, port는 server_info에 있는 정보를 불러와 처리, 없으면 default값 이용>  


# Protocol  
OPERATION operand1 operand2  
OPERATION 종류 : ADD, SUB, MUL DIV  
Ex) ADD 1 2  

