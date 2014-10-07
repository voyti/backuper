import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class ConnectionManager extends Communicator{
	private Socket socket;
	private String ipAddress;
	private int port;
	private PrintWriter protocolOut;
	private BufferedReader in;

	public ConnectionManager() throws UnknownHostException, IOException {

	}

	public void initConnection() throws UnknownHostException, IOException {
		socket = new Socket(ipAddress, port);
        protocolOut = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        sendServerQuery(Protocol.WELCOME_MESSAGE);

		System.out.println("Accepted connection : " + socket);
	}

	public void receiveFile() {
		try {
			int filesize = 1022386;
			int bytesRead;
			int currentTot = 0;
			byte[] bytearray = new byte[filesize];
			InputStream is = socket.getInputStream();
			FileOutputStream fos = new FileOutputStream("copy.doc");
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bytesRead = is.read(bytearray, 0, bytearray.length);
			currentTot = bytesRead;

			do {
				bytesRead = is.read(bytearray, currentTot, (bytearray.length - currentTot));
				if (bytesRead >= 0)
					currentTot += bytesRead;
			}
			while (bytesRead > -1);

			bos.write(bytearray, 0, currentTot);
			bos.flush();
			bos.close();
			socket.close();
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendFile(File transferFile) throws IOException {
		sendServerQuery(Protocol.SENDING_FILE + ":" + transferFile.getName() + ":" + transferFile.length());

		byte[] bytearray = new byte[(int) transferFile.length()];
		FileInputStream fin = new FileInputStream(transferFile);
		BufferedInputStream bin = new BufferedInputStream(fin);
		bin.read(bytearray, 0, bytearray.length);
		OutputStream os = socket.getOutputStream();
		System.out.println("Sending File...");
		os.write(bytearray, 0, bytearray.length);
		os.flush();
		fin.close();
		bin.close();
		//socket.close();
		System.out.println("File transfer complete");
	}

	public void sendFilesFromDir(File path) throws IOException {
		File[] files = path.listFiles();
		this.emit("totalFilesToSend", files.length);
		int i;
		for (i = 0; i < files.length; i++) {
			this.emit("filesSent", i);
			sendFile(files[i]);
		}
		this.emit("filesSent", i);
		this.emit("allFilesSent", true);
	}

	private void sendServerQuery(String query) {
	    protocolOut.println(query);
	    System.out.println("CLIENT SENT: " + query);
	}


	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void getFilesFromServer(File selectedFile) {


	}
}
