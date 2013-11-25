package com.curfew;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

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

        String time = object.getString("Curfew");
        TextView text = (TextView) vi.findViewById(R.id.curfew_item_text);
        ImageView image = (ImageView) vi.findViewById(R.id.curfew_item_image);

        ParseUser user = null;
        try {
            user = object.getParseUser("toUser").fetchIfNeeded();
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        String username = user.getString("username");

        int hour = Integer.parseInt(time.split(":")[0]);
        int minute = Integer.parseInt(time.split(":")[1]);

        // TODO: create appropriate clock drawables here
        ClockDrawable cd = new ClockDrawable(40, R.color.black);
        cd.setTime(hour, minute, 0);
        image.setBackground(cd);
        text.setText(username);

        return vi;
    }


}


