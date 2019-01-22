package io.github.mimerme.whereu.utility;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;

public class AndroidStorage implements Storage{


    private File mFile;
    private BufferedReader reader;

    public AndroidStorage(Context context, String filename){
        mFile = new File(context.getFilesDir(), filename);

        try {
            if(!mFile.exists())
                mFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeTo(String data) {
        try {
            FileOutputStream fOut = new FileOutputStream(mFile);
            OutputStreamWriter mWriter = new OutputStreamWriter(fOut);
            mWriter.write(data);
            mWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Iterator<String> iterator() {
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(mFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new Iterator<String>() {
                String line;

                {
                    try {
                        line = reader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                @Override
                public boolean hasNext() {
                    return line != null;
                }

                @Override
                public String next() {
                    String temp = line;
                    try {
                        line = reader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return temp;
                }
            };
    }
}
