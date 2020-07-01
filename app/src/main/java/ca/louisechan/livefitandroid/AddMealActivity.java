package ca.louisechan.livefitandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddMealActivity extends AppCompatActivity {
    private static final String TAG = "AddMealActivity";

    // Request codes for camera and photo functions
    final int GET_PHOTO_FROM_GALLERY_REQUEST_CODE = 3001;
    final int TAKE_PHOTO_ACTIVITY_REQUEST_CODE = 3000;

    File photoFile;
    String photoFilename = "meal.jpg";
    Bitmap registerPhotoBMP = null;
    String mealUID = null;
    String mkUID = null;

    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    HashMap<String, String> mealkitsHashMap = new HashMap<>();
    ArrayList<String> mkNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Read all the mealkit name and uids and store it in an array
        db.collection("mealpackages").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        mealkitsHashMap.put(document.getString("name"), document.getId());
                        mkNames.add(document.getString("name"));
                    }
                }

                AddMealActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Populate spinner with mealkit names
                        Spinner spMealkit = (Spinner) findViewById(R.id.spinMealkit);

                        ArrayAdapter<String> mkAdapter = new ArrayAdapter<String>(AddMealActivity.this, android.R.layout.simple_spinner_item, mkNames);
                        mkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spMealkit.setAdapter(mkAdapter);
                    }
                });

            }
        });

    }

    public void submitNewMeal(View view) {
        EditText etName = (EditText) findViewById(R.id.edtMealName);
        EditText etDesc = (EditText) findViewById(R.id.edtMealDesc);
        EditText etCalories = (EditText) findViewById(R.id.edtMealCalories);

        String name = etName.getText().toString();
        String desc = etDesc.getText().toString();
        String cal = etCalories.getText().toString();

        // Check if any of the fields are empty
        if (name.isEmpty() || desc.isEmpty() || cal.isEmpty()) {
            Toast.makeText(this, "No fields should be left blank.", Toast.LENGTH_SHORT).show();
            return;
        }

        // All fields are filled up.
        // Get the doc uid of the mealkit so that we may be able to add the meal to its meals collection.
        Spinner spMealkit = (Spinner) findViewById(R.id.spinMealkit);
        String selectedMealkit = spMealkit.getSelectedItem().toString();

        mkUID = mealkitsHashMap.get(selectedMealkit);
        Log.d(TAG, "submitNewMeal: Mealkit is " + selectedMealkit + ", document UID is " + mkUID);

        addMealInfoExceptImage(mkUID);

    }

    public void addPhotoAndImageUrl() {

        // Upload photo to Firebase storage
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            registerPhotoBMP.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteArrayData = baos.toByteArray();

            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference("/meals/" + mealUID + ".jpg");

            UploadTask uploadTask = storageRef.putBytes(byteArrayData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(TAG, "onFailure: Photo was not uploaded");
                    // no photo url to set since upload failed
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Log.d(TAG, "onSuccess: File uploaded successfully.");


                    db.collection("mealpackages").document(mkUID).collection("meals").document(mealUID).update("imageName", mealUID + ".jpg")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "onSuccess: Meal image updated successfully");
                                    Intent i = new Intent(AddMealActivity.this, AdminUserMainActivity.class);
                                    startActivity(i);
                                }
                            });
                }
            });
        }

    }

    public void addMealInfoExceptImage(String mkUID) {
        // no photo url to set
        EditText etName = (EditText) findViewById(R.id.edtMealName);
        EditText etDesc = (EditText) findViewById(R.id.edtMealDesc);
        EditText etCalories = (EditText) findViewById(R.id.edtMealCalories);

        Map<String, Object> mealMap = new HashMap<>();
        mealMap.put("name", etName.getText().toString());
        mealMap.put("description", etDesc.getText().toString());
        mealMap.put("calories", etCalories.getText().toString());
        mealMap.put("imageName", null);

        db.collection("mealpackages").document(mkUID).collection("meals")
                .add(mealMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());

                        mealUID = documentReference.getId();
                        Toast.makeText(AddMealActivity.this, "Meal added to database.", Toast.LENGTH_SHORT).show();

                        if (registerPhotoBMP == null) {
                            Intent i = new Intent(AddMealActivity.this, AdminUserMainActivity.class);
                            startActivity(i);
                        } else {
                            addPhotoAndImageUrl();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(AddMealActivity.this, "Failure adding document to database.\nException: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }


    public void registerTakePhotoPressed(View view) {
        // use the built in camera app
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // create a file handle for the phoot that will be taken
        photoFile = getFileForPhoto(photoFilename);


        // For api > 24, you need to wrap the File object in a file provider (content provider)
        // - See AndroidManifest.xml: <provider> tag
        // - See res/xml/fileprovider.xml
        Uri fileProvider = FileProvider.getUriForFile(AddMealActivity.this, "ca.louisechan.livefitandroid", photoFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);


        // Try to open the camera app
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_PHOTO_ACTIVITY_REQUEST_CODE);
        }
    }

    // Helper function to generate an empty file to store the photo that you take with the Camera app
    public File getFileForPhoto(String fileName) {
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        if (mediaStorageDir.exists() == false && mediaStorageDir.mkdirs() == false) {
            Log.d(TAG, "Cannot create directory for storing photos");
        }

        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }

    public void registerChoosePhotoPressed(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Log.d(TAG, "registerChoosePhotoPressed: Path to photo gallery: " + MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());

        if(intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, GET_PHOTO_FROM_GALLERY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: The imagepicker activity closed!");
        Log.d(TAG, "onActivityResult: The request code is " + requestCode);


        // check if it was the camera app that just closed
        if (requestCode == TAKE_PHOTO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // the photo is saved to file
                registerPhotoBMP = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                // make the image fit into the imageview
                ImageView ivPhoto = (ImageView) findViewById(R.id.imgRegisterPhoto);
                ivPhoto.setImageBitmap(registerPhotoBMP);

            } else {
                Toast t = Toast.makeText(this, "Not able to take photo", Toast.LENGTH_SHORT);
                t.show();
            }
        }

        // check if it was the photo gallery that just closed
        // get the photo and display it in the image view
        // check if we got a picture from the gallery
        if (requestCode == GET_PHOTO_FROM_GALLERY_REQUEST_CODE) {
            if (data != null) {
                Uri photoURI = data.getData();

                Log.d(TAG, "Path to the photo the user selected: " + photoURI.toString());

                try {
                    registerPhotoBMP = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);

                    // make the image fit into the imageview
                    ImageView imagePhoto = (ImageView) findViewById(R.id.imgRegisterPhoto);
                    imagePhoto.setImageBitmap(registerPhotoBMP);
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "FileNotFoundException: Unable to open photo gallery file");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d(TAG, "IOException: Unable to open photo gallery file");
                    e.printStackTrace();
                }
            }
        }
    }
}
