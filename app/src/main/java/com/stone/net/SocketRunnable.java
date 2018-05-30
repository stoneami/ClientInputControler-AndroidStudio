package com.stone.net;

import com.stone.utils.MouseAction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by stoneami on 2018/5/30.
 */

public class SocketRunnable implements Runnable {
    protected MouseAction mAction = null;
    protected String mCommandStr = null;
    protected String mDstHost = "";
    protected int mDstPort = -1;

    public SocketRunnable(MouseAction action, String dstHost, int dstPort) {
        if (action == null) {
            throw new RuntimeException("Invalid MouseAction!");
        }

        mDstHost = dstHost;
        mDstPort = dstPort;
        mAction = action;
    }

    public SocketRunnable(String command, String dstHost, int dstPort) {
        if (command == null || command.length() < 1)
            throw new RuntimeException("Invalid Command!");

        mDstHost = dstHost;
        mDstPort = dstPort;
        mCommandStr = command;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        if (mAction != null) {
            sendServerMsg(mAction);
        } else {
            sendServerMsg(mCommandStr);
        }
    }

    public void sendServerMsg(String msg) {
        try {
            Socket socket = new Socket(mDstHost, mDstPort);
            BufferedWriter br = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream()));
            br.write(msg);
            br.flush();
            br.close();
        }  catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendServerMsg(MouseAction action) {
        String msg = null;
        switch (action) {
            case LEFT:
                msg = "l";
                break;
            case RIGHT:
                msg = "r";
                break;
            case UP:
                msg = "u";
                break;
            case DOWN:
                msg = "d";
                break;
            case CLICK:
                msg = "s";
                break;
            case DOUBLE_CLICK:
                msg = "t";
                break;
            case RIGHT_CLICK:
                msg = "m";
                break;
            case SCROLL:
                msg = "o";
                break;
            default:
                break;
        }

        if (msg != null)
            sendServerMsg(msg);
    }

}