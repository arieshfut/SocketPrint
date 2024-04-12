import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



public class WifiPrinterServer extends Thread {
    private final static String TAG = "WifiPrinterServer";
    private int mPort;

    private Socket mSocket;
    private ServerSocket mServerSocket;
    private OutputStream out = null;
    private InputStream in = null;

    public static String PJL_CMD_INFO_ID = "@PJL INFO ID\r\n";

    public WifiPrinterServer(int port) {
        this.mPort = port;
        try {
            mServerSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(TAG + "constructor done.");
    }

    public static void main(String[] args) {
        WifiPrinterServer server = new WifiPrinterServer(9100);
        server.start();
    }

    @Override
    public void run() {
        super.run();
        try {
            System.out.println("wait client connect...");
            mSocket = mServerSocket.accept();
            in = mSocket.getInputStream();
            out = mSocket.getOutputStream();

            String ip = mSocket.getInetAddress().getHostAddress();
            int port = mSocket.getPort();
            System.out.println("Client " + ip + ":" + port + " connect SUCCESS...");
            FileOutputStream fos = new FileOutputStream(currentTime() + "_output.txt");
            int len = 0;
            byte[] buf = new byte[1024];

            int pos = 0;
            int size = 0;
            while (mSocket.isConnected()) {
                while ((len = in.read(buf)) > 0) {
                    if (!mSocket.isConnected()) {
                        break;
                    }

                    pos = 0;
                    size = len;
                    String buffer = new String(buf);
                    if (buffer.startsWith(PJL_CMD_INFO_ID)) {
                        System.out.println("Receive client command:" + PJL_CMD_INFO_ID);
                        new SendMassageThread().start();
                        pos = PJL_CMD_INFO_ID.getBytes(StandardCharsets.UTF_8.name()).length;
                        size = len - pos;
                    }
                    fos.write(buf, pos, size);
                }
            }

            if (in != null) {
                in.close();
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
            System.out.println("Client " + ip + ":" + port + " disconnect...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SendMassageThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                if (out != null) {
                    String in = "@PJL INFO ID\r\n\"FUJI XEROX DocuCentre-V C3373 T2\"\r\n";
                    out.write(in.getBytes());
                    out.flush();//清空缓存区的内容
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String currentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return now.format(formatter);
    }
}