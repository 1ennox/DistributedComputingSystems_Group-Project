package chat;

import java.rmi.RemoteException;

public interface Chatter extends java.rmi.Remote {
	/**
	 * inform users' enter behave
	 */
	public void receiveEnter(String name, Chatter chatter, boolean hasEntered) throws RemoteException;
	
	/**
	 * inform users' leaving behave
	 */
	public void receiveExit(String name) throws RemoteException;
	
	/**
	 * user receive messages from server
	 */
	public void receivePublicChat(String name, String message) throws RemoteException;
	
	/**
	 * private chat
	 */
	public void receivePrivateChat(String name, String message) throws RemoteException;
	
	/**
	 * stop the server
	 */
	public void serverStop() throws RemoteException;
	/**
	 * Client get File
	 */
	public void getFile(String name, byte[] fileBytes, String fileName)throws RemoteException;
}
