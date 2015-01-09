package br.com.tairoroberto.testegooglemapsv2ii;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;


public class GpsMainActivity extends FragmentActivity implements LocationListener{
    private SupportMapFragment mapFrag;
    private GoogleMap map;
    private Marker marker;
    private Polyline polyline;
    private List<LatLng> list;
    private long distance;
    private LocationManager locationManager;

    //variavel para verificar rede para acha localização
    private boolean allowNetWork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_main);

        //MAPS VIA XML
        //Declara o fragment do maps
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        map = mapFrag.getMap();
        //ativa o mapa do xml
        configMap();
    }


    @Override
    protected void onResume() {
        super.onResume();

        allowNetWork = true;
        locationManager =  (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Antes de buscar a localização com GPS devemos verificar se ele está ativo
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //Chama obtem a localização por Triangulação e o GPS
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        }else{
            //Se GPS não estiver ativo, enva chamada para ativação de GPS
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }



    public void configMap(){
        //Se mapFragment não for null, será carregado os googleMap
        map = mapFrag.getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Latitude e logitude
        LatLng latLng = new LatLng(-23.5154135,-46.5867317);
    }

    public void configLocation(LatLng latLng){
        //Configura a posição da camera, local, zoom etc.
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15).bearing(0).tilt(45).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);

        //Habilita a triangulação do gps
        map.setMyLocationEnabled(true);

        //atualiza a posicao da camera no mapa
        map.animateCamera(update);

        MyLocation myLocation = new MyLocation();
        map.setLocationSource(myLocation);
        myLocation.setLocation(latLng);
    }


    //Metodos de implementação do layer, icone de localização
    @Override
    public void onLocationChanged(Location location) {
        //verificação de rede para começar o GPS en vez da rede
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)){
            allowNetWork = false;
        }

        if (allowNetWork || location.getProvider().equals(LocationManager.GPS_PROVIDER)){
            configLocation(new LatLng(location.getLatitude(),location.getLongitude()));
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    //Classe pra podeer manipular o layer de localização
    public class MyLocation implements LocationSource{
        private OnLocationChangedListener listener;

        //Metodo que mostra o icone de localização no mapa
        @Override
        public void activate(OnLocationChangedListener listener) {
            this.listener = listener;
            Log.i("Script","activate()");
        }

        @Override
        public void deactivate() {
            Log.i("Script","deactivate()");
        }

        public void setLocation(LatLng latLng){
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);

            if (listener != null){
                listener.onLocationChanged(location);
            }
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gps_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
