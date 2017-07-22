package com.donateblood.blooddonation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class HowToUse  extends AppCompatActivity {

    @InjectView(R.id.howtouse) TextView howtouse;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.howtouse);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ButterKnife.inject(this);
        String Howto = getResources().getString(R.string.howtouse);
        howtouse.setText(Howto);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
