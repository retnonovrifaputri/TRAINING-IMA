package com.eno.userss.mapstraining;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.eno.userss.mapstraining.helper.DirectionMapsV2;
import com.eno.userss.mapstraining.helper.GPStrack;
import com.eno.userss.mapstraining.helper.HeroHelper;
import com.eno.userss.mapstraining.helper.MyFunction;
import com.eno.userss.mapstraining.model.Distance;
import com.eno.userss.mapstraining.model.Duration;
import com.eno.userss.mapstraining.model.LegsItem;
import com.eno.userss.mapstraining.model.ResponseWayPoint;
import com.eno.userss.mapstraining.model.RoutesItem;
import com.eno.userss.mapstraining.network.ApiService;
import com.eno.userss.mapstraining.network.RetrofitConfig;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import android.content.Intent;

public class MapsActivity extends MyFunction implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION = 3;
    private static final int REQ_AWAL = 23;
    private static final int REQ_AKHIR = 44 ;
    @BindView(R.id.edtawal)
    EditText edtawal;
    @BindView(R.id.edtakhir)
    EditText edtakhir;
    @BindView(R.id.textjarak)
    TextView textjarak;
    @BindView(R.id.textwaktu)
    TextView textwaktu;
    @BindView(R.id.textharga)
    TextView textharga;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    @BindView(R.id.btnlokasiku)
    Button btnlokasiku;
    @BindView(R.id.btnpanorama)
    Button btnpanorama;
    @BindView(R.id.linearbottom)
    LinearLayout linearbottom;
    @BindView(R.id.spinmode)
    Spinner spinmode;
    @BindView(R.id.relativemap)
    RelativeLayout relativemap;
    @BindView(R.id.frame1)
    FrameLayout frame1;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private GPStrack gps;
    private double lat;
    private double lon;
    private String name_location;
    private LatLng lokasiku;
    private List<RoutesItem>routes;
    private List<LegsItem>legs;
    private Intent intent;
    private Distance distance;
    private Duration duration;
    private String dataGaris;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        cekStatus();

    }

    private void cekStatus() {
        final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Gps already enabled", Toast.LENGTH_SHORT).show();
            //     finish();
        }
        // Todo 3 Location Already on  ... end

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Gps not enabled", Toast.LENGTH_SHORT).show();
            //todo 4 menampilkan popup untuk mengaktifkan GPS (allow or not)
            enableLoc();
        }
    }

    private void enableLoc() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MapsActivity.this, REQUEST_LOCATION);
                                finish();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
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

        myLocation();

        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        110);


            }
            return;
        }
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void myLocation() {
        gps = new GPStrack(this);
        if (gps.canGetLocation()&& mMap!= null){
            lat = gps.getLatitude();
            lon = gps.getLongitude();
            name_location = convertLocation(lat, lon);
            Toast.makeText(this, "lat :"+ lat +"\n lon :+lon", Toast.LENGTH_SHORT).show();
            lokasiku = new LatLng(lat,lon);
            mMap.addMarker(new MarkerOptions().position(lokasiku).title(name_location)).
                    setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_map));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiku,17));
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
    }

    private String  convertLocation(double lat, double lon) {
        name_location = null;
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> list = geocoder.getFromLocation(lat, lon, 1);
            if (list != null && list.size() > 0) {
                name_location = list.get(0).getAddressLine(0) + "" + list.get(0).getCountryName();

                //fetch data from addresses
            } else {
                Toast.makeText(this, "kosong", Toast.LENGTH_SHORT).show();
                //display Toast message
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name_location;
    }

    @OnClick({R.id.edtawal, R.id.edtakhir, R.id.btnlokasiku, R.id.btnpanorama})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.edtawal:
                try {
                    intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(MapsActivity.this);
                    startActivityForResult(intent, REQ_AWAL);
                }catch (GooglePlayServicesNotAvailableException e){
                    e.printStackTrace();
                }catch (GooglePlayServicesRepairableException e){
                    e.printStackTrace();
                }
                break;
            case R.id.edtakhir:
                try {
                    intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(MapsActivity.this);
                    startActivityForResult(intent, REQ_AKHIR);
                }catch (GooglePlayServicesNotAvailableException e){
                    e.printStackTrace();
                }catch (GooglePlayServicesRepairableException e){
                    e.printStackTrace();
                }
                break;
            case R.id.btnlokasiku:
                myLocation();
                break;
            case R.id.btnpanorama:
                aksespanorama();
                break;
        }
    }

    private void aksespanorama() {
        relativemap.setVisibility(View.GONE);
        frame1.setVisibility(View.VISIBLE);
        SupportStreetViewPanoramaFragment panorama = (SupportStreetViewPanoramaFragment) getSupportFragmentManager()
                .findFragmentById(R.id.panorama);
        panorama.getStreetViewPanoramaAsync(new OnStreetViewPanoramaReadyCallback() {
            @Override
            public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
                streetViewPanorama.setPosition(lokasiku);
            }
        });
    }

    //untuk menangkap result dari suatu aksi internet
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Place place = PlaceAutocomplete.getPlace(this, data);
        //getlat dan get lon
        if (requestCode == REQ_AWAL && resultCode == RESULT_OK) {
            lat = place.getLatLng().latitude;
            lon = place.getLatLng().longitude;
            name_location = place.getName().toString();
            edtawal.setText(name_location);
            mMap.clear();
            addmarker(lat, lon);
        } else if (requestCode == REQ_AKHIR && resultCode == RESULT_OK) {
            lat = place.getLatLng().latitude;
            lon = place.getLatLng().longitude;
            name_location = place.getName().toString();
            edtakhir.setText(name_location);
            mMap.clear();
            addmarker(lat, lon);
            aksesrute();
        }

    }

    private void addmarker(double lat, double lon) {
        lokasiku = new LatLng(lat, lon);
        name_location = convertLocation(lat, lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiku,15));
        mMap.addMarker(new MarkerOptions().position(lokasiku).title(name_location));
    }


    private void aksesrute() {
        ApiService api = RetrofitConfig.getInstanceRetrofit();
        String key = getString(R.string.google_maps_key);
        //mengirim request
        Call<ResponseWayPoint> wayPointCall = api.request_route(
                edtawal.getText().toString(),
                edtakhir.getText().toString(),
                key
        );
        //tangkap response dari database / json
        wayPointCall.enqueue(new Callback<ResponseWayPoint>() {
            public void onResponse(Call<ResponseWayPoint> call, Response<ResponseWayPoint> response) {
                if (response.isSuccessful()) {
                    String status = response.body().getStatus();
                    if (status.equals("OK")) {
                        routes = response.body().getRoutes();
                        legs = routes.get(0).getLegs();
                        distance = legs.get(0).getDistance();
                        duration = legs.get(0).getDuration();

                        textjarak.setText(distance.getText());
                        textwaktu.setText(duration.getText());

                        double harga = Math.ceil(Double.valueOf(distance.getValue() / 1000));
                        double total = harga * 1000;
                        textharga.setText("Rp. " + HeroHelper.toRupiahFormat2(String.valueOf(total)));
                        DirectionMapsV2 mapsV2 = new DirectionMapsV2(MapsActivity.this);
                        dataGaris = routes.get(0).getOverviewPolyline().getPoints();
                        mapsV2.gambarRoute(mMap, dataGaris);
                    } else {
                        mytoast("Gagal menampilkan data");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseWayPoint> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Cek koneksi anda", Toast.LENGTH_SHORT).show();
            }


        });
    }
}
