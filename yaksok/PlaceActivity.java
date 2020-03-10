package com.example.yaksok;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlaceActivity extends AppCompatActivity implements OnMapReadyCallback  {

    private static final String TAG = PlaceActivity.class.getSimpleName();
    private GoogleMap mMap;
    private Button btnsearch, btnsave, btnnaver, btngoogle;
    private FloatingActionButton btnres, btncafe;
    private EditText editText;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location myLocation;
    private String apiKey;

    // For findplacefromtext
    public double place_lat, place_lng, place_rating;
    public String place_name, place_address, place_id;;

    // For getnearbyplaces
    public double camera_lat, camera_lng;
    ArrayList<Double> lat_list, lng_list;
    ArrayList<String> name_list, id_list, vicinity_list;
    ArrayList<String[]> rating_list;

    ArrayList<Marker> markers_list;

    private final LatLng mDefaultLocation = new LatLng(37.450601, 126.657318);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_place);
        editText = findViewById(R.id.editText);
        btnsearch = findViewById(R.id.btn_search);
        btnsave = findViewById(R.id.btn_save);
        btnnaver = findViewById(R.id.btn_naver);
        btngoogle = findViewById(R.id.btn_google);
        btnres = findViewById(R.id.btn_res);
        btncafe = findViewById(R.id.btn_cafe);
        apiKey = getString(R.string.google_map_key);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);


        // 키보드 enter 키 action
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER) {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    btnsearch.callOnClick();
                }
                return false;
            }
        });

        // 키보드 검색 action
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        btnsearch.callOnClick();
                        break;
                }
                return true;
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        lat_list = new ArrayList<>();
        lng_list = new ArrayList<>();
        name_list = new ArrayList<>();
        id_list = new ArrayList<>();
        vicinity_list = new ArrayList<>();
        rating_list = new ArrayList<>();
        markers_list = new ArrayList<>();
    }

    // Map이 사용 가능 하면 실행
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Camera 이동 event
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                camera_lat = mMap.getCameraPosition().target.latitude;
                camera_lng = mMap.getCameraPosition().target.longitude;
            }
        });
        // Marker click event
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker m) {
                place_lat = m.getPosition().latitude;
                place_lng = m.getPosition().longitude;
                place_name = m.getTitle();
                if(m.getTag()!=null)
                    place_id = m.getTag().toString();
                return false;
            }
        });
        // Button event
        btncafe.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                NearbyThread thread = new NearbyThread("cafe");
                thread.start();
            }
        });
        btnres.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                NearbyThread thread = new NearbyThread("restaurant");
                thread.start();
            }
        });
        btnsave.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("place", place_name);
                intent.putExtra("place_lat", place_lat);
                intent.putExtra("place_lng", place_lng);
                if(place_id != null)
                    intent.putExtra("place_id", place_id);
                else
                    intent.putExtra("place_id", "null");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        btnsearch.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                String tv = editText.getText().toString();

                if(tv!=null) {
                    PlaceThread thread = new PlaceThread(tv);
                    thread.start();
                }
            }
        });
        btnnaver.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlNaver = "nmap://route/public?dlat="+place_lat+"&dlng="+place_lng+"&dname="+place_name+"&appname=com.example.yaksok";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlNaver));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list == null || list.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap")));
                } else {
                    startActivity(intent);
                }

            }
        });
        btngoogle.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlGoogle = "https://www.google.com/maps/dir/?api=1&destination="+place_lat+"%2C"+place_lng;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlGoogle));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list == null || list.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps")));
                } else {
                    startActivity(intent);
                }

            }
        });
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    // Location 얻어와서 map 위치 옮김
    private void getDeviceLocation() {

        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            myLocation = task.getResult();
                            if(myLocation != null){
                                camera_lat = myLocation.getLatitude();
                                camera_lng = myLocation.getLongitude();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(myLocation.getLatitude(),
                                                myLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // Location permission 얻어옴
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // Location permission 후 handle
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    // Location permission 후 map UI 설정
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
                mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
                mMap.getUiSettings().setMapToolbarEnabled(false);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                myLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // 마커를 markers_list에 추가
    public void addMarker(MarkerOptions m, String id) {
        Marker marker = mMap.addMarker(m);
        marker.setTag(id);
        markers_list.add(marker);
    }

    // 지도에 표시되어있는 마커를 모두 제거
    public void clearMarker() {
        for (Marker marker : markers_list) {
            marker.remove();
        }
        markers_list.clear();
    }

    // NearbyThread : 주변 정보 가져오는 스레드
    class NearbyThread extends Thread {
        String type_keyword;

        public NearbyThread(String type_keyword) {
            this.type_keyword = type_keyword;
        }

        @Override
        public void run() {
            try {
                lat_list.clear();
                lng_list.clear();
                name_list.clear();
                id_list.clear();
                vicinity_list.clear();
                rating_list.clear();

                String site = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
                site += "?location=" + camera_lat + "," + camera_lng
                        + "&radius=900&language=ko&"
                        + "&types=" + type_keyword + "&key="
                        + apiKey;

                URL url = new URL(site);
                InputStream is = url.openConnection().getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                StringBuffer buffer = new StringBuffer();

                do {
                    line = br.readLine();
                    if (line != null) {
                        buffer.append(line);
                    }
                } while (line != null);

                JSONObject root = new JSONObject(buffer.toString());

                String status = root.getString("status");
                if (status.equals("OK")) {
                    JSONArray results = root.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {

                        String rating, rating_num, name, id;

                        JSONObject obj = results.getJSONObject(i);
                        name = obj.getString("name");
                        id = obj.getString("place_id");
                        JSONObject location = obj.getJSONObject("geometry").getJSONObject("location");
                        lat_list.add(location.getDouble("lat"));
                        lng_list.add(location.getDouble("lng"));
                        name_list.add(name);
                        id_list.add(id);
                        vicinity_list.add(obj.getString("vicinity"));

                        try {
                            rating = obj.getString("rating");
                            rating_num = obj.getString("user_ratings_total");
                        } catch(JSONException e) {
                            rating = "0";
                            rating_num = "0";
                        }
                        rating_list.add(new String[]{name, rating, rating_num});
                    }
                    showNearby();
                } else {
                    Toast.makeText(getApplicationContext(), "가져온 데이터가 없습니다.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // for NearbyThread
    public void showNearby() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                rating_list.sort(new Comparator<String[]>() {
                    @Override
                    public int compare(String[] o1, String[] o2) {
                        int n1 = Integer.parseInt(o1[2]);
                        int n2 = Integer.parseInt(o2[2]);

                        if(n1 == n2) return 0;
                        else if(n1 < n2) return 1;
                        else return -1;
                    }
                });

                ArrayList<String[]> recommend_list = new ArrayList<>();
                for(int i = 0;i < 7; i++)
                    recommend_list.add(rating_list.get(i));
                recommend_list.sort(new Comparator<String[]>() {
                    @Override
                    public int compare(String[] o1, String[] o2) {
                        double r1 = Double.parseDouble(o1[1]);
                        double r2 = Double.parseDouble(o2[1]);

                        if(r1 == r2) return 0;
                        else if(r1 < r2) return 1;
                        else return -1;
                    }
                });

                clearMarker();

                for (int i = 0; i < lat_list.size(); i++) {
                    double lat = lat_list.get(i);
                    double lng = lng_list.get(i);
                    String name = name_list.get(i);
                    String id = id_list.get(i);
                    String vicinity = vicinity_list.get(i);
                    LatLng pos = new LatLng(lat, lng);

                    boolean isRecommend = false;
                    for(int j = 0; j < 3; j++){
                        if(recommend_list.get(j)[0] == name)
                            isRecommend = true;
                    }

                    if(isRecommend) {
                        MarkerOptions options  = new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_markerblue));
                        options.position(pos);
                        options.title(name);
                        options.snippet(vicinity);
                        addMarker(options, id);
                    }
                    else {
                        MarkerOptions options = new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_markergrey));;
                        options.position(pos);
                        options.title(name);
                        options.snippet(vicinity);
                        addMarker(options, id);
                    }

                }
            }
        });
    }

    // PlaceThread : text로 장소 정보 가져오는 thread
    class PlaceThread extends Thread {
        String text;

        public PlaceThread(String _text) {
            this.text = _text;
        }

        @Override
        public void run() {
            try {
                String site = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?";
                site += "input=" + text + "&inputtype=textquery&fields=formatted_address," +
                        "name,user_ratings_total,rating,geometry&language=ko&key=" + apiKey;


                URL url = new URL(site);
                InputStream is = url.openConnection().getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader reader = new BufferedReader(isr);
                String line = null;
                StringBuffer buffer = new StringBuffer();

                do {
                    line = reader.readLine();
                    if (line != null) {
                        buffer.append(line);
                    }
                } while (line != null);

                JSONObject root = new JSONObject(buffer.toString());
                String status = root.getString("status");
                if (status.equals("OK")) {


                    JSONArray candidates = root.getJSONArray("candidates");
                    JSONObject obj = candidates.getJSONObject(0);
                    JSONObject geometry = obj.getJSONObject("geometry");

                    JSONObject location = geometry.getJSONObject("location");
                    place_lat = location.getDouble("lat");
                    place_lng = location.getDouble("lng");
                    place_name = obj.getString("name");
                    place_address = obj.getString("formatted_address");
                    place_rating = obj.getDouble("rating");
                    moveMarker();
                }
                else {
                    Toast.makeText(getApplicationContext(), "가져온 데이터가 없습니다.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // for PlaceThread
    public void moveMarker() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 지도에 마커를 표시한다.
                clearMarker();
                LatLng pos = new LatLng(place_lat, place_lng);
                MarkerOptions options = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_markerblue));
                options.position(pos);
                options.title(place_name);
                options.snippet(place_address);
                place_id = null;

                Marker m = mMap.addMarker(options);
                m.showInfoWindow();
                markers_list.add(m);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
                camera_lat = place_lat;
                camera_lng = place_lng;
            }
        });
    }
    // PlaceIdThread : place_id로 장소 정보 가져오는 thread
    class PlaceIdThread extends Thread {
        String text;
        public PlaceIdThread(String _text) {
            this.text = _text;
        }

        @Override
        public void run() {
            try {

                String site = "https://maps.googleapis.com/maps/api/place/details/json?";
                site += "place_id=" + text + "&fields=formatted_address," +
                        "name,geometry&language=ko&key=" + apiKey;

                URL url = new URL(site);
                InputStream is = url.openConnection().getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader reader = new BufferedReader(isr);
                String line = null;
                StringBuffer buffer = new StringBuffer();

                do {
                    line = reader.readLine();
                    if (line != null) {
                        buffer.append(line);
                    }
                } while (line != null);

                JSONObject root = new JSONObject(buffer.toString());
                String status = root.getString("status");
                if (status.equals("OK")) {
                    JSONObject obj = root.getJSONObject("result");
                    JSONObject geometry = obj.getJSONObject("geometry");

                    JSONObject location = geometry.getJSONObject("location");
                    place_lat = location.getDouble("lat");
                    place_lng = location.getDouble("lng");
                    place_name = obj.getString("name");
                    place_address = obj.getString("formatted_address");
                    moveMarker();
                }
                else {
                    Toast.makeText(getApplicationContext(), "가져온 데이터가 없습니다.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

