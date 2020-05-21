package chatserver;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chat.ChatServer;
import chat.Chatter;

// Server interface implementation
public class ChatServerImpl extends java.rmi.server.UnicastRemoteObject implements ChatServer {
	static ChatServerImpl server = null;
	private final static String BINDNAME = "ChatServer";
	private final static String[] STATEMSG = new String[] { "Server Start", "Server Stop" };
	List chatters = new ArrayList();
	List listeners = new ArrayList();
	
	protected ChatServerImpl() throws java.rmi.RemoteException {
		
	}
	
	public static ChatServerImpl getInstance() {
		try {
			if (server == null) {
				server = new ChatServerImpl();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
		return server;
	}
	
	public void start() throws RemoteException, MalformedURLException {
		java.rmi.Naming.rebind(BINDNAME, server);
		notifyListener(STATEMSG[0]);
	}
	
	public void stop() throws RemoteException, NotBoundException, MalformedURLException {
		notifyListener(STATEMSG[1]);
		Iterator itr = chatters.iterator();
		while (itr.hasNext()) {
			User u = (User) itr.next();
			u.getChatter().serverStop();
		}
		java.rmi.Naming.unbind(BINDNAME);
	}
	
	public void addListener(ChatServerListener listener) {
		listeners.add(listener);
	}
	
	public void removeListense(ChatServerListener listener) {
		listeners.remove(listener);
	}
	
	void notifyListener(String msg) {
		Iterator itr = listeners.iterator();
		ChatServerEvent evt = new ChatServerEvent(this, msg);
		while (itr.hasNext()) {
			((ChatServerListener) itr.next()).serverEvent(evt);
		}
	}
	
	public boolean login(String name, Chatter c) throws java.rmi.RemoteException {
		if (c != null && name != null) {
			Iterator i = chatters.iterator();
			try {
				while (i.hasNext()) {
					User u  = (User)i.next();
					if(u.getName().equals(name)) {
						notifyListener("Duplicate user name: " + name);
						return false;
					}
				}
			}catch(Exception e) {
				System.out.print(e);
			}
			User u = new User(name, c);
			notifyListener(u.getName() + " Enter the ChatRoom");
			Iterator itr = chatters.iterator();
			while (itr.hasNext()) {
				User u2 = (User) itr.next();
				u2.getChatter().receiveEnter(name, c, false);
				c.receiveEnter(u2.getName(), u2.getChatter(), true);
			}
			chatters.add(u);
			return true;
		}
		return false;
	}
	
	public void logout(String name) throws java.rmi.RemoteException {
		if (name == null) {
			System.out.println("null name on logout: cannot remove chatter");
			return;
		}
		User u_gone = null;
		Iterator itr = null;
		
		synchronized (chatters) {
			for (int i = 0; i < chatters.size(); i++) {
				User u = (User) chatters.get(i);
				if (u.getName().equals(name)) {
					notifyListener(name + " leave the ChatRoom");
					u_gone = u;
					chatters.remove(i);
					itr = chatters.iterator();
					break;
				}
			}
		}
		
		if (u_gone == null || itr == null) {
			System.out.println("No user called " + name + " found: cannot removing chatter");
			return;
		}
		
		while (itr.hasNext()) {
			User u = (User) itr.next();
			u.getChatter().receiveExit(name);
		}
	}
	
	public void chat(String name, String message) throws java.rmi.RemoteException {
		Iterator itr = chatters.iterator();
		while (itr.hasNext()) {
			User u = (User) itr.next();
			if (!name.equals(u.getName()))
				u.getChatter().receivePublicChat(name, message);
		}
		notifyListener(name + ":" + message);
	}

	@Override
	public void fileToAll(String my_name, byte[] fileBytes, String fileName) throws java.rmi.RemoteException {
		// TODO Auto-generated method stub
		Iterator itr = chatters.iterator();
		while (itr.hasNext()) {
			User u  = (User)itr.next();
			if(!u.getName().equals(my_name)) {
				u.getChatter().getFile(u.getName(),fileBytes,fileName);
			}
		}
		
	}

	@Override
	public void fileToOne(String my_name, String destChatter, byte[] fileBytes, String fileName) throws java.rmi.RemoteException {
		// TODO Auto-generated method stub
		Iterator itr = chatters.iterator();
		try {
		while (itr.hasNext()) {
			User u  = (User)itr.next();
			if(u.getName().equals(destChatter)) {
				u.getChatter().getFile(u.getName(),fileBytes,fileName);
			}
		}
		}catch(Exception e) {
			System.out.print(e);
		}
	}
}
