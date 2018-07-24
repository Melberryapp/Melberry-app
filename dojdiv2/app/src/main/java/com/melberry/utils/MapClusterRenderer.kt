package com.melberry.utils

import android.content.Context
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.melberry.R

class MapClusterRenderer(private val context: Context, map: GoogleMap, clusterManager: ClusterManager<MapMarker>) : DefaultClusterRenderer<MapMarker>(context, map, clusterManager) {

    private val clusterManager: ClusterManager<*>

    init {
        this.clusterManager = clusterManager
        minClusterSize = 5 // TODO create configuration file
        setAnimation(false)
    }

    override fun onBeforeClusterItemRendered(item: MapMarker?, markerOptions: MarkerOptions?) {
        super.onBeforeClusterItemRendered(item, markerOptions)

        markerOptions!!.icon(getMarkerIcon(item!!.snippet))
        if (item.zIndex != null) markerOptions.zIndex(item.zIndex)
    }

    override fun getClusterText(bucket: Int): String {
        return bucket.toString()
    }

    override fun getBucket(cluster: Cluster<MapMarker>): Int {
        return getFreeSpotsSize(cluster)
    }

    override fun getColor(clusterSize: Int): Int {
        return ContextCompat.getColor(context, R.color.green)
    }


    private fun getMarkerIcon(status: String): BitmapDescriptor {
        return when (ParkingSpotState.valueOf(status)) {
            ParkingSpotState.FREE -> BitmapDescriptorFactory.fromResource(R.drawable.poi_new)
            ParkingSpotState.CLICKED -> BitmapDescriptorFactory.fromResource(R.drawable.velka_poi)
            else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
        }
    }

    private fun getFreeSpotsSize(cluster: Cluster<MapMarker>): Int {
        return cluster.items.filter { it.snippet == ParkingSpotState.FREE.toString() }.size
    }

}
