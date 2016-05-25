package com.coltrack.controlrutasmonitor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
    TextView textViewEstudianteSeleccionado;
    TextView textViewGrado;
    TextView textViewMobilAcudiente;


    String ruta;
    String accion;
    Button buttonAccion;
    String LOGTAG="log";
    String evento;
    String finalizado;
    Button buttonRecogerDejar;

    Button buttonFinalizar;
    Button buttonLlamar;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);
        textViewEstudiantesRuta=(TextView)findViewById(R.id.textViewEstudiantesRuta);
        textViewAccion=(TextView)findViewById(R.id.textViewAccion);
        buttonAccion=(Button)findViewById(R.id.buttonAccion);
        buttonRecogerDejar=(Button)findViewById(R.id.buttonRecoger);
        buttonFinalizar=(Button)findViewById(R.id.buttonFinalizar);
        expListView = (ExpandableListView) findViewById(R.id.lvExp1);
        buttonLlamar=(Button)findViewById(R.id.buttonLlamar);
        textViewProgreso=(TextView)findViewById(R.id.textViewProgreso);
        textViewEstudianteSeleccionado=(TextView)findViewById(R.id.textViewEstudianteSeleccionado);
        textViewGrado=(TextView)findViewById(R.id.textViewGrado);
        textViewMobilAcudiente=(TextView)findViewById(R.id.textViewMobilAcudiente);




        Bundle bundle=getIntent().getExtras();
        ruta=bundle.getString("ruta").toString();
        accion=bundle.getString("accion");
        textViewEstudiantesRuta.setText("Estudiantes Ruta: " + ruta);
        if (accion.equals("recoger")){
            textViewAccion.setText("Accion: Recoger Estudiantes");
            buttonAccion.setText("Comenzar Recoger Estudiantes");
        }else if (accion.equals("dejar")){
            textViewAccion.setText("Accion: Dejar Estudiantes");
            buttonAccion.setText("Comenzar Dejar Estudiantes");

        }
        db = openOrCreateDatabase("controlRutas", MODE_PRIVATE, null);
        //db.execSQL("DROP TABLE IF EXISTS eventos");//borramos tabla
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + "eventos"
                + " (ruta TEXT, evento TEXT, timeStamp TEXT,finalizado TEXT);");
        c = db.rawQuery("select * from eventos where ruta="+"'"+ruta+"'", null);
        Log.d(LOGTAG, "numero de filas db: " + c.getCount());

        if (c.getCount()==0) {
            //si la tabla estaba vacia
            if (accion.equals("recoger")) {
                db.execSQL("insert into eventos (ruta,evento,finalizado) values(" + "'" + ruta + "'," + "'" + "recoger" + "'" + "," + "'" + "si" + "'" + ")");
            }
            if (accion.equals("dejar")) {
                db.execSQL("insert into eventos (ruta,evento,finalizado) values(" + "'" + ruta + "'," + "'" + "dejar" + "'" + "," + "'" + "si" + "'" + ")");
            }
        }else {


        }
        //c.close();
        buttonFinalizar.setVisibility(View.GONE);
        buttonRecogerDejar.setVisibility(View.GONE);
        buttonLlamar.setVisibility(View.GONE);
        expListView.setVisibility(View.GONE);
        textViewEstudianteSeleccionado.setVisibility(View.GONE);
        textViewGrado.setVisibility(View.GONE);
        textViewMobilAcudiente.setVisibility(View.GONE);

        c = db.rawQuery("select * from eventos where ruta=" + "'" + ruta + "'", null);
        c.moveToNext();
        evento=c.getString(c.getColumnIndex("evento"));
        Log.d(LOGTAG, "Evento en db: " + evento);
        finalizado=c.getString(c.getColumnIndex("finalizado"));
        Log.d(LOGTAG, "Finalizado evento en db: " + finalizado);
        if (finalizado.equals("si")){
            buttonAccion.setVisibility(View.VISIBLE);
            buttonAccion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(Listado.this)
                            .setTitle("Accion")
                            .setMessage("Desea  "+buttonAccion.getText().toString()+" ?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //Toast.makeText(Listado.this, "Yaay", Toast.LENGTH_SHORT).show();
                                    if (accion.equals("recoger")){
                                        buttonAccion.setVisibility(View.GONE);
                                        //buttonRecoger.setVisibility(View.VISIBLE);
                                        //buttonLlamar.setVisibility(View.VISIBLE);
                                        expListView.setVisibility(View.VISIBLE);
                                    }
                                    if (accion.equals("dejar")){
                                        buttonAccion.setVisibility(View.GONE);
                                        //buttonDejar.setVisibility(View.VISIBLE);
                                        //buttonLlamar.setVisibility(View.VISIBLE);
                                        expListView.setVisibility(View.VISIBLE);
                                    }
                                    buttonFinalizar.setVisibility(View.VISIBLE);
                                }})
                            .setNegativeButton("No", null).show();
                }
            });
        }else {
            buttonAccion.setVisibility(View.GONE);
        }
        listado = new ArrayList<String>();
        pupulateItems();

        buttonRecogerDejar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Listado.this)
                        .setTitle("Accion")
                        .setMessage("Desea "+accion+" al estudiante " +estudianteSeleccionado+"?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                //Toast.makeText(Listado.this, "Yaay", Toast.LENGTH_SHORT).show();
                                db.execSQL("update estudiantes set evento='realizado' where nombreEstudiante='" + estudianteSeleccionado + "'");
                                //buttonFinalizar.setVisibility(View.GONE);
                                buttonRecogerDejar.setVisibility(View.GONE);
                                buttonLlamar.setVisibility(View.GONE);
                                textViewEstudianteSeleccionado.setVisibility(View.GONE);
                                textViewGrado.setVisibility(View.GONE);
                                textViewMobilAcudiente.setVisibility(View.GONE);
                                estudiantesProcesados++;
                                pupulateItems();
                                //Enviamos el evento al servidor
                                Log.d(LOGTAG,"Enviando evento al servidor....");
                                sendEventos send1=new sendEventos();
                                send1.execute();






                            }})
                        .setNegativeButton("No", null).show();

            }
        });


        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                estudianteSeleccionado = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                Log.d(LOGTAG, "Estudiante Seleccionado: " + estudianteSeleccionado);
                textViewEstudianteSeleccionado.setVisibility(View.VISIBLE);
                textViewEstudianteSeleccionado.setText("Estudiante Seleccionado: " + estudianteSeleccionado);
                textViewGrado.setVisibility(View.VISIBLE);
                textViewMobilAcudiente.setVisibility(View.VISIBLE);
                c = db.rawQuery("SELECT * FROM estudiantes WHERE nombreEstudiante=" + "'" + estudianteSeleccionado + "'", null);
                c.moveToFirst();
                if (c != null) {
                    do {
                        String grado = null;
                        grado = c.getString(c.getColumnIndex("curso"));
                        String correoAcudiente = c.getString(c.getColumnIndex("correoAcudiente"));
                        String telefonoAcudiente = c.getString(c.getColumnIndex("telefonoAcudiente"));
                        colegio= c.getString(c.getColumnIndex("colegio"));
                        Log.d(LOGTAG, "Grado Estudiante Seleccionado: " + grado);
                        Log.d(LOGTAG, "Telefono Acudiente: " + telefonoAcudiente);
                        Log.d(LOGTAG, "Correo Acudiente: " + correoAcudiente);
                        textViewGrado.setText("Grado: " + grado);
                        textViewMobilAcudiente.setText("Telefono Acudiente: " + telefonoAcudiente);
                    } while (c.moveToNext());
                    buttonLlamar.setVisibility(View.VISIBLE);
                    buttonFinalizar.setVisibility(View.VISIBLE);
                    if (accion.equals("recoger")) {
                        buttonRecogerDejar.setVisibility(View.VISIBLE);
                        buttonRecogerDejar.setText("Recoger");
                    } else if (accion.equals("dejar")) {
                        buttonRecogerDejar.setVisibility(View.VISIBLE);
                        buttonRecogerDejar.setText("Dejar");
                    }
                }


                parent.collapseGroup(groupPosition);
                return false;
            }
        });








        //db.close();





    }
    public void pupulateItems(){
        // get the listview
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        listDataHeader.add("Listado Estudiantes");
        listado.clear();

        c=db.rawQuery("SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"'", null);
        c.moveToFirst();
        estudiantesTotales=0;
        if (c != null && c.getCount()>0) {
            // Loop through all Results
            do {
                estudiantesTotales++;

            }while(c.moveToNext());
        }

        c = db.rawQuery("SELECT * FROM " + "estudiantes WHERE ruta1='"+ruta+"' and evento='FALTA'", null);
        int Column1=0;int Column2=0;int Column3=0;int Column4=0;int Column5=0;
        int Column6=0;int Column7=0;int Column8=0;
        Column1 = c.getColumnIndex("nombreEstudiante");
        Column2 = c.getColumnIndex("ruta1");
        Column3 = c.getColumnIndex("nombreAcudiente");
        Column4 = c.getColumnIndex("telefonoAcudiente");
        Column5 = c.getColumnIndex("correoAcudiente");
        c.moveToFirst();
        String Data=null;

        if (c != null && c.getCount()>0) {
            // Loop through all Results
            do {
                Data=c.getString(Column1)+'\t'+c.getString(Column2)+'\t'+c.getString(Column3)+'\t'+c.getString(Column4)+'\t'+c.getString(Column5);
                //Log.d(LOGTAG, Data);
                listado.add(c.getString(Column1));

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
        textViewProgreso.setText("Progreso: "+estudiantesProcesados+" estudiantes de "+estudiantesTotales+ " ["+porcentaje+" %]");
        listDataChild.put(listDataHeader.get(0), listado); // Header, Child data
        listAdapter=new com.coltrack.controlrutasmonitor.ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);


    }


    //CLASE PARA ENVIAR DATOS DE EVENTO AL SERVIDOR
    private class sendEventos extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(Listado.this, "Estado Conexion Servidor", "Conectando...");
            Log.d(LOGTAG,"preexecute");
        }
        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(LOGTAG,"post execute");
            if (pd.isShowing()) {
                pd.dismiss();
            }
            if (!result) {
                Toast.makeText(Listado.this,"Problema procesando evento!!",Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //update ui here
                        // display toast here
                        //Toast.makeText(Login.this,"Usuario y/o contraseña invalidos!!!!",Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                Toast.makeText(Listado.this,"Evento procesado correctamente!!",Toast.LENGTH_SHORT).show();

            }
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean status=false;
            Log.d(LOGTAG, "doing");
            jsonObject = new JSONObject();
            try {


                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = sdf.format(new Date());

                Log.d(LOGTAG,"nombreEstudiante: "+estudianteSeleccionado);
                Log.d(LOGTAG,"evento: "+accion);
                Log.d(LOGTAG,"datetime: "+timestamp);
                Log.d(LOGTAG,"ruta: "+ruta);
                Log.d(LOGTAG,"colegio: "+colegio);
                Log.d(LOGTAG,"latitud: "+latitud);
                Log.d(LOGTAG,"longitud: "+longitud);




                jsonObject.put("nombreEstudiante", estudianteSeleccionado);
                jsonObject.put("evento", accion);
                jsonObject.put("datetime", timestamp);
                jsonObject.put("ruta", ruta);
                jsonObject.put("colegio", colegio);
                jsonObject.put("latitud", latitud);
                jsonObject.put("longitud", longitud);


                //Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG).show();
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("json", jsonObject.toString()));
                String response = makePOSTRequest("http://107.170.38.31/phpControlRutas/leerEventos.php", nameValuePairs );
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
