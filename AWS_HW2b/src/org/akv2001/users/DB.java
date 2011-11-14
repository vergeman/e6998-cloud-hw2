package org.akv2001.users;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3Client;


public class DB {
	private String db_file;
	private HashMap<String, User> UserMap;
	
	
	public DB(String db_file) {
		this.db_file = db_file;
		this.UserMap = new HashMap<String, User>();
	}
	
	public ArrayList<String> getUsers() {
		ArrayList<String> users = new ArrayList<String>();
		users.addAll(UserMap.keySet());
		return users;
	}
	
	public User getUser(String username) {
		return UserMap.get(username);
	}
	
	public void setUser(String username, User u) {
		UserMap.put(username, u);
	}
	
	public HashMap<String, User> getMap() {
		return UserMap;
	}
	
	public int numUsers() {
		return UserMap.size();
	}
	

	public boolean Save() {
		System.out.println("Saving fake-db");
		FileOutputStream fos;
		ObjectOutputStream oos;
		try {
			fos = new FileOutputStream(db_file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(UserMap);
			oos.close();
			return true;
		}
		catch(IOException ex) {
			ex.printStackTrace();
			return false;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public boolean Load(AmazonEC2 ec2, AmazonS3Client s3) {
		FileInputStream fis=null;
		ObjectInputStream oin= null;
		
		try {
			fis = new FileInputStream(db_file);
			oin = new ObjectInputStream(fis);
			UserMap = (HashMap<String, User>) oin.readObject();
			oin.close();
		}
		catch(IOException ex) {
			System.out.println("[Error] Couldn't find file");
			System.out.println("First time being run");
			//ex.printStackTrace();
			return false;
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	
	
	public static void main(String args[]) throws FileNotFoundException, IllegalArgumentException, IOException {
		String db_file = "./data/users.ser";
		String cred_file = "./data/AwsCredentials.properties";
		DB db = new DB(db_file);

		AWSCredentials credentials = new PropertiesCredentials(new File(cred_file));
		AmazonEC2 ec2 = new AmazonEC2Client(credentials);
		AmazonS3Client s3 = new AmazonS3Client(credentials);

		
		db.Load(ec2, s3);
		
		HashMap<String, User> UserMap = db.getMap();
		
		for (String k : UserMap.keySet()) {
			System.out.println(UserMap.get(k).getUsername());
			System.out.println(UserMap.get(k).getInstance_id());
			System.out.println(UserMap.get(k).getImageId());
			System.out.println(UserMap.get(k).get_ip());//
			System.out.println(UserMap.get(k).getKp());
			System.out.println(UserMap.get(k).getS3_bucket());
			System.out.println(UserMap.get(k).getSecurity_grp());
			System.out.println(UserMap.get(k).getVolume_id());
			System.out.println(UserMap.get(k).getAvailability_zone());
			System.out.println(UserMap.get(k).getWakeDate().get(Calendar.MONTH) 
					+ "-" + UserMap.get(k).getWakeDate().get(Calendar.DAY_OF_MONTH)
					+ "-" + UserMap.get(k).getWakeDate().get(Calendar.HOUR_OF_DAY) 
					+ ":" + UserMap.get(k).getWakeDate().get(Calendar.MINUTE));
			System.out.println();
		
			/*you can edit the "db" here  remember to save*/
			//UserMap.get(k).getWakeDate().set(Calendar.DAY_OF_MONTH, 30);
			//UserMap.get(k).getWakeDate().set(Calendar.HOUR_OF_DAY, 2);
		}
		
		//db.Save();

	}
}
