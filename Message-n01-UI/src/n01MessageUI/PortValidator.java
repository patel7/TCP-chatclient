package n01MessageUI;

import org.eclipse.jface.dialogs.IInputValidator;

public class PortValidator implements IInputValidator {

	@Override
	public String isValid(String input) {
		try {
			int port = Integer.parseInt(input);
			if(port < 1024 || port > 65535) {
				return "Invalid port. Valid ports: 1024 - 65535.";
			} else {
				return null;
			}
		} catch(Exception e) {
			return "";
		}
	}
}
