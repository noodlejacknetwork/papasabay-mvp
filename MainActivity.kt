package com.example.papasabay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Central data model for routes containing fare, geofence, and dynamic barangay quotas
data class RouteInfo(
    val name: String,
    val distance: Int,
    val fare: Int,
    val geofenceMeters: Int,
    val currentQuota: Int,
    val maxQuota: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PapaSabayApp()
                }
            }
        }
    }
}

@Composable
fun PapaSabayApp() {
    var currentRole by remember { mutableStateOf("commuter") }

    // Centralized route database state
    val routesDb = mapOf(
        "Loakan -> City Center" to RouteInfo("Loakan -> City Center", 8, 80, 16, 6, 10),
        "Bakakeng -> City Center" to RouteInfo("Bakakeng -> City Center", 5, 50, 2500, 3, 7),
        "Irisan -> City Center" to RouteInfo("Irisan -> City Center", 7, 70, 5200, 8, 8) // Full quota example
    )

    // Shared state for demo synchronization between commuter & driver views
    var selectedRouteKey by remember { mutableStateOf("Loakan -> City Center") }
    var hasJoinedLine by remember { mutableStateOf(false) }
    var tripStarted by remember { mutableStateOf(false) }
    var driverArrived by remember { mutableStateOf(false) }
    var driverSeats by remember { mutableIntStateOf(3) }

    val currentRoute = routesDb[selectedRouteKey] ?: routesDb.values.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // App Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PapaSabay MVP",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Baguio Community Carpooling",
                    color = Color(0xFF4ADE80),
                    fontSize = 12.sp
                )
            }
        }

        // Role Switcher Toggle Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { currentRole = "commuter" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentRole == "commuter") Color(0xFF2563EB) else Color.White
                ),
                modifier = Modifier.weight(1f).height(45.dp)
            ) {
                Text(
                    "Commuter View",
                    color = if (currentRole == "commuter") Color.White else Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { currentRole = "driver" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentRole == "driver") Color(0xFF16A34A) else Color.White
                ),
                modifier = Modifier.weight(1f).height(45.dp)
            ) {
                Text(
                    "Driver View",
                    color = if (currentRole == "driver") Color.White else Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Dynamic Screen Content based on Active Role
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (currentRole == "commuter") {
                CommuterScreen(
                    routesMap = routesDb,
                    selectedRouteKey = selectedRouteKey,
                    onRouteChanged = { selectedRouteKey = it },
                    hasJoinedLine = hasJoinedLine,
                    onJoinLine = { hasJoinedLine = true },
                    tripStarted = tripStarted,
                    driverArrived = driverArrived
                )
            } else {
                DriverScreen(
                    routesMap = routesDb,
                    selectedRouteKey = selectedRouteKey,
                    onRouteChanged = { selectedRouteKey = it },
                    driverSeats = driverSeats,
                    onSeatsChanged = { driverSeats = it },
                    tripStarted = tripStarted,
                    onStartTrip = { tripStarted = true },
                    driverArrived = driverArrived,
                    onArrived = { driverArrived = true }
                )
            }
        }
    }
}

@Composable
fun CommuterScreen(
    routesMap: Map<String, RouteInfo>,
    selectedRouteKey: String,
    onRouteChanged: (String) -> Unit,
    hasJoinedLine: Boolean,
    onJoinLine: () -> Unit,
    tripStarted: Boolean,
    driverArrived: Boolean
) {
    val currentRoute = routesMap[selectedRouteKey] ?: routesMap.values.first()
    val isWithinGeofence = currentRoute.geofenceMeters <= 20

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Route Selector Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Station Route", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                routesMap.keys.forEach { routeKey ->
                    Button(
                        onClick = { onRouteChanged(routeKey) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRouteKey == routeKey) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(routeKey, color = if (selectedRouteKey == routeKey) Color.White else Color.DarkGray)
                    }
                }
            }
        }

        // Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cash Fare (₱10/km):")
                    Text("₱${currentRoute.fare}", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Distance to Station:")
                    Text("${currentRoute.geofenceMeters} meters", fontWeight = FontWeight.Bold, color = if (isWithinGeofence) Color(0xFF16A34A) else Color.Red)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isWithinGeofence) Color(0xFFDCFCE7) else Color(0xFFFEE2E2), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isWithinGeofence) "Within 20m Geofence (Unlocked)" else "Outside Geofence (Too far)",
                        color = if (isWithinGeofence) Color(0xFF16A34A) else Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action / State Output
        if (!hasJoinedLine) {
            Button(
                onClick = onJoinLine,
                enabled = isWithinGeofence,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Join Virtual Line", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Queue Status", fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                    Text("You are 4th in line at $selectedRouteKey.")

                    if (tripStarted && !driverArrived) {
                        Text("Driver is En Route! ETA: 11 mins", color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                    } else if (driverArrived) {
                        Text("Driver Arrived! Show QR to board.", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("[ QR CODE PASS: #1042 ]", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverScreen(
    routesMap: Map<String, RouteInfo>,
    selectedRouteKey: String,
    onRouteChanged: (String) -> Unit,
    driverSeats: Int,
    onSeatsChanged: (Int) -> Unit,
    tripStarted: Boolean,
    onStartTrip: () -> Unit,
    driverArrived: Boolean,
    onArrived: () -> Unit
) {
    val currentRoute = routesMap[selectedRouteKey] ?: routesMap.values.first()
    val isQuotaFull = currentRoute.currentQuota >= currentRoute.maxQuota

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Route Selector Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Target Route", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                routesMap.keys.forEach { routeKey ->
                    Button(
                        onClick = { onRouteChanged(routeKey) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRouteKey == routeKey) Color(0xFF16A34A) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(routeKey, color = if (selectedRouteKey == routeKey) Color.White else Color.DarkGray)
                    }
                }
            }
        }

        // Driver Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Available Spare Seats:")
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (driverSeats > 1) onSeatsChanged(driverSeats - 1) }) { Text("-") }
                        Text("$driverSeats", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Button(onClick = { if (driverSeats < 6) onSeatsChanged(driverSeats + 1) }) { Text("+") }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Barangay Route Quota:")
                    Text("${currentRoute.currentQuota} / ${currentRoute.maxQuota} Active Drivers", fontWeight = FontWeight.Bold, color = if (isQuotaFull) Color.Red else Color(0xFF2563EB))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isQuotaFull) Color(0xFFFEE2E2) else Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isQuotaFull) "Route Quota Full. Cannot start trip." else "Quota Permitted. You may start trip.",
                        color = if (isQuotaFull) Color(0xFFDC2626) else Color(0xFF16A34A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action flow (Disabled if quota is full)
        if (!tripStarted) {
            Button(
                onClick = onStartTrip,
                enabled = !isQuotaFull,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A),
                    disabledContainerColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Start Trip (Notify Commuters)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        } else if (!driverArrived) {
            Button(
                onClick = onArrived,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("I'm Here (Arrived at Station)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Station Arrival Mode", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                    Text("Scanner Active: Ready to scan passenger QR codes.")
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text("Scan Next Passenger QR")
                    }
                }
            }
        }
    }
}