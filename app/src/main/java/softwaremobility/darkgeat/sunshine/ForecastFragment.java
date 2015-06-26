package softwaremobility.darkgeat.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    ArrayAdapter<String> adapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        List<String> weekForecast = new ArrayList<>(Arrays.asList(getActivity().getResources().getStringArray(R.array.lista)));

        adapter = new ArrayAdapter<>(getActivity(),R.layout.list_item_forecast,weekForecast);
        ListView list = (ListView) v.findViewById(R.id.listview_forecast);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dayInfo = adapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, dayInfo);
                startActivity(intent);
            }
        });

        return v;
    }

    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forescastJSONString = null;
            String zipCode = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;

            String[] rows = null;

            if(params.length == 0)
                return null;
            else
                zipCode = params[0];

            try {

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,zipCode)
                        .appendQueryParameter(FORMAT_PARAM,format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                        .build();

                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                Log.d(LOG_TAG,builtUri.toString());

                InputStream is = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(is == null){
                    //Nothing to do
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }
                if(buffer.length() == 0)
                    return null;
                forescastJSONString = buffer.toString();
                Log.d(LOG_TAG,forescastJSONString);

                WeatherDataParser dataParser = new WeatherDataParser();
                rows = dataParser.getWeatherDataFromJSON(forescastJSONString, numDays);
            } catch (IOException e) {
                Log.e(LOG_TAG,"Error ",e);
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try {
                        reader.close();
                    }catch (IOException e){
                        Log.e(LOG_TAG,"Error Closing Stream",e);
                    }
                }
            }
            return rows;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String[] data) {
            if(data != null){
                adapter.clear();
                for(String dayInfo : data)
                    adapter.add(dayInfo);
            }
        }

        public class WeatherDataParser {

            final String LOG_TAG = this.getClass().getSimpleName();
            final String TAG_LIST = "list";
            final String TAG_TEMP = "temp";
            final String TAG_MAX = "max";
            final String TAG_MIN = "min";
            final String TAG_WEATHER = "weather";
            final String TAG_DESCRIPTION = "main";

            /**
             * Given a string of the form returned by the api call:
             * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
             * retrieve the maximum temperature for the day indicated by dayIndex
             * (Note: 0-indexed, so 0 would refer to the first day).
             */
            public double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
                    throws JSONException {

                JSONObject obj = new JSONObject(weatherJsonStr);
                JSONArray list = obj.getJSONArray(TAG_LIST);
                JSONObject dayTemperature = list.getJSONObject(dayIndex);
                JSONObject temperature = dayTemperature.getJSONObject(TAG_TEMP);
                double max = temperature.getDouble(TAG_MAX);
                return max;
            }

            /**
             * Prepare the weather high/lows for presentation.
             */
            private String formatHighLows(double high, double low) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String unitType = preferences.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));

                if(unitType.equalsIgnoreCase(getString(R.string.pref_units_imperial))){
                    high = (high * 1.8) + 32;
                    low = (low * 1.8) + 32;
                }else if(!unitType.equalsIgnoreCase(getString(R.string.pref_units_metric))){
                    Log.e(LOG_TAG,"Unit Type not Found");
                }

                // For presentation, assume the user doesn't care about tenths of a degree.
                long roundedHigh = Math.round(high);
                long roundedLow = Math.round(low);

                String highLowStr = roundedHigh + "/" + roundedLow;
                return highLowStr;
            }


            /** The date/time conversion code is going to be moved outside the asynctask later,
             * so for convenience we're breaking it out into its own method now.
             **/
            private String getReadableDateString(long time){
                // Because the API returns a unix timestamp (measured in seconds),
                // it must be converted to milliseconds in order to be converted to valid date.
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(time);
            }

            /**
             * Take the String representing the complete forecast in JSON Format and
             * pull out the data we need to construct the Strings needed for the wireframes.
             *
             **/
            private String[] getWeatherDataFromJSON(String JSONStr, int numDays) throws JSONException{

                JSONObject obj = new JSONObject(JSONStr);
                JSONArray list = obj.getJSONArray(TAG_LIST);

                Time daytime = new Time();
                daytime.setToNow();

                int julianStartDay = Time.getJulianDay(System.currentTimeMillis(),daytime.gmtoff);
                daytime = new Time();

                String[] resultStrs = new String[numDays];

                for(int i = 0 ; i < list.length() ; i++){
                    // For now, using the format "Day, description, hi/low"
                    String day;
                    String description;
                    String highAndLow;

                    // Get the JSON object representing the day
                    JSONObject dayForecast = list.getJSONObject(i);

                    // The date/time is returned as a long.  We need to convert that
                    // into something human-readable, since most people won't read "1400356800" as
                    // "this saturday".
                    long dateTime;
                    // Cheating to convert this to UTC time, which is what we want anyhow
                    dateTime = daytime.setJulianDay(julianStartDay+i);
                    day = getReadableDateString(dateTime);

                    // description is in a child array called "weather", which is 1 element long.
                    JSONObject weatherObject = dayForecast.getJSONArray(TAG_WEATHER).getJSONObject(0);
                    description = weatherObject.getString(TAG_DESCRIPTION);

                    // Temperatures are in a child object called "temp".  Try not to name variables
                    // "temp" when working with temperature.  It confuses everybody.
                    JSONObject temperatureObject = dayForecast.getJSONObject(TAG_TEMP);
                    double high = temperatureObject.getDouble(TAG_MAX);
                    double low = temperatureObject.getDouble(TAG_MIN);

                    highAndLow = formatHighLows(high, low);
                    resultStrs[i] = day + " - " + description + " - " + highAndLow;

                }

                for (String s : resultStrs) {
                    Log.v(LOG_TAG, "Forecast entry: " + s);
                }
                return resultStrs;


            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragmentmenu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_refresh){
            updateData();
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateData();
    }

    private void updateData() {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = preferences.getString(getString(R.string.pref_location),getString(R.string.default_id_location));
        weatherTask.execute(location);
    }
}
