package chatserver;

import chat.Chatter;

// user info
public class User {
	// user name
	private String name;
	// remote object for client
	private Chatter chatter;
	
	public User(String name, Chatter chatter) {
		setName(name);
		setChatter(chatter);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Chatter getChatter() {
		return chatter;
	}

	public void setChatter(Chatter chatter) {
		this.chatter = chatter;
	}

}
