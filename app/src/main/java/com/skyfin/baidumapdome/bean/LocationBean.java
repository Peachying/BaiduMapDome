package com.skyfin.baidumapdome.bean;

import java.util.List;

/**
 * Created by Skyfin on 2015/10/11.
 */
public class LocationBean {
    public String title;
    public List<Double> location;
    public String city;
    public String create_time;
    public int geotable_id;
    public String address;
    public String province;
    public String district;
    public int city_id;
    public int  id;

    @Override
    public String toString() {
        return "{" +
                "address='" + address + '\'' +
                ", title='" + title + '\'' +
                ", location=" + location +
                ", city='" + city + '\'' +
                ", create_time='" + create_time + '\'' +
                ", geotable_id=" + geotable_id +
                ", province='" + province + '\'' +
                ", district='" + district + '\'' +
                ", city_id=" + city_id +
                ", id=" + id +
                '}';
    }
}
