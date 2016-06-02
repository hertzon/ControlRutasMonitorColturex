package com.coltrack.controlrutasmonitor;

/**
 * Created by Nelson Rodriguez on 31/05/2016.
 */
import java.util.List;

//import com.danielme.blog.demo.listviewcheckbox.R;
import com.coltrack.controlrutasmonitor.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Custom adapter - "View Holder Pattern".
 *
 * @author danielme.com
 *
 */
public class CustomArrayAdapter extends ArrayAdapter<Row> implements
        View.OnClickListener {

    private LayoutInflater layoutInflater;
    static SQLiteDatabase dbb;
    //public int n[];

    public CustomArrayAdapter(Context context, List<Row> objects) {
        super(context, 0, objects);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // holder pattern
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();

            convertView = layoutInflater.inflate(R.layout.listview_row, parent, false);
            holder.setTextViewTitle((TextView) convertView
                    .findViewById(R.id.textViewTitle));
            holder.setTextViewSubtitle((TextView) convertView
                    .findViewById(R.id.textViewSubtitle));
            holder.setCheckBox((CheckBox) convertView
                    .findViewById(R.id.checkBox));
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        final Row row = getItem(position);
        holder.getTextViewTitle().setText(row.getTitle());
        holder.getTextViewSubtitle().setText(row.getSubtitle());
        holder.getCheckBox().setTag(position);
        holder.getCheckBox().setChecked(row.isChecked());
        holder.getCheckBox().setOnClickListener(this);

        changeBackground(getContext(), holder.getCheckBox());

        return convertView;
    }


    @Override
    public void onClick(View v) {

        CheckBox checkBox = (CheckBox) v;
        int position = (Integer) v.getTag();
        getItem(position).setChecked(checkBox.isChecked());

        changeBackground(CustomArrayAdapter.this.getContext(), checkBox);
        String msg = this.getContext().getString(R.string.check_toast,
                position, checkBox.isChecked());
        //Toast.makeText(this.getContext(),"msg: "+ msg, Toast.LENGTH_SHORT).show();

//
//        dbb=SQLiteDatabase.openOrCreateDatabase()Database("controlRutas", null, getContext().MODE_PRIVATE);
//
//        dbb.execSQL("INSERT INTO "
//                + "posEnviar"
//                + " (n)"
//                + " VALUES ('"+position+"')");
//
//
//
//
//        dbb.close();


    }


    /**
     * Set the background of a row based on the value of its checkbox value.
     * Checkbox has its own style.
     */
    @SuppressWarnings("deprecation")
    private void changeBackground(Context context, CheckBox checkBox) {
        View row = (View) checkBox.getParent();
        Drawable drawable = context.getResources().getDrawable(
                R.drawable.listview_selector_checked);
        if (checkBox.isChecked()) {
            drawable = context.getResources().getDrawable(
                    R.drawable.listview_selector_checked);
        } else {
            drawable = context.getResources().getDrawable(
                    R.drawable.listview_selector);
        }
        row.setBackgroundDrawable(drawable);
    }

    static class Holder {
        TextView textViewTitle;
        TextView textViewSubtitle;
        CheckBox checkBox;

        public TextView getTextViewTitle() {
            return textViewTitle;
        }

        public void setTextViewTitle(TextView textViewTitle) {
            this.textViewTitle = textViewTitle;
        }

        public TextView getTextViewSubtitle() {
            return textViewSubtitle;
        }

        public void setTextViewSubtitle(TextView textViewSubtitle) {
            this.textViewSubtitle = textViewSubtitle;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }

    }

}