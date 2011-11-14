package org.akv2001;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.akv2001.users.User;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;


public class Monitor {

	public static boolean isIdle(User u, double cpu_util) {

		if (u.getAverageCPU() != null && 
				u.getAverageCPU() <  cpu_util)  {
			return true;
		}

		return false;
	}
	
	public static boolean isEOD(int wake_hour, int end_hour) {
		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);   

		if (hour >= wake_hour && hour <= end_hour) {
			return false;
		}
		
		return true;
	}
	

	
	public static void getStats(AmazonCloudWatchClient cloudWatch, User u, List<String> stats, String metric) {
		try {
			// set up request message
			GetMetricStatisticsRequest statRequest = new GetMetricStatisticsRequest();
			statRequest.setNamespace("AWS/EC2"); // namespace
			statRequest.setPeriod(60); // period of data

			// set metrics
			statRequest.setStatistics(stats);
			statRequest.setMetricName(metric);

			// set timeframe
			GregorianCalendar calendar = new GregorianCalendar(
					TimeZone.getTimeZone("UTC"));
			calendar.add(GregorianCalendar.SECOND,
					-1 * calendar.get(GregorianCalendar.SECOND)); // 1 second
																	// ago
			Date endTime = calendar.getTime();
			calendar.add(GregorianCalendar.MINUTE, -10); // 10 minutes ago
			Date startTime = calendar.getTime();
			statRequest.setStartTime(startTime);
			statRequest.setEndTime(endTime);

			// specify an instance
			ArrayList<Dimension> dimensions = new ArrayList<Dimension>();
			dimensions.add(new Dimension().withName("InstanceId").withValue(
					u.getInstance_id()));
			statRequest.setDimensions(dimensions);

			// get statistics
			GetMetricStatisticsResult statResult = cloudWatch.getMetricStatistics(statRequest);
			List<Datapoint> dataList = statResult.getDatapoints();

			if (dataList.isEmpty()) {
				System.out.println("Awaiting statistics. . .");
			}
			
			for (Datapoint data : dataList) {
				u.setTimeStamp(data.getTimestamp());
				u.setAverageCPU(data.getAverage());
				u.setData(data);

				// print
				if(u.isSleeping()) {
					System.out.println("==Terminated Instance Statistics==");
				}
				System.out.println(statResult.toString());
				System.out.println("User: " + u.getUsername());
				System.out.println("instanceID: " + u.getInstance_id());
				System.out
						.println("Average CPU utilization for last 10 minutes: "
								+ u.getAverageCPU());
				System.out
						.println("Total CPU utilization for last 10 minutes: "
								+ u.getData().getSum());
			}
		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}

	}
	

	
}
