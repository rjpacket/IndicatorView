package com.taovo.rjp.testapp.ruler;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.taovo.rjp.testapp.R;

public class RulerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruler);

        final TextView tvNumber = (TextView) findViewById(R.id.tv_number);
        RulerView rulerView = (RulerView) findViewById(R.id.ruler_view);
        rulerView.setmListener(new RulerCallback() {
            @Override
            public void onRulerSelected(int length, int value) {
                tvNumber.setText(String.valueOf(value * 1.0 / 10));
            }
        });
        rulerView.setNumber(80);
    }
}
