package co.etornam.freeminds.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConventer {
    //method ome
    public static String caluculateTimeAgo(long timeStamp) {

        long timeDiffernce;
        long unixTime = System.currentTimeMillis() / 1000L;  //get current time in seconds.
        int j;
        String[] periods = {"s", "mins", "hr", "days", "weeks", "mon", "yr", "d"};
        // you may choose to write full time intervals like seconds, minutes, days and so on
        double[] lengths = {60, 60, 24, 7, 4.35, 12, 10};
        timeDiffernce = unixTime - timeStamp;
        String tense = "ago";
        for (j = 0; timeDiffernce >= lengths[j] && j < lengths.length - 1; j++) {
            timeDiffernce /= lengths[j];
        }
        return timeDiffernce + periods[j] + " " + tense;
    }

//method two
public static String calculateTimePeriod(long timeStamp){
        String timeDifference;
     if (timeStamp < 60){
         timeDifference = "some seconds ago";
     }else if (timeStamp == 60){
         timeDifference = "a min ago";
     }else if (timeStamp < 3600){
         long min = timeStamp / 60;
         timeDifference = min + " mins ago";
     }else if (timeStamp == 3600){
         timeDifference = "an hr ago";
     }else if (timeStamp < 86400){
         long hr = timeStamp / 3600;
         timeDifference = hr + " hrs ago";
     }else if (timeStamp == 86400){
         timeDifference = "a day ago";
     }else if (timeStamp < 604800 ){
         long day = timeStamp / 86400;
         timeDifference = day + " days ago";
     }else if (timeStamp == 604800){
         timeDifference = "a week ago";
     }else{
         timeDifference = "some months ago";
     }
return timeDifference;
}

}
