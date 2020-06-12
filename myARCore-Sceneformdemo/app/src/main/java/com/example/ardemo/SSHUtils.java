package com.example.ardemo;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class SSHUtils {
    public static String executeRemoteCommand(String username,String password,String hostname,int port, String command)
            throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        // SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);



        InputStream inputStream = channelssh.getInputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder stringBuilder = new StringBuilder();

        String line;



        // Execute command
        channelssh.setCommand(command);
        channelssh.connect();

        while ((line = bufferedReader.readLine()) != null)
        {

            stringBuilder.append(line);
            stringBuilder.append('\n');

        }

        channelssh.disconnect();




        return stringBuilder.toString();
        //return baos.toString();
    }
}
