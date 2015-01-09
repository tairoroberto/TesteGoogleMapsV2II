package br.com.tairoroberto.testegooglemapsv2ii;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity {
    private SupportMapFragment mapFrag;
    private GoogleMap map;
    private Marker marker;
    private Polyline polyline;
    private List<LatLng> list;
    private long distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //MAPS VIA XML
        //Declara o fragment do maps
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        map = mapFrag.getMap();
        //ativa o mapa do xml
        configMap();
    }

    public void irGpsActivity(View view){
        Intent intent = new Intent(MainActivity.this,GpsMainActivity.class);
        startActivity(intent);
    }


    public void configMap(){
        //Se mapFragment não for null, será carregado os googleMap
        map = mapFrag.getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //Inicializa a lista para desenhar uma rota
        list = new ArrayList<LatLng>();

        //Latitude e logitude
        LatLng latLng = new LatLng(-23.5154135,-46.5867317);

        //Configura a posição da camera, local, zoom etc.
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15).bearing(0).tilt(45).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);

        //atualiza a posicao da camera no mapa
        //googleMap.moveCamera(cameraUpdate);

        //map.moveCamera(update);  //animação do mapa
        map.animateCamera(update, 3000, new CancelableCallback(){
            @Override
            public void onCancel() {
                Log.i("Script", "CancelableCallback.onCancel()");
            }

            @Override
            public void onFinish() {
                Log.i("Script", "CancelableCallback.onFinish()");
            }
        });

        // MARKERS
        //customAddMarker(new LatLng(-23.564224, -46.653156), "Marcador 1", "O Marcador 1 foi reposicionado");
        //customAddMarker(new LatLng(-23.564205, -46.653102), "Marcador 2", "O Marcador 2 foi reposicionado");

        map.setInfoWindowAdapter(new InfoWindowAdapter(){

            @Override
            public View getInfoContents(Marker marker) {
                TextView tv = new TextView(MainActivity.this);
                tv.setText(Html.fromHtml("<b><font color=\"#ff0000\">"+marker.getTitle()+":</font></b> "+marker.getSnippet()));

                return tv;
            }

            @Override
            public View getInfoWindow(Marker marker) {
                LinearLayout ll = new LinearLayout(MainActivity.this);
                ll.setPadding(20, 20, 20, 20);
                ll.setBackgroundColor(Color.GREEN);

                TextView tv = new TextView(MainActivity.this);
                tv.setText(Html.fromHtml("<b><font color=\"#ffffff\">"+marker.getTitle()+":</font></b> "+marker.getSnippet()));
                ll.addView(tv);

                //teste com listener de botão dentro do infoWindow
                //o Clique no infoWindow não funciona
                //ele pega o clique do infowindow e não do botão
                //ele trava os filhos do infowindow como se fosse uma imagem
                Button bt = new Button(MainActivity.this);
                bt.setText("Botão");
                bt.setBackgroundColor(Color.RED);
                bt.setOnClickListener(new Button.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        Log.i("Script", "Botão clicado");
                    }

                });

                ll.addView(bt);

                return ll;
            }
        });


        // EVENTS
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
					/*Log.i("Script", "setOnCameraChangeListener()");

					if(marker != null){
						marker.remove();
					}
					customAddMarker(new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude), "1: Marcador Alterado", "O Marcador foi reposicionado");
					*/
            }
        });

        //Evento de clique no mapa
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i("Script", "setOnMapClickListener()");

                if(marker != null){
                    marker.remove();
                }
                customAddMarker(new LatLng(latLng.latitude, latLng.longitude), "2: Marcador Alterado", "O Marcador foi reposicionado");
                //adiciona latitude e longitude a lista de rotas
                list.add(latLng);
                //Desenha a rota
                drawRoute();
            }
        });

        //Evento de clique no marcador do mapa
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i("Script", "3: Marker: "+marker.getTitle());
                //quando retorna false = o android qeu administra as mudanção no marcador
                //quando retorna false = o android assume que vc ira trator todas as mudanção do markador
                return false;
            }
        });

        //Evento de clique no  infoWindow "Janekla de informação"
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.i("Script", "4: Marker: "+marker.getTitle());
            }
        });
    }

    //funcão que adiciona o marcador no mapa
    public void customAddMarker(LatLng latLng, String title, String snippet){
        MarkerOptions options = new MarkerOptions();
        options.position(latLng).title(title).snippet(snippet).draggable(true);

        //Muda o Pin do mapa "a imagem do marcador"
        // options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_mapa));
        marker = map.addMarker(options);
    }

    //Metodo para desenhar a rota
    public void drawRoute(){
        PolylineOptions po;

        if(polyline == null){
            po = new PolylineOptions();

            //Adiciona as linhas no PolylineOptions
            for(int i = 0, tam = list.size(); i < tam; i++){
                po.add(list.get(i));
            }
            //Adiciona uma cor
            po.color(Color.BLACK).width(4);
            //Adiciona o Polyline no maps
            polyline = map.addPolyline(po);
        }
        else{
            polyline.setPoints(list);
        }
    }


    public void getDistance(View view){
		/*double distance = 0;

		for(int i = 0, tam = list.size(); i < tam; i++){
			if(i < tam - 1){
				distance += distance(list.get(i), list.get(i+1));
			}
		}*/

        Toast.makeText(MainActivity.this, "Distancia: "+distance+" metros", Toast.LENGTH_LONG).show();
    }



    //Metodo para pegar a localização
    public void getLocation(View view){
        Geocoder gc = new Geocoder(MainActivity.this);

        List<Address> addressList;
        try {

            //Pega a localização pelo nome
            //addressList = gc.getFromLocationName("Rua Vergueiro, São Paulo, São Paulo, Brasil", 1);

            //Pega por localização
            addressList = gc.getFromLocation(list.get(list.size() - 1).latitude, list.get(list.size() - 1).longitude, 1);

            String address = "Rua: "+addressList.get(0).getThoroughfare()+"\n";
            address += "Cidade: "+addressList.get(0).getSubAdminArea()+"\n";
            address += "Estado: "+addressList.get(0).getAdminArea()+"\n";
            address += "País: "+addressList.get(0).getCountryName();

            LatLng ll = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());

            Toast.makeText(MainActivity.this, "Local: "+address, Toast.LENGTH_LONG).show();
            // Toast.makeText(MainActivity.this, "LatLng: "+ll, Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    //Metodo para calcular a distancia de um ponto inicial ao final
    //metodo do stackoverflow melhor que os metodo sda classe de maps para calcular a distancia em metros
    public static double distance(LatLng StartP, LatLng EndP) {
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return 6366000 * c;
    }






    /**********************************************************************************************/
    /*           WEB CONNECTION pega um arquivo Json do site da api do google maps                 */
    /**********************************************************************************************/

    public void getRouteByGMAV2(View view) throws UnsupportedEncodingException{
        EditText etO = (EditText) findViewById(R.id.edtOrigem);
        EditText etD = (EditText) findViewById(R.id.edtDestino);

        //Decodifica a origem e o destino que o uuario digitou e converte para o formato
        //que o google maps possa entender
        String origin = URLEncoder.encode(etO.getText().toString(), "UTF-8");
        String destination = URLEncoder.encode(etD.getText().toString(), "UTF-8");

        //Chama o metodo para pegar a rota
        getRoute(/*new LatLng(-20.195403, -40.234478)*/ origin, /*new LatLng(-20.304596, -40.291813)*/ destination);
    }





    // WEB CONNECTION
    //public void getRoute(final String origin, final String destination){
    public void getRoute(final String origin, final String destination){
        new Thread(){
            public void run(){
						/*String url= "http://maps.googleapis.com/maps/api/directions/json?origin="
								+ origin+"&destination="
								+ destination+"&sensor=false";*/
                String url= "http://maps.googleapis.com/maps/api/directions/json?origin="
                        + origin/*.latitude+","+origin.longitude*/+"&destination="
                        + destination/*.latitude+","+destination.longitude*/+"&sensor=false";


                HttpResponse response;
                HttpGet request;
                AndroidHttpClient client = AndroidHttpClient.newInstance("route");

                request = new HttpGet(url);
                try {
                    response = client.execute(request);
                    final String answer = EntityUtils.toString(response.getEntity());

                    runOnUiThread(new Runnable(){
                        public void run(){
                            try {
                                //Log.i("Script", answer);
                                list = buildJSONRoute(answer);
                                drawRoute();
                            }
                            catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }




    // PARSER JSON
    public List<LatLng> buildJSONRoute(String json) throws JSONException{
        JSONObject result = new JSONObject(json);
        JSONArray routes = result.getJSONArray("routes");

        distance = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");

        JSONArray steps = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
        List<LatLng> lines = new ArrayList<LatLng>();

        for(int i=0; i < steps.length(); i++) {
            //Mostra o local de inicio
            Log.i("Script", "STEP: LAT: "+steps.getJSONObject(i).getJSONObject("start_location").getDouble("lat")+" | LNG: "+steps.getJSONObject(i).getJSONObject("start_location").getDouble("lng"));


            String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");

            for(LatLng p : decodePolyline(polyline)) {
                lines.add(p);
            }

            //Mostra o local de fim
            Log.i("Script", "STEP: LAT: "+steps.getJSONObject(i).getJSONObject("end_location").getDouble("lat")+" | LNG: "+steps.getJSONObject(i).getJSONObject("end_location").getDouble("lng"));
        }

        return(lines);
    }




    // DECODE POLYLINE
    private List<LatLng> decodePolyline(String encoded) {

        List<LatLng> listPoints = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            Log.i("Script", "POL: LAT: "+p.latitude+" | LNG: "+p.longitude);
            listPoints.add(p);
        }
        return listPoints;
    }
}



