package ca.louisechan.livefitandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;



public class OrderConfirmActivity extends AppCompatActivity {
    private static final String TAG = "OrderConfirmActivity";
    private FusedLocationProviderClient fusedLocationClient;

    Location shopLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 101;
    private boolean locationPermissionGranted = false;
    private LocationRequest locationRequest;
    private LocationCallback mlocationCallback;
    private LocationSettingsRequest.Builder builder;
    //LatLng lastKnownLocation = null;
    Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        LinearLayout llThankyou = (LinearLayout) findViewById(R.id.llThankYou);
        llThankyou.setVisibility(View.INVISIBLE);

        Intent i = getIntent();
        order = (Order) i.getSerializableExtra("order");

        TextView tvPackageName = (TextView) findViewById(R.id.txtMealPackageName);
        TextView tvOrderCode = (TextView) findViewById(R.id.txtOrderCode);

        tvPackageName.setText(order.getMealPackage() + " Package");
        tvOrderCode.setText(order.getOrderCode());

        // Initialize fused location client provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        shopLocation = new Location("LiveFitFood");
        shopLocation.setLatitude(43.5387886);
        shopLocation.setLongitude(-79.7337268);

        mlocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                double lat = locationResult.getLastLocation().getLatitude();
                double lng = locationResult.getLastLocation().getLongitude();
                Log.d(TAG, "onLocationResult: Latitude: " + lat + ", Longitude: " + lng);
            }
        };

    }

    public void checkLocationSetting(LocationSettingsRequest.Builder builder) {
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "onSuccess: Location request was set successfully.");
                fusedLocationClient.requestLocationUpdates(locationRequest, mlocationCallback, null);
                return;
            }
        })
        .addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Location services setting not updated successfully.");
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(OrderConfirmActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                        Log.d(TAG, "onFailure: Permission request prompt can't be displayed.");
                    }
                } else {
                    Toast.makeText(OrderConfirmActivity.this, "Continuous location updates not available.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public LocationRequest createLocationRequest(long interval, long fastInterval, int priority) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastInterval);
        locationRequest.setPriority(priority);

        return locationRequest;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;

            // Get the current location
            getCurrentLocation();

            // Note: Uncomment to allow continuous location queries
            // setupContinuousLocationRequest();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public void getCurrentLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    Log.d(TAG, "onSuccess: Latitude: " + lat + ", Longitude: " + lng);

                    // Get distance from current location to the store.
                    float shopDistance = location.distanceTo(shopLocation);
                    Log.d(TAG, "onSuccess: Shop distance from current location: " + String.format("%.2fm", shopDistance));

                    if (shopDistance <= 200) {
                        //startActivity(new Intent(OrderConfirmActivity.this, PickUpActivity.class));
                        LinearLayout llThankyou = (LinearLayout) findViewById(R.id.llThankYou);
                        llThankyou.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(OrderConfirmActivity.this, "Not yet within the vicinity.\nPlease click again once you arrive.", Toast.LENGTH_LONG).show();
                    }

                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OrderConfirmActivity.this, "Can't determine current location.\nMake sure that Location Services is enabled.", Toast.LENGTH_LONG).show();;
            }
        });
    }

    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

    public void setupContinuousLocationRequest() {
        locationRequest = createLocationRequest(5000, 3000, LocationRequest.PRIORITY_HIGH_ACCURACY);
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        checkLocationSetting(builder);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE && requestCode != REQUEST_CHECK_SETTINGS) {
            return;
        }

        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            getCurrentLocation();
            // Note: Use this function if you want continuous location updates.
            // setupContinuousLocationRequest();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            locationPermissionGranted = false;
            Toast.makeText(this,
                    "Location Services permission is denied.\nApp won't be able to determine if you are in the shop's vicinity.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                Toast.makeText(this, "Location tracking is now active!", Toast.LENGTH_SHORT);
            }
            else {
                checkLocationSetting(builder);
            }
        }
    }

    public void orderConfButtonPressed(View view) {
        // Check if location services are enabled
        if (locationPermissionGranted == false) {
            AlertDialog.Builder popupBox = new AlertDialog.Builder(OrderConfirmActivity.this);

            popupBox.setTitle("Location Services Is Not Available");
            popupBox.setMessage("We need to check your current location to confirm that you are here.");
            popupBox.setPositiveButton("Enable location services", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Check if location services permission has been granted
                    getLocationPermission();
                }
            });

            popupBox.setNegativeButton("No, thanks", null);
            popupBox.show();

        } else {
            // Call function that will check if customer is within the vicinity.
            getCurrentLocation();;
        }

// Use this code to disable continuous location updates.
//        fusedLocationClient.removeLocationUpdates(mlocationCallback);
//        startActivity(new Intent(this, PickUpActivity.class));
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Back function disabled.\nOption to return will be available when you arrive at the shop.", Toast.LENGTH_LONG).show();
        //super.onBackPressed();
        // Use this code to disable continuous location updates.
        //fusedLocationClient.removeLocationUpdates(mlocationCallback);
    }

    public void returnMainScreenPressed(View view) {
        startActivity(new Intent(this, RegularUserMainActivity.class));
    }
}
