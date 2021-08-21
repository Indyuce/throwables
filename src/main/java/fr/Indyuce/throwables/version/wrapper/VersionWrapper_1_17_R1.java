package fr.Indyuce.throwables.version.wrapper;

import fr.Indyuce.throwables.version.objects.ItemTag;
import fr.Indyuce.throwables.version.objects.NBTItem;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionWrapper_1_17_R1 implements VersionWrapper {

    @Override
    public NBTItem getNBTItem(ItemStack item) {
        return new NBTItem_v1_17_R1(item);
    }

    private static final Pattern PATTERN = Pattern.compile("<(#|HEX)([a-fA-F0-9]{6})>");

    @Override
    public String parseColors(String format) {
        Matcher match = PATTERN.matcher(format);

        while (match.find()) {
            String color = format.substring(match.start(), match.end());
            format = format.replace(color, "" + ChatColor.of('#' + match.group(2)));
            match = PATTERN.matcher(format);
        }

        return ChatColor.translateAlternateColorCodes('&', format);
    }

    private class NBTItem_v1_17_R1 extends NBTItem {
        private final net.minecraft.world.item.ItemStack nms;
        private final NBTTagCompound compound;

        public NBTItem_v1_17_R1(ItemStack item) {
            super(item);

            nms = CraftItemStack.asNMSCopy(item);
            compound = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
        }

        @Override
        public String getString(String path) {
            return compound.getString(path);
        }

        @Override
        public boolean hasTag(String path) {
            return compound.hasKey(path);
        }

        @Override
        public boolean getBoolean(String path) {
            return compound.getBoolean(path);
        }

        @Override
        public double getDouble(String path) {
            return compound.getDouble(path);
        }

        @Override
        public int getInteger(String path) {
            return compound.getInt(path);
        }

        @Override
        public NBTItem addTag(List<ItemTag> tags) {
            tags.forEach(tag -> {
                if (tag.getValue() instanceof Boolean)
                    compound.setBoolean(tag.getPath(), (boolean) tag.getValue());
                else if (tag.getValue() instanceof Double)
                    compound.setDouble(tag.getPath(), (double) tag.getValue());
                else if (tag.getValue() instanceof String)
                    compound.setString(tag.getPath(), (String) tag.getValue());
                else if (tag.getValue() instanceof Integer)
                    compound.setInt(tag.getPath(), (int) tag.getValue());
            });
            return this;
        }

        @Override
        public NBTItem removeTag(String... paths) {
            for (String path : paths)
                compound.remove(path);
            return this;
        }

        @Override
        public Set<String> getTags() {
            return compound.getKeys();
        }

        @Override
        public ItemStack toItem() {
            nms.setTag(compound);
            return CraftItemStack.asBukkitCopy(nms);
        }
    }
}
