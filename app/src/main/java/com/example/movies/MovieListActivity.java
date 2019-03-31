package com.example.movies;

import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MovieListActivity extends AppCompatActivity {

    public static final String MOVIE = "movie";
    public static final String PHOTO_PATH = "PhotoPath";
    public static final int PICTURE_DIMENTION = 1080;
    List<Movie> movies;
    View clickedView;
    String photoPath;
    FloatingActionButton fab;
//    RecyclerView recyclerView;
    MovieAdapter adapter;
    int PHOTO_REQUEST_CODE = 37;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movies=getIntent().getParcelableArrayListExtra(SplashActivity.MOVIES);
        photoPath=getIntent().getStringExtra(PHOTO_PATH);
        setContentView(R.layout.activity_movie_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(SplashActivity.MOVIES, ""+v.getTag());
                clickedView = v;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                                AppDatabase.class, "movie").build();
                        MovieDao movieDao = db.movieDao();
                        Intent intent = new Intent(getApplicationContext(), MovieDetailsActivity.class);
                        intent.putExtra(MOVIE,movieDao.getMovieById((int)clickedView.getTag()));
                        startActivity(intent);
                    }
                }).start();
            }
        };

        RecyclerView recyclerView = findViewById(R.id.recycler);
        adapter=new MovieAdapter(movies, clickListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File photoFile = new File(storageDir,"photo.jpg");
                photoPath = photoFile.getAbsolutePath();
                Uri photoUri = FileProvider.getUriForFile(MovieListActivity.this, "com.example.movies.fileprovider", photoFile);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, PHOTO_REQUEST_CODE);
                } else {
                    Toast.makeText(MovieListActivity.this, "No Photo Apps on this Device!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        getIntent().putExtra(PHOTO_PATH, photoPath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            detectQR();
        }
    }

    void detectQR(){
        fab.setEnabled(false);

        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                        .build();

        int orientation = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(photoPath);
            int ori = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,1);
            if (ori == ExifInterface.ORIENTATION_ROTATE_90) orientation=90;
            else if (ori == ExifInterface.ORIENTATION_ROTATE_180) orientation=180;
            else if (ori == ExifInterface.ORIENTATION_ROTATE_270) orientation=270;
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e(MOVIE,"Orientation = "+orientation);

        //BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        Bitmap large = BitmapFactory.decodeFile(photoPath);//,bitmapOptions

        Matrix matrix = new Matrix();
        if (orientation!=0) matrix.postRotate(orientation);
        Bitmap large_corrected = Bitmap.createBitmap(large,0,0,large.getWidth(),large.getHeight(),matrix,true);
        Bitmap small;

        double ratio = (double)large_corrected.getWidth()/(double)large_corrected.getHeight();
        if (large_corrected.getWidth()>large_corrected.getHeight()) {
            small = Bitmap.createScaledBitmap(large_corrected, PICTURE_DIMENTION, (int)(PICTURE_DIMENTION/ratio), false);
        } else {
            small = Bitmap.createScaledBitmap(large_corrected, (int)(PICTURE_DIMENTION*ratio), PICTURE_DIMENTION, false);
        }

        Log.e(MOVIE, "Width="+small.getWidth()+", height="+small.getHeight());

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(small);

        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);


        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        if (barcodes.isEmpty()){
                            Toast.makeText(MovieListActivity.this, "Nothing detected...", Toast.LENGTH_SHORT).show();
                        } else {
                            for (FirebaseVisionBarcode barcode : barcodes) {
                                Log.e(MOVIE, barcode.getRawValue());
                            }
                            updateDB(barcodes);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MovieListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        fab.setEnabled(true);
    }

    void updateDB(final List<FirebaseVisionBarcode> barcodes){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "movie").build();

                MovieDao movieDao = db.movieDao();

                for (FirebaseVisionBarcode barcode : barcodes) {
                    try {
                        Movie newMovie = new Gson().fromJson(barcode.getRawValue(),Movie.class);
                        movieDao.insertAll(newMovie);
                        movies.clear();
                        movies.addAll(movieDao.getAll());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                Snackbar.make(fab, R.string.movie_added, Snackbar.LENGTH_LONG).show();
                            }
                        });
                    } catch (JsonSyntaxException e) {
                        Log.e(MOVIE,"Not a movie");
                    } catch (SQLiteConstraintException e){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(fab, R.string.movie_exists, Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                db.close();
            }
        });
        thread.start();
    }


}
