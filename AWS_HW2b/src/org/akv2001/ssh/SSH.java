package org.akv2001.ssh;
import org.akv2001.ssh.MyUserInfo;
import com.jcraft.jsch.*;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.*;

public class SSH {
	
	String host;
	String user;
	JSch jsch;
	Session session;
	UserInfo ui;
	
	public SSH (String host, String user) {
		this.host = host;
		this.user = user;
		this.jsch = new JSch();
		this.ui = new MyUserInfo();
		
		try {
			jsch.addIdentity("./data/" + user + ".pem");
		} catch (JSchException e) {

			e.printStackTrace();
		}
	}

	public boolean connect() throws JSchException {
		System.out.println("Trying to connect via ssh [takes some time]");
		this.session = jsch.getSession("ec2-user", host, 22);
		session.setUserInfo(ui);
		
		/*ping for connection*/
		while (!session.isConnected()) {
			try {
				session.connect();
			} catch (Exception e) {
				session.disconnect();
			}
			
			System.out.println("Waiting for ssh to become active. . .");
			
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

		}
		
		System.out.println("Connected");
		return true;

	}
	
	
	public void exec(String command) {

		try {
			connect();
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);
			InputStream in = channel.getInputStream();

			channel.connect();
			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					System.out.println("exit-status: "
							+ channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			channel.disconnect();
			session.disconnect();
		}

		catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void scp(String filename) {
		FileInputStream fis = null;
		String lfile = "./data/" + filename;
		String command = "scp -p -t " +  filename;
		
		try {
			connect();
			
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			System.out.println("transferring file");
			channel.connect();

			if (checkAck(in) != 0) {
				//System.exit(0);
			}

			// send "C0644 filesize filename", where filename should not include
			// '/'
			long filesize = (new File(lfile)).length();
			command = "C0644 " + filesize + " ";
			if (lfile.lastIndexOf('/') > 0) {
				command += lfile.substring(lfile.lastIndexOf('/') + 1);
			} else {
				command += lfile;
			}
			command += "\n";
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) {
				//System.exit(0);
			}

			// send a content of lfile
			fis = new FileInputStream(lfile);
			byte[] buf = new byte[1024];
			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len); // out.flush();
			}
			fis.close();
			fis = null;
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			if (checkAck(in) != 0) {
				//System.exit(0);
			}
			out.close();

			channel.disconnect();
			session.disconnect();

			//System.exit(0);
		} catch (Exception e) {
			System.out.println(e);
			try {
				if (fis != null)
					fis.close();
			} catch (Exception ee) {
			}
		}
	}
	
	

	
	int checkAck(InputStream in) throws IOException{
	    int b=in.read();
	    // b may be 0 for success,
	    //          1 for error,
	    //          2 for fatal error,
	    //          -1
	    if(b==0) return b;
	    if(b==-1) return b;

	    if(b==1 || b==2){
	      StringBuffer sb=new StringBuffer();
	      int c;
	      do {
		c=in.read();
		sb.append((char)c);
	      }
	      while(c!='\n');
	      if(b==1){ // error
		System.out.print(sb.toString());
	      }
	      if(b==2){ // fatal error
		System.out.print(sb.toString());
	      }
	    }
	    return b;
	  }
}
