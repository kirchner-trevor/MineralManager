package me.hellfire212.MineralManager;

import java.io.Serializable;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private String onBlockBreak = null;
	private String onBlockProspect = null;
	
	private BlockInfo placeholderBlock = new BlockInfo(Type.BLOCK, 0, 0);
	private HashMap<BlockInfo, MineralConfig> blockMap = new HashMap<BlockInfo, MineralConfig>();
	private String name = "";
	
	public Configuration() {}
	
	public Configuration(String name) {
		this();
		this.name = name;
	}
	
	public Configuration(ConfigurationSection currentConfig, Configuration def) throws ParseException, NumberFormatException, DataFormatException {
		this(currentConfig.getName());
		isLocked = currentConfig.contains(LOCKED) ? currentConfig.getBoolean(LOCKED) : def.isLocked();
		isVolatile = currentConfig.contains(VOLATILE) ? currentConfig.getBoolean(VOLATILE) : def.isVolatile();
		isGlobal = currentConfig.contains(GLOBAL) ? currentConfig.getBoolean(GLOBAL) : def.isGlobal();
		isActive = currentConfig.contains(ACTIVE) ? currentConfig.getBoolean(ACTIVE) : def.isActive();
		mineOriginalOnly = currentConfig.contains(MINE_ORIGINAL_ONLY) ? currentConfig.getBoolean(MINE_ORIGINAL_ONLY) : def.isMineOriginalOnly();
		usePermissions = currentConfig.contains(USE_PERMISSIONS) ? currentConfig.getBoolean(USE_PERMISSIONS) : def.isUsePermissions();
		onBlockBreak = currentConfig.contains(DISPLAY_MESSAGES_ON_BLOCK_BREAK) ? Tools.parseDispMessage(currentConfig, DISPLAY_MESSAGES_ON_BLOCK_BREAK) : def.getOnBlockBreak();
		onBlockProspect = currentConfig.contains(DISPLAY_MESSAGES_ON_BLOCK_PROSPECT) ? Tools.parseDispMessage(currentConfig, DISPLAY_MESSAGES_ON_BLOCK_PROSPECT) : def.getOnBlockProspect();
		
		List<?> mineralList = currentConfig.contains(MANAGED_BLOCKS) ? currentConfig.getList(MANAGED_BLOCKS) : null;
		blockMap = def.blockMap;
		
		if (currentConfig.contains(PLACEHOLDER)) {
		    placeholderBlock = parsePlaceholderMaterial(currentConfig.getString(PLACEHOLDER));
	        if(placeholderBlock == null) {
	            throw new DataFormatException("[" + name + "]->[placeholder] : The value contains an error.");
	        }
		} else {
		    placeholderBlock = def.getPlaceholderBlock();
		}
		
		if(mineralList != null) {
			blockMap = new HashMap<BlockInfo, MineralConfig>();
			int count = 0;
			for(Object current : mineralList) {
				Map<?, ?> m = (Map<?, ?>) current;
				BlockInfo myPlaceholder = placeholderBlock;
				if (m.containsKey(PLACEHOLDER)) {
				    myPlaceholder = parsePlaceholderMaterial((String) m.get(PLACEHOLDER));
				}
				//****XXX I think we're allowed to pass null parameters into the "generate" methods but I need to test to be sure!
				Object typeObject = m.get("type");
				BlockInfo info = new BlockInfo(
				    generate(Field.TYPE_ID, count, typeObject).intValue(), 
				    generate(Field.DATA, count, typeObject).intValue(),
				    myPlaceholder.getPlaceholderData(),
				    myPlaceholder.getPlaceholderTypeId()
				);
				blockMap.put(info, new MineralConfig(
				    info, 
				    generate(Field.COOLDOWN, count, m.get("cooldown")).intValue(), 
				    generate(Field.DEGRADE, count, m.get("degrade")).doubleValue()
				));
				count++;
			}
		}
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
	
	public HashMap<BlockInfo, MineralConfig> getBlockMap() {
		return  blockMap;
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
