package com.github.shwuup.app.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileManager {

    protected Context context;

    public FileManager(Context newContext) {
        this.context = newContext;
    }

    protected File getFile(String fileName) {
        Context context = this.context;
        File file = new File(context.getFilesDir(), fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public String readFile(File file) {
        String contents = null;
        try {

            int length = (int) file.length();
            byte[] bytes = new byte[length];
            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);

            } finally {
                in.close();
            }
            contents = new String(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    public void writeFile(File file, String content) {
        try {
            FileOutputStream stream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.append(content);
            writer.close();
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
