package com.example.botondepanico;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import static android.location.LocationManager.GPS_PROVIDER;


public class MainActivity extends AppCompatActivity {
    private Location loc;
    private Button boton1;
    private Button boton2;
    private Button boton3;
    private Button boton4;
    private Button boton5;
    private int botonPresionado;

    private Button botonActivador;

    private TableLayout tabla;
    private TextView textoLatitud;
    private TextView textoLongitud;
    private TextView textoNivel;
    private TextView textoX;
    private TextView textoY;
    private TextView textoZona;
    private TextView textoHemisferio;
    private Utm utm;
    private int nivelPercepcionSeguridad = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boton1 = findViewById(R.id.button_nivel1);
        boton2 = findViewById(R.id.button_nivel2);
        boton3 = findViewById(R.id.button_nivel3);
        boton4 = findViewById(R.id.button_nivel4);
        boton5 = findViewById(R.id.button_nivel5);

        botonActivador = findViewById(R.id.Activador);

        tabla = findViewById(R.id.TablaDeValores);
        textoNivel = findViewById(R.id.textViewNivelValor);
        textoLatitud = findViewById(R.id.textViewLatitudValor);
        textoLongitud = findViewById(R.id.textViewLongitudValor);
        textoX = findViewById(R.id.textViewXValor);
        textoY = findViewById(R.id.textViewYValor);
        textoZona = findViewById(R.id.textViewZonaValor);
        textoHemisferio = findViewById(R.id.textViewHemisferioValor);

        botonActivador.setVisibility(View.GONE);
        tabla.setVisibility(View.GONE);
    }

    //Método para el botón Activar
    public void localizar(View view) {
        LocationManager ubicacion;
        //solicitar permisos de ubicación
        //Hay permisos para acceder a la ubicación?
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //No los tengo, Necesito pedirlos?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){}
            //los pido
            else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            //tenemos los permisos para obtener la ubicación
            ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //solicitamos una actualización de localización
            ubicacion.requestLocationUpdates(GPS_PROVIDER, 10000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
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
            });
            //obtenemos la ultima ubicacion conocida, o sea, la ubicación que obtuvimos al ejecutar requestLocationUpdates. Tal ubicación la guardamos en la variable loc (LocationManager)
            loc = ubicacion.getLastKnownLocation(GPS_PROVIDER);
            //lo anterior lo ejecutamos para evitar que la variable loc sea NULL
            if (loc != null) {
                double [] coordenadas = new double[]{loc.getLongitude(),loc.getLatitude()};
                utm = new Utm(coordenadas);
                String nivel = nivelPercepcionSeguridad + "";
                textoNivel.setText(nivel);
                String longitud = Double.toString(loc.getLongitude());
                textoLongitud.setText(longitud);
                String latitud = loc.getLatitude() + "";
                textoLatitud.setText(latitud);
                String x = utm.getUMTX() + "";
                textoX.setText(x);
                String y = utm.getUMTY() + "";
                textoY.setText(y);
                String zona = utm.getZona() + "";
                textoZona.setText(zona);
                textoHemisferio.setText(utm.getHemisferio());
                alarmar();
            }
            else {
                Toast.makeText(getApplicationContext(), "ubicación nula", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //Método para subir la ubicación a la base de datos
    public void alarmar() {

        String direccion = "https://www.edacarquitectos.com/appBotonDePanico/insertaralarma.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, direccion,

            //Mensaje por si la conexión y el envio fueron exitosos
            new Response.Listener<String>() {
            @Override public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "Enviado", Toast.LENGTH_SHORT).show();
                tabla.setVisibility(View.VISIBLE);
            }},

            //Mensaje por si la conexión y el envio no fueron exitosos
            new Response.ErrorListener() {
            @Override public void onErrorResponse(VolleyError error) { Toast.makeText(getApplicationContext(), "No Enviado", Toast.LENGTH_SHORT).show(); }}) {

            //obtener los parametros de la consulta
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parametros = new HashMap<String, String>();
                String latidud = String.valueOf(loc.getLatitude());
                String longitud = String.valueOf(loc.getLongitude());
                parametros.put("latitud", latidud);
                parametros.put("longitud", longitud);
                parametros.put("valor_inseguridad", nivelPercepcionSeguridad + "");
                parametros.put("x", utm.getUMTX() + "");
                parametros.put("y", utm.getUMTY() + "");
                parametros.put("zona", utm.getZona() + "");
                parametros.put("hemisferio", utm.getHemisferio() + "");
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //enviamos la consulta al webservice
        requestQueue.add(stringRequest);
    }

    //metodo para los botones del 1 al 5
    public void nivel(View view){
        volverColorNormalBotonSeleccionado();
        tabla.setVisibility(View.GONE);
        switch (view.getId()){
            case R.id.button_nivel1:
                if (botonPresionado != 1) {
                    nivelPercepcionSeguridad = 1;
                    boton1.setBackgroundColor(Color.rgb(130,130,130));
                    botonPresionado = 1;
                    botonActivador.setVisibility(View.VISIBLE);
                }
                else{
                    botonPresionado = 0;
                    botonActivador.setVisibility(View.GONE);
                }
                break;
            case R.id.button_nivel2:
                if (botonPresionado != 2) {
                    nivelPercepcionSeguridad = 2;
                    boton2.setBackgroundColor(Color.rgb(130,130,130));
                    botonPresionado = 2;
                    botonActivador.setVisibility(View.VISIBLE);
                }
                else{
                    botonPresionado = 0;
                    botonActivador.setVisibility(View.GONE);
                }
                break;
            case R.id.button_nivel3:
                if (botonPresionado != 3) {
                    nivelPercepcionSeguridad = 3;
                    boton3.setBackgroundColor(Color.rgb(130,130,130));
                    botonPresionado = 3;
                    botonActivador.setVisibility(View.VISIBLE);
                }
                else{
                    botonPresionado = 0;
                    botonActivador.setVisibility(View.GONE);
                }
                break;
            case R.id.button_nivel4:
                if (botonPresionado != 4) {
                    nivelPercepcionSeguridad = 4;
                    boton4.setBackgroundColor(Color.rgb(130,130,130));

                    //Antigua animacion
                    /*Animation animationScale = AnimationUtils.loadAnimation(this, R.anim.escalarboton);
                    boton4.startAnimation(animationScale);*/
                    botonPresionado = 4;
                    botonActivador.setVisibility(View.VISIBLE);
                }
                else{
                    botonPresionado = 0;
                    botonActivador.setVisibility(View.GONE);
                }
                break;
            case R.id.button_nivel5:
                if (botonPresionado != 5) {
                    nivelPercepcionSeguridad = 5;
                    boton5.setBackgroundColor(Color.rgb(130,130,130));
                    botonPresionado = 5;
                    botonActivador.setVisibility(View.VISIBLE);
                }
                else{
                    botonPresionado = 0;
                    botonActivador.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void volverColorNormalBotonSeleccionado() {
        switch (botonPresionado){
            case 1:
                boton1.setBackgroundColor(Color.rgb(215,215,215));
                break;
            case 2:
                boton2.setBackgroundColor(Color.rgb(215,215,215));
                break;
            case 3:
                boton3.setBackgroundColor(Color.rgb(215,215,215));
                break;
            case 4:
                boton4.setBackgroundColor(Color.rgb(215,215,215));
                break;
            case 5:
                boton5.setBackgroundColor(Color.rgb(215,215,215));
                break;
            default:
                break;
        }
    }

}
