package com.devansh.xcapproject;

import android.content.Intent;
import android.net.Uri;

public class BugReportUtility {
    // https://stackoverflow.com/a/18875346
    // https://stackoverflow.com/a/62877003
    public static Intent getEmailIntent(String participantId) {
        if (participantId == null) {
            participantId = "UserId not provided";
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "dponda3@gatech.edu" });
        intent.putExtra(Intent.EXTRA_SUBJECT, "[XCAP BUG]: " + participantId);
        return intent;
    }
}
