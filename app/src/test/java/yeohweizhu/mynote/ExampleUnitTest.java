package yeohweizhu.mynote;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    public ExampleUnitTest(){
    }

    @Test
    public void addition_isCorrect() throws Exception {
        //TEST
        ArrayList<Note.CheckListItem> checkListItemArrayList= new ArrayList<>();

        Note.CheckListItem item =new Note.CheckListItem();
        item.setChecked(true);
        item.setItemText("Aloha");
        checkListItemArrayList.add(item);
        item =new Note.CheckListItem();
        item.setChecked(false);
        item.setItemText("Behqi");
        checkListItemArrayList.add(item);
        item =new Note.CheckListItem();
        item.setChecked(true);
        item.setItemText("Ehqi");
        checkListItemArrayList.add(item);

        String jsonString = Note.CheckListItem.serializeToJsonArray(checkListItemArrayList).toString();
        Log.d("UnitTest", jsonString);

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            checkListItemArrayList = Note.CheckListItem.deserializeFromJsonArray(jsonArray);
            jsonString = Note.CheckListItem.serializeToJsonArray(checkListItemArrayList).toString();
            Log.d("UnitTestReversed", jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}