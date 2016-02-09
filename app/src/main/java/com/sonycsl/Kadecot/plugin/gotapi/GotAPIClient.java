package com.sonycsl.Kadecot.plugin.gotapi;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.sonycsl.Kadecot.plugin.DeviceData;
import com.sonycsl.Kadecot.plugin.KadecotProtocolClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.role.WampCallee;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.basic.message.DConnectResponseMessage;
import org.deviceconnect.message.http.impl.factory.HttpMessageFactory;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.deviceconnect.profile.ConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.FileDescriptorProfileConstants;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.profile.MediaPlayerProfileConstants;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;
import org.deviceconnect.profile.NotificationProfileConstants;
import org.deviceconnect.profile.PhoneProfileConstants;
import org.deviceconnect.profile.ProximityProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.deviceconnect.profile.SettingsProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.profile.VibrationProfileConstants;
import org.deviceconnect.utils.AuthProcesser;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Fumiaki on 2015/03/13.
 */
public class GotAPIClient extends KadecotProtocolClient {
    public static final String PROTOCOL_NAME = "gotapi";

    static final String DEVICE_TYPE_DEVICE_CONNECT = "device-connect";

    private static final String LOCALHOST = "127.0.0.1";

    private static final String PRE_FIX = "com.sonycsl.kadecot." + PROTOCOL_NAME;
    private static final String PROCEDURE = ".procedure.";
    private static final String TOPIC = ".topic.";

    private static final String DELAY_PUBLISH_TOPIC = PRE_FIX + TOPIC + "delaypublish";

    private Context mContext;
    private Handler mHandler;

    private static final int GOT_API_PORT = 4035;

    private HashMap<String, String> mDeviceTypeDict;

    private String[] scopes = {
            ServiceInformationProfileConstants.PROFILE_NAME,
            AuthorizationProfileConstants.PROFILE_NAME,
            BatteryProfileConstants.PROFILE_NAME,
            ConnectProfileConstants.PROFILE_NAME,
            DeviceOrientationProfileConstants.PROFILE_NAME,
            FileDescriptorProfileConstants.PROFILE_NAME,
            FileProfileConstants.PROFILE_NAME,
            MediaPlayerProfileConstants.PROFILE_NAME,
            MediaStreamRecordingProfileConstants.PROFILE_NAME,
            ServiceDiscoveryProfileConstants.PROFILE_NAME,
            NotificationProfileConstants.PROFILE_NAME,
            PhoneProfileConstants.PROFILE_NAME,
            ProximityProfileConstants.PROFILE_NAME,
            SettingsProfileConstants.PROFILE_NAME,
            SystemProfileConstants.PROFILE_NAME,
            VibrationProfileConstants.PROFILE_NAME,

            // 独自プロファイル
            "light",
            "camera",
            "temperature",
            "dice",
            "sphero",
            "drive_controller",
            "remote_controller",
            "mhealth",

    };
    private AuthProcesser.AuthorizationHandler mAuthHandler
            = new AuthProcesser.AuthorizationHandler() {

        @Override
        public void onAuthorized(final String clientId,
                                 final String accessToken) {
            AccessTokenPreference.setAccessToken(mContext, accessToken);
            searchGotAPIDevice(LOCALHOST, GOT_API_PORT);

        }

        @Override
        public void onAuthFailed(DConnectMessage.ErrorCode error) {
            AccessTokenPreference.setAccessToken(mContext, null);
        }

    };

    /* profile -> procedure */
    public static enum Procedure {
        PROCEDURE1("procedure1", "http://example.plugin.explanation/procedure1"),
        PROCEDURE2("procedure2", "http://example.plugin.explanation/procedure2"),
        TESTPUBLISH("testpublish", "http://example.plugin.explanation/testpublish"),
        ECHO("echo", "http://example.plugin.explanation/echo"),
        GET("get", "http://kadecot.sonycsl.com/plugin/gotapi/get"), ;

        private final String mUri;
        private final String mServiceName;
        private final String mDescription;

        /**
         * @param servicename
         * @param description is displayed on JSONP called /v
         */
        Procedure(String servicename, String description) {
            mUri = PRE_FIX + PROCEDURE + servicename;
            mServiceName = servicename;
            mDescription = description;
        }

