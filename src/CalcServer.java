import java.io.*;
import java.net.*;
import java.util.Properties;

public class CalcServer {

    public static String processRequest(String request) {
        // 요청 프로토콜을 처리하고 응답을 반환하는 메서드
        String[] tokens = request.split(" ");
        if (tokens.length != 3)
            return "error: invalid request";

        try {
            int op1 = Integer.parseInt(tokens[1]);
            String opcode = tokens[0];
            int op2 = Integer.parseInt(tokens[2]);

            switch (opcode) {
                case "ADD":
                    return Integer.toString(op1 + op2);
                case "SUB":
                    return Integer.toString(op1 - op2);
                case "MUL":
                    return Integer.toString(op1 * op2);
                case "DIV":
                    if (op2 != 0) {
                        return Double.toString((double) op1 / op2);
                    } else {
                        return "error: division by zero";
                    }
                default:
                    return "error: unsupported operation";
            }
        } catch (NumberFormatException e) {
            return "error: invalid operand";
        }
    }

    public static void main(String[] args) {

        ServerSocket listener = null;

        try {
            // 파일에서 서버 정보를 읽어옴
            Properties properties = loadProperties();

            // 서버 정보 가져오기
            String serverIp = properties.getProperty("server_ip");
            int serverPort = Integer.parseInt(properties.getProperty("server_port"));

            // 서버 소켓 생성
            listener = new ServerSocket(serverPort, 50, InetAddress.getByName(serverIp));
            System.out.println("연결을 기다리고 있습니다.....");

            while (true) {
                Socket socket = listener.accept(); // 클라이언트로부터 연결 요청 대기
                System.out.println("연결되었습니다.");

                // 클라이언트 처리를 위한 스레드 시작
                new ServerThread(socket).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (listener != null)
                    listener.close(); // 서버 소켓 닫기
            } catch (IOException e) {
                System.out.println("서버 소켓 닫기 중 오류가 발생했습니다.");
            }
        }
    }

    private static Properties loadProperties() {
        // 파일에서 서버 정보를 읽어오거나 디폴트 값 설정
        Properties properties = new Properties();

        try {
            properties.load(new FileReader("src\\server_info.txt"));
        } catch (IOException | NullPointerException e) {
            properties.setProperty("server_ip", "localhost");
            properties.setProperty("server_port", "9999");
            System.out.println("서버 정보를 읽어오지 못했습니다. 디폴트 값으로 설정합니다.");
        }

        return properties;
    }

    // 각 클라이언트 처리를 위한 스레드 클래스
    private static class ServerThread extends Thread {
        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream()));

                while (true) {
                    String request = in.readLine();
                    if (request == null || request.equalsIgnoreCase("bye")) {
                        System.out.println("클라이언트에서 연결을 종료하였음");
                        break; // 연결 종료
                    }

                    System.out.println("Received request: " + request);

                    // 요청을 처리하고 응답 생성
                    String response = processRequest(request);

                    // 응답을 클라이언트에게 전송
                    out.write(response + "\n");
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println("클라이언트와 통신 중 오류가 발생했습니다.");
            } finally {
                try {
                    if (socket != null)
                        socket.close(); // 통신용 소켓 닫기
                } catch (IOException e) {
                    System.out.println("클라이언트 소켓 닫기 중 오류가 발생했습니다.");
                }
            }
        }
    }
}