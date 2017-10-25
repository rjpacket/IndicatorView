package com.taovo.rjp.testapp.qqBezier;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.taovo.rjp.testapp.R;

public class QQNumberActivity extends Activity {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qqnumber);
        mContext = this;
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 20;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_qq_number_list_view, null);
                }
                QQNumberView qqNumberView = (QQNumberView) convertView.findViewById(R.id.qq_number_view);

                return convertView;
            }
        });
    }
}
