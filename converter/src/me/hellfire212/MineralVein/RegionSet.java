/*     */ package me.hellfire212.MineralVein;
/*     */ 
/*     */ import java.io.Serializable;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
import org.bukkit.block.Block;
/*     */ 
/*     */ public class RegionSet
/*     */   implements Serializable
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*     */   private ArrayList<Region> regionList;
/*     */   private ArrayList<RegionPoint> pointList;
/*     */ 
/*     */   public RegionSet()
/*     */   {
/*  19 */     this.regionList = new ArrayList<Region>();
/*  20 */     this.pointList = new ArrayList<RegionPoint>();
/*     */   }
/*     */ 
/*     */   public boolean addRegion(Region r) {
/*  24 */     Iterator<Region> itr = this.regionList.iterator();
/*     */ 
/*  26 */     while (itr.hasNext()) {
/*  27 */       Region currentRegion = itr.next();
/*  28 */       if (currentRegion.getName().equalsIgnoreCase(r.getName())) {
/*  29 */         return false;
/*     */       }
/*     */     }
/*     */ 
/*  33 */     this.regionList.add(r);
/*  34 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean removeRegion(String name) {
/*  38 */     Iterator<Region> itr = this.regionList.iterator();
/*     */ 
/*  40 */     while (itr.hasNext()) {
/*  41 */       Region currentRegion = itr.next();
/*  42 */       if (currentRegion.getName().equalsIgnoreCase(name)) {
/*  43 */         this.regionList.remove(currentRegion);
/*  44 */         return true;
/*     */       }
/*     */     }
/*  47 */     return false;
/*     */   }
/*     */ 
/*     */   public Region getRegion(String name) {
/*  51 */     Iterator<Region> itr = this.regionList.iterator();
/*     */ 
/*  53 */     while (itr.hasNext()) {
/*  54 */       Region currentRegion = itr.next();
/*  55 */       if (currentRegion.getName().equalsIgnoreCase(name)) {
/*  56 */         return currentRegion;
/*     */       }
/*     */     }
/*  59 */     return new Region();
/*     */   }
/*     */ 
/*     */   public boolean addPoint(RegionPoint p) {
/*  63 */     Iterator<RegionPoint> itr = this.pointList.iterator();
/*     */ 
/*  65 */     while (itr.hasNext()) {
/*  66 */       RegionPoint currentPoint = itr.next();
/*  67 */       if (currentPoint.getName().equalsIgnoreCase(p.getName())) {
/*  68 */         return false;
/*     */       }
/*     */     }
/*     */ 
/*  72 */     this.pointList.add(p);
/*  73 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean removePoint(String name) {
/*  77 */     Iterator<RegionPoint> itr = this.pointList.iterator();
/*     */ 
/*  79 */     while (itr.hasNext()) {
/*  80 */       RegionPoint currentPoint = itr.next();
/*  81 */       if (currentPoint.getName().equalsIgnoreCase(name)) {
/*  82 */         this.pointList.remove(currentPoint);
/*  83 */         return true;
/*     */       }
/*     */     }
/*  86 */     return false;
/*     */   }
/*     */ 
/*     */   public RegionPoint getPoint(String name) {
/*  90 */     Iterator<RegionPoint> itr = this.pointList.iterator();
/*     */ 
/*  92 */     while (itr.hasNext()) {
/*  93 */       RegionPoint currentPoint = itr.next();
/*  94 */       if (currentPoint.getName().equalsIgnoreCase(name)) {
/*  95 */         return currentPoint;
/*     */       }
/*     */     }
/*  98 */     return new RegionPoint();
/*     */   }
/*     */ 
/*     */   public boolean containsBlock(Block b) {
/* 102 */     Iterator<Region> itr = this.regionList.iterator();
/* 103 */     while (itr.hasNext()) {
/* 104 */       if (itr.next().containsBlock(b)) {
/* 105 */         return true;
/*     */       }
/*     */     }
/* 108 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean isEmpty() {
/* 112 */     return (this.regionList.isEmpty()) && (this.pointList.isEmpty());
/*     */   }
/*     */ 
/*     */   public ArrayList<String> getRegionNames() {
/* 116 */     ArrayList<String> regionNames = new ArrayList<String>();
/* 117 */     if (!this.regionList.isEmpty()) {
/* 118 */       Iterator<Region> itr = this.regionList.iterator();
/* 119 */       while (itr.hasNext()) {
/* 120 */         regionNames.add(itr.next().getName());
/*     */       }
/*     */     }
/* 123 */     return regionNames;
/*     */   }
/*     */ 
/*     */   public ArrayList<String> getPointNames() {
/* 127 */     ArrayList<String> pointNames = new ArrayList<String>();
/* 128 */     if (!this.pointList.isEmpty()) {
/* 129 */       Iterator<RegionPoint> itr = this.pointList.iterator();
/* 130 */       while (itr.hasNext()) {
/* 131 */         pointNames.add(itr.next().getName());
/*     */       }
/*     */     }
/* 134 */     return pointNames;
/*     */   }
/*     */ }

/* Location:           /Volumes/ramdisk/temp/
 * Qualified Name:     me.hellfire212.MineralVein.RegionSet
 * JD-Core Version:    0.6.0
 */