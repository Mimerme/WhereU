package io.github.mimerme.whereu.utility;

import android.content.Context;
import android.os.Build;
import android.telephony.PhoneNumberUtils;

import java.io.DataOutputStream;
import java.io.IOException;

import io.github.mimerme.whereu.R;

public class Utility {
    public static void runSuperUserCommand(String command){
        try {
            //Start the superuser binary
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes(command);
            outputStream.flush();

            //Terminate the process
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        } catch (SecurityException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //Ripped from https://www.programcreek.com/java-api-examples/?class=android.telephony.PhoneNumberUtils&method=formatNumberToE164
    public static String formatNumber(String unformattedNumber, String simCountryIso){
        String formattedNumber;
        if(Build.VERSION.SDK_INT >= 21) {
            formattedNumber = PhoneNumberUtils.formatNumberToE164(unformattedNumber, simCountryIso);
        } else {
            formattedNumber = PhoneNumberUtils.formatNumber(unformattedNumber);
        }
        if(formattedNumber == null){
            unformattedNumber = PhoneNumberUtils.normalizeNumber(unformattedNumber);
            formattedNumber = unformattedNumber.replaceAll("[-,+,(,)]","");
        }
        return formattedNumber;
    }

    //Ripped from https://stackoverflow.com/questions/31578958/how-to-get-country-codecalling-code-in-android?lq=1
    public static String getCountryDialCode(Context context, String countryId){
        String contryDialCode = null;

        String[] arrContryCode=context.getResources().getStringArray(R.array.DialingCountryCode);
        for(int i=0; i<arrContryCode.length; i++){
            String[] arrDial = arrContryCode[i].split(",");
            if(arrDial[1].trim().equals(countryId.toUpperCase().trim())){
                contryDialCode = arrDial[0];
                break;
            }
        }
        return "+" + contryDialCode;
    }
}