        public String getUri() {
            return mUri;
        }

        public String getServiceName() {
            return mServiceName;
        }

        public String getDescription() {
            return mDescription;
        }

        public static Procedure getEnum(String procedure) {
            for (Procedure p : Procedure.values()) {
                if (p.getUri().equals(procedure)) {
                    return p;
                }
            }
            return null;
        }
    }

    public GotAPIClient(Context context) {
        mHandler = new Handler();
        mContext = context;
        mDeviceTypeDict = new HashMap<>();
    }

    /**
     * Get the topics this plug-in want to SUBSCRIBE <br>
     */
    @Override
    public Set<String> getTopicsToSubscribe() {
        return new HashSet<String>();
    }

    /**
     * Get the procedures this plug-in supported. <br>
     */
    @Override
    public Map<String, String> getRegisterableProcedures() {
        Map<String, String> procs = new HashMap<String, String>();
        for (Procedure p : Procedure.values()) {
            procs.put(p.getUri(), p.getDescription());
        }
        return procs;
    }

    /**
     * Get the topics this plug-in supported. <br>
     */
    @Override
    public Map<String, String> getSubscribableTopics() {
        return new HashMap<String, String>();
    }

    private void searchDevice() {
        /**
         * Call after finding device.
         */
        (new Thread(new Runnable() {
            @Override
            public void run() {
                if(isGotAPIAvailable(LOCALHOST, GOT_API_PORT)) {
                    registerDevice(new DeviceData.Builder(PROTOCOL_NAME, "gotapi", DEVICE_TYPE_DEVICE_CONNECT,
                            "GotAPI", true, LOCALHOST).build());

                    searchGotAPIDevice(LOCALHOST, GOT_API_PORT);
                    if(AccessTokenPreference.getAccessToken(mContext) == null) {
                        AuthProcesser.asyncAuthorize(LOCALHOST, GOT_API_PORT, false, "com.sonycsl.Kadecot.plugin.gotapi", "GotAPIPlugin", scopes, mAuthHandler);
                    }
                }
            }
        })).start();
    }

    @Override
    public void onSearchEvent(WampEventMessage eventMsg) {
        searchDevice();
    }

    @Override
    protected void onInvocation(final int requestId, String procedure, final String uuid,
                                final JSONObject argumentsKw, final WampCallee.WampInvocationReplyListener listener) {

        try {
            final Procedure proc = Procedure.getEnum(procedure);

            if(proc == Procedure.GET) {
                String url = argumentsKw.getString("url");
                String result = callGetProcedure(Uri.parse(url));
                listener.replyYield(WampMessageFactory.createYield(requestId, new JSONObject(),
                        new JSONArray(),
                        new JSONObject(result))
                        .asYieldMessage());
                return;
            }
            if (proc == Procedure.ECHO) {
                listener.replyYield(WampMessageFactory.createYield(requestId, new JSONObject(),
                        new JSONArray(),
                        new JSONObject().put("text", argumentsKw.getString("text")))
                        .asYieldMessage());
                return;
            }

            if (proc == Procedure.TESTPUBLISH) {
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        sendPublish(uuid, DELAY_PUBLISH_TOPIC, new JSONArray(),
                                new JSONObject());
                    }
                }, 5000);

