package com.manvir.SnapColors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EditGroups extends ActionBarActivity {
    static ListView groupUsers;
    static ArrayList<String> usersList;
    static private SharedPreferences groupsPref;
    private static EditGroupsFragment groupsListFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editgroups);
        groupsPref = getSharedPreferences("groups", Context.MODE_WORLD_READABLE);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new EditGroupsFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editgroups_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()){
            case R.id.addGroup:
                final List<String> useresToWrite = new ArrayList<>();
                final Dialog editGroupsDialog = new Dialog(EditGroups.this);
                editGroupsDialog.setTitle("Add Group");
                editGroupsDialog.setContentView(R.layout.editgroup_dialog_layout);

                final EditText groupName = (EditText) editGroupsDialog.findViewById(R.id.groupName);
                groupName.setHint("Best Friends");
                groupName.setImeOptions(EditorInfo.IME_ACTION_DONE);

                groupUsers = (ListView) editGroupsDialog.findViewById(R.id.groupUsers);
                groupUsers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                Button cancel = new Button(EditGroups.this);
                Button done = new Button(EditGroups.this);
                cancel.setText("Cancel");
                done.setText("Done");

                groupUsers.addFooterView(cancel);
                groupUsers.addFooterView(done);

                UsersListAdapter usersListAdapter = new UsersListAdapter(EditGroups.this, usersList);
                groupUsers.setAdapter(usersListAdapter);

                groupUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        CheckedTextView userCheckBox = (CheckedTextView) view;
                        String username = usersList.get(position);

                        if (!userCheckBox.isChecked()) {
                            Log.d("SnapColors", "Remove user: " + username);
                            useresToWrite.remove(username);
                        } else {
                            Log.d("SnapColors", "Add user: " + username);
                            useresToWrite.add(username);
                            Log.d("SnapColors", useresToWrite.toString());
                        }
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editGroupsDialog.dismiss();
                        useresToWrite.clear();
                    }
                });

                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (groupName.getText().toString().equals(" ") || groupName.getText().toString().equals("") || groupName.getText().toString().matches("")) {
                            Toast.makeText(EditGroups.this, "You must give your group a name!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (useresToWrite.size() <= 1){
                            Toast.makeText(EditGroups.this, "You must select at least two friends!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String[] users = useresToWrite.toArray(new String[useresToWrite.size()]);
                        for(Map.Entry<String, ?> entry : groupsPref.getAll().entrySet()){
                            if (entry.getKey().equals(groupName.getText().toString())){
                                Toast.makeText(EditGroups.this, "Group with name \""+groupName.getText().toString()+"\" already exists", Toast.LENGTH_LONG).show();
                            }
                        }
                        StringBuilder sb = new StringBuilder();
                        //noinspection ForLoopReplaceableByForEach
                        for (int i = 0; i < users.length; i++) {
                                sb.append(users[i]).append(",");
                        }
                        groupsPref.edit().putString(groupName.getText().toString(), sb.toString()).commit();
                        Log.d("SnapColors", "Wrote group with name " + groupName.getText().toString()+" and with users "+ sb.toString());
                        editGroupsDialog.dismiss();

                        //Refresh groups
                        ArrayList<Group> groups = new ArrayList<Group>();
                        for(Map.Entry<String, ?> entry : groupsPref.getAll().entrySet()){
                            groups.add(new Group(entry.getKey(), entry.getValue().toString().split(",")));
                        }
                        groupsListFrag.setListAdapter(new EditGroupsListAdapter(EditGroups.this, groups));

                        useresToWrite.clear();
                    }
                });

                editGroupsDialog.setCancelable(false);
                editGroupsDialog.show();
                break;
        }
        return true;
    }

    public static class EditGroupsFragment extends ListFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            groupsListFrag = this;
            usersList = getActivity().getIntent().getStringArrayListExtra("users");

            ArrayList<Group> groups = new ArrayList<Group>();

            for(Map.Entry<String, ?> entry : groupsPref.getAll().entrySet()){
                groups.add(new Group(entry.getKey(), entry.getValue().toString().split(",")));
            }

            setListAdapter(new EditGroupsListAdapter(getActivity(), groups));
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            final List<String> useresToWrite = new ArrayList<>();
            final Group group = (Group)getListAdapter().getItem(position);

            final Dialog editGroupsDialog = new Dialog(getActivity());
            editGroupsDialog.setTitle("Edit Group");
            editGroupsDialog.setContentView(R.layout.editgroup_dialog_layout);

            final EditText groupName = (EditText) editGroupsDialog.findViewById(R.id.groupName);
            groupName.setImeOptions(EditorInfo.IME_ACTION_DONE);
            groupName.setText(group.name);

            groupUsers = (ListView) editGroupsDialog.findViewById(R.id.groupUsers);
            groupUsers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            Button delete = new Button(getActivity());
            Button cancel = new Button(getActivity());
            Button done = new Button(getActivity());
            delete.setText("Delete");
            cancel.setText("Cancel");
            done.setText("Done");

            groupUsers.addFooterView(delete);
            groupUsers.addFooterView(cancel);
            groupUsers.addFooterView(done);

            UsersListAdapter usersListAdapter = new UsersListAdapter(getActivity(), usersList);
            groupUsers.setAdapter(usersListAdapter);

            //Checks the users in the list based on users who have been already added
            for (int i = 0; i < (groupUsers.getCount()-groupUsers.getFooterViewsCount()); i++) {
                for (String s : group.users) {
                    if (usersList.get(i).equals(s)){
                        useresToWrite.add(usersList.get(i));
                        groupUsers.setItemChecked(i, true);
                    }
                }
            }

            groupUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckedTextView userCheckBox = (CheckedTextView)view;
                    String username = usersList.get(position);

                    if(!userCheckBox.isChecked()){
                        Log.d("SnapColors", "Remove user: " + username);
                        useresToWrite.remove(username);
                    }else {
                        Log.d("SnapColors", "Add user: " + username);
                        useresToWrite.add(username);
                        Log.d("SnapColors", useresToWrite.toString());
                    }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder areYouSure = new AlertDialog.Builder(getActivity());
                    areYouSure.setTitle("Delete \"" +group.name+"\" ?");
                    areYouSure.setMessage("Are you sure?");

                    areYouSure.setNegativeButton("No", null);
                    areYouSure.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("SnapColors", "Deleting group with name \"" + group.name + "\"");
                            removeGroup(group.name);
                            editGroupsDialog.dismiss();
                            useresToWrite.clear();
                            refreshGroups();
                        }
                    });

                    areYouSure.show();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editGroupsDialog.dismiss();
                    useresToWrite.clear();
                }
            });

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (groupName.getText().toString().equals(" ") || groupName.getText().toString().equals("") || groupName.getText().toString().matches("")) {
                        Toast.makeText(getActivity(), "You must give your group a name!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (useresToWrite.size() <= 1){
                        Toast.makeText(getActivity(), "You must select at least two friends!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    writeGroup(groupName.getText().toString(), useresToWrite.toArray(new String[useresToWrite.size()]));
                    editGroupsDialog.dismiss();
                    useresToWrite.clear();
                    refreshGroups();
                }
            });

            editGroupsDialog.setCancelable(false);
            editGroupsDialog.show();
        }

        private void writeGroup(String groupName, String[] users) {
            StringBuilder sb = new StringBuilder();
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < users.length; i++) {
                sb.append(users[i]).append(",");
            }
            groupsPref.edit().putString(groupName, sb.toString()).commit();
            Log.d("SnapColors", "Wrote group with name " + groupName+" and with users "+ sb.toString());
        }

        private void removeGroup(String groupName) {
            groupsPref.edit().remove(groupName).commit();
        }

        private void refreshGroups() {
            ArrayList<Group> groups = new ArrayList<Group>();

            for(Map.Entry<String, ?> entry : groupsPref.getAll().entrySet()){
                groups.add(new Group(entry.getKey(), entry.getValue().toString().split(",")));
            }

            setListAdapter(new EditGroupsListAdapter(getActivity(), groups));
        }
    }

    static class UsersListAdapter extends ArrayAdapter<String> {
        public UsersListAdapter(Context context, ArrayList<String> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            }
            CheckedTextView userView = (CheckedTextView) convertView;
            userView.setText(usersList.get(position));
            return convertView;
        }
    }

    static class EditGroupsListAdapter extends ArrayAdapter<Group> {
        public EditGroupsListAdapter(Context context, ArrayList<Group> groups) {
            super(context, 0, groups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_item_layout, parent, false);
            }

            Group group = getItem(position);

            TextView name = (TextView) convertView.findViewById(R.id.name);
            name.setText("Name: "+group.name);

            TextView users = (TextView) convertView.findViewById(R.id.users);
            users.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            users.setText("Friends: "+group.getUsersAsString());

            return convertView;
        }
    }

    static private class Group {
        String name;
        String[] users;
        private Group(String name, String[] users){
            this.name = name;
            this.users = users;
        }

        public String getUsersAsString(){
            return Arrays.toString(users).replace("[", "").replace("]", "");
        }
    }
}