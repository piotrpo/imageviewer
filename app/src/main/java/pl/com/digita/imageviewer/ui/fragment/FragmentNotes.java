package pl.com.digita.imageviewer.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import pl.com.digita.imageviewer.R;
import pl.com.digita.imageviewer.backend.IBackendStub;
import pl.com.digita.imageviewer.backend.dataobject.Note;
import pl.com.digita.imageviewer.ui.activity.ActivityShowImage;
import pl.com.digita.imageviewer.utils.ApplicationUtils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.List;

/**
 * Fragment to display notes
 */
public class FragmentNotes extends Fragment {

    private RestAdapter mRestAdapter;
    private ListView mListView;
    private FragmentNotes.NotesAdapter mAdapter;

    public FragmentNotes() {

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(ApplicationUtils.WEB_SERVICE_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        IBackendStub backendStub = mRestAdapter.create(IBackendStub.class);

        backendStub.downloadNotes(new Callback<List<Note>>() {
            @Override
            public void success(List<Note> pNotes, Response pResponse) {

                mAdapter = new NotesAdapter(getActivity(), R.layout.item_note, pNotes);

                mListView.setAdapter(mAdapter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getActivity(), ActivityShowImage.class);
                        String pictureUrl = mAdapter.getItem(position).getPictureUrl();
                        intent.putExtra(ApplicationUtils.EXTRA_URL, pictureUrl);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void failure(RetrofitError pRetrofitError) {
                Log.e(ApplicationUtils.TAG, "Error while retrieving list of notes");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activity_list_of_notes, container, false);

        mListView = (ListView) rootView.findViewById(R.id.list_view_notes);
        return rootView;
    }


    private class NotesAdapter extends ArrayAdapter<Note> {
        LayoutInflater mLayoutInflater;

        public NotesAdapter(Context context, int resource, List<Note> objects) {
            super(context, resource, objects);
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_note, null);
            }

            TextView textViewTitle = (TextView) convertView.findViewById(R.id.text_view_title);

            textViewTitle.setText(getItem(position).getTitle());

            return convertView;

        }
    }
}