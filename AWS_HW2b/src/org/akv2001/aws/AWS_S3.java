package org.akv2001.aws;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;


import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class AWS_S3 {
	
	public static void createBucket(AmazonS3Client s3, String bucketName) {
		try {
			s3.createBucket(bucketName);
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}
	}
	
	public static void write(AmazonS3Client s3, String bucketName, String key, String content) {
		if (!s3.doesBucketExist(bucketName)) {
			AWS_S3.createBucket(s3, bucketName);
		}
		// set value
		try {
			File file = File.createTempFile("temp", ".txt");
			file.deleteOnExit();
			Writer writer = new OutputStreamWriter(new FileOutputStream(file));
			writer.write(content);
			writer.close();
			s3.putObject(new PutObjectRequest(bucketName, key, file));	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AmazonClientException e) {
			e.printStackTrace();
		}
		
	}
	
}
