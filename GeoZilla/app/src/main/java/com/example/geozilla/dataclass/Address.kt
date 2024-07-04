package com.example.geozilla.dataclass

import com.google.android.gms.maps.model.LatLng

data class Address(val name: String, val details: String, val location: LatLng)
// Sample data (mock addresses)
object MockData {
    val addresses = listOf(
        Address("Lane No 10", "QPJF+776, Unnamed Road, Wah, Rawalpindi, Punjab, Pakistan", LatLng(33.780516360959815, 72.72307707071013)),
        Address("POF Factories", "Quaid Ave, Wah Cantt, Rawalpindi, Punjab 47010, Pakistan ", LatLng(33.771634378734255, 72.77184216534695)),
        Address("Sir Syed College Campus 2 Wah Cantt", "B 3/31 Quaid Ave, Wah Cantt, Rawalpindi, Punjab 47040, Pakistan", LatLng(33.78168715687332, 72.7377312627917)),
        Address("Pain And Gain", "QPRG+353, Lalazar, Shah Wali Colony, Wah Cantt, Rawalpindi, Punjab, Pakistan", LatLng(33.79016624989952, 72.72547295658771)),
        Address("Keyani Restaurant Gt Road", "QQ35+HR2, New City Wah Cantt, Wah, Rawalpindi, Punjab 47040, Pakistan", LatLng(33.75406209638899, 72.75953007641832)),
        Address("O3 Interfaces (Pvt) Ltd.","Plot 8, I-11/3 I 11/3 I-11, Islamabad, Islamabad Capital Territory 44000, Pakistan",LatLng(33.644744601184094, 73.02224879416639))
    )
}
