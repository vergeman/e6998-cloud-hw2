package org.akv2001.aws;

import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;


public class AWS_SecurityGroup {
	String grp_name;
	String desc;
	
	CreateSecurityGroupRequest sg;
	ArrayList<AuthorizeSecurityGroupIngressRequest> IngressInfoList;
	DescribeSecurityGroupsResult d_sg;
	
	
	public AWS_SecurityGroup(String grp_name, String desc) {
		setGrp_name(grp_name);
		setDesc(desc);
		setSg(new CreateSecurityGroupRequest(grp_name, desc));
	}
	 
	public void setSecurityGroup(AmazonEC2 ec2) {
		try {
			ec2.createSecurityGroup(sg);
			
			for (AuthorizeSecurityGroupIngressRequest asg : IngressInfoList) {
				ec2.authorizeSecurityGroupIngress(asg);
			}
		
		}
		catch (AmazonClientException e) {
			
		}
	}
	
	public void add_GroupIngressRequest(String protocol, String IP, int from_port, int to_port) {
		
		if (IngressInfoList == null) {
			IngressInfoList = new ArrayList<AuthorizeSecurityGroupIngressRequest>();
		}
		
		AuthorizeSecurityGroupIngressRequest asg = create_SecurityGroupIngress(grp_name,
				protocol, IP, from_port, to_port);
		
		IngressInfoList.add(asg);
	}
	
	private AuthorizeSecurityGroupIngressRequest create_SecurityGroupIngress(String grp_name,
			String protocol, String IP, int from_port, int to_port) {
		
		AuthorizeSecurityGroupIngressRequest asg = new AuthorizeSecurityGroupIngressRequest();
		asg.setCidrIp(IP);
		asg.setIpProtocol(protocol);
		asg.setFromPort(from_port);
		asg.setToPort(to_port);
		asg.setGroupName(grp_name);
		return asg;
	}
	
	public CreateSecurityGroupRequest getSg() {
		return sg;
	}

	public void setSg(CreateSecurityGroupRequest sg) {
		this.sg = sg;
	}

	public String getGrp_name() {
		return grp_name;
	}

	public void setGrp_name(String grp_name) {
		this.grp_name = grp_name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}




	
	
	

}
