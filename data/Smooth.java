public class Smooth {
  private float buffer[];
  private int count = 10;
  private int center = 5;

  public Smooth () {
    buffer = new float[count];
    for (int i = 0 ; i < count ; i++) {
      buffer[i] = 0.0f;
    }
    System.out.println("Count: "+count);
  }

  public Smooth (float data) {
    buffer = new float[count];
    for (int i = 0 ; i < count ; i++) {
      buffer[i] = data;
    }
    System.out.println("Count: "+count);
  }

  public Smooth (int c) {
    count = c;
    center = count/2;
    buffer = new float[count];
    for (int i = 0 ; i < count ; i++) {
      buffer[i] = 0.0f;
    }
    System.out.println("Count: "+count);
  }

  public Smooth (int c, float data) {
    count = c;
    center = count/2;
    buffer = new float[count];
    for (int i = 0 ; i < count ; i++) {
      buffer[i] = data;
    }
    System.out.println("Count: "+count);
  }

  public float avg (float data) {
    float total = 0.0f;
    int i = 0;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < count ; i++) {
      total = total + buffer[i];
    }

    return (total/count);
  }

  public float avg (String data_string) {
    float data = 0.0f;
    data = Float.parseFloat(data_string);
    float total = 0.0f;
    int i = 0;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < count ; i++) {
      total = total + buffer[i];
    }

    return (total/count);
  }

  public float max (float data) {
    int i = 0;
    int c = center - 1;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < c ; i++) {
      if (buffer[i] > buffer[c]) {
	return 0;
      }
    }

    for (i = c + 1 ; i < count ; i++) {
      if (buffer[i] > buffer[c]) {
	return 0;
      }
    }

    return buffer[c];
  }

  public float max (String data_string) {
    float data = 0.0f;
    data = Float.parseFloat(data_string);
    int i = 0;
    int c = center - 1;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < c ; i++) {
      if (buffer[i] > buffer[c]) {
	return 0;
      }
    }

    for (i = c + 1 ; i < count ; i++) {
      if (buffer[i] > buffer[c]) {
	return 0;
      }
    }

    return buffer[c];
  }

  public boolean is_max (String data_string) {
    float data = 0.0f;
    data = Float.parseFloat(data_string);
    int i = 0;
    int c = center - 1;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < c ; i++) {
      if (buffer[i] > buffer[c]) {
	return false;
      }
    }

    for (i = c + 1 ; i < count ; i++) {
      if (buffer[i] > buffer[c]) {
	return false;
      }
    }

    return true;
  }

  public boolean is_max (float data) {
    int i = 0;
    int c = center - 1;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < c ; i++) {
      if (buffer[i] > buffer[c]) {
	return false;
      }
    }

    for (i = c + 1 ; i < count ; i++) {
      if (buffer[i] > buffer[c]) {
	return false;
      }
    }

    return true;
  }

  public float min (String data_string) {
    float data = 0.0f;
    data = Float.parseFloat(data_string);
    int i = 0;
    int c = center - 1;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < c ; i++) {
      if (buffer[i] < buffer[c]) {
	return 0;
      }
    }

    for (i = c + 1 ; i < count ; i++) {
      if (buffer[i] < buffer[c]) {
	return 0;
      }
    }

    return buffer[c];
  }

  public float min (float data) {
    int i = 0;
    int c = center - 1;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < c ; i++) {
      if (buffer[i] < buffer[c]) {
	return 0;
      }
    }

    for (i = c + 1 ; i < count ; i++) {
      if (buffer[i] < buffer[c]) {
	return 0;
      }
    }

    return buffer[c];
  }

  public boolean is_min (String data_string) {
    float data = 0.0f;
    data = Float.parseFloat(data_string);
    int i = 0;
    int c = center - 1;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < c ; i++) {
      if (buffer[i] < buffer[c]) {
	return false;
      }
    }

    for (i = c + 1 ; i < count ; i++) {
      if (buffer[i] < buffer[c]) {
	return false;
      }
    }

    return true;
  }

  public boolean is_min (float data) {
    int i = 0;
    int c = center - 1;

    for (i = count - 1 ; i > 0 ; i--) {
      buffer[i] = buffer[i-1];
    }
    buffer[0] = data;

    for (i = 0 ; i < c ; i++) {
      if (buffer[i] < buffer[c]) {
	return false;
      }
    }

    for (i = c + 1 ; i < count ; i++) {
      if (buffer[i] < buffer[c]) {
	return false;
      }
    }

    return true;
  }
}

