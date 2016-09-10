package com.elynx.pogoxmitm.module;

import com.github.aeonlucid.pogoprotos.networking.Requests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that supervises several ModuleManagers
 *
 * Provide single entry point for MitmProvider into installed hacks
 */
public class ModuleSupervisor {
    List<ModuleManager> modules;

    Set<Requests.RequestType> packageTypes;

    ModuleSupervisor() {
        modules = new ArrayList<>();

        packageTypes = new HashSet<>();
    }

    boolean addModule(ModuleManager module) {
        for (ModuleManager m : modules) {
            if (module.moduleId() == m.moduleId()) {
                return false;
            }
        }

        updatePackageTypes();

        return true;
    }

    void updatePackageTypes() {
        packageTypes.clear();

        for (ModuleManager m : modules) {
            packageTypes.addAll(m.requestTypes());
            packageTypes.addAll(m.responseTypes());
        }
    }
}
