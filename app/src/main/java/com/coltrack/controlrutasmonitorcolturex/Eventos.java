package com.coltrack.controlrutasmonitorcolturex;

import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Eventos extends AppCompatActivity {
    String LOGTAG="log";
    TextView txtViewEstudiante;
    RadioButton rBtnEnfermedad;
    RadioButton rBtnAusente;
    RadioButton rBtnOtraRuta;
    JSONObject jsonObject;
    String cuerpoMensaje=null;
    Button btnVolver;
    Button btnEnviarNovedad;
    String evento;
    EditText editTxt;
    EditText editTextEvento;
    String nombreEstudiante;
    String eventos;
    String dateTime;
    String ruta;
    String colegio;
    String latitud;
    String longitud;
    String segundos;
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_eventos);
        btnVolver=(Button)findViewById(R.id.btnVolver);
        rBtnEnfermedad=(RadioButton)findViewById(R.id.rBtnEnfermedad);
        rBtnAusente=(RadioButton)findViewById(R.id.rBtnAusente);
        rBtnOtraRuta=(RadioButton)findViewById(R.id.rBtnOtraRtua);
        editTextEvento=(EditText)findViewById(R.id.editTextEvento);
        btnEnviarNovedad=(Button)findViewById(R.id.btnEnviarNovedad);
        txtViewEstudiante=(TextView)findViewById(R.id.txtViewEstudiante);


        Bundle bundle=getIntent().getExtras();
        nombreEstudiante=bundle.getString("estudianteSeleccionado");
        txtViewEstudiante.setText("Estudiante: "+nombreEstudiante);
        eventos=bundle.getString("evento");
        dateTime=bundle.getString("dateTime");
        ruta=bundle.getString("ruta");
        colegio=bundle.getString("colegio");

        latitud=bundle.getString("strLatitud");
        longitud=bundle.getString("strLongitud");
        segundos=bundle.getString("segundos");

        Log.d(LOGTAG, "Datos entrantes a evento.java");
        Log.d(LOGTAG,"nombreEstudiante: "+nombreEstudiante);
        Log.d(LOGTAG,"eventos: "+eventos);
        Log.d(LOGTAG,"dateTime: "+dateTime);
        Log.d(LOGTAG,"ruta: "+ruta);
        Log.d(LOGTAG,"colegio: "+colegio);
        Log.d(LOGTAG,"strlatitud: "+latitud);
        Log.d(LOGTAG,"strlongitud: "+longitud);
        Log.d(LOGTAG,"segundos: "+segundos);


        rBtnAusente.setChecked(true);
        evento="ausente";
        editTextEvento.setText(evento);


        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        rBtnEnfermedad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                evento = "enfermedad";
                editTextEvento.setText(evento);

            }
        });
        rBtnAusente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                evento="ausente";
                editTextEvento.setText(evento);

            }
        });
        rBtnOtraRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                evento="otra ruta";
                editTextEvento.setText(evento);

            }
        });

        btnEnviarNovedad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEventos send1=new sendEventos();
                send1.execute();

            }
        });


    }
    //CLASE PARA ENVIAR DATOS DE EVENTO AL SERVIDOR
    private class sendEventos extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(Eventos.this, "Estado Conexion Servidor", "Conectando...");
            Log.d(LOGTAG,"preexecute");
        }
        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(LOGTAG,"post execute");
            if (pd.isShowing()) {
                pd.dismiss();
            }
            if (!result) {
                Toast.makeText(Eventos.this, "Problema procesando evento!!", Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //update ui here
                        // display toast here
                        //Toast.makeText(Login.this,"Usuario y/o contraseña invalidos!!!!",Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                Toast.makeText(Eventos.this,"Evento procesado correctamente!!",Toast.LENGTH_SHORT).show();
                db = openOrCreateDatabase("controlRutas", MODE_PRIVATE, null);



                String queri="update estudiantes set evento='realizado' where nombreEstudiante='" + nombreEstudiante + "'";
                Log.d(LOGTAG, "Query boton evento: " + queri);
                db.execSQL(queri);
                db.close();
                finish();

                //estudiantesProcesados++;
                //populateItems();


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

                Log.d(LOGTAG,"nombreEstudiante: "+nombreEstudiante);
                Log.d(LOGTAG,"evento: "+eventos);
                Log.d(LOGTAG,"datetime: "+timestamp);
                Log.d(LOGTAG,"ruta: "+ruta);
                Log.d(LOGTAG,"colegio: "+colegio);
                Log.d(LOGTAG,"latitud: "+latitud);
                Log.d(LOGTAG,"longitud: "+longitud);


                try {
                    jsonObject.put("nombreEstudiante", nombreEstudiante);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                evento=editTextEvento.getText().toString();
                jsonObject.put("evento", evento);
                jsonObject.put("datetime", timestamp);
                jsonObject.put("ruta", ruta);
                jsonObject.put("colegio", colegio);
                jsonObject.put("latitud", latitud);
                jsonObject.put("longitud", longitud);
                jsonObject.put("segundos",segundos);


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
