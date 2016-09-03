package com.elynx.pogoxmitm;

import com.elynx.pogoxmitm.modules.DataExporter;
import com.github.aeonlucid.pogoprotos.Data;
import com.github.aeonlucid.pogoprotos.Inventory;
import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.github.aeonlucid.pogoprotos.networking.Responses;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import de.robv.android.xposed.XposedBridge;

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
            DataExporter dataExporter = new DataExporter();
            boolean doExport = false;
            boolean deltaChanged = false;

            Inventory.InventoryDelta.Builder inventoryDeltaBuilder = inventoryResponseBuilder.getInventoryDelta().toBuilder();

            for (int invItemNo = 0; invItemNo < inventoryDeltaBuilder.getInventoryItemsCount(); ++invItemNo) {
                Inventory.InventoryItem.Builder invItemBuilder = inventoryDeltaBuilder.getInventoryItems(invItemNo).toBuilder();

                if (invItemBuilder.hasInventoryItemData()) {
                    Inventory.InventoryItemData.Builder invItemDataBuilder = invItemBuilder.getInventoryItemData().toBuilder();

                    if (Options.getInstance().getExportHack().isActive()) {
                        if (invItemDataBuilder.hasCandy()) {
                            int candies = invItemDataBuilder.getCandy().toBuilder().getCandy();
                            int family = invItemDataBuilder.getCandy().toBuilder().getFamilyIdValue();
                            dataExporter.addCandyData(family, candies);
                        }

                        if (invItemDataBuilder.hasPokemonData()) {
                            Data.PokemonData pokeData = invItemDataBuilder.getPokemonData();

                            if (!pokeData.getIsEgg()) {
                                dataExporter.addPokemonData(pokeData);
                                doExport = true;
                            }
                        }
                    }

                    if (Options.getInstance().getIvHack().isActive()) {
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
                }
            } //end of delta->items

            if (doExport) {
                dataExporter.run();
            }

            if (deltaChanged) {
                inventoryResponseBuilder.setInventoryDelta(inventoryDeltaBuilder.build());
                return inventoryResponseBuilder.build().toByteString();
            }
        } // end of delta

        return null;
    }

    // From PoGo Optimizer by Justin Wells
    protected static float levelFromCpMultiplier(float cpMultiplier) {
        final float[] Cps = {
                0.094f,
                0.135137432f,
                0.16639787f,
                0.192650919f,
                0.21573247f,
                0.236572661f,
                0.25572005f,
                0.273530381f,
                0.29024988f,
                0.306057377f,
                0.3210876f,
                0.335445036f,
                0.34921268f,
                0.362457751f,
                0.37523559f,
                0.387592406f,
                0.39956728f,
                0.411193551f,
                0.42250001f,
                0.432926419f,
                0.44310755f,
                0.4530599578f,
                0.46279839f,
                0.472336083f,
                0.48168495f,
                0.4908558f,
                0.49985844f,
                0.508701765f,
                0.51739395f,
                0.525942511f,
                0.53435433f,
                0.542635767f,
                0.55079269f,
                0.558830576f,
                0.56675452f,
                0.574569153f,
                0.58227891f,
                0.589887917f,
                0.59740001f,
                0.604818814f,
                0.61215729f,
                0.619399365f,
                0.62656713f,
                0.633644533f,
                0.64065295f,
                0.647576426f,
                0.65443563f,
                0.661214806f,
                0.667934f,
                0.674577537f,
                0.68116492f,
                0.687680648f,
                0.69414365f,
                0.700538673f,
                0.70688421f,
                0.713164996f,
                0.71939909f,
                0.725571552f,
                0.7317f,
                0.734741009f,
                0.73776948f,
                0.740785574f,
                0.74378943f,
                0.746781211f,
                0.74976104f,
                0.752729087f,
                0.75568551f,
                0.758630378f,
                0.76156384f,
                0.764486065f,
                0.76739717f,
                0.770297266f,
                0.7731865f,
                0.776064962f,
                0.77893275f,
                0.781790055f,
                0.78463697f,
                0.787473578f,
                0.79030001f
        };

        for (int i = 0; i < Cps.length; ++i) {
            if (Math.abs(Cps[i] - cpMultiplier) < 0.0001) {
                float level = i + 1.0f;
                level /= 2.0f;
                return level;
            }
        }

        if (BuildConfig.DEBUG) {
            XposedBridge.log("[PoGo MITM] For CP Multiplier " + Float.toString(cpMultiplier) + " no level found");
        }

        return 0.0f;
    }

    protected static Data.PokemonData.Builder makeIvNickname(Data.PokemonData.Builder pokeBuilder) {
        final String circles = "⓪①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳";

        int atk = pokeBuilder.getIndividualAttack();
        int def = pokeBuilder.getIndividualDefense();
        int sta = pokeBuilder.getIndividualStamina();

        int total = (atk + def + sta) * 100 / 45;

        float cpMul = pokeBuilder.getCpMultiplier();
        float levelNow = levelFromCpMultiplier(cpMul);
        //float levelAdded = pokeBuilder.getNumUpgrades() / 2.0f;
        //float levelFound = levelNow - levelAdded;

        //Item.ItemId ballId = pokeBuilder.getPokeball();
        //String ballStr = "";

        //switch (ballId) {
        //    case ITEM_GREAT_BALL:
        //        ballStr += "G";
        //        break;

        //    case ITEM_MASTER_BALL:
        //        ballStr += "M";
        //        break;

        //    case ITEM_POKE_BALL:
        //        ballStr += "P";
        //        break;

        //    case ITEM_ULTRA_BALL:
        //        ballStr += "U";
        //        break;
        //}

        //String gymString = pokeBuilder.getDeployedFortId();

        // parse UTF-16
        //try {
        //    if (gymString.endsWith(".16")) {
        //        gymString = new String(pokeBuilder.getDeployedFortIdBytes(), "UTF-16");
        //    }
        //}
        //catch (UnsupportedEncodingException e)
        //{
        //    gymString = "";
        //}

        //if (BuildConfig.DEBUG) {
        //     XposedBridge.log(gymString);
        //}

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

        String nickname = prefix + " " + Integer.toString(total) + "% " +
                circles.charAt(atk) +
                circles.charAt(def) +
                circles.charAt(sta);

        //if (gymString.isEmpty()) {
        nickname += " " + Float.toString(levelNow);// + " "  + ballStr;
        //} else {
        //    nickname += " " + gymString;
        //}

        int length = Math.min(nickname.length(), 15);
        nickname = nickname.substring(0, length);

        if (BuildConfig.DEBUG) {
            XposedBridge.log(nickname);
        }

        pokeBuilder.setNickname(nickname);

        return pokeBuilder;
    }
}
