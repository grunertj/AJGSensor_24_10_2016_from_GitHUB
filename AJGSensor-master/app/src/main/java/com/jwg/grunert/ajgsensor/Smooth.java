package com.jwg.grunert.ajgsensor;

/**
 * Created by grunert on 5/1/16.
 */
public class Smooth {
    private float time[];
    private float buffer[];
    private int count = 10;
    static final float ALPHA = 0.10f; // if ALPHA = 1 OR 0, no filter applies.




    public Smooth () {
        buffer = new float[count];
        time = new float[count];
        for (int i = 0 ; i < count ; i++) {
            time[i] = 0.0f;
            buffer[i] = 0.0f;
        }
        System.out.println("Count: "+count);
    }

    public Smooth (int c) {
        count = c;
        buffer = new float[count];
        for (int i = 0 ; i < count ; i++) {
            buffer[i] = 0.0f;
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

    public float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public float lowPass( float input, float output ) {
        if ( output == 0 ) return input;
        output = output + ALPHA * (input - output);
        return output;
    }

    public float highPass( float input, float output ) {
        float vel=0;
        if ( output == 0 ) return input;
        vel +=  (input - output);
        vel -= (-output * 0.5);
        output += vel;

        return output;
    }
}

/*
A LP filter can be thought of as an output sample attached via spring to the input sample, stepping through the array, like this...

vel += (in - out) * cutoff;
out += vel;
vel *= resonance;




A HP filter is similar, except the output is 'kicked' by the input, and is attached to equilibrium by a spring, like this...

vel += (in - out);
vel -= (-out * cutoff);
out += vel;
vel *= resonance;
*/
