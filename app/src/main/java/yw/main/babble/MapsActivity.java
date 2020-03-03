package yw.main.babble;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    private static final int PERMISSIONS_REQUEST = 2;
    private boolean isMapZoomed = false;
    private Marker locationMarker;
    CameraUpdate cameraUpdate;
    public static final String ZOOM_STATUS = "zoom_status";

    // EMOTION CONSTANTS
    public static final String JOY = "JOY";
    public static final String FEAR = "FEAR";
    public static final String SADNESS = "SADNESS";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String TENTATIVE = "TENTATIVE";
    public static final String ANALYTICAL = "ANALYTICAL";
    public static final String CONFIDENT = "CONFIDENT";
    public static final String ANGER = "ANGER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        checkPermissions();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // TODO: Add floating action bar to create a new note

        // If you have already been restarted
        if (savedInstanceState != null) {
            isMapZoomed = savedInstanceState.getBoolean(ZOOM_STATUS);
            // TODO: check if markers clear or not
        }
    }


    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(ZOOM_STATUS, isMapZoomed);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // TODO: Async task to get all the emotions/locations
    }

    private void initLocationManager(){
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(criteria, true);
            // Log.d provider will print GPS
            locationManager.requestLocationUpdates(provider, 0, 0, this);
            Location location = locationManager.getLastKnownLocation(provider);
            // One situation to use callback manually
            onLocationChanged(location);
        }
        catch (SecurityException e) {}
    }

    public void onLocationChanged(Location location) {
        if (location == null) return;
        // location object gets you current latitude and long of phone
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        if (locationMarker != null)
                locationMarker.remove();

        // Add a marker where you are
        LatLng myLocation = new LatLng(lat, lng);
        locationMarker = mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));

        // Zoom if you need to
        if (!isMapZoomed) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLocation, 21);
            isMapZoomed = true;
        } else {
            cameraUpdate = CameraUpdateFactory.newLatLng(myLocation);
        }
        // Move the camera
        mMap.animateCamera(cameraUpdate);
    }

    public void onDestroy(){
        super.onDestroy();
        if(locationManager != null)
            locationManager.removeUpdates(this);
    }

    public void onProviderEnabled(String provider) {}
    public void onProviderDisabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle bundle) {}

    public void checkPermissions(){
        if(Build.VERSION.SDK_INT < 23) return;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
        else
            initLocationManager();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                initLocationManager();
        }
    }

    // Create a marker based on the emotion
    public MarkerOptions createMarker(LatLng latLng, String emotion) {
        switch (emotion) {
            case JOY:
                return new MarkerOptions()
                        .position(latLng)
                        .title(JOY)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.joy));
            case SADNESS:
                return new MarkerOptions()
                        .position(latLng)
                        .title(SADNESS)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.sadness));
            case FEAR:
                return new MarkerOptions()
                        .position(latLng)
                        .title(FEAR)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.fear));
            case ANGER:
                return new MarkerOptions()
                        .position(latLng)
                        .title(ANGER)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.anger));
            case TENTATIVE:
                return new MarkerOptions()
                        .position(latLng)
                        .title(TENTATIVE)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.tentative));
            case ANALYTICAL:
                return new MarkerOptions()
                        .position(latLng)
                        .title(ANALYTICAL)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.analytical));
            case CONFIDENT:
                return new MarkerOptions()
                        .position(latLng)
                        .title(CONFIDENT)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.confident));
                default:
                    return new MarkerOptions()
                            .position(latLng)
                            .title(UNKNOWN)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.unknown));
        }
    }

}
