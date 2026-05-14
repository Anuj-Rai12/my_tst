package com.pos10.view

import com.pos10.model.remote.GetTrackHistoryResponse
import com.pos10.model.remote.RouteCoordinate

object CoordinateManager {
    private val routeCoordinates = mutableListOf<RouteCoordinate>()

    fun setCoordinatesFromApi(data: List<GetTrackHistoryResponse.Data>) {
        routeCoordinates.clear()
        data.forEach {
            val lat = it.latitude.toDoubleOrNull()
            val lon = it.longitude.toDoubleOrNull()
            if (lat != null && lon != null) {
                routeCoordinates.add(RouteCoordinate(lat, lon))
            }
        }
    }

    fun getCoordinates(): List<RouteCoordinate> = routeCoordinates

    fun clearCoordinates() {
        routeCoordinates.clear()
    }
}
