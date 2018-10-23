package controlledBurn;

import android.app.job.JobParameters;
import android.app.job.JobService;

import org.joda.time.LocalDate;

public class MyJobService extends JobService {


    @Override
    public boolean onStartJob(JobParameters params) {
        // Note: this is preformed on the main thread.

        Repository repo = new Repository(getFilesDir().toURI());
        repo.setupRepository();

        LocalDate today = Repository.getToday();

        LocalDate firstDate = repo.getFirstRecordDate();

        repo.smooth(today,firstDate);

        return false;
    }


    // Stopping jobs if our job requires change.

    @Override
    public boolean onStopJob(JobParameters params) {
        // Note: return true to reschedule this job.

        return false;
    }

}