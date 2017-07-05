package com.jwg.grunert.ajgsensor;
// http://code.hootsuite.com/orientation-changes-on-android/

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class ViewSpeedFragment extends Fragment {
    Bitmap bitmap;
    Button buttonSpeedFile;
    Button buttonFitSpeed;
    TextView textViewSpeedFile;
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> gpxseries;
    GraphView graph;
    View view;
    RelativeLayout graphLayout;
    List<DataPoint> gpx_data_points;
    double time_min = -10, time_max = -10, speed_min = -10, speed_max = -10;
    double speed_m_per_s;

    public ViewSpeedFragment() {
        // Required empty public constructor
    }

    public boolean isPackageExisted(String targetPackage) {
        PackageManager pm = getActivity().getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    // http://stackoverflow.com/questions/691184/scanner-vs-stringtokenizer-vs-string-split
    public static ArrayList<String> splitBySingleChar(final char[] s,
                                                      final char splitChar) {
        final ArrayList<String> result = new ArrayList<String>();
        final int length = s.length;
        int offset = 0;
        int count = 0;
        for (int i = 0; i < length; i++) {
            if (s[i] == splitChar) {
                if (count > 0) {
                    result.add(new String(s, offset, count));
                }
                offset = i + 1;
                count = 0;
            } else {
                count++;
            }
        }
        if (count > 0) {
            result.add(new String(s, offset, count));
        }
        return result;
    }

    private class readFromFile extends AsyncTask<String, Integer, String > {
        File file;



        public readFromFile(File file) {
            this.file = file;
        }

        @Override
        protected String doInBackground(String... params) {

            String[] array;
            ArrayList<String> array_list;
            long timestamp = 0 ,offset = 0;
            double speed = 0, time = 0;
            Smooth smooth = new Smooth(10,10.0f);
            Kalman kalman = new Kalman(1f,1f,10.5f);
            gpx_data_points = new ArrayList<DataPoint>();
            time_min = -10;
            time_max = -10; speed_min = -10; speed_max = -10;
            try {
                publishProgress(2);
                Scanner scan = new Scanner(file);
                scan.useDelimiter("\\Z");
                String content = scan.next();
                content = content.replaceAll("<cmt>","<cmt> ");
                content = content.replaceAll("</cmt>"," </cmt>");
                if (scan != null) {
                    scan.close();
                }

                String lines[] = content.split(System.getProperty("line.separator"));

                int in = 0;

                publishProgress(in++);
                // start tracing to "/sdcard/ajgsensor_read_file.trace"
                //Debug.startMethodTracing("ajgsensor_parse_file");
                /*
                for (String line: lines) {
                    array = line.split("\\s+");
                    array_list = splitBySingleChar(line.toCharArray(), ' ');
                    if ( array.length == 9 && array[2].matches("Speed") ) {
                        publishProgress(in++);
                        timestamp = Long.parseLong(array[7]);
                        if (offset == 0 ) {
                            offset = timestamp;
                        }
                        time = ((timestamp - offset)/1000.0)/60.0;
                        speed = smooth.avg((float)(Double.parseDouble(array[3]) * 3.6));
                        if (time_min == -10) {
                            time_min = time;
                            time_max = time;
                            speed_min = speed;
                            speed_max = speed;
                        } else {
                            if ( time > time_max ) { time_max = time; }
                            if ( speed > speed_max) { speed_max = speed; }
                            if ( speed < speed_min ) { speed_min = speed; }
                        }
                        gpx_data_points.add(new DataPoint(time, speed));
                    }
                }
                */
                for (String line: lines) {
                    array_list = splitBySingleChar(line.toCharArray(), ' ');
                    if ( array_list.size() == 8 && array_list.get(1).matches("Speed") && (speed_m_per_s = Double.parseDouble(array_list.get(2))) > 1 ) {
                        // System.out.println("Jens: " + array_list.size() + " " + line);
                        publishProgress(in++);
                        timestamp = Long.parseLong(array_list.get(6));
                        if (offset == 0 ) {
                            offset = timestamp;
                        }
                        time = ((timestamp - offset)/1000.0)/60.0;
                        speed = smooth.avg((float) (speed_m_per_s * 3.6));
                        if (time_min == -10) {
                            time_min = time;
                            time_max = time;
                            speed_min = speed;
                            speed_max = speed;
                        } else {
                            if ( time > time_max ) { time_max = time; }
                            if ( speed > speed_max) { speed_max = speed; }
                            if ( speed < speed_min ) { speed_min = speed; }
                        }
                        gpx_data_points.add(new DataPoint(time, speed));
                    }
                }

                //Debug.stopMethodTracing();

                publishProgress(2);

                int gpx_data_points_size = gpx_data_points.size();

                DataPoint[] points = new DataPoint[gpx_data_points_size];

                for (int i = 0; i < gpx_data_points_size; i++) {
                    points[i] = new DataPoint(gpx_data_points.get(i).getX(),gpx_data_points.get(i).getY());
                }

                gpxseries = new LineGraphSeries<>(points);
                gpx_data_points.clear();

                publishProgress(3);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            int index = values[0];

            textViewSpeedFile.setText("Loading: " + file.getName() + " " + index);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textViewSpeedFile.setTextColor(Color.RED);
            textViewSpeedFile.setText("Loading: " + file.getName());
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textViewSpeedFile.setTextColor(Color.GREEN);
            textViewSpeedFile.setText("Finished: " + file.getName() + " " + speed_max);
            graph = (GraphView) view.findViewById(R.id.graph);
            graph.setDrawingCacheEnabled(true);

            graph.removeAllSeries();

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(time_min);
            graph.getViewport().setMaxX(time_max);

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(speed_min);
            graph.getViewport().setMaxY(speed_max);

            graph.getViewport().setScalable(true);
            graph.getViewport().setScalableY(true);

            GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
            gridLabel.setHorizontalAxisTitle("Minutes");
            gridLabel.setVerticalAxisTitle("km/h");

            // gpxseries.setThickness(10);
            gpxseries.setThickness(3);

            graph.addSeries(gpxseries);

            bitmap = Bitmap.createBitmap(graph.getDrawingCache());
            graph.setDrawingCacheEnabled(false);
            File f;
            f = new File(MainFragment.getAbsoluteFileName("plot.png"));
            try {
                FileOutputStream ostream = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.PNG, 10, ostream);
                ostream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_viewspeed, container, false);

        // getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        buttonSpeedFile = (Button) view.findViewById(R.id.buttonSpeedFile);
        buttonFitSpeed = (Button) view.findViewById(R.id.buttonFitSpeed);
        textViewSpeedFile = (TextView) view.findViewById(R.id.textViewSpeedFile);

        textViewSpeedFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = ((TextView) v);
                String string_from_text_view = textView.getText().toString();
                String[] string_array = string_from_text_view.split(" ");
                String string = string_array[1];
                Uri uri = Uri.fromFile(new File(MainFragment.getAbsoluteFileName(string)));

                if (isPackageExisted("com.vecturagames.android.app.gpxviewer")) {
                    /*
                    Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.vecturagames.android.app.gpxviewer");
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    launchIntent.setAction(Intent.ACTION_SEND);
                    launchIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    launchIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    launchIntent.setType("text/plain");
                    launchIntent.setData(uri);
                    Toast.makeText(getActivity().getApplicationContext(), "Sharing file: " + string, Toast.LENGTH_SHORT).show();
                    startActivity(launchIntent);
                    */
                    Intent email = new Intent(Intent.ACTION_SEND);
                    // Intent email = new Intent(getActivity().getPackageManager().getLaunchIntentForPackage("com.vecturagames.android.app.gpxviewer"),Intent.ACTION_SEND);
                    email.setPackage("com.vecturagames.android.app.gpxviewer");
                    email.setAction(Intent.ACTION_SEND);
                    email.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    email.putExtra(Intent.EXTRA_STREAM,uri);
                    email.setType("text/plain");
                    startActivity(email);

                }
            }
        });

        graph = (GraphView) view.findViewById(R.id.graph);
        graphLayout = (RelativeLayout) view.findViewById(R.id.graphLayout);
        series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        series.appendData(new DataPoint(5, 4), true, 1);
        series.appendData(new DataPoint(6, 0),true,1);
        graph.addSeries(series);
        //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        /*
        buttonSpeedFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FileChooser(getActivity()).setFileListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        String[] array;
                        String line = null;
                        long timestamp = 0 ,offset = 0;
                        double speed = 0, time = 0;
                        Smooth smooth = new Smooth(10,10.0f);
                        Kalman kalman = new Kalman(1f,1f,10.5f);
                        gpx_data_points = new ArrayList<DataPoint>();
                        time_min = -10; time_max = -10; speed_min = -10; speed_max = -10;

                        textViewSpeedFile.setText(file.getName());

                        try {
                            FileReader fileReader = new FileReader(file);
                            BufferedReader bufferedReader = new BufferedReader(fileReader);
                            while((line = bufferedReader.readLine()) != null) {
                                array = line.split("\\s+");
                                if ( array.length == 9 && array[2].matches("Speed") ) {
                                    timestamp = Long.parseLong(array[7]);
                                    if (offset == 0 ) {
                                        offset = timestamp;
                                    }
                                    time = (timestamp - offset)/1000.0;
                                    speed = Double.parseDouble(array[3]) * 3.6;
                                    if (time_min == -10) {
                                        time_min = time;
                                        time_max = time;
                                        speed_min = speed;
                                        speed_max = speed;
                                    } else {
                                        if ( time > time_max ) { time_max = time; }
                                        if ( speed > speed_max) { speed_max = speed; }
                                        if ( speed < speed_min ) { speed_min = speed; }
                                    }
                                    gpx_data_points.add(new DataPoint(time, smooth.avg((float)speed)));
                                }
                            }

                            Scanner scan = new Scanner(file);
                            scan.useDelimiter("\\Z");
                            String content = scan.next();
                            String lines[] = content.split(System.getProperty("line.separator"));

                            for (String linea: lines) {

                            }

                            DataPoint[] points = new DataPoint[gpx_data_points.size()];

                            for (int i = 0; i < gpx_data_points.size(); i++) {
                                points[i] = new DataPoint(gpx_data_points.get(i).getX(),gpx_data_points.get(i).getY());
                            }

                            gpxseries = new LineGraphSeries<>(points);
                            gpx_data_points.clear();

                            graph = (GraphView) view.findViewById(R.id.graph);
                            graph.removeAllSeries();

                            graph.getViewport().setXAxisBoundsManual(true);
                            graph.getViewport().setMinX(time_min);
                            graph.getViewport().setMaxX(time_max);

                            graph.getViewport().setYAxisBoundsManual(true);
                            graph.getViewport().setMinY(speed_min);
                            graph.getViewport().setMaxY(speed_max);

                            graph.getViewport().setScalable(true);
                            graph.getViewport().setScalableY(true);

                            gpxseries.setThickness(10);

                            graph.addSeries(gpxseries);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).showDialog();
            }
        });
        */

        /*
        buttonSpeedFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FileChooser(getActivity()).setFileListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        String[] array;
                        long timestamp = 0 ,offset = 0;
                        double speed = 0, time = 0;
                        Smooth smooth = new Smooth(10,10.0f);
                        Kalman kalman = new Kalman(1f,1f,10.5f);
                        gpx_data_points = new ArrayList<DataPoint>();
                        time_min = -10; time_max = -10; speed_min = -10; speed_max = -10;

                        textViewSpeedFile.setText(file.getName());

                        try {
                            Scanner scan = new Scanner(file);
                            scan.useDelimiter("\\Z");
                            String content = scan.next();
                            if (scan != null) {
                                scan.close();
                            }

                            String lines[] = content.split(System.getProperty("line.separator"));

                            for (String line: lines) {
                                array = line.split("\\s+");
                                if ( array.length == 9 && array[2].matches("Speed") ) {
                                    timestamp = Long.parseLong(array[7]);
                                    if (offset == 0 ) {
                                        offset = timestamp;
                                    }
                                    time = (timestamp - offset)/1000.0;
                                    speed = Double.parseDouble(array[3]) * 3.6;
                                    if (time_min == -10) {
                                        time_min = time;
                                        time_max = time;
                                        speed_min = speed;
                                        speed_max = speed;
                                    } else {
                                        if ( time > time_max ) { time_max = time; }
                                        if ( speed > speed_max) { speed_max = speed; }
                                        if ( speed < speed_min ) { speed_min = speed; }
                                    }
                                    gpx_data_points.add(new DataPoint(time, smooth.avg((float)speed)));
                                }
                            }

                            DataPoint[] points = new DataPoint[gpx_data_points.size()];

                            for (int i = 0; i < gpx_data_points.size(); i++) {
                                points[i] = new DataPoint(gpx_data_points.get(i).getX(),gpx_data_points.get(i).getY());
                            }

                            gpxseries = new LineGraphSeries<>(points);
                            gpx_data_points.clear();

                            graph = (GraphView) view.findViewById(R.id.graph);
                            graph.removeAllSeries();

                            graph.getViewport().setXAxisBoundsManual(true);
                            graph.getViewport().setMinX(time_min);
                            graph.getViewport().setMaxX(time_max);

                            graph.getViewport().setYAxisBoundsManual(true);
                            graph.getViewport().setMinY(speed_min);
                            graph.getViewport().setMaxY(speed_max);

                            graph.getViewport().setScalable(true);
                            graph.getViewport().setScalableY(true);

                            gpxseries.setThickness(10);

                            graph.addSeries(gpxseries);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).showDialog();
            }
        });

        */

        buttonSpeedFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FileChooser(getActivity(),MainFragment.directory_name,".*location.*").setFileListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        new readFromFile(file).execute();
                    }
                }).showDialog();
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //do something
                }
                return true;
            }
        });

        buttonFitSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int backgroundPreviousColor = graphLayout.getDrawingCacheBackgroundColor();
                graphLayout.setDrawingCacheEnabled(true);
                graphLayout.setDrawingCacheBackgroundColor(0xffffffff);

                graph.removeAllSeries();

                // Paint paint = new Paint();
                // paint.setStyle(Paint.Style.FILL_AND_STROKE);

                graph.getViewport().setXAxisBoundsManual(true);
                graph.getViewport().setMinX(time_min);
                graph.getViewport().setMaxX(time_max);

                graph.getViewport().setYAxisBoundsManual(true);
                graph.getViewport().setMinY(speed_min);
                graph.getViewport().setMaxY(speed_max);

                graph.getViewport().setScalable(true);
                graph.getViewport().setScalableY(true);

                gpxseries.setThickness(2);
                //gpxseries.setDrawDataPoints(true);
                //gpxseries.setDataPointsRadius(10);

                //gpxseries.setCustomPaint(paint);

                // gpxseries.setDrawBackground(true);
                // gpxseries.setBackgroundColor(10);

                graph.addSeries(gpxseries);
                bitmap = Bitmap.createScaledBitmap(graphLayout.getDrawingCache(),1920,1200,false);
                graphLayout.setDrawingCacheBackgroundColor(backgroundPreviousColor);
                graphLayout.setDrawingCacheEnabled(false);
                File f;
                f = new File(MainFragment.getAbsoluteFileName("plot.png"));
                try {
                    FileOutputStream ostream = new FileOutputStream(f);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 5, ostream);
                    ostream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });




        return view;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            MainActivity.dim_screen(false, getActivity());
            // getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else if (isResumed()) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}