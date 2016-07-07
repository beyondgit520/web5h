package cn.com.husan.cs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/7/6.
 */
public class AppJs {
    //微信支付
    public static void sendPayReq(WebView webView, JSONObject json) {
        Toast.makeText(webView.getContext(), json.toString(), Toast.LENGTH_LONG).show();
        Log.d("hohoapp", json.toString());

        try {
            JSONObject object = json;
            //微信支付
            PayReq req;
            final IWXAPI msgApi = WXAPIFactory.createWXAPI(webView.getContext(), Constant.APP_ID,true);
            msgApi.registerApp(Constant.APP_ID);
            req = new PayReq();
            req.appId = object.getString("appid");
            req.partnerId = object.getString("mch_id");
            req.prepayId = object.getString("prepay_id");
            req.packageValue = "Sign=WXPay";
            req.nonceStr = object.getString("nonce_str");
            req.timeStamp = object.getString("timestamp");
            req.sign = object.getString("sign");
            msgApi.sendReq(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void call(WebView webView, String number) {
        Log.d("hohoapp", number);
        Toast.makeText(webView.getContext(), number, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(webView.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        webView.getContext().startActivity(intent);
    }

    public static String getDeviceId(WebView webView) {
        return ((TelephonyManager) webView.getContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }
}
