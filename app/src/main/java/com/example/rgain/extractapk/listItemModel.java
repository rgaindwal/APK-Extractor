package com.example.rgain.extractapk;

import android.graphics.drawable.Drawable;
import androidx.cardview.widget.CardView;
import android.widget.LinearLayout;

/**
 * Created by rgain on 2/12/2018.
 */

public class listItemModel {
    CardView cardView;

    public listItemModel(CardView cardView, String appname, String pname, String versionName, int versionCode, Drawable icon, String filePath) {
        this.cardView = cardView;
        this.appname = appname;
        this.pname = pname;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.icon = icon;
        this.filePath = filePath;
    }

    public CardView getCardView() {

        return cardView;
    }

    public listItemModel(String appname, String pname, String versionName, int versionCode, Drawable icon, String filePath) {
        this.appname = appname;
        this.pname = pname;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.icon = icon;
        this.filePath = filePath;
    }

    public void setCardView(CardView cardView) {
        this.cardView = cardView;
    }

    String appname = "";
    String pname = "";
    String versionName = "";
    int versionCode = 0;
    Drawable icon;
    String filePath;


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }


}
