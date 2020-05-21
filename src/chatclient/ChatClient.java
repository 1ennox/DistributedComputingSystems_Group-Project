package chatclient;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Objects;
import java.io.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import chat.ChatServer;
import chat.Chatter;
import chatserver.UserInfo;

public class ChatClient extends JFrame{
	// store the user£¬key£ºusername£¬value£ºcorresponding Chatter
	Hashtable hash = new Hashtable();
	// chatter name
	String my_name = "chatter";
	// server name
	String serverAddr = "127.0.0.1:1099";
	// Client side to remote object
	Chatter chatter;
	// Server side to remote object
	ChatServer server;
	JTextArea displayBox;
	JTextArea inputBox;
	JComboBox usersBox;
	JButton sendButton;
	JButton fileButton;
	JLabel statusLabel;
	ConnectionAction connectAction = new ConnectionAction();
	// Let user input username
	ConnectDlg dlg = new ConnectDlg(this);
	
	public static void main(String[] args) {
		new ChatClient();
	}
	
	public ChatClient() {
		super("Multi-user ChatRoom");
		layoutComponent();
		setupMenu();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					exit();
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		show();
	}

	private void setupMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenuItem conn = new JMenuItem(connectAction);
		JMenuItem exit = new JMenuItem("Leave");
		exit.addActionListener(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					exit();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		JMenu options = new JMenu("Option");
		options.add(conn);
		options.add(exit);
		menuBar.add(options);
		setJMenuBar(menuBar);
	}
	
	private void exit() throws RemoteException {
		hash.remove(my_name);
		this.dispose();
		destroy();
		System.exit(0);
	}
	
