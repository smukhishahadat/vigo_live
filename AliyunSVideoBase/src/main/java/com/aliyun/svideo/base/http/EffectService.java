/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.base.http;

import android.text.TextUtils;
import android.util.Log;

import com.aliyun.jasonparse.JSONSupportImpl;
import com.aliyun.qupaiokhttp.BaseHttpRequestCallback;
import com.aliyun.qupaiokhttp.HttpRequest;
import com.aliyun.qupaiokhttp.RequestParams;
import com.aliyun.qupaiokhttp.StringHttpRequestCallback;
import com.aliyun.svideo.base.Form.FontForm;
import com.aliyun.svideo.base.Form.IMVForm;
import com.aliyun.svideo.base.Form.PasterForm;
import com.aliyun.svideo.base.Form.ResourceForm;
import com.aliyun.svideo.sdk.external.struct.form.PreviewPasterForm;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 尊敬的客户，此Server服务只用于demo演示使用，无法作为商用服务上线使用，我们不能保证这个服务的稳定性，高并发性，请自行搭建自己的Server服务，
 * 如何集成自己的Server服务详见文档：https://help.aliyun.com/document_detail/108783.html?spm=a2c4g.11186623.6.1075.a70a3a4895Qysq。
 */
public class EffectService {
    private Gson mGson = new GsonBuilder().disableHtmlEscaping().create();
    private static final String KEY_PACKAGE_NAME = "PACKAGE_NAME";

