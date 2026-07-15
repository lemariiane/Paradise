package com.example.paradise.bebe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.paradise.R;
import java.util.List;

public class BebeSpinnerAdapter extends ArrayAdapter<BebeModel> {

    public BebeSpinnerAdapter(Context context, List<BebeModel> bebes) {
        super(context, 0, bebes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return criarView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return criarView(position, convertView, parent);
    }

    private View criarView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        BebeModel bebe = getItem(position);

        if (bebe != null) {
            textView.setText(bebe.getNome());
        }

        return convertView;
    }
}