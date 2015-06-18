package ru.neal.dimbler.propusk;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;



public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    final String LOG_TAG = "PropuskLog";

    final public String save_url_str[] = new String[] {"https://propusk.neal.ru/save_pedestrian.php", "https://propusk.neal.ru/save_auto.php"};

    ExpandableListView elvMain;
    AdapterHelper ah;
    SimpleExpandableListAdapter adapter;
    Context context;

    private Timer myTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this.getApplicationContext();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ah = new AdapterHelper(this);
        adapter = ah.getAdapter();

        elvMain = (ExpandableListView) findViewById(R.id.elvMain);
        elvMain.setAdapter(adapter);

        // нажатие на элемент
        elvMain.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Log.d(LOG_TAG, "onChildClick groupPosition = " + groupPosition +
                        " childPosition = " + childPosition +
                        " id = " + id + " groupchildtext=" + ah.getGroupChildText(groupPosition, childPosition));

                new GetSQLData(context).execute(new String[]{save_url_str[groupPosition], ah.getChildText(groupPosition, childPosition)});


                ah.CreateCollection();
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        // разворачивание группы
        elvMain.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            public void onGroupExpand(int groupPosition) {
                Log.d(LOG_TAG, "onGroupExpand groupPosition = " + groupPosition);
                ah.CreateCollection();
                adapter.notifyDataSetChanged();
            }
        });

        // разворачиваем группы
        elvMain.expandGroup(0);
        elvMain.expandGroup(1);
        }


    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            //This method runs in the same thread as the UI.
            ah.CreateCollection();
            adapter.notifyDataSetChanged();
            //Do something to the UI thread here
        }
    };

    //Создаем таймер обновления коллекции каждые 60 секунд работы
    @Override
    protected void onResume(){
        super.onResume();
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 0, 60000);
        Log.d(LOG_TAG, "Create timer at 60 seconds");
    }

    @Override
    protected void onStop() {
        super.onStop();
        myTimer.cancel();
        Log.d(LOG_TAG, "MainActivity: onStop()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, 1, 0, R.string.refresh);
        menu.add(0, 2, 0, R.string.exit);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d("Checked menu: ", String.valueOf(id));

        //noinspection SimplifiableIfStatement
        if (id == 1) {
            ah.CreateCollection();
            adapter.notifyDataSetChanged();
            return true;
        }
        if (id == 2) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }
}
