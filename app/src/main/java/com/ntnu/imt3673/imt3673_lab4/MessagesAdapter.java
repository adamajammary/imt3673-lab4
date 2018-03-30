package com.ntnu.imt3673.imt3673_lab4;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Messages Adapter
 */
class MessagesAdapter extends ArrayAdapter<String> {

    private final Activity activity;
    private final int      listItemLayoutId;

    /**
     * Messages Adapter
     * @param context Current activity context
     * @param listItemLayoutId Resource ID of the list item layout
     */
    public MessagesAdapter(final Context context, final int listItemLayoutId) {
        super(context, listItemLayoutId);

        this.activity         = (Activity)context;
        this.listItemLayoutId = listItemLayoutId;
    }

    /**
     * Returns a view for the list row item.
     */
    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull final ViewGroup container) {
        if (convertView == null)
            convertView = this.activity.getLayoutInflater().inflate(this.listItemLayoutId, container, false);

        String   row     = this.getItem(position);
        TextView message = convertView.findViewById(R.id.tv_message_content);

        if ((row != null) && (message != null))
            message.setText(row);

        return convertView;
    }

}
