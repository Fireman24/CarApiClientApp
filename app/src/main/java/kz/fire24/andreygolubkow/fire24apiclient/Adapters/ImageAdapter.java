package kz.fire24.andreygolubkow.fire24apiclient.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import kz.fire24.andreygolubkow.fire24apiclient.Models.FireImage;
import kz.fire24.andreygolubkow.fire24apiclient.R;

/**
 * Created by andreygolubkow on 22.09.2017.
 */

public class ImageAdapter extends ArrayAdapter<FireImage> {

    private LayoutInflater inflater;
    private int layout;
    private List<FireImage> states;

    public ImageAdapter(Context context, int resource, List<FireImage> states) {

        super(context, resource, states);
        this.states = states;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View view = inflater.inflate(this.layout, parent, false);

        TextView nameView = (TextView) view.findViewById(R.id.imageName);

        FireImage state = states.get(position);

        nameView.setText(state.Name);

        return view;
    }
}
