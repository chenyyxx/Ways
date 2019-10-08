package com.project.ways;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback, ReportDialog.DialogCallBack {
    private static final int REQUEST_CAPTURE_IMAGE = 100;
    private final String path = Environment.getExternalStorageDirectory()+"/temp.png";
    private static final int REQUEST_EXTERNAL_STORAGE =1;
    private static String[] PERMISSIONS_STORAGE={
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private MapView mapView;
    private View view;
    private LocationTracker locationTracker;
    private GoogleMap googleMap;
    private FloatingActionButton fabReport;
    private FloatingActionButton fabFocus;
    private ReportDialog dialog;
    private DatabaseReference database;
    private FirebaseStorage storage;
    private StorageReference storageRef;


    public static MainFragment newInstance() {
        
        Bundle args = new Bundle();
        
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        verifyStoragePermissions(getActivity());
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) this.view.findViewById(R.id.event_map_view);
        fabReport = (FloatingActionButton) this.view.findViewById(R.id.fab);
        fabReport.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showDialog(null, null);
            }
        });

        fabFocus = (FloatingActionButton) this.view.findViewById(R.id.fab_focus);
        fabFocus.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mapView.getMapAsync(MainFragment.this);
            }
        });

        if(mapView!=null){
            mapView.onCreate(null);
            mapView.onResume();//needed to get the map to display immediately
            mapView.getMapAsync(this);
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        this.googleMap = googleMap;
        this.googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json)
        );

        locationTracker = new LocationTracker(getActivity());
        locationTracker.getLocation();
        double latitude = locationTracker.getLatitude();
        double longitude = locationTracker.getLongitude();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude,longitude)).zoom(16).bearing(90).tilt(30).build();
        // Animate the zoom process
        googleMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        // Create marker on google map
        MarkerOptions marker = new MarkerOptions().position(
                new LatLng(latitude,longitude)).title("You");
        // Change marker Icon on google map
        marker.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.boy));
        // Add marker to google map
        googleMap.addMarker(marker);
        loadEventInVisibleMap();

    }

    // Get Center Coordinate
    private void loadEventInVisibleMap(){
        database.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapShot: dataSnapshot.getChildren()){
                    TrafficEvent event = noteDataSnapShot.getValue(TrafficEvent.class);
                    double eventLatitude = event.getEvent_latitude();
                    double eventLongitude = event.getEvent_longitude();
                    locationTracker.getLocation();
                    double centerLatitude = locationTracker.getLatitude();
                    double centerLongitude = locationTracker.getLongitude();

                    int distance = Utils.distanceBetweenTwoLocations(centerLatitude, centerLongitude, eventLatitude, eventLongitude);
                    if(distance <20){
                        LatLng latLng = new LatLng(eventLatitude, eventLongitude);
                        MarkerOptions marker = new MarkerOptions().position(latLng);

                        // changing marker icon
                        String type = event.getEvent_type();
                        Bitmap icon = BitmapFactory.decodeResource(getContext().getResources(), Config.trafficMap.get(type));

                        Bitmap resizeBitmap = Utils.getResizedBitmap(icon, 130, 130);

                        marker.icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap));

                        // Adding marker
                        Marker mker = googleMap.addMarker(marker);
                        mker.setTag(event);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // TODO: do something
            }
        });
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    private void showDialog(String label, String prefillText) {
        int cx = (int)(fabReport.getX()+(fabReport.getWidth()/2));
        int cy = (int)(fabReport.getY()+fabReport.getHeight() +56);
        dialog = ReportDialog.newInstance(getContext(),cx, cy, this);
        dialog.show();
    }

    private String uploadEvent(String user_id, String editString, String event_type){
        TrafficEvent event = new TrafficEvent();
        event.setEvent_type(event_type);
        event.setEvent_description(editString);
        event.setEvent_reporter_id(user_id);
        event.setEvent_timestamp(System.currentTimeMillis());
        event.setEvent_latitude(locationTracker.getLatitude());
        event.setEvent_longitude(locationTracker.getLongitude());
        event.setEvent_like_number(0);
        event.setEvent_comment_number(0);

        String key = database.child("events").push().getKey();
        event.setId(key);
        database.child("events").child(key).setValue(event, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError!=null){
                    Toast toast = Toast.makeText(getContext(),
                            "The event is failed, please check your network status.", Toast.LENGTH_SHORT);
                    toast.show();
                    dialog.dismiss();
                } else {
                    Toast toast = Toast.makeText(getContext(), "The event is reported", Toast.LENGTH_SHORT);
                    toast.show();
                    //TODO: update map fragment
                }
            }
        });
        return key;
    }

    @Override
    public void onSubmit(String editString, String event_type) {
        String key = uploadEvent(Config.username, editString, event_type);

        // Upload image and link the image to the corresponding key
        uploadImage(key);
    }

    @Override
    public void startCamera() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
        );
        startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE);
    }

    // Store the image into local disk
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case REQUEST_CAPTURE_IMAGE:{
                if(resultCode == RESULT_OK && data!= null && data.getExtras()!=null){
                    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                    if(dialog!=null && dialog.isShowing()){
                        dialog.updateImage(imageBitmap);
                    }
                    //Compress the image, this is optional
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes);

                    File destination = new File(Environment.getExternalStorageDirectory(),"temp.png");
                    if(!destination.exists()){
                        try{
                            destination.createNewFile();
                        } catch (IOException ex){
                            ex.printStackTrace();
                        }
                    }
                    FileOutputStream fo;
                    try{
                        fo = new FileOutputStream(destination);
                        fo.write(bytes.toByteArray());
                        fo.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
                break;
            }
            default:
        }
    }

    // Upload image to cloud storage
    private void uploadImage(final String key){
        File file = new File(path);
        if(!file.exists()){
            dialog.dismiss();
            loadEventInVisibleMap();
            return;
        }
        Uri uri = Uri.fromFile(file);
        final StorageReference imgRef = storageRef.child("images/"+uri.getLastPathSegment()+"_"+System.currentTimeMillis());
        imgRef.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return imgRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloadUri = task.getResult();
                database.child("events").child(key).child("imgUri").setValue(downloadUri.toString());
                File file = new File(path);
                file.delete();
                dialog.dismiss();
                loadEventInVisibleMap();
            }
        });
    }

    public static void verifyStoragePermissions(Activity activity){
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission!= PackageManager.PERMISSION_GRANTED){
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
