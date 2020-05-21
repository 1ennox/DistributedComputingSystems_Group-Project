package chatclient;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.IllegalFormatCodePointException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

// Connect
public class ConnectDialog extends JDialog implements PropertyChangeListener {
	JTextField userNameField;
	JOptionPane optionPane;
	String  userName;
	int value = -1;
	
	public ConnectDialog(Frame frame) {
		super(frame, "Connect", true);
		userNameField = new JTextField(20);
		Object[] array = { "UserName:", userNameField };
		optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		setContentPane(optionPane);
		
		optionPane.addPropertyChangeListener(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				setVisible(false);
			}
		});
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();
		if (isVisible() && (e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
				JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			if (optionPane.getValue() == JOptionPane.UNINITIALIZED_VALUE) {
				return;
			}
			value = ((Integer) optionPane.getValue()).intValue();
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			if (value == JOptionPane.OK_OPTION) {
				if ( "".equals(userNameField.getText())) {
					JOptionPane.showMessageDialog(this, "Enter the user name");
					if ("".equals(userNameField.getText()))
						userNameField.requestFocus();
				} else {
					setUserName(userNameField.getText());
					setVisible(false);
				}
			} else {
				setVisible(false);
			}
		}
	}


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getValue() {
		return value;
	}
	
}
