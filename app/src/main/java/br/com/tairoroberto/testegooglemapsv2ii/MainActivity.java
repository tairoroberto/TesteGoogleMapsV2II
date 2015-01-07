package br.com.tairoroberto.testegooglemapsv2ii;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends FragmentActivity {
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private Marker marker;
    private Polyline polyline;
    private List<LatLng> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //MAPS VIA XML
        //Declara o fragment do maps
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        googleMap = mapFragment.getMap();
        //ativa o mapa do xml
        configmap();

    }


    public void configmap(){
        //Se mapFragment não for null, será carregado os googleMap
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //Inicializa a lista para desenhar uma rota
        list = new ArrayList<LatLng>();
        //Latitude e logitude
        final LatLng latLng = new LatLng(-23.548998, -46.633058);

        //Configura a posição da camera, local, zoom etc.
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15).bearing(0).tilt(45).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        //atualiza a posicao da camera no mapa
        //googleMap.moveCamera(cameraUpdate);

        //animação do mapa
        googleMap.animateCamera(cameraUpdate, 3000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                Log.i("Script", "Animação: CancelableCallback.onFinish()");
            }

            @Override
            public void onCancel() {
                Log.i("Script","Animação: CancelableCallback.onCancel()");
            }
        });

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                LinearLayout layout = new LinearLayout(getBaseContext());
                layout.setPadding(20,20,20,20);
                layout.setBackgroundColor(Color.GREEN);
                layout.setOrientation(LinearLayout.VERTICAL);

                TextView textView = new TextView(getBaseContext());
                textView.setText(Html.fromHtml("<b><font color=\"#ff0000\">" + marker.getTitle() + ":</font></b>" + marker.getSnippet()));

                Button btnTeste = new Button(getBaseContext());
                btnTeste.setText("Botão de teste");
                btnTeste.setBackgroundColor(Color.BLUE);

                //teste com listener de botão dentro do infoWindow
                //o Clique no infoWindow não funciona
                //ele pega o clique do infowindow e não do botão
                //ele trava os filhos do infowindow como se fosse uma imagem
                btnTeste.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("Script","Clique do botão dentro do infoWindow");
                    }
                });

                layout.addView(textView);
                layout.addView(btnTeste);

                return layout;

            }

            @Override
            public View getInfoContents(Marker marker) {
                TextView textView = new TextView(getBaseContext());
                textView.setText(Html.fromHtml("<b><font color=\"#ff0000\">"+marker.getTitle()+":</font></b>"+marker.getSnippet()));
                return textView;
            }
        });



        //Evento de clique no mapa
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i("Script","setOnMapClickListener()");
                if (marker != null){
                    marker.remove();
                }
                custonMarker(new LatLng(latLng.latitude,latLng.longitude),"2: Marcador alterado","O marcador foi reposicionado");
                //adiciona latitude e longitude a lista de rotas
                list.add(latLng);
                //Desenha a rota
                drawRoute();

            }
        });

        //Evento de clique no marcador do mapa
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i("Script","3: Marker: "+ marker.getTitle());

                //quando retorna false = o android qeu administra as mudanção no marcador
                //quando retorna false = o android assume que vc ira trator todas as mudanção do markador
                return false;
            }
        });

        //Evento de clique no  infoWindow "Janekla de informação"
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.i("Script","4: setOnInfoWindowClickListener()");
            }
        });

    }

    //funcão que adiciona o marcador no mapa
    public void custonMarker( LatLng latLng, String title, String snippet){
        MarkerOptions options = new MarkerOptions();
        options.position(latLng).title(title).snippet(snippet).draggable(true);

        //Muda o Pin do mapa "a imagem do marcador"
        // options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_mapa));

        //Adiciona as opçções ao marker
        marker = googleMap.addMarker(options);
    }


    //Metodo para desenhar a rota
    public void drawRoute(){
        PolylineOptions options;
        if(polyline == null){
            options = new PolylineOptions();

            //Adiciona as linhas no PolylineOptions
            for (int i = 0; i < list.size(); i++) {
                options.add(list.get(i));
            }

            //Adiciona uma cor
            options.color(Color.BLACK);
            //Adiciona o Polyline no maps
            polyline = googleMap.addPolyline(options);
        }else {
            polyline.setPoints(list);
        }
    }


    //Metodo para pegar a distancia
    public void getDistance(View view){
        double distance = 0;
        for (int i = 0; i < list.size(); i++) {
            if (i < list.size() - 1){
                distance += distance(list.get(i),list.get(i+1));
            }
        }
        //Mostra a distancia em um toast
        Toast.makeText(this,"Distancia: "+ distance + " metros",Toast.LENGTH_LONG).show();
    }

    //Metodo para pegar a localização
    public void getLocation(View view) {
        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {

            List<Address> addressList = geocoder.getFromLocation(list.get(list.size() - 1).latitude, list.get(list.size() - 1).longitude,1);

             //Pega a localização pelo nome
            //List<Address> addressList = geocoder.getFromLocationName("Rua Vergueiro,São Paulo,Brasil", 1);

            String address = "Rua: " + addressList.get(0).getThoroughfare()+"\n";
            address += "Cidade: " + addressList.get(0).getSubAdminArea()+"\n";
            address += "Estado: " + addressList.get(0).getAdminArea()+"\n";
            address += "Cidade: " + addressList.get(0).getCountryName();

           //Mostra a distancia em um toast
            Toast.makeText(this,"Local: "+ address,Toast.LENGTH_LONG).show();

           // LatLng latLng = new LatLng(addressList.get(0).getLatitude(),addressList.get(0).getLongitude());
          //  Toast.makeText(this,"LatLng: "+ latLng,Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //Metodo para calcular a distancia de um ponto inicial ao final
    //metodo do stackoverflow melhor que os metodo sda classe de maps para calcular a distancia em metros
    public static double distance(LatLng StartP, LatLng EndP){
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double log1 = StartP.longitude;
        double log2 = EndP.longitude;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(log2-log1);
        double  a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double  c = 2 * Math.asin(Math.sqrt(a));

        return 6366000 * c;
    }

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
