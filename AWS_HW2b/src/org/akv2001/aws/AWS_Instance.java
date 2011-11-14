package org.akv2001.aws;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.akv2001.users.User;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

public class AWS_Instance  {

	/* creates new instance, populates info to user */
	public static void New(AmazonEC2 ec2, String imageId, User u) {
		try {
			RunInstancesRequest rir = new RunInstancesRequest(imageId, 1, 1);

			rir
			.withSecurityGroupIds(u.getSecurity_grp())
			.withKeyName(u.getKp())
			.withInstanceType("t1.micro");

			if (u.getAvailability_zone() != null) {
				rir.withPlacement(new Placement(u.getAvailability_zone()));
			}
			
			RunInstancesResult result = ec2.runInstances(rir);
		
			List<Instance> resultInstance = result.getReservation()
					.getInstances();

			for (Instance ins : resultInstance) {
				u.setInstance_id(ins.getInstanceId());
				u.setAvailability_zone(ins.getPlacement().getAvailabilityZone());
				u.setSleeping(true);
				
				System.out.println("New instance has been created: "
						+ ins.getInstanceId());

			}
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}

	}

	public static void Terminate(AmazonEC2 ec2, String instance_id) {
		List<String> tList = new ArrayList<String>();
		tList.add(instance_id);
		TerminateInstancesRequest tr = new TerminateInstancesRequest(tList);
		TerminateInstancesResult tres = ec2.terminateInstances(tr);
		boolean still_terminating = true;

		while (still_terminating) {
			still_terminating = false;
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			tres = ec2.terminateInstances(tr);
			for (InstanceStateChange ins : tres.getTerminatingInstances()) {
				
					if (!ins.getCurrentState().getName()
							.equals("terminated")) {
						still_terminating = true;
					}

				
			}
			
			System.out.println("Verifying termination of: " + instance_id);
		}
	}

	public static void Snapshot(AmazonEC2 ec2, User u, String instance_id) {
		/***********************************
		 * #2 Create an AMI from an instance
		 *********************************/
		try {
			CreateImageRequest cir = new CreateImageRequest();
			cir.setInstanceId(instance_id);

			String ami_name = u.getUsername() + "_" + instance_id + "_" + Calendar.getInstance().getTimeInMillis();
			cir.setName(ami_name);
			u.set_ami_name(ami_name);

			CreateImageResult createImageResult = ec2.createImage(cir);
			String createdImageId = createImageResult.getImageId();

			u.setImageId(createdImageId);

			System.out.println("Sent snapshot creating AMI request. AMI id="
					+ createdImageId);
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}

	
		
	}
	
	public static void waitforAvailableSnapshot(AmazonEC2 ec2, User u, String createdImageId) {
		boolean available = false;
		while (!available) {

			System.out.println("waiting for " + createdImageId
					+ " snapshot to become available");

			try {
				DescribeImagesRequest direq = new DescribeImagesRequest();
				direq.withImageIds(createdImageId);
				DescribeImagesResult dir = ec2.describeImages(direq);

				for (Image i : dir.getImages()) {

					if (i.getImageId().equals(createdImageId)
							&& i.getState().equals("available")) {
						available = true;
					}

				}
			} catch (AmazonClientException e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
	
	public static void loadInstanceStatus(AmazonEC2 ec2, User u) {
		while (true) {
			try {
				DescribeInstancesResult dir = ec2.describeInstances();
				List<Reservation> reservations = dir.getReservations();

				for (Reservation res : reservations) {
					for (Instance ins : res.getInstances()) {

						if (ins.getInstanceId().toString().equals(u.getInstance_id())
								&& ins.getState().getName().equals("running")) {
							System.out.println(u.getInstance_id() + " status is: running");
							u.setSleeping(false);
							return;
						}
						if (ins.getInstanceId().toString().equals(u.getInstance_id())
								&& !ins.getState().getName().equals("running")) {
							System.out.println(u.getInstance_id() + " status is: " + ins.getState().getName());
							u.setSleeping(true);
							return;
						}
					}
				}
			} catch (AmazonClientException e) {
				e.printStackTrace();
			}
			try {
				System.out.println("Waiting for instance to run: "
						+ u.getInstance_id());
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	
	}
	
	
	public static void waitforRunningInstances(AmazonEC2 ec2, User u) {

		while (true) {
			try {
				DescribeInstancesResult dir = ec2.describeInstances();
				List<Reservation> reservations = dir.getReservations();

				for (Reservation res : reservations) {
					for (Instance ins : res.getInstances()) {

						if (ins.getInstanceId().toString()
								.equals(u.getInstance_id())
								&& ins.getState().getName().equals("running")) {
							System.out.println(ins.getInstanceId() + " running");
							u.setSleeping(false);
							return;
						}
					}
				}
			} catch (AmazonClientException e) {
				e.printStackTrace();
			}
			try {
				System.out.println("Waiting for instance to run: "
						+ u.getInstance_id());
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}


}