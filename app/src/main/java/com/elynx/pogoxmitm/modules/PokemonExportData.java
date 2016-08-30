package com.elynx.pogoxmitm.modules;

import com.github.aeonlucid.pogoprotos.Enums;

class PokemonExportData implements Comparable<PokemonExportData> {
    private int Id;
    private int Family;
    private int Candies;
    private boolean Favourite;
    private int Cp;
    private int Attack;
    private int Defence;
    private int Stamina;
    private String MoveQuick;
    private String MoveCharge;
    private boolean FromEgg;
    private int NumUpgrades;

    public int getNumUpgrades() {
        return NumUpgrades;
    }

    public void setNumUpgrades(int numUpgrades) {
        NumUpgrades = numUpgrades;
    }

    public boolean isFromEgg() {
        return FromEgg;
    }

    public void setFromEgg(boolean fromEgg) {
        FromEgg = fromEgg;
    }

    public int getAttack() {
        return Attack;
    }

    public void setAttack(int attack) {
        Attack = attack;
    }

    public int getDefence() {
        return Defence;
    }

    public void setDefence(int defence) {
        Defence = defence;
    }

    public int getStamina() {
        return Stamina;
    }

    public void setStamina(int stamina) {
        Stamina = stamina;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getFamily() {
        return Family;
    }

    public void setFamily(int family) {
        Family = family;
    }

    public void setFamilyFromPokemonId(int id) {
        Family = PokemonExportData.findFamilyId(id);
    }

    public int getCandies() {
        return Candies;
    }

    public void setCandies(int candies) {
        Candies = candies;
    }

    public boolean isFavourite() {
        return Favourite;
    }

    public void setFavourite(boolean favourite) {
        Favourite = favourite;
    }

    public int getCp() {
        return Cp;
    }

    public void setCp(int cp) {
        Cp = cp;
    }

    public int getIv() {
        return (getAttack() + getDefence() + getStamina()) * 100 / 45;
    }

    public String getMoveQuick() {
        return MoveQuick;
    }

    public void setMoveQuick(String moveQuick) {
        MoveQuick = moveQuick;
    }

    public String getMoveCharge() {
        return MoveCharge;
    }

    public void setMoveCharge(String moveCharge) {
        MoveCharge = moveCharge;
    }

    public static int findFamilyId(Integer pokeId) {
        // fix for incorrect hypno family
        if (pokeId == Enums.PokemonId.HYPNO_VALUE) {
            return Enums.PokemonFamilyId.FAMILY_DROWZEE_VALUE;
        }

        // this might be stupid but it works
        if (Enums.PokemonFamilyId.valueOf(pokeId) != null) {
            return Enums.PokemonFamilyId.valueOf(pokeId).getNumber();
        } else if (Enums.PokemonFamilyId.valueOf(pokeId - 1) != null) {
            return Enums.PokemonFamilyId.valueOf(pokeId - 1).getNumber();
        } else if (Enums.PokemonFamilyId.valueOf(pokeId - 2) != null) {
            return Enums.PokemonFamilyId.valueOf(pokeId - 2).getNumber();
        }
        return 0;
    }

    @Override
    public int compareTo(PokemonExportData pokemonData) {
        return Double.compare(getId(), pokemonData.getId());
    }
}
