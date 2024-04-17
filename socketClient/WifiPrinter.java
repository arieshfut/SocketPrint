import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class WifiPrinter {
    private final static String TAG = "WifiPrinter";

    private final char ESC = (char)27;
    private final char CR = (char)13;
    private final String LF = "\r\n";
    private final String UEL = "%-12345X";
    private final String PJL = "@PJL";
    private String title = "" + ESC + UEL + PJL + LF;
    private String tail = "" + ESC + UEL + LF;

    private final String mEncode;
    private String mIp;
    private int mPort;

	private Socket mSocket;
	private ServerSocket mServerSocket;
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private BufferedReader mReader;

	public WifiPrinter(String ip, int port) {
		mEncode = StandardCharsets.UTF_8.name();
		this.mIp = ip;
		this.mPort = port;
		System.out.println(TAG + " " + ip + ":"+ port + " constructor done.");
	}

	public static void main(String[] args) {
		String ip = "172.18.2.51";
		int port = 9100;
		if (args.length > 0) {
			ip = args[0];
		}
		if (args.length > 1) {
			port = Integer.parseInt(args[1]);
		}
		WifiPrinter wifi = new WifiPrinter(ip, port);
		wifi.connect();
		wifi.printerInfo();
	}

	public void connect() {
		try {
			mSocket = new Socket(mIp, mPort);
			System.out.println(TAG + " " + mIp + ":"+ mPort + " connected...");
			mInputStream = mSocket.getInputStream();
			mOutputStream = mSocket.getOutputStream();
			mReader = new BufferedReader(new InputStreamReader(mInputStream));
		} catch (UnknownHostException e) {
			System.out.println(TAG + " UnknownHostException" + e);
		} catch (IOException e) {
			System.out.println(TAG + " IOException" + e);
		}

		new Thread(new ReadHandlerThread(mSocket)).start();
	}

	/*
	 * receive message from remote printer
	 */
	class ReadHandlerThread implements Runnable{
		private Socket client;

		public ReadHandlerThread(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			System.out.println(TAG + " start ReadHandlerThread.");
			try {
				while(true) {
					// convert message and print
					String message = mReader.readLine();
					if (message != null)
						System.out.println(TAG + " Receive message:" + message);
				}
			} catch (IOException e) {
				System.out.println("ReadHandlerThread IOException:" + e);
			}
		}
	}

	public void sendMsg(String message) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (mSocket != null && isConnect()) {
					try {
						mOutputStream.write(title.getBytes(mEncode));
						mOutputStream.write(message.getBytes(mEncode));
						mOutputStream.write(tail.getBytes(mEncode));
						mOutputStream.flush();
						// mSocket.shutdownOutput();
						System.out.println(TAG + " Send message:" + message + "done!");
					} catch (IOException e) {
						System.out.println(TAG + " sendMsg message error!" + e);
					}
				} else {
					System.out.println(TAG + "Socket disconnect can not send message!");
				}
			}
		}).start();
	}

	public String printerInfo() {
		String cmd = "@PJL INFO ID\r\n";
		// String cmd = "@PJL ECHO HELLO\r\n";
		// String cmd = "@PJL INFO ID\r\n";
		// String cmd = "@PJL INFO CONFIG\r\n";
		// String cmd = "@PJL INFO STATUS\r\n";
		// String cmd = "@PJL INFO VARIABLES\r\n";
		// String cmd = "@PJL INFO USTATUS\r\n";
		// String cmd = "@PJL INFO FILESYS\r\n";
		sendMsg(cmd);
		return "printerInfo done";
	}

    public boolean isConnect() {
		return (mSocket != null) && (!mSocket.isClosed()) && mSocket.isConnected();
	}
	
}