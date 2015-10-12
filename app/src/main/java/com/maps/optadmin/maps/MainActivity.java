package com.maps.optadmin.maps;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.Button;
import android.view.View.OnClickListener;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AddPlaceRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MainActivity extends FragmentActivity implements OnMapClickListener, OnMapLongClickListener, OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks
        {
            double curLat = 0;
            double curLng = 0;

            private ProgressDialog pDialog;

            // URL to get contacts JSON
            private static String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=0,0&radius=500&types=shoe_store&key=AIzaSyAY_EbeHnnYo08F4Rlh5i83fX_Xr0dD4Jo";


            // JSON Node names
            private static final String TAG_RESULTS = "results";
            private static final String TAG_GEOMETRY = "geometry";
            private static final String TAG_GEOMETRY_LOCATION = "location";
            private static final String TAG_GEOMETRY_LOCATION_LAT = "lat";
            private static final String TAG_GEOMETRY_LOCATION_LNG = "lng";
            private static final String TAG_ICON = "icon";
            private static final String TAG_ID = "id";
            private static final String TAG_NAME = "name";
            private static final String TAG_PLACEID = "place_id";
            private static final String TAG_REFERENCE = "reference";
            private static final String TAG_SCOPE = "scope";

            // contacts JSONArray
            JSONArray nearBySearch = null;

            // Hashmap for ListView
            ArrayList<HashMap<String, String>> searchList;





    //  static final LatLng TutorialsPoint = new LatLng(12.9365037,77.5789278);
    private Button button;

    private GoogleMap googleMap;
    public String TAG = "Juber";

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;


    private static final int GOOGLE_API_CLIENT_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchList = new ArrayList<HashMap<String, String>>();

                // Calling async task to get json
                new GetNearBySearch().execute();
                //  ListView lv = getListView();

                Toast.makeText(MainActivity.this, "YOUR MESSAGE", Toast.LENGTH_LONG).show();
            }


        });

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        //

        try {
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }

            //   googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            //   googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            googleMap.setOnMapClickListener(MainActivity.this);
            googleMap.setOnMapLongClickListener(this);

           /* Marker TP = googleMap.addMarker(new MarkerOptions().
                    position(TutorialsPoint).title("Opteamix"));*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*-------------- START : Zooming Camera Position User----------------*/
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
//        LocationListener locationListener = new MyLocationListener();
//        locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                curLat=location.getLatitude();
                curLng=location.getLongitude();

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(0)                // Sets the orientation of the camera to north
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                // private static final LatLng MELBOURNE = new LatLng(-37.813, 144.962);
                googleMap.clear();
                Marker melbourne = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("Current Location")
                        .snippet("You are Here..!!")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot)));

                //  googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Marker"));
                // googleMap.addMarker(melbourne);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }



        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,10,locationListener);
      /*  if (location != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to north
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            // private static final LatLng MELBOURNE = new LatLng(-37.813, 144.962);
            Marker melbourne = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("Current Location")
                    .snippet("You are Here..!!")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot)));
            //  googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Marker"));
            // googleMap.addMarker(melbourne);


        }*/
 /*-------------- END : Zooming Camera Position User----------------*/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapClick(LatLng point) {

        googleMap.animateCamera(CameraUpdateFactory.newLatLng(point));

        Toast.makeText(getApplicationContext(), point.toString(),
                Toast.LENGTH_LONG).show();


    }

    @Override
    public void onMapLongClick(LatLng point) {

        googleMap.addMarker(new MarkerOptions()
                .position(point)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN
                )));


        final AddPlaceRequest place =
                new AddPlaceRequest(
                        "xyz",
                        new LatLng(point.latitude, point.longitude),
                        "2095",
                        Collections.singletonList(Place.TYPE_SHOE_STORE),
                        "+123456789",
                        Uri.parse("www.abc.com/")
                );

            Places.GeoDataApi.addPlace(mGoogleApiClient, place).setResultCallback(new ResultCallback<PlaceBuffer>() {
                @Override
                public void onResult(PlaceBuffer places) {

                    if (!places.getStatus().isSuccess()) {

                        Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                        Log.e(TAG, "Error Message : " + places.getStatus().getStatusCode());
                        places.release();
                        return;
                    }

                    final Place place = places.get(0);
                    Log.i(TAG, "Place add result: " + place.getName());
                    places.release();

                }


            });


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, Boolean.toString(mGoogleApiClient.isConnected()), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Google Places API Connected", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }



            @Override
            protected void onStart() {
                super.onStart();
                if (mGoogleApiClient != null)
                    mGoogleApiClient.connect();
            }

            @Override
            protected void onResume() {
                super.onResume();

                mGoogleApiClient.connect();
            }

            @Override
            protected void onStop() {
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                super.onStop();
            }

            /**
             * Async task class to get json by making HTTP call
             * */
            private class GetNearBySearch extends AsyncTask<Void, Void, Void> {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    // Showing progress dialog
                    pDialog = new ProgressDialog(MainActivity.this);
                    pDialog.setMessage("Please wait...");
                    pDialog.setCancelable(false);
                    pDialog.show();

                }

                @Override
                protected Void doInBackground(Void... arg0) {
                    // Creating service handler class instance
                    NearbySearchHandler sh = new NearbySearchHandler();
                    Log.d("LAT: ", "> " + curLat );
                    Log.d("LANG: ", "> " + curLng );
                  url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+curLat+","+curLng+"&radius=500&types=shoe_store&key=AIzaSyAY_EbeHnnYo08F4Rlh5i83fX_Xr0dD4Jo";
                    // Making a request to url and getting response
                    Log.d("URLLL: ", "> " + url );
                    String jsonStr = sh.makeServiceCall(url, NearbySearchHandler.GET);

                    Log.d("Response: ", "> " + jsonStr);

                    if (jsonStr != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(jsonStr);

                            // Getting JSON Array node
                            nearBySearch = jsonObj.getJSONArray(TAG_RESULTS);

                            // looping through All Contacts
                            for (int i = 0; i < nearBySearch.length(); i++) {
                                JSONObject c = nearBySearch.getJSONObject(i);

                                String id = c.getString(TAG_ID);
                                String name = c.getString(TAG_NAME);


                                // Phone node is JSON Object
                                JSONObject geometry = c.getJSONObject(TAG_GEOMETRY);
                                JSONObject location = geometry.getJSONObject(TAG_GEOMETRY_LOCATION);
                                String lat = location.getString(TAG_GEOMETRY_LOCATION_LAT);
                                String lng = location.getString(TAG_GEOMETRY_LOCATION_LNG);


                                // tmp hashmap for single contact
                                HashMap<String, String> nearby = new HashMap<String, String>();

                                // adding each child node to HashMap key => value
                                nearby.put(TAG_ID, id);
                                nearby.put(TAG_NAME, name);
                                nearby.put(TAG_GEOMETRY_LOCATION_LAT, lat);
                                nearby.put(TAG_GEOMETRY_LOCATION_LNG, lng);

                                // adding contact to contact list
                                searchList.add(nearby);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("ServiceHandler", "Couldn't get any data from the url");
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    // Dismiss the progress dialog
                    if (pDialog.isShowing())
                        pDialog.dismiss();

//                    googleMap.addMarker(new MarkerOptions()
//                            .position(new LatLng(Double.parseDouble(searchList.get(0).get(TAG_GEOMETRY_LOCATION_LAT)),
//                                    Double.parseDouble(searchList.get(0).get(TAG_GEOMETRY_LOCATION_LNG))))
//                            .title("Another marker"));

                    for(int i=0; i < searchList.size(); i++) {
                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(Double.parseDouble(searchList.get(i).get(TAG_GEOMETRY_LOCATION_LAT)),
                                        Double.parseDouble(searchList.get(i).get(TAG_GEOMETRY_LOCATION_LNG))))
                                .title(searchList.get(i).get(TAG_NAME))
                                .snippet(searchList.get(i).get(TAG_PLACEID))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        Log.d("SearchList: ", "> " + searchList);
                        Log.d("SearchList: ", "> " + searchList.get(i).get(TAG_GEOMETRY_LOCATION_LAT));
                        Log.d("SearchList: ", "> " + searchList.get(i).get(TAG_GEOMETRY_LOCATION_LNG));
                    }
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                 /*   ListAdapter adapter = new SimpleAdapter(
                            MainActivity.this, searchList,
                            R.layout.list_item, new String[] { TAG_NAME, TAG_EMAIL,
                            TAG_PHONE_MOBILE }, new int[] { R.id.name,
                            R.id.email, R.id.mobile });

                    setListAdapter(adapter);*/
                }

            }



        }
