package com.project.trackapp.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.project.trackapp.R;
import com.project.trackapp.model.PolylineData;
import com.project.trackapp.model.UserLocation;
import com.project.trackapp.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnPolylineClickListener{

    private GoogleMap mMap;
    private static String tripDuration;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    private boolean setRoute = false;
    private boolean setDestination = false;
    private MarkerOptions destinationMarker;
    private LatLng destination;
    private static int index = 0;
    private static final String TAG = "MapsActivity";
    private LatLngBounds mMapBoundary;
    private LatLng userLocation;
    FirebaseFirestore mFirebaseFirestoreRef;

    Marker marker;

    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    private SupportMapFragment mMapView;

    private UserLocation mUserLocation;
    private GeoApiContext mGeoApiContext;
    private Marker userMarker;
    private Marker blueMarker;

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocation();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }
    private void stopLocationUpdates() {
        mHandler.removeCallbacks(mRunnable);
    }
    private void retrieveUserLocation(){
        DocumentReference userLocationRef = mFirebaseFirestoreRef
                .collection(getString(R.string.collection_user_locations))
                .document(FirebaseAuth.getInstance().getUid());
        userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    final UserLocation updatedUserLocation =task.getResult().toObject(UserLocation.class);
                    Log.d(TAG, "onComplete: "+ updatedUserLocation.getUser().getEmail());
                    LatLng updatedLatLng = new LatLng(
                            updatedUserLocation.getGeo_point().getLatitude(),
                            updatedUserLocation.getGeo_point().getLongitude()
                    );
                    if(userMarker != null) {
                        userMarker.setPosition(updatedLatLng);
                    }
                    mUserLocation = updatedUserLocation;
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        /*Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Maps");
        */


        mFirebaseFirestoreRef = FirebaseFirestore.getInstance();

        //success!
        mUserLocation = getIntent().getParcelableExtra("CurrentLocation");

        initMap();
    }



    /*
        CALLBACK FUNCTIONSS
     */
    private void initMap(){
        Log.d(TAG, "initMap: initializing");
        mMapView = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapView.getMapAsync(this);
        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_api_key))
                    .build();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready!");
        mMap = googleMap;
        moveCamera(mUserLocation);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnPolylineClickListener(this);
    }







    private void moveCamera(UserLocation mUserPosition){
        double bottomBoundary = mUserPosition.getGeo_point().getLatitude() - .1;
        double leftBoundary = mUserPosition.getGeo_point().getLongitude() - .1;
        double topBoundary = mUserPosition.getGeo_point().getLatitude() + .1;
        double rightBoundary = mUserPosition.getGeo_point().getLongitude() + .1;
        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
        MarkerOptions marker = new MarkerOptions()
                .position(new LatLng(
                        mUserPosition.getGeo_point().getLatitude(),
                        mUserPosition.getGeo_point().getLongitude()
                ))
                .title("This is you")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        userMarker = mMap.addMarker(marker);

        checkDestinationAndRoute();
    }
    private void checkDestinationAndRoute() {


        DocumentReference destinationRef = mFirebaseFirestoreRef
                .collection("UsersDestination")
                .document(FirebaseAuth.getInstance().getUid());
        destinationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    GeoPoint geoPoint = task.getResult().getGeoPoint("destination");
                    int route = task.getResult().getLong("route_index").intValue();
                    if(task.getResult().getBoolean("hasDestination")){
                        setDestination = true;
                        if(task.getResult().getLong("route_index").intValue() != -1) {
                            recalculateDirection(geoPoint, route);
                            setDestinationMarker(geoPoint
                                    ,task.getResult().getString("duration")
                                    ,task.getResult().getLong("route_index").intValue() + 1);
                            setRoute = true;
                        }
                        else
                        {
                            Toast.makeText(MapsActivity.this, "No selected route", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        setDestination = false;
                        setDestination = false;
                        Toast.makeText(MapsActivity.this, "No selected destination and route", Toast.LENGTH_SHORT).show();
                        Toast.makeText(MapsActivity.this, "Long tap on the map to select destination", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void recalculateDirection(final GeoPoint geoPoint,final int routeIndex) {

        final com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                geoPoint.getLatitude(),
                geoPoint.getLongitude()
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
        directions.alternatives(true);


        //setting the origin , by user location
        directions.origin(
                new com.google.maps.model.LatLng(
                        mUserLocation.getGeo_point().getLatitude(),
                        mUserLocation.getGeo_point().getLongitude()
                )
        );

        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(final DirectionsResult result) {
                Log.d(TAG, "onResult: successfully retrieved directions.");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: result routes: " + result.routes.length);
                        if(mPolyLinesData.size() > 0){
                            for(PolylineData polylineData: mPolyLinesData){
                                polylineData.getPolyline().remove();
                            }
                            mPolyLinesData.clear();
                            mPolyLinesData = new ArrayList<>();
                        }

                            DirectionsRoute route = result.routes[routeIndex];
                            Log.d(TAG, "run: leg: " + route.legs[0].toString());
                            List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                            List<LatLng> newDecodedPath = new ArrayList<>();

                            // This loops through all the LatLng coordinates of ONE polyline.
                            for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                                newDecodedPath.add(new LatLng(
                                        latLng.lat,
                                        latLng.lng
                                ));
                            }

                            Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                            polyline.setColor(ContextCompat.getColor(MapsActivity.this, R.color.palette2));
                            mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));
                            //onPolylineClick(polyline);
                        PolylineData p = mPolyLinesData.get(0);
//
                    }
                });

            }
            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });


    }
    private void setDestinationMarker(GeoPoint g,String duration,int route){
        destinationMarker = new MarkerOptions()
                .position(new LatLng(g.getLatitude(),g.getLongitude()))
                .title("Route: " + route)
                .snippet("Duration: " + duration)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        blueMarker = mMap.addMarker(destinationMarker);
    }

    private void calculateDirections(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mUserLocation.getGeo_point().getLatitude(),
                        mUserLocation.getGeo_point().getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
//                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
//                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
//                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
//                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                Log.d(TAG, "onResult: successfully retrieved directions.");
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if(mPolyLinesData.size() > 0){
                    for(PolylineData polylineData: mPolyLinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolyLinesData.clear();
                    mPolyLinesData = new ArrayList<>();
                }
                double duration = 99999999;
                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(MapsActivity.this, R.color.palette2));
                    polyline.setClickable(true);
                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));

                    double tempDuration = route.legs[0].duration.inSeconds;
                    if(tempDuration < duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                    }
                }
            }
        });
    }


    @Override
    public void onPolylineClick(Polyline polyline) {
        if(!setRoute) {
            index = 0;
            for (PolylineData polylineData : mPolyLinesData) {
                index++;
                Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
                if (polyline.getId().equals(polylineData.getPolyline().getId())) {
                    polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.orangy));
                    polylineData.getPolyline().setZIndex(1);
                    LatLng endLocation = new LatLng(
                            polylineData.getLeg().endLocation.lat,
                            polylineData.getLeg().endLocation.lng
                    );
                    Toast.makeText(this, ""+polylineData.getLeg().duration.toString(), Toast.LENGTH_SHORT).show();
                    if(marker != null) {
                        marker.remove();
                    }
                    tripDuration = polylineData.getLeg().duration.toString();
                    MarkerOptions m = new MarkerOptions()
                            .position(endLocation)
                            .title("Route: " + index)
                            .snippet("Duration: " + tripDuration)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                    marker = mMap.addMarker(m);
                    marker.showInfoWindow();



                } else {
                    polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.palette1));
                    polylineData.getPolyline().setZIndex(0);
                }
            }
        }
        else{

        }
    }


    @Override
    public void onMapLongClick(final LatLng latLng) {
        if(!setRoute) {
            if (!setDestination) {
                destination = latLng;
                if (destinationMarker == null) {
                    destinationMarker = new MarkerOptions()
                            .position(latLng)
                            .title("Destination")
                            .snippet("Tap to view possible routes")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    blueMarker = mMap.addMarker(destinationMarker);
                } else {
                    blueMarker.setVisible(true);
                    blueMarker.setPosition(latLng);
                }
                setDestination = true;
            } else {
                showAlertChangeDestination(latLng);
            }
        }
        else
        {
            showAlertChangeDestination(latLng);
        }

    }

    private void showAlertChangeDestination(final LatLng latLng){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Change to new destination?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMap.clear();
                        MarkerOptions newDestination = new MarkerOptions()
                                .position(latLng)
                                .title("Destination")
                                .snippet("Tap to view routes to this location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        MarkerOptions refUserMarker = new MarkerOptions()
                                .position(new LatLng(mUserLocation.getGeo_point().getLatitude(), mUserLocation.getGeo_point().getLongitude()))
                                .title("This is you")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                        DocumentReference destinationRef = mFirebaseFirestoreRef
                                .collection("UsersDestination")
                                .document(FirebaseAuth.getInstance().getUid());

                        Map<String,Object> hash = new HashMap<>();

                        hash.put("user_location",mUserLocation);
                        hash.put("destination",new GeoPoint(latLng.latitude,latLng.latitude));
                        hash.put("route_index",-1);
                        hash.put("duration","null");
                        hash.put("hasDestination",true);
                        destinationRef.set(hash).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d(TAG, "onComplete: saving users destination");
                                    Toast.makeText(MapsActivity.this, "Save as current destination", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        setDestination = true;
                        setRoute = false;
                        blueMarker = mMap.addMarker(newDestination);
                        userMarker = mMap.addMarker(refUserMarker);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        if(!setRoute) {
            if (marker.getTitle().contains("Route")) {
                setAsSelectedRoute(marker);
                return;
            }
            if (marker.getTitle().equals("This is you")) {
                marker.hideInfoWindow();
                return;
            }
            Toast.makeText(this, "marker " + marker.getTitle(), Toast.LENGTH_SHORT).show();
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Determine routes in this location?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            blueMarker.setVisible(false);
                            calculateDirections(marker);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }

    }

    private void setAsSelectedRoute(final Marker marker) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Set this as selected route?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setRoute = true;
                        DocumentReference destinationRef = mFirebaseFirestoreRef
                                .collection("UsersDestination")
                                .document(FirebaseAuth.getInstance().getUid());
                        Map<String,Object> hash = new HashMap<>();
                        hash.put("destination",new GeoPoint(marker.getPosition().latitude,marker.getPosition().longitude));
                        hash.put("user_location",mUserLocation);
                        hash.put("route_index",index-1);
                        hash.put("duration",tripDuration);
                        hash.put("hasDestination",true);
                        destinationRef.set(hash).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d(TAG, "onComplete: saving users destination");
                                    Toast.makeText(MapsActivity.this, "Save as current destination", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUserLocationsRunnable();
        mMapView.onResume();
    }
    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }
    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    @Override
    public void onDestroy() {

        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    public void onPause() {
        mMapView.onPause();
        stopLocationUpdates(); // stop updating user locations
        super.onPause();
    }

        /*
        UTIL METHOD!
     */
//    private void getDeviceLocation(){
//        Log.d(TAG, "getDeviceLocation: a;sdlkfjas;lkdfjasdfl;asjdfa;slkfjasl;kfdjas;df");
//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//        try{
//            if(mLocationPermissionGranted){
//                final Task location = mFusedLocationProviderClient.getLastLocation();
//                location.addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if(task.isSuccessful()){
//                            Log.d(TAG, "onComplete: Success!");
//                            Location currentLocation = (Location) task.getResult();
//                            marker = new MarkerOptions()
//                                    .position(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()))
//                                    .title(((UserClient)(getApplication())).getUser().getEmail());
//                            mMap.addMarker(marker);
//                            //moveCamera(currentLocation);
//                        }
//                        else{
//                            Log.d(TAG, "onComplete: current Location is null");
//                            Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//
//        }catch(SecurityException e){
//            Log.d(TAG, "getDeviceLocation: SecurityException " + e.getMessage());
//            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
//        }
//    }
}
