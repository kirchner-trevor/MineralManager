package me.hellfire212.MineralManager.datastructures;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import me.hellfire212.MineralManager.BlockInfo;
import me.hellfire212.MineralManager.Coordinate;
import me.hellfire212.MineralManager.MineralManager;
import me.hellfire212.MineralManager.utils.SaveFlipper;
import me.hellfire212.MineralManager.utils.Saveable;

public class ActiveBlockMap implements Saveable {
    private File file;
    private boolean dirty = false;
    private HashMap<Coordinate, BlockInfo> data = new HashMap<Coordinate, BlockInfo>();

    public ActiveBlockMap(File file) {
        super();
        this.file = file;
        this.load();
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Object o: config.getList("active")) {
            if (o instanceof ActiveDataPair) {
                ActiveDataPair p = (ActiveDataPair) o;
                this.data.put(p.coord, p.info);
            } else {
                MineralManager.getInstance().getLogger().severe("Expected ActiveDataPair, got " + o.getClass().toString());
            }
        }
    }

    @Override
    public boolean save(boolean force) {
        if (force || dirty) {
            ArrayList<ActiveDataPair> sBlocks = new ArrayList<ActiveDataPair>();
            for (Map.Entry<Coordinate, BlockInfo> e : all()) {
                sBlocks.add(new ActiveDataPair(e.getKey(), e.getValue()));
            }
            YamlConfiguration config = new YamlConfiguration();
            config.set("active", sBlocks);
            SaveFlipper sf = new SaveFlipper(file);
            try {
                config.save(sf.getSaveTemp());
                if (sf.saveFinished()) {
                    this.dirty = false;
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("<ActiveBlockMap: %d entries, file %s>", 
                             this.data.size(), this.file.getAbsolutePath());
    }

    public void add(Coordinate coordinate, BlockInfo info) {
        this.data.put(coordinate, info);
        this.dirty  = true;
    }
    
    public BlockInfo get(Coordinate coordinate) {
        return this.data.get(coordinate);
    }
    
    public boolean has(Coordinate coordinate) {
        return this.data.containsKey(coordinate);
    }

    public BlockInfo remove(Coordinate coordinate) {
        BlockInfo removed = this.data.remove(coordinate);
        this.dirty = true;
        return removed;
    }
    
    public Collection<Map.Entry<Coordinate, BlockInfo>> all() {
        return this.data.entrySet();
    }
}