package uk.ac.mmu.advprog.kotlinmaps

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.Timestamp

val DB_NAME = "mapinfo"
val TABLE_NAME_1 = "connector"
val TABLE_NAME_2 = "location"


class DatabaseHelper(context : Context) : SQLiteOpenHelper(context, DB_NAME, null,1) {

    val db = this.writableDatabase

    //If db does not exist, onCreate is ran and tables are created.
    override fun onCreate(p0: SQLiteDatabase?) {
        var connectorCreate =
            "CREATE TABLE `connector` ( `locationid` TEXT, `connectorid` AUTO_INCREMENT, `outputkw` TEXT, `outputvoltage` TEXT, `outputcurrent` TEXT, `chargemethod` TEXT, `service` TEXT, PRIMARY KEY(`connectorid`,`locationid`) )"
        var locationCreate =
            "CREATE TABLE `location` ( `locationid` TEXT, `locationname` TEXT, `latitude` TEXT, `longitude` TEXT, `street` TEXT, `postcode` TEXT, `payment` TEXT, `paymentdetails` TEXT,`subscription` TEXT, `subscriptiondetails` TEXT, `parkingpayment` TEXT, `parkingpaymentdetails` TEXT, `onstreet` TEXT, PRIMARY KEY(`locationid`) )"
        var chargeCreate =
            "CREATE TABLE `chargemethod` (`locationid` TEXT, `chargeid` INTEGER )"
        var createConfig = "CREATE TABLE `config` (`schedule` NUMBER, `timestamp` TEXT)"

        p0?.execSQL(connectorCreate)
        p0?.execSQL(locationCreate)
        p0?.execSQL(chargeCreate)
        p0?.execSQL(createConfig)

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setUpdate(schedule : Int){
        println("Updating ETAG....")
        val db = this.writableDatabase
        var query = ""
        var timestamp = System.currentTimeMillis()


        if(getUpdate().equals("")){
            query = "INSERT INTO config VALUES ('$schedule','$timestamp')"
        }
        else{
            query = "UPDATE config SET etag = '$schedule' AND timestamp = '$timestamp'"
        }
        db.execSQL(query)
    }

    fun getUpdate() : String{
        var db = this.writableDatabase
        var etag = ""

        var query =
            "SELECT schedule FROM config"

        var cursor: Cursor = db.rawQuery(query, null)

        if (cursor.moveToNext()) {
            do {

                etag = cursor.getString(cursor.getColumnIndex("schedule"))

            } while (cursor.moveToNext())
        }

        return etag

    }


    fun getTimestamp() : String{
        var db = this.writableDatabase
        var timestamp = ""

        var query =
            "SELECT timestamp FROM config"

        var cursor: Cursor = db.rawQuery(query, null)

        if (cursor.moveToNext()) {
            do {

                timestamp = cursor.getString(cursor.getColumnIndex("timestamp"))

            } while (cursor.moveToNext())
        }

        return timestamp

    }


    fun setETag(etag : String){
        println("Updating ETAG....")
        val db = this.writableDatabase
        var query = ""

        if(getETag().equals("")){
            query = "INSERT INTO config VALUES ('$etag')"
        }
        else{
            query = "UPDATE config SET etag = '$etag'"
        }
        db.execSQL(query)
    }

    fun getETag() : String{
        var db = this.writableDatabase
        var etag = ""

        var query =
            "SELECT etag FROM config"

        var cursor: Cursor = db.rawQuery(query, null)

        if (cursor.moveToNext()) {
            do {

                etag = cursor.getString(cursor.getColumnIndex("etag"))

            } while (cursor.moveToNext())
        }

        return etag

    }

    fun emptyTables(){
        val db = this.writableDatabase
        var query = "DELETE FROM connector"
        var query2 ="DELETE FROM location"
        var query3 ="DELETE FROM chargemethod"

        db.execSQL(query)
        db.execSQL(query2)
        db.execSQL(query3)


    }

    fun createChargeIDs(con: Connector, id: Int) {
        val db = this.writableDatabase
        var content = ContentValues()

        content.put("locationid", con.locationid)
        content.put("chargeid", id)

        db.insert("chargemethod",null,content)
        db.close()

    }


    fun insertConnector(con: Connector) {

        var content = ContentValues()

        content.put("locationid", con.locationid)
        content.put("connectorid", con.connectorid)
        content.put("outputkw", con.outputkw)
        content.put("outputvoltage", con.outputvoltage)
        content.put("outputcurrent", con.outputcurrent)
        content.put("chargemethod", con.chargemethod)
        content.put("service", con.service)

        var result = db.insert(TABLE_NAME_1, null, content)

        // Result return : 1 = success, -1 = fail

        var fail: Long = -1
        var success: Long = 1

        if (result == success) {
            println("Data Inserted Successfully")
        } else if (result == fail) {
            println("Data Failed To Be Inserted")
        }

    }

    fun insertLocations(loc : ArrayList<Location>){
        var content = ContentValues()

        val db = this.writableDatabase

        db.beginTransaction()

        for(item in loc){
            content.put("locationid", item.locationid)
            content.put("locationname", item.locationname)
            content.put("latitude", item.latitude)
            content.put("longitude", item.longitude)
            content.put("street", item.street)
            content.put("postcode", item.postcode)
            content.put("payment", item.payment)
            content.put("paymentdetails", item.paymentdetails)
            content.put("subscription", item.subscription)
            content.put("subscriptiondetails", item.subscriptiondetails)
            content.put("parkingpayment", item.parkingpayment)
            content.put("parkingpaymentdetails", item.parkingpaymentdetails)
            content.put("onstreet", item.onstreet)

            var result = db.insert(TABLE_NAME_2, null, content)

            // Result return : 1 = success, -1 = fail

            var fail: Long = -1
            var success: Long = 1

            if (result == success) {
                println("Data Inserted Successfully")
            } else if (result == fail) {
                println("Data Failed To Be Inserted")
            }
        }

        db.setTransactionSuccessful()
        db.endTransaction()
        db.close()
    }

    fun insertConnectors(con : ArrayList<Connector>){
        var content = ContentValues()

        val db = this.writableDatabase

        db.beginTransaction()

        var id = 0

        for(item in con){
            content.put("locationid", item.locationid)
            content.put("connectorid", id)
            content.put("outputkw", item.outputkw)
            content.put("outputvoltage", item.outputvoltage)
            content.put("outputcurrent", item.outputcurrent)
            content.put("chargemethod", item.chargemethod)
            content.put("service", item.service)

            var result = db.insert(TABLE_NAME_1, null, content)

            // Result return : 1 = success, -1 = fail

            var fail: Long = -1
            var success: Long = 1

            if (result == success) {
                println("Data Inserted Successfully")
            } else if (result == fail) {
                println("Data Failed To Be Inserted")
            }

            id++

        }

        db.setTransactionSuccessful()
        db.endTransaction()
        db.close()
    }

    fun insertLocation(loc: Location) {
        var content = ContentValues()



        content.put("locationid", loc.locationid)
        content.put("locationname", loc.locationname)
        content.put("latitude", loc.latitude)
        content.put("longitude", loc.longitude)
        content.put("street", loc.street)
        content.put("postcode", loc.postcode)
        content.put("payment", loc.payment)
        content.put("paymentdetails", loc.paymentdetails)
        content.put("subscription", loc.subscription)
        content.put("subscriptiondetails", loc.subscriptiondetails)
        content.put("parkingpayment", loc.parkingpayment)
        content.put("parkingpaymentdetails", loc.parkingpaymentdetails)
        content.put("onstreet", loc.onstreet)

        var result = db.insert(TABLE_NAME_2, null, content)

        // Result return : 1 = success, -1 = fail

        var success: Long = -1
        var fail: Long = 1

        if (result == success) {
            println("Data Inserted Successfully")
        } else if (result == fail) {
            println("Data Failed To Be Inserted")
        }

    }

    fun getSinglePhase(onStreet: Boolean, isFree: Boolean): ArrayList<Location> {
        var locations = ArrayList<Location>()
        var query =
            "SELECT DISTINCT * FROM connector INNER JOIN location ON connector.locationid = location.locationid " +
                    "WHERE chargemethod = 'Single Phase AC' AND payment = '" + isFree.toString() + "' AND onstreet = '" + onStreet.toString() + "';"
        var db = this.writableDatabase
        var cursor: Cursor = db.rawQuery(query, null)

        if (cursor.moveToNext()) {
            do {
                var loc = Location(
                    cursor.getString(cursor.getColumnIndex("locationid"))
                    , cursor.getString(cursor.getColumnIndex("locationname"))
                    , cursor.getString(cursor.getColumnIndex("latitude"))
                    , cursor.getString(cursor.getColumnIndex("longitude"))
                    , cursor.getString(cursor.getColumnIndex("street"))
                    , cursor.getString(cursor.getColumnIndex("postcode"))
                    , cursor.getString(cursor.getColumnIndex("payment"))
                    , cursor.getString(cursor.getColumnIndex("paymentdetails"))
                    , cursor.getString(cursor.getColumnIndex("subscription"))
                    , cursor.getString(cursor.getColumnIndex("subscriptiondetails"))
                    , cursor.getString(cursor.getColumnIndex("parkingpayment"))
                    , cursor.getString(cursor.getColumnIndex("parkingpaymentdetails"))
                    , cursor.getString(cursor.getColumnIndex("onstreet"))
                )

                locations.add(loc)

            } while (cursor.moveToNext())
        }
        println(locations.size)
        return locations
    }

    fun getDC(onStreet: Boolean, isFree: Boolean): ArrayList<Location> {
        var locations = ArrayList<Location>()
        var query =
            "SELECT DISTINCT * FROM connector INNER JOIN location ON connector.locationid = location.locationid " +
                    "WHERE chargemethod = 'DC' AND payment = '" + isFree.toString() + "' AND onstreet = '" + onStreet.toString() + "';"
        var db = this.writableDatabase
        var cursor: Cursor = db.rawQuery(query, null)

        if (cursor.moveToNext()) {
            do {
                var loc = Location(
                    cursor.getString(cursor.getColumnIndex("locationid"))
                    , cursor.getString(cursor.getColumnIndex("locationname"))
                    , cursor.getString(cursor.getColumnIndex("latitude"))
                    , cursor.getString(cursor.getColumnIndex("longitude"))
                    , cursor.getString(cursor.getColumnIndex("street"))
                    , cursor.getString(cursor.getColumnIndex("postcode"))
                    , cursor.getString(cursor.getColumnIndex("payment"))
                    , cursor.getString(cursor.getColumnIndex("paymentdetails"))
                    , cursor.getString(cursor.getColumnIndex("subscription"))
                    , cursor.getString(cursor.getColumnIndex("subscriptiondetails"))
                    , cursor.getString(cursor.getColumnIndex("parkingpayment"))
                    , cursor.getString(cursor.getColumnIndex("parkingpaymentdetails"))
                    , cursor.getString(cursor.getColumnIndex("onstreet"))
                )

                locations.add(loc)

            } while (cursor.moveToNext())
        }
        println(locations.size)
        return locations
    }
    
    fun getTriplePhase(onStreet: Boolean, isFree: Boolean): ArrayList<Location> {
        var locations = ArrayList<Location>()
        var query =
            "SELECT DISTINCT * FROM connector INNER JOIN location ON connector.locationid = location.locationid " +
                    "WHERE chargemethod = 'Three Phase AC' AND payment = '" + isFree.toString() + "' AND onstreet = '" + onStreet.toString() + "';"
        var db = this.writableDatabase
        var cursor: Cursor = db.rawQuery(query, null)

        if (cursor.moveToNext()) {
            do {
                var loc = Location(
                    cursor.getString(cursor.getColumnIndex("locationid"))
                    , cursor.getString(cursor.getColumnIndex("locationname"))
                    , cursor.getString(cursor.getColumnIndex("latitude"))
                    , cursor.getString(cursor.getColumnIndex("longitude"))
                    , cursor.getString(cursor.getColumnIndex("street"))
                    , cursor.getString(cursor.getColumnIndex("postcode"))
                    , cursor.getString(cursor.getColumnIndex("payment"))
                    , cursor.getString(cursor.getColumnIndex("paymentdetails"))
                    , cursor.getString(cursor.getColumnIndex("subscription"))
                    , cursor.getString(cursor.getColumnIndex("subscriptiondetails"))
                    , cursor.getString(cursor.getColumnIndex("parkingpayment"))
                    , cursor.getString(cursor.getColumnIndex("parkingpaymentdetails"))
                    , cursor.getString(cursor.getColumnIndex("onstreet"))
                )

                locations.add(loc)

            } while (cursor.moveToNext())
        }
        println(locations.size)
        return locations
    }

    fun getLocations(onStreet: Boolean, isFree: Boolean): ArrayList<Location> {

        var locations = arrayListOf<Location>()
        var query = "SELECT * FROM LOCATION WHERE onstreet = '" + onStreet.toString() + "' AND payment ='" +isFree.toString() +"';"
        var db = this.writableDatabase
        var cursor: Cursor = db.rawQuery(query, null)

        if (cursor.moveToNext()) {
            do {
                var loc = Location(
                    cursor.getString(cursor.getColumnIndex("locationid"))
                    , cursor.getString(cursor.getColumnIndex("locationname"))
                    , cursor.getString(cursor.getColumnIndex("latitude"))
                    , cursor.getString(cursor.getColumnIndex("longitude"))
                    , cursor.getString(cursor.getColumnIndex("street"))
                    , cursor.getString(cursor.getColumnIndex("postcode"))
                    , cursor.getString(cursor.getColumnIndex("payment"))
                    , cursor.getString(cursor.getColumnIndex("paymentdetails"))
                    , cursor.getString(cursor.getColumnIndex("subscription"))
                    , cursor.getString(cursor.getColumnIndex("subscriptiondetails"))
                    , cursor.getString(cursor.getColumnIndex("parkingpayment"))
                    , cursor.getString(cursor.getColumnIndex("parkingpaymentdetails"))
                    , cursor.getString(cursor.getColumnIndex("onstreet"))
                )

                locations.add(loc)
            } while (cursor.moveToNext())
        }
        return locations
    }
}
