package com.coltrack.controlrutasmonitor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Listado extends AppCompatActivity {
    TextView textViewEstudiantesRuta;
    TextView textViewAccion;
    TextView textViewProgreso;
    Button buttonSeleccionarEstudiante;


    String ruta;
    String accion;
    String LOGTAG="log";
    String evento;
    String finalizado;




    Button buttonTestGPS;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    List<String> listado;
    Cursor c;
    SQLiteDatabase db;
    int estudiantesTotales=0;
    int estudiantesProcesados=0;
    String estudianteSeleccionado;
    JSONObject jsonObject;
    String colegio;
    String latitud;
    String longitud;
    boolean selected=false;
    double latitude=0;
    double longitude=0;
    String strLongitud=null;
    String strLatitud=null;
    String paraderoAM=null;
    String paraderoPM=null;
    String query=null;
    AlertDialog.Builder builder;
    int seconds=0;
    boolean stopCounter=false;
    String progresoOriginal;
    boolean onlyTimeProgreso=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);
        textViewEstudiantesRuta=(TextView)findViewById(R.id.textViewEstudiantesRuta);
        textViewAccion=(TextView)findViewById(R.id.textViewAccion);
        expListView = (ExpandableListView) findViewById(R.id.lvExp1);
        textViewProgreso=(TextView)findViewById(R.id.textViewProgreso);
        buttonTestGPS=(Button)findViewById(R.id.buttonTestGPS);
        buttonSeleccionarEstudiante=(Button)findViewById(R.id.buttonSeleccionarEstudiante);







        Bundle bundle=getIntent().getExtras();
        ruta=bundle.getString("ruta").toString();
        accion=bundle.getString("accion");
        textViewEstudiantesRuta.setText("Estudiantes Ruta: " + ruta);
        if (accion.equals("recoger")){
            textViewAccion.setText("Accion: Recoger Estudiantes");
        }else if (accion.equals("dejar")){
            textViewAccion.setText("Accion: Dejar Estudiantes");

        }
        db = openOrCreateDatabase("controlRutas", MODE_PRIVATE, null);
        new GPSTracker(Listado.this);

        getCurrentLocation();
        readGPS();

        buttonSeleccionarEstudiante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getApplicationContext(),Selecciona.class);
                i.putExtra("accion",accion);
                i.putExtra("ruta",ruta);
                startActivity(i);
            }
        });




//        listado = new ArrayList<String>();
//        pupulateItems();
//
//        buttonLlegoRuta.setOnClickListener(new View.OnClickListener() {
//            @Override
//
//            public void onClick(View v) {
//                stopCounter=false;
//                seconds=0;
//                new CountDownTimer(1000, 1000) {
//
//
//
//
//                    public void onTick(long millisUntilFinished) {
//                        seconds++;
//                        Log.d(LOGTAG,"conteo: "+seconds);
//                        if (!onlyTimeProgreso){
//                            onlyTimeProgreso=true;
//                            progresoOriginal=textViewProgreso.getText().toString();
//                        }
//
//                        String texto=progresoOriginal+", Espera: "+seconds;
//                        textViewProgreso.setText(texto);
//
//
//                    }
//
//                    public void onFinish() {
//                        if (!stopCounter) {
//                            this.start();
//                        }
//                    }
//                }.start();
//            }
//        });

        buttonTestGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readGPS();
                Toast.makeText(getApplicationContext(),"Lat: "+strLatitud+" Long: "+strLongitud,Toast.LENGTH_SHORT).show();
                getCurrentLocation();

            }
        });

//        buttonRecogerDejar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                new AlertDialog.Builder(Listado.this)
//                        .setTitle("Accion")
//                        .setMessage("Desea "+accion+" al estudiante " +estudianteSeleccionado+"?")
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
//
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                stopCounter=true;
//                                onlyTimeProgreso=false;
//                                textViewProgreso.setText(progresoOriginal);
//                                //Enviamos el evento al servidor
//                                Log.d(LOGTAG,"Enviando evento al servidor....");
//                                sendEventos send1=new sendEventos();
//                                send1.execute();
//                            }})
//                        .setNegativeButton("No", null).show();
//
//            }
//        });


