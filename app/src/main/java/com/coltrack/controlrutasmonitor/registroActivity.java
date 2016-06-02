package com.coltrack.controlrutasmonitor;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class registroActivity extends AppCompatActivity {
    EditText edTxtNombre;
    EditText edTxtCorreo;
    EditText edTxtTelefono;
    EditText edTxtColegio;
    Button enviar;
    String strNombre;
    String strCorreo;
    String strTelefono;
    String strColegio;
    String LOGTAG="log";
    JSONObject jsonObject;
    int dia=0;
    int mes=0;
    int ano=0;
    int hora=0;
    int minuto=0;
    String asunto;
    String correo;
    Calendar cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        edTxtNombre=(EditText)findViewById(R.id.editTextNombre);
        edTxtCorreo=(EditText)findViewById(R.id.editTextCorreo);
        edTxtTelefono=(EditText)findViewById(R.id.editTextNumeroCelular);
        edTxtColegio=(EditText)findViewById(R.id.editTextColegio);
        enviar=(Button)findViewById(R.id.buttonEnviar);

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strNombre=null;
                strTelefono=null;
                strCorreo=null;
                strColegio=null;
                strColegio=edTxtColegio.getText().toString();
                strCorreo=edTxtCorreo.getText().toString();
                strNombre=edTxtNombre.getText().toString();
                strTelefono=edTxtTelefono.getText().toString();
                BackgroundTask enviarContacto = new BackgroundTask();
                enviarContacto.execute();

            }
        });

    }

    private class BackgroundTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(registroActivity.this, "Informacion de contacto", "Enviando...");
            Log.d(LOGTAG, "pre  execute");
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status=false;
            Log.d(LOGTAG, "doing");
            jsonObject = new JSONObject();
            try {
                leerFechaHora();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateandTime = sdf.format(new Date());
                Log.d(LOGTAG, "date: " + currentDateandTime);


                asunto="";
                asunto="Contacto: Se ha recibido la siguiente informacion de contacto:\r\n ";
                asunto=asunto+"Nombre: "+strNombre+",";
                asunto=asunto+"Correo: "+strCorreo+",";
                asunto=asunto+"Celular: "+strTelefono+",";
                asunto=asunto+"Colegio: "+strColegio+"";
                asunto=asunto+"a las "+currentDateandTime;


                jsonObject.put("nombre",strNombre);
                jsonObject.put("correo",strCorreo);
                jsonObject.put("telefono",strTelefono);
                jsonObject.put("colegio",strColegio);



                jsonObject.put("date",currentDateandTime);
                //jsonObject.put("cuerpoMensaje",asunto);

                Log.d(LOGTAG, "Enviando: ");
                Log.d(LOGTAG, "Asunto: " + asunto);
                Log.d(LOGTAG,"date: "+currentDateandTime);

                Log.d(LOGTAG, "Enviando datos de contacto al servidor....");
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("json", jsonObject.toString()));
                String response = makePOSTRequest("http://107.170.38.31/phpDir/datosContacto.php", nameValuePairs);
                Log.d(LOGTAG, "mail php response: "+response);
                if (response.equals("contacto almacenado")){
                    status=true;
                }else {
                    status=false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return status;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(LOGTAG,"post execute");
            if (pd.isShowing()) {
                pd.dismiss();
            }
            if (!result) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //update ui here
                        // display toast here
                        Toast.makeText(registroActivity.this, "Problema enviando datos contacto...", Toast.LENGTH_LONG).show();
                    }
                });
            }else {
                Toast.makeText(registroActivity.this,"Un agente de servicio al cliente pronto te contactara!!",Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }
    public void leerFechaHora(){
        cal = Calendar.getInstance();
        dia=cal.get(Calendar.DAY_OF_MONTH);
        mes=cal.get(Calendar.MONTH);
        ano=cal.get(Calendar.YEAR);
        hora=cal.get(Calendar.HOUR_OF_DAY);
        minuto=cal.get(Calendar.MINUTE);
    }
    public String makePOSTRequest(String url, List<NameValuePair> nameValuePairs) {
        String response = "";

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            try {
                HttpResponse httpResponse = httpClient.execute(httpPost);
                String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                JSONObject object = null;
                Log.d(LOGTAG, "Response:" + jsonResult);
                try {
                    object = new JSONObject(jsonResult);
                    String estadoLogin = object.getString("action");

                    Log.d(LOGTAG, "Estado Login:" + estadoLogin);
                    response=estadoLogin;

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(LOGTAG, "Error1:" + e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
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