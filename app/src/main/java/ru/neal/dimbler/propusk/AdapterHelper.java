package ru.neal.dimbler.propusk;

/**
 * Created by dimbler on 17.06.2015.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.util.Log;
import android.widget.SimpleExpandableListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AdapterHelper {

    final String LOG_TAG = "PropuskLog";

    final String ATTR_GROUP_NAME= "groupName";
    final String ATTR_POSETITEL_NAME= "posetitel";
    final String ATTR_ID="posetitel_id";
    final String url_str[] = new String[] {"https://propusk.neal.ru/view_pedestrian_andro.php", "https://propusk.neal.ru/view_auto_andro.php"};

    // коллекция для групп
    ArrayList<Map<String, String>> groupData;

    // коллекция для элементов одной группы
    ArrayList<Map<String, String>> childDataItem;

    // общая коллекция для коллекций элементов
    ArrayList<ArrayList<Map<String, String>>> childData;
    // в итоге получится childData = ArrayList<childDataItem>

    // список аттрибутов группы или элемента
    Map<String, String> m;

    Context ctx;

    AdapterHelper(Context _ctx) {
        ctx = _ctx;
    }

    SimpleExpandableListAdapter adapter;

    public void CreateCollection() {
        // создаем коллекцию для коллекций элементов
        childData = new ArrayList<ArrayList<Map<String, String>>>();

        for (String cur_url: url_str) {

            // создаем коллекцию элементов для первой группы
            childDataItem = new ArrayList<Map<String, String>>();
            try {

                JSONObject jsonFromSQL = new GetSQLData(ctx).execute(cur_url).get();

                try {
                    int values_count = Integer.parseInt(jsonFromSQL.getString("iTotalDisplayRecords"));
                    if (values_count > 0) {
                        JSONArray SQL_data = jsonFromSQL.getJSONArray("aaData");
                        for (int i = 0; i < SQL_data.length(); i++) {
                            JSONArray row = SQL_data.getJSONArray(i);
                            Log.d(LOG_TAG, row.getString(1));
                            m = new HashMap<String, String>();
                            m.put(ATTR_ID, row.getString(0));//ид посетителя
                            m.put(ATTR_POSETITEL_NAME, row.getString(1)); // фио посетителя или авто
                            childDataItem.add(m);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            childData.add(childDataItem);
        }
    }

    SimpleExpandableListAdapter getAdapter() {

        //Создаем коллекцию групп
        String[] groups = new String[] {ctx.getString(R.string.pedestrian), ctx.getString(R.string.auto)};

        // заполняем коллекцию групп из массива с названиями групп
        groupData = new ArrayList<Map<String, String>>();
        for (String group : groups) {
            // заполняем список аттрибутов для каждой группы
            m = new HashMap<String, String>();
            m.put(ATTR_GROUP_NAME, group); // имя компании
            groupData.add(m);
        }

        // список аттрибутов групп для чтения
        String groupFrom[] = new String[] {ATTR_GROUP_NAME};
        // список ID view-элементов, в которые будет помещены аттрибуты групп
        int groupTo[] = new int[] {android.R.id.text1};

        CreateCollection();

       // список аттрибутов элементов для чтения
        String childFrom[] = new String[] {ATTR_POSETITEL_NAME};
        // список ID view-элементов, в которые будет помещены аттрибуты элементов
        int childTo[] = new int[] {android.R.id.text1};

        adapter = new SimpleExpandableListAdapter(
                ctx,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                groupFrom,
                groupTo,
                childData,
                android.R.layout.simple_list_item_1,
                childFrom,
                childTo);

        return adapter;
    }

    String getGroupText(int groupPos) {
        return ((Map<String,String>)(adapter.getGroup(groupPos))).get(ATTR_GROUP_NAME);
    }

    String getChildText(int groupPos, int childPos) {
        return ((Map<String,String>)(adapter.getChild(groupPos, childPos))).get(ATTR_ID);
    }

    String getGroupChildText(int groupPos, int childPos) {
        return getGroupText(groupPos) + " " +  getChildText(groupPos, childPos);
    }
}
