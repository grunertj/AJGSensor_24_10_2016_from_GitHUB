import java.io.*;
import java.util.zip.*;

// http://www.xyzws.com/javafaq/how-to-make-a-gzip-file-in-java/215
// Usage: java SmoothFileKalman 2017_04_17_14_54_57
public class SmoothFileKalman {
    public static void main(String[] args) {
	String sensor_fname;
	String gpx_fname;

	if (args.length > 0) {
	  sensor_fname = "sensor-" + args[0] + ".txt.gz";
	  gpx_fname = "location-" + args[0] + ".gpx";
	} else {
	   sensor_fname = "sensor-2017_04_17_14_54_57.txt.gz";
	   gpx_fname = "location-2017_04_17_14_54_57.gpx";
	}

	// process_speed("location-2017_04_07_17_29_36.gpx","speed.dat",process_sensor(sensor_fname,"strokes.dat"));
	// process_sensor(sensor_fname,"strokes.dat",1000000000.0f);
	process_speed(gpx_fname,"speed.dat");
	process_sensor(sensor_fname,"strokes.dat");
	//process_sensor_old_sensor_format(sensor_fname,"strokes.dat");
    }

    static void process_speed(String gpx_input, String gpx_output) {
        BufferedWriter speeds = null;
        long loc_timestamp,timestamp,offset = 0;
	float time,speed = 0.0f;
        Smooth smooth = new Smooth(10,10.0f);
	Kalman kalman = new Kalman(1f,1f,10.5f);

	String[] array;

	String line = null;

	try {
          FileReader fileReader = new FileReader(gpx_input);
          BufferedReader bufferedReader = new BufferedReader(fileReader);
          speeds = new BufferedWriter(new FileWriter(gpx_output));

          while((line = bufferedReader.readLine()) != null) {
	    line = line.replaceAll("<cmt>","<cmt> ");
	    line = line.replaceAll("</cmt>"," </cmt>");
            array = line.split("\\s+");

	    if ( array.length == 9 && array[2].matches("Speed") ) {
	      timestamp = Long.parseLong(array[7]);
	      loc_timestamp = Long.parseLong(array[5]);
              speed = Float.parseFloat(array[3]) * 3.6f;

              if (offset == 0 ) {
                offset = timestamp;
              }

              time = (float)(timestamp - offset)/1000.0f;

	      speeds.append(String.format("%.6f %.6f %.6f %.6f %d %d\n",time,speed,smooth.avg(speed),kalman.filter(speed),loc_timestamp,timestamp));

              // System.out.println( "Time: " + time + " " + "Speed: " + speed);                  
	    }
	  }

  	  bufferedReader.close();
  	  speeds.close();
	} catch(FileNotFoundException ex) {
           System.out.println( "Unable to open file '" + gpx_input + "'");                
        } catch(IOException ex) {
           System.out.println( "Error reading file '" + gpx_input + "'");                  
        }
    }

    static void process_speed(String gpx_input, String gpx_output, long offset) {
        BufferedWriter speeds = null;
        long loc_timestamp,timestamp;
	float time,speed = 0.0f;
        Smooth smooth = new Smooth(10,10.0f);
	Kalman kalman = new Kalman(1f,1f,10.5f);

	String[] array;

	String line = null;

	try {
          FileReader fileReader = new FileReader(gpx_input);
          BufferedReader bufferedReader = new BufferedReader(fileReader);
          speeds = new BufferedWriter(new FileWriter(gpx_output));

          while((line = bufferedReader.readLine()) != null) {
	    line = line.replaceAll("<cmt>","<cmt> ");
	    line = line.replaceAll("</cmt>"," </cmt>");
            array = line.split("\\s+");

	    if ( array.length == 9 && array[2].matches("Speed") ) {
	      timestamp = Long.parseLong(array[7]);
	      loc_timestamp = Long.parseLong(array[5]);
              speed = Float.parseFloat(array[3]) * 3.6f;

              time = (float)(timestamp - offset)/1000.0f;

	      speeds.append(String.format("%.6f %.6f %.6f %.6f %d %d\n",time,speed,smooth.avg(speed),kalman.filter(speed),loc_timestamp,timestamp));

              // System.out.println( "Time: " + time + " " + "Speed: " + speed);                  
	    }
	  }

  	  bufferedReader.close();
  	  speeds.close();
	} catch(FileNotFoundException ex) {
           System.out.println( "Unable to open file '" + gpx_input + "'");                
        } catch(IOException ex) {
           System.out.println( "Error reading file '" + gpx_input + "'");                  
        }
    }