    public static final int EFFECT_TEXT = 1;        //字体
    public static final int EFFECT_PASTER = 2;      //动图
    public static final int EFFECT_MV = 3;          //MV
    public static final int EFFECT_FILTER = 4;      //滤镜
    public static final int EFFECT_MUSIC = 5;       //音乐
    public static final int EFFECT_CAPTION = 6;     //字幕
    public static final int EFFECT_FACE_PASTER = 7; //人脸动图
    public static final int EFFECT_IMG = 8;         //静态贴纸
    /**
     * 素材分发服务为官方demo演示使用，无法达到商业化使用程度。请自行搭建相关的服务
     */
    public static final String BASE_URL = "https://alivc-demo.aliyuncs.com";
    public static final String APP_BASE_URL = "http://unglii.com/API/index.php";
    /**
     * 素材分发服务为官方demo演示使用，无法达到商业化使用程度。请自行搭建相关的服务
     */
    public void loadEffectPaster(String packageName, final HttpCallback<List<ResourceForm>> callback) {
        String url = new StringBuilder(BASE_URL).append("/resource/getPasterInfo").toString();
        RequestParams params = new RequestParams();
        params.addFormDataPart("type", 2);
        params.addFormDataPart(KEY_PACKAGE_NAME, packageName );
        get(url, params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    List<ResourceForm> resourceList = mGson.fromJson(jsonArray.toString(), new TypeToken<List<ResourceForm>>() {
                    } .getType());

                    if (callback != null) {
                        callback.onSuccess(resourceList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }

            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        });

    }
    /**
     * 素材分发服务为官方demo演示使用，无法达到商业化使用程度。请自行搭建相关的服务
     */
    public void getPasterListById(String packageName, int id, final HttpCallback<List<PasterForm>> callback) {
        StringBuilder resUrl = new StringBuilder();
        resUrl.append(BASE_URL).append("/resource/getPasterList");
        RequestParams params = new RequestParams();
        params.addFormDataPart("type", 2);
        params.addFormDataPart(KEY_PACKAGE_NAME, packageName );
        params.addFormDataPart("pasterId", id);
        get(resUrl.toString(), params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    List<PasterForm> resourceList = mGson.fromJson(jsonArray.toString(), new TypeToken<List<PasterForm>>() {
                    } .getType());
                    if (callback != null) {
                        callback.onSuccess(resourceList);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }

            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        } );
    }
    /**
     * 素材分发服务为官方demo演示使用，无法达到商业化使用程度。请自行搭建相关的服务
     */
    public void getCaptionListById(String packageName, int id, final HttpCallback<List<PasterForm>> callback) {
        StringBuilder resUrl = new StringBuilder();
        resUrl.append(BASE_URL).append("/resource/getTextPasterList");
        RequestParams params = new RequestParams();
        params.addFormDataPart("textPasterId", id);
        params.addFormDataPart(KEY_PACKAGE_NAME, packageName );
        get(resUrl.toString(), params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    List<PasterForm> resourceList = mGson.fromJson(jsonArray.toString(), new TypeToken<List<PasterForm>>() {
                    } .getType());

                    if (callback != null) {
                        callback.onSuccess(resourceList);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }

            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        } );
    }
    /**
     * 素材分发服务为官方demo演示使用，无法达到商业化使用程度。请自行搭建相关的服务
     * 通过字体id获取字体信息
     */
    public void getFontById(String packageName, int id, final HttpCallback<FontForm> callback) {
        StringBuilder resUrl = new StringBuilder();
        resUrl.append(BASE_URL).append("/resource/getFont");
        RequestParams params = new RequestParams();
        params.addFormDataPart("fontId", id);
        params.addFormDataPart(KEY_PACKAGE_NAME, packageName );
        get(resUrl.toString(), params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);

                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONObject data = jsonObject.getJSONObject("data");
                    JSONSupportImpl jsonSupport = new JSONSupportImpl();
                    FontForm fontForm = jsonSupport.readValue(data.toString(), FontForm.class);
                    if (callback != null) {
                        callback.onSuccess(fontForm);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }

            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        });
    }
    /**
     * 素材分发服务为官方demo演示使用，无法达到商业化使用程度。请自行搭建相关的服务
     * 获取前置动图
     */
    public void loadFrontEffectPaster(String packageName, final HttpCallback<List<PreviewPasterForm>> callback) {
        StringBuilder resUrl = new StringBuilder();
        resUrl.append(BASE_URL).append("/resource/getFrontPasterList");
        RequestParams params = new RequestParams();
        params.addFormDataPart(KEY_PACKAGE_NAME, packageName );
        get(resUrl.toString(), params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                JSONSupportImpl jsonSupport = new JSONSupportImpl();
                List<PreviewPasterForm> resources;
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    resources = jsonSupport.readListValue(jsonArray.toString(),
                            new TypeToken<List<PreviewPasterForm>>() {
                            } .getType());

                    if (callback != null) {
                        callback.onSuccess(resources);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        } );

    }
    /**
     * 素材分发服务为官方demo演示使用，无法达到商业化使用程度。请自行搭建相关的服务
     */
    public void loadEffectMv(String packageName,
                             final HttpCallback<List<IMVForm>> callback) {
        RequestParams params = new RequestParams();
        params.addFormDataPart(KEY_PACKAGE_NAME, packageName );
        String url = new StringBuilder(BASE_URL).append("/resource/getMv").toString();
        get(url, params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);

                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    List<IMVForm> resourceList = mGson.fromJson(jsonArray.toString(), new TypeToken<List<IMVForm>>() {
                    } .getType());
                    if (callback != null) {
                        callback.onSuccess(resourceList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        });


    }
    /**
     * 素材分发服务为官方demo演示使用，无法达到商业化使用程度。请自行搭建相关的服务
     */
    public void loadEffectCaption(String packageName, final HttpCallback<List<ResourceForm>> callback) {
        String url = new StringBuilder(BASE_URL).append("/resource/getTextPaster").toString();
        RequestParams params = new RequestParams();
        params.addFormDataPart(KEY_PACKAGE_NAME, packageName );
        get(url, params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    List<ResourceForm> resourceList = mGson.fromJson(jsonArray.toString(), new TypeToken<List<ResourceForm>>() {
                    } .getType());
                    if (callback != null) {
                        callback.onSuccess(resourceList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }

            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        });

    }

    /**
     * 素材分发服务为官方demo演示使用，无法达到商业化使用程度。请自行搭建相关的服务
     */
    public void loadingMusicData(String packageName, int pageNo, int pageSize, String keyWord, final HttpCallback<List<MusicFileBean>> callback) {

        //String url = BASE_URL + "/music/getRecommendMusic";
        String url = APP_BASE_URL + "";
        RequestParams params = new RequestParams();
       // params.addFormDataPart("pageNo", pageNo);
       // params.addFormDataPart("pageSize", pageSize);
        params.addFormDataPart("p", "allSounds2");
        params.addFormDataPart(KEY_PACKAGE_NAME, packageName );

        if (!TextUtils.isEmpty(keyWord)) {
            params.addFormDataPart("keyWords", keyWord);
        }

        Log.e("params+_+_", String.valueOf(params));

        get(url, params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    JSONObject jsonObject = new JSONObject(s);

                    Log.e("jsonObject___", String.valueOf(jsonObject));

                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    JSONArray jsonArray = dataObject.getJSONArray("musicList");
                    List<MusicBean> resourceList = mGson.fromJson(jsonArray.toString(), new TypeToken<List<MusicBean>>() {
                    } .getType());
                    Log.e("callback___", String.valueOf(callback));
                    if (callback != null) {
                        List<MusicFileBean> musicFileBeanList = new ArrayList<>();
                        for (MusicBean musicBean : resourceList) {
                            musicFileBeanList.add(new MusicFileBean(
                                    musicBean.getTitle(),
                                    musicBean.getArtistName(),
                                    musicBean.getMusicId(),
                                    musicBean.getImage(),
                                    musicBean.getSource()
                            ));
                        }

                        callback.onSuccess(musicFileBeanList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                Log.e("error___", msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        });
    }
    /**
     * 素材分发服务为官方demo演示使用，无法达到商业化使用程度。请自行搭建相关的服务
     */
    public void getMusicDownloadUrlById(String packageName, String musicId, final HttpCallback<String> callback) {
        String url = BASE_URL + "/music/getPlayPath";
        final RequestParams params = new RequestParams();
        params.addFormDataPart("musicId", musicId);
        params.addFormDataPart(KEY_PACKAGE_NAME, packageName );
        get(url, params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    String playPath = dataObject.getString("playPath");
                    if (callback != null) {
                        callback.onSuccess(playPath);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if (callback != null) {
                    callback.onFailure(new Throwable(msg));
                }
            }
        });

    }

    private void get(String url, RequestParams params, BaseHttpRequestCallback callback) {
        if (sAppInfo != null) {
            Set<Map.Entry<String, String>> entries = sAppInfo.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                params.addHeader(entry.getKey(), entry.getValue());
            }

        }
        HttpRequest.get(url, params, callback);
    }

    private static Map<String, String> sAppInfo;

    /**
     * 设置app信息
     * @param appName 应用名
     * @param packageName 应用包名
     * @param versionName 版本名
     * @param versionCode 版本号
     */
    public static void setAppInfo(String appName, String packageName, String versionName, long versionCode) {
        if (sAppInfo == null) {
            sAppInfo = new HashMap<>();
            try {
                sAppInfo.put("appName", URLEncoder.encode(appName, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            sAppInfo.put("packageName", packageName);
            sAppInfo.put("appVersionName", versionName);
            sAppInfo.put("appVersionCode", String.valueOf(versionCode));
        }
    }


}

