package com.example.mapbox;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.SortedList;
import android.view.View;
import android.widget.Toast;
/*
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.core.constants.Constants;*/
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;*/

/*import static com.mapbox.api.directions.v5.DirectionsCriteria.GEOMETRY_POLYLINE;*/
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.api.directions.v5.DirectionsCriteria.GEOMETRY_POLYLINE;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;

public class RutaSerpiente extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {


    private static final float NAVIGATION_LINE_WIDTH = 6;
    private static final float NAVIGATION_LINE_OPACITY = .8f;
    private static final String DRIVING_ROUTE_POLYLINE_LINE_LAYER_ID = "DRIVING_ROUTE_POLYLINE_LINE_LAYER_ID";
    private static final String DRIVING_ROUTE_POLYLINE_SOURCE_ID = "DRIVING_ROUTE_POLYLINE_SOURCE_ID";
    private static final int DRAW_SPEED_MILLISECONDS = 500;


    private static final String PROFILE_NAME = "PROFILE_NAME";
    private static final String ORIGEN = "ORIGIN";
    private static final String DESTINO = "DESTINATION";

    // Origin point in Paris, France
    private Point PARIS_ORIGIN_POINT = Point.fromLngLat(-76.048722, 1.852760);

    // Destination point in Lyon, France
    private static final Point LYON_DESTINATION_POINT = Point.fromLngLat(-75.932837, 1.971442);
    private static final String SOURCE_ID = "source-id";
    private  GeoJsonSource jsonSource;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private MapboxDirections mapboxDirectionsClient;
    private Handler handler = new Handler();
    private Runnable runnable;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.key_Token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_ruta_serpiente);

        // Setup the MapView
        mapView = findViewById(R.id.mapViewSerpiente);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;




        this.mapboxMap.setStyle(new Style.Builder().fromUri(Style.LIGHT)
                , new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapboxMap.addOnMapClickListener(RutaSerpiente.this);
                inicioMarkers(style);
                //getDirectionsRoute(PARIS_ORIGIN_POINT, LYON_DESTINATION_POINT);
            }
        });

    }

    private void inicioMarkers(Style style) {

        Drawable image  = ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_marker);
        Bitmap bitmap =drawableToBitmap(image);


        Feature origFeature = Feature.fromGeometry(Point.fromLngLat(PARIS_ORIGIN_POINT.longitude(),
                PARIS_ORIGIN_POINT.latitude()));
        origFeature.addStringProperty(PROFILE_NAME, ORIGEN);

        Feature destiFeature = Feature.fromGeometry(Point.fromLngLat(LYON_DESTINATION_POINT.longitude(),
                LYON_DESTINATION_POINT.latitude()));
        destiFeature.addStringProperty(PROFILE_NAME, DESTINO);

        jsonSource = new GeoJsonSource(SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[] {
                        origFeature,
                        destiFeature,
                }));

        style.addSource(jsonSource);
        // Add a source and LineLayer for the snaking directions route line
        style.addSource(new GeoJsonSource(DRIVING_ROUTE_POLYLINE_SOURCE_ID));

        style.addLayerBelow(new LineLayer(DRIVING_ROUTE_POLYLINE_LINE_LAYER_ID,
                DRIVING_ROUTE_POLYLINE_SOURCE_ID)
                .withProperties(
                        lineWidth(NAVIGATION_LINE_WIDTH),
                        lineOpacity(NAVIGATION_LINE_OPACITY),
                        lineCap(LINE_CAP_ROUND),
                        lineJoin(LINE_JOIN_ROUND),
                        lineColor(Color.parseColor("#d742f4"))
                ), "layer-id");

        style.addLayer(new SymbolLayer("layer_id", SOURCE_ID).withProperties(
                iconImage(get(PROFILE_NAME)),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                textField(get(PROFILE_NAME)),
                textIgnorePlacement(true),
                textAllowOverlap(true),
                textOffset(new Float[] {0f, 2f})
        ));
        mapView.addOnStyleImageMissingListener(new MapView.OnStyleImageMissingListener() {
            @Override
            public void onStyleImageMissing(@NonNull String id) {
                switch (id) {
                    case ORIGEN:
                        addImage(id, R.drawable.ic_car);
                        break;
                    case DESTINO:
                        addImage(id, R.drawable.red_marker);
                        break;
                }
            }
        });
        LatLng position = new LatLng(PARIS_ORIGIN_POINT.latitude(),PARIS_ORIGIN_POINT.longitude());
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 10));
        getDirectionsRoute(PARIS_ORIGIN_POINT,LYON_DESTINATION_POINT);
    }

    private void addImage(String id, int drawableImage) {
        Style style = mapboxMap.getStyle();
        if (style != null) {
            style.addImageAsync(id, BitmapUtils.getBitmapFromDrawable(
                    getResources().getDrawable(drawableImage)));
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        PARIS_ORIGIN_POINT = Point.fromLngLat(point.getLongitude(),point.getLatitude());

        Feature origFeature = Feature.fromGeometry(Point.fromLngLat(PARIS_ORIGIN_POINT.longitude(),
                PARIS_ORIGIN_POINT.latitude()));
        origFeature.addStringProperty(PROFILE_NAME, ORIGEN);

        Feature destiFeature = Feature.fromGeometry(Point.fromLngLat(LYON_DESTINATION_POINT.longitude(),
                LYON_DESTINATION_POINT.latitude()));
        destiFeature.addStringProperty(PROFILE_NAME, DESTINO);


        if (mapboxMap.getStyle() != null) {
            jsonSource = mapboxMap.getStyle().getSourceAs(SOURCE_ID);
            if (jsonSource != null) {
                jsonSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{origFeature,destiFeature}
                ));
            }
        }
        getDirectionsRoute(PARIS_ORIGIN_POINT,LYON_DESTINATION_POINT);

        return true;
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

    /**
     * Build the Mapbox Directions API request
     *
     * @param origin      The starting point for the directions route
     * @param destination The final point for the directions route
     */
    private void getDirectionsRoute(Point origin, Point destination) {
        mapboxDirectionsClient = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .geometries(GEOMETRY_POLYLINE)
                .alternatives(true)
                .steps(true)
                .accessToken(getString(R.string.key_Token))
                .build();

        mapboxDirectionsClient.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.body() == null) {
                    Timber.d("No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Timber.d("No routes found");
                    return;
                }

                // Get the route from the Mapbox Directions API response
                DirectionsRoute currentRoute = response.body().routes().get(0);

                // Start the step-by-step process of drawing the route
                runnable = new DrawRouteRunnable(mapboxMap, currentRoute.legs().get(0).steps(), handler);
                handler.postDelayed(runnable, DRAW_SPEED_MILLISECONDS);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Toast.makeText(RutaSerpiente.this,
                        "Error", Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * Runnable class which goes through the route and draws each {@link LegStep} of the Directions API route
     */
    private static class DrawRouteRunnable implements Runnable {
        private MapboxMap mapboxMap;
        private List<LegStep> steps;
        private List<Feature> drivingRoutePolyLineFeatureList;
        private Handler handler;
        private int counterIndex;

        DrawRouteRunnable(MapboxMap mapboxMap, List<LegStep> steps, Handler handler) {
            this.mapboxMap = mapboxMap;
            this.steps = steps;
            this.handler = handler;
            this.counterIndex = 0;
            drivingRoutePolyLineFeatureList = new ArrayList<>();
        }

        @Override
        public void run() {
            if (counterIndex < steps.size()) {
                LegStep singleStep = steps.get(counterIndex);
                if (singleStep != null && singleStep.geometry() != null) {
                    LineString lineStringRepresentingSingleStep = LineString.fromPolyline(
                            singleStep.geometry(), Constants.PRECISION_5);
                    Feature featureLineString = Feature.fromGeometry(lineStringRepresentingSingleStep);
                    drivingRoutePolyLineFeatureList.add(featureLineString);
                }
                if (mapboxMap.getStyle() != null) {
                    GeoJsonSource source = mapboxMap.getStyle().getSourceAs(DRIVING_ROUTE_POLYLINE_SOURCE_ID);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(drivingRoutePolyLineFeatureList));
                    }
                }
                counterIndex++;
                handler.postDelayed(this, DRAW_SPEED_MILLISECONDS);
            }
        }
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
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
// Cancel the directions API request
        if (mapboxDirectionsClient != null) {
            mapboxDirectionsClient.cancelCall();
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
