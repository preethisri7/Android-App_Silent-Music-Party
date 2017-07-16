package de.tudarmstadt.informatik.tk.shhparty.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by rohit on 28-02-2017.
 */

public class StorageUtils {

    /*public static String saveImageToStorage(Bitmap img, Context context){
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File myFile=new File(directory,"ProfilePic.jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myFile);
            // Use the compress method on the BitMap object to write image to the OutputStream
            img.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            Log.e("saveImageToStorage()", e.getMessage());
        } finally {
            try {
                if(fos != null) {
                    fos.close();
                    Log.v("StorageUtils","Profile Pic stored!");
                }
            } catch (IOException e) {
                Log.e("saveImageToStorage()", e.getMessage());
            }
        }
        return directory.getAbsolutePath();
    }*/


    public static void saveImageToStorage(Context context, Bitmap b, String name, String extension){
        name=name+"."+extension;
        FileOutputStream out;
        try {
            out = context.openFileOutput(name, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
            Log.v("StorageUtils","Profile Pic stored!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("saveImageToStorage()", e.getMessage());
        }
    }

    public static Bitmap getImageFromStorage(Context context,String name,String extension){
        name=name+"."+extension;
        try{
            FileInputStream fis = context.openFileInput(name);
            Bitmap b = BitmapFactory.decodeStream(fis);
            fis.close();
            Log.v("StorageUtils","Profile Pic retrieved!");
            return b;
        }
        catch(Exception e){
            Log.e("getImageFromStorage()", e.getMessage());
        }
        return null;
    }


    /*public Bitmap getImageFromStorage(String filename, Context context) {
        Bitmap image = null;
            try {
                File filePath = context.getFileStreamPath(filename);
                FileInputStream fi = new FileInputStream(filePath);
                image = BitmapFactory.decodeStream(fi);
            } catch (Exception ex) {
                Log.e("getImageFromStorage()", ex.getMessage());
            }
        return image;
    }*/

}