	//Layout
	public void layoutComponent() {
		setSize(400, 400);
		JPanel contentPane = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contentPane.setLayout(gridbag);
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 6;
		c.weightx = 100;
		c.weighty = 100;
		c.insets.top = 5;
		displayBox = new JTextArea();
		displayBox.setLineWrap(true);
		displayBox.setEditable(false);
		displayBox.setMargin(new Insets(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(displayBox);
		contentPane.add(scrollPane, c);
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.insets.top = 10;
		
		//Message Box
		JLabel msgLabel = new JLabel("Message:");
		contentPane.add(msgLabel, c);
		c.gridheight = 6;
		c.insets.top = 0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 100;
		inputBox = new JTextArea();
		addKeymapBindings();
		inputBox.setLineWrap(true);
		inputBox.setWrapStyleWord(true);
		JScrollPane inputScrollPane = new JScrollPane(inputBox);
		inputScrollPane.setPreferredSize(new Dimension(250, 50));
		inputScrollPane.setMinimumSize(new Dimension(250, 50));
		contentPane.add(inputScrollPane, c);
		
		//send Message Button
		c.weightx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		sendButton = new JButton(new ImageIcon(getClass().getResource("../images/flashLight.png")));
		sendButton.setToolTipText("Send Message");
		sendButton.setPreferredSize(new Dimension(50, 40));
		sendButton.setMinimumSize(new Dimension(50, 40));
		sendButton.addActionListener(new ActionListener() {
			//Action to send a message 
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String message = inputBox.getText();
				sendMessage(message);
			}
		});
		contentPane.add(sendButton, c);
		
		//send File Button
		fileButton = new JButton("Send File");
		fileButton.setToolTipText("Send Message");
		fileButton.setPreferredSize(new Dimension(50, 40));
		fileButton.setMinimumSize(new Dimension(50, 40));
		fileButton.addActionListener(new ActionListener() {
			//Action to send a message 
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jf = new JFileChooser();
                jf.showOpenDialog(null);
                File file = jf.getSelectedFile();
                System.out.println("Send File: " + file.getName());
                try {
					sendFile(fileTobyte(file.getAbsolutePath()), file.getName());
					sendMessage(my_name+ " Send a file to "+ usersBox.getSelectedItem());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		contentPane.add(fileButton, c);
		
		
		c.weightx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridheight = 1;
		JLabel sendtoLabel = new JLabel("Send to:");
		contentPane.add(sendtoLabel, c);
		usersBox = new JComboBox();
		usersBox.setBackground(Color.WHITE);
		usersBox.addItem("All Users");
		contentPane.add(usersBox, c);
		JPanel statusPane = new JPanel(new GridLayout(1, 1));
		statusLabel = new JLabel("Not Connected");
		statusPane.add(statusLabel);
		contentPane.add(statusPane, c);
		setContentPane(contentPane);
		try {
			chatter = new ChatterImpl(this);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void destroy() {
		try {
			disconnect();
		} catch (java.rmi.RemoteException ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean connect() throws java.rmi.RemoteException, java.net.MalformedURLException, java.rmi.NotBoundException{
		boolean ifConnect;

		server = (ChatServer) java.rmi.Naming.lookup("//" + serverAddr + "/ChatServer");
		ifConnect = server.login(my_name, chatter);
		if(ifConnect == false){
			return false;
		}
		return true;
	}
	
	protected void disconnect() throws java.rmi.RemoteException {
		if (server != null)
			server.logout(my_name);
	}

	// Send a 'Enter' message on screen for newcomer
	public void receiveEnter(String enterUserName, Chatter chatter, boolean hasEntered) {
		if (enterUserName != null && chatter != null) {
			hash.put(enterUserName, chatter);
			if (!enterUserName.equals(my_name)) {
				if (!hasEntered)
					display(enterUserName + " Enter the ChatRoom");
				usersBox.addItem(enterUserName);
			}
		}
	}
	// Send a 'Leave' message on screen
	public void receiveExit(String exitUserName) {
		if (exitUserName != null && chatter != null)
			hash.remove(exitUserName);
		for (int i = 0; i < usersBox.getItemCount(); i++) {
			if (exitUserName.equals((String) usersBox.getItemAt(i))) {
				usersBox.removeItem(usersBox.getItemAt(i));
				break;
			}
		}
		display(exitUserName + " Leave the ChatRoom");
	}
	
	public void receivePublicChat(String name, String message) {
		display("(Public) " + name + " : " + message);
	}
	
	public void receivePrivateChat(String name, String message) {
		display("(Private) " + name + " : " + message);
	}
	
	// bind keyboard
	protected void addKeymapBindings() {
		Keymap keymap = JTextComponent.addKeymap("MyBindings", inputBox.getKeymap());
		Action action = null;
		KeyStroke key = null;
		//press "Enter" to send message
		action = new AbstractAction() {
			// Press 'Enter' to send a message
			@Override 
			public void actionPerformed(ActionEvent arg0) {
				String message = inputBox.getText();
				sendMessage(message);
			}
		};
		key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		keymap.addActionForKeyStroke(key, action);
		
		// 'Ctrl+Enter' to make a linebreak
		action = new AbstractAction() {
			//press 'Ctrl + Enter' make a line break
			@Override
			public void actionPerformed(ActionEvent arg0) {
				inputBox.append("\n");
			}
		};
		key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.CTRL_MASK);
		keymap.addActionForKeyStroke(key, action);
		
		inputBox.setKeymap(keymap);
	}
	// display the message on the screen
	private void display(String s) {
		if (!s.endsWith("\n")) {
			displayBox.append(s + "\n");
		} else {
			displayBox.append(s);
		}
		
		int length = displayBox.getText().length() - 1;
		displayBox.select(length, length);
	}
	//Send Message Method
	private void sendMessage(String message) {
		if (message != null && message.length() > 0) {
			inputBox.setText(null);
			inputBox.setCaretPosition(0);
			display(my_name + ":" + message);
			if (server != null) {
				if ("All Users".equals(usersBox.getSelectedItem())) {// send to all user
					try {
						server.chat(my_name, message);
					} catch (java.rmi.RemoteException ex) {
						ex.printStackTrace();
					}
				} else {// private chat
					String destUserName = (String) usersBox.getSelectedItem();
					Chatter destChatter = (Chatter) hash.get(destUserName);
					try {
						destChatter.receivePrivateChat(my_name, message);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
		inputBox.requestFocus();
	}
	
	//Convert a file into bytes
	private byte[] fileTobyte(String filePath) {
        try {
            System.out.println(filePath + "\n");
            File file = new File(filePath);
            if (file.length() > Integer.MAX_VALUE) {
                JOptionPane.showMessageDialog(null, "The file is too large.");
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            byte[] temp = new byte[1024];
            int size;
            while ((size = fis.read(temp)) != -1) {
                baos.write(temp, 0, size);
            }
            fis.close();
            byte[] fileBytes;
            fileBytes = baos.toByteArray();
            return fileBytes;
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "File Not Found:\n" + e.getMessage());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "IOException:\n" + e.getMessage());
        }
        return null;
    }
	//send File Method
	private void sendFile(byte[] fileBytes, String fileName) throws RemoteException {
        if ("All Users".equals(usersBox.getSelectedItem())) {
            server.fileToAll(my_name, fileBytes, fileName);
        } else {
        	String destChatter = (String)usersBox.getSelectedItem();
            server.fileToOne(my_name, destChatter, fileBytes, fileName);
        }
    }
	
	//Server Stop and clear all the user
	public void serverStop() {
		display("Server Stop working.");
		server = null;
		hash.clear();
		connectAction.setEnabled(true);
	}
	
	class ConnectionAction extends AbstractAction {
		public ConnectionAction() {
			super("Connect");
			putValue(Action.SHORT_DESCRIPTION, "Connected to server");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
		}
		
		public void actionPerformed(ActionEvent evt) {
			boolean ifSuccess;

			dlg.pack();
			dlg.setLocationRelativeTo(ChatClient.this);
			dlg.setVisible(true);

			if (dlg.getValue() == JOptionPane.OK_OPTION) {
				try {
					my_name = dlg.getUserName();
					ifSuccess = connect();
					if(ifSuccess){
						inputBox.setEditable(true);
						displayBox.setText("You can post your message now.\n");
						statusLabel.setText("(Connected) Your user ID is: " + my_name);
						this.setEnabled(false);
					}else{
						statusLabel.setText("Invalid user name.");
						displayBox.setText("Duplicate user name, please try again.\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
					statusLabel.setText("Cannot connect to server");
					return;
				}
			}
		}
	}

	public void getFile(String name, byte[] fileBytes, String fileName) {
		try {
            Desktop.getDesktop().open(new File(Objects.requireNonNull(byteTofile(fileBytes, fileName,name))));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	 private String byteTofile(byte[] fileBytes, String fileName,String name) {
	        try {
	            File directory = new File(name);
	            if (!directory.exists()) {
	            	directory.mkdirs();
	            }
	            String filePath = directory.getCanonicalPath() + "\\" + fileName;
	            FileOutputStream fos = new FileOutputStream(filePath);
	            fos.write(fileBytes);
	            fos.close();
	            return filePath;
	        } catch (FileNotFoundException e) {
	            JOptionPane.showMessageDialog(null, "File Not Found:\n" + e.getMessage());
	        } catch (IOException e) {
	            JOptionPane.showMessageDialog(null, "IOException:\n" + e.getMessage());
	        }
	        return null;
	    }
}

