package cn.com.husan.cs;

import android.util.Log;

/**
 * Created by 李 on 16-7-7.
 */
public class Logger {
    public static void d(String tag, String msg) {
        if (Constant.DEBUG) Log.d(tag, msg);
    }
}
