package fr.android.sportFinder.fragments;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
//import fr.android.sportFinder.AsyncGeocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import fr.android.sportFinder.MapManager;
import fr.android.sportFinder.R;

/**
 * Fragment qui gère l'affichage de la carte et la géolocalisation
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private TextView textLatitude;
    private TextView textLongitude;
    //private TextView textAddress;
    private GoogleMap map;
    private MapManager mapManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate le layout pour ce fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialisation des vues
        textLatitude = view.findViewById(R.id.textLatitude);
        textLongitude = view.findViewById(R.id.textLongitude);
        //textAddress = view.findViewById(R.id.textAddress);

        // Initialisation de la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialisation du gestionnaire de carte
        mapManager = new MapManager(requireActivity());

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        
        // Activer le bouton de localisation si les permissions sont accordées
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        }

        // Demander la localisation
        mapManager.requestLocation(new MapManager.LocationCallbackInterface() {
            @Override
            public void onLocationReceived(double lat, double lon) {
                // Mettre à jour les TextViews avec les coordonnées
                if (isAdded()) {
                    textLatitude.setText(getString(R.string.latitude_format, lat));
                    textLongitude.setText(getString(R.string.longitude_format, lon));
                    
                    // Déplacer la caméra vers la position
                    LatLng position = new LatLng(lat, lon);
                    map.clear(); // Nettoyer les anciens marqueurs
                    map.addMarker(new MarkerOptions()
                            .position(position)
                            .title(getString(R.string.map_my_location)));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
                }
            }
            
            @Override
            public void onLocationUnavailable() {
                // En cas d'erreur, afficher des coordonnées par défaut
                if (isAdded()) {
                    textLatitude.setText(getString(R.string.latitude_format, 0.0));
                    textLongitude.setText(getString(R.string.longitude_format, 0.0));
                    
                    // Centrer la carte sur la France par défaut
                    LatLng france = new LatLng(46.227638, 2.213749);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(france, 5));
                }
            }
        });

        // Mise à jour des coordonnées affichées
        updateLocationDisplay();
    }

    /**
     * Met à jour l'affichage des coordonnées
     */
    private void updateLocationDisplay() {
        if (mapManager.getLatitude() != 0 && mapManager.getLongitude() != 0) {
            textLatitude.setText(getString(R.string.latitude_format, mapManager.getLatitude()));
            textLongitude.setText(getString(R.string.longitude_format, mapManager.getLongitude()));
            //getAddressFromLocation(mapManager.getLatitude(), mapManager.getLongitude());
        }
    }

    /**
     * Récupère l'adresse à partir des coordonnées GPS
     */
    /*private void getAddressFromLocation(final double latitude, final double longitude) {
        // Afficher "Recherche de l'adresse..." pendant le chargement
        textAddress.setText(getString(R.string.address_searching));
        
        // Utiliser AsyncGeocoder pour le géocodage asynchrone
        new AsyncGeocoder(requireContext(), new AsyncGeocoder.GeocodeCallback() {
            @Override
            public void onAddressFound(String address) {
                textAddress.setText(getString(R.string.address_format, address));
            }
            
            @Override
            public void onAddressNotFound() {
                textAddress.setText(getString(R.string.address_not_found));
            }
        }).execute(latitude, longitude);
    }*/
}
