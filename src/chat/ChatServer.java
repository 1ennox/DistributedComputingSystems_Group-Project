package chat;

import java.rmi.RemoteException;

// 聊天服务器接口
public interface ChatServer extends java.rmi.Remote {
	/**
	 * register new chatter
     * @return
     */
	public boolean login(String name, Chatter chatter) throws RemoteException;
	
	/**
	 * user logout
	 */
	public void logout(String name) throws RemoteException;
	
	/**
	 * user broadcast messages
	 */
	public void chat(String name, String message) throws RemoteException;
	/**
	 * 
	 * Send file to all users
	 */
	public void fileToAll(String my_name, byte[] fileBytes, String fileName) throws RemoteException;
	/**
	 * 
	 *Send file to a user
	 */
	public void fileToOne(String my_name, String destChatter, byte[] fileBytes, String fileName) throws RemoteException;
}
