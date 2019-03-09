package com.example.android.wearable.wear.wearhighbandwidthnetworking;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.android.wearable.wear.wearhighbandwidthnetworking.Adapter.DataModel;
import com.example.android.wearable.wear.wearhighbandwidthnetworking.Adapter.DatabaseHelper;
import com.example.android.wearable.wear.wearhighbandwidthnetworking.Adapter.RecycleAdapter;

import java.util.ArrayList;
import java.util.List;

public class Viewall extends Activity {

    Button show;
    DatabaseHelper database;
    RecyclerView recyclerView;
    RecycleAdapter recycler;
    List<DataModel> datamodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.view );
        show = (Button) findViewById( R.id.view );
        datamodel = new ArrayList<DataModel>();
        recyclerView = (RecyclerView) findViewById( R.id.recycle );


        show.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = new DatabaseHelper( Viewall.this );
                datamodel = database.getdata();
                recycler = new RecycleAdapter( datamodel );


                Log.i( "View content", "" + datamodel );
                RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager( getApplicationContext() );
                recyclerView.setLayoutManager( reLayoutManager );
                recyclerView.setItemAnimator( new DefaultItemAnimator() );
                recyclerView.setAdapter( recycler );


            }
        } );

    }
}