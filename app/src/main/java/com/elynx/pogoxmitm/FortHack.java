package com.elynx.pogoxmitm;

import com.github.aeonlucid.pogoprotos.inventory.Item;
import com.github.aeonlucid.pogoprotos.map.Fort;
import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.github.aeonlucid.pogoprotos.networking.Responses;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XposedBridge;

/**
 * Class that shows some more info for fort
 */
public class FortHack {
    public static Requests.RequestType monitoredType() {
        return Requests.RequestType.FORT_DETAILS;
    }

    public static ByteString hack(ByteString response) throws InvalidProtocolBufferException {
        Responses.FortDetailsResponse.Builder fortBuilder = Responses.FortDetailsResponse.parseFrom(response).toBuilder();

        if (fortBuilder.getType() == Fort.FortType.CHECKPOINT) {
            boolean modified = false;

            for (int modNo = 0; modNo < fortBuilder.getModifiersCount(); ++modNo) {
                Fort.FortModifier mod = fortBuilder.getModifiers(modNo);
                Responses.FortDetailsResponse.Builder changedBuilder = addModInfo(fortBuilder, mod);

                if (changedBuilder != null) {
                    fortBuilder = changedBuilder;
                    modified = true;
                }
            }

            if (modified) {
                return fortBuilder.build().toByteString();
            }

            // to debug visual appearance of info - pretend that some mod was found
            // will affect only fort description
            if (BuildConfig.DEBUG) {
                Fort.FortModifier.Builder debugMod = Fort.FortModifier.newBuilder();

                debugMod.setDeployerPlayerCodename("Prof. Willow");
                debugMod.setItemId(Item.ItemId.ITEM_TROY_DISK);

                // make possible to be already expired ~ one in seven checks
                long randomRemaining = (new Random().nextLong()) % TimeUnit.MINUTES.toMillis(35);
                randomRemaining -= TimeUnit.MINUTES.toMillis(5);
                long expiration = new Date().getTime() + randomRemaining;

                debugMod.setExpirationTimestampMs(expiration);

                Responses.FortDetailsResponse.Builder changedBuilder = addModInfo(fortBuilder, debugMod.build());

                if (changedBuilder != null) {
                    fortBuilder = changedBuilder;

                    return fortBuilder.build().toByteString();
                }

                if (randomRemaining > 0) {
                    XposedBridge.log("[PoGo MITM] ERROR For not expired mod no changes were made");
                }
            }
        }

        return null;
    }

    protected static Responses.FortDetailsResponse.Builder addModInfo(Responses.FortDetailsResponse.Builder builder, Fort.FortModifier mod) {
        if (mod.getItemId() == Item.ItemId.ITEM_TROY_DISK) {
            long expires = mod.getExpirationTimestampMs();
            long now = new Date().getTime();
            long delta = expires - now;

            if (delta > 0) {
                // add player info - tmi
                //String moreInfo = "Lure by " + mod.getDeployerPlayerCodename() + "\n";
                String moreInfo = "";

                // add expiration time
                long minutes = TimeUnit.MILLISECONDS.toMinutes(delta);
                delta -= TimeUnit.MINUTES.toMillis(minutes);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(delta);

                if (minutes > 0) {
                    moreInfo += Long.toString(minutes) + "m";
                }

                if (minutes < 5) {
                    if (seconds > 0) {
                        if (moreInfo.length() > 0) {
                            moreInfo += " ";
                        }

                        moreInfo += Long.toString(seconds) + "s";
                    }
                }
                
                moreInfo += " left";

                // retain original description - tmi
                // moreInfo += "\n" + builder.getDescription();

                builder.setDescription(moreInfo);

                return builder;
            }
        }

        return null;
    }
}
