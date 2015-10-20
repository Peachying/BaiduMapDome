package com.skyfin.baidumapdome.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.skyfin.baidumapdome.R;
import com.skyfin.baidumapdome.adapter.ItemAdapter;
import com.skyfin.baidumapdome.app.LocationApplication;
import com.skyfin.baidumapdome.bean.LocationBean;
import com.skyfin.baidumapdome.bean.MyLocation;
import com.skyfin.baidumapdome.http.PoiClient;
import com.skyfin.baidumapdome.util.Util;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 主地图页面的HomeFragment
 */
public class HomeFragment extends Fragment implements BaiduMap.OnMapLongClickListener, BaiduMap.OnMarkerClickListener {

    View mHomeFragmentView = null;
    //百度地图层
    RelativeLayout mBaidumap_layout = null;
    //搜索布局层
    RelativeLayout mSearch_layout = null;
    //百度地图层实体
    BaiduMap mBaiduMap = null;
    //百度地图控件布局
    MapView mMapView = null;
    //自己标记的poi点的搜索布局
    RecyclerView mRecyclerView = null;
    LinearLayoutManager mLinearLayoutManager = null;
    ItemAdapter mAdapter = null;
    //标注的POI数据
    List<LocationBean> mdata = new ArrayList<>();
    //toolbar上面的搜索控件
    SearchView mSearchView;
    //搜索控件的MenuItem,控制searchview的展开和收起
    MenuItem searchviewItem;
    //定位的view
    ImageView locatedIvw = null;
    //资源初始化
    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_marka);
    BitmapDescriptor bdend = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_en);
    BitmapDescriptor bdstart = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_st);
    BitmapDescriptor bdpt = BitmapDescriptorFactory
            .fromResource(R.drawable.sns_shoot_location_pressed);
    LatLng mCenterlLatLng = new LatLng(31.541756, 104.700008);
    //定位的client
    private LocationClient mLocationClient;
    //定位服务state
    private boolean mLocationState = false;
    /*
    LocationMode.FOLLOWING;  跟随
    LocationMode.NORMAL;  普通
    LocationMode.COMPASS; 罗盘
     */
    //定位模式
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;

    //POI搜索的实体
    PoiSearch mPoiSearch;
    //布局管理器
    LayoutInflater mLayoutInflater = null;
    //POI搜索之后的数据
    List<PoiInfo> mSearchPoiInfos = new ArrayList<>();
    //导航的坐标
    LatLng startLatLng;
    LatLng endLatLng;
    //PoiNearbySearchOption 搜索结果
    PoiResult mPoiResult = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mHomeFragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        //初始化 POI搜索
        mPoiSearch = PoiSearch.newInstance();
        mLocationClient = ((LocationApplication) getActivity().getApplication()).mLocationClient;
        //初始化布局
        initLayout(mHomeFragmentView);
        //初始化数据
        initData();
        //设置fragment的menuitem可以监听fragment的
        setHasOptionsMenu(true);
        initView(mHomeFragmentView);
        //初始化布局管理器
        mLayoutInflater = inflater;
        return mHomeFragmentView;
    }

    private void initView(View view) {
        mMapView = (MapView) view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.mRecycleView);
        mLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new ItemAdapter(getActivity().getApplicationContext(), mdata);
        mAdapter.setOnItemClickListener(new ItemAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, String data) {
                Gson gson = new Gson();
                LocationBean pBean = gson.fromJson(data, LocationBean.class);
                LatLng searchLocation = new LatLng(pBean.location.get(1), pBean.location.get(0));
                MoveToCenter(searchLocation);
                OverlayOptions overlayOptions = new MarkerOptions().position(searchLocation).icon(bdA);
                mBaiduMap.clear();
                mBaiduMap.addOverlay(overlayOptions);

                mSearch_layout.setVisibility(View.INVISIBLE);
                mBaidumap_layout.setVisibility(View.VISIBLE);
                searchviewItem.collapseActionView();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        locatedIvw = (ImageView) view.findViewById(R.id.locatedIvw);
        locatedIvw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLocation();
                if (!mLocationState) {
                    mLocationState = true;
                    mLocationClient.start();//定位SDK start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
                    ((LocationApplication) getActivity().getApplication()).setStringBuffer(new LocationApplication.OnStringBuffer() {
                        @Override
                        public void buffer(MyLocation location) {
                            mBaiduMap.setMyLocationEnabled(true);
                            // 构造定位数据
                            MyLocationData locData = new MyLocationData.Builder()
                                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                                    .direction(100).latitude(location.getLatitude())
                                    .longitude(location.getLongitude()).build();
                            // 设置定位数据
                            mBaiduMap.setMyLocationData(locData);
                            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
                            //mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
                            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, null);
                            mBaiduMap.setMyLocationConfigeration(config);
                        }
                    });
                } else {
                    mLocationClient.stop();
                    mLocationState = false;
                }
            }
        });
        MoveToCenter(mCenterlLatLng);
        mBaiduMap.setOnMapLongClickListener(this);
    }

    private void initLayout(View view) {
        mBaidumap_layout = (RelativeLayout) view.findViewById(R.id.baidumap_layout);
        mSearch_layout = (RelativeLayout) view.findViewById(R.id.search_layout);
        mSearch_layout.setVisibility(View.INVISIBLE);
    }


    /*
    *初始POI的数据
     */
    private void initData() {
        cleanStartAndEndLatLng();
        RequestParams params = new RequestParams();
        params.add("geotable_id", "120320");
        params.add("ak", "hrYYul3RhKkq25YgkGG1c5yz");
        params.add("page_size", "200");
        PoiClient.get("", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int state, Header[] headers, byte[] bytes) {
                if (state == 200) {
                    String mJsonString = new String(bytes);
                    //字节转码
                    mJsonString = Util.decodeUnicode(mJsonString);
                    Gson gson = new Gson();
                    try {
                        JSONObject jsonObject = new JSONObject(mJsonString);
                        //获取到pois节点的数据
                        JSONArray jsonArray = jsonObject.getJSONArray("pois");
                        mJsonString = jsonArray.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    List<LocationBean> pdata;
                    Type type = new TypeToken<ArrayList<LocationBean>>() {
                    }.getType();
                    pdata = gson.fromJson(mJsonString, type);
                    for (LocationBean mBean : pdata) {
                        mdata.add(mBean);
                    }
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在Fragment执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在Fragment执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在Fragment执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        searchviewItem = menu.findItem(R.id.action_search);

        mSearchView = (SearchView) MenuItemCompat.getActionView(searchviewItem);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                PoiNearbySearchOption poiNearbySearchOption = new PoiNearbySearchOption();
                poiNearbySearchOption.location(mCenterlLatLng);
                poiNearbySearchOption.keyword(query);
                poiNearbySearchOption.radius(1000);
                mPoiSearch.searchNearby(poiNearbySearchOption);
                mPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
                    @Override
                    public void onGetPoiResult(PoiResult poiResult) {

                        searchviewItem.collapseActionView();
                        mSearch_layout.setVisibility(View.INVISIBLE);
                        mBaidumap_layout.setVisibility(View.VISIBLE);
                        if (poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                            Snackbar.make(mHomeFragmentView, "没有搜索到你想要的数据", Snackbar.LENGTH_SHORT).show();
                        } else {
                            mPoiResult = poiResult;
                            mSearchPoiInfos = poiResult.getAllPoi();
                            ShowStartAndEndLatLng();

                        }
                    }
                    @Override
                    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                    }
                });
                MoveToCenter(mCenterlLatLng);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_switch) {
            mBaiduMap.setMapType(mBaiduMap.getMapType() == 2 ? 1 : 2);
        }
        if (id == R.id.action_search) {
            MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    mSearch_layout.setVisibility(View.VISIBLE);
                    mBaidumap_layout.setVisibility(View.INVISIBLE);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    mSearch_layout.setVisibility(View.INVISIBLE);
                    mBaidumap_layout.setVisibility(View.VISIBLE);
                    return true;
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
    /*
    *初始化地图中心的坐标位置
     */

    private void MoveToCenter(LatLng latLng) {
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(latLng)
                .zoom(18)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        // mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(15).build()));
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setPriority(LocationClientOption.GpsOnly);
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setEnableSimulateGps(true);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {

        //AddOverlay(latLng);
        final View view = mLayoutInflater.inflate(R.layout.long_pop_layout, null);
        Button selectButton = (Button) view.findViewById(R.id.select);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.clear();
                AddOverlay(latLng, 1);
            }
        });

        Button startButton = (Button) view.findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.clear();
                AddOverlay(latLng, 2);
            }
        });
        Button endButton = (Button) view.findViewById(R.id.end);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.clear();
                AddOverlay(latLng, 3);
            }
        });

        InfoWindow infoWindow = new InfoWindow(view, latLng, -47);
        mBaiduMap.showInfoWindow(infoWindow);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    private class MyPoiOverlay extends PoiOverlay {
        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            if (mSearchPoiInfos.size() > 0) {
                PoiInfo mPoiInfo = mSearchPoiInfos.get(index);
                final View view = mLayoutInflater.inflate(R.layout.pop_hint_layout, null);
                TextView textViewtitle = (TextView) view.findViewById(R.id.title);
                TextView textViewaddr = (TextView) view.findViewById(R.id.addr);
                TextView textViewphone = (TextView) view.findViewById(R.id.phone);
                textViewtitle.setText(mPoiInfo.name);
                textViewaddr.setText(mPoiInfo.address);
                if (!mPoiInfo.phoneNum.equals("")) {
                    textViewphone.setText(mPoiInfo.phoneNum);
                }
                InfoWindow infoWindow = new InfoWindow(view, mPoiInfo.location, -47);
                mBaiduMap.showInfoWindow(infoWindow);

                final Snackbar snackbar = Snackbar.make(mHomeFragmentView,
                        mPoiInfo.name + "\n" + mPoiInfo.address + "\n" + mPoiInfo.phoneNum,
                        Snackbar.LENGTH_SHORT);
                snackbar.setAction("关闭",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        });
                snackbar.show();

            }
            return true;
        }
    }

    private void AddOverlay(LatLng latLng, int index) {
        switch (index) {
            case 1:
                break;
            case 2:
                startLatLng = latLng;
                break;
            case 3:
                endLatLng = latLng;
                break;
        }
        ShowStartAndEndLatLng();
    }

    private void cleanStartAndEndLatLng() {
        startLatLng = new LatLng(0, 0);
        endLatLng = new LatLng(0, 0);
    }

    private void ShowStartAndEndLatLng() {
        mBaiduMap.clear();
        if (endLatLng.latitude != 0 && endLatLng.longitude != 0) {
            OverlayOptions overlayOptions = new MarkerOptions().position(endLatLng).icon(bdend);
            mBaiduMap.addOverlay(overlayOptions);
        }
        if (startLatLng.latitude != 0 && startLatLng.longitude != 0) {
            OverlayOptions overlayOptions = new MarkerOptions().position(startLatLng).icon(bdstart);
            mBaiduMap.addOverlay(overlayOptions);
        }
        if (mPoiResult != null) {
            //创建PoiOverlay
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            //设置overlay可以处理标注点击事件
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(mPoiResult); //设置PoiOverlay数据
            overlay.addToMap(); //添加PoiOverlay到地图中
            overlay.zoomToSpan();
        }
    }
}
