package uk.ac.mmu.advprog.kotlinmaps

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.PolyUtil
import me.rozkmin.gmapspolyline.DirectionsApiClient
import org.json.JSONException
import org.json.JSONObject

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    internal lateinit var mMapView: MapView
    private lateinit var currentLoc: LatLng
    private lateinit var destLoc: LatLng
    private lateinit var mMap: GoogleMap
    private var API_KEY = ""

    var onStreet = false
    var isFree = false
    var onePhase = false
    var threePhase = false
    var dc = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var context: Context = activity!!.applicationContext
        var db = DatabaseHelper(context)

        val v = inflater.inflate(R.layout.mapsfragment, container, false)
        mMapView = v.findViewById(R.id.map)
        mMapView.onCreate(savedInstanceState)
        mMapView.getMapAsync(this)
        mMapView.onResume()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Context)

        return v

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        var context: Context = activity!!.applicationContext
        var db = DatabaseHelper(context)

        onStreet = Data.onstreet
        isFree = Data.isfree
        onePhase = Data.onephase
        threePhase = Data.triplephase
        dc = Data.dc
        var locationmarkers = ArrayList<uk.ac.mmu.advprog.kotlinmaps.Location>()

        if (onePhase) {
            locationmarkers = db.getSinglePhase(onStreet, isFree)
        } else if (threePhase) {
            locationmarkers = db.getTriplePhase(onStreet, isFree)
        } else if (dc) {
            locationmarkers = db.getDC(onStreet, isFree)
        } else {
            locationmarkers = db.getLocations(onStreet, isFree)
        }


        var markerLocation = Location("")


        println("HERE WE ARE99999")

        for (item in locationmarkers) {

            println("HERE WE ARE")
            println(item.locationid)

            markerLocation.longitude = item.longitude.toDouble()
            markerLocation.latitude = item.latitude.toDouble()


            var latlng = LatLng(
                java.lang.Double.parseDouble(item.latitude),
                java.lang.Double.parseDouble(item.longitude)
            )
            mMap.addMarker(MarkerOptions().position(latlng).title(item.locationname))

        }

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        setUpMap(mMap)

    }


    fun getPoints(current : LatLng, destination : LatLng){

        val queue = Volley.newRequestQueue(activity!!.applicationContext)

        val url =
            "https://maps.googleapis.com/maps/api/directions/json?origin=" + current.latitude + "," + current.longitude + "&destination=" + destination.latitude + "," + destination.longitude + "&key=${API_KEY}"

        println(url)

        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->

                var directions: JSONObject

                try {
                    directions = JSONObject(response)
                    var routes = directions.getJSONArray("routes")
                    var coordinates = arrayListOf<LatLng>()

                    for (i in 0..routes.length() - 1) {
                        var routesObj = routes.getJSONObject(i)
                        var legsArr = routesObj.getJSONArray("legs")

                        for (j in 0..legsArr.length() - 1) {
                            var legsObj = legsArr.getJSONObject(j)
                            var steps = legsObj.getJSONArray("steps")

                            for (k in 0..steps.length() - 1) {
                                var stepsObj = steps.getJSONObject(k)
                                var polylineObj = stepsObj.getJSONObject("polyline")
                                coordinates.addAll(PolyUtil.decode(polylineObj.getString("points")))
                            }
                        }

                        var options = PolylineOptions()
                        options.color(Color.RED)
                        options.width(5f)

                        for (item in coordinates) {
                            var current = LatLng(item.latitude, item.longitude)
                            options.add(current)
                        }

                        var route : Polyline = mMap.addPolyline(options)


                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                it.printStackTrace()
            })


        queue.add(stringRequest)

    }


    override fun onMarkerClick(p0: Marker?): Boolean {

        fusedLocationClient.lastLocation.addOnSuccessListener(MainActivity()) { location ->
            var current = LatLng(location.latitude,location.longitude)
            currentLoc = current

            var destination = LatLng(p0!!.position.latitude,p0!!.position.longitude)
            destLoc = destination

            getPoints(current,destination)

        }

        return true
    }

    private fun setUpMap(map : GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                activity!!.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                MainActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true


        fusedLocationClient.lastLocation.addOnSuccessListener(MainActivity()) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                currentLoc = LatLng(location.latitude,location.longitude)

                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }
}
