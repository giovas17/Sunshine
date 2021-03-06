package softwaremobility.darkgeat.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import softwaremobility.darkgeat.sunshine.data.WeatherContract;

/**
 * Created by darkgeat on 1/07/15.
 */
public class ForeCastAdapter extends CursorAdapter {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    public ForeCastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        if(!MainActivity.mTwoPane)
            return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
        else
            return VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = (viewType == VIEW_TYPE_TODAY) ? R.layout.list_item_forecast_today : R.layout.list_item_forecast;
        View view = LayoutInflater.from(mContext).inflate(layoutId,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType){
            case VIEW_TYPE_TODAY:{
                Utility.loadImageFromIconPack(cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID),context,viewHolder.iconWeather);
                /*Glide.with(context).load(Utility.getAnimationResourceForWeatherCondition(
                        cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)))
                        .asGif()
                        .placeholder(Utility.getArtResourceForWeatherCondition(
                                cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)))
                        .error(Utility.getArtResourceForWeatherCondition(
                                cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)))
                        .fitCenter()
                        .into(viewHolder.iconWeather);*/
                //viewHolder.iconWeather.setImageResource(Utility.getArtResourceForWeatherCondition(
                //        cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                viewHolder.iconWeather.setImageResource(Utility.getIconResourceForWeatherCondition(
                        cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                break;
            }
        }

        viewHolder.tday.setText(Utility.getFriendlyDayString(context, dateInMillis));

        boolean isMetric = Utility.isMetric(context);
        double hight = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);

        viewHolder.tmax.setText(Utility.formatTemperature(context,hight,isMetric));

        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.tmin.setText(Utility.formatTemperature(context,low,isMetric));

        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.description.setText(description);
    }

    static class ViewHolder {
        public final TextView tday;
        public final TextView tmax;
        public final TextView tmin;
        public final ImageView iconWeather;
        public final TextView description;

        public ViewHolder(View view){
            iconWeather = (ImageView)view.findViewById(R.id.imageIconWeather);
            tday = (TextView) view.findViewById(R.id.textDay);
            description = (TextView) view.findViewById(R.id.textWeather);
            tmax = (TextView) view.findViewById(R.id.textMaxTemp);
            tmin = (TextView) view.findViewById(R.id.textMinTemp);
        }

    }

}
