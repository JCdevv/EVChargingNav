package uk.ac.mmu.advprog.kotlinmaps

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.text.format.DateUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Timestamp


class MainActivity : AppCompatActivity() {

    var sectionsStatePagerAdapter: SectionsStatePagerAdapter? = null
    var viewPager: ViewPager? = null
    var adapter: SectionsStatePagerAdapter? = null

    var connectors = arrayListOf<Connector>()
    var locations = arrayListOf<Location>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sectionsStatePagerAdapter = SectionsStatePagerAdapter((supportFragmentManager))
        viewPager = findViewById(R.id.container)

        var DB  = DatabaseHelper(applicationContext)
        var timestamp = DB.getTimestamp().toLong()
        var currentTime = Timestamp(System.currentTimeMillis()).toString().toLong()

        val result: CharSequence = DateUtils.getRelativeTimeSpanString(timestamp, currentTime, 0)

        println(result)

        if(result[0].equals(3)){
            //get conns
        }else{
            //continue
        }

        setupViewPager(viewPager)
    }



    //Opens requested fragment
    fun setViewPager(fragmentNumber: Int) {
        viewPager?.currentItem = fragmentNumber
    }

    //Sets up view pager, inflates fragment one
    fun setupViewPager(vp: ViewPager?) {
        adapter = SectionsStatePagerAdapter(supportFragmentManager)

        adapter?.addFragment(LoadingFragment(),"Loading Fragment")
        adapter?.addFragment(FragmentOne(), "Fragment One")


        viewPager?.adapter = adapter
    }

    inner class GetEtag : AsyncTask<Void, Int, Boolean>() {

        //ETAG on saturday 14th 642393ff884a5f2108fa0d49f762bade

        var DB  = DatabaseHelper(applicationContext)

        override fun doInBackground(vararg p0: Void?): Boolean {

            var etag = ""
            var update = false

            //Make http call
            var urlConnection: HttpURLConnection? = null
            var input: InputStream? = null

            try {
                var url = URL("https://data.gov.uk/dataset/1ce239a6-d720-4305-ab52-17793fedfac3/national-charge-point-registry")
                //Open connection
                urlConnection = url.openConnection() as HttpURLConnection
                input = BufferedInputStream(urlConnection.inputStream)

            } catch (e: IOException) {
                e.printStackTrace()
            }


            try {
                etag = urlConnection!!.getHeaderField("ETag")
                update = DB.getETag().equals(etag)

                println("ETAG IS: $etag")
                println("Current etag is: " + DB.getETag())

                if(!update){
                    DB.setETag(etag)
                }



            } catch (e: Exception) {
                e.printStackTrace()
            }

            return update
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

            if(!result){

                DB.emptyTables()

                //var getData = GetData()
                //getData.execute()

                var getCon = GetConnectors();
                getCon.execute()
            }
            else{
                viewPager!!.setCurrentItem(1)
            }
        }
    }




    inner class GetConnectors : AsyncTask<Void, Int, Boolean>() {

        override fun doInBackground(vararg p0: Void?): Boolean {



            //Make http call
            var urlConnection: HttpURLConnection? = null
            var input: InputStream? = null

            try {
                var url = URL("http://chargepoints.dft.gov.uk/api/retrieve/registry/format/json")
                //Open connection
                urlConnection = url.openConnection() as HttpURLConnection
                input = BufferedInputStream(urlConnection.inputStream)

            } catch (e: IOException) {
                e.printStackTrace()
            }

            //Convert stream into a usable string

            println(input)

            var response: String = convertStreamToString(input)

            println(response)

            try {

                // Parse json

                var counter = 0

                var obj = JSONObject(response)
                var jarry: JSONArray = obj.getJSONArray("ChargeDevice")

                var progress = 0;

                for (i in 0..(jarry.length()) -1) {
                    var deviceObject: JSONObject = jarry.get(i) as JSONObject

                    val locationid = deviceObject.get("ChargeDeviceId").toString()
                    val locationname = deviceObject.get("ChargeDeviceName").toString()

                    var connArray : JSONArray = deviceObject.get("Connector") as JSONArray

                    var markerObject: JSONObject =
                        deviceObject.get("ChargeDeviceLocation") as JSONObject

                    val latitude = markerObject.get("Latitude").toString()
                    val longitude = markerObject.get("Longitude").toString()

                    var addressObject: JSONObject = markerObject.get("Address") as JSONObject

                    val street = addressObject.get("Street").toString()
                    val postcode = addressObject.get("PostCode").toString()
                    val payment = deviceObject.get("PaymentRequiredFlag").toString()
                    val paymentdetails = deviceObject.get("PaymentDetails").toString()
                    val subscription = deviceObject.get("SubscriptionRequiredFlag").toString()
                    val subscriptiondetails = deviceObject.get("SubscriptionDetails").toString()
                    val parkingpayment = deviceObject.get("ParkingFeesFlag").toString()
                    val parkingpaymentdetails = deviceObject.get("ParkingFeesDetails").toString()
                    val onstreet = deviceObject.get("OnStreetFlag").toString()


                    for( i in 0..connArray.length()-1){


                        var connectorObject : JSONObject = connArray.get(i) as JSONObject

                        val outputkw = connectorObject.get("RatedOutputkW").toString()
                        val outputvoltage = connectorObject.get("RatedOutputVoltage").toString()
                        val outputcurrent = connectorObject.get("RatedOutputCurrent").toString()
                        val chargemethod = connectorObject.get("ChargeMethod").toString()
                        val service = connectorObject.get("ChargePointStatus").toString()


                        val con = Connector(locationid,
                            0,
                            outputkw,
                            outputvoltage,
                            outputcurrent,
                            chargemethod,
                            service)

                        connectors.add(con)

                    }

                    val loc = Location(
                        locationid,
                        locationname,
                        latitude,
                        longitude,
                        street,
                        postcode,
                        payment,
                        paymentdetails,
                        subscription,
                        subscriptiondetails,
                        parkingpayment,
                        parkingpaymentdetails,
                        onstreet
                    )

                    locations.add(loc)

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return true
        }



        override fun onPostExecute(result: Boolean) {
            insertLocations()
            insertConnectors()


            viewPager!!.setCurrentItem(1)

        }
    }

    fun insertConnectors(){

        println("Inserting Connectors...")



        var context: Context = this@MainActivity
        var db = DatabaseHelper(context)

        db.insertConnectors(connectors)

        for (i in 0..connectors!!.size - 1) {

            var con = connectors!!.get(i)
            con.connectorid = i

            // IDS:
            // 1 = Single Phase
            // 2 = DC
            // 3 = Three Phase

            when(con.chargemethod){
                "Single Phase AC" -> {
                    db.createChargeIDs(con,1)
                }
                "Three Phase AC" -> {
                    db.createChargeIDs(con,3)
                }
                "DC" ->{
                    db.createChargeIDs(con,2)
                }
            }
        }

        connectors.clear()
    }

    fun insertLocations(){

        println("Inserting Locations...")

        var context: Context = this@MainActivity
        var db = DatabaseHelper(context)

        db.insertLocations(locations)

        locations.clear()
    }



    inner class GetData : AsyncTask<Void, Int, ArrayList<Location>?>() {

        override fun doInBackground(vararg p0: Void?): ArrayList<Location>? {

            var locations = arrayListOf<Location>()

            //Make http call
            var urlConnection: HttpURLConnection? = null
            var input: InputStream? = null

            try {
                var url = URL("http://chargepoints.dft.gov.uk/api/retrieve/registry/format/json")
                //Open connection
                urlConnection = url.openConnection() as HttpURLConnection
                input = BufferedInputStream(urlConnection.inputStream)

            } catch (e: IOException) {
                e.printStackTrace()
            }

            //Convert stream into a usable string

            println(input)

            var response: String = convertStreamToString(input)

            println(response)

            try {

                // Parse json

                var counter = 0

                var obj = JSONObject(response)
                var jarry: JSONArray = obj.getJSONArray("ChargeDevice")

                var progress = 0;

                for (i in 0..(jarry.length() - 1)) {
                    var deviceObject: JSONObject = jarry.get(i) as JSONObject

                    val locationname = deviceObject.get("ChargeDeviceName").toString()
                    val locationid = deviceObject.get("ChargeDeviceId").toString()

                    var markerObject: JSONObject =
                        deviceObject.get("ChargeDeviceLocation") as JSONObject

                    val latitude = markerObject.get("Latitude").toString()
                    val longitude = markerObject.get("Longitude").toString()

                    var addressObject: JSONObject = markerObject.get("Address") as JSONObject

                    val street = addressObject.get("Street").toString()
                    val postcode = addressObject.get("PostCode").toString()
                    val payment = deviceObject.get("PaymentRequiredFlag").toString()
                    val paymentdetails = deviceObject.get("PaymentDetails").toString()
                    val subscription = deviceObject.get("SubscriptionRequiredFlag").toString()
                    val subscriptiondetails = deviceObject.get("SubscriptionDetails").toString()
                    val parkingpayment = deviceObject.get("ParkingFeesFlag").toString()
                    val parkingpaymentdetails = deviceObject.get("ParkingFeesDetails").toString()
                    val onstreet = deviceObject.get("OnStreetFlag").toString()

                    val loc = Location(
                        locationid,
                        locationname,
                        latitude,
                        longitude,
                        street,
                        postcode,
                        payment,
                        paymentdetails,
                        subscription,
                        subscriptiondetails,
                        parkingpayment,
                        parkingpaymentdetails,
                        onstreet
                    )

                    print(loc.toString())

                    locations.add(loc)

                    counter + counter + 1

                    publishProgress(counter)

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


            println(locations.size)
            return locations
        }


        override fun onPostExecute(result: ArrayList<Location>?) {
            super.onPostExecute(result)

            var array = result
            println(array!!.size)

            var context: Context = this@MainActivity
            var db = DatabaseHelper(context)

            for (i in 0..array!!.size -1) {

                db.insertLocation(array!!.get(i))
            }

            //Delays the map fragment inflation until marker data has been and stored, so that this data can be used within onMapReady

            //var adapter = SectionsStatePagerAdapter(supportFragmentManager)
            //adapter.addFragment(MapsFragment(), "Fragment Two")
            //viewPager?.adapter = adapter
        }
    }

    fun convertStreamToString(`is`: InputStream?): String {
        val s = java.util.Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

}
