package com.example.mapbox;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconTextFit;

public class AnimarMarker extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

    private MapView mapView;
    private MapboxMap map;
    private LatLng ubicacionActual = new LatLng(1.967983, -75.921382);
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
        setContentView(R.layout.activity_animar_marker);

        mapView = findViewById(R.id.mapViewAnimate);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.map = mapboxMap;

        geoJsonSource = new GeoJsonSource("id_geoJsonSource",
                Feature.fromGeometry(Point.fromLngLat(ubicacionActual.getLongitude(),
                        ubicacionActual.getLatitude())));

        mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                inicioDeAnadirMarcador(style);
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

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        actualizarPosisionMarcador(point);

        return true;
    }
    private void inicioDeAnadirMarcador(@NonNull Style style) {

        Drawable image  = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_car);
        Bitmap bitmap =drawableToBitmap(image);
        style.addImage(id_img_icon, bitmap);

        geoJsonSource = new GeoJsonSource(id_geo_JsonSource,
                Feature.fromGeometry(Point.fromLngLat(ubicacionActual.getLongitude(),
                        ubicacionActual.getLatitude())));

        style.addSource(geoJsonSource);

        style.addLayer(new SymbolLayer(id_contenedor_marcador, id_geo_JsonSource).withProperties(
                iconImage(id_img_icon),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconSize(1f)
        ));

        map.addOnMapClickListener(AnimarMarker.this);
    }

    private void actualizarPosisionMarcador(LatLng position) {
// This method is were we update the marker position once we have new coordinates. First we
// check if this is the first time we are executing this handler, the best way to do this is
// check if marker is null;
        if (map.getStyle() != null) {
            geoJsonSource = map.getStyle().getSourceAs(id_geo_JsonSource);
            if (geoJsonSource != null) {
                geoJsonSource.setGeoJson(FeatureCollection.fromFeature(
                        Feature.fromGeometry(Point.fromLngLat(position.getLongitude(), position.getLatitude()))
                ));
            }
        }

// Lastly, animate the camera to the new position so the user
// wont have to search for the marker and then return.
        map.animateCamera(CameraUpdateFactory.newLatLng(position));
    }



    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
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

}
