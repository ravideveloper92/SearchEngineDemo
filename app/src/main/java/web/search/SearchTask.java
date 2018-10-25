package web.search;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SearchTask extends AsyncTask<String, Void, List<GObject>> {

    private Context context;
    private AsyncDelegate delegate;
    private long firstItemID = 1;
    private boolean type = false;
    private static final String TAG = "SearchTask";
    public int error_code = 0;

    public SearchTask(AsyncDelegate delegate, Context context) {
        this.delegate = delegate;
        this.context = context;

    }

    public void setFirstItemID(long firstItemID) {
        this.firstItemID = firstItemID;
    }

    public boolean isType() {
        return type;
    }

    public long getFirstItemID() {
        return firstItemID;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    @Override
    protected List<GObject> doInBackground(String... params) {
        Customsearch.Builder customSearch = new Customsearch.Builder(new NetHttpTransport(), new JacksonFactory(), null);
        customSearch.setApplicationName("Search");
        try {
            com.google.api.services.customsearch.Customsearch.Cse.List list = customSearch.build().cse().list(params[0]);
/*
            list.setKey("AIzaSyC75jB3i2b1T9uJWx4PaZr2nkk_1Rm6ji4");
            list.setCx("008811021703158615141:a6plh-c0seq");
*/

            /*list.setKey("AIzaSyD53YaZP4FPbPFdJNgOrFQ7vHJoe_3TpfY");
            list.setCx("013835402155573620987:kruaky5vq3o");
*/
          /*  list.setKey("AIzaSyBalhmKt4_n55LeunoGYpgrhrrc8uiPsOA");
            list.setCx("017536850012760035502:s8bbzdqizq4");*/


            list.setKey("AIzaSyDbbmgAfTt1k_Nyn0QptMfURVJO0k3g_tE");
            list.setCx("013704916076216824849:glckvt1zoxg");


            // list.setStart(firstItemID);
            list.setStart(firstItemID);
            if (type) {
                list.setSearchType("image");
            }
            Log.d(TAG, "doInBackground: list " + list.toString());
            Search results = null;
            results = list.execute();
            Log.d(TAG, "doInBackground: results " + results);
            List<GObject> objects = new ArrayList<>();
            if (results.getItems() != null)
                for (Result result : results.getItems()) {
                    if (result != null)
                        objects.add(new GObject(result.getTitle(), result.getFormattedUrl()));
                }
            return objects;
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            error_code = 3;
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.d(TAG, "doInBackground: network ");
            error_code = 1;
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "doInBackground: network ");
            error_code = 1;
            e.printStackTrace();
        } catch (IOException e) {
            error_code = 2;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<GObject> items) {
        super.onPostExecute(items);
        this.delegate.asyncComplete(items, error_code);
    }


}