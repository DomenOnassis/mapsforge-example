package com.example.mapsforge_application

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.location.FusedLocationProviderClient
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.ExternalRenderTheme
import org.mapsforge.map.rendertheme.InternalRenderTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: org.mapsforge.map.android.view.MapView
    private var tileRendererLayer: TileRendererLayer? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val themes = listOf("default.xml", "motorider.xml", "motorider-dark.xml", "osmarender.xml")

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AndroidGraphicFactory.createInstance(application)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)

        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)

        requestLocationPermissions()

        // Copy the map file from assets to external storage
        val mapFile = getMapFile("slovenia.map")
        if (mapFile == null || !mapFile.exists()) {
            Log.e("MainActivity", "Map file not found!")
            return
        }

        // Create a tile cache
        val tileCache = AndroidUtil.createTileCache(
            this,
            "mapcache",
            mapView.model.displayModel.tileSize,
            1f,
            mapView.model.frameBufferModel.overdrawFactor
        )

        // Create a TileRendererLayer with the map file
        tileRendererLayer = TileRendererLayer(
            tileCache,
            MapFile(mapFile),
            mapView.model.mapViewPosition,
            AndroidGraphicFactory.INSTANCE
        ).apply {
            setXmlRenderTheme(InternalRenderTheme.OSMARENDER) //InternalRenderTheme.DEFAULT
        }

        // Add the TileRendererLayer to the MapView
        mapView.layerManager.layers.add(tileRendererLayer)
        mapView.setBuiltInZoomControls(true)

        getCurrentLocation()
    }

    private fun getThemeFilePath(themeName: String): String {
        val themeFile = File(getExternalFilesDir("themes"), themeName)
        if (!themeFile.exists()) {
            try {
                assets.open("themes/$themeName").use { input ->
                    FileOutputStream(themeFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error copying theme file: ${e.message}")
            }
        }
        return themeFile.absolutePath
    }


    override fun onDestroy() {
        super.onDestroy()
        mapView.destroyAll()
        AndroidGraphicFactory.clearResourceMemoryCache()
    }

    private fun getMapFile(fileName: String): File? {
        val file = File(getExternalFilesDir(null), fileName)
        if (!file.exists()) {
            try {
                assets.open("maps/$fileName").use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error copying map file: ${e.message}")
                return null
            }
        }
        return file
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e("MainActivity", "Location permission not granted!")
            }
        }
    }


    private fun requestLocationPermissions() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLatLong = LatLong(location.latitude, location.longitude)

                // Add a marker for the user's location
                val userMarker = org.mapsforge.map.layer.overlay.Marker(
                    userLatLong,
                    AndroidGraphicFactory.convertToBitmap(
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_user_marker, null)!!
                    ),
                    0,
                    0
                )

                // Add marker to the map and center the view
                mapView.layerManager.layers.add(userMarker)
                mapView.model.mapViewPosition.center = userLatLong
                mapView.model.mapViewPosition.zoomLevel = 15.toByte()
            } else {
                Log.e("MainActivity", "Location is null. Unable to display user location.")
            }
        }.addOnFailureListener { exception ->
            Log.e("MainActivity", "Failed to get location: ${exception.message}")
        }
    }

}
