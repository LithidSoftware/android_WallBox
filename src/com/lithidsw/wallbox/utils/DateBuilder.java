/*
 * Copyright 2013 Jeremie Long
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lithidsw.wallbox.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.text.format.DateFormat;

public class DateBuilder {

    public String getFullDate(Context c, String date) {
        Date dateFromSms = new Date(Long.parseLong(date));
        Locale locale = Locale.getDefault();

        SimpleDateFormat day = new SimpleDateFormat("EEE", locale);
        String strDay = day.format(dateFromSms);

        SimpleDateFormat month = new SimpleDateFormat("MMM", locale);
        String strMonth = month.format(dateFromSms);

        SimpleDateFormat dayNum = new SimpleDateFormat("dd", locale);
        String strDayNum = dayNum.format(dateFromSms);

        SimpleDateFormat hour = new SimpleDateFormat("HH", locale);
        String strH = hour.format(dateFromSms);
        String strHour = String.valueOf(getHour(c, strH));

        SimpleDateFormat min = new SimpleDateFormat("mm", locale);
        String strMin = min.format(dateFromSms);
        return strDay + ", " + strMonth + strDayNum + " " + strHour + ":"
                + strMin + getAmPm(c, strH);
    }

    private int getHour(Context c, String sdf) {
        int hour = Integer.parseInt(sdf.toString());
        if (!DateFormat.is24HourFormat(c)) {
            if (hour > 12) {
                return hour - 12;
            } else if (hour == 0) {
                return 12;
            } else {
                return hour;
            }
        } else {
            return hour;
        }
    }

    private String getAmPm(Context c, String sdf) {
        int hour = Integer.parseInt(sdf.toString());
        if (!DateFormat.is24HourFormat(c)) {
            if (hour >= 12) {
                return "PM";
            } else {
                return "AM";
            }
        } else {
            return "";
        }
    }
}
