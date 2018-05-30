package com.stone.utils;

import android.util.Log;

/**
 * Created by stoneami on 2018/5/30.
 */

public class Util {
    private final static String TAG = "Util";

    public static String createCommandString(int dx, int dy) {
        StringBuilder msg = new StringBuilder("###");
        if (dx > 0) {
            msg.append("r");
            msg.append(String.valueOf(dx));
        } else {
            msg.append("l");
            msg.append(String.valueOf(-dx));
        }

        if (dy > 0) {
            msg.append("d");
            msg.append(String.valueOf(dy));
        } else {
            msg.append("u");
            msg.append(String.valueOf(-dy));
        }

        //Log.i(TAG, "onMove(): msg = " + msg.toString());

        return msg.toString();
    }
}
