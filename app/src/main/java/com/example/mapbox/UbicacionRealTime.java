package com.example.mapbox;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

public class UbicacionRealTime extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

    private MapboxMap map;
    private MapView mapView;
    Style styl;
    private GeoJsonSource geoJsonSource;
    private String id_img_icon = "id_Icon";
    private String id_geo_JsonSource= "id_geoJsonSource";
    private String id_contenedor_marcador= "id_layer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.key_Token));

// This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_ubicacion_real_time);

        mapView = findViewById(R.id.mapViewUbicacion);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        UbicacionRealTime.this.map = mapboxMap;

        map.setStyle(Style.TRAFFIC_DAY, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                styl=style;
                permisoGPS();
            }
        });

    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void inicioDeAnadirMarcador(@NonNull Style style, LatLng position) {

        Drawable image  = ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_marker);
        Bitmap bitmap =drawableToBitmap(image);
        style.addImage(id_img_icon, bitmap);

        geoJsonSource = new GeoJsonSource(id_geo_JsonSource,
                Feature.fromGeometry(Point.fromLngLat(position.getLongitude(),
                        position.getLatitude())));


        style.addSource(geoJsonSource);

        style.addLayer(new SymbolLayer(id_contenedor_marcador, id_geo_JsonSource).withProperties(
                iconImage(id_img_icon),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconSize(1f)
        ));
        map.addOnMapClickListener(UbicacionRealTime.this);
    }

    private void actualizarPosisionMarcador(LatLng position) {

        if (map.getStyle() != null) {
            geoJsonSource = map.getStyle().getSourceAs(id_geo_JsonSource);
            if (geoJsonSource != null) {
                geoJsonSource.setGeoJson(FeatureCollection.fromFeature(
                        Feature.fromGeometry(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                ));
            }
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(position)) // Sets the new camera position
                .zoom(15) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder

        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition), 7000);
    }

    private void permisoGPS() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            locationStart();
        } else {
            locationStart();
        }
    }


    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        gpsEnaDis();
        onstartGos(mlocManager,Local);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        actualizarPosisionMarcador(point);
        return true;
    }

    public class Localizacion implements LocationListener {
        UbicacionRealTime ubicacionRealTime;

        /*public MainActivity getMainActivity() {
            return mainActivity;
        }*/

        public void setMainActivity(UbicacionRealTime ubicacionRealTime) {
            this.ubicacionRealTime = ubicacionRealTime;
        }

        @Override
        public void onLocationChanged(Location loc) {


           LatLng position = new LatLng(loc.getLatitude(), loc.getLongitude());

           if(geoJsonSource == null){
               inicioDeAnadirMarcador(styl,position);
           }else{
               actualizarPosisionMarcador(position);
           }


        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            //Toast.makeText(getApplicationContext(), "Activo",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            gpsEnaDis();
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }

    }

    private void gpsEnaDis() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showAlertGPS("ACTIVAR", "GPS");
        }
    }

    private void onstartGos(LocationManager mlocManager, Localizacion Local) {
        gpsEnaDis();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }

        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, (LocationListener) Local);
    }

    private void showAlertGPS(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        builder.setPositiveButton("Activar gps", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*Terminate ubicacion*/


    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}
