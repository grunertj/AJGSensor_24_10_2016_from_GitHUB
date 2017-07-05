package com.jwg.grunert.ajgsensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;

public class MainActivity extends AppCompatActivity {
    String global_fragments[];
    MainFragment mainFragment=null;
    SettingsFragment settingsFragment=null;
    DebugFragment debugFragment=null;
    TabLayout tabLayout;
    ViewPager viewPager;
    static final int TYPE_GPS = 0;

    static final int OVERRIDE = 1;
    static final int APPEND = 2;
    static final int NEW = 3;

    static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

    static boolean COMPRESS = true;
    static boolean FILTERED = false;

    static boolean dim_state = true;

    static int file_mode = NEW;

    static boolean[] log;

    static int curBrightnessValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        log = new boolean[100];

        log[TYPE_GPS] = false;
        log[Sensor.TYPE_ACCELEROMETER] = false;
        log[Sensor.TYPE_LINEAR_ACCELERATION] = false;
        log[Sensor.TYPE_STEP_DETECTOR] = false;
        log[Sensor.TYPE_STEP_COUNTER] = false;
        log[Sensor.TYPE_GYROSCOPE] = false;
        log[Sensor.TYPE_GYROSCOPE_UNCALIBRATED] = false;
        log[Sensor.TYPE_ROTATION_VECTOR] = false;

        if (mainFragment == null) {
            mainFragment = new MainFragment();
        }

        if (mainFragment == null) {
            mainFragment = new MainFragment();
            mainFragment.setRetainInstance(true);
        }

        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
            settingsFragment.setRetainInstance(true);
        }

        if (debugFragment == null) {
            debugFragment = new DebugFragment();
            debugFragment.setRetainInstance(true);
        }

        global_fragments = new String[3];
        global_fragments[0] = "Main";
        global_fragments[1] = "Settings";
        global_fragments[2] = "Debug";

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new CustomAdapter(getSupportFragmentManager(), getApplicationContext()));
        viewPager.setOffscreenPageLimit(100);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });
    }

    private class CustomAdapter extends FragmentPagerAdapter {
        private String fragments [] = global_fragments;

        public CustomAdapter(FragmentManager fm) {
            super(fm);
        }

        public CustomAdapter(FragmentManager fm, Context applicationContext) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mainFragment;
                case 1:
                    return settingsFragment;
                case 2:
                    return debugFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            curBrightnessValue = android.provider.Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    static public boolean dim_screen (boolean on, Context context) {
        if (on == true) {
            try {
                try {
                    curBrightnessValue = android.provider.Settings.System.getInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
            } catch (Exception e) {}
            return false;
        } else {
            try {
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                if (curBrightnessValue > 64) {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, curBrightnessValue);
                } else {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 64);
                }
            } catch (Exception e) {}
            return  true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dim_screen(false,this);
    }
}
