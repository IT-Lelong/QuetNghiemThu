package com.example.klb_pda;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.klb_pda.QR232.QR232_Main;
import com.example.klb_pda.QRprint.qrcode_print;
import com.example.klb_pda.qr231.qr231;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Menu extends AppCompatActivity {

    String g_server = "";
    Button btnQR210, btnQR230, btnprint, btnQR231, btnQR232;
    TextView menuID;
    String ID;
    private CheckAppUpdate checkAppUpdate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Bundle getbundle = getIntent().getExtras();
        ID = getbundle.getString("ID");
        g_server = getbundle.getString("SERVER");
        btnQR210 = (Button) findViewById(R.id.btnQR210);
        btnQR230 = (Button) findViewById(R.id.btnQR230);
        btnQR231 = (Button) findViewById(R.id.btnQR231);
        btnQR232 = (Button) findViewById(R.id.btnQR232);
        menuID = (TextView) findViewById(R.id.menuID);
        btnprint = (Button) findViewById(R.id.btnprint);


        btnQR210.setOnClickListener(btnlistener);
        btnQR230.setOnClickListener(btnlistener);
        btnQR231.setOnClickListener(btnlistener);
        btnQR232.setOnClickListener(btnlistener);
        btnprint.setOnClickListener(btnlistener);


        getIDname("http://172.16.40.20/" + g_server + "/getidJson.php?ID=" + ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAppUpdate = new CheckAppUpdate(this, g_server);
        checkAppUpdate.checkVersion();
    }

    private void getIDname(String apiUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = "";
                    URL url = new URL(apiUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.connect();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String jsonstring = reader.readLine();
                    reader.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!jsonstring.equals("FALSE")){
                                try{
                                JSONArray jsonarray = new JSONArray(jsonstring);
                                for (int i = 0; i < jsonarray.length(); i++) {
                                    JSONObject jsonObject = jsonarray.getJSONObject(i);
                                    menuID.setText(ID + " " + jsonObject.getString("TA_CPF001") + "\n" + jsonObject.getString("GEM02") );
                                    Constant_Class.UserID = ID;
                                    Constant_Class.UserName_zh = jsonObject.getString("CPF02");;
                                    Constant_Class.UserName_vn = jsonObject.getString("TA_CPF001");;
                                    Constant_Class.UserDepID = jsonObject.getString("CPF29");;
                                    Constant_Class.UserDepName = jsonObject.getString("GEM02");;
                                }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast alert = Toast.makeText(Menu.this, e.toString(), Toast.LENGTH_LONG);
                            alert.show();
                        }
                    });
                }
            }
        }).start();
    }

    private View.OnClickListener btnlistener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnQR210: {
                    try {
                        Intent QR210 = new Intent();
                        QR210.setClass(Menu.this, qr210.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", ID);
                        bundle.putString("SERVER", g_server);
                        QR210.putExtras(bundle);
                        startActivity(QR210);
                    } catch (Exception e) {
                        Toast alert = Toast.makeText(Menu.this, e.toString(), Toast.LENGTH_LONG);
                        alert.show();
                    }

                    break;
                }

                case R.id.btnQR230: {
                    try {
                        Intent QR230 = new Intent();
                        QR230.setClass(Menu.this, qr230.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", ID);
                        bundle.putString("SERVER", g_server);
                        QR230.putExtras(bundle);
                        startActivity(QR230);
                    } catch (Exception e) {
                        Toast alert = Toast.makeText(Menu.this, e.toString(), Toast.LENGTH_LONG);
                        alert.show();
                    }

                    break;
                }

                case R.id.btnQR231: {
                    try {
                        Intent QR231 = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", ID);
                        bundle.putString("SERVER", g_server);
                        QR231.setClass(Menu.this, qr231.class);
                        QR231.putExtras(bundle);
                        startActivity(QR231);
                    } catch (Exception e) {
                        Toast alert = Toast.makeText(Menu.this, e.toString(), Toast.LENGTH_LONG);
                        alert.show();
                    }
                    break;
                }

                case R.id.btnQR232: {
                    String[] g_factory = {"A", "B", "C", "D", "I"};
                    try {
                        Dialog dialog = new Dialog(Menu.this);
                        dialog.setContentView(R.layout.activity_qr232_dialog_choose_location);
                        Spinner FactorySpinner = dialog.findViewById(R.id.factory_spinner);
                        Button btnOK  = dialog.findViewById(R.id.btnok);
                        Button btnCancel  = dialog.findViewById(R.id.btncancel);

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_spinner_dropdown_item, g_factory);
                        FactorySpinner.setAdapter(adapter);
                        FactorySpinner.setSelection(2);

                        btnOK.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent QR232 = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putString("ID", ID);
                                bundle.putString("Factory", FactorySpinner.getSelectedItem().toString());
                                QR232.setClass(Menu.this, QR232_Main.class);
                                QR232.putExtras(bundle);
                                startActivity(QR232);
                                dialog.dismiss();
                            }
                        });

                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();

                    } catch (Exception e) {
                        Toast alert = Toast.makeText(Menu.this, e.toString(), Toast.LENGTH_LONG);
                        alert.show();
                    }
                    break;
                }

                case R.id.btnprint: {
                    try {
                        Intent printqrcode = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", ID);
                        bundle.putString("SERVER", g_server);
                        printqrcode.setClass(Menu.this, qrcode_print.class);
                        printqrcode.putExtras(bundle);
                        startActivity(printqrcode);
                    } catch (Exception e) {
                        Toast alert = Toast.makeText(Menu.this, e.toString(), Toast.LENGTH_LONG);
                        alert.show();
                    }
                    break;
                }


            }
        }
    };

}