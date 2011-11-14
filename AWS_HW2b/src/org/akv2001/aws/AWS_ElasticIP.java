package org.akv2001.aws;


import org.akv2001.users.User;


import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.DisassociateAddressRequest;


public class AWS_ElasticIP {
	

	public static void allocate(AmazonEC2 ec2, User u) {
		try {
			AllocateAddressResult elasticResult = ec2.allocateAddress();
			String elasticIp = elasticResult.getPublicIp();
			u.set_ip(elasticIp);
			System.out.println("New elastic IP: " + elasticIp);
	
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}

	}

	public static void associate(AmazonEC2 ec2, String instanceId, String elasticIp) {
		try {
			AssociateAddressRequest aar = new AssociateAddressRequest();
			aar.setInstanceId(instanceId);
			aar.setPublicIp(elasticIp);
			
			ec2.associateAddress(aar);
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}
	}
	
	public static void dissassociate(AmazonEC2 ec2, String ip) {
		try {
			DisassociateAddressRequest dar = new DisassociateAddressRequest();
			dar.setPublicIp(ip);
			ec2.disassociateAddress(dar);
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}
