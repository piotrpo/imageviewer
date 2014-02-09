package pl.com.digita.imageviewer.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import pl.com.digita.imageviewer.R;
import pl.com.digita.imageviewer.ui.fragment.FragmentNotes;


public class ActivityListOfNotes extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_notes);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new FragmentNotes())
                    .commit();
        }
    }

}
