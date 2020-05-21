package chatclient;

import java.rmi.RemoteException;
import chat.*;

// Client interface implementation
public class ChatterImpl extends java.rmi.server.UnicastRemoteObject implements Chatter {
	ChatClientStart client;
	
	public ChatterImpl(ChatClientStart client) throws java.rmi.RemoteException {
		this.client = client;
	}
	
	public void receiveEnter(String name, Chatter chatter, boolean hasEntered) throws java.rmi.RemoteException {
		client.receiveEnter(name, chatter, hasEntered);
	}
	
	public void receiveExit(String name) throws java.rmi.RemoteException {
		client.receiveExit(name);
	}
	
	public void receivePublicChat(String name, String message) throws java.rmi.RemoteException {
		client.receivePublicChat(name, message);
	}
	
	public void receivePrivateChat(String name, String message) throws java.rmi.RemoteException {
		client.receivePrivateChat(name, message);
	}
	
	public void serverStop() throws RemoteException {
		client.serverStop();
	}

	@Override
	public void getFile(String name, byte[] fileBytes, String fileName) throws java.rmi.RemoteException{
		// TODO Auto-generated method stub
		client.getFile(name,fileBytes,fileName);
	}
}
