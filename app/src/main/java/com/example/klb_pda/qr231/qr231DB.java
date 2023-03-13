package com.example.klb_pda.qr231;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

public class qr231DB {
    public SQLiteDatabase db = null;
    String DATABASE_NAME = "qr231DB.db";
    String TABLE_NAME = "qr231_table";
    String qr231_01 = "qr231_01"; //項次
    String qr231_02 = "qr231_02"; //料號
    String qr231_03 = "qr231_03"; //名稱
    String qr231_04 = "qr231_04"; //規格
    String qr231_05 = "qr231_05"; //申請量
    String qr231_06 = "qr231_06"; //已掃量

    String TABLE_NAME2 = "qr231b_table";
    String qr231b_01 = "qr231b_01"; //項次
    String qr231b_02 = "qr231b_02"; //QRcode
    String qr231b_03 = "qr231b_03"; //料號
    String qr231b_04 = "qr231b_04"; //批號
    String qr231b_05 = "qr231b_05"; //數量
    String qr231b_06 = "qr231b_06"; //驗收狀況

    String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + qr231_01 + " INTEGER," + qr231_02 + " TEXT," + qr231_03 + " TEXT," + qr231_04 + " TEXT," + qr231_05 + " INTEGER," + qr231_06 + " INTEGER," + " PRIMARY KEY(qr231_02))";
    String CREATE_TABLE2 = "CREATE TABLE " + TABLE_NAME2 + " (" + qr231b_01 + " INTEGER," + qr231b_02 + " TEXT," + qr231b_03 + " TEXT," + qr231b_04 + " INTEGER," + qr231b_05 + " INTEGER," + qr231b_06 + " TEXT)";
    private Context mCtx = null;

    public qr231DB(Context ctx) {
        this.mCtx = ctx;
    }

    public void open() throws SQLException {
        db = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
        try {
            db.execSQL(CREATE_TABLE);
            db.execSQL(CREATE_TABLE2);
        } catch (Exception e) {

        }
    }

    public void close() {
        try {
            String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
            String DROP_TABLE2 = "DROP TABLE IF EXISTS " + TABLE_NAME2;
            db.execSQL(DROP_TABLE);
            db.execSQL(DROP_TABLE2);
            db.close();
        } catch (Exception e) {

        }
    }

