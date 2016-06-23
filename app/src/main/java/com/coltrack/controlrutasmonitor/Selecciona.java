package com.coltrack.controlrutasmonitor;

import android.accounts.AccountManager;
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
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.List;

public class Selecciona extends AppCompatActivity {
    String LOGTAG="log";
    List<Row> rows;
    private ListView listView;
    String ruta;
    String accion;
    double latitude=0;
    double longitude=0;
    String strLongitud=null;
    String strLatitud=null;
    SQLiteDatabase db;
    Cursor c;
    int estudiantesTotales=0;

    Button buttonLlamar;
    Button buttonLlego;
    Button buttonAccion;
    Button buttonEventos;
    int seconds=0;
    boolean stopCounter=false;
    boolean onlyTimeProgreso=false;
    String estudianteSeleccionado;
    int estudiantesProcesados=0;
    JSONObject jsonObject;
    String colegio;
    String telefonoAcudiente;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        listView = (ListView) findViewById(android.R.id.list);
        buttonLlamar=(Button)findViewById(R.id.buttonLlamar);
        buttonLlego=(Button)findViewById(R.id.buttonLlego);
        buttonAccion=(Button)findViewById(R.id.buttonAccion);
        buttonEventos=(Button)findViewById(R.id.buttonEventos);


        Bundle bundle=getIntent().getExtras();
        ruta=bundle.getString("ruta").toString();
        accion=bundle.getString("accion");
        Log.d(LOGTAG, "Recibido en Selecciona: " + "ruta: " + ruta + " accion: " + accion);
        if (accion.equals("recoger")){
            buttonAccion.setText("Recoger");
        }else {
            buttonAccion.setText("Dejar");
        }

        db = openOrCreateDatabase("controlRutas", MODE_PRIVATE, null);
        new GPSTracker(Selecciona.this);

        getCurrentLocation();
        readGPS();

        populateItems();


        buttonEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkeados = 0;
                int i = 0;
                for (i = 0; i < rows.size(); i++) {
                    if (rows.get(i).isChecked()) {
                        checkeados++;
                    }
                }
                for (i = 0; i < rows.size(); i++) {
                    if (rows.get(i).isChecked()) {
                        estudianteSeleccionado = rows.get(i).getTitle();
                    }
                }



                getCurrentLocation();
                readGPS();

