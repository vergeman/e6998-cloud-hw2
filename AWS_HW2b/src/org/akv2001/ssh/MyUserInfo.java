package org.akv2001.ssh;
/*
 * this is helper class for a sample shell modified from jsch
 * http://www.jcraft.com/jsch/examples/UserAuthPubKey.java
 * 
 */
import com.jcraft.jsch.*;

public class MyUserInfo implements UserInfo, UIKeyboardInteractive {
	public String getPassword() {
		return null;
	}

	public boolean promptYesNo(String str) {
		return true;
	}

	String passphrase;

	public String getPassphrase() {
		return passphrase;
	}

	public boolean promptPassphrase(String message) {
		return true;
	}

	public boolean promptPassword(String message) {
		return true;
	}

	public String[] promptKeyboardInteractive(String arg0, String arg1,
			String arg2, String[] arg3, boolean[] arg4) {
		return null;
	}

	@Override
	public void showMessage(String arg0) {

	}

}
