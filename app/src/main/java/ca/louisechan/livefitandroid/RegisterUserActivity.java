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
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RegisterUserActivity extends AppCompatActivity {
    private static final String TAG = "RegisterUserActivity";

    // Request codes for camera and photo functions
    final int GET_PHOTO_FROM_GALLERY_REQUEST_CODE = 1001;
    final int TAKE_PHOTO_ACTIVITY_REQUEST_CODE = 1000;

    File photoFile;
    String photoFilename = "register.jpg";
    Bitmap registerPhotoBMP = null;
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    StorageReference storageRef;
    User registerUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
    }

    public void registerTakePhotoPressed(View view) {
        // use the built in camera app
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // create a file handle for the phoot that will be taken
        photoFile = getFileForPhoto(photoFilename);


        // For api > 24, you need to wrap the File object in a file provider (content provider)
        // - See AndroidManifest.xml: <provider> tag
        // - See res/xml/fileprovider.xml
        Uri fileProvider = FileProvider.getUriForFile(RegisterUserActivity.this, "ca.louisechan.livefitandroid", photoFile);

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

    public boolean checkIfEmailIsValid(String email) {
        if(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        }
        else {
            return false;
        }
    }

    public void registerSignupPressed(View view) {
        // Check if any of the fields are empty
        EditText etName = (EditText) findViewById(R.id.edtRegisterName);
        EditText etEmail = (EditText) findViewById(R.id.edtRegisterEmail);
        EditText etPasswd = (EditText) findViewById(R.id.edtRegisterPassword);
        EditText etCPasswd = (EditText) findViewById(R.id.edtRegisterConfPassword);
        ImageView imgPhoto = (ImageView) findViewById(R.id.imgRegisterPhoto);

        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String passwd = etPasswd.getText().toString();
        String cpasswd = etCPasswd.getText().toString();

        // Check if any of the entries is empty
        if (name.isEmpty() || email.isEmpty() || passwd.isEmpty() || cpasswd.isEmpty()) {
            Toast.makeText(this, "Input fields should not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email is valid
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches() == false) {
            Toast.makeText(this, "Invalid email input.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if passwords are the same
        if (passwd.equals(cpasswd) == false) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user to database
        registerUser = new User(name, email, passwd, registerPhotoBMP);
        addFirebaseUser(registerUser);

    }



    public void registerCancelPressed(View view) {
        // Go back to login screen without doing anything
        returnToLoginScreen();

    }

    public void addFirebaseUser(User u) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(u.getEmail(), u.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            if (registerPhotoBMP != null) {
                                // Upload photo to Firebase storage
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    registerPhotoBMP.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] byteArrayData = baos.toByteArray();

                                    storage = FirebaseStorage.getInstance();
                                    storageRef = storage.getReference("/userPhotos/" + user.getUid() + ".jpg");

                                    UploadTask uploadTask = storageRef.putBytes(byteArrayData);
                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Log.d(TAG, "onFailure: Photo was not uploaded");
                                            // no photo url to set since upload failed
                                            setupUserProfile(null);
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                            Log.d(TAG, "onSuccess: File uploaded successfully.");

                                            // Setup the rest of the user's data
                                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    Log.d(TAG, "onSuccess: Image url: " + uri.toString());
                                                    setupUserProfile(uri);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Log.d(TAG, "onFailure: Failure in getting photo url.");
                                                }
                                            });

                                        }
                                    });
                                }
                            } else {
                                // no photo url to set
                                setupUserProfile(null);
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Exception e = task.getException();
                            String errMsg = e.getMessage();

                            Toast.makeText(RegisterUserActivity.this, "Registration failed.\n" + errMsg,
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    public void setupUserProfile(Uri photoUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // setup user info
            if (registerUser != null) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(registerUser.getName())
                        .setPhotoUri(photoUrl)
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");
                                }

                                Toast.makeText(RegisterUserActivity.this, "User successfully registered.",
                                        Toast.LENGTH_SHORT).show();
                                returnToLoginScreen();
                            }
                        });
            }
        }

        Toast.makeText(RegisterUserActivity.this, "User profile not updated.",
                Toast.LENGTH_SHORT).show();
    }

    public void returnToLoginScreen() {
        startActivity(new Intent(this, MainActivity.class));
    }


}


