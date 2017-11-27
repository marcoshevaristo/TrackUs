package com.marcosevaristo.tcc001.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.internal.pe;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.marcosevaristo.tcc001.App;
import com.marcosevaristo.tcc001.R;
import com.marcosevaristo.tcc001.model.Carro;
import com.marcosevaristo.tcc001.model.Linha;
import com.marcosevaristo.tcc001.utils.CollectionUtils;
import com.marcosevaristo.tcc001.utils.FirebaseUtils;
import com.marcosevaristo.tcc001.utils.GoogleMapsUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mapa extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private GoogleMap gMap;
    private List<Marker> lMarker;
    private Linha linha;
    private boolean permitiuLocalizacao;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        linha = (Linha) getIntent().getExtras().get("linha");
        setupToolbar();
        permitiuLocalizacao = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if(!permitiuLocalizacao) {
            verificaPermissoes();
        } else {
            solicitaAtivarLocalizacao();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        try{
            gMap.setMyLocationEnabled(permitiuLocalizacao);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        setaCarrosNoMapa();
        setupLocationsOnMap();
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_map);
        toolbar.setTitle("");
        TextView textViewLinhaTitulo = (TextView) findViewById(R.id.linhaMapaText);
        TextView textViewLinhaSubTitulo = (TextView) findViewById(R.id.linhaMapaSubText);

        textViewLinhaTitulo.setText(linha.getNumero()+" - "+linha.getTitulo());
        textViewLinhaSubTitulo.setText(linha.getSubtitulo());
    }

    private void setaCarrosNoMapa() {
        if (linha != null) {
            FirebaseUtils.getCarrosReference(linha.getId(), null).addValueEventListener(getEventoFirebaseGetCarros());
            if (CollectionUtils.isEmpty(linha.getRota())) {
                FirebaseUtils.getLinhasReference(linha.getId()).child("rota").addListenerForSingleValueEvent(getEventoFirebaseGetRota());
            } else {
                gMap.addPolyline(GoogleMapsUtils.desenhaRota((ArrayList<LatLng>) GoogleMapsUtils.getListLatLngFromListString(linha.getRota())));
            }
        }
    }

    private void setupLocationsOnMap() {
        try {
            Geocoder gc = new Geocoder(this);
            List<Address> addresses = gc.getFromLocationName(linha.getMunicipio().getNome(), 1);

            if(CollectionUtils.isNotEmpty(addresses)) {
                for(Address a : addresses){
                    if(a.hasLatitude() && a.hasLongitude()){
                        gMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                        gMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(a.getLatitude(), a.getLongitude())));
                    }
                }
            }
        } catch (IOException e) {
            App.toast(R.string.nao_achou_municipio_no_mapa, linha.getMunicipio().getNome());
        }
    }

    private ValueEventListener getEventoFirebaseGetRota() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null && dataSnapshot.getChildren().iterator().hasNext()) {
                    linha.setRota((List<String>) dataSnapshot.getValue());
                }
                if(CollectionUtils.isNotEmpty(linha.getRota())) {
                    gMap.addPolyline(GoogleMapsUtils.desenhaRota((ArrayList<LatLng>) GoogleMapsUtils.getListLatLngFromListString(linha.getRota())));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void removeMarkers() {
        for(Marker umMarker : lMarker) {
            umMarker.remove();
        }
    }

    private ValueEventListener getEventoFirebaseGetCarros() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null && dataSnapshot.getChildren().iterator().hasNext()) {
                    if(CollectionUtils.isNotEmpty(lMarker)) {
                        removeMarkers();
                    }
                    List<Carro> lCarros = new ArrayList<>();
                    for(DataSnapshot umDataSnapshot : dataSnapshot.getChildren()) {
                        lCarros.add(umDataSnapshot.getValue(Carro.class));
                    }

                    populaMarkersDoMapaComCarros(lCarros);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void populaMarkersDoMapaComCarros(List<Carro> lCarros) {
        lMarker = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(lCarros)) {
            for(Carro umCarro : lCarros) {
                Double latitude = Double.parseDouble(umCarro.getLatitude());
                Double longitude = Double.parseDouble(umCarro.getLongitude());
                LatLng posicaoUmCarro = new LatLng(latitude, longitude);
                MarkerOptions umMarker = new MarkerOptions().position(posicaoUmCarro).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_marker));
                lMarker.add(gMap.addMarker(umMarker));
            }

            if(permitiuLocalizacao) {
                try {
                    mFusedLocationClient = new FusedLocationProviderClient(Mapa.this);
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(Mapa.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                calculaTempoChegadaSetaMarkersTitles(location.getLatitude(), location.getLongitude());
                            }
                        }
                    });
                } catch (SecurityException e) {e.printStackTrace();}
            }
        }
    }

    private void solicitaAtivarLocalizacao() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(App.getAppContext())
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(App.getAppContext().getString(R.string.title_activity_mapa), App.getAppContext().getString(R.string.permissoes_de_localizacao_ja_concedidas));
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(App.getAppContext().getString(R.string.title_activity_mapa), App.getAppContext().getString(R.string.solicitando_permissoes));

                        try {
                            status.startResolutionForResult(Mapa.this, 0x1);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(App.getAppContext().getString(R.string.title_activity_mapa), App.getAppContext().getString(R.string.aguardando_permissao_localizacao));
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(App.getAppContext().getString(R.string.title_activity_mapa), App.getAppContext().getString(R.string.permissoes_de_localizacao_inadequadas));
                        break;
                    case LocationSettingsStatusCodes.CANCELED:
                        App.toast(R.string.localizacao_necessaria);
                }
            }
        });
    }

    private void calculaTempoChegadaSetaMarkersTitles(double latitudeAtual, double longitudeAtual) {
        if(CollectionUtils.isNotEmpty(lMarker)) {
            for(Marker umMarker : lMarker) {
                float[] distanciaArr = new float[1];
                Location.distanceBetween(umMarker.getPosition().latitude, umMarker.getPosition().longitude, latitudeAtual, longitudeAtual, distanciaArr);
                Float distancia = distanciaArr[0];

                umMarker.setTitle(String.valueOf(distancia));
            }
        }
    }

    public void verificaPermissoes() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Titulo teste")
                        .setMessage("Mensagem teste")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(Mapa.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permitiuLocalizacao = ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                }
                if(permitiuLocalizacao) {
                    solicitaAtivarLocalizacao();
                } else {
                    verificaPermissoes();
                }
                return;
            }

        }
    }
}