    static long process_sensor(String sensor_input, String sensor_output, float multiple) {
        BufferedWriter strokes = null;
        BufferedWriter accelerometer = null;

        float time,x,sxa,xs,ys,zs,cpm=0.0f;
	Kalman kalman = new Kalman(0.2f,100f,70.5f);
        Smooth smooth_x = new Smooth(200,70.0f);
        long timestamp,offset = 0;

	Stroke stroke = new Stroke();
	Biquad lpFilterLP = new Biquad();
	Biquad lpFilterHP = new Biquad();
        Smooth smooth_a = new Smooth(20);

        lpFilterLP.setBiquad(Biquad.bq_type_lowpass,1.4/100.0,0.707,0.0);
	lpFilterHP.setBiquad(Biquad.bq_type_highpass,0.1/100.0,0.707,0.0);

	String[] array;

	String line = null;

	try {
          FileInputStream fin = new FileInputStream(sensor_input);
          GZIPInputStream gzis = new GZIPInputStream(fin);
          InputStreamReader xover = new InputStreamReader(gzis);
          BufferedReader bufferedReader = new BufferedReader(xover);

          //FileReader fileReader = new FileReader(sensor_input);
          //BufferedReader bufferedReader = new BufferedReader(fileReader);
          strokes = new BufferedWriter(new FileWriter(sensor_output));
          accelerometer = new BufferedWriter(new FileWriter("accelerometer.dat"));
          // bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream("liner_acceleration_smooth.dat.gz"))));
  
          while((line = bufferedReader.readLine()) != null) {
            array = line.split("\\s+");
	    if ( array[1].matches("1") ) {
	      timestamp = Long.parseLong(array[0]);
              if (offset == 0 ) {
                offset = timestamp;
              }

              time = (float)(timestamp - offset)/multiple;
              // System.out.println( "time: " + time);                  

              xs = Float.parseFloat(array[2]);
              ys = Float.parseFloat(array[3]);
              zs = Float.parseFloat(array[4]);

	      x = (float)Math.sqrt(xs*xs+ys*ys+zs*zs);

	    } else {
              continue;
	    }

	    sxa = stroke.strokes(time,(float)lpFilterHP.process((float)lpFilterLP.process(x)));
            accelerometer.append(String.format("%.6f %.6f %.6f %.6f %6f %.6f\n",time,xs,ys,zs,x,smooth_a.avg((float)x)));
	    
	    if ( sxa > 0.0f ) {
	      cpm = 60.0f / (sxa * 2.0f);
	      if ( cpm > 50.0f && cpm < 90.f ) {
	        strokes.append(String.format("%.6f %.6f %.6f %.6f %d\n",time,cpm,smooth_x.avg(cpm),kalman.filter(cpm),timestamp));
	      }
	    }
  	  }

  	  bufferedReader.close();
  	  strokes.close();
  	  accelerometer.close();
	} catch(FileNotFoundException ex) {
           System.out.println( "Unable to open file '" + sensor_input + "'");                
        } catch(IOException ex) {
           System.out.println( "Error reading file '" + sensor_input + "'");                  
        }
        return offset;
    }

