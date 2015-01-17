package com.geosoftware.victor.photovertice;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;



public class MainActivity extends ActionBarActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    File pictureFile;
    Display display;

    LocationManager locationManager;
    LocationListener locListenerGps;

    double latitude = 0;
    double longitude = 0;
    double altitude = 0;

    boolean galleryVisible = false;
    static String selectedBitmap;
    ArrayList<ImageDataModel> bitmaps = new ArrayList<ImageDataModel>();

    FrameLayout preview;
    LinearLayout imageDataLayout;
    ImageView btnPhoto;
    ImageView btnGallery;
    TextView txtLatitude;
    TextView txtLongitude;
    TextView txtAltitude;
    TextView txtTimeStamp;
    ListView lstGallery;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Prevengo que la imagen se apaise!!

        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_preview);

        preview = (FrameLayout) findViewById(R.id.camera_preview);
        imageDataLayout = (LinearLayout) findViewById(R.id.image_data_layout);
        btnPhoto = (ImageView) findViewById(R.id.imageView);
        btnGallery = (ImageView) findViewById(R.id.imageView2);
        txtLatitude = (TextView) findViewById(R.id.textView);
        txtLongitude = (TextView) findViewById(R.id.textView3);
        txtAltitude = (TextView) findViewById(R.id.textView4);
        txtTimeStamp = (TextView) findViewById(R.id.textView2);
        lstGallery = (ListView) findViewById(R.id.listView);



        //------------------ Control del GPS
        locListenerGps = new MyLocationListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListenerGps);
        txtTimeStamp.setText(getString(R.string.esperando_senal));

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.no_gps));
            builder.setMessage(getString(R.string.no_gps_descripcion));

            builder.setPositiveButton(getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            builder.setNegativeButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog gpsAlertDialog = builder.create();
            gpsAlertDialog.show();
        }

        new LstGalleryTask().execute();

        //----- EVENTOS -----
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });

        lstGallery.setVisibility(View.INVISIBLE);
        lstGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedBitmap = bitmaps.get(position).getSrc();
                ImageDialogFragment dialog = new ImageDialogFragment();
                dialog.show(getFragmentManager(), "Dialog");
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(galleryVisible){
                    lstGallery.setVisibility(View.INVISIBLE);
                    galleryVisible = false;
                }else{
                    lstGallery.setVisibility(View.VISIBLE);
                    galleryVisible = true;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //------------------- Control de la camara
        display = getWindowManager().getDefaultDisplay();
        Log.i("","ESTADO onResume");
        Log.i("","VALORES: " + mCamera + " " + mPreview);
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);

        mPreview = new CameraPreview(this, mCamera, display.getWidth(), display.getHeight());
        preview.addView(mPreview);

        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        params.setRotation(90);

        try{
            mCamera.setParameters(params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("","ESTADO onPause");
        if(mCamera !=  null){
            mCamera.release();
            mCamera = null;
            mPreview = null;
            preview.removeAllViews();
        }
    }




//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    //----- Dialogos

    public static class ImageDialogFragment extends DialogFragment{
        public ImageDialogFragment(){}

        View view;
        ImageView imageView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.image_dialog_fragment, container);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
            imageView = (ImageView) view.findViewById(R.id.imageView3);

            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bitmap bmp = decodeSampledBitmapFromResource(getResources(), selectedBitmap, 400, 300);
            imageView.setImageBitmap(bmp);
        }
    }


    //----- METODOS
    public static Camera getCameraInstance(){
        Camera c = null;

        try{
            c = Camera.open();
        }catch(Exception e){

        }

        return c;
    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            pictureFile = getOutputMediaFile();

            if(pictureFile == null){
                return;
            }

            new ProccessPictureTask(data).execute(pictureFile);
        }
    };

    public static Bitmap loadBitmapFromView(LinearLayout v) {
        Bitmap b = Bitmap.createBitmap( v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    private static File getOutputMediaFile(){
        File dir = new File(Environment.getExternalStorageDirectory(), "GeoPhoto");

        if(!dir.exists()){
            if(!dir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(dir.getPath() + File.separator + "VER_" + timeStamp + ".jpg");


        return mediaFile;
    }

    int j = 0; // variable para el metodo de ordenar ficheros por fecha
    private class ProccessPictureTask extends AsyncTask<File, Void, Void>{

        byte[] pictureData;
        ProgressDialog pd;


        public ProccessPictureTask(byte[] data){
            this.pictureData = data;
            pd = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            pd.setMessage(getString(R.string.procesando_datos));
            pd.show();
        }

        @Override
        protected Void doInBackground(File... params) {
            pd.show();

            try{
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(pictureData);
                fos.close();
            }catch (FileNotFoundException e){
                Log.i("","ERROR foto not found: " + e);
            }catch (IOException ioe){
                Log.i("","ERROR foto io exception: " + ioe);
            }

            //----- Añadir marco con metadatos y timestamp
                //----- Preparo imagen grande
            Bitmap image = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
            Bitmap mutableImage = image.copy(Bitmap.Config.ARGB_8888, true);
            Matrix matrix = new Matrix();
            Log.i("","Resolucion imagen: " + mutableImage.getWidth() + " " + mutableImage.getHeight());

            if(mutableImage.getWidth() < mutableImage.getHeight()){//Imagen de pie
                matrix.postRotate(0f);
            }else{ //Imagen tumbada
                matrix.postRotate(90f);
            }

            Bitmap mutableImageRotated = Bitmap.createBitmap(mutableImage, 0, 0, mutableImage.getWidth(),
                    mutableImage.getHeight(), matrix, true);

            Canvas comboImage = new Canvas(mutableImageRotated);

                //----- Preparo metadatos y timestamp
            Bitmap topImage = loadBitmapFromView(imageDataLayout);
            int topImageHeight = imageDataLayout.getHeight()*mutableImageRotated.getHeight()/display.getHeight();
            Log.i("", "altura - valores: " + mutableImageRotated.getHeight() + " - " +display.getHeight());
            matrix.postScale(mutableImageRotated.getWidth(), topImageHeight);
            Bitmap topImageScaled = Bitmap.createScaledBitmap(topImage, mutableImageRotated.getWidth(), topImageHeight, true);

                   //----- Pinto metadatos encima de imagen grande
            comboImage.drawBitmap(topImageScaled, 0f, 0f, null);

            OutputStream os = null;
            try{
                os = new FileOutputStream(pictureFile.getAbsolutePath());
                mutableImageRotated.compress(Bitmap.CompressFormat.JPEG, 90, os);
            }catch (IOException e){
                Log.i("", "------------------------ ERROR EN FOTO -------------------------");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
            Toast.makeText(getApplicationContext(),
                    getString(R.string.fotografia) + " " + pictureFile.getName() + " " + getString(R.string.realizada_con_exito),
                    Toast.LENGTH_SHORT).show();
            mCamera.startPreview();
            new LstGalleryTask().execute();
        }
    }

    private class LstGalleryTask extends AsyncTask<Void, Void, Void>{
        private LstGalleryTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            bitmaps.clear();
            String path = Environment.getExternalStorageDirectory().toString()
                    + File.separator + "GeoPhoto";
            File directory = new File(path);

            final long[] fileModifieDate = new long[directory.listFiles().length];

            File[] imageList = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File file = new File(dir, filename);
                    try{
                        fileModifieDate[j++] = file.lastModified();
                        Log.i("LstGalleryTask", "Fichero: "+" j: "+ j  + " " + file.getName());

                    }catch (Exception e){

                    }
                    return true;
                }
            });

            for(int i = 0; i < imageList.length; i++){
                Bitmap b = decodeSampledBitmapFromResource(getResources(),
                        imageList[i].getAbsolutePath(), 64, 32);
                bitmaps.add(new ImageDataModel(b, imageList[i].getAbsolutePath()));
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageGalleryAdapter adapter1 = new ImageGalleryAdapter(MainActivity.this, bitmaps);
                    lstGallery.setAdapter(adapter1);
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class ImageGalleryAdapter extends ArrayAdapter<ImageDataModel>{
        Activity contextActivity;
        ArrayList<ImageDataModel> images;

        private ImageGalleryAdapter(Activity context, ArrayList<ImageDataModel> images) {
            super(context, R.layout.image_row, images);
            this.contextActivity = context;
            this.images = images;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if(convertView == null){
                LayoutInflater inflater = contextActivity.getLayoutInflater();
                convertView = inflater.inflate(R.layout.image_row, null);
                holder = new ViewHolder();

                holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            holder.imageView.setImageBitmap(images.get(position).getBmp());

            return convertView;
        }
    }

    private class ViewHolder{
        ImageView imageView;
    }

    static Bitmap decodeSampledBitmapFromResource(Resources res, String filePath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    static int calculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            final int halfHeight = options.outHeight / 2;
            final int halfWidth = options.outWidth / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    public class MyLocationListener implements LocationListener{
        DecimalFormat tresDecimales = new DecimalFormat("0.000");

        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            String timeStamp = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(new Date());

            txtLatitude.setText(getString(R.string.latitud) + ": " + pasar_a_sexa(latitude));
            txtLongitude.setText(getString(R.string.longitud) + ": " + pasar_a_sexa(longitude));
            txtAltitude.setText(getString(R.string.altitud) + ": " + tresDecimales.format(altitude) + " m");
            txtTimeStamp.setText(timeStamp);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), getString(R.string.gps_activado), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), getString(R.string.gps_desactivado), Toast.LENGTH_SHORT).show();
        }
    }

    public String pasar_a_sexa (double angulo) //angulo -> angulo sexa con decimales
    {
        DecimalFormat Decimal = new DecimalFormat("0.00");
        DecimalFormat NoDecimal = new DecimalFormat("0");

        String angulo_str = Double.toString(angulo);
        String grados= quita_puntos(angulo_str);
        double angulo_entero = Double.parseDouble(grados);
        //Decimales
        double parte_decimal = angulo - angulo_entero;
        double minutos = 0;
        minutos = parte_decimal * 60; //Minutos
        String minutos_str = Double.toString(minutos);
        String minutos_enteros = quita_puntos(minutos_str);
        double minutos_ent = Double.parseDouble(minutos_enteros);

        double parte_decimal_segundos = minutos - minutos_ent;
        double segundos = 0;
        segundos = parte_decimal_segundos * 60;

        String resultado = grados + "º " + NoDecimal.format(Math.abs(minutos_ent))+ "' " + Decimal.format(Math.abs(segundos))+"''";
        return resultado;
    }

    public String quita_puntos(String numero)
    {
        String angulo_str = numero;
        String lector = "";
        String entero = "";
        int caracteres = angulo_str.length();
        for (int i = 0; i < caracteres; i++)
        {
            lector = angulo_str.substring(i, i+1);
            if (lector.contentEquals(".") || lector.contentEquals(","))
            {
                i = caracteres;
            }
            else
            {
                entero = entero + lector;
            }
        }
        return entero;
    }
}




