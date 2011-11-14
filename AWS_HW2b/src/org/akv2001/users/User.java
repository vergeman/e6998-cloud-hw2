package org.akv2001.users;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;

public class User implements Serializable {

	private static final long serialVersionUID = -7315774117496862887L;

	// instance creation
	private String username;
	private String security_grp;
	private String kp_id;
	private String kp_material;

	// instance creation identifiers
	private String instance_id;
	private String availability_zone;
	
	// volume
	private String volume_id;

	// s3
	private String s3_bucket;

	// elastic ip
	private String ip;

	// snapshot..
	private String ami_name;
	private String imageId;

	//montoring
	private boolean sleeping;
	private Calendar sleepDate;
	private Calendar wakeDate;
	
	public Calendar getWakeDate() {
		return wakeDate;
	}

	public Calendar getSleepDate() {
		return sleepDate;
	}

	public void setSleepDate(Calendar sleepDate) {
		this.sleepDate = sleepDate;
	}
	

	public void setWakeDate(int wake_hour, int end_hour) {
		this.sleepDate = Calendar.getInstance();
		this.wakeDate = this.sleepDate;
		
		//if after wake hour, set to "next" wake hour
		if (this.wakeDate.get(Calendar.HOUR_OF_DAY) >= wake_hour) {
			this.wakeDate.add(Calendar.DATE, 1);
		}
		//otherwise, wake hour is next
		this.wakeDate.set(Calendar.HOUR_OF_DAY, wake_hour);
		this.wakeDate.set(Calendar.MINUTE, 0);
		this.wakeDate.set(Calendar.SECOND, 0);

	}

	private transient Double averageCPU;
	private transient Date timeStamp;
	private transient Datapoint data;
	
	
	public Double getAverageCPU() {
		return averageCPU;
	}

	public void setAverageCPU(Double averageCPU) {
		this.averageCPU = averageCPU;
	}

	public User(AmazonEC2 ec2, String username, String security_grp) {
		setUsername(username);
		setSecurity_grp(security_grp);
		setKp(username);
		create_KeyPair(ec2);

	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String get_ip() {
		return ip;
	}

	public void set_ip(String ip) {
		this.ip = ip;
	}

	public String getS3_bucket() {
		return s3_bucket;
	}

	public void setS3_bucket(String s3_bucket) {
		this.s3_bucket = s3_bucket;
	}

	private void create_KeyPair(AmazonEC2 ec2) {
		/* create new keypair */
		try {
			CreateKeyPairResult result = ec2
					.createKeyPair(new CreateKeyPairRequest(kp_id));
			kp_material = result.getKeyPair().getKeyMaterial();
			write_pkey("./data/" + username);
		} catch (AmazonClientException e) {

		}
	}

	public String getKp() {
		return kp_id;
	}

	public void setKp(String kp_id) {
		this.kp_id = kp_id;
	}

	public String getSecurity_grp() {
		return security_grp;
	}

	public void setSecurity_grp(String security_grp) {
		this.security_grp = security_grp;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getInstance_id() {
		return instance_id;
	}

	public void setInstance_id(String instance_id) {
		this.instance_id = instance_id;
	}

	public void write_pkey(String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename
					+ ".pem"));
			out.write(kp_material);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String get_ami_name() {
		return ami_name;
	}

	public void set_ami_name(String ami_name) {
		this.ami_name = ami_name;
	}

	public boolean isSleeping() {
		return sleeping;
	}

	public void setSleeping(boolean sleeping) {
		this.sleeping = sleeping;
	}

	public String getAvailability_zone() {
		return availability_zone;
	}

	public void setAvailability_zone(String availability_zone) {
		this.availability_zone = availability_zone;
	}

	public String getVolume_id() {
		return volume_id;
	}

	public void setVolume_id(String volume_id) {
		this.volume_id = volume_id;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Datapoint getData() {
		return data;
	}

	public void setData(Datapoint data) {
		this.data = data;
	}



}