    static long process_sensor_old_sensor_format(String sensor_input, String sensor_output) {
        BufferedWriter strokes = null;
        BufferedWriter accelerometer = null;
        float time,td = 0.0f,ts = 0.0f,x,sxa,xs,ys,zs,cpm=0.0f,xfilter,sxb;
	Kalman kalman = new Kalman(0.2f,100f,70.5f);
        Smooth smooth_x = new Smooth(100,70.0f);
        Smooth smooth_xf = new Smooth(200);
        long timestamp,offset = 0;
	int v = 0, vprev = 0;
	boolean is_max = false, is_min = false;
	float tf = 0.0f;
	float maxv = 0.0f;

	Stroke stroke = new Stroke();
	Stroke strokem = new Stroke();
	Biquad lpFilterLP1 = new Biquad();
	Biquad lpFilterLP = new Biquad();
	Biquad lpFilterHP = new Biquad();
        Smooth smooth_a = new Smooth(200);
        Smooth smooth_b = new Smooth(200);
	Smooth smooth_min = new Smooth(10);
	Smooth smooth_max = new Smooth(10);

        lpFilterLP.setBiquad(Biquad.bq_type_lowpass,0.4/100.0,0.707,0.0);
        lpFilterLP1.setBiquad(Biquad.bq_type_lowpass,0.1/100.0,0.707,0.0);
	lpFilterHP.setBiquad(Biquad.bq_type_highpass,0.1/100.0,0.707,0.0);

	String[] array;

	String line = null;

	try {
          FileInputStream fin = new FileInputStream(sensor_input);
          GZIPInputStream gzis = new GZIPInputStream(fin);
          InputStreamReader xover = new InputStreamReader(gzis);
          BufferedReader bufferedReader = new BufferedReader(xover);

          //FileReader fileReader = new FileReader(sensor_input);
          //BufferedReader bufferedReader = new BufferedReader(fileReader);
          strokes = new BufferedWriter(new FileWriter(sensor_output));
          accelerometer = new BufferedWriter(new FileWriter("accelerometer.dat"));
          // bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream("liner_acceleration_smooth.dat.gz"))));
  
          while((line = bufferedReader.readLine()) != null) {
            array = line.split("\\s+");
	    if ( array[1].matches("1") ) {
	      timestamp = Long.parseLong(array[0]);
              if (offset == 0 ) {
                offset = timestamp;
              }

              time = (float)(timestamp - offset)/1000000000.0f;
              // System.out.println( "time: " + time);                  

              xs = Float.parseFloat(array[2]);
              ys = Float.parseFloat(array[3]);
              zs = Float.parseFloat(array[4]);

	      x = (float)Math.sqrt(xs*xs+ys*ys+zs*zs);
	      xfilter = (float)lpFilterLP.process((float)lpFilterHP.process(x));
	      //xfilter = (float)lpFilterLP1.process((float)lpFilterLP.process((float)lpFilterHP.process(smooth_b.avg(x))));
  	      // sxa = stroke.strokes(time,xfilter);
	      

	      if ( x > 0.5 ) {
		v = 1;
	      } else {
		v = 0;
	      }

	      if ( vprev == 0 && v == 1 ) {
		ts = time;
	      } else if ( vprev == 1 && v == 0 ) {
		td = time - ts;
	      }

	      vprev = v;

	      /*
              if (smooth_max.is_max(xfilter) == true ) {
		tf = 0.1f;
	      } else {
		tf = 0f;
	      }
	      */
	      maxv = smooth_max.max(xfilter);
	      sxa = strokem.strokes_by_max(time,maxv);

              accelerometer.append(String.format("%.6f %.6f %.6f %.6f %.6f %6f %.6f %.6f %d %.6f %6f\n",time,xs,ys,zs,x,xfilter,smooth_a.avg((float)zs),(float)lpFilterLP.process((float)lpFilterHP.process(zs)),v,td,maxv));
              // accelerometer.append(String.format("%.6f %.6f %.6f %.6f %.6f\n",time,xs,ys,zs,x));
  	    
  	      if ( sxa > 0.0f ) {
		// System.out.println( "time: " + sxa);
  	        cpm = 60.0f / (sxa * 1.0f);
  	        if ( cpm > 50.0f && cpm < 90.f ) {
  	          strokes.append(String.format("%.6f %.6f %.6f %.6f %d\n",time,cpm,smooth_x.avg(cpm),kalman.filter(cpm),timestamp));
  	        }
  	      }
	    } else {
              continue;
	    }

  	  }

  	  bufferedReader.close();
  	  strokes.close();
  	  accelerometer.close();
	} catch(FileNotFoundException ex) {
           System.out.println( "Unable to open file '" + sensor_input + "'");                
        } catch(IOException ex) {
           System.out.println( "Error reading file '" + sensor_input + "'");                  
        }
        return offset;
    }

    static float lowPass( float input, float output ) {
      float ALPHA = 0.10f;

      if ( output == 0 ) return input;
     
      output = output + ALPHA * (input - output);
      return output;
    }

