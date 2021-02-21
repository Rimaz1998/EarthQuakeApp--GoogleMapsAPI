package com.rinzler.earthquake.ui;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.rinzler.earthquake.R;

import org.w3c.dom.Text;

//here we can create our own custom window snippet when we click the marker
public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
    private LayoutInflater inflater;
    private Context context;
    private View view;

    public CustomInfoWindow(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.custom_info_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    //we get the details using the marker and adding it to the views of the snippet
    @Override
    public View getInfoContents(Marker marker) {
        //set the views inside the info window
        TextView title = view.findViewById(R.id.winTitle);
        title.setText(marker.getTitle());

        TextView magnitude = view.findViewById(R.id.magnitude);
        magnitude.setText(marker.getSnippet());
        return view;
    }
}
