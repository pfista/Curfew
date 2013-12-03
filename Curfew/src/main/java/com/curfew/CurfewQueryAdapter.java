package com.curfew;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pfister on 11/24/13.
 */
public class CurfewQueryAdapter<T> extends ParseQueryAdapter {

    private final String TAG = "com.curfew.adapter";

    public CurfewQueryAdapter(Context context, QueryFactory queryFactory) {
        super(context, queryFactory);
    }

    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {

        Log.d("ADAPTER", "in item view");
        View vi = v;
        if (vi == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.curfewtextview, null);
        }

        Date dateTime = (Date)object.get("Curfew");
        DateFormat df = new SimpleDateFormat("hh:mm");

        final String time = df.format(dateTime);
        final TextView text = (TextView) vi.findViewById(R.id.curfew_item_text);
        final ImageView image = (ImageView) vi.findViewById(R.id.curfew_item_image);

        object.getParseUser("toUser").fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    ParseUser user = (ParseUser) parseObject;
                    int hour = Integer.parseInt(time.split(":")[0]);
                    int minute = Integer.parseInt(time.split(":")[1]);

                    // Create appropriate clock drawables here
                    ClockDrawable cd = new ClockDrawable(40, R.color.black);
                    cd.setTime(hour, minute, 0);
                    image.setBackground(cd);
                    text.setText(user.getString("username"));
                }
            }
        });

        return vi;
    }

}