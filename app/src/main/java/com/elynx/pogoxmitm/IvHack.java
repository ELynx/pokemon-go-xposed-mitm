package com.elynx.pogoxmitm;

import com.elynx.pogoxmitm.modules.DataExporter;
import com.github.aeonlucid.pogoprotos.Data;
import com.github.aeonlucid.pogoprotos.Inventory;
import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.github.aeonlucid.pogoprotos.networking.Responses;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that shows IVs for pokemon in nickname
 */
public class IvHack {
    public static boolean isMonitoredType(Requests.RequestType type) {
        if (type == Requests.RequestType.GET_INVENTORY) {
            return true;
        }

        if (type == Requests.RequestType.UPGRADE_POKEMON) {
            return true;
        }

        if (type == Requests.RequestType.EVOLVE_POKEMON) {
            return true;
        }

        return false;
    }

    public static ByteString hack(ByteString response, Requests.RequestType type) throws InvalidProtocolBufferException {
        if (type == Requests.RequestType.GET_INVENTORY) {
            return hackGetInventory(response);
        }

        if (type == Requests.RequestType.UPGRADE_POKEMON) {
            return hackUpgradePokemon(response);
        }

        if (type == Requests.RequestType.EVOLVE_POKEMON) {
            return hackEvolvePokemon(response);
        }

        return null;
    }

    protected static ByteString hackUpgradePokemon(ByteString response) throws InvalidProtocolBufferException {
        Responses.UpgradePokemonResponse.Builder upgradeBuilder = Responses.UpgradePokemonResponse.parseFrom(response).toBuilder();

        if (upgradeBuilder.getResult() == Responses.UpgradePokemonResponse.Result.SUCCESS) {
            if (upgradeBuilder.hasUpgradedPokemon()) {
                Data.PokemonData.Builder pokeBuilder = upgradeBuilder.getUpgradedPokemon().toBuilder();

                if (!pokeBuilder.getIsEgg()) {
                    pokeBuilder = makeIvNickname(pokeBuilder);

                    upgradeBuilder.setUpgradedPokemon(pokeBuilder.build());

                    return upgradeBuilder.build().toByteString();
                }
            }
        }

        return null;
    }

    protected static ByteString hackEvolvePokemon(ByteString response) throws InvalidProtocolBufferException {
        Responses.EvolvePokemonResponse.Builder evolveBuilder = Responses.EvolvePokemonResponse.parseFrom(response).toBuilder();

        if (evolveBuilder.getResult() == Responses.EvolvePokemonResponse.Result.SUCCESS) {
            if (evolveBuilder.hasEvolvedPokemonData()) {
                Data.PokemonData.Builder pokeBuilder = evolveBuilder.getEvolvedPokemonData().toBuilder();

                if (!pokeBuilder.getIsEgg()) {
                    pokeBuilder = makeIvNickname(pokeBuilder);

                    evolveBuilder.setEvolvedPokemonData(pokeBuilder.build());

                    return evolveBuilder.build().toByteString();
                }
            }
        }

        return null;
    }

    protected static ByteString hackGetInventory(ByteString response) throws InvalidProtocolBufferException {
        Responses.GetInventoryResponse.Builder inventoryResponseBuilder = Responses.GetInventoryResponse.parseFrom(response).toBuilder();

        if (inventoryResponseBuilder.hasInventoryDelta()) {
            boolean deltaChanged = false;
            DataExporter dataExporter = new DataExporter();

            Inventory.InventoryDelta.Builder inventoryDeltaBuilder = inventoryResponseBuilder.getInventoryDelta().toBuilder();

            for (int invItemNo = 0; invItemNo < inventoryDeltaBuilder.getInventoryItemsCount(); ++invItemNo) {
                Inventory.InventoryItem.Builder invItemBuilder = inventoryDeltaBuilder.getInventoryItems(invItemNo).toBuilder();

                if (invItemBuilder.hasInventoryItemData()) {
                    Inventory.InventoryItemData.Builder invItemDataBuilder = invItemBuilder.getInventoryItemData().toBuilder();

                    if (Injector.doExportHack) {
                        if (invItemDataBuilder.hasCandy()) {
                            int candies = invItemDataBuilder.getCandy().toBuilder().getCandy();
                            int family = invItemDataBuilder.getCandy().toBuilder().getFamilyIdValue();
                            dataExporter.addCandyData(family, candies);
                        }
                    }

                    if (invItemDataBuilder.hasPokemonData()) {
                        Data.PokemonData.Builder pokeBuilder = invItemDataBuilder.getPokemonData().toBuilder();

                        if (pokeBuilder.getIsEgg())
                            continue; // with another item

                        dataExporter.addPokemonData(pokeBuilder);

                        pokeBuilder = makeIvNickname(pokeBuilder);

                        invItemDataBuilder.setPokemonData(pokeBuilder.build());
                        invItemBuilder.setInventoryItemData(invItemDataBuilder.build());
                        inventoryDeltaBuilder.setInventoryItems(invItemNo, invItemBuilder.build());

                        deltaChanged = true;
                    }
                }
            } //end of delta->items

            if (deltaChanged) {
                dataExporter.run();

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

        /*Item.ItemId ballId = pokeBuilder.getPokeball();
        String ballStr = "";

        switch (ballId)
        {
            case ITEM_GREAT_BALL:
                ballStr += " G";
                break;

            case ITEM_MASTER_BALL:
                ballStr += " M";
                break;

            case ITEM_POKE_BALL:
                //ballStr += " P";
                break;

            case ITEM_ULTRA_BALL:
                ballStr += " U";
                break;
        }

        int battlesAttacked = pokeBuilder.getBattlesAttacked();
        int battlesDefended = pokeBuilder.getBattlesDefended();
        String battleString = "";

        if (battlesAttacked > 0)
            battleString += " a" + Integer.toString(battlesAttacked);

        if (battlesDefended > 0)
            battleString += " d" + Integer.toString(battlesDefended);*/

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
                " S" + Integer.toString(sta);/* +
                battleString +
                ballStr;*/

        pokeBuilder.setNickname(nickname);

        return pokeBuilder;
    }
}