//        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                estudianteSeleccionado = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
//                String[] parts=estudianteSeleccionado.split(",");
//                estudianteSeleccionado=parts[0];
//                getCurrentLocation();
//                seconds=0;
//
//                Log.d(LOGTAG, "Estudiante Seleccionado: " + estudianteSeleccionado);
//                textViewEstudianteSeleccionado.setVisibility(View.VISIBLE);
//                textViewEstudianteSeleccionado.setText("Estudiante Seleccionado: " + estudianteSeleccionado);
//                textViewGrado.setVisibility(View.VISIBLE);
//                textViewMobilAcudiente.setVisibility(View.VISIBLE);
//                buttonLlegoRuta.setVisibility(View.VISIBLE);
//                query="SELECT * FROM estudiantes WHERE nombreEstudiante=" + "'" + estudianteSeleccionado + "'";
//                Log.d(LOGTAG, "Query onChildClick: " + query);
//                c = db.rawQuery(query, null);
//                c.moveToFirst();
//                if (c != null) {
//                    do {
////                        String grado = null;
////                        grado = c.getString(c.getColumnIndex("curso"));
////                        String correoAcudiente = c.getString(c.getColumnIndex("correoAcudiente"));
////                        String telefonoAcudiente = c.getString(c.getColumnIndex("telefonoAcudiente"));
////                        colegio= c.getString(c.getColumnIndex("colegio"));
////                        paraderoAM=c.getString(c.getColumnIndex("paraderoAM"));
////                        paraderoPM=c.getString(c.getColumnIndex("paraderoPM"));
////
////                        Log.d(LOGTAG, "Grado Estudiante Seleccionado: " + grado);
////                        Log.d(LOGTAG, "Telefono Acudiente: " + telefonoAcudiente);
////                        Log.d(LOGTAG, "Correo Acudiente: " + correoAcudiente);
////                        textViewGrado.setText("Grado: " + grado);
////                        textViewMobilAcudiente.setText("Telefono Acudiente: " + telefonoAcudiente);
//                    } while (c.moveToNext());
//                    buttonLlamar.setVisibility(View.VISIBLE);
//                    if (accion.equals("recoger")) {
//                        buttonRecogerDejar.setVisibility(View.VISIBLE);
//                        buttonRecogerDejar.setText("Recoger");
//                    } else if (accion.equals("dejar")) {
//                        buttonRecogerDejar.setVisibility(View.VISIBLE);
//                        buttonRecogerDejar.setText("Dejar");
//                    }
//                }
//
//
//                parent.collapseGroup(groupPosition);
//                return false;
//            }
//        });








        //db.close();





    }
    public void pupulateItems(){
        // get the listview
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        listDataHeader.add("Listado Estudiantes");
        listado.clear();

        //Leemos estudiantes totales
        c=db.rawQuery("SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"'", null);
        c.moveToFirst();
        estudiantesTotales=0;
        if (c != null && c.getCount()>0) {
            // Loop through all Results
            do {
                estudiantesTotales++;

            }while(c.moveToNext());
        }

        //Leemos estudiantes que ya estan marcados
        //c = db.rawQuery("SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"' and evento='FALTA'", null);
        if (accion.equals("recoger")){
            c = db.rawQuery("SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"' and evento='FALTA' order by paraderoAM*1 asc", null);
            Log.d(LOGTAG,"Query recoger: "+"SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"' and evento='FALTA' order by paraderoAM asc");
        }
        if (accion.equals("dejar")){
            c = db.rawQuery("SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"' and evento='FALTA' order by paraderoPM*1 asc", null);
            Log.d(LOGTAG,"Query dejar: "+"SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"' and evento='FALTA' order by paraderoPM desc");
        }

        //c = db.rawQuery("SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"'", null);
        int Column1=0;int Column2=0;int Column3=0;int Column4=0;int Column5=0;
        int Column6=0;int Column7=0;int Column8=0;
        Column1 = c.getColumnIndex("nombreEstudiante");
        Column2 = c.getColumnIndex("ruta1");
        Column3 = c.getColumnIndex("nombreAcudiente");
        Column4 = c.getColumnIndex("telefonoAcudiente");
        Column5 = c.getColumnIndex("correoAcudiente");
        Column6 = c.getColumnIndex("paraderoAM");
        Column7 = c.getColumnIndex("paraderoPM");
        c.moveToFirst();
        String Data=null;

        if (c != null && c.getCount()>0) {
            // Loop through all Results
            do {
                Data=c.getString(Column1)+'\t'+c.getString(Column2)+'\t'+c.getString(Column3)+'\t'+c.getString(Column4)+'\t'+c.getString(Column5);
                //Log.d(LOGTAG, Data);
                if (accion.equals("recoger") && isInteger(c.getString(Column6))){
                    listado.add(c.getString(Column1)+", "+"(P"+c.getString(Column6)+")");
                }
                if (accion.equals("dejar") && isInteger(c.getString(Column7))){
                    listado.add(c.getString(Column1)+", "+"(P"+c.getString(Column7)+")");
                }


            }while(c.moveToNext());
        }
        int porcentaje=0;
        float temp=0;
        if (estudiantesProcesados>0){
            temp=(float)estudiantesProcesados/estudiantesTotales;
            temp=temp*100;
            porcentaje=(int)temp;
            porcentaje=porcentaje;
        }
        textViewProgreso.setText("Progreso: "+estudiantesProcesados+" / "+estudiantesTotales+ " ["+porcentaje+" %]");
        listDataChild.put(listDataHeader.get(0), listado); // Header, Child data
        listAdapter=new com.coltrack.controlrutasmonitor.ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);


    }

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if(s.isEmpty()) return false;
        }
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    public void readGPS(){

        latitude  = GPSTracker.latitude; // latitude
        longitude = GPSTracker.longitude; // latitude

        DecimalFormat numberFormat = new DecimalFormat("#.#######");
        strLatitud=numberFormat.format(latitude);
        strLatitud=strLatitud.replace(",",".");
        strLongitud=numberFormat.format(longitude);
        strLongitud=strLongitud.replace(",",".");
        //Toast.makeText(getApplicationContext(),"Lat: "+strLatitud+" Long: "+strLongitud,Toast.LENGTH_SHORT).show();
        Log.d(LOGTAG,"Lat: "+strLatitud +" Long: "+strLongitud);
    }


    //CLASE PARA ENVIAR DATOS DE EVENTO AL SERVIDOR
//    private class sendEventos extends AsyncTask<Void, Void, Boolean> {
//        private ProgressDialog pd;
//        @Override
//        protected void onPreExecute() {
//            pd = ProgressDialog.show(Listado.this, "Estado Conexion Servidor", "Conectando...");
//            Log.d(LOGTAG,"preexecute");
//        }
//        @Override
//        protected void onPostExecute(Boolean result) {
//            Log.d(LOGTAG,"post execute");
//            if (pd.isShowing()) {
//                pd.dismiss();
//            }
//            if (!result) {
//                Toast.makeText(Listado.this,"Problema procesando evento!!",Toast.LENGTH_SHORT).show();
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        //update ui here
//                        // display toast here
//                        //Toast.makeText(Login.this,"Usuario y/o contraseña invalidos!!!!",Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }else {
//                Toast.makeText(Listado.this,"Evento procesado correctamente!!",Toast.LENGTH_SHORT).show();
//                query="update estudiantes set evento='realizado' where nombreEstudiante='" + estudianteSeleccionado + "'";
//                Log.d(LOGTAG,"Query boton evento: "+query);
//                db.execSQL(query);
//                buttonRecogerDejar.setVisibility(View.GONE);
//                buttonLlamar.setVisibility(View.GONE);
//                textViewEstudianteSeleccionado.setVisibility(View.VISIBLE);
//                textViewEstudianteSeleccionado.setText("Seleccione un estudiante!");
//                textViewGrado.setVisibility(View.GONE);
//                textViewMobilAcudiente.setVisibility(View.GONE);
//                buttonLlegoRuta.setVisibility(View.INVISIBLE);
//                estudiantesProcesados++;
//                pupulateItems();
//
//
//            }
//        }
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            boolean status=false;
//            Log.d(LOGTAG, "doing");
//            jsonObject = new JSONObject();
//            try {
//
//                //readGPS();
//
//
//
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                String timestamp = sdf.format(new Date());
//
//                Log.d(LOGTAG,"nombreEstudiante: "+estudianteSeleccionado);
//                Log.d(LOGTAG,"evento: "+accion);
//                Log.d(LOGTAG,"datetime: "+timestamp);
//                Log.d(LOGTAG,"ruta: "+ruta);
//                Log.d(LOGTAG,"colegio: "+colegio);
//                Log.d(LOGTAG,"latitud: "+strLatitud);
//                Log.d(LOGTAG,"longitud: "+strLongitud);
//
//
//
//
//                jsonObject.put("nombreEstudiante", estudianteSeleccionado);
//                jsonObject.put("evento", accion);
//                jsonObject.put("datetime", timestamp);
//                jsonObject.put("ruta", ruta);
//                jsonObject.put("colegio", colegio);
//                jsonObject.put("latitud", strLatitud);
//                jsonObject.put("longitud", strLongitud);
//                jsonObject.put("segundos",seconds);
//
//
//                //Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG).show();
//                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//                nameValuePairs.add(new BasicNameValuePair("json", jsonObject.toString()));
//                String response = makePOSTRequest("http://107.170.38.31/phpControlRutas/leerEventos.php", nameValuePairs );
//                Log.d(LOGTAG,"Response: "+response);
//                if (response.equals("PROBLEM")){
//                    //Toast.makeText(Login.this,"Usuario y/o contraseña errados!",Toast.LENGTH_SHORT).show();
//                    status=false;
//                }
//                if (response.equals("OK")){
//
//                    status=true;
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return status;
//        }
//        public String makePOSTRequest(String url, List<NameValuePair> nameValuePairs) {
//            String response = "";
//
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpPost httpPost = new HttpPost(url);
//            try {
//                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                try {
//                    HttpResponse httpResponse = httpClient.execute(httpPost);
//                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
//                    JSONObject object = null;
//                    Log.d(LOGTAG, "Response:" + jsonResult);
//                    try {
//                        object = new JSONObject(jsonResult);
//                        String estadoLogin = object.getString("rta");
//
//                        Log.d(LOGTAG, "Respuesta Server:" + estadoLogin);
//                        response=estadoLogin;
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Log.d(LOGTAG, "Error1:" + e);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            return response;
//        }
//        private StringBuilder inputStreamToString(InputStream is)
//        {
//            String rLine = "";
//            StringBuilder answer = new StringBuilder();
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//            try
//            {
//                while ((rLine = rd.readLine()) != null)
//                {
//                    answer.append(rLine);
//                }
//            }
//            catch(IOException e)
//            {
//                e.printStackTrace();
//            }
//            return answer;
//        }
//
//    }

    public void getCurrentLocation() {
        LocationManager locationManager;
        String context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getSystemService(context);
        Criteria crta = new Criteria();
        crta.setAccuracy(Criteria.ACCURACY_FINE);
        crta.setAltitudeRequired(false);
        crta.setBearingRequired(false);
        crta.setCostAllowed(true);
        crta.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(crta, true);

        locationManager.requestLocationUpdates(provider, 1000, 0,
                new LocationListener() {
                    @Override
                    public void onStatusChanged(String provider, int status,
                                                Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }

                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lng = location.getLongitude();
                            if (lat != 0.0 && lng != 0.0) {
                                System.out.println("WE GOT THE LOCATION");
                                System.out.println(lat);
                                System.out.println(lng);
                                latitude=lat;
                                longitude=lng;

                                DecimalFormat numberFormat = new DecimalFormat("#.#######");
                                strLatitud=numberFormat.format(latitude);
                                strLatitud=strLatitud.replace(",",".");
                                strLongitud=numberFormat.format(longitude);
                                strLongitud=strLongitud.replace(",",".");
                                //Toast.makeText(getApplication(),"lat: "+lat+" long: "+lng,Toast.LENGTH_SHORT).show();
                            }
                        }

                    }

                });
    }
//    @Override
//    public void onBackPressed() {
//        moveTaskToBack(true);
//    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cerrarApp:
                //Toast.makeText(getApplicationContext(),"Cargnado estudiantes de servidor",Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(Listado.this)
                        .setTitle("Accion")
                        .setMessage("Desea cerrar la aplicacion?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                Toast.makeText(getApplicationContext(),"Chao!",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("EXIT", true);
                                startActivity(intent);
                            }})
                        .setNegativeButton("No", null).show();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
