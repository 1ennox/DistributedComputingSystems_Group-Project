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
			if (server == null) {//only one server can be started
				server = new ChatServerImpl();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
		return server;
	}
	
	public void start() throws RemoteException, MalformedURLException {
		java.rmi.Naming.rebind(BINDNAME, server);//start running the server
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
					if(u.getName().equals(name)) {//if the user input the same name
						notifyListener("Duplicate user name: " + name);//server will not allow him to login
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
			chatters.add(u);//update server's client list
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
					chatters.remove(i);//remove this user from server's client list
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
			u.getChatter().receiveExit(name);//remote invoke function
		}
	}
	
	public void chat(String name, String message) throws java.rmi.RemoteException {
		Iterator itr = chatters.iterator();
		while (itr.hasNext()) {//traverse all the clients exist in the server
			User u = (User) itr.next();
			if (!name.equals(u.getName()))//send message to all users except the sender
				u.getChatter().receivePublicChat(name, message);//remote invoke function
		}
		notifyListener(name + ":" + message);
	}

	@Override
	public void fileToAll(String my_name, byte[] fileBytes, String fileName) throws java.rmi.RemoteException {
		// TODO Auto-generated method stub
		Iterator itr = chatters.iterator();
		while (itr.hasNext()) {//traverse all the clients exist in the server
			User u  = (User)itr.next();
			if(!u.getName().equals(my_name)) {
				u.getChatter().getFile(u.getName(),fileBytes,fileName);//remote invoke function, let clients get file
			}
		}
	}

	@Override
	public void fileToOne(String my_name, String destChatter, byte[] fileBytes, String fileName) throws java.rmi.RemoteException {
		// TODO Auto-generated method stub
		Iterator itr = chatters.iterator();
		while (itr.hasNext()) {//traverse all the clients exist in the server
			User u = (User) itr.next();
			if (u.getName().equals(destChatter)) {
				u.getChatter().getFile(u.getName(), fileBytes, fileName);//remote invoke function, let a specific client get file
			}
		}
	}
}
