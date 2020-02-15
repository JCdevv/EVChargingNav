package uk.ac.mmu.advprog.kotlinmaps

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.TextView
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
import java.util.*
import kotlin.collections.ArrayList


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


        if(DB.getTimestamp() == ""){
            //var GM = getMarkers()
            //GM.execute()

            DB.setUpdate(0)
        }

        var timestamp = Timestamp(DB.getTimestamp().toLong())

        var currentTime = Timestamp(System.currentTimeMillis())

        val result = getDaysBetween(timestamp,currentTime)

        println("The current update schedule is every  ${DB.getSchedule()} days")

        println(result)

        setupViewPager(viewPager)

        if(result < Integer.parseInt(DB.getSchedule())){

            //var loadingText : TextView = adapter!!.getItem(1).view!!.findViewById(R.id.textTitle)
            //loadingText.setText("Updating..")

            println("Hello we are here")

            /*
            DB.emptyTables()

            if(DB.getSource() == 1){
                var GM = getMarkers()
                GM.execute()
            }
            else{
                //
            }*/



        }
        else{
            adapter?.addFragment(FragmentOne(), "Fragment One")

            viewPager?.adapter = adapter
            setViewPager(2)
        }


    }

    private fun getDaysBetween(start: Timestamp, end: Timestamp): Int {
        var start = start
        var end = end
        var negative = false
        if (end.before(start)) {
            negative = true
            val temp = start
            start = end
            end = temp
        }
        val cal = GregorianCalendar()
        cal.setTime(start)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val calEnd = GregorianCalendar()
        calEnd.setTime(end)
        calEnd.set(Calendar.HOUR_OF_DAY, 0)
        calEnd.set(Calendar.MINUTE, 0)
        calEnd.set(Calendar.SECOND, 0)
        calEnd.set(Calendar.MILLISECOND, 0)
        if (cal.get(Calendar.YEAR) === calEnd.get(Calendar.YEAR)) {
            return if (negative) (calEnd.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR)) * -1 else calEnd.get(
                Calendar.DAY_OF_YEAR
            ) - cal.get(Calendar.DAY_OF_YEAR)
        }
        var days = 0
        while (calEnd.after(cal)) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            days++
        }
        return if (negative) days * -1 else days
    }

    //Opens requested fragment
    fun setViewPager(fragmentNumber: Int) {
        viewPager?.currentItem = fragmentNumber
    }

    //Sets up view pager, inflates fragment one
    fun setupViewPager(vp: ViewPager?) {
        adapter = SectionsStatePagerAdapter(supportFragmentManager)

        adapter?.addFragment(LoadingFragment(),"Loading Fragment")



        viewPager?.adapter = adapter
    }



    inner class getMarkers : AsyncTask<Void, Int, Boolean>() {

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

    /*
    inner class getOpenMarkers : AsyncTask<Void, Int, Boolean>() {

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

                var jarry = JSONArray(response)

                for (i in 0..(jarry.length()) -1) {
                    var deviceObject: JSONObject = jarry.get(i) as JSONObject

                    val locationid = deviceObject.get("ID").toString()

                    var addressObj: JSONObject =
                        deviceObject.get("AddressInfo") as JSONObject

                    val street = addressObj.get("Title").toString()
                    val postcode = addressObj.get("Postcode").toString()

                    var usageObj: JSONObject =
                        deviceObject.get("UsageType") as JSONObject

                    val payment = usageObj.get("IsPayAtLocation").toString()
                    val subscription = usageObj.get("IsMembershipRequired").toString()

                    val parkingpayment = deviceObject.get("ParkingFeesFlag").toString()
                    val parkingpaymentdetails = deviceObject.get("ParkingFeesDetails").toString()
                    val onstreet = deviceObject.get("OnStreetFlag").toString()

                    val latitude = deviceObject.get("Latitude").toString()
                    val longitude = deviceObject.get("Longitude").toString()

                    var addressObject: JSONObject = markerObject.get("Address") as JSONObject



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
    }*/



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


    fun convertStreamToString(`is`: InputStream?): String {
        val s = java.util.Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

}
