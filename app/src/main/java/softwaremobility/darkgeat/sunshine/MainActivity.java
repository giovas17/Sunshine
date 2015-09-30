package softwaremobility.darkgeat.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import softwaremobility.darkgeat.sunshine.sync.SyncAdapter;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback{

    private String mLocation;
    private boolean mTwoPane;
    public static final String DETAILFRAGMENT_TAG = "DetailFragmentTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}
