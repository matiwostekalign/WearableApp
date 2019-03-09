package com.example.android.wearable.wear.wearhighbandwidthnetworking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.wearable.wear.wearhighbandwidthnetworking.Adapter.DatabaseHelper;

public class MyRecyclerActivity extends AppCompatActivity {



    Button submit,show;
    DatabaseHelper databaseHelper;

    EditText etname,etemail,etcompany,etnumber;
    String name,city,company,country ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recycler);
        etname = (EditText) findViewById(R.id.etname);
        etemail = (EditText) findViewById(R.id.etemail);
        etcompany = (EditText) findViewById(R.id.etcompany);
        etnumber = (EditText) findViewById(R.id.etphone);
        submit = (Button) findViewById(R.id.submit);


        submit = (Button) findViewById(R.id.submit);
        show= (Button) findViewById(R.id.show);

        databaseHelper = new DatabaseHelper(this);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = etname.getText().toString();
                city = etemail.getText().toString();
                company=etcompany.getText().toString();
                country = etnumber.getText().toString();
                //Toast.makeText(MainActivity.this,name, Toast.LENGTH_SHORT).show();
                if (name.isEmpty() && city.isEmpty()&& company.isEmpty()&& country.isEmpty()){

                    Toast.makeText(MyRecyclerActivity.this, "please fill details", Toast.LENGTH_SHORT).show();
                }else {

                    databaseHelper.insertdata(name,company,city,country);
                    etname.setText("");
                    etemail.setText("");
                    etcompany.setText("");
                    etnumber.setText("");
                    Intent intent = new Intent(MyRecyclerActivity.this,Viewall.class);
                    startActivity(intent);

                }
            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyRecyclerActivity.this,Viewall.class);
                startActivity(intent);
            }
        });
    }


}
