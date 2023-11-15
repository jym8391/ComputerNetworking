import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Scanner;

public class CalcClient {
    public static void main(String[] args) {
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);
        try {
            // 서버 정보 가져오기
            Properties properties = loadProperties();
            String serverIp = properties.getProperty("server_ip");
            int serverPort = Integer.parseInt(properties.getProperty("server_port"));

            // 서버에 연결
            socket = new Socket(serverIp, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            while (true) {
                System.out.print("연산 요청(빈칸으로 띄어 입력, 예:ADD 24 42) >> "); // 프롬프트
                String outputMessage = scanner.nextLine(); // 키보드에서 연산 요청 읽기

                if (outputMessage.equalsIgnoreCase("bye")) {
                    out.write(outputMessage + "\n"); // "bye" 문자열 전송
                    out.flush();
                    break; // 사용자가 "bye"를 입력한 경우 서버로 전송 후 연결 종료
                }

                out.write(outputMessage + "\n"); // 키보드에서 읽은 연산 요청 문자열 전송
                out.flush();

                String inputMessage = in.readLine(); // 서버로부터 계산 결과 수신
                System.out.println("계산 결과: " + inputMessage);
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
}