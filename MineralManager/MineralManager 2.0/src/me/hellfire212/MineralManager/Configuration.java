package me.hellfire212.MineralManager;

import java.io.Serializable;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.DataFormatException;

import me.hellfire212.MineralManager.BlockInfo.Type;

import org.bukkit.configuration.ConfigurationSection;

public class Configuration implements Serializable {
	
	private static final long serialVersionUID = -557406791839275784L;
	
	private static final String LOCKED = "locked";
	private static final String VOLATILE = "volatile";
	private static final String GLOBAL = "global";
	private static final String ACTIVE = "active";
	private static final String MANAGED_BLOCKS = "managedBlocks";
	private static final String PLACEHOLDER = "placeholder";
	private static final String DISPLAY_MESSAGES_ON_BLOCK_PROSPECT = "displayMessages.onBlockProspect";
	private static final String DISPLAY_MESSAGES_ON_BLOCK_BREAK = "displayMessages.onBlockBreak";
	private static final String MINE_ORIGINAL_ONLY = "mineOriginalOnly";
	private static final String USE_PERMISSIONS = "usePermissions";
	
	public enum Field { TYPE_ID, DATA, COOLDOWN, DEGRADE };
	
	private boolean isLocked = false;
	private boolean isVolatile = false;
	private boolean isGlobal = false;
	private boolean isActive = true;
	private boolean mineOriginalOnly = false;
	private boolean usePermissions = false;
	private String onBlockBreak = "";
	private String onBlockProspect = "";
	
	private BlockInfo placeholderBlock = new BlockInfo(Type.BLOCK, 0, 0);
	private HashMap<BlockInfo, Mineral> blockMap = new HashMap<BlockInfo, Mineral>();
	private String name = "";
	
	public Configuration() {}
	
	public Configuration(ConfigurationSection currentConfig, Configuration def) throws ParseException, NumberFormatException, DataFormatException {
		isLocked = currentConfig.contains(LOCKED) ? currentConfig.getBoolean(LOCKED) : def.isLocked();
		isVolatile = currentConfig.contains(VOLATILE) ? currentConfig.getBoolean(VOLATILE) : def.isVolatile();
		isGlobal = currentConfig.contains(GLOBAL) ? currentConfig.getBoolean(GLOBAL) : def.isGlobal();
		isActive = currentConfig.contains(ACTIVE) ? currentConfig.getBoolean(ACTIVE) : def.isActive();
		mineOriginalOnly = currentConfig.contains(MINE_ORIGINAL_ONLY) ? currentConfig.getBoolean(MINE_ORIGINAL_ONLY) : def.isMineOriginalOnly();
		usePermissions = currentConfig.contains(USE_PERMISSIONS) ? currentConfig.getBoolean(USE_PERMISSIONS) : def.isUsePermissions();
		onBlockBreak = currentConfig.contains(DISPLAY_MESSAGES_ON_BLOCK_BREAK) ? currentConfig.getString(DISPLAY_MESSAGES_ON_BLOCK_BREAK) : def.getOnBlockBreak();
		onBlockProspect = currentConfig.contains(DISPLAY_MESSAGES_ON_BLOCK_PROSPECT) ? currentConfig.getString(DISPLAY_MESSAGES_ON_BLOCK_PROSPECT) : def.getOnBlockProspect();
		name = currentConfig.getName();
		
		List<?> mineralList = currentConfig.contains(MANAGED_BLOCKS) ? currentConfig.getList(MANAGED_BLOCKS) : null;
		HashMap<BlockInfo, Mineral> blockMap = def.blockMap;
		
		if(mineralList != null) {
			blockMap = new HashMap<BlockInfo, Mineral>(8);
			int count = 0;
			for(Object currList : mineralList) {
				LinkedHashMap<?, ?> lhm = (LinkedHashMap<?, ?>) currList;
				
				//****I think we're allowed to pass null parameters into the "generate" methods but I need to test to be sure!
				Object typeObject = lhm.get("type");
				BlockInfo info = new BlockInfo(Type.BLOCK, generate(Field.TYPE_ID, count, typeObject).intValue(), generate(Field.DATA, count, typeObject).intValue());
				blockMap.put(info, new Mineral(info, generate(Field.COOLDOWN, count, lhm.get("cooldown")).intValue(), generate(Field.DEGRADE, count, lhm.get("degrade")).doubleValue()));
				count++;
			}
		}
		
		this.blockMap = blockMap;
	
		BlockInfo placeholderBlock = parsePlaceholderMaterial(currentConfig.contains(PLACEHOLDER) ? currentConfig.getString(PLACEHOLDER) : def.getPlaceholderBlock().toString(Type.PLACEHOLDER));
		if(placeholderBlock == null) {
			throw new DataFormatException("[" + name + "]->[placeholder] : The value contains an error.");
		}
		
		this.placeholderBlock = placeholderBlock;
	}
	
	private Number generate(Field field, int count, Object object) throws ParseException, NumberFormatException {
		Number result;
		String branch = "type";
		String value = "";
		switch(field) {
		case TYPE_ID:
			result = Tools.parseTypeId(object);
			value = " id";
			break;
		case DATA:
			result = Tools.parseTypeData(object);
			value = " data";
			break;
		case COOLDOWN:
			result = Tools.parseCooldown(object);
			branch = "cooldown";
			break;
		case DEGRADE:
			result = Tools.parseDegrade(object);
			branch = "degrade";
			break;
		default:
			result = -1;
		}
		if(result.intValue() == -1) {
			if(object == null) {
				throw new ParseException("[" + name + "]->[managedBlocks]->[" + count + "]->[" + branch + "] : The key contains an error.", count);
			} else {
				throw new NumberFormatException("[" + name + "]->[managedBlocks]->[" + count + "]->[" + branch + "] : The" + value + " value \"" + object + "\" contains an error.");
			}
		}
		return result;
	}
	
	public boolean isLocked() {
		return isLocked;
	}

	public boolean isVolatile() {
		return isVolatile;
	}
	
	public boolean isGlobal() {
		return isGlobal;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public boolean isMineOriginalOnly() {
		return mineOriginalOnly;
	}
	
	public boolean isUsePermissions() {
		return usePermissions;
	}
	
	public String getOnBlockBreak() {
		return onBlockBreak;
	}
	
	public String getOnBlockProspect() {
		return onBlockProspect;
	}
	
	public BlockInfo getPlaceholderBlock() {
		return placeholderBlock;
	}
	
	public HashMap<BlockInfo, Mineral> getBlockMap() {
		return blockMap == null ? new HashMap<BlockInfo, Mineral>() : blockMap;
	}
	
	public String getName() {
		return name;
	}
	
	private BlockInfo parsePlaceholderMaterial(String string) {
		int typeId = Tools.parseTypeId(string);
		if(typeId == -1) {
			return null;
		}
		byte typeData = Tools.parseTypeData(string);
		if(typeData == -1) {
			return null;
		}
		return new BlockInfo(Type.PLACEHOLDER, typeId, typeData);
	}
	
	@Override
	public String toString() {
		return  name;
	}
}
