package com.sonycsl.Kadecot.plugin.gotapi;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;

import com.sonycsl.Kadecot.plugin.PostReceiveCallback;
import com.sonycsl.Kadecot.plugin.ProviderAccessObject;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.transport.ProxyPeer;
import com.sonycsl.wamp.transport.WampWebSocketTransport;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GotAPIPluginService extends Service {
    private static final String LOCALHOST = "localhost";
    private static final int WEBSOCKET_PORT = 41314;

    private static final String EXTRA_ACCEPTED_ORIGIN = "acceptedOrigin";
    private static final String EXTRA_ACCEPTED_TOKEN = "acceptedToken";

    private ProviderAccessObject mPao;

    private GotAPIClient mClient;

    private WampWebSocketTransport mTransport;

    @Override
    public void onCreate() {
        super.onCreate();
        mClient = new GotAPIClient(this);
        mClient.setCallback(new PostReceiveCallback() {

            @Override
            public void postReceive(WampPeer transmitter, WampMessage msg) {
                if (msg.isWelcomeMessage()) {
                    mClient.onSearchEvent(null);
                }
            }
        });

        mPao = new ProviderAccessObject(getContentResolver());
        mTransport = new WampWebSocketTransport();

        final ProxyPeer proxyPeer = new ProxyPeer(mTransport);

        /**
         * Set a listener to transmit the message which is sent from web socket
         * server to client. <br>
         * Stop this service when the web socket is closed <br>
         */
        mTransport.setOnWampMessageListener(new WampWebSocketTransport.OnWampMessageListener() {

            @Override
            public void onMessage(WampMessage msg) {
                proxyPeer.transmit(msg);
            }

            @Override
            public void onError(Exception e) {
                stopSelf();
            }

            @Override
            public void onClose() {
                stopSelf();
            }
        });

        mClient.connect(proxyPeer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /**
         * Send GOODBYE message to close WAMP session. <br>
         */
        mClient.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                WampError.GOODBYE_AND_OUT));
        /**
         * Close Web socket transport.
         */
        mTransport.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        /**
         * Put Plug-in information into content provider of kadecot. <br>
         * The information includes protocol data with setting activity name
         * space and device type data with icons. <br>
         * Stop service when this plug-in can not access to the provider. <br>
         */
        try {
            mPao.putProtocolInfo(getProtocolData());
            mPao.putDeviceTypesInfo(getDeviceTypesData());
        } catch (IllegalArgumentException e) {
            stopSelf();
        }

        String origin = "";
        if (intent != null && intent.hasExtra(EXTRA_ACCEPTED_ORIGIN)) {
            origin = intent.getStringExtra(EXTRA_ACCEPTED_ORIGIN);
        }
        String token = "";
        if (intent.hasExtra(EXTRA_ACCEPTED_TOKEN)) {
            token = intent.getStringExtra(EXTRA_ACCEPTED_TOKEN);
        }
        /**
         * Open Web socket transport.
         */
        mTransport.open(LOCALHOST, WEBSOCKET_PORT, origin, token);
        /**
         * Send HELLO message to open WAMP session. <br>
         */
        mClient.transmit(WampMessageFactory.createHello("realm", new JSONObject()));
        return START_REDELIVER_INTENT;
    }

    private KadecotCoreStore.ProtocolData getProtocolData() {
        return new KadecotCoreStore.ProtocolData(GotAPIClient.PROTOCOL_NAME
                , "org.deviceconnect.android.manager", "org.deviceconnect.android.manager.setting.SettingActivity");
    }

    private Set<KadecotCoreStore.DeviceTypeData> getDeviceTypesData() {
        Set<KadecotCoreStore.DeviceTypeData> set = new HashSet<KadecotCoreStore.DeviceTypeData>();
        set.add(new KadecotCoreStore.DeviceTypeData(GotAPIClient.DEVICE_TYPE_DEVICE_CONNECT,
                GotAPIClient.PROTOCOL_NAME, BitmapFactory.decodeResource(getResources(),
                R.drawable.icon)));
        PackageManager pm = getPackageManager();
        List<PackageInfo> pkgList = pm.getInstalledPackages(PackageManager.GET_RECEIVERS);
        if (pkgList != null) {
            for (PackageInfo pkg : pkgList) {
                ActivityInfo[] receivers = pkg.receivers;
                String packageName  = pkg.packageName;
                if (receivers != null) {
                    for (int i = 0; i < receivers.length; i++) {
                        String className = receivers[i].name;
                        if (packageName == null) {
                            continue;
                        }
                        ComponentName component = new ComponentName(packageName, className);
                        try {
                            ActivityInfo info = pm.getReceiverInfo(component, PackageManager.GET_META_DATA);
                            if (info.metaData == null) {
                                continue;
                            }
                            Object value = info.metaData.get("org.deviceconnect.android.deviceplugin");
                            if (value == null) {
                                continue;
                            }
                            Drawable icon = info.applicationInfo.loadIcon(pm);
                            KadecotCoreStore.DeviceTypeData data = new KadecotCoreStore.DeviceTypeData(
                                    packageName, GotAPIClient.PROTOCOL_NAME, drawableToBitmap(icon));
                            set.add(data);
                            break;
                        } catch (PackageManager.NameNotFoundException e) {
//                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return set;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()
                    , Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
