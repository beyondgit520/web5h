package cn.com.husan.cs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SafeWebViewBridge.JsCallback;

/**
 * Created by Administrator on 2016/7/6.
 */
public class AppJs {
    public static String SUCCESSURL = "";
    public static String FAILURL = "";
    public static String CANCELURL = "";

    //微信支付
    public static void sendPayReq(WebView webView, JSONObject json) {
        Logger.d("hohoapp", json.toString());
        try {
            //微信支付
            PayReq req;
            final IWXAPI msgApi = WXAPIFactory.createWXAPI(webView.getContext(), Constant.APP_ID, true);
            msgApi.registerApp(Constant.APP_ID);
            req = new PayReq();
            req.appId = json.getString("appid");
            req.partnerId = json.getString("mch_id");
            req.prepayId = json.getString("prepay_id");
            req.packageValue = "Sign=WXPay";
            req.nonceStr = json.getString("nonce_str");
            req.timeStamp = json.getString("timestamp");
            req.sign = json.getString("sign");
            msgApi.sendReq(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void successurl(WebView webView, String url) {
        SUCCESSURL = url;
//        Toast.makeText(webView.getContext(),"successurl:"+url,Toast.LENGTH_SHORT).show();
    }

    public static void failurl(WebView webView, String url) {
        FAILURL = url;
//        Toast.makeText(webView.getContext(),"failurl:"+url,Toast.LENGTH_SHORT).show();
    }

    public static void cancelurl(WebView webView, String url) {
        CANCELURL = url;
//        Toast.makeText(webView.getContext(),"cancelurl:"+url,Toast.LENGTH_SHORT).show();
    }

    public static void wxAuth(WebView webView, final JsCallback jsCallback) {
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        WXAPIFactory.createWXAPI(webView.getContext(), Constant.APP_ID, true).sendReq(req);
        ((MainActivity) webView.getContext()).setJsCallback(jsCallback);
    }

    public static void call(WebView webView, String number) {
        Logger.d("hohoapp", number);
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
