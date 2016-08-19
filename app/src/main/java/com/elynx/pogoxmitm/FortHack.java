package com.elynx.pogoxmitm;

import com.github.aeonlucid.pogoprotos.inventory.Item;
import com.github.aeonlucid.pogoprotos.map.Fort;
import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.github.aeonlucid.pogoprotos.networking.Responses;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Class that shows some more info for fort
 */
public class FortHack {
    public static Requests.RequestType monitoredType() { return Requests.RequestType.FORT_DETAILS; }

    public static ByteString hack(ByteString response) throws InvalidProtocolBufferException {
        Responses.FortDetailsResponse.Builder fortBuilder = Responses.FortDetailsResponse.parseFrom(response).toBuilder();

        if (fortBuilder.getType() == Fort.FortType.CHECKPOINT) {
            boolean modified = false;

            for (int modNo = 0; modNo < fortBuilder.getModifiersCount(); ++modNo) {
                Fort.FortModifier mod = fortBuilder.getModifiers(modNo);

                if (mod.getItemId() == Item.ItemId.ITEM_TROY_DISK) {
                    long unixTimeExpire = mod.getExpirationTimestampMs();
                    String moreInfo = "Lure by " + mod.getDeployerPlayerCodename() + " ";

                    // TODO nice time

                    moreInfo += "\n" + fortBuilder.getDescription();
                    fortBuilder.setDescription(moreInfo);

                    modified = true;
                }
            }

            if (modified) {
                return fortBuilder.build().toByteString();
            }
        }

        return null;
    }
}