    public String append(JSONObject result) {
        try {
            JSONArray jarray1 = result.getJSONArray("detail1");
            JSONArray jarray2 = result.getJSONArray("detail2");
            for (int i = 0; i < jarray1.length(); i++) {
                JSONObject data = jarray1.getJSONObject(i);
                Integer xqr231_01 = i + 1;
                String xqr231_02 = data.getString("TC_PTW002");
                String xqr231_03 = data.getString("TA_IMA02_1");
                String xqr231_04 = data.getString("TA_IMA021_1");
                Integer xqr231_05 = data.getInt("TC_PTW003");
                Integer xqr231_06 = data.getInt("TC_PTW005");
                ContentValues args = new ContentValues();
                args.put(qr231_01, xqr231_01);
                args.put(qr231_02, xqr231_02);
                args.put(qr231_03, xqr231_03);
                args.put(qr231_04, xqr231_04);
                args.put(qr231_05, xqr231_05);
                args.put(qr231_06, xqr231_06);
                db.insert(TABLE_NAME, null, args);
            }
            for (int i = 0; i < jarray2.length(); i++) {
                JSONObject data = jarray2.getJSONObject(i);
                Integer xqr231b_01 = i + 1;
                String xqr231b_02 = data.getString("QR_PTB003");
                String xqr231b_03 = data.getString("QR_PTB004");
                String xqr231b_04 = data.getString("QR_PTB009");
                Integer xqr231b_05 = data.getInt("QR_PTB005");
                String xqr231b_06 = "true";
                ContentValues args = new ContentValues();
                args.put(qr231b_01, xqr231b_01);
                args.put(qr231b_02, xqr231b_02);
                args.put(qr231b_03, xqr231b_03);
                args.put(qr231b_04, xqr231b_04);
                args.put(qr231b_05, xqr231b_05);
                args.put(qr231b_06, xqr231b_06);
                db.insert(TABLE_NAME2, null, args);
            }
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }

    public String scan(String xqr231b_02, String xqr231b_03, String xqr231b_04, Double xqr231b_05) {
        try {
            //確認是否有此品號
            Cursor b = db.rawQuery("SELECT COUNT(qr231_02) count FROM " + TABLE_NAME + " WHERE qr231_02='" + xqr231b_03 + "'", null);
            b.moveToFirst();
            Integer bcount = b.getInt(0);
            b.close();
            if (bcount > 0) {
                //
                Cursor c = db.rawQuery("SELECT COUNT(qr231b_02) count FROM " + TABLE_NAME2 + " WHERE qr231b_02='" + xqr231b_02 + "'", null);
                c.moveToFirst();
                Integer tcount = c.getInt(0);
                c.close();
                if (tcount == 0) {
                    //檢查掃描數量是否超過
                    Cursor c1 = db.rawQuery("SELECT qr231_01,qr231_05,qr231_06 FROM " + TABLE_NAME + " WHERE qr231_02='" + xqr231b_03 + "' AND qr231_05>qr231_06", null);
                    if (c1.getCount() == 0) {
                        return "OVERQTY";
                    }
                    c1.moveToFirst();
                    Integer tqr231_01 = c1.getInt(0); //項次
                    Integer tqr231_05 = c1.getInt(1); //已掃數量
                    Integer tqr231_06 = c1.getInt(2); //已驗收量
                    if (tqr231_05 - tqr231_06 - xqr231b_05 >= 0) {
                        db.execSQL("UPDATE " + TABLE_NAME + " SET qr231_06=qr231_06+" + xqr231b_05 + " WHERE qr231_02='" + xqr231b_03 + "'");

                        String[] maxqr231b_01 = new String[]{"MAX(" + qr231b_01 + ") AS MAX"};
                        String selection = "qr231b_03 = ? ";
                        String[] selectionArgs = new String[]{xqr231b_03};

                        Cursor max = db.query(TABLE_NAME2, maxqr231b_01, selection, selectionArgs, null, null, null);
                        max.moveToFirst();
                        int g_max = 0;
                        if (max.isNull(0)) {
                            g_max = 1;
                        } else {
                            g_max = max.getInt(0) + 1;
                        }
                        db.execSQL("INSERT INTO " + TABLE_NAME2 + " (qr231b_01,qr231b_02,qr231b_03,qr231b_04,qr231b_05,qr231b_06) " +
                                "VALUES(" + g_max + ",'" + xqr231b_02 + "','" + xqr231b_03 + "','" + xqr231b_04 + "'," + xqr231b_05 + ",'true')");
                        return "TRUE";

                    } else {
                        return "OVERQTY";
                    }
                } else {
                    return "HASRECORD";
                }
            } else {
                return "NORECORD";
            }

        } catch (Exception e) {
            return "FALSE";
        }
    }

    //MVL , STT, Số lượng, QRcode
    public String delscan(String xqr231b_03, String xqr231b_01, Double xqr231b_05, String xqr231b_02) {
        try {
            Cursor c = db.rawQuery("SELECT count(*) cn FROM " + TABLE_NAME2 + " " +
                    " WHERE qr231b_03='" + xqr231b_03 + "' AND qr231b_01=" + xqr231b_01 + " AND qr231b_02 = '" + xqr231b_02 + "'", null);
            c.moveToFirst();
            if (c.getInt(0) > 0) {
                try {
                    db.execSQL("DELETE FROM " + TABLE_NAME2 + " WHERE qr231b_01=" + xqr231b_01 + " AND qr231b_03='" + xqr231b_03 + "' AND qr231b_02 = '" + xqr231b_02 + "'    ");
                    db.execSQL("UPDATE " + TABLE_NAME + " SET qr231_06=qr231_06-" + xqr231b_05 + " WHERE qr231_02='" + xqr231b_03 + "'");
                } catch (Exception e) {
                }
            }
            c.close();
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }

    public Cursor getall() {
        try {
            return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY qr231_01", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public Cursor getallb() {
        try {
            return db.rawQuery("SELECT * FROM " + TABLE_NAME2 + " ORDER BY qr231b_01 ASC,qr231b_04 ASC", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public Cursor getdetail() {
        try {
            return db.rawQuery("SELECT rowid _id,* FROM " + TABLE_NAME + " ORDER BY " + qr231_01 + " ASC", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }

    }

    public Cursor getdialogdetail(String xqr231b_02) {
        try {
            return db.rawQuery("SELECT rowid _id," +
                    " (SELECT count(*) FROM qr231b_table b WHERE a.rowid >=b.rowid AND b.qr231b_03='" + xqr231b_02 + "' ) AS rownum," +
                    " qr231b_01,qr231b_02,qr231b_04,qr231b_05,qr231b_06" +
                    " FROM " + TABLE_NAME2 + " a WHERE qr231b_03='" + xqr231b_02 + "' ORDER BY " + qr231b_01, null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public String updCheckBox(String dialogitem01, String qr231_02, String g_dialogitem03, boolean b) {
        try {
            if (b == true) {
                db.execSQL("UPDATE " + TABLE_NAME + " SET qr231_06=qr231_06+" + g_dialogitem03 + " WHERE qr231_02='" + qr231_02 + "'");
                db.execSQL("UPDATE " + TABLE_NAME2 + " SET qr231b_06= '" + b + "' WHERE qr231b_01 ='" + dialogitem01 + "'");
            } else {
                db.execSQL("UPDATE " + TABLE_NAME + " SET qr231_06=qr231_06-" + g_dialogitem03 + " WHERE qr231_02='" + qr231_02 + "'");
                db.execSQL("UPDATE " + TABLE_NAME2 + " SET qr231b_06= '" + b + "' WHERE qr231b_01 ='" + dialogitem01 + "'");
            }
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }

    public String updCheckBox(String qr231_02, boolean b) {
        try {
            if (b == true) {
                String[] sumqr231b_05 = new String[]{"sum(" + qr231b_05 + ")"};
                String selection = "qr231b_03 = ? ";
                String[] selectionArgs = new String[]{qr231_02};

                Cursor sum_cur = db.query(TABLE_NAME2, sumqr231b_05, selection, selectionArgs, null, null, null);
                sum_cur.moveToFirst();
                if (sum_cur.getCount() > 0) {
                    int g_sum_qr231b_05 = sum_cur.getInt(0);
                    db.execSQL("UPDATE " + TABLE_NAME + " SET qr231_06= " + g_sum_qr231b_05 + " WHERE qr231_02='" + qr231_02 + "'");
                }
            } else {
                db.execSQL("UPDATE " + TABLE_NAME + " SET qr231_06 = 0  WHERE qr231_02='" + qr231_02 + "'");
            }
            db.execSQL("UPDATE " + TABLE_NAME2 + " SET qr231b_06= '" + b + "' WHERE qr231b_03 ='" + qr231_02 + "'");
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }
}
