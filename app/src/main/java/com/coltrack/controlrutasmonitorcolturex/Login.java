package com.coltrack.controlrutasmonitorcolturex;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.github.ybq.android.spinkit.style.FadingCircle;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    EditText editTextUsuario;
    EditText editTextPassword;
    EditText editTextRuta;
    Button btnIngresar;
    String LOGTAG="CONTROLRUTAS";
    //TextView btnRegistrar;
    String strUsuario;
    String strPass;
    String strRuta;
    JSONObject jsonObject;
    SQLiteDatabase myDB;
    SharedPreferences sharedPreferences;
    private AlarmManager alarmManager;
    private Intent gpsTrackerIntent;
    private PendingIntent pendingIntent;
    //ProgressBar progressBar;
    FadingCircle fadingCircle;
    Vibrator v;



    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setContentView(R.layout.activity_login);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
        Log.d(LOGTAG,"Starting app Control Rutas monitor...");
        sharedPreferences = this.getSharedPreferences("controlRutas", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("alarmState",false);
        editor.apply();
        v = (Vibrator) this.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);


        //progressBar = (ProgressBar)findViewById(R.id.progressBar);
        //progressBar.setVisibility(View.GONE);

        fadingCircle = new FadingCircle();

        editTextUsuario=(EditText)findViewById(R.id.editTextUsuario);
        editTextPassword=(EditText)findViewById(R.id.editTextPassword);
        editTextRuta=(EditText)findViewById(R.id.editTextRuta);
        btnIngresar=(Button)findViewById(R.id.btnIngresar);


        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strUsuario=editTextUsuario.getText().toString();
                strPass=editTextPassword.getText().toString();
                strRuta=editTextRuta.getText().toString();
                //Eliminamos espacios en blanco
                strUsuario=strUsuario.replaceAll(" ","");
                strPass=strPass.replaceAll(" ","");
                strRuta=strRuta.replaceAll(" ","");
                boolean isError=false;
                if (TextUtils.isEmpty(editTextUsuario.getText())){
                    editTextUsuario.setError("Ingrese nombre del usuario!!");
                    isError=true;
                }
                if (TextUtils.isEmpty(editTextPassword.getText())){
                    editTextPassword.setError("Ingrese una contraseña!!");
                    isError=true;
                }
                if (TextUtils.isEmpty(editTextRuta.getText())){
                    editTextRuta.setError("Ingrese la ruta!!");
                    isError=true;
                }
                if (!isError) {
                    //Empezamos asynctask que se comunica con el server
                    conexionServer con1 = new conexionServer();
                    con1.execute();
                }else {
                    Toast.makeText(getApplicationContext(), "Revise datos de acceso!!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    boolean isGPSEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
            return true;
        }else{
            Toast.makeText(getApplicationContext(),"Debe habilitar el GPS!!!",Toast.LENGTH_SHORT).show();
            showGPSDisabledAlertToUser();
            return false;

        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOGTAG,"Login OnStop");
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGTAG,"Login OnResume");
        if (isGPSEnabled()){
            Log.d(LOGTAG,"GPS habilitado....");
        }else {
            Log.d(LOGTAG,"GPS deshabilitado...habilitando....");
        }

    }

    private void startAlarmManager() {
        Log.d(LOGTAG,"Arrancando alarmManager!!!");
        Context context = getBaseContext();
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        gpsTrackerIntent = new Intent(context, GpsTrackerAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                1 * 60000, // 60000 = 1 minute
                pendingIntent);




    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS esta deshabilitado, para el funcionamiento de esta aplicacion debe habilitarlo!")
                .setCancelable(false)
                .setPositiveButton("Habilitar el GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    private class conexionServer extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog pd;
        @Override
        protected void onPreExecute() {




            pd = ProgressDialog.show(Login.this, "Estado Conexion Servidor", "Conectando...");
            Log.d(LOGTAG,"preexecute");
        }
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(LOGTAG,"post execute");

            if (!result) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //update ui here
                        // display toast here
                        Toast.makeText(Login.this,"Usuario y/o contraseña invalidos!!!!",Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                //Loging correcto
                //Cargamos base de datos de la ruta
                //Y la almacenamos en la base de datos SQLITE
                readSaveDB();
                myDB.close();
                sharedPreferences = getApplicationContext().getSharedPreferences("controlRutas", Context.MODE_PRIVATE);
                boolean estadoAlarma=sharedPreferences.getBoolean("alarmState",false);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Log.d(LOGTAG,"alarmState: "+estadoAlarma);
                if (!estadoAlarma){
                    Log.d(LOGTAG,"Arrancando alarmanager(1)...");
                    startAlarmManager();
                    editor.putBoolean("alarmState",true);
                    editor.apply();
                }
                editor.putString("Ruta",strRuta);
                editor.apply();



//                progressBar.setVisibility(View.VISIBLE);
//                FadingCircle fadingCircle = new FadingCircle();
//                progressBar.setIndeterminateDrawable(fadingCircle);




                AlertDialog dialog;

                final CharSequence[] items = {"MAÑANA","TARDE"};
                final ArrayList seletedItems=new ArrayList();
                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                builder.setTitle("Escoja el horario de ruta:");
                builder.setMultiChoiceItems(items, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            // indexSelected contains the index of item (of which checkbox checked)
                            @Override
                            public void onClick(DialogInterface dialog, int indexSelected,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    // write your code when user checked the checkbox
                                    Log.d(LOGTAG,"Se checkeo: "+indexSelected);
                                    seletedItems.add(indexSelected);

                                    Log.d(LOGTAG,"seletedItems size: "+seletedItems.size());
                                    if (seletedItems.size()>1){
                                        Toast.makeText(getApplicationContext(),"Solo debe seleccionar uno!!!",Toast.LENGTH_LONG).show();
                                        v.vibrate(1000);

                                    }else {

                                    }


                                } else if (seletedItems.contains(indexSelected)) {
                                    // Else, if the item is already in the array, remove it
                                    // write your code when user Uchecked the checkbox
                                    seletedItems.remove(Integer.valueOf(indexSelected));
                                    Log.d(LOGTAG,"Se descheckeo: "+indexSelected);
                                }
                                Log.d(LOGTAG,"items: "+seletedItems.toString());
                                if (seletedItems.size()>1){
                                    Log.d(LOGTAG,"seletedItems.size: "+seletedItems.size());

                                }
//                                if (seletedItems.size()>1){
//                                    for (int i=0;i<seletedItems.size();i++){
//                                        Log.d(LOGTAG,"borrando...");
//                                        seletedItems.remove(i);
//                                    }
//                                }


                            }
                        })
                        // Set the action buttons
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //  Your code when user clicked on OK
                                //  You can write the code  to save the selected item here
                                if (seletedItems.size()>1){
                                    v.vibrate(3000);
                                    Toast.makeText(getApplicationContext(),"Error: Solo debe seleccionar una jornada!!!",Toast.LENGTH_LONG).show();

                                }else {
                                    if (seletedItems.size()>0){
                                        String jornada=null;
                                        Log.d(LOGTAG,"seletedItems.get(0): "+seletedItems.get(0));
                                        if ((int)seletedItems.get(0)==0){
                                            jornada="recoger";
                                        }else if ((int)seletedItems.get(0)==1) {
                                            jornada="dejar";
                                        }


                                        Log.d(LOGTAG,"Jornada: "+jornada);
                                        Log.d(LOGTAG,"Abrimos Selecciona.java");

                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("Ruta",strRuta);
                                        editor.putString("Jornada",jornada);
                                        editor.apply();

                                        Intent i=new Intent(Login.this,Selecciona.class);
                                        startActivity(i);


                                    }else {
                                        v.vibrate(3000);
                                        Toast.makeText(getApplicationContext(),"Error: Debe seleccionar una jornada!!!",Toast.LENGTH_LONG).show();
                                    }

                                }


                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //  Your code when user clicked on Cancel

                            }
                        });
                        dialog = builder.create();//AlertDialog dialog; create like this outside onClick
                        dialog.show();



                //Intent i=new Intent(getApplicationContext(),amopm.class);
                //i.putExtra("ruta", strRuta);
                //startActivity(i);
            }
            if (pd.isShowing()) {
                pd.dismiss();
            }
        }

        public void readSaveDB(){
            final JSONObject jsonObject = new JSONObject();
            Log.d(LOGTAG,"Ruta: "+strRuta);
            try {
                jsonObject.put("ruta", strRuta);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("json", jsonObject.toString()));
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://107.170.62.116/phpControlRutas/leerEstudiantes.php");
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    try {
                        HttpResponse httpResponse = httpClient.execute(httpPost);
                        String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                        Log.d(LOGTAG, "EstudiantesJSON: " + jsonResult);








//                        String json=jsonResult;
//                        json=json.replace("[", "");
//                        json=json.replace("]", "");
//                        int nrows=countOccurrences(json, '{');
//                        Log.d(LOGTAG, "nrows: " + nrows);
//                        String[]parts=json.split(Pattern.quote("}"));
                        Log.d(LOGTAG,"Creando DB...");
                        myDB = openOrCreateDatabase("controlRutas", MODE_PRIVATE, null);
                        myDB.execSQL("DROP TABLE IF EXISTS estudiantes");//borramos tabla
                        myDB.execSQL("CREATE TABLE IF NOT EXISTS "
                                + "estudiantes"
                                + " (nid INTEGER,nombreEstudiante TEXT, ruta1 TEXT, nombreAcudiente TEXT, telefonoAcudiente TEXT, correoAcudiente TEXT, codigo TEXT, curso TEXT, colegio TEXT, evento TEXT, paraderoAM TEXT, paraderoPM TEXT);");


                        String nombreEstudiante=null;
                        String nombreAcudiente=null;
                        String telefonoAcudiente=null;
                        String correoAcudiente=null;
                        String codigo=null;
                        String curso=null;
                        String colegio=null;
                        String paraderoAM=null;
                        String paraderoPM=null;

                        JSONArray jsonarray = new JSONArray(jsonResult);
                        int registros=0;
                        for (registros = 0; registros < jsonarray.length(); registros++) {
                            JSONObject jsonobject = jsonarray.getJSONObject(registros);
                            nombreEstudiante = jsonobject.getString("nombreEstudiante");
                            nombreAcudiente = jsonobject.getString("nombreAcudiente");
                            telefonoAcudiente = jsonobject.getString("telefonoAcudiente");
                            correoAcudiente = jsonobject.getString("correoAcudiente");
                            curso = jsonobject.getString("curso");
                            colegio = jsonobject.getString("colegio");
                            codigo = jsonobject.getString("codigo");
                            paraderoAM = jsonobject.getString("paraderoAM");
                            paraderoPM = jsonobject.getString("paraderoPM");


                            //Log.d(LOGTAG,"nombreEstudiante: "+nombreEstudiante);

                            myDB.execSQL("INSERT INTO "
                                    + "estudiantes"
                                    + " (nid, nombreEstudiante, ruta1, nombreAcudiente, telefonoAcudiente, correoAcudiente, codigo, curso, colegio,evento,paraderoAM,paraderoPM)"
                                    + " VALUES ("+"'"+registros+"' ,"+"'"+ nombreEstudiante+"'" + ", "+"'"+strRuta+"'"+", "+"'"+nombreAcudiente+"'"+", "+"'"+telefonoAcudiente+"'"+", "+"'"+correoAcudiente +"'"+", "+"'"+codigo+"'"+", "+"'"+curso+"'"+", "+"'"+colegio+"'"+", 'FALTA',"+"'"+paraderoAM+"',"+"'"+paraderoPM+"'"+");");


                        }
                        Log.d(LOGTAG,"Se almacenaron en DB: "+registros+" registros");

                        Log.d(LOGTAG,"Reading DB...");
                        Cursor c;
                        c = myDB.rawQuery("Select * from estudiantes",null);
                        Log.d(LOGTAG,"c.getCount(): "+c.getCount());
                        c.moveToFirst();
                        if (c != null && c.getCount() > 0) {
                            do{
                                Log.d(LOGTAG,"nid: "+c.getInt(c.getColumnIndex("nid"))+"\t"+"nombreEstudiante: "+c.getString(c.getColumnIndex("nombreEstudiante"))+"\t\t\t\t"+c.getString(c.getColumnIndex("codigo")));
                                //Log.d(LOGTAG,"nid: "+c.getInt(0));
                            }while (c.moveToNext());
                        }





//                        for (int i=0;i<nrows;i++){
//                            parts[i]=parts[i].replace("{", "");
//                            if (i>0){
//                                parts[i]=parts[i].substring(1);
//                            }
//                            parts[i]=parts[i].replace("\"", "");
//                            Log.d(LOGTAG, "Parte: " + i + ":" + parts[i]);
//                            String[] partes=parts[i].split(Pattern.quote(","));
//                            String nombreEstudiante=null;
//                            String curso=null;
//                            String nombreAcudiente=null;
//                            String telefonoAcudiente=null;
//                            String correoAcudiente=null;
//                            String codigo=null;
//                            String colegio=null;
//                            String paraderoAM=null;
//                            String paraderoPM=null;
//                            for (int j=0;j<partes.length;j++){
//                                String[] subParts=partes[j].split(Pattern.quote(":"));
//                                Log.d(LOGTAG, "subparte " + j + ":" + subParts[1]);
//                                switch (j){
//                                    case 0:
//                                        nombreEstudiante=subParts[1];
//                                        break;
//                                    case 1:
//                                        curso=subParts[1];
//                                        break;
//                                    case 2:
//                                        telefonoAcudiente=subParts[1];
//                                        break;
//                                    case 3:
//                                        nombreAcudiente=subParts[1];
//                                        break;
//                                    case 4:
//                                        correoAcudiente=subParts[1];
//                                        break;
//                                    case 5:
//                                        codigo=subParts[1];
//                                        break;
//                                    case 6:
//                                        colegio=subParts[1];
//                                        break;
//                                    case 7:
//                                        paraderoAM=subParts[1];
//                                        break;
//                                    case 8:
//                                        paraderoPM=subParts[1];
//                                        break;
//                                }
//                            }
////                            myDB.execSQL("INSERT INTO "
////                                    + "estudiantes"
////                                    + " (nombreEstudiante, grado, nombreAcudiente, telefonoAcudiente, correoAcudiente, codigo, colegio)"
////                                    + " VALUES ("+"'"+ nombreEstudiante+"'" + ", "+"'"+curso+"'"+", "+"'"+nombreAcudiente+"'"+", "+"'"+telefonoAcudiente+"'"+", "+"'"+correoAcudiente +"'"+", "+"'"+codigo +"'"+", "+"'"+colegio+"'"+");");
//                            myDB.execSQL("INSERT INTO "
//                                    + "estudiantes"
//                                    + " (nombreEstudiante, ruta1, nombreAcudiente, telefonoAcudiente, correoAcudiente, codigo, curso, colegio,evento,paraderoAM,paraderoPM)"
//                                    + " VALUES ("+"'"+ nombreEstudiante+"'" + ", "+"'"+strRuta+"'"+", "+"'"+nombreAcudiente+"'"+", "+"'"+telefonoAcudiente+"'"+", "+"'"+correoAcudiente +"'"+", "+"'"+codigo +"'"+", "+"'"+curso +"'"+", "+"'"+colegio+"'"+", 'FALTA',"+"'"+paraderoAM+"',"+"'"+paraderoPM+"'"+");");
//
//                        }
                        Log.d(LOGTAG,"closing DB..");
                        myDB.close();



                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
        public  int countOccurrences(String haystack, char needle)
        {
            int count = 0;
            for (int i=0; i < haystack.length(); i++)
            {
                if (haystack.charAt(i) == needle)
                {
                    count++;
                }
            }
            return count;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status=false;
            Log.d(LOGTAG, "doing");
            jsonObject = new JSONObject();
            try {
                jsonObject.put("usuario", strUsuario);
                jsonObject.put("pass", strPass);
                jsonObject.put("ruta", strRuta);
                Log.d(LOGTAG, "Enviando a servidor: usuario: " + strUsuario);
                Log.d(LOGTAG,"Enviando a servidor: pass: "+strPass);
                Log.d(LOGTAG,"Enviando a servidor: ruta: "+strRuta);

                //Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG).show();
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("json", jsonObject.toString()));
                String response = makePOSTRequest("http://107.170.62.116/phpControlRutas/login.php", nameValuePairs );
                Log.d(LOGTAG,"Response: "+response);
                if (response.equals("0")){
                    //Toast.makeText(Login.this,"Usuario y/o contraseña errados!",Toast.LENGTH_SHORT).show();
                    status=false;
                }
                if (response.equals("1")){
                    //Toast.makeText(MainActivity.this,"Bienvenido!",Toast.LENGTH_SHORT).show();
                    //Intent i=new Intent(getApplicationContext(),ListadoEstudiantes.class);
                    //i.putExtra("usuario",strUsuario);
                    //i.putExtra("ruta", strRuta);
                    status=true;
                    //startActivity(i);
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
                try {
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                    JSONObject object = null;
                    Log.d(LOGTAG, "Response:" + jsonResult);
                    try {
                        object = new JSONObject(jsonResult);
                        String estadoLogin = object.getString("rta");

                        Log.d(LOGTAG, "Respuesta Server Login:" + estadoLogin);
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

}
