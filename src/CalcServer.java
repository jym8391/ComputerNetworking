import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalcServer {
    private static final String ERROR_PREFIX = "ERR: ";
    private static final String ANS = "ANS: ";

    public static String processRequest(String request) {
        // 요청 프로토콜을 처리하고 응답을 반환하는 메서드
        String[] tokens = request.split(" ");
        if (tokens.length != 3)
            return ERROR_PREFIX + "Invalid request";

        try {
            int op1 = Integer.parseInt(tokens[1]);
            String opcode = tokens[0];
            int op2 = Integer.parseInt(tokens[2]);

            switch (opcode) {
                case "ADD":
                    return ANS+Integer.toString(op1 + op2);
                case "SUB":
                    return ANS+Integer.toString(op1 - op2);
                case "MUL":
                    return ANS+Integer.toString(op1 * op2);
                case "DIV":
                    if (op2 != 0) {
                        return ANS+Double.toString((double) op1 / op2);
                    } else {
                        return ERROR_PREFIX + "Division by zero";
                    }
                default:
                    return ERROR_PREFIX + "Unsupported operation";
            }
        } catch (NumberFormatException e) {
            return ERROR_PREFIX + "Invalid operand";
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

            // Threadpool 생성
            ExecutorService executorService = Executors.newFixedThreadPool(10); // 적절한 스레드 수를 설정

            while (true) {
                Socket socket = listener.accept(); // 클라이언트로부터 연결 요청 대기
                // 클라이언트 처리를 위한 스레드 시작
                executorService.submit(new ServerThread(socket));
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
    private static class ServerThread implements Runnable {
        private Socket socket;
    
        public ServerThread(Socket socket) {
            this.socket = socket;
        }
    
        @Override
        public void run() {
            BufferedReader inFromClient = null;
            DataOutputStream outToClient = null;
            System.out.println(socket+"연결되었습니다.");
            try {
                inFromClient = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                outToClient = new DataOutputStream(socket.getOutputStream());
    
                while (true) {
                    String request = inFromClient.readLine();
                    if (request == null || request.equalsIgnoreCase("bye")) {
                        System.out.println("클라이언트에서 연결을 종료하였음");
                        break; // 연결 종료
                    }
    
                    System.out.println("Thread " + Thread.currentThread().getName() + " received request: " + request);
    
                    // 요청을 처리하고 응답 생성
                    String response = processRequest(request);
    
                    // 응답(결과,에러)을 클라이언트에게 전송

                    outToClient.writeBytes(response + "\n");
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
                System.out.println(socket + " 소켓을 닫았습니다.");
            }
        }
    }
}