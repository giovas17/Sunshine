package softwaremobility.darkgeat.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.sql.Connection;

import softwaremobility.darkgeat.sunshine.sync.SyncAdapter;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback{

    private String mLocation;
    private boolean mTwoPane;
    public static final String DETAILFRAGMENT_TAG = "DetailFragmentTAG";
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String SENDER_ID;
    private GoogleCloudMessaging mGcm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SENDER_ID = getString(R.string.projectIdNumber);

        if(findViewById(R.id.detail_container) != null){
            mTwoPane = true;
            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new DetailFragment(),DETAILFRAGMENT_TAG)
                        .commit();
            }
        }else {
            mTwoPane = false;
        }
        SyncAdapter.initializeSyncAdapter(this);
        if (checkPlayServices()) {
            mGcm = GoogleCloudMessaging.getInstance(this);
            String regId = getRegistrationId(this);

            if (!SENDER_ID.equals(getString(R.string.projectIdNumber))) {
                new AlertDialog.Builder(this)
                        .setTitle("Needs Sender ID")
                        .setMessage("GCM will not function in Sunshine until you replace your Sender ID with a Sender ID from the Google Developers Console.")
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show();
            } else if (regId.isEmpty()) {
                registerInBackground(this);
            }
        } else {
            Log.i(LOG_TAG, "No valid Google Play Services APK. Weather alerts will be disabled.");
            // Store regID as null
            storeRegistrationId(this, null);
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }else {
                Log.i(LOG_TAG,"This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }/*else if(id == R.id.action_map){
            openPreferredLocationInMap();
        }*/

        return super.onOptionsItemSelected(item);
    }

    /*private void openPreferredLocationInMap() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String location = preferences.getString(getString(R.string.pref_location_key), getString(R.string.default_id_location));
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q",location)
                .build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        startActivity(intent);
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreffrerredLocation(this);
        if(location != null && !location.equals(mLocation)){
            ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.IdForecastFragment);
            if( null != forecastFragment){
                forecastFragment.onLocationChanged();
            }
            DetailFragment detailFragment = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if( null != detailFragment){
                detailFragment.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if(mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI,dateUri);
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_container,fragment,DETAILFRAGMENT_TAG).commit();
        }else {
            Intent intent = new Intent(this,DetailActivity.class);
            intent.setData(dateUri);
            startActivity(intent);
        }
    }

    private void registerInBackground(final Context context){
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                String msg = "";
                try {
                    if (mGcm == null){
                        mGcm = GoogleCloudMessaging.getInstance(context);
                    }
                    String regId = mGcm.register(SENDER_ID);
                    msg = "Device registered, registration ID = " + regId;

                    storeRegistrationId(context,regId);
                    Log.d(LOG_TAG, msg);
                }catch (IOException e){}
                return null;
            }
        }.execute();
    }

    private String getRegistrationId(Context context){
        final SharedPreferences preferences = getGCMPreferences();
        String registrationId = preferences.getString(PROPERTY_REG_ID,"");
        if (registrationId.isEmpty()){
            Log.i(LOG_TAG,"GCM Registration not Found");
            return "";
        }
        int registeredVersion = preferences.getInt(PROPERTY_APP_VERSION,Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion){
            Log.i(LOG_TAG,"App version changed.");
            return "";
        }
        return registrationId;
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences preferences = getGCMPreferences();
        int appVersion = getAppVersion(context);
        Log.i(LOG_TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROPERTY_REG_ID,regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private SharedPreferences getGCMPreferences() {
        // Sunshine persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you. Just make sure
        // that it is private!
        return getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE);
    }
}
