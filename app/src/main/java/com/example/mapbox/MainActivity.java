package com.example.mapbox;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private Marker marker;
    MapboxMap mapboxMap;
    LatLng ubicacion;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getResources().getString(R.string.key_Token));
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                cargarMarkerWihtCameraPosition();

            }
        });



        ubicacion = new LatLng(1.967983, -75.921382);
        context = this;
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    private void cargarMarkerWihtCameraPosition() {

        if (marker == null){
            marker = mapboxMap.addMarker(new MarkerOptions().position(ubicacion));
        }else {
            marker.setPosition(new LatLng(ubicacion));
        }

        CameraPosition camara = new CameraPosition.Builder()
                .target(ubicacion)
                .zoom(14)
                .bearing(0)
                .tilt(30)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(camara));
    }

    /* START CODE MAP */

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                // Create an Icon object for the marker to use


// Add the marker to the map



                Drawable image  = ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_marker);
                Bitmap bitmap =drawableToBitmap(image);
                Icon icon = IconFactory.getInstance(getApplicationContext()).fromBitmap(bitmap);

                marker = mapboxMap.addMarker(new MarkerOptions()
                        .position(ubicacion)
                        .title("SICANDE")
                        .snippet("Bienvenidos a la vereda sicande.")
                .icon(icon));

/*

                marker = mapboxMap.addMarker(new MarkerOptions()
                .position(ubicacion)
                .title("Bienvenidos Sicande")
                .snippet("SICANDE"));
*/

            }
        });
       /* LatLng ubicacion = new LatLng(1.967983, -75.921382);*/

       /* mapboxMap.addMarker(new MarkerOptions()
        .position(ubicacion)
        .title("Bienvenidos Sicande")
        .snippet("SICANDE"));*/
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



    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /* END CODE MAP */

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
        if (id == R.id.action_MapNew) {
            Intent ma = new Intent(getApplicationContext(), AddRainFallStyleActivity.class);
            newInten(ma);
            return true;
        }
        if (id == R.id.action_MarAni) {
            Intent ma = new Intent(getApplicationContext(), AnimarMarker.class);
            newInten(ma);
            return true;
        }
        if (id == R.id.action_Ruta) {
            Intent ma = new Intent(getApplicationContext(), RutaSerpiente.class);
            newInten(ma);
            return true;
        }
        if (id == R.id.action_Ubi) {
            Intent ma = new Intent(getApplicationContext(), UbicacionRealTime.class);
            newInten(ma);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void newInten(Intent ma) {
        startActivity(ma);
    }
}
