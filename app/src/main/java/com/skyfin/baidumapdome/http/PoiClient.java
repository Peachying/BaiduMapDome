package com.skyfin.baidumapdome.http;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


public class PoiClient {
    private static final String BASE_URL =  "http://api.map.baidu.com/geodata/v3/poi/list?";
    //http://api.map.baidu.com/geodata/v3/poi/list?geotable_id=120320&ak=hrYYul3RhKkq25YgkGG1c5yz&page_size=200
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
