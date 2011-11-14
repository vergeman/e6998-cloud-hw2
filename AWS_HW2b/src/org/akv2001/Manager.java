package org.akv2001;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.akv2001.aws.AWS_ElasticIP;
import org.akv2001.aws.AWS_Instance;
import org.akv2001.aws.AWS_S3;
import org.akv2001.aws.AWS_SecurityGroup;
import org.akv2001.aws.AWS_Volume;
import org.akv2001.ssh.SSH;
import org.akv2001.users.DB;
import org.akv2001.users.User;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3Client;

public class Manager {
	static String cred_file = "./data/AwsCredentials.properties";
	static String db_file = "./data/users.ser";
	
	//arbitrary users
	static String user1 = "akv2001";
	static String user2 = "2001akv";
	
	static String security_grp="hw2_group";

	//amazon linux 32-bit 
	static String imageId = "ami-7f418316";

	//choose range of working hours
	static int wake_hour = 8; //8 --> 8am
	static int end_hour = 17; //17 --> 5pm
	static double cpu_util = 2.0;
	
	static AWSCredentials credentials;
	static AmazonEC2 ec2;
	static AmazonS3Client s3;
	static AmazonCloudWatchClient cloudWatch;
	
	public static void main (String args[]) {
	
		// setup clients
		try {
			credentials = new PropertiesCredentials(new File(cred_file));
			ec2 = new AmazonEC2Client(credentials);
			s3 = new AmazonS3Client(credentials);
			cloudWatch = new AmazonCloudWatchClient(credentials);
			
		} catch (IOException e) {
			System.err.println("Couldn't open Credentials File");
			e.printStackTrace();
			System.exit(-1);
		}

		DB Users = new DB(db_file);
		
		/*set security group*/
		AWS_SecurityGroup sg = new AWS_SecurityGroup(security_grp, "ssh");
		sg.add_GroupIngressRequest("tcp", "0.0.0.0/0", 22, 22);
		sg.setSecurityGroup(ec2);
		
		
		//No Users? let's make some.
		if (!Users.Load(ec2, s3))  {
			
			/*add new users*/
			Users.setUser(user1, new User(ec2, user1, security_grp));
			Users.setUser(user2, new User(ec2, user2, security_grp));
			
			//bootstrap process
			for (String username: Users.getUsers()) {
				User u = Users.getUser(username);
			
				/*create instance*/
				AWS_Instance.New(ec2, imageId, u);
				AWS_Instance.waitforRunningInstances(ec2, u);
				
				/*allocating persistent storage*/
				/*creating volumes*/
				AWS_Volume.createVolume(ec2, u);
				AWS_Volume.attachVolume(ec2, u.getInstance_id(), u.getVolume_id());
				 
				/*Write to S3*/
	            String bucketName =  u.getUsername() + "-bucket";
	            u.setS3_bucket(bucketName);
	            AWS_S3.write(s3, bucketName, username + "file.txt",
	            		"This is a sample sentence for user: " + username + ".\r\n" + (new Date()).toString() );
				
	            /*elastic IP*/
				AWS_ElasticIP.allocate(ec2, u);
				AWS_ElasticIP.associate(ec2, u.getInstance_id(), u.get_ip());
				
	            /*copy file to run*/
	            SSH ssh = new SSH(u.get_ip(), u.getUsername());
	            ssh.scp("test.pl");
	            
			}
		
			//save db
			Users.Save();
		}
		

		//check status of instances: sleep or not sleeping
		for (String username : Users.getUsers()) {
			User u = Users.getUser(username);
			AWS_Instance.loadInstanceStatus(ec2, u);
			
			//run temp task
			if (!u.isSleeping()) {
				System.out.println("SSH and running process on machine");
				SSH ssh = new SSH(u.get_ip(), u.getUsername());
				ssh.exec("perl ./test.pl");
			}
		}


		//monitor loop
		while (true) {
		for (String username : Users.getUsers()) {
			User u = Users.getUser(username);


			// Terminate case: cpu idle or end of day (5pm)
			if (!u.isSleeping() && (Monitor.isIdle(u, cpu_util) || Monitor.isEOD(wake_hour, end_hour))) {
				
				System.out.println("Going to sleep: " + u.getUsername() + " " + u.getInstance_id());
				u.setSleeping(true);
				u.setWakeDate(wake_hour, end_hour);
				
				AWS_Volume.DetachVolume(ec2, u.getInstance_id(),
						u.getVolume_id());

				AWS_Instance.Snapshot(ec2, u, u.getInstance_id());
				AWS_Instance.waitforAvailableSnapshot(ec2, u, u.getImageId());
				
				AWS_Instance.Terminate(ec2, u.getInstance_id());


			}
			
			// Wake case: the next wake hour
			if (u.isSleeping() && Calendar.getInstance().after(u.getWakeDate())) {
				
				System.out.println("Waking: " + u.getUsername() + " " + u.getInstance_id());
				u.setSleeping(false);

				AWS_Instance.New(ec2, u.getImageId(), u);
				AWS_Instance.waitforRunningInstances(ec2, u);

				AWS_Volume.attachVolume(ec2, u.getInstance_id(),
						u.getVolume_id());

				AWS_ElasticIP.associate(ec2, u.getInstance_id(), u.get_ip());
				
				//run process on wake
				System.out.println("SSH and running process on machine");
				SSH ssh = new SSH(u.get_ip(), u.getUsername());
				ssh.exec("perl ./test.pl");
			}

				/*get monitor stats*/
				Monitor.getStats(cloudWatch, u,
						Arrays.asList("Average", "Sum"), "CPUUtilization");
				try {
					Thread.sleep(3000);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			Users.Save();

		}

	}

}