                if (checkeados==1) {
                    Intent intentE = new Intent(getApplicationContext(), Eventos.class);


                    c = db.rawQuery("SELECT colegio FROM estudiantes WHERE nombreEstudiante='"+estudianteSeleccionado+"'", null);
                    c.moveToFirst();
                    if (c.getCount()>0){
                        if (c != null && c.getCount()>0) {
                            // Loop through all Results
                            do {
                                colegio=c.getString(c.getColumnIndex("colegio"));
                                Log.d(LOGTAG,"Colegio estudiante: "+colegio);

                            }while(c.moveToNext());
                        }
                    }

                    intentE.putExtra("estudianteSeleccionado",estudianteSeleccionado);
                    intentE.putExtra("evento",accion);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String timestamp = sdf.format(new Date());
                    intentE.putExtra("dateTime",timestamp);
                    intentE.putExtra("ruta",ruta);
                    intentE.putExtra("colegio",colegio);
                    intentE.putExtra("strLatitud",strLatitud);
                    intentE.putExtra("strLongitud",strLongitud);
                    intentE.putExtra("segundos",seconds);





                    startActivity(intentE);
                }else if (checkeados==0){
                    Toast.makeText(getApplicationContext(),"Seleccione al menos un estudiante",Toast.LENGTH_SHORT).show();
                }else if(checkeados>0){
                    Toast.makeText(getApplicationContext(),"Seleccione solo un estudiante",Toast.LENGTH_SHORT).show();
                }
            }
        });



        buttonLlamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOGTAG, "presiono boton llamar....");
                int checkeados = 0;
                int i = 0;
                for (i = 0; i < rows.size(); i++) {
                    if (rows.get(i).isChecked()) {
                        checkeados++;
                    }
                }
                Log.d(LOGTAG, "Checkeados: " + checkeados);
                if (checkeados > 0) {
                    for (i = 0; i < rows.size(); i++) {
                        if (rows.get(i).isChecked()) {
                            Log.d(LOGTAG, "Checkeado: " + rows.get(i).getTitle());
                            estudianteSeleccionado = rows.get(i).getTitle();
                            //aca sacamos el dato de telefono del acudiente
                            c = db.rawQuery("SELECT telefonoAcudiente FROM estudiantes WHERE nombreEstudiante='" + estudianteSeleccionado + "'", null);
                            c.moveToFirst();
                            if (c.getCount() > 0) {
                                if (c != null && c.getCount() > 0) {
                                    // Loop through all Results
                                    do {
                                        telefonoAcudiente = c.getString(c.getColumnIndex("telefonoAcudiente"));
                                        Log.d(LOGTAG, "telefonoAcudiente: " + telefonoAcudiente);
                                    } while (c.moveToNext());
                                }
                                //Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + editTextTelefonoAcudiente.getText().toString()));
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telefonoAcudiente));
                                startActivity(intent);
                            }
                        }
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Seleccione al menos un estudiante",Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonAccion.setOnClickListener(new View.OnClickListener() {
            //Cuando se va a recoger o dejar niños
            @Override
            public void onClick(View v) {
                stopCounter=true;
                onlyTimeProgreso=false;
                int i=0;
                int checkeados=0;
                for (i=0;i<rows.size();i++){
                    if (rows.get(i).isChecked()){
                        checkeados++;
                    }
                }
                Log.d(LOGTAG,"Checkeados: "+checkeados);
                if (checkeados>0){
                    for (i=0;i<rows.size();i++){

                        if (rows.get(i).isChecked()){
                            Log.d(LOGTAG,"Checkeado: "+rows.get(i).getTitle());
                            estudianteSeleccionado=rows.get(i).getTitle();
                            //aca sacamos el dato de colegio
                            c = db.rawQuery("SELECT colegio FROM estudiantes WHERE nombreEstudiante='"+estudianteSeleccionado+"'", null);
                            c.moveToFirst();
                            if (c.getCount()>0){
                                if (c != null && c.getCount()>0) {
                                    // Loop through all Results
                                    do {
                                        colegio=c.getString(c.getColumnIndex("colegio"));
                                        Log.d(LOGTAG,"Colegio estudiante: "+colegio);

                                    }while(c.moveToNext());
                                }
                            }
                            sendEventos send1=new sendEventos();
                            send1.execute();

                            String query="update estudiantes set evento='realizado' where nombreEstudiante='" + estudianteSeleccionado + "'";
                            db.execSQL(query);
                            seconds=0;

                        }

                    }
                    rows.clear();
                    populateItems();
                }else {
                    Toast.makeText(getApplicationContext(),"Seleccione al menos un estudiante!!!",Toast.LENGTH_SHORT).show();
                }
            }
        });


