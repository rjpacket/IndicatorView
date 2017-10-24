package com.taovo.rjp.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.taovo.rjp.testapp.indicator.IndicatorView;

import java.util.LinkedList;

public class ViewPagerActivity extends Activity {

    private LinkedList<View> viewCache;
    private LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewCache = new LinkedList<>();
        layoutInflater = LayoutInflater.from(this);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view;
                if (viewCache.size() > 0) {
                    view = viewCache.remove();
                } else {
                    view = layoutInflater.inflate(R.layout.layout_view_pager, null);
                }
                TextView textView = (TextView)view.findViewById(R.id.text);
                textView.setText("这是第" + position + "页");
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                View piv = (View) object;
                container.removeView(piv);
                viewCache.add(piv);
            }
        });

        IndicatorView indicatorView = (IndicatorView) findViewById(R.id.indicator_view);
        indicatorView.setViewPager(viewPager);
    }
}
