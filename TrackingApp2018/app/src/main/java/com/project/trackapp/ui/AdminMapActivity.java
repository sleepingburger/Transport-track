package com.project.trackapp.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.project.trackapp.R;
import com.project.trackapp.model.ClusterMarker;
import com.project.trackapp.model.PolylineData;
import com.project.trackapp.model.User;
import com.project.trackapp.model.UserLocation;
import com.project.trackapp.util.MyClusterManagerRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.project.trackapp.Constants.MAPVIEW_BUNDLE_KEY;

public class AdminMapActivity extends FragmentActivity implements OnMapReadyCallback ,
        GoogleMap.OnInfoWindowClickListener, ClusterManager.OnClusterInfoWindowClickListener<ClusterMarker> {


    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;


    private LatLng userDestination;

    private static final String TAG = "AdminMapActivity";
    private GoogleMap mGoogleMap;

    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();



    FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private UserLocation mUserPosition;
    private SupportMapFragment mMapView;
    private MapView mMap;
    private GeoApiContext mGeoApiContext;
    private LatLngBounds mMapBoundary;
    //var
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    private ArrayList<UserLocation> finaUserLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_map);

        if (mUserLocations.size() == 0) { // make sure the list doesn't duplicate by navigating back
            if (getIntent().getExtras() != null) {
                final ArrayList<User> users = getIntent().getParcelableArrayListExtra(getString(R.string.intent_user_list));
                mUserList.addAll(users);
                final ArrayList<UserLocation> locations = getIntent().getParcelableArrayListExtra(getString(R.string.intent_user_locations));
                mUserLocations.addAll(locations);
                Log.d(TAG, "onCreate: " + mUserLocations.get(0).getGeo_point());
            }
        }





        mUserPosition = mUserLocations.get(0);
        Toast.makeText(this, "Google map", Toast.LENGTH_SHORT).show();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        initGoogleMap();
    }

    private void initGoogleMap() {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.






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


    private void retrieveUserLocations() {
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users.");
        try{
            for(final ClusterMarker clusterMarker: mClusterMarkers){
                DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                        .collection(getString(R.string.collection_user_locations))
                        .document(clusterMarker.getUser().getUid());

                userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            final UserLocation updatedUserLocation = task.getResult().toObject(UserLocation.class);

                            // update the location
                            for (int i = 0; i < mClusterMarkers.size(); i++) {
                                try {
                                    if (mClusterMarkers.get(i).getUser().getUid().equals(updatedUserLocation.getUser().getUid())) {

                                        LatLng updatedLatLng = new LatLng(
                                                updatedUserLocation.getGeo_point().getLatitude(),
                                                updatedUserLocation.getGeo_point().getLongitude()
                                        );

                                        mClusterMarkers.get(i).setPosition(updatedLatLng);
                                        mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));
                                    }
                                } catch (NullPointerException e) {
                                    Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }
        }catch (IllegalStateException e){
            Log.e(TAG, "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage() );
        }
    }


    private void addMapMarkers() {
        if(mGoogleMap != null){
            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(this, mGoogleMap);
            }
            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        this,
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            mGoogleMap.setOnInfoWindowClickListener(this);

            int i = 0;
            for(UserLocation userLocation: mUserLocations){
                Log.d(TAG, "addMapMarkers: "+userLocation.getUser().getEmail());
                //Log.d(TAG, "addMapMarkers: location: " + userLocation.getGeo_point().toString());
                try{
                    String snippet = "";
                    Log.d(TAG, "addMapMarkers: ?" + userLocation.getUser());
                    snippet = "tap to view route";
                    i++;
                    int avatar = R.drawable.ic_main_icon; // set the default avatar

                    ClusterMarker newCMarker = new ClusterMarker(
                            new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()),
                            userLocation.getUser().getEmail(),snippet,userLocation.getUser(),avatar,userLocation.getUser().getUid());
                    mClusterManager.addItem(newCMarker);
                    mClusterMarkers.add(newCMarker);

                }catch (NullPointerException e){
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage() );
                }

            }
            mClusterManager.cluster();
            mGoogleMap.setOnMarkerClickListener(mClusterManager);
            setCameraView();
        }
    }

    private void setCameraView() {
        // Set a boundary to start
        double bottomBoundary = mUserPosition.getGeo_point().getLatitude() - .05;
        double leftBoundary = mUserPosition.getGeo_point().getLongitude() - .05;
        double topBoundary = mUserPosition.getGeo_point().getLatitude() + .05;
        double rightBoundary = mUserPosition.getGeo_point().getLongitude() + .05;
        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        addMapMarkers();

    }


    @Override
    public void onInfoWindowClick(final Marker marker) {

        Log.d(TAG, "onInfoWindowClick: "+ marker.getId());
        if(marker.getSnippet().equals("This is you")){
            marker.hideInfoWindow();
        }
        else{
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Show current route?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            //recalculateDirection(new GeoPoint(marker.getPosition().latitude,marker.getPosition().longitude));
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }

    }


    /*
    private void selectedMarker(Marker marker) {
        DocumentReference docRef = mDb.collection("UsersDestination")
                .document()

    }


    private void recalculateDirection(final GeoPoint geoPoint) {
        final com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                geoPoint.getLatitude(),
                geoPoint.getLongitude()
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

                        DirectionsRoute route = result.routes[index];
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
                        //onPolylineClick(polyline);
                        PolylineData p = mPolyLinesData.get(0);
//                            marker.remove();
                        Toast.makeText(MapsActivity.this, "A:SLKDJAS:LKDJASDASD:LASKDJAS:LDKAJSD:ASLDKJ" + p.getLeg().endLocation.lat, Toast.LENGTH_SHORT).show();
                        MarkerOptions m = new MarkerOptions()
                                .position(new LatLng(p.getLeg().endLocation.lat,p.getLeg().endLocation.lat))
                                .title("Route: " + index)
                                .snippet("Duration: " + p.getLeg().arrivalTime)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        mMap.addMarker(m);
                    }
                });
            }
            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }


*/


    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        startUserLocationsRunnable(); // update user locations every 'LOCATION_UPDATE_INTERVAL'
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
    public void onPause() {
        mMapView.onPause();
        stopLocationUpdates(); // stop updating user locations
        super.onPause();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    @Override
    public void onClusterInfoWindowClick(Cluster<ClusterMarker> cluster) {

        Log.d(TAG, "onClusterInfoWindowClick: "+cluster.toString());
    }
}
