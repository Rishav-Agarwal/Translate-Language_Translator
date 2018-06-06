package me.rishavagarwal.translate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {

    private static FirebaseDatabase db = null;
    private static DatabaseReference ref = null;

    public static FirebaseDatabase getDb() {
        if (db == null) {
            db = FirebaseDatabase.getInstance();
            db.setPersistenceEnabled(true);
        }
        return db;
    }

    public static DatabaseReference getRef() {
        if (ref == null) {
            ref = getDb().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Translations");
            ref.keepSynced(true);
        }
        return ref;
    }
}
