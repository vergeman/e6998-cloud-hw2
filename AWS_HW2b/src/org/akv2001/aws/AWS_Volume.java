package org.akv2001.aws;

import org.akv2001.users.User;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.Volume;

public class AWS_Volume {


	/*********************************************
	 * #2.1 Create a volume
	 *********************************************/
	public static void createVolume(AmazonEC2 ec2, User u) {
		// create a volume
		String availability_zone = u.getAvailability_zone();
		
		try {
			CreateVolumeRequest cvr = new CreateVolumeRequest();
			cvr.setAvailabilityZone(availability_zone);
			cvr.setSize(8); // size = 8gigabytes
			CreateVolumeResult volumeResult = ec2.createVolume(cvr);
			
			u.setVolume_id(volumeResult.getVolume().getVolumeId());
			
			System.out.println("Created Volume: " + u.getVolume_id());
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}

	}
	
	/*********************************************
     *  #2.2 Attach the volume to the instance
     *********************************************/
	public static void attachVolume(AmazonEC2 ec2, String instance_id, String volume_id) {
		try {
			AttachVolumeRequest avr = new AttachVolumeRequest();
			avr.setVolumeId(volume_id);
			avr.setInstanceId(instance_id);
			avr.setDevice("/dev/sdf");
			ec2.attachVolume(avr);
			
			System.out.println("Attached Volume: " + volume_id + " to " + instance_id);
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}
	}

	
	/*********************************************
     *  #2.3 Detach the volume from the instance
     *********************************************/
	public static void DetachVolume(AmazonEC2 ec2, String instance_id, String volume_id) {
		try {
			DetachVolumeRequest dvr = new DetachVolumeRequest();
			dvr.setVolumeId(volume_id);
			dvr.setInstanceId(instance_id);
			ec2.detachVolume(dvr);
			
			System.out.println("Detached Volume: " + volume_id + " to " + instance_id);
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}
     	
	}
	
	public static void WaitRunningVolume(AmazonEC2 ec2, String volume_id) {
		DescribeVolumesRequest dvr = new DescribeVolumesRequest();
		dvr.withVolumeIds(volume_id);

		while (true) {
			try {
				DescribeVolumesResult dvres = ec2.describeVolumes(dvr);
				for (Volume v : dvres.getVolumes()) {
					if (v.getState().equals("running")) {
						return;
					}
				}
			} catch (AmazonClientException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