//        buttonLlamar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(LOGTAG,rows.get(0).getTitle());
//                Log.d(LOGTAG, String.valueOf(rows.get(0).isChecked()));
//                Log.d(LOGTAG, String.valueOf(rows.size()));
//            }
//        });


        buttonLlego.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Comenzando conteo!!",Toast.LENGTH_SHORT).show();
                stopCounter=false;
                seconds=0;
                new CountDownTimer(1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        seconds++;
                        Log.d(LOGTAG,"conteo: "+seconds);
                        if (!onlyTimeProgreso){
                            onlyTimeProgreso=true;
                        }
                    }
                    public void onFinish() {
                        if (!stopCounter) {
                            this.start();
                        }
                    }
                }.start();
            }
        });



    }

    public void populateItems(){
        //Leemos DB tabla estudiantes y populamos el listview
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
        Log.d(LOGTAG,"Estudiantes totales: "+estudiantesTotales);


        if (accion.equals("recoger")){
            c = db.rawQuery("SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"' and evento='FALTA' order by paraderoAM*1 asc", null);
            Log.d(LOGTAG,"Query recoger: "+"SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"' and evento='FALTA' order by paraderoAM asc");
        }
        if (accion.equals("dejar")){
            c = db.rawQuery("SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"' and evento='FALTA' order by paraderoPM*1 asc", null);
            Log.d(LOGTAG,"Query dejar: "+"SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"' and evento='FALTA' order by paraderoPM desc");
        }
        int Column1=0;int Column2=0;int Column3=0;int Column4=0;int Column5=0;
        int Column6=0;int Column7=0;int Column8=0;
        Column1 = c.getColumnIndex("nombreEstudiante");
        Column2 = c.getColumnIndex("ruta1");
        Column3 = c.getColumnIndex("nombreAcudiente");
        Column4 = c.getColumnIndex("telefonoAcudiente");
        Column5 = c.getColumnIndex("correoAcudiente");
        Column6 = c.getColumnIndex("paraderoAM");
        Column7 = c.getColumnIndex("paraderoPM");
        Column8 = c.getColumnIndex("colegio");
        c.moveToFirst();
        String Data=null;
        Row row = null;

        rows = new ArrayList<Row>(estudiantesTotales);
        Log.d(LOGTAG,"Estudiantes encontrados db: "+c.getCount());

        if (c != null && c.getCount()>0) {
            // Loop through all Results
            do {
                Data=c.getString(Column1)+'\t'+c.getString(Column2)+'\t'+c.getString(Column3)+'\t'+c.getString(Column4)+'\t'+c.getString(Column5);
                Log.d(LOGTAG, Data);
                row = new Row();
                if (accion.equals("recoger") && isInteger(c.getString(Column6)) && !c.getString(Column6).equals("0")){
                    //listado.add(c.getString(Column1)+", "+"(P"+c.getString(Column6)+")");
                    row.setTitle(c.getString(Column1));
                    row.setSubtitle(c.getString(Column6));
                    rows.add(row);

                }
                if (accion.equals("dejar") && isInteger(c.getString(Column7)) && !c.getString(Column7).equals("0")){
                    //listado.add(c.getString(Column1)+", "+"(P"+c.getString(Column7)+")");
                    row.setTitle(c.getString(Column1));
                    row.setSubtitle(c.getString(Column7));
                    rows.add(row);
                }


            }while(c.moveToNext());
        }
        listView.setAdapter(new CustomArrayAdapter(this, rows));
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
                                //System.out.println("WE GOT THE LOCATION");
                                //System.out.println(lat);
                                //System.out.println(lng);
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
    public void readGPS(){

        latitude  = GPSTracker.latitude; // latitude
        longitude = GPSTracker.longitude; // latitude

        DecimalFormat numberFormat = new DecimalFormat("#.#######");
        strLatitud=numberFormat.format(latitude);
        strLatitud=strLatitud.replace(",",".");
        strLongitud=numberFormat.format(longitude);
        strLongitud=strLongitud.replace(",",".");
        //Toast.makeText(getApplicationContext(),"Lat: "+strLatitud+" Long: "+strLongitud,Toast.LENGTH_SHORT).show();
        //Log.d(LOGTAG, "Lat: " + strLatitud + " Long: " + strLongitud);
    }

    //CLASE PARA ENVIAR DATOS DE EVENTO AL SERVIDOR
    private class sendEventos extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(Selecciona.this, "Estado Conexion Servidor", "Conectando...");
            Log.d(LOGTAG,"preexecute");
        }
        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(LOGTAG,"post execute");
            if (pd.isShowing()) {
                pd.dismiss();
            }
            if (!result) {
                Toast.makeText(Selecciona.this,"Problema procesando evento!!",Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //update ui here
                        // display toast here
                        //Toast.makeText(Login.this,"Usuario y/o contraseña invalidos!!!!",Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                Toast.makeText(Selecciona.this,"Evento procesado correctamente!!",Toast.LENGTH_SHORT).show();
                String queri="update estudiantes set evento='realizado' where nombreEstudiante='" + estudianteSeleccionado + "'";
                Log.d(LOGTAG, "Query boton evento: "+queri);
                db.execSQL(queri);

                estudiantesProcesados++;
                populateItems();


            }
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status=false;
            Log.d(LOGTAG, "doing");
            jsonObject = new JSONObject();
            try {
                //readGPS();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = sdf.format(new Date());

                Log.d(LOGTAG,"nombreEstudiante: "+estudianteSeleccionado);
                Log.d(LOGTAG,"evento: "+accion);
                Log.d(LOGTAG,"datetime: "+timestamp);
                Log.d(LOGTAG,"ruta: "+ruta);
                Log.d(LOGTAG,"colegio: "+colegio);
                Log.d(LOGTAG,"latitud: "+strLatitud);
                Log.d(LOGTAG,"longitud: "+strLongitud);


                try {
                    jsonObject.put("nombreEstudiante", estudianteSeleccionado);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonObject.put("evento", accion);
                jsonObject.put("datetime", timestamp);
                jsonObject.put("ruta", ruta);
                jsonObject.put("colegio", colegio);
                jsonObject.put("latitud", strLatitud);
                jsonObject.put("longitud", strLongitud);
                jsonObject.put("segundos",seconds);


                //Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG).show();
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("json", jsonObject.toString()));
                String response = makePOSTRequest("http://107.170.62.116/phpControlRutas/leerEventos.php", nameValuePairs );
                Log.d(LOGTAG,"Response: "+response);
                if (response.equals("PROBLEM")){
                    //Toast.makeText(Login.this,"Usuario y/o contraseña errados!",Toast.LENGTH_SHORT).show();
                    status=false;
                }
                if (response.equals("OK")){

                    status=true;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return status;
        }
        public String makePOSTRequest(String url, List<NameValuePair> nameValuePairs) {
            String response = "";

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                HttpResponse httpResponse = null;
                try {
                    httpResponse = httpClient.execute(httpPost);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                JSONObject object = null;
                Log.d(LOGTAG, "Response:" + jsonResult);
                try {
                    object = new JSONObject(jsonResult);
                    String estadoLogin = object.getString("rta");

                    Log.d(LOGTAG, "Respuesta Server:" + estadoLogin);
                    response=estadoLogin;

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(LOGTAG, "Error1:" + e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        private StringBuilder inputStreamToString(InputStream is)
        {
            String rLine = "";
            StringBuilder answer = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            try
            {
                while ((rLine = rd.readLine()) != null)
                {
                    answer.append(rLine);
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return answer;
        }

    }
    @Override protected void onResume() {
        super.onResume();
        //Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
        populateItems();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu2) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu2, menu2);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cerrarApp:
                //Toast.makeText(getApplicationContext(),"Cargnado estudiantes de servidor",Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(Selecciona.this)
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
//            case R.id.comenzarRuta:
//                //Enviamos evento de comienzo de ruta
//                Toast.makeText(getApplicationContext(),"Comenzando Ruta",Toast.LENGTH_SHORT).show();
//                estudianteSeleccionado=null;
//                accion="comienzaRuta";
//                sendEventos send1=new sendEventos();
//                send1.execute();
//                populateItems();
//
//
//                return true;
            case R.id.finalizarRuta:
                Toast.makeText(getApplicationContext(),"Ruta Finalizada..",Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(Selecciona.this)
                        .setTitle("Accion")
                        .setMessage("Desea cerrar finalizar la ruta?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                sendEvento finalizarRuta=new sendEvento();
                                finalizarRuta.execute();
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
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private class sendEvento extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(Selecciona.this, "Estado Conexion Servidor", "Conectando...");
            Log.d(LOGTAG,"preexecute");
        }
        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(LOGTAG,"post execute");
            if (pd.isShowing()) {
                pd.dismiss();
            }
            if (!result) {
                Toast.makeText(Selecciona.this,"Problema procesando evento!!",Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //update ui here

                    }
                });
            }else {
                Toast.makeText(Selecciona.this,"Evento procesado correctamente!!",Toast.LENGTH_SHORT).show();

            }
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status=false;
            Log.d(LOGTAG, "doing");
            jsonObject = new JSONObject();
            try {
                //readGPS();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = sdf.format(new Date());

//                Log.d(LOGTAG,"nombreEstudiante: "+estudianteSeleccionado);
//                Log.d(LOGTAG,"evento: "+accion);
//                Log.d(LOGTAG,"datetime: "+timestamp);
//                Log.d(LOGTAG,"ruta: "+ruta);
//                Log.d(LOGTAG,"colegio: "+colegio);
//                Log.d(LOGTAG,"latitud: "+strLatitud);
//                Log.d(LOGTAG,"longitud: "+strLongitud);


                try {
                    jsonObject.put("nombreEstudiante", estudianteSeleccionado);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                accion="Termina ruta";
                jsonObject.put("evento", accion);
                jsonObject.put("datetime", timestamp);
                jsonObject.put("ruta", ruta);
                jsonObject.put("colegio", colegio);
                jsonObject.put("latitud", strLatitud);
                jsonObject.put("longitud", strLongitud);
                jsonObject.put("segundos",seconds);


                //Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG).show();
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("json", jsonObject.toString()));
                String response = makePOSTRequest("http://107.170.62.116/phpControlRutas/leerEventos.php", nameValuePairs );
                Log.d(LOGTAG,"Response: "+response);
                if (response.equals("PROBLEM")){
                    //Toast.makeText(Login.this,"Usuario y/o contraseña errados!",Toast.LENGTH_SHORT).show();
                    status=false;
                }
                if (response.equals("OK")){

                    status=true;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return status;
        }
        public String makePOSTRequest(String url, List<NameValuePair> nameValuePairs) {
            String response = "";

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                HttpResponse httpResponse = null;
                try {
                    httpResponse = httpClient.execute(httpPost);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                JSONObject object = null;
                Log.d(LOGTAG, "Response:" + jsonResult);
                try {
                    object = new JSONObject(jsonResult);
                    String estadoLogin = object.getString("rta");

                    Log.d(LOGTAG, "Respuesta Server:" + estadoLogin);
                    response=estadoLogin;

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(LOGTAG, "Error1:" + e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        private StringBuilder inputStreamToString(InputStream is)
        {
            String rLine = "";
            StringBuilder answer = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            try
            {
                while ((rLine = rd.readLine()) != null)
                {
                    answer.append(rLine);
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return answer;
        }

    }

}
