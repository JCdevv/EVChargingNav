package uk.ac.mmu.advprog.kotlinmaps

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Timestamp
import java.util.*

class LoadingFragment : Fragment() {

    var connectors = arrayListOf<Connector>()
    var locations = arrayListOf<Location>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view : View = inflater.inflate(R.layout.loadingfragment,container,false)

        var DB  = DatabaseHelper(activity!!.applicationContext)

        //If timestamp is empty, the database is empty so it is automatically populated and a defauly update schedule of every app startup is used.
        if(DB.getTimestamp() == ""){
            var GM = getGovMarkers()
            GM.execute()

            DB.setUpdate(0)
        }

        //Gets last time app updated
        var timestamp = Timestamp(DB.getTimestamp().toLong())

        //Gets current time
        var currentTime = Timestamp(System.currentTimeMillis())

        //Gets the difference between both dates
        val result = getDaysBetween(timestamp,currentTime)

        //If time since last updated greater than update schedule, update. OR if user is currently trying to update
        if(result >= Integer.parseInt(DB.getSchedule()) || Data.isupdating == true){

            //Empty tables so new data can be populated
            DB.emptyTables()

            //Chooses data source based on users selected one
            if(DB.getSource() == 1){
                var GM = getGovMarkers()
                GM.execute()
            }
            else{
                var GOM = getOpenMarkers()
                GOM.execute()
            }

            Data.isupdating = false
        }
        else{

            //If time since last updated not greater than update schedule, show filter fragment
            var fragTransation = fragmentManager!!.beginTransaction()
            fragTransation.replace(android.R.id.content,FilterFragment())
            fragTransation.commit()

        }

