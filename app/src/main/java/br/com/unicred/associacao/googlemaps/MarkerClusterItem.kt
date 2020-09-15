package br.com.unicred.associacao.googlemaps

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


class MarkerClusterItem(
    private val latLng: LatLng,
    private val title: String
) : ClusterItem {
    override fun getPosition(): LatLng {
        return latLng
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getSnippet(): String? {
        return ""
    }

}