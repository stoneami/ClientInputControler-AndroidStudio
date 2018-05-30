package com.stone.net;

import com.stone.utils.MouseAction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;

/**
 * Created by stoneami on 2018/5/30.
 */

public class AutoReproductSocketRunnable  extends SocketRunnable {
    private volatile boolean mGoOn = true;
    private BufferedWriter mWriter = null;
    private ExecutorService mSocketExecutor;

    public AutoReproductSocketRunnable(MouseAction action,
                                       ExecutorService executor, String dstHost, int dstPort) {
        super(action, dstHost, dstPort);
        // TODO Auto-generated constructor stub

        if (executor == null)
            throw new RuntimeException("ExecutorService cannot be null !");
        mSocketExecutor = executor;
    }

    @SuppressWarnings("unused")
    public void enableReproduct() {
        mGoOn = true;
    }

    public void disableReproduct() {
        mGoOn = false;
    }

    @Override
    public void sendServerMsg(String msg) {
        // TODO Auto-generated method stub
        try {
            if (mWriter == null) {
                Socket socket = new Socket(mDstHost, mDstPort);
                mWriter = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream()));
            }
            mWriter.flush();
            mWriter.write(msg);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();

        if (mGoOn) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mSocketExecutor.execute(this);

        } else {
            try {
                if (mWriter != null)
                    mWriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}