# Add any ProGuard configurations specific to this
# extension here.

-keep public class xyz.kumaraswamy.* {
    public *;
 }
-keeppackagenames gnu.kawa**, gnu.expr**

-optimizationpasses 4
-allowaccessmodification
-mergeinterfacesaggressively

-repackageclasses 'io/itoo/itoox/repack'
-flattenpackagehierarchy
-dontpreverify
