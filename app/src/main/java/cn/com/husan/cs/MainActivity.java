package cn.com.husan.cs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;

import cn.pedant.SafeWebViewBridge.InjectedChromeClient;
import cn.pedant.SafeWebViewBridge.JsCallback;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;
    private WebSettings webSettings;
    LocalBroadcastManager localBroadcastManager;
    private Context mContext;
    private IWXAPI iwxapi;
    private JsCallback jsCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        webView = (WebView) findViewById(R.id.mWebView);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        initWebView();
        webView.setWebViewClient(mWebViewClient);
        webView.setWebChromeClient(new CustomChromeClient("HohoApp", AppJs.class));
        webView.loadUrl(Constant.path);
//        webView.loadUrl("file:///android_asset/test.html");
        dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        regToWx();
    }

    public void setJsCallback(JsCallback jsCallback) {
        this.jsCallback = jsCallback;
    }

    private void regToWx() {
        iwxapi = WXAPIFactory.createWXAPI(mContext, Constant.APP_ID, true);
        iwxapi.registerApp(Constant.APP_ID);
    }

    public void login(View view) {
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        iwxapi.sendReq(req);
    }

    private void initWebView() {
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setGeolocationEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        String cacheDirPath = getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();//APP.baseDir+APP_CACAHE_DIRNAME;
        webSettings.setGeolocationDatabasePath(cacheDirPath);
        //设置数据库缓存路径
        webSettings.setDatabasePath(cacheDirPath);
        //设置  Application Caches 缓存目录
        webSettings.setAppCachePath(getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath());
        //开启 Application Caches 功能
        webSettings.setAppCacheEnabled(true);
        webSettings.setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= 19) {
            webSettings.setLoadsImagesAutomatically(true);
        } else {
            webSettings.setLoadsImagesAutomatically(false);
        }
    }

    private long lastTime;
    private Handler handler = new Handler();

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (System.currentTimeMillis() - lastTime < 3000) {
                super.onBackPressed();
            } else {
                Toast.makeText(mContext, "再按一次退出！", Toast.LENGTH_SHORT).show();
            }
            lastTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        localBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                Toast.makeText(MainActivity.this, "支付:" + intent.getIntExtra("result", -1), Toast.LENGTH_LONG).show();
                switch (intent.getIntExtra("result", -1)) {
                    case 0:
                        Toast.makeText(MainActivity.this, "支付成功!", Toast.LENGTH_LONG).show();
//                        webView.loadUrl("http://www.xi6666.com/user.php");
                        break;
                    case -1:
                        Toast.makeText(MainActivity.this, "支付失败!", Toast.LENGTH_LONG).show();
                        break;
                    case -2:
                        Toast.makeText(MainActivity.this, "你已取消支付!", Toast.LENGTH_LONG).show();
//                        webView.loadUrl("http://www.xi6666.com/user.php?act=order_list&info=pay");
                        break;
                }
            }
        }, new IntentFilter("wx_pay"));
    }

    WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            if (!webSettings.getLoadsImagesAutomatically()) {
                webSettings.setLoadsImagesAutomatically(true);
            }
            if (url.equals(Constant.path)) view.clearHistory();
        }
    };
    private AlertDialog.Builder dialog;
    private static final int REQUEST_FILE_PICKER = 1;
    private static final int TAKE_PICTURE = 2;
    private ValueCallback<Uri> mFilePathCallback4;
    private ValueCallback<Uri[]> mFilePathCallback5;

    public class CustomChromeClient extends InjectedChromeClient {

        public CustomChromeClient(String injectedName, Class injectedCls) {
            super(injectedName, injectedCls);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        public void openFileChooser(ValueCallback<Uri> filePathCallback) {
//            Toast.makeText(mContext, "openFileChooser1:", Toast.LENGTH_SHORT).show();
            mFilePathCallback4 = filePathCallback;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
        }

        public void openFileChooser(ValueCallback filePathCallback, String acceptType) {
//            Toast.makeText(mContext, "openFileChooser2:", Toast.LENGTH_SHORT).show();
            mFilePathCallback4 = filePathCallback;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
        }

        public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
//            Toast.makeText(mContext, "openFileChooser3:\nacceptType:"+acceptType+" \ncapture:"+capture, Toast
//                    .LENGTH_SHORT).show();
            mFilePathCallback4 = filePathCallback;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                Toast.makeText(mContext, "onShowFileChooser:" + fileChooserParams.getTitle() + " " + fileChooserParams
//                        .getFilenameHint() + " " + fileChooserParams.getMode()+" "+fileChooserParams.isCaptureEnabled
//                        ()+" "+fileChooserParams.getAcceptTypes().toString(),
//                        Toast.LENGTH_LONG)
//                        .show();
//            }
            mFilePathCallback5 = filePathCallback;
//            if (fileChooserParams.isCaptureEnabled()) {
//                takePicture();
//            } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
//            }

            return true;
        }

        @Override
        public boolean onJsAlert(final WebView view, final String url, String message, final JsResult result) {
            // to do your work
            // ...
//            Toast.makeText(MainActivity.this, "onJsAlert:" + message, Toast.LENGTH_SHORT).show();
            dialog.setTitle(view.getTitle());
            dialog.setMessage(message);
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.cancel();
                }
            });
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            });
            dialog.show();
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            // to do your work
            // ...
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            // to do your work
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }
    }

    private String imagePath;

    /**
     * 从相机获取图片
     */
    private void takePicture() {
        // 设置intent的意图 即获取图片
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 设置拍照的图片名字以当时的时间命名
        String imageName = "file.jpg";
        // 设置拍照的照片保存的绝对路径
        File dir = getExternalCacheDir();
        if (dir == null) {
            return;
        }
        imagePath = dir.getAbsolutePath() + "/" + imageName;
        // 设置intent中包含的拍照保存照片的Uri
        Uri imageUri = Uri.fromFile(new File(dir, imageName));
        // 给intent设置附加信息
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        // 启动拍照并获取结果
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_FILE_PICKER) {
            if (mFilePathCallback4 != null) {
                Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
                if (result != null) {
                    String path = getPath(MainActivity.this, result);
                    Uri uri = Uri.fromFile(new File(path));
                    mFilePathCallback4.onReceiveValue(uri);
                } else {
                    mFilePathCallback4.onReceiveValue(null);
                }
            }
            if (mFilePathCallback5 != null) {
                Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
                if (result != null) {
                    String path = getPath(MainActivity.this, result);
                    Uri uri = Uri.fromFile(new File(path));
                    mFilePathCallback5.onReceiveValue(new Uri[]{uri});
                } else {
                    mFilePathCallback5.onReceiveValue(null);
                }
            }

            mFilePathCallback4 = null;
            mFilePathCallback5 = null;
        } else if (requestCode == TAKE_PICTURE) {
            if (mFilePathCallback4 != null) {
                Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
                if (result != null) {
                    File photo = new File(imagePath);
                    if (photo.exists()) {
                        Uri uri = Uri.fromFile(photo);
                        mFilePathCallback4.onReceiveValue(uri);
                    } else {
                        mFilePathCallback4.onReceiveValue(null);
                    }
                } else {
                    mFilePathCallback4.onReceiveValue(null);
                }
            }
            if (mFilePathCallback5 != null) {
                Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
                if (result != null) {
                    File photo = new File(imagePath);
                    if (photo.exists()) {
                        Uri uri = Uri.fromFile(photo);
                        mFilePathCallback5.onReceiveValue(new Uri[]{uri});
                    } else {
                        mFilePathCallback5.onReceiveValue(null);
                    }
                } else {
                    mFilePathCallback5.onReceiveValue(null);
                }
            }
            mFilePathCallback4 = null;
            mFilePathCallback5 = null;
        }

    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
