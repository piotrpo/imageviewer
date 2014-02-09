package pl.com.digita.imageviewer.backend;

import pl.com.digita.imageviewer.backend.dataobject.Note;
import retrofit.Callback;
import retrofit.http.GET;

import java.util.List;

/**
 * Stub for backend server methods
 */
public interface IBackendStub {

    @GET("/notes")
    public void downloadNotes(Callback<List<Note>> pCallback);

}
