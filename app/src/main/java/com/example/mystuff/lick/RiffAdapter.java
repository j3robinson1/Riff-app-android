package com.example.mystuff.lick;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Service;


import java.util.ArrayList;

/**
 * Created by MyStuff on 9/22/15.
 */
public class RiffAdapter extends BaseAdapter {
    private ArrayList<Riffs> riffs;
    private LayoutInflater riffInf;

    public RiffAdapter(Context c, ArrayList<Riffs> theRiffs){
        riffs=theRiffs;
        riffInf=LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return riffs.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        LinearLayout riffLay = (LinearLayout)riffInf.inflate
                (R.layout.riff_layout, parent, false);
        //get title and artist views
        TextView riffView = (TextView)riffLay.findViewById(R.id.riff_title);
        TextView artistView = (TextView)riffLay.findViewById(R.id.riff_artist);
        //get song using position
        Riffs currRiff = riffs.get(position);
        //get title and artist strings
        riffView.setText(currRiff.getTitle());
        artistView.setText(currRiff.getArtist());
        //set position as tag
        riffLay.setTag(position);
        return riffLay;
    }

}