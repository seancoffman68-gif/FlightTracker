package com.example.flighttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

// Data model matching OpenSky's /flights/arrival fields we use
data class Flight(
    val icao24: String,
    val firstSeen: Long,
    val estArrivalAirport: String?,
    val callsign: String?
)

// Retrofit API interface
interface OpenSkyApi {
    @GET("flights/arrival")
    suspend fun getArrivals(
        @Query("airport") airport: String,
        @Query("begin") begin: Long,
        @Query("end") end: Long
    ): List<Flight>
}

// Simple repository
class FlightRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://opensky-network.org/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(OpenSkyApi::class.java)

    suspend fun getFlights(airport: String, begin: Long, end: Long) =
        api.getArrivals(airport, begin, end)
}

// UI list
@Composable
fun FlightList(flights: List<Flight>) {
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val sortedFlights = flights.sortedBy { it.firstSeen }

    LazyColumn {
        items(sortedFlights) { flight ->
            val arrivalTime = formatter.format(Date(flight.firstSeen * 1000))
            val flightId = flight.callsign?.trim()?.ifEmpty { null } ?: flight.icao24.uppercase()
            Text(
                text = "Flight: $flightId  |  Arrival: $arrivalTime",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

// Main Activity (auto-refresh + last updated)
class MainActivity : ComponentActivity() {
    private val repo = FlightRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var flights by remember { mutableStateOf<List<Flight>>(emptyList()) }
            var lastUpdated by remember { mutableStateOf<Long?>(null) }

            // Auto-refresh every 60 seconds
            LaunchedEffect(Unit) {
                while (true) {
                    val now = System.currentTimeMillis() / 1000
                    val oneHourAgo = now - 3600
                    try {
                        flights = repo.getFlights("KMOD", oneHourAgo, now)
                        lastUpdated = System.currentTimeMillis()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    delay(60_000)
                }
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)) {

                lastUpdated?.let {
                    val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    Text(
                        text = "Last Updated: ${timeFmt.format(Date(it))}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                FlightList(flights)
            }
        }
    }
}
