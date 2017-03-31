package com.coltrack.controlrutasmonitorcolturex;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class amopm extends AppCompatActivity {
    String ruta;
    TextView logoMain;
    TextView textViewRuta;
    RadioButton rBtnManana;
    RadioButton rBtnTarde;
    Button btnVolver;
    Button btnContinuar;
    String accion;
    String LOGTAG="log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amopm);
        Bundle bundle=getIntent().getExtras();
        ruta=bundle.getString("ruta").toString();
        logoMain=(TextView)findViewById(R.id.textViewPrincipal);
        textViewRuta=(TextView)findViewById(R.id.textViewRuta);
        textViewRuta.setText("Ruta: "+ruta);
        rBtnManana=(RadioButton)findViewById(R.id.rBtnMañana);
        rBtnTarde=(RadioButton)findViewById(R.id.rBtnTarde);
        btnVolver=(Button)findViewById(R.id.buttonVolver);
        btnContinuar=(Button)findViewById(R.id.buttonContinuar);
        rBtnManana.setChecked(true);
        accion="recoger";
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });
        rBtnManana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accion="recoger";
                Log.d(LOGTAG, "Accion: " + accion);
            }
        });
        rBtnTarde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accion="dejar";
                Log.d(LOGTAG,"Accion: "+accion);
            }
        });
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getApplicationContext(),Listado.class);
                i.putExtra("accion",accion);
                i.putExtra("ruta",ruta);
                startActivity(i);

            }
        });

    }
}
