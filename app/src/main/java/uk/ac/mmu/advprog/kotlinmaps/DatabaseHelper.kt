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
        var createConfig = "CREATE TABLE `config` (`schedule` TEXT, `timestamp` TEXT, `source` NUMBER)"

        p0?.execSQL(connectorCreate)
        p0?.execSQL(locationCreate)
        p0?.execSQL(chargeCreate)
        p0?.execSQL(createConfig)

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getSource() : Int{
        var db = this.writableDatabase
        var source = 0

        var query =
            "SELECT source FROM config"

        var cursor: Cursor = db.rawQuery(query, null)

        if (cursor.moveToNext()) {
            do {

                source = cursor.getInt(cursor.getColumnIndex("source"))

            } while (cursor.moveToNext())
        }

        return source
    }

    fun setSource(Source : Int){
        val db = this.writableDatabase
        var query = "UPDATE config SET source = $Source"
        
        db.beginTransaction()

        db.execSQL(query)

        println(query)
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun setUpdate(schedule : Int){
        val db = this.writableDatabase
        var query = ""
        var timestamp = System.currentTimeMillis()
        var source = 2

        db.beginTransaction()


        if(getSchedule().equals("")){
            query = "INSERT INTO config VALUES ('$schedule','$timestamp',$source)"
            println(query)
        }
        else{
            query = "UPDATE config SET schedule = '$schedule'; UPDATE config SET timestamp = '$timestamp'"
            println(query)
        }
        db.execSQL(query)
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun setTimestamp(){
        val db = this.writableDatabase
        var query = ""
        var timestamp = System.currentTimeMillis()


        db.beginTransaction()



        query = "UPDATE config SET timestamp = '$timestamp'"
        println(query)

        db.execSQL(query)
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun getSchedule() : String{
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

            println(item.toString())

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

        setTimestamp()
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

        if(getSource() == 1) {
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
        }
        else{
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