    static long process_sensor(String sensor_input, String sensor_output) {
        BufferedWriter strokes = null;
        BufferedWriter accelerometer = null;
        float time,td = 0.0f,ts = 0.0f,x,sxa,xs=0f,ys=0f,zs=0f,cpm=0.0f,xfilter,sxb;
	Kalman kalman = new Kalman(0.2f,100f,70.5f);
        Smooth smooth_x = new Smooth(100,70.0f);
        Smooth smooth_xf = new Smooth(200);
        long timestamp,offset = 0;
	int v = 0, vprev = 0;
	boolean is_max = false, is_min = false;
	float tf = 0.0f;
	float maxv = 0.0f;

	Stroke stroke = new Stroke();
	Stroke strokem = new Stroke();
	Biquad lpFilterLP1 = new Biquad();
	Biquad lpFilterLP = new Biquad();
	Biquad lpFilterHP = new Biquad();
        Smooth smooth_a = new Smooth(200);
        Smooth smooth_b = new Smooth(200);
	Smooth smooth_min = new Smooth(10);
	Smooth smooth_max = new Smooth(10);

        lpFilterLP.setBiquad(Biquad.bq_type_lowpass,0.4/100.0,0.707,0.0);
        lpFilterLP1.setBiquad(Biquad.bq_type_lowpass,0.1/100.0,0.707,0.0);
	lpFilterHP.setBiquad(Biquad.bq_type_highpass,0.1/100.0,0.707,0.0);

	String[] array;

	String line = null;

	try {
          FileInputStream fin = new FileInputStream(sensor_input);
          GZIPInputStream gzis = new GZIPInputStream(fin);
          InputStreamReader xover = new InputStreamReader(gzis);
          BufferedReader bufferedReader = new BufferedReader(xover);

          //FileReader fileReader = new FileReader(sensor_input);
          //BufferedReader bufferedReader = new BufferedReader(fileReader);
          strokes = new BufferedWriter(new FileWriter(sensor_output));
          accelerometer = new BufferedWriter(new FileWriter("accelerometer.dat"));
          // bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream("liner_acceleration_smooth.dat.gz"))));
  
          while((line = bufferedReader.readLine()) != null) {
            array = line.split("\\s+");
	    if ( array[2].matches("1") ) {
	      timestamp = Long.parseLong(array[1]);
              if (offset == 0 ) {
                offset = timestamp;
              }

              time = (float)(timestamp - offset)/1000.0f;
              // System.out.println( "time: " + time);                  

              // xs = Float.parseFloat(array[3]);
	      xs = lowPass(Float.parseFloat(array[3]),xs);
              // ys = Float.parseFloat(array[4]);
	      ys = lowPass(Float.parseFloat(array[4]),ys);
              // zs = Float.parseFloat(array[5]);
	      zs = lowPass(Float.parseFloat(array[5]),zs);

	      x = (float)Math.sqrt(xs*xs+ys*ys+zs*zs);
	       xfilter = (float)lpFilterLP.process((float)lpFilterHP.process(x));
	      //xfilter = (float)lpFilterLP1.process((float)lpFilterLP.process((float)lpFilterHP.process(smooth_b.avg(x))));

	      if ( x > 0.5 ) {
		v = 1;
	      } else {
		v = 0;
	      }

	      if ( vprev == 0 && v == 1 ) {
		ts = time;
	      } else if ( vprev == 1 && v == 0 ) {
		td = time - ts;
	      }

	      vprev = v;

	      /*
              if (smooth_max.is_max(xfilter) == true ) {
		tf = 0.1f;
	      } else {
		tf = 0f;
	      }
	      */
	      maxv = smooth_max.max(xfilter);
	      sxa = strokem.strokes_by_max(time,maxv);

              accelerometer.append(String.format("%.6f %.6f %.6f %.6f %.6f %6f %.6f %.6f %d %.6f %6f\n",time,xs,ys,zs,x,xfilter,smooth_a.avg((float)zs),(float)lpFilterLP.process((float)lpFilterHP.process(zs)),v,td,maxv));
              // accelerometer.append(String.format("%.6f %.6f %.6f %.6f %.6f\n",time,xs,ys,zs,x));
  	    
  	      if ( sxa > 0.0f ) {
		// System.out.println( "time: " + sxa);
  	        cpm = 60.0f / (sxa * 1.0f);
  	        if ( cpm > 50.0f && cpm < 90.f ) {
  	          strokes.append(String.format("%.6f %.6f %.6f %.6f %d\n",time,cpm,smooth_x.avg(cpm),kalman.filter(cpm),timestamp));
  	        }
  	      }
	    } else {
              continue;
	    }

  	  }

  	  bufferedReader.close();
  	  strokes.close();
  	  accelerometer.close();
	} catch(FileNotFoundException ex) {
           System.out.println( "Unable to open file '" + sensor_input + "'");                
        } catch(IOException ex) {
           System.out.println( "Error reading file '" + sensor_input + "'");                  
        }
        return offset;
    }
}

