package com.example.klb_pda.qr231;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.klb_pda.Adapter.Barcode_adapter;
import com.example.klb_pda.Constant_Class;
import com.example.klb_pda.Listdata.Barcode_listData;
import com.example.klb_pda.R;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class qr231 extends AppCompatActivity implements EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener {
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    String ID, g_server;
    TextView head1;
    ListView list01;
    Button btnupload, btnclear;
    UIHandler uiHandler;
    JSONObject ujsonobject;
    JSONArray ujsonArray;
    ListView dialoglist01;
    qr231DB db = null;
    Barcode_adapter barcodeAdapter;
    ArrayList<Barcode_listData> barcodeListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr231);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        db = new qr231DB(this);
        db.open();
        db.close();
        db.open();
        Bundle getbundle = getIntent().getExtras();
        ID = getbundle.getString("ID");
        g_server = Constant_Class.server;
        head1 = (TextView) findViewById(R.id.qr231_head1);
        list01 = (ListView) findViewById(R.id.qr231_list01);
        btnupload = (Button) findViewById(R.id.qr231_btnupload);
        btnclear = (Button) findViewById(R.id.qr231_btnclear);
        btnupload.setOnClickListener(btnuploadlistener);
        btnclear.setOnClickListener(btnclearlistener);
        list01.setOnItemClickListener(lsit01listener);
        uiHandler = new UIHandler();

        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            //statusTextView.setText("EMDKManager Request Failed");
            updateStatus("EMDKManager Request Failed");
        }
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        // Get a reference to EMDKManager
        this.emdkManager = emdkManager;

        // Get a  reference to the BarcodeManager feature object
        initBarcodeManager();

        // Initialize the scanner
        initScanner();
    }

    @Override
    public void onClosed() {

        // The EMDK closed unexpectedly. Release all the resources.
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
        updateStatus("EMDK closed unexpectedly! Please close and restart the application.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (scanner != null) {
                // releases the scanner hardware resources for other application
                // to use. You must call this as soon as you're done with the
                // scanning.
                scanner.removeDataListener(this);
                scanner.removeStatusListener(this);
                scanner.disable();
                scanner = null;
            }
        } catch (ScannerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emdkManager != null) {

            // Clean up the objects created by EMDK manager
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        String dataStr = "";
        if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
            // Iterate through scanned data and prepare the data.
            for (ScanDataCollection.ScanData data : scanData) {
                String a = data.getData();
                //ScanDataCollection.LabelType labelType=data.getLabelType();
                dataStr = a;
            }
            if (head1.length() > 0) {
                updatedetail(dataStr);
            } else {
                updateData(dataStr);
            }

        }
    }

    @Override
    public void onStatus(StatusData statusData) {
        // The status will be returned on multiple cases. Check the state and take the action.
        // Get the current state of scanner in background
        StatusData.ScannerStates state = statusData.getState();
        String statusStr = "";
        // Different states of Scanner
        switch (state) {
            case IDLE:
                // Scanner is idle and ready to change configuration and submit read.
                statusStr = statusData.getFriendlyName() + " is   enabled and idle...";
                // Change scanner configuration. This should be done while the scanner is in IDLE state.
                setConfig();
                try {
                    // Starts an asynchronous Scan. The method will NOT turn ON the scanner beam,
                    //but puts it in a  state in which the scanner can be turned on automatically or by pressing a hardware trigger.
                    scanner.read();
                } catch (ScannerException e) {
                    updateStatus(e.getMessage());
                }
                break;
            case WAITING:
                // Scanner is waiting for trigger press to scan...
                statusStr = "Scanner is waiting for trigger press...";
                break;
            case SCANNING:
                // Scanning is in progress...
                statusStr = "Scanning...";
                break;
            case DISABLED:
                // Scanner is disabledstatusStr = statusData.getFriendlyName()+" is disabled.";
                break;
            case ERROR:
                // Error has occurred during scanning
                statusStr = "An error has occurred.";
                break;
            default:
                break;
        }
        // Updates TextView with scanner state on UI thread.
        updateStatus(statusStr);
    }

    private void deInitScanner() {
        if (scanner != null) {
            try {
                // Release the scanner
                scanner.release();
            } catch (Exception e) {
                updateStatus(e.getMessage());
            }
            scanner = null;
        }
    }

    private void setConfig() {
        if (scanner != null) {
            try {
                // Get scanner config
                ScannerConfig config = scanner.getConfig();
                // Enable haptic feedback
                if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                    config.scanParams.decodeHapticFeedback = true;
                }
                // Set scanner config
                scanner.setConfig(config);
            } catch (ScannerException e) {
                updateStatus(e.getMessage());
            }
        }
    }

    private void updateStatus(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update the status text view on UI thread with current scanner state
                //statusTextView.setText(""+  status);
                //vqrb00.setText(status);
            }
        });
    }

    private void initBarcodeManager() {
        // Get the feature object such as BarcodeManager object for accessing the feature.
        barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
        // Add external scanner connection listener.
        if (barcodeManager == null) {
            Toast.makeText(this, "Barcode scanning is not supported.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initScanner() {
        if (scanner == null) {
            // Get default scanner defined on the device
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            if (scanner != null) {
                // Implement the DataListener interface and pass the pointer of this object to get the data callbacks.
                scanner.addDataListener(this);

                // Implement the StatusListener interface and pass the pointer of this object to get the status callbacks.
                scanner.addStatusListener(this);

                // Hard trigger. When this mode is set, the user has to manually
                // press the trigger on the device after issuing the read call.
                // NOTE: For devices without a hard trigger, use TriggerType.SOFT_ALWAYS.
                scanner.triggerType = Scanner.TriggerType.HARD;

                try {
                    // Enable the scanner
                    // NOTE: After calling enable(), wait for IDLE status before calling other scanner APIs
                    // such as setConfig() or read().
                    scanner.enable();

                } catch (ScannerException e) {
                    updateStatus(e.getMessage());
                    deInitScanner();
                }
            } else {
                updateStatus("Failed to   initialize the scanner device.");
            }
        }

    }

    //取得調撥單內容
    private void updateData(final String dataStr) {
        Thread scan = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String g_URL = "http://172.16.40.20/" + g_server + "/PDA_QR231/getdata.php?tc_ptx001=" + dataStr;
                    String result = getdata(g_URL);
                    if (result.equals("FALSE")) {
                        qr231.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                                builder.setTitle("ERROR");
                                builder.setMessage(getString(R.string.qr231_msg01));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.show();
                            }
                        });
                    } else {
                        ujsonobject = new JSONObject(result);
                        if (ujsonobject.getJSONArray("detail1").length() == 0) {
                            qr231.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                                    builder.setTitle("ERROR");
                                    builder.setMessage(getString(R.string.qr231_msg02));
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                                    builder.show();
                                }
                            });
                        } else {
                            head1.setText(dataStr);
                            uiHandler.sendEmptyMessage(0);
                        }
                    }
                } catch (Exception e) {
                    qr231.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                            builder.setTitle("ERROR");
                            builder.setMessage(getString(R.string.qr231_msg01));
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.show();
                        }
                    });
                }

            }
        });
        scan.start();
    }

    //掃描後更新資料
    private void updatedetail(String datastr) {
        try {
            if (datastr.startsWith("new")) {
                String qr01 = "", qr02 = "";
                Double qr03 = 0.0;
                //重印、拆單標籤格式 new_料號_批號_數量
                int index1 = datastr.indexOf("_");
                int index2 = datastr.indexOf("_", index1 + 1);
                int index3 = datastr.indexOf("_", index2 + 1);
                int index4 = datastr.indexOf("_", index3 + 1);  //預備給 測試料號會多出 T_
                //料號
                qr01 = datastr.substring(4, index2);
                if (qr01.equals("T")) {
                    qr01 = datastr.substring(4, index3);
                    qr02 = datastr.substring(index3 + 1, index4);
                    qr03 = Double.valueOf(datastr.substring(index4 + 1));
                } else {
                    //批號
                    qr02 = datastr.substring(index2 + 1, index3);
                    //數量
                    qr03 = Double.valueOf(datastr.substring(index3 + 1));
                }
                scan(datastr, qr01, qr02, qr03);

            } else if (datastr.substring(0, 5).equals("BC525") || datastr.substring(0, 5).equals("BC527") ||
                    datastr.substring(0, 5).equals("BB525") || datastr.substring(0, 5).equals("BB527") ||
                    datastr.substring(0, 5).equals("BC52F")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String qr01 = "", qr02 = "";
                        Double qr03 = 0.0;
                        //極版標籤 BC527-2101000198_1_07030333C_29568
                        int index1 = datastr.indexOf("_");
                        int index2 = datastr.indexOf("_", index1 + 1);
                        int index3 = datastr.indexOf("_", index2 + 1);
                        int index4 = datastr.indexOf("_", index3 + 1); //預備給 測試料號會多出 T_

                        //取得料號
                        qr01 = datastr.substring(index2 + 1, index3);
                        //取得批號
                        qr02 = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + datastr + "&kind=" + 2);
                        if (qr01.equals("T")) {
                            qr01 = datastr.substring(index2 + 1, index4);
                            qr03 = Double.valueOf(datastr.substring(index4 + 1));
                        } else {
                            //取得數量
                            qr03 = Double.valueOf(datastr.substring(index3 + 1));
                        }

                        scan(datastr, qr01, qr02, qr03);
                    }
                });
                api.start();
            } else if (datastr.substring(0, 5).equals("CC510") || datastr.substring(0, 5).equals("CC512") || datastr.substring(0, 5).equals("CC513") || datastr.substring(0, 5).equals("CC514") || datastr.substring(0, 5).equals("CC515") || datastr.substring(0, 5).equals("CC518") || datastr.substring(0, 8).equals("OLDSTAMP")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Double qr03 = 0.0;
                        //廣泰標籤 CC512-2010000697-108(CC5A2_KD91-1_NULL)_(03020489V_nap_WPX7A-BA)_20201026007002
                        int index1 = datastr.indexOf("-", 6);
                        int index2 = datastr.indexOf("("); //第一個(位置
                        int index3 = datastr.indexOf("(", index2 + 1); //第二個(位置
                        int index4 = datastr.indexOf("_", index3); //料號後的_位置
                        //取得料號
                        String qr01 = datastr.substring(index3 + 1, index4);
                        //取得批號
                        String qr02 = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + datastr + "&kind=" + 1);
                        //取得數量
                        //取得數量
                        if (index2 > 32) {
                            String g_qr03_datastr = datastr.substring(0, index2);
                            Integer g_len_g_qr03_datastr = g_qr03_datastr.length();
                            Integer l_sl1 = 0, l_sl2 = 0, l_sl3 = 0, k = 0;

                            for (int i = 0; i <= g_len_g_qr03_datastr; i++) {
                                String l_code = "";
                                String j = "";
                                if (i < g_len_g_qr03_datastr) {
                                    j = g_qr03_datastr.substring(i, i + 1);
                                }

                                if (j.equals("_") || i == g_len_g_qr03_datastr) {
                                    if (i == g_len_g_qr03_datastr) {
                                        l_code = g_qr03_datastr.substring(k, i);
                                        l_sl3 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                    } else {
                                        l_code = g_qr03_datastr.substring(k, i);
                                        if (l_sl1 == 0) {
                                            l_sl1 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                        } else {
                                            l_sl2 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                        }
                                    }
                                    k = i + 1;
                                }
                            }

                            qr03 = Double.valueOf(l_sl1 + l_sl2 + l_sl3);
                        } else {
                            qr03 = Double.valueOf(datastr.substring(index1 + 1, index2));
                        }
                        scan(datastr, qr01, qr02, qr03);
                    }
                });
                api.start();
            } else if (datastr.startsWith("-", 5)) {
                //供應商條碼 BB421-2101000169_2_07100071A_20211007_792
                int index1 = datastr.indexOf("_");
                int index2 = datastr.indexOf("_", index1 + 1);
                int index3 = datastr.indexOf("_", index2 + 1);
                int index4 = datastr.indexOf("_", index3 + 1);
                //取得料號
                String qr01 = datastr.substring(index2 + 1, index3);
                //取得批號
                String qr02 = datastr.substring(index3 + 1, index4);
                //取得數量
                Double qr03 = Double.valueOf(datastr.substring(index4 + 1));
                scan(datastr, qr01, qr02, qr03);
            } else {
                qr231.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg02));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            }
        } catch (Exception e) {

        }
    }

    //QR_code,MVL,Số Lô, Số Lượng
    private void scan(String xqr231b_02, String xqr231b_03, String xqr231b_04, Double xqr231b_05) {
        try {
            //確認是否已過帳
            String g_ketqua = check_tc_ptxpost();
            if (g_ketqua.equals("TRUE")) {
                //確認是否標籤已掃過
                String g_URL = "http://172.16.40.20/" + g_server + "/PDA_QR231/check_data.php?qr_ptb003=" + xqr231b_02;
                String chk_result = getdata(g_URL);
                if (chk_result.equals("FALSE")) {
                    qr231.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                            builder.setTitle("ERROR");
                            builder.setMessage(getString(R.string.qr231_msg03));
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.show();
                        }
                    });
                } else {
                    String result = db.scan(xqr231b_02, xqr231b_03, xqr231b_04, xqr231b_05);
                    if (result.equals("FALSE")) {
                        qr231.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                                builder.setTitle("ERROR");
                                builder.setMessage(getString(R.string.qr210_msg03));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.show();
                            }
                        });
                    } else if (result.equals("NORECORD")) {
                        qr231.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                                builder.setTitle("ERROR");
                                builder.setMessage(getString(R.string.qr210_msg04));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.show();
                            }
                        });
                    } else if (result.equals("OVERQTY")) {
                        qr231.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                                builder.setTitle("ERROR");
                                builder.setMessage(getString(R.string.qr210_msg05));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.show();
                            }
                        });
                    } else if (result.equals("HASRECORD")) {
                        qr231.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                                builder.setTitle("ERROR");
                                builder.setMessage(getString(R.string.qr231_msg03));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.show();
                            }
                        });
                    } else {
                        uiHandler.sendEmptyMessage(2);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private String check_tc_ptxpost() {
        String g_ketqua = "FALSE";
        String g_URL = "http://172.16.40.20/" + g_server + "/PDA_QR231/check_tc_ptxpost.php?tc_ptx001=" + head1.getText().toString();
        String chk_result = getdata(g_URL);
        if (chk_result.equals("TRUE")) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(qr231.this);
                    builder1.setCancelable(false);
                    builder1.setTitle(getString(R.string.qr210_msg06));
                    builder1.setMessage(getString(R.string.qr231_msg04));
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.close();
                            db.open();
                            uiHandler.sendEmptyMessage(1);
                        }
                    });
                    builder1.show();
                }
            });
        } else {
            g_ketqua = "TRUE";
        }

        return g_ketqua;
    }

    private View.OnClickListener btnuploadlistener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (head1.getText().length() > 0) {
                //確認是否已過帳
                String g_ketqua = check_tc_ptxpost();
                if (g_ketqua.equals("TRUE")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                    builder.setCancelable(false);
                    builder.setTitle(getString(R.string.qr210_msg08));
                    builder.setMessage(getString(R.string.qr210_msg09));
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String result = upload();
                                    if (result.equals("TRUE")) {
                                        qr231.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                                                builder.setTitle(getString(R.string.qr210_msg08));
                                                builder.setMessage(getString(R.string.qr210_msg10));
                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        //qr231_mail("http://172.16.40.20/" + g_server + "/PDA_qr231/mail.php?ID=" + ID);
                                                    }
                                                });
                                                builder.show();
                                                uiHandler.sendEmptyMessage(1);
                                            }
                                        });
                                    } else {
                                        qr231.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                                                builder.setTitle(getString(R.string.qr210_msg08));
                                                builder.setMessage(getString(R.string.qr210_msg11));
                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                }
                            });
                            thread.start();
                        }
                    });
                    builder.show();
                }
            } else {
                Toast.makeText(qr231.this, R.string.qr210_msg16, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener btnclearlistener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (head1.getText().length() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(qr231.this);
                builder.setCancelable(false);
                builder.setTitle(getString(R.string.qr210_msg12));
                builder.setMessage(getString(R.string.qr210_msg13));
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.close();
                        db.open();
                        uiHandler.sendEmptyMessage(1);
                    }
                });
                builder.show();
            } else {
                Toast.makeText(qr231.this, R.string.qr210_msg16, Toast.LENGTH_SHORT).show();
            }
        }
    };


    private AdapterView.OnItemClickListener lsit01listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Button dialogbtn01, btn_select_all, btn_cancel_all;

            TextView item01 = (TextView) view.findViewById(R.id.qr210_view01_item01);
            TextView item02 = (TextView) view.findViewById(R.id.qr210_view01_item02);
            String qr231_01 = item01.getText().toString();
            String qr231_02 = item02.getText().toString();
            final Dialog dialog = new Dialog(qr231.this);
            dialog.setContentView(R.layout.activity_qr210_dialog01);

            dialogbtn01 = (Button) dialog.findViewById(R.id.qr210_dialog01_btn01);
            btn_select_all = dialog.findViewById(R.id.btn_select_all);
            btn_cancel_all = dialog.findViewById(R.id.btn_cancel_all);
            dialoglist01 = (ListView) dialog.findViewById(R.id.qr210_dialog01_list01);

            btn_select_all.setEnabled(false);
            btn_cancel_all.setEnabled(false);

            barcodeListData = new ArrayList<>();
            barcodeAdapter = new Barcode_adapter(getApplicationContext(),
                    R.layout.activity_qr210_dialog01_view,
                    barcodeListData);
            dialoglist01.setAdapter(barcodeAdapter);
            Cursor cursor = db.getdialogdetail(qr231_02);
            UpdateAdapterdialog(cursor);

            dialogbtn01.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uiHandler.sendEmptyMessage(2);
                    dialog.dismiss();
                }
            });

            btn_select_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.updCheckBox(qr231_02, true);
                    Cursor cursor = db.getdialogdetail(qr231_02);
                    UpdateAdapterdialog(cursor);
                }
            });

            btn_cancel_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.updCheckBox(qr231_02, false);
                    Cursor cursor = db.getdialogdetail(qr231_02);
                    UpdateAdapterdialog(cursor);
                }
            });


            //點選刪除
            dialoglist01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        TextView dialogitem01 = (TextView) view.findViewById(R.id.qr210_dialog01_item01);
                        TextView dialogitem03 = (TextView) view.findViewById(R.id.qr210_dialog01_item03);
                        TextView dialogTV_dialog_matem = (TextView) view.findViewById(R.id.TV_dialog_matem);
                        String g_dialogitem01 = dialogitem01.getText().toString();
                        String g_dialogTV_dialog_matem = dialogTV_dialog_matem.getText().toString();
                        Double g_dialogitem03 = Double.valueOf(dialogitem03.getText().toString());

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(qr231.this);
                        builder1.setCancelable(false);
                        builder1.setTitle(getString(R.string.qr210_msg06));
                        builder1.setMessage(getString(R.string.qr210_msg07));
                        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String result = db.delscan(qr231_02, g_dialogitem01, g_dialogitem03, g_dialogTV_dialog_matem);
                                if (result.equals("TRUE")) {
                                    Cursor cursor = db.getdialogdetail(qr231_02);
                                    UpdateAdapterdialog(cursor);
                                }
                            }
                        });
                        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder1.show();
                    } catch (Exception e) {
                    }
                }
            });
            dialog.show();
        }
    };

    public String getdata(String dataStr) {
        try {
            URL url = new URL(dataStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String result = reader.readLine();
            reader.close();
            return result;
        } catch (Exception e) {
            return "FALSE";
        }
    }

    //取得批號
    private String getdatecode(String apiUrl) {
        try {
            HttpURLConnection conn = null;
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String jsonstring = reader.readLine();
            reader.close();
            if (!jsonstring.equals("FALSE")) {
                return jsonstring;
            } else {
                return "NULL";
            }
        } catch (Exception e) {
            return "NULL";
        }
    }

    public String upload() {
        try {
            URL url = new URL("http://172.16.40.20/" + g_server + "/PDA_qr231/upload.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            Cursor c = db.getall();
            Cursor c1 = db.getallb();
            JSONArray jarray = cur2Json(c);
            JSONArray jarray1 = cur2Json(c1);
            JSONObject jobejct = new JSONObject();
            jobejct.put("ID", ID);
            jobejct.put("TC_PTW001", head1.getText());
            jobejct.put("detail", jarray);
            jobejct.put("detail2", jarray1);
            writer.write(jobejct.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            reader.close();
            return result;
        } catch (Exception e) {
            return "FALSE";
        }
    }

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //資料新增
                case 0:
                    try {
                        db.append(ujsonobject);
                        Cursor getdetail = db.getdetail();
                        UpdateAdapter(getdetail);
                    } catch (Exception e) {

                    }
                    break;
                //資料清除
                case 1:
                    try {
                        db.close();
                        db.open();
                        head1.setText("");
                        Cursor getdetail = db.getdetail();
                        UpdateAdapter(getdetail);
                    } catch (Exception e) {

                    }
                    break;
                //掃描標籤
                case 2:
                    try {
                        Cursor getdetail = db.getdetail();
                        UpdateAdapter(getdetail);
                    } catch (Exception e) {

                    }
                    break;

            }
        }
    }

    public void UpdateAdapter(Cursor cursor) {
        try {
            if (cursor != null && cursor.getCount() >= 0) {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.activity_qr210_view01, cursor,
                        new String[]{"qr231_01", "qr231_02", "qr231_04", "qr231_05", "qr231_06", "qr231_03"},
                        new int[]{R.id.qr210_view01_item01, R.id.qr210_view01_item02, R.id.qr210_view01_item03, R.id.qr210_view01_item04, R.id.qr210_view01_item05, R.id.qr210_view01_item07}, 0);
                list01.setAdapter(adapter);
            }
        } catch (Exception e) {
            String x = e.toString();
        } finally {

        }
    }

    public void UpdateAdapterdialog(Cursor cursor) {
        try {
            if (cursor != null && cursor.getCount() >= 0) {
                /*SimpleCursorAdapter adapter=new SimpleCursorAdapter(qr231.this,R.layout.activity_qr210_dialog01_view,cursor,
                        new String[]{"rownum","qr231b_04","qr231b_05"},
                        new int[]{R.id.qr210_dialog01_item01,R.id.qr210_dialog01_item02,R.id.qr210_dialog01_item03},0);
                dialoglist01.setAdapter(adapter);*/

                barcodeListData.clear();
                cursor.moveToFirst();
                int k = cursor.getCount();
                for (int i2 = 1; i2 <= k; i2++) {
                    barcodeListData.add(
                            new Barcode_listData(Integer.parseInt(cursor.getString(1)),
                                    cursor.getString(4),
                                    Double.parseDouble(cursor.getString(5)),
                                    Boolean.parseBoolean(cursor.getString(6)),
                                    cursor.getString(2),
                                    cursor.getString(3),
                                    "0"));
                    cursor.moveToNext();
                }
                barcodeAdapter.notifyDataSetChanged();

            }
        } catch (Exception e) {

        } finally {

        }
    }

    //Cursor 轉 Json
    public JSONArray cur2Json(Cursor cursor) {
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        rowObject.put(cursor.getColumnName(i),
                                cursor.getString(i));
                    } catch (Exception e) {
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;

    }

    //發送mail
    private void qr231_mail(String apiurl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiurl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.connect();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String result = reader.readLine();
                    reader.close();
                } catch (Exception e) {
                }
            }
        }).start();
    }

}
