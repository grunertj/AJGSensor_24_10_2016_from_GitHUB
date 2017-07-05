import java.io.*;
import java.util.zip.*;

// http://www.xyzws.com/javafaq/how-to-make-a-gzip-file-in-java/215

public class SmoothFile {
  static BufferedWriter bufferedWriter = null;
  static BufferedWriter bufferedWriter_direct = null;
  static BufferedWriter gyro_direct = null;
  static BufferedWriter rotation_direct = null;
  static BufferedWriter linear = null;
  static BufferedWriter gyro = null;
  static BufferedWriter gyro_l = null;
  static BufferedWriter gyro_r = null;
  static BufferedWriter gyro_d = null;
  static BufferedWriter accelerometer = null;

    public static void main(String[] args) {
        System.out.println("Hello, World");
	Smooth smooth = new Smooth(100);
	Smooth smooth_d = new Smooth(100);
	Smooth smooth_a = new Smooth(100);
	Smooth smooth_l = new Smooth(100);
	String[] array;
	float x,y,z,py=-100000.0f;
	long timestamp,offset = 0;
	double tl;

	String fname = "sensor-2017_04_09_09_50_48.txt.gz";
	String line = null;

	try {
          // FileReader fileReader = new FileReader(fname);
          // BufferedReader bufferedReader = new BufferedReader(fileReader);
          FileInputStream fin = new FileInputStream(fname);
          GZIPInputStream gzis = new GZIPInputStream(fin);
          InputStreamReader xover = new InputStreamReader(gzis);
          BufferedReader bufferedReader = new BufferedReader(xover);
          bufferedWriter = new BufferedWriter(new FileWriter("liner_acceleration_smooth.dat"));
          bufferedWriter_direct = new BufferedWriter(new FileWriter("liner_acceleration_direct.dat"));
          gyro_direct = new BufferedWriter(new FileWriter("gyro_direct.dat"));
          rotation_direct = new BufferedWriter(new FileWriter("rotation_direct.dat"));
          linear = new BufferedWriter(new FileWriter("linear.dat"));
          gyro = new BufferedWriter(new FileWriter("gyro.dat"));
          gyro_l = new BufferedWriter(new FileWriter("gyro_l.dat"));
          gyro_r = new BufferedWriter(new FileWriter("gyro_r.dat"));
          gyro_d = new BufferedWriter(new FileWriter("gyro_d.dat"));
          accelerometer = new BufferedWriter(new FileWriter("accelerometer.dat"));
          // bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream("liner_acceleration_smooth.dat.gz"))));
  
          while((line = bufferedReader.readLine()) != null) {
            array = line.split("\\s+");

  	    timestamp = Long.parseLong(array[1]);
  
  	    if (offset == 0 ) {
  	      offset = timestamp;
  	    }

  	    tl = (timestamp - offset)/1000.0f;
  
	    if (array[2].matches("10")) {
	      x = Float.parseFloat(array[3]);
	      y = Float.parseFloat(array[4]);
	      z = Float.parseFloat(array[5]);

              // System.out.println(String.format("%d %.6f",timestamp,smooth.avg((float)Math.sqrt(x*x+y*y+z*z))));
	      bufferedWriter.append(String.format("%.6f %.6f\n",tl,smooth.avg((float)Math.sqrt(x*x+y*y+z*z))));
	      bufferedWriter_direct.append(String.format("%.6f %.6f\n",tl,(float)Math.sqrt(x*x+y*y+z*z)));
	      linear.append(String.format("%.6f %.6f %.6f %.6f %6f %.6f\n",tl,x,y,z,(float)Math.sqrt(x*x+y*y+z*z),smooth_l.avg((float)Math.sqrt(x*x+y*y+z*z))));
	    } else if (array[2].matches("1")) {
	      x = Float.parseFloat(array[3]);
	      y = Float.parseFloat(array[4]);
	      z = Float.parseFloat(array[5]);
	      accelerometer.append(String.format("%.6f %.6f %.6f %.6f %6f %.6f\n",tl,x,y,z,(float)Math.sqrt(x*x+y*y+z*z),smooth_a.avg((float)Math.sqrt(x*x+y*y+z*z))));
	    } else if (array[2].matches("4")) {
	      x = Float.parseFloat(array[3]);
	      y = Float.parseFloat(array[4]);
	      z = Float.parseFloat(array[5]);

              // System.out.println(String.format("%d %.6f",timestamp,smooth.avg((float)Math.sqrt(x*x+y*y+z*z))));
	      gyro_direct.append(String.format("%.6f %.6f\n",tl,(float)Math.sqrt(x*x+y*y+z*z)));
	      gyro.append(String.format("%.6f %.6f %.6f %.6f\n",tl,x,y,z));
	      if (y > -2.0 && y < 2.0) {
		if (py < -10000) { py = y; }
	        gyro_d.append(String.format("%.6f %.6f %.6f\n",tl,y+py,smooth_d.avg(y+py)));
		py = y;
	      }
	      if ( y > 0.8 && y < 2.0) {
	        gyro_l.append(String.format("%.6f %.6f %.6f %.6f\n",tl,x,y,z));
	      } else if ( y < -0.8 && y > -2.0 ) {
	        gyro_r.append(String.format("%.6f %.6f %.6f %.6f\n",tl,x,y,z));
	      }
	    } else if (array[2].matches("11")) {
	      x = Float.parseFloat(array[3]);
	      y = Float.parseFloat(array[4]);
	      z = Float.parseFloat(array[5]);

              // System.out.println(String.format("%d %.6f",timestamp,smooth.avg((float)Math.sqrt(x*x+y*y+z*z))));
	      rotation_direct.append(String.format("%.6f %.6f\n",tl,(float)Math.sqrt(x*x+y*y+z*z)));
	    }

  	  }

  	  bufferedReader.close();
  	  bufferedWriter.close();
  	  bufferedWriter_direct.close();
  	  gyro_direct.close();
  	  rotation_direct.close();
  	  gyro.close();
  	  gyro_l.close();
  	  gyro_r.close();
  	  gyro_d.close();
  	  linear.close();
  	  accelerometer.close();

	} catch(FileNotFoundException ex) {
           System.out.println( "Unable to open file '" + fname + "'");                
        } catch(IOException ex) {
           System.out.println( "Error reading file '" + fname + "'");                  
        }
    }
}

