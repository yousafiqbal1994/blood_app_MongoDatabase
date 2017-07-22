package com.donateblood.blooddonation;
import java.util.ArrayList;
import java.util.List;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;

public class GCM {

    public void sendMessage(String API_KEY,String IDofDonor, String ContactNumber, String[] requesterDetails)
    {
        Sender sender = new Sender(API_KEY);
        Message message = new Message.Builder()
                .collapseKey("TEST")
                .timeToLive(30)
                .delayWhileIdle(false)
                .addData("data", "Blood needed urgently nearby Please Help,"+ContactNumber+","+requesterDetails[0]+","+requesterDetails[1])
                .build();
        try
        {
            List<String> androidTargets = new ArrayList<String>();
            androidTargets.add(IDofDonor);

            MulticastResult result = sender.send(message, androidTargets, 1);

            if (result.getResults() != null)
            {
                int canonicalRegId = result.getCanonicalIds();
                if (canonicalRegId != 0)
                {
                }
                System.out.println("Done");
            } else {
                int error = result.getFailure();
                System.out.println("Broadcast failure: " + error);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendMessageBack(String API_KEY,String IDofRequester){

        Sender sender = new Sender(API_KEY);
        if(ContactBack.DonorNotAvailable==true){
        Message message = new Message.Builder()
                .collapseKey("TEST")
                .timeToLive(30)
                .delayWhileIdle(false)
                .addData("data", "Donor Not Available").build();
            try
            {
                List<String> androidTargets = new ArrayList<String>();
                androidTargets.add(IDofRequester);

                MulticastResult result = sender.send(message, androidTargets, 1);

                if (result.getResults() != null)
                {
                    int canonicalRegId = result.getCanonicalIds();
                    if (canonicalRegId != 0)
                    {
                    }
                    System.out.println("Done");
                } else {
                    int error = result.getFailure();
                    System.out.println("Broadcast failure: " + error);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(ContactBack.DonorUnfit==true){
            Message message = new Message.Builder()
                    .collapseKey("TEST")
                    .timeToLive(30)
                    .delayWhileIdle(false)
                    .addData("data", "Donor Medically unfit").build();
            try
            {
                List<String> androidTargets = new ArrayList<String>();
                androidTargets.add(IDofRequester);

                MulticastResult result = sender.send(message, androidTargets, 1);

                if (result.getResults() != null)
                {
                    int canonicalRegId = result.getCanonicalIds();
                    if (canonicalRegId != 0)
                    {
                    }
                    System.out.println("Done");
                } else {
                    int error = result.getFailure();
                    System.out.println("Broadcast failure: " + error);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}