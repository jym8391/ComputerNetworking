import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Scanner;

public class CalcClient {
    public static void main(String[] args) {
        BufferedReader inFromUser = null;
        DataOutputStream outToServer = null;
        Socket socket = null;

        Scanner scanner = new Scanner(System.in);
        
        try {
            // 서버 정보 가져오기
            Properties properties = loadProperties();
            String serverIp = properties.getProperty("server_ip");
            int serverPort = Integer.parseInt(properties.getProperty("server_port"));

            // 서버에 연결
            socket = new Socket(serverIp, serverPort);
            inFromUser = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToServer = new DataOutputStream(socket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {
                System.out.print("연산 요청(빈칸으로 띄어 입력) ex:ADD 10 20 >> "); // 프롬프트
                String outputMessage = scanner.nextLine(); // 키보드에서 연산 요청 읽기

                if (outputMessage.equalsIgnoreCase("bye")) {
                    System.out.println("클라이언트를 종료합니다.");
                    break; // 사용자가 "bye"를 입력한 경우 서버로 전송 후 연결 종료
                }

                outToServer.writeBytes(outputMessage + "\n"); // 키보드에서 읽은 연산 요청 문자열 전송


                String serverResponse = inFromServer.readLine(); // 서버로부터 계산 결과 응답 수신

                displayResponse(serverResponse);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                scanner.close();
                if (socket != null)
                    socket.close(); // 클라이언트 소켓 닫기
            } catch (IOException e) {
                System.out.println("서버와 채팅 중 오류가 발생했습니다.");
            }
        }
    }

    private static Properties loadProperties() {
        // 파일에서 서버 정보를 읽어오거나 디폴트 값 설정
        Properties properties = new Properties();

        try {
            properties.load(new FileReader("src\\server_info.txt"));
        } catch (IOException | NullPointerException e) {
            // 파일이 없거나 읽을 수 없는 경우 디폴트 값으로 설정
            properties.setProperty("server_ip", "localhost");
            properties.setProperty("server_port", "9999");
            System.out.println("서버 정보를 읽어오지 못했습니다. 디폴트 값으로 설정합니다.");
        }

        return properties;
    }

    private static void displayResponse(String serverResponse) {
        // 서버 응답을 공백으로 분리하여 처리
        String[] tokens = serverResponse.split("\\s", 2);

        if (tokens.length >= 2) {
            String response = tokens[0];
            String data = tokens[1];

            // 응답에 따라 결과 또는 에러 출력
            switch (response) {
                case "ANS:":
                    System.out.println("결과: " + data);
                    break;
                case "ERR:":
                    System.out.println("에러 메세지: " + data);
                    break;
                default:
                    System.out.println("알 수 없는 응답 형식: " + serverResponse);
            }
        } else {
            System.out.println("알 수 없는 응답 형식: " + serverResponse);
        }
    }
}