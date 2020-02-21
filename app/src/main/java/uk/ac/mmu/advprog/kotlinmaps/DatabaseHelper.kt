package uk.ac.mmu.advprog.kotlinmaps

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

val DB_NAME = "mapinfo"
val TABLE_NAME_1 = "connector"
val TABLE_NAME_2 = "location"


class DatabaseHelper(context : Context) : SQLiteOpenHelper(context, DB_NAME, null,1) {

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

    //Gets the currently selected data source int.
    //1 = gov
    //2 = openapi
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

    //Updates the chosen data source
    fun setSource(Source : Int){
        val db = this.writableDatabase
        var query = "UPDATE config SET source = $Source"
        
        db.beginTransaction()

        db.execSQL(query)

        println(query)
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    //Sets a new update schedule, and as this is used called if the database is empty, sets default values for schedule and source if needed
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

    //Sets a new timestamp of last time the database was updated
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

    //Gets the current chosen update schedule
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

    //Gets the last time the database was updated
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

    //Empties all tables so data can be updated
    fun emptyTables(){
        val db = this.writableDatabase
        var query = "DELETE FROM connector"
        var query2 ="DELETE FROM location"
        var query3 ="DELETE FROM chargemethod"

        db.execSQL(query)
        db.execSQL(query2)
        db.execSQL(query3)


    }

    fun createChargeIDs(conns : ArrayList<Connector>) {
        val db = this.writableDatabase
        var content = ContentValues()

        db.beginTransaction()

        for (i in 0..conns!!.size - 1) {
            var con = conns!!.get(i)


            content.put("locationid", con.locationid)

            // IDS:
            // 1 = Single Phase
            // 2 = DC
            // 3 = Three Phase

            when(con.chargemethod){
                "Single Phase AC" -> {
                    content.put("chargeid",1)
                    db.insert("chargemethod", null, content)
                }
                "Three Phase AC" -> {
                    content.put("chargeid",3)
                    db.insert("chargemethod", null, content)
                }
                "DC" ->{
                    content.put("chargeid",2)
                    db.insert("chargemethod", null, content)
                }
            }
        }

        db.setTransactionSuccessful()
        db.endTransaction()
        db.close()

    }

    //Inserts locations retrieved from data source into the database
    fun insertLocations(loc : ArrayList<Location>){
        var content = ContentValues()

        val db = this.writableDatabase

        //Makes use of transactions due to a very large amount of inserts. Completes all inserts at once in a single transaction, significantly reducing insert time.
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

        //Updates the last updated timestamps
        setTimestamp()
    }

    //Inserts connectors retrieved from data source into the database
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


    fun getSinglePhase(onStreet: Int, isFree: Int): ArrayList<Location> {

        // AC (Single-Phase)
        var locations = ArrayList<Location>()
        var query = ""


        //Below is a series of checks, changing the query based on what filter options were selected by the user, as well as changes the query based on what data source is used
        if(getSource() == 1) {

            query = "SELECT DISTINCT * FROM connector INNER JOIN location ON connector.locationid = location.locationid WHERE chargemethod = 'Single Phase AC'"

            if(onStreet == 2 && isFree == 2){
                query += " AND payment = 'false' AND onstreet = 'true';"
            }
            else if(onStreet == 2 && isFree == 1){
                query += " AND payment = 'true' AND onstreet = 'true';"
            }
            else if(onStreet == 1 && isFree == 2){
                query += " AND payment = 'false' AND onstreet = 'false';"
            }
            else if(onStreet == 1 && isFree == 1){
                query +=  " AND payment = 'false' AND onstreet = 'false';"
            }
            else if(onStreet == 2 && isFree == 0){
                query +=
                    " AND onstreet = 'true';"
            }
            else if(onStreet == 0 && isFree == 2){
                query += " AND payment = 'false';"
            }
            else if(onStreet == 1 && isFree == 0){
                query +=
                    " AND onstreet = 'false';"
            }
            else if(onStreet == 0 && isFree == 1){
                query += " AND payment = 'true';"
            }
            else if(onStreet == 2 && isFree == 0){
                query += " AND onstreet = 'true';"
            }
            else if(onStreet == 1 && isFree == 0){
                query += " AND onstreet = 'false';"
            }
            else if(onStreet == 0 && isFree == 0){
                //Change nothing. Users does not want to filter by either onstreet or isfree
            }

            var db = this.writableDatabase
            var cursor: Cursor = db.rawQuery(query, null)

            println(query)

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

            query = "SELECT DISTINCT * FROM connector INNER JOIN location ON connector.locationid = location.locationid " +
                        "WHERE chargemethod = 'AC (Single-Phase)'"

            //Below is a series of checks, changing the query based on what filter options were selected by the user, as well as changes the query based on what data source is used
            if(onStreet == 2 && isFree == 2){
                query += " AND payment = 'false' AND onstreet = 'true';"
            }
            else if(onStreet == 2 && isFree == 1){
                query += " AND payment = 'true' AND onstreet = 'true';"
            }
            else if(onStreet == 1 && isFree == 2){
                query += " AND payment = 'false' AND onstreet = 'false';"
            }
            else if(onStreet == 1 && isFree == 1){
                query +=  " AND payment = 'false' AND onstreet = 'false';"
            }
            else if(onStreet == 2 && isFree == 0){
                query +=
                    " AND onstreet = 'true';"
            }
            else if(onStreet == 0 && isFree == 2){
                query += " AND payment = 'false';"
            }
            else if(onStreet == 1 && isFree == 0){
                query +=
                    " AND onstreet = 'false';"
            }
            else if(onStreet == 0 && isFree == 1){
                query += " AND payment = 'true';"
            }
            else if(onStreet == 2 && isFree == 0){
                query += " AND onstreet = 'true';"
            }
            else if(onStreet == 1 && isFree == 0){
                query += " AND onstreet = 'false';"
            }
            else if(onStreet == 0 && isFree == 0){
                //Change nothing. Users does not want to filter by either onstreet or isfree
            }
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

    fun getDC(onStreet: Int, isFree: Int): ArrayList<Location> {
        var locations = ArrayList<Location>()
        var query = "SELECT DISTINCT * FROM connector INNER JOIN location ON connector.locationid = location.locationid WHERE chargemethod = 'DC'"

        //Below is a series of checks, changing the query based on what filter options were selected by the user
        //Both data sources use 'DC' , so same queries can be used for both data sets.
        if(onStreet == 2 && isFree == 2){
            query += " AND payment = 'false' AND onstreet = 'true';"
        }
        else if(onStreet == 2 && isFree == 1){
            query += " AND payment = 'true' AND onstreet = 'true';"
        }
        else if(onStreet == 1 && isFree == 2){
            query += " AND payment = 'false' AND onstreet = 'false';"
        }
        else if(onStreet == 1 && isFree == 1){
            query +=  " AND payment = 'false' AND onstreet = 'false';"
        }
        else if(onStreet == 2 && isFree == 0){
            query +=
                " AND onstreet = 'true';"
        }
        else if(onStreet == 0 && isFree == 2){
            query += " AND payment = 'false';"
        }
        else if(onStreet == 1 && isFree == 0){
            query +=
                " AND onstreet = 'false';"
        }
        else if(onStreet == 0 && isFree == 1){
            query += " AND payment = 'true';"
        }
        else if(onStreet == 2 && isFree == 0){
            query += " AND onstreet = 'true';"
        }
        else if(onStreet == 1 && isFree == 0){
            query += " AND onstreet = 'false';"
        }
        else if(onStreet == 0 && isFree == 0){
            //Change nothing. Users does not want to filter by either onstreet or isfree
        }

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
    
    fun getTriplePhase(onStreet: Int, isFree: Int): ArrayList<Location> {

        //AC (Three-Phase)

        var locations = ArrayList<Location>()
        var query = ""
        //Below is a series of checks, changing the query based on what filter options were selected by the user, as well as changes the query based on what data source is used
        if(getSource() == 1) {
            query = "SELECT DISTINCT * FROM connector INNER JOIN location ON connector.locationid = location.locationid " +
                    "WHERE chargemethod = 'Three Phase AC'"
            if(onStreet == 2 && isFree == 2){
                query += " AND payment = 'false' AND onstreet = 'true';"
            }
            else if(onStreet == 2 && isFree == 1){
                query += " AND payment = 'true' AND onstreet = 'true';"
            }
            else if(onStreet == 1 && isFree == 2){
                query += " AND payment = 'false' AND onstreet = 'false';"
            }
            else if(onStreet == 1 && isFree == 1){
                query +=  " AND payment = 'false' AND onstreet = 'false';"
            }
            else if(onStreet == 2 && isFree == 0){
                query +=
                    " AND onstreet = 'true';"
            }
            else if(onStreet == 0 && isFree == 2){
                query += " AND payment = 'false';"
            }
            else if(onStreet == 1 && isFree == 0){
                query +=
                    " AND onstreet = 'false';"
            }
            else if(onStreet == 0 && isFree == 1){
                query += " AND payment = 'true';"
            }
            else if(onStreet == 2 && isFree == 0){
                query += " AND onstreet = 'true';"
            }
            else if(onStreet == 1 && isFree == 0){
                query += " AND onstreet = 'false';"
            }
            else if(onStreet == 0 && isFree == 0){
                //Change nothing. Users does not want to filter by either onstreet or isfree
            }

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
            //Below is a series of checks, changing the query based on what filter options were selected by the user, as well as changes the query based on what data source is used

            query = "SELECT DISTINCT * FROM connector INNER JOIN location ON connector.locationid = location.locationid " +
            "WHERE chargemethod = 'AC (Three-Phase)'"
            if(onStreet == 2 && isFree == 2){
                query += " AND payment = 'false' AND onstreet = 'true';"
            }
            else if(onStreet == 2 && isFree == 1){
                query += " AND payment = 'true' AND onstreet = 'true';"
            }
            else if(onStreet == 1 && isFree == 2){
                query += " AND payment = 'false' AND onstreet = 'false';"
            }
            else if(onStreet == 1 && isFree == 1){
                query +=  " AND payment = 'false' AND onstreet = 'false';"
            }
            else if(onStreet == 2 && isFree == 0){
                query +=
                    " AND onstreet = 'true';"
            }
            else if(onStreet == 0 && isFree == 2){
                query += " AND payment = 'false';"
            }
            else if(onStreet == 1 && isFree == 0){
                query +=
                    " AND onstreet = 'false';"
            }
            else if(onStreet == 0 && isFree == 1){
                query += " AND payment = 'true';"
            }
            else if(onStreet == 2 && isFree == 0){
                query += " AND onstreet = 'true';"
            }
            else if(onStreet == 1 && isFree == 0){
                query += " AND onstreet = 'false';"
            }
            else if(onStreet == 0 && isFree == 0){
                //Change nothing. Users does not want to filter by either onstreet or isfree
            }


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
}
