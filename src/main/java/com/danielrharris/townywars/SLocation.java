package com.danielrharris.townywars;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class SLocation implements Serializable {
    private static final long serialVersionUID = -2185982130721731539L;
    private Double x = null;
    private Double y = null;
    private Double z = null;
    private Float pitch = null;
    private Float yaw = null;
    private UUID world = null;


    public SLocation(Location loc) {
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        pitch = loc.getPitch();
        yaw = loc.getYaw();
        world = loc.getWorld().getUID();
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public World getWorld() {
        return Bukkit.getServer().getWorld(this.world);
    }

    public Location toLocation() {
        return new Location(this.getWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public String serialize() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(this);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save location", e);
        }
    }

    public static SLocation deSerialize(String base64) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            SLocation slocation = (SLocation)dataInput.readObject();
            dataInput.close();
            return slocation;
        } catch (ClassNotFoundException | IOException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

}