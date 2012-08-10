/*    */ package me.hellfire212.MineralVein;
/*    */ 
/*    */ import java.io.Serializable;
/*    */ 
/*    */ public class RegionPoint
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */   private int x;
/*    */   private int y;
/*    */   private int z;
/*    */   private String name;
/*    */ 
/*    */   public RegionPoint()
/*    */   {
/* 15 */     setX(0);
/* 16 */     setY(0);
/* 17 */     setZ(0);
/* 18 */     setName(null);
/*    */   }
/*    */ 
/*    */   public RegionPoint(String name, int x, int y, int z) {
/* 22 */     setX(x);
/* 23 */     setY(y);
/* 24 */     setZ(z);
/* 25 */     setName(name);
/*    */   }
/*    */ 
/*    */   public int getZ() {
/* 29 */     return this.z;
/*    */   }
/*    */ 
/*    */   public void setZ(int z) {
/* 33 */     this.z = z;
/*    */   }
/*    */ 
/*    */   public int getY() {
/* 37 */     return this.y;
/*    */   }
/*    */ 
/*    */   public void setY(int y) {
/* 41 */     this.y = y;
/*    */   }
/*    */ 
/*    */   public int getX() {
/* 45 */     return this.x;
/*    */   }
/*    */ 
/*    */   public void setX(int x) {
/* 49 */     this.x = x;
/*    */   }
/*    */ 
/*    */   public String getName() {
/* 53 */     return this.name;
/*    */   }
/*    */ 
/*    */   public void setName(String name) {
/* 57 */     this.name = name;
/*    */   }
/*    */ 
/*    */   public boolean isNull() {
/* 61 */     return this.name == null;
/*    */   }
/*    */ 
/*    */   public int[] getLocation() {
/* 65 */     int[] location = { this.x, this.y, this.z };
/* 66 */     return location;
/*    */   }
/*    */ }

/* Location:           /Volumes/ramdisk/temp/
 * Qualified Name:     me.hellfire212.MineralVein.RegionPoint
 * JD-Core Version:    0.6.0
 */