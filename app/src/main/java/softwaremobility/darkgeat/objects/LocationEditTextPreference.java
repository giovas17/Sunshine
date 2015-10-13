package softwaremobility.darkgeat.objects;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

import softwaremobility.darkgeat.sunshine.R;

/**
 * Created by darkgeat on 10/12/15.
 */
public class LocationEditTextPreference extends EditTextPreference{

    static private final int DEFAULT_MINIMUM_LENGTH = 5;
    private int minLength;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LocationEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0,0);
        try {
            minLength = array.getInteger(R.styleable.LocationEditTextPreference_min_length,DEFAULT_MINIMUM_LENGTH);
        }finally {
            array.recycle();
        }
    }

    public LocationEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0,0);
        try {
            minLength = array.getInteger(R.styleable.LocationEditTextPreference_min_length,DEFAULT_MINIMUM_LENGTH);
        }finally {
            array.recycle();
        }
    }

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0,0);
        try {
            minLength = array.getInteger(R.styleable.LocationEditTextPreference_min_length,DEFAULT_MINIMUM_LENGTH);
        }finally {
            array.recycle();
        }
    }

    public LocationEditTextPreference(Context context) {
        super(context);

    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog dialog = getDialog();
                if(dialog instanceof AlertDialog){
                    AlertDialog alertDialog = (AlertDialog)dialog;
                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if(s.length() < minLength){
                        //Disable OK button
                        positiveButton.setEnabled(false);
                    }else{
                        //Enable OK button
                        positiveButton.setEnabled(true);
                    }
                }
            }
        });

    }
}
