package cn.com.husan.cs.wxapi;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import cn.com.husan.cs.Constant;
import cn.com.husan.cs.R;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXPayEntryActivity";

    private IWXAPI api;
    LocalBroadcastManager localBroadcastManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        api = WXAPIFactory.createWXAPI(this, Constant.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(final BaseResp resp) {
        Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);

        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
/*            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent("wx_pay");
                    switch (resp.errCode) {
                        case 0:
                            intent.putExtra("result", 0);
                            break;
                        case -1:
                            intent.putExtra("result", -1);
                            break;
                        case -2:
                            intent.putExtra("result", -2);
                            break;
                    }
                    localBroadcastManager.sendBroadcast(intent);
                    finish();
                }
            });
            builder.setTitle("提示");
            builder.setMessage("微信支付结果：" + resp.errCode);
            builder.show();
            builder.setCancelable(false);*/
            Intent intent = new Intent("wx_pay");
            switch (resp.errCode) {
                case 0:
                    intent.putExtra("result", 0);
                    break;
                case -1:
                    intent.putExtra("result", -1);
                    break;
                case -2:
                    intent.putExtra("result", -2);
                    break;
            }
            localBroadcastManager.sendBroadcast(intent);
            finish();
        }
    }
}