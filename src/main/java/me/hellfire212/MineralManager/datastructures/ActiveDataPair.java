package me.hellfire212.MineralManager.datastructures;

import java.util.HashMap;
import java.util.Map;

import me.hellfire212.MineralManager.BlockInfo;
import me.hellfire212.MineralManager.Coordinate;
import me.hellfire212.MineralManager.BlockInfo.Type;
import static me.hellfire212.MineralManager.utils.DecoderRing.*;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("ActiveDataPair")
public class ActiveDataPair implements ConfigurationSerializable {
    public Coordinate coord;
    public BlockInfo info;

    public ActiveDataPair(Coordinate coord, BlockInfo info) {
        this.coord = coord;
        this.info = info;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("x", (int) coord.getX());
        m.put("y", (int) coord.getY());
        m.put("z", (int) coord.getZ());
        m.put("world", coord.getWorldName());
         
        m.put("bType", info.getTypeId(Type.BLOCK));
        m.put("pType", info.getTypeId(Type.PLACEHOLDER));
        m.put("bData", info.getData(Type.BLOCK));
        m.put("pData", info.getData(Type.PLACEHOLDER));
        m.put("respawn", info.getRespawn());
        return m;
    }
    
    public static ActiveDataPair deserialize(Map<String, Object> m) {
        Coordinate coord = new Coordinate(
            decodeDouble(m.get("x")),
            decodeDouble(m.get("y")),
            decodeDouble(m.get("z" )),
            decodeString(m.get("world"), "")
        );
        BlockInfo info = new BlockInfo(
            decodeInt(m.get("bType"), 0),
            decodeInt(m.get("pType"), 0),
            decodeInt(m.get("bData"), 0),
            decodeInt(m.get("pData"), 0)
        );
        info.setRespawn(decodeLong(m.get("respawn")));
        return new ActiveDataPair(coord, info);
    }


}
