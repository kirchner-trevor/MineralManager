/*    */ package me.hellfire212.MineralVein;
/*    */ 
/*    */ import java.io.Serializable;
/*    */ import org.bukkit.Location;
/*    */ import org.bukkit.Material;
/*    */ import org.bukkit.block.Block;
/*    */ 
/*    */ public final class SBlock
/*    */   implements Serializable
/*    */ {
/*    */   private String world;
/*    */   private String material;
/*    */   private int x;
/*    */   private int y;
/*    */   private int z;
/*    */   private static final long serialVersionUID = -1034913999687434914L;
/*    */ 
/*    */   public SBlock(Block block)
/*    */   {
/* 12 */     this.material = block.getType().toString();
/* 13 */     this.world = block.getWorld().getName();
/* 14 */     this.x = block.getX();
/* 15 */     this.y = block.getY();
/* 16 */     this.z = block.getZ();
/*    */   }
/*    */ 
/*    */   public SBlock(Location location, Material material) {
/* 20 */     this.material = material.toString();
/* 21 */     this.world = location.getWorld().getName();
/* 22 */     this.x = location.getBlockX();
/* 23 */     this.y = location.getBlockY();
/* 24 */     this.z = location.getBlockZ();
/*    */   }
/*    */ 
/*    */   public final String getWorld() {
/* 28 */     return this.world;
/*    */   }
/*    */ 
/*    */   public final String getMaterial() {
/* 32 */     return this.material;
/*    */   }
/*    */ 
/*    */   public final int getX() {
/* 36 */     return this.x;
/*    */   }
/*    */ 
/*    */   public final int getY() {
/* 40 */     return this.y;
/*    */   }
/*    */ 
/*    */   public final int getZ() {
/* 44 */     return this.z;
/*    */   }
/*    */ }

/* Location:           /Volumes/ramdisk/temp/
 * Qualified Name:     me.hellfire212.MineralVein.SBlock
 * JD-Core Version:    0.6.0
 */