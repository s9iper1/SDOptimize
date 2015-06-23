package com.android.sd.optimize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class Util extends ContextWrapper {

    private String LOG_TAG ="LOGTAG";

    private Session mSession;
    private Channel mChannel;
    private ChannelSftp mChannelSftp;
	private Context mContext;

    public Util(Context base) {
        super(base);
		mContext = base;
    }

    void uploadFileViaSftp(String path, String name) {
        String userName = mContext.getResources().getString(R.string.sftp_username);
        String password = mContext.getResources().getString(R.string.sftp_password);
        String host = mContext.getResources().getString(R.string.sftp_host);
        String port = mContext.getResources().getString(R.string.sftp_port);
        String workingDirectory = mContext.getResources().getString(R.string.sftp_working_directory);

		JSch jsch = new JSch();
		try {
			mSession = jsch.getSession(userName, host, Integer.valueOf(port));
			mSession.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			mSession.setConfig(config);
			mSession.connect();
			Log.i(LOG_TAG, "Host connected.");
			mChannel = mSession.openChannel("sftp");
			mChannel.connect();
			Log.i(LOG_TAG, "sftp channel opened and connected.");
			mChannelSftp = (ChannelSftp) mChannel;
			mChannelSftp.cd(workingDirectory);
		} catch (JSchException ignore) {

		} catch (SftpException e) {
			e.printStackTrace();
		}

        File file = new File(path.concat(name));
        try {
            mChannelSftp.put(new FileInputStream(file), file.getName());
            Log.i(LOG_TAG, "upload done");
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, String.format("File %s not found", path.concat(name)));
        } catch (SftpException e) {
            Log.e(LOG_TAG, String.format("There was an error uploading: %s", path.concat(name)));
        }
	}

	public static String getTime() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
		String formattedDate = df.format(c.getTime());
		return formattedDate;
	}
}