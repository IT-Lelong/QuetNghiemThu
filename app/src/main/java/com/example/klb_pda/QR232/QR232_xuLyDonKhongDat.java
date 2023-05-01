package com.example.klb_pda.QR232;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.klb_pda.Constant_Class;
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

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class QR232_xuLyDonKhongDat extends AppCompatActivity implements EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener {
    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    DecimalFormat decimalFormat;
    String ID, g_server, g_Factory, g_item, g_itemName, g_itemSpec, g_quantity, g_madon;

    TextView tv_MaDon, tv_MaXuong, tv_MaNV, tv_TenNV, tv_tenVatLieu, tv_QuyCach, tv_SoLo;
    EditText edt_soLuong, edt_ghichu, edt_MVL;
    ListView lv_xuly;
    Button btnok, btncancel;

    private QR232DB qr232DB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr232_xu_ly);

        AddControls();
        AddEvents();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void AddEvents() {

        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean aBoolean = qr232DB.sumTemp05();
                if (aBoolean && edt_soLuong.getText().toString().trim().length() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Xác nhận");
                    builder.setMessage("Bạn có chắc chắn muốn Nhận : " + edt_soLuong.getText().toString().trim());
                    builder.setPositiveButton(getApplicationContext().getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Xử lý khi người dùng chọn "Có"
                            String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                            qr232DB.updateTempToHistory(g_Factory, ID, formattedDate);
                            edt_soLuong.setText("");
                            setupData(null, edt_MVL.getText().toString().trim(), null, 0.0);
                            loadLVData();
                        }
                    });

                    builder.setNegativeButton(getApplicationContext().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Xử lý khi người dùng chọn "Không"
                            Toast.makeText(QR232_xuLyDonKhongDat.this, "FAIL", Toast.LENGTH_SHORT).show();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        edt_MVL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (edt_MVL.getRight() - edt_MVL.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (edt_MVL.getText().toString().trim().length() > 0) {
                            edt_soLuong.setText("");
                            tv_tenVatLieu.setText("");
                            tv_QuyCach.setText("");
                            setupData(null, edt_MVL.getText().toString().trim(), null, 0.0);
                            loadLVData();
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        edt_soLuong.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (edt_soLuong.getRight() - edt_soLuong.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (edt_soLuong.getText().toString().trim().length() > 0) {
                            qr232DB.phantichSL(edt_soLuong.getText().toString().trim().replace(",", ""));
                            loadLVData();
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        edt_soLuong.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Xử lý sự kiện trước khi văn bản trong EditText thay đổi
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Xử lý sự kiện khi văn bản trong EditText thay đổi
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Xử lý sự kiện sau khi văn bản trong EditText thay đổi
                if (s.length() > 0) {
                    qr232DB.phantichSL(edt_soLuong.getText().toString().trim().replace(",", ""));
                    loadLVData();
                } else {
                    qr232DB.phantichSL("0");
                    loadLVData();
                }
            }
        });
    }

    private void AddControls() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        qr232DB = new QR232DB(this);
        qr232DB.open();

        Bundle getbundle = getIntent().getExtras();
        ID = getbundle.getString("ID");
        g_Factory = getbundle.getString("Factory");
        g_item = getbundle.getString("Item");
        g_itemName = getbundle.getString("ItemName");
        g_itemSpec = getbundle.getString("ItemSpec");
        g_server = Constant_Class.server;

        String pattern = "#,###.##";
        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        decimalFormat.applyPattern(pattern);

        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            //statusTextView.setText("EMDKManager Request Failed");
            updateStatus("EMDKManager Request Failed");
        }

        tv_tenVatLieu = findViewById(R.id.tv_tenVatLieu);
        tv_QuyCach = findViewById(R.id.tv_QuyCach);
        edt_MVL = findViewById(R.id.edt_MVL);
        edt_soLuong = findViewById(R.id.edt_soLuong);
        lv_xuly = findViewById(R.id.lv_xuly);
        btnok = findViewById(R.id.btnok);
        btncancel = findViewById(R.id.btncancel);

        if (g_item != null) {
            edt_MVL.setText(g_item);
            tv_tenVatLieu.setText(g_itemName);
            tv_QuyCach.setText(g_itemSpec);
            qr232DB.setTempData(g_Factory, g_item);
            loadLVData();
        }
    }

    private void loadLVData() {
        Cursor cursor = qr232DB.getItemsData();
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,
                R.layout.activity_qr232_xu_ly_item_row, cursor,
                new String[]{"_id", "temp02", "temp03", "temp04", "slchuaxyly", "temp05", "temp01"},
                new int[]{R.id.tv_stt, R.id.tv_ngay, R.id.tv_soluong, R.id.tv_soluongdaxuly, R.id.tv_SLThieu, R.id.tv_soluonglannay, R.id.tv_MaDon},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        simpleCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.tv_stt) {
                    int rowNumber = cursor.getPosition() + 1;
                    ((TextView) view).setText(String.valueOf(rowNumber));
                    return true;
                }

                if (view.getId() == R.id.tv_soluong) {
                    ((TextView) view).setText(String.valueOf(decimalFormat.format(cursor.getDouble(columnIndex))));
                    return true;
                }

                if (view.getId() == R.id.tv_soluongdaxuly) {
                    ((TextView) view).setText(String.valueOf(decimalFormat.format(cursor.getDouble(columnIndex))));
                    return true;
                }

                if (view.getId() == R.id.tv_SLThieu) {
                    ((TextView) view).setText(String.valueOf(decimalFormat.format(cursor.getDouble(columnIndex))));
                    return true;
                }

                if (view.getId() == R.id.tv_soluonglannay) {
                    ((TextView) view).setText(String.valueOf(decimalFormat.format(cursor.getDouble(columnIndex))));
                    return true;
                }

                return false;
            }
        });

        lv_xuly.setAdapter(simpleCursorAdapter);
    }

    //取得料號資訊
    private String get_ItemData(String s) {
        try {
            HttpURLConnection conn = null;
            URL url = new URL(s);
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
                return "FALSE";
            }
        } catch (Exception e) {
            return "FALSE";
        }
    }

    private void getMVL(String datastr) {
        Thread getMVL_Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (datastr.startsWith("new")) {
                        //重印、拆單標籤格式 new_料號_批號_數量
                        String qr01 = "", qr02 = "";
                        Double qr03 = 0.0;

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

                        setupData(datastr, qr01, qr02, qr03);

                    } else if (datastr.substring(0, 5).equals("BC525") || datastr.substring(0, 5).equals("BC527") || datastr.substring(0, 5).equals("BB525") ||
                            datastr.substring(0, 5).equals("BB527")) {
                        Thread api = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //極版標籤 BC527-2101000198_1_07030333C_29568
                                String qr01 = "", qr02 = "";
                                Double qr03 = 0.0;

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
                                setupData(datastr, qr01, qr02, qr03);
                            }
                        });
                        api.start();
                    } else if (datastr.substring(0, 5).equals("CC510") || datastr.substring(0, 5).equals("CC512") || datastr.substring(0, 5).equals("CC513") ||
                            datastr.substring(0, 5).equals("CC514") || datastr.substring(0, 5).equals("CC515") || datastr.substring(0, 5).equals("CC518") ||
                            datastr.substring(0, 8).equals("OLDSTAMP")) {
                        Thread api = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //廣泰標籤 CC512-2010000697-108(CC5A2_KD91-1_NULL)_(03020489V_nap_WPX7A-BA)_20201026007002
                                //特殊標籤 CC514-2207000073-1492_CC514-2206000253-236(CC5A4_KD91-2_NULL)_(03010374D_vỏ bình ắc quy_WP8.5-12)_20220711059001
                                Double qr03 = 0.0;

                                int index1 = datastr.indexOf("-", 6);
                                int index2 = datastr.indexOf("("); //第一個(位置
                                int index3 = datastr.indexOf("(", index2 + 1); //第二個(位置
                                int index4 = datastr.indexOf("_", index3); //料號後的_位置
                                //取得料號
                                String qr01 = datastr.substring(index3 + 1, index4);
                                //取得批號
                                String qr02 = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + datastr + "&kind=" + 1);
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

                                setupData(datastr, qr01, qr02, qr03);
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

                        setupData(datastr, qr01, qr02, qr03);
                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg02));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Thread.currentThread().interrupt();
                            }
                        });
                        builder.show();
                    }

                } catch (Exception e) {
                }
            }
        });

        getMVL_Thread.start();// Khởi động luồng để thực hiện

    }

    private void setupData(String datastr, String qr01, String qr02, Double qr03) {
        Thread setupData_Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (qr01.length() > 0) {
                    String g_tenSP = "", g_quyCach = "";
                    String res_fab = get_ItemData("http://172.16.40.20/" + g_server + "/get_ima_file.php?IMA01=" + qr01);
                    if (!res_fab.equals("FALSE")) {
                        try {
                            JSONArray jsonarray = new JSONArray(res_fab);
                            if (jsonarray.length() > 0) {
                                for (int i = 0; i < jsonarray.length(); i++) {
                                    JSONObject jsonObject = jsonarray.getJSONObject(i);
                                    g_tenSP = jsonObject.getString("TA_IMA02_1"); //Tên tiếng việt
                                    g_quyCach = jsonObject.getString("TA_IMA021_1");  //Quy cách tiếng việt
                                }

                                String finalG_tenSP = g_tenSP;
                                String finalG_quyCach = g_quyCach;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        edt_MVL.setText(qr01);
                                        tv_tenVatLieu.setText(finalG_tenSP);
                                        tv_QuyCach.setText(finalG_quyCach);
                                        edt_soLuong.setText(String.valueOf(decimalFormat.format(qr03)));
                                        qr232DB.setTempData(g_Factory, qr01);
                                        qr232DB.phantichSL(edt_soLuong.getText().toString().trim().replace(",", ""));
                                        loadLVData();
                                    }
                                });

                                Thread.currentThread().interrupt();
                            } else {
                                // Tạo một luồng con và sử dụng Handler để hiển thị Toast thông qua luồng con đó
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        qr232DB.del_temp_table();
                                        loadLVData();
                                        Toast.makeText(getApplicationContext(), "Mã vật liệu không tồn tại", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Thread.currentThread().interrupt();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Tạo một luồng con và sử dụng Handler để hiển thị Toast thông qua luồng con đó
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                qr232DB.del_temp_table();
                                loadLVData();
                                Toast.makeText(getApplicationContext(), "Mã vật liệu không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        });

                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        setupData_Thread.start();
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
            getMVL(dataStr);
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
                //Update the status text view on UI thread with current scanner state
                //tv_scannerSTT.setText("" + status);
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
}