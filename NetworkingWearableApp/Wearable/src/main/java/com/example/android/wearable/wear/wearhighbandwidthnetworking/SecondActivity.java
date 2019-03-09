package com.example.android.wearable.wear.wearhighbandwidthnetworking;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ImageView;

import com.example.android.wearable.wear.wearhighbandwidthnetworking.Adapter.SimpleRecyclerAdapter;

public class SecondActivity extends AppCompatActivity {

    ImageView btnFallDown,btnSlideFromBottom;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        btnFallDown = (ImageView)findViewById(R.id.btnFallDown);
        btnSlideFromBottom = (ImageView)findViewById(R.id.btnBottom);
        //btnSlideFromRight = (Button)findViewById(R.id.btnRight);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setLayoutManager(new GridLayoutManager(this,4));

        btnFallDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runAnimation(recyclerView,0);

            }
        });

        btnSlideFromBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runAnimation(recyclerView,1);
            }
        });

        //btnSlideFromRight.setOnClickListener(new View.OnClickListener() {
          //  @Override
           // public void onClick(View v) {
             //   runAnimation(recyclerView,3);}});
    }

    private void runAnimation(RecyclerView recyclerView, int type) {
        Context context = recyclerView.getContext();
        LayoutAnimationController controller = null;

        if (type == 0)
            controller = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_fall_down);
        else if (type==1)
            controller = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_slide_from_bottom);
        //else if (type==2)
           // controller = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_slide_from_right);
        //else if (type==3)
           // controller = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_slide_from_left);

        SimpleRecyclerAdapter adapter = new SimpleRecyclerAdapter();
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

}
