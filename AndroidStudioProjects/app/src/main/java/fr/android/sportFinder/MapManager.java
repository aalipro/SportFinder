package fr.android.sportFinder;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MapManager {

    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    private double latitude;
    private double longitude;

    public MapManager(@NonNull Activity activity) {
        this.activity = activity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public void requestLocation(LocationCallbackInterface callback) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        // Créer une requête de localisation avec haute précision
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)        // 5 secondes entre les mises à jour
                .setFastestInterval(3000)  // 3 secondes minimum entre les mises à jour
                .setNumUpdates(1);         // Une seule mise à jour

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLocations().isEmpty()) {
                    callback.onLocationUnavailable();
                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    callback.onLocationReceived(latitude, longitude);
                } else {
                    callback.onLocationUnavailable();
                }

                // Arrêter les mises à jour après réception
                fusedLocationClient.removeLocationUpdates(this);
            }
        };

        // Demander d'abord la dernière localisation connue
        fusedLocationClient.getLastLocation().addOnCompleteListener(activity, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location lastLocation = task.getResult();
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                callback.onLocationReceived(latitude, longitude);
            } else {
                // Si pas de dernière localisation, demander une mise à jour
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        });
    }

    public interface LocationCallbackInterface {
        void onLocationReceived(double lat, double lon);
        void onLocationUnavailable();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
