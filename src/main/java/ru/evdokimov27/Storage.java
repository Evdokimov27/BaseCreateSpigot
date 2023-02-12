package ru.evdokimov27;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class Storage {
    private File file;


    public Storage(String name) {
        file = new File(basecreate.getInstance().getDataFolder(), name);
        try {
            if (!file.exists() && !file.createNewFile()) throw new IOException();
        } catch (IOException e)
        {
            throw new RuntimeException("failed", e);
        }
    }


}
