package com.melberry.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MapMarker implements ClusterItem {

    private final LatLng position;
    private String title;
    private String status;
    private Float zIndex;

    public MapMarker(Double lat, Double lng, String title, String status, Float zIndex) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.status = status;
        this.zIndex = zIndex; // used to bring clicked marker to forefront
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return status;
    }

    public Float getZIndex() {
        return zIndex;
    }
}