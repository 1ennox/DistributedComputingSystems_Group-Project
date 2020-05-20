package chat;

import java.rmi.RemoteException;

// 聊天服务器接口
public interface ChatServer extends java.rmi.Remote {
	/**
	 * 注册新的聊天用户
     * @return
     */
	public boolean login(String name, Chatter chatter) throws RemoteException;
	
	/**
	 * 用户退出
	 */
	public void logout(String name) throws RemoteException;
	
	/**
	 * 用户调用此函数将消息发给所有用户
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
