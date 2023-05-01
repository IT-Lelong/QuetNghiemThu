package com.example.klb_pda.QR232;

import java.io.Serializable;

public class QR232_TraCuu_Dialog_Model implements Serializable {
    private String g_qr_imp005;
    private String g_qr_imp007;
    private String g_qr_imp004;
    private String g_qr_imp006;

    public String getG_qr_imp005() {
        return g_qr_imp005;
    }

    public void setG_qr_imp005(String g_qr_imp005) {
        this.g_qr_imp005 = g_qr_imp005;
    }

    public String getG_qr_imp007() {
        return g_qr_imp007;
    }

    public void setG_qr_imp007(String g_qr_imp007) {
        this.g_qr_imp007 = g_qr_imp007;
    }

    public String getG_qr_imp004() {
        return g_qr_imp004;
    }

    public void setG_qr_imp004(String g_qr_imp004) {
        this.g_qr_imp004 = g_qr_imp004;
    }

    public String getG_qr_imp006() {
        return g_qr_imp006;
    }

    public void setG_qr_imp006(String g_qr_imp006) {
        this.g_qr_imp006 = g_qr_imp006;
    }

    public String getG_ta_cpf001() {
        return g_ta_cpf001;
    }

    public void setG_ta_cpf001(String g_ta_cpf001) {
        this.g_ta_cpf001 = g_ta_cpf001;
    }

    private String g_ta_cpf001;

    public QR232_TraCuu_Dialog_Model(String g_qr_imp005, String g_qr_imp007, String g_qr_imp004, String g_qr_imp006, String g_ta_cpf001) {
        this.g_qr_imp005 = g_qr_imp005;
        this.g_qr_imp007 = g_qr_imp007;
        this.g_qr_imp004 = g_qr_imp004;
        this.g_qr_imp006 = g_qr_imp006;
        this.g_ta_cpf001 = g_ta_cpf001;
    }
}
