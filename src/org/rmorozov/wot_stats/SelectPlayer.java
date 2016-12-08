package org.rmorozov.wot_stats;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SelectPlayer extends Activity {
    public static final String Player = "org.morozov.wot_stats";
    private static final String URL_LIST_PLAYER = "/wot/account/list/?search=";
    private static final String URL_SITE = "http://api.worldoftanks.";
    private static final String account_id = "account_id";
    private static final String nickname = "nickname";
    private ArrayList<String> listPlayer;
    private String[] playerArray;
    private String[] playerIDArray;
    private ListView playerListView;
    private String[] playerServerArray;
    private MyArrayAdapter arrayAdapter;
    private boolean mListType;
    private SQLiteDatabase sdb;

    public class MyArrayAdapter extends ArrayAdapter<String> {
        private Context context;
        private ImageView deleteButton;
        private ImageView deleteButtonTank;
        protected ListView mListView;
        private TextView tipoEditText;
        private TextView tipoEditTextServer;

        public MyArrayAdapter(Context context, ListView listView, List<String> values) {
            super(context, R.layout.simple_list_select_item, values);
            tipoEditText = null;
            tipoEditTextServer = null;
            deleteButton = null;
            deleteButtonTank = null;
            this.context = context;
            mListView = listView;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.simple_list_select_item, parent, false);
            tipoEditText = (TextView) view.findViewById(R.id.textSelPlayerName);
            tipoEditTextServer = (TextView) view.findViewById(R.id.textPlayerServer);
            deleteButton = (ImageView) view.findViewById(R.id.imageViewSelDelete);
            deleteButtonTank = (ImageView) view.findViewById(R.id.imageViewTankDelete);
            tipoEditText.setText(playerArray[position]);
            tipoEditTextServer.setText(playerServerArray[position]);
            if (mListType) {
                deleteButton.setImageResource(R.drawable.button_delete);
                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = mListView.getPositionForView((View) v.getParent());
                        StringBuilder append = new StringBuilder().append("SELECT * FROM ");
                        append = append.append(DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER).append(" WHERE ");
                        append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(playerIDArray[position]).append("' AND ");
                        Cursor cursor = sdb.rawQuery(append.append(DatabaseHelper.ACTIVE).append("= 1").toString(), null);
                        cursor.moveToFirst();
                        if (cursor.getCount() > 0) {
                            Toast.makeText(SelectPlayer.this, SelectPlayer.this.getString(R.string.msg_player_delete), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        cursor.close();
                        Builder dlgAlert = new Builder(SelectPlayer.this);
                        String title = SelectPlayer.this.getString(R.string.msg_player_delete_ttl);
                        String message = SelectPlayer.this.getString(R.string.msg_player_delete_q);
                        dlgAlert.setTitle(title);
                        dlgAlert.setMessage(message);
                        dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sdb.delete(DatabaseHelper.DATABASE_TABLE_SH,
                                        DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + playerIDArray[position] + "\'", null);
                                sdb.delete(DatabaseHelper.DATABASE_TABLE_TANK_STAT,
                                        DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + playerIDArray[position] + "\'", null);
                                sdb.delete(DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST,
                                        DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + playerIDArray[position] + "\'", null);
                                sdb.delete(DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER,
                                        DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + playerIDArray[position] + "\'", null);
                                Cursor cursor = sdb.rawQuery("SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER, null);
                                if (cursor.getCount() > 0) {
                                    playerArray = new String[cursor.getCount()];
                                    playerIDArray = new String[cursor.getCount()];
                                    playerServerArray = new String[cursor.getCount()];
                                    cursor.moveToFirst();
                                    for (int i = 0; i < cursor.getCount(); i++) {
                                        playerArray[i] = cursor.getString(1);
                                        playerIDArray[i] = cursor.getString(2);
                                        playerServerArray[i] = cursor.getString(4);
                                        cursor.moveToNext();
                                    }
                                }
                                cursor.close();
                                remove(getItem(position));
                                dialog.dismiss();
                            }
                        });
                        dlgAlert.setNegativeButton(SelectPlayer.this.getString(R.string.msg_player_delete_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dlgAlert.create();
                        dlgAlert.show();
                    }
                });
                deleteButtonTank.setVisibility(View.INVISIBLE);
            }
            tipoEditText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = mListView.getPositionForView((View) v.getParent());
                    Intent answerIntent = new Intent();
                    answerIntent.putExtra(SelectPlayer.Player, playerArray[position] + "$" + playerIDArray[position] + "$" + playerServerArray[position]);
                    setResult(Activity.RESULT_OK, answerIntent);
                    finish();
                }
            });
            return view;
        }
    }

    private class PrefetchDataCurrency extends AsyncTask<String, Void, Void> {
        private ProgressDialog progressDialog;

        private PrefetchDataCurrency() {
        }

        protected Void doInBackground(String... args) {
            String name = args[0];
            String selectServer = args[1];
            ServersChangeHelper sch = new ServersChangeHelper();
            JSONArray json = new JSONParser().getJSONArrayFromUrl(SelectPlayer.URL_SITE + sch.GetZoneByServerName(selectServer) + SelectPlayer.URL_LIST_PLAYER + name.replaceAll(" ", "") + "&application_id=" + sch.GetAppIdByServerName(selectServer));
            if (json != null) {
                listPlayer = new ArrayList<>();
                playerArray = new String[json.length()];
                playerIDArray = new String[json.length()];
                playerServerArray = new String[json.length()];
                for (int i = 0; i < json.length(); i++) {
                    try {
                        JSONObject c = json.getJSONObject(i);
                        playerArray[i] = c.getString(SelectPlayer.nickname);
                        playerIDArray[i] = c.getString(SelectPlayer.account_id);
                        playerServerArray[i] = selectServer;
                        listPlayer.add(c.getString(SelectPlayer.nickname));
                    } catch (JSONException ignored) {
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (playerArray != null) {
                playerListView = (ListView) findViewById(R.id.listPlayerView);
                arrayAdapter = new MyArrayAdapter(SelectPlayer.this, playerListView, listPlayer);
                mListType = false;
                playerListView.setAdapter(arrayAdapter);
                return;
            }
            Toast.makeText(SelectPlayer.this, SelectPlayer.this.getString(R.string.err_load_data_msg), Toast.LENGTH_SHORT).show();
        }

        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SelectPlayer.this, "", SelectPlayer.this.getString(R.string.msg_wait1));
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_player_2);
        registerClickCallback();
        DatabaseHelper dbHelper = DatabaseHelper.createDatabaseHelper(this);
        sdb = dbHelper.getWritableDatabase();
        StringBuilder append = new StringBuilder().append("SELECT * FROM ");
        Cursor cursor = sdb.rawQuery(append.append(DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER).toString(), null);
        listPlayer = new ArrayList<>();
        if (cursor.getCount() > 0) {
            playerListView = (ListView) findViewById(R.id.listPlayerView);
            playerArray = new String[cursor.getCount()];
            playerIDArray = new String[cursor.getCount()];
            playerServerArray = new String[cursor.getCount()];
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                playerArray[i] = cursor.getString(1);
                playerIDArray[i] = cursor.getString(2);
                playerServerArray[i] = cursor.getString(4);
                listPlayer.add(cursor.getString(1));
                cursor.moveToNext();
            }
            arrayAdapter = new MyArrayAdapter(this, (ListView) findViewById(R.id.listPlayerView), listPlayer);
            mListType = true;
            playerListView.setAdapter(arrayAdapter);
        }
        cursor.close();
        append = new StringBuilder().append("SELECT * FROM ");
        cursor = sdb.rawQuery(append.append(DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER).append(" WHERE active = 1").toString(), null);
        if (cursor.moveToFirst()) {
            Spinner TextServer = (Spinner) findViewById(R.id.spinnerServers);
            if (cursor.getString(4).equals("RU")) {
                TextServer.setSelection(0);
            }
            if (cursor.getString(4).equals("EU")) {
                TextServer.setSelection(1);
            }
            if (cursor.getString(4).equals("US")) {
                TextServer.setSelection(2);
            }
            if (cursor.getString(4).equals("SEA")) {
                TextServer.setSelection(3);
            }
            if (cursor.getString(4).equals("RK")) {
                TextServer.setSelection(4);
            }
        }
    }

    private void registerClickCallback() {
        ((ListView) findViewById(R.id.listPlayerView)).setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                Intent answerIntent = new Intent();
                answerIntent.putExtra(SelectPlayer.Player, textView.getText().toString() + "$" + playerIDArray[position] + "$" + playerServerArray[position]);
                setResult(Activity.RESULT_OK, answerIntent);
                finish();
            }
        });
    }

    public void buttonSearchPlayerClick(View v) {
        EditText playerName = (EditText) findViewById(R.id.editPlayerName);
        Spinner textServer = (Spinner) findViewById(R.id.spinnerServers);
        String[] choose = getResources().getStringArray(R.array.servers);
        String serverName = choose[textServer.getSelectedItemPosition()];
        if (playerName.getText().length() > 0) {
            new PrefetchDataCurrency().execute(playerName.getText().toString(), serverName);
        } else {
            Toast.makeText(this, getString(R.string.ttl_select_player), Toast.LENGTH_SHORT).show();
        }
    }
}
