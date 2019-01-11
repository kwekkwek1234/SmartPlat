package my.edu.tarc.user.smartplat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EventFragment extends Fragment {

    public static final String TAG = "my.edu.tarc.user.smartplat";
    private ListView listView;
    private ProgressDialog pDialog;
    private SearchView searchView;
    private List<Event> EventList;
    private EventFragment.EventAdapter adapter;
    private RequestQueue queue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.event_fragment, container, false);
        searchView = (SearchView) view.findViewById(R.id.searchViewEvent);
        listView = (ListView) view.findViewById(R.id.eventmenu);
        pDialog = new ProgressDialog(getContext());
        EventList = new ArrayList<>();
        downloadEvent(getContext(), Constants.URL_SELECTEVENT);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), EventPop.class);
                Bundle bundle = new Bundle();
                bundle.putString("title", EventList.get(position).getTitle());
                bundle.putString("desc", EventList.get(position).getDescription());
                bundle.putString("datetime", EventList.get(position).getDatetime());
                bundle.putString("venue", EventList.get(position).getVenue());
                bundle.putDouble("fee", EventList.get(position).getFee());
                bundle.putInt("image", EventList.get(position).getImage());
                bundle.putString("place", EventList.get(position).getPlace());
                bundle.putString("url", EventList.get(position).getUrl());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String txt) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String txt) {
                final List<Event> filtered = new ArrayList<Event>();
                for (int pos = 0; pos < EventList.size(); pos++) {
                    if (EventList.get(pos).toString().toLowerCase().contains(txt)) {
                        Event event =
                                new Event(EventList.get(pos).getTitle(),
                                        EventList.get(pos).getDescription(),
                                        EventList.get(pos).getDatetime(),
                                        EventList.get(pos).getVenue(),
                                        EventList.get(pos).getFee(),
                                        EventList.get(pos).getImage(),
                                        EventList.get(pos).getPlace(),
                                        EventList.get(pos).getUrl());
                        filtered.add(event);
                    }
                    adapter = new EventFragment.EventAdapter(getActivity(), R.layout.event_item_layout, filtered);
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), EventPop.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("title", filtered.get(position).getTitle());
                            bundle.putString("desc", filtered.get(position).getDescription());
                            bundle.putString("datetime", filtered.get(position).getDatetime());
                            bundle.putString("venue", filtered.get(position).getVenue());
                            bundle.putDouble("fee", filtered.get(position).getFee());
                            bundle.putInt("image", filtered.get(position).getImage());
                            bundle.putString("url", filtered.get(position).getUrl());
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });
                }
                return false;
            }
        });
        return view;
    }

    private void downloadEvent(Context context, String url) {
        // Instantiate the RequestQueue
        queue = Volley.newRequestQueue(context);

        if (!pDialog.isShowing())
            pDialog.setMessage("Syn with server...");
        pDialog.show();

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            EventList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject eventResponse = (JSONObject) response.get(i);
                                String title = eventResponse.getString("title");
                                String desc = eventResponse.getString("desc");
                                String datetime = eventResponse.getString("datetime");
                                String venue = eventResponse.getString("venue");
                                double fee = eventResponse.getDouble("fee");
                                int image = eventResponse.getInt("image");
                                String place = eventResponse.getString("place");
                                String url = eventResponse.getString("url");
                                Event event = new Event(title, desc, datetime, venue, fee, image, place, url);
                                EventList.add(event);
                            }
                            loadEvent();
                            if (pDialog.isShowing())
                                pDialog.dismiss();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getContext(), "Error" + volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        if (pDialog.isShowing())
                            pDialog.dismiss();
                    }
                });

        // Set the tag on the request.
        jsonObjectRequest.setTag(TAG);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private void loadEvent() {
        adapter = new EventAdapter(getActivity(), R.layout.event_item_layout, EventList);
        listView.setAdapter(adapter);
        if (EventList != null) {
            int size = EventList.size();
            if (size > 0)
                Toast.makeText(getContext(), "No. of record : " + size + ".", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), "No record found.", Toast.LENGTH_SHORT).show();
        }
    }

    class EventAdapter extends ArrayAdapter<Event> {

        private EventAdapter(Activity context, int resource, List<Event> list) {
            super(context, resource, list);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Event event = getItem(i);
            view = getLayoutInflater().inflate(R.layout.event_item_layout, viewGroup, false);

            ImageView imageView = (ImageView) view.findViewById(R.id.imageViewEvent);
            TextView textViewTitle = (TextView) view.findViewById(R.id.tv_title);
            TextView textViewDesc = (TextView) view.findViewById(R.id.tv_description);

            imageView.setImageResource(event.getImage());
            textViewTitle.setText(event.getTitle());
            textViewDesc.setText(event.getDescription());

            return view;
        }
    }
}
