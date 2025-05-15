package fr.android.sportFinder;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class AsyncGeocoder extends AsyncTask<Double, Void, String> {
    private final WeakReference<Context> contextRef;
    private final GeocodeCallback callback;

    public AsyncGeocoder(Context context, GeocodeCallback callback) {
        this.contextRef = new WeakReference<>(context);
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Double... params) {
        if (params.length < 2 || contextRef.get() == null) {
            return null;
        }

        double latitude = params[0];
        double longitude = params[1];
        Context context = contextRef.get();

        try {
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                // Ajouter la rue
                if (address.getThoroughfare() != null) {
                    sb.append(address.getThoroughfare());
                }

                // Ajouter la ville
                if (address.getLocality() != null) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(address.getLocality());
                }

                // Ajouter le code postal
                if (address.getPostalCode() != null) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(address.getPostalCode());
                }

                // Ajouter le pays
                if (address.getCountryName() != null) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(address.getCountryName());
                }

                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String address) {
        if (callback != null) {
            if (address != null) {
                callback.onAddressFound(address);
            } else {
                callback.onAddressNotFound();
            }
        }
    }

    public interface GeocodeCallback {
        void onAddressFound(String address);
        void onAddressNotFound();
    }
}