        return view
    }

    //Gets the number of days between two timestamps, used to compare against the users chosen update schedule
    private fun getDaysBetween(start: Timestamp, end: Timestamp): Int {
        var start = start
        var end = end
        var negative = false

        //if end timestamp is before start
        if (end.before(start)) {
            negative = true
            val temp = start
            start = end
            end = temp
        }

        //Create calendar of start timestamp
        val calStart = GregorianCalendar()
        calStart.setTime(start)
        calStart.set(Calendar.HOUR_OF_DAY, 0)
        calStart.set(Calendar.MINUTE, 0)
        calStart.set(Calendar.SECOND, 0)
        calStart.set(Calendar.MILLISECOND, 0)

        //Create calendar of end timestamp
        val calEnd = GregorianCalendar()
        calEnd.setTime(end)
        calEnd.set(Calendar.HOUR_OF_DAY, 0)
        calEnd.set(Calendar.MINUTE, 0)
        calEnd.set(Calendar.SECOND, 0)
        calEnd.set(Calendar.MILLISECOND, 0)

        //if years are equal
        if (calStart.get(Calendar.YEAR) === calEnd.get(Calendar.YEAR)) {
            return if (negative) (calEnd.get(Calendar.DAY_OF_YEAR) - calStart.get(Calendar.DAY_OF_YEAR)) * -1 else calEnd.get(Calendar.DAY_OF_YEAR) - calStart.get(Calendar.DAY_OF_YEAR)
        }

        var days = 0
        while (calEnd.after(calStart)) {
            calStart.add(Calendar.DAY_OF_YEAR, 1)
            days++
        }
        return if (negative) days * -1 else days
    }

    //Makes a http request to the gov api, parses the json and inserts the locations and connectors into the database
    inner class getGovMarkers : AsyncTask<Void, Int, Boolean>() {

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

            var response: String = convertStreamToString(input)

            try {

                // Parse json

                var obj = JSONObject(response)
                var jarry: JSONArray = obj.getJSONArray("ChargeDevice")

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

                    //Add current connector and location to array
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


        //When done, insert locations and connectors and change to filter fragment
        override fun onPostExecute(result: Boolean) {
            insertLocations()
            insertConnectors()

            var fragTransation = fragmentManager!!.beginTransaction()
            fragTransation.replace(android.R.id.content,FilterFragment())
            fragTransation.commit()


        }
    }

    //Makes a http request to the open chargemap api, parses the json and inserts the locations and connectors into the database
    inner class getOpenMarkers : AsyncTask<Void, Int, Boolean>() {

        override fun doInBackground(vararg p0: Void?): Boolean {

            //Make http call
            var urlConnection: HttpURLConnection? = null
            var input: InputStream? = null

            try {
                var url = URL("https://api.openchargemap.io/v3/poi/?output=json&countrycode=GB&maxresults=500&key=")
                //Open connection
                urlConnection = url.openConnection() as HttpURLConnection
                input = BufferedInputStream(urlConnection.inputStream)

            } catch (e: IOException) {
                e.printStackTrace()
            }

            //Convert stream into a usable string

            var response: String = convertStreamToString(input)

            try {

                // Parse json

                var jarry = JSONArray(response)

                var lastLoc = ""

                for (i in 0..(jarry.length()) -1) {
                    var deviceObject: JSONObject = jarry.get(i) as JSONObject

                    val locationid = deviceObject.get("UUID").toString()

                    //Openchargemap data contains duplicate values, check ID's to ensure duplicate data is not inserted
                    if (locationid.equals(lastLoc)) {
                        //do nothing
                    }
                    else{
                        lastLoc = locationid

                        var addressObj: JSONObject =
                            deviceObject.get("AddressInfo") as JSONObject

                        val street = addressObj.get("Title").toString()
                        val postcode = addressObj.get("Postcode").toString()
                        val locationname = "N/A"
                        val paymentdetails = "N/A"
                        val subscriptiondetails = "N/A"

                        var usageObj: JSONObject =
                            deviceObject.get("UsageType") as JSONObject

                        val payment = usageObj.get("IsPayAtLocation").toString()
                        val subscription = usageObj.get("IsMembershipRequired").toString()

                        val parkingpayment = "N/A"
                        val parkingpaymentdetails = "N/A"

                        //No strong indicator for whether something is onstreet, so access key required string used as this very likely
                        //Indicates if something is onstreet, or in a private location
                        val onstreet = usageObj.get("IsAccessKeyRequired").toString()

                        var addressObject: JSONObject =
                            deviceObject.get("AddressInfo") as JSONObject

                        val latitude = addressObject.get("Latitude").toString()
                        val longitude = addressObject.get("Longitude").toString()

                        var connArray = deviceObject.getJSONArray("Connections")

                        var connObject: JSONObject = connArray.get(0) as JSONObject

                        var chargeObj = connObject.get("CurrentType") as JSONObject

                        var chargemethod = chargeObj.get("Title").toString()

                        val outputkw = connObject.get("PowerKW").toString()
                        val outputvoltage = connObject.get("Voltage").toString()
                        val outputcurrent = "N/A"
                        val service = "N/A"

                        //Add current connector and location to array
                        val con = Connector(
                            locationid,
                            0,
                            outputkw,
                            outputvoltage,
                            outputcurrent,
                            chargemethod,
                            service
                        )

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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return true
        }


        //When done, insert locations and connectors and change to filter fragment
        override fun onPostExecute(result: Boolean) {
            insertLocations()
            insertConnectors()

            var fragTransation = fragmentManager!!.beginTransaction()
            fragTransation.replace(android.R.id.content,FilterFragment())
            fragTransation.commit()

        }
    }

    //Inserts connectors into database
    fun insertConnectors(){

        println("Inserting Connectors...")

        var context: Context = activity!!.applicationContext
        var db = DatabaseHelper(context)

        db.insertConnectors(connectors)
        db.createChargeIDs(connectors)

        //clear array for next update
        connectors.clear()
    }


    //Inserts locations into database
    fun insertLocations(){

        println("Inserting Locations...")

        var context: Context = activity!!.applicationContext
        var db = DatabaseHelper(context)

        db.insertLocations(locations)

        //clear array for next update
        locations.clear()
    }

    //converts stream input from http request into readable string
    fun convertStreamToString(`is`: InputStream?): String {
        val s = java.util.Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

}