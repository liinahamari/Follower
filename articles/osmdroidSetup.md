## Setting up osmdroid and drawing route overlay

First of all, add ```osmdroid``` as a dependency in your app's gradle file:
```implementation 'org.osmdroid:osmdroid-android:6.1.8'``` (version might vary, check [official page](https://github.com/osmdroid/osmdroid) to be aware of latest version)

Also, for such functionality as drawing routes you'll need "bonus pack" dependency:
```implementation 'com.github.MKergall:osmbonuspack:6.6.0'``` ([official page](https://github.com/MKergall/osmbonuspack))

Core functionality of showing the map won't work until configuration provides Application ID and path for osmdroid files. The better place for configuration setup is your Application's subclass:
```
    class MyApp: Application() {
        ...
        override fun onCreate() {
             with(org.osmdroid.config.Configuration.getInstance()) {
                  userAgentValue = BuildConfig.APPLICATION_ID
                  osmdroidBasePath = getExternalFilesDir(null)
             }
        }
    }
```

In view container's (such as Fragment) layout, you'll need to declare MapView as a key element:
```        <org.osmdroid.views.MapView
               android:id="@+id/map"
               android:layout_width="match_parent"
               android:layout_height="match_parent"
               android:contentDescription="@string/desc_map_view"/>
```

Allocate field for ```MapController``` in your Fragment (in particular, controller will help us to center the map after route drawing):
```
    class MapFragment: Fragment() {
        ...
        private lateinit var mapController: IMapController
    }
```

General map setup:
```
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = inflater.inflate(R.layout.fragment_map, container, false)
        with(layout.findViewById(R.id.map) as MapView) {
            mapController = controller
            isTilesScaledToDpi = true
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
        }
        return layout.rootView
    }
```

Optionally, you can invert colors in case your app supports dark theme:
```
    if (userPreferenceNightMode == MODE_NIGHT_YES) {
        overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
    }
```

... and add CompassOverlay (but, to be honest, on my device sensors doesn't provide accurate data for compass to show valid information):
```
    overlays.add(CompassOverlay(context, InternalCompassOrientationProvider(context), this).apply {
        enableCompass()
        setCompassCenter(36f, 36f + (0 / requireContext().resources.displayMetrics.density))
    })
```

### How to draw route overlay

<div align="center"><img src="screenshots/line_preview.png" border="10"></div>

To draw route as ordinary overlay represented by the line, you'll need "bonus pack" library mentioned above (com.github.MKergall:osmbonuspack). 
Imagine, you have a set of coordinates (paired longitudes and latitudes, ```Double```s in most cases). First of all, coordinates you have needed to be presented as ```Geopoint``` object, which osmdroid operate:
```
    yourDataSource.getCoondinatesSet<List<Pair<Double, Double>>>()
        .map { GeoPoint(it.latitude, it.longitude, it.altitude /*optional, might be 0.0*/) }
        .toList()
        .map { OSRMRoadManager(context).getRoad(ArrayList(it)) 
    }
```
Beware! getRoad() function uses Internet to work and must be invoked in background! 

When the Road is Ready, show it in UI:
```
    map.overlays.add(RoadManager.buildRoadOverlay(road))
        map.invalidate()
        with(road.mRouteHigh.first()) {
        centerMap(latitude, longitude)
    }
``` 

### How to represent a track as set of markers
<div align="center">
<img src="screenshots/set_of_markers.png" border="10"/></div>

Sometimes it might be handy to show trace as set of markers.
```
    yourDataSource.getCoondinatesSet<List<Pair<Double, Double>>>()
        .subscribe { coordinatesList ->
            coordinatesList.map { wayPoint -> 
                        Marker(mapView).apply {
                            position = GeoPoint(wayPoint.lat, wayPoint.long)
                            icon = yourIconDrawable
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                    }
                    .apply { mapView.overlays.addAll(this) }
                with(coordinatesList.first()) {
                    centerMap(first, second)
                }       
        }
```
