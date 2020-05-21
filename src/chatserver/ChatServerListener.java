package chatserver;

import java.util.EventListener;

// Server listener
public interface ChatServerListener extends EventListener {
	public void serverEvent(ChatServerEvent evt);//listen if there exists any change
}
