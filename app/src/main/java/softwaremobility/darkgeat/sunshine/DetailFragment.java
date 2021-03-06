package softwaremobility.darkgeat.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import softwaremobility.darkgeat.objects.WindSpeedControl;
import softwaremobility.darkgeat.sunshine.data.WeatherContract;

/**
 * Created by darkgeat on 7/07/15.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private String dataDay;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHtAG = " #SunshineApp";
    private ShareActionProvider mShareActionProvider;
    private static String mForecast;
    static final String DETAIL_URI = "URI";
    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
        WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
        WeatherContract.WeatherEntry.COLUMN_DATE,
        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
        WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
        WeatherContract.WeatherEntry.COLUMN_PRESSURE,
        WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
        WeatherContract.WeatherEntry.COLUMN_DEGREES,
        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;
    private WindSpeedControl mSpeedControl;
    private Uri mUri;

    public DetailFragment(){
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) root.findViewById(R.id.detail_icon_weather);
        mDateView = (TextView) root.findViewById(R.id.detail_month_text);
        mFriendlyDateView = (TextView) root.findViewById(R.id.detail_day_text);
        mDescriptionView = (TextView) root.findViewById(R.id.detail_desc_text);
        mHighTempView = (TextView) root.findViewById(R.id.detail_max_temp);
        mLowTempView = (TextView) root.findViewById(R.id.detail_min_temp);
        mHumidityView = (TextView) root.findViewById(R.id.detail_humidity_text);
        mWindView = (TextView) root.findViewById(R.id.detail_wind_text);
        mPressureView = (TextView) root.findViewById(R.id.detail_pressure_text);
        mSpeedControl = (WindSpeedControl) root.findViewById(R.id.SpeedControl);
        return root;
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, dataDay + FORECAST_SHARE_HASHtAG);
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.detailfragmentmenu, menu);

        MenuItem share = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(share);

        if (mShareActionProvider != null && mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mUri != null) {

            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        ViewParent viewParent = getView().getParent();
        if(viewParent instanceof CardView){
            ((View)viewParent).setVisibility(View.INVISIBLE);
        }
        return null;

    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (data != null && data.moveToFirst()) {

            ViewParent viewParent = getView().getParent();
            if(viewParent instanceof CardView){
                ((View)viewParent).setVisibility(View.VISIBLE);
            }

            int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            Utility.loadImageFromIconPack(weatherId,getActivity(),mIconView);
            /*Glide.with(this).load(Utility.getAnimationResourceForWeatherCondition(weatherId))
                    .asGif().fitCenter().placeholder(Utility.getArtResourceForWeatherCondition(weatherId,getActivity()))
                    .error(Utility.getArtResourceForWeatherCondition(weatherId,getActivity())).into(mIconView);*/
            //mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            long date = data.getLong(COL_WEATHER_DATE);
            String dateString = Utility.getDayName(getActivity(), date);
            String dayMonthText = Utility.getFormattedMonthDay(getActivity(), date);

            mFriendlyDateView.setText(dateString);
            mDateView.setText(dayMonthText);

            String weatherDescription = data.getString(COL_WEATHER_DESC);
            mDescriptionView.setText(weatherDescription);

            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(getActivity(),
                    data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
            mHighTempView.setText(high);

            String low = Utility.formatTemperature(getActivity(),
                    data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
            mLowTempView.setText(low);

            String humidity = getString(R.string.format_humidity,
                    data.getFloat(COL_WEATHER_HUMIDITY));
            mHumidityView.setText(humidity);

            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            String sPressure = getString(R.string.format_pressure, pressure);
            mPressureView.setText(sPressure);

            float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirection = data.getFloat(COL_WEATHER_DEGREES);
            String wind = Utility.getFormattedWind(getActivity(), windSpeed, windDirection);

            mSpeedControl.setWindSpeedDirection(Utility.getWindDirection(windDirection));

            mWindView.setText(wind);
            mForecast = String.format("%s - %s/%s", weatherDescription, high, low);
            dataDay = getString(R.string.format_sharing_weather,dateString + ", " + dayMonthText,mForecast);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    public void onLocationChanged(String newLocation) {
        Uri uri = mUri;
        if( uri != null ){
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation,date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER,null,this);
        }
    }
}
