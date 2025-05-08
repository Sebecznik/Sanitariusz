package com.example.sanitariuszapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Map;

public class ContactAdapter extends BaseExpandableListAdapter {

    // ***
    // Application context and data structures for groups and children
    // ***
    private Context context;
    private Map<String, List<String>> contactsCollection;
    private List<String> groupList;

    // ***
    // Constructor: initialize adapter with group titles and corresponding contacts
    // ***
    public ContactAdapter(Context context, List<String> groupList,
                          Map<String, List<String>> contactsCollection) {
        this.context = context;
        this.groupList = groupList;
        this.contactsCollection = contactsCollection;
    }

    // ***
    // Return the number of groups
    // ***
    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    // ***
    // Return the number of children in a specific group
    // ***
    @Override
    public int getChildrenCount(int groupPosition) {
        return contactsCollection.get(groupList.get(groupPosition)).size();
    }

    // ***
    // Return the group title at given position
    // ***
    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    // ***
    // Return the child item at given group and child positions
    // ***
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return contactsCollection.get(groupList.get(groupPosition)).get(childPosition);
    }

    // ***
    // Provide stable IDs for groups and children
    // ***
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    // ***
    // Create and return the view for each group header
    // ***
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupTitle = getGroup(groupPosition).toString();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_contact_group, parent, false);
        }
        TextView txtGroup = convertView.findViewById(R.id.mobile);
        txtGroup.setTypeface(null, Typeface.BOLD);
        txtGroup.setText(groupTitle);
        txtGroup.setTextColor(ContextCompat.getColor(context, R.color.Accent));
        return convertView;
    }

    // ***
    // Create and return the view for each child item
    // ***
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String contact = getChild(groupPosition, childPosition).toString();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_contact_child, parent, false);
        }
        TextView txtChild = convertView.findViewById(R.id.model);
        txtChild.setText(contact);
        txtChild.setTextColor(ContextCompat.getColor(context, R.color.Accent));
        return convertView;
    }

    // ***
    // Make child items selectable
    // ***
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
