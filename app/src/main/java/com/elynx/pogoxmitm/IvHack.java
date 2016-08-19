package com.elynx.pogoxmitm;

import com.github.aeonlucid.pogoprotos.Data;
import com.github.aeonlucid.pogoprotos.Inventory;
import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.github.aeonlucid.pogoprotos.networking.Responses;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Class that shows IVs for pokemon in nickname
 */
public class IvHack {
    public static Requests.RequestType monitoredType() { return Requests.RequestType.GET_INVENTORY; }

    public static ByteString hack(ByteString response) throws InvalidProtocolBufferException {
        Responses.GetInventoryResponse.Builder inventoryResponseBuilder = Responses.GetInventoryResponse.parseFrom(response).toBuilder();

        if (inventoryResponseBuilder.hasInventoryDelta()) {
            boolean deltaChanged = false;
            Inventory.InventoryDelta.Builder inventoryDeltaBuilder = inventoryResponseBuilder.getInventoryDelta().toBuilder();

            for (int invItemNo = 0; invItemNo < inventoryDeltaBuilder.getInventoryItemsCount(); ++invItemNo) {
                Inventory.InventoryItem.Builder invItemBuilder = inventoryDeltaBuilder.getInventoryItems(invItemNo).toBuilder();

                if (invItemBuilder.hasInventoryItemData()) {
                    Inventory.InventoryItemData.Builder invItemDataBuilder = invItemBuilder.getInventoryItemData().toBuilder();

                    if (invItemDataBuilder.hasPokemonData()) {
                        Data.PokemonData.Builder pokeBuilder = invItemDataBuilder.getPokemonData().toBuilder();

                        if (pokeBuilder.getIsEgg())
                            continue; // with another item

                        pokeBuilder = makeIvNickname(pokeBuilder);

                        invItemDataBuilder.setPokemonData(pokeBuilder.build());
                        invItemBuilder.setInventoryItemData(invItemDataBuilder.build());
                        inventoryDeltaBuilder.setInventoryItems(invItemNo, invItemBuilder.build());

                        deltaChanged = true;
                    }
                }
            } //end of delta->items

            if (deltaChanged) {
                inventoryResponseBuilder.setInventoryDelta(inventoryDeltaBuilder.build());
                return inventoryResponseBuilder.build().toByteString();
            }
        } // end of delta

        return null;
    }

    protected static Data.PokemonData.Builder makeIvNickname(Data.PokemonData.Builder pokeBuilder) {
        int atk = pokeBuilder.getIndividualAttack();
        int def = pokeBuilder.getIndividualDefense();
        int sta = pokeBuilder.getIndividualStamina();

        int total = (atk + def + sta) * 100 / 45;

        // For A-Z sorting to see best ones at glance
        String prefix;
        if (total > 89)
            prefix = "A";
        else if (total > 79)
            prefix = "B";
        else if (total > 69)
            prefix = "C";
        else if (total > 59)
            prefix = "D";
        else if (total > 49)
            prefix = "E";
        else if (total > 39)
            prefix = "F";
        else if (total > 29)
            prefix = "G";
        else if (total > 19)
            prefix = "H";
        else if (total > 9)
            prefix = "I";
        else
            prefix = "J";

        String nickname = prefix + " " +
                Integer.toString(total) + "%" +
                " A" + Integer.toString(atk) +
                " D" + Integer.toString(def) +
                " S" + Integer.toString(sta);

        pokeBuilder.setNickname(nickname);

        return pokeBuilder;
    }
}
