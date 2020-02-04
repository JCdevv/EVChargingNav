package uk.ac.mmu.advprog.kotlinmaps

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var currentLoc : LatLng
    private lateinit var destLoc : LatLng
    private lateinit var mMap : GoogleMap
    private lateinit var directionsApiClient : DirectionsApiClient
    private var API_KEY = ""
    var options = PolylineOptions()


    var onStreet = false
    var isFree = false
    var onePhase = false
    var threePhase = false
    var dc = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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

        directionsApiClient = DirectionsApiClient(
            apiKey = API_KEY,
            logHttp = true)


        return v

    }



    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        var context : Context = activity!!.applicationContext
        var db = DatabaseHelper(context)

        onStreet = Data.onstreet
        isFree = Data.isfree
        onePhase = Data.onephase
        threePhase = Data.triplephase
        dc = Data.dc
        var locationmarkers = ArrayList<uk.ac.mmu.advprog.kotlinmaps.Location>()


        println(onStreet.toString() + isFree.toString())
        println(onePhase.toString())

        if(onePhase){
            locationmarkers = db.getSinglePhase(onStreet,isFree)
        }
        else if(threePhase){
            locationmarkers = db.getTriplePhase(onStreet,isFree)
        }
        else if(dc){
            locationmarkers = db.getDC(onStreet,isFree)
        }
        else{
            locationmarkers = db.getLocations(onStreet,isFree)
        }


        var markerLocation = Location("")



        for(item in locationmarkers){

            markerLocation.longitude = item.longitude.toDouble()
            markerLocation.latitude = item.latitude.toDouble()


                var latlng = LatLng(java.lang.Double.parseDouble(item.latitude), java.lang.Double.parseDouble(item.longitude))
                mMap.addMarker(MarkerOptions().position(latlng).title(item.locationname))

        }

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        options.color(Color.RED)
        options.width(5f)

        setUpMap(mMap)

    }


/*
    fun buildUrl(trackPoints : List<LatLng>) : String{
        var url = StringBuilder()
        url.append("https://roads.googleapis.com/v1/snapToRoads?path=")

        for(item in trackPoints){
            url.append(String.format("%8.5f",item.latitude))
            url.append(",")
            url.append(String.format("%8.5f",item.longitude))
            url.append("|")
        }

        url.delete(url.length -1, url.length)
        url.append("&interpolate=true")
        url.append(String.format("&key=%s",API_KEY))

        println(url.toString())
        return url.toString()
    }


    inner class getPoints : AsyncTask<List<LatLng>, Void?, List<LatLng>>() {

        override fun doInBackground(vararg p0: List<LatLng>): List<LatLng> {
            val snappedPoints: MutableList<LatLng> = ArrayList()
            var connection: HttpURLConnection? = null
            var reader: BufferedReader? = null
            try {
                val url = URL(buildUrl(p0[0]))
                connection = url.openConnection() as HttpURLConnection
                connection.setRequestMethod("GET")
                connection.connect()
                val stream: InputStream = connection.getInputStream()
                reader = BufferedReader(InputStreamReader(stream))
                val jsonStringBuilder = java.lang.StringBuilder()
                val buffer = StringBuffer()
                var line : String? = ""
                while (reader.readLine().also({ line = it }) != null) {
                    buffer.append(line + "\n")
                    jsonStringBuilder.append(line)
                    jsonStringBuilder.append("\n")
                }
                val jsonObject = JSONObject(jsonStringBuilder.toString())
                val snappedPointsArr = jsonObject.getJSONArray("snappedPoints")
                for (i in 0 until snappedPointsArr.length()) {
                    val snappedPointLocation =
                        (snappedPointsArr[i] as JSONObject).getJSONObject("location")
                    val lattitude = snappedPointLocation.getDouble("latitude")
                    val longitude = snappedPointLocation.getDouble("longitude")
                    snappedPoints.add(LatLng(lattitude, longitude))
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            } finally {
                if (connection != null) {
                    connection.disconnect()
                }
                try {
                    if (reader != null) {
                        reader.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return snappedPoints
        }

        override fun onPostExecute(result: List<LatLng>) {
            super.onPostExecute(result)
            val polyLineOptions = PolylineOptions()
            polyLineOptions.addAll(result)
            polyLineOptions.width(5f)
            polyLineOptions.color(Color.RED)
            mMap.addPolyline(polyLineOptions)
            val builder = LatLngBounds.Builder()
            builder.include(result[0])
            builder.include(result[result.size - 1])
            val bounds = builder.build()
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10))
        }


    }*/



    /*private val mutableList: MutableList<Polyline> = mutableListOf()

    fun requestRoute(from : LatLng, to: LatLng){

        mutableList.forEach { it.remove() }
        mutableList.clear()

        val transitOptions = TransitOptions(mode = MODE.DRIVING)

        var points: MutableList<LatLng> = ArrayList()

        directionsApiClient.getRoutePolylines(origin = from, dest = to, options = transitOptions){



            it.forEach{

                

                for (point in it.points) {
                    var currentPoint = LatLng(point.latitude,point.longitude)
                    points.add(currentPoint)
                }



                /*
                Thread(Runnable {
                    this.activity!!.runOnUiThread{

                        mutableList.add(mMap.addPolyline(it))
                    }
                }).start()*/
            }

            var getP = getPoints()
            getP.execute(points)
        }


    }*/




    inner class getPoints : AsyncTask<Void, Void?, PolylineOptions>() {

        override fun doInBackground(vararg p0: Void): PolylineOptions {



            val queue = Volley.newRequestQueue(activity!!.applicationContext)


            val url =
                "https://maps.googleapis.com/maps/api/directions/json?origin=" + currentLoc.latitude + "," + currentLoc.longitude + "&destination=" + destLoc.latitude + "," + destLoc.longitude + "&key=${API_KEY}"

            println(url)


                val stringRequest = StringRequest(Request.Method.GET, url,
                    Response.Listener<String> { response ->



                        var directions: JSONObject

                        try {
                            directions = JSONObject(response)
                            var routes = directions.getJSONArray("routes")
                            var coordinates = arrayListOf<LatLng>()

                            for (i in 0..routes.length()-1) {
                                var routesObj = routes.getJSONObject(i)
                                var legsArr = routesObj.getJSONArray("legs")

                                for (j in 0..legsArr.length()-1) {
                                    var legsObj = legsArr.getJSONObject(j)
                                    var steps = legsObj.getJSONArray("steps")




                                    for (k in 0..steps.length()-1) {
                                        var stepsObj = steps.getJSONObject(k)
                                        var polylineObj = stepsObj.getJSONObject("polyline")
                                        coordinates.addAll(PolyUtil.decode(polylineObj.getString("points")))
                                    }
                                }



                                for (item in coordinates) {
                                    var current = LatLng(item.latitude, item.longitude)
                                    options.add(current)
                                }




                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        it.printStackTrace()
                    })


            queue.add(stringRequest)

            return options

        }

        override fun onPostExecute(result: PolylineOptions?) {
            super.onPostExecute(result)

            mMap.addPolyline(result)


        }

    }


    override fun onMarkerClick(p0: Marker?): Boolean {

        fusedLocationClient.lastLocation.addOnSuccessListener(MainActivity()) { location ->
            var current = LatLng(location.latitude,location.longitude)
            currentLoc = current

            var destination = LatLng(p0!!.position.latitude,p0!!.position.longitude)
            destLoc = destination


        }

        var getP = getPoints()
        getP.execute()


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
