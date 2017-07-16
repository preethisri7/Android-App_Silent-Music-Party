package de.tudarmstadt.informatik.tk.shhparty.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.tudarmstadt.informatik.tk.shhparty.PartyInfoActivity;
import de.tudarmstadt.informatik.tk.shhparty.R;
import de.tudarmstadt.informatik.tk.shhparty.member.MemberBean;
import de.tudarmstadt.informatik.tk.shhparty.member.SearchForParties;
import de.tudarmstadt.informatik.tk.shhparty.utils.SharedBox;
import de.tudarmstadt.informatik.tk.shhparty.utils.StorageUtils;

public class CreateProfile extends Activity implements View.OnClickListener {

  private static int RESULT_LOAD_IMAGE = 1;

  String name ;
  Button buttonEnter,buttonLoadPicture;

  Bitmap bmp = null;
  Boolean profilePicChanged = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_profile);
    buttonEnter = (Button) findViewById(R.id.buttonEnter);

    buttonEnter.setOnClickListener(this);

    buttonLoadPicture = (Button) findViewById(R.id.buttonLoadPicture);

    buttonLoadPicture.setOnClickListener(this);

    // Load PersonName from storage (Shared Preferences), if it is present
    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
    name = sharedPref.getString("PersonName", "Anonymous");

    EditText et1 = (EditText) findViewById(R.id.editText);
    et1.setText(name);

    // Load ProfilePic from storage, if it is present
    bmp = StorageUtils.getImageFromStorage(this, "ProfilePic", "jpg"); // Context context,String name,String extension
    ImageView imageView = (ImageView) findViewById(R.id.imgView);
    imageView.setImageBitmap(bmp);
  }

  public void onClick(View v) {

    EditText et1 = (EditText) findViewById(R.id.editText);

    switch (v.getId()) {
      case R.id.buttonEnter:

        //Code to save data in bean
        MemberBean myProfileData=new MemberBean();
        myProfileData.setName(et1.getText().toString());
       // File imgAsPng=new File(getApplicationContext().getCacheDir(),"picAsPng");
      /*  try {
          imgAsPng.createNewFile();
        } catch (IOException e) {
          e.printStackTrace();
        }*/
        if(bmp!=null) {
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          bmp.compress(Bitmap.CompressFormat.PNG, 10, bos);
          byte[] bitmapdata = bos.toByteArray();

          myProfileData.setBitmapdata(bitmapdata);
        }

        Log.d("Profile",myProfileData.toString());
        SharedBox.setMyProfileBean(myProfileData);


        Intent intentBasedOnRole = new Intent(this, PartyInfoActivity.class);
        if(getIntent().getStringExtra("role")!=null) {
          if (getIntent().getStringExtra("role").equals("member")) {
            intentBasedOnRole = new Intent(this, SearchForParties.class);
          }
        }

        intentBasedOnRole.putExtra("editText", et1.getText().toString());

        // Store PersonName
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("PersonName", et1.getText().toString());
        editor.commit();

        if (profilePicChanged) {
          // store the selected image to local storage
          StorageUtils.saveImageToStorage(this, bmp, "ProfilePic", "jpg"); // Context context, Bitmap b, String name, String extension
        }

        startActivity(intentBasedOnRole);
        break;
      case R.id.buttonLoadPicture:
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, RESULT_LOAD_IMAGE);
        break;
    }

  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
      Uri selectedImage = data.getData();
      String[] filePathColumn = { MediaStore.Images.Media.DATA };

      Cursor cursor = getContentResolver().query(selectedImage,
              filePathColumn, null, null, null);
      cursor.moveToFirst();

      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
      String picturePath = cursor.getString(columnIndex);
      cursor.close();

      ImageView imageView = (ImageView) findViewById(R.id.imgView);


      try {
        bmp = getBitmapFromUri(selectedImage);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      imageView.setImageBitmap(bmp);
      profilePicChanged = true;

    }


  }



  private Bitmap getBitmapFromUri(Uri uri) throws IOException {
    ParcelFileDescriptor parcelFileDescriptor =
            getContentResolver().openFileDescriptor(uri, "r");
    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
    Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

    parcelFileDescriptor.close();
    return image;
  }



}
