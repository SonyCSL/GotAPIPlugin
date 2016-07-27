package com.sonycsl.Kadecot.plugin.gotapi;

import android.content.Context;
import android.os.Handler;

import com.sonycsl.Kadecot.plugin.DeviceData;
import com.sonycsl.Kadecot.plugin.KadecotProtocolClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.role.WampCallee;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.basic.message.DConnectResponseMessage;
import org.deviceconnect.message.http.impl.factory.HttpMessageFactory;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.AvailabilityProfileConstants;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.deviceconnect.profile.CanvasProfileConstants;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    private static final String GOT_API_SYSTEM_UUID = "gotapi";

    private static final String DELAY_PUBLISH_TOPIC = PRE_FIX + TOPIC + "delaypublish";

    private static final String ARGS_KEY_ATTRIBUTE = "attribute";
    private static final String ARGS_KEY_INTERFACE = "interface";

    private Context mContext;
    private Handler mHandler;

    private static final int GOT_API_PORT = 4035;

    private HashMap<String, String> mDeviceTypeDict;

    private final String[] scopes = {
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
            ServiceDiscoveryProfileConstants.PROFILE_NAME,
            VibrationProfileConstants.PROFILE_NAME,
            AvailabilityProfileConstants.PROFILE_NAME,
            CanvasProfileConstants.PROFILE_NAME,

            // 独自プロファイル
            "light",
            "camera",
            "temperature",
            "dice",
            "sphero",
            "drive_controller",
            "remote_controller",
            "health",
            "airconditioner",
            "canvas",
            "keyevent",
            "touch",

    };

    private final String HTTP_METHOD_GET = "get";
    private final String HTTP_METHOD_POST = "post";
    private final String HTTP_METHOD_DELETE = "delete";
    private final String HTTP_METHOD_PUT = "put";

    private final String[] httpMethods = new String[] {
            HTTP_METHOD_GET,
            HTTP_METHOD_POST,
            HTTP_METHOD_DELETE,
            HTTP_METHOD_PUT,
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
        for (String profile : scopes) {
            for(String m : httpMethods) {
                String uri = PRE_FIX + PROCEDURE + profile + "." + m;
                String description = "http://kadecot.sonycsl.com/plugin/gotapi/" + profile + "/" + m;
                procs.put(uri, description);
            }
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
                    registerDevice(new DeviceData.Builder(PROTOCOL_NAME, GOT_API_SYSTEM_UUID, DEVICE_TYPE_DEVICE_CONNECT,
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
            String serviceId = uuid.substring(uuid.lastIndexOf(":")+1);
            if(GOT_API_SYSTEM_UUID.equals(uuid)) {
                serviceId = null;
            }
            String procedureName = procedure.substring((PRE_FIX+PROCEDURE).length());
            int d = procedureName.lastIndexOf(".");
            final String profile = procedureName.substring(0, d);
            final String httpMethod = procedureName.substring(d + 1);



            String result = callGotAPI(serviceId, profile, argumentsKw, httpMethod);
            listener.replyYield(WampMessageFactory.createYield(requestId, new JSONObject(),
                    new JSONArray(),
                    new JSONObject(result))
                    .asYieldMessage());

        } catch (Exception e) {
            e.printStackTrace();
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
                System.out.print("id:"+id);
                System.out.print(", deviceType:"+deviceType);
                System.out.print(", protocolId:"+protocolId);
                System.out.print(", name:"+name);
                System.out.println();
                if(mContext.getPackageName().equals(deviceType)) {
                    continue;
                }
                registerDevice(new DeviceData.Builder(PROTOCOL_NAME,
                        PROTOCOL_NAME
                                + ":" + deviceType.substring("org.deviceconnect.android.deviceplugin.".length()) // device name
                                +":"+PROTOCOL_NAME  // company id
                                +":0"   // version
                                +":"+id.replace(":","_"), // unique id
                        //id,
                        deviceType,
                        name, true, LOCALHOST).build());
            }
        }
    }

    protected String callGotAPI(String serviceId, String profile, JSONObject args, String httpMethod) throws URISyntaxException, IOException, JSONException {

        URIBuilder uriBuilder = new URIBuilder();

        uriBuilder.setScheme("http");
        uriBuilder.setHost("localhost");
        uriBuilder.setPort(4035);

        uriBuilder.setProfile(profile);

        if(serviceId != null) {
            uriBuilder.addParameter(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
        }

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();


        Iterator<String> iter = args.keys();
        while (iter.hasNext()){
            String key = iter.next();
            if(ARGS_KEY_ATTRIBUTE.equals(key)) {
                uriBuilder.setAttribute(args.getString(ARGS_KEY_ATTRIBUTE));
            } else if(ARGS_KEY_INTERFACE.equals(key)) {
                uriBuilder.setInterface(args.getString(ARGS_KEY_INTERFACE));
            } else {
                if(HTTP_METHOD_GET.equals(httpMethod) || HTTP_METHOD_DELETE.equals(httpMethod)) {
                    uriBuilder.addParameter(key, args.getString(key));
                } else {
                    params.add(new BasicNameValuePair(key, args.getString(key)));
                }
            }

        }

        if(HTTP_METHOD_GET.equals(httpMethod) || HTTP_METHOD_DELETE.equals(httpMethod)) {
            uriBuilder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, AccessTokenPreference.getAccessToken(mContext));
        } else {
            params.add(new BasicNameValuePair(DConnectMessage.EXTRA_ACCESS_TOKEN, AccessTokenPreference.getAccessToken(mContext)));
        }

        System.out.println("uri:"+uriBuilder.build());

        HttpUriRequest req = null;
        if(HTTP_METHOD_GET.equals(httpMethod)) {
            req = new HttpGet(uriBuilder.build());
        } else if(HTTP_METHOD_POST.equals(httpMethod)) {
            HttpPost post = new HttpPost(uriBuilder.build());
            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            req = post;
        } else if(HTTP_METHOD_DELETE.equals(httpMethod)) {
            req = new HttpDelete(uriBuilder.build());
        } else if(HTTP_METHOD_PUT.equals(httpMethod)) {
            HttpPut put = new HttpPut(uriBuilder.build());
            put.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            put.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            req = put;
        }
        req.addHeader("origin", mContext.getPackageName());
        HttpClient client = new DefaultHttpClient();
        HttpResponse res = client.execute(req);
        HttpEntity entity = res.getEntity();
        final String result = EntityUtils.toString(entity, "UTF-8");
        if(entity != null) {
            entity.consumeContent();
            req.abort();
        }
        return result;
    }
}
