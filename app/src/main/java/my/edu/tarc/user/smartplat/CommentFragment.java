package my.edu.tarc.user.smartplat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CommentFragment extends Fragment {


    public static final String TAG = "my.edu.tarc.user.smartplat";
    private ListView listView;
    private ProgressDialog pDialog;
    private View view;
    private EditText editTextAddComment;
    private Button buttonComment;
    private TextView textViewAddLike;
    private TextView textViewUpdateLike;
    private TextView textViewUsername;
    private TextView textViewCommentUser;
    private ImageButton buttonLike;
    private List<Comment> CommentList;
    private CommentFragment.CommentAdapter adapter;
    private RequestQueue queue;
    private int newLike;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.comment_fragment,container,false);

        editTextAddComment= view.findViewById(R.id.addComment);
        buttonComment= view.findViewById(R.id.commentBtn);

        textViewUsername = (TextView) view.findViewById(R.id.coment_user_name);






        buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment();
                downloadComment(getContext(), Constants.URL_SELECTCOMMENT);
            }
        });


        listView = view.findViewById(R.id.commentmenu);
        pDialog = new ProgressDialog(getContext());
        CommentList = new ArrayList<>();
        downloadComment(getContext(), Constants.URL_SELECTCOMMENT);



        //view = inflater.inflate(R.layout.comment_page,container,false);
/*
        textViewAddLike=(TextView)view.findViewById(R.id.comment_like);
        int addOneLike=1;
        textViewAddLike.getText().toString();
        int oldLike=Integer.parseInt(textViewAddLike.getText().toString());
        newLike=oldLike+addOneLike;
       buttonLike=view.findViewById(R.id.add_like);
        buttonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLike();
                textViewAddLike.setText(Integer.toString(newLike));
            }
        });
        */


        return view;
    }

    private void downloadComment(Context context, String url) {
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
                            CommentList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject eventResponse = (JSONObject) response.get(i);
                                String CommentUser = eventResponse.getString("CommentUser");
                                int CommentTime = eventResponse.getInt("CommentTime");
                                String CommentDescription = eventResponse.getString("CommentDescription");
                                int Commentlike = eventResponse.getInt("CommentLike");
                                String CommentDate = eventResponse.getString("CommentDate");
                                Comment comment = new Comment(CommentUser,CommentTime,CommentDescription,Commentlike,"",CommentDate);
                                CommentList.add(comment);
                            }
                            loadComment();
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

    private void loadComment() {
        adapter = new CommentFragment.CommentAdapter(getActivity(), R.layout.comment_page, CommentList);
        listView.setAdapter(adapter);
        if(CommentList != null){
            int size = CommentList.size();
            if(size > 0)
                Toast.makeText(getContext(), "No. of record : " + size + ".", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), "No record found.", Toast.LENGTH_SHORT).show();
        }
    }


    class CommentAdapter extends ArrayAdapter<Comment> {

        private CommentAdapter(Activity context, int resource, List<Comment> list){
            super(context,resource,list);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public View getView(int i, View view, ViewGroup viewGroup){
            Comment comment = getItem(i);
            view =  getLayoutInflater().inflate(R.layout.comment_page, viewGroup,false);

            TextView textViewCommentUser = (TextView)view.findViewById(R.id.coment_user_name);
            TextView textViewCommentTime = (TextView)view.findViewById(R.id.comment_time);
            TextView textViewCommentDescription=(TextView)view.findViewById(R.id.comment_description);
            TextView textViewCommentLike=(TextView)view.findViewById(R.id.comment_like);


 /*           Calendar calendar=Calendar.getInstance();
            SimpleDateFormat format =  new SimpleDateFormat("hh:mm:ss");
            String time=" "+ format.format(calendar.getTime());
            String time2="10:08:00";*/

            //String toyBornTime = "2019-01-11 12:26:59";

            String toyBornTime=comment.getCommentDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");


            Date oldDate = null;
            try {
                oldDate = dateFormat.parse(toyBornTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            System.out.println(oldDate);

            Date currentDate = new Date();

            long diff = currentDate.getTime()-oldDate.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            //textViewCommentUser.setText(+days+"days"+hours+"hours"+minutes+"minutes"+seconds+"seconds");


            textViewCommentUser.setText(comment.getCommentUser());

            if(seconds>=0 && seconds<60){
                textViewCommentTime.setText(""+seconds+" sec ago...");
            }
            else if(seconds>=60 && minutes < 60){
                textViewCommentTime.setText(""+minutes+" min ago...");
            }
            else if(minutes>=60 && hours< 24){
                textViewCommentTime.setText(""+hours+" hours ago...");
            }
            else{
                textViewCommentTime.setText(""+days+" days ago...");
            }

            //textViewCommentUser.setText(comment.getCommentUser());
            // textViewCommentTime.setText(""+comment.getCommentDate());
            textViewCommentDescription.setText(comment.getCommentDescription());
            textViewCommentLike.setText(""+comment.getCommentLike()+" likes");


            return view;
        }
    }

    private void addComment(){

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        Calendar calendar=Calendar.getInstance();
        // String =""+ dateFormat.format(calendar.getTime());




        final String newComment = editTextAddComment.getText().toString().trim();
        final String username = MainActivity.textViewNickName.getText().toString().trim();
        //Date currentDate = new Date();
        final String commentDate=""+ dateFormat.format(calendar.getTime());

        try {
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    Constants.URL_ADDCOMMENT,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);;
                                Toast.makeText(getContext(),
                                        jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("username", username);
                    params.put("comment", newComment);
                    params.put("time", "0");
                    params.put("commentDate",commentDate);
                    return params;
                }
            };

            RequestHandler.getInstance(getContext()).addToRequestQueue(stringRequest);
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    private void addLike(){


        final int oldLike = Integer.parseInt(textViewAddLike.getText().toString().trim());
        final int updatedLike=newLike;



        try {
            StringRequest stringRequest;
            stringRequest = new StringRequest(
                    Request.Method.POST,
                    Constants.URL_ADDLIKE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getBoolean("error")) {
                                    Toast.makeText(getContext(),
                                            jsonObject.getString("message"),
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(),
                                            jsonObject.getString("message"),
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(),
                                    error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("CommentUser", SharedPrefManager.getInstance(getContext()).getUsername());
                    params.put("oldLike", String.valueOf(oldLike));
                    params.put("newLike", String.valueOf(updatedLike));
                    return params;
                }
            };
            RequestHandler.getInstance(getContext()).addToRequestQueue(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}