                listener.replyYield(WampMessageFactory.createYield(requestId, new JSONObject(),
                        new JSONArray(),
                        new JSONObject().put("result", "Do Publish after 5s"))
                        .asYieldMessage());
                return;
            }

            /**
             * Return YIELD message as a result of INVOCATION.
             */
            JSONObject argumentKw = new JSONObject().put("targetDevice", uuid).put(
                    "calledProcedure", procedure);

            listener.replyYield(WampMessageFactory.createYield(requestId, new JSONObject(),
                    new JSONArray(), argumentKw).asYieldMessage());
        } catch (Exception e) {
            listener.replyError(WampMessageFactory
                    .createError(WampMessageType.INVOCATION, requestId,
                            new JSONObject(), WampError.INVALID_ARGUMENT, new JSONArray(),
                            new JSONObject()).asErrorMessage());
        }
    }

    protected boolean isGotAPIAvailable(String host, int port) {
        try {
            String url = "http://"+host+":"+port+"/gotapi/system";
            HttpGet request = new HttpGet(url);
            DefaultHttpClient client = new DefaultHttpClient();
            request.addHeader(DConnectMessage.HEADER_GOTAPI_ORIGIN, "com.sonycsl.Kadecot.plugin.gotapi");
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                try {
                    JSONObject root = new JSONObject(result);
                    JSONArray plugins = root.getJSONArray("plugins");
                    System.out.println("plugins:"+plugins.toString());
                    for(int i = 0; i < plugins.length(); i++) {
                        JSONObject plugin = plugins.getJSONObject(i);
                        String id = plugin.getString("id");
                        String packageName = plugin.getString("packageName");
                        System.out.print("id:"+id);
                        System.out.print(", packageName:"+packageName);
                        System.out.println();
                        mDeviceTypeDict.put(id, packageName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void searchGotAPIDevice(String host, int port) {
        DConnectMessage message = new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
        String accessToken = AccessTokenPreference.getAccessToken(mContext);
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http");
            builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
            builder.setHost(host);
            builder.setPort(port);
            builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
            HttpUriRequest request = new HttpGet(builder.build());
            request.addHeader(DConnectMessage.HEADER_GOTAPI_ORIGIN, "com.sonycsl.Kadecot.plugin.gotapi");
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(request);
            message = (new HttpMessageFactory()).newDConnectMessage(response);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (message == null) {

            AccessTokenPreference.setAccessToken(mContext, null);
            return;
        }
        int result = message.getInt(DConnectMessage.EXTRA_RESULT);
        if (result == DConnectMessage.RESULT_ERROR) {

            AccessTokenPreference.setAccessToken(mContext, null);
            return;
        }
        List<Object> services
                = message.getList(
                ServiceDiscoveryProfileConstants.PARAM_SERVICES);
        if (services != null) {
            for (Object object: services) {
                @SuppressWarnings("unchecked")
                Map<String, Object> service = (Map<String, Object>) object;
                String id = service.get(ServiceDiscoveryProfileConstants.PARAM_ID).toString();
                String name = service.get(ServiceDiscoveryProfileConstants.PARAM_NAME).toString();
//                String protocolId = id.substring(id.indexOf('.') + 1);
                int end = id.lastIndexOf(".localhost.deviceconnect.org");
                int start = id.substring(0,end).lastIndexOf('.')+1;
                String protocolId = id.substring(start);
                String deviceType = mDeviceTypeDict.get(protocolId);
                if(mContext.getPackageName().equals(deviceType)) {
                    continue;
                }
                System.out.print("id:"+id);
                System.out.print(", deviceType:"+deviceType);
                System.out.print(", protocolId:"+protocolId);
                System.out.print(", name:"+name);
                System.out.println();
                registerDevice(new DeviceData.Builder(PROTOCOL_NAME, id, deviceType,
                        name, true, LOCALHOST).build());
            }
        }
    }

    protected String callGetProcedure(Uri original) throws URISyntaxException, IOException {
        DConnectMessage message = new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
        URIBuilder uriBuilder = new URIBuilder();
//        uriBuilder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
        uriBuilder.setPath(original.getPath());
        uriBuilder.setScheme("http");
        uriBuilder.setHost("localhost");
        uriBuilder.setPort(4035);
//        uriBuilder.addParameter(DConnectMessage.EXTRA_SERVICE_ID,
//                devices.get(0).getId());
        uriBuilder.addParameter(DConnectMessage.EXTRA_SERVICE_ID, original.getQueryParameter(DConnectMessage.EXTRA_SERVICE_ID));

        uriBuilder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, AccessTokenPreference.getAccessToken(mContext));

        HttpUriRequest req = new HttpGet(uriBuilder.build());
        req.addHeader("origin", mContext.getPackageName());
        HttpClient client = new DefaultHttpClient();
        HttpResponse res = client.execute(req);
        final String result = EntityUtils.toString(res.getEntity(), "UTF-8");
        return result;
    }
}
