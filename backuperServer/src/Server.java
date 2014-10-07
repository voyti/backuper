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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JOptionPane;

public class Server {

	private ServerSocket serverSocket;
	private Socket socket;
	BufferedReader protocolIn;
	InputStream fileIn;

	private final static int STATE_LISTENING = 0;
	private final static int STATE_RECEIVING_FILES = 1;

	private int state;

	public void initConnection() {
		try {
			serverSocket = new ServerSocket(555);
			socket = serverSocket.accept();
			setListeningMode();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setListeningMode() {

		state = STATE_LISTENING;

		try {
			protocolIn = new BufferedReader(
					new InputStreamReader(
						socket.getInputStream()));

			String inputLine;
			while (state == STATE_LISTENING && (inputLine = protocolIn.readLine()) != null) {
				//System.out.println("SERVER GOT: " + inputLine);
				handleReceivedQuery(inputLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setReceivingMode() {
		state = STATE_RECEIVING_FILES;
		//protocolIn.close();
		//System.out.println("Server protocol input closed");
	}

	private void handleReceivedQuery(String inputLine) {
		if (inputLine.split(":").length == 0) {
			return;
		}
		String header = inputLine.split(":")[0];
		if (header.equals(Protocol.SENDING_FILE)) {
			//System.out.println("Received sending file request: " + inputLine);
			setReceivingMode();
			receiveFile(inputLine.split(":")[1], Integer.parseInt(inputLine.split(":")[2]));
			setListeningMode();
		}

	}

	public void sendFile() {
		try {
			//System.out.println("Accepted connection : " + socket);
			File transferFile = new File("Document.doc");
			byte[]bytearray = new byte[(int)transferFile.length()];
			FileInputStream fin = new FileInputStream(transferFile);
			BufferedInputStream bin = new BufferedInputStream(fin);
			bin.read(bytearray, 0, bytearray.length);
			OutputStream os = socket.getOutputStream();
			//System.out.println("Sending Files...");
			os.write(bytearray, 0, bytearray.length);
			os.flush();
			//System.out.println("File transfer complete");
		} catch (Exception e) {}
	}

	public void receiveFile(String name, int filesize) {
		try {
			//System.out.println("receiveFile with name " + name + " and size " + filesize);
			int bytesRead;
			int currentTot = 0;
			byte[]bytearray = new byte[filesize];
			fileIn = socket.getInputStream();
			//System.out.println("Server file input opened");

			new File("backup").mkdirs();
			FileOutputStream fos = new FileOutputStream("backup/" + name);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bytesRead = fileIn.read(bytearray, 0, bytearray.length);
			currentTot = bytesRead;

			do {
				bytesRead =
					fileIn.read(bytearray, currentTot, (bytearray.length - currentTot));
				////System.out.println(currentTot + "/" + filesize);
				if (bytesRead >= 0)
					currentTot += bytesRead;
			} while (currentTot != filesize);

			bos.write(bytearray, 0, currentTot);
			bos.flush();
			//TODO all streams/sockets should close after EOF
			//bos.close();
			//fileIn.close();
			//socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
