/*     */ package me.hellfire212.MineralVein;
/*     */ 
/*     */ import java.io.Serializable;
/*     */ import org.bukkit.block.Block;
/*     */ 
/*     */ public class Region
/*     */   implements Serializable
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*     */   private int x1;
/*     */   private int x2;
/*     */   private int y1;
/*     */   private int y2;
/*     */   private int z1;
/*     */   private int z2;
/*     */   private String name;
/*     */ 
/*     */   public Region()
/*     */   {
/*  20 */     this.x1 = 1;
/*  21 */     this.y1 = 1;
/*  22 */     this.z1 = 1;
/*     */ 
/*  24 */     this.x2 = -1;
/*  25 */     this.y2 = -1;
/*  26 */     this.z2 = -1;
/*     */ 
/*  28 */     this.name = null;
/*     */   }
/*     */ 
/*     */   public Region(String n, RegionPoint p1, RegionPoint p2) {
/*  32 */     this.x1 = Math.min(p1.getX(), p2.getX());
/*  33 */     this.y1 = Math.min(p1.getY(), p2.getY());
/*  34 */     this.z1 = Math.min(p1.getZ(), p2.getZ());
/*     */ 
/*  36 */     this.x2 = Math.max(p1.getX(), p2.getX());
/*  37 */     this.y2 = Math.max(p1.getY(), p2.getY());
/*  38 */     this.z2 = Math.max(p1.getZ(), p2.getZ());
/*     */ 
/*  40 */     this.name = n;
/*     */   }
/*     */ 
/*     */   public Region(String n, Block b1, Block b2)
/*     */   {
/*  49 */     this.x1 = Math.min(b1.getLocation().getBlockX(), b2.getLocation().getBlockX());
/*  50 */     this.y1 = Math.min(b1.getLocation().getBlockY(), b2.getLocation().getBlockY());
/*  51 */     this.z1 = Math.min(b1.getLocation().getBlockZ(), b2.getLocation().getBlockZ());
/*     */ 
/*  53 */     this.x2 = Math.max(b1.getLocation().getBlockX(), b2.getLocation().getBlockX());
/*  54 */     this.y2 = Math.max(b1.getLocation().getBlockY(), b2.getLocation().getBlockY());
/*  55 */     this.z2 = Math.max(b1.getLocation().getBlockZ(), b2.getLocation().getBlockZ());
/*     */ 
/*  57 */     this.name = n;
/*     */   }
/*     */ 
/*     */   public Region(String n, Block b1, int radius) {
/*  61 */     this.x1 = Math.min(b1.getLocation().getBlockX() - radius, b1.getLocation().getBlockX() + radius);
/*  62 */     this.y1 = Math.min(b1.getLocation().getBlockY() - radius, b1.getLocation().getBlockY() + radius);
/*  63 */     this.z1 = Math.min(b1.getLocation().getBlockZ() - radius, b1.getLocation().getBlockZ() + radius);
/*     */ 
/*  65 */     this.x2 = Math.max(b1.getLocation().getBlockX() - radius, b1.getLocation().getBlockX() + radius);
/*  66 */     this.y2 = Math.max(b1.getLocation().getBlockY() - radius, b1.getLocation().getBlockY() + radius);
/*  67 */     this.z2 = Math.max(b1.getLocation().getBlockZ() - radius, b1.getLocation().getBlockZ() + radius);
/*     */ 
/*  69 */     this.name = n;
/*     */   }
/*     */ 
/*     */   public Region(String n, Integer rx1, Integer ry1, Integer rz1, Integer rx2, Integer ry2, Integer rz2) {
/*  73 */     this.x1 = Math.min(rx1.intValue(), rx2.intValue());
/*  74 */     this.y1 = Math.min(ry1.intValue(), ry2.intValue());
/*  75 */     this.z1 = Math.min(rz1.intValue(), rz2.intValue());
/*     */ 
/*  77 */     this.x2 = Math.max(rx1.intValue(), rx2.intValue());
/*  78 */     this.y2 = Math.max(ry1.intValue(), ry2.intValue());
/*  79 */     this.z2 = Math.max(rz1.intValue(), rz2.intValue());
/*     */ 
/*  81 */     this.name = n;
/*     */   }
/*     */ 
/*     */   public boolean containsBlock(Block b) {
/*  85 */     int x = b.getLocation().getBlockX();
/*  86 */     int y = b.getLocation().getBlockY();
/*  87 */     int z = b.getLocation().getBlockZ();
/*     */ 
/*  89 */     return (x >= this.x1) && (x <= this.x2) && (y >= this.y1) && (y <= this.y2) && (z >= this.z1) && (z <= this.z2);
/*     */   }
/*     */ 
/*     */   public int[] getLocation() {
/*  93 */     int[] location = { this.x1, this.y1, this.z1, this.x2, this.y2, this.z2 };
/*  94 */     return location;
/*     */   }
/*     */ 
/*     */   public String getName() {
/*  98 */     return this.name;
/*     */   }
/*     */ 
/*     */   public boolean isNull() {
/* 102 */     return this.name == null;
/*     */   }
/*     */ }

/* Location:           /Volumes/ramdisk/temp/
 * Qualified Name:     me.hellfire212.MineralVein.Region
 * JD-Core Version:    0.6.0
 */