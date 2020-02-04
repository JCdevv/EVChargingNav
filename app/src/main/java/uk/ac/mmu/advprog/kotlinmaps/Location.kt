package uk.ac.mmu.advprog.kotlinmaps

class Location (var locationid: String, var locationname :String, var latitude : String, var longitude : String, var street : String, var postcode : String, var payment : String, var paymentdetails : String, var subscription : String, var subscriptiondetails : String, var parkingpayment : String, var parkingpaymentdetails : String,var onstreet : String ) {

    override fun toString() : String{
        return locationid + " " + locationname + " " + latitude + " " + longitude
    }

}