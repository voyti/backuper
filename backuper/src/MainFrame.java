import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;


public class MainFrame extends JFrame implements IListener {

	private JPanel mainPane;
	private JButton doBackup, getBackup, setDir, setServer, setFrequency, exit;
	private JProgressBar progressBar;
	private ConnectionManager cm;
	private File rootDirectory;
	private Integer totalfilesToSend = 0;
	private Integer totalTransferSize = 0;
	private Integer transferSent = 0;
	private Integer filesSent = 0;
	BackupInterval backupInterval;
	private boolean silentMode;

	public MainFrame() {
		super("Backuper");
		
		rootDirectory = null;// TODO read from file 
		init();
	}

	private void init() {
		
		try {
			cm = new ConnectionManager();
			cm.subscribe(this);
			cm.initConnection();
		}
		catch (UnknownHostException e) {
			handleUnknownHostException(e);
			//e.printStackTrace();
		}
		catch (IOException e) {
			handleServerIOException(e);
			//e.printStackTrace();
		}
		
		backupInterval = new BackupInterval(1); // backup every minute
		backupInterval.subscribe(this);
		(new Thread(backupInterval)).start();
		
		this.setLayout(new BorderLayout());
		mainPane = new JPanel(new GridLayout(6, 1, 10, 10));
		doBackup = new JButton("Backup files now");
		getBackup = new JButton("Get backup now");
		setDir = new JButton("Set backup directory");
		setServer = new JButton("Set backup server");
		setFrequency = new JButton("Set backup frequency");
		exit = new JButton("Exit");
		
		doBackup.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doBackup(false);
			}
		});
		
		getBackup.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getBackup();
			}
		});
		
		setDir.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setBackupSource();
			}
		});
		
		setServer.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setBackupTarget();
			}
		});
		
		setFrequency.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setFrequency();
			}
		});
		
		exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		
		progressBar = new JProgressBar(0, 100);
		
		mainPane.add(doBackup, 0);
		mainPane.add(setDir, 1);
		mainPane.add(setServer, 2);
		mainPane.add(setFrequency, 3);
		mainPane.add(exit, 4);
		
		this.add(mainPane, BorderLayout.CENTER);
		this.add(progressBar, BorderLayout.PAGE_END);
	}


	protected void doBackup(boolean silent) {
		silentMode = silent;
		try {
			if (this.rootDirectory != null && this.rootDirectory.isDirectory()) {
				cm.sendFilesFromDir(this.rootDirectory);
			} else if (silent){
				JOptionPane
						.showMessageDialog(
								this,
								"The backup root directory is not properly set.\nPlease use set the backup directory first.",
								"Directory not properly set", JOptionPane.WARNING_MESSAGE);
	
				setBackupSource();
				doBackup(false);
			}
		} catch (IOException e) {
			handleFileTransferIOException(e);
			e.printStackTrace();
		}
	}

	protected void getBackup() {
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == 0) { //directory selected
			cm.getFilesFromServer(fc.getSelectedFile());
		}
	}
	
	protected void setBackupSource() {
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == 0) { //directory selected
			this.rootDirectory = fc.getSelectedFile();
		}
	}
	
	protected void setBackupTarget() {
		String rawInput = JOptionPane.showInputDialog(this, "Please enter backup server ip address and port (ex. 192.168.137.12:8080):", "Enter server address", JOptionPane.QUESTION_MESSAGE);
		if (rawInput == null) {
			System.exit(0);
		}
		System.out.println(rawInput);
		try{
			testRawIpAndPortInput(rawInput);
			cm.setIpAddress(rawInput.split(":")[0]);
			cm.setPort(Integer.parseInt(rawInput.split(":")[1]));
			cm.initConnection();
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this,
				    "Incorrect data entered.\n" +
				    "Please check that the input is in format \"0-255.0-255.0-255.0-225:0-65535\",\n" +
				    " like: 192.168.137.12:8080", "Incorrect data format", JOptionPane.WARNING_MESSAGE);
			setBackupTarget();
			e.printStackTrace();
			return;

		} catch (UnknownHostException e) {
			handleUnknownHostException(e);
			e.printStackTrace();
		} catch (IOException e) {
			handleServerIOException(e);
			e.printStackTrace();
		}
		
	}
	
	private void testRawIpAndPortInput(String rawInput) throws NumberFormatException{
		String[] result = rawInput.split(":");
		String[] ipResult = result[0].split("\\.");
		String portResult = result[1];
		Integer.parseInt(portResult);
		for (int i = 0; i < ipResult.length; i++) {
			Integer.parseInt(ipResult[i]);
		}
	}

	protected void setFrequency() {
		String rawInput = JOptionPane.showInputDialog(this, "Please enter backup interval in hours.\n"
				+ "", "Enter backup interval", JOptionPane.QUESTION_MESSAGE);
		try {
			backupInterval.setInterval(Double.parseDouble(rawInput));
			
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this,
				    "Wrong input, please input number.", "Wrong input format", JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
	}
	
	@Override
	public void notify(String eventName, Object value) {
		if (eventName.equals("totalFilesToSend")) {
			totalfilesToSend = (Integer) value;
			System.out.println("Total files are:" + value);
			
		} else if (eventName.equals("filesSent")) {
			filesSent = (Integer) value;
			progressBar.setStringPainted(true);
			System.out.println("Setting value:" + (int) (((double) filesSent / (double) totalfilesToSend) * 100));
			progressBar.setValue((int) (((double) filesSent / (double) totalfilesToSend) * 100));
			progressBar.setString("Sent " + filesSent + " files out of " + totalfilesToSend);
			System.out.println("Files sent:" + value);
			
		} else if (eventName.equals("allFilesSent")) {
			if (!silentMode) {
				JOptionPane.showMessageDialog(this, "All files sent to the server.");
			} else {
				System.out.println("All files sent to the server.");
			}

		} else if (eventName.equals("doBackup")) {
			System.out.println("Event to do backup");
			doBackup((Boolean) value);
		} else if (eventName.equals("wake")) {
			//(new Thread(backupInterval)).notify();
		}
	}
	
	private void handleUnknownHostException(UnknownHostException e) {
		JOptionPane.showMessageDialog(this,
			    "Connection to the server failed - the host is unknown.\n" +
			    		"Please set the backup server address.", "Server connection error", JOptionPane.WARNING_MESSAGE);
		this.setBackupTarget();
	}
	
	private void handleServerIOException(IOException e) {
		JOptionPane.showMessageDialog(this,
			    "Please set the backup server address.", "Server connection error", JOptionPane.WARNING_MESSAGE);
		this.setBackupTarget();	
	}
	
	private void handleFileTransferIOException(IOException e) {
		JOptionPane.showMessageDialog(this,
			    "File trasfer failed due to input/output exception.\n" +
			    "Please check your Internet connection and try again.", "File transfer error", JOptionPane.WARNING_MESSAGE);
	}
}
