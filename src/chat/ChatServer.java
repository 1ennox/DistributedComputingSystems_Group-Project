package chat;

import java.rmi.RemoteException;

// ����������ӿ�
public interface ChatServer extends java.rmi.Remote {
	/**
	 * ע���µ������û�
     * @return
     */
	public boolean login(String name, Chatter chatter) throws RemoteException;
	
	/**
	 * �û��˳�
	 */
	public void logout(String name) throws RemoteException;
	
	/**
	 * �û����ô˺�������Ϣ���������û�
